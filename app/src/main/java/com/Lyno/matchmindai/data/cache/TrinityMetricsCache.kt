package com.Lyno.matchmindai.data.cache

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap
import android.util.Log

val Context.trinityMetricsDataStore by preferencesDataStore(name = "trinity_metrics_cache")

/**
 * Serializable data class for Trinity Metrics caching.
 * Trinity Metrics consist of:
 * - Fatigue Score (0-100): Team fatigue level
 * - Lineup Strength (0-100): Strength of current lineup
 * - Style Matchup (0-100): How well team styles match up
 */
@Serializable
data class TrinityMetricsEntry(
    val fixtureId: Int,
    val homeTeamId: Int,
    val awayTeamId: Int,
    val season: Int,
    val fatigueScore: Int,
    val lineupStrength: Int,
    val styleMatchup: Int,
    val homeFitness: Int,
    val awayFitness: Int,
    val homeDistraction: Int,
    val awayDistraction: Int,
    val reasoning: String? = null,
    val lastUpdated: Long = System.currentTimeMillis(),
    val cacheTtl: Long = 6 * 60 * 60 * 1000L // 6 hours in milliseconds (shorter TTL than team cache)
) {
    /**
     * Checks if the cache entry is still valid.
     */
    fun isValid(): Boolean {
        return System.currentTimeMillis() - lastUpdated < cacheTtl
    }

    /**
     * Creates a copy with updated timestamp.
     */
    fun refreshed(): TrinityMetricsEntry {
        return copy(lastUpdated = System.currentTimeMillis())
    }
    
    /**
     * Returns a formatted string for logging.
     */
    fun toLogString(): String {
        return "TrinityMetrics(fixture=$fixtureId, fatigue=$fatigueScore, lineup=$lineupStrength, style=$styleMatchup)"
    }
}

/**
 * Persistent cache for Trinity Metrics to reduce API calls and improve performance.
 * Uses DataStore for persistence and in-memory cache for performance.
 *
 * Features:
 * - Persistent storage using DataStore
 * - In-memory ConcurrentHashMap for fast access
 * - TTL-based cache invalidation (6 hours)
 * - Thread-safe operations
 * - Keyed by fixtureId for exact match lookup
 * - Also supports team-season lookup for fallback
 */
