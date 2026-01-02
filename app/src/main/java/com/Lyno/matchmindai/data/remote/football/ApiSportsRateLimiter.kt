package com.Lyno.matchmindai.data.remote.football

import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Rate limiter for API-SPORTS API calls to prevent exceeding the 4 requests per second limit.
 * 
 * API-SPORTS subscription limits:
 * - 4 requests per second (250ms minimum between requests)
 * - 60 requests per minute
 * - 1000 requests per day
 * 
 * This rate limiter implements:
 * 1. Request queue with FIFO processing
 * 2. Minimum 250ms interval between requests
 * 3. Exponential backoff for rate limit errors
 * 4. Request prioritization (statistics > predictions > other)
 * 5. Global singleton instance for consistent rate limiting across all API calls
 */
class ApiSportsRateLimiter {
    
    companion object {
        // 🔥 CRITICAL FIX: Updated for API-SPORTS subscription limits
        // Most subscriptions have 10-60 requests per minute limits
        // Using 2 requests per second (120 per minute) as a safe default
        // This prevents "Too many requests per minute" errors
        private const val MIN_REQUEST_INTERVAL_MS = 500L // 2 requests per second (safer)
        private const val REQUESTS_PER_MINUTE_LIMIT = 30 // Conservative limit for basic plans
        private const val INITIAL_BACKOFF_MS = 1000L
        private const val MAX_BACKOFF_MS = 60000L // 60 seconds max backoff for minute limits
        private const val BACKOFF_MULTIPLIER = 2.0
        
        @Volatile
        private var instance: ApiSportsRateLimiter? = null
        
        fun getInstance(): ApiSportsRateLimiter {
            return instance ?: synchronized(this) {
                instance ?: ApiSportsRateLimiter().also { instance = it }
            }
        }
    }
    
    private data class Request(
        val id: String,
        val priority: Int, // Higher number = higher priority
        val execute: suspend () -> Unit,
        val onSuccess: (() -> Unit)? = null,
        val onError: ((Throwable) -> Unit)? = null,
        val retryCount: Int = 0
    )
    
    private val requestQueue = ConcurrentLinkedQueue<Request>()
    private var lastRequestTime = 0L
    private var isProcessing = false
    private var currentBackoffMs = INITIAL_BACKOFF_MS
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Track requests per minute
    private val requestTimestamps = mutableListOf<Long>()
    private val minuteInMillis = 60 * 1000L
    
    // Priority constants
    object Priority {
        const val HIGH = 3    // Statistics (xG data) - most important for predictions
        const val MEDIUM = 2  // Predictions, odds, standings
        const val LOW = 1     // Other data (injuries, fixtures, etc.)
    }
    
    /**
     * Schedule a request with priority.
     * 
     * @param id Unique identifier for the request (for debugging)
     * @param priority Request priority (use Priority constants)
     * @param execute The suspend function to execute
     * @param onSuccess Optional callback on success
     * @param onError Optional callback on error
     */
    fun scheduleRequest(
        id: String,
        priority: Int = Priority.MEDIUM,
        execute: suspend () -> Unit,
        onSuccess: (() -> Unit)? = null,
        onError: ((Throwable) -> Unit)? = null
    ) {
        val request = Request(id, priority, execute, onSuccess, onError)
        requestQueue.add(request)
        processQueue()
    }
    
    /**
     * Execute a request with rate limiting.
     * This is a suspend function that will wait if needed.
     */
    suspend fun <T> executeWithRateLimit(
        id: String,
        priority: Int = Priority.MEDIUM,
        block: suspend () -> T
    ): T {
        return suspendCancellableCoroutine { continuation ->
            scheduleRequest(
                id = id,
                priority = priority,
                execute = {
                    try {
                        val result = block()
                        continuation.resume(result)
                    } catch (e: Throwable) {
                        continuation.resumeWithException(e)
                    }
                },
                onError = { error ->
                    continuation.resumeWithException(error)
                }
            )
        }
    }
    
    /**
     * Process the request queue.
     */
    private fun processQueue() {
        if (isProcessing || requestQueue.isEmpty()) {
            return
        }
        
        isProcessing = true
        
        // Sort requests by priority (higher priority first)
        val sortedRequests = requestQueue.sortedByDescending { it.priority }
        requestQueue.clear()
        requestQueue.addAll(sortedRequests)
        
        val nextRequest = requestQueue.poll() ?: run {
            isProcessing = false
            return
        }
        
        val now = System.currentTimeMillis()
        
        // Check per-minute limit
        cleanupOldTimestamps(now)
        if (requestTimestamps.size >= REQUESTS_PER_MINUTE_LIMIT) {
            // We've hit the per-minute limit, wait before processing next request
            val oldestTimestamp = requestTimestamps.firstOrNull() ?: now
            val timeUntilNextMinute = minuteInMillis - (now - oldestTimestamp)
            val delayNeeded = maxOf(timeUntilNextMinute, MIN_REQUEST_INTERVAL_MS)
            
            Log.w("ApiSportsRateLimiter", 
                "Per-minute limit reached (${requestTimestamps.size}/$REQUESTS_PER_MINUTE_LIMIT). " +
                "Waiting ${delayNeeded}ms before next request.")
            
            Handler(Looper.getMainLooper()).postDelayed({
                executeRequest(nextRequest)
            }, delayNeeded)
            return
        }
        
        // Check minimum interval between requests
        val timeSinceLastRequest = now - lastRequestTime
        val delayNeeded = maxOf(0, MIN_REQUEST_INTERVAL_MS - timeSinceLastRequest)
        
        if (delayNeeded > 0) {
            // Wait before processing next request
            Handler(Looper.getMainLooper()).postDelayed({
                executeRequest(nextRequest)
            }, delayNeeded)
        } else {
            // Execute immediately
            executeRequest(nextRequest)
        }
    }
    
