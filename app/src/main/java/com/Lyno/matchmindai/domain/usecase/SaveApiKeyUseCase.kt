package com.Lyno.matchmindai.domain.usecase

import com.Lyno.matchmindai.domain.repository.SettingsRepository

/**
 * Use case for saving the API key.
 * Simple wrapper around the SettingsRepository save operation.
 */
class SaveApiKeyUseCase(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Save the API key to secure storage.
     * @param key The DeepSeek API key to save
     */
    suspend operator fun invoke(key: String) {
        settingsRepository.saveApiKey(key)
    }
}
