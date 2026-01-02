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
     * Set the live data enabled preference.
     * @param enabled Whether live data scraping should be enabled
     */
    suspend fun setLiveDataEnabled(enabled: Boolean)
}
