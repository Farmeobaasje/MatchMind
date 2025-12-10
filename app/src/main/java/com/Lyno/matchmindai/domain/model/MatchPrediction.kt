package com.Lyno.matchmindai.domain.model

/**
 * Domain model representing a match prediction.
 * This is a pure Kotlin class with no Android dependencies.
 */
data class MatchPrediction(
    val homeTeam: String,
    val awayTeam: String,
    val winner: String,
    val confidenceScore: Int, // 0-100
    val riskLevel: RiskLevel,
    val reasoning: String,
    val keyFactor: String,
    val recentMatches: List<String> = emptyList()
) {
    enum class RiskLevel {
        LOW, MEDIUM, HIGH
    }

    /**
     * The predicted winner with confidence percentage.
     */
    val winnerWithConfidence: String
        get() = "$winner ($confidenceScore%)"

    /**
     * Whether the prediction indicates a home win.
     */
    val isHomeWin: Boolean
        get() = winner.equals(homeTeam, ignoreCase = true)

    /**
     * Whether the prediction indicates an away win.
     */
    val isAwayWin: Boolean
        get() = winner.equals(awayTeam, ignoreCase = true)

    /**
     * Whether the prediction indicates a draw.
     */
    val isDraw: Boolean
        get() = winner.equals("Draw", ignoreCase = true) ||
                winner.equals("Gelijkspel", ignoreCase = true)

    companion object {
        /**
         * Creates an invalid prediction for error cases.
         */
        fun invalid(): MatchPrediction = MatchPrediction(
            homeTeam = "",
            awayTeam = "",
            winner = "Ongeldig",
            confidenceScore = 0,
            riskLevel = RiskLevel.HIGH,
            reasoning = "Kon geen voorspelling maken.",
            keyFactor = "Onbekend",
            recentMatches = emptyList()
        )
    }
}
