package com.Lyno.matchmindai.domain.model

import kotlin.math.abs

/**
 * Consensus analysis showing agreement between different prediction engines.
 * This model helps users understand when engines agree or disagree,
 * and provides insight into prediction reliability.
 *
 * @property oracleScore Oracle prediction score (e.g., "3-0")
 * @property tesseractScore Tesseract prediction score (e.g., "0-0")
 * @property llmGradeScore LLMGRADE prediction score (e.g., "2-1") or null
 * @property agreementLevel Overall agreement level between engines
 * @property confidenceDiscrepancy Maximum difference in confidence scores (0-100)
 * @property primaryDisagreement Description of the main disagreement point
 * @property engineWeights Weighting of each engine in final prediction
 * @property consensusScore Overall consensus score (0-100)
 */
data class ConsensusAnalysis(
    val oracleScore: String,
    val tesseractScore: String,
    val llmGradeScore: String?,
    val agreementLevel: AgreementLevel,
    val confidenceDiscrepancy: Int,
    val primaryDisagreement: String?,
    val engineWeights: EngineWeights,
    val consensusScore: Int // 0-100
) {
    init {
        require(confidenceDiscrepancy in 0..100) { "Confidence discrepancy must be between 0 and 100" }
        require(consensusScore in 0..100) { "Consensus score must be between 0 and 100" }
    }

    /**
     * Returns true if all engines agree on the outcome (win/draw/loss).
     */
    val hasOutcomeAgreement: Boolean
        get() {
            val oracleOutcome = getOutcome(oracleScore)
            val tesseractOutcome = getOutcome(tesseractScore)
            val llmGradeOutcome = llmGradeScore?.let { getOutcome(it) }
            
            return when (llmGradeOutcome) {
                null -> oracleOutcome == tesseractOutcome
                else -> oracleOutcome == tesseractOutcome && tesseractOutcome == llmGradeOutcome
            }
        }

    /**
     * Returns the most likely score based on engine consensus.
     */
    val consensusScorePrediction: String
        get() = when (agreementLevel) {
            AgreementLevel.HIGH -> oracleScore // High agreement = trust Oracle
            AgreementLevel.MEDIUM -> {
                // Weighted average of predictions
                val scores = listOfNotNull(
                    oracleScore to engineWeights.oracle,
                    tesseractScore to engineWeights.tesseract,
                    llmGradeScore?.let { it to engineWeights.llmGrade }
                )
                
                if (scores.isEmpty()) oracleScore
                else calculateWeightedScore(scores)
            }
            AgreementLevel.LOW -> {
                // Low agreement = show range or "unpredictable"
                val scores = listOf(oracleScore, tesseractScore, llmGradeScore).filterNotNull()
                if (scores.distinct().size == 1) scores.first()
                else "VARIABLE"
            }
        }

    /**
     * Returns a description of the consensus situation.
     */
    val consensusDescription: String
        get() = when (agreementLevel) {
            AgreementLevel.HIGH -> when {
                hasOutcomeAgreement -> "Alle motoren zijn het eens - Hoge betrouwbaarheid"
                else -> "Motoren zijn het eens over score, niet over uitkomst"
            }
            AgreementLevel.MEDIUM -> when {
                hasOutcomeAgreement -> "Motoren zijn het eens over uitkomst, verschillen in score"
                else -> "Motoren verschillen in voorspelling - Matige betrouwbaarheid"
            }
            AgreementLevel.LOW -> "Motoren zijn sterk verdeeld - Lage betrouwbaarheid"
        }

    /**
     * Returns color for consensus visualization.
     */
    val consensusColor: androidx.compose.ui.graphics.Color
        get() = when (agreementLevel) {
            AgreementLevel.HIGH -> androidx.compose.ui.graphics.Color(0xFF00C853) // Green
            AgreementLevel.MEDIUM -> androidx.compose.ui.graphics.Color(0xFFFFC107) // Yellow
            AgreementLevel.LOW -> androidx.compose.ui.graphics.Color(0xFFF44336) // Red
        }

    /**
     * Returns icon for consensus visualization.
     */
    val consensusIcon: String
        get() = when (agreementLevel) {
            AgreementLevel.HIGH -> "✅"
            AgreementLevel.MEDIUM -> "⚠️"
            AgreementLevel.LOW -> "❌"
        }

    companion object {
        /**
         * Create a ConsensusAnalysis from individual engine results.
         */
        fun create(
            oracleAnalysis: OracleAnalysis,
            tesseractResult: TesseractResult?,
            llmGradeEnhancement: com.Lyno.matchmindai.domain.model.LLMGradeEnhancement?
        ): ConsensusAnalysis {
            val tesseractScore = tesseractResult?.mostLikelyScore ?: "0-0"
            val llmGradeScore = llmGradeEnhancement?.let { 
                // LLMGradeEnhancement doesn't have predictedScore, use enhancedReasoning summary
                "LLM-Analyse"
            }
            
            // Calculate agreement level
            val agreementLevel = calculateAgreementLevel(
                oracleScore = oracleAnalysis.prediction,
                tesseractScore = tesseractScore,
                llmGradeScore = llmGradeScore
            )
            
            // Calculate confidence discrepancy
            val confidences = listOfNotNull(
                oracleAnalysis.confidence,
                tesseractResult?.let { 
                    // TesseractResult doesn't have confidence, use homeWinProbability
                    (it.homeWinProbability * 100).toInt()
                },
                llmGradeEnhancement?.let { 
                    // LLMGradeEnhancement doesn't have confidence, use overallContextScore
                    (it.overallContextScore * 10).toInt()
                }
            )
            val confidenceDiscrepancy = if (confidences.size >= 2) {
                confidences.maxOrNull()!! - confidences.minOrNull()!!
            } else 0
            
            // Identify primary disagreement
            val primaryDisagreement = identifyPrimaryDisagreement(
                oracleScore = oracleAnalysis.prediction,
                tesseractScore = tesseractScore,
                llmGradeScore = llmGradeScore,
                oracleReasoning = oracleAnalysis.reasoning,
                tesseractReasoning = tesseractResult?.let { 
                    // TesseractResult doesn't have reasoning, use mostLikelyScore
                    "Tesseract voorspelt: ${it.mostLikelyScore}"
                },
                llmGradeReasoning = llmGradeEnhancement?.enhancedReasoning
            )
            
            // Calculate engine weights based on confidence and data source
            val engineWeights = calculateEngineWeights(
                oracleAnalysis = oracleAnalysis,
                tesseractResult = tesseractResult,
                llmGradeEnhancement = llmGradeEnhancement
            )
            
            // Calculate consensus score
            val consensusScore = calculateConsensusScore(
                agreementLevel = agreementLevel,
                confidenceDiscrepancy = confidenceDiscrepancy,
                engineWeights = engineWeights
            )
            
            return ConsensusAnalysis(
                oracleScore = oracleAnalysis.prediction,
                tesseractScore = tesseractScore,
                llmGradeScore = llmGradeScore,
                agreementLevel = agreementLevel,
                confidenceDiscrepancy = confidenceDiscrepancy,
                primaryDisagreement = primaryDisagreement,
                engineWeights = engineWeights,
                consensusScore = consensusScore
            )
        }
        
        private fun calculateAgreementLevel(
            oracleScore: String,
            tesseractScore: String,
            llmGradeScore: String?
        ): AgreementLevel {
            val scores = listOfNotNull(oracleScore, tesseractScore, llmGradeScore)
            val uniqueScores = scores.distinct().size
            
            return when {
                uniqueScores == 1 -> AgreementLevel.HIGH
                uniqueScores == 2 && scores.size == 3 -> AgreementLevel.MEDIUM
                else -> AgreementLevel.LOW
            }
        }
        
        private fun identifyPrimaryDisagreement(
            oracleScore: String,
            tesseractScore: String,
            llmGradeScore: String?,
            oracleReasoning: String,
            tesseractReasoning: String?,
            llmGradeReasoning: String?
        ): String? {
            val disagreements = mutableListOf<String>()
            
            // Check Oracle vs Tesseract
            if (oracleScore != tesseractScore) {
                val oracleOutcome = getOutcome(oracleScore)
                val tesseractOutcome = getOutcome(tesseractScore)
                
                if (oracleOutcome != tesseractOutcome) {
                    disagreements.add("Oracle voorspelt $oracleOutcome, Tesseract voorspelt $tesseractOutcome")
                } else {
                    disagreements.add("Oracle en Tesseract verschillen in score ($oracleScore vs $tesseractScore)")
                }
            }
            
            // Check Oracle vs LLMGRADE
            llmGradeScore?.let {
                if (oracleScore != it) {
                    val oracleOutcome = getOutcome(oracleScore)
                    val llmGradeOutcome = getOutcome(it)
                    
                    if (oracleOutcome != llmGradeOutcome) {
                        disagreements.add("Oracle voorspelt $oracleOutcome, LLMGRADE voorspelt $llmGradeOutcome")
                    }
                }
            }
            
            return disagreements.firstOrNull()
        }
        
        private fun calculateEngineWeights(
            oracleAnalysis: OracleAnalysis,
            tesseractResult: TesseractResult?,
            llmGradeEnhancement: com.Lyno.matchmindai.domain.model.LLMGradeEnhancement?
        ): EngineWeights {
            // Base weights
            var oracleWeight = 0.4f
            var tesseractWeight = 0.3f
            var llmGradeWeight = 0.3f
            
            // Adjust based on confidence
            oracleWeight *= (oracleAnalysis.confidence / 100f)
            tesseractWeight *= (tesseractResult?.let { 
                // TesseractResult doesn't have confidence, use homeWinProbability
                (it.homeWinProbability * 100).toInt()
            } ?: 50) / 100f
            llmGradeWeight *= (llmGradeEnhancement?.let { 
                // LLMGradeEnhancement doesn't have confidence, use overallContextScore
                (it.overallContextScore * 10).toInt()
            } ?: 50) / 100f
            
            // Adjust based on data source quality
            oracleWeight *= when (oracleAnalysis.standingsSource) {
                DataSource.API_OFFICIAL -> 1.2f
                DataSource.CALCULATED -> 1.0f
                DataSource.PREVIOUS_SEASON -> 0.8f
                DataSource.DEFAULT -> 0.6f
            }
            
            // Normalize weights to sum to 1.0
            val total = oracleWeight + tesseractWeight + llmGradeWeight
            if (total > 0) {
                oracleWeight /= total
                tesseractWeight /= total
                llmGradeWeight /= total
            }
            
            return EngineWeights(
                oracle = oracleWeight,
                tesseract = tesseractWeight,
                llmGrade = llmGradeWeight
            )
        }
        
        private fun calculateConsensusScore(
            agreementLevel: AgreementLevel,
            confidenceDiscrepancy: Int,
            engineWeights: EngineWeights
        ): Int {
            var score = when (agreementLevel) {
                AgreementLevel.HIGH -> 80
                AgreementLevel.MEDIUM -> 50
                AgreementLevel.LOW -> 20
            }
            
            // Adjust for confidence discrepancy
            score = score - (confidenceDiscrepancy / 2f).toInt()
            
            // Adjust for balanced weights (more balanced = more reliable consensus)
            val weightBalance = 1f - kotlin.math.max(
                kotlin.math.abs(engineWeights.oracle - 0.33f),
                kotlin.math.max(
                    kotlin.math.abs(engineWeights.tesseract - 0.33f),
                    kotlin.math.abs(engineWeights.llmGrade - 0.33f)
                )
            )
            score = score + (weightBalance * 20f).toInt()
            
            return score.coerceIn(0, 100)
        }
        
        private fun getOutcome(score: String): String {
            val (home, away) = score.split("-").map { it.toInt() }
            return when {
                home > away -> "Thuiswinst"
                away > home -> "Uitwinst"
                else -> "Gelijkspel"
            }
        }
        
        private fun calculateWeightedScore(scores: List<Pair<String, Float>>): String {
            if (scores.isEmpty()) return "0-0"
            
            // Simple approach: return most common score
            val scoreCounts = mutableMapOf<String, Int>()
            scores.forEach { (score, weight) ->
                scoreCounts[score] = (scoreCounts[score] ?: 0) + (weight * 100).toInt()
            }
            
            return scoreCounts.maxByOrNull { it.value }?.key ?: "0-0"
        }
    }
}

