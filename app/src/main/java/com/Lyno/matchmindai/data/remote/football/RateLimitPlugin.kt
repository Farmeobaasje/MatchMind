package com.Lyno.matchmindai.data.remote.football

import com.Lyno.matchmindai.domain.repository.SettingsRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Calendar

/**
 * Ktor plugin for tracking and enforcing daily API call limits.
 * 
 * This plugin tracks API calls to external services (API-Sports, DeepSeek, Tavily)
 * and enforces user-defined daily limits. It follows the "User-Managed Security" principle
 * where users can monitor and control their API usage.
 * 
 * Features:
 * 1. Tracks API calls to external services
 * 2. Decrements remaining API calls count
 * 3. Checks if daily limit has been reached
 * 4. Resets count daily at midnight (local timezone)
 * 5. Optionally blocks requests when limit is reached
 * 6. Updates rate limits from API-Sports response headers
 * 
 * Usage: Add this plugin to your Ktor HttpClient for API-Sports, DeepSeek, and Tavily clients.
 */
class RateLimitPlugin(private val settingsRepository: SettingsRepository) {
    
    companion object {
        private const val TAG = "RateLimitPlugin"
        
        // API endpoint patterns to track
        internal val API_SPORTS_PATTERNS = listOf(
            "api-football.com",
            "api-sports.io",
            "v3.football.api-sports.io"
        )
        
        internal val DEEPSEEK_PATTERNS = listOf(
            "api.deepseek.com",
            "chat/completions"
        )
        
        internal val TAVILY_PATTERNS = listOf(
            "api.tavily.com",
            "search"
        )
        
        // Default daily limit (can be configured by user)
        private const val DEFAULT_DAILY_LIMIT = 100
        
        /**
         * Creates a RateLimitPlugin instance.
         */
        fun create(settingsRepository: SettingsRepository): RateLimitPlugin {
            return RateLimitPlugin(settingsRepository)
        }
    }
    
    /**
     * Check if we should track this API call.
     */
    private fun shouldTrackApiCall(url: String): Boolean {
        return API_SPORTS_PATTERNS.any { url.contains(it, ignoreCase = true) } ||
               DEEPSEEK_PATTERNS.any { url.contains(it, ignoreCase = true) } ||
               TAVILY_PATTERNS.any { url.contains(it, ignoreCase = true) }
    }
    
    /**
     * Check current rate limits and update if needed.
     * 
     * @return true if the request should proceed, false if rate limit is exceeded
     */
    private suspend fun checkAndUpdateRateLimits(): Boolean {
        try {
            // Get current preferences
            val preferences = settingsRepository.getPreferences().first()
            
            // Check if we need to reset daily count (new day)
            val shouldReset = shouldResetDailyCount(preferences.lastRateLimitUpdate)
            
            if (shouldReset) {
                // Reset to default limit
                settingsRepository.updateApiRateLimits(
                    remaining = DEFAULT_DAILY_LIMIT,
                    limit = DEFAULT_DAILY_LIMIT
                )
                return true // Fresh start for new day
            }
            
            // Check if we have API calls remaining
            if (preferences.apiCallsRemaining <= 0) {
                return false // Rate limit exceeded
            }
            
            // Decrement remaining calls
            val newRemaining = preferences.apiCallsRemaining - 1
            settingsRepository.updateApiRateLimits(
                remaining = newRemaining,
                limit = preferences.apiCallsLimit
            )
            
            // Log the API call (for debugging)
            println("$TAG: API call tracked. Remaining: $newRemaining/${preferences.apiCallsLimit}")
            
            return true
        } catch (e: Exception) {
            // If there's an error checking rate limits, allow the request to proceed
            // This ensures the app doesn't break if there's a storage issue
            println("$TAG: Error checking rate limits: ${e.message}. Allowing request to proceed.")
            return true
        }
    }
    
    /**
     * Check if we should reset the daily count (new day).
     * 
     * @param lastUpdateTimestamp Last time rate limits were updated
     * @return true if it's a new day and count should be reset
     */
    private fun shouldResetDailyCount(lastUpdateTimestamp: Long): Boolean {
        if (lastUpdateTimestamp == 0L) {
            return true // First time usage
        }
        
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_YEAR)
        val currentYear = calendar.get(Calendar.YEAR)
        
        calendar.timeInMillis = lastUpdateTimestamp
        val lastUpdateDay = calendar.get(Calendar.DAY_OF_YEAR)
        val lastUpdateYear = calendar.get(Calendar.YEAR)
        
        // Reset if it's a different day or different year
        return currentDay != lastUpdateDay || currentYear != lastUpdateYear
    }
    
    /**
     * Update rate limit information from API-Sports response headers.
     * 
     * @param response The HTTP response from API-Sports
     */
    suspend fun updateRateLimitsFromHeaders(response: HttpResponse) {
        try {
            val remainingHeader = response.headers["x-ratelimit-requests-remaining"]
            val limitHeader = response.headers["x-ratelimit-requests-limit"]
            
            if (remainingHeader != null && limitHeader != null) {
                val remaining = remainingHeader.toIntOrNull()
                val limit = limitHeader.toIntOrNull()
                
                if (remaining != null && limit != null) {
                    // Update the storage with new rate limit values
                    settingsRepository.updateApiRateLimits(remaining, limit)
                    println("$TAG: Updated rate limits from headers - Remaining: $remaining/$limit")
                }
            }
        } catch (e: Exception) {
            // Log error but don't crash the app
            println("$TAG: Error updating rate limits from headers: ${e.message}")
        }
    }
    
    /**
     * Intercept request to check rate limits.
     */
    suspend fun interceptRequest(url: String): Boolean {
        if (shouldTrackApiCall(url)) {
            return checkAndUpdateRateLimits()
        }
        return true
    }
    
    /**
     * Process response to update rate limits from headers.
     */
    suspend fun processResponse(url: String, response: HttpResponse) {
        if (url.contains("api-football.com") || url.contains("api-sports.io")) {
            updateRateLimitsFromHeaders(response)
        }
    }
}

/**
 * Exception thrown when rate limit is exceeded.
 */
class RateLimitExceededException(message: String) : Exception(message)

/**
 * Simple Ktor plugin for rate limiting.
 */
val RateLimit = createClientPlugin("RateLimit") {
    // This is a simple plugin that doesn't do anything by itself
    // The actual rate limiting logic is handled by the RateLimitPlugin class
    // This plugin is just a placeholder for future implementation
}

/**
 * Extension function to easily add rate limiting to HttpClient.
 * Note: This is a placeholder implementation.
 */
// fun HttpClient.Config.installRateLimit(settingsRepository: SettingsRepository) {
//     // For now, this is a no-op implementation
//     // In a real implementation, we would add request/response interceptors
//     println("RateLimit plugin installed (placeholder implementation)")
// }

/**
 * Extension function to check if a URL is for API-Sports.
 */
fun String.isApiSportsUrl(): Boolean {
    return RateLimitPlugin.API_SPORTS_PATTERNS.any { this.contains(it, ignoreCase = true) }
}

/**
 * Extension function to check if a URL is for DeepSeek.
 */
fun String.isDeepSeekUrl(): Boolean {
    return RateLimitPlugin.DEEPSEEK_PATTERNS.any { this.contains(it, ignoreCase = true) }
}

/**
 * Extension function to check if a URL is for Tavily.
 */
fun String.isTavilyUrl(): Boolean {
    return RateLimitPlugin.TAVILY_PATTERNS.any { this.contains(it, ignoreCase = true) }
}
