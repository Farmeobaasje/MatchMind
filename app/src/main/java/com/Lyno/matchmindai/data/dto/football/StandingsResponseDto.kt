package com.Lyno.matchmindai.data.dto.football

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StandingsResponseDto(
    val response: List<LeagueResponseDto> = emptyList(),
    val results: Int = 0
)

@Serializable
data class LeagueResponseDto(
    val league: LeagueStandingsDetailsDto
)

@Serializable
data class LeagueStandingsDetailsDto(
    val id: Int,
    val name: String,
    val country: String? = null,
    val logo: String? = null,
    val season: Int,
    // LET OP: Dit is een Lijst van Lijsten! [[TeamA, TeamB...]]
    val standings: List<List<StandingItemDto>> = emptyList()
)

@Serializable
data class StandingItemDto(
    val rank: Int,
    val team: TeamDto,
    val points: Int,
    val goalsDiff: Int,
    val group: String? = null,
    val form: String? = null,
    val status: String? = null,
    val description: String? = null,
    val all: MatchStatsDto,
    val home: MatchStatsDto,
    val away: MatchStatsDto,
    val update: String? = null
)

@Serializable
data class MatchStatsDto(
    val played: Int,
    val win: Int,
    val draw: Int,
    val lose: Int,
    val goals: GoalsDto
)

@Serializable
data class GoalsDto(
    val `for`: Int, // 'for' is een reserved keyword, dus backticks nodig
    val against: Int
)

// Reuse existing TeamDto
