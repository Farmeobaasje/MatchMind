package com.Lyno.matchmindai.domain.repository

import com.Lyno.matchmindai.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

/**
 * Interface for API key management operations.
 * Domain layer contract for settings-related operations.
 */
interface SettingsRepository {
    /**
     * Save the API key to secure storage.
     * @param key The DeepSeek API key to save
     */
    suspend fun saveApiKey(key: String)

    /**
     * Get the API key as a Flow.
     * @return Flow emitting the API key or null if not set
     */
    fun getApiKey(): Flow<String?>

    /**
     * Check if an API key is currently saved.
     * @return Flow emitting true if an API key is saved, false otherwise
     */
    fun hasApiKey(): Flow<Boolean>

    /**
     * Clear the saved API key.
     */
    suspend fun clearApiKey()

    /**
     * Get all user preferences as a Flow.
     * @return Flow emitting UserPreferences
     */
    fun getPreferences(): Flow<UserPreferences>

    /**
     * Get all user settings as a Flow.
     * @return Flow emitting UserPreferences
     */
    fun getSettings(): Flow<UserPreferences>

    /**
     * Set the live data enabled preference.
     * @param enabled Whether live data scraping should be enabled
     */
    suspend fun setLiveDataEnabled(enabled: Boolean)

    /**
     * Set the DeepSeek API key.
     * @param key The DeepSeek API key to save
     */
    suspend fun setDeepSeekApiKey(key: String)

    /**
     * Set the Tavily API key.
     * @param key The Tavily API key to save
     */
    suspend fun setTavilyApiKey(key: String)

    /**
     * Set the creativity level.
     * @param creativity Creativity level between 0.0 and 1.0
     */
    suspend fun setCreativity(creativity: Float)

    /**
     * Set the analysis mode.
     * @param mode The analysis mode to use
     */
    suspend fun setAnalysisMode(mode: com.Lyno.matchmindai.domain.model.AnalysisMode)

    /**
     * Set the RapidAPI key.
     * @param key The RapidAPI key to save
     */
    suspend fun setRapidApiKey(key: String)

    /**
     * Update the last fixture update timestamp.
     * @param timestamp The timestamp when fixtures were last updated
     */
    suspend fun updateLastFixtureUpdate(timestamp: Long)

    /**
     * Update API rate limit information.
     * @param remaining Number of API calls remaining
     * @param limit Total API call limit
     */
    suspend fun updateApiRateLimits(remaining: Int, limit: Int)

    /**
     * Set the user's favorite team.
     * @param teamId The team ID from API-Sports
     * @param teamName The team name for display
     */
    suspend fun setFavoriteTeam(teamId: String, teamName: String)

    /**
     * Clear the user's favorite team.
     */
    suspend fun clearFavoriteTeam()

    /**
     * Set the live data saver mode.
     * @param enabled Whether live data saver mode is enabled
     */
    suspend fun setLiveDataSaver(enabled: Boolean)
}
