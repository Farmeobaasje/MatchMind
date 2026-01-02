package com.Lyno.matchmindai.data.dto.football

import kotlinx.serialization.Serializable

/**
 * DTO for fixture details from RapidAPI Football API.
 * 
 * Note: For injuries API, the 'status' field may be missing.
 * For other endpoints (fixtures, predictions), status is present.
 */
@Serializable
data class FixtureDetailsDto(
    val id: Int,
    val date: String,
    val status: StatusDto? = null
)
