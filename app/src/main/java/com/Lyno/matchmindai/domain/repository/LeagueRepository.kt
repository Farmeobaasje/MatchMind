package com.Lyno.matchmindai.domain.repository

import com.Lyno.matchmindai.data.local.entity.LeagueEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for league operations.
 * Provides access to cached league data for Dynamic League Discovery.
 */
interface LeagueRepository {
    
    /**
     * Get all leagues from the database.
     */
    fun getAllLeagues(): Flow<List<LeagueEntity>>
    
    /**
     * Get current (active) leagues only.
     */
    fun getCurrentLeagues(): Flow<List<LeagueEntity>>
    
    /**
     * Get a league by its ID.
     */
    suspend fun getLeagueById(leagueId: Int): LeagueEntity?
    
    /**
     * Search leagues by name.
     */
    suspend fun searchLeagues(query: String): List<LeagueEntity>
    
    /**
     * Get top priority league IDs for filtering fixtures.
     * This replaces the hardcoded topLeagueIds list.
     */
    suspend fun getTopPriorityLeagueIds(minScore: Int = 50, limit: Int = 20): List<Int>
    
    /**
     * Get leagues with specific coverage (e.g., leagues that have standings data).
     */
    fun getLeaguesWithStandings(): Flow<List<LeagueEntity>>
    
    /**
     * Get leagues with events coverage.
     */
    fun getLeaguesWithEvents(): Flow<List<LeagueEntity>>
    
    /**
     * Get leagues with lineups coverage.
     */
    fun getLeaguesWithLineups(): Flow<List<LeagueEntity>>
    
    /**
     * Check if a league exists in the database.
     */
    suspend fun leagueExists(leagueId: Int): Boolean
    
    /**
     * Refresh leagues from the API and update the cache.
     * This should be called on app startup or periodically.
     */
    suspend fun refreshLeaguesFromApi(): Result<Int>
    
    /**
     * Get the count of leagues in the database.
     */
    suspend fun getLeagueCount(): Int
    
    /**
     * Check if leagues need to be refreshed (e.g., data is older than 7 days).
     */
    suspend fun shouldRefreshLeagues(): Boolean
    
    /**
     * Get league priority score for a specific league.
     * Higher score = higher priority in filtering.
     */
    suspend fun getLeaguePriorityScore(leagueId: Int): Int
    
    /**
     * Get league coverage information for a specific league.
     * Returns a map of coverage flags.
     */
    suspend fun getLeagueCoverage(leagueId: Int): Map<String, Boolean>
}
