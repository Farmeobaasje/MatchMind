package com.Lyno.matchmindai.domain.model

/**
 * Hybrid prediction combining Dixon-Coles statistical model with AI analysis.
 * This represents the enhanced prediction after Tavily + DeepSeek analysis.
 */
data class HybridPrediction(
    val originalPrediction: MatchPrediction, // Original Dixon-Coles prediction
    val enhancedPrediction: MatchPrediction, // Enhanced prediction with AI modifiers
    val analysis: AiAnalysisResult,          // AI analysis explaining the changes
    val breakingNewsUsed: List<String> = emptyList(), // News headlines used for analysis
    val timestamp: Long = System.currentTimeMillis() // When this analysis was performed
) {
    /**
     * Gets the percentage change for each outcome.
     * Returns (homeChange, drawChange, awayChange) as percentages.
     */
    fun getPercentageChanges(): Triple<Double, Double, Double> {
        val homeChange = (enhancedPrediction.homeWinProbability - originalPrediction.homeWinProbability) * 100
        val drawChange = (enhancedPrediction.drawProbability - originalPrediction.drawProbability) * 100
        val awayChange = (enhancedPrediction.awayWinProbability - originalPrediction.awayWinProbability) * 100
        return Triple(homeChange, drawChange, awayChange)
    }
    
    /**
     * Gets the most significant change for display.
     */
    fun getMostSignificantChange(): String {
        val (homeChange, drawChange, awayChange) = getPercentageChanges()
        val changes = listOf(
            "home" to homeChange,
            "draw" to drawChange,
            "away" to awayChange
        )
        
        val (outcome, change) = changes.maxByOrNull { Math.abs(it.second) } ?: return "No significant changes"
        
        val direction = if (change > 0) "increased" else "decreased"
        val absChange = String.format("%.1f", Math.abs(change))
        
        return "${outcome.capitalize()} probability $direction by $absChange%"
    }
    
    /**
     * Checks if the AI analysis made a meaningful difference.
     */
    fun hasMeaningfulChange(): Boolean {
        val (homeChange, drawChange, awayChange) = getPercentageChanges()
        return Math.abs(homeChange) > 2.0 || 
               Math.abs(drawChange) > 2.0 || 
               Math.abs(awayChange) > 2.0
    }
    
    /**
     * Gets a summary of the hybrid prediction.
     */
    fun getSummary(): String {
        return if (hasMeaningfulChange()) {
            "AI analysis updated the prediction. ${getMostSignificantChange()}. " +
            "Confidence: ${analysis.confidence_score}%"
        } else {
            "AI analysis confirms the original prediction. " +
            "No significant changes based on current news."
        }
    }
}
