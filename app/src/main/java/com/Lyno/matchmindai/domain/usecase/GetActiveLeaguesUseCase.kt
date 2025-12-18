package com.Lyno.matchmindai.domain.usecase

import com.Lyno.matchmindai.data.local.entity.LeagueEntity
import com.Lyno.matchmindai.domain.repository.LeagueRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving active leagues for Dynamic League Discovery.
 * This replaces the hardcoded topLeagueIds list with dynamic league data.
 */
class GetActiveLeaguesUseCase @Inject constructor(
    private val leagueRepository: LeagueRepository
) {
    
    /**
     * Get all active leagues from the cache.
     */
    fun execute(): Flow<List<LeagueEntity>> {
        return leagueRepository.getCurrentLeagues()
    }
    
    /**
     * Get top priority league IDs for filtering fixtures.
     * This is the direct replacement for the hardcoded topLeagueIds list.
     */
    suspend fun getTopPriorityLeagueIds(minScore: Int = 50, limit: Int = 20): List<Int> {
        return leagueRepository.getTopPriorityLeagueIds(minScore, limit)
    }
    
    /**
     * Check if leagues need to be refreshed and refresh if necessary.
     * This should be called on app startup.
     */
    suspend fun refreshIfNeeded(): Result<Int> {
        return if (leagueRepository.shouldRefreshLeagues()) {
            leagueRepository.refreshLeaguesFromApi()
        } else {
            Result.success(0) // No refresh needed
        }
    }
    
    /**
     * Force refresh leagues from API (e.g., on manual refresh).
     */
    suspend fun forceRefresh(): Result<Int> {
        return leagueRepository.refreshLeaguesFromApi()
    }
    
    /**
     * Get league priority score for a specific league.
     * Higher score = higher priority in filtering.
     */
    suspend fun getLeaguePriorityScore(leagueId: Int): Int {
        return leagueRepository.getLeaguePriorityScore(leagueId)
    }
    
    /**
     * Search for leagues by name.
     * Used by the agent to find league IDs dynamically.
     */
    suspend fun searchLeagues(query: String): List<LeagueEntity> {
        return leagueRepository.searchLeagues(query)
    }
    
    /**
     * Get league by ID.
     */
    suspend fun getLeagueById(leagueId: Int): LeagueEntity? {
        return leagueRepository.getLeagueById(leagueId)
    }
    
    /**
     * Check if a league exists in the database.
     */
    suspend fun leagueExists(leagueId: Int): Boolean {
        return leagueRepository.leagueExists(leagueId)
    }
    
    /**
     * Get leagues with specific coverage (e.g., leagues that have standings data).
     */
    fun getLeaguesWithStandings(): Flow<List<LeagueEntity>> {
        return leagueRepository.getLeaguesWithStandings()
    }
    
    /**
     * Get leagues with events coverage.
     */
    fun getLeaguesWithEvents(): Flow<List<LeagueEntity>> {
        return leagueRepository.getLeaguesWithEvents()
    }
    
    /**
     * Get leagues with lineups coverage.
     */
    fun getLeaguesWithLineups(): Flow<List<LeagueEntity>> {
        return leagueRepository.getLeaguesWithLineups()
    }
    
    /**
     * Determine priority for a fixture based on league coverage and type.
     * This is used for the "Smart Filter" in MatchRepositoryImpl.
     */
    suspend fun determineFixturePriority(leagueId: Int): Int {
        val league = leagueRepository.getLeagueById(leagueId)
        return league?.priorityScore ?: 0
    }
}
