package com.Lyno.matchmindai.domain.model

import kotlinx.serialization.Serializable
import kotlin.math.min

/**
 * Domain model representing Kelly Criterion calculation results.
 * The Kelly Criterion helps determine optimal bet sizing based on
 * the probability of winning and the odds offered.
 *
 * Formula: f = (bp - q) / b
 * Where:
 * - f = fraction of bankroll to bet
 * - b = decimal odds - 1
 * - p = probability of winning (our prediction)
 * - q = probability of losing (1 - p)
 */
@Serializable
data class KellyResult(
    val fixtureId: Int,
    val homeTeam: String,
    val awayTeam: String,
    
    // Kelly calculations for each market
    val homeWinKelly: Double?,
    val drawKelly: Double?,
    val awayWinKelly: Double?,
    
    // Value scores (0-10)
    val homeWinValueScore: Int,
    val drawValueScore: Int,
    val awayWinValueScore: Int,
    
    // Best value bet
    val bestValueBet: ValueBet,
    
    // Risk assessment
    val riskLevel: RiskLevel,
    val recommendedStakePercentage: Double, // 0.0 to 1.0 (0% to 100%)
    
    // Analysis
    val analysis: String,
    val confidence: Int // 0-100
) {
    /**
     * Get the Kelly fraction for a specific market.
     */
    fun getKellyForMarket(market: BettingMarket): Double? = when (market) {
        BettingMarket.HOME_WIN -> homeWinKelly
        BettingMarket.DRAW -> drawKelly
        BettingMarket.AWAY_WIN -> awayWinKelly
    }

    /**
     * Get the value score for a specific market.
     */
    fun getValueScoreForMarket(market: BettingMarket): Int = when (market) {
        BettingMarket.HOME_WIN -> homeWinValueScore
        BettingMarket.DRAW -> drawValueScore
        BettingMarket.AWAY_WIN -> awayWinValueScore
    }

    /**
     * Check if a market is a value bet (positive Kelly).
     */
    fun isValueBet(market: BettingMarket): Boolean = when (market) {
        BettingMarket.HOME_WIN -> homeWinKelly?.let { it > 0.0 } ?: false
        BettingMarket.DRAW -> drawKelly?.let { it > 0.0 } ?: false
        BettingMarket.AWAY_WIN -> awayWinKelly?.let { it > 0.0 } ?: false
    }

    /**
     * Get the recommended stake as a formatted string.
     */
    val recommendedStakeFormatted: String
        get() = when {
            recommendedStakePercentage <= 0.0 -> "Niet wedden"
            recommendedStakePercentage < 0.01 -> "<1% van bankroll"
            else -> "${String.format("%.1f", recommendedStakePercentage * 100)}% van bankroll"
        }

    /**
     * Get the overall value score (0-10).
     */
    val overallValueScore: Int
        get() = kotlin.math.max(
            homeWinValueScore,
            kotlin.math.max(drawValueScore, awayWinValueScore)
        )

    /**
     * Get the Kelly fraction for the best value bet.
     */
    val bestKellyFraction: Double?
        get() = when (bestValueBet.market) {
            BettingMarket.HOME_WIN -> homeWinKelly
            BettingMarket.DRAW -> drawKelly
            BettingMarket.AWAY_WIN -> awayWinKelly
        }

    /**
     * Check if there's any value bet available.
     */
    val hasValueBet: Boolean
        get() = homeWinKelly?.let { it > 0.0 } == true ||
                drawKelly?.let { it > 0.0 } == true ||
                awayWinKelly?.let { it > 0.0 } == true

    /**
     * Get a summary of the Kelly analysis.
     */
    val summary: String
        get() = when {
            !hasValueBet -> "Geen waarde weddenschap gevonden"
            bestValueBet.valueScore >= 8 -> "⭐ Uitstekende waarde: ${bestValueBet.description}"
            bestValueBet.valueScore >= 6 -> "✅ Goede waarde: ${bestValueBet.description}"
            bestValueBet.valueScore >= 4 -> "⚠️ Matige waarde: ${bestValueBet.description}"
            else -> "❌ Lage waarde: ${bestValueBet.description}"
        }

    /**
     * Get the risk assessment description.
     */
    val riskDescription: String
        get() = when (riskLevel) {
            RiskLevel.LOW -> "Laag risico - Veilig voor beginners"
            RiskLevel.MEDIUM -> "Gemiddeld risico - Goede balans"
            RiskLevel.HIGH -> "Hoog risico - Alleen voor ervaren spelers"
            RiskLevel.VERY_HIGH -> "Zeer hoog risico - Wees voorzichtig"
        }

    companion object {
        /**
         * Create an empty KellyResult for matches without sufficient data.
         */
        fun empty(fixtureId: Int, homeTeam: String, awayTeam: String): KellyResult {
            return KellyResult(
                fixtureId = fixtureId,
                homeTeam = homeTeam,
                awayTeam = awayTeam,
                homeWinKelly = null,
                drawKelly = null,
                awayWinKelly = null,
                homeWinValueScore = 0,
                drawValueScore = 0,
                awayWinValueScore = 0,
                bestValueBet = ValueBet(
                    market = BettingMarket.HOME_WIN,
                    description = "Geen waarde weddenschap beschikbaar",
                    valueScore = 0
                ),
                riskLevel = RiskLevel.HIGH,
                recommendedStakePercentage = 0.0,
                analysis = "Onvoldoende data voor Kelly analyse",
                confidence = 0
            )
        }
    }
}

