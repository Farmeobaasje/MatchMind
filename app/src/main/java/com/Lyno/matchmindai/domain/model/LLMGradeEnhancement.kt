package com.Lyno.matchmindai.domain.model

/**
 * LLMGRADE Enhancement - The result of LLM analysis on unstructured data.
 * Contains context factors and outlier scenarios that traditional models might miss.
 *
 * @property contextFactors List of context factors extracted from news/social media
 * @property outlierScenarios List of possible outlier scenarios
 * @property enhancedReasoning Enhanced reasoning in Dutch with LLM insights
 * @property confidenceAdjustment Confidence adjustment based on context factors (-20 to +20)
 * @property timestamp When this enhancement was generated
 */
data class LLMGradeEnhancement(
    val contextFactors: List<ContextFactor>,
    val outlierScenarios: List<OutlierScenario>,
    val enhancedReasoning: String,
    val confidenceAdjustment: Int,
    val timestamp: Long = System.currentTimeMillis()
) {
    init {
        require(confidenceAdjustment in -20..20) { 
            "Confidence adjustment must be between -20 and +20" 
        }
        require(enhancedReasoning.isNotBlank()) { 
            "Enhanced reasoning cannot be blank" 
        }
    }

    /**
     * Returns the overall context score (weighted average of context factors).
     */
    val overallContextScore: Double
        get() = if (contextFactors.isEmpty()) {
            5.0 // Neutral score if no factors
        } else {
            contextFactors.sumOf { it.weightedScore } / contextFactors.size
        }

    /**
     * Returns true if there are high-probability outlier scenarios.
     */
    val hasHighProbabilityOutliers: Boolean
        get() = outlierScenarios.any { it.isHighProbability }

    /**
     * Returns true if there are high-impact context factors.
     */
    val hasHighImpactFactors: Boolean
        get() = contextFactors.any { it.isHighImpact }

    /**
     * Returns the highest probability outlier scenario.
     */
    val highestProbabilityOutlier: OutlierScenario?
        get() = outlierScenarios.maxByOrNull { it.probability }

    /**
     * Returns the most impactful context factor.
     */
    val mostImpactfulFactor: ContextFactor?
        get() = contextFactors.maxByOrNull { it.weightedScore }

    /**
     * Returns a summary of the enhancement for display purposes.
     */
    fun summary(): String {
        return buildString {
            appendLine("LLMGRADE Analyse")
            appendLine("Context Score: ${String.format("%.1f", overallContextScore)}/10")
            
            if (contextFactors.isNotEmpty()) {
                appendLine("Belangrijkste factor: ${mostImpactfulFactor?.type?.dutchDescription()}")
            }
            
            if (outlierScenarios.isNotEmpty()) {
                val highRiskCount = outlierScenarios.count { it.riskLevel == RiskLevel.HIGH }
                if (highRiskCount > 0) {
                    appendLine("⚠️  $highRiskCount hoog-risico uitschieterscenario's")
                }
            }
            
            if (confidenceAdjustment != 0) {
                val sign = if (confidenceAdjustment > 0) "+" else ""
                appendLine("Vertrouwensaanpassing: $sign$confidenceAdjustment%")
            }
        }
    }

    /**
     * Returns the adjusted confidence based on base confidence and context factors.
     */
    fun calculateAdjustedConfidence(baseConfidence: Int): Int {
        val adjusted = baseConfidence + confidenceAdjustment
        return adjusted.coerceIn(0, 100)
    }

    /**
     * Returns the risk level based on context factors and outlier scenarios.
     */
    fun overallRiskLevel(): RiskLevel {
        val outlierRisk = outlierScenarios.maxOfOrNull { it.riskLevel } ?: RiskLevel.LOW
        val contextRisk = if (hasHighImpactFactors) RiskLevel.MEDIUM else RiskLevel.LOW
        
        return when {
            outlierRisk == RiskLevel.HIGH || contextRisk == RiskLevel.HIGH -> RiskLevel.HIGH
            outlierRisk == RiskLevel.MEDIUM || contextRisk == RiskLevel.MEDIUM -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }
    }
}

/**
 * Companion object with factory methods for LLMGradeEnhancement.
 */
object LLMGradeEnhancementFactory {
    
    /**
     * Creates an empty enhancement (when LLM analysis is not available).
     */
    fun createEmpty(): LLMGradeEnhancement {
        return LLMGradeEnhancement(
            contextFactors = emptyList(),
            outlierScenarios = emptyList(),
            enhancedReasoning = "Geen LLM analyse beschikbaar. Gebaseerd op traditionele modellen.",
            confidenceAdjustment = 0
        )
    }
    
    /**
     * Creates an enhancement from context factors only.
     */
    fun createFromContextFactors(
        contextFactors: List<ContextFactor>,
        enhancedReasoning: String
    ): LLMGradeEnhancement {
        val adjustment = calculateConfidenceAdjustment(contextFactors)
        
        return LLMGradeEnhancement(
            contextFactors = contextFactors,
            outlierScenarios = emptyList(),
            enhancedReasoning = enhancedReasoning,
            confidenceAdjustment = adjustment
        )
    }
    
    /**
     * Creates a full enhancement with both context factors and outlier scenarios.
     */
    fun createFullEnhancement(
        contextFactors: List<ContextFactor>,
        outlierScenarios: List<OutlierScenario>,
        enhancedReasoning: String
    ): LLMGradeEnhancement {
        val adjustment = calculateConfidenceAdjustment(contextFactors)
        
        return LLMGradeEnhancement(
            contextFactors = contextFactors,
            outlierScenarios = outlierScenarios,
            enhancedReasoning = enhancedReasoning,
            confidenceAdjustment = adjustment
        )
    }
    
    /**
     * Calculates confidence adjustment based on context factors.
     * Positive factors increase confidence, negative factors decrease it.
     */
    private fun calculateConfidenceAdjustment(factors: List<ContextFactor>): Int {
        if (factors.isEmpty()) return 0
        
        val weightedAverage = factors.sumOf { it.weightedScore } / factors.size
        
        // Convert 1-10 scale to -20 to +20 adjustment
        return when {
            weightedAverage >= 8.0 -> 15  // Very positive context
            weightedAverage >= 6.5 -> 10  // Positive context
            weightedAverage >= 5.5 -> 5   // Slightly positive
            weightedAverage >= 4.5 -> 0   // Neutral
            weightedAverage >= 3.5 -> -5  // Slightly negative
            weightedAverage >= 2.5 -> -10 // Negative
            else -> -15                   // Very negative
        }
    }
}
