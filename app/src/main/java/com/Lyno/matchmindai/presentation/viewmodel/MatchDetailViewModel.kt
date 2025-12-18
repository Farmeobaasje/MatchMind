package com.Lyno.matchmindai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Lyno.matchmindai.common.Resource
import com.Lyno.matchmindai.domain.model.Injury
import com.Lyno.matchmindai.domain.model.MatchDetail
import com.Lyno.matchmindai.domain.model.MatchPredictionData
import com.Lyno.matchmindai.domain.model.OddsData
import com.Lyno.matchmindai.domain.repository.MatchRepository
import com.Lyno.matchmindai.domain.service.MatchCacheManager
import com.Lyno.matchmindai.domain.service.MatchDetailCacheData
import com.Lyno.matchmindai.domain.service.CacheUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the Match Detail screen.
 * Sealed interface for different states in the match detail flow.
 */
sealed interface MatchDetailUiState {
    object Loading : MatchDetailUiState
    data class Success(val matchDetail: MatchDetail) : MatchDetailUiState
    data class Error(val message: String) : MatchDetailUiState
    object NoDataAvailable : MatchDetailUiState
}

/**
 * ViewModel for the Match Detail screen.
 * Handles match detail requests and state management.
 * Accepts fixtureId as constructor parameter instead of SavedStateHandle.
 * Enhanced with smart caching for AI agent integration.
 */
