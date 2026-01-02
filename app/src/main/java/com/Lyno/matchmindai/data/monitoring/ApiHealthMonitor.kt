package com.Lyno.matchmindai.data.monitoring

import android.content.Context
import android.util.Log
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
import java.util.concurrent.atomic.AtomicInteger

val Context.apiHealthDataStore by preferencesDataStore(name = "api_health_monitor")

/**
 * API Health status enumeration.
 */
enum class ApiHealthStatus {
    HEALTHY,      // API is responding normally
    DEGRADED,     // API is responding but with errors
    UNHEALTHY,    // API is frequently failing
    OFFLINE       // API is completely offline
}

/**
 * API Health metrics for monitoring.
 */
@Serializable
data class ApiHealthMetrics(
    val apiName: String,
    val totalRequests: Int,
    val successfulRequests: Int,
    val failedRequests: Int,
    val lastErrorCode: Int? = null,
    val lastErrorMessage: String? = null,
    val lastSuccessTime: Long? = null,
    val lastFailureTime: Long? = null,
    val averageResponseTime: Long = 0,
    val healthScore: Int = 100, // 0-100 score
    val status: ApiHealthStatus = ApiHealthStatus.HEALTHY
) {
    /**
     * Calculate success rate as percentage.
     */
    val successRate: Double
        get() = if (totalRequests > 0) {
            (successfulRequests.toDouble() / totalRequests) * 100
        } else {
            100.0
        }
    
    /**
     * Calculate failure rate as percentage.
     */
    val failureRate: Double
        get() = if (totalRequests > 0) {
            (failedRequests.toDouble() / totalRequests) * 100
        } else {
            0.0
        }
    
    /**
     * Returns a formatted string for logging.
     */
    fun toLogString(): String {
        return "ApiHealthMetrics(api=$apiName, status=$status, successRate=${"%.1f".format(successRate)}%, " +
               "total=$totalRequests, success=$successfulRequests, failed=$failedRequests, " +
               "healthScore=$healthScore)"
    }
}

/**
 * API Health Monitor for tracking API performance and health.
 * 
 * Features:
 * - Tracks request success/failure rates
 * - Calculates health scores
 * - Determines API status (HEALTHY, DEGRADED, UNHEALTHY, OFFLINE)
 * - Persistent storage using DataStore
 * - In-memory caching for performance
 * - Automatic status calculation based on thresholds
 */
