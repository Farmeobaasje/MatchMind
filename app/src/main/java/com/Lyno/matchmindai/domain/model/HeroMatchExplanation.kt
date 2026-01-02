package com.Lyno.matchmindai.domain.model

/**
 * Explanation for why a match was selected as the hero match in the dashboard.
 * Provides AI-powered insights into match selection criteria.
 */
data class HeroMatchExplanation(
    val match: MatchFixture,
    val primaryReasons: List<String>, // "Hoge historische rivaliteit", "Beide teams in topvorm"
    val secondaryFactors: List<String>,
    val aiConfidence: Double, // 0.0-1.0 confidence in the explanation
    val bettingImplications: String?,
    val mastermindInsights: List<String> = emptyList(),
    val chaosLevel: String = "Gemiddeld",
    val atmosphereLevel: String = "Neutraal"
) {
    /**
     * Gets a summary of the explanation for display.
     */
    fun getSummary(): String {
        return if (primaryReasons.isNotEmpty()) {
            "Hero match geselecteerd omdat: ${primaryReasons.first()}"
        } else {
            "Top match gebaseerd op AI-curatie algoritme"
        }
    }
    
    /**
     * Gets the full explanation text.
     */
    fun getFullExplanation(): String {
        val builder = StringBuilder()
        
        builder.append("## 🏆 Hero Match Analyse\n\n")
        
        if (primaryReasons.isNotEmpty()) {
            builder.append("**Primaire redenen:**\n")
            primaryReasons.forEach { reason ->
                builder.append("• $reason\n")
            }
            builder.append("\n")
        }
        
        if (secondaryFactors.isNotEmpty()) {
            builder.append("**Secundaire factoren:**\n")
            secondaryFactors.take(3).forEach { factor ->
                builder.append("• $factor\n")
            }
            builder.append("\n")
        }
        
        if (mastermindInsights.isNotEmpty()) {
            builder.append("**Mastermind Insights:**\n")
            mastermindInsights.take(2).forEach { insight ->
                builder.append("• $insight\n")
            }
            builder.append("\n")
        }
        
        builder.append("**Chaos niveau:** $chaosLevel\n")
        builder.append("**Atmosfeer:** $atmosphereLevel\n")
        
        bettingImplications?.let {
            builder.append("\n**Betting implicaties:** $it")
        }
        
        return builder.toString()
    }
    
    /**
     * Gets the AI confidence as a percentage.
     */
    fun getConfidencePercentage(): Int {
        return (aiConfidence * 100).toInt()
    }
    
    /**
     * Checks if this explanation has meaningful insights.
     */
    fun hasMeaningfulInsights(): Boolean {
        return primaryReasons.isNotEmpty() || secondaryFactors.isNotEmpty() || mastermindInsights.isNotEmpty()
    }
}
