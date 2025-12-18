    package com.Lyno.matchmindai.domain.model

import com.Lyno.matchmindai.domain.model.WinningPercent
import com.Lyno.matchmindai.domain.model.ExpectedGoals

/**
 * Domain model representing a match prediction.
 * This is a pure Kotlin class with no Android dependencies.
 * 
 * This model supports both probability-based predictions (from API-SPORTS)
 * and winner-based predictions (from AI analysis).
 */
data class MatchPrediction(
    val fixtureId: Int,
    val homeTeam: String,
    val awayTeam: String,
    val homeWinProbability: Double,
    val drawProbability: Double,
    val awayWinProbability: Double,
    val expectedGoalsHome: Double,
    val expectedGoalsAway: Double,
    val analysis: String? = null,
    // Computed fields for UI compatibility
    val winner: String = "",
    val confidenceScore: Int = 0, // 0-100
    val riskLevel: RiskLevel = RiskLevel.MEDIUM,
    val reasoning: String = "",
    val keyFactor: String = "",
    val recentMatches: List<String> = emptyList(),
    val sources: List<String> = emptyList(),
    val suggestedActions: List<String> = emptyList()
) {
    enum class RiskLevel {
        LOW, MEDIUM, HIGH
    }

    /**
     * Get the predicted winner based on highest probability.
     */
    val computedWinner: String
        get() = when {
            homeWinProbability >= drawProbability && homeWinProbability >= awayWinProbability -> homeTeam
            drawProbability >= homeWinProbability && drawProbability >= awayWinProbability -> "Draw"
            else -> awayTeam
        }

    /**
     * Get confidence score based on highest probability.
     */
    val computedConfidenceScore: Int
        get() = (maxOf(homeWinProbability, drawProbability, awayWinProbability) * 100).toInt()

    /**
     * Get risk level based on probability distribution.
     */
    val computedRiskLevel: RiskLevel
        get() {
            val maxProb = maxOf(homeWinProbability, drawProbability, awayWinProbability)
            return when {
                maxProb >= 60 -> RiskLevel.LOW
                maxProb >= 40 -> RiskLevel.MEDIUM
                else -> RiskLevel.HIGH
            }
        }

    /**
     * The predicted winner with confidence percentage.
     */
    val winnerWithConfidence: String
        get() = "${winner.ifEmpty { computedWinner }} (${if (confidenceScore > 0) confidenceScore else computedConfidenceScore}%)"

    /**
     * Whether the prediction indicates a home win.
     */
    val isHomeWin: Boolean
        get() {
            val actualWinner = if (winner.isNotEmpty()) winner else computedWinner
            return actualWinner.equals(homeTeam, ignoreCase = true)
        }

    /**
     * Whether the prediction indicates an away win.
     */
    val isAwayWin: Boolean
        get() {
            val actualWinner = if (winner.isNotEmpty()) winner else computedWinner
            return actualWinner.equals(awayTeam, ignoreCase = true)
        }

    /**
     * Whether the prediction indicates a draw.
     */
    val isDraw: Boolean
        get() {
            val actualWinner = if (winner.isNotEmpty()) winner else computedWinner
            return actualWinner.equals("Draw", ignoreCase = true) ||
                    actualWinner.equals("Gelijkspel", ignoreCase = true)
        }

    /**
     * Convert to MatchPredictionData for API compatibility.
     */
    fun toMatchPredictionData(): MatchPredictionData {
        return MatchPredictionData(
            fixtureId = fixtureId,
            homeTeam = homeTeam,
            awayTeam = awayTeam,
            league = "", // League not available in this model
            primaryPrediction = if (winner.isNotEmpty()) winner else computedWinner,
            winningPercent = WinningPercent(
                home = homeWinProbability,
                draw = drawProbability,
                away = awayWinProbability
            ),
            expectedGoals = ExpectedGoals(
                home = expectedGoalsHome,
                away = expectedGoalsAway
            ),
            analysis = analysis ?: reasoning
        )
    }

    companion object {
        /**
         * Creates a MatchPrediction from MatchPredictionData.
         */
        fun fromMatchPredictionData(data: MatchPredictionData): MatchPrediction {
            return MatchPrediction(
                fixtureId = data.fixtureId,
                homeTeam = data.homeTeam,
                awayTeam = data.awayTeam,
                homeWinProbability = data.winningPercent.home,
                drawProbability = data.winningPercent.draw,
                awayWinProbability = data.winningPercent.away,
                expectedGoalsHome = data.expectedGoals.home,
                expectedGoalsAway = data.expectedGoals.away,
                analysis = data.analysis,
                winner = data.primaryPrediction,
                confidenceScore = data.winningPercent.mostLikely.toInt(),
                reasoning = data.analysis ?: ""
            )
        }

        /**
         * Creates an invalid prediction for error cases.
         */
        fun invalid(): MatchPrediction = MatchPrediction(
            fixtureId = 0,
            homeTeam = "",
            awayTeam = "",
            homeWinProbability = 0.0,
            drawProbability = 0.0,
            awayWinProbability = 0.0,
            expectedGoalsHome = 0.0,
            expectedGoalsAway = 0.0,
            analysis = "Kon geen voorspelling maken.",
            winner = "Ongeldig",
            confidenceScore = 0,
            riskLevel = RiskLevel.HIGH,
            reasoning = "Kon geen voorspelling maken.",
            keyFactor = "Onbekend"
        )
    }
}
