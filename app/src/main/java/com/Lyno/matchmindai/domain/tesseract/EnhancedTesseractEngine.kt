package com.Lyno.matchmindai.domain.tesseract

import com.Lyno.matchmindai.domain.model.EnrichedOracleAnalysis
import com.Lyno.matchmindai.domain.model.OracleAnalysis
import com.Lyno.matchmindai.domain.model.PlayerScoringProbability
import com.Lyno.matchmindai.domain.model.TeamPlayerStatistics
import com.Lyno.matchmindai.domain.prediction.PlayerScoringCalculator
import kotlin.math.exp
import kotlin.math.pow
import kotlin.random.Random

/**
 * Enhanced Tesseract Engine that incorporates player scoring probabilities.
 * Extends the Monte Carlo simulation with individual player scoring models.
 */
class EnhancedTesseractEngine {

    companion object {
        private const val SIMULATION_COUNT = 10000
        private const val PLAYER_GOAL_PROBABILITY_THRESHOLD = 0.3 // 30% chance to score per simulation
        private const val TEAM_LAMBDA_BASE = 1.5 // Base lambda for team expected goals
        private const val PLAYER_CONTRIBUTION_WEIGHT = 0.4 // Weight of player contributions vs team lambda
    }

    private val playerScoringCalculator = PlayerScoringCalculator()

    /**
     * Enhanced Tesseract result with player scoring details.
     */
    data class EnhancedTesseractResult(
        val baseResult: TesseractResult,
        val homeScorerProbabilities: Map<String, Double>, // Player name -> scoring probability
        val awayScorerProbabilities: Map<String, Double>,
        val mostLikelyHomeScorer: String?,
        val mostLikelyAwayScorer: String?,
        val homeTeamExpectedGoals: Double,
        val awayTeamExpectedGoals: Double,
        val bothTeamsToScoreProbability: Double,
        val over25GoalsProbability: Double
    )

    /**
     * Runs enhanced Monte Carlo simulation with player scoring models.
     *
     * @param oracleAnalysis Base Oracle analysis
     * @param homeScorers Home team player statistics
     * @param awayScorers Away team player statistics
     * @return Enhanced Tesseract result with player scoring insights
     */
    fun runEnhancedSimulation(
        oracleAnalysis: OracleAnalysis,
        homeScorers: TeamPlayerStatistics,
        awayScorers: TeamPlayerStatistics
    ): EnhancedTesseractResult {
        // Get base Tesseract result
        val baseResult = runBaseSimulation(oracleAnalysis)

        // Calculate player scoring probabilities
        val homeScorerProbs = calculatePlayerScoringProbabilities(homeScorers.topScorers)
        val awayScorerProbs = calculatePlayerScoringProbabilities(awayScorers.topScorers)

        // Run player-enhanced simulations
        val simulationResults = runPlayerEnhancedSimulations(
            homeScorers = homeScorers.topScorers,
            awayScorers = awayScorers.topScorers,
            baseHomeLambda = baseResult.homeLambda,
            baseAwayLambda = baseResult.awayLambda
        )

        // Calculate enhanced metrics
        val homeTeamExpectedGoals = calculateTeamExpectedGoals(
            baseLambda = baseResult.homeLambda,
            playerContributions = homeScorers.totalExpectedGoals
        )
        val awayTeamExpectedGoals = calculateTeamExpectedGoals(
            baseLambda = baseResult.awayLambda,
            playerContributions = awayScorers.totalExpectedGoals
        )

        val bothTeamsToScoreProbability = calculateBothTeamsToScoreProbability(
            simulationResults = simulationResults
        )
        val over25GoalsProbability = calculateOver25GoalsProbability(
            simulationResults = simulationResults
        )

        return EnhancedTesseractResult(
            baseResult = baseResult,
            homeScorerProbabilities = homeScorerProbs,
            awayScorerProbabilities = awayScorerProbs,
            mostLikelyHomeScorer = findMostLikelyScorer(homeScorerProbs),
            mostLikelyAwayScorer = findMostLikelyScorer(awayScorerProbs),
            homeTeamExpectedGoals = homeTeamExpectedGoals,
            awayTeamExpectedGoals = awayTeamExpectedGoals,
            bothTeamsToScoreProbability = bothTeamsToScoreProbability,
            over25GoalsProbability = over25GoalsProbability
        )
    }

