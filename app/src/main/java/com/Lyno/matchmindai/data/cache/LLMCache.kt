package com.Lyno.matchmindai.data.cache

import android.util.Log
import com.Lyno.matchmindai.domain.model.LLMGradeEnhancement
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.TimeUnit

/**
 * Cache for LLM responses to reduce API calls and improve performance.
 * Implements time-based expiration and size-based eviction.
 */
class LLMCache {

    companion object {
        private const val TAG = "LLMCache"
        private const val DEFAULT_CACHE_SIZE = 50
        private const val DEFAULT_TTL_HOURS = 24L  // Cache for 24 hours
    }

    private data class CacheEntry(
        val response: LLMGradeEnhancement,
        val timestamp: Long,
        val fixtureId: Int,
        val promptHash: String
    )

    private val cache = LinkedHashMap<String, CacheEntry>()
    private val mutex = Mutex()
    private var maxSize = DEFAULT_CACHE_SIZE
    private var ttlMillis = TimeUnit.HOURS.toMillis(DEFAULT_TTL_HOURS)

    /**
     * Configure cache settings.
     * 
     * @param maxSize Maximum number of entries in cache (default: 50)
     * @param ttlHours Time-to-live in hours (default: 24)
     */
    fun configure(maxSize: Int = DEFAULT_CACHE_SIZE, ttlHours: Long = DEFAULT_TTL_HOURS) {
        this.maxSize = maxSize
        this.ttlMillis = TimeUnit.HOURS.toMillis(ttlHours)
        Log.d(TAG, "Cache configured: maxSize=$maxSize, TTL=${ttlHours}h")
    }

    /**
     * Get cached LLM enhancement or fetch if not available/expired.
     * 
     * @param fixtureId Fixture ID for the match
     * @param promptHash Hash of the prompt used (for cache key)
     * @param fetchFunction Function to fetch the enhancement if not in cache
     * @return LLMGradeEnhancement from cache or fresh fetch
     */
    suspend fun getOrFetch(
        fixtureId: Int,
        promptHash: String,
        fetchFunction: suspend () -> LLMGradeEnhancement?
    ): LLMGradeEnhancement? = mutex.withLock {
        val cacheKey = generateCacheKey(fixtureId, promptHash)
        
        // Check cache first
        val cachedEntry = cache[cacheKey]
        if (cachedEntry != null && !isExpired(cachedEntry)) {
            Log.d(TAG, "✅ LLM cache HIT for fixture $fixtureId, promptHash: ${promptHash.take(8)}...")
            return cachedEntry.response
        }
        
        // Remove expired entry if present
        if (cachedEntry != null && isExpired(cachedEntry)) {
            Log.d(TAG, "🗑️ Removing expired cache entry for fixture $fixtureId")
            cache.remove(cacheKey)
        }
        
        // Cache miss - fetch fresh data
        Log.d(TAG, "❌ LLM cache MISS for fixture $fixtureId, fetching fresh data...")
        val freshResponse = fetchFunction()
        
        // Cache the response if successful
        if (freshResponse != null) {
            val newEntry = CacheEntry(
                response = freshResponse,
                timestamp = System.currentTimeMillis(),
                fixtureId = fixtureId,
                promptHash = promptHash
            )
            cache[cacheKey] = newEntry
            Log.d(TAG, "💾 Cached LLM response for fixture $fixtureId")
            
            // Enforce size limit
            enforceSizeLimit()
        }
        
        return freshResponse
    }

    /**
     * Get cached enhancement if available and not expired.
     * 
     * @param fixtureId Fixture ID
     * @param promptHash Prompt hash
     * @return Cached enhancement or null if not available/expired
     */
    suspend fun getIfAvailable(fixtureId: Int, promptHash: String): LLMGradeEnhancement? = mutex.withLock {
        val cacheKey = generateCacheKey(fixtureId, promptHash)
        val cachedEntry = cache[cacheKey]
        
        return if (cachedEntry != null && !isExpired(cachedEntry)) {
            Log.d(TAG, "✅ LLM cache HIT (getIfAvailable) for fixture $fixtureId")
            cachedEntry.response
        } else {
            null
        }
    }

    /**
     * Put enhancement in cache.
     * 
     * @param fixtureId Fixture ID
     * @param promptHash Prompt hash
     * @param enhancement LLM enhancement to cache
     */
    suspend fun put(fixtureId: Int, promptHash: String, enhancement: LLMGradeEnhancement) = mutex.withLock {
        val cacheKey = generateCacheKey(fixtureId, promptHash)
        val entry = CacheEntry(
            response = enhancement,
            timestamp = System.currentTimeMillis(),
            fixtureId = fixtureId,
            promptHash = promptHash
        )
        
        cache[cacheKey] = entry
        Log.d(TAG, "💾 Put LLM response in cache for fixture $fixtureId")
        
        // Enforce size limit
        enforceSizeLimit()
    }

