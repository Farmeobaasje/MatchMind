package com.Lyno.matchmindai.domain.tesseract

import com.Lyno.matchmindai.domain.model.TesseractResult
import kotlin.math.exp
import kotlin.math.ln
import kotlin.random.Random

/**
 * Tesseract Monte Carlo Simulation Engine for probabilistic match predictions.
 * 
 * This engine takes "Hard Reality" power scores (0-100 scale) and simulates
 * the match 10,000 times using a Poisson process to generate probability distributions.
 * 
 * Algorithm:
 * 1. Convert power scores (0-100) to expected goals (lambda)
 * 2. Apply random "Form Noise" to lambda (0.9-1.1 multiplier)
 * 3. Generate home/away goals using Poisson distribution (Knuth algorithm)
 * 4. Aggregate results over 10,000 simulations
 * 5. Return TesseractResult with probabilities and most likely score
 */
class TesseractEngine {

    companion object {
        private const val TAG = "TesseractEngine"
        private const val DEFAULT_SIMULATION_COUNT = 10000
        private const val BASE_SCORING_RATE = 3.2 // Maximum expected goals for 100 power
        private const val FORM_NOISE_MIN = 0.9
        private const val FORM_NOISE_MAX = 1.1
        
        // Phase 2: Simulation Context constants
        private const val MIN_FITNESS_IMPACT = 0.5 // Minimum lambda multiplier when fitness is 0
        private const val MAX_DISTRACTION_IMPACT = 0.5 // Maximum additional noise range when distraction is 100
    }

    /**
     * Simulate a match between two teams using Monte Carlo simulation.
     * 
     * @param homePower Home team power score (0-100 scale)
     * @param awayPower Away team power score (0-100 scale)
     * @param simulationCount Number of simulations to run (default: 10,000)
     * @return TesseractResult with probabilities and most likely score
     */
    fun simulateMatch(
        homePower: Int,
        awayPower: Int,
        simulationCount: Int = DEFAULT_SIMULATION_COUNT
    ): TesseractResult {
        return simulateMatch(
            homePower = homePower,
            awayPower = awayPower,
            context = com.Lyno.matchmindai.domain.model.SimulationContext.NEUTRAL,
            simulationCount = simulationCount
        )
    }

    /**
     * Simulate a match between two teams using Monte Carlo simulation with SimulationContext.
     * Phase 2: Enhanced with psychological and physical factors.
     * 
     * @param homePower Home team power score (0-100 scale)
     * @param awayPower Away team power score (0-100 scale)
     * @param context SimulationContext with distraction and fitness values
     * @param simulationCount Number of simulations to run (default: 10,000)
     * @return TesseractResult with probabilities and most likely score
     */
    fun simulateMatch(
        homePower: Int,
        awayPower: Int,
        context: com.Lyno.matchmindai.domain.model.SimulationContext,
        simulationCount: Int = DEFAULT_SIMULATION_COUNT
    ): TesseractResult {
        require(homePower in 0..100) { "Home power must be between 0 and 100" }
        require(awayPower in 0..100) { "Away power must be between 0 and 100" }
        require(simulationCount > 0) { "Simulation count must be positive" }

        // Step A: Convert power scores to expected goals (lambda)
        val baseHomeLambda = powerToLambda(homePower)
        val baseAwayLambda = powerToLambda(awayPower)

        // Initialize counters
        var homeWins = 0
        var draws = 0
        var awayWins = 0
        var bttsCount = 0
        var over25Count = 0
        val scoreCounts = mutableMapOf<String, Int>()

        // Step B: Run Monte Carlo simulations with context
        repeat(simulationCount) {
            // Apply form noise with context impact (Phase 2)
            val homeLambda = applyFormNoiseWithContext(baseHomeLambda, context)
            val awayLambda = applyFormNoiseWithContext(baseAwayLambda, context)

            // Generate goals using Poisson distribution
            val homeGoals = poisson(homeLambda)
            val awayGoals = poisson(awayLambda)

            // Determine match outcome
            when {
                homeGoals > awayGoals -> homeWins++
                homeGoals < awayGoals -> awayWins++
                else -> draws++
            }

            // Count BTTS (Both Teams To Score)
            if (homeGoals > 0 && awayGoals > 0) {
                bttsCount++
            }

            // Count Over/Under 2.5
            if ((homeGoals + awayGoals) > 2.5) {
                over25Count++
            }

            // Track score occurrences
            val score = "$homeGoals-$awayGoals"
            scoreCounts[score] = scoreCounts.getOrDefault(score, 0) + 1
        }

        // Step C: Calculate probabilities
        val homeWinProbability = homeWins.toDouble() / simulationCount
        val drawProbability = draws.toDouble() / simulationCount
        val awayWinProbability = awayWins.toDouble() / simulationCount
        val bttsProbability = bttsCount.toDouble() / simulationCount
        val over2_5Probability = over25Count.toDouble() / simulationCount

        // Step D: Find most likely score
        val mostLikelyScore = scoreCounts.maxByOrNull { it.value }?.key ?: "0-0"

        // Step E: Get top 3 score distribution
        val topScoreDistribution = scoreCounts.entries
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key to it.value }

