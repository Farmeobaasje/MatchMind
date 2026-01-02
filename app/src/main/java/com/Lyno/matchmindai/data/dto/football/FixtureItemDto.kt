package com.Lyno.matchmindai.data.dto.football

import kotlinx.serialization.Serializable

/**
 * DTO for a single fixture item from RapidAPI Football API.
 */
@Serializable
data class FixtureItemDto(
    val fixture: FixtureDetailsDto,
    val league: LeagueDetailsDto,
    val teams: TeamsDetailsDto,
    val goals: MatchGoalsDto? = null
)
