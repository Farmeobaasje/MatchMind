package com.Lyno.matchmindai.data.utils

import android.util.Log
import com.Lyno.matchmindai.data.dto.football.FixtureItemDto
import com.Lyno.matchmindai.data.dto.TeamsDto
import com.Lyno.matchmindai.data.dto.football.StandingsResponse
import com.Lyno.matchmindai.data.remote.dto.StandingTeamDto
import com.Lyno.matchmindai.data.remote.dto.StandingsResponse as RemoteStandingsResponse
import com.Lyno.matchmindai.data.remote.dto.LeagueNode
import com.Lyno.matchmindai.data.remote.dto.LeagueDetails
import com.Lyno.matchmindai.data.remote.dto.TeamIdentityDto
import com.Lyno.matchmindai.data.remote.dto.StandingStatsDto
import com.Lyno.matchmindai.data.remote.dto.GoalsDto
import com.Lyno.matchmindai.domain.model.DataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Helper class for handling standings data fallback strategies.
 * Implements tiered fallback approach for missing standings data.
 */
class StandingsFallbackHelper {

    companion object {
        private const val TAG = "StandingsFallbackHelper"
        
        // Confidence adjustment multipliers based on data source
        private const val CONFIDENCE_API_OFFICIAL = 1.0f
        private const val CONFIDENCE_CALCULATED = 0.75f  // 25% reduction
        private const val CONFIDENCE_PREVIOUS_SEASON = 0.7f  // 30% reduction
        private const val CONFIDENCE_DEFAULT = 0.6f  // 40% reduction
        
        // Default values for missing data
        private const val DEFAULT_RANK = 10
        private const val DEFAULT_POINTS = 30
        private const val DEFAULT_GOALS_DIFF = 0
        private const val DEFAULT_GAMES_PLAYED = 20
    }

