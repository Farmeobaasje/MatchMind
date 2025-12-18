package com.Lyno.matchmindai.data.remote.search

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Tavily AI Search API client for retrieving high-quality search results optimized for RAG.
 * Replaces the unstable Jsoup-based web scraper with a professional search API.
 *
 * Tavily provides better context for AI agents and includes features like:
 * - Search depth control (basic/advanced)
 * - Topic filtering (general/news)
 * - Built-in answer generation
 * - Trusted sources filtering
 */
class TavilyApi(private val httpClient: HttpClient) {

    companion object {
        private const val TAG = "TavilyApi"
        private const val BASE_URL = "https://api.tavily.com"
        private const val SEARCH_ENDPOINT = "$BASE_URL/search"
    }

    /**
     * Search for information using Tavily AI Search API.
     *
     * @param query The search query
     * @param focus The type of information to focus on: "stats" for statistics/scores,
     *              "news" for news/injuries, "general" for mixed results
     * @param apiKey The Tavily API key (should be retrieved from BuildConfig or local storage)
     * @return TavilyResponse containing search results and AI-generated answer
     */
    suspend fun search(
        query: String,
        focus: String = "general",
        apiKey: String
    ): Result<TavilyResponse> {
        return try {
            // Build Tavily request with focus-based parameters
            val request = buildTavilyRequest(query, focus, apiKey)
            
            Log.d(TAG, "Searching Tavily with query: '$query', focus: '$focus'")
            
            // Make API call
            val response: TavilyResponse = httpClient.post(SEARCH_ENDPOINT) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
            
            Log.d(TAG, "Tavily search successful: ${response.results.size} results")
            Result.success(response)
            
        } catch (e: Exception) {
            Log.e(TAG, "Tavily API error for query: '$query', focus: '$focus'", e)
            Result.failure(Exception("Tavily search failed: ${e.message}"))
        }
    }

    /**
     * Builds a TavilyRequest based on the query and focus parameter.
     * Implements focus logic: news topic for news focus, enhanced query for stats focus.
     */
    private fun buildTavilyRequest(
        query: String,
        focus: String,
        apiKey: String
    ): TavilyRequest {
        // Enhance query based on focus
        val enhancedQuery = when (focus) {
            "stats" -> "$query statistics site:flashscore.com OR site:transfermarkt.nl"
            else -> query
        }
        
        // Set topic based on focus
        val topic = when (focus) {
            "news" -> "news"
            else -> "general"
        }
        
        return TavilyRequest(
            api_key = apiKey,
            query = enhancedQuery,
            search_depth = "advanced", // Advanced for deeper analysis as per report
            topic = topic,
            max_results = 5,
            include_answer = false // Disabled to reduce tokens as per report
        )
    }
}

/**
 * Request DTO for Tavily Search API.
 */
@Serializable
data class TavilyRequest(
    @SerialName("api_key")
    val api_key: String,

    val query: String,

    @SerialName("search_depth")
    val search_depth: String = "advanced", // Default to advanced as recommended

    val topic: String = "news", // Default to news for football context

    @SerialName("max_results")
    val max_results: Int = 5,

    @SerialName("include_answer")
    val include_answer: Boolean = false // Disabled to reduce token usage
)

/**
 * Response DTO from Tavily Search API.
 */
@Serializable
data class TavilyResponse(
    val answer: String? = null, // CRUCIAL: Make nullable! API sends null when include_answer=false
    val results: List<TavilyResult>,
    
    @SerialName("query")
    val originalQuery: String? = null,
    
    @SerialName("response_time")
    val responseTime: Double? = null
)

/**
 * Individual search result from Tavily.
 */
@Serializable
data class TavilyResult(
    val title: String,
    val url: String,
    val content: String,
    val score: Double? = null,
    
    @SerialName("published_date")
    val publishedDate: String? = null,
    
    @SerialName("raw_content")
    val rawContent: String? = null // For safety, make nullable
)

/**
 * Exception thrown when Tavily API fails.
 */
class TavilyApiException(message: String) : Exception(message)
