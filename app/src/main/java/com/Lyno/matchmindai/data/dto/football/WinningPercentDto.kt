package com.Lyno.matchmindai.data.dto.football

import kotlinx.serialization.Serializable

/**
 * DTO for winning percentages in the predictions response from API-SPORTS V3.
 * This contains the probability percentages for home win, draw, and away win.
 * 
 * These percentages are crucial for the AI analysis logic as they provide
 * quantitative data about match outcomes.
 */
@Serializable
data class WinningPercentDto(
    val home: String,
    val draw: String,
    val away: String
)
