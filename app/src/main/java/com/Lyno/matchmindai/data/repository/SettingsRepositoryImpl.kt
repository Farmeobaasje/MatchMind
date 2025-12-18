package com.Lyno.matchmindai.data.repository

import com.Lyno.matchmindai.data.local.ApiKeyStorage
import com.Lyno.matchmindai.domain.model.UserPreferences
import com.Lyno.matchmindai.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Implementation of SettingsRepository that uses ApiKeyStorage for persistence.
 */
class SettingsRepositoryImpl(
    private val apiKeyStorage: ApiKeyStorage
) : SettingsRepository {

    override suspend fun saveApiKey(key: String) {
        apiKeyStorage.saveDeepSeekKey(key)
    }

    override fun getApiKey(): Flow<String?> {
        return apiKeyStorage.getPreferences().map { preferences ->
            preferences.deepSeekApiKey
        }
    }

    override fun hasApiKey(): Flow<Boolean> {
        return apiKeyStorage.getPreferences().map { preferences ->
            preferences.deepSeekApiKey.isNotBlank()
        }
    }

    override suspend fun clearApiKey() {
        apiKeyStorage.clearAll()
    }

    override fun getPreferences(): Flow<UserPreferences> {
        return apiKeyStorage.getPreferences()
    }

    override fun getSettings(): Flow<UserPreferences> {
        return apiKeyStorage.getPreferences()
    }

    override suspend fun setLiveDataEnabled(enabled: Boolean) {
        apiKeyStorage.setLiveDataEnabled(enabled)
    }

    override suspend fun setDeepSeekApiKey(key: String) {
        apiKeyStorage.saveDeepSeekKey(key)
    }

    override suspend fun setTavilyApiKey(key: String) {
        apiKeyStorage.saveTavilyKey(key)
    }

    override suspend fun setCreativity(creativity: Float) {
        apiKeyStorage.setCreativity(creativity)
    }

    override suspend fun setAnalysisMode(mode: com.Lyno.matchmindai.domain.model.AnalysisMode) {
        apiKeyStorage.setAnalysisMode(mode)
    }

    override suspend fun setRapidApiKey(key: String) {
        apiKeyStorage.saveRapidApiKey(key)
    }

    /**
     * Set the API-Sports key (new method for API-Sports Direct subscription).
     */
    suspend fun setApiSportsKey(key: String) {
        apiKeyStorage.saveApiSportsKey(key)
    }

    override suspend fun updateLastFixtureUpdate(timestamp: Long) {
        // TODO: Implement this method in ApiKeyStorage
        // For now, we'll update the preferences directly
        val currentPrefs = apiKeyStorage.getPreferences().first()
        val updatedPrefs = currentPrefs.copy(lastFixtureUpdate = timestamp)
        apiKeyStorage.savePreferences(updatedPrefs)
    }

    override suspend fun updateApiRateLimits(remaining: Int, limit: Int) {
        val currentPrefs = apiKeyStorage.getPreferences().first()
        val updatedPrefs = currentPrefs.copy(
            apiCallsRemaining = remaining,
            apiCallsLimit = limit,
            lastRateLimitUpdate = System.currentTimeMillis()
        )
        apiKeyStorage.savePreferences(updatedPrefs)
    }

    override suspend fun setFavoriteTeam(teamId: String, teamName: String) {
        val currentPrefs = apiKeyStorage.getPreferences().first()
        val updatedPrefs = currentPrefs.copy(
            favoriteTeamId = teamId,
            favoriteTeamName = teamName
        )
        apiKeyStorage.savePreferences(updatedPrefs)
    }

    override suspend fun clearFavoriteTeam() {
        val currentPrefs = apiKeyStorage.getPreferences().first()
        val updatedPrefs = currentPrefs.copy(
            favoriteTeamId = null,
            favoriteTeamName = null
        )
        apiKeyStorage.savePreferences(updatedPrefs)
    }

    override suspend fun setLiveDataSaver(enabled: Boolean) {
        val currentPrefs = apiKeyStorage.getPreferences().first()
        val updatedPrefs = currentPrefs.copy(liveDataSaver = enabled)
        apiKeyStorage.savePreferences(updatedPrefs)
    }
}
