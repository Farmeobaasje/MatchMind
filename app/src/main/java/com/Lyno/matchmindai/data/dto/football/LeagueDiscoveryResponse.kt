package com.Lyno.matchmindai.data.dto.football

import kotlinx.serialization.Serializable

/**
 * DTO for the leagues discovery response from API-SPORTS V3.
 * This response has a different structure than other league endpoints:
 * - The 'country' field is a sibling of 'league', not inside it
 * - Used specifically for the /leagues endpoint with current=true parameter
 */
@Serializable
data class LeagueDiscoveryResponse(
    val get: String,
    val parameters: Map<String, String>,
    val errors: List<String>,
    val results: Int,
    val response: List<LeagueEntryDto>
)

/**
 * DTO for individual league entry in the discovery response.
 * Contains league info, country info, and seasons.
 */
@Serializable
data class LeagueEntryDto(
    val league: LeagueInfoDto,
    val country: CountryDto,
    val seasons: List<LeagueSeasonDto>
)

/**
 * DTO for league information in discovery response.
 * NOTE: This does NOT contain a 'country' field - country is separate!
 */
@Serializable
data class LeagueInfoDto(
    val id: Int,
    val name: String,
    val type: String,
    val logo: String
    // LET OP: Geen 'country' hier!
)

/**
 * DTO for season information in league discovery response.
 * This is different from the existing SeasonDto in LeagueResponse.kt
 * Note: start and end can be null for some leagues (e.g., future seasons)
 */
@Serializable
data class LeagueSeasonDto(
    val year: Int,
    val start: String? = null,
    val end: String? = null,
    val current: Boolean,
    val coverage: LeagueCoverageDto
)

/**
 * DTO for coverage information in league discovery response.
 * Indicates what data endpoints are available for this league/season.
 * This is different from the existing CoverageDto in CoverageDto.kt
 */
@Serializable
data class LeagueCoverageDto(
    val fixtures: FixturesCoverageDto,
    val standings: Boolean,
    val players: Boolean,
    val top_scorers: Boolean,
    val top_assists: Boolean,
    val top_cards: Boolean,
    val injuries: Boolean,
    val predictions: Boolean,
    val odds: Boolean
)

/**
 * DTO for fixtures coverage details.
 */
@Serializable
data class FixturesCoverageDto(
    val events: Boolean,
    val lineups: Boolean,
    val statistics_fixtures: Boolean,
    val statistics_players: Boolean
)
