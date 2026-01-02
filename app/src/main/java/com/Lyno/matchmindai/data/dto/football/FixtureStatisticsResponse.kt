package com.Lyno.matchmindai.data.dto.football

import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * DTO for the fixture statistics response from API-SPORTS V3.
 * This endpoint provides detailed match statistics including Expected Goals (xG),
 * shots, possession, passes, and other key metrics.
 * 
 * Available statistics include:
 * - Shots on Goal, Shots off Goal, Total Shots
 * - Expected Goals (xG) - CRITICAL FOR ENHANCED PREDICTIONS
 * - Ball Possession, Total passes, Passes accurate
 * - Fouls, Corner Kicks, Offsides
 * - Yellow Cards, Red Cards, Goalkeeper Saves
 * 
 * Update Frequency: Every minute for in-progress fixtures, otherwise 1 call per day
 * Recommended Calls: 1 call every minute for teams with fixtures in progress
 * 
 * IMPORTANT FIX: The API returns "parameters": [] (empty array) for rate limit errors,
 * but "parameters": {...} (object) for successful responses. We handle both cases.
 */
@Serializable
data class FixtureStatisticsResponse(
    val get: String? = null,
    val parameters: JsonElement? = null,
    val errors: List<String>? = null,
    val results: Int? = null,
    val paging: Paging? = null,
    val response: List<TeamStatistics>? = null
) {
    /**
     * Check if this is a rate limit error response.
     * Rate limit errors have "parameters" as empty array [] and "errors" list.
     */
    val isRateLimitError: Boolean
        get() = parameters?.jsonArray != null && errors?.isNotEmpty() == true
    
    /**
     * Get parsed parameters as FixtureStatisticsParameters if available.
     * Returns null if parameters is an array (rate limit error) or missing.
     */
    fun getParsedParameters(): FixtureStatisticsParameters? {
        return try {
            parameters?.jsonObject?.let { jsonObj ->
                // Try to parse as FixtureStatisticsParameters
                val fixture = jsonObj["fixture"]?.jsonPrimitive?.content
                val team = jsonObj["team"]?.jsonPrimitive?.content
                val type = jsonObj["type"]?.jsonPrimitive?.content
                val half = jsonObj["half"]?.jsonPrimitive?.content
                
                if (fixture != null || team != null || type != null || half != null) {
                    FixtureStatisticsParameters(fixture, team, type, half)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Parameters for fixture statistics request.
 */
@Serializable
data class FixtureStatisticsParameters(
    val fixture: String? = null,
    val team: String? = null,
    val type: String? = null,
    val half: String? = null
)

/**
 * Team statistics for a fixture.
 * Reusing the existing TeamStatistics class from MatchDetailsResponse.kt
 * which contains team and statistics list.
 * 
 * Note: The statistics list contains Statistic objects with type and value fields.
 * Expected Goals (xG) is typically under type "Expected Goals" or similar.
 * We need to extract the xG value for enhanced predictions.
 */
// TeamStatistics and Statistic classes are already defined in MatchDetailsResponse.kt
// We'll import and reuse them

/**
 * Helper extension to extract Expected Goals (xG) value from statistics.
 */
fun TeamStatistics?.extractExpectedGoals(): Double? {
    // Try multiple possible field names for xG data
    val possibleFieldNames = listOf(
        "expected_goals",  // API-SPORTS standard field name
        "Expected Goals",  // Alternative format
        "xG",              // Short form
        "expected goals",  // Lowercase with space
        "Expected goals"   // Mixed case
    )
    
    for (fieldName in possibleFieldNames) {
        val xgStatistic = this?.statistics?.find { stat ->
            stat.type?.equals(fieldName, ignoreCase = true) == true
        }
        
        if (xgStatistic != null) {
            val xgValue = extractValueFromStatistic(xgStatistic)
            if (xgValue != null) {
                android.util.Log.d("XG_EXTRACT", "✅ Found xG data using field name '$fieldName': $xgValue")
                return xgValue
            }
        }
    }
    
    // If no xG found, log available statistics for debugging
    val availableStats = this?.statistics?.mapNotNull { it.type } ?: emptyList()
    android.util.Log.w("XG_EXTRACT", "❌ No xG data found. Available statistics: ${availableStats.joinToString(", ")}")
    return null
}

/**
 * Helper extension to extract a specific statistic value by type.
 */
fun TeamStatistics?.extractStatistic(type: String): Double? {
    val statistic = this?.statistics?.find { stat ->
        stat.type?.equals(type, ignoreCase = true) == true
    }
    
    return extractValueFromStatistic(statistic)
}

/**
 * Helper function to extract value from a statistic.
 */
private fun extractValueFromStatistic(statistic: Statistic?): Double? {
    val value = statistic?.value
    return try {
        // Handle JsonElement conversion properly
        when (value) {
            is kotlinx.serialization.json.JsonPrimitive -> {
                val rawValue = value.content
                val xgValue = rawValue.toDoubleOrNull()
                
                if (xgValue != null) {
                    android.util.Log.d("XG_EXTRACT", "✅ Successfully parsed xG value: $xgValue from raw: '$rawValue'")
                } else {
                    android.util.Log.w("XG_EXTRACT", "⚠️ Failed to parse xG value from raw: '$rawValue'")
                }
                
                xgValue
            }
            else -> {
                // Fallback: try to convert to string then to double
                val rawValue = value?.toString()?.trim('"', '\'')
                val xgValue = rawValue?.toDoubleOrNull()
                
                if (xgValue != null) {
                    android.util.Log.d("XG_EXTRACT", "✅ Fallback parsing successful: $xgValue from raw: '$rawValue'")
                } else {
                    android.util.Log.w("XG_EXTRACT", "⚠️ Fallback parsing failed for value: $value")
                }
                
                xgValue
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("XG_EXTRACT", "❌ Error extracting xG data", e)
        null
    }
}