/**
 * Agreement level between prediction engines.
 */
enum class AgreementLevel {
    HIGH,    // All engines agree on score or outcome
    MEDIUM,  // Engines agree on outcome but differ on score
    LOW      // Engines disagree on outcome
}

/**
 * Weighting of each prediction engine in final consensus.
 */
data class EngineWeights(
    val oracle: Float,    // 0.0 to 1.0
    val tesseract: Float, // 0.0 to 1.0
    val llmGrade: Float   // 0.0 to 1.0
) {
    init {
        require(oracle in 0f..1f) { "Oracle weight must be between 0.0 and 1.0" }
        require(tesseract in 0f..1f) { "Tesseract weight must be between 0.0 and 1.0" }
        require(llmGrade in 0f..1f) { "LLMGRADE weight must be between 0.0 and 1.0" }
    }
    
    /**
     * Returns formatted weights as percentages.
     */
    val formattedWeights: String
        get() = "Oracle: ${(oracle * 100).toInt()}%, " +
                "Tesseract: ${(tesseract * 100).toInt()}%, " +
                "LLMGRADE: ${(llmGrade * 100).toInt()}%"
    
    /**
     * Returns true if weights are relatively balanced.
     */
    val isBalanced: Boolean
        get() {
            val avg = (oracle + tesseract + llmGrade) / 3f
            val maxDeviation = kotlin.math.max(
                kotlin.math.abs(oracle - avg),
                kotlin.math.max(
                    kotlin.math.abs(tesseract - avg),
                    kotlin.math.abs(llmGrade - avg)
                )
            )
            return maxDeviation < 0.2f
        }
}