/**
 * Represents a value bet recommendation.
 */
@Serializable
data class ValueBet(
    val market: BettingMarket,
    val description: String,
    val valueScore: Int // 0-10
)

/**
 * Enum for betting markets.
 */
enum class BettingMarket {
    HOME_WIN,
    DRAW,
    AWAY_WIN
}

/**
 * Risk level for Kelly betting.
 */
enum class RiskLevel {
    LOW,       // Kelly < 0.05
    MEDIUM,    // 0.05 <= Kelly < 0.15
    HIGH,      // 0.15 <= Kelly < 0.25
    VERY_HIGH; // Kelly >= 0.25

    /**
     * Returns the Dutch translation.
     */
    fun dutch(): String {
        return when (this) {
            LOW -> "Laag"
            MEDIUM -> "Medium"
            HIGH -> "Hoog"
            VERY_HIGH -> "Zeer hoog"
        }
    }

    /**
     * Returns the color for this risk level.
     */
    fun color(): androidx.compose.ui.graphics.Color {
        return when (this) {
            LOW -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green
            MEDIUM -> androidx.compose.ui.graphics.Color(0xFFFFA726) // Orange/Yellow
            HIGH -> androidx.compose.ui.graphics.Color(0xFFEF5350) // Red
            VERY_HIGH -> androidx.compose.ui.graphics.Color(0xFFD32F2F) // Dark Red
        }
    }
}

/**
 * Helper function to calculate Kelly Criterion.
 * 
 * Formula: f = (bp - q) / b
 * Where:
 * - f = fraction of bankroll to bet
 * - b = decimal odds - 1
 * - p = probability of winning (our prediction)
 * - q = probability of losing (1 - p)
 * 
 * @param probability Our predicted probability (0.0 to 1.0)
 * @param decimalOdds Bookmaker decimal odds (must be > 1.0)
 * @param bankrollFraction Fractional Kelly multiplier for risk management (default 0.25 = quarter Kelly)
 * @return Kelly fraction (0.0 to 1.0) or null if no value bet
 */
fun calculateKellyFraction(
    probability: Double, 
    decimalOdds: Double, 
    bankrollFraction: Double = 0.25
): Double? {
    if (probability <= 0.0 || probability >= 1.0 || decimalOdds <= 1.0) {
        return null
    }
    
    val b = decimalOdds - 1.0
    val q = 1.0 - probability
    
    // Kelly formula: f = (bp - q) / b
    val rawKelly = (b * probability - q) / b
    
    // Return null for negative Kelly (no value bet)
    if (rawKelly <= 0.0) return null
    
    // Apply fractional Kelly for risk management
    val fractionalKelly = rawKelly * bankrollFraction
    
    // Cap at 25% of bankroll for extreme cases
    return kotlin.math.min(fractionalKelly, 0.25)
}

/**
 * Convert Kelly fraction to value score (0-10).
 */
fun kellyToValueScore(kelly: Double?): Int {
    return when {
        kelly == null -> 0
        kelly >= 0.25 -> 10
        kelly >= 0.20 -> 9
        kelly >= 0.15 -> 8
        kelly >= 0.10 -> 7
        kelly >= 0.08 -> 6
        kelly >= 0.06 -> 5
        kelly >= 0.04 -> 4
        kelly >= 0.02 -> 3
        kelly >= 0.01 -> 2
        else -> 1
    }
}

/**
 * Determine risk level based on Kelly fraction.
 * Adjusted thresholds for fractional Kelly (quarter Kelly).
 */
fun kellyToRiskLevel(kelly: Double?): RiskLevel {
    return when {
        kelly == null -> RiskLevel.HIGH
        kelly >= 0.20 -> RiskLevel.VERY_HIGH  // Adjusted from 0.25
        kelly >= 0.10 -> RiskLevel.HIGH       // Adjusted from 0.15
        kelly >= 0.04 -> RiskLevel.MEDIUM     // Adjusted from 0.05
        else -> RiskLevel.LOW
    }
}

/**
 * Get recommended stake percentage based on Kelly and risk level.
 * Uses fractional Kelly (quarter Kelly) for risk management.
 */
fun getRecommendedStake(kelly: Double?, riskLevel: RiskLevel): Double {
    if (kelly == null || kelly <= 0.0) return 0.0
    
    // Kelly already includes fractional adjustment (quarter Kelly)
    // Further reduce based on risk level
    return when (riskLevel) {
        RiskLevel.LOW -> kelly
        RiskLevel.MEDIUM -> kelly * 0.8
        RiskLevel.HIGH -> kelly * 0.5
        RiskLevel.VERY_HIGH -> kelly * 0.3
    }
}
