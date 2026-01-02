package com.Lyno.matchmindai.domain.model

/**
 * Represents a player's probability of scoring in a match.
 * Combines base statistics with contextual factors.
 *
 * @property playerId Unique player identifier
 * @property playerName Player's name
 * @property teamId Team identifier
 * @property position Player position (F, M, D, G)
 * @property baseProbability Base scoring probability (0-100) based on goals per 90
 * @property adjustedProbability Adjusted probability (0-100) with context factors
 * @property goalsPer90 Goals per 90 minutes played
 * @property shotsOnTargetPer90 Shots on target per 90 minutes
 * @property minutesPerGame Average minutes played per appearance
 * @property recentForm List of goals scored in last 5 matches (most recent first)
 * @property isPlaying True if player is in starting lineup
 * @property homeAdvantage True if player is playing at home
 * @property expectedGoalsPer90 Expected goals (xG) per 90 minutes (if available)
 * @property scoringStreak Number of consecutive matches with a goal
 */
data class PlayerScoringProbability(
    val playerId: Int,
    val playerName: String,
    val teamId: Int,
    val position: String,
    val baseProbability: Double,
    val adjustedProbability: Double,
    val goalsPer90: Double,
    val shotsOnTargetPer90: Double,
    val minutesPerGame: Double,
    val recentForm: List<Int>,
    val isPlaying: Boolean,
    val homeAdvantage: Boolean,
    val expectedGoalsPer90: Double? = null,
    val scoringStreak: Int = 0
) {
    init {
        require(baseProbability in 0.0..100.0) { "Base probability must be between 0 and 100" }
        require(adjustedProbability in 0.0..100.0) { "Adjusted probability must be between 0 and 100" }
        require(goalsPer90 >= 0.0) { "Goals per 90 must be non-negative" }
        require(shotsOnTargetPer90 >= 0.0) { "Shots on target per 90 must be non-negative" }
        require(minutesPerGame in 0.0..90.0) { "Minutes per game must be between 0 and 90" }
    }

    /**
     * Returns true if player is a forward (likely to score).
     */
    val isForward: Boolean
        get() = position.startsWith("F") || position == "FW" || position == "ST" || position == "CF"

    /**
     * Returns true if player is a midfielder (moderate scoring chance).
     */
    val isMidfielder: Boolean
        get() = position.startsWith("M") || position == "MF" || position == "CM" || position == "AM"

    /**
     * Returns true if player is in good form (scored in recent matches).
     */
    val isInForm: Boolean
        get() = recentForm.sum() >= 2 || scoringStreak >= 2

    /**
     * Returns the form rating (0-100) based on recent goals.
     */
    val formRating: Double
        get() = minOf(100.0, recentForm.sum() * 20.0)

    /**
     * Returns a human-readable description of scoring probability.
     */
    val probabilityDescription: String
        get() = when {
            adjustedProbability >= 70 -> "Very High"
            adjustedProbability >= 50 -> "High"
            adjustedProbability >= 30 -> "Moderate"
            adjustedProbability >= 15 -> "Low"
            else -> "Very Low"
        }

    /**
     * Returns the expected contribution to team lambda (expected goals).
     */
    val lambdaContribution: Double
        get() = adjustedProbability / 100.0 * 0.5 // Max 0.5 expected goals per top scorer
}

/**
 * Represents team player statistics for a match.
 *
 * @property topScorers List of players with scoring probabilities
 * @property totalExpectedGoals Sum of expected goals from all players
 * @property averageMinutesPlayed Average minutes played per player
 * @property shotsOnTargetRatio Ratio of shots on target to total shots
 * @property lineupStrength Overall lineup strength (0-100)
 */
data class TeamPlayerStatistics(
    val topScorers: List<PlayerScoringProbability>,
    val totalExpectedGoals: Double,
    val averageMinutesPlayed: Double,
    val shotsOnTargetRatio: Double,
    val lineupStrength: Int
) {
    /**
     * Returns the top N scorers by adjusted probability.
     */
    fun getTopScorers(n: Int = 3): List<PlayerScoringProbability> {
        return topScorers.sortedByDescending { it.adjustedProbability }.take(n)
    }

    /**
     * Returns the most likely scorer.
     */
    val mostLikelyScorer: PlayerScoringProbability?
        get() = topScorers.maxByOrNull { it.adjustedProbability }

    /**
     * Returns true if team has strong attacking options.
     */
    val hasStrongAttack: Boolean
        get() = topScorers.any { it.adjustedProbability >= 50.0 } && totalExpectedGoals >= 1.5
}

/**
 * Enhanced Oracle Analysis with player scoring predictions.
 *
 * @property baseAnalysis Original OracleAnalysis
 * @property homeScorers Home team player statistics
 * @property awayScorers Away team player statistics
 * @property homeMostLikelyScorer Most likely home scorer
 * @property awayMostLikelyScorer Most likely away scorer
 * @property enrichedTesseract Enhanced Tesseract result with player data
 */
data class EnrichedOracleAnalysis(
    val baseAnalysis: OracleAnalysis,
    val homeScorers: TeamPlayerStatistics,
    val awayScorers: TeamPlayerStatistics,
    val homeMostLikelyScorer: PlayerScoringProbability?,
    val awayMostLikelyScorer: PlayerScoringProbability?,
    val enrichedTesseract: Any? = null
) {
    /**
     * Returns true if both teams have likely scorers.
     */
    val bothTeamsHaveScorers: Boolean
        get() = homeMostLikelyScorer != null && awayMostLikelyScorer != null

    /**
     * Returns the combined expected goals from top scorers.
     */
    val totalExpectedGoalsFromScorers: Double
        get() = (homeScorers.totalExpectedGoals + awayScorers.totalExpectedGoals) / 2.0
}
