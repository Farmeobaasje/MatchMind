package com.Lyno.matchmindai.domain.model

import com.Lyno.matchmindai.data.local.entity.PredictionLogEntity
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * Retrospective analysis comparing a prediction with actual match results.
 * Used for Project Historxyi - Phase 1 (Data & Logic).
 *
 * @property prediction The original prediction made by Tesseract
 * @property actualMatch The actual match details from API
 * @property kaptigunStats Kaptigun analysis with xG data (may be null if not available)
 * @property outcomeCorrect True if predicted winner matches actual winner
 * @property exactScoreCorrect True if predicted score exactly matches actual score
 * @property xgVerdict xG-based performance verdict (DOMINANT, LUCKY, UNLUCKY, NEUTRAL)
 */
@Serializable
data class RetrospectiveAnalysis(
    @Contextual val prediction: PredictionLogEntity,
    val actualMatch: MatchDetail,
    val kaptigunStats: KaptigunAnalysis?,
    val outcomeCorrect: Boolean,
    val exactScoreCorrect: Boolean,
    val xgVerdict: XgVerdict
) {
    /**
     * Get match name in "Home vs Away" format.
     */
    val matchName: String
        get() = prediction.matchName

    /**
     * Get predicted score display.
     */
    val predictedScore: String
        get() = prediction.predictedScore

    /**
     * Get actual score display.
     */
    val actualScore: String
        get() = actualMatch.scoreDisplay

    /**
     * Get prediction confidence as percentage.
     */
    val confidencePercentage: Int
        get() = when {
            prediction.isHomeFavorite -> prediction.homeWinPercentage
            prediction.isAwayFavorite -> prediction.awayWinPercentage
            prediction.isDrawFavorite -> prediction.drawPercentage
            else -> 0
        }

    /**
     * Get formatted analysis summary.
     */
    fun getSummary(): String {
        return buildString {
            appendLine("Voorspelling: $predictedScore")
            appendLine("Werkelijkheid: $actualScore")
            
            when {
                outcomeCorrect && exactScoreCorrect -> 
                    appendLine("✅ Perfecte voorspelling!")
                outcomeCorrect -> 
                    appendLine("✅ Correcte uitslag")
                else -> 
                    appendLine("❌ Onjuiste uitslag")
            }
            
            if (kaptigunStats != null) {
                appendLine("xG Analyse: ${xgVerdict.displayText}")
            }
        }
    }

    companion object {
        /**
         * Creates an empty retrospective analysis for loading/error states.
         */
        fun empty(
            fixtureId: Int,
            homeTeam: String,
            awayTeam: String
        ): RetrospectiveAnalysis {
            val emptyPrediction = PredictionLogEntity(
                fixtureId = fixtureId,
                homeTeamId = 0,
                awayTeamId = 0,
                matchName = "$homeTeam vs $awayTeam",
                predictedScore = "0-0",
                homeProb = 0.33,
                drawProb = 0.34,
                awayProb = 0.33,
                homeFitness = 50,
                homeDistraction = 50
            )
            
            return RetrospectiveAnalysis(
                prediction = emptyPrediction,
                actualMatch = MatchDetailUtils.empty(fixtureId, homeTeam, awayTeam, ""),
                kaptigunStats = null,
                outcomeCorrect = false,
                exactScoreCorrect = false,
                xgVerdict = XgVerdict.NEUTRAL
            )
        }
    }
}

/**
 * xG-based performance verdict for retrospective analysis.
 * Based on Tesseract Twists logic from Project Kaptigun.
 */
@Serializable
enum class XgVerdict {
    DOMINANT,   // Won with higher xG (deserved win)
    LUCKY,      // Won with lower xG (lucky win)
    UNLUCKY,    // Lost with higher xG (unlucky loss)
    NEUTRAL;    // Balanced xG or insufficient data

    /**
     * Get display color for UI.
     */
    val color: String
        get() = when (this) {
            DOMINANT -> "#4CAF50" // Green
            LUCKY -> "#FF9800"    // Orange
            UNLUCKY -> "#F44336"  // Red
            NEUTRAL -> "#9E9E9E"  // Gray
        }

    /**
     * Get display text in Dutch.
     */
    val displayText: String
        get() = when (this) {
            DOMINANT -> "Dominant"
            LUCKY -> "Gelukkig"
            UNLUCKY -> "Ongelukkig"
            NEUTRAL -> "Neutraal"
        }

    /**
     * Get emoji representation for UI.
     */
    val emoji: String
        get() = when (this) {
            DOMINANT -> "✅"
            LUCKY -> "🍀"
            UNLUCKY -> "😞"
            NEUTRAL -> "➖"
        }
}

/**
 * Utility functions for retrospective analysis.
 */
