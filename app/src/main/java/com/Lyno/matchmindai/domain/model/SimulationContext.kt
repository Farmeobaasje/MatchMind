package com.Lyno.matchmindai.domain.model

import kotlinx.serialization.Serializable

/**
 * Quantitative variables for Tesseract simulation using the "Trinity" metrics.
 * These variables represent key factors that influence match outcomes.
 * 
 * Used in VoorspellingX Phase 1: Cleanup & Foundation.
 * 
 * @property fatigueScore Fatigue level (0 = Fit, 100 = Exhausted)
 * @property styleMatchup Style matchup advantage (0.5 = Disadvantage, 1.0 = Neutral, 1.5 = Advantage)
 * @property lineupStrength Lineup strength (0 = Weakest, 100 = Strongest)
 * @property reasoning AI reasoning behind the simulation context
 * 
 * Legacy fields for backward compatibility (Phase 1):
 * @property homeDistraction Home team distraction level (0-100)
 * @property awayDistraction Away team distraction level (0-100)
 * @property homeFitness Home team fitness level (0-100)
 * @property awayFitness Away team fitness level (0-100)
 */
@Serializable
data class SimulationContext(
    val fatigueScore: Int = 0,
    val styleMatchup: Double = 1.0,
    val lineupStrength: Int = 100,
    val reasoning: String = "Analysis pending...",
    
    // Legacy fields for backward compatibility (Phase 1)
    val homeDistraction: Int = 0,
    val awayDistraction: Int = 0,
    val homeFitness: Int = 100,
    val awayFitness: Int = 100
) {
    init {
        require(fatigueScore in 0..100) { "fatigueScore must be between 0 and 100" }
        require(styleMatchup in 0.5..1.5) { "styleMatchup must be between 0.5 and 1.5" }
        require(lineupStrength in 0..100) { "lineupStrength must be between 0 and 100" }
        require(homeDistraction in 0..100) { "homeDistraction must be between 0 and 100" }
        require(awayDistraction in 0..100) { "awayDistraction must be between 0 and 100" }
        require(homeFitness in 0..100) { "homeFitness must be between 0 and 100" }
        require(awayFitness in 0..100) { "awayFitness must be between 0 and 100" }
    }

    /**
     * Returns true if fatigue is high (> 70).
     * High fatigue indicates potential performance degradation.
     */
    val hasHighFatigue: Boolean
        get() = fatigueScore > 70

    /**
     * Returns true if lineup is weak (< 70).
     * Weak lineup indicates missing key players.
     */
    val hasWeakLineup: Boolean
        get() = lineupStrength < 70

    /**
     * Returns true if style matchup provides advantage (> 1.1).
     */
    val hasStyleAdvantage: Boolean
        get() = styleMatchup > 1.1

    /**
     * Returns true if style matchup provides disadvantage (< 0.9).
     */
    val hasStyleDisadvantage: Boolean
        get() = styleMatchup < 0.9

    /**
     * Returns a summary of the simulation context for debugging.
     */
    fun getSummary(): String {
        return buildString {
            appendLine("Simulation Context (Trinity Metrics):")
            appendLine("  Fatigue: $fatigueScore/100 (${if (hasHighFatigue) "⚠️ High" else "OK"})")
            appendLine("  Style Matchup: ${String.format("%.2f", styleMatchup)} (${getStyleMatchupDescription()})")
            appendLine("  Lineup Strength: $lineupStrength/100 (${if (hasWeakLineup) "⚠️ Weak" else "Strong"})")
            if (reasoning.isNotBlank()) {
                appendLine("  Reasoning: $reasoning")
            }
        }
    }

    /**
     * Returns a description of the style matchup.
     */
    private fun getStyleMatchupDescription(): String {
        return when {
            styleMatchup > 1.2 -> "Strong Advantage"
            styleMatchup > 1.1 -> "Advantage"
            styleMatchup > 0.9 -> "Neutral"
            styleMatchup > 0.8 -> "Disadvantage"
            else -> "Strong Disadvantage"
        }
    }

    /**
     * Returns true if this context has meaningful data (not all defaults).
     */
    fun hasMeaningfulData(): Boolean {
        return fatigueScore != 0 || styleMatchup != 1.0 || lineupStrength != 100 ||
               homeDistraction != 0 || awayDistraction != 0 || homeFitness != 100 || awayFitness != 100
    }
    
    /**
     * Legacy properties for backward compatibility
     */
    val hasHighDistraction: Boolean
        get() = homeDistraction > 70 || awayDistraction > 70
    
    val hasLowFitness: Boolean
        get() = homeFitness < 70 || awayFitness < 70

    companion object {
        /**
         * Neutral simulation context (default conditions).
         */
        val NEUTRAL = SimulationContext(
            fatigueScore = 0,
            styleMatchup = 1.0,
            lineupStrength = 100,
            reasoning = "Default neutral context"
        )
    }
}
