package com.Lyno.matchmindai.domain.prediction

import com.Lyno.matchmindai.domain.model.OracleAnalysis
import com.Lyno.matchmindai.domain.model.TesseractResult
import com.Lyno.matchmindai.domain.model.ContextFactor
import com.Lyno.matchmindai.domain.model.ContextFactorType

/**
 * Context-Adjusted Oracle Model
 * 
 * This model addresses the 3-0 bias by applying contextual corrections to the base Oracle prediction.
 * It considers injuries, form, home advantage, and alignment with Tesseract simulations.
 */
class ContextAdjustedOracle {

    /**
     * Calculate adjusted prediction based on context factors.
     * 
     * @param baseOracle Base Oracle prediction
     * @param contextFactors Context factors (injuries, form, etc.)
     * @param tesseractResult Tesseract Monte Carlo simulation results
     * @return Adjusted prediction with corrected score and confidence
     */
    fun calculateAdjustedPrediction(
        baseOracle: OracleAnalysis,
        contextFactors: List<ContextFactor>,
        tesseractResult: TesseractResult?
    ): AdjustedPrediction {
        
        val powerDiff = baseOracle.homePowerScore - baseOracle.awayPowerScore
        val basePrediction = baseOracle.prediction
        val baseConfidence = baseOracle.confidence / 100.0f // Convert to 0-1 scale
        
        // Contextual corrections
        val injuryCorrection = calculateInjuryCorrection(contextFactors)
        val formCorrection = calculateFormCorrection(contextFactors)
        val homeAdvantageCorrection = calculateHomeAdvantageCorrection(contextFactors)
        
        // Tesseract alignment factor
        val alignmentFactor = calculateAlignmentFactor(basePrediction, tesseractResult)
        
        // Calculate adjusted score
        val adjustedScore = adjustScore(
            baseScore = basePrediction,
            powerDiff = powerDiff,
            corrections = listOf(injuryCorrection, formCorrection, homeAdvantageCorrection),
            alignmentFactor = alignmentFactor
        )
        
        // Calculate adjusted confidence
        val adjustedConfidence = baseConfidence * 
            (1.0f - injuryCorrection) * 
            (1.0f - formCorrection) * 
            alignmentFactor
        
        return AdjustedPrediction(
            score = adjustedScore,
            confidence = (adjustedConfidence * 100).toInt().coerceIn(0, 100),
            reasoning = generateReasoning(baseOracle, contextFactors, tesseractResult, adjustedScore)
        )
    }

    /**
     * Calculate injury correction factor (0.0-0.6).
     * Higher injuries = higher correction (reduces confidence and adjusts score).
     */
    private fun calculateInjuryCorrection(contextFactors: List<ContextFactor>): Float {
        val injuryFactors = contextFactors.filter { it.type == ContextFactorType.INJURIES }
        if (injuryFactors.isEmpty()) return 0.0f
        
        var totalImpact = 0.0f
        for (factor in injuryFactors) {
            totalImpact += when {
                factor.score in 9..10 -> 0.3f  // Critical impact
                factor.score in 7..8 -> 0.2f   // High impact
                factor.score in 5..6 -> 0.15f  // Medium impact
                factor.score in 3..4 -> 0.1f   // Low impact
                else -> 0.05f     // Minimal impact
            }
        }
        
        return totalImpact.coerceAtMost(0.6f)
    }

    /**
     * Calculate form correction factor (0.0-0.2).
     * Poor form = higher correction.
     */
    private fun calculateFormCorrection(contextFactors: List<ContextFactor>): Float {
        val formFactors = contextFactors.filter { it.type == ContextFactorType.TEAM_MORALE }
        if (formFactors.isEmpty()) return 0.0f
        
        var formScore = 0
        for (factor in formFactors) {
            formScore += when {
                factor.score in 9..10 -> 3  // Excellent form
                factor.score in 7..8 -> 2   // Good form
                factor.score in 5..6 -> 1   // Average form
                factor.score in 3..4 -> 0   // Poor form
                else -> -1     // Terrible form
            }
        }
        
        return when {
            formScore <= -2 -> 0.2f  // Terrible form
            formScore <= 0 -> 0.15f  // Poor form
            formScore <= 2 -> 0.05f  // Average form
            else -> 0.0f             // Good/Excellent form
        }
    }

    /**
     * Calculate home advantage correction factor (0.0-0.1).
     * Strong home advantage = lower correction (actually boosts confidence).
     */
    private fun calculateHomeAdvantageCorrection(contextFactors: List<ContextFactor>): Float {
        // Use PRESSURE factor for home advantage (pressure on home team)
        val pressureFactors = contextFactors.filter { it.type == ContextFactorType.PRESSURE }
        if (pressureFactors.isEmpty()) return 0.0f
        
        var pressureScore = 0
        for (factor in pressureFactors) {
            pressureScore += when {
                factor.score in 9..10 -> 3  // Strong pressure (negative for home team)
                factor.score in 7..8 -> 2   // Moderate pressure
                factor.score in 5..6 -> 1   // Weak pressure
                else -> 0      // No pressure
            }
        }
        
        // High pressure reduces home advantage (increases correction)
        return when {
            pressureScore >= 3 -> 0.1f   // Strong pressure
            pressureScore >= 2 -> 0.05f  // Moderate pressure
            else -> 0.0f                 // Weak or no pressure
        }
    }