object RetrospectiveAnalysisUtils {
    /**
     * Determine xG verdict based on prediction outcome and actual xG data.
     * Implements Tesseract Twists logic:
     * - Win with higher xG → DOMINANT
     * - Win with lower xG → LUCKY
     * - Loss with higher xG → UNLUCKY
     * - Balanced or insufficient data → NEUTRAL
     *
     * @param predictedWinner The predicted winner (HOME, AWAY, or DRAW)
     * @param actualWinner The actual winner (HOME, AWAY, or DRAW)
     * @param homeXg Home team's expected goals
     * @param awayXg Away team's expected goals
     * @return xG verdict
     */
    fun determineXgVerdict(
        predictedWinner: String,
        actualWinner: String,
        homeXg: Double?,
        awayXg: Double?
    ): XgVerdict {
        // If xG data is not available, return NEUTRAL
        if (homeXg == null || awayXg == null) {
            return XgVerdict.NEUTRAL
        }

        val outcomeCorrect = predictedWinner == actualWinner
        
        // Determine which team was predicted to win
        val predictedHomeWin = predictedWinner == "HOME"
        val predictedAwayWin = predictedWinner == "AWAY"
        val predictedDraw = predictedWinner == "DRAW"
        
        // Determine which team actually won
        val actualHomeWin = actualWinner == "HOME"
        val actualAwayWin = actualWinner == "AWAY"
        val actualDraw = actualWinner == "DRAW"
        
        // Calculate xG difference (positive = home better, negative = away better)
        val xgDifference = homeXg - awayXg
        
        // Logic for HOME win predictions
        if (predictedHomeWin) {
            return when {
                outcomeCorrect && xgDifference > 0.3 -> XgVerdict.DOMINANT
                outcomeCorrect && xgDifference < -0.3 -> XgVerdict.LUCKY
                !outcomeCorrect && xgDifference > 0.5 -> XgVerdict.UNLUCKY
                else -> XgVerdict.NEUTRAL
            }
        }
        
        // Logic for AWAY win predictions
        if (predictedAwayWin) {
            return when {
                outcomeCorrect && xgDifference < -0.3 -> XgVerdict.DOMINANT
                outcomeCorrect && xgDifference > 0.3 -> XgVerdict.LUCKY
                !outcomeCorrect && xgDifference < -0.5 -> XgVerdict.UNLUCKY
                else -> XgVerdict.NEUTRAL
            }
        }
        
        // Logic for DRAW predictions
        if (predictedDraw) {
            return when {
                outcomeCorrect && Math.abs(xgDifference) < 0.3 -> XgVerdict.DOMINANT
                outcomeCorrect && Math.abs(xgDifference) > 0.5 -> XgVerdict.LUCKY
                !outcomeCorrect && Math.abs(xgDifference) < 0.3 -> XgVerdict.UNLUCKY
                else -> XgVerdict.NEUTRAL
            }
        }
        
        return XgVerdict.NEUTRAL
    }

    /**
     * Determine winner from score string (e.g., "2-1").
     *
     * @param score Score string in "X-Y" format
     * @return Winner (HOME, AWAY, or DRAW)
     */
    fun getWinnerFromScore(score: String): String {
        val parts = score.split("-")
        if (parts.size != 2) return "DRAW"
        
        val home = parts[0].toIntOrNull() ?: 0
        val away = parts[1].toIntOrNull() ?: 0
        
        return when {
            home > away -> "HOME"
            away > home -> "AWAY"
            else -> "DRAW"
        }
    }

    /**
     * Determine winner from match detail.
     *
     * @param matchDetail The match detail
     * @return Winner (HOME, AWAY, or DRAW)
     */
    fun getWinnerFromMatch(matchDetail: MatchDetail): String {
        val score = matchDetail.score ?: MatchScore(0, 0)
        return when {
            score.home > score.away -> "HOME"
            score.away > score.home -> "AWAY"
            else -> "DRAW"
        }
    }

    /**
     * Create retrospective analysis from prediction and actual data.
     *
     * @param prediction The original prediction
     * @param actualMatch The actual match details
     * @param kaptigunStats Kaptigun analysis (optional)
     * @return RetrospectiveAnalysis
     */
    fun createAnalysis(
        prediction: PredictionLogEntity,
        actualMatch: MatchDetail,
        kaptigunStats: KaptigunAnalysis?
    ): RetrospectiveAnalysis {
        // Determine winners
        val predictedWinner = getWinnerFromScore(prediction.predictedScore)
        val actualWinner = getWinnerFromMatch(actualMatch)
        
        // Compare outcomes
        val outcomeCorrect = predictedWinner == actualWinner
        val exactScoreCorrect = prediction.predictedScore == actualMatch.scoreDisplay
        
        // Get xG data from Kaptigun analysis
        val homeXg = kaptigunStats?.deepStats?.avgXg?.first
        val awayXg = kaptigunStats?.deepStats?.avgXg?.second
        
        // Determine xG verdict
        val xgVerdict = determineXgVerdict(
            predictedWinner = predictedWinner,
            actualWinner = actualWinner,
            homeXg = homeXg,
            awayXg = awayXg
        )
        
        return RetrospectiveAnalysis(
            prediction = prediction,
            actualMatch = actualMatch,
            kaptigunStats = kaptigunStats,
            outcomeCorrect = outcomeCorrect,
            exactScoreCorrect = exactScoreCorrect,
            xgVerdict = xgVerdict
        )
    }
}