        return TesseractResult(
            homeWinProbability = homeWinProbability,
            drawProbability = drawProbability,
            awayWinProbability = awayWinProbability,
            mostLikelyScore = mostLikelyScore,
            simulationCount = simulationCount,
            bttsProbability = bttsProbability,
            over2_5Probability = over2_5Probability,
            topScoreDistribution = topScoreDistribution
        )
    }

    /**
     * Convert power score (0-100) to expected goals (lambda).
     * Formula: lambda = (powerScore / 100.0) * BASE_SCORING_RATE
     * 
     * This creates a linear relationship where:
     * - 0 power → 0 expected goals
     * - 50 power → 1.6 expected goals
     * - 100 power → 3.2 expected goals (maximum)
     */
    private fun powerToLambda(powerScore: Int): Double {
        return (powerScore.toDouble() / 100.0) * BASE_SCORING_RATE
    }

    /**
     * Apply random form noise to lambda.
     * Multiplies lambda by a random factor between FORM_NOISE_MIN and FORM_NOISE_MAX.
     * This simulates day-to-day form variations that affect team performance.
     */
    private fun applyFormNoise(lambda: Double): Double {
        val noiseFactor = Random.nextDouble(FORM_NOISE_MIN, FORM_NOISE_MAX)
        return lambda * noiseFactor
    }

    /**
     * Apply form noise with SimulationContext impact (Phase 2).
     * 
     * Physics Logic:
     * 1. Fatigue Impact: effectiveLambda = baseLambda * (1 - fatigueScore/200.0)
     *    - 0 fatigue = full lambda (no reduction)
     *    - 100 fatigue = lambda * 0.5 (50% reduction)
     *    
     * 2. Lineup Strength Impact: effectiveLambda = baseLambda * (lineupStrength / 100.0)
     *    - 100 lineup = full lambda
     *    - 0 lineup = lambda * 0.0 (no scoring)
     *    
     * 3. Style Matchup Impact: effectiveLambda = baseLambda * styleMatchup
     *    - 1.0 = neutral
     *    - >1.0 = advantage
     *    - <1.0 = disadvantage
     *    
     * @param baseLambda Base expected goals (lambda)
     * @param context SimulationContext with Trinity metrics
     * @return Adjusted lambda with context impact
     */
    private fun applyFormNoiseWithContext(
        baseLambda: Double,
        context: com.Lyno.matchmindai.domain.model.SimulationContext
    ): Double {
        // 1. Apply fatigue impact (linear reduction)
        val fatigueMultiplier = 1.0 - (context.fatigueScore / 200.0)
        val fatigueAdjustedLambda = baseLambda * fatigueMultiplier
        
        // 2. Apply lineup strength impact
        val lineupMultiplier = context.lineupStrength / 100.0
        val lineupAdjustedLambda = fatigueAdjustedLambda * lineupMultiplier
        
        // 3. Apply style matchup impact
        val styleAdjustedLambda = lineupAdjustedLambda * context.styleMatchup
        
        // 4. Apply random noise
        val noiseFactor = Random.nextDouble(FORM_NOISE_MIN, FORM_NOISE_MAX)
        
        return styleAdjustedLambda * noiseFactor
    }

    /**
     * Generate a Poisson-distributed random number using Knuth's algorithm.
     * 
     * Algorithm:
     * 1. Let L = exp(-lambda)
     * 2. Let k = 0, p = 1
     * 3. While p > L:
     *    - k = k + 1
     *    - p = p * U(0,1)
     * 4. Return k - 1
     * 
     * @param lambda Expected value (mean) of the Poisson distribution
     * @return Random integer following Poisson(lambda) distribution
     */
    private fun poisson(lambda: Double): Int {
        if (lambda <= 0.0) return 0
        
        val l = exp(-lambda)
        var k = 0
        var p = 1.0
        
        do {
            k++
            p *= Random.nextDouble()
        } while (p > l)
        
        return k - 1
    }

    /**
     * Validate that probabilities sum to approximately 1.0.
     * Used for debugging and testing.
     */
    fun validateProbabilities(
        homeWin: Double,
        draw: Double,
        awayWin: Double,
        tolerance: Double = 0.001
    ): Boolean {
        val sum = homeWin + draw + awayWin
        return sum in (1.0 - tolerance)..(1.0 + tolerance)
    }

    /**
     * Get expected goals for a given power score.
     * Useful for debugging and analysis.
     */
    fun getExpectedGoals(powerScore: Int): Double {
        return powerToLambda(powerScore)
    }

    /**
     * Run a quick test simulation and return debug information.
     */
    fun debugSimulation(homePower: Int, awayPower: Int): Map<String, Any> {
        val result = simulateMatch(homePower, awayPower, 1000)
        
        return mapOf(
            "homePower" to homePower,
            "awayPower" to awayPower,
            "homeLambda" to powerToLambda(homePower),
            "awayLambda" to powerToLambda(awayPower),
            "homeWinProbability" to result.homeWinProbability,
            "drawProbability" to result.drawProbability,
            "awayWinProbability" to result.awayWinProbability,
            "mostLikelyScore" to result.mostLikelyScore,
            "probabilitiesValid" to validateProbabilities(
                result.homeWinProbability,
                result.drawProbability,
                result.awayWinProbability
            )
        )
    }
}
