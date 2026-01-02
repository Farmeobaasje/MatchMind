package com.Lyno.matchmindai.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StandingsResponse(
    @SerialName("get") val get: String? = null,
    @SerialName("results") val results: Int? = null,
    @SerialName("response") val response: List<LeagueNode> = emptyList()
)

@Serializable
data class LeagueNode(
    @SerialName("league") val league: LeagueDetails? = null
)

@Serializable
data class LeagueDetails(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("country") val country: String,
    @SerialName("season") val season: Int,
    // IMPORTANT: Standings is a List of Lists (Groups)
    @SerialName("standings") val standings: List<List<StandingTeamDto>> = emptyList()
)

@Serializable
data class StandingTeamDto(
    @SerialName("rank") val rank: Int,
    @SerialName("team") val team: TeamIdentityDto,
    @SerialName("points") val points: Int,
    @SerialName("goalsDiff") val goalsDiff: Int,
    @SerialName("group") val group: String? = null,
    @SerialName("form") val form: String? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("all") val all: StandingStatsDto? = null,
    @SerialName("home") val home: StandingStatsDto? = null,
    @SerialName("away") val away: StandingStatsDto? = null
)

@Serializable
data class TeamIdentityDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("logo") val logo: String? = null
)

@Serializable
data class StandingStatsDto(
    @SerialName("played") val played: Int,
    @SerialName("win") val win: Int,
    @SerialName("draw") val draw: Int,
    @SerialName("lose") val lose: Int,
    @SerialName("goals") val goals: GoalsDto? = null
)

@Serializable
data class GoalsDto(
    @SerialName("for") val forGoals: Int,
    @SerialName("against") val againstGoals: Int
)
