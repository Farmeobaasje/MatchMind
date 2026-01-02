package com.Lyno.matchmindai.data.remote.football

import android.util.Log
import com.Lyno.matchmindai.domain.model.ExpectedGoalsData
import java.util.concurrent.ConcurrentHashMap

/**
 * Cache for Expected Goals (xG) data to reduce API calls and improve performance.
 * 
 * This cache implements:
 * 1. In-memory storage with TTL (Time To Live)
 * 2. Automatic cleanup of expired entries
 * 3. Thread-safe operations
 * 4. Statistics tracking for debugging
 * 
 * Cache strategy:
 * - xG data is cached for 24 hours (since it doesn't change after match completion)
 * - Statistics data is cached for 1 hour (can be updated during live matches)
 * - Cache is automatically cleaned up when entries expire
 */
class XGDataCache {
    
    companion object {
        private const val XG_CACHE_TTL_MS = 24 * 60 * 60 * 1000L // 24 hours
        private const val STATS_CACHE_TTL_MS = 60 * 60 * 1000L // 1 hour
        private const val CLEANUP_INTERVAL_MS = 30 * 60 * 1000L // 30 minutes
        
        @Volatile
        private var instance: XGDataCache? = null
        
        fun getInstance(): XGDataCache {
            return instance ?: synchronized(this) {
                instance ?: XGDataCache().also { instance = it }
            }
        }
    }
    
    private data class CacheEntry(
        val xgData: ExpectedGoalsData,
        val timestamp: Long,
        val ttl: Long
    )
    
    private val cache = ConcurrentHashMap<Int, CacheEntry>()
    private var lastCleanupTime = System.currentTimeMillis()
    
    // Statistics for monitoring
    private var hits = 0
    private var misses = 0
    private var evictions = 0
    
    /**
     * Get xG data from cache if available and not expired.
     * 
     * @param fixtureId The fixture ID to look up
     * @return ExpectedGoalsData if found and valid, null otherwise
     */
    fun getXGData(fixtureId: Int): ExpectedGoalsData? {
        // Perform periodic cleanup
        performCleanupIfNeeded()
        
        val entry = cache[fixtureId]
        if (entry == null) {
            misses++
            return null
        }
        
        // Check if entry is expired
        if (System.currentTimeMillis() - entry.timestamp > entry.ttl) {
            cache.remove(fixtureId)
            evictions++
            misses++
            return null
        }
        
        hits++
        Log.d("XGDataCache", "Cache HIT for fixture $fixtureId")
        return entry.xgData
    }
    
    /**
     * Store xG data in cache.
     * 
     * @param fixtureId The fixture ID
     * @param xgData The ExpectedGoalsData to cache
     * @param ttl Optional TTL in milliseconds (defaults to 24 hours)
     */
    fun setXGData(fixtureId: Int, xgData: ExpectedGoalsData, ttl: Long = XG_CACHE_TTL_MS) {
        val entry = CacheEntry(xgData, System.currentTimeMillis(), ttl)
        cache[fixtureId] = entry
        Log.d("XGDataCache", "Cache SET for fixture $fixtureId (TTL: ${ttl / 1000 / 60} minutes)")
    }
    
    /**
     * Store statistics data in cache with shorter TTL.
     * 
     * @param fixtureId The fixture ID
     * @param statisticsData The statistics data to cache
     */
    fun setStatisticsData(fixtureId: Int, statisticsData: ExpectedGoalsData) {
        setXGData(fixtureId, statisticsData, STATS_CACHE_TTL_MS)
    }
    
    /**
     * Remove xG data from cache.
     * 
     * @param fixtureId The fixture ID to remove
     */
    fun removeXGData(fixtureId: Int) {
        cache.remove(fixtureId)
        Log.d("XGDataCache", "Cache REMOVE for fixture $fixtureId")
    }
    
    /**
     * Clear all cached data.
     */
    fun clear() {
        cache.clear()
        hits = 0
        misses = 0
        evictions = 0
        lastCleanupTime = System.currentTimeMillis()
        Log.d("XGDataCache", "Cache CLEARED")
    }
    
    /**
     * Get cache statistics for debugging.
     */
    fun getStats(): String {
        val hitRate = if (hits + misses > 0) {
            (hits.toDouble() / (hits + misses) * 100).toInt()
        } else {
            0
        }
        
        return "Cache Stats: " +
               "Size=${cache.size}, " +
               "Hits=$hits, " +
               "Misses=$misses, " +
               "Evictions=$evictions, " +
               "Hit Rate=${hitRate}%"
    }
    
    /**
     * Get cache size.
     */
    fun size(): Int = cache.size
    
    /**
     * Check if cache contains valid data for a fixture.
     */
    fun contains(fixtureId: Int): Boolean {
        return getXGData(fixtureId) != null
    }
    
    /**
     * Perform cleanup of expired entries if enough time has passed.
     */
    private fun performCleanupIfNeeded() {
        val now = System.currentTimeMillis()
        if (now - lastCleanupTime > CLEANUP_INTERVAL_MS) {
            cleanup()
            lastCleanupTime = now
        }
    }
    
    /**
     * Clean up expired entries from cache.
     */
    private fun cleanup() {
        val now = System.currentTimeMillis()
        val expiredKeys = mutableListOf<Int>()
        
        for ((fixtureId, entry) in cache) {
            if (now - entry.timestamp > entry.ttl) {
                expiredKeys.add(fixtureId)
            }
        }
        
        expiredKeys.forEach { key ->
            cache.remove(key)
            evictions++
        }
        
        if (expiredKeys.isNotEmpty()) {
            Log.d("XGDataCache", "Cleanup removed ${expiredKeys.size} expired entries")
        }
    }
}
