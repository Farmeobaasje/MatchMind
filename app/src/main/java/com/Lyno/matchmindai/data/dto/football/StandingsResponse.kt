package com.Lyno.matchmindai.data.dto.football

import kotlinx.serialization.Serializable

/**
 * Response for standings endpoint.
 * Contains league standings information.
 * Updated to match API-Sports response structure with nested standings list.
 */
@Serializable
data class StandingsResponse(
    val response: List<LeagueStandings> = emptyList(),
    val results: Int = 0
)

/**
 * League standings information.
 */
@Serializable
data class LeagueStandings(
    val league: LeagueDetailsDto,
    val standings: List<List<StandingEntry>> = emptyList()
)

/**
 * Standing entry for a team in the league.
 */
@Serializable
data class StandingEntry(
    val rank: Int,
    val team: TeamDto,
    val points: Int,
    val goalsDiff: Int,
    val group: String,
    val form: String? = null,
    val status: String? = null,
    val description: String? = null,
    val all: MatchStats,
    val home: MatchStats,
    val away: MatchStats,
    val update: String
)

/**
 * Match statistics for a team.
 */
@Serializable
data class MatchStats(
    val played: Int,
    val win: Int,
    val draw: Int,
    val lose: Int,
    val goals: GoalsStats
)

/**
 * Goals statistics.
 */
@Serializable
data class GoalsStats(
    val `for`: Int,
    val against: Int
)
