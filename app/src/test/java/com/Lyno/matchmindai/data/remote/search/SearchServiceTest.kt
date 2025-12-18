package com.Lyno.matchmindai.data.remote.search

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for SearchService class.
 * Tests error handling and graceful degradation when Tavily API calls fail.
 */
class SearchServiceTest {

    private val mockJson = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private fun createMockHttpClient(responseBody: String, statusCode: HttpStatusCode = HttpStatusCode.OK): HttpClient {
        return HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    respond(
                        content = responseBody,
                        status = statusCode,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    )
                }
            }
            install(ContentNegotiation) {
                json(mockJson)
            }
        }
    }

    @Test
    fun `searchContext returns fallback string on network error`() = runBlocking {
        // Arrange
        val mockHttpClient = createMockHttpClient(
            """{"error": "API key invalid"}""",
            HttpStatusCode.Unauthorized
        )
        val tavilyApi = TavilyApi(mockHttpClient)
        val searchService = SearchService(tavilyApi, "invalid_api_key")

        // Act
        val result = searchService.searchContext("Ajax", "Feyenoord", "general")

        // Assert
        // The search service should return a fallback string, not crash
        assertNotNull("Result should not be null", result)
        assertTrue("Result should contain fallback message", result.contains("Geen live data gevonden"))
    }

    @Test
    fun `searchContext handles empty team names gracefully`() = runBlocking {
        // Arrange
        val mockHttpClient = createMockHttpClient(
            """{"answer": "No results", "results": []}"""
        )
        val tavilyApi = TavilyApi(mockHttpClient)
        val searchService = SearchService(tavilyApi, "test_api_key")

        // Act
        val result = searchService.searchContext("", "", "general")

        // Assert
        // Should handle empty strings without crashing
        assertNotNull("Result should not be null", result)
        assertTrue("Result should be a string", result is String)
        assertTrue("Result should not be empty", result.isNotEmpty())
    }

    @Test
    fun `searchContext handles special characters in team names`() = runBlocking {
        // Arrange
        val mockHttpClient = createMockHttpClient(
            """{"answer": "Test results", "results": [{"title": "Test", "url": "https://example.com", "content": "Test content"}]}"""
        )
        val tavilyApi = TavilyApi(mockHttpClient)
        val searchService = SearchService(tavilyApi, "test_api_key")

        // Act
        val result = searchService.searchContext("Team A & B", "Team C/D", "general")

        // Assert
        // Should handle special characters without crashing
        assertNotNull("Result should not be null", result)
        assertTrue("Result should be a string", result is String)
        assertTrue("Result should not be empty", result.isNotEmpty())
    }

    @Test
    fun `searchWithQuery returns fallback on API error`() = runBlocking {
        // Arrange
        val mockHttpClient = createMockHttpClient(
            """{"error": "Rate limit exceeded"}""",
            HttpStatusCode.TooManyRequests
        )
        val tavilyApi = TavilyApi(mockHttpClient)
        val searchService = SearchService(tavilyApi, "test_api_key")

        // Act
        val result = searchService.searchWithQuery("Ajax vs Feyenoord 2024", "stats")

        // Assert
        assertNotNull("Result should not be null", result)
        assertTrue("Result should contain fallback message", result.contains("Geen live data gevonden"))
    }

    @Test
    fun `searchWithQuery handles empty query gracefully`() = runBlocking {
        // Arrange
        val mockHttpClient = createMockHttpClient(
            """{"answer": "No query provided", "results": []}"""
        )
        val tavilyApi = TavilyApi(mockHttpClient)
        val searchService = SearchService(tavilyApi, "test_api_key")

        // Act
        val result = searchService.searchWithQuery("", "general")

        // Assert
        assertNotNull("Result should not be null", result)
        assertTrue("Result should be a string", result is String)
        assertTrue("Result should not be empty", result.isNotEmpty())
    }

    @Test
    fun `extractUrlsFromResponse extracts URLs correctly`() {
        // Arrange
        val response = TavilyResponse(
            answer = "Test answer",
            results = listOf(
                TavilyResult(
                    title = "Test Result 1",
                    url = "https://example.com/1",
                    content = "Test content 1"
                ),
                TavilyResult(
                    title = "Test Result 2",
                    url = "https://example.com/2",
                    content = "Test content 2"
                )
            )
        )
        val searchService = SearchService(TavilyApi(createMockHttpClient("{}")), "test_api_key")

        // Act
        val urls = searchService.extractUrlsFromResponse(response)

        // Assert
        assertEquals("Should extract 2 URLs", 2, urls.size)
        assertTrue("Should contain first URL", urls.contains("https://example.com/1"))
        assertTrue("Should contain second URL", urls.contains("https://example.com/2"))
    }

    @Test
    fun `searchContext with stats focus builds enhanced query`() = runBlocking {
        // Arrange
        var capturedRequest: String? = null
        val mockHttpClient = HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    capturedRequest = request.body.toByteArray().decodeToString()
                    respond(
                        content = """{"answer": "Stats results", "results": []}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    )
                }
            }
            install(ContentNegotiation) {
                json(mockJson)
            }
        }
        val tavilyApi = TavilyApi(mockHttpClient)
        val searchService = SearchService(tavilyApi, "test_api_key")

        // Act
        searchService.searchContext("Ajax", "Feyenoord", "stats")

        // Assert
        assertNotNull("Request should be captured", capturedRequest)
        assertTrue("Request should contain stats enhancement", 
            capturedRequest?.contains("statistics site:flashscore.com OR site:transfermarkt.nl") == true)
    }

    @Test
    fun `searchContext with news focus sets news topic`() = runBlocking {
        // Arrange
        var capturedRequest: String? = null
        val mockHttpClient = HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    capturedRequest = request.body.toByteArray().decodeToString()
                    respond(
                        content = """{"answer": "News results", "results": []}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    )
                }
            }
            install(ContentNegotiation) {
                json(mockJson)
            }
        }
        val tavilyApi = TavilyApi(mockHttpClient)
        val searchService = SearchService(tavilyApi, "test_api_key")

        // Act
        searchService.searchContext("Ajax", "Feyenoord", "news")

        // Assert
        assertNotNull("Request should be captured", capturedRequest)
        assertTrue("Request should contain news topic", 
            capturedRequest?.contains("\"topic\":\"news\"") == true)
    }
}
