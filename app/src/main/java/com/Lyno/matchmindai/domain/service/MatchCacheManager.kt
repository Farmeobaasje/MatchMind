package com.Lyno.matchmindai.domain.service

import com.Lyno.matchmindai.domain.model.MatchDetail
import com.Lyno.matchmindai.domain.model.MatchPrediction
import com.Lyno.matchmindai.domain.model.MatchPredictionData
import com.Lyno.matchmindai.domain.model.Injury
import com.Lyno.matchmindai.domain.model.OddsData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.ConcurrentHashMap

/**
 * Cache status for match data.
 */
enum class CacheStatus {
    FRESH,      // Data is up-to-date (within TTL)
    STALE,      // Data exists but is expired
    MISSING     // No cached data available
}

/**
 * Cache update operation for partial updates.
 */
sealed class MatchCacheUpdate {
    data class UpdateMatchDetail(val matchDetail: MatchDetail) : MatchCacheUpdate()
    data class UpdatePredictions(val predictions: MatchPredictionData) : MatchCacheUpdate()
    data class UpdateDixonColesPrediction(val dixonColesPrediction: MatchPrediction) : MatchCacheUpdate()
    data class UpdateInjuries(val injuries: List<Injury>) : MatchCacheUpdate()
    data class UpdateOdds(val odds: OddsData) : MatchCacheUpdate()
    data class UpdateAll(
        val matchDetail: MatchDetail,
        val predictions: MatchPredictionData? = null,
        val dixonColesPrediction: MatchPrediction? = null,
        val injuries: List<Injury> = emptyList(),
        val odds: OddsData? = null
    ) : MatchCacheUpdate()
}

/**
 * Complete match data cache container.
 */
data class MatchDetailCacheData(
    val fixtureId: Int,
    val matchDetail: MatchDetail,
    val predictions: MatchPredictionData? = null,
    val dixonColesPrediction: MatchPrediction? = null,
    val injuries: List<Injury> = emptyList(),
    val odds: OddsData? = null,
    val lastUpdated: Long = System.currentTimeMillis(),
    val cacheExpiry: Long = System.currentTimeMillis() + DEFAULT_TTL
) {
    /**
     * Check if cache is still fresh (within TTL).
     */
    val isFresh: Boolean
        get() = System.currentTimeMillis() < cacheExpiry

    /**
     * Get cache status.
     */
    val status: CacheStatus
        get() = when {
            System.currentTimeMillis() < cacheExpiry -> CacheStatus.FRESH
            else -> CacheStatus.STALE
        }

    /**
     * Get time until expiry in seconds.
     */
    val timeUntilExpiry: Long
        get() = maxOf(0, (cacheExpiry - System.currentTimeMillis()) / 1000)

    companion object {
        const val DEFAULT_TTL = 300_000L // 5 minutes
        const val SHORT_TTL = 60_000L    // 1 minute for live matches
        const val LONG_TTL = 900_000L    // 15 minutes for static data
    }
}

/**
 * Interface for managing match data cache.
 */
interface MatchCacheManager {
    /**
     * Cache complete match data.
     */
    suspend fun cacheMatchData(fixtureId: Int, data: MatchDetailCacheData)

    /**
     * Get cached match data.
     */
    suspend fun getCachedMatchData(fixtureId: Int): MatchDetailCacheData?

    /**
     * Update partial match data.
     */
    suspend fun updatePartialData(fixtureId: Int, update: MatchCacheUpdate)

    /**
     * Invalidate cache for specific match.
     */
    suspend fun invalidateMatchCache(fixtureId: Int)

    /**
     * Clear all cache.
     */
    suspend fun clearAllCache()

    /**
     * Get cache status for a match.
     */
    suspend fun getCacheStatus(fixtureId: Int): CacheStatus

    /**
     * Get cache statistics.
     */
    suspend fun getCacheStats(): CacheStats

    /**
     * Observe cache changes for a specific match.
     */
    fun observeMatchCache(fixtureId: Int): Flow<MatchDetailCacheData?>

    /**
     * Get all cached fixture IDs.
     */
    suspend fun getAllCachedFixtureIds(): List<Int>
}

/**
 * Cache statistics.
 */
data class CacheStats(
    val totalCachedMatches: Int,
    val freshMatches: Int,
    val staleMatches: Int,
    val averageCacheAge: Long,
    val memoryUsage: Long
)

/**
 * In-memory implementation of MatchCacheManager.
 */
class InMemoryMatchCacheManager : MatchCacheManager {
    private val cache = ConcurrentHashMap<Int, MatchDetailCacheData>()
    private val cacheObservers = ConcurrentHashMap<Int, MutableStateFlow<MatchDetailCacheData?>>()

    override suspend fun cacheMatchData(fixtureId: Int, data: MatchDetailCacheData) {
        cache[fixtureId] = data
        notifyObservers(fixtureId, data)
    }

    override suspend fun getCachedMatchData(fixtureId: Int): MatchDetailCacheData? {
        return cache[fixtureId]
    }

