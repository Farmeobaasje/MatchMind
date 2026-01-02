package com.Lyno.matchmindai.domain.prediction

import com.Lyno.matchmindai.domain.model.DataSource
import com.Lyno.matchmindai.domain.model.OracleAnalysis
import kotlin.math.roundToInt

/**
 * The "Hard Reality" math engine for deterministic, fact-based predictions.
 * This calculator bases predictions strictly on standings (rank, points) and goal difference,
 * ignoring complex match history loops to identify clear favorites.
 *
 * Algorithm:
 * 1. Power Score = (100 - (Rank * 3)) + (PointsPerGame * 10) + (GoalDiffPerGame * 5)
 * 2. Home Bonus: Add +10 to Home Power
 * 3. Delta = HomePower - AwayPower
 * 4. Prediction Logic:
 *    - Delta < -30: Strong Away Win (0-3)
 *    - Delta < -15: Away Win (1-2)
 *    - Delta > +30: Strong Home Win (3-0)
 *    - Delta > +15: Home Win (2-1)
 *    - Else: Close Game/Draw (1-1)
 */
object PowerRankCalculator {

    private const val HOME_BONUS = 10
    private const val RANK_MULTIPLIER = 3
    private const val POINTS_PER_GAME_MULTIPLIER = 10
    private const val GOAL_DIFF_PER_GAME_MULTIPLIER = 5
    private const val MAX_RANK = 20 // Default mid-table rank for missing teams

    // Prediction thresholds
    private const val STRONG_WIN_THRESHOLD = 30
    private const val WIN_THRESHOLD = 15

    /**
     * Calculate Oracle analysis based on team standings data.
     *
     * @param homeRank Home team rank (1 = best, higher = worse)
     * @param awayRank Away team rank (1 = best, higher = worse)
     * @param homePoints Home team total points
     * @param awayPoints Away team total points
     * @param homeGoalsDiff Home team goal difference (goals for - goals against)
     * @param awayGoalsDiff Away team goal difference (goals for - goals against)
     * @param gamesPlayed Number of games played (same for both teams in same league)
     * @param standingsSource Source of the standings data used for this prediction
     * @param confidenceAdjustment Multiplier applied to confidence based on data source quality (0.0-1.0)
     * @return OracleAnalysis with prediction, confidence, reasoning, and power scores
     */
    fun calculate(
        homeRank: Int,
        awayRank: Int,
        homePoints: Int,
        awayPoints: Int,
        homeGoalsDiff: Int,
        awayGoalsDiff: Int,
        gamesPlayed: Int,
        standingsSource: DataSource = DataSource.API_OFFICIAL,
        confidenceAdjustment: Float = 1.0f
    ): OracleAnalysis {
        require(gamesPlayed > 0) { "Games played must be positive" }
        require(homeRank > 0 && awayRank > 0) { "Ranks must be positive" }
        require(confidenceAdjustment in 0.0f..1.0f) { "Confidence adjustment must be between 0.0 and 1.0" }

        // Calculate power scores
        val homePower = calculatePowerScore(
            rank = homeRank,
            points = homePoints,
            goalsDiff = homeGoalsDiff,
            gamesPlayed = gamesPlayed,
            isHome = true
        )

        val awayPower = calculatePowerScore(
            rank = awayRank,
            points = awayPoints,
            goalsDiff = awayGoalsDiff,
            gamesPlayed = gamesPlayed,
            isHome = false
        )

        val delta = homePower - awayPower

        // Generate prediction based on delta
        val (prediction, baseConfidence, reasoning) = generatePrediction(
            homeRank = homeRank,
            awayRank = awayRank,
            homePower = homePower,
            awayPower = awayPower,
            delta = delta
        )

        // Apply confidence adjustment based on data source
        val adjustedConfidence = (baseConfidence * confidenceAdjustment).toInt().coerceIn(0, 100)

        return OracleAnalysis(
            prediction = prediction,
            confidence = adjustedConfidence,
            reasoning = reasoning,
            homePowerScore = homePower,
            awayPowerScore = awayPower,
            standingsSource = standingsSource,
            confidenceAdjustment = confidenceAdjustment
        )
    }

