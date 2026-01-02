package com.Lyno.matchmindai.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Secure storage for the DeepSeek API key using Jetpack DataStore.
 * This class follows the "User-Managed Security" principle where the user
 * provides their own API key which is stored locally and encrypted.
 */
class ApiKeyStorage(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "api_key_store")
        private val API_KEY = stringPreferencesKey("deepseek_api_key")
        private val USE_LIVE_DATA = booleanPreferencesKey("use_live_data")
    }

    /**
     * Save the API key to secure storage.
     * @param key The DeepSeek API key to save
     */
    suspend fun saveKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[API_KEY] = key
        }
        // Debug log
        println("ApiKeyStorage: Key saved (length: ${key.length}, starts with sk-: ${key.startsWith("sk-")})")
    }

    /**
     * Get the API key as a Flow. Returns null if no key is stored.
     * @return Flow emitting the API key or null
     */
    fun getKey(): Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[API_KEY]
        }

    /**
     * Clear the stored API key.
     */
    suspend fun clearKey() {
        context.dataStore.edit { preferences ->
            preferences.remove(API_KEY)
        }
    }

    /**
     * Get all user preferences as a Flow.
     * @return Flow emitting UserPreferences
     */
    fun getPreferences(): Flow<com.Lyno.matchmindai.domain.model.UserPreferences> = context.dataStore.data
        .map { preferences ->
            com.Lyno.matchmindai.domain.model.UserPreferences(
                apiKey = preferences[API_KEY],
                useLiveData = preferences[USE_LIVE_DATA] ?: true // Default to true
            )
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
}
