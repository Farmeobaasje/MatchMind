package com.Lyno.matchmindai.data.remote.football

import com.Lyno.matchmindai.data.local.ApiKeyStorage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Simple interceptor for API-Sports HTTP client that dynamically retrieves the API key
 * from ApiKeyStorage (user-managed).
 * 
 * This follows the "User-Managed Security" principle from .clinerules:
 * - Primary: User-provided key from ApiKeyStorage (DataStore)
 * - Fallback: Empty string (will cause 403 Forbidden if no key is provided)
 */
class SimpleApiKeyInterceptor(
    private val apiKeyStorage: ApiKeyStorage
) {
    /**
     * Get the current API key from user storage.
     * @return The API key being used
     */
    fun getApiKey(): String {
        return runBlocking {
            val preferencesFlow = apiKeyStorage.getPreferences()
            val preferences = preferencesFlow.first()
            preferences.apiSportsKey.trim()
        }
    }
    
    /**
     * Get the current API key for logging/debugging purposes.
     * @return The API key being used (masked for security)
     */
    suspend fun getCurrentApiKeyForLogging(): String {
        val preferences = apiKeyStorage.getPreferences().first()
        val userKey = preferences.apiSportsKey
        
        return if (userKey.isNotBlank()) {
            "User key: ${userKey.take(5)}..." // Mask for security
        } else {
            "No API key configured"
        }
    }
}
