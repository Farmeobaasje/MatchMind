package com.Lyno.matchmindai.data.remote

import com.Lyno.matchmindai.data.dto.DeepSeekRequest
import com.Lyno.matchmindai.data.dto.DeepSeekResponse
import com.Lyno.matchmindai.data.dto.DeepSeekRequestFactory
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

/**
 * Ktor-based HTTP client for DeepSeek API.
 * Implements dynamic authentication where the API key is passed as a parameter.
 */
class DeepSeekApi(private val client: HttpClient) {

    companion object {
        private const val BASE_URL = "https://api.deepseek.com"
        private const val CHAT_ENDPOINT = "/chat/completions"
        private const val AUTH_HEADER = "Authorization"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        prettyPrint = true
    }

    /**
     * Sends a prediction request to DeepSeek API.
     * @param apiKey The user's DeepSeek API key (dynamic authentication)
     * @param request The prediction request
     * @return DeepSeekResponse from the API
     * @throws DeepSeekApiException if the request fails
     */
    suspend fun getPrediction(
        apiKey: String,
        request: DeepSeekRequest
    ): DeepSeekResponse {
        return try {
            val response: HttpResponse = client.post("$BASE_URL$CHAT_ENDPOINT") {
                header(AUTH_HEADER, "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (response.status.value in 200..299) {
                response.body()
            } else {
                throw DeepSeekApiException(
                    "API request failed with status ${response.status.value}",
                    response.status.value
                )
            }
        } catch (e: Exception) {
            throw DeepSeekApiException(
                "Network error: ${e.message}",
                cause = e
            )
        }
    }

    /**
     * Simplified method to get a match prediction.
     * @param apiKey The user's DeepSeek API key
     * @param homeTeam The home team name
     * @param awayTeam The away team name
     * @return DeepSeekResponse containing the prediction
     */
    suspend fun predictMatch(
        apiKey: String,
        homeTeam: String,
        awayTeam: String
    ): DeepSeekResponse {
        val request = DeepSeekRequestFactory.createMatchPredictionRequest(homeTeam, awayTeam)
        return getPrediction(apiKey, request)
    }
}

/**
 * Custom exception for DeepSeek API errors.
 */
class DeepSeekApiException(
    message: String,
    val statusCode: Int? = null,
    cause: Throwable? = null
) : Exception(message, cause)
