package com.Lyno.matchmindai.domain.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing a historical football fixture for Dixon-Coles prediction model.
 * Contains only the essential data needed for statistical analysis.
 * 
 * @property fixtureId The unique identifier of the fixture
 * @property homeTeamId The ID of the home team
 * @property awayTeamId The ID of the away team
 * @property homeTeamName The name of the home team
 * @property awayTeamName The name of the away team
 * @property homeGoals Number of goals scored by home team
 * @property awayGoals Number of goals scored by away team
 * @property date The date of the fixture in ISO format (YYYY-MM-DD)
 * @property leagueId The ID of the league
 * @property leagueName The name of the league
 * @property season The season year (e.g., 2025)
 * @property status The match status (must be "FT" for finished matches)
 */
@Serializable
data class HistoricalFixture(
    val fixtureId: Int,
    val homeTeamId: Int,
    val awayTeamId: Int,
    val homeTeamName: String,
    val awayTeamName: String,
    val homeGoals: Int,
    val awayGoals: Int,
    val date: String,
    val leagueId: Int,
    val leagueName: String,
    val season: Int,
    val status: String
) {
    /**
     * Check if this is a valid historical fixture for Dixon-Coles analysis.
     * Valid fixtures must be finished (status = "FT") and have valid goal counts.
     */
    val isValidForAnalysis: Boolean
        get() = status == "FT" && homeGoals >= 0 && awayGoals >= 0

    /**
     * Total goals scored in the match.
     */
    val totalGoals: Int
        get() = homeGoals + awayGoals

    /**
     * Goal difference (home - away).
     */
    val goalDifference: Int
        get() = homeGoals - awayGoals

    /**
     * Check if home team won.
     */
    val isHomeWin: Boolean
        get() = homeGoals > awayGoals

    /**
     * Check if away team won.
     */
    val isAwayWin: Boolean
        get() = awayGoals > homeGoals

    /**
     * Check if match was a draw.
     */
    val isDraw: Boolean
        get() = homeGoals == awayGoals

    /**
     * Get match result as a string (e.g., "2-1", "0-0").
     */
    val result: String
        get() = "$homeGoals-$awayGoals"

    companion object {
        /**
         * Creates an invalid historical fixture for error cases.
         */
        fun invalid(): HistoricalFixture = HistoricalFixture(
            fixtureId = 0,
            homeTeamId = 0,
            awayTeamId = 0,
            homeTeamName = "",
            awayTeamName = "",
            homeGoals = 0,
            awayGoals = 0,
            date = "",
            leagueId = 0,
            leagueName = "",
            season = 0,
            status = ""
        )

        /**
         * Creates a list of historical fixtures from raw data.
         * Filters out invalid fixtures (non-FT status, technical 3-0 wins, etc.).
         */
        fun createValidFixtures(
            fixtures: List<HistoricalFixture>,
            filterTechnicalWins: Boolean = true
        ): List<HistoricalFixture> {
            return fixtures.filter { fixture ->
                // Basic validation
                if (!fixture.isValidForAnalysis) return@filter false

                // Filter out technical 3-0 wins (common in cup competitions)
                if (filterTechnicalWins && fixture.homeGoals == 3 && fixture.awayGoals == 0) {
                    return@filter false
                }

                // Filter out matches with unrealistic goal counts (e.g., > 10 goals)
                if (fixture.totalGoals > 10) {
                    return@filter false
                }

                true
            }
        }
    }
}

/**
 * Data class representing Dixon-Coles prediction results.
 * Contains win probabilities and expected goals.
 */
@Serializable
data class DixonColesPrediction(
    val fixtureId: Int,
    val homeTeam: String,
    val awayTeam: String,
    val homeWinProbability: Double,    // 0.0 - 1.0
    val drawProbability: Double,       // 0.0 - 1.0
    val awayWinProbability: Double,    // 0.0 - 1.0
    val expectedHomeGoals: Double,     // Expected goals for home team
    val expectedAwayGoals: Double,     // Expected goals for away team
    val leagueAverageHomeGoals: Double, // League average home goals per match
    val leagueAverageAwayGoals: Double, // League average away goals per match
    val homeAttackStrength: Double,    // Home team attack strength
    val awayAttackStrength: Double,    // Away team attack strength
    val homeDefenseStrength: Double,   // Home team defense strength
    val awayDefenseStrength: Double,   // Away team defense strength
    val calculationTimestamp: Long = System.currentTimeMillis()
) {
    /**
     * Get win probabilities as percentages (0-100).
     */
    val homeWinPercentage: Int get() = (homeWinProbability * 100).toInt()
    val drawPercentage: Int get() = (drawProbability * 100).toInt()
    val awayWinPercentage: Int get() = (awayWinProbability * 100).toInt()

    /**
     * Get expected score as formatted string (e.g., "1.4 - 0.9").
     */
    val expectedScore: String get() = "%.1f - %.1f".format(expectedHomeGoals, expectedAwayGoals)

    /**
     * Get most likely result based on probabilities.
     */
    val mostLikelyResult: String
        get() = when {
            homeWinProbability >= drawProbability && homeWinProbability >= awayWinProbability -> "Home Win"
            drawProbability >= homeWinProbability && drawProbability >= awayWinProbability -> "Draw"
            else -> "Away Win"
        }

    /**
     * Get confidence level (highest probability).
     */
    val confidence: Double
        get() = maxOf(homeWinProbability, drawProbability, awayWinProbability)

    /**
     * Check if prediction is valid (probabilities sum to ~1.0).
     */
    val isValid: Boolean
        get() {
            val sum = homeWinProbability + drawProbability + awayWinProbability
            return sum in 0.95..1.05 // Allow small rounding errors
        }
}
