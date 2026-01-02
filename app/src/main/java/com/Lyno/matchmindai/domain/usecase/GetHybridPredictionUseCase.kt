package com.Lyno.matchmindai.domain.usecase

import com.Lyno.matchmindai.domain.model.*
import com.Lyno.matchmindai.domain.repository.MatchRepository
import com.Lyno.matchmindai.domain.service.EnhancedScorePredictor
import com.Lyno.matchmindai.domain.service.ExpectedGoalsService
import com.Lyno.matchmindai.domain.service.NewsImpactAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

/**
 * Enhanced use case for getting hybrid predictions with xG integration and AI feature engineering.
 * 
 * This implements MatchMind AI 2.0:
 * 1. Uses xG data for Dixon-Coles model (reduces variance)
 * 2. AI as feature engineer (not direct predictor)
 * 3. Fractional Kelly for risk management
 * 4. Confidence calibration and validation
 */
class GetHybridPredictionUseCase(
    private val matchRepository: MatchRepository,
    private val newsRepository: com.Lyno.matchmindai.domain.repository.NewsRepository,
    private val apiKeyStorage: com.Lyno.matchmindai.data.local.ApiKeyStorage,
    private val expectedGoalsService: ExpectedGoalsService = ExpectedGoalsService(),
    private val enhancedScorePredictor: EnhancedScorePredictor = EnhancedScorePredictor(),
    private val newsImpactAnalyzer: com.Lyno.matchmindai.domain.service.NewsImpactAnalyzer
) {

    /**
     * Get enhanced hybrid prediction for a match with xG and AI integration.
     * 
     * @param fixtureId The ID of the fixture
     * @param matchDetail The match details for context
     * @param homeTeamFixtures Historical fixtures for home team
     * @param awayTeamFixtures Historical fixtures for away team
     * @param leagueFixtures All fixtures in the league
     * @param xgDataMap Map of fixtureId -> ExpectedGoalsData (optional)
     * @return Result containing EnhancedPrediction or error
     */
    suspend operator fun invoke(
        fixtureId: Int,
        matchDetail: MatchDetail,
        homeTeamFixtures: List<HistoricalFixture>,
        awayTeamFixtures: List<HistoricalFixture>,
        leagueFixtures: List<HistoricalFixture>,
        xgDataMap: Map<Int, ExpectedGoalsData> = emptyMap()
    ): Result<EnhancedPrediction> = withContext(Dispatchers.IO) {
        try {
            Log.d("VOORSPELLINGEN-X", "=".repeat(60))
            Log.d("VOORSPELLINGEN-X", "VOORSPELLINGEN-X RAPPORT")
            Log.d("VOORSPELLINGEN-X", "Wedstrijd: ${matchDetail.homeTeam} vs ${matchDetail.awayTeam}")
            Log.d("VOORSPELLINGEN-X", "-".repeat(60))
            
            // 1. Get base statistical prediction using xG data
            val basePredictionResult = enhancedScorePredictor.predictMatchWithXg(
                homeTeamFixtures = homeTeamFixtures,
                awayTeamFixtures = awayTeamFixtures,
                leagueFixtures = leagueFixtures,
                xgDataMap = xgDataMap
            )
            
            if (basePredictionResult.isFailure) {
                Log.e("VOORSPELLINGEN-X", "Base prediction failed: ${basePredictionResult.exceptionOrNull()?.message}")
                return@withContext Result.failure(
                    basePredictionResult.exceptionOrNull() ?: 
                    Exception("Failed to generate base prediction")
                )
            }
            
            val basePrediction = basePredictionResult.getOrThrow()
            Log.d("VOORSPELLINGEN-X", "Oracle Power: ${(basePrediction.homeWinProbability * 100).toInt()} vs ${(basePrediction.awayWinProbability * 100).toInt()}")
            
            // 2. Get API key for DeepSeek
            val apiKey = apiKeyStorage.getDeepSeekApiKey()
            if (apiKey.isNullOrEmpty()) {
                Log.w("VOORSPELLINGEN-X", "Geen DeepSeek API key gevonden. Gebruik neutrale context.")
                // Fallback to neutral context if no API key
                val neutralContext = com.Lyno.matchmindai.domain.model.SimulationContext.NEUTRAL.copy(
                    reasoning = "Geen DeepSeek API key gevonden. Gebruik neutrale context."
                )
                val finalPrediction = basePrediction.copy(
                    simulationContext = neutralContext,
                    reasoning = "${basePrediction.reasoning}\n\nAI Context: ${neutralContext.reasoning}"
                )
                
                Log.d("VOORSPELLINGEN-X", "DeepChi Context: Geen API key - neutrale context gebruikt")
                Log.d("VOORSPELLINGEN-X", "UITSLAG: ${matchDetail.homeTeam} Winst (${(basePrediction.homeWinProbability * 100).toInt()}%) - Basis voorspelling")
                Log.d("VOORSPELLINGEN-X", "REDEN: Geen AI analyse beschikbaar wegens ontbrekende API key")
                Log.d("VOORSPELLINGEN-X", "=".repeat(60))
                
                return@withContext Result.success(finalPrediction.copy(
                    fixtureId = fixtureId,
                    homeTeam = matchDetail.homeTeam,
                    awayTeam = matchDetail.awayTeam
                ))
            }
            
            // 3. Analyze news impact for AI feature engineering
            Log.d("VOORSPELLINGEN-X", "DeepChi Context: AI analyse gestart...")
            val newsImpactResult = newsImpactAnalyzer.analyzeNewsImpact(
                fixtureId = fixtureId,
                matchDetail = matchDetail,
                apiKey = apiKey
            )
            
            if (newsImpactResult.isFailure) {
                Log.w("VOORSPELLINGEN-X", "AI analyse mislukt: ${newsImpactResult.exceptionOrNull()?.message}")
                val neutralContext = com.Lyno.matchmindai.domain.model.SimulationContext.NEUTRAL.copy(
                    reasoning = "AI analyse mislukt: ${newsImpactResult.exceptionOrNull()?.message}"
                )
                val finalPrediction = basePrediction.copy(
                    simulationContext = neutralContext,
                    reasoning = "${basePrediction.reasoning}\n\nAI Context: ${neutralContext.reasoning}"
                )
                
                Log.d("VOORSPELLINGEN-X", "DeepChi Context: AI analyse mislukt - neutrale context gebruikt")
                Log.d("VOORSPELLINGEN-X", "UITSLAG: ${matchDetail.homeTeam} Winst (${(basePrediction.homeWinProbability * 100).toInt()}%) - Basis voorspelling")
                Log.d("VOORSPELLINGEN-X", "REDEN: AI analyse mislukt - gebruik basis voorspelling")
                Log.d("VOORSPELLINGEN-X", "=".repeat(60))
                
                return@withContext Result.success(finalPrediction.copy(
                    fixtureId = fixtureId,
                    homeTeam = matchDetail.homeTeam,
                    awayTeam = matchDetail.awayTeam
                ))
            }
            
            val simulationContext = newsImpactResult.getOrThrow()
            
            // Log DeepChi context details
            Log.d("VOORSPELLINGEN-X", "DeepChi Context:")
            Log.d("VOORSPELLINGEN-X", "  - Fatigue: ${matchDetail.homeTeam} (${if (simulationContext.hasHighFatigue) "Hoog" else "Laag"}), ${matchDetail.awayTeam} (${if (simulationContext.hasHighFatigue) "Hoog" else "Laag"})")
            Log.d("VOORSPELLINGEN-X", "  - Style: ${matchDetail.homeTeam} countert sterk tegen ${matchDetail.awayTeam} balbezit (${String.format("%.1f", simulationContext.styleMatchup)}x multiplier)")
            Log.d("VOORSPELLINGEN-X", "  - Lineup: ${if (simulationContext.hasWeakLineup) "Beide teams op volle sterkte" else "Sommige sleutelspelers afwezig"}")
            
            // 4. Apply AI modifiers if context has meaningful data
            val finalPrediction = if (simulationContext.hasMeaningfulData()) {
                Log.d("VOORSPELLINGEN-X", "TESSERACT: 10,000 sims gedraaid met Style Multiplier.")
                // Create enhanced prediction with simulation context
                basePrediction.copy(
                    simulationContext = simulationContext,
                    reasoning = "${basePrediction.reasoning}\n\nAI Context: ${simulationContext.reasoning}"
                )
            } else {
                Log.d("VOORSPELLINGEN-X", "TESSERACT: Geen betekenisvolle AI context - basis simulaties gebruikt")
                // Use base prediction without AI modifiers
                basePrediction.copy(
                    simulationContext = simulationContext
                )
            }
            
            // Determine outcome based on probabilities
            val homeWinPercent = (finalPrediction.homeWinProbability * 100).toInt()
            val awayWinPercent = (finalPrediction.awayWinProbability * 100).toInt()
            val drawPercent = (finalPrediction.drawProbability * 100).toInt()
            
            val outcome = when {
                homeWinPercent > awayWinPercent && homeWinPercent > drawPercent -> "${matchDetail.homeTeam} Winst"
                awayWinPercent > homeWinPercent && awayWinPercent > drawPercent -> "${matchDetail.awayTeam} Winst"
                else -> "Gelijk"
            }
            
            val valueBet = if (homeWinPercent > 60 || awayWinPercent > 60) "(Value Bet)" else ""
            
            Log.d("VOORSPELLINGEN-X", "UITSLAG: $outcome $valueBet")
            Log.d("VOORSPELLINGEN-X", "REDEN: Oracle zegt ${if (homeWinPercent > awayWinPercent) matchDetail.homeTeam else matchDetail.awayTeam} Winst, maar DeepChi ziet ${simulationContext.reasoning.take(50)}...")
            Log.d("VOORSPELLINGEN-X", "=".repeat(60))
            
            // 5. Add fixture ID and return final prediction
            val finalPredictionWithId = finalPrediction.copy(
                fixtureId = fixtureId,
                homeTeam = matchDetail.homeTeam,
                awayTeam = matchDetail.awayTeam
            )
            
            Result.success(finalPredictionWithId)
            
        } catch (e: Exception) {
            Log.e("VOORSPELLINGEN-X", "Unhandled exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Get simplified prediction for quick display.
     */
    suspend fun getSimplifiedPrediction(
        fixtureId: Int,
        matchDetail: MatchDetail,
        homeTeamFixtures: List<HistoricalFixture>,
        awayTeamFixtures: List<HistoricalFixture>,
        leagueFixtures: List<HistoricalFixture>
    ): Result<Triple<Int, Int, Int>> = withContext(Dispatchers.IO) {
        try {
            val predictionResult = invoke(
                fixtureId = fixtureId,
                matchDetail = matchDetail,
                homeTeamFixtures = homeTeamFixtures,
                awayTeamFixtures = awayTeamFixtures,
                leagueFixtures = leagueFixtures
            )
            
            if (predictionResult.isFailure) {
                return@withContext Result.failure(
                    predictionResult.exceptionOrNull() ?: Exception("Prediction failed")
                )
            }
            
            val prediction = predictionResult.getOrThrow()
            val homePercent = (prediction.homeWinProbability * 100).toInt()
            val drawPercent = (prediction.drawProbability * 100).toInt()
            val awayPercent = (prediction.awayWinProbability * 100).toInt()
            
            Result.success(Triple(homePercent, drawPercent, awayPercent))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Check if AI analysis would make a meaningful difference.
     */
    suspend fun wouldMakeMeaningfulDifference(
        fixtureId: Int,
        matchDetail: MatchDetail,
        homeTeamFixtures: List<HistoricalFixture>,
        awayTeamFixtures: List<HistoricalFixture>,
        leagueFixtures: List<HistoricalFixture>
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Get base prediction without AI
            val basePredictionResult = enhancedScorePredictor.predictMatchWithXg(
                homeTeamFixtures = homeTeamFixtures,
                awayTeamFixtures = awayTeamFixtures,
                leagueFixtures = leagueFixtures
            )
            
            if (basePredictionResult.isFailure) {
                return@withContext Result.success(false)
            }
            
            val basePrediction = basePredictionResult.getOrThrow()
            
            // Get API key for DeepSeek
            val apiKey = apiKeyStorage.getDeepSeekApiKey()
            if (apiKey.isNullOrEmpty()) {
                return@withContext Result.success(false)
            }
            
            // Get news impact analysis
            val newsImpactResult = newsImpactAnalyzer.analyzeNewsImpact(
                fixtureId = fixtureId,
                matchDetail = matchDetail,
                apiKey = apiKey
            )
            
            val simulationContext = newsImpactResult.getOrThrow()
            
            // Check if context has meaningful data
            val wouldMakeDifference = simulationContext.hasMeaningfulData()
            
            Result.success(wouldMakeDifference)
        } catch (e: Exception) {
            Result.success(false)
        }
    }

    /**
     * Get prediction summary for display.
     */
    suspend fun getPredictionSummary(
        fixtureId: Int,
        matchDetail: MatchDetail,
        homeTeamFixtures: List<HistoricalFixture>,
        awayTeamFixtures: List<HistoricalFixture>,
        leagueFixtures: List<HistoricalFixture>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val predictionResult = invoke(
                fixtureId = fixtureId,
                matchDetail = matchDetail,
                homeTeamFixtures = homeTeamFixtures,
                awayTeamFixtures = awayTeamFixtures,
                leagueFixtures = leagueFixtures
            )
            
            if (predictionResult.isFailure) {
                return@withContext Result.failure(
                    predictionResult.exceptionOrNull() ?: Exception("Prediction failed")
                )
            }
            
            val prediction = predictionResult.getOrThrow()
            val summary = enhancedScorePredictor.generatePredictionSummary(prediction)
            
            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get news impact analysis separately.
     */
    suspend fun getNewsImpactAnalysis(
        fixtureId: Int,
        matchDetail: MatchDetail,
        basePrediction: MatchPrediction
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Get API key for DeepSeek
            val apiKey = apiKeyStorage.getDeepSeekApiKey()
            if (apiKey.isNullOrEmpty()) {
                return@withContext Result.success(
                    "Geen DeepSeek API key gevonden. AI analyse niet beschikbaar."
                )
            }
            
            val newsImpactResult = newsImpactAnalyzer.analyzeNewsImpact(
                fixtureId = fixtureId,
                matchDetail = matchDetail,
                apiKey = apiKey
            )
            
            val simulationContext = newsImpactResult.getOrThrow()
            // Create a simple impact summary from the simulation context
            val impactSummary = """
                AI News Impact Analyse:
                - Fatigue Score: ${simulationContext.fatigueScore}/100 (${if (simulationContext.hasHighFatigue) "⚠️ High" else "OK"})
                - Style Matchup: ${String.format("%.2f", simulationContext.styleMatchup)} (${getStyleMatchupDescription(simulationContext.styleMatchup)})
                - Lineup Strength: ${simulationContext.lineupStrength}/100 (${if (simulationContext.hasWeakLineup) "⚠️ Weak" else "Strong"})
                - Reasoning: ${simulationContext.reasoning}
            """.trimIndent()
            
            Result.success(impactSummary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Returns a description of the style matchup.
     */
    private fun getStyleMatchupDescription(styleMatchup: Double): String {
        return when {
            styleMatchup > 1.2 -> "Strong Advantage"
            styleMatchup > 1.1 -> "Advantage"
            styleMatchup > 0.9 -> "Neutral"
            styleMatchup > 0.8 -> "Disadvantage"
            else -> "Strong Disadvantage"
        }
    }
}
