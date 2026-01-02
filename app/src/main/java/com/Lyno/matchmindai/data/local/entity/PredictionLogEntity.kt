package com.Lyno.matchmindai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for storing Tesseract prediction results for future accuracy evaluation.
 * This serves as the "Black Box Recorder" for Project Tesseract - Phase 4.
 * 
 * Phase 5 Enhancement: Added actual result fields for truth reconciliation.
 * Phase 15 Enhancement: Added LLMGRADE context score and risk level fields.
 *
 * @property fixtureId Unique identifier for the fixture (composite of homeTeamId-awayTeamId)
 * @property homeTeamId Home team ID from API-Sports (for analytics and reconciliation)
 * @property awayTeamId Away team ID from API-Sports (for analytics and reconciliation)
 * @property matchName Human-readable match name in "Home vs Away" format
 * @property predictedScore Most likely score predicted by Tesseract (e.g., "2-1")
 * @property homeProb Probability of home win (0.0-1.0)
 * @property drawProb Probability of draw (0.0-1.0)
 * @property awayProb Probability of away win (0.0-1.0)
 * @property homeFitness Home team fitness level from SimulationContext (0-100)
 * @property homeDistraction Home team distraction level from SimulationContext (0-100)
 * @property llmGradeContextScore LLMGRADE overall context score (0-10) - null if not available
 * @property llmGradeRiskLevel LLMGRADE overall risk level (LOW, MEDIUM, HIGH) - null if not available
 * @property actualScore Actual match result (e.g., "2-1") - null until match is finished
 * @property outcomeCorrect True if predicted winner matches actual winner - null until match is finished
 * @property exactScoreCorrect True if predicted score exactly matches actual score - null until match is finished
 * @property timestamp When the prediction was recorded (System.currentTimeMillis())
 */
@Entity(tableName = "prediction_logs")
data class PredictionLogEntity(
    @PrimaryKey
    val fixtureId: Int,
    val homeTeamId: Int,
    val awayTeamId: Int,
    val matchName: String,
    val predictedScore: String,
    val homeProb: Double,
    val drawProb: Double,
    val awayProb: Double,
    val homeFitness: Int,
    val homeDistraction: Int,
    val llmGradeContextScore: Float? = null,
    val llmGradeRiskLevel: String? = null,
    val actualScore: String? = null,
    val outcomeCorrect: Boolean? = null,
    val exactScoreCorrect: Boolean? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    init {
        require(fixtureId > 0) { 
            "fixtureId must be positive. Received: $fixtureId. " +
            "This might indicate an API call failure or data corruption. " +
            "Check if the fixture ID is valid and API calls are succeeding." 
        }
        require(matchName.isNotBlank()) { "matchName cannot be blank" }
        require(predictedScore.matches(Regex("^\\d+-\\d+$"))) {
            "predictedScore must be in format 'X-Y' (e.g., '2-1'). Received: $predictedScore"
        }
        require(homeProb in 0.0..1.0) { "homeProb must be between 0.0 and 1.0. Received: $homeProb" }
        require(drawProb in 0.0..1.0) { "drawProb must be between 0.0 and 1.0. Received: $drawProb" }
        require(awayProb in 0.0..1.0) { "awayProb must be between 0.0 and 1.0. Received: $awayProb" }
        require((homeProb + drawProb + awayProb).let { 
            it >= 0.90 && it <= 1.10 
        }) { 
            "Probabilities must sum to approximately 1.0 (tolerance: ±10%). " +
            "Sum: ${homeProb + drawProb + awayProb}. " +
            "Home: $homeProb, Draw: $drawProb, Away: $awayProb" 
        }
        require(homeFitness in 0..100) { "homeFitness must be between 0 and 100. Received: $homeFitness" }
        require(homeDistraction in 0..100) { "homeDistraction must be between 0 and 100. Received: $homeDistraction" }
        llmGradeContextScore?.let { require(it in 0f..10f) { "llmGradeContextScore must be between 0 and 10. Received: $it" } }
        require(timestamp > 0) { "timestamp must be positive. Received: $timestamp" }
    }

    /**
     * Returns the home win probability as a percentage (0-100).
     */
    val homeWinPercentage: Int
        get() = (homeProb * 100).toInt()

    /**
     * Returns the draw probability as a percentage (0-100).
     */
    val drawPercentage: Int
        get() = (drawProb * 100).toInt()

    /**
     * Returns the away win probability as a percentage (0-100).
     */
    val awayWinPercentage: Int
        get() = (awayProb * 100).toInt()

    /**
     * Returns true if home team is the favorite (highest probability).
     */
    val isHomeFavorite: Boolean
        get() = homeProb > awayProb && homeProb > drawProb

    /**
     * Returns true if away team is the favorite (highest probability).
     */
    val isAwayFavorite: Boolean
        get() = awayProb > homeProb && awayProb > drawProb

    /**
     * Returns true if draw is the most likely outcome.
     */
    val isDrawFavorite: Boolean
        get() = drawProb > homeProb && drawProb > awayProb

    /**
     * Returns a formatted string representation of the prediction.
     */
    fun getFormattedPrediction(): String {
        return "$matchName: $predictedScore (H:${homeWinPercentage}% D:${drawPercentage}% A:${awayWinPercentage}%)"
    }

    companion object {
        /**
         * Creates a fixture ID from home and away team IDs.
         * Uses a deterministic hash to ensure uniqueness.
         */
        fun createFixtureId(homeTeamId: Int, awayTeamId: Int): Int {
            return "$homeTeamId-$awayTeamId".hashCode()
        }
    }
}
