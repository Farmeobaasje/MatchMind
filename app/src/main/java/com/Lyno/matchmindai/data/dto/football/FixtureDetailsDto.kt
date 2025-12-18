package com.Lyno.matchmindai.data.dto.football

import kotlinx.serialization.Serializable

/**
 * DTO for fixture details from RapidAPI Football API.
 */
@Serializable
data class FixtureDetailsDto(
    val id: Int,
    val date: String,
    val status: StatusDto
)
