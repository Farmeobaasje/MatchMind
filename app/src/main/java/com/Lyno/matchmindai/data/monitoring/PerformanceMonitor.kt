package com.Lyno.matchmindai.data.monitoring

import android.util.Log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.TimeUnit

/**
 * Performance monitoring system for tracking API response times and system performance.
 * Helps identify bottlenecks and optimize response times.
 */
class PerformanceMonitor {

    companion object {
        private const val TAG = "PerformanceMonitor"
        private const val SLOW_THRESHOLD_MS = 3000L  // 3 seconds
        private const val VERY_SLOW_THRESHOLD_MS = 6000L  // 6 seconds
        private const val MAX_ENTRIES_PER_ENDPOINT = 100
    }

    data class PerformanceEntry(
        val endpoint: String,
        val durationMs: Long,
        val timestamp: Long,
        val success: Boolean,
        val errorMessage: String? = null,
        val metadata: Map<String, Any> = emptyMap()
    )

    private val performanceLog = mutableListOf<PerformanceEntry>()
    private val mutex = Mutex()
    private val endpointStats = mutableMapOf<String, EndpointStats>()

    /**
     * Record performance of an operation.
     * 
     * @param endpoint Endpoint or operation name
     * @param durationMs Duration in milliseconds
     * @param success Whether the operation succeeded
     * @param errorMessage Error message if failed
     * @param metadata Additional metadata for analysis
     */
    suspend fun recordPerformance(
        endpoint: String,
        durationMs: Long,
        success: Boolean = true,
        errorMessage: String? = null,
        metadata: Map<String, Any> = emptyMap()
    ) = mutex.withLock {
        val entry = PerformanceEntry(
            endpoint = endpoint,
            durationMs = durationMs,
            timestamp = System.currentTimeMillis(),
            success = success,
            errorMessage = errorMessage,
            metadata = metadata
        )
        
        performanceLog.add(entry)
        
        // Update endpoint statistics
        val stats = endpointStats.getOrPut(endpoint) { EndpointStats(endpoint) }
        stats.update(durationMs, success)
        
        // Log slow operations
        if (durationMs > SLOW_THRESHOLD_MS) {
            val severity = if (durationMs > VERY_SLOW_THRESHOLD_MS) "🚨 VERY SLOW" else "⚠️ SLOW"
            Log.w(TAG, "$severity: $endpoint took ${durationMs}ms (${metadata["fixtureId"] ?: "no fixture"})")
        }
        
        // Clean up old entries
        cleanupOldEntries()
    }

    /**
     * Start timing an operation and return a TimingContext.
     * 
     * @param endpoint Endpoint or operation name
     * @param metadata Additional metadata for analysis
     * @return TimingContext that can be used to record the result
     */
    suspend fun startTiming(
        endpoint: String,
        metadata: Map<String, Any> = emptyMap()
    ): TimingContext {
        val startTime = System.currentTimeMillis()
        return TimingContext(this, endpoint, startTime, metadata)
    }

    /**
     * Get performance statistics for an endpoint.
     * 
     * @param endpoint Endpoint name
     * @return EndpointStats or null if no data
     */
    suspend fun getEndpointStats(endpoint: String): EndpointStats? = mutex.withLock {
        endpointStats[endpoint]
    }

    /**
     * Get all endpoint statistics.
     */
    suspend fun getAllEndpointStats(): List<EndpointStats> = mutex.withLock {
        endpointStats.values.toList()
    }

    /**
     * Get recent performance entries.
     * 
     * @param limit Maximum number of entries to return
     * @return List of recent performance entries
     */
    suspend fun getRecentEntries(limit: Int = 50): List<PerformanceEntry> = mutex.withLock {
        performanceLog.takeLast(limit)
    }

    /**
     * Get slow operations (above threshold).
     * 
     * @param thresholdMs Threshold in milliseconds
     * @param limit Maximum number of entries to return
     * @return List of slow performance entries
     */
    suspend fun getSlowOperations(thresholdMs: Long = SLOW_THRESHOLD_MS, limit: Int = 20): List<PerformanceEntry> = mutex.withLock {
        performanceLog.filter { it.durationMs > thresholdMs }.takeLast(limit)
    }

