package com.Lyno.matchmindai.domain.model

/**
 * Data class representing user preferences.
 * This is a domain model that should be independent of any data layer implementation.
 * 
 * HYBRID ENGINE UPDATE:
 * - Added lastFixtureUpdate timestamp for cache management
 * - Changed rapidApiKey to apiSportsKey for API-Sports Direct subscription
 * 
 * PHASE 4 UPDATE:
 * - Added rate limit tracking for API-Sports
 * - Added favorite team personalization
 * - Added data saver mode for live data
 */
data class UserPreferences(
    val deepSeekApiKey: String = "",
    val tavilyApiKey: String = "",
    val apiSportsKey: String = "",
    val useLiveData: Boolean = true,
    val creativity: Float = 0.5f,
    val analysisMode: AnalysisMode = AnalysisMode.TURBO,
    val lastFixtureUpdate: Long = 0L,
    // Phase 4: Rate Limit Tracking
    val apiCallsRemaining: Int = 100, // Default to 100 calls
    val apiCallsLimit: Int = 100, // Default to 100 calls limit
    val lastRateLimitUpdate: Long = 0L,
    // Phase 4: Favorite Team Personalization
    val favoriteTeamId: String? = null,
    val favoriteTeamName: String? = null,
    // Phase 4: Data Saver Mode
    val liveDataSaver: Boolean = false
) {
    /**
     * Backward compatibility: rapidApiKey property for migration
     */
    val rapidApiKey: String
        get() = apiSportsKey
    
    /**
     * Calculate API usage percentage (0-100)
     */
    val apiUsagePercentage: Int
        get() = if (apiCallsLimit > 0) {
            ((apiCallsRemaining.toFloat() / apiCallsLimit.toFloat()) * 100).toInt()
        } else {
            100
        }
    
    /**
     * Check if API usage is critical (< 10%)
     */
    val isApiUsageCritical: Boolean
        get() = apiUsagePercentage < 10
    
    /**
     * Check if API usage is warning (< 25%)
     */
    val isApiUsageWarning: Boolean
        get() = apiUsagePercentage < 25
}