    /**
     * Calculate power score for a team.
     * Formula: Power = (100 - (Rank * 3)) + (PointsPerGame * 10) + (GoalDiffPerGame * 5)
     * Home Bonus: +10 if isHome = true
     */
    private fun calculatePowerScore(
        rank: Int,
        points: Int,
        goalsDiff: Int,
        gamesPlayed: Int,
        isHome: Boolean
    ): Int {
        val pointsPerGame = points.toFloat() / gamesPlayed
        val goalDiffPerGame = goalsDiff.toFloat() / gamesPlayed

        val basePower = (100 - (rank * RANK_MULTIPLIER)) +
                (pointsPerGame * POINTS_PER_GAME_MULTIPLIER).roundToInt() +
                (goalDiffPerGame * GOAL_DIFF_PER_GAME_MULTIPLIER).roundToInt()

        val homeBonus = if (isHome) HOME_BONUS else 0

        // Ensure power score stays within reasonable bounds (0-200)
        return (basePower + homeBonus).coerceIn(0, 200)
    }

    /**
     * Generate prediction, confidence, and reasoning based on power delta.
     */
    private fun generatePrediction(
        homeRank: Int,
        awayRank: Int,
        homePower: Int,
        awayPower: Int,
        delta: Int
    ): Triple<String, Int, String> {
        val rankDifference = awayRank - homeRank // Positive if home is better ranked

        return when {
            delta < -STRONG_WIN_THRESHOLD -> Triple(
                "0-3",
                90,
                "Strong away win. ${getTeamName("Away")} (#$awayRank) is ${-rankDifference} ranks higher than ${getTeamName("Home")} (#$homeRank) and has significantly better form (Power: $awayPower vs $homePower)."
            )

            delta < -WIN_THRESHOLD -> Triple(
                "1-2",
                75,
                "Away win. ${getTeamName("Away")} (#$awayRank) is ${-rankDifference} ranks higher than ${getTeamName("Home")} (#$homeRank) and has better form (Power: $awayPower vs $homePower)."
            )

            delta > STRONG_WIN_THRESHOLD -> Triple(
                "3-0",
                90,
                "Strong home win. ${getTeamName("Home")} (#$homeRank) is $rankDifference ranks higher than ${getTeamName("Away")} (#$awayRank) and has significantly better form (Power: $homePower vs $awayPower)."
            )

            delta > WIN_THRESHOLD -> Triple(
                "2-1",
                75,
                "Home win. ${getTeamName("Home")} (#$homeRank) is $rankDifference ranks higher than ${getTeamName("Away")} (#$awayRank) and has better form (Power: $homePower vs $awayPower)."
            )

            else -> Triple(
                "1-1",
                60,
                "Close game/draw. Teams are closely matched (Power: $homePower vs $awayPower). ${getTeamName("Home")} (#$homeRank) and ${getTeamName("Away")} (#$awayRank) have similar strength."
            )
        }
    }

    /**
     * Helper to get team name for reasoning (will be replaced with actual team names in repository).
     */
    private fun getTeamName(teamType: String): String = when (teamType) {
        "Home" -> "Home team"
        "Away" -> "Away team"
        else -> "Team"
    }

    /**
     * Calculate default power score for a missing team (mid-table assumption).
     */
    fun calculateDefaultPowerScore(isHome: Boolean = false): Int {
        return calculatePowerScore(
            rank = MAX_RANK,
            points = 30, // Mid-table points assumption
            goalsDiff = 0,
            gamesPlayed = 20,
            isHome = isHome
        )
    }

    /**
     * Get the rank difference explanation for reasoning.
     */
    fun getRankDifferenceExplanation(homeRank: Int, awayRank: Int): String {
        val difference = awayRank - homeRank
        return when {
            difference > 10 -> "massively higher"
            difference > 5 -> "significantly higher"
            difference > 2 -> "higher"
            difference < -10 -> "massively lower"
            difference < -5 -> "significantly lower"
            difference < -2 -> "lower"
            else -> "similar"
        }
    }
}
