package com.Lyno.matchmindai.domain.usecase

import com.Lyno.matchmindai.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Enhanced Kelly Criterion calculator with fractional Kelly and risk management.
 * 
 * This implements MatchMind AI 2.0 risk management:
 * 1. Fractional Kelly (max 25% of full Kelly) until edge is proven
 * 2. Confidence-based stake adjustment
 * 3. Value edge calculation vs bookmaker closing lines
 * 4. Risk level classification based on data quality
 * 
 * Formula: f = (bp - q) / b
 * Where:
 * - f = fraction of bankroll to bet
 * - b = decimal odds - 1
 * - p = probability of winning (our prediction)
 * - q = probability of losing (1 - p)
 * 
 * IMPORTANT: Uses fractional Kelly (fraction = 0.25) for risk management.
 */
class EnhancedCalculateKellyUseCase {
    
    companion object {
        // Fractional Kelly parameters
        private const val FRACTIONAL_KELLY_FACTOR = 0.25 // Use 25% of full Kelly
        private const val MAX_STAKE_PERCENTAGE = 0.05   // Max 5% of bankroll
        private const val MIN_STAKE_PERCENTAGE = 0.005  // Min 0.5% of bankroll
        
        // Confidence thresholds
        private const val HIGH_CONFIDENCE_THRESHOLD = 0.7
        private const val MEDIUM_CONFIDENCE_THRESHOLD = 0.4
        
        // Value edge thresholds
        private const val HIGH_VALUE_EDGE = 0.10  // 10% edge
        private const val MEDIUM_VALUE_EDGE = 0.05 // 5% edge
        private const val MIN_VALUE_EDGE = 0.02   // 2% edge
    }

