package com.Lyno.matchmindai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.Lyno.matchmindai.data.local.entity.LeagueEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for league entities.
 * Provides methods for caching and retrieving active leagues for Dynamic League Discovery.
 */
@Dao
interface LeagueDao {
    
    /**
     * Insert or replace a league entity.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeague(league: LeagueEntity)
    
    /**
     * Insert or replace multiple league entities.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeagues(leagues: List<LeagueEntity>)
    
    /**
     * Update an existing league entity.
     */
    @Update
    suspend fun updateLeague(league: LeagueEntity)
    
    /**
     * Get all leagues from the database.
     */
    @Query("SELECT * FROM leagues ORDER BY priority_score DESC, name ASC")
    fun getAllLeagues(): Flow<List<LeagueEntity>>
    
    /**
     * Get leagues by country.
     */
    @Query("SELECT * FROM leagues WHERE country = :country ORDER BY priority_score DESC, name ASC")
    fun getLeaguesByCountry(country: String): Flow<List<LeagueEntity>>
    
    /**
     * Get leagues by type (League or Cup).
     */
    @Query("SELECT * FROM leagues WHERE type = :type ORDER BY priority_score DESC, name ASC")
    fun getLeaguesByType(type: String): Flow<List<LeagueEntity>>
    
    /**
     * Get current (active) leagues only.
     */
    @Query("SELECT * FROM leagues WHERE is_current = 1 ORDER BY priority_score DESC, name ASC")
    fun getCurrentLeagues(): Flow<List<LeagueEntity>>
    
    /**
     * Get a league by its ID.
     */
    @Query("SELECT * FROM leagues WHERE league_id = :leagueId")
    suspend fun getLeagueById(leagueId: Int): LeagueEntity?
    
    /**
     * Get leagues with specific coverage (e.g., leagues that have standings data).
     */
    @Query("SELECT * FROM leagues WHERE has_standings = 1 ORDER BY priority_score DESC, name ASC")
    fun getLeaguesWithStandings(): Flow<List<LeagueEntity>>
    
    /**
     * Get leagues with events coverage.
     */
    @Query("SELECT * FROM leagues WHERE has_events = 1 ORDER BY priority_score DESC, name ASC")
    fun getLeaguesWithEvents(): Flow<List<LeagueEntity>>
    
    /**
     * Get leagues with lineups coverage.
     */
    @Query("SELECT * FROM leagues WHERE has_lineups = 1 ORDER BY priority_score DESC, name ASC")
    fun getLeaguesWithLineups(): Flow<List<LeagueEntity>>
    
    /**
     * Search leagues by name (case-insensitive).
     */
    @Query("SELECT * FROM leagues WHERE name LIKE '%' || :query || '%' ORDER BY priority_score DESC, name ASC")
    suspend fun searchLeagues(query: String): List<LeagueEntity>
    
    /**
     * Get top priority leagues (for filtering fixtures).
     */
    @Query("SELECT * FROM leagues WHERE priority_score >= :minScore ORDER BY priority_score DESC LIMIT :limit")
    suspend fun getTopPriorityLeagues(minScore: Int = 50, limit: Int = 20): List<LeagueEntity>
    
    /**
     * Get league IDs for top priority leagues.
     * This is used for the Dynamic League Discovery filter.
     */
    @Query("SELECT league_id FROM leagues WHERE priority_score >= :minScore ORDER BY priority_score DESC LIMIT :limit")
    suspend fun getTopPriorityLeagueIds(minScore: Int = 50, limit: Int = 20): List<Int>
    
    /**
     * Check if a league exists in the database.
     */
    @Query("SELECT COUNT(*) FROM leagues WHERE league_id = :leagueId")
    suspend fun leagueExists(leagueId: Int): Int
    
    /**
     * Delete a league by ID.
     */
    @Query("DELETE FROM leagues WHERE league_id = :leagueId")
    suspend fun deleteLeague(leagueId: Int)
    
    /**
     * Delete all leagues.
     */
    @Query("DELETE FROM leagues")
    suspend fun deleteAllLeagues()
    
    /**
     * Delete leagues older than specified timestamp.
     */
    @Query("DELETE FROM leagues WHERE last_updated < :timestamp")
    suspend fun deleteOldLeagues(timestamp: Long)
    
    /**
     * Get the count of leagues in the database.
     */
    @Query("SELECT COUNT(*) FROM leagues")
    suspend fun getLeagueCount(): Int
    
    /**
     * Get the timestamp of the most recently updated league.
     */
    @Query("SELECT MAX(last_updated) FROM leagues")
    suspend fun getLastUpdateTime(): Long?
}
