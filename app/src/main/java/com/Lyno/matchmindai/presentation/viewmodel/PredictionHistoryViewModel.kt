package com.Lyno.matchmindai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Lyno.matchmindai.data.local.dao.PredictionDao
import com.Lyno.matchmindai.data.repository.OracleRepositoryImpl
import com.Lyno.matchmindai.data.local.entity.PredictionLogEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import android.util.Log

/**
 * ViewModel for displaying prediction history and accuracy statistics.
 * Phase 5: The Truth Reconciliation - Shows completed predictions with actual results.
 * 
 * Features:
 * 1. Lazy reconciliation on init (checks pending predictions older than 2 hours)
 * 2. Live updates of completed predictions via Flow
 * 3. Accuracy statistics calculation (win rate, exact score percentage)
 * 4. Team-specific analytics
 */
class PredictionHistoryViewModel(
    private val oracleRepository: OracleRepositoryImpl,
    private val predictionDao: PredictionDao
) : ViewModel() {

    companion object {
        private const val TAG = "PredictionHistoryVM"
    }

    // State flows
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _reconciliationStatus = MutableStateFlow<String?>(null)
    val reconciliationStatus: StateFlow<String?> = _reconciliationStatus.asStateFlow()

    // Public flows for UI
    val allPredictions: Flow<List<PredictionLogEntity>> = predictionDao.getAllPredictions()
    val completedPredictions: Flow<List<PredictionLogEntity>> = predictionDao.getAllCompletedPredictions()
    
    val accuracyStats: Flow<PredictionAccuracyStats> = completedPredictions.map { predictions ->
        calculateAccuracyStats(predictions)
    }

    /**
     * Initialize the ViewModel with lazy reconciliation.
     * Checks for pending predictions older than 2 hours and attempts to reconcile them.
     */
    init {
        Log.d(TAG, "Initializing PredictionHistoryViewModel")
        viewModelScope.launch {
            performLazyReconciliation()
        }
    }

    /**
     * Perform lazy reconciliation of pending predictions.
     * Only processes predictions older than 2 hours.
     * This runs automatically when the user navigates to the history screen.
     */
    private suspend fun performLazyReconciliation() {
        try {
            _isLoading.value = true
            _reconciliationStatus.value = "Checking for pending predictions..."
            
            Log.d(TAG, "Starting lazy reconciliation")
            val reconciledCount = oracleRepository.checkPendingPredictions()
            
            if (reconciledCount > 0) {
                _reconciliationStatus.value = "Updated $reconciledCount predictions with actual results"
                Log.d(TAG, "✅ Lazy reconciliation complete: $reconciledCount predictions updated")
            } else {
                _reconciliationStatus.value = "No pending predictions to update"
                Log.d(TAG, "✅ No predictions needed reconciliation")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to perform lazy reconciliation", e)
            _reconciliationStatus.value = "Failed to update predictions: ${e.message}"
        } finally {
            _isLoading.value = false
            
            // Clear status message after 3 seconds
            viewModelScope.launch {
                kotlinx.coroutines.delay(3000)
                _reconciliationStatus.value = null
            }
        }
    }

    /**
     * Manually trigger reconciliation of pending predictions.
     * Can be called from UI via refresh button.
     */
    fun refreshPredictions() {
        viewModelScope.launch {
            performLazyReconciliation()
        }
    }

    /**
     * Get predictions for a specific team.
     * Useful for analytics: "How accurate is Tesseract for Ajax matches?"
     * 
     * @param teamId The team ID to filter predictions
     * @return Flow of predictions involving the specified team
     */
    fun getPredictionsByTeam(teamId: Int): Flow<List<PredictionLogEntity>> {
        return completedPredictions.map { predictions ->
            predictions.filter { 
                it.homeTeamId == teamId || it.awayTeamId == teamId 
            }
        }
    }
    
    /**
     * Get predictions filtered by completion status.
     * 
     * @param completedOnly If true, returns only completed predictions (with actual results)
     * @return Flow of filtered predictions
     */
    fun getPredictionsWithStatus(completedOnly: Boolean): Flow<List<PredictionLogEntity>> {
        return if (completedOnly) {
            completedPredictions
        } else {
            allPredictions
        }
    }

    /**
     * Calculate accuracy statistics from a list of completed predictions.
     * 
     * @param predictions List of completed predictions (with actual results)
     * @return PredictionAccuracyStats with calculated percentages
     */
    private fun calculateAccuracyStats(predictions: List<PredictionLogEntity>): PredictionAccuracyStats {
        if (predictions.isEmpty()) {
            return PredictionAccuracyStats(
                total = 0,
                correctOutcomes = 0,
                correctExactScores = 0,
                winRatePercentage = 0.0,
                exactScorePercentage = 0.0
            )
        }

        val total = predictions.size
        val correctOutcomes = predictions.count { it.outcomeCorrect == true }
        val correctExactScores = predictions.count { it.exactScoreCorrect == true }

        val winRatePercentage = if (total > 0) (correctOutcomes.toDouble() / total) * 100.0 else 0.0
        val exactScorePercentage = if (total > 0) (correctExactScores.toDouble() / total) * 100.0 else 0.0

        return PredictionAccuracyStats(
            total = total,
            correctOutcomes = correctOutcomes,
            correctExactScores = correctExactScores,
            winRatePercentage = winRatePercentage,
            exactScorePercentage = exactScorePercentage
        )
    }

    /**
     * Get predictions filtered by outcome correctness.
     * 
     * @param correctOnly If true, returns only correct predictions
     * @return Flow of filtered predictions
     */
    fun getPredictionsByAccuracy(correctOnly: Boolean): Flow<List<PredictionLogEntity>> {
        return completedPredictions.map { predictions ->
            if (correctOnly) {
                predictions.filter { it.outcomeCorrect == true }
            } else {
                predictions
            }
        }
    }

    /**
     * Get the most recent N predictions.
     * 
     * @param limit Maximum number of predictions to return
     * @return Flow of recent predictions
     */
    fun getRecentPredictions(limit: Int): Flow<List<PredictionLogEntity>> {
        return completedPredictions.map { predictions ->
            predictions.take(limit)
        }
    }

    /**
     * Delete a specific prediction by fixture ID.
     * 
     * @param fixtureId The fixture ID of the prediction to delete
     */
    fun deletePrediction(fixtureId: Int) {
        viewModelScope.launch {
            try {
                predictionDao.deletePrediction(fixtureId)
                Log.d(TAG, "✅ Prediction deleted: fixtureId=$fixtureId")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to delete prediction: fixtureId=$fixtureId", e)
            }
        }
    }

    /**
     * Delete all predictions from the database.
     * Shows a confirmation dialog before executing.
     */
    fun deleteAllPredictions() {
        viewModelScope.launch {
            try {
                predictionDao.deleteAllPredictions()
                Log.d(TAG, "✅ All predictions deleted")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to delete all predictions", e)
            }
        }
    }
}

/**
 * Data class for prediction accuracy statistics.
 * Used to display win rate and exact score accuracy in the UI.
 */
data class PredictionAccuracyStats(
    val total: Int,
    val correctOutcomes: Int,
    val correctExactScores: Int,
    val winRatePercentage: Double,
    val exactScorePercentage: Double
) {
    /**
     * Returns a formatted string representation of the statistics.
     */
    fun getFormattedStats(): String {
        return "Win Rate: ${"%.1f".format(winRatePercentage)}% (${correctOutcomes}/${total}), " +
               "Exact Score: ${"%.1f".format(exactScorePercentage)}% (${correctExactScores}/${total})"
    }

    /**
     * Returns true if there are any completed predictions.
     */
    val hasData: Boolean
        get() = total > 0
}
