package com.Lyno.matchmindai.domain.prediction

import com.Lyno.matchmindai.data.dto.football.PlayerStatisticsDto
import com.Lyno.matchmindai.domain.model.PlayerScoringProbability
import kotlin.math.max
import kotlin.math.min

/**
 * Calculator for player scoring probabilities based on statistics and context.
 * Combines base statistics with contextual factors to predict scoring likelihood.
 */
class PlayerScoringCalculator {

    companion object {
        private const val MAX_GOALS_PER_90 = 1.0 // Maximum goals per 90 for normalization
        private const val HOME_ADVANTAGE_MULTIPLIER = 1.2 // +20% for home players
        private const val FORM_MULTIPLIER_MAX = 1.3 // +30% for in-form players
        private const val MINUTES_THRESHOLD = 30.0 // Minimum minutes per game to be considered regular
        private const val POSITION_MULTIPLIER_FORWARD = 1.2
        private const val POSITION_MULTIPLIER_MIDFIELDER = 1.0
        private const val POSITION_MULTIPLIER_DEFENDER = 0.5
        private const val POSITION_MULTIPLIER_GOALKEEPER = 0.1
    }

    /**
     * Calculates scoring probability for a player.
     *
     * @param playerStats Player statistics DTO
     * @param isHome True if player is playing at home
     * @param isPlaying True if player is in starting lineup
     * @param opponentStrength Opponent defensive strength (0-100, higher = stronger defense)
     * @param recentGoals List of goals scored in last 5 matches (most recent first)
     * @param scoringStreak Number of consecutive matches with a goal
     * @return PlayerScoringProbability with calculated probabilities
     */
    fun calculateScoringProbability(
        playerStats: PlayerStatisticsDto,
        isHome: Boolean,
        isPlaying: Boolean,
        opponentStrength: Int = 50,
        recentGoals: List<Int> = emptyList(),
        scoringStreak: Int = 0
    ): PlayerScoringProbability {
        // Get the most recent season statistics
        val seasonStats = playerStats.statistics.firstOrNull()
            ?: return createDefaultProbability(playerStats, isHome, isPlaying, recentGoals, scoringStreak)

        val games = seasonStats.games
        val goals = seasonStats.goals
        val shots = seasonStats.shots

        // Calculate base statistics
        val minutesPlayed = games?.minutes ?: 0
        val appearances = games?.appearances ?: 1
        val totalGoals = goals?.total ?: 0
        val shotsOnTarget = shots?.on ?: 0
        val totalShots = shots?.total ?: 1

        // Calculate per-90 statistics
        val goalsPer90 = calculateGoalsPer90(totalGoals, minutesPlayed)
        val shotsOnTargetPer90 = calculateShotsOnTargetPer90(shotsOnTarget, minutesPlayed)
        val minutesPerGame = calculateMinutesPerGame(minutesPlayed, appearances)

        // Calculate base probability (0-100)
        val baseProbability = calculateBaseProbability(goalsPer90)

        // Apply context factors
        val adjustedProbability = applyContextFactors(
            baseProbability = baseProbability,
            isHome = isHome,
            isPlaying = isPlaying,
            opponentStrength = opponentStrength,
            position = games?.position ?: "M",
            minutesPerGame = minutesPerGame,
            recentGoals = recentGoals,
            scoringStreak = scoringStreak
        )

        return PlayerScoringProbability(
            playerId = playerStats.player.id,
            playerName = playerStats.player.name,
            teamId = seasonStats.team.id,
            position = games.position ?: "M",
            baseProbability = baseProbability,
            adjustedProbability = adjustedProbability,
            goalsPer90 = goalsPer90,
            shotsOnTargetPer90 = shotsOnTargetPer90,
            minutesPerGame = minutesPerGame,
            recentForm = recentGoals,
            isPlaying = isPlaying,
            homeAdvantage = isHome,
            expectedGoalsPer90 = null, // Could be calculated from xG data if available
            scoringStreak = scoringStreak
        )
    }

