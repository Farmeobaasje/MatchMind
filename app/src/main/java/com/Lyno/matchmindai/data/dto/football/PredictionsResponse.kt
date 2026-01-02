package com.Lyno.matchmindai.data.dto.football

import kotlinx.serialization.Serializable

/**
 * DTO for the predictions response from API-SPORTS V3.
 * This contains prediction data for a specific fixture including winning percentages,
 * advice, and head-to-head statistics.
 * 
 * This response is crucial for the AI analysis logic as it provides structured
 * data that can be combined with Tavily search results for comprehensive analysis.
 */
@Serializable
data class PredictionsResponse(
    val response: List<PredictionItemDto>
)

/**
 * DTO for individual prediction item in the predictions response.
 * This wraps the prediction data with fixture and team information.
 * Fields are nullable to handle cases where API returns incomplete data.
 * 
 * Actual API structure:
 * {
 *   "predictions": {...},
 *   "league": {...},
 *   "teams": {...},
 *   "comparison": {...},
 *   "h2h": [...]
 * }
 */
@Serializable
data class PredictionItemDto(
    val predictions: PredictionsDto? = null,
    val league: LeagueDetailsDto? = null,
    val teams: TeamsDetailsDto? = null,
    val comparison: ComparisonDto? = null,
    val h2h: List<H2HMatchDto>? = null
)
