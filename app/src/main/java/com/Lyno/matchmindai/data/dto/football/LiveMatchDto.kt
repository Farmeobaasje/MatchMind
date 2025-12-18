package com.Lyno.matchmindai.data.dto.football

import kotlinx.serialization.Serializable

/**
 * DTO for live match data with goals information.
 */
@Serializable
data class LiveMatchDto(
    val fixture: FixtureDetailsDto,
    val league: LeagueDetailsDto,
    val teams: TeamsDetailsDto,
    val goals: MatchGoalsDto? = null
)

/**
 * DTO for match goals information.
 */
@Serializable
data class MatchGoalsDto(
    val home: Int? = null,
    val away: Int? = null
)

/**
 * DTO for live scores response.
 */
@Serializable
data class LiveScoresResponse(
    val response: List<LiveMatchDto>
)
