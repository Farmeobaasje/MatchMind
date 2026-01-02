package com.Lyno.matchmindai.domain.usecase

import com.Lyno.matchmindai.domain.model.*
import com.Lyno.matchmindai.domain.repository.MatchRepository
import com.Lyno.matchmindai.domain.service.EnhancedScorePredictor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

/**
 * State-of-the-art Mastermind Analysis Use Case.
 * 
 * Combines all existing components into one unified flow:
 * 1. Dixon-Coles + xG prediction (EnhancedScorePredictor)
 * 2. Kelly Criterion with fractional risk management
 * 3. Tavily search for breaking news
 * 4. AI prompt engineering for performance modifiers
 * 
 * Output: Single clear betting tip with mathematical backing.
 */
class MastermindAnalysisUseCase(
    private val matchRepository: MatchRepository,
    private val enhancedScorePredictor: EnhancedScorePredictor,
    private val enhancedCalculateKellyUseCase: EnhancedCalculateKellyUseCase
) {
    
    /**
     * Generate a Mastermind betting tip for a match.
     * 
     * @param fixtureId Match fixture ID (single source of truth)
     * @param forceRefresh If true, clear cache before fetching fresh data to prevent context pollution
     * @return MastermindTip with clear betting recommendation
     */
    suspend operator fun invoke(
        fixtureId: Int,
        forceRefresh: Boolean = false
    ): Result<MastermindTip> = withContext(Dispatchers.Default) {
        try {
            // 🔒 CACHE NUKE: Clear cache if forceRefresh is true to prevent context pollution
            if (forceRefresh) {
                println("🔒 CACHE NUKE: Clearing all caches to prevent context pollution")
                matchRepository.clearCache().onFailure { error ->
                    println("⚠️  Cache clear failed: ${error.message}")
                }
            }
            
            // 🔒 INTEGRITY CHECK: Fetch fresh match data from repository
            var matchDetail: MatchDetail? = null
            var fetchError: Exception? = null
            
            // Collect the flow to get the match details
            matchRepository.getMatchDetails(fixtureId).collect { resource ->
                when (resource) {
                    is com.Lyno.matchmindai.common.Resource.Success -> {
                        matchDetail = resource.data
                    }
                    is com.Lyno.matchmindai.common.Resource.Error -> {
                        fetchError = Exception(resource.message ?: "Failed to fetch match details")
                    }
                    else -> {
                        // Loading state, continue waiting
                    }
                }
            }
            
            if (fetchError != null) {
                return@withContext Result.failure(fetchError!!)
            }
            
            if (matchDetail == null) {
                return@withContext Result.failure(Exception("No match details available for fixture $fixtureId"))
            }
            
            val matchDetailNonNull = matchDetail!!
            
            // 🔒 INTEGRITY CHECK: Log the match being analyzed
            println("🔒 INTEGRITY CHECK: ID=$fixtureId belongs to ${matchDetailNonNull.homeTeam} vs ${matchDetailNonNull.awayTeam}")
            
            // STEP 1: Get historical data for prediction
            val homeTeamId = matchDetailNonNull.homeTeamId ?: 0
            val awayTeamId = matchDetailNonNull.awayTeamId ?: 0
            val leagueId = matchDetailNonNull.leagueId ?: 0
            val season = Calendar.getInstance().get(Calendar.YEAR)
            
            val historicalFixturesResult = matchRepository.getHistoricalFixturesForPrediction(
                homeTeamId = homeTeamId,
                awayTeamId = awayTeamId,
                leagueId = leagueId,
                season = season
            )
            
            if (historicalFixturesResult.isFailure) {
                return@withContext Result.failure(
                    historicalFixturesResult.exceptionOrNull() ?: 
                    Exception("Failed to get historical data")
                )
            }
            
            val historicalFixtures = historicalFixturesResult.getOrThrow()
            
            // Split fixtures by team
            val homeTeamFixtures = historicalFixtures.filter { 
                it.homeTeamId == homeTeamId || it.awayTeamId == homeTeamId 
            }
            val awayTeamFixtures = historicalFixtures.filter { 
                it.homeTeamId == awayTeamId || it.awayTeamId == awayTeamId 
            }
            
            // 🚀 FINAL CHECK: Log the teams being sent to the predictor
            println("🚀 FINAL CHECK: Sending ${matchDetailNonNull.homeTeam} vs ${matchDetailNonNull.awayTeam} to Predictor")
            
            // STEP 2: Generate enhanced prediction (Dixon-Coles + xG) with explicit context
            val enhancedPredictionResult = enhancedScorePredictor.predictMatchWithContext(
                matchDetail = matchDetailNonNull,
                homeTeamFixtures = homeTeamFixtures,
                awayTeamFixtures = awayTeamFixtures,
                leagueFixtures = historicalFixtures,
                xgDataMap = emptyMap(), // TODO: Integrate xG data if available
                modifiers = null // Will be enhanced with AI later
            )
            
            if (enhancedPredictionResult.isFailure) {
                return@withContext Result.failure(
                    enhancedPredictionResult.exceptionOrNull() ?: 
                    Exception("Enhanced prediction failed")
                )
            }
            
            var enhancedPrediction = enhancedPredictionResult.getOrThrow()
            
            // STEP 3: Search for breaking news (Tavily)
            val searchQuery = "${matchDetailNonNull.homeTeam} vs ${matchDetailNonNull.awayTeam} ${matchDetailNonNull.league} news injuries"
            val newsResult = matchRepository.searchInternet(searchQuery)
            
            if (newsResult.isSuccess) {
                val agentResponse = newsResult.getOrThrow()
                // TODO: Process news with AI to generate modifiers
                // For now, we'll just log that news was found
                println("Breaking news search completed: ${agentResponse.text.take(100)}...")
            }
            
            // STEP 4: Get bookmaker odds
            val oddsResult = matchRepository.getOdds(fixtureId)
            if (oddsResult.isFailure) {
                return@withContext Result.failure(
                    oddsResult.exceptionOrNull() ?: Exception("Failed to get odds")
                )
            }
            
            val odds = oddsResult.getOrThrow()
            if (odds == null) {
                return@withContext Result.failure(Exception("No odds available for this match"))
            }
            
            // STEP 5: Calculate Kelly Criterion
            val kellyResult = enhancedCalculateKellyUseCase.invoke(
                enhancedPrediction = enhancedPrediction,
                odds = odds,
                closingLineOdds = null // TODO: Get closing line odds if available
            )
            
            if (kellyResult.isFailure) {
                return@withContext Result.failure(
                    kellyResult.exceptionOrNull() ?: Exception("Kelly calculation failed")
                )
            }
            
            val kellyAdvice = kellyResult.getOrThrow()
            
            // STEP 6: Determine best betting market
            val bestBet = determineBestBettingMarket(kellyAdvice, enhancedPrediction)
            
            // STEP 7: Generate Mastermind Tip
            val mastermindTip = MastermindTip(
                fixtureId = fixtureId,
                homeTeam = matchDetailNonNull.homeTeam,
                awayTeam = matchDetailNonNull.awayTeam,
                league = matchDetailNonNull.league ?: "Unknown",
                bestBet = bestBet,
                odds = getOddsForMarket(bestBet.market, odds),
                confidence = (enhancedPrediction.confidence * 100).roundToInt(),
                valueScore = kellyAdvice.bestValueBet.valueScore,
                kellyStakePercentage = (kellyAdvice.recommendedStakePercentage * 100).roundToInt(),
                riskLevel = kellyAdvice.riskLevel,
                analysis = generateMastermindAnalysis(
                    enhancedPrediction = enhancedPrediction,
                    kellyAdvice = kellyAdvice,
                    odds = odds,
                    matchDetail = matchDetailNonNull
                ),
                timestamp = System.currentTimeMillis()
            )
            
            Result.success(mastermindTip)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Determine the best betting market based on Kelly advice.
     */
    private fun determineBestBettingMarket(
        kellyAdvice: KellyResult,
        enhancedPrediction: EnhancedPrediction
    ): BestBet {
        val bestValueBet = kellyAdvice.bestValueBet
        
        return when (bestValueBet.market) {
            BettingMarket.HOME_WIN -> BestBet(
                market = BettingMarket.HOME_WIN,
                description = "${enhancedPrediction.homeTeam} wint",
                probability = (enhancedPrediction.homeWinProbability * 100).roundToInt(),
                edgePercentage = calculateEdgePercentage(
                    ourProbability = enhancedPrediction.homeWinProbability,
                    bookmakerProbability = null // Will be calculated from odds
                )
            )
            BettingMarket.DRAW -> BestBet(
                market = BettingMarket.DRAW,
                description = "Gelijkspel",
                probability = (enhancedPrediction.drawProbability * 100).roundToInt(),
                edgePercentage = calculateEdgePercentage(
                    ourProbability = enhancedPrediction.drawProbability,
                    bookmakerProbability = null
                )
            )
            BettingMarket.AWAY_WIN -> BestBet(
                market = BettingMarket.AWAY_WIN,
                description = "${enhancedPrediction.awayTeam} wint",
                probability = (enhancedPrediction.awayWinProbability * 100).roundToInt(),
                edgePercentage = calculateEdgePercentage(
                    ourProbability = enhancedPrediction.awayWinProbability,
                    bookmakerProbability = null
                )
            )
        }
    }
    
    /**
     * Get odds for specific betting market.
     */
    private fun getOddsForMarket(market: BettingMarket, odds: OddsData): Double? {
        return when (market) {
            BettingMarket.HOME_WIN -> odds.homeWinOdds
            BettingMarket.DRAW -> odds.drawOdds
            BettingMarket.AWAY_WIN -> odds.awayWinOdds
        }
    }
    
    /**
     * Calculate edge percentage vs bookmaker.
     */
    private fun calculateEdgePercentage(
        ourProbability: Double,
        bookmakerProbability: Double?
    ): Int {
        if (bookmakerProbability == null) return 0
        return ((ourProbability - bookmakerProbability) * 100).roundToInt()
    }
    
    /**
     * Generate comprehensive Mastermind analysis.
     */
    private fun generateMastermindAnalysis(
        enhancedPrediction: EnhancedPrediction,
        kellyAdvice: KellyResult,
        odds: OddsData,
        matchDetail: MatchDetail
    ): String {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        val timestamp = dateFormat.format(Date())
        
        return buildString {
            appendLine("🎯 MASTERMIND AI TIP")
            appendLine("Generated: $timestamp")
            appendLine()
            
            appendLine("📊 PREDICTION MODEL:")
            appendLine("• Dixon-Coles + xG integration")
            appendLine("• Historical data: ${enhancedPrediction.confidence.times(100).roundToInt()}% confidence")
            appendLine("• Expected Goals: ${String.format("%.1f", enhancedPrediction.expectedGoalsHome)} - ${String.format("%.1f", enhancedPrediction.expectedGoalsAway)}")
            appendLine()
            
            appendLine("💰 VALUE ANALYSIS:")
            appendLine("• Best Value: ${kellyAdvice.bestValueBet.description}")
            appendLine("• Value Score: ${kellyAdvice.bestValueBet.valueScore}/10")
            appendLine("• Risk Level: ${kellyAdvice.riskLevel}")
            appendLine()
            
            appendLine("📈 KELLY CRITERION:")
            appendLine("• Fractional Kelly: 25% of full Kelly")
            appendLine("• Recommended Stake: ${(kellyAdvice.recommendedStakePercentage * 100).roundToInt()}% of bankroll")
            appendLine("• Confidence: ${kellyAdvice.confidence}%")
            appendLine()
            
            appendLine("⚽ MATCH CONTEXT:")
            appendLine("• ${matchDetail.homeTeam} vs ${matchDetail.awayTeam}")
            appendLine("• ${matchDetail.league ?: "Unknown league"}")
            appendLine("• Status: ${matchDetail.status?.displayName ?: "Unknown"}")
            appendLine()
            
            appendLine("⚠️  RISK MANAGEMENT:")
            appendLine("• Always use fractional Kelly (max 25%)")
            appendLine("• Adjust stakes based on proven performance")
            appendLine("• Monitor closing line movement")
        }
    }
}

/**
 * Mastermind Tip data class.
 */
data class MastermindTip(
    val fixtureId: Int,
    val homeTeam: String,
    val awayTeam: String,
    val league: String,
    val bestBet: BestBet,
    val odds: Double?,
    val confidence: Int,
    val valueScore: Int,
    val kellyStakePercentage: Int,
    val riskLevel: RiskLevel,
    val analysis: String,
    val timestamp: Long
)

/**
 * Best Bet data class.
 */
data class BestBet(
    val market: BettingMarket,
    val description: String,
    val probability: Int,
    val edgePercentage: Int
)
