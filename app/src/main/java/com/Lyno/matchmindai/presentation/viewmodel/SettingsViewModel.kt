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
    val apiKey: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val keySaved: Boolean = false,
    val isLiveDataEnabled: Boolean = true
)

/**
 * ViewModel for the Settings screen.
 * Handles API key management and state updates.
 */
class SettingsViewModel(
    private val saveApiKeyUseCase: SaveApiKeyUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // Subscribe to user preferences updates
        viewModelScope.launch {
            settingsRepository.getPreferences().collectLatest { preferences ->
                _uiState.update { state ->
                    state.copy(
                        isLiveDataEnabled = preferences.useLiveData,
                        apiKey = preferences.apiKey ?: state.apiKey
                    )
                }
            }
        }
    }

    /**
     * Update API key input.
     */
    fun updateApiKey(text: String) {
        _uiState.update { it.copy(apiKey = text) }
    }

    /**
     * Save the API key to secure storage.
     * Uses the current input value from state.
     * DeepSeek API keys should start with "sk-".
     */
    fun saveApiKey() {
        var key = _uiState.value.apiKey.trim()
        
        if (key.isBlank()) {
            _uiState.update { 
                it.copy(error = "Voer een API key in") 
            }
            return
        }
        
        // Ensure the key starts with "sk-" if not already
        if (!key.startsWith("sk-")) {
            key = "sk-$key"
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                saveApiKeyUseCase(key)
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        keySaved = true,
                        error = null,
                        apiKey = key // Update the displayed key with the corrected version
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Kon API key niet opslaan: ${e.message ?: "Onbekende fout"}",
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
}
