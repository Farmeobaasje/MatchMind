package com.Lyno.matchmindai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Lyno.matchmindai.common.Resource
import com.Lyno.matchmindai.domain.model.KaptigunAnalysis
import com.Lyno.matchmindai.domain.repository.KaptigunRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for Kaptigun Performance Analysis Dashboard.
 * Manages the state and business logic for the analysis tab.
 */
class KaptigunViewModel(
    private val kaptigunRepository: KaptigunRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(KaptigunUiState())
    val uiState: StateFlow<KaptigunUiState> = _uiState.asStateFlow()

    /**
     * Load Kaptigun analysis for a specific fixture.
     */
    fun loadAnalysis(fixtureId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Get analysis flow from repository
                kaptigunRepository.getAnalysisFlow(fixtureId).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            _uiState.update { it.copy(isLoading = true, error = null) }
                        }
                        is Resource.Success -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = null,
                                    analysis = resource.data,
                                    lastUpdated = System.currentTimeMillis()
                                )
                            }
                        }
                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = resource.message,
                                    analysis = null
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Fout bij laden analyse: ${e.message}",
                        analysis = null
                    )
                }
            }
        }
    }

    /**
     * Refresh the analysis data.
     */
    fun refreshAnalysis(fixtureId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            
            try {
                // Clear cache and reload
                kaptigunRepository.clearAnalysisCache()
                loadAnalysis(fixtureId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        error = "Vernieuwen mislukt: ${e.message}"
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
     * Get cached analysis if available.
     */
    fun getCachedAnalysis(fixtureId: Int): KaptigunAnalysis? {
        return _uiState.value.analysis
    }
}

/**
 * UI state for Kaptigun analysis screen.
 */
data class KaptigunUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val analysis: KaptigunAnalysis? = null,
    val lastUpdated: Long = 0L
) {
    /**
     * Check if data is stale (older than 5 minutes).
     */
    val isDataStale: Boolean
        get() = lastUpdated > 0 && System.currentTimeMillis() - lastUpdated > 5 * 60 * 1000

    /**
     * Format last updated time for display.
     */
    val lastUpdatedFormatted: String
        get() = if (lastUpdated > 0) {
            val minutesAgo = (System.currentTimeMillis() - lastUpdated) / (60 * 1000)
            when {
                minutesAgo < 1 -> "Zojuist"
                minutesAgo == 1L -> "1 minuut geleden"
                minutesAgo < 60 -> "$minutesAgo minuten geleden"
                else -> {
                    val hoursAgo = minutesAgo / 60
                    if (hoursAgo == 1L) "1 uur geleden" else "$hoursAgo uur geleden"
                }
            }
        } else {
            "Nooit"
        }
}