    override suspend fun updatePartialData(fixtureId: Int, update: MatchCacheUpdate) {
        val current = cache[fixtureId]
        val updated = when (update) {
            is MatchCacheUpdate.UpdateMatchDetail -> {
                current?.copy(
                    matchDetail = update.matchDetail,
                    lastUpdated = System.currentTimeMillis()
                ) ?: MatchDetailCacheData(
                    fixtureId = fixtureId,
                    matchDetail = update.matchDetail,
                    lastUpdated = System.currentTimeMillis()
                )
            }
            is MatchCacheUpdate.UpdatePredictions -> {
                current?.copy(
                    predictions = update.predictions,
                    lastUpdated = System.currentTimeMillis()
                ) ?: throw IllegalStateException("Cannot update predictions without match detail")
            }
            is MatchCacheUpdate.UpdateDixonColesPrediction -> {
                current?.copy(
                    dixonColesPrediction = update.dixonColesPrediction,
                    lastUpdated = System.currentTimeMillis()
                ) ?: throw IllegalStateException("Cannot update Dixon-Coles prediction without match detail")
            }
            is MatchCacheUpdate.UpdateInjuries -> {
                current?.copy(
                    injuries = update.injuries,
                    lastUpdated = System.currentTimeMillis()
                ) ?: throw IllegalStateException("Cannot update injuries without match detail")
            }
            is MatchCacheUpdate.UpdateOdds -> {
                current?.copy(
                    odds = update.odds,
                    lastUpdated = System.currentTimeMillis()
                ) ?: throw IllegalStateException("Cannot update odds without match detail")
            }
            is MatchCacheUpdate.UpdateAll -> {
                MatchDetailCacheData(
                    fixtureId = fixtureId,
                    matchDetail = update.matchDetail,
                    predictions = update.predictions,
                    dixonColesPrediction = update.dixonColesPrediction,
                    injuries = update.injuries,
                    odds = update.odds,
                    lastUpdated = System.currentTimeMillis()
                )
            }
        }
        
        cache[fixtureId] = updated
        notifyObservers(fixtureId, updated)
    }

    override suspend fun invalidateMatchCache(fixtureId: Int) {
        cache.remove(fixtureId)
        notifyObservers(fixtureId, null)
    }

    override suspend fun clearAllCache() {
        cache.clear()
        cacheObservers.values.forEach { observer ->
            observer.value = null
        }
    }

    override suspend fun getCacheStatus(fixtureId: Int): CacheStatus {
        val data = cache[fixtureId]
        return when {
            data == null -> CacheStatus.MISSING
            data.isFresh -> CacheStatus.FRESH
            else -> CacheStatus.STALE
        }
    }

    override suspend fun getCacheStats(): CacheStats {
        val now = System.currentTimeMillis()
        val cachedMatches = cache.values.toList()
        
        return CacheStats(
            totalCachedMatches = cachedMatches.size,
            freshMatches = cachedMatches.count { it.isFresh },
            staleMatches = cachedMatches.count { !it.isFresh },
            averageCacheAge = if (cachedMatches.isNotEmpty()) {
                cachedMatches.map { now - it.lastUpdated }.average().toLong()
            } else 0,
            memoryUsage = estimateMemoryUsage()
        )
    }

    override fun observeMatchCache(fixtureId: Int): Flow<MatchDetailCacheData?> {
        return cacheObservers.getOrPut(fixtureId) {
            MutableStateFlow(cache[fixtureId])
        }.asStateFlow()
    }

    override suspend fun getAllCachedFixtureIds(): List<Int> {
        return cache.keys.toList()
    }

    private fun notifyObservers(fixtureId: Int, data: MatchDetailCacheData?) {
        val observer = cacheObservers[fixtureId]
        observer?.value = data
    }

    private fun estimateMemoryUsage(): Long {
        // Rough estimation: each cache entry ~ 1KB
        return cache.size * 1024L
    }
}

/**
 * Smart cache utility functions.
 */
object CacheUtils {
    /**
     * Determine TTL based on match status.
     */
    fun determineTTL(matchDetail: MatchDetail): Long {
        return when (matchDetail.status?.name) {
            "1H", "2H", "HT", "ET", "P" -> MatchDetailCacheData.SHORT_TTL // Live match
            "FT", "AET", "PEN" -> MatchDetailCacheData.LONG_TTL // Finished match
            else -> MatchDetailCacheData.DEFAULT_TTL // Upcoming match
        }
    }

    /**
     * Check if data should be refreshed based on cache status and match importance.
     */
    fun shouldRefresh(
        cacheData: MatchDetailCacheData?,
        isUserRequested: Boolean = false,
        matchImportance: Int = 1 // 1-5 scale
    ): Boolean {
        return when {
            cacheData == null -> true
            isUserRequested -> true
            !cacheData.isFresh && matchImportance >= 3 -> true
            cacheData.status == CacheStatus.STALE && matchImportance >= 4 -> true
            else -> false
        }
    }

    /**
     * Merge cached data with fresh data.
     */
    fun mergeCacheData(
        cached: MatchDetailCacheData,
        fresh: MatchDetailCacheData
    ): MatchDetailCacheData {
        return cached.copy(
            matchDetail = fresh.matchDetail,
            predictions = fresh.predictions ?: cached.predictions,
            dixonColesPrediction = fresh.dixonColesPrediction ?: cached.dixonColesPrediction,
            injuries = fresh.injuries.ifEmpty { cached.injuries },
            odds = fresh.odds ?: cached.odds,
            lastUpdated = System.currentTimeMillis(),
            cacheExpiry = determineTTL(fresh.matchDetail)
        )
    }
}