    /**
     * Creates a default probability for players with insufficient data.
     */
    private fun createDefaultProbability(
        playerStats: PlayerStatisticsDto,
        isHome: Boolean,
        isPlaying: Boolean,
        recentGoals: List<Int>,
        scoringStreak: Int
    ): PlayerScoringProbability {
        return PlayerScoringProbability(
            playerId = playerStats.player.id,
            playerName = playerStats.player.name,
            teamId = 0,
            position = "M",
            baseProbability = 10.0,
            adjustedProbability = applyContextFactors(
                baseProbability = 10.0,
                isHome = isHome,
                isPlaying = isPlaying,
                opponentStrength = 50,
                position = "M",
                minutesPerGame = 45.0,
                recentGoals = recentGoals,
                scoringStreak = scoringStreak
            ),
            goalsPer90 = 0.1,
            shotsOnTargetPer90 = 0.5,
            minutesPerGame = 45.0,
            recentForm = recentGoals,
            isPlaying = isPlaying,
            homeAdvantage = isHome,
            scoringStreak = scoringStreak
        )
    }

    /**
     * Calculates goals per 90 minutes.
     */
    private fun calculateGoalsPer90(goals: Int, minutesPlayed: Int): Double {
        if (minutesPlayed <= 0) return 0.0
        return (goals.toDouble() / minutesPlayed) * 90.0
    }

    /**
     * Calculates shots on target per 90 minutes.
     */
    private fun calculateShotsOnTargetPer90(shotsOnTarget: Int, minutesPlayed: Int): Double {
        if (minutesPlayed <= 0) return 0.0
        return (shotsOnTarget.toDouble() / minutesPlayed) * 90.0
    }

    /**
     * Calculates average minutes per game.
     */
    private fun calculateMinutesPerGame(minutesPlayed: Int, appearances: Int): Double {
        if (appearances <= 0) return 0.0
        return minutesPlayed.toDouble() / appearances
    }

    /**
     * Calculates base probability from goals per 90.
     * Normalizes to 0-100 scale where 1.0 goals/90 = 100 probability.
     */
    private fun calculateBaseProbability(goalsPer90: Double): Double {
        return minOf(100.0, (goalsPer90 / MAX_GOALS_PER_90) * 100.0)
    }

    /**
     * Applies all context factors to base probability.
     */
    private fun applyContextFactors(
        baseProbability: Double,
        isHome: Boolean,
        isPlaying: Boolean,
        opponentStrength: Int,
        position: String,
        minutesPerGame: Double,
        recentGoals: List<Int>,
        scoringStreak: Int
    ): Double {
        var adjustedProbability = baseProbability

        // 1. Home advantage
        adjustedProbability *= if (isHome) HOME_ADVANTAGE_MULTIPLIER else 1.0

        // 2. Playing status
        adjustedProbability *= if (isPlaying) 1.0 else 0.0

        // 3. Position multiplier
        adjustedProbability *= getPositionMultiplier(position)

        // 4. Minutes played factor
        val minutesFactor = calculateMinutesFactor(minutesPerGame)
        adjustedProbability *= minutesFactor

        // 5. Opponent strength factor (stronger defense = lower probability)
        val defenseFactor = calculateDefenseFactor(opponentStrength)
        adjustedProbability *= defenseFactor

        // 6. Form factor
        val formFactor = calculateFormFactor(recentGoals, scoringStreak)
        adjustedProbability *= formFactor

        // Ensure probability stays within bounds
        return adjustedProbability.coerceIn(0.0, 100.0)
    }

