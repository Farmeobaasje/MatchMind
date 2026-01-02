package com.Lyno.matchmindai.domain.model

/**
 * Outlier Scenario - A possible unexpected outcome that traditional models might miss.
 * Identified by LLM analysis of unstructured data and historical patterns.
 *
 * @property description Dutch description of the outlier scenario
 * @property probability Probability of this scenario occurring (0-100%)
 * @property supportingFactors List of factors supporting this scenario
 * @property historicalPrecedents List of historical precedents (similar matches)
 * @property impactScore Estimated impact on match outcome (1-10)
 */
data class OutlierScenario(
    val description: String,
    val probability: Double,
    val supportingFactors: List<String>,
    val historicalPrecedents: List<String>,
    val impactScore: Int = 5
) {
    init {
        require(probability in 0.0..100.0) { "Probability must be between 0 and 100" }
        require(impactScore in 1..10) { "Impact score must be between 1 and 10" }
        require(description.isNotBlank()) { "Description cannot be blank" }
    }

    /**
     * Returns true if this is a high-probability outlier (probability >= 70%).
     */
    val isHighProbability: Boolean
        get() = probability >= 70.0

    /**
     * Returns true if this is a high-impact outlier (impactScore >= 8).
     */
    val isHighImpact: Boolean
        get() = impactScore >= 8

    /**
     * Returns the risk level based on probability and impact.
     */
    val riskLevel: RiskLevel
        get() = when {
            probability >= 70.0 && impactScore >= 8 -> RiskLevel.HIGH
            probability >= 50.0 && impactScore >= 5 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }

    /**
     * Returns a short summary for display purposes.
     */
    fun shortSummary(): String {
        return "${description.take(50)}... (${probability.toInt()}% kans)"
    }
}


/**
 * Companion object with factory methods for common outlier scenarios.
 */
object OutlierScenarioFactory {
    
    /**
     * Creates a "surprise home win" outlier scenario.
     */
    fun createSurpriseHomeWin(
        homeTeam: String,
        awayTeam: String,
        probability: Double,
        supportingFactors: List<String> = emptyList(),
        historicalPrecedents: List<String> = emptyList()
    ): OutlierScenario {
        return OutlierScenario(
            description = "$homeTeam wint verrassend van $awayTeam ondanks onderdogstatus",
            probability = probability,
            supportingFactors = supportingFactors,
            historicalPrecedents = historicalPrecedents,
            impactScore = 8
        )
    }
    
    /**
     * Creates a "surprise away win" outlier scenario.
     */
    fun createSurpriseAwayWin(
        homeTeam: String,
        awayTeam: String,
        probability: Double,
        supportingFactors: List<String> = emptyList(),
        historicalPrecedents: List<String> = emptyList()
    ): OutlierScenario {
        return OutlierScenario(
            description = "$awayTeam wint verrassend uit bij $homeTeam",
            probability = probability,
            supportingFactors = supportingFactors,
            historicalPrecedents = historicalPrecedents,
            impactScore = 8
        )
    }
    
    /**
     * Creates a "high scoring draw" outlier scenario.
     */
    fun createHighScoringDraw(
        homeTeam: String,
        awayTeam: String,
        probability: Double,
        supportingFactors: List<String> = emptyList(),
        historicalPrecedents: List<String> = emptyList()
    ): OutlierScenario {
        return OutlierScenario(
            description = "Hoge gelijkspel (3-3 of 4-4) tussen $homeTeam en $awayTeam",
            probability = probability,
            supportingFactors = supportingFactors,
            historicalPrecedents = historicalPrecedents,
            impactScore = 7
        )
    }
    
    /**
     * Creates a "goalfest" outlier scenario.
     */
    fun createGoalfest(
        homeTeam: String,
        awayTeam: String,
        probability: Double,
        supportingFactors: List<String> = emptyList(),
        historicalPrecedents: List<String> = emptyList()
    ): OutlierScenario {
        return OutlierScenario(
            description = "Doelpuntfestijn (5+ goals) tussen $homeTeam en $awayTeam",
            probability = probability,
            supportingFactors = supportingFactors,
            historicalPrecedents = historicalPrecedents,
            impactScore = 6
        )
    }
    
    /**
     * Creates a "defensive stalemate" outlier scenario.
     */
    fun createDefensiveStalemate(
        homeTeam: String,
        awayTeam: String,
        probability: Double,
        supportingFactors: List<String> = emptyList(),
        historicalPrecedents: List<String> = emptyList()
    ): OutlierScenario {
        return OutlierScenario(
            description = "Defensieve impasse (0-0) tussen $homeTeam en $awayTeam",
            probability = probability,
            supportingFactors = supportingFactors,
            historicalPrecedents = historicalPrecedents,
            impactScore = 5
        )
    }
    
    /**
     * Creates a "late drama" outlier scenario.
     */
    fun createLateDrama(
        homeTeam: String,
        awayTeam: String,
        probability: Double,
        supportingFactors: List<String> = emptyList(),
        historicalPrecedents: List<String> = emptyList()
    ): OutlierScenario {
        return OutlierScenario(
            description = "Laatste minuten drama met beslissende doelpunten",
            probability = probability,
            supportingFactors = supportingFactors,
            historicalPrecedents = historicalPrecedents,
            impactScore = 9
        )
    }
}