    /**
     * Runs base Monte Carlo simulation (original Tesseract logic).
     */
    private fun runBaseSimulation(oracleAnalysis: OracleAnalysis): TesseractResult {
        // This would call the existing TesseractEngine
        // For now, create a mock result
        return TesseractResult(
            homeWinProbability = 45.0,
            drawProbability = 25.0,
            awayWinProbability = 30.0,
            mostLikelyScore = "2-1",
            homeLambda = 1.8,
            awayLambda = 1.2,
            simulationCount = SIMULATION_COUNT,
            confidenceInterval = 0.95
        )
    }

    /**
     * Calculates player scoring probabilities for display.
     */
    private fun calculatePlayerScoringProbabilities(
        players: List<PlayerScoringProbability>
    ): Map<String, Double> {
        return players.associate { player ->
            player.playerName to player.adjustedProbability
        }
    }

    /**
     * Runs player-enhanced Monte Carlo simulations.
     */
    private fun runPlayerEnhancedSimulations(
        homeScorers: List<PlayerScoringProbability>,
        awayScorers: List<PlayerScoringProbability>,
        baseHomeLambda: Double,
        baseAwayLambda: Double
    ): List<Pair<Int, Int>> {
        val results = mutableListOf<Pair<Int, Int>>()

        repeat(SIMULATION_COUNT) {
            // Calculate team goals from base Poisson distribution
            val homeGoals = poissonRandom(baseHomeLambda)
            val awayGoals = poissonRandom(baseAwayLambda)

            // Add player contributions
            val homePlayerGoals = simulatePlayerGoals(homeScorers)
            val awayPlayerGoals = simulatePlayerGoals(awayScorers)

            val totalHomeGoals = homeGoals + homePlayerGoals
            val totalAwayGoals = awayGoals + awayPlayerGoals

            results.add(Pair(totalHomeGoals, totalAwayGoals))
        }

        return results
    }

    /**
     * Simulates goals from individual players.
     */
    private fun simulatePlayerGoals(players: List<PlayerScoringProbability>): Int {
        var totalGoals = 0

        for (player in players) {
            if (player.isPlaying) {
                // Convert probability to chance per simulation
                val scoringChance = player.adjustedProbability / 100.0 * PLAYER_GOAL_PROBABILITY_THRESHOLD
                
                // Simulate if player scores
                if (Random.nextDouble() < scoringChance) {
                    totalGoals++
                    
                    // Small chance for multiple goals (hat-trick)
                    if (player.adjustedProbability > 70.0 && Random.nextDouble() < 0.1) {
                        totalGoals++ // Second goal
                    }
                }
            }
        }

        return totalGoals
    }

    /**
     * Generates Poisson random number.
     */
    private fun poissonRandom(lambda: Double): Int {
        var k = 0
        var p = 1.0
        val l = exp(-lambda)

        do {
            k++
            p *= Random.nextDouble()
        } while (p > l)

        return k - 1
    }

    /**
     * Calculates team expected goals combining base lambda and player contributions.
     */
    private fun calculateTeamExpectedGoals(
        baseLambda: Double,
        playerContributions: Double
    ): Double {
        return (baseLambda * (1 - PLAYER_CONTRIBUTION_WEIGHT)) + 
               (playerContributions * PLAYER_CONTRIBUTION_WEIGHT)
    }

    /**
     * Calculates probability that both teams score.
     */
    private fun calculateBothTeamsToScoreProbability(
        simulationResults: List<Pair<Int, Int>>
    ): Double {
        val bothTeamsScored = simulationResults.count { (home, away) ->
            home > 0 && away > 0
        }
        return (bothTeamsScored.toDouble() / simulationResults.size) * 100.0
    }

    /**
     * Calculates probability of over 2.5 goals.
     */
    private fun calculateOver25GoalsProbability(
        simulationResults: List<Pair<Int, Int>>
    ): Double {
        val over25Goals = simulationResults.count { (home, away) ->
            home + away > 2
        }
        return (over25Goals.toDouble() / simulationResults.size) * 100.0
    }

    /**
     * Finds the most likely scorer from probability map.
     */
    private fun findMostLikelyScorer(probabilities: Map<String, Double>): String? {
        return probabilities.maxByOrNull { it.value }?.key
    }

