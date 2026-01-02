package com.Lyno.matchmindai.domain.model

/**
 * Context Factor - A qualitative factor that can influence match outcomes.
 * Extracted from unstructured data (news, social media, press conferences) by LLM.
 *
 * @property type Type of context factor
 * @property score Score from 1-10 indicating the strength/impact of the factor
 * @property description Human-readable description in Dutch
 * @property weight Weight multiplier for this factor (default 1.0)
 */
data class ContextFactor(
    val type: ContextFactorType,
    val score: Int,
    val description: String,
    val weight: Double = 1.0
) {
    init {
        require(score in 1..10) { "Score must be between 1 and 10" }
        require(weight > 0) { "Weight must be positive" }
        require(description.isNotBlank()) { "Description cannot be blank" }
    }

    /**
     * Returns the weighted score (score * weight).
     */
    val weightedScore: Double
        get() = score * weight

    /**
     * Returns true if this is a high-impact factor (score >= 8).
     */
    val isHighImpact: Boolean
        get() = score >= 8

    /**
     * Returns true if this is a negative factor (score <= 4).
     */
    val isNegative: Boolean
        get() = score <= 4
}

/**
 * Type of context factor that can influence match outcomes.
 */
enum class ContextFactorType {
    TEAM_MORALE,
    INJURIES,
    TACTICAL_CHANGES,
    WEATHER,
    PRESSURE,
    HISTORICAL_ANOMALY;

    /**
     * Returns the default weight for this factor type.
     * Higher weights indicate more important factors.
     */
    fun defaultWeight(): Double {
        return when (this) {
            INJURIES -> 1.5          // Injuries have high impact
            TACTICAL_CHANGES -> 1.3   // Tactical changes are important
            TEAM_MORALE -> 1.2        // Morale affects performance
            PRESSURE -> 1.1           // Pressure can affect performance
            HISTORICAL_ANOMALY -> 1.0 // Historical patterns
            WEATHER -> 0.8            // Weather has moderate impact
        }
    }

    /**
     * Returns a Dutch description of the factor type.
     */
    fun dutchDescription(): String {
        return when (this) {
            TEAM_MORALE -> "Teammorale"
            INJURIES -> "Blessureproblematiek"
            TACTICAL_CHANGES -> "Tactische veranderingen"
            WEATHER -> "Weersinvloeden"
            PRESSURE -> "Externe drukfactoren"
            HISTORICAL_ANOMALY -> "Historische anomalieën"
        }
    }
}

/**
 * Companion object with factory methods for common context factors.
 */
object ContextFactorFactory {
    
    /**
     * Creates a team morale factor.
     */
    fun createTeamMorale(
        score: Int,
        description: String,
        weight: Double = ContextFactorType.TEAM_MORALE.defaultWeight()
    ): ContextFactor {
        return ContextFactor(
            type = ContextFactorType.TEAM_MORALE,
            score = score,
            description = description,
            weight = weight
        )
    }
    
    /**
     * Creates an injuries factor.
     */
    fun createInjuries(
        score: Int,
        description: String,
        weight: Double = ContextFactorType.INJURIES.defaultWeight()
    ): ContextFactor {
        return ContextFactor(
            type = ContextFactorType.INJURIES,
            score = score,
            description = description,
            weight = weight
        )
    }
    
    /**
     * Creates a tactical changes factor.
     */
    fun createTacticalChanges(
        score: Int,
        description: String,
        weight: Double = ContextFactorType.TACTICAL_CHANGES.defaultWeight()
    ): ContextFactor {
        return ContextFactor(
            type = ContextFactorType.TACTICAL_CHANGES,
            score = score,
            description = description,
            weight = weight
        )
    }
    
    /**
     * Creates a weather factor.
     */
    fun createWeather(
        score: Int,
        description: String,
        weight: Double = ContextFactorType.WEATHER.defaultWeight()
    ): ContextFactor {
        return ContextFactor(
            type = ContextFactorType.WEATHER,
            score = score,
            description = description,
            weight = weight
        )
    }
    
    /**
     * Creates a pressure factor.
     */
    fun createPressure(
        score: Int,
        description: String,
        weight: Double = ContextFactorType.PRESSURE.defaultWeight()
    ): ContextFactor {
        return ContextFactor(
            type = ContextFactorType.PRESSURE,
            score = score,
            description = description,
            weight = weight
        )
    }
    
    /**
     * Creates a historical anomaly factor.
     */
    fun createHistoricalAnomaly(
        score: Int,
        description: String,
        weight: Double = ContextFactorType.HISTORICAL_ANOMALY.defaultWeight()
    ): ContextFactor {
        return ContextFactor(
            type = ContextFactorType.HISTORICAL_ANOMALY,
            score = score,
            description = description,
            weight = weight
        )
    }
}
