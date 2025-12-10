package com.Lyno.matchmindai.domain.usecase

import com.Lyno.matchmindai.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for retrieving the API key.
 * Simple wrapper around the SettingsRepository get operation.
 */
class GetApiKeyUseCase(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Get the API key as a Flow.
     * @return Flow emitting the API key or null if not set
     */
    operator fun invoke(): Flow<String?> {
        return settingsRepository.getApiKey()
    }
}