class ApiHealthMonitor(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }
    
    // In-memory cache for fast access (thread-safe)
    private val memoryCache = ConcurrentHashMap<String, ApiHealthMetrics>()
    
    // Request counters for real-time tracking
    private val requestCounters = ConcurrentHashMap<String, AtomicInteger>()
    
    companion object {
        private const val TAG = "ApiHealthMonitor"
        
        // DataStore keys
        private fun apiKey(apiName: String) = stringPreferencesKey("api_health_$apiName")
        
        // Health thresholds
        private const val HEALTHY_THRESHOLD = 95.0    // 95%+ success rate
        private const val DEGRADED_THRESHOLD = 80.0   // 80-95% success rate
        private const val UNHEALTHY_THRESHOLD = 50.0  // 50-80% success rate
        private const val OFFLINE_THRESHOLD = 0.0     // Below 50% success rate
        
        // Time window for status calculation (24 hours)
        private const val STATUS_WINDOW_MS = 24 * 60 * 60 * 1000L
        
        // Known APIs to monitor
        val KNOWN_APIS = listOf(
            "API-SPORTS",
            "DeepSeek",
            "FootballApi",
            "NewsApi",
            "RSS"
        )
    }

    /**
     * Record a successful API request.
     * 
     * @param apiName The name of the API
     * @param responseTime The response time in milliseconds
     */
    suspend fun recordSuccess(apiName: String, responseTime: Long = 0) {
        Log.d(TAG, "Recording success for API: $apiName, responseTime: ${responseTime}ms")
        
        // Update in-memory counter
        val counter = requestCounters.getOrPut(apiName) { AtomicInteger(0) }
        counter.incrementAndGet()
        
        // Load current metrics
        val currentMetrics = getMetrics(apiName)
        
        // Update metrics
        val updatedMetrics = currentMetrics.copy(
            totalRequests = currentMetrics.totalRequests + 1,
            successfulRequests = currentMetrics.successfulRequests + 1,
            lastSuccessTime = System.currentTimeMillis(),
            averageResponseTime = calculateNewAverage(
                currentAverage = currentMetrics.averageResponseTime,
                currentCount = currentMetrics.totalRequests,
                newValue = responseTime
            )
        ).recalculateHealth()
        
        // Save updated metrics
        saveMetrics(apiName, updatedMetrics)
        
        Log.d(TAG, "Success recorded for $apiName. New success rate: ${"%.1f".format(updatedMetrics.successRate)}%")
    }

    /**
     * Record a failed API request.
     * 
     * @param apiName The name of the API
     * @param errorCode The HTTP error code (if available)
     * @param errorMessage The error message
     */
    suspend fun recordFailure(apiName: String, errorCode: Int? = null, errorMessage: String? = null) {
        Log.w(TAG, "Recording failure for API: $apiName, errorCode: $errorCode, message: $errorMessage")
        
        // Update in-memory counter
        val counter = requestCounters.getOrPut(apiName) { AtomicInteger(0) }
        counter.incrementAndGet()
        
        // Load current metrics
        val currentMetrics = getMetrics(apiName)
        
        // Update metrics
        val updatedMetrics = currentMetrics.copy(
            totalRequests = currentMetrics.totalRequests + 1,
            failedRequests = currentMetrics.failedRequests + 1,
            lastErrorCode = errorCode,
            lastErrorMessage = errorMessage,
            lastFailureTime = System.currentTimeMillis()
        ).recalculateHealth()
        
        // Save updated metrics
        saveMetrics(apiName, updatedMetrics)
        
        Log.w(TAG, "Failure recorded for $apiName. New failure rate: ${"%.1f".format(updatedMetrics.failureRate)}%")
    }

    /**
     * Get health metrics for an API.
     * 
     * @param apiName The name of the API
     * @return Current health metrics
     */
    suspend fun getMetrics(apiName: String): ApiHealthMetrics {
        // Check in-memory cache first
        memoryCache[apiName]?.let { return it }
        
        // Load from DataStore
        val storedMetrics = loadFromDataStore(apiName)
        if (storedMetrics != null) {
            memoryCache[apiName] = storedMetrics
            return storedMetrics
        }
        
        // Return default metrics if not found
        return ApiHealthMetrics(
            apiName = apiName,
            totalRequests = 0,
            successfulRequests = 0,
            failedRequests = 0
        )
    }

    /**
     * Get health status for an API.
     * 
     * @param apiName The name of the API
     * @return Current health status
     */
    suspend fun getStatus(apiName: String): ApiHealthStatus {
        return getMetrics(apiName).status
    }

    /**
     * Get health score for an API (0-100).
     * 
     * @param apiName The name of the API
     * @return Health score (higher is better)
     */
    suspend fun getHealthScore(apiName: String): Int {
        return getMetrics(apiName).healthScore
    }

    /**
     * Check if an API is healthy.
     * 
     * @param apiName The name of the API
     * @return True if API is HEALTHY or DEGRADED (still usable)
     */
    suspend fun isApiHealthy(apiName: String): Boolean {
        val status = getStatus(apiName)
        return status == ApiHealthStatus.HEALTHY || status == ApiHealthStatus.DEGRADED
    }

    /**
     * Check if an API is degraded but still usable.
     * 
     * @param apiName The name of the API
     * @return True if API is DEGRADED
     */
    suspend fun isApiDegraded(apiName: String): Boolean {
        return getStatus(apiName) == ApiHealthStatus.DEGRADED
    }

    /**
     * Check if an API is unhealthy (should use fallbacks).
     * 
     * @param apiName The name of the API
     * @return True if API is UNHEALTHY or OFFLINE
     */
    suspend fun isApiUnhealthy(apiName: String): Boolean {
        val status = getStatus(apiName)
        return status == ApiHealthStatus.UNHEALTHY || status == ApiHealthStatus.OFFLINE
    }

    /**
     * Get all API metrics.
     * 
     * @return Map of API name to health metrics
     */
    suspend fun getAllMetrics(): Map<String, ApiHealthMetrics> {
        val metrics = mutableMapOf<String, ApiHealthMetrics>()
        
        KNOWN_APIS.forEach { apiName ->
            metrics[apiName] = getMetrics(apiName)
        }
        
        return metrics
    }

    /**
     * Get overall system health.
     * 
     * @return True if all critical APIs are healthy
     */
    suspend fun getSystemHealth(): Boolean {
        val criticalApis = listOf("API-SPORTS", "DeepSeek")
        
        return criticalApis.all { apiName ->
            isApiHealthy(apiName)
        }
    }

    /**
     * Reset metrics for an API.
     * 
     * @param apiName The name of the API
     */
    suspend fun resetMetrics(apiName: String) {
        Log.d(TAG, "Resetting metrics for API: $apiName")
        
        val defaultMetrics = ApiHealthMetrics(
            apiName = apiName,
            totalRequests = 0,
            successfulRequests = 0,
            failedRequests = 0
        )
        saveMetrics(apiName, defaultMetrics)
        
        // Clear in-memory cache
        memoryCache.remove(apiName)
        requestCounters.remove(apiName)
    }

    /**
     * Reset all metrics.
     */
    suspend fun resetAllMetrics() {
        Log.d(TAG, "Resetting all API metrics")
        
        KNOWN_APIS.forEach { apiName ->
            resetMetrics(apiName)
        }
    }

    /**
     * Get API usage statistics.
     * 
     * @return Map of API name to request count
     */
    fun getUsageStatistics(): Map<String, Int> {
        return requestCounters.mapValues { (_, counter) -> counter.get() }
    }

    /**
     * Log current health status for all APIs.
     */
    suspend fun logHealthStatus() {
        Log.d(TAG, "=== API Health Status Report ===")
        
        val allMetrics = getAllMetrics()
        allMetrics.forEach { (apiName, metrics) ->
            Log.d(TAG, metrics.toLogString())
        }
        
        val systemHealth = getSystemHealth()
        Log.d(TAG, "System Health: ${if (systemHealth) "✅ HEALTHY" else "❌ UNHEALTHY"}")
        Log.d(TAG, "================================")
    }

    // Private helper methods
    
    private suspend fun saveMetrics(apiName: String, metrics: ApiHealthMetrics) {
        try {
            val jsonString = json.encodeToString(metrics)
            context.apiHealthDataStore.edit { preferences ->
                preferences[apiKey(apiName)] = jsonString
            }
            memoryCache[apiName] = metrics
        } catch (e: Exception) {
            Log.e(TAG, "Error saving metrics for API $apiName", e)
        }
    }
    
    private suspend fun loadFromDataStore(apiName: String): ApiHealthMetrics? {
        return try {
            val preferences = context.apiHealthDataStore.data.first()
            val jsonString = preferences[apiKey(apiName)]
            jsonString?.let { json.decodeFromString<ApiHealthMetrics>(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading metrics for API $apiName", e)
            null
        }
    }
    
    private fun calculateNewAverage(currentAverage: Long, currentCount: Int, newValue: Long): Long {
        if (currentCount == 0) return newValue
        return ((currentAverage * currentCount) + newValue) / (currentCount + 1)
    }
    
    /**
     * Extension function to recalculate health score and status.
     */
    private fun ApiHealthMetrics.recalculateHealth(): ApiHealthMetrics {
        // Calculate health score (0-100)
        val score = when {
            totalRequests == 0 -> 100
            else -> {
                val baseScore = (successRate / 100.0 * 90).toInt() // 90% weight for success rate
                val timePenalty = calculateTimePenalty()
                (baseScore - timePenalty).coerceIn(0, 100)
            }
        }
        
        // Determine status based on success rate
        val status = when {
            successRate >= HEALTHY_THRESHOLD -> ApiHealthStatus.HEALTHY
            successRate >= DEGRADED_THRESHOLD -> ApiHealthStatus.DEGRADED
            successRate >= UNHEALTHY_THRESHOLD -> ApiHealthStatus.UNHEALTHY
            else -> ApiHealthStatus.OFFLINE
        }
        
        return copy(
            healthScore = score,
            status = status
        )
    }
    
    /**
     * Calculate time penalty based on recent failures.
     */
    private fun ApiHealthMetrics.calculateTimePenalty(): Int {
        lastFailureTime ?: return 0
        
        val timeSinceFailure = System.currentTimeMillis() - lastFailureTime
        if (timeSinceFailure > STATUS_WINDOW_MS) {
            // Failure was more than 24 hours ago, no penalty
            return 0
        }
        
        // Calculate penalty based on how recent the failure was
        val recencyFactor = 1.0 - (timeSinceFailure.toDouble() / STATUS_WINDOW_MS)
        return (recencyFactor * 10).toInt() // 0-10 penalty
    }
}

/**
 * Extension function for easy API health monitoring in repositories.
 */
suspend fun ApiHealthMonitor.monitorApiCall(
    apiName: String,
    apiCall: suspend () -> Unit,
    onSuccess: (() -> Unit)? = null,
    onFailure: ((Throwable) -> Unit)? = null
) {
    val startTime = System.currentTimeMillis()
    
    try {
        apiCall()
        val responseTime = System.currentTimeMillis() - startTime
        recordSuccess(apiName, responseTime)
        onSuccess?.invoke()
    } catch (e: Exception) {
        // Extract error code from exception if available
        val errorCode = extractErrorCode(e)
        recordFailure(apiName, errorCode, e.message)
        onFailure?.invoke(e)
        throw e
    }
}

/**
 * Extension function for API calls that return a result.
 */
suspend fun <T> ApiHealthMonitor.monitorApiCallWithResult(
    apiName: String,
    apiCall: suspend () -> T,
    onSuccess: ((T) -> Unit)? = null,
    onFailure: ((Throwable) -> Unit)? = null
): T {
    val startTime = System.currentTimeMillis()
    
    return try {
        val result = apiCall()
        val responseTime = System.currentTimeMillis() - startTime
        recordSuccess(apiName, responseTime)
        onSuccess?.invoke(result)
        result
    } catch (e: Exception) {
        // Extract error code from exception if available
        val errorCode = extractErrorCode(e)
        recordFailure(apiName, errorCode, e.message)
        onFailure?.invoke(e)
        throw e
    }
}

/**
 * Helper function to extract error code from exception.
 */
private fun extractErrorCode(e: Exception): Int? {
    return try {
        // Try to get status code via reflection if the field exists
        val statusCodeField = e::class.java.getDeclaredField("statusCode")
        statusCodeField.isAccessible = true
        statusCodeField.get(e) as? Int
    } catch (ex: Exception) {
        null
    }
}