    /**
     * Calculate Kelly Criterion for an enhanced prediction.
     * 
     * @param enhancedPrediction Enhanced prediction with xG and AI integration
     * @param odds Bookmaker odds for the match
     * @param closingLineOdds Optional closing line odds for value edge calculation
     * @return Result containing KellyResult with fractional Kelly recommendations
     */
    suspend operator fun invoke(
        enhancedPrediction: EnhancedPrediction,
        odds: OddsData,
        closingLineOdds: OddsData? = null
    ): Result<KellyResult> = withContext(Dispatchers.Default) {
        try {
            // Validate input data
            if (!enhancedPrediction.hasValidProbabilities() || !odds.hasOdds) {
                return@withContext Result.success(
                    KellyResult.empty(enhancedPrediction.fixtureId, enhancedPrediction.homeTeam, enhancedPrediction.awayTeam)
                )
            }
            
            // Calculate Kelly for each market with fractional adjustment
            val homeWinKelly = calculateFractionalKellyForMarket(
                probability = enhancedPrediction.homeWinProbability,
                odds = odds.homeWinOdds,
                confidence = enhancedPrediction.confidence
            )
            
            val drawKelly = calculateFractionalKellyForMarket(
                probability = enhancedPrediction.drawProbability,
                odds = odds.drawOdds,
                confidence = enhancedPrediction.confidence
            )
            
            val awayWinKelly = calculateFractionalKellyForMarket(
                probability = enhancedPrediction.awayWinProbability,
                odds = odds.awayWinOdds,
                confidence = enhancedPrediction.confidence
            )
            
            // Calculate value edges vs bookmaker
            val homeWinValueEdge = calculateValueEdge(
                ourProbability = enhancedPrediction.homeWinProbability,
                bookmakerProbability = odds.homeWinProbability,
                closingLineProbability = closingLineOdds?.homeWinProbability
            )
            
            val drawValueEdge = calculateValueEdge(
                ourProbability = enhancedPrediction.drawProbability,
                bookmakerProbability = odds.drawProbability,
                closingLineProbability = closingLineOdds?.drawProbability
            )
            
            val awayWinValueEdge = calculateValueEdge(
                ourProbability = enhancedPrediction.awayWinProbability,
                bookmakerProbability = odds.awayWinProbability,
                closingLineProbability = closingLineOdds?.awayWinProbability
            )
            
            // Calculate value scores based on Kelly and value edge
            val homeWinValueScore = calculateValueScore(homeWinKelly, homeWinValueEdge)
            val drawValueScore = calculateValueScore(drawKelly, drawValueEdge)
            val awayWinValueScore = calculateValueScore(awayWinKelly, awayWinValueEdge)
            
            // Determine best value bet
            val bestValueBet = determineBestValueBet(
                homeTeam = enhancedPrediction.homeTeam,
                awayTeam = enhancedPrediction.awayTeam,
                homeWinKelly = homeWinKelly,
                drawKelly = drawKelly,
                awayWinKelly = awayWinKelly,
                homeWinValueEdge = homeWinValueEdge,
                drawValueEdge = drawValueEdge,
                awayWinValueEdge = awayWinValueEdge,
                homeWinValueScore = homeWinValueScore,
                drawValueScore = drawValueScore,
                awayWinValueScore = awayWinValueScore
            )
            
            // Determine overall risk level
            val bestKelly = listOfNotNull(homeWinKelly, drawKelly, awayWinKelly).maxOrNull()
            val bestValueEdge = maxOf(homeWinValueEdge, drawValueEdge, awayWinValueEdge)
            val riskLevel = calculateRiskLevel(
                bestKelly = bestKelly,
                confidence = enhancedPrediction.confidence,
                valueEdge = bestValueEdge
            )
            
            // Calculate recommended stake percentage with fractional Kelly
            val recommendedStakePercentage = calculateRecommendedStake(
                bestKelly = bestKelly,
                riskLevel = riskLevel,
                confidence = enhancedPrediction.confidence,
                valueEdge = bestValueEdge
            )
            
            // Generate analysis text
            val analysis = generateEnhancedAnalysis(
                enhancedPrediction = enhancedPrediction,
                odds = odds,
                closingLineOdds = closingLineOdds,
                bestValueBet = bestValueBet,
                bestKelly = bestKelly,
                riskLevel = riskLevel,
                recommendedStakePercentage = recommendedStakePercentage
            )
            
            // Create KellyResult
            val kellyResult = KellyResult(
                fixtureId = enhancedPrediction.fixtureId,
                homeTeam = enhancedPrediction.homeTeam,
                awayTeam = enhancedPrediction.awayTeam,
                homeWinKelly = homeWinKelly,
                drawKelly = drawKelly,
                awayWinKelly = awayWinKelly,
                homeWinValueScore = homeWinValueScore,
                drawValueScore = drawValueScore,
                awayWinValueScore = awayWinValueScore,
                bestValueBet = bestValueBet,
                riskLevel = riskLevel,
                recommendedStakePercentage = recommendedStakePercentage,
                analysis = analysis,
                confidence = (enhancedPrediction.confidence * 100).toInt()
            )
            
            Result.success(kellyResult)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Calculate fractional Kelly for a specific market.
     */
    private fun calculateFractionalKellyForMarket(
        probability: Double,
        odds: Double?,
        confidence: Double
    ): Double? {
        if (probability <= 0.0 || probability >= 1.0 || odds == null || odds <= 1.0) {
            return null
        }
        
        // Calculate full Kelly
        val fullKelly = calculateKellyFraction(probability, odds)
        
        // Apply fractional Kelly (25% of full Kelly)
        val fractionalKelly = fullKelly * FRACTIONAL_KELLY_FACTOR
        
        // Adjust based on confidence
        val confidenceAdjustedKelly = fractionalKelly * confidence
        
        // Apply bounds
        return confidenceAdjustedKelly.coerceIn(0.0, MAX_STAKE_PERCENTAGE)
    }
    
    /**
     * Calculate value edge vs bookmaker.
     */
    private fun calculateValueEdge(
        ourProbability: Double,
        bookmakerProbability: Double?,
        closingLineProbability: Double?
    ): Double {
        // Use closing line if available, otherwise use opening line
        val marketProbability = closingLineProbability ?: bookmakerProbability ?: return 0.0
        
        // Calculate edge: our probability - market probability
        val edge = ourProbability - marketProbability
        
        // Only return positive edges
        return edge.coerceAtLeast(0.0)
    }
    
    /**
     * Calculate value score based on Kelly and value edge.
     */
    private fun calculateValueScore(kelly: Double?, valueEdge: Double): Int {
        if (kelly == null || kelly <= 0.0 || valueEdge <= 0.0) {
            return 0
        }
        
        // Base score from Kelly (0-5)
        val kellyScore = when {
            kelly >= 0.02 -> 5
            kelly >= 0.01 -> 4
            kelly >= 0.005 -> 3
            kelly >= 0.002 -> 2
            else -> 1
        }
        
        // Edge score (0-5)
        val edgeScore = when {
            valueEdge >= HIGH_VALUE_EDGE -> 5
            valueEdge >= MEDIUM_VALUE_EDGE -> 4
            valueEdge >= MIN_VALUE_EDGE -> 3
            valueEdge >= 0.01 -> 2
            else -> 1
        }
        
        // Combined score (0-10)
        return (kellyScore + edgeScore).coerceIn(0, 10)
    }
    
    /**
     * Determine the best value bet.
     */
    private fun determineBestValueBet(
        homeTeam: String,
        awayTeam: String,
        homeWinKelly: Double?,
        drawKelly: Double?,
        awayWinKelly: Double?,
        homeWinValueEdge: Double,
        drawValueEdge: Double,
        awayWinValueEdge: Double,
        homeWinValueScore: Int,
        drawValueScore: Int,
        awayWinValueScore: Int
    ): ValueBet {
        // Find the market with highest value score
        val markets = listOf(
            Quadruple(BettingMarket.HOME_WIN, homeWinValueScore, homeWinKelly, homeWinValueEdge),
            Quadruple(BettingMarket.DRAW, drawValueScore, drawKelly, drawValueEdge),
            Quadruple(BettingMarket.AWAY_WIN, awayWinValueScore, awayWinKelly, awayWinValueEdge)
        )
        
        val bestMarket = markets.maxByOrNull { it.second } ?: return ValueBet(
            market = BettingMarket.HOME_WIN,
            description = "Geen waarde weddenschap gevonden",
            valueScore = 0
        )
        
        val (market, valueScore, kelly, valueEdge) = bestMarket
        
        // Generate description
        val description = generateValueBetDescription(
            market = market,
            homeTeam = homeTeam,
            awayTeam = awayTeam,
            kelly = kelly,
            valueScore = valueScore,
            valueEdge = valueEdge
        )
        
        return ValueBet(
            market = market,
            description = description,
            valueScore = valueScore
        )
    }
    
    /**
     * Generate value bet description.
     */
    private fun generateValueBetDescription(
        market: BettingMarket,
        homeTeam: String,
        awayTeam: String,
        kelly: Double?,
        valueScore: Int,
        valueEdge: Double
    ): String {
        val edgePercent = (valueEdge * 100).toInt()
        val kellyPercent = kelly?.let { (it * 100).toInt() } ?: 0
        
        return when (market) {
            BettingMarket.HOME_WIN -> when {
                valueScore >= 8 -> "⭐ $homeTeam wint - Uitstekende waarde (Edge: ${edgePercent}%, Kelly: ${kellyPercent}%)"
                valueScore >= 6 -> "✅ $homeTeam wint - Goede waarde (Edge: ${edgePercent}%, Kelly: ${kellyPercent}%)"
                valueScore >= 4 -> "⚠️ $homeTeam wint - Matige waarde (Edge: ${edgePercent}%, Kelly: ${kellyPercent}%)"
                else -> "❌ $homeTeam wint - Lage waarde (Edge: ${edgePercent}%, Kelly: ${kellyPercent}%)"
            }
            BettingMarket.DRAW -> when {
                valueScore >= 8 -> "⭐ Gelijkspel - Uitstekende waarde (Edge: ${edgePercent}%, Kelly: ${kellyPercent}%)"
                valueScore >= 6 -> "✅ Gelijkspel - Goede waarde (Edge: ${edgePercent}%, Kelly: ${kellyPercent}%)"
                valueScore >= 4 -> "⚠️ Gelijkspel - Matige waarde (Edge: ${edgePercent}%, Kelly: ${kellyPercent}%)"
                else -> "❌ Gelijkspel - Lage waarde (Edge: ${edgePercent}%, Kelly: ${kellyPercent}%)"
            }
            BettingMarket.AWAY_WIN -> when {
                valueScore >= 8 -> "⭐ $awayTeam wint - Uitstekende waarde (Edge: ${edgePercent}%, Kelly: ${kellyPercent}%)"
                valueScore >= 6 -> "✅ $awayTeam wint - Goede waarde (Edge: ${edgePercent}%, Kelly: ${kellyPercent}%)"
                valueScore >= 4 -> "⚠️ $awayTeam wint - Matige waarde (Edge: ${edgePercent}%, Kelly: ${kellyPercent}%)"
                else -> "❌ $awayTeam wint - Lage waarde (Edge: ${edgePercent}%, Kelly: ${kellyPercent}%)"
            }
        }
    }
    
    /**
     * Calculate risk level based on multiple factors.
     */
    private fun calculateRiskLevel(
        bestKelly: Double?,
        confidence: Double,
        valueEdge: Double
    ): RiskLevel {
        if (bestKelly == null || bestKelly <= 0.0) {
            return RiskLevel.LOW
        }
        
        // Calculate risk score (0-100)
        var riskScore = 0
        
        // Kelly contribution (0-40)
        riskScore += when {
            bestKelly >= 0.02 -> 40
            bestKelly >= 0.01 -> 30
            bestKelly >= 0.005 -> 20
            else -> 10
        }
        
        // Confidence contribution (0-30)
        riskScore += when {
            confidence < 0.3 -> 30
            confidence < 0.6 -> 20
            else -> 10
        }
        
        // Value edge contribution (0-30)
        riskScore += when {
            valueEdge >= HIGH_VALUE_EDGE -> 10
            valueEdge >= MEDIUM_VALUE_EDGE -> 20
            valueEdge >= MIN_VALUE_EDGE -> 30
            else -> 30
        }
        
        return when {
            riskScore >= 80 -> RiskLevel.VERY_HIGH
            riskScore >= 60 -> RiskLevel.HIGH
            riskScore >= 40 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }
    }
    
    /**
     * Calculate recommended stake with fractional Kelly.
     */
    private fun calculateRecommendedStake(
        bestKelly: Double?,
        riskLevel: RiskLevel,
        confidence: Double,
        valueEdge: Double
    ): Double {
        if (bestKelly == null || bestKelly <= 0.0) {
            return 0.0
        }
        
        // Start with fractional Kelly
        var stake = bestKelly
        
        // Adjust based on risk level
        stake *= when (riskLevel) {
            RiskLevel.VERY_HIGH -> 0.5
            RiskLevel.HIGH -> 0.7
            RiskLevel.MEDIUM -> 0.9
            RiskLevel.LOW -> 1.0
        }
        
        // Adjust based on confidence
        stake *= confidence
        
        // Adjust based on value edge (higher edge = higher stake)
        stake *= (1.0 + (valueEdge * 2.0)).coerceAtMost(1.5)
        
        // Apply bounds
        return stake.coerceIn(MIN_STAKE_PERCENTAGE, MAX_STAKE_PERCENTAGE)
    }
    
    /**
     * Generate enhanced analysis text.
     */
    private fun generateEnhancedAnalysis(
        enhancedPrediction: EnhancedPrediction,
        odds: OddsData,
        closingLineOdds: OddsData?,
        bestValueBet: ValueBet,
        bestKelly: Double?,
        riskLevel: RiskLevel,
        recommendedStakePercentage: Double
    ): String {
        val homeTeam = enhancedPrediction.homeTeam
        val awayTeam = enhancedPrediction.awayTeam
        
        return buildString {
            appendLine("🎯 VERBETERDE KELLY ANALYSE (MatchMind AI 2.0)")
            appendLine()
            
            // Value bet summary
            appendLine("Beste waarde weddenschap:")
            appendLine("${bestValueBet.description}")
            appendLine()
            
            // Kelly explanation
            bestKelly?.let { kelly ->
                val fullKelly = kelly / FRACTIONAL_KELLY_FACTOR
                appendLine("Kelly Fractie: ${String.format("%.3f", kelly)} (25% van volledige Kelly: ${String.format("%.3f", fullKelly)})")
                appendLine(when {
                    kelly >= 0.02 -> "⭐ Zeer hoge waarde - Sterke edge vs bookmaker"
                    kelly >= 0.01 -> "✅ Goede waarde - Duidelijke edge vs bookmaker"
                    kelly >= 0.005 -> "⚠️ Matige waarde - Kleine edge vs bookmaker"
                    else -> "❌ Lage waarde - Minimale edge"
                })
                appendLine()
            }
            
            // Risk assessment
            appendLine("Risico Beoordeling:")
            appendLine(when (riskLevel) {
                RiskLevel.LOW -> "🟢 Laag risico - Veilig voor beginners (Fractional Kelly: 25%)"
                RiskLevel.MEDIUM -> "🟡 Gemiddeld risico - Goede balans (Fractional Kelly: 25%)"
                RiskLevel.HIGH -> "🟠 Hoog risico - Alleen voor ervaren spelers (Fractional Kelly: 25%)"
                RiskLevel.VERY_HIGH -> "🔴 Zeer hoog risico - Wees voorzichtig (Fractional Kelly: 25%)"
            })
            appendLine()
            
            // Stake recommendation
            val stakePercent = (recommendedStakePercentage * 100).toInt()
            appendLine("Aanbevolen Inzet: $stakePercent% van je bankroll")
            appendLine(when {
                stakePercent >= 3 -> "⚠️ Hoge inzet - Alleen met bewezen edge"
                stakePercent >= 1 -> "✅ Gemiddelde inzet - Goede balans"
                else -> "🟢 Lage inzet - Veilig voor beginners"
            })
            appendLine()
            
            // Comparison with bookmaker
            appendLine("Vergelijking met Bookmaker:")
            val ourHomeProb = (enhancedPrediction.homeWinProbability * 100).toInt()
            val bookmakerHomeProb = odds.homeWinProbability?.let { (it * 100).toInt() }
            val ourAwayProb = (enhancedPrediction.awayWinProbability * 100).toInt()
            val bookmakerAwayProb = odds.awayWinProbability?.let { (it * 100).toInt() }
            
            if (bookmakerHomeProb != null && bookmakerAwayProb != null) {
                appendLine("• $homeTeam: Wij ${ourHomeProb}% vs Bookmaker ${bookmakerHomeProb}%")
                appendLine("• $awayTeam: Wij ${ourAwayProb}% vs Bookmaker ${bookmakerAwayProb}%")
                
                // Show closing line comparison if available
                closingLineOdds?.let { closing ->
                    val closingHomeProb = closing.homeWinProbability?.let { (it * 100).toInt() }
                    val closingAwayProb = closing.awayWinProbability?.let { (it * 100).toInt() }
                    
                    if (closingHomeProb != null && closingAwayProb != null) {
                        appendLine()
                        appendLine("📊 Closing Line Vergelijking:")
                        appendLine("• $homeTeam: Closing ${closingHomeProb}% (Opening ${bookmakerHomeProb}%)")
                        appendLine("• $awayTeam: Closing ${closingAwayProb}% (Opening ${bookmakerAwayProb}%)")
                        
                        // Check if we beat the closing line
                        val beatsClosingHome = ourHomeProb > closingHomeProb
                        val beatsClosingAway = ourAwayProb > closingAwayProb
                        
                        if (beatsClosingHome || beatsClosingAway) {
                            appendLine("✅ Ons model verslaat de closing line!")
                        } else {
                            appendLine("⚠️  Ons model verslaat de closing line niet")
                        }
                    }
                }
            }
            
            appendLine()
            appendLine("📈 MODEL INSIGHTS:")
            appendLine("• xG-gebaseerde Dixon-Coles model")
            appendLine("• AI feature engineering: ${if (enhancedPrediction.newsImpactModifiers != null) "ACTIEF" else "NIET ACTIEF"}")
            appendLine("• Confidence: ${(enhancedPrediction.confidence * 100).toInt()}%")
            
            if (enhancedPrediction.newsImpactModifiers != null) {
                val modifiers = enhancedPrediction.newsImpactModifiers!!
                appendLine("• AI Vertrouwen: ${(modifiers.confidence * 100).toInt()}%")
                appendLine("• Chaos Niveau: ${getChaosLevelDescription(modifiers.chaosFactor)}")
            }
            
            appendLine()
            appendLine("⚠️  BELANGRIJK:")
            appendLine("• Gebruik ALTIJD Fractional Kelly (max 25% van volledige Kelly)")
            appendLine("• Backtest je model voordat je live inzet")
            appendLine("• Vergelijk altijd met closing lines voor edge validatie")
            appendLine("• Pas je inzet aan op basis van bewezen performance")
        }.toString()
    }
    
    /**
     * Get chaos level description.
     */
    private fun getChaosLevelDescription(chaosFactor: Double): String {
        return when {
            chaosFactor >= 0.8 -> "Zeer hoog (onvoorspelbaar)"
            chaosFactor >= 0.6 -> "Hoog (veel variabelen)"
            chaosFactor >= 0.4 -> "Gemiddeld"
            chaosFactor >= 0.2 -> "Laag (voorspelbaar)"
            else -> "Zeer laag (stabiel)"
        }
    }
}

/**
 * Data class for market quadruple.
 */
private data class Quadruple<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

/**
 * Extension function to check if EnhancedPrediction has valid probabilities.
 */
private fun EnhancedPrediction.hasValidProbabilities(): Boolean {
    return homeWinProbability > 0.0 && drawProbability > 0.0 && awayWinProbability > 0.0 &&
           (homeWinProbability + drawProbability + awayWinProbability) > 0.95 // Allow small rounding errors
}

/**
 * Calculate Kelly fraction.
 */
private fun calculateKellyFraction(probability: Double, odds: Double): Double {
    val b = odds - 1.0
    val p = probability
    val q = 1.0 - p
    
    return (b * p - q) / b
}
