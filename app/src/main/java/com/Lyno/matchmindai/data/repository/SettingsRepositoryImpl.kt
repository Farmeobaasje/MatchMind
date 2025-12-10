package com.Lyno.matchmindai.data.repository

import com.Lyno.matchmindai.data.local.ApiKeyStorage
import com.Lyno.matchmindai.domain.model.UserPreferences
import com.Lyno.matchmindai.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementation of SettingsRepository that uses ApiKeyStorage for persistence.
 */
class SettingsRepositoryImpl(
    private val apiKeyStorage: ApiKeyStorage
) : SettingsRepository {

    override suspend fun saveApiKey(key: String) {
        apiKeyStorage.saveKey(key)
    }

    override fun getApiKey(): Flow<String?> {
        return apiKeyStorage.getKey()
    }

    override fun hasApiKey(): Flow<Boolean> {
        return apiKeyStorage.getKey().map { key ->
            !key.isNullOrBlank()
        }
    }

    override suspend fun clearApiKey() {
        apiKeyStorage.clearKey()
    }

    override fun getPreferences(): Flow<UserPreferences> {
        return apiKeyStorage.getPreferences()
    }

    override suspend fun setLiveDataEnabled(enabled: Boolean) {
        apiKeyStorage.setLiveDataEnabled(enabled)
    }
}
