package com.Lyno.matchmindai.domain.prediction

import com.Lyno.matchmindai.domain.model.OracleAnalysis
import com.Lyno.matchmindai.domain.model.TesseractResult
import com.Lyno.matchmindai.domain.model.LLMGradeEnhancement
import com.Lyno.matchmindai.domain.model.OutlierScenario
import com.Lyno.matchmindai.domain.model.RiskLevel

/**
 * Hybrid Prediction Engine
 * 
 * Combines Oracle, Tesseract, and LLMGRADE predictions with weighted factors
 * to generate the most accurate final prediction.
 */
class HybridPredictionEngine {

    /**
     * Generate hybrid prediction by combining all prediction sources.
     * 
     * @param oracle Oracle prediction (deterministic, fact-based)
     * @param tesseract Tesseract Monte Carlo simulation results
     * @param llmGrade LLMGRADE enhancement with context factors
     * @return HybridPrediction with combined score and confidence
     */
    fun generateHybridPrediction(
        oracle: OracleAnalysis,
        tesseract: TesseractResult?,
        llmGrade: LLMGradeEnhancement?
    ): HybridPrediction {
        
        // Calculate dynamic weights based on confidence and quality
        val weights = calculatePredictionWeights(oracle, tesseract, llmGrade)
        
        // Parse scores
        val oracleScore = parseScore(oracle.prediction)
        val tesseractScore = tesseract?.mostLikelyScore?.let { parseScore(it) } ?: Pair(0, 0)
        
        // Determine the most likely score
        val hybridScore = when {
            // Case 1: Oracle is strong but there are significant outliers
            oracle.confidence > 80 && llmGrade?.hasHighProbabilityOutliers == true -> {
                adjustForOutliers(oracleScore, llmGrade.outlierScenarios)
            }
            
            // Case 2: Tesseract strongly suggests draw
            tesseract?.drawProbability ?: 0.0 > 0.4 -> {
                favorDrawScore(oracleScore, tesseractScore)
            }
            
            // Case 3: LLMGRADE indicates high risk
            llmGrade?.overallRiskLevel() == RiskLevel.HIGH -> {
                applyRiskAdjustment(oracleScore, tesseractScore, llmGrade)
            }
            
            // Default: Weighted combination
            else -> {
                calculateWeightedScore(oracleScore, tesseractScore, weights)
            }
        }
        
        // Calculate hybrid confidence
        val hybridConfidence = calculateHybridConfidence(oracle, tesseract, llmGrade, weights)
        
        // Determine primary source
        val primarySource = determinePrimarySource(oracle, tesseract, llmGrade, weights)
        
        return HybridPrediction(
            score = "${hybridScore.first}-${hybridScore.second}",
            confidence = hybridConfidence,
            primarySource = primarySource,
            reasoning = generateHybridReasoning(oracle, tesseract, llmGrade, hybridScore, primarySource)
        )
    }

    /**
     * Calculate dynamic weights for each prediction source.
     */
    private fun calculatePredictionWeights(
        oracle: OracleAnalysis,
        tesseract: TesseractResult?,
        llmGrade: LLMGradeEnhancement?
    ): PredictionWeights {
        
        // Base weights
        var oracleWeight = when {
            oracle.confidence > 80 -> 0.4f
            oracle.confidence > 60 -> 0.3f
            else -> 0.2f
        }
        
        var tesseractWeight = 0.3f
        var llmGradeWeight = 0.3f
        
        // Adjust based on quality indicators
        if (tesseract == null) {
            tesseractWeight = 0.0f
            // Redistribute to other sources
            oracleWeight += 0.15f
            llmGradeWeight += 0.15f
        }
        
        if (llmGrade == null) {
            llmGradeWeight = 0.0f
            // Redistribute
            oracleWeight += 0.15f
            tesseractWeight += 0.15f
        } else if (llmGrade.contextFactors.isEmpty()) {
            // LLMGRADE has no context factors - reduce weight
            llmGradeWeight = 0.1f
            oracleWeight += 0.1f
            tesseractWeight += 0.1f
        }
        
        // Normalize weights to sum to 1.0
        val totalWeight = oracleWeight + tesseractWeight + llmGradeWeight
        if (totalWeight > 0) {
            oracleWeight /= totalWeight
            tesseractWeight /= totalWeight
            llmGradeWeight /= totalWeight
        }
        
        return PredictionWeights(
            oracle = oracleWeight,
            tesseract = tesseractWeight,
            llmGrade = llmGradeWeight
        )
    }

