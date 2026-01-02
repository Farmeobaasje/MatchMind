package com.Lyno.matchmindai.domain.usecase

import com.Lyno.matchmindai.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Use case for calculating Kelly Criterion for betting value analysis.
 * 
 * The Kelly Criterion helps determine optimal bet sizing based on:
 * - Our predicted probability (from Dixon-Coles/AI)
 * - Bookmaker odds
 * 
 * Formula: f = (bp - q) / b
 * Where:
 * - f = fraction of bankroll to bet
 * - b = decimal odds - 1
 * - p = probability of winning (our prediction)
 * - q = probability of losing (1 - p)
 * 
 * Positive Kelly = Value bet (bookmaker underestimates the team)
 * Negative Kelly = Bad value (avoid betting, even if team is likely to win)
 */
class CalculateKellyUseCase {
    
    /**
     * Calculate Kelly Criterion for a match.
     * 
     * @param prediction Our probability prediction for the match
     * @param odds Bookmaker odds for the match
     * @param aiAnalysis Optional AI analysis for confidence adjustment
     * @return Result containing KellyResult or error
     */
    suspend operator fun invoke(
        prediction: MatchPrediction,
        odds: OddsData,
        aiAnalysis: AiAnalysisResult? = null
    ): Result<KellyResult> = withContext(Dispatchers.Default) {
        try {
            // Validate input data
            if (!prediction.hasValidProbabilities() || !odds.hasOdds) {
                return@withContext Result.success(
                    KellyResult.empty(prediction.fixtureId, prediction.homeTeam, prediction.awayTeam)
                )
            }
            
            // Calculate Kelly for each market
            val homeWinKelly = calculateKellyForMarket(
                probability = prediction.homeWinProbability,
                odds = odds.homeWinOdds
            )
            
            val drawKelly = calculateKellyForMarket(
                probability = prediction.drawProbability,
                odds = odds.drawOdds
            )
            
            val awayWinKelly = calculateKellyForMarket(
                probability = prediction.awayWinProbability,
                odds = odds.awayWinOdds
            )
            
            // Calculate value scores
            val homeWinValueScore = kellyToValueScore(homeWinKelly)
            val drawValueScore = kellyToValueScore(drawKelly)
            val awayWinValueScore = kellyToValueScore(awayWinKelly)
            
            // Determine best value bet
            val bestValueBet = determineBestValueBet(
                homeTeam = prediction.homeTeam,
                awayTeam = prediction.awayTeam,
                homeWinKelly = homeWinKelly,
                drawKelly = drawKelly,
                awayWinKelly = awayWinKelly,
                homeWinValueScore = homeWinValueScore,
                drawValueScore = drawValueScore,
                awayWinValueScore = awayWinValueScore
            )
            
            // Determine overall risk level based on best Kelly
            val bestKelly = listOfNotNull(homeWinKelly, drawKelly, awayWinKelly).maxOrNull()
            val riskLevel = kellyToRiskLevel(bestKelly)
            
            // Calculate recommended stake percentage
            val recommendedStakePercentage = getRecommendedStake(bestKelly, riskLevel)
            
            // Calculate confidence based on prediction confidence and AI analysis
            val confidence = calculateConfidence(prediction, aiAnalysis)
            
            // Generate analysis text
            val analysis = generateAnalysis(
                prediction = prediction,
                odds = odds,
                bestValueBet = bestValueBet,
                bestKelly = bestKelly,
                riskLevel = riskLevel
            )
            
            // Create KellyResult
            val kellyResult = KellyResult(
                fixtureId = prediction.fixtureId,
                homeTeam = prediction.homeTeam,
                awayTeam = prediction.awayTeam,
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
                confidence = confidence
            )
            
            Result.success(kellyResult)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Calculate Kelly fraction for a specific market.
     */
    private fun calculateKellyForMarket(probability: Double, odds: Double?): Double? {
        if (probability <= 0.0 || probability >= 1.0 || odds == null || odds <= 1.0) {
            return null
        }
        
        return calculateKellyFraction(probability, odds)
    }
    
    /**
     * Determine the best value bet based on Kelly fractions and value scores.
     */
    private fun determineBestValueBet(
        homeTeam: String,
        awayTeam: String,
        homeWinKelly: Double?,
        drawKelly: Double?,
        awayWinKelly: Double?,
        homeWinValueScore: Int,
        drawValueScore: Int,
        awayWinValueScore: Int
    ): ValueBet {
        // Find the market with highest value score
        val markets = listOf(
            Triple(BettingMarket.HOME_WIN, homeWinValueScore, homeWinKelly),
            Triple(BettingMarket.DRAW, drawValueScore, drawKelly),
            Triple(BettingMarket.AWAY_WIN, awayWinValueScore, awayWinKelly)
        )
        
        val bestMarket = markets.maxByOrNull { it.second } ?: return ValueBet(
            market = BettingMarket.HOME_WIN,
            description = "Geen waarde weddenschap gevonden",
            valueScore = 0
        )
        
        val (market, valueScore, kelly) = bestMarket
        
        // Generate description based on market and Kelly value
        val description = when (market) {
            BettingMarket.HOME_WIN -> generateHomeWinDescription(homeTeam, kelly, valueScore)
            BettingMarket.DRAW -> generateDrawDescription(kelly, valueScore)
            BettingMarket.AWAY_WIN -> generateAwayWinDescription(awayTeam, kelly, valueScore)
        }
        
        return ValueBet(
            market = market,
            description = description,
            valueScore = valueScore
        )
    }
    
    /**
     * Generate description for home win value bet.
     */
    private fun generateHomeWinDescription(
        homeTeam: String,
        kelly: Double?,
        valueScore: Int
    ): String {
        return when {
            kelly == null || valueScore == 0 -> "Geen waarde in $homeTeam winst"
            valueScore >= 8 -> "⭐ $homeTeam wint - Uitstekende waarde (Kelly: ${String.format("%.2f", kelly)})"
            valueScore >= 6 -> "✅ $homeTeam wint - Goede waarde (Kelly: ${String.format("%.2f", kelly)})"
            valueScore >= 4 -> "⚠️ $homeTeam wint - Matige waarde (Kelly: ${String.format("%.2f", kelly)})"
            else -> "❌ $homeTeam wint - Lage waarde (Kelly: ${String.format("%.2f", kelly)})"
        }
    }
    
    /**
     * Generate description for draw value bet.
     */
    private fun generateDrawDescription(kelly: Double?, valueScore: Int): String {
        return when {
            kelly == null || valueScore == 0 -> "Geen waarde in gelijkspel"
            valueScore >= 8 -> "⭐ Gelijkspel - Uitstekende waarde (Kelly: ${String.format("%.2f", kelly)})"
            valueScore >= 6 -> "✅ Gelijkspel - Goede waarde (Kelly: ${String.format("%.2f", kelly)})"
            valueScore >= 4 -> "⚠️ Gelijkspel - Matige waarde (Kelly: ${String.format("%.2f", kelly)})"
            else -> "❌ Gelijkspel - Lage waarde (Kelly: ${String.format("%.2f", kelly)})"
        }
    }
    
    /**
     * Generate description for away win value bet.
     */
    private fun generateAwayWinDescription(
        awayTeam: String,
        kelly: Double?,
        valueScore: Int
    ): String {
        return when {
            kelly == null || valueScore == 0 -> "Geen waarde in $awayTeam winst"
            valueScore >= 8 -> "⭐ $awayTeam wint - Uitstekende waarde (Kelly: ${String.format("%.2f", kelly)})"
            valueScore >= 6 -> "✅ $awayTeam wint - Goede waarde (Kelly: ${String.format("%.2f", kelly)})"
            valueScore >= 4 -> "⚠️ $awayTeam wint - Matige waarde (Kelly: ${String.format("%.2f", kelly)})"
            else -> "❌ $awayTeam wint - Lage waarde (Kelly: ${String.format("%.2f", kelly)})"
        }
    }
    
    /**
     * Calculate confidence score based on prediction and AI analysis.
     */
    private fun calculateConfidence(
        prediction: MatchPrediction,
        aiAnalysis: AiAnalysisResult?
    ): Int {
        var confidence = prediction.confidenceScore
        
        // Adjust based on AI analysis if available
        aiAnalysis?.let { analysis ->
            // Use AI confidence if available
            if (analysis.confidence_score > 0) {
                confidence = (confidence + analysis.confidence_score) / 2
            }
            
            // Adjust based on chaos score (higher chaos = lower confidence)
            if (analysis.chaos_score > 70) {
                confidence = (confidence * 0.8).toInt()
            } else if (analysis.chaos_score > 50) {
                confidence = (confidence * 0.9).toInt()
            }
        }
        
        return confidence.coerceIn(0, 100)
    }
    
    /**
     * Generate analysis text for the Kelly result.
     */
    private fun generateAnalysis(
        prediction: MatchPrediction,
        odds: OddsData,
        bestValueBet: ValueBet,
        bestKelly: Double?,
        riskLevel: RiskLevel
    ): String {
        val homeTeam = prediction.homeTeam
        val awayTeam = prediction.awayTeam
        
        return buildString {
            appendLine("🎯 KELLY VALUE ANALYSE")
            appendLine()
            
            // Value bet summary
            appendLine("Beste waarde weddenschap:")
            appendLine("${bestValueBet.description}")
            appendLine()
            
            // Kelly fraction explanation
            bestKelly?.let { kelly ->
                appendLine("Kelly Fractie: ${String.format("%.3f", kelly)}")
                appendLine(when {
                    kelly >= 0.20 -> "⭐ Zeer hoge waarde - Bookmaker onderschat deze kans significant"
                    kelly >= 0.10 -> "✅ Goede waarde - Bookmaker onderschat deze kans"
                    kelly >= 0.05 -> "⚠️ Matige waarde - Kleine edge ten opzichte van bookmaker"
                    else -> "❌ Lage waarde - Minimale edge"
                })
                appendLine()
            }
            
            // Risk assessment
            appendLine("Risico Beoordeling:")
            appendLine(when (riskLevel) {
                RiskLevel.LOW -> "🟢 Laag risico - Veilig voor beginners"
                RiskLevel.MEDIUM -> "🟡 Gemiddeld risico - Goede balans"
                RiskLevel.HIGH -> "🟠 Hoog risico - Alleen voor ervaren spelers"
                RiskLevel.VERY_HIGH -> "🔴 Zeer hoog risico - Wees voorzichtig"
            })
            appendLine()
            
            // Comparison with bookmaker
            appendLine("Vergelijking met Bookmaker:")
            val ourHomeProb = (prediction.homeWinProbability * 100).toInt()
            val bookmakerHomeProb = odds.homeWinProbability?.let { (it * 100).toInt() }
            val ourAwayProb = (prediction.awayWinProbability * 100).toInt()
            val bookmakerAwayProb = odds.awayWinProbability?.let { (it * 100).toInt() }
            
            if (bookmakerHomeProb != null && bookmakerAwayProb != null) {
                appendLine("• $homeTeam: Wij ${ourHomeProb}% vs Bookmaker ${bookmakerHomeProb}%")
                appendLine("• $awayTeam: Wij ${ourAwayProb}% vs Bookmaker ${bookmakerAwayProb}%")
                
                when (bestValueBet.market) {
                    BettingMarket.HOME_WIN -> {
                        val difference = ourHomeProb - bookmakerHomeProb
                        if (difference > 0) {
                            appendLine("• Bookmaker onderschat $homeTeam met $difference%")
                        }
                    }
                    BettingMarket.AWAY_WIN -> {
                        val difference = ourAwayProb - bookmakerAwayProb
                        if (difference > 0) {
                            appendLine("• Bookmaker onderschat $awayTeam met $difference%")
                        }
                    }
                    else -> {}
                }
            }
            
            appendLine()
            appendLine("⚠️ Let op: Kelly Criterion is een wiskundig model. Gebruik altijd verstandig bankroll management.")
        }
    }
}

/**
 * Extension function to check if prediction has valid probabilities.
 */
private fun MatchPrediction.hasValidProbabilities(): Boolean {
    return homeWinProbability > 0.0 && drawProbability > 0.0 && awayWinProbability > 0.0 &&
           (homeWinProbability + drawProbability + awayWinProbability) > 0.95 // Allow small rounding errors
}
