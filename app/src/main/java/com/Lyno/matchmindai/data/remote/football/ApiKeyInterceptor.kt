package com.Lyno.matchmindai.data.remote.football

import com.Lyno.matchmindai.BuildConfig
import com.Lyno.matchmindai.data.local.ApiKeyStorage
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.headers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Simple interceptor for API-Sports HTTP client that dynamically retrieves the API key
 * from ApiKeyStorage (user-managed) with BuildConfig fallback for development.
 * 
 * This follows the "User-Managed Security" principle from .clinerules:
 * - Primary: User-provided key from ApiKeyStorage (DataStore)
 * - Fallback: BuildConfig.API_SPORTS_KEY (developer convenience)
 * - Error: If both are empty, request will fail with 403 Forbidden
 */
class ApiKeyInterceptor(
    private val apiKeyStorage: ApiKeyStorage
) {
    /**
     * Intercept the HTTP request and add the API-Sports key header.
     * This is designed to be used with Ktor's defaultRequest configuration.
     */
    fun intercept(builder: HttpRequestBuilder) {
        // Get the API key - use runBlocking since this is called from non-suspend context
        val apiKey = runBlocking {
            val preferencesFlow = apiKeyStorage.getPreferences()
            val preferences = preferencesFlow.first()
            preferences.apiSportsKey.trim()
        }
        
        // Use user-provided key if available, otherwise fallback to BuildConfig
        val finalApiKey = if (apiKey.isNotBlank()) {
            apiKey.trim()
        } else {
            BuildConfig.API_SPORTS_KEY.trim()
        }
        
        // Add headers directly
        builder.headers.remove("x-apisports-key")
        builder.headers.append("x-apisports-key", finalApiKey.trim())
        builder.headers.append("Accept", "application/json")
        builder.headers.append("Accept-Charset", "UTF-8")
        builder.headers.append("Content-Type", "application/json")
    }
    
    /**
     * Get the current API key for logging/debugging purposes.
     * @return The API key being used (masked for security)
     */
    suspend fun getCurrentApiKey(): String {
        val preferences = apiKeyStorage.getPreferences().first()
        val userKey = preferences.apiSportsKey
        
        return if (userKey.isNotBlank()) {
            "User key: ${userKey.take(5)}..." // Mask for security
        } else {
            "BuildConfig key: ${BuildConfig.API_SPORTS_KEY.take(5)}..."
        }
    }
}
