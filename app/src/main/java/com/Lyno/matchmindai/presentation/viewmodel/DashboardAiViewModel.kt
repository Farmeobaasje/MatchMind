package com.Lyno.matchmindai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Lyno.matchmindai.domain.model.AiAnalysisResult
import com.Lyno.matchmindai.domain.model.HeroMatchExplanation
import com.Lyno.matchmindai.domain.model.LiveEventAnalysis
import com.Lyno.matchmindai.domain.model.MatchDetail
import com.Lyno.matchmindai.domain.model.MatchFixture
import com.Lyno.matchmindai.domain.service.CuratedFeed
import com.Lyno.matchmindai.domain.service.HeroMatchExplainer
import com.Lyno.matchmindai.domain.service.LiveEventAnalyzer
import com.Lyno.matchmindai.presentation.model.DashboardAiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for managing AI-powered insights in the dashboard.
 * Handles hero match explanations, AI predictions, and live event analysis.
 */
class DashboardAiViewModel(
    private val heroMatchExplainer: HeroMatchExplainer,
    private val liveEventAnalyzer: LiveEventAnalyzer
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DashboardAiState())
    val uiState: StateFlow<DashboardAiState> = _uiState.asStateFlow()
    
    /**
     * Explain the hero match selection.
     */
    fun explainHeroMatch(
        match: MatchFixture,
        curatedFeed: CuratedFeed,
        matchDetail: MatchDetail? = null,
        curationScore: Int = 0
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = heroMatchExplainer.explainHeroMatchSelection(
                    match = match,
                    curatedFeed = curatedFeed,
                    matchDetail = matchDetail,
                    curationScore = curationScore
                )
                
                if (result.isSuccess) {
                    _uiState.update { state ->
                        state.copy(
                            heroMatchExplanation = result.getOrThrow(),
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { state ->
                        state.copy(
                            error = result.exceptionOrNull()?.message ?: "Failed to explain hero match",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        error = e.message ?: "Unknown error",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    /**
     * Analyze live events for a match.
     */
    fun analyzeLiveEvents(
        match: MatchFixture,
        liveData: com.Lyno.matchmindai.domain.model.LiveMatchData,
        matchDetail: MatchDetail? = null,
        basePrediction: com.Lyno.matchmindai.domain.model.MatchPrediction? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = liveEventAnalyzer.analyzeLiveEvents(
                    match = match,
                    liveData = liveData,
                    matchDetail = matchDetail,
                    basePrediction = basePrediction
                )
                
                if (result.isSuccess) {
                    _uiState.update { state ->
                        state.copy(
                            liveEventAnalysis = result.getOrThrow(),
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { state ->
                        state.copy(
                            error = result.exceptionOrNull()?.message ?: "Failed to analyze live events",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        error = e.message ?: "Unknown error",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    /**
     * Update AI analysis for a match.
     */
    fun updateAiAnalysis(match: MatchFixture, aiAnalysis: AiAnalysisResult) {
        _uiState.update { state ->
            val newAnalyzedMatches = if (match.fixtureId != null) {
                state.analyzedMatches + match.fixtureId!!
            } else {
                state.analyzedMatches
            }
            state.copy(
                aiAnalysis = aiAnalysis,
                analyzedMatches = newAnalyzedMatches
            )
        }
    }
    
    /**
     * Clear hero match explanation.
     */
    fun clearHeroMatchExplanation() {
        _uiState.update { it.copy(heroMatchExplanation = null) }
    }
    
    /**
     * Clear live event analysis.
     */
    fun clearLiveEventAnalysis() {
        _uiState.update { it.copy(liveEventAnalysis = null) }
    }
    
    /**
     * Clear all AI insights.
     */
    fun clearAllInsights() {
        _uiState.update { 
            DashboardAiState(
                isLoading = false,
                error = null,
                heroMatchExplanation = null,
                liveEventAnalysis = null,
                aiAnalysis = null,
                analyzedMatches = emptySet()
            )
        }
    }
    
    /**
     * Check if a match has been analyzed.
     */
    fun isMatchAnalyzed(fixtureId: Int?): Boolean {
        return fixtureId != null && _uiState.value.analyzedMatches.contains(fixtureId)
    }
    
    /**
     * Get AI insights summary for the dashboard.
     */
    fun getAiInsightsSummary(): String {
        val state = _uiState.value
        
        return when {
            state.heroMatchExplanation != null -> {
                state.heroMatchExplanation.getSummary()
            }
            state.liveEventAnalysis != null && state.liveEventAnalysis.isFresh() -> {
                state.liveEventAnalysis.getSummary()
            }
            state.aiAnalysis != null -> {
                "AI analyse beschikbaar voor deze wedstrijd"
            }
            else -> {
                "Geen AI insights beschikbaar"
            }
        }
    }
    
    /**
     * Get the current AI confidence level (0-100).
     */
    fun getAiConfidence(): Int {
        val state = _uiState.value
        
        return when {
            state.heroMatchExplanation != null -> {
                state.heroMatchExplanation.getConfidencePercentage()
            }
            state.liveEventAnalysis != null -> {
                (state.liveEventAnalysis.confidence * 100).toInt()
            }
            state.aiAnalysis != null -> {
                state.aiAnalysis.getBettingConfidence()
            }
            else -> 0
        }
    }
    
    /**
     * Check if there are any meaningful AI insights available.
     */
    fun hasMeaningfulInsights(): Boolean {
        val state = _uiState.value
        
        return state.heroMatchExplanation?.hasMeaningfulInsights() == true ||
               (state.liveEventAnalysis != null && state.liveEventAnalysis.keyInsights.isNotEmpty()) ||
               (state.aiAnalysis != null && state.aiAnalysis.hasMeaningfulData())
    }
}
