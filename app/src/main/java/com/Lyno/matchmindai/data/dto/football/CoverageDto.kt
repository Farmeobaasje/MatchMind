package com.Lyno.matchmindai.data.dto.football

import kotlinx.serialization.Serializable

/**
 * DTO for the coverage object within the League response from API-SPORTS V3.
 * This object indicates what data is available per league and is critical for UI logic.
 * 
 * The coverage object tells us which endpoints are supported for a particular league:
 * - standings: Whether standings data is available
 * - players: Whether player statistics are available  
 * - odds: Whether betting odds are available
 * - predictions: Whether prediction data is available
 * 
 * This information helps the app decide which features to enable/disable per league.
 */
@Serializable
data class CoverageDto(
    val standings: Boolean,
    val players: Boolean,
    val odds: Boolean,
    val predictions: Boolean
)
