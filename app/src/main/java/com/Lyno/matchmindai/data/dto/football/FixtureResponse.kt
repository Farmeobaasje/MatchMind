    package com.Lyno.matchmindai.data.dto.football

import kotlinx.serialization.Serializable

/**
 * DTO for the full fixture response from RapidAPI Football API.
 */
@Serializable
data class FixtureResponse(
    val response: List<FixtureItemDto>
)
