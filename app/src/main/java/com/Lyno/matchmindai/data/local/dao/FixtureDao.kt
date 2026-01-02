package com.Lyno.matchmindai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.Lyno.matchmindai.data.local.entity.FixtureEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for fixture caching operations.
 * Provides methods to store and retrieve football fixtures from the local database.
 */
@Dao
interface FixtureDao {

    /**
     * Inserts or replaces a list of fixtures for a specific date.
     * Uses REPLACE strategy to update existing fixtures.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFixtures(fixtures: List<FixtureEntity>)

    /**
     * Retrieves all fixtures for a specific date.
     * Returns a Flow for reactive updates.
     */
    @Query("SELECT * FROM fixtures WHERE date = :date ORDER BY timestamp ASC")
    fun getFixturesByDate(date: String): Flow<List<FixtureEntity>>

    /**
     * Retrieves all fixtures for a specific date (non-reactive).
     * Useful for one-time operations.
     */
    @Query("SELECT * FROM fixtures WHERE date = :date ORDER BY timestamp ASC")
    suspend fun getFixturesByDateSync(date: String): List<FixtureEntity>

    /**
     * Checks if there are cached fixtures for a specific date.
     */
    @Query("SELECT COUNT(*) FROM fixtures WHERE date = :date")
    suspend fun hasCachedFixtures(date: String): Int

    /**
     * Gets the timestamp of the oldest cached fixture for a date.
     * Used to determine cache freshness.
     */
    @Query("SELECT MIN(createdAt) FROM fixtures WHERE date = :date")
    suspend fun getOldestCacheTimestamp(date: String): Long?

    /**
     * Deletes all fixtures for a specific date.
     * Used for cache invalidation.
     */
    @Query("DELETE FROM fixtures WHERE date = :date")
    suspend fun deleteFixturesByDate(date: String)

    /**
     * Deletes fixtures older than the specified timestamp.
     * Used for cache cleanup.
     */
    @Query("DELETE FROM fixtures WHERE createdAt < :timestamp")
    suspend fun deleteFixturesOlderThan(timestamp: Long)

    /**
     * Transaction that clears old fixtures and inserts new ones for a date.
     * Ensures atomic cache update.
     */
    @Transaction
    suspend fun updateFixturesForDate(date: String, fixtures: List<FixtureEntity>) {
        deleteFixturesByDate(date)
        insertFixtures(fixtures)
    }

    /**
     * Gets the total count of cached fixtures.
     * Useful for monitoring cache size.
     */
    @Query("SELECT COUNT(*) FROM fixtures")
    suspend fun getTotalCachedFixtures(): Int
}
