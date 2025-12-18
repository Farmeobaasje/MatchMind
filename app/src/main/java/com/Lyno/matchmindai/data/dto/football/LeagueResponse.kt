package com.Lyno.matchmindai.data.dto.football

import kotlinx.serialization.Serializable

/**
 * DTO for the leagues response from API-SPORTS V3.
 * This response includes the coverage object which is critical for UI logic.
 */
@Serializable
data class LeagueResponse(
    val response: List<LeagueDto>
)

/**
 * DTO for individual league data including coverage information.
 * The coverage object indicates what data endpoints are available for this league.
 */
@Serializable
data class LeagueDto(
    val league: LeagueDetailsDto,
    val country: CountryDto,
    val seasons: List<SeasonDto>? = null,
    val coverage: CoverageDto? = null
)

/**
 * DTO for country information in league responses.
 */
@Serializable
data class CountryDto(
    val name: String,
    val code: String? = null,
    val flag: String? = null
)

/**
 * DTO for season information in league responses.
 */
@Serializable
data class SeasonDto(
    val year: Int,
    val start: String,
    val end: String,
    val current: Boolean
)
