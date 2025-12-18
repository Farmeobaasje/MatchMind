package com.Lyno.matchmindai.domain.model

import kotlinx.serialization.Serializable

/**
 * Domain model for betting odds data.
 * Contains comprehensive odds information for a specific match,
 * including match result odds, over/under odds, and both teams to score odds.
 * 
 * This model is used in MatchDetail for displaying betting information
 * and in AI analysis for providing betting recommendations.
 */
@Serializable
data class OddsData(
    val fixtureId: Int,
    val homeTeam: String,
    val awayTeam: String,
    val bookmakerName: String,
    val homeWinOdds: Double?,
    val drawOdds: Double?,
    val awayWinOdds: Double?,
    val overUnderOdds: Map<String, Double>?,
    val bothTeamsToScoreOdds: Map<String, Double>?,
    val valueRating: Double,
    val safetyRating: Double,
    val highestOdds: Double?,
    val lastUpdated: String
) {
    /**
     * Get the best odds among home, draw, and away.
     */
    val bestOdds: Double?
        get() = listOfNotNull(homeWinOdds, drawOdds, awayWinOdds).maxOrNull()

    /**
     * Get the lowest odds among home, draw, and away.
     */
    val lowestOdds: Double?
        get() = listOfNotNull(homeWinOdds, drawOdds, awayWinOdds).minOrNull()

    /**
     * Check if odds are available for this match.
     */
    val hasOdds: Boolean
        get() = homeWinOdds != null || drawOdds != null || awayWinOdds != null

    /**
     * Get the implied probability for home win.
     */
    val homeWinProbability: Double?
        get() = homeWinOdds?.let { 1.0 / it }

    /**
     * Get the implied probability for draw.
     */
    val drawProbability: Double?
        get() = drawOdds?.let { 1.0 / it }

    /**
     * Get the implied probability for away win.
     */
    val awayWinProbability: Double?
        get() = awayWinOdds?.let { 1.0 / it }

    /**
     * Get the total implied probability (should be > 1.0 due to bookmaker margin).
     */
    val totalImpliedProbability: Double
        get() = listOfNotNull(homeWinProbability, drawProbability, awayWinProbability).sum()

    /**
     * Get the bookmaker margin (overround).
     */
    val bookmakerMargin: Double
        get() = totalImpliedProbability - 1.0

    /**
     * Get a beginner-friendly betting recommendation.
     */
    val bettingRecommendation: String
        get() = when {
            safetyRating >= 80 && valueRating >= 60 -> "⭐ Sterke weddenschap - Goede balans tussen veiligheid en waarde"
            safetyRating >= 70 -> "✅ Veilige weddenschap - Laag risico, goed voor beginners"
            valueRating >= 70 -> "💰 Waarde weddenschap - Hoger potentieel rendement, matig risico"
            else -> "⚠️ Voorzichtigheid geadviseerd - Overweeg andere wedstrijden"
        }

    /**
     * Get a simple odds summary for display.
     */
    val oddsSummary: String
        get() = buildString {
            if (homeWinOdds != null) append("${homeTeam}: ${String.format("%.2f", homeWinOdds)}")
            if (drawOdds != null) {
                if (isNotEmpty()) append(", ")
                append("Gelijkspel: ${String.format("%.2f", drawOdds)}")
            }
            if (awayWinOdds != null) {
                if (isNotEmpty()) append(", ")
                append("${awayTeam}: ${String.format("%.2f", awayWinOdds)}")
            }
        }

    /**
     * Get the value bet (highest value rating).
     */
    val valueBet: String
        get() = when {
            homeWinOdds != null && valueRating >= 60 -> "${homeTeam} wint"
            drawOdds != null && valueRating >= 60 -> "Gelijkspel"
            awayWinOdds != null && valueRating >= 60 -> "${awayTeam} wint"
            else -> "Geen duidelijke waarde weddenschap"
        }

    /**
     * Get the safe bet (highest safety rating).
     */
    val safeBet: String
        get() = when {
            homeWinOdds != null && safetyRating >= 70 -> "${homeTeam} wint"
            drawOdds != null && safetyRating >= 70 -> "Gelijkspel"
            awayWinOdds != null && safetyRating >= 70 -> "${awayTeam} wint"
            else -> "Geen duidelijke veilige weddenschap"
        }

    companion object {
        /**
         * Create an empty OddsData instance for matches without odds.
         */
        fun empty(fixtureId: Int, homeTeam: String, awayTeam: String): OddsData {
            return OddsData(
                fixtureId = fixtureId,
                homeTeam = homeTeam,
                awayTeam = awayTeam,
                bookmakerName = "Geen boekmaker",
                homeWinOdds = null,
                drawOdds = null,
                awayWinOdds = null,
                overUnderOdds = null,
                bothTeamsToScoreOdds = null,
                valueRating = 0.0,
                safetyRating = 0.0,
                highestOdds = null,
                lastUpdated = ""
            )
        }
    }
}