    /**
     * Calculate alignment factor between Oracle and Tesseract (0.5-1.5).
     * Higher alignment = higher factor (boosts confidence).
     */
    private fun calculateAlignmentFactor(
        oracleScore: String,
        tesseractResult: TesseractResult?
    ): Float {
        if (tesseractResult == null) return 1.0f
        
        val tesseractScore = tesseractResult.mostLikelyScore
        
        return when {
            oracleScore == tesseractScore -> 1.5f  // Perfect alignment
            scoresAreSimilar(oracleScore, tesseractScore) -> 1.2f  // Similar scores
            else -> 0.8f  // Different predictions
        }
    }

    /**
     * Check if two scores are similar (same outcome type).
     */
    private fun scoresAreSimilar(score1: String, score2: String): Boolean {
        val (home1, away1) = parseScore(score1)
        val (home2, away2) = parseScore(score2)
        
        val outcome1 = when {
            home1 > away1 -> "HOME_WIN"
            home1 < away1 -> "AWAY_WIN"
            else -> "DRAW"
        }
        
        val outcome2 = when {
            home2 > away2 -> "HOME_WIN"
            home2 < away2 -> "AWAY_WIN"
            else -> "DRAW"
        }
        
        return outcome1 == outcome2
    }

    /**
     * Adjust score based on context corrections.
     */
    private fun adjustScore(
        baseScore: String,
        powerDiff: Int,
        corrections: List<Float>,
        alignmentFactor: Float
    ): String {
        val (homeGoals, awayGoals) = parseScore(baseScore)
        
        // Calculate total correction impact
        val totalCorrection = corrections.sum()
        
        // Adjust goals based on context
        var adjustedHome = homeGoals
        var adjustedAway = awayGoals
        
        when {
            totalCorrection > 0.3 -> {
                // Significant negative context - reduce goal difference
                if (powerDiff > 30) {
                    // Reduce strong win to moderate win
                    adjustedHome = (homeGoals * 0.7).toInt().coerceAtLeast(1)
                    adjustedAway = (awayGoals * 1.3).toInt().coerceAtMost(3)
                } else if (powerDiff < -30) {
                    adjustedHome = (homeGoals * 1.3).toInt().coerceAtMost(3)
                    adjustedAway = (awayGoals * 0.7).toInt().coerceAtLeast(1)
                }
            }
            totalCorrection > 0.1 -> {
                // Moderate negative context - slight adjustment
                if (powerDiff > 15) {
                    adjustedHome = (homeGoals * 0.85).toInt().coerceAtLeast(1)
                } else if (powerDiff < -15) {
                    adjustedAway = (awayGoals * 0.85).toInt().coerceAtLeast(1)
                }
            }
            totalCorrection < -0.1 -> {
                // Positive context (strong home advantage) - boost
                if (powerDiff > 0) {
                    adjustedHome = (homeGoals * 1.15).toInt().coerceAtMost(4)
                } else if (powerDiff < 0) {
                    adjustedAway = (awayGoals * 1.15).toInt().coerceAtMost(4)
                }
            }
        }
        
        // Apply alignment factor
        if (alignmentFactor < 1.0) {
            // Tesseract disagrees - move toward draw
            val drawBias = 1.0f - alignmentFactor
            adjustedHome = (adjustedHome * (1.0f - drawBias * 0.3f)).toInt().coerceAtLeast(0)
            adjustedAway = (adjustedAway * (1.0f - drawBias * 0.3f)).toInt().coerceAtLeast(0)
        }
        
        return "${adjustedHome.coerceAtMost(5)}-${adjustedAway.coerceAtMost(5)}"
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
     * Generate reasoning for the adjusted prediction.
     */
    private fun generateReasoning(
        baseOracle: OracleAnalysis,
        contextFactors: List<ContextFactor>,
        tesseractResult: TesseractResult?,
        adjustedScore: String
    ): String {
        val baseReasoning = baseOracle.reasoning
        val injuryCount = contextFactors.count { it.type == ContextFactorType.INJURIES }
        val formFactors = contextFactors.filter { it.type == ContextFactorType.TEAM_MORALE }
        
        val sb = StringBuilder()
        sb.append(baseReasoning)
        
        if (injuryCount > 0) {
            sb.append(" Context adjustment: $injuryCount injury factor(s) considered.")
        }
        
        if (formFactors.isNotEmpty()) {
            val formSummary = formFactors.joinToString(", ") { it.description }
            sb.append(" Recent form: $formSummary.")
        }
        
        if (tesseractResult != null && baseOracle.prediction != adjustedScore) {
            sb.append(" Tesseract simulation suggested ${tesseractResult.mostLikelyScore}, adjusting prediction accordingly.")
        }
        
        sb.append(" Final adjusted prediction: $adjustedScore.")
        
        return sb.toString()
    }

    /**
     * Quick adjustment for the 3-0 bias problem.
     */
    fun adjustOracleScoreQuickFix(
        baseScore: String,
        powerDiff: Int,
        totalInjuries: Int,
        homeForm: String
    ): String {
        return when {
            powerDiff > 50 && totalInjuries > 8 -> "2-0"
            powerDiff > 50 && homeForm == "poor" -> "2-1"
            powerDiff > 30 && totalInjuries > 4 -> "1-0"
            else -> baseScore
        }
    }
}

/**
 * Result of context-adjusted prediction.
 */
data class AdjustedPrediction(
    val score: String,
    val confidence: Int,
    val reasoning: String
)