    /**
     * Remove entry from cache.
     * 
     * @param fixtureId Fixture ID
     * @param promptHash Prompt hash
     */
    suspend fun remove(fixtureId: Int, promptHash: String) = mutex.withLock {
        val cacheKey = generateCacheKey(fixtureId, promptHash)
        cache.remove(cacheKey)
        Log.d(TAG, "🗑️ Removed cache entry for fixture $fixtureId")
    }

    /**
     * Clear all cache entries.
     */
    suspend fun clear() = mutex.withLock {
        val size = cache.size
        cache.clear()
        Log.d(TAG, "🧹 Cleared all $size cache entries")
    }

    /**
     * Get cache statistics.
     * 
     * @return CacheStats object with current cache information
     */
    suspend fun getStats(): CacheStats = mutex.withLock {
        val now = System.currentTimeMillis()
        val expiredEntries = cache.values.count { isExpired(it, now) }
        val validEntries = cache.size - expiredEntries
        
        return CacheStats(
            totalEntries = cache.size,
            validEntries = validEntries,
            expiredEntries = expiredEntries,
            maxSize = maxSize,
            ttlHours = TimeUnit.MILLISECONDS.toHours(ttlMillis)
        )
    }

    /**
     * Clean up expired entries.
     * 
     * @return Number of entries removed
     */
    suspend fun cleanup(): Int = mutex.withLock {
        val now = System.currentTimeMillis()
        val entriesToRemove = cache.entries.filter { isExpired(it.value, now) }
        
        entriesToRemove.forEach { (key, _) ->
            cache.remove(key)
        }
        
        Log.d(TAG, "🧹 Cleaned up ${entriesToRemove.size} expired cache entries")
        return entriesToRemove.size
    }

    /**
     * Generate cache key from fixture ID and prompt hash.
     */
    private fun generateCacheKey(fixtureId: Int, promptHash: String): String {
        return "fixture_${fixtureId}_prompt_${promptHash}"
    }

    /**
     * Check if cache entry is expired.
     */
    private fun isExpired(entry: CacheEntry, currentTime: Long = System.currentTimeMillis()): Boolean {
        return (currentTime - entry.timestamp) > ttlMillis
    }

    /**
     * Enforce cache size limit by removing oldest entries.
     */
    private fun enforceSizeLimit() {
        while (cache.size > maxSize) {
            val oldestKey = cache.keys.firstOrNull()
            if (oldestKey != null) {
                cache.remove(oldestKey)
                Log.d(TAG, "📉 Evicted oldest cache entry to maintain size limit")
            } else {
                break
            }
        }
    }

    /**
     * Cache statistics container.
     */
    data class CacheStats(
        val totalEntries: Int,
        val validEntries: Int,
        val expiredEntries: Int,
        val maxSize: Int,
        val ttlHours: Long
    ) {
        /**
         * Get cache utilization percentage.
         */
        fun getUtilizationPercentage(): Int {
            return if (maxSize > 0) {
                (totalEntries * 100) / maxSize
            } else {
                0
            }
        }
        
        /**
         * Get cache health status.
         */
        fun getHealthStatus(): String {
            return when {
                expiredEntries > totalEntries / 2 -> "⚠️ Needs cleanup"
                getUtilizationPercentage() > 90 -> "⚠️ Near capacity"
                else -> "✅ Healthy"
            }
        }
    }
}

/**
 * Helper for generating prompt hashes for cache keys.
 */
object PromptHashGenerator {
    
    /**
     * Generate a simple hash for a prompt string.
     * Uses basic hash code for simplicity.
     * 
     * @param prompt The prompt string to hash
     * @return Hash string
     */
    fun generateHash(prompt: String): String {
        return prompt.hashCode().toString()
    }
    
    /**
     * Generate hash for LLMGRADE analysis parameters.
     * 
     * @param fixtureId Fixture ID
     * @param homeTeamName Home team name
     * @param awayTeamName Away team name
     * @param prediction Oracle prediction
     * @param confidence Oracle confidence
     * @return Combined hash string
     */
    fun generateLLMGradeHash(
        fixtureId: Int,
        homeTeamName: String,
        awayTeamName: String,
        prediction: String,
        confidence: Int
    ): String {
        val combinedString = "${fixtureId}_${homeTeamName}_${awayTeamName}_${prediction}_${confidence}"
        return generateHash(combinedString)
    }
}
