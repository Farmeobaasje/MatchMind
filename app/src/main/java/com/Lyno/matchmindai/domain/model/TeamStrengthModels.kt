package com.Lyno.matchmindai.domain.model

import kotlinx.serialization.Serializable

/**
 * Result of the statistical Dixon-Coles calculation (Phase 1)
 * Contains the base team strengths before AI modifiers are applied.
 */
@Serializable
data class BaseTeamStrength(
    val homeAttackStrength: Double,
    val homeDefenseStrength: Double,
    val awayAttackStrength: Double,
    val awayDefenseStrength: Double,
    val homeAdvantage: Double,
    val leagueAverageHomeGoals: Double,
    val leagueAverageAwayGoals: Double,
    val calculationConfidence: Double = 1.0 // 0.0 to 1.0
) {
    /**
     * Check if the base strengths are valid for prediction.
     */
    val isValid: Boolean
        get() = homeAttackStrength > 0.0 &&
                homeDefenseStrength > 0.0 &&
                awayAttackStrength > 0.0 &&
                awayDefenseStrength > 0.0 &&
                leagueAverageHomeGoals > 0.0 &&
                leagueAverageAwayGoals > 0.0 &&
                calculationConfidence in 0.0..1.0

    /**
     * Get expected goals based on base strengths.
     */
    fun getExpectedGoals(): Pair<Double, Double> {
        val expectedHomeGoals = homeAttackStrength * awayDefenseStrength * homeAdvantage * leagueAverageHomeGoals
        val expectedAwayGoals = awayAttackStrength * homeDefenseStrength * leagueAverageAwayGoals
        return Pair(expectedHomeGoals, expectedAwayGoals)
    }

    /**
     * Apply AI modifiers to create adjusted team strengths.
     */
    fun applyModifiers(modifiers: NewsImpactModifiers): AdjustedTeamStrength {
        return AdjustedTeamStrength(
            homeAttackStrength = homeAttackStrength * modifiers.homeAttackMod,
            homeDefenseStrength = homeDefenseStrength * modifiers.homeDefenseMod,
            awayAttackStrength = awayAttackStrength * modifiers.awayAttackMod,
            awayDefenseStrength = awayDefenseStrength * modifiers.awayDefenseMod,
            homeAdvantage = homeAdvantage,
            leagueAverageHomeGoals = leagueAverageHomeGoals,
            leagueAverageAwayGoals = leagueAverageAwayGoals,
            baseStrength = this,
            modifiers = modifiers
        )
    }
}

/**
 * Result of the AI Analysis (Phase 2)
 * Contains modifiers for team strengths based on news analysis.
 * Default values are 1.0 (no impact).
 */
@Serializable
data class NewsImpactModifiers(
    val homeAttackMod: Double = 1.0,      // Default 1.0 (no impact)
    val homeDefenseMod: Double = 1.0,
    val awayAttackMod: Double = 1.0,
    val awayDefenseMod: Double = 1.0,
    val confidence: Double,               // 0.0 to 1.0
    val reasoning: String,
    val chaosFactor: Double = 0.5,        // 0.0 to 1.0, how unpredictable is the match?
    val newsRelevance: Double = 1.0       // 0.0 to 1.0, how relevant is the news?
) {
    /**
     * Check if modifiers are within reasonable bounds.
     */
    val isValid: Boolean
        get() = homeAttackMod in 0.5..1.5 &&
                homeDefenseMod in 0.5..1.5 &&
                awayAttackMod in 0.5..1.5 &&
                awayDefenseMod in 0.5..1.5 &&
                confidence in 0.0..1.0 &&
                chaosFactor in 0.0..1.0 &&
                newsRelevance in 0.0..1.0

    /**
     * Check if modifiers have meaningful impact (not all 1.0).
     */
    val hasMeaningfulImpact: Boolean
        get() = homeAttackMod != 1.0 ||
                homeDefenseMod != 1.0 ||
                awayAttackMod != 1.0 ||
                awayDefenseMod != 1.0

    /**
     * Get overall impact score (0.0 to 1.0).
     */
    val impactScore: Double
        get() {
            val modifierDeviation = listOf(
                kotlin.math.abs(homeAttackMod - 1.0),
                kotlin.math.abs(homeDefenseMod - 1.0),
                kotlin.math.abs(awayAttackMod - 1.0),
                kotlin.math.abs(awayDefenseMod - 1.0)
            ).average()
            
            return (modifierDeviation * confidence * newsRelevance).coerceIn(0.0, 1.0)
        }

    /**
     * Sanity check: Flag if modifiers are too extreme.
     */
    val isExtreme: Boolean
        get() = homeAttackMod < 0.7 || homeAttackMod > 1.3 ||
                homeDefenseMod < 0.7 || homeDefenseMod > 1.3 ||
                awayAttackMod < 0.7 || awayAttackMod > 1.3 ||
                awayDefenseMod < 0.7 || awayDefenseMod > 1.3

    /**
     * Convert to legacy AiAnalysisResult for backward compatibility.
     */
    fun toAiAnalysisResult(): AiAnalysisResult {
        return AiAnalysisResult(
            home_attack_modifier = homeAttackMod,
            away_defense_modifier = awayDefenseMod,
            away_attack_modifier = awayAttackMod,
            home_defense_modifier = homeDefenseMod,
            chaos_score = (chaosFactor * 100).toInt(),
            confidence_score = (confidence * 100).toInt(),
            reasoning_short = reasoning,
            news_relevant = newsRelevance > 0.5
        )
    }
}

