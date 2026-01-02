package com.Lyno.matchmindai.domain.prediction

import com.Lyno.matchmindai.domain.model.ContextFactor
import com.Lyno.matchmindai.domain.model.ContextFactorType

/**
 * Score Distribution Model
 * 
 * Replaces fixed 3-0 predictions with a probability-based model that generates
 * realistic score distributions based on power difference and context factors.
 */
class ScoreDistributionModel {

    companion object {
        // Base score probabilities for different power differences
        private val BASE_SCORE_PROBABILITIES = mapOf(
            "3-0" to 0.08,
            "2-0" to 0.12,
            "2-1" to 0.15,
            "1-0" to 0.10,
            "1-1" to 0.20,
            "0-0" to 0.05,
            "0-1" to 0.08,
            "0-2" to 0.07,
            "1-2" to 0.10,
            "0-3" to 0.05
        )
    }

    /**
     * Generate realistic score probabilities based on power difference and context.
     * 
     * @param powerDiff Power difference (Home - Away)
     * @param contextFactors Context factors (injuries, form, etc.)
     * @return ScoreProbability with most likely score and distribution
     */
    fun generateRealisticScore(
        powerDiff: Int,
        contextFactors: List<ContextFactor>
    ): ScoreProbability {
        
        // Base expectation based on power difference
        val baseExpectation = when {
            powerDiff > 50 -> listOf("3-0", "2-0", "2-1", "3-1", "4-0")
            powerDiff > 30 -> listOf("2-0", "2-1", "1-0", "3-0", "3-1")
            powerDiff > 15 -> listOf("2-1", "1-0", "1-1", "2-0", "0-0")
            powerDiff > 0 -> listOf("1-1", "1-0", "0-0", "2-1", "0-1")
            powerDiff > -15 -> listOf("1-1", "0-1", "1-0", "0-0", "1-2")
            powerDiff > -30 -> listOf("0-1", "1-2", "0-2", "1-1", "0-0")
            else -> listOf("0-2", "0-3", "1-2", "0-1", "1-3")
        }
        
        // Calculate probabilities with context adjustments
        val adjustedProbabilities = baseExpectation.associateWith { score ->
            calculateScoreProbability(score, powerDiff, contextFactors)
        }
        
        // Normalize probabilities
        val totalProbability = adjustedProbabilities.values.sum()
        val normalizedProbabilities = if (totalProbability > 0) {
            adjustedProbabilities.mapValues { (_, prob) -> prob / totalProbability }
        } else {
            adjustedProbabilities
        }
        
        // Find most likely score
        val mostLikely = normalizedProbabilities.maxByOrNull { it.value }?.key ?: "1-1"
        
        return ScoreProbability(
            mostLikely = mostLikely,
            distribution = normalizedProbabilities,
            reasoning = generateDistributionReasoning(powerDiff, contextFactors, mostLikely)
        )
    }

    /**
     * Calculate probability for a specific score considering context.
     */
    private fun calculateScoreProbability(
        score: String,
        powerDiff: Int,
        context: List<ContextFactor>
    ): Double {
        var probability = BASE_SCORE_PROBABILITIES[score] ?: 0.05
        
        // Parse score
        val (homeGoals, awayGoals) = parseScore(score)
        val goalDifference = homeGoals - awayGoals
        
        // Power difference alignment
        probability *= calculatePowerAlignment(goalDifference, powerDiff)
        
        // Injury impact
        probability *= calculateInjuryImpact(context, homeGoals, awayGoals)
        
        // Form impact
        probability *= calculateFormImpact(context, goalDifference)
        
        // Home advantage impact
        probability *= calculateHomeAdvantageImpact(context, homeGoals)
        
        // Ensure reasonable bounds
        return probability.coerceIn(0.01, 0.95)
    }

    /**
     * Calculate alignment between score goal difference and power difference.
     */
    private fun calculatePowerAlignment(scoreDiff: Int, powerDiff: Int): Double {
        val diffAlignment = when {
            powerDiff > 30 && scoreDiff >= 2 -> 1.5  // Strong win expected
            powerDiff > 15 && scoreDiff >= 1 -> 1.3  // Win expected
            powerDiff > 0 && scoreDiff == 0 -> 1.2   // Draw when slight advantage
            powerDiff < -30 && scoreDiff <= -2 -> 1.5
            powerDiff < -15 && scoreDiff <= -1 -> 1.3
            powerDiff < 0 && scoreDiff == 0 -> 1.2
            else -> 0.8  // Misalignment
        }
        
        return diffAlignment
    }

