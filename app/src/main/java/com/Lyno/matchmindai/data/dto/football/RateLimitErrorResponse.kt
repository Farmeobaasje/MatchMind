package com.Lyno.matchmindai.data.dto.football

import kotlinx.serialization.Serializable

/**
 * DTO for API-Sports rate limit error responses.
 * When the API rate limit is exceeded, it returns a different JSON structure:
 * {
 *   "errors": {
 *     "rateLimit": "Too many requests. You have exceeded the limit of requests per minute..."
 *   },
 *   "parameters": []
 * }
 * 
 * Note: The "parameters" field is an empty array [] instead of an object {},
 * which causes JsonConvertException if we try to parse it as a normal response.
 */
@Serializable
data class RateLimitErrorResponse(
    val errors: Map<String, String> = emptyMap(),
    val parameters: List<String> = emptyList()
) {
    /**
     * Check if this is a rate limit error.
     */
    val isRateLimitError: Boolean
        get() = errors.containsKey("rateLimit") || 
                errors.values.any { it.contains("Too many requests", ignoreCase = true) }
    
    /**
     * Get the rate limit error message.
     */
    val rateLimitMessage: String
        get() = errors["rateLimit"] ?: errors.values.firstOrNull() ?: "Rate limit exceeded"
}
