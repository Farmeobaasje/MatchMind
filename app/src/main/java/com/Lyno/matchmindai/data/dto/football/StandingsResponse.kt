package com.Lyno.matchmindai.data.dto.football

import kotlinx.serialization.Serializable

/**
 * Response for standings endpoint.
 * Updated to match API-Sports JSON structure exactly.
 * Simplified structure based on actual API response analysis.
 */
@Serializable
data class StandingsResponse(
    val response: List<StandingsResponseData> = emptyList(),
    val results: Int = 0
)

/**
 * Data wrapper for league standings.
 */
@Serializable
data class StandingsResponseData(
    val league: LeagueStandingsDto? = null
)

/**
 * League standings information.
 */
@Serializable
data class LeagueStandingsDto(
    val id: Int? = null,
    val name: String? = null,
    val country: String? = null,
    val logo: String? = null,
    val flag: String? = null,
    val season: Int? = null,
    // CRITICAL: Nested List for groups (List<List<StandingDto>>)
    val standings: List<List<StandingDto>> = emptyList()
)

/**
 * Standing entry for a team in the league.
 * Simplified to match actual API response.
 */
@Serializable
data class StandingDto(
    val rank: Int = 0,
    // CRITICAL: Team must be an Object, not String
    val team: TeamDto? = null,
    val points: Int = 0,
    val goalsDiff: Int = 0,
    val group: String? = null,
    val form: String? = null,
    val status: String? = null,
    val description: String? = null,
    val update: String? = null,
    // Match statistics - made nullable as they might not always be present
    val all: MatchStats? = null,
    val home: MatchStats? = null,
    val away: MatchStats? = null
)

/**
 * Match statistics for a team.
 */
@Serializable
data class MatchStats(
    val played: Int = 0,
    val win: Int = 0,
    val draw: Int = 0,
    val lose: Int = 0,
    val goals: GoalsStats? = null
)

/**
 * Goals statistics.
 */
@Serializable
data class GoalsStats(
    val `for`: Int = 0,
    val against: Int = 0
)
