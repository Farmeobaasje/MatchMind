package com.Lyno.matchmindai.data.dto.football

import kotlinx.serialization.Serializable

/**
 * DTO for teams details from RapidAPI Football API.
 */
@Serializable
data class TeamsDetailsDto(
    val home: TeamDto,
    val away: TeamDto
)
