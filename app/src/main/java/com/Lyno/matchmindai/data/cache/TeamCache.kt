package com.Lyno.matchmindai.data.cache

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap
import android.util.Log

val Context.teamCacheDataStore by preferencesDataStore(name = "team_cache")

/**
 * Serializable data class for team information with caching metadata.
 */
@Serializable
data class TeamCacheEntry(
    val teamId: Int,
    val teamName: String,
    val logoUrl: String? = null,
    val leagueId: Int? = null,
    val leagueName: String? = null,
    val country: String? = null,
    val lastUpdated: Long = System.currentTimeMillis(),
    val cacheTtl: Long = 24 * 60 * 60 * 1000L // 24 hours in milliseconds
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
    fun refreshed(): TeamCacheEntry {
        return copy(lastUpdated = System.currentTimeMillis())
    }
}

/**
 * Persistent cache for team information to solve "Teamnaam niet gevonden" issues.
 * Uses DataStore for persistence and in-memory cache for performance.
 *
 * Features:
 * - Persistent storage using DataStore
 * - In-memory ConcurrentHashMap for fast access
 * - TTL-based cache invalidation (24 hours)
 * - Thread-safe operations
 * - Fallback mechanisms for missing data
 */
class TeamCache(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }
    
    // In-memory cache for fast access (thread-safe)
    private val memoryCache = ConcurrentHashMap<Int, TeamCacheEntry>()
    
    // Mutex for thread-safe DataStore operations
    private val dataStoreMutex = Any()

    companion object {
        private const val TAG = "TeamCache"
        
        // DataStore keys
        private fun teamKey(teamId: Int) = stringPreferencesKey("team_$teamId")
        
        // Known friendlies league IDs
        private val FRIENDLIES_LEAGUE_IDS = setOf(667, 668, 669)
        
        // Known team IDs with their names (for fallback)
        private val KNOWN_TEAMS = mapOf(
            194 to "Ajax",
            33 to "Manchester United",
            40 to "Liverpool",
            50 to "Manchester City",
            49 to "Chelsea",
            42 to "Arsenal",
            541 to "Real Madrid",
            529 to "Barcelona",
            157 to "Bayern Munich",
            165 to "Borussia Dortmund",
            505 to "Inter Milan",
            489 to "AC Milan",
            496 to "Juventus",
            85 to "Paris Saint Germain"
        )
    }

    /**
     * Get team name by ID with comprehensive fallback strategy.
     * 
     * Fallback order:
     * 1. Check in-memory cache (valid entries only)
     * 2. Check persistent DataStore cache
     * 3. Check known teams map (hardcoded fallback)
     * 4. Return null (caller should fetch from API)
     */
    suspend fun getTeamNameById(teamId: Int): String? {
        Log.d(TAG, "Getting team name for ID: $teamId")
        
        // 1. Check in-memory cache
        memoryCache[teamId]?.let { entry ->
            if (entry.isValid()) {
                Log.d(TAG, "Cache HIT (memory) for team $teamId: ${entry.teamName}")
                return entry.teamName
            } else {
                Log.d(TAG, "Cache EXPIRED (memory) for team $teamId")
                memoryCache.remove(teamId)
            }
        }
        
        // 2. Check persistent DataStore cache
        val persistentEntry = getFromDataStore(teamId)
        persistentEntry?.let { entry ->
            if (entry.isValid()) {
                // Update memory cache
                memoryCache[teamId] = entry
                Log.d(TAG, "Cache HIT (persistent) for team $teamId: ${entry.teamName}")
                return entry.teamName
            } else {
                Log.d(TAG, "Cache EXPIRED (persistent) for team $teamId")
                removeFromDataStore(teamId)
            }
        }
        
        // 3. Check known teams map (hardcoded fallback)
        KNOWN_TEAMS[teamId]?.let { teamName ->
            Log.d(TAG, "Using known team fallback for $teamId: $teamName")
            // Cache the known team
            cacheTeam(teamId, teamName)
            return teamName
        }
        
        // 4. No cache entry found
        Log.w(TAG, "Cache MISS for team $teamId")
        return null
    }

    /**
     * Get full team cache entry by ID.
     */
    suspend fun getTeamById(teamId: Int): TeamCacheEntry? {
        // 1. Check in-memory cache
        memoryCache[teamId]?.let { entry ->
            if (entry.isValid()) {
                return entry
            } else {
                memoryCache.remove(teamId)
            }
        }
        
        // 2. Check persistent DataStore cache
        val persistentEntry = getFromDataStore(teamId)
        persistentEntry?.let { entry ->
            if (entry.isValid()) {
                memoryCache[teamId] = entry
                return entry
            } else {
                removeFromDataStore(teamId)
            }
        }
        
        return null
    }

    /**
     * Cache team information.
     * 
     * @param teamId The team ID
     * @param teamName The team name
     * @param logoUrl Optional logo URL
     * @param leagueId Optional league ID
     * @param leagueName Optional league name
     * @param country Optional country
     */
    suspend fun cacheTeam(
        teamId: Int,
        teamName: String,
        logoUrl: String? = null,
        leagueId: Int? = null,
        leagueName: String? = null,
        country: String? = null
    ) {
        val entry = TeamCacheEntry(
            teamId = teamId,
            teamName = teamName,
            logoUrl = logoUrl,
            leagueId = leagueId,
            leagueName = leagueName,
            country = country
        )
        
        // Update memory cache
        memoryCache[teamId] = entry
        
        // Update persistent cache
        saveToDataStore(teamId, entry)
        
        Log.d(TAG, "Cached team $teamId: $teamName")
    }

    /**
     * Batch cache multiple teams.
     */
    suspend fun cacheTeams(teams: List<TeamCacheEntry>) {
        teams.forEach { team ->
            memoryCache[team.teamId] = team
            saveToDataStore(team.teamId, team)
        }
        Log.d(TAG, "Batch cached ${teams.size} teams")
    }

    /**
     * Remove team from cache.
     */
    suspend fun removeTeam(teamId: Int) {
        memoryCache.remove(teamId)
        removeFromDataStore(teamId)
        Log.d(TAG, "Removed team $teamId from cache")
    }

    /**
     * Clear all cache entries.
     */
    suspend fun clearAll() {
        memoryCache.clear()
        try {
            context.teamCacheDataStore.edit { preferences ->
                val keysToRemove = mutableListOf<androidx.datastore.preferences.core.Preferences.Key<*>>()
                for (key in preferences.asMap().keys) {
                    if (key.name.startsWith("team_")) {
                        keysToRemove.add(key)
                    }
                }
                keysToRemove.forEach { key ->
                    preferences.remove(key)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing team cache", e)
        }
        Log.d(TAG, "Cleared all team cache")
    }

    /**
     * Get all cached team IDs.
     */
    suspend fun getAllCachedTeamIds(): List<Int> {
        return try {
            val preferences = context.teamCacheDataStore.data.first()
            val teamIds = mutableListOf<Int>()
            for ((key, _) in preferences.asMap()) {
                if (key.name.startsWith("team_")) {
                    val teamIdStr = key.name.removePrefix("team_")
                    teamIdStr.toIntOrNull()?.let { teamIds.add(it) }
                }
            }
            teamIds
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cached team IDs", e)
            emptyList()
        }
    }

    /**
     * Check if a team is cached and valid.
     */
    suspend fun isTeamCached(teamId: Int): Boolean {
        return getTeamById(teamId) != null
    }

    /**
     * Get cache statistics.
     */
    suspend fun getStats(): CacheStats {
        val allIds = getAllCachedTeamIds()
        val validEntries = allIds.count { teamId ->
            getTeamById(teamId)?.isValid() == true
        }
        
        return CacheStats(
            totalEntries = allIds.size,
            validEntries = validEntries,
            memoryCacheSize = memoryCache.size
        )
    }

    /**
     * Check if a league is a friendlies league.
     */
    fun isFriendliesLeague(leagueId: Int): Boolean {
        return leagueId in FRIENDLIES_LEAGUE_IDS
    }

    /**
     * Get known team name for fallback.
     */
    fun getKnownTeamName(teamId: Int): String? {
        return KNOWN_TEAMS[teamId]
    }

    // Private helper methods for DataStore operations
    
    private suspend fun getFromDataStore(teamId: Int): TeamCacheEntry? {
        return try {
            val preferences = context.teamCacheDataStore.data.first()
            val jsonString = preferences[teamKey(teamId)]
            jsonString?.let { json.decodeFromString<TeamCacheEntry>(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading team $teamId from DataStore", e)
            null
        }
    }
    
    private suspend fun saveToDataStore(teamId: Int, entry: TeamCacheEntry) {
        try {
            val jsonString = json.encodeToString(entry)
            context.teamCacheDataStore.edit { preferences ->
                preferences[teamKey(teamId)] = jsonString
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving team $teamId to DataStore", e)
        }
    }
    
    private suspend fun removeFromDataStore(teamId: Int) {
        try {
            context.teamCacheDataStore.edit { preferences ->
                preferences.remove(teamKey(teamId))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error removing team $teamId from DataStore", e)
        }
    }
}

/**
 * Cache statistics data class.
 */
data class CacheStats(
    val totalEntries: Int,
    val validEntries: Int,
    val memoryCacheSize: Int
) {
    val hitRate: Double
        get() = if (totalEntries > 0) validEntries.toDouble() / totalEntries else 0.0
    
    override fun toString(): String {
        return "CacheStats(total=$totalEntries, valid=$validEntries, memory=$memoryCacheSize, hitRate=${"%.1f".format(hitRate * 100)}%)"
    }
}