    /**
     * Get error rate for an endpoint.
     * 
     * @param endpoint Endpoint name
     * @param timeWindowMs Time window in milliseconds (default: 1 hour)
     * @return Error rate as percentage (0-100)
     */
    suspend fun getErrorRate(endpoint: String, timeWindowMs: Long = TimeUnit.HOURS.toMillis(1)): Double = mutex.withLock {
        val cutoffTime = System.currentTimeMillis() - timeWindowMs
        val relevantEntries = performanceLog.filter { 
            it.endpoint == endpoint && it.timestamp > cutoffTime 
        }
        
        if (relevantEntries.isEmpty()) return 0.0
        
        val errorCount = relevantEntries.count { !it.success }
        return (errorCount.toDouble() / relevantEntries.size) * 100
    }

    /**
     * Get average response time for an endpoint.
     * 
     * @param endpoint Endpoint name
     * @param timeWindowMs Time window in milliseconds (default: 1 hour)
     * @return Average response time in milliseconds
     */
    suspend fun getAverageResponseTime(endpoint: String, timeWindowMs: Long = TimeUnit.HOURS.toMillis(1)): Double = mutex.withLock {
        val cutoffTime = System.currentTimeMillis() - timeWindowMs
        val relevantEntries = performanceLog.filter { 
            it.endpoint == endpoint && it.timestamp > cutoffTime && it.success 
        }
        
        if (relevantEntries.isEmpty()) return 0.0
        
        return relevantEntries.map { it.durationMs }.average()
    }

    /**
     * Get performance summary for reporting.
     */
    suspend fun getPerformanceSummary(): PerformanceSummary = mutex.withLock {
        val now = System.currentTimeMillis()
        val oneHourAgo = now - TimeUnit.HOURS.toMillis(1)
        val oneDayAgo = now - TimeUnit.DAYS.toMillis(1)
        
        val recentEntries = performanceLog.filter { it.timestamp > oneHourAgo }
        val dailyEntries = performanceLog.filter { it.timestamp > oneDayAgo }
        
        val slowOperations = recentEntries.filter { it.durationMs > SLOW_THRESHOLD_MS }
        val errorCount = recentEntries.count { !it.success }
        val totalCalls = recentEntries.size
        
        val errorRate = if (totalCalls > 0) (errorCount.toDouble() / totalCalls) * 100 else 0.0
        
        val avgResponseTime = if (recentEntries.isNotEmpty()) {
            recentEntries.map { it.durationMs }.average()
        } else {
            0.0
        }
        
        // Identify bottlenecks
        val bottlenecks = endpointStats.values
            .filter { it.averageResponseTime > SLOW_THRESHOLD_MS }
            .sortedByDescending { it.averageResponseTime }
            .take(5)
            .map { Bottleneck(it.endpoint, it.averageResponseTime, it.callCount) }
        
        PerformanceSummary(
            totalCalls = totalCalls,
            errorRate = errorRate,
            averageResponseTime = avgResponseTime,
            slowOperations = slowOperations.size,
            bottlenecks = bottlenecks,
            timestamp = now
        )
    }

    /**
     * Clear all performance data.
     */
    suspend fun clear() = mutex.withLock {
        performanceLog.clear()
        endpointStats.clear()
        Log.d(TAG, "🧹 Cleared all performance data")
    }

    /**
     * Clean up old entries to prevent memory issues.
     */
    private fun cleanupOldEntries() {
        val oneDayAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)
        performanceLog.removeAll { it.timestamp < oneDayAgo }
        
        // Also limit per endpoint
        val entriesByEndpoint = performanceLog.groupBy { it.endpoint }
        performanceLog.clear()
        
        entriesByEndpoint.forEach { (_, entries) ->
            performanceLog.addAll(entries.takeLast(MAX_ENTRIES_PER_ENDPOINT))
        }
        
