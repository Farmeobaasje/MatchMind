package com.Lyno.matchmindai.data.dto.football

import kotlinx.serialization.Serializable

/**
 * DTO for predictions data from API-SPORTS V3.
 * This contains the core prediction information including winning percentages,
 * advice, winner prediction, and goals predictions.
 * 
 * This data is crucial for the AI analysis logic as it provides structured
 * quantitative and qualitative data about match outcomes.
 * 
 * Actual API structure:
 * {
 *   "winner": {"id": 1189, "name": "Deportivo Santani", "comment": "Win or draw"},
 *   "win_or_draw": true,
 *   "under_over": "-3.5",
 *   "goals": {"home": "-2.5", "away": "-1.5"},
 *   "advice": "Combo Double chance...",
 *   "percent": {"home": "45%", "draw": "45%", "away": "10%"}
 * }
 */
@Serializable
data class PredictionsDto(
    val winner: WinnerDto? = null,
    @kotlinx.serialization.SerialName("win_or_draw")
    val winOrDraw: Boolean? = null,
    @kotlinx.serialization.SerialName("under_over")
    val underOver: String? = null,
    val goals: PredictionGoalsDto? = null,
    val advice: String? = null,
    val percent: WinningPercentDto? = null,
    @kotlinx.serialization.SerialName("winning_percent")
    val winningPercent: WinningPercentDto? = null
) {
    /**
     * Get the winning percentages, preferring 'percent' over 'winning_percent'.
     */
    val winningPercentData: WinningPercentDto?
        get() = percent ?: winningPercent
}
