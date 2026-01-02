package com.Lyno.matchmindai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Lyno.matchmindai.common.Resource
import com.Lyno.matchmindai.data.local.dao.PredictionDao
import com.Lyno.matchmindai.domain.model.RetrospectiveAnalysis
import com.Lyno.matchmindai.domain.model.RetrospectiveAnalysisUtils
import com.Lyno.matchmindai.domain.repository.KaptigunRepository
import com.Lyno.matchmindai.domain.repository.MatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.util.Log

/**
 * ViewModel for History Detail screen (Project Historxyi - Phase 1).
 * Loads a saved prediction from local DB and fetches actual match statistics
 * from API to compare "Prophecy vs Reality".
 *
 * Features:
 * 1. Load prediction from PredictionDao
 * 2. Fetch actual match details from MatchRepository
 * 3. Fetch xG statistics from KaptigunRepository
 * 4. Perform reconciliation analysis (score, outcome, xG verdict)
 * 5. Display comprehensive retrospective analysis
 */
class HistoryDetailViewModel(
    private val predictionDao: PredictionDao,
    private val matchRepository: MatchRepository,
    private val kaptigunRepository: KaptigunRepository
) : ViewModel() {

    companion object {
        private const val TAG = "HistoryDetailVM"
    }

    // Input parameters
    private var _predictionId: Int? = null
    private var _fixtureId: Int? = null

    // UI State
    private val _uiState = MutableStateFlow<HistoryDetailUiState>(HistoryDetailUiState.Loading)
    val uiState: StateFlow<HistoryDetailUiState> = _uiState.asStateFlow()

    /**
     * Initialize the ViewModel with prediction and fixture IDs.
     * Starts loading retrospective analysis immediately.
     *
     * @param predictionId The ID of the prediction (fixtureId from PredictionLogEntity)
     * @param fixtureId The fixture ID for fetching actual match data
     */
    fun initialize(predictionId: Int, fixtureId: Int) {
        _predictionId = predictionId
        _fixtureId = fixtureId
        
        Log.d(TAG, "Initializing HistoryDetailViewModel with predictionId=$predictionId, fixtureId=$fixtureId")
        loadRetrospective(predictionId, fixtureId)
    }

    /**
     * Load retrospective analysis comparing prediction with actual results.
     * Implements the reconciliation logic:
     * 1. Fetch prediction from PredictionDao
     * 2. Fetch actual match details from MatchRepository
     * 3. Fetch xG statistics from KaptigunRepository
     * 4. Perform comparison analysis
     *
     * @param predictionId The prediction ID (fixtureId)
     * @param fixtureId The fixture ID for API calls
     */
    fun loadRetrospective(predictionId: Int, fixtureId: Int) {
        viewModelScope.launch {
            try {
                _uiState.value = HistoryDetailUiState.Loading
                Log.d(TAG, "Loading retrospective analysis for predictionId=$predictionId")

                // 1. Fetch prediction from local DB
                val prediction = predictionDao.getPrediction(predictionId)
                if (prediction == null) {
                    _uiState.value = HistoryDetailUiState.Error("Prediction not found for ID: $predictionId")
                    Log.w(TAG, "Prediction not found for ID: $predictionId")
                    return@launch
                }

                // 2. Fetch actual match details (using Flow for reactive updates)
                val matchDetailFlow = matchRepository.getMatchDetails(fixtureId)
                
                // 3. Fetch Kaptigun analysis for xG data
                val kaptigunAnalysisResult = kaptigunRepository.fetchAnalysis(
                    fixtureId = fixtureId,
                    homeTeamId = prediction.homeTeamId,
                    awayTeamId = prediction.awayTeamId
                )

                // Combine data and create analysis
                matchDetailFlow.collect { matchDetailResource ->
                    when (matchDetailResource) {
                        is Resource.Loading -> {
                            _uiState.value = HistoryDetailUiState.Loading
                        }
                        
                        is Resource.Success -> {
                            val matchDetail = matchDetailResource.data
                            val kaptigunStats = kaptigunAnalysisResult.getOrNull()
                            
                            // 4. Create retrospective analysis
                            val analysis = RetrospectiveAnalysisUtils.createAnalysis(
                                prediction = prediction,
                                actualMatch = matchDetail,
                                kaptigunStats = kaptigunStats
                            )
                            
                            _uiState.value = HistoryDetailUiState.Success(analysis)
                            Log.d(TAG, "✅ Retrospective analysis loaded successfully")
                        }
                        
                        is Resource.Error -> {
                            val errorMessage = matchDetailResource.message ?: "Failed to load match details"
                            _uiState.value = HistoryDetailUiState.Error(errorMessage)
                            Log.e(TAG, "❌ Failed to load match details: $errorMessage")
                        }
                    }
                }

            } catch (e: Exception) {
                val errorMessage = "Failed to load retrospective analysis: ${e.message}"
                _uiState.value = HistoryDetailUiState.Error(errorMessage)
                Log.e(TAG, "❌ Error loading retrospective analysis", e)
            }
        }
    }

    /**
     * Refresh the retrospective analysis.
     * Can be called from UI via pull-to-refresh or refresh button.
     */
    fun refresh() {
        val predictionId = _predictionId
        val fixtureId = _fixtureId
        
        if (predictionId != null && fixtureId != null) {
            Log.d(TAG, "Refreshing retrospective analysis")
            loadRetrospective(predictionId, fixtureId)
        } else {
            Log.w(TAG, "Cannot refresh: predictionId or fixtureId is null")
            _uiState.value = HistoryDetailUiState.Error("Cannot refresh: missing prediction or fixture ID")
        }
    }

    /**
     * Get detailed reconciliation breakdown for debugging or advanced UI.
     *
     * @return Map of reconciliation metrics
     */
    fun getReconciliationBreakdown(analysis: RetrospectiveAnalysis): Map<String, Any> {
        return mapOf(
            "predictedScore" to analysis.predictedScore,
            "actualScore" to analysis.actualScore,
            "outcomeCorrect" to analysis.outcomeCorrect,
            "exactScoreCorrect" to analysis.exactScoreCorrect,
            "xgVerdict" to analysis.xgVerdict.name,
            "confidencePercentage" to analysis.confidencePercentage,
            "hasXgData" to (analysis.kaptigunStats != null)
        )
    }

    /**
     * Check if the match has sufficient data for analysis.
     * Some matches might not have xG data or detailed statistics.
     */
    fun hasSufficientData(analysis: RetrospectiveAnalysis): Boolean {
        return analysis.kaptigunStats != null && 
               analysis.actualMatch.score != null &&
               analysis.actualMatch.hasStarted
    }

    /**
     * Get analysis summary for sharing or exporting.
     */
    fun getAnalysisSummary(analysis: RetrospectiveAnalysis): String {
        return analysis.getSummary()
    }
}

