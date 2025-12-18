package com.Lyno.matchmindai.data.dto.football

import kotlinx.serialization.Serializable

/**
 * DTO for team information from RapidAPI Football API.
 */
@Serializable
data class TeamDto(
    val id: Int,
    val name: String,
    val logo: String?
)