    /**
     * Clean up old timestamps (older than 1 minute).
     */
    private fun cleanupOldTimestamps(currentTime: Long) {
        val oneMinuteAgo = currentTime - minuteInMillis
        requestTimestamps.removeAll { timestamp -> timestamp < oneMinuteAgo }
    }
    
    /**
     * Execute a request with error handling and retry logic.
     */
    private fun executeRequest(request: Request) {
        val now = System.currentTimeMillis()
        
        // Update last request time and track timestamp
        lastRequestTime = now
        requestTimestamps.add(now)
        
        // Execute in coroutine scope
        coroutineScope.launch {
            try {
                request.execute()
                request.onSuccess?.invoke()
                
                // Reset backoff on successful request
                currentBackoffMs = INITIAL_BACKOFF_MS
                
                // Process next request
                isProcessing = false
                processQueue()
                
            } catch (e: FootballApiException) {
                when (e.errorType) {
                    FootballApiErrorType.RATE_LIMIT_ERROR -> {
                        // Rate limit hit - implement exponential backoff
                        handleRateLimitError(request)
                    }
                    FootballApiErrorType.NETWORK_ERROR,
                    FootballApiErrorType.SERVER_ERROR -> {
                        // Network or server error - retry with backoff
                        handleRetryableError(request, e)
                    }
                    else -> {
                        // Other errors - don't retry
                        request.onError?.invoke(e)
                        isProcessing = false
                        processQueue()
                    }
                }
            } catch (e: Exception) {
                // Other exceptions - don't retry
                request.onError?.invoke(e)
                isProcessing = false
                processQueue()
            }
        }
    }
    
    /**
     * Handle rate limit error with exponential backoff.
     */
    private fun handleRateLimitError(request: Request) {
        if (request.retryCount < 3) {
            // Calculate exponential backoff with jitter
            val backoffWithJitter = (currentBackoffMs * (0.8 + Math.random() * 0.4)).toLong()
            val nextBackoff = minOf(backoffWithJitter, MAX_BACKOFF_MS)
            
            Log.w("ApiSportsRateLimiter", 
                "Rate limit hit for request ${request.id}. " +
                "Retry ${request.retryCount + 1}/3 in ${nextBackoff}ms")
            
            // Update backoff for next retry
            currentBackoffMs = (currentBackoffMs * BACKOFF_MULTIPLIER).toLong()
            
            // Requeue with retry count
            val retryRequest = request.copy(retryCount = request.retryCount + 1)
            requestQueue.add(retryRequest)
            
            // Wait before processing next request
            Handler(Looper.getMainLooper()).postDelayed({
                isProcessing = false
                processQueue()
            }, nextBackoff)
        } else {
            // Max retries reached
            Log.e("ApiSportsRateLimiter", 
                "Max retries reached for request ${request.id}. Giving up.")
            request.onError?.invoke(
                FootballApiException(
                    "API rate limit exceeded after 3 retries",
                    FootballApiErrorType.RATE_LIMIT_ERROR
                )
            )
            isProcessing = false
            processQueue()
        }
    }
    
    /**
     * Handle retryable errors (network, server).
     */
    private fun handleRetryableError(request: Request, error: Throwable) {
        if (request.retryCount < 2) {
            val retryDelay = INITIAL_BACKOFF_MS * (request.retryCount + 1)
            
            Log.w("ApiSportsRateLimiter", 
                "${error.javaClass.simpleName} for request ${request.id}. " +
                "Retry ${request.retryCount + 1}/2 in ${retryDelay}ms")
            
            // Requeue with retry count
            val retryRequest = request.copy(retryCount = request.retryCount + 1)
            requestQueue.add(retryRequest)
            
            // Wait before processing next request
            Handler(Looper.getMainLooper()).postDelayed({
                isProcessing = false
                processQueue()
            }, retryDelay)
        } else {
            // Max retries reached
            Log.e("ApiSportsRateLimiter", 
                "Max retries reached for request ${request.id}. Giving up.")
            request.onError?.invoke(error)
            isProcessing = false
            processQueue()
        }
    }
    
    /**
     * Clear all pending requests.
     * Useful when user logs out or app goes to background.
     */
    fun clearQueue() {
        requestQueue.clear()
        isProcessing = false
        currentBackoffMs = INITIAL_BACKOFF_MS
        Log.d("ApiSportsRateLimiter", "Request queue cleared")
    }
    
    /**
     * Get queue statistics for debugging.
     */
    fun getQueueStats(): String {
        val highPriority = requestQueue.count { it.priority == Priority.HIGH }
        val mediumPriority = requestQueue.count { it.priority == Priority.MEDIUM }
        val lowPriority = requestQueue.count { it.priority == Priority.LOW }
        
        return "Queue: ${requestQueue.size} requests " +
               "(High: $highPriority, Medium: $mediumPriority, Low: $lowPriority)"
    }
    
    /**
     * Get the current queue size.
     */
    fun getQueueSize(): Int = requestQueue.size
    
    /**
     * Check if the rate limiter is currently processing a request.
     */
    fun isProcessing(): Boolean = isProcessing
}