/**
 * UI State for History Detail screen.
 */
sealed class HistoryDetailUiState {
    /**
     * Initial loading state.
     */
    object Loading : HistoryDetailUiState()

    /**
     * Success state with retrospective analysis.
     *
     * @property analysis The retrospective analysis data
     */
    data class Success(val analysis: RetrospectiveAnalysis) : HistoryDetailUiState()

    /**
     * Error state with message.
     *
     * @property message Error message to display
     */
    data class Error(val message: String) : HistoryDetailUiState()
}

/**
 * Data class for reconciliation metrics.
 * Used for detailed breakdown display.
 */
data class ReconciliationMetrics(
    val scoreMatch: Boolean,
    val outcomeMatch: Boolean,
    val xgVerdict: String,
    val confidence: Int,
    val dataCompleteness: Int // 0-100%
) {
    /**
     * Get overall reconciliation score (0-100).
     */
    val overallScore: Int
        get() {
            var score = 0
            if (scoreMatch) score += 40
            if (outcomeMatch) score += 30
            score += (dataCompleteness * 0.3).toInt()
            return score
        }

    /**
     * Get performance label based on overall score.
     */
    val performanceLabel: String
        get() = when {
            overallScore >= 80 -> "Excellent"
            overallScore >= 60 -> "Good"
            overallScore >= 40 -> "Fair"
            else -> "Poor"
        }
}