/**
 * Team strengths after applying AI modifiers.
 */
@Serializable
data class AdjustedTeamStrength(
    val homeAttackStrength: Double,
    val homeDefenseStrength: Double,
    val awayAttackStrength: Double,
    val awayDefenseStrength: Double,
    val homeAdvantage: Double,
    val leagueAverageHomeGoals: Double,
    val leagueAverageAwayGoals: Double,
    val baseStrength: BaseTeamStrength,
    val modifiers: NewsImpactModifiers
) {
    /**
     * Get expected goals after applying modifiers.
     */
    fun getExpectedGoals(): Pair<Double, Double> {
        val expectedHomeGoals = homeAttackStrength * awayDefenseStrength * homeAdvantage * leagueAverageHomeGoals
        val expectedAwayGoals = awayAttackStrength * homeDefenseStrength * leagueAverageAwayGoals
        return Pair(expectedHomeGoals, expectedAwayGoals)
    }

    /**
     * Get prediction confidence (0.0 to 1.0).
     */
    val predictionConfidence: Double
        get() = baseStrength.calculationConfidence * modifiers.confidence * (1.0 - modifiers.chaosFactor * 0.5)

    /**
     * Check if the adjusted strengths are valid.
     */
    val isValid: Boolean
        get() = homeAttackStrength > 0.0 &&
                homeDefenseStrength > 0.0 &&
                awayAttackStrength > 0.0 &&
                awayDefenseStrength > 0.0 &&
                leagueAverageHomeGoals > 0.0 &&
                leagueAverageAwayGoals > 0.0 &&
                baseStrength.isValid &&
                modifiers.isValid
}

/**
 * Enhanced prediction result with xG and AI integration.
 */
@Serializable
data class EnhancedPrediction(
    val fixtureId: Int,
    val homeTeam: String,
    val awayTeam: String,
    val homeWinProbability: Double,
    val drawProbability: Double,
    val awayWinProbability: Double,
    val expectedGoalsHome: Double,      // Based on xG data
    val expectedGoalsAway: Double,      // Based on xG data
    val valueEdge: Double,              // Difference with bookmaker (0.0 to 1.0)
    val kellyStake: Double,             // Fractional Kelly stake (0.0 to 0.25)
    val baseTeamStrength: BaseTeamStrength,
    val newsImpactModifiers: NewsImpactModifiers? = null,
    val simulationContext: SimulationContext? = null, // Phase 1: Trinity metrics
    val reasoning: String = "Statistische voorspelling", // Phase 1: Prediction reasoning
    val confidence: Double = 0.5,       // Overall confidence (0.0 to 1.0)
    val calculationTimestamp: Long = System.currentTimeMillis()
) {
    /**
     * Check if this is a value bet (positive edge).
     */
    val isValueBet: Boolean
        get() = valueEdge > 0.05 && kellyStake > 0.01

    /**
     * Get risk level based on confidence and stake.
     */
    val riskLevel: RiskLevel
        get() = when {
            confidence < 0.3 || kellyStake > 0.15 -> RiskLevel.HIGH
            confidence < 0.6 || kellyStake > 0.08 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }

    /**
     * Convert to legacy MatchPrediction for backward compatibility.
     */
    fun toMatchPrediction(): MatchPrediction {
        return MatchPrediction(
            fixtureId = fixtureId,
            homeTeam = homeTeam,
            awayTeam = awayTeam,
            homeWinProbability = homeWinProbability,
            drawProbability = drawProbability,
            awayWinProbability = awayWinProbability,
            expectedGoalsHome = expectedGoalsHome,
            expectedGoalsAway = expectedGoalsAway,
            analysis = newsImpactModifiers?.reasoning ?: "Statistische voorspelling",
            confidenceScore = (confidence * 100).toInt()
        )
    }
}