    /**
     * Get standings with comprehensive fallback strategy.
     * 
     * Priority order:
     * 1. Official API data for current season
     * 2. Calculated standings from recent fixtures
     * 3. Previous season data
     * 4. Default data with low confidence
     * 
     * @param getStandings Function to fetch standings from API
     * @param getRecentFixtures Function to fetch recent fixtures for calculation
     * @param leagueId League ID
     * @param season Season year
     * @param homeTeamId Home team ID (for fixture calculation)
     * @param awayTeamId Away team ID (for fixture calculation)
     * @return Pair of standings data and data source information
     */
    suspend fun getStandingsWithFallback(
        getStandings: suspend (Int, Int) -> RemoteStandingsResponse,
        getRecentFixtures: suspend (Int, Int) -> List<FixtureItemDto>,
        leagueId: Int,
        season: Int,
        homeTeamId: Int? = null,
        awayTeamId: Int? = null
    ): StandingsResult {
        Log.d(TAG, "🔍 Starting standings fallback for league=$leagueId, season=$season")
        
        // 1. Try official API data for current season
        try {
            val currentStandings = getStandings(leagueId, season)
            val flattenedStandings = extractStandings(currentStandings)
            
            if (flattenedStandings.isNotEmpty()) {
                Log.d(TAG, "✅ Using official API data for season $season (${flattenedStandings.size} teams)")
                return StandingsResult(
                    standings = currentStandings,
                    source = DataSource.API_OFFICIAL,
                    confidenceAdjustment = CONFIDENCE_API_OFFICIAL,
                    seasonUsed = season
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Failed to get official standings for season $season: ${e.message}")
        }

        // 2. Try to calculate standings from recent fixtures
        if (homeTeamId != null && awayTeamId != null) {
            try {
                val calculatedStandings = calculateStandingsFromFixtures(
                    getRecentFixtures = getRecentFixtures,
                    leagueId = leagueId,
                    season = season,
                    homeTeamId = homeTeamId,
                    awayTeamId = awayTeamId
                )
                
                if (calculatedStandings.response.isNotEmpty()) {
                    Log.d(TAG, "✅ Using calculated standings from fixtures for season $season")
                    return StandingsResult(
                        standings = calculatedStandings,
                        source = DataSource.CALCULATED,
                        confidenceAdjustment = CONFIDENCE_CALCULATED,
                        seasonUsed = season
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Failed to calculate standings from fixtures: ${e.message}")
            }
        }

        // 3. Try previous season data
        try {
            val previousSeason = season - 1
            val previousStandings = getStandings(leagueId, previousSeason)
            val flattenedStandings = extractStandings(previousStandings)
            
            if (flattenedStandings.isNotEmpty()) {
                Log.d(TAG, "⚠️ Using previous season ($previousSeason) data as fallback")
                return StandingsResult(
                    standings = previousStandings,
                    source = DataSource.PREVIOUS_SEASON,
                    confidenceAdjustment = CONFIDENCE_PREVIOUS_SEASON,
                    seasonUsed = previousSeason
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Failed to get previous season standings: ${e.message}")
        }

        // 4. Return default data (last resort)
        Log.w(TAG, "❌ No standings data available, using default data")
        return StandingsResult(
            standings = createDefaultStandings(leagueId, season),
            source = DataSource.DEFAULT,
            confidenceAdjustment = CONFIDENCE_DEFAULT,
            seasonUsed = season
        )
    }

    /**
     * Calculate standings from recent fixtures for both teams.
     * This is useful for tournaments where official standings may not be available.
     */
    private suspend fun calculateStandingsFromFixtures(
        getRecentFixtures: suspend (Int, Int) -> List<FixtureItemDto>,
        leagueId: Int,
        season: Int,
        homeTeamId: Int,
        awayTeamId: Int
    ): RemoteStandingsResponse {
        Log.d(TAG, "🧮 Calculating standings from fixtures for teams $homeTeamId and $awayTeamId")
        
        // Get recent fixtures for both teams
        val homeFixtures = getRecentFixtures(homeTeamId, season)
        val awayFixtures = getRecentFixtures(awayTeamId, season)
        
        // Calculate statistics for home team
        val homeStats = calculateTeamStats(homeTeamId, homeFixtures)
        val awayStats = calculateTeamStats(awayTeamId, awayFixtures)
        
        // Create synthetic standings
        return createSyntheticStandings(
            leagueId = leagueId,
            season = season,
            homeTeamId = homeTeamId,
            awayTeamId = awayTeamId,
            homeStats = homeStats,
            awayStats = awayStats
        )
    }

    /**
     * Calculate team statistics from recent fixtures.
     */
    private fun calculateTeamStats(
        teamId: Int,
        fixtures: List<FixtureItemDto>
    ): TeamStats {
        var played = 0
        var wins = 0
        var draws = 0
        var losses = 0
        var goalsFor = 0
        var goalsAgainst = 0
        
        fixtures.forEach { fixture ->
            val homeTeamId = fixture.teams?.home?.id
            val awayTeamId = fixture.teams?.away?.id
            val homeGoals = fixture.goals?.home ?: 0
            val awayGoals = fixture.goals?.away ?: 0
            
            if (homeTeamId == teamId || awayTeamId == teamId) {
                played++
                
                val isHome = homeTeamId == teamId
                val teamGoals = if (isHome) homeGoals else awayGoals
                val opponentGoals = if (isHome) awayGoals else homeGoals
                
                goalsFor += teamGoals
                goalsAgainst += opponentGoals
                
                when {
                    teamGoals > opponentGoals -> wins++
                    teamGoals < opponentGoals -> losses++
                    else -> draws++
                }
            }
        }
        
        val points = (wins * 3) + draws
        val goalDiff = goalsFor - goalsAgainst
        
        return TeamStats(
            teamId = teamId,
            played = played,
            wins = wins,
            draws = draws,
            losses = losses,
            points = points,
            goalsFor = goalsFor,
            goalsAgainst = goalsAgainst,
            goalDiff = goalDiff
        )
    }

    /**
     * Create synthetic standings for two teams.
     */
    private fun createSyntheticStandings(
        leagueId: Int,
        season: Int,
        homeTeamId: Int,
        awayTeamId: Int,
        homeStats: TeamStats,
        awayStats: TeamStats
    ): RemoteStandingsResponse {
        // Determine ranks based on points (higher points = better rank)
        val homeRank = if (homeStats.points >= awayStats.points) 1 else 2
        val awayRank = if (awayStats.points >= homeStats.points) 1 else 2
        
        // Create standing entries
        val homeStanding = StandingTeamDto(
            rank = homeRank,
            team = TeamIdentityDto(
                id = homeTeamId,
                name = "Home Team",
                logo = null
            ),
            points = homeStats.points,
            goalsDiff = homeStats.goalDiff,
            group = null,
            form = null,
            status = null,
            description = null,
            all = StandingStatsDto(
                played = homeStats.played,
                win = homeStats.wins,
                draw = homeStats.draws,
                lose = homeStats.losses,
                goals = GoalsDto(
                    forGoals = homeStats.goalsFor,
                    againstGoals = homeStats.goalsAgainst
                )
            ),
            home = null,
            away = null
        )
        
        val awayStanding = StandingTeamDto(
            rank = awayRank,
            team = TeamIdentityDto(
                id = awayTeamId,
                name = "Away Team",
                logo = null
            ),
            points = awayStats.points,
            goalsDiff = awayStats.goalDiff,
            group = null,
            form = null,
            status = null,
            description = null,
            all = StandingStatsDto(
                played = awayStats.played,
                win = awayStats.wins,
                draw = awayStats.draws,
                lose = awayStats.losses,
                goals = GoalsDto(
                    forGoals = awayStats.goalsFor,
                    againstGoals = awayStats.goalsAgainst
                )
            ),
            home = null,
            away = null
        )
        
        // Create league details
        val leagueDetails = LeagueDetails(
            id = leagueId,
            name = "League $leagueId",
            country = "Unknown",
            season = season,
            standings = listOf(listOf(homeStanding, awayStanding))
        )
        
        val leagueNode = LeagueNode(league = leagueDetails)
        
        return RemoteStandingsResponse(
            get = "standings",
            results = 2,
            response = listOf(leagueNode)
        )
    }

    /**
     * Create default standings when no data is available.
     */
    private fun createDefaultStandings(
        leagueId: Int,
        season: Int
    ): RemoteStandingsResponse {
        val homeStanding = StandingTeamDto(
            rank = DEFAULT_RANK,
            team = TeamIdentityDto(
                id = 0,
                name = "Home Team",
                logo = null
            ),
            points = DEFAULT_POINTS,
            goalsDiff = DEFAULT_GOALS_DIFF,
            all = StandingStatsDto(
                played = DEFAULT_GAMES_PLAYED,
                win = 6,
                draw = 6,
                lose = 8,
                goals = GoalsDto(forGoals = 20, againstGoals = 20)
            )
        )
        
        val awayStanding = StandingTeamDto(
            rank = DEFAULT_RANK,
            team = TeamIdentityDto(
                id = 1,
                name = "Away Team",
                logo = null
            ),
            points = DEFAULT_POINTS,
            goalsDiff = DEFAULT_GOALS_DIFF,
            all = StandingStatsDto(
                played = DEFAULT_GAMES_PLAYED,
                win = 6,
                draw = 6,
                lose = 8,
                goals = GoalsDto(forGoals = 20, againstGoals = 20)
            )
        )
        
        val leagueDetails = LeagueDetails(
            id = leagueId,
            name = "League $leagueId",
            country = "Unknown",
            season = season,
            standings = listOf(listOf(homeStanding, awayStanding))
        )
        
        val leagueNode = LeagueNode(league = leagueDetails)
        
        return RemoteStandingsResponse(
            get = "standings",
            results = 2,
            response = listOf(leagueNode)
        )
    }

    /**
     * Extract and flatten standings from API response.
     */
    fun extractStandings(response: RemoteStandingsResponse): List<StandingTeamDto> {
        val leagueData = response.response.firstOrNull()?.league
        if (leagueData == null) {
            Log.e(TAG, "❌ No league data found in standings response")
            return emptyList()
        }

        // Explicitly flatten the nested list structure
        val flattenedList = ArrayList<StandingTeamDto>()
        for (group in leagueData.standings) {
            for (team in group) {
                flattenedList.add(team)
            }
        }
        return flattenedList
    }

    /**
     * Get confidence adjustment based on data source.
     */
    fun getConfidenceAdjustment(source: DataSource): Float {
        return when (source) {
            DataSource.API_OFFICIAL -> CONFIDENCE_API_OFFICIAL
            DataSource.CALCULATED -> CONFIDENCE_CALCULATED
            DataSource.PREVIOUS_SEASON -> CONFIDENCE_PREVIOUS_SEASON
            DataSource.DEFAULT -> CONFIDENCE_DEFAULT
        }
    }

    /**
     * Result container for standings fallback.
     */
    data class StandingsResult(
        val standings: RemoteStandingsResponse,
        val source: DataSource,
        val confidenceAdjustment: Float,
        val seasonUsed: Int
    )

    /**
     * Team statistics container.
     */
    private data class TeamStats(
        val teamId: Int,
        val played: Int,
        val wins: Int,
        val draws: Int,
        val losses: Int,
        val points: Int,
        val goalsFor: Int,
        val goalsAgainst: Int,
        val goalDiff: Int
    )
}