        // Sort by timestamp
        performanceLog.sortBy { it.timestamp }
    }

    /**
     * Endpoint statistics container.
     */
    data class EndpointStats(
        val endpoint: String,
        var callCount: Int = 0,
        var successCount: Int = 0,
        var totalResponseTime: Long = 0,
        var minResponseTime: Long = Long.MAX_VALUE,
        var maxResponseTime: Long = 0
    ) {
        val averageResponseTime: Double
            get() = if (callCount > 0) totalResponseTime.toDouble() / callCount else 0.0
        
        val successRate: Double
            get() = if (callCount > 0) (successCount.toDouble() / callCount) * 100 else 0.0
        
        fun update(durationMs: Long, success: Boolean) {
            callCount++
            if (success) successCount++
            totalResponseTime += durationMs
            minResponseTime = minOf(minResponseTime, durationMs)
            maxResponseTime = maxOf(maxResponseTime, durationMs)
        }
        
        fun getHealthStatus(): String {
            return when {
                averageResponseTime > VERY_SLOW_THRESHOLD_MS -> "🚨 Critical"
                averageResponseTime > SLOW_THRESHOLD_MS -> "⚠️ Warning"
                successRate < 90 -> "⚠️ Unreliable"
                else -> "✅ Healthy"
            }
        }
    }

    /**
     * Performance summary container.
     */
    data class PerformanceSummary(
        val totalCalls: Int,
        val errorRate: Double,
        val averageResponseTime: Double,
        val slowOperations: Int,
        val bottlenecks: List<Bottleneck>,
        val timestamp: Long
    ) {
        fun getOverallHealth(): String {
            return when {
                errorRate > 20 -> "🚨 Critical"
                averageResponseTime > VERY_SLOW_THRESHOLD_MS -> "⚠️ Slow"
                slowOperations > 10 -> "⚠️ Warning"
                else -> "✅ Healthy"
            }
        }
    }

    /**
     * Bottleneck identification container.
     */
    data class Bottleneck(
        val endpoint: String,
        val averageResponseTime: Double,
        val callCount: Int
    ) {
        fun getSeverity(): String {
            return when {
                averageResponseTime > VERY_SLOW_THRESHOLD_MS -> "🚨 Critical"
                averageResponseTime > SLOW_THRESHOLD_MS -> "⚠️ High"
                else -> "⚠️ Medium"
            }
        }
    }

    /**
     * Timing context for recording operation duration.
     */
    class TimingContext(
        private val monitor: PerformanceMonitor,
        private val endpoint: String,
        private val startTime: Long,
        private val metadata: Map<String, Any>
    ) {
        /**
         * Record successful completion.
         */
        suspend fun recordSuccess() {
            val duration = System.currentTimeMillis() - startTime
            monitor.recordPerformance(endpoint, duration, true, null, metadata)
        }

        /**
         * Record failure with error message.
         */
        suspend fun recordFailure(errorMessage: String) {
            val duration = System.currentTimeMillis() - startTime
            monitor.recordPerformance(endpoint, duration, false, errorMessage, metadata)
        }

        /**
         * Record with custom success status.
         */
        suspend fun record(success: Boolean, errorMessage: String? = null) {
            val duration = System.currentTimeMillis() - startTime
            monitor.recordPerformance(endpoint, duration, success, errorMessage, metadata)
        }
    }
}

/**
 * Global performance monitor instance.
 */
object GlobalPerformanceMonitor {
    private val monitor = PerformanceMonitor()
    
    /**
     * Get the performance monitor instance.
     */
    fun get(): PerformanceMonitor = monitor
    
    /**
     * Quick method to record performance.
     */
    suspend fun record(
        endpoint: String,
        durationMs: Long,
        success: Boolean = true,
        errorMessage: String? = null,
        metadata: Map<String, Any> = emptyMap()
    ) {
        monitor.recordPerformance(endpoint, durationMs, success, errorMessage, metadata)
    }
    
    /**
     * Quick method to start timing.
     */
    suspend fun startTiming(
        endpoint: String,
        metadata: Map<String, Any> = emptyMap()
    ): PerformanceMonitor.TimingContext {
        return monitor.startTiming(endpoint, metadata)
    }
}
