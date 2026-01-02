package com.Lyno.matchmindai.data.repository

import android.util.Log
import com.Lyno.matchmindai.data.local.dao.PredictionDao
import com.Lyno.matchmindai.data.local.entity.PredictionLogEntity
import com.Lyno.matchmindai.data.remote.dto.StandingTeamDto
import com.Lyno.matchmindai.data.remote.dto.StandingsResponse
import com.Lyno.matchmindai.data.remote.dto.LeagueNode
import com.Lyno.matchmindai.data.remote.football.FootballApiException
import com.Lyno.matchmindai.data.remote.football.FootballApiService
import com.Lyno.matchmindai.domain.model.OracleAnalysis
import com.Lyno.matchmindai.domain.model.SimulationContext
import com.Lyno.matchmindai.domain.model.TesseractResult
import com.Lyno.matchmindai.domain.model.LLMGradeEnhancement
import com.Lyno.matchmindai.domain.model.ContextFactorType
import com.Lyno.matchmindai.domain.usecase.LLMGradeAnalysisUseCase
import com.Lyno.matchmindai.domain.prediction.PowerRankCalculator
import com.Lyno.matchmindai.domain.repository.OracleRepository
import com.Lyno.matchmindai.domain.tesseract.TesseractEngine
import com.Lyno.matchmindai.domain.service.NewsImpactAnalyzer
import com.Lyno.matchmindai.domain.service.DeepChiService
import com.Lyno.matchmindai.data.local.ApiKeyStorage
import com.Lyno.matchmindai.data.cache.TrinityMetricsCache
import com.Lyno.matchmindai.data.cache.TrinityMetricsEntry
import com.Lyno.matchmindai.data.utils.StandingsFallbackHelper
import com.Lyno.matchmindai.data.monitoring.GlobalPerformanceMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OracleRepositoryImpl(
    private val footballApiService: FootballApiService,
    private val tesseractEngine: TesseractEngine,
    private val newsImpactAnalyzer: NewsImpactAnalyzer,
    private val deepChiService: DeepChiService,
    private val predictionDao: PredictionDao,
    private val llmGradeAnalysisUseCase: LLMGradeAnalysisUseCase? = null,
    private val apiKeyStorage: ApiKeyStorage? = null,
    private val trinityMetricsCache: TrinityMetricsCache? = null
) : OracleRepository {

    companion object {
        private const val TAG = "OracleRepository"
        private const val DEFAULT_RANK = 10
        private const val DEFAULT_POINTS = 30
        private const val DEFAULT_GOALS_DIFF = 0
        private const val DEFAULT_GAMES_PLAYED = 20
        
        // Trinity metrics logging constants
        private const val TRINITY_FATIGUE = "fatigue"
        private const val TRINITY_LINEUP = "lineup"
        private const val TRINITY_STYLE = "style"
    }

    // --- CENTRALIZED EXTRACTION LOGIC ---
    private fun extractStandings(response: StandingsResponse): List<StandingTeamDto> {
        val leagueData = response.response.firstOrNull()?.league
        if (leagueData == null) {
            Log.e(TAG, "❌ No league data found in standings response")
            return emptyList()
        }

        // Explicitly flatten the nested list structure to avoid type inference errors
        val flattenedList = ArrayList<StandingTeamDto>()
        // leagueData.standings is List<List<StandingTeamDto>>
        for (group in leagueData.standings) {
            for (team in group) {
                flattenedList.add(team)
            }
        }
        return flattenedList
    }
    // -------------------------------------

    override suspend fun getOracleAnalysis(
        leagueId: Int,
        season: Int,
        homeTeamId: Int,
        awayTeamId: Int,
        fixtureId: Int?
    ): OracleAnalysis = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔍 Starting Oracle Analysis: league=$leagueId, season=$season, home=$homeTeamId, away=$awayTeamId, fixture=$fixtureId")
            
            // Use enhanced standings fallback helper
            val standingsFallbackHelper = StandingsFallbackHelper()
            val standingsResult = standingsFallbackHelper.getStandingsWithFallback(
                getStandings = { leagueIdParam, seasonParam -> 
                    footballApiService.getStandings(leagueIdParam, seasonParam) 
                },
                getRecentFixtures = { teamId, seasonParam ->
                    // Get recent fixtures for the team (last 10 matches)
                    try {
                        footballApiService.getLastFixturesForTeam(teamId, count = 10, status = "FT")
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to get recent fixtures for team $teamId: ${e.message}")
                        emptyList()
                    }
                },
                leagueId = leagueId,
                season = season,
                homeTeamId = homeTeamId,
                awayTeamId = awayTeamId
            )
            
            val flattenedStandings = standingsFallbackHelper.extractStandings(standingsResult.standings)
            
            Log.d(TAG, "📊 Standings data: ${flattenedStandings.size} teams found, season=${standingsResult.seasonUsed}, source=${standingsResult.source}")

            // Extract Home Team
            val homeEntry = flattenedStandings.find { it.team.id == homeTeamId }
            val homeData = if (homeEntry != null) {
                TeamData(homeEntry.rank, homeEntry.points, homeEntry.goalsDiff, homeEntry.all?.played ?: 0)
            } else {
                Log.w(TAG, "⚠️ Home team $homeTeamId not found in standings, using default data")
                TeamData.default()
            }

            // Extract Away Team
            val awayEntry = flattenedStandings.find { it.team.id == awayTeamId }
            val awayData = if (awayEntry != null) {
                TeamData(awayEntry.rank, awayEntry.points, awayEntry.goalsDiff, awayEntry.all?.played ?: 0)
            } else {
                Log.w(TAG, "⚠️ Away team $awayTeamId not found in standings, using default data")
                TeamData.default()
            }

            Log.d(TAG, "🏆 Team Data: Home=rank=${homeData.rank}, pts=${homeData.points}, gd=${homeData.goalsDiff}, gp=${homeData.gamesPlayed} | " +
                      "Away=rank=${awayData.rank}, pts=${awayData.points}, gd=${awayData.goalsDiff}, gp=${awayData.gamesPlayed}")

            val analysis = PowerRankCalculator.calculate(
                homeRank = homeData.rank,
                awayRank = awayData.rank,
                homePoints = homeData.points,
                awayPoints = awayData.points,
                homeGoalsDiff = homeData.goalsDiff,
                awayGoalsDiff = awayData.goalsDiff,
                gamesPlayed = homeData.gamesPlayed.coerceAtLeast(1),
                standingsSource = standingsResult.source,
                confidenceAdjustment = standingsResult.confidenceAdjustment
            )
            
            Log.d(TAG, "📈 Power Rank Analysis: homePower=${analysis.homePowerScore}, awayPower=${analysis.awayPowerScore}, " +
                      "prediction=${analysis.prediction}, confidence=${analysis.confidence}, " +
                      "source=${analysis.standingsSource}, confidenceAdjustment=${analysis.confidenceAdjustment}")

            // Get AI Simulation Context using DeepChi Service (Trinity Metrics)
            val homeTeamName = homeEntry?.team?.name ?: "Home Team"
            val awayTeamName = awayEntry?.team?.name ?: "Away Team"
            
            Log.d(TAG, "🧠 Starting Trinity Metrics analysis via DeepChi: $homeTeamName vs $awayTeamName")
            
            // Try to get real Trinity metrics from DeepChiService
            val context = try {
                getDeepChiSimulationContext(
                    fixtureId = fixtureId,
                    homeTeamId = homeTeamId,
                    awayTeamId = awayTeamId,
                    season = season,
                    homeTeamName = homeTeamName,
                    awayTeamName = awayTeamName
                )
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ DeepChi analysis failed, using neutral context: ${e.message}")
                com.Lyno.matchmindai.domain.model.SimulationContext.NEUTRAL.copy(
                    reasoning = "DeepChi analysis failed: ${e.message}"
                )
            }
            
            // Log Trinity metrics
            Log.d(TAG, "🎯 Trinity Metrics: fatigue=${context.fatigueScore}, lineup=${context.lineupStrength}, " +
                      "style=${context.styleMatchup}, homeFitness=${context.homeFitness}, awayFitness=${context.awayFitness}")

            val tesseractResult = tesseractEngine.simulateMatch(
                homePower = (analysis.homePowerScore / 2).toInt(),
                awayPower = (analysis.awayPowerScore / 2).toInt(),
                context = context
            )
            
            Log.d(TAG, "🎲 Tesseract Simulation: homeWin=${tesseractResult.homeWinProbability}, " +
                      "draw=${tesseractResult.drawProbability}, awayWin=${tesseractResult.awayWinProbability}, " +
                      "score=${tesseractResult.mostLikelyScore}")

            // Get LLMGRADE enhancement if available
            // Use real fixture ID if available, otherwise generate fake one
            val effectiveFixtureId = fixtureId ?: generateFixtureId(homeTeamId, awayTeamId)
            
            Log.d(TAG, "🤖 Starting LLMGRADE analysis for fixture $effectiveFixtureId")
            
            val llmGradeEnhancement = getLLMGradeEnhancement(
                fixtureId = effectiveFixtureId,
                oracleAnalysis = analysis,
                tesseractResult = tesseractResult,
                homeTeamName = homeTeamName,
                awayTeamName = awayTeamName
            )

            // Fire & Forget Save
            savePredictionInternal(
                hId = homeTeamId, 
                aId = awayTeamId, 
                hName = homeTeamName, 
                aName = awayTeamName, 
                an = analysis, 
                tes = tesseractResult, 
                ctx = context,
                llmGrade = llmGradeEnhancement
            )
            
            val finalAnalysis = analysis.copy(
                tesseract = tesseractResult,
                simulationContext = context,
                llmGradeEnhancement = llmGradeEnhancement
            )
            
            Log.d(TAG, "✅ Oracle Analysis Complete: prediction=${finalAnalysis.prediction}, " +
                      "confidence=${finalAnalysis.confidence}, " +
                      "trinityMetrics=[fatigue=${context.fatigueScore}, lineup=${context.lineupStrength}, style=${context.styleMatchup}], " +
                      "llmGrade=${llmGradeEnhancement != null}")
            
            finalAnalysis

        } catch (e: Exception) {
            Log.e(TAG, "❌ Critical Error in Oracle Analysis", e)
            // Return default analysis with error flag
            val defaultAnalysis = PowerRankCalculator.calculate(10, 10, 30, 30, 0, 0, 20) // Default neutral
            defaultAnalysis.copy(
                prediction = "Error",
                confidence = 0,
                simulationContext = SimulationContext.NEUTRAL.copy(
                    reasoning = "Oracle analysis failed: ${e.message}"
                )
            )
        }
    }
    
    /**
     * Get enhanced Oracle analysis with LLMGRADE context factors.
     */
    override suspend fun getEnhancedOracleAnalysis(
        leagueId: Int,
        season: Int,
        homeTeamId: Int,
        awayTeamId: Int,
        fixtureId: Int
    ): OracleAnalysis = withContext(Dispatchers.IO) {
        try {
            // Get base analysis
            val baseAnalysis = getOracleAnalysis(leagueId, season, homeTeamId, awayTeamId, fixtureId)
            
            // Get LLMGRADE enhancement with force refresh
            val llmGradeEnhancement = getLLMGradeEnhancement(
                fixtureId = fixtureId,
                oracleAnalysis = baseAnalysis,
                tesseractResult = baseAnalysis.tesseract,
                homeTeamName = "Home Team",
                awayTeamName = "Away Team",
                forceRefresh = true
            )
            
            // Create fallback TesseractResult with explicit Double types
            val fallbackTesseractResult = TesseractResult(
                homeWinProbability = 0.33,
                drawProbability = 0.34,
                awayWinProbability = 0.33,
                mostLikelyScore = "1-1"
            )
            
            // Create fallback SimulationContext with explicit Int types
            val fallbackSimulationContext = SimulationContext(
                fatigueScore = 0,
                styleMatchup = 1.0,
                lineupStrength = 100,
                reasoning = "Fallback context for enhanced analysis",
                homeDistraction = 50,
                awayDistraction = 50,
                homeFitness = 100,
                awayFitness = 100
            )
            
            // Save with LLMGRADE enhancement
            savePredictionInternal(
                hId = homeTeamId,
                aId = awayTeamId,
                hName = "Home Team",
                aName = "Away Team",
                an = baseAnalysis,
                tes = baseAnalysis.tesseract ?: fallbackTesseractResult,
                ctx = baseAnalysis.simulationContext ?: fallbackSimulationContext,
                llmGrade = llmGradeEnhancement
            )
            
            baseAnalysis.copy(
                llmGradeEnhancement = llmGradeEnhancement
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in Enhanced Oracle Analysis", e)
            getOracleAnalysis(leagueId, season, homeTeamId, awayTeamId, fixtureId)
        }
    }
    
    /**
     * Get LLMGRADE enhancement for a match.
     */
    private suspend fun getLLMGradeEnhancement(
        fixtureId: Int,
        oracleAnalysis: OracleAnalysis,
        tesseractResult: TesseractResult?,
        homeTeamName: String,
        awayTeamName: String,
        forceRefresh: Boolean = false
    ): LLMGradeEnhancement? {
        return try {
            if (llmGradeAnalysisUseCase == null) {
                Log.d(TAG, "LLMGRADE analysis not available - use case not injected")
                return null
            }
            
            Log.d(TAG, "🎯 Starting LLMGRADE analysis for $homeTeamName vs $awayTeamName")
            
            val result = llmGradeAnalysisUseCase.invoke(
                fixtureId = fixtureId,
                oracleAnalysis = oracleAnalysis,
                tesseractResult = tesseractResult,
                forceRefresh = forceRefresh
            )
            
            when {
                result?.isSuccess == true -> {
                    val enhancement = result.getOrThrow()
                    Log.d(TAG, "✅ LLMGRADE analysis completed: ${enhancement.contextFactors.size} context factors, ${enhancement.outlierScenarios.size} outlier scenarios")
                    enhancement
                }
                else -> {
                    Log.w(TAG, "⚠️ LLMGRADE analysis failed: ${result?.exceptionOrNull()?.message}")
                    null
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error in LLMGRADE analysis", e)
            null
        }
    }
    
    /**
     * Generate a fixture ID from team IDs.
     */
    private fun generateFixtureId(homeTeamId: Int, awayTeamId: Int): Int {
        return (homeTeamId * 1000) + awayTeamId
    }

    private suspend fun fetchStandingsWithFallback(leagueId: Int, season: Int): StandingsResult {
        // Try current season
        try {
            val current = footballApiService.getStandings(leagueId, season)
            if (extractStandings(current).isNotEmpty()) return StandingsResult(current, season)
        } catch (e: Exception) { Log.w(TAG, "Failed current season $season") }

        // Fallback previous season
        try {
            val prev = footballApiService.getStandings(leagueId, season - 1)
            if (extractStandings(prev).isNotEmpty()) return StandingsResult(prev, season - 1)
        } catch (e: Exception) { Log.w(TAG, "Failed prev season ${season-1}") }

        return StandingsResult(StandingsResponse(), season)
    }

    // Helper classes
    private data class StandingsResult(val standings: StandingsResponse, val season: Int)
    private data class TeamData(val rank: Int, val points: Int, val goalsDiff: Int, val gamesPlayed: Int) {
        companion object { fun default() = TeamData(DEFAULT_RANK, DEFAULT_POINTS, DEFAULT_GOALS_DIFF, DEFAULT_GAMES_PLAYED) }
    }

    private fun savePredictionInternal(
        hId: Int, 
        aId: Int, 
        hName: String, 
        aName: String, 
        an: OracleAnalysis, 
        tes: TesseractResult, 
        ctx: SimulationContext,
        llmGrade: LLMGradeEnhancement? = null
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val fixtureId = PredictionLogEntity.createFixtureId(hId, aId)
                val prediction = PredictionLogEntity(
                    fixtureId = fixtureId,
                    homeTeamId = hId,
                    awayTeamId = aId,
                    matchName = "$hName vs $aName",
                    predictedScore = an.prediction,
                    homeProb = tes.homeWinProbability,
                    drawProb = tes.drawProbability,
                    awayProb = tes.awayWinProbability,
                    homeFitness = ctx.homeFitness,
                    homeDistraction = ctx.homeDistraction,
                    llmGradeContextScore = llmGrade?.overallContextScore?.toFloat(),
                    llmGradeRiskLevel = llmGrade?.overallRiskLevel()?.name
                )
                predictionDao.insertPrediction(prediction)
                Log.d(TAG, "✅ Prediction saved for $hName vs $aName")
                
                // Log LLMGRADE insights if available
                if (llmGrade != null) {
                    Log.d(TAG, "🎯 LLMGRADE Insights: ${llmGrade.contextFactors.size} context factors, ${llmGrade.outlierScenarios.size} outlier scenarios")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to save prediction", e)
            }
        }
    }
    
    // Implement savePrediction interface method
    override suspend fun savePrediction(fixtureId: Int, homeTeamId: Int, awayTeamId: Int, homeTeamName: String, awayTeamName: String, analysis: OracleAnalysis): Boolean {
        return try {
            Log.d(TAG, "Saving prediction to database for fixture $fixtureId: $homeTeamName vs $awayTeamName")
            
            // Validate fixtureId before proceeding
            if (fixtureId <= 0) {
                Log.e(TAG, "Cannot save prediction: invalid fixtureId $fixtureId. " +
                          "This indicates an API call failure or data corruption. " +
                          "HomeTeamId: $homeTeamId, AwayTeamId: $awayTeamId")
                return false
            }
            
            // Extract data from analysis
            val predictedScore = analysis.prediction
            val homeProb = analysis.tesseract?.homeWinProbability ?: 0.33
            val drawProb = analysis.tesseract?.drawProbability ?: 0.34
            val awayProb = analysis.tesseract?.awayWinProbability ?: 0.33
            // confidence is not used in this function but kept for consistency
            
            // Get simulation context data
            val simulationContext = analysis.simulationContext ?: SimulationContext(
                fatigueScore = 0,
                styleMatchup = 1.0,
                lineupStrength = 100,
                reasoning = "Default context for saved prediction",
                homeDistraction = 50,
                awayDistraction = 50,
                homeFitness = 100,
                awayFitness = 100
            )
            
            // Get LLMGRADE data if available
            val llmGrade = analysis.llmGradeEnhancement
            val llmGradeContextScore = llmGrade?.overallContextScore?.toFloat()
            val llmGradeRiskLevel = llmGrade?.overallRiskLevel()?.name
            
            // Validate predicted score format
            if (!predictedScore.matches(Regex("^\\d+-\\d+$"))) {
                Log.e(TAG, "Invalid predicted score format: $predictedScore. Expected format: 'X-Y' (e.g., '2-1')")
                // Try to fix the format or use default
                val defaultScore = "1-1"
                Log.w(TAG, "Using default score: $defaultScore")
                // Continue with default score
            }
            
            // Create prediction entity
            val prediction = PredictionLogEntity(
                fixtureId = fixtureId,
                homeTeamId = homeTeamId,
                awayTeamId = awayTeamId,
                matchName = "$homeTeamName vs $awayTeamName",
                predictedScore = predictedScore,
                homeProb = homeProb,
                drawProb = drawProb,
                awayProb = awayProb,
                homeFitness = simulationContext.homeFitness,
                homeDistraction = simulationContext.homeDistraction,
                llmGradeContextScore = llmGradeContextScore,
                llmGradeRiskLevel = llmGradeRiskLevel
            )
            
            // Save to database
            predictionDao.insertPrediction(prediction)
            Log.d(TAG, "✅ Prediction saved successfully to database with fixtureId: $fixtureId")
            true
            
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "❌ Validation failed for prediction: ${e.message}", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to save prediction to database", e)
            false
        }
    }

    /**
     * Get SimulationContext from DeepChi Service with Trinity metrics.
     * Uses caching to reduce API calls and improve performance.
     */
    private suspend fun getDeepChiSimulationContext(
        fixtureId: Int?,
        homeTeamId: Int,
        awayTeamId: Int,
        season: Int,
        homeTeamName: String,
        awayTeamName: String
    ): SimulationContext {
        // Check if we have API keys
        val apiSportsApiKey = apiKeyStorage?.getApiSportsApiKey() ?: ""
        val deepSeekApiKey = apiKeyStorage?.getDeepSeekApiKey() ?: ""
        
        // First, try to get from cache if available
        if (trinityMetricsCache != null) {
            // Try by fixture ID first
            fixtureId?.takeIf { it > 0 }?.let { validFixtureId ->
                val cachedEntry = trinityMetricsCache.getByFixtureId(validFixtureId)
                if (cachedEntry != null) {
                    Log.d(TAG, "✅ Trinity metrics cache HIT for fixture $validFixtureId")
                    return SimulationContext(
                        fatigueScore = cachedEntry.fatigueScore,
                        lineupStrength = cachedEntry.lineupStrength,
                        styleMatchup = cachedEntry.styleMatchup / 100.0,
                        homeFitness = cachedEntry.homeFitness,
                        awayFitness = cachedEntry.awayFitness,
                        homeDistraction = cachedEntry.homeDistraction,
                        awayDistraction = cachedEntry.awayDistraction,
                        reasoning = cachedEntry.reasoning ?: "From cache"
                    )
                }
            }
            
            // Try by teams and season as fallback
            val teamSeasonEntry = trinityMetricsCache.getByTeamsAndSeason(homeTeamId, awayTeamId, season)
            if (teamSeasonEntry != null) {
                Log.d(TAG, "✅ Trinity metrics cache HIT for teams $homeTeamId vs $awayTeamId, season $season")
                return SimulationContext(
                    fatigueScore = teamSeasonEntry.fatigueScore,
                    lineupStrength = teamSeasonEntry.lineupStrength,
                    styleMatchup = teamSeasonEntry.styleMatchup / 100.0,
                    homeFitness = teamSeasonEntry.homeFitness,
                    awayFitness = teamSeasonEntry.awayFitness,
                    homeDistraction = teamSeasonEntry.homeDistraction,
                    awayDistraction = teamSeasonEntry.awayDistraction,
                    reasoning = teamSeasonEntry.reasoning ?: "From cache (team-season)"
                )
            }
            
            Log.d(TAG, "❌ Trinity metrics cache MISS, proceeding to DeepChi analysis")
        }
        
        if (apiSportsApiKey.isBlank() || deepSeekApiKey.isBlank()) {
            Log.w(TAG, "Missing API keys for DeepChi analysis. Using neutral context.")
            return SimulationContext.NEUTRAL.copy(
                reasoning = "Missing API keys for DeepChi analysis"
            )
        }
        
        // Check if we have a real fixtureId
        if (fixtureId == null || fixtureId <= 0) {
            Log.d(TAG, "No real fixtureId available for DeepChi analysis. Using fallback.")
            val fallbackContext = deepChiService.analyzeMatchFallback(
                fixtureId = generateFixtureId(homeTeamId, awayTeamId),
                homeTeamId = homeTeamId,
                awayTeamId = awayTeamId,
                season = season,
                apiKey = apiSportsApiKey
            )
            
            // Cache the fallback result if we have a cache
            trinityMetricsCache?.let { cache ->
                val fallbackEntry = TrinityMetricsEntry(
                    fixtureId = generateFixtureId(homeTeamId, awayTeamId),
                    homeTeamId = homeTeamId,
                    awayTeamId = awayTeamId,
                    season = season,
                    fatigueScore = fallbackContext.fatigueScore,
                    lineupStrength = fallbackContext.lineupStrength,
                    styleMatchup = (fallbackContext.styleMatchup * 100).toInt(),
                    homeFitness = fallbackContext.homeFitness,
                    awayFitness = fallbackContext.awayFitness,
                    homeDistraction = fallbackContext.homeDistraction,
                    awayDistraction = fallbackContext.awayDistraction,
                    reasoning = fallbackContext.reasoning
                )
                cache.cacheMetrics(fallbackEntry)
                Log.d(TAG, "Cached fallback Trinity metrics")
            }
            
            return fallbackContext
        }
        
        // Try to get real Trinity metrics from DeepChiService
        return try {
            val result = deepChiService.analyzeMatch(
                fixtureId = fixtureId,
                homeTeamId = homeTeamId,
                awayTeamId = awayTeamId,
                season = season,
                apiSportsApiKey = apiSportsApiKey,
                deepSeekApiKey = deepSeekApiKey
            )
            
            when {
                result.isSuccess -> {
                    val context = result.getOrThrow()
                    Log.d(TAG, "✅ DeepChi analysis successful: fatigue=${context.fatigueScore}, lineup=${context.lineupStrength}, style=${context.styleMatchup}")
                    
                    // Cache the successful result
                    trinityMetricsCache?.let { cache ->
                        val entry = TrinityMetricsEntry(
                            fixtureId = fixtureId,
                            homeTeamId = homeTeamId,
                            awayTeamId = awayTeamId,
                            season = season,
                            fatigueScore = context.fatigueScore,
                            lineupStrength = context.lineupStrength,
                            styleMatchup = (context.styleMatchup * 100).toInt(),
                            homeFitness = context.homeFitness,
                            awayFitness = context.awayFitness,
                            homeDistraction = context.homeDistraction,
                            awayDistraction = context.awayDistraction,
                            reasoning = context.reasoning
                        )
                        cache.cacheMetrics(entry)
                        Log.d(TAG, "Cached successful Trinity metrics for fixture $fixtureId")
                    }
                    
                    context
                }
                else -> {
                    Log.w(TAG, "⚠️ DeepChi analysis failed: ${result.exceptionOrNull()?.message}")
                    // Fallback to basic analysis
                    val fallbackContext = deepChiService.analyzeMatchFallback(
                        fixtureId = fixtureId,
                        homeTeamId = homeTeamId,
                        awayTeamId = awayTeamId,
                        season = season,
                        apiKey = apiSportsApiKey
                    )
                    
                    // Cache the fallback result
                    trinityMetricsCache?.let { cache ->
                        val entry = TrinityMetricsEntry(
                            fixtureId = fixtureId,
                            homeTeamId = homeTeamId,
                            awayTeamId = awayTeamId,
                            season = season,
                            fatigueScore = fallbackContext.fatigueScore,
                            lineupStrength = fallbackContext.lineupStrength,
                            styleMatchup = (fallbackContext.styleMatchup * 100).toInt(),
                            homeFitness = fallbackContext.homeFitness,
                            awayFitness = fallbackContext.awayFitness,
                            homeDistraction = fallbackContext.homeDistraction,
                            awayDistraction = fallbackContext.awayDistraction,
                            reasoning = fallbackContext.reasoning
                        )
                        cache.cacheMetrics(entry)
                        Log.d(TAG, "Cached fallback Trinity metrics for fixture $fixtureId")
                    }
                    
                    fallbackContext
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ DeepChi analysis exception: ${e.message}", e)
            SimulationContext.NEUTRAL.copy(
                reasoning = "DeepChi analysis exception: ${e.message}"
            )
        }
    }

    // Implement checkPendingPredictions interface method
    override suspend fun checkPendingPredictions(): Int {
        // For now, return 0 as a stub implementation
        // TODO: Implement actual reconciliation logic
        Log.d(TAG, "checkPendingPredictions called - stub implementation returning 0")
        return 0
    }

    /**
     * Get context-adjusted Oracle analysis with 3-0 bias correction.
     * Uses the new ContextAdjustedOracle model to apply contextual corrections.
     */
    override suspend fun getContextAdjustedOracleAnalysis(
        leagueId: Int,
        season: Int,
        homeTeamId: Int,
        awayTeamId: Int,
        fixtureId: Int?
    ): OracleAnalysis = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔧 Starting Context-Adjusted Oracle Analysis for 3-0 bias correction")
            
            // Get base analysis
            val baseAnalysis = getOracleAnalysis(leagueId, season, homeTeamId, awayTeamId, fixtureId)
            
            // Get LLMGRADE enhancement for context factors
            val homeTeamName = "Home Team" // Would be fetched from API in real implementation
            val awayTeamName = "Away Team"
            val effectiveFixtureId = fixtureId ?: generateFixtureId(homeTeamId, awayTeamId)
            
            val llmGradeEnhancement = getLLMGradeEnhancement(
                fixtureId = effectiveFixtureId,
                oracleAnalysis = baseAnalysis,
                tesseractResult = baseAnalysis.tesseract,
                homeTeamName = homeTeamName,
                awayTeamName = awayTeamName
            )
            
            // Extract context factors from LLMGRADE enhancement
            val contextFactors = llmGradeEnhancement?.contextFactors ?: emptyList()
            
            // Apply context-adjusted prediction
            val contextAdjustedOracle = com.Lyno.matchmindai.domain.prediction.ContextAdjustedOracle()
            val adjustedPrediction = contextAdjustedOracle.calculateAdjustedPrediction(
                baseOracle = baseAnalysis,
                contextFactors = contextFactors,
                tesseractResult = baseAnalysis.tesseract
            )
            
            // Create adjusted OracleAnalysis
            val adjustedAnalysis = baseAnalysis.copy(
                prediction = adjustedPrediction.score,
                confidence = adjustedPrediction.confidence,
                reasoning = adjustedPrediction.reasoning
            )
            
            Log.d(TAG, "✅ Context-Adjusted Analysis Complete: " +
                      "Original: ${baseAnalysis.prediction} (${baseAnalysis.confidence}%) -> " +
                      "Adjusted: ${adjustedAnalysis.prediction} (${adjustedAnalysis.confidence}%)")
            
            // Apply quick fix for 3-0 bias if needed
            val powerDiff = baseAnalysis.homePowerScore - baseAnalysis.awayPowerScore
            val totalInjuries = contextFactors.count { it.type == ContextFactorType.INJURIES }
            
            val finalScore = contextAdjustedOracle.adjustOracleScoreQuickFix(
                baseScore = adjustedAnalysis.prediction,
                powerDiff = powerDiff,
                totalInjuries = totalInjuries,
                homeForm = "average" // Would be fetched from form data in real implementation
            )
            
            val finalAnalysis = if (finalScore != adjustedAnalysis.prediction) {
                adjustedAnalysis.copy(
                    prediction = finalScore,
                    reasoning = "${adjustedAnalysis.reasoning} Quick fix applied: ${adjustedAnalysis.prediction} -> $finalScore due to context factors."
                )
            } else {
                adjustedAnalysis
            }
            
            Log.d(TAG, "🎯 Final Context-Adjusted Prediction: ${finalAnalysis.prediction} (${finalAnalysis.confidence}%)")
            
            finalAnalysis
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error in Context-Adjusted Oracle Analysis", e)
            // Fallback to regular analysis
            getOracleAnalysis(leagueId, season, homeTeamId, awayTeamId, fixtureId)
        }
    }
}