class TrinityMetricsCache(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }
    
    // In-memory cache for fast access (thread-safe)
    private val memoryCache = ConcurrentHashMap<Int, TrinityMetricsEntry>()
    
    companion object {
        private const val TAG = "TrinityMetricsCache"
        
        // DataStore keys
        private fun fixtureKey(fixtureId: Int) = stringPreferencesKey("fixture_$fixtureId")
        private fun teamSeasonKey(homeTeamId: Int, awayTeamId: Int, season: Int) = 
            stringPreferencesKey("team_season_${homeTeamId}_${awayTeamId}_$season")
    }

    /**
     * Get Trinity Metrics by fixture ID.
     * 
     * @param fixtureId The fixture ID
     * @return Cached Trinity metrics or null if not found/expired
     */
    suspend fun getByFixtureId(fixtureId: Int): TrinityMetricsEntry? {
        Log.d(TAG, "Getting Trinity metrics for fixture: $fixtureId")
        
        // 1. Check in-memory cache
        memoryCache[fixtureId]?.let { entry ->
            if (entry.isValid()) {
                Log.d(TAG, "Cache HIT (memory) for fixture $fixtureId: ${entry.toLogString()}")
                return entry
            } else {
                Log.d(TAG, "Cache EXPIRED (memory) for fixture $fixtureId")
                memoryCache.remove(fixtureId)
            }
        }
        
        // 2. Check persistent DataStore cache
        val persistentEntry = getFromDataStore(fixtureId)
        persistentEntry?.let { entry ->
            if (entry.isValid()) {
                // Update memory cache
                memoryCache[fixtureId] = entry
                Log.d(TAG, "Cache HIT (persistent) for fixture $fixtureId: ${entry.toLogString()}")
                return entry
            } else {
                Log.d(TAG, "Cache EXPIRED (persistent) for fixture $fixtureId")
                removeFromDataStore(fixtureId)
            }
        }
        
        // 3. No cache entry found
        Log.w(TAG, "Cache MISS for fixture $fixtureId")
        return null
    }

    /**
     * Get Trinity Metrics by team IDs and season (fallback lookup).
     * This is useful when we don't have a specific fixture ID yet.
     * 
     * @param homeTeamId Home team ID
     * @param awayTeamId Away team ID
     * @param season Season year
     * @return Cached Trinity metrics or null if not found/expired
     */
    suspend fun getByTeamsAndSeason(homeTeamId: Int, awayTeamId: Int, season: Int): TrinityMetricsEntry? {
        Log.d(TAG, "Getting Trinity metrics for teams: $homeTeamId vs $awayTeamId, season $season")
        
        // First, try to find any cached entry for these teams in this season
        val allEntries = getAllCachedEntries()
        val matchingEntry = allEntries.firstOrNull { entry ->
            entry.homeTeamId == homeTeamId && 
            entry.awayTeamId == awayTeamId && 
            entry.season == season &&
            entry.isValid()
        }
        
        matchingEntry?.let { entry ->
            Log.d(TAG, "Found team-season cache for $homeTeamId vs $awayTeamId: ${entry.toLogString()}")
            // Also cache by fixture ID if we have one
            entry.fixtureId.takeIf { it > 0 }?.let { fixtureId ->
                memoryCache[fixtureId] = entry
            }
            return entry
        }
        
        Log.w(TAG, "Cache MISS for teams $homeTeamId vs $awayTeamId, season $season")
        return null
    }

    /**
     * Cache Trinity Metrics.
     * 
     * @param entry The Trinity metrics entry to cache
     */
    suspend fun cacheMetrics(entry: TrinityMetricsEntry) {
        // Update memory cache
        memoryCache[entry.fixtureId] = entry
        
        // Update persistent cache with fixture key
        saveToDataStore(entry.fixtureId, entry)
        
        // Also save with team-season key for fallback lookup
        saveTeamSeasonToDataStore(entry)
        
        Log.d(TAG, "Cached Trinity metrics: ${entry.toLogString()}")
    }

    /**
     * Remove Trinity Metrics by fixture ID.
     */
    suspend fun removeByFixtureId(fixtureId: Int) {
        memoryCache.remove(fixtureId)
        removeFromDataStore(fixtureId)
        Log.d(TAG, "Removed Trinity metrics for fixture $fixtureId from cache")
    }

    /**
     * Clear all cache entries.
     */
    suspend fun clearAll() {
        memoryCache.clear()
        try {
            context.trinityMetricsDataStore.edit { preferences ->
                val keysToRemove = mutableListOf<androidx.datastore.preferences.core.Preferences.Key<*>>()
                for (key in preferences.asMap().keys) {
                    if (key.name.startsWith("fixture_") || key.name.startsWith("team_season_")) {
                        keysToRemove.add(key)
                    }
                }
                keysToRemove.forEach { key ->
                    preferences.remove(key)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing Trinity metrics cache", e)
        }
        Log.d(TAG, "Cleared all Trinity metrics cache")
    }

    /**
     * Get all cached fixture IDs.
     */
    suspend fun getAllCachedFixtureIds(): List<Int> {
        return try {
            val preferences = context.trinityMetricsDataStore.data.first()
            val fixtureIds = mutableListOf<Int>()
            for ((key, _) in preferences.asMap()) {
                if (key.name.startsWith("fixture_")) {
                    val fixtureIdStr = key.name.removePrefix("fixture_")
                    fixtureIdStr.toIntOrNull()?.let { fixtureIds.add(it) }
                }
            }
            fixtureIds
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cached fixture IDs", e)
            emptyList()
        }
    }

    /**
     * Get all cached entries.
     */
    suspend fun getAllCachedEntries(): List<TrinityMetricsEntry> {
        val fixtureIds = getAllCachedFixtureIds()
        val entries = mutableListOf<TrinityMetricsEntry>()
        
        fixtureIds.forEach { fixtureId ->
            getFromDataStore(fixtureId)?.let { entry ->
                if (entry.isValid()) {
                    entries.add(entry)
                }
            }
        }
        
        return entries
    }

    /**
     * Check if Trinity metrics are cached for a fixture.
     */
    suspend fun isCachedForFixture(fixtureId: Int): Boolean {
        return getByFixtureId(fixtureId) != null
    }

    /**
     * Get cache statistics.
     */
    suspend fun getStats(): TrinityCacheStats {
        val allFixtureIds = getAllCachedFixtureIds()
        val validEntries = allFixtureIds.count { fixtureId ->
            getByFixtureId(fixtureId) != null
        }
        
        return TrinityCacheStats(
            totalEntries = allFixtureIds.size,
            validEntries = validEntries,
            memoryCacheSize = memoryCache.size
        )
    }

    // Private helper methods for DataStore operations
    
    private suspend fun getFromDataStore(fixtureId: Int): TrinityMetricsEntry? {
        return try {
            val preferences = context.trinityMetricsDataStore.data.first()
            val jsonString = preferences[fixtureKey(fixtureId)]
            jsonString?.let { json.decodeFromString<TrinityMetricsEntry>(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading Trinity metrics for fixture $fixtureId from DataStore", e)
            null
        }
    }
    
    private suspend fun saveToDataStore(fixtureId: Int, entry: TrinityMetricsEntry) {
        try {
            val jsonString = json.encodeToString(entry)
            context.trinityMetricsDataStore.edit { preferences ->
                preferences[fixtureKey(fixtureId)] = jsonString
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving Trinity metrics for fixture $fixtureId to DataStore", e)
        }
    }
    
    private suspend fun saveTeamSeasonToDataStore(entry: TrinityMetricsEntry) {
        try {
            val jsonString = json.encodeToString(entry)
            val key = teamSeasonKey(entry.homeTeamId, entry.awayTeamId, entry.season)
            context.trinityMetricsDataStore.edit { preferences ->
                preferences[key] = jsonString
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving team-season Trinity metrics to DataStore", e)
        }
    }
    
    private suspend fun removeFromDataStore(fixtureId: Int) {
        try {
            context.trinityMetricsDataStore.edit { preferences ->
                preferences.remove(fixtureKey(fixtureId))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error removing Trinity metrics for fixture $fixtureId from DataStore", e)
        }
    }
}

/**
 * Trinity Metrics cache statistics data class.
 */
data class TrinityCacheStats(
    val totalEntries: Int,
    val validEntries: Int,
    val memoryCacheSize: Int
) {
    val hitRate: Double
        get() = if (totalEntries > 0) validEntries.toDouble() / totalEntries else 0.0
    
    override fun toString(): String {
        return "TrinityCacheStats(total=$totalEntries, valid=$validEntries, memory=$memoryCacheSize, hitRate=${"%.1f".format(hitRate * 100)}%)"
    }
}