class MatchDetailViewModel(
    private val fixtureId: Int,
    private val matchRepository: MatchRepository,
    private val matchCacheManager: MatchCacheManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<MatchDetailUiState>(MatchDetailUiState.Loading)
    val uiState: StateFlow<MatchDetailUiState> = _uiState.asStateFlow()

    // AI Analysis state
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    // Injuries state
    private val _injuries = MutableStateFlow<List<Injury>>(emptyList())
    val injuries: StateFlow<List<Injury>> = _injuries.asStateFlow()
    private val _isLoadingInjuries = MutableStateFlow(false)
    val isLoadingInjuries: StateFlow<Boolean> = _isLoadingInjuries.asStateFlow()

    // Prediction state
    private val _prediction = MutableStateFlow<MatchPredictionData?>(null)
    val prediction: StateFlow<MatchPredictionData?> = _prediction.asStateFlow()
    private val _isLoadingPrediction = MutableStateFlow(false)
    val isLoadingPrediction: StateFlow<Boolean> = _isLoadingPrediction.asStateFlow()

    // Odds state
    private val _odds = MutableStateFlow<OddsData?>(null)
    val odds: StateFlow<OddsData?> = _odds.asStateFlow()
    private val _isLoadingOdds = MutableStateFlow(false)
    val isLoadingOdds: StateFlow<Boolean> = _isLoadingOdds.asStateFlow()

    init {
        if (fixtureId > 0) {
            loadMatchDetails()
        } else {
            _uiState.update { 
                MatchDetailUiState.Error("Geen geldige wedstrijd ID ontvangen") 
            }
        }
    }

    /**
     * Load match details from repository using Resource pattern.
     */
    private fun loadMatchDetails() {
        viewModelScope.launch {
            matchRepository.getMatchDetails(fixtureId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { MatchDetailUiState.Loading }
                    }
                    is Resource.Success -> {
                        val matchDetail = resource.data
                        // Check if we have valid fixture ID
                        if (matchDetail.fixtureId == 0) {
                            _uiState.update { MatchDetailUiState.NoDataAvailable }
                        } else {
                            // Always show match details even if stats/lineups/events are empty
                            // This handles matches that haven't started yet
                            _uiState.update { MatchDetailUiState.Success(matchDetail) }
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { 
                            MatchDetailUiState.Error(resource.message ?: "Onbekende fout opgetreden") 
                        }
                    }
                }
            }
        }
    }

    /**
     * Refresh match details.
     */
    fun refresh() {
        _uiState.update { MatchDetailUiState.Loading }
        loadMatchDetails()
    }

    /**
     * Trigger AI analysis for the current match.
     * This prepares the match data and navigates to chat screen.
     * @return Simple string with match context for AI analysis
     */
    fun prepareAiAnalysis(): String {
        val currentState = _uiState.value
        return when (currentState) {
            is MatchDetailUiState.Success -> {
                val matchDetail = currentState.matchDetail
                buildAiAnalysisContext(matchDetail)
            }
            else -> {
                "Geen wedstrijdgegevens beschikbaar voor analyse"
            }
        }
    }

    /**
     * Build AI analysis context from match details.
     * Creates a simple, human-readable prompt string for AI consumption.
     */
    private fun buildAiAnalysisContext(matchDetail: MatchDetail): String {
        val homeTeam = matchDetail.homeTeam
        val awayTeam = matchDetail.awayTeam
        val league = matchDetail.league
        val score = matchDetail.score?.let { "${it.home ?: 0}-${it.away ?: 0}" } ?: "0-0"
        val status = matchDetail.status?.displayName ?: "Onbekend"
        
        // Get key statistics for analysis
        val keyStats = matchDetail.stats.take(3).joinToString(", ") { stat ->
            "${stat.type}: ${stat.homeValue}${stat.unit} vs ${stat.awayValue}${stat.unit}"
        }
        
        // Get key events for analysis
        val keyEvents = matchDetail.events.take(3).joinToString(", ") { event ->
            "${event.minute}' ${event.type.name}: ${event.player ?: "Onbekende speler"}"
        }
        
        return "Analyseer de wedstrijd $homeTeam vs $awayTeam in de $league. " +
               "Stand: $score. Status: $status. " +
               "Belangrijkste statistieken: $keyStats. " +
               "Belangrijkste gebeurtenissen: $keyEvents. " +
               "Geef een gedetailleerde analyse van de wedstrijdverloop en tactiek."
    }

    /**
     * Load injuries for the match.
     */
    fun loadInjuries(fixtureId: Int) {
        viewModelScope.launch {
            _isLoadingInjuries.update { true }
            try {
                val result = matchRepository.getInjuries(fixtureId)
                if (result.isSuccess) {
                    _injuries.update { result.getOrNull() ?: emptyList() }
                } else {
                    // Keep empty list on error
                    _injuries.update { emptyList() }
                }
            } catch (e: Exception) {
                _injuries.update { emptyList() }
            } finally {
                _isLoadingInjuries.update { false }
            }
        }
    }

    /**
     * Load prediction for the match.
     */
    fun loadPrediction(fixtureId: Int) {
        viewModelScope.launch {
            _isLoadingPrediction.update { true }
            try {
                val result = matchRepository.getPredictions(fixtureId)
                if (result.isSuccess) {
                    _prediction.update { result.getOrNull() }
                } else {
                    // Keep null on error
                    _prediction.update { null }
                }
            } catch (e: Exception) {
                _prediction.update { null }
            } finally {
                _isLoadingPrediction.update { false }
            }
        }
    }

    /**
     * Load odds for the match.
     */
    fun loadOdds(fixtureId: Int) {
        viewModelScope.launch {
            _isLoadingOdds.update { true }
            try {
                val result = matchRepository.getOdds(fixtureId)
                if (result.isSuccess) {
                    _odds.update { result.getOrNull() }
                } else {
                    // Keep null on error
                    _odds.update { null }
                }
            } catch (e: Exception) {
                _odds.update { null }
            } finally {
                _isLoadingOdds.update { false }
            }
        }
    }

    /**
     * Set analyzing state.
     */
    fun setAnalyzing(isAnalyzing: Boolean) {
        _isAnalyzing.update { isAnalyzing }
    }

    /**
     * Handle error state.
     */
    fun clearError() {
        if (_uiState.value is MatchDetailUiState.Error) {
            _uiState.update { MatchDetailUiState.Loading }
            loadMatchDetails()
        }
    }

    /**
     * Cache match data for AI agent usage.
     * This is called when match details are successfully loaded.
     */
    private suspend fun cacheMatchData(matchDetail: MatchDetail) {
        try {
            val cacheData = MatchDetailCacheData(
                fixtureId = fixtureId,
                matchDetail = matchDetail,
                predictions = _prediction.value,
                injuries = _injuries.value,
                odds = _odds.value,
                lastUpdated = System.currentTimeMillis(),
                cacheExpiry = System.currentTimeMillis() + CacheUtils.determineTTL(matchDetail)
            )
            
            matchCacheManager.cacheMatchData(fixtureId, cacheData)
        } catch (e: Exception) {
            // Log but don't fail - caching is optional
            println("Failed to cache match data: ${e.message}")
        }
    }

    /**
     * Check if cached data is available for this match.
     */
    suspend fun hasCachedData(): Boolean {
        return matchCacheManager.getCachedMatchData(fixtureId) != null
    }

    /**
     * Get cache status for this match.
     */
    suspend fun getCacheStatus(): com.Lyno.matchmindai.domain.service.CacheStatus {
        return matchCacheManager.getCacheStatus(fixtureId)
    }

    /**
     * Update cache with fresh data.
     * This should be called after loading predictions, injuries, or odds.
     */
    suspend fun updateCache() {
        val currentState = _uiState.value
        if (currentState is MatchDetailUiState.Success) {
            cacheMatchData(currentState.matchDetail)
        }
    }

    /**
     * Load match details with cache-first strategy.
     * Checks cache first, then falls back to network if needed.
     */
    private fun loadMatchDetailsWithCache() {
        viewModelScope.launch {
            // First check cache
            val cachedData = matchCacheManager.getCachedMatchData(fixtureId)
            if (cachedData != null && cachedData.isFresh) {
                // Use cached data
                _uiState.update { MatchDetailUiState.Success(cachedData.matchDetail) }
                _prediction.update { cachedData.predictions }
                _injuries.update { cachedData.injuries }
                _odds.update { cachedData.odds }
                return@launch
            }

            // If cache is stale or missing, load from network
            loadMatchDetails()
        }
    }

    /**
     * Smart refresh that updates cache after loading.
     */
    fun smartRefresh() {
        viewModelScope.launch {
            _uiState.update { MatchDetailUiState.Loading }
            
            // Load fresh data
            matchRepository.getMatchDetails(fixtureId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val matchDetail = resource.data
                        if (matchDetail.fixtureId != 0) {
                            _uiState.update { MatchDetailUiState.Success(matchDetail) }
                            // Cache the fresh data
                            cacheMatchData(matchDetail)
                        } else {
                            _uiState.update { MatchDetailUiState.NoDataAvailable }
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { 
                            MatchDetailUiState.Error(resource.message ?: "Onbekende fout opgetreden") 
                        }
                    }
                    else -> {
                        // Loading state already set
                    }
                }
            }
        }
    }
}