/**
 * Expected Goals (xG) data for a fixture.
 */
@Serializable
data class ExpectedGoalsData(
    val fixtureId: Int,
    val homeTeamId: Int,
    val awayTeamId: Int,
    val homeXg: Double,                 // Expected goals for home team
    val awayXg: Double,                 // Expected goals for away team
    val homeShotsOnTarget: Int,         // Shots on target for home team
    val awayShotsOnTarget: Int,         // Shots on target for away team
    val homeTotalShots: Int,            // Total shots for home team
    val awayTotalShots: Int,            // Total shots for away team
    val homePossession: Double,         // Possession percentage (0-100)
    val awayPossession: Double,         // Possession percentage (0-100)
    val date: String,
    val leagueId: Int,
    val dataQuality: Double = 1.0       // 0.0 to 1.0, quality of xG data
) {
    /**
     * Calculate input score for Dixon-Coles model.
     * Uses xG if available, falls back to shots on target, then actual goals.
     * IMPROVED: Handles partial xG data (when only one team has xG)
     * 
     * @return Triple of (homeScore, awayScore, dataSource) where dataSource indicates which metric was used
     */
    fun getInputScoreWithSource(actualHomeGoals: Int, actualAwayGoals: Int): Triple<Double, Double, String> {
        return if (homeXg > 0.0 || awayXg > 0.0) {
            // Use xG data for teams that have it, fallback to actual goals for others
            val homeScore = if (homeXg > 0.0) {
                (0.7 * homeXg) + (0.3 * actualHomeGoals) // xG + smoothing
            } else {
                actualHomeGoals.toDouble() // Fallback to actual goals
            }
            
            val awayScore = if (awayXg > 0.0) {
                (0.7 * awayXg) + (0.3 * actualAwayGoals) // xG + smoothing
            } else {
                actualAwayGoals.toDouble() // Fallback to actual goals
            }
            
            // Determine data source
            val dataSource = when {
                homeXg > 0.0 && awayXg > 0.0 -> "XG"
                homeXg > 0.0 || awayXg > 0.0 -> "XG_PARTIAL"
                homeShotsOnTarget > 0 || awayShotsOnTarget > 0 -> "SHOTS"
                else -> "GOALS"
            }
            
            Triple(homeScore, awayScore, dataSource)
        } else if (homeShotsOnTarget > 0 || awayShotsOnTarget > 0) {
            // Fallback: Use shots on target (divided by 3 as rough xG proxy)
            Triple(
                homeShotsOnTarget.toDouble() / 3.0,
                awayShotsOnTarget.toDouble() / 3.0,
                "SHOTS"
            )
        } else {
            // Final fallback: Use actual goals
            Triple(
                actualHomeGoals.toDouble(),
                actualAwayGoals.toDouble(),
                "GOALS"
            )
        }
    }

    /**
     * Calculate input score for Dixon-Coles model (legacy version without data source).
     * Uses xG if available, falls back to shots on target, then actual goals.
     */
    fun getInputScore(actualHomeGoals: Int, actualAwayGoals: Int): Pair<Double, Double> {
        val (homeScore, awayScore, _) = getInputScoreWithSource(actualHomeGoals, actualAwayGoals)
        return Pair(homeScore, awayScore)
    }

    /**
     * Check if xG data is available.
     * Improved: Consider xG data available if at least one team has xG > 0.0
     * This prevents the fallback when only one team's xG is available
     */
    val hasXgData: Boolean
        get() = homeXg > 0.0 || awayXg > 0.0

    /**
     * Check if data quality is sufficient for analysis.
     */
    val isHighQuality: Boolean
        get() = dataQuality > 0.7
}
