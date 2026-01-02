package com.Lyno.matchmindai.data.dto

import kotlinx.serialization.Serializable

/**
 * DTOs for API-SPORTS responses.
 * These match the JSON structure from API-SPORTS v3 API.
 */

@Serializable
data class ApiSportsResponse<T>(
    val get: String,
    val parameters: Map<String, String> = emptyMap(),
    val errors: List<ApiSportsError> = emptyList(),
    val results: Int,
    val paging: PagingInfo,
    val response: T
)

@Serializable
data class ApiSportsError(
    val value: String? = null
)

@Serializable
data class PagingInfo(
    val current: Int,
    val total: Int
)

@Serializable
data class FixtureResponse(
    val fixture: FixtureDto,
    val teams: TeamsDto,
    val goals: GoalsDto,
    val league: LeagueDto? = null
)

@Serializable
data class FixtureDto(
    val id: Int,
    val date: String,
    val status: StatusDto
)

@Serializable
data class StatusDto(
    val short: String,
    val long: String,
    val elapsed: Int? = null
)

@Serializable
data class TeamsDto(
    val home: TeamDto,
    val away: TeamDto
)

@Serializable
data class TeamDto(
    val id: Int,
    val name: String,
    val logo: String? = null,
    val winner: Boolean? = null,
    val code: String? = null,
    val country: String? = null,
    val founded: Int? = null,
    val national: Boolean? = null
)

@Serializable
data class GoalsDto(
    val home: Int? = null,
    val away: Int? = null
)

@Serializable
data class LeagueDto(
    val id: Int,
    val name: String,
    val country: String,
    val logo: String? = null,
    val season: Int
)

@Serializable
data class TeamsSearchResponse(
    val team: TeamDto,
    val venue: VenueDto? = null
)

@Serializable
data class VenueDto(
    val id: Int? = null,
    val name: String? = null,
    val address: String? = null,
    val city: String? = null,
    val capacity: Int? = null,
    val surface: String? = null,
    val image: String? = null
)

@Serializable
data class FixturesSearchParams(
    val team: Int? = null,
    val league: Int? = null,
    val season: Int? = null,
    val date: String? = null,
    val status: String? = null,
    val live: String? = null
)
