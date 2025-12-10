package com.Lyno.matchmindai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Lyno.matchmindai.data.repository.ApiKeyMissingException
import com.Lyno.matchmindai.domain.model.MatchPrediction
import com.Lyno.matchmindai.domain.usecase.GetPredictionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the Match prediction screen.
 * Sealed interface for different states in the prediction flow.
 */
sealed interface MatchUiState {
    object Idle : MatchUiState
    object Loading : MatchUiState
    data class Success(val prediction: MatchPrediction) : MatchUiState
    data class Error(val message: String) : MatchUiState
    object MissingApiKey : MatchUiState
}

/**
 * ViewModel for the Match prediction screen.
 * Handles match prediction requests and state management.
 */
class MatchViewModel(
    private val getPredictionUseCase: GetPredictionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<MatchUiState>(MatchUiState.Idle)
    val uiState: StateFlow<MatchUiState> = _uiState.asStateFlow()

    // Input state for home and away teams
    private val _homeTeam = MutableStateFlow("")
    val homeTeam: StateFlow<String> = _homeTeam.asStateFlow()

    private val _awayTeam = MutableStateFlow("")
    val awayTeam: StateFlow<String> = _awayTeam.asStateFlow()

    /**
     * Update home team input.
     */
    fun updateHomeTeam(text: String) {
        _homeTeam.update { text }
    }

    /**
     * Update away team input.
     */
    fun updateAwayTeam(text: String) {
        _awayTeam.update { text }
    }

    /**
     * Get a prediction for a match between two teams.
     * Uses the current input values from state flows.
     */
    fun predictMatch() {
        val home = _homeTeam.value
        val away = _awayTeam.value
        
        if (home.isBlank() || away.isBlank()) {
            _uiState.update { 
                MatchUiState.Error("Voer beide teamnamen in") 
            }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { MatchUiState.Loading }
            
            val result = getPredictionUseCase(home, away)
            
            result.fold(
                onSuccess = { prediction ->
                    _uiState.update { MatchUiState.Success(prediction) }
                },
                onFailure = { error ->
                    when (error) {
                        is ApiKeyMissingException -> {
                            _uiState.update { MatchUiState.MissingApiKey }
                        }
                        is IllegalArgumentException -> {
                            _uiState.update { 
                                MatchUiState.Error(error.message ?: "Invalid input") 
                            }
                        }
                        else -> {
                            _uiState.update { 
                                MatchUiState.Error(
                                    error.message ?: "Failed to get prediction"
                                ) 
                            }
                        }
                    }
                }
            )
        }
    }

    /**
     * Reset the UI state to Idle.
     */
    fun resetState() {
        _uiState.update { MatchUiState.Idle }
    }

    /**
     * Clear any error messages.
     */
    fun clearError() {
        if (_uiState.value is MatchUiState.Error) {
            _uiState.update { MatchUiState.Idle }
        }
    }
}
