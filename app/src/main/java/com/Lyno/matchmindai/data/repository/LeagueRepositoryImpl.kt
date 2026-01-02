package com.Lyno.matchmindai.data.repository

import com.Lyno.matchmindai.data.local.dao.LeagueDao
import com.Lyno.matchmindai.data.local.entity.LeagueEntity
import com.Lyno.matchmindai.data.remote.football.FootballApiService
import com.Lyno.matchmindai.domain.repository.LeagueRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import android.util.Log

/**
 * Implementation of the LeagueRepository interface.
 * Handles caching and retrieval of league data for Dynamic League Discovery.
 */
class LeagueRepositoryImpl(
    private val leagueDao: LeagueDao,
    private val footballApiService: FootballApiService
) : LeagueRepository {
    
    companion object {
        private const val TAG = "LeagueRepository"
        private const val REFRESH_INTERVAL_DAYS = 7L // Refresh leagues every 7 days
    }
    
    override fun getAllLeagues(): Flow<List<LeagueEntity>> {
        return leagueDao.getAllLeagues()
    }
    
    override fun getCurrentLeagues(): Flow<List<LeagueEntity>> {
        return leagueDao.getCurrentLeagues()
    }
    
    override suspend fun getLeagueById(leagueId: Int): LeagueEntity? {
        return leagueDao.getLeagueById(leagueId)
    }
    
    override suspend fun searchLeagues(query: String): List<LeagueEntity> {
        return leagueDao.searchLeagues(query)
    }
    
    override suspend fun getTopPriorityLeagueIds(minScore: Int, limit: Int): List<Int> {
        return leagueDao.getTopPriorityLeagueIds(minScore, limit)
    }
    
    override fun getLeaguesWithStandings(): Flow<List<LeagueEntity>> {
        return leagueDao.getLeaguesWithStandings()
    }
    
    override fun getLeaguesWithEvents(): Flow<List<LeagueEntity>> {
        return leagueDao.getLeaguesWithEvents()
    }
    
    override fun getLeaguesWithLineups(): Flow<List<LeagueEntity>> {
        return leagueDao.getLeaguesWithLineups()
    }
    
    override suspend fun leagueExists(leagueId: Int): Boolean {
        return leagueDao.leagueExists(leagueId) > 0
    }
    
    override suspend fun refreshLeaguesFromApi(): Result<Int> {
        return try {
            Log.d(TAG, "Refreshing leagues from API...")
            
            // Fetch current leagues from API-SPORTS
            val apiResponse = footballApiService.getLeagues(current = true)
            
            if (apiResponse.response.isEmpty()) {
                Log.w(TAG, "API returned empty league list")
                return Result.success(0)
            }
            
            // Convert API response to LeagueEntity objects
            val leagueEntities = apiResponse.response.mapNotNull { leagueEntry ->
                try {
                    val firstSeason = leagueEntry.seasons.firstOrNull()
                    val seasonYear = firstSeason?.year ?: 2025
                    
                    // Log if season has null start/end dates for debugging
                    if (firstSeason?.start == null || firstSeason.end == null) {
                        Log.w(TAG, "League ${leagueEntry.league.id} (${leagueEntry.league.name}) has null season dates: start=${firstSeason?.start}, end=${firstSeason?.end}")
                    }
                    
                    LeagueEntity.fromApiResponse(
                        leagueId = leagueEntry.league.id,
                        name = leagueEntry.league.name,
                        country = leagueEntry.country.name,
                        logoUrl = leagueEntry.league.logo,
                        type = leagueEntry.league.type,
                        season = seasonYear,
                        isCurrent = leagueEntry.seasons.any { it.current == true },
                        coverage = extractCoverageFromLeagueEntry(leagueEntry)
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error converting league ${leagueEntry.league.id}: ${e.message}", e)
                    null
                }
            }.filter { it.leagueId > 0 } // Filter out invalid leagues
            
            Log.d(TAG, "Converted ${leagueEntities.size} leagues from API response")
            
            // Clear old data and insert new leagues
            leagueDao.deleteAllLeagues()
            leagueDao.insertLeagues(leagueEntities)
            
            Log.d(TAG, "Successfully refreshed ${leagueEntities.size} leagues")
            Result.success(leagueEntities.size)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to refresh leagues from API: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getLeagueCount(): Int {
        return leagueDao.getLeagueCount()
    }
    
    override suspend fun shouldRefreshLeagues(): Boolean {
        val lastUpdate = leagueDao.getLastUpdateTime()
        if (lastUpdate == null) {
            // No data in database, needs refresh
            return true
        }
        
        val currentTime = System.currentTimeMillis()
        val daysSinceUpdate = TimeUnit.MILLISECONDS.toDays(currentTime - lastUpdate)
        
        return daysSinceUpdate >= REFRESH_INTERVAL_DAYS
    }
    
    override suspend fun getLeaguePriorityScore(leagueId: Int): Int {
        val league = leagueDao.getLeagueById(leagueId)
        return league?.priorityScore ?: 0
    }
    
    override suspend fun getLeagueCoverage(leagueId: Int): Map<String, Boolean> {
        val league = leagueDao.getLeagueById(leagueId)
        return if (league != null) {
            mapOf(
                "standings" to league.hasStandings,
                "players" to league.hasPlayers,
                "top_scorers" to league.hasTopScorers,
                "predictions" to league.hasPredictions,
                "odds" to league.hasOdds,
                "fixtures" to league.hasFixtures,
                "events" to league.hasEvents,
                "lineups" to league.hasLineups,
                "statistics" to league.hasStatistics
            )
        } else {
            emptyMap()
        }
    }
    
    /**
     * Extract coverage information from API league response.
     */
    private fun extractCoverageFromLeagueEntry(leagueEntry: com.Lyno.matchmindai.data.dto.football.LeagueEntryDto): Map<String, Boolean> {
        val coverage = mutableMapOf<String, Boolean>()
        
        // Use coverage from league if available, otherwise use default values
        leagueEntry.seasons.firstOrNull()?.coverage?.let { cov ->
            coverage["standings"] = cov.standings
            coverage["players"] = cov.players
            coverage["top_scorers"] = cov.top_scorers
            coverage["predictions"] = cov.predictions
            coverage["odds"] = cov.odds
            coverage["fixtures"] = cov.fixtures.events || cov.fixtures.lineups || cov.fixtures.statistics_fixtures || cov.fixtures.statistics_players
            coverage["events"] = cov.fixtures.events
            coverage["lineups"] = cov.fixtures.lineups
            coverage["statistics"] = cov.fixtures.statistics_fixtures || cov.fixtures.statistics_players
        } ?: run {
            // Default values if no coverage object
            coverage["standings"] = false
            coverage["players"] = false
            coverage["top_scorers"] = false
            coverage["predictions"] = false
            coverage["odds"] = false
            coverage["fixtures"] = true
            coverage["events"] = false
            coverage["lineups"] = false
            coverage["statistics"] = false
        }
        
        return coverage
    }
}