    /**
     * Adjust score for significant outlier scenarios.
     */
    private fun adjustForOutliers(
        oracleScore: Pair<Int, Int>,
        outlierScenarios: List<OutlierScenario>
    ): Pair<Int, Int> {
        val significantOutliers = outlierScenarios.filter { it.probability > 30.0 }
        if (significantOutliers.isEmpty()) return oracleScore
        
        // Find highest impact outlier
        val highestImpact = significantOutliers.maxByOrNull { it.impactScore } ?: return oracleScore
        
        // Check description for common outlier types
        val description = highestImpact.description.lowercase()
        return when {
            description.contains("rood") || description.contains("rode kaart") -> {
                // Red card scenario - reduce goals by 30%
                Pair(
                    (oracleScore.first * 0.7).toInt().coerceAtLeast(0),
                    (oracleScore.second * 0.7).toInt().coerceAtLeast(0)
                )
            }
            description.contains("weer") || description.contains("weersomstandigheden") -> {
                // Weather scenario - reduce all goals
                Pair(
                    (oracleScore.first * 0.6).toInt().coerceAtLeast(0),
                    (oracleScore.second * 0.6).toInt().coerceAtLeast(0)
                )
            }
            description.contains("blessure") || description.contains("speler mist") -> {
                // Injury scenario - moderate reduction
                Pair(
                    (oracleScore.first * 0.8).toInt().coerceAtLeast(0),
                    (oracleScore.second * 0.8).toInt().coerceAtLeast(0)
                )
            }
            else -> oracleScore
        }
    }

    /**
     * Favor draw score when Tesseract strongly suggests draw.
     */
    private fun favorDrawScore(
        oracleScore: Pair<Int, Int>,
        tesseractScore: Pair<Int, Int>
    ): Pair<Int, Int> {
        // If Tesseract suggests draw, use it
        if (tesseractScore.first == tesseractScore.second) {
            return tesseractScore
        }
        
        // Otherwise, move toward draw from Oracle score
        val homeAvg = (oracleScore.first + tesseractScore.first) / 2
        val awayAvg = (oracleScore.second + tesseractScore.second) / 2
        
        // If scores are close, make them equal
        return if (abs(homeAvg - awayAvg) <= 1) {
            val avg = (homeAvg + awayAvg) / 2
            Pair(avg, avg)
        } else {
            // Move one goal toward draw
            when {
                homeAvg > awayAvg -> Pair(homeAvg - 1, awayAvg + 1)
                else -> Pair(homeAvg + 1, awayAvg - 1)
            }
        }
    }

    /**
     * Apply risk adjustment based on LLMGRADE risk level.
     */
    private fun applyRiskAdjustment(
        oracleScore: Pair<Int, Int>,
        tesseractScore: Pair<Int, Int>,
        llmGrade: LLMGradeEnhancement
    ): Pair<Int, Int> {
        val riskLevel = llmGrade.overallRiskLevel()
        
        return when (riskLevel) {
            com.Lyno.matchmindai.domain.model.RiskLevel.HIGH -> {
                // High risk - moderate reduction
                val home = (oracleScore.first * 0.75 + tesseractScore.first * 0.25).toInt()
                val away = (oracleScore.second * 0.75 + tesseractScore.second * 0.25).toInt()
                Pair(home.coerceAtMost(3), away.coerceAtMost(3))
            }
            com.Lyno.matchmindai.domain.model.RiskLevel.MEDIUM -> {
                // Medium risk - slight reduction
                val home = (oracleScore.first * 0.85 + tesseractScore.first * 0.15).toInt()
                val away = (oracleScore.second * 0.85 + tesseractScore.second * 0.15).toInt()
                Pair(home.coerceAtMost(4), away.coerceAtMost(4))
            }
            else -> {
                // Low risk - standard weighted combination
                calculateWeightedScore(oracleScore, tesseractScore, 
                    PredictionWeights(oracle = 0.6f, tesseract = 0.4f, llmGrade = 0.0f))
            }
        }
    }

    /**
     * Calculate weighted score combination.
     */
    private fun calculateWeightedScore(
        oracleScore: Pair<Int, Int>,
        tesseractScore: Pair<Int, Int>,
        weights: PredictionWeights
    ): Pair<Int, Int> {
        val home = (oracleScore.first * weights.oracle + tesseractScore.first * weights.tesseract).toInt()
        val away = (oracleScore.second * weights.oracle + tesseractScore.second * weights.tesseract).toInt()
        
        return Pair(home.coerceAtLeast(0).coerceAtMost(5), away.coerceAtLeast(0).coerceAtMost(5))
    }

