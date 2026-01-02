package com.Lyno.matchmindai.data.dto

import com.Lyno.matchmindai.data.dto.football.TeamDto
import com.Lyno.matchmindai.data.dto.football.MatchGoalsDto
import com.Lyno.matchmindai.data.dto.football.LeagueDetailsDto
import com.Lyno.matchmindai.data.dto.football.StatusDto
import com.Lyno.matchmindai.data.dto.football.TeamsDetailsDto
import kotlinx.serialization.Serializable

@Serializable
data class FixtureItemDto(
    val fixture: FixtureDetailsDto,
    val teams: TeamsDetailsDto,
    val goals: MatchGoalsDto,
    val league: LeagueDetailsDto
)

@Serializable
data class FixtureDetailsDto(
    val id: Int,
    val date: String,
    val status: StatusDto
)

@Serializable
data class FixtureListResponse(
    val response: List<FixtureItemDto>
)