    /**
     * Generates enhanced betting insights based on simulation results.
     */
    fun generateEnhancedBettingInsights(
        enhancedResult: EnhancedTesseractResult,
        homeScorers: TeamPlayerStatistics,
        awayScorers: TeamPlayerStatistics
    ): List<String> {
        val insights = mutableListOf<String>()

        // Base insights from original Tesseract
        insights.add("Home win probability: ${enhancedResult.baseResult.homeWinProbability}%")
        insights.add("Draw probability: ${enhancedResult.baseResult.drawProbability}%")
        insights.add("Away win probability: ${enhancedResult.baseResult.awayWinProbability}%")
        insights.add("Most likely score: ${enhancedResult.baseResult.mostLikelyScore}")

        // Enhanced player insights
        enhancedResult.mostLikelyHomeScorer?.let { scorer ->
            val probability = enhancedResult.homeScorerProbabilities[scorer] ?: 0.0
            insights.add("Most likely home scorer: $scorer (${probability.toInt()}% chance)")
        }

        enhancedResult.mostLikelyAwayScorer?.let { scorer ->
            val probability = enhancedResult.awayScorerProbabilities[scorer] ?: 0.0
            insights.add("Most likely away scorer: $scorer (${probability.toInt()}% chance)")
        }

        // Team scoring insights
        insights.add("Home team expected goals: ${enhancedResult.homeTeamExpectedGoals.format(1)}")
        insights.add("Away team expected goals: ${enhancedResult.awayTeamExpectedGoals.format(1)}")
        insights.add("Both teams to score: ${enhancedResult.bothTeamsToScoreProbability.format(1)}%")
        insights.add("Over 2.5 goals: ${enhancedResult.over25GoalsProbability.format(1)}%")

        // Player form insights
        homeScorers.getTopScorers(3).forEachIndexed { index, player ->
            insights.add("Home top scorer ${index + 1}: ${player.playerName} (${player.adjustedProbability.toInt()}%)")
        }

        awayScorers.getTopScorers(3).forEachIndexed { index, player ->
            insights.add("Away top scorer ${index + 1}: ${player.playerName} (${player.adjustedProbability.toInt()}%)")
        }

        // Generate betting recommendations
        insights.addAll(generateBettingRecommendations(enhancedResult))

        return insights
    }

    /**
     * Generates betting recommendations based on enhanced analysis.
     */
    private fun generateBettingRecommendations(
        enhancedResult: EnhancedTesseractResult
    ): List<String> {
        val recommendations = mutableListOf<String>()

        // Match result recommendations
        when {
            enhancedResult.baseResult.homeWinProbability > 60 -> 
                recommendations.add("Strong home win expected - consider home win bet")
            enhancedResult.baseResult.awayWinProbability > 60 -> 
                recommendations.add("Strong away win expected - consider away win bet")
            enhancedResult.baseResult.drawProbability > 40 -> 
                recommendations.add("High draw probability - consider draw bet")
        }

        // Goalscorer recommendations
        enhancedResult.mostLikelyHomeScorer?.let { scorer ->
            val probability = enhancedResult.homeScorerProbabilities[scorer] ?: 0.0
            if (probability > 40.0) {
                recommendations.add("$scorer has high scoring probability - consider anytime scorer bet")
            }
        }

        enhancedResult.mostLikelyAwayScorer?.let { scorer ->
            val probability = enhancedResult.awayScorerProbabilities[scorer] ?: 0.0
            if (probability > 40.0) {
                recommendations.add("$scorer has high scoring probability - consider anytime scorer bet")
            }
        }

        // Over/under recommendations
        when {
            enhancedResult.over25GoalsProbability > 70 -> 
                recommendations.add("High probability of over 2.5 goals - consider over bet")
            enhancedResult.over25GoalsProbability < 30 -> 
                recommendations.add("Low probability of over 2.5 goals - consider under bet")
        }

        // Both teams to score
        when {
            enhancedResult.bothTeamsToScoreProbability > 70 -> 
                recommendations.add("High probability both teams score - consider BTTS bet")
            enhancedResult.bothTeamsToScoreProbability < 30 -> 
                recommendations.add("Low probability both teams score - consider BTTS no bet")
        }

        return recommendations
    }

    /**
     * Extension function to format doubles.
     */
    private fun Double.format(digits: Int) = "%.${digits}f".format(this)
}

/**
 * Placeholder for original TesseractResult (should exist in the codebase).
 */
data class TesseractResult(
    val homeWinProbability: Double,
    val drawProbability: Double,
    val awayWinProbability: Double,
    val mostLikelyScore: String,
    val homeLambda: Double,
    val awayLambda: Double,
    val simulationCount: Int,
    val confidenceInterval: Double
)