    /**
     * Calculate hybrid confidence.
     */
    private fun calculateHybridConfidence(
        oracle: OracleAnalysis,
        tesseract: TesseractResult?,
        llmGrade: LLMGradeEnhancement?,
        weights: PredictionWeights
    ): Int {
        var confidence = oracle.confidence * weights.oracle
        
        if (tesseract != null) {
            // Convert Tesseract probability to confidence (0-100)
            val tesseractConfidence = ((tesseract.homeWinProbability + tesseract.drawProbability + 
                                      tesseract.awayWinProbability) / 3 * 100).toInt()
            confidence += tesseractConfidence * weights.tesseract
        }
        
        if (llmGrade != null) {
            // LLMGRADE confidence based on context score
            val llmGradeConfidence = when (llmGrade.overallRiskLevel()) {
                com.Lyno.matchmindai.domain.model.RiskLevel.LOW -> 80
                com.Lyno.matchmindai.domain.model.RiskLevel.MEDIUM -> 60
                com.Lyno.matchmindai.domain.model.RiskLevel.HIGH -> 40
                else -> 50 // Default for any other risk level
            }
            confidence += llmGradeConfidence * weights.llmGrade
        }
        
        return confidence.toInt().coerceIn(0, 100)
    }

    /**
     * Determine primary prediction source.
     */
    private fun determinePrimarySource(
        oracle: OracleAnalysis,
        tesseract: TesseractResult?,
        llmGrade: LLMGradeEnhancement?,
        weights: PredictionWeights
    ): PredictionSource {
        return when {
            weights.oracle >= 0.5 -> PredictionSource.ORACLE
            weights.tesseract >= 0.5 -> PredictionSource.TESSERACT
            weights.llmGrade >= 0.5 -> PredictionSource.LLMGRADE
            oracle.confidence > 70 -> PredictionSource.ORACLE
            tesseract != null && (tesseract.homeWinProbability > 0.5 || 
                                 tesseract.awayWinProbability > 0.5) -> PredictionSource.TESSERACT
            llmGrade != null && llmGrade.contextFactors.size >= 3 -> PredictionSource.LLMGRADE
            else -> PredictionSource.HYBRID
        }
    }

    /**
     * Generate hybrid reasoning.
     */
    private fun generateHybridReasoning(
        oracle: OracleAnalysis,
        tesseract: TesseractResult?,
        llmGrade: LLMGradeEnhancement?,
        hybridScore: Pair<Int, Int>,
        primarySource: PredictionSource
    ): String {
        val sb = StringBuilder()
        
        sb.append("Hybrid prediction combining ")
        
        when (primarySource) {
            PredictionSource.ORACLE -> sb.append("Oracle's fact-based analysis")
            PredictionSource.TESSERACT -> sb.append("Tesseract's Monte Carlo simulation")
            PredictionSource.LLMGRADE -> sb.append("LLMGRADE's contextual insights")
            else -> sb.append("multiple prediction sources")
        }
        
        sb.append(". ")
        
        // Add Oracle reasoning
        sb.append("Oracle: ${oracle.reasoning} ")
        
        // Add Tesseract info if available
        tesseract?.let {
            sb.append("Tesseract simulation suggests ${it.mostLikelyScore} (${(it.homeWinProbability * 100).toInt()}% home, " +
                     "${(it.drawProbability * 100).toInt()}% draw, ${(it.awayWinProbability * 100).toInt()}% away). ")
        }
        
        // Add LLMGRADE info if available
        llmGrade?.let {
            if (it.contextFactors.isNotEmpty()) {
                sb.append("LLMGRADE identified ${it.contextFactors.size} context factors. ")
            }
            if (it.outlierScenarios.isNotEmpty()) {
                sb.append("${it.outlierScenarios.size} outlier scenario(s) considered. ")
            }
        }
        
        sb.append("Final hybrid prediction: ${hybridScore.first}-${hybridScore.second}.")
        
        return sb.toString()
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
     * Absolute value helper.
     */
    private fun abs(value: Int): Int = if (value < 0) -value else value
}

/**
 * Prediction weights for hybrid combination.
 */
data class PredictionWeights(
    val oracle: Float,
    val tesseract: Float,
    val llmGrade: Float
)

/**
 * Result of hybrid prediction.
 */
data class HybridPrediction(
    val score: String,
    val confidence: Int,
    val primarySource: PredictionSource,
    val reasoning: String
)

/**
 * Prediction source enum.
 */
enum class PredictionSource {
    ORACLE,
    TESSERACT,
    LLMGRADE,
    HYBRID
}

// Extension functions for LLMGradeEnhancement
private fun LLMGradeEnhancement.hasSignificantOutliers(): Boolean {
    return outlierScenarios.any { it.probability > 30.0 && it.impactScore >= 7 }
}

private fun LLMGradeEnhancement.overallRiskLevel(): com.Lyno.matchmindai.domain.model.RiskLevel {
    return when (overallContextScore) {
        in 0.0..3.0 -> com.Lyno.matchmindai.domain.model.RiskLevel.LOW
        in 3.1..6.0 -> com.Lyno.matchmindai.domain.model.RiskLevel.MEDIUM
        else -> com.Lyno.matchmindai.domain.model.RiskLevel.HIGH
    }
}
