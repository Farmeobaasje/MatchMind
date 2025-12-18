package com.Lyno.matchmindai.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.Lyno.matchmindai.domain.model.AnalysisMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Secure storage for user preferences using Jetpack DataStore.
 * This class follows the "User-Managed Security" principle where the user
 * provides their own API keys which are stored locally and encrypted.
 */
class ApiKeyStorage(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "api_key_store")
        private val DEEPSEEK_API_KEY = stringPreferencesKey("deepseek_api_key")
        private val TAVILY_API_KEY = stringPreferencesKey("tavily_api_key")
        private val API_SPORTS_KEY = stringPreferencesKey("api_sports_key")
        private val RAPID_API_KEY = stringPreferencesKey("rapid_api_key") // Backward compatibility
        private val USE_LIVE_DATA = booleanPreferencesKey("use_live_data")
        private val CREATIVITY = floatPreferencesKey("creativity")
        private val ANALYSIS_MODE = stringPreferencesKey("analysis_mode")
        private val LAST_FIXTURE_UPDATE = stringPreferencesKey("last_fixture_update")
        // Phase 4: Rate Limit Tracking
        private val API_CALLS_REMAINING = intPreferencesKey("api_calls_remaining")
        private val API_CALLS_LIMIT = intPreferencesKey("api_calls_limit")
        private val LAST_RATE_LIMIT_UPDATE = stringPreferencesKey("last_rate_limit_update")
        // Phase 4: Favorite Team Personalization
        private val FAVORITE_TEAM_ID = stringPreferencesKey("favorite_team_id")
        private val FAVORITE_TEAM_NAME = stringPreferencesKey("favorite_team_name")
        // Phase 4: Data Saver Mode
        private val LIVE_DATA_SAVER = booleanPreferencesKey("live_data_saver")
    }

    /**
     * Save all user preferences to secure storage.
     */
    suspend fun savePreferences(preferences: com.Lyno.matchmindai.domain.model.UserPreferences) {
        context.dataStore.edit { data ->
            data[DEEPSEEK_API_KEY] = preferences.deepSeekApiKey
            data[TAVILY_API_KEY] = preferences.tavilyApiKey
            data[API_SPORTS_KEY] = preferences.apiSportsKey
            // Keep RAPID_API_KEY for backward compatibility during migration
            data[RAPID_API_KEY] = preferences.apiSportsKey
            data[USE_LIVE_DATA] = preferences.useLiveData
            data[CREATIVITY] = preferences.creativity
            data[ANALYSIS_MODE] = preferences.analysisMode.name
            data[LAST_FIXTURE_UPDATE] = preferences.lastFixtureUpdate.toString()
            // Phase 4: Rate Limit Tracking
            data[API_CALLS_REMAINING] = preferences.apiCallsRemaining
            data[API_CALLS_LIMIT] = preferences.apiCallsLimit
            data[LAST_RATE_LIMIT_UPDATE] = preferences.lastRateLimitUpdate.toString()
            // Phase 4: Favorite Team Personalization
            preferences.favoriteTeamId?.let { data[FAVORITE_TEAM_ID] = it }
            preferences.favoriteTeamName?.let { data[FAVORITE_TEAM_NAME] = it }
            // Phase 4: Data Saver Mode
            data[LIVE_DATA_SAVER] = preferences.liveDataSaver
        }
        println("ApiKeyStorage: Preferences saved")
    }

    /**
     * Get all user preferences as a Flow.
     * @return Flow emitting UserPreferences
     */
    fun getPreferences(): Flow<com.Lyno.matchmindai.domain.model.UserPreferences> = context.dataStore.data
        .map { preferences ->
            // Try to get API-Sports key from new field, fall back to old field for migration
            val apiSportsKey = preferences[API_SPORTS_KEY] ?: preferences[RAPID_API_KEY] ?: ""
            
            com.Lyno.matchmindai.domain.model.UserPreferences(
                deepSeekApiKey = preferences[DEEPSEEK_API_KEY] ?: "",
                tavilyApiKey = preferences[TAVILY_API_KEY] ?: "",
                apiSportsKey = apiSportsKey,
                useLiveData = preferences[USE_LIVE_DATA] ?: true,
                creativity = preferences[CREATIVITY] ?: 0.5f,
                analysisMode = try {
                    AnalysisMode.valueOf(preferences[ANALYSIS_MODE] ?: "TURBO")
                } catch (e: IllegalArgumentException) {
                    AnalysisMode.TURBO
                },
                lastFixtureUpdate = preferences[LAST_FIXTURE_UPDATE]?.toLongOrNull() ?: 0L,
                // Phase 4: Rate Limit Tracking
                apiCallsRemaining = preferences[API_CALLS_REMAINING] ?: 100,
                apiCallsLimit = preferences[API_CALLS_LIMIT] ?: 100,
                lastRateLimitUpdate = preferences[LAST_RATE_LIMIT_UPDATE]?.toLongOrNull() ?: 0L,
                // Phase 4: Favorite Team Personalization
                favoriteTeamId = preferences[FAVORITE_TEAM_ID],
                favoriteTeamName = preferences[FAVORITE_TEAM_NAME],
                // Phase 4: Data Saver Mode
                liveDataSaver = preferences[LIVE_DATA_SAVER] ?: false
            )
        }

    /**
     * Save only the DeepSeek API key (for backward compatibility).
     * @param key The DeepSeek API key to save
     */
    suspend fun saveDeepSeekKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[DEEPSEEK_API_KEY] = key
        }
        println("ApiKeyStorage: DeepSeek key saved (length: ${key.length}, starts with sk-: ${key.startsWith("sk-")})")
    }

    /**
     * Save only the Tavily API key.
     * @param key The Tavily API key to save
     */
    suspend fun saveTavilyKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[TAVILY_API_KEY] = key
        }
        println("ApiKeyStorage: Tavily key saved")
    }

    /**
     * Save only the API-Sports key.
     * @param key The API-Sports key to save
     */
    suspend fun saveApiSportsKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[API_SPORTS_KEY] = key
            // Also save to old field for backward compatibility
            preferences[RAPID_API_KEY] = key
        }
        println("ApiKeyStorage: API-Sports key saved")
    }

    /**
     * Save only the RapidAPI key (for backward compatibility).
     * @param key The RapidAPI key to save
     */
    suspend fun saveRapidApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[RAPID_API_KEY] = key
            // Also save to new field for migration
            preferences[API_SPORTS_KEY] = key
        }
        println("ApiKeyStorage: RapidAPI key saved (migrated to API-Sports)")
    }

    /**
     * Set the live data enabled preference.
     * @param enabled Whether live data scraping should be enabled
     */
    suspend fun setLiveDataEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_LIVE_DATA] = enabled
        }
        println("ApiKeyStorage: Live data enabled set to $enabled")
    }

    /**
     * Set the creativity level.
     * @param creativity Creativity level between 0.0 and 1.0
     */
    suspend fun setCreativity(creativity: Float) {
        context.dataStore.edit { preferences ->
            preferences[CREATIVITY] = creativity.coerceIn(0.0f, 1.0f)
        }
        println("ApiKeyStorage: Creativity set to $creativity")
    }

    /**
     * Set the analysis mode.
     * @param mode The analysis mode to use
     */
    suspend fun setAnalysisMode(mode: AnalysisMode) {
        context.dataStore.edit { preferences ->
            preferences[ANALYSIS_MODE] = mode.name
        }
        println("ApiKeyStorage: Analysis mode set to $mode")
    }

    /**
     * Clear all stored preferences.
     */
    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
        println("ApiKeyStorage: All preferences cleared")
    }

    /**
     * Migrate old API key to new structure (for backward compatibility).
     */
    suspend fun migrateOldApiKey() {
        context.dataStore.edit { preferences ->
            val oldKey = preferences[stringPreferencesKey("deepseek_api_key")]
            if (oldKey != null && oldKey.isNotEmpty()) {
                preferences[DEEPSEEK_API_KEY] = oldKey
                // Don't remove old key yet for safety
            }
        }
        println("ApiKeyStorage: Migration check completed")
    }
}