    /**
     * Gets position-based multiplier.
     */
    private fun getPositionMultiplier(position: String): Double {
        return when {
            position.startsWith("F") || position == "FW" || position == "ST" || position == "CF" -> 
                POSITION_MULTIPLIER_FORWARD
            position.startsWith("M") || position == "MF" || position == "CM" || position == "AM" -> 
                POSITION_MULTIPLIER_MIDFIELDER
            position.startsWith("D") || position == "DF" || position == "CB" || position == "FB" -> 
                POSITION_MULTIPLIER_DEFENDER
            position.startsWith("G") || position == "GK" -> 
                POSITION_MULTIPLIER_GOALKEEPER
            else -> POSITION_MULTIPLIER_MIDFIELDER
        }
    }

    /**
     * Calculates minutes factor based on average minutes per game.
     */
    private fun calculateMinutesFactor(minutesPerGame: Double): Double {
        return when {
            minutesPerGame >= 75.0 -> 1.0
            minutesPerGame >= 60.0 -> 0.9
            minutesPerGame >= 45.0 -> 0.8
            minutesPerGame >= 30.0 -> 0.6
            minutesPerGame >= 15.0 -> 0.4
            else -> 0.2
        }
    }

    /**
     * Calculates defense factor based on opponent strength.
     */
    private fun calculateDefenseFactor(opponentStrength: Int): Double {
        // Normalize opponent strength to 0.7-1.3 range
        // 0 strength = 1.3 factor (weak defense = higher scoring chance)
        // 100 strength = 0.7 factor (strong defense = lower scoring chance)
        return 1.3 - (opponentStrength / 100.0 * 0.6)
    }

    /**
     * Calculates form factor based on recent goals.
     */
    private fun calculateFormFactor(recentGoals: List<Int>, scoringStreak: Int): Double {
        val recentGoalsSum = recentGoals.sum()
        
        // Base form from recent goals
        val recentFormFactor = when {
            recentGoalsSum >= 5 -> 1.3
            recentGoalsSum >= 3 -> 1.2
            recentGoalsSum >= 1 -> 1.1
            else -> 1.0
        }
        
        // Streak bonus
        val streakFactor = when {
            scoringStreak >= 3 -> 1.2
            scoringStreak >= 2 -> 1.1
            else -> 1.0
        }
        
        return min(FORM_MULTIPLIER_MAX, recentFormFactor * streakFactor)
    }

    /**
     * Filters and sorts players by adjusted probability.
     *
     * @param players List of player scoring probabilities
     * @param minMinutes Minimum minutes per game to include
     * @param topN Number of top players to return
     * @return Sorted list of top players
     */
    fun getTopScorers(
        players: List<PlayerScoringProbability>,
        minMinutes: Double = 30.0,
        topN: Int = 5
    ): List<PlayerScoringProbability> {
        return players
            .filter { it.minutesPerGame >= minMinutes && it.isPlaying }
            .sortedByDescending { it.adjustedProbability }
            .take(topN)
    }

    /**
     * Calculates team expected goals from player probabilities.
     *
     * @param players List of player scoring probabilities
     * @return Total expected goals for the team
     */
    fun calculateTeamExpectedGoals(players: List<PlayerScoringProbability>): Double {
        return players.sumOf { player ->
            // Convert probability to expected goals contribution
            // 100% probability = 0.5 expected goals per player (max)
            (player.adjustedProbability / 100.0) * 0.5
        }
    }

    /**
     * Calculates lineup strength based on player availability and form.
     *
     * @param players List of player scoring probabilities
     * @return Lineup strength (0-100)
     */
    fun calculateLineupStrength(players: List<PlayerScoringProbability>): Int {
        if (players.isEmpty()) return 50
        
        val playingPlayers = players.filter { it.isPlaying }
        if (playingPlayers.isEmpty()) return 30
        
        val averageProbability = playingPlayers.map { it.adjustedProbability }.average()
        val formRating = playingPlayers.map { it.formRating }.average()
        val minutesFactor = playingPlayers.map { it.minutesPerGame / 90.0 }.average()
        
        return ((averageProbability * 0.4 + formRating * 0.3 + minutesFactor * 100 * 0.3)).toInt()
    }
}
