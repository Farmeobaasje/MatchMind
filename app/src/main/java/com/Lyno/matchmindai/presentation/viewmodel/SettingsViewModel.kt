        package com.Lyno.matchmindai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Lyno.matchmindai.domain.repository.SettingsRepository
import com.Lyno.matchmindai.domain.usecase.SaveApiKeyUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the Settings screen.
 */
data class SettingsUiState(
    val deepSeekApiKey: String = "",
    val tavilyApiKey: String = "",
    val apiSportsKey: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val keySaved: Boolean = false,
    val isLiveDataEnabled: Boolean = true,
    val creativity: Float = 0.7f,
    val analysisMode: com.Lyno.matchmindai.domain.model.AnalysisMode = com.Lyno.matchmindai.domain.model.AnalysisMode.BALANCED,
    // Phase 4: Control Room Fields
    val apiCallsRemaining: Int = 100,
    val apiCallsLimit: Int = 100,
    val lastRateLimitUpdate: Long = 0L,
    val favoriteTeamId: String? = null,
    val favoriteTeamName: String? = null,
    val liveDataSaver: Boolean = false,
    // Cache confirmation dialog
    val showCacheConfirmation: Boolean = false
)

/**
 * ViewModel for the Settings screen.
 * Handles API key management and state updates.
 */
class SettingsViewModel(
    private val saveApiKeyUseCase: SaveApiKeyUseCase,
    private val settingsRepository: SettingsRepository,
    private val matchRepository: com.Lyno.matchmindai.domain.repository.MatchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // Subscribe to user preferences updates
        viewModelScope.launch {
            settingsRepository.getSettings().collectLatest { settings ->
                _uiState.update { state ->
                    state.copy(
                        deepSeekApiKey = settings.deepSeekApiKey,
                        tavilyApiKey = settings.tavilyApiKey,
                        apiSportsKey = settings.apiSportsKey,
                        isLiveDataEnabled = settings.useLiveData,
                        creativity = settings.creativity,
                        analysisMode = settings.analysisMode,
                        // Phase 4: Control Room Fields
                        apiCallsRemaining = settings.apiCallsRemaining,
                        apiCallsLimit = settings.apiCallsLimit,
                        lastRateLimitUpdate = settings.lastRateLimitUpdate,
                        favoriteTeamId = settings.favoriteTeamId,
                        favoriteTeamName = settings.favoriteTeamName,
                        liveDataSaver = settings.liveDataSaver
                    )
                }
            }
        }
    }

    /**
     * Update DeepSeek API key input.
     */
    fun updateDeepSeekApiKey(text: String) {
        _uiState.update { it.copy(deepSeekApiKey = text) }
    }

    /**
     * Update Tavily API key input.
     */
    fun updateTavilyApiKey(text: String) {
        _uiState.update { it.copy(tavilyApiKey = text) }
    }

    /**
     * Update API-Sports key input.
     */
    fun updateApiSportsKey(text: String) {
        _uiState.update { it.copy(apiSportsKey = text) }
    }

    /**
     * Save the API keys to secure storage.
     */
    fun saveApiKeys() {
        val deepSeekKey = _uiState.value.deepSeekApiKey.trim()
        val tavilyKey = _uiState.value.tavilyApiKey.trim()
        val apiSportsKey = _uiState.value.apiSportsKey.trim()
        
        if (deepSeekKey.isBlank()) {
            _uiState.update { 
                it.copy(error = "Voer een DeepSeek API key in") 
            }
            return
        }
        
        var correctedDeepSeekKey = deepSeekKey
        // Ensure the DeepSeek key starts with "sk-" if not already
        if (!correctedDeepSeekKey.startsWith("sk-")) {
            correctedDeepSeekKey = "sk-$correctedDeepSeekKey"
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Save all API keys
                settingsRepository.setDeepSeekApiKey(correctedDeepSeekKey)
                settingsRepository.setTavilyApiKey(tavilyKey)
                settingsRepository.setRapidApiKey(apiSportsKey) // Uses backward compatibility method
                
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        keySaved = true,
                        error = null,
                        deepSeekApiKey = correctedDeepSeekKey // Update the displayed key with the corrected version
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Kon API keys niet opslaan: ${e.message ?: "Onbekende fout"}",
                        keySaved = false
                    ) 
                }
            }
        }
    }

    /**
     * Clear any error messages.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Reset the key saved state.
     */
    fun resetKeySaved() {
        _uiState.update { it.copy(keySaved = false) }
    }

    /**
     * Toggle live data scraping enabled/disabled.
     * @param enabled Whether live data scraping should be enabled
     */
    fun toggleLiveData(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setLiveDataEnabled(enabled)
            // UI state will be updated automatically via the flow in init
        }
    }

    /**
     * Update creativity level.
     * @param creativity Creativity value between 0.0 and 1.0
     */
    fun updateCreativity(creativity: Float) {
        viewModelScope.launch {
            settingsRepository.setCreativity(creativity)
            // UI state will be updated automatically via the flow in init
        }
    }

    /**
     * Update analysis mode.
     * @param mode The analysis mode to use
     */
    fun updateAnalysisMode(mode: com.Lyno.matchmindai.domain.model.AnalysisMode) {
        viewModelScope.launch {
            settingsRepository.setAnalysisMode(mode)
            // UI state will be updated automatically via the flow in init
        }
    }

    /**
     * Update favorite team.
     * @param teamId The team ID (e.g., "88" for Ajax)
     * @param teamName The team name (e.g., "Ajax")
     */
    fun updateFavoriteTeam(teamId: String, teamName: String) {
        viewModelScope.launch {
            settingsRepository.setFavoriteTeam(teamId, teamName)
            // UI state will be updated automatically via the flow in init
        }
    }

    /**
     * Clear favorite team.
     */
    fun clearFavoriteTeam() {
        viewModelScope.launch {
            settingsRepository.clearFavoriteTeam()
            // UI state will be updated automatically via the flow in init
        }
    }

    /**
     * Toggle data saver mode.
     * @param enabled Whether data saver mode should be enabled
     */
    fun toggleDataSaver(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setLiveDataSaver(enabled)
            // UI state will be updated automatically via the flow in init
        }
    }

    /**
     * Show cache confirmation dialog.
     */
    fun showCacheConfirmation() {
        _uiState.update { it.copy(showCacheConfirmation = true) }
    }

    /**
     * Hide cache confirmation dialog.
     */
    fun hideCacheConfirmation() {
        _uiState.update { it.copy(showCacheConfirmation = false) }
    }

    /**
     * Clear all in-memory caches for predictions, injuries, odds, and match details.
     * This forces the next API calls to fetch fresh data instead of using cached data.
     */
    fun clearAllCache() {
        // Hide confirmation dialog first
        hideCacheConfirmation()
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = matchRepository.clearCache()
                if (result.isSuccess) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = null
                        ) 
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Kon cache niet wissen: ${result.exceptionOrNull()?.message ?: "Onbekende fout"}"
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Fout bij wissen cache: ${e.message ?: "Onbekende fout"}"
                    ) 
                }
            }
        }
    }
}
