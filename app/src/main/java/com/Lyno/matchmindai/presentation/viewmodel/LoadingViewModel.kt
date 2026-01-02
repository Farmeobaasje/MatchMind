package com.Lyno.matchmindai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Lyno.matchmindai.domain.model.Fact
import com.Lyno.matchmindai.domain.repository.FactRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for managing the loading screen state and facts.
 */
class LoadingViewModel(
    private val factRepository: FactRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoadingUiState())
    val uiState: StateFlow<LoadingUiState> = _uiState.asStateFlow()
    
    init {
        startFactStream()
    }
    
    /**
     * Start streaming facts for the loading screen.
     */
    private fun startFactStream() {
        viewModelScope.launch {
            factRepository.getFactStream(intervalMs = 3500).collect { fact ->
                _uiState.update { currentState ->
                    currentState.copy(
                        currentFact = fact,
                        factCount = currentState.factCount + 1
                    )
                }
            }
        }
    }
    
    /**
     * Update loading progress.
     */
    fun updateProgress(progress: Float) {
        _uiState.update { currentState ->
            currentState.copy(loadingProgress = progress)
        }
    }
    
    /**
     * Skip loading and mark as complete.
     */
    fun skipLoading() {
        _uiState.update { currentState ->
            currentState.copy(
                loadingProgress = 1f,
                isLoadingComplete = true
            )
        }
    }
    
    /**
     * Mark loading as complete.
     */
    fun completeLoading() {
        _uiState.update { currentState ->
            currentState.copy(isLoadingComplete = true)
        }
    }
    
    /**
     * Get a random fact (for testing/preview).
     */
    fun getRandomFact() {
        viewModelScope.launch {
            val fact = factRepository.getRandomFact()
            _uiState.update { currentState ->
                currentState.copy(currentFact = fact)
            }
        }
    }
}

/**
 * UI state for the loading screen.
 */
data class LoadingUiState(
    val currentFact: Fact? = null,
    val loadingProgress: Float = 0f,
    val isLoadingComplete: Boolean = false,
    val factCount: Int = 0,
    val errorMessage: String? = null
)
