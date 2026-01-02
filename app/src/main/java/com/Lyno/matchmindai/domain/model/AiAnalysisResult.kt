    package com.Lyno.matchmindai.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * AI analysis result from DeepSeek containing modifiers for team performance.
 * This is the JSON structure we expect from DeepSeek API.
 * 
 * MASTERMIND MODEL: Enhanced with psychological and tactical analysis.
 */
@Serializable
data class AiAnalysisResult(
    // The hard numbers for Dixon-Coles - support both old and new field names
    @SerialName("home_attack_modifier")
    val home_attack_modifier: Double = 1.0,
    
    @SerialName("home_attack_mod")
    private val _home_attack_mod: Double? = null,
    
    @SerialName("away_defense_modifier")
    val away_defense_modifier: Double = 1.0,
    
    @SerialName("away_defense_mod")
    private val _away_defense_mod: Double? = null,
    
    @SerialName("away_attack_modifier")
    val away_attack_modifier: Double = 1.0,
    
    @SerialName("away_attack_mod")
    private val _away_attack_mod: Double? = null,
    
    @SerialName("home_defense_modifier")
    val home_defense_modifier: Double = 1.0,
    
    @SerialName("home_defense_mod")
    private val _home_defense_mod: Double? = null,
    
    // NEW: Mastermind Metrics - support both old and new field names
    @SerialName("chaos_score")
    val chaos_score: Int = 50, // 0-100: How unpredictable is this match? (Derbies/Cards/High Variance)
    
    @SerialName("chaos_factor")
    private val _chaos_factor: Double? = null, // 0.0-1.0: New format
    
    @SerialName("atmosphere_score")
    val atmosphere_score: Int = 50, // 0-100: Home advantage impact (50 = neutral, 100 = fortress, 0 = dead)
    
    // NEW: The Narrative
    val primary_scenario_title: String = "",
    val primary_scenario_desc: String = "",
    val tactical_key: String = "", // "Exploit the high line"
    val key_player_watch: String = "",
    
    // NEW: Betting fields for Mastermind
    val betting_tip: String = "",
    val betting_confidence: Int = 0, // 0 means not calculated yet, will be derived from confidence_score
    
    // Legacy fields (backward compatibility) - support both old and new field names
    @SerialName("reasoning_short")
    val reasoning_short: String = "",
    
    @SerialName("reasoning")
    private val _reasoning: String? = null,
    
    @SerialName("confidence_score")
    val confidence_score: Int = 0, // 0-100, how confident is the AI about this news? 0 means not calculated
    
    @SerialName("confidence")
    private val _confidence: Double? = null, // 0.0-1.0: New format
    
    @SerialName("news_relevant")
    val news_relevant: Boolean = true, // Whether the news is actually about the match teams
    
    @SerialName("news_relevance")
    private val _news_relevance: Double? = null, // 0.0-1.0: New format

    // NEW: Simulation Context fields for Phase 2 (Trinity Metrics)
    @SerialName("fatigue_score")
    val fatigue_score: Int = 50, // 0-100 (Higher = more fatigue)
    
    @SerialName("style_matchup")
    val style_matchup: Double = 1.0, // 0.5-2.0 (1.0 = neutral, >1.0 = advantage)
    
    @SerialName("lineup_strength")
    val lineup_strength: Int = 100, // 0-100 (100 = full strength)
    
    // Legacy fields for backward compatibility
    @SerialName("home_distraction")
    private val _home_distraction: Int? = null,
    
    @SerialName("away_distraction")
    private val _away_distraction: Int? = null,
    
    @SerialName("home_fitness")
    private val _home_fitness: Int? = null,
    
    @SerialName("away_fitness")
    private val _away_fitness: Int? = null
) {
    /**
     * Get home attack modifier, preferring new format if available.
     */
    val homeAttackMod: Double
        get() = _home_attack_mod ?: home_attack_modifier
    
    /**
     * Get away defense modifier, preferring new format if available.
     */
    val awayDefenseMod: Double
        get() = _away_defense_mod ?: away_defense_modifier
    
    /**
     * Get away attack modifier, preferring new format if available.
     */
    val awayAttackMod: Double
        get() = _away_attack_mod ?: away_attack_modifier
    
    /**
     * Get home defense modifier, preferring new format if available.
     */
    val homeDefenseMod: Double
        get() = _home_defense_mod ?: home_defense_modifier
    
    /**
     * Get chaos factor, converting from score if needed.
     */
    val chaosFactor: Double
        get() = _chaos_factor ?: (chaos_score / 100.0)
    
    /**
     * Get confidence, converting from score if needed.
     */
    val confidence: Double
        get() = _confidence ?: (confidence_score / 100.0)
    
    /**
     * Get reasoning, preferring new format if available.
     */
    val reasoning: String
        get() = _reasoning ?: reasoning_short
    
    /**
     * Get news relevance, converting from boolean if needed.
     */
    val newsRelevance: Double
        get() = _news_relevance ?: (if (news_relevant) 1.0 else 0.3)
    /**
     * Validates that all modifiers are within reasonable bounds.
     */
    fun isValid(): Boolean {
        return home_attack_modifier in 0.5..1.5 &&
               away_defense_modifier in 0.5..1.5 &&
               away_attack_modifier in 0.5..1.5 &&
               home_defense_modifier in 0.5..1.5 &&
               confidence_score in 0..100 &&
               chaos_score in 0..100 &&
               atmosphere_score in 0..100
    }
    
    /**
     * Gets a summary of the analysis for display.
     */
    fun getSummary(): String {
        return if (reasoning_short.isNotEmpty()) {
            reasoning_short
        } else {
            "AI analysis complete. " +
            "Attack modifiers: Home ${String.format("%.0f", (home_attack_modifier - 1.0) * 100)}%, " +
            "Away ${String.format("%.0f", (away_attack_modifier - 1.0) * 100)}%. " +
            "Confidence: $confidence_score%"
        }
    }
    
    /**
     * Gets the mastermind scenario description.
     * Returns primary_scenario_desc if available, otherwise falls back to reasoning_short.
     */
    fun getMastermindScenario(): String {
        return if (primary_scenario_desc.isNotEmpty()) {
            primary_scenario_desc
        } else {
            reasoning_short
        }
    }
    
    /**
     * Gets the chaos level description.
     */
    fun getChaosLevel(): String {
        return when {
            chaos_score >= 80 -> "Totale Oorlog"
            chaos_score >= 60 -> "Hoog Risico"
            chaos_score >= 40 -> "Gemiddeld"
            chaos_score >= 20 -> "Gecontroleerd"
            else -> "Voorspelbaar"
        }
    }
    
    /**
     * Gets the atmosphere level description.
     */
    fun getAtmosphereLevel(): String {
        return when {
            atmosphere_score >= 80 -> "Heksenketel"
            atmosphere_score >= 60 -> "Sterk Thuispubliek"
            atmosphere_score >= 40 -> "Neutraal"
            atmosphere_score >= 20 -> "Rustig"
            else -> "Doods"
        }
    }
    
    /**
     * Checks if this is a mastermind analysis (has enhanced fields).
     */
    fun isMastermindAnalysis(): Boolean {
        return primary_scenario_title.isNotEmpty() || tactical_key.isNotEmpty()
    }
    
    /**
     * Gets the betting confidence, calculating it dynamically if not set.
     * Uses confidence_score as base and adjusts based on chaos and atmosphere.
     */
    fun getBettingConfidence(): Int {
        if (betting_confidence > 0) {
            return betting_confidence
        }
        
        // Calculate dynamic confidence based on analysis factors
        var calculatedConfidence = confidence_score
        
        // Adjust based on chaos (higher chaos = lower confidence)
        if (chaos_score > 70) {
            calculatedConfidence = (calculatedConfidence * 0.8).toInt()
        } else if (chaos_score > 50) {
            calculatedConfidence = (calculatedConfidence * 0.9).toInt()
        }
        
        // Adjust based on atmosphere (higher atmosphere = higher confidence for home team)
        if (atmosphere_score > 80) {
            calculatedConfidence = (calculatedConfidence * 1.1).toInt().coerceAtMost(100)
        }
        
        // Ensure confidence is at least 10 and at most 100
        return calculatedConfidence.coerceIn(10, 100)
    }
    
    /**
     * Gets a dynamic betting tip based on analysis if not already set.
     * Generates tips based on modifiers, chaos, and atmosphere.
     */
    fun getBettingTip(homeTeam: String = "Thuis", awayTeam: String = "Uit"): String {
        if (betting_tip.isNotEmpty()) {
            return betting_tip
        }
        
        // Generate dynamic tip based on analysis
        val homeAdvantage = home_attack_modifier > 1.1
        val awayAdvantage = away_attack_modifier > 1.1
        val highChaos = chaos_score > 70
        val strongAtmosphere = atmosphere_score > 80
        
        return when {
            homeAdvantage && strongAtmosphere -> "$homeTeam wint & Over 1.5 Goals"
            awayAdvantage && !strongAtmosphere -> "$awayTeam wint & Beide Teams Scoren"
            highChaos -> "Over 2.5 Goals & Veel kaarten verwacht"
            home_attack_modifier > away_attack_modifier -> "$homeTeam wint of Gelijk"
            else -> "Beide Teams Scoren & Over 2.5 Goals"
        }
    }
    
    /**
     * Validates that the analysis has meaningful data (not all defaults).
     */
    fun hasMeaningfulData(): Boolean {
        return confidence_score > 0 || 
               betting_confidence > 0 || 
               primary_scenario_title.isNotEmpty() ||
               home_attack_modifier != 1.0 ||
               away_attack_modifier != 1.0
    }

    /**
     * Converts this AI analysis to a SimulationContext for Tesseract engine.
     * Uses Trinity metrics (fatigue, style matchup, lineup strength).
     */
    fun toSimulationContext(): SimulationContext {
        return SimulationContext(
            fatigueScore = fatigue_score.coerceIn(0, 100),
            styleMatchup = style_matchup.coerceIn(0.5, 2.0),
            lineupStrength = lineup_strength.coerceIn(0, 100),
            reasoning = reasoning
        )
    }
    
    /**
     * Legacy getters for backward compatibility
     */
    val home_distraction: Int
        get() = _home_distraction ?: 0
    
    val away_distraction: Int
        get() = _away_distraction ?: 0
    
    val home_fitness: Int
        get() = _home_fitness ?: 100
    
    val away_fitness: Int
        get() = _away_fitness ?: 100
}
