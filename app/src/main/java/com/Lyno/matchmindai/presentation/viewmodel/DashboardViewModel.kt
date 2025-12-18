package com.Lyno.matchmindai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Lyno.matchmindai.data.mapper.MatchMapper
import com.Lyno.matchmindai.data.repository.ApiKeyMissingException
import com.Lyno.matchmindai.domain.model.MatchFixture
import com.Lyno.matchmindai.domain.model.MatchStatus
import com.Lyno.matchmindai.domain.repository.MatchRepository
import com.Lyno.matchmindai.domain.service.CuratedFeed
import com.Lyno.matchmindai.domain.service.MatchCuratorService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the Dashboard screen.
 */
sealed interface DashboardUiState {
    object Idle : DashboardUiState
    object Loading : DashboardUiState
    data class Success(
        val curatedFeed: CuratedFeed,
        val isLoading: Boolean = false
    ) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
    object MissingApiKey : DashboardUiState
}

/**
 * ViewModel for the Dashboard screen.
 * Handles curated feed data with MatchCuratorService integration.
 */
class DashboardViewModel(
    private val matchRepository: MatchRepository,
    private val matchCuratorService: MatchCuratorService = MatchCuratorService()
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Idle)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // Curated feed state
    private val _curatedFeed = MutableStateFlow<CuratedFeed>(CuratedFeed())
    val curatedFeed: StateFlow<CuratedFeed> = _curatedFeed.asStateFlow()

    // Standings cache for curator service (empty for now - would need proper standings model)
    private val _standings = MutableStateFlow<List<Any>>(emptyList())
    val standings: StateFlow<List<Any>> = _standings.asStateFlow()

    init {
        // Start auto-refresh loop for curated feed
        startCuratedFeedAutoRefresh()
    }

    /**
     * Start the auto-refresh loop for curated feed.
     * Checks every 90 seconds for updates.
     */
    private fun startCuratedFeedAutoRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(90_000) // 90 seconds
                
                // Only refresh if we're in a success state (not loading or error)
                when (val currentState = _uiState.value) {
                    is DashboardUiState.Success -> {
                        // Refresh curated feed
                        loadCuratedFeed()
                    }
                    is DashboardUiState.Idle -> {
                        // Initial load
                        loadCuratedFeed()
                    }
                    else -> {
                        // Don't refresh if we're in loading or error state
                    }
                }
            }
        }
    }

    /**
     * Load curated feed from repository and process with MatchCuratorService.
     */
    fun loadCuratedFeed() {
        viewModelScope.launch {
            _uiState.update { DashboardUiState.Loading }
            
            try {
                // Fetch fixtures
                val fixturesResult = matchRepository.getTodaysMatches()
                
                fixturesResult.fold(
                    onSuccess = { fixtures ->
                        // For now, use empty standings since getStandings() returns AgentResponse
                        // In a real implementation, we would need to parse standings from AgentResponse
                        val curated = matchCuratorService.curateMatches(
                            fixtures = fixtures,
                            standings = emptyList()
                        )
                        
                        _curatedFeed.update { curated }
                        
                        // Update UI state
                        _uiState.update {
                            DashboardUiState.Success(
                                curatedFeed = curated,
                                isLoading = false
                            )
                        }
                    },
                    onFailure = { error ->
                        // Handle specific errors
                        when (error) {
                            is ApiKeyMissingException -> {
                                _uiState.update { DashboardUiState.MissingApiKey }
                            }
                            else -> {
                                _uiState.update { 
                                    DashboardUiState.Error(
                                        error.message ?: "Kon wedstrijden niet ophalen"
                                    ) 
                                }
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { 
                    DashboardUiState.Error(
                        e.message ?: "Onverwachte fout bij laden feed"
                    ) 
                }
            }
        }
    }

    /**
     * Refresh live matches specifically (for manual refresh).
     */
    fun refreshLiveMatches() {
        viewModelScope.launch {
            val result = matchRepository.getLiveMatches()
            
            result.fold(
                onSuccess = { fixtures ->
                    // Filter for truly live matches using MatchMapper.mapStatus
                    val liveFixtures = fixtures.filter { fixture ->
                        val matchStatus = MatchMapper.mapStatus(fixture.status)
                        matchStatus == MatchStatus.LIVE
                    }
                    
                    // Update curated feed with new live matches
                    val currentFeed = _curatedFeed.value
                    val updatedFeed = currentFeed.copy(
                        liveMatches = liveFixtures
                    )
                    
                    _curatedFeed.update { updatedFeed }
                    
                    // Update UI state if we're in success state
                    if (_uiState.value is DashboardUiState.Success) {
                        _uiState.update {
                            DashboardUiState.Success(
                                curatedFeed = updatedFeed,
                                isLoading = false
                            )
                        }
                    }
                },
                onFailure = { error ->
                    // Don't update UI state for live match refresh failures
                    // Just log the error
                    println("Live match refresh failed: ${error.message}")
                }
            )
        }
    }

    /**
     * Handle match selection from the dashboard.
     */
    fun onMatchSelected(fixture: MatchFixture) {
        // TODO: Navigate to match details or trigger analysis
        // For now, just log the selection
        println("Match selected: ${fixture.homeTeam} vs ${fixture.awayTeam}")
    }

    /**
     * Reset the UI state to Idle.
     */
    fun resetState() {
        _uiState.update { DashboardUiState.Idle }
    }

    /**
     * Clear any error messages.
     */
    fun clearError() {
        if (_uiState.value is DashboardUiState.Error) {
            _uiState.update { DashboardUiState.Idle }
        }
    }

    /**
     * Check if a match has event coverage for timeline display.
     */
    fun hasEventCoverage(fixture: MatchFixture): Boolean {
        // TODO: Implement based on fixture.coverage object
        // For now, return false
        return false
    }

    /**
     * Get recent events for a match (for timeline display).
     */
    fun getRecentEvents(fixture: MatchFixture): List<String> {
        // TODO: Implement based on fixture.events or API
        // For now, return empty list
        return emptyList()
    }
}
