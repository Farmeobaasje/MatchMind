package com.Lyno.matchmindai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.Lyno.matchmindai.data.local.entity.PredictionLogEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Tesseract prediction logging operations.
 * Provides methods to store and retrieve prediction results for accuracy evaluation.
 * 
 * This serves as the "Black Box Recorder" for Project Tesseract - Phase 4.
 */
@Dao
interface PredictionDao {

    /**
     * Inserts or replaces a prediction log entry.
     * Uses REPLACE strategy to update existing predictions for the same fixture.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrediction(prediction: PredictionLogEntity)

    /**
     * Retrieves a prediction log entry by fixture ID.
     * Returns null if no prediction exists for the given fixture.
     */
    @Query("SELECT * FROM prediction_logs WHERE fixtureId = :fixtureId")
    suspend fun getPrediction(fixtureId: Int): PredictionLogEntity?

    /**
     * Retrieves all prediction logs, ordered by most recent first.
     * Returns a Flow for reactive updates.
     */
    @Query("SELECT * FROM prediction_logs ORDER BY timestamp DESC")
    fun getAllPredictions(): Flow<List<PredictionLogEntity>>

    /**
     * Retrieves prediction logs for a specific date range.
     * Useful for analyzing predictions over time.
     */
    @Query("SELECT * FROM prediction_logs WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    suspend fun getPredictionsInRange(startTime: Long, endTime: Long): List<PredictionLogEntity>

    /**
     * Retrieves the most recent N predictions.
     * Useful for dashboard displays.
     */
    @Query("SELECT * FROM prediction_logs ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentPredictions(limit: Int): List<PredictionLogEntity>

    /**
     * Counts the total number of stored predictions.
     * Useful for monitoring the size of the training dataset.
     */
    @Query("SELECT COUNT(*) FROM prediction_logs")
    suspend fun getPredictionCount(): Int

    /**
     * Deletes a specific prediction by fixture ID.
     * Used for data cleanup or correction.
     */
    @Query("DELETE FROM prediction_logs WHERE fixtureId = :fixtureId")
    suspend fun deletePrediction(fixtureId: Int)

    /**
     * Deletes predictions older than the specified timestamp.
     * Used for automatic data retention management.
     */
    @Query("DELETE FROM prediction_logs WHERE timestamp < :timestamp")
    suspend fun deletePredictionsOlderThan(timestamp: Long)

    /**
     * Deletes all predictions from the database.
     * Used for clearing the entire history.
     */
    @Query("DELETE FROM prediction_logs")
    suspend fun deleteAllPredictions()

    /**
     * Transaction that updates a prediction if it exists, otherwise inserts it.
     * Ensures atomic update of prediction data.
     */
    @Transaction
    suspend fun upsertPrediction(prediction: PredictionLogEntity) {
        deletePrediction(prediction.fixtureId)
        insertPrediction(prediction)
    }

    /**
     * Checks if a prediction exists for the given fixture ID.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM prediction_logs WHERE fixtureId = :fixtureId)")
    suspend fun hasPrediction(fixtureId: Int): Boolean

    /**
     * Retrieves predictions where the home team was the favorite.
     * Useful for analyzing prediction accuracy by favorite status.
     */
    @Query("SELECT * FROM prediction_logs WHERE homeProb > awayProb AND homeProb > drawProb ORDER BY timestamp DESC")
    suspend fun getHomeFavoritePredictions(): List<PredictionLogEntity>

    /**
     * Retrieves predictions where the away team was the favorite.
     */
    @Query("SELECT * FROM prediction_logs WHERE awayProb > homeProb AND awayProb > drawProb ORDER BY timestamp DESC")
    suspend fun getAwayFavoritePredictions(): List<PredictionLogEntity>

    /**
     * Retrieves predictions where draw was the favorite.
     */
    @Query("SELECT * FROM prediction_logs WHERE drawProb > homeProb AND drawProb > awayProb ORDER BY timestamp DESC")
    suspend fun getDrawFavoritePredictions(): List<PredictionLogEntity>

    /**
     * Updates a prediction with actual match results.
     * Phase 5: Truth Reconciliation - Updates the prediction with actual score and accuracy flags.
     * 
     * @param fixtureId The unique fixture identifier
     * @param actualScore The actual match result (e.g., "2-1")
     * @param outcomeCorrect True if predicted winner matches actual winner
     * @param exactScoreCorrect True if predicted score exactly matches actual score
     */
    @Query("""
        UPDATE prediction_logs 
        SET actualScore = :actualScore, 
            outcomeCorrect = :outcomeCorrect, 
            exactScoreCorrect = :exactScoreCorrect 
        WHERE fixtureId = :fixtureId
    """)
    suspend fun updateResult(
        fixtureId: Int,
        actualScore: String,
        outcomeCorrect: Boolean,
        exactScoreCorrect: Boolean
    )

    /**
     * Retrieves all predictions that are pending reconciliation.
     * Phase 5: Returns predictions where actualScore is NULL (match not finished yet).
     * 
     * @return List of predictions waiting for actual results
     */
    @Query("SELECT * FROM prediction_logs WHERE actualScore IS NULL ORDER BY timestamp DESC")
    suspend fun getAllPendingPredictions(): List<PredictionLogEntity>

    /**
     * Retrieves all predictions that have been reconciled (completed).
     * Phase 5: Returns predictions where actualScore is NOT NULL (match finished).
     * Returns a Flow for reactive UI updates.
     * 
     * @return Flow of completed predictions
     */
    @Query("SELECT * FROM prediction_logs WHERE actualScore IS NOT NULL ORDER BY timestamp DESC")
    fun getAllCompletedPredictions(): Flow<List<PredictionLogEntity>>

    /**
     * Retrieves predictions for a specific team.
     * Useful for analytics: "How accurate is Tesseract for Ajax matches?"
     * 
     * @param teamId The team ID to filter predictions
     * @return List of predictions involving the specified team
     */
    @Query("""
        SELECT * FROM prediction_logs 
        WHERE homeTeamId = :teamId OR awayTeamId = :teamId 
        ORDER BY timestamp DESC
    """)
    suspend fun getPredictionsByTeam(teamId: Int): List<PredictionLogEntity>

    /**
     * Calculates accuracy statistics for completed predictions.
     * Phase 5: Returns counts of correct outcomes and exact scores.
     * 
     * @return Pair of (correctOutcomeCount, correctExactScoreCount, totalCompletedCount)
     */
    @Query("""
        SELECT 
            COUNT(*) as total,
            SUM(CASE WHEN outcomeCorrect = 1 THEN 1 ELSE 0 END) as correctOutcomes,
            SUM(CASE WHEN exactScoreCorrect = 1 THEN 1 ELSE 0 END) as correctExactScores
        FROM prediction_logs 
        WHERE actualScore IS NOT NULL
    """)
    suspend fun getAccuracyStats(): AccuracyStats
}

/**
 * Data class for accuracy statistics.
 * Phase 5: Used to calculate win rate and exact score accuracy.
 */
data class AccuracyStats(
    val total: Int,
    val correctOutcomes: Int,
    val correctExactScores: Int
) {
    /**
     * Returns the win rate percentage (correct outcomes / total).
     * Returns 0.0 if no completed predictions.
     */
    val winRatePercentage: Double
        get() = if (total > 0) (correctOutcomes.toDouble() / total) * 100.0 else 0.0

    /**
     * Returns the exact score accuracy percentage.
     */
    val exactScorePercentage: Double
        get() = if (total > 0) (correctExactScores.toDouble() / total) * 100.0 else 0.0

    /**
     * Returns a formatted string representation of the statistics.
     */
    fun getFormattedStats(): String {
        return "Win Rate: ${"%.1f".format(winRatePercentage)}% (${correctOutcomes}/${total}), " +
               "Exact Score: ${"%.1f".format(exactScorePercentage)}% (${correctExactScores}/${total})"
    }
}
