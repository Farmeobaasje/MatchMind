package com.Lyno.matchmindai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Lyno.matchmindai.common.Resource
import com.Lyno.matchmindai.domain.model.AiAnalysisResult
import com.Lyno.matchmindai.domain.model.HybridPrediction
import com.Lyno.matchmindai.domain.model.Injury
import com.Lyno.matchmindai.domain.model.LiveMatchData
import com.Lyno.matchmindai.domain.model.MatchDetail
import com.Lyno.matchmindai.domain.model.MatchPrediction
import com.Lyno.matchmindai.domain.model.MatchPredictionData
import com.Lyno.matchmindai.domain.model.MatchReport
import com.Lyno.matchmindai.domain.model.OddsData
import com.Lyno.matchmindai.domain.repository.MatchRepository
import com.Lyno.matchmindai.domain.service.MatchCacheManager
import com.Lyno.matchmindai.domain.service.MatchDetailCacheData
import com.Lyno.matchmindai.domain.service.MatchReportGenerator
import com.Lyno.matchmindai.domain.service.CacheUtils
import com.Lyno.matchmindai.domain.usecase.GetHybridPredictionUseCase
import com.Lyno.matchmindai.domain.usecase.MastermindAnalysisUseCase
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
    private val matchCacheManager: MatchCacheManager,
    private val getHybridPredictionUseCase: GetHybridPredictionUseCase,
    private val matchReportGenerator: MatchReportGenerator,
    private val mastermindAnalysisUseCase: MastermindAnalysisUseCase
) : ViewModel() {
    
    // Mastermind Analysis state
    private val _mastermindTip = MutableStateFlow<com.Lyno.matchmindai.domain.usecase.MastermindTip?>(null)
    val mastermindTip: StateFlow<com.Lyno.matchmindai.domain.usecase.MastermindTip?> = _mastermindTip.asStateFlow()
    private val _isLoadingMastermind = MutableStateFlow(false)
    val isLoadingMastermind: StateFlow<Boolean> = _isLoadingMastermind.asStateFlow()
    private val _mastermindError = MutableStateFlow<String?>(null)
    val mastermindError: StateFlow<String?> = _mastermindError.asStateFlow()

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

    // API Prediction state
    private val _prediction = MutableStateFlow<MatchPredictionData?>(null)
    val prediction: StateFlow<MatchPredictionData?> = _prediction.asStateFlow()
    private val _isLoadingPrediction = MutableStateFlow(false)
    val isLoadingPrediction: StateFlow<Boolean> = _isLoadingPrediction.asStateFlow()

    // Hybrid Prediction (Enhanced xG + AI) state
    private val _hybridPrediction = MutableStateFlow<HybridPrediction?>(null)
    val hybridPrediction: StateFlow<HybridPrediction?> = _hybridPrediction.asStateFlow()
    private val _isLoadingHybrid = MutableStateFlow(false)
    val isLoadingHybrid: StateFlow<Boolean> = _isLoadingHybrid.asStateFlow()
    private val _hybridError = MutableStateFlow<String?>(null)
    val hybridError: StateFlow<String?> = _hybridError.asStateFlow()
    
    // Match Report state (generated from hybrid prediction)
    private val _matchReport = MutableStateFlow<MatchReport?>(null)
    val matchReport: StateFlow<MatchReport?> = _matchReport.asStateFlow()
    private val _isGeneratingReport = MutableStateFlow(false)
    val isGeneratingReport: StateFlow<Boolean> = _isGeneratingReport.asStateFlow()
    private val _reportError = MutableStateFlow<String?>(null)
    val reportError: StateFlow<String?> = _reportError.asStateFlow()
    
    // Quick Metrics state (for Intel tab)
    private val _quickMetrics = MutableStateFlow<com.Lyno.matchmindai.domain.model.QuickMetrics?>(null)
    val quickMetrics: StateFlow<com.Lyno.matchmindai.domain.model.QuickMetrics?> = _quickMetrics.asStateFlow()
    private val _isGeneratingQuickMetrics = MutableStateFlow(false)
    val isGeneratingQuickMetrics: StateFlow<Boolean> = _isGeneratingQuickMetrics.asStateFlow()
    private val _quickMetricsError = MutableStateFlow<String?>(null)
    val quickMetricsError: StateFlow<String?> = _quickMetricsError.asStateFlow()

    // Odds state
    private val _odds = MutableStateFlow<OddsData?>(null)
    val odds: StateFlow<OddsData?> = _odds.asStateFlow()
    private val _isLoadingOdds = MutableStateFlow(false)
    val isLoadingOdds: StateFlow<Boolean> = _isLoadingOdds.asStateFlow()

    // Live match data state
    private val _liveMatchData = MutableStateFlow<Resource<LiveMatchData>>(Resource.Loading<LiveMatchData>())
    val liveMatchData: StateFlow<Resource<LiveMatchData>> = _liveMatchData.asStateFlow()
    private val _isRefreshingLiveData = MutableStateFlow(false)
    val isRefreshingLiveData: StateFlow<Boolean> = _isRefreshingLiveData.asStateFlow()
    private var pollingJob: kotlinx.coroutines.Job? = null

    init {
        if (fixtureId > 0) {
            loadMatchDetailsWithDependencies()
        } else {
            _uiState.update { 
                MatchDetailUiState.Error("Geen geldige wedstrijd ID ontvangen") 
            }
        }
    }
    
    /**
     * Load match details from repository.
     */
    private fun loadMatchDetails() {
        viewModelScope.launch {
            matchRepository.getMatchDetails(fixtureId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val matchDetail = resource.data
                        if (matchDetail.fixtureId != 0) {
                            _uiState.update { MatchDetailUiState.Success(matchDetail) }
                            // Cache the data
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
    
    /**
     * Load match details with all dependencies in parallel for optimal user flow.
     * This method loads match details first, then loads all related data (injuries,
     * predictions, odds) in parallel to provide a complete experience.
     * AI analysis (Hybrid Prediction) is NOT loaded automatically - user must trigger it manually.
     */
    private fun loadMatchDetailsWithDependencies() {
        viewModelScope.launch {
            matchRepository.getMatchDetails(fixtureId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val matchDetail = resource.data
                        if (matchDetail.fixtureId != 0) {
                            _uiState.update { MatchDetailUiState.Success(matchDetail) }
                            
                            // ✅ Load all dependencies in parallel for optimal user experience
                            // AI analysis (Hybrid Prediction) is NOT loaded automatically
                            launch { loadInjuries(fixtureId) }
                            launch { loadPrediction(fixtureId) }
                            launch { loadOdds(fixtureId) }
                            // DO NOT load hybrid prediction automatically - user must trigger it manually
                            // launch { loadHybridPrediction(matchDetail) } <- REMOVED
                            
                            // Cache the complete data
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
                dixonColesPrediction = null,
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
     * Load API prediction for the match.
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

    /**
     * Load hybrid prediction (Enhanced xG + AI) for the current match.
     */
    fun loadHybridPrediction(matchDetail: MatchDetail) {
        viewModelScope.launch {
            _isLoadingHybrid.update { true }
            _hybridError.update { null }
            
            try {
                // STEP 1: Get historical fixtures for prediction
                val historicalFixturesResult = matchRepository.getHistoricalFixturesForPrediction(
                    homeTeamId = matchDetail.homeTeamId ?: 0,
                    awayTeamId = matchDetail.awayTeamId ?: 0,
                    leagueId = matchDetail.leagueId ?: 0,
                    season = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                )
                
                if (historicalFixturesResult.isFailure) {
                    _hybridError.update { "Kon historische data niet ophalen: ${historicalFixturesResult.exceptionOrNull()?.message}" }
                    _isLoadingHybrid.update { false }
                    return@launch
                }
                
                val historicalFixtures = historicalFixturesResult.getOrThrow()
                
                // STEP 2: Filter fixtures by team
                val homeTeamFixtures = historicalFixtures.filter { 
                    it.homeTeamId == matchDetail.homeTeamId || it.awayTeamId == matchDetail.homeTeamId 
                }
                val awayTeamFixtures = historicalFixtures.filter { 
                    it.homeTeamId == matchDetail.awayTeamId || it.awayTeamId == matchDetail.awayTeamId 
                }
                
                // Log data sizes for debugging
                android.util.Log.d("MatchDetailViewModel", 
                    "Historical data loaded: " +
                    "total=${historicalFixtures.size}, " +
                    "home=${homeTeamFixtures.size}, " +
                    "away=${awayTeamFixtures.size}"
                )
                
                // STEP 3: Use the enhanced hybrid prediction use case with real data
                val result = getHybridPredictionUseCase(
                    fixtureId = fixtureId,
                    matchDetail = matchDetail,
                    homeTeamFixtures = homeTeamFixtures,
                    awayTeamFixtures = awayTeamFixtures,
                    leagueFixtures = historicalFixtures
                )
                
                if (result.isSuccess) {
                    // Convert EnhancedPrediction to MatchPrediction for HybridPrediction compatibility
                    val enhancedPrediction = result.getOrThrow()
                    
                    // Create base prediction (same as enhanced for now since we don't have old Dixon-Coles)
                    val basePrediction = MatchPrediction(
                        fixtureId = fixtureId,
                        homeTeam = matchDetail.homeTeam,
                        awayTeam = matchDetail.awayTeam,
                        homeWinProbability = enhancedPrediction.homeWinProbability,
                        drawProbability = enhancedPrediction.drawProbability,
                        awayWinProbability = enhancedPrediction.awayWinProbability,
                        expectedGoalsHome = enhancedPrediction.expectedGoalsHome,
                        expectedGoalsAway = enhancedPrediction.expectedGoalsAway,
                        analysis = "Enhanced xG prediction",
                        confidenceScore = (enhancedPrediction.confidence * 100).toInt()
                    )
                    
                    // Create enhanced prediction (same as base for now)
                    val enhancedMatchPrediction = MatchPrediction(
                        fixtureId = fixtureId,
                        homeTeam = matchDetail.homeTeam,
                        awayTeam = matchDetail.awayTeam,
                        homeWinProbability = enhancedPrediction.homeWinProbability,
                        drawProbability = enhancedPrediction.drawProbability,
                        awayWinProbability = enhancedPrediction.awayWinProbability,
                        expectedGoalsHome = enhancedPrediction.expectedGoalsHome,
                        expectedGoalsAway = enhancedPrediction.expectedGoalsAway,
                        analysis = "Enhanced xG + AI prediction",
                        confidenceScore = (enhancedPrediction.confidence * 100).toInt()
                    )
                    
                    val hybridPrediction = HybridPrediction(
                        originalPrediction = basePrediction,
                        enhancedPrediction = enhancedMatchPrediction,
                        analysis = AiAnalysisResult(
                            reasoning_short = "Enhanced xG + AI prediction using MatchMind AI 2.0 with xG integration",
                            confidence_score = (enhancedPrediction.confidence * 100).toInt()
                        ),
                        breakingNewsUsed = emptyList()
                    )
                    _hybridPrediction.update { hybridPrediction }
                    
                    // Auto-generate match report when hybrid prediction is loaded
                    generateMatchReport(matchDetail)
                } else {
                    val errorMessage = result.exceptionOrNull()?.message ?: "Onbekende fout bij AI-verrijkte voorspelling"
                    _hybridError.update { errorMessage }
                    _hybridPrediction.update { null }
                }
            } catch (e: Exception) {
                _hybridError.update { "Fout bij laden AI-verrijkte voorspelling: ${e.message}" }
                _hybridPrediction.update { null }
            } finally {
                _isLoadingHybrid.update { false }
            }
        }
    }

    /**
     * Get simplified hybrid prediction percentages.
     * Returns enhanced (homeWin%, draw%, awayWin%) as integers.
     */
    suspend fun getHybridPercentages(): Triple<Int, Int, Int>? {
        val hybrid = _hybridPrediction.value
        return if (hybrid != null) {
            val homePercent = (hybrid.enhancedPrediction.homeWinProbability * 100).toInt()
            val drawPercent = (hybrid.enhancedPrediction.drawProbability * 100).toInt()
            val awayPercent = (hybrid.enhancedPrediction.awayWinProbability * 100).toInt()
            Triple(homePercent, drawPercent, awayPercent)
        } else {
            null
        }
    }

    /**
     * Get AI analysis summary for display.
     */
    fun getAiAnalysisSummary(): String? {
        val hybrid = _hybridPrediction.value
        return hybrid?.analysis?.getSummary()
    }

    /**
     * Get breaking news used for AI analysis.
     */
    fun getBreakingNews(): List<String> {
        val hybrid = _hybridPrediction.value
        return hybrid?.breakingNewsUsed ?: emptyList()
    }

    /**
     * Check if AI analysis made a meaningful difference.
     */
    fun hasMeaningfulAiChange(): Boolean {
        val hybrid = _hybridPrediction.value
        return hybrid?.hasMeaningfulChange() ?: false
    }

    /**
     * Get percentage changes from AI analysis.
     * Returns (homeChange%, drawChange%, awayChange%) as doubles.
     */
    fun getAiPercentageChanges(): Triple<Double, Double, Double>? {
        val hybrid = _hybridPrediction.value
        return hybrid?.getPercentageChanges()
    }

    /**
     * Refresh hybrid prediction.
     */
    fun refreshHybridPrediction() {
        val currentState = _uiState.value
        if (currentState is MatchDetailUiState.Success) {
            loadHybridPrediction(currentState.matchDetail)
        }
    }

    /**
     * Clear hybrid prediction to reset to Dixon-Coles baseline.
     */
    fun clearHybridPrediction() {
        _hybridPrediction.update { null }
        _hybridError.update { null }
        _matchReport.update { null }
        _reportError.update { null }
    }

    /**
     * Check if hybrid prediction is available.
     */
    fun hasHybridPrediction(): Boolean {
        return _hybridPrediction.value != null
    }
    
    /**
     * Check if match report is available.
     */
    fun hasMatchReport(): Boolean {
        return _matchReport.value != null
    }

    /**
     * Check if hybrid prediction is loading.
     */
    fun isHybridLoading(): Boolean {
        return _isLoadingHybrid.value
    }
    
    /**
     * Check if match report is being generated.
     */
    fun isGeneratingReport(): Boolean {
        return _isGeneratingReport.value
    }

    /**
     * Get hybrid prediction error message.
     */
    fun getHybridError(): String? {
        return _hybridError.value
    }
    
    /**
     * Get match report error message.
     */
    fun getReportError(): String? {
        return _reportError.value
    }

    // ==================== MASTERMIND TIP METHODS ====================
    
    /**
     * Load Mastermind betting tip for the current match.
     * Combines all analysis into one clear betting recommendation.
     * 🔒 INTEGRITY FIX: Uses fixtureId as single source of truth
     * 
     * @param forceRefresh If true, clear cache before fetching fresh data to prevent context pollution
     */
    fun loadMastermindTip(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoadingMastermind.update { true }
            _mastermindError.update { null }
            
            try {
                // Generate Mastermind tip using the injected use case
                // 🔒 INTEGRITY FIX: Only pass fixtureId - repository will fetch fresh match data
                val result = mastermindAnalysisUseCase.invoke(fixtureId, forceRefresh)
                
                if (result.isSuccess) {
                    _mastermindTip.update { result.getOrThrow() }
                } else {
                    val errorMessage = result.exceptionOrNull()?.message ?: "Onbekende fout bij Mastermind analyse"
                    _mastermindError.update { errorMessage }
                    _mastermindTip.update { null }
                }
            } catch (e: Exception) {
                _mastermindError.update { "Fout bij laden Mastermind tip: ${e.message}" }
                _mastermindTip.update { null }
            } finally {
                _isLoadingMastermind.update { false }
            }
        }
    }
    
    /**
     * Check if Mastermind tip is available.
     */
    fun hasMastermindTip(): Boolean {
        return _mastermindTip.value != null
    }
    
    /**
     * Check if Mastermind tip is loading.
     */
    fun isMastermindLoading(): Boolean {
        return _isLoadingMastermind.value
    }
    
    /**
     * Get Mastermind tip error message.
     */
    fun getMastermindError(): String? {
        return _mastermindError.value
    }
    
    /**
     * Clear Mastermind tip to reset.
     */
    fun clearMastermindTip() {
        _mastermindTip.update { null }
        _mastermindError.update { null }
    }
    
    /**
     * Refresh Mastermind tip with cache clearing to prevent context pollution.
     */
    fun refreshMastermindTip() {
        loadMastermindTip(forceRefresh = true)
    }
    
    // ==================== MATCH REPORT METHODS ====================
    
    /**
     * Generate a comprehensive match report from the current hybrid prediction.
     * This creates a human-readable narrative from the AI analysis data.
     */
    fun generateMatchReport(matchDetail: MatchDetail) {
        viewModelScope.launch {
            val hybridPrediction = _hybridPrediction.value
            if (hybridPrediction == null) {
                _reportError.update { "Geen AI analyse beschikbaar. Voer eerst Mastermind analyse uit." }
                return@launch
            }
            
            _isGeneratingReport.update { true }
            _reportError.update { null }
            
            try {
                // Check if we can generate a report
                if (!matchReportGenerator.canGenerateReport(hybridPrediction)) {
                    _reportError.update { "Onvoldoende AI data beschikbaar voor verslaggeneratie" }
                    _matchReport.update { null }
                    return@launch
                }
                
                // Generate the report
                val report = matchReportGenerator.generateReport(hybridPrediction, matchDetail)
                
                if (report.hasMeaningfulContent()) {
                    _matchReport.update { report }
                } else {
                    _reportError.update { "Kon geen betekenisvol verslag genereren uit de beschikbare data" }
                    _matchReport.update { null }
                }
            } catch (e: Exception) {
                _reportError.update { "Fout bij genereren verslag: ${e.message}" }
                _matchReport.update { null }
            } finally {
                _isGeneratingReport.update { false }
            }
        }
    }
    
    /**
     * Refresh the match report with current data.
     */
    fun refreshMatchReport() {
        val currentState = _uiState.value
        if (currentState is MatchDetailUiState.Success) {
            generateMatchReport(currentState.matchDetail)
        }
    }
    
    /**
     * Get a quick summary of the match report for preview.
     */
    fun getReportSummary(): String? {
        val report = _matchReport.value
        return report?.getSummary()
    }
    
    /**
     * Get the full formatted report text.
     */
    fun getFullReportText(): String? {
        val report = _matchReport.value
        return report?.getFullReport()
    }
    
    /**
     * Get the report sections for UI display.
     */
    fun getReportSections(): List<com.Lyno.matchmindai.domain.model.ReportSection>? {
        val report = _matchReport.value
        return report?.getFormattedSections()
    }
    
    // ==================== QUICK METRICS METHODS ====================
    
    /**
     * Generate quick metrics for Intel tab display.
     * Lightweight version with key metrics only.
     */
    fun generateQuickMetrics(matchDetail: MatchDetail) {
        viewModelScope.launch {
            val hybridPrediction = _hybridPrediction.value
            if (hybridPrediction == null) {
                _quickMetricsError.update { "Geen AI analyse beschikbaar. Voer eerst Mastermind analyse uit." }
                return@launch
            }
            
            _isGeneratingQuickMetrics.update { true }
            _quickMetricsError.update { null }
            
            try {
                // Get API confidence from prediction
                val apiConfidence = _prediction.value?.winningPercent?.home?.toInt()
                
                // Generate quick metrics
                val metrics = matchReportGenerator.generateQuickMetrics(
                    hybridPrediction = hybridPrediction,
                    matchDetail = matchDetail,
                    apiConfidence = apiConfidence
                )
                
                _quickMetrics.update { metrics }
            } catch (e: Exception) {
                _quickMetricsError.update { "Fout bij genereren quick metrics: ${e.message}" }
                _quickMetrics.update { null }
            } finally {
                _isGeneratingQuickMetrics.update { false }
            }
        }
    }
    
    /**
     * Refresh quick metrics with current data.
     */
    fun refreshQuickMetrics() {
        val currentState = _uiState.value
        if (currentState is MatchDetailUiState.Success) {
            generateQuickMetrics(currentState.matchDetail)
        }
    }
    
    /**
     * Check if quick metrics are available.
     */
    fun hasQuickMetrics(): Boolean {
        return _quickMetrics.value != null
    }
    
    /**
     * Check if quick metrics are being generated.
     */
    fun isGeneratingQuickMetrics(): Boolean {
        return _isGeneratingQuickMetrics.value
    }
    
    /**
     * Get quick metrics error message.
     */
    fun getQuickMetricsError(): String? {
        return _quickMetricsError.value
    }
    
    /**
     * Clear quick metrics to reset.
     */
    fun clearQuickMetrics() {
        _quickMetrics.update { null }
        _quickMetricsError.update { null }
    }
    
    /**
     * Get quick metrics summary for display.
     */
    fun getQuickMetricsSummary(): String? {
        val metrics = _quickMetrics.value
        return metrics?.getQuickSummary()
    }

    // ==================== LIVE DATA METHODS ====================
    
    /**
     * Start polling for live match data.
     * Polls every 30 seconds when match is live.
     */
    fun startLivePolling(fixtureId: Int) {
        // Stop any existing polling
        stopLivePolling()

        pollingJob = viewModelScope.launch {
            while (true) {
                loadLiveMatchData(fixtureId)
                // Poll every 30 seconds for live data
                kotlinx.coroutines.delay(30_000L)
            }
        }
    }

    /**
     * Stop polling for live match data.
     */
    fun stopLivePolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    /**
     * Load live match data from repository.
     */
    fun loadLiveMatchData(fixtureId: Int) {
        viewModelScope.launch {
            _isRefreshingLiveData.update { true }
            try {
                val result = matchRepository.getLiveMatchData(fixtureId)
                if (result.isSuccess) {
                    val liveData = result.getOrNull()
                    if (liveData != null) {
                        _liveMatchData.update { Resource.Success(liveData) }
                    } else {
                        _liveMatchData.update { Resource.Error("Geen live data beschikbaar") }
                    }
                } else {
                    val errorMessage = result.exceptionOrNull()?.message ?: "Onbekende fout bij laden live data"
                    _liveMatchData.update { Resource.Error(errorMessage) }
                }
            } catch (e: Exception) {
                _liveMatchData.update { Resource.Error("Fout bij laden live data: ${e.message}") }
            } finally {
                _isRefreshingLiveData.update { false }
            }
        }
    }

    /**
     * Refresh live match data manually.
     */
    fun refreshLiveData(fixtureId: Int) {
        loadLiveMatchData(fixtureId)
    }

    /**
     * Check if match is currently live based on status.
     */
    fun isMatchLive(): Boolean {
        val currentState = _uiState.value
        return if (currentState is MatchDetailUiState.Success) {
            currentState.matchDetail.status?.isLive == true
        } else {
            false
        }
    }

    /**
     * Get current live match data if available.
     */
    fun getCurrentLiveData(): LiveMatchData? {
        return when (val resource = _liveMatchData.value) {
            is Resource.Success -> resource.data
            else -> null
        }
    }

    /**
     * Clear live match data when match ends.
     */
    fun clearLiveData() {
        _liveMatchData.update { Resource.Loading<LiveMatchData>() }
        stopLivePolling()
    }
}
