package com.Lyno.matchmindai.data.dto.football

import kotlinx.serialization.Serializable

/**
 * DTO for head-to-head statistics in the predictions response from API-SPORTS V3.
 * This contains historical match data between the two teams.
 * 
 * H2H data is important for the AI analysis as it provides context about
 * historical performance and rivalries between teams.
 */
@Serializable
data class HeadToHeadDto(
    val played: HeadToHeadStatsDto? = null,
    val wins: HeadToHeadStatsDto? = null,
    val draws: HeadToHeadStatsDto? = null,
    val loses: HeadToHeadStatsDto? = null
)

/**
 * DTO for head-to-head statistics breakdown.
 */
@Serializable
data class HeadToHeadStatsDto(
    val home: Int? = null,
    val away: Int? = null,
    val total: Int? = null
)
