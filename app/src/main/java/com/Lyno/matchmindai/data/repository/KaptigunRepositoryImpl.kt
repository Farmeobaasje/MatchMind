package com.Lyno.matchmindai.data.repository

import com.Lyno.matchmindai.common.Resource
import com.Lyno.matchmindai.data.local.dao.FixtureDao
import com.Lyno.matchmindai.data.remote.football.RateLimitedFootballApiService
import com.Lyno.matchmindai.domain.model.*
import com.Lyno.matchmindai.domain.repository.KaptigunRepository
import com.Lyno.matchmindai.domain.service.NewsImpactAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

/**
 * Implementation of KaptigunRepository for Performance Analysis Dashboard.
 * Aggregates data from multiple sources to provide deep tactical analysis.
 */
class KaptigunRepositoryImpl(
    private val footballApiService: RateLimitedFootballApiService,
    private val fixtureDao: FixtureDao,
    private val newsImpactAnalyzer: NewsImpactAnalyzer,
    private val matchRepository: com.Lyno.matchmindai.domain.repository.MatchRepository,
    private val apiKeyStorage: com.Lyno.matchmindai.data.local.ApiKeyStorage
) : KaptigunRepository {

    // Cache for Kaptigun analysis (5 minutes TTL)
    private val analysisCache = mutableMapOf<Int, KaptigunAnalysis>()
    private val cacheTimestamps = mutableMapOf<Int, Long>()
    private val cacheTtl = 5 * 60 * 1000L // 5 minutes

    override suspend fun fetchAnalysis(
        fixtureId: Int,
        homeTeamId: Int,
        awayTeamId: Int
    ): Result<KaptigunAnalysis> = withContext(Dispatchers.IO) {
        try {
            Log.d("KaptigunRepository", "Fetching analysis for fixture $fixtureId (Home: $homeTeamId, Away: $awayTeamId)")

            // Check cache first
            val cachedAnalysis = getCachedAnalysis(fixtureId)
            if (cachedAnalysis != null) {
                Log.d("KaptigunRepository", "Using cached analysis for fixture $fixtureId")
                return@withContext Result.success(cachedAnalysis)
            }

            // Get match details for team names and league info
            val matchDetailFlow = matchRepository.getMatchDetails(fixtureId)
            var matchDetail: MatchDetail? = null
            
            // Collect the first successful result from the flow
            var foundMatchDetail = false
            matchDetailFlow.collect { resource ->
                if (!foundMatchDetail && resource is Resource.Success) {
                    matchDetail = resource.data
                    foundMatchDetail = true
                }
            }
            
            if (matchDetail == null) {
                Log.w("KaptigunRepository", "No match details found for fixture $fixtureId")
                return@withContext Result.failure(Exception("No match details found"))
            }

            // Use safe call operator with elvis operator for null safety
            val homeTeamName = matchDetail?.homeTeam ?: "Home Team"
            val awayTeamName = matchDetail?.awayTeam ?: "Away Team"
            val leagueId = matchDetail?.leagueId ?: 0
            val leagueName = matchDetail?.league ?: "Unknown League"

            // Fetch all data in parallel
            coroutineScope {
                val headToHeadDeferred = async { fetchHeadToHead(homeTeamId, awayTeamId) }
                val homeFormDeferred = async { fetchTeamRecentForm(homeTeamId) }
                val awayFormDeferred = async { fetchTeamRecentForm(awayTeamId) }
                val deepStatsDeferred = async { fetchDeepStatsComparison(homeTeamId, awayTeamId) }
                val homeSentimentDeferred = async { 
                    getSentimentScore(homeTeamId, homeTeamName, leagueName) 
                }
                val awaySentimentDeferred = async { 
                    getSentimentScore(awayTeamId, awayTeamName, leagueName) 
                }

                // Wait for all results
                val headToHeadResult = headToHeadDeferred.await()
                val homeFormResult = homeFormDeferred.await()
                val awayFormResult = awayFormDeferred.await()
                val deepStatsResult = deepStatsDeferred.await()
                val homeSentiment = homeSentimentDeferred.await()
                val awaySentiment = awaySentimentDeferred.await()

                // Combine results
                val headToHead = headToHeadResult.getOrElse { emptyList() }
                val homeRecentForm = homeFormResult.getOrElse { 
                    TeamRecentForm(
                        teamId = homeTeamId,
                        teamName = homeTeamName,
                        matches = emptyList(),
                        results = emptyList()
                    )
                }
                val awayRecentForm = awayFormResult.getOrElse { 
                    TeamRecentForm(
                        teamId = awayTeamId,
                        teamName = awayTeamName,
                        matches = emptyList(),
                        results = emptyList()
                    )
                }
                val deepStats = deepStatsResult.getOrElse { 
                    DeepStatsComparison(
                        avgXg = Pair(0.0, 0.0),
                        avgPossession = Pair(50.0, 50.0),
                        avgShotsOnTarget = Pair(0.0, 0.0),
                        ppda = Pair(15.0, 15.0),
                        sentimentScore = Pair(homeSentiment, awaySentiment)
                    )
                }

                // Update deep stats with sentiment scores
                val updatedDeepStats = deepStats.copy(
                    sentimentScore = Pair(homeSentiment, awaySentiment)
                )

                // Create KaptigunAnalysis
                val analysis = KaptigunAnalysis(
                    fixtureId = fixtureId,
                    homeTeamId = homeTeamId,
                    awayTeamId = awayTeamId,
                    homeTeamName = homeTeamName,
                    awayTeamName = awayTeamName,
                    leagueId = leagueId,
                    leagueName = leagueName,
                    headToHead = headToHead,
                    homeRecentForm = homeRecentForm,
                    awayRecentForm = awayRecentForm,
                    deepStats = updatedDeepStats,
                    lastUpdated = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                )

                // Cache the analysis
                cacheAnalysis(analysis)

                Log.d("KaptigunRepository", "Analysis generated successfully for fixture $fixtureId")
                Result.success(analysis)
            }
        } catch (e: Exception) {
            Log.e("KaptigunRepository", "Error fetching analysis", e)
            Result.failure(e)
        }
    }

    override fun getAnalysisFlow(fixtureId: Int): Flow<Resource<KaptigunAnalysis>> = flow {
        emit(Resource.Loading())

        try {
            // Get match details to extract team IDs
            var matchDetail: MatchDetail? = null
            var foundDetail = false
            matchRepository.getMatchDetails(fixtureId).collect { resource ->
                if (!foundDetail && resource is Resource.Success) {
                    matchDetail = resource.data
                    foundDetail = true
                }
            }

            // Create a local copy to avoid smart cast issues
            val localMatchDetail = matchDetail
            
            if (localMatchDetail == null) {
                emit(Resource.Error("Kon wedstrijddetails niet laden"))
                return@flow
            }

            val extractedHomeTeamId = localMatchDetail.homeTeamId ?: 0
            val extractedAwayTeamId = localMatchDetail.awayTeamId ?: 0

            if (extractedHomeTeamId == 0 || extractedAwayTeamId == 0) {
                emit(Resource.Error("Team ID's niet gevonden"))
                return@flow
            }

            val analysisResult = fetchAnalysis(fixtureId, extractedHomeTeamId, extractedAwayTeamId)
            
            if (analysisResult.isSuccess) {
                emit(Resource.Success(analysisResult.getOrThrow()))
            } else {
                emit(Resource.Error("Analyse mislukt: ${analysisResult.exceptionOrNull()?.message}"))
            }
        } catch (e: Exception) {
            Log.e("KaptigunRepository", "Error in analysis flow", e)
            emit(Resource.Error("Fout bij laden analyse: ${e.message}"))
        }
    }

    override suspend fun fetchHeadToHead(
        homeTeamId: Int,
        awayTeamId: Int
    ): Result<List<HeadToHeadDuel>> = withContext(Dispatchers.IO) {
        try {
            Log.d("KaptigunRepository", "Fetching H2H for teams $homeTeamId vs $awayTeamId")

            // Get last 10 fixtures for home team to find matches against away team
            val homeFixtures = footballApiService.getLastFixturesForTeam(
                teamId = homeTeamId,
                count = 10,
                status = "FT"
            )

            // Filter fixtures where the opponent is the away team
            val h2hFixtures = homeFixtures.filter { fixture ->
                val opponentId = if (fixture.teams.home.id == homeTeamId) {
                    fixture.teams.away.id
                } else {
                    fixture.teams.home.id
                }
                opponentId == awayTeamId
            }.take(5) // Take only last 5 matches

            val headToHeadDuels = mutableListOf<HeadToHeadDuel>()

            for (fixture in h2hFixtures) {
                try {
                    // Get fixture statistics for xG and other metrics
                    val statisticsResponse = footballApiService.getFixtureStatistics(fixture.fixture.id)
                    val teamStatistics = statisticsResponse.response

                    var homeXg: Double? = null
                    var awayXg: Double? = null
                    var homeShotsOnTarget: Int? = null
                    var awayShotsOnTarget: Int? = null
                    var homeTotalShots: Int? = null
                    var awayTotalShots: Int? = null
                    var homePossession: Double? = null
                    var awayPossession: Double? = null

                    // Extract statistics for both teams
                    val statsList = teamStatistics ?: emptyList()
                    for (teamStats in statsList) {
                        val teamId = teamStats.team?.id
                        if (teamId == homeTeamId) {
                            homeXg = teamStats.statistics?.find { it.type == "Expected Goals" }?.value?.toString()?.toDoubleOrNull()
                            homeShotsOnTarget = teamStats.statistics?.find { it.type == "Shots on Goal" }?.value?.toString()?.toIntOrNull()
                            homeTotalShots = teamStats.statistics?.find { it.type == "Total Shots" }?.value?.toString()?.toIntOrNull()
                            homePossession = teamStats.statistics?.find { it.type == "Ball Possession" }?.value?.toString()?.removeSuffix("%")?.toDoubleOrNull()
                        } else if (teamId == awayTeamId) {
                            awayXg = teamStats.statistics?.find { it.type == "Expected Goals" }?.value?.toString()?.toDoubleOrNull()
                            awayShotsOnTarget = teamStats.statistics?.find { it.type == "Shots on Goal" }?.value?.toString()?.toIntOrNull()
                            awayTotalShots = teamStats.statistics?.find { it.type == "Total Shots" }?.value?.toString()?.toIntOrNull()
                            awayPossession = teamStats.statistics?.find { it.type == "Ball Possession" }?.value?.toString()?.removeSuffix("%")?.toDoubleOrNull()
                        }
                    }

                    // Calculate xG if not available from API
                    val homeShotsOffTarget = (homeTotalShots ?: 0) - (homeShotsOnTarget ?: 0)
                    val awayShotsOffTarget = (awayTotalShots ?: 0) - (awayShotsOnTarget ?: 0)
                    
                    val finalHomeXg = calculateExpectedGoals(
                        shotsOnTarget = homeShotsOnTarget ?: 0,
                        shotsOffTarget = homeShotsOffTarget,
                        apiXg = homeXg
                    )
                    
                    val finalAwayXg = calculateExpectedGoals(
                        shotsOnTarget = awayShotsOnTarget ?: 0,
                        shotsOffTarget = awayShotsOffTarget,
                        apiXg = awayXg
                    )

                    // Format date
                    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val displayDate = dateFormatter.format(fixture.fixture.date)

                    val headToHeadDuel = HeadToHeadDuel(
                        fixtureId = fixture.fixture.id,
                        date = displayDate,
                        homeTeam = fixture.teams.home.name,
                        awayTeam = fixture.teams.away.name,
                        homeScore = fixture.goals?.home ?: 0,
                        awayScore = fixture.goals?.away ?: 0,
                        homeXg = finalHomeXg,
                        awayXg = finalAwayXg,
                        homeShotsOnTarget = homeShotsOnTarget,
                        awayShotsOnTarget = awayShotsOnTarget,
                        homeTotalShots = homeTotalShots,
                        awayTotalShots = awayTotalShots,
                        homePossession = homePossession,
                        awayPossession = awayPossession
                    )

                    headToHeadDuels.add(headToHeadDuel)
                } catch (e: Exception) {
                    Log.w("KaptigunRepository", "Error processing H2H fixture ${fixture.fixture.id}", e)
                    // Continue with next fixture
                }
            }

            // Sort by date (most recent first)
            val sortedDuels = headToHeadDuels.sortedByDescending { it.date }

            Log.d("KaptigunRepository", "Fetched ${sortedDuels.size} head-to-head duels")
            Result.success(sortedDuels)
        } catch (e: Exception) {
            Log.e("KaptigunRepository", "Error fetching head-to-head", e)
            Result.failure(e)
        }
    }

    override suspend fun fetchTeamRecentForm(
        teamId: Int,
        isHome: Boolean
    ): Result<TeamRecentForm> = withContext(Dispatchers.IO) {
        try {
            Log.d("KaptigunRepository", "Fetching recent form for team $teamId (home only: $isHome)")

            // Get last 5 matches for the team
            val recentFixtures = footballApiService.getLastFixturesForTeam(
                teamId = teamId,
                count = 5,
                status = "FT"
            )

            val formMatches = mutableListOf<FormMatch>()
            val results = mutableListOf<MatchResult>()

            for (fixture in recentFixtures) {
                try {
                    // Check if we should filter for home matches only
                    val isHomeMatch = fixture.teams.home.id == teamId
                    if (isHome && !isHomeMatch) {
                        continue
                    }

                    // Get statistics for xG calculation
                    val statisticsResponse = footballApiService.getFixtureStatistics(fixture.fixture.id)
                    val teamStatistics = statisticsResponse.response

                    // Extract xG for this team
                    var teamXg: Double? = null
                    var opponentXg: Double? = null
                    var shotsOnTarget: Int? = null
                    var totalShots: Int? = null

                    val statsList = teamStatistics ?: emptyList()
                    for (teamStats in statsList) {
                        val currentTeamId = teamStats.team?.id
                        if (currentTeamId == teamId) {
                            teamXg = teamStats.statistics?.find { it.type == "Expected Goals" }?.value?.toString()?.toDoubleOrNull()
                            shotsOnTarget = teamStats.statistics?.find { it.type == "Shots on Goal" }?.value?.toString()?.toIntOrNull()
                            totalShots = teamStats.statistics?.find { it.type == "Total Shots" }?.value?.toString()?.toIntOrNull()
                        } else if (currentTeamId != null) {
                            opponentXg = teamStats.statistics?.find { it.type == "Expected Goals" }?.value?.toString()?.toDoubleOrNull()
                        }
                    }

                    // Calculate xG if not available
                    val shotsOffTarget = (totalShots ?: 0) - (shotsOnTarget ?: 0)
                    val finalTeamXg = calculateExpectedGoals(
                        shotsOnTarget = shotsOnTarget ?: 0,
                        shotsOffTarget = shotsOffTarget,
                        apiXg = teamXg
                    )
                    
                    val finalOpponentXg = opponentXg ?: 0.0

                    val goals = if (isHomeMatch) fixture.goals?.home ?: 0 else fixture.goals?.away ?: 0
                    val opponentGoals = if (isHomeMatch) fixture.goals?.away ?: 0 else fixture.goals?.home ?: 0
                    val opponentName = if (isHomeMatch) fixture.teams.away.name else fixture.teams.home.name

                    val formMatch = FormMatch(
                        fixtureId = fixture.fixture.id,
                        opponent = opponentName,
                        isHome = isHomeMatch,
                        goals = goals,
                        opponentGoals = opponentGoals,
                        xg = finalTeamXg,
                        opponentXg = finalOpponentXg,
                        shotsOnTarget = shotsOnTarget,
                        totalShots = totalShots
                    )

                    formMatches.add(formMatch)
                    results.add(formMatch.result)
                } catch (e: Exception) {
                    Log.w("KaptigunRepository", "Error processing form fixture ${fixture.fixture.id}", e)
                    // Continue with next fixture
                }
            }

            // Get team name from first fixture or use placeholder
            val teamName = recentFixtures.firstOrNull()?.let { fixture ->
                if (fixture.teams.home.id == teamId) fixture.teams.home.name else fixture.teams.away.name
            } ?: "Team $teamId"

            val teamRecentForm = TeamRecentForm(
                teamId = teamId,
                teamName = teamName,
                matches = formMatches,
                results = results
            )

            Log.d("KaptigunRepository", "Fetched ${formMatches.size} form matches for team $teamId")
            Result.success(teamRecentForm)
        } catch (e: Exception) {
            Log.e("KaptigunRepository", "Error fetching team recent form", e)
            Result.failure(e)
        }
    }

    override suspend fun fetchDeepStatsComparison(
        homeTeamId: Int,
        awayTeamId: Int
    ): Result<DeepStatsComparison> = withContext(Dispatchers.IO) {
        try {
            Log.d("KaptigunRepository", "Fetching deep stats for teams $homeTeamId vs $awayTeamId")

            // Get last 5 matches for each team
            val homeFixtures = footballApiService.getLastFixturesForTeam(
                teamId = homeTeamId,
                count = 5,
                status = "FT"
            )
            
            val awayFixtures = footballApiService.getLastFixturesForTeam(
                teamId = awayTeamId,
                count = 5,
                status = "FT"
            )

            // Collect statistics for both teams
            val homeStats = mutableListOf<TeamMatchStats>()
            val awayStats = mutableListOf<TeamMatchStats>()

            // Process home team fixtures
            for (fixture in homeFixtures) {
                try {
                    // Extract statistics for home team
                    val statisticsResponse = footballApiService.getFixtureStatistics(fixture.fixture.id)
                    val teamStatistics = statisticsResponse.response

                    var xg: Double? = null
                    var possession: Double? = null
                    var shotsOnTarget: Int? = null
                    var totalShots: Int? = null
                    var passesCompleted: Int? = null
                    var defensiveActions: Int? = null

                    val statsList = teamStatistics ?: emptyList()
                    for (teamStats in statsList) {
                        val teamId = teamStats.team?.id
                        if (teamId == homeTeamId) {
                            xg = teamStats.statistics?.find { it.type == "Expected Goals" }?.value?.toString()?.toDoubleOrNull()
                            possession = teamStats.statistics?.find { it.type == "Ball Possession" }?.value?.toString()?.removeSuffix("%")?.toDoubleOrNull()
                            shotsOnTarget = teamStats.statistics?.find { it.type == "Shots on Goal" }?.value?.toString()?.toIntOrNull()
                            totalShots = teamStats.statistics?.find { it.type == "Total Shots" }?.value?.toString()?.toIntOrNull()
                            passesCompleted = teamStats.statistics?.find { it.type == "Passes" }?.value?.toString()?.toIntOrNull()
                            defensiveActions = teamStats.statistics?.find { it.type == "Tackles" }?.value?.toString()?.toIntOrNull()
                        }
                    }

                    val shotsOffTarget = (totalShots ?: 0) - (shotsOnTarget ?: 0)
                    val finalXg = calculateExpectedGoals(
                        shotsOnTarget = shotsOnTarget ?: 0,
                        shotsOffTarget = shotsOffTarget,
                        apiXg = xg
                    )

                    val stats = TeamMatchStats(
                        fixtureId = fixture.fixture.id,
                        teamId = homeTeamId,
                        xg = finalXg,
                        possession = possession ?: 50.0,
                        shotsOnTarget = shotsOnTarget ?: 0,
                        totalShots = totalShots ?: 0,
                        passes = passesCompleted ?: 0,
                        defensiveActions = defensiveActions ?: 0
                    )

                    homeStats.add(stats)
                } catch (e: Exception) {
                    Log.w("KaptigunRepository", "Error processing home team stats for fixture ${fixture.fixture.id}", e)
                }
            }

            // Process away team fixtures
            for (fixture in awayFixtures) {
                try {
                    // Extract statistics for away team
                    val statisticsResponse = footballApiService.getFixtureStatistics(fixture.fixture.id)
                    val teamStatistics = statisticsResponse.response

                    var xg: Double? = null
                    var possession: Double? = null
                    var shotsOnTarget: Int? = null
                    var totalShots: Int? = null
                    var passesCompleted: Int? = null
                    var defensiveActions: Int? = null

                    val statsList = teamStatistics ?: emptyList()
                    for (teamStats in statsList) {
                        val teamId = teamStats.team?.id
                        if (teamId == awayTeamId) {
                            xg = teamStats.statistics?.find { it.type == "Expected Goals" }?.value?.toString()?.toDoubleOrNull()
                            possession = teamStats.statistics?.find { it.type == "Ball Possession" }?.value?.toString()?.removeSuffix("%")?.toDoubleOrNull()
                            shotsOnTarget = teamStats.statistics?.find { it.type == "Shots on Goal" }?.value?.toString()?.toIntOrNull()
                            totalShots = teamStats.statistics?.find { it.type == "Total Shots" }?.value?.toString()?.toIntOrNull()
                            passesCompleted = teamStats.statistics?.find { it.type == "Passes" }?.value?.toString()?.toIntOrNull()
                            defensiveActions = teamStats.statistics?.find { it.type == "Tackles" }?.value?.toString()?.toIntOrNull()
                        }
                    }

                    val shotsOffTarget = (totalShots ?: 0) - (shotsOnTarget ?: 0)
                    val finalXg = calculateExpectedGoals(
                        shotsOnTarget = shotsOnTarget ?: 0,
                        shotsOffTarget = shotsOffTarget,
                        apiXg = xg
                    )

                    val stats = TeamMatchStats(
                        fixtureId = fixture.fixture.id,
                        teamId = awayTeamId,
                        xg = finalXg,
                        possession = possession ?: 50.0,
                        shotsOnTarget = shotsOnTarget ?: 0,
                        totalShots = totalShots ?: 0,
                        passes = passesCompleted ?: 0,
                        defensiveActions = defensiveActions ?: 0
                    )

                    awayStats.add(stats)
                } catch (e: Exception) {
                    Log.w("KaptigunRepository", "Error processing away team stats for fixture ${fixture.fixture.id}", e)
                }
            }

            // Calculate averages
            val homeAvgXg = if (homeStats.isNotEmpty()) homeStats.map { it.xg }.average() else 0.0
            val awayAvgXg = if (awayStats.isNotEmpty()) awayStats.map { it.xg }.average() else 0.0
            
            val homeAvgPossession = if (homeStats.isNotEmpty()) homeStats.map { it.possession }.average() else 50.0
            val awayAvgPossession = if (awayStats.isNotEmpty()) awayStats.map { it.possession }.average() else 50.0
            
            val homeAvgShotsOnTarget = if (homeStats.isNotEmpty()) homeStats.map { it.shotsOnTarget.toDouble() }.average() else 0.0
            val awayAvgShotsOnTarget = if (awayStats.isNotEmpty()) awayStats.map { it.shotsOnTarget.toDouble() }.average() else 0.0
            
            val homePpda = if (homeStats.isNotEmpty()) {
                val avgPasses = homeStats.map { it.passes.toDouble() }.average()
                val avgDefensiveActions = homeStats.map { it.defensiveActions.toDouble() }.average()
                calculatePPDA(avgPasses.toInt(), avgDefensiveActions.toInt())
            } else 15.0
            
            val awayPpda = if (awayStats.isNotEmpty()) {
                val avgPasses = awayStats.map { it.passes.toDouble() }.average()
                val avgDefensiveActions = awayStats.map { it.defensiveActions.toDouble() }.average()
                calculatePPDA(avgPasses.toInt(), avgDefensiveActions.toInt())
            } else 15.0

            val deepStatsComparison = DeepStatsComparison(
                avgXg = Pair(homeAvgXg, awayAvgXg),
                avgPossession = Pair(homeAvgPossession, awayAvgPossession),
                avgShotsOnTarget = Pair(homeAvgShotsOnTarget, awayAvgShotsOnTarget),
                ppda = Pair(homePpda, awayPpda),
                sentimentScore = Pair(0.0, 0.0) // Will be updated by caller
            )

            Log.d("KaptigunRepository", "Deep stats comparison calculated successfully")
            Result.success(deepStatsComparison)
        } catch (e: Exception) {
            Log.e("KaptigunRepository", "Error fetching deep stats comparison", e)
            Result.failure(e)
        }
    }

    override fun calculateExpectedGoals(
        shotsOnTarget: Int,
        shotsOffTarget: Int,
        apiXg: Double?
    ): Double {
        return apiXg ?: (shotsOnTarget * 0.3) + (shotsOffTarget * 0.07)
    }

    override fun determinePerformanceLabel(
        isWin: Boolean,
        teamXg: Double,
        opponentXg: Double
    ): PerformanceLabel {
        return when {
            isWin && teamXg > opponentXg + 0.3 -> PerformanceLabel.DOMINANT
            isWin && teamXg < opponentXg - 0.3 -> PerformanceLabel.LUCKY
            !isWin && teamXg > opponentXg + 0.3 -> PerformanceLabel.UNLUCKY
            else -> PerformanceLabel.NEUTRAL
        }
    }

    override fun determineEfficiencyIcon(
        goals: Int,
        xg: Double
    ): EfficiencyIcon {
        return when {
            goals > xg + 0.5 -> EfficiencyIcon.CLINICAL
            xg > goals + 0.5 -> EfficiencyIcon.INEFFICIENT
            else -> EfficiencyIcon.BALANCED
        }
    }

    override suspend fun getSentimentScore(
        teamId: Int,
        teamName: String,
        leagueName: String
    ): Double {
        return try {
            // Get API key for DeepSeek
            val apiKey = apiKeyStorage.getDeepSeekApiKey()
            if (apiKey.isNullOrEmpty()) {
                Log.w("KaptigunRepository", "No DeepSeek API key found, returning neutral sentiment")
                return 0.0
            }
            
            // Create a mock MatchDetail for the team
            val mockMatchDetail = com.Lyno.matchmindai.domain.model.MatchDetail(
                fixtureId = 0,
                homeTeam = teamName,
                awayTeam = "Opponent",
                homeTeamId = teamId,
                awayTeamId = 0,
                homeTeamLogo = null,
                awayTeamLogo = null,
                league = leagueName,
                leagueId = 0,
                leagueLogo = null,
                info = com.Lyno.matchmindai.domain.model.MatchInfo(),
                stats = emptyList(),
                lineups = com.Lyno.matchmindai.domain.model.MatchLineups(
                    home = com.Lyno.matchmindai.domain.model.TeamLineup(
                        teamName = teamName,
                        players = emptyList()
                    ),
                    away = com.Lyno.matchmindai.domain.model.TeamLineup(
                        teamName = "Opponent",
                        players = emptyList()
                    )
                ),
                events = emptyList(),
                score = null,
                status = com.Lyno.matchmindai.domain.model.MatchStatus.SCHEDULED,
                standings = null,
                injuries = emptyList(),
                prediction = null,
                odds = null
            )
            
            // Use generateMatchScenario to get simulation context
            val simulationContextResult = newsImpactAnalyzer.generateMatchScenario(
                fixtureId = 0,
                matchDetail = mockMatchDetail,
                apiKey = apiKey
            )
            
            if (simulationContextResult.isSuccess) {
                val context = simulationContextResult.getOrThrow()
                // Calculate sentiment from distraction and fitness
                // Since we passed teamName as homeTeam, use homeDistraction and homeFitness
                val distraction = context.homeDistraction
                val fitness = context.homeFitness
                
                // Convert to sentiment score (-1.0 to 1.0)
                // Higher fitness = positive sentiment, higher distraction = negative sentiment
                val fitnessScore = (fitness - 50.0) / 50.0 // -1.0 to 1.0
                val distractionScore = (50.0 - distraction) / 50.0 // -1.0 to 1.0
                
                (fitnessScore + distractionScore) / 2.0
            } else {
                0.0
            }
        } catch (e: Exception) {
            Log.w("KaptigunRepository", "Error getting sentiment score for team $teamId", e)
            0.0 // Neutral sentiment if analysis fails
        }
    }

    override fun calculatePPDA(
        passesCompleted: Int,
        defensiveActions: Int
    ): Double {
        return if (defensiveActions > 0) {
            passesCompleted.toDouble() / defensiveActions
        } else {
            15.0 // Default value
        }
    }

    override suspend fun cacheAnalysis(analysis: KaptigunAnalysis) {
        analysisCache[analysis.fixtureId] = analysis
        cacheTimestamps[analysis.fixtureId] = System.currentTimeMillis()
        Log.d("KaptigunRepository", "Analysis cached for fixture ${analysis.fixtureId}")
    }

    override suspend fun getCachedAnalysis(fixtureId: Int): KaptigunAnalysis? {
        val timestamp = cacheTimestamps[fixtureId] ?: return null
        if (System.currentTimeMillis() - timestamp < cacheTtl) {
            return analysisCache[fixtureId]
        }
        return null
    }

    override suspend fun clearAnalysisCache() {
        analysisCache.clear()
        cacheTimestamps.clear()
        Log.d("KaptigunRepository", "Analysis cache cleared")
    }
}
