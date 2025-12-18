package com.Lyno.matchmindai.data.dto.football

import kotlinx.serialization.Serializable

/**
 * DTO for league details from RapidAPI Football API.
 * Used for both fixtures and standings responses.
 */
@Serializable
data class LeagueDetailsDto(
    val id: Int,
    val name: String,
    val country: String,
    val flag: String? = null,
    val logo: String? = null,
    val season: Int? = null
)