    /**
     * Calculate injury impact on score probability.
     */
    private fun calculateInjuryImpact(
        context: List<ContextFactor>,
        homeGoals: Int,
        awayGoals: Int
    ): Double {
        val injuryFactors = context.filter { it.type == ContextFactorType.INJURIES }
        if (injuryFactors.isEmpty()) return 1.0
        
        // Count injuries based on description containing team hints
        val homeInjuries = injuryFactors.count { it.description.contains("home", ignoreCase = true) || 
                                                it.description.contains("thuis", ignoreCase = true) }
        val awayInjuries = injuryFactors.count { it.description.contains("away", ignoreCase = true) || 
                                                it.description.contains("uit", ignoreCase = true) }
        
        var impact = 1.0
        
        // Reduce scoring probability for injured teams
        if (homeInjuries > 0 && homeGoals > 0) {
            impact *= (1.0 - (homeInjuries * 0.1)).coerceAtLeast(0.5)
        }
        
        if (awayInjuries > 0 && awayGoals > 0) {
            impact *= (1.0 - (awayInjuries * 0.1)).coerceAtLeast(0.5)
        }
        
        return impact
    }

    /**
     * Calculate form impact on score probability.
     * Uses TEAM_MORALE factor as proxy for form.
     */
    private fun calculateFormImpact(
        context: List<ContextFactor>,
        goalDifference: Int
    ): Double {
        val moraleFactors = context.filter { it.type == ContextFactorType.TEAM_MORALE }
        if (moraleFactors.isEmpty()) return 1.0
        
        // Use score as form indicator (1-10 scale)
        val homeMorale = moraleFactors.find { 
            it.description.contains("home", ignoreCase = true) || 
            it.description.contains("thuis", ignoreCase = true)
        }?.score ?: 5
        
        val awayMorale = moraleFactors.find { 
            it.description.contains("away", ignoreCase = true) || 
            it.description.contains("uit", ignoreCase = true)
        }?.score ?: 5
        
        var impact = 1.0
        
        // Home morale impact
        when {
            homeMorale >= 8 -> impact *= if (goalDifference > 0) 1.3 else 0.9
            homeMorale >= 6 -> impact *= if (goalDifference > 0) 1.2 else 0.95
            homeMorale <= 4 -> impact *= if (goalDifference > 0) 0.8 else 1.1
            homeMorale <= 2 -> impact *= if (goalDifference > 0) 0.7 else 1.2
        }
        
        // Away morale impact
        when {
            awayMorale >= 8 -> impact *= if (goalDifference < 0) 1.3 else 0.9
            awayMorale >= 6 -> impact *= if (goalDifference < 0) 1.2 else 0.95
            awayMorale <= 4 -> impact *= if (goalDifference < 0) 0.8 else 1.1
            awayMorale <= 2 -> impact *= if (goalDifference < 0) 0.7 else 1.2
        }
        
        return impact
    }

    /**
     * Calculate home advantage impact.
     * Uses PRESSURE factor as proxy for home advantage.
     */
    private fun calculateHomeAdvantageImpact(
        context: List<ContextFactor>,
        homeGoals: Int
    ): Double {
        val pressureFactors = context.filter { it.type == ContextFactorType.PRESSURE }
        if (pressureFactors.isEmpty()) return 1.0
        
        // Use score as pressure indicator (higher score = more pressure)
        val pressureScore = pressureFactors.firstOrNull()?.score ?: 5
        
        return when {
            pressureScore >= 8 -> if (homeGoals > 0) 1.25 else 0.9  // High pressure helps home team
            pressureScore >= 6 -> if (homeGoals > 0) 1.15 else 0.95
            pressureScore >= 4 -> if (homeGoals > 0) 1.05 else 0.98
            else -> 1.0
        }
    }

    /**
     * Parse score string to home and away goals.
     */
    private fun parseScore(score: String): Pair<Int, Int> {
        val parts = score.split("-")
        return if (parts.size == 2) {
            Pair(parts[0].toIntOrNull() ?: 0, parts[1].toIntOrNull() ?: 0)
        } else {
            Pair(0, 0)
        }
    }

    /**
     * Generate reasoning for the score distribution.
     */
    private fun generateDistributionReasoning(
        powerDiff: Int,
        contextFactors: List<ContextFactor>,
        mostLikelyScore: String
    ): String {
        val sb = StringBuilder()
        
        sb.append("Based on power difference of $powerDiff")
        
        val injuryCount = contextFactors.count { it.type == ContextFactorType.INJURIES }
        if (injuryCount > 0) {
            sb.append(" and $injuryCount injury factor(s)")
        }
        
        val moraleFactors = contextFactors.count { it.type == ContextFactorType.TEAM_MORALE }
        if (moraleFactors > 0) {
            sb.append(" with team morale considerations")
        }
        
        sb.append(". Most likely score: $mostLikelyScore.")
        
        return sb.toString()
    }

    /**
     * Get top N most likely scores.
     */
    fun getTopScores(
        powerDiff: Int,
        contextFactors: List<ContextFactor>,
        count: Int = 3
    ): List<Pair<String, Double>> {
        val scoreProbability = generateRealisticScore(powerDiff, contextFactors)
        return scoreProbability.distribution.entries
            .sortedByDescending { it.value }
            .take(count)
            .map { it.key to it.value }
    }
}

/**
 * Result of score distribution analysis.
 */
data class ScoreProbability(
    val mostLikely: String,
    val distribution: Map<String, Double>,
    val reasoning: String
)
