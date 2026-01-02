package com.Lyno.matchmindai.data.dto.football

import kotlinx.serialization.Serializable

/**
 * DTO for team statistics response from API-SPORTS /teams/statistics endpoint.
 * Used for style clash analysis in Voorspellingen-X.
 */
@Serializable
data class TeamStatisticsResponse(
    val team: TeamStatisticsTeam,
    val fixtures: TeamStatisticsFixtures,
    val goals: TeamStatisticsGoals,
    val lineups: List<TeamStatsLineup>? = emptyList(),
    val cards: TeamStatisticsCards? = null
)

@Serializable
data class TeamStatisticsTeam(
    val id: Int,
    val name: String,
    val logo: String?
)

@Serializable
data class TeamStatisticsFixtures(
    val played: PlayedStats,
    val wins: PlayedStats,
    val draws: PlayedStats,
    val loses: PlayedStats
)

@Serializable
data class PlayedStats(
    val home: Int,
    val away: Int,
    val total: Int
)

@Serializable
data class TeamStatisticsGoals(
    val `for`: TeamStatsGoalStats,
    val against: TeamStatsGoalStats
)

@Serializable
data class TeamStatsGoalStats(
    val total: TotalGoalStats,
    val average: AverageGoalStats,
    val minute: Map<String, TeamStatsMinuteStats>? = emptyMap()
)

@Serializable
data class TotalGoalStats(
    val home: Int,
    val away: Int,
    val total: Int
)

@Serializable
data class AverageGoalStats(
    val home: String,
    val away: String,
    val total: String
)

@Serializable
data class TeamStatsMinuteStats(
    val total: Int? = null,
    val percentage: String? = null
)

@Serializable
data class TeamStatsLineup(
    val formation: String,
    val played: Int
)

@Serializable
data class TeamStatisticsCards(
    val yellow: TeamStatsCardStats,
    val red: TeamStatsCardStats
)

@Serializable
data class TeamStatsCardStats(
    val `0-15`: TeamStatsMinuteCardStats? = null,
    val `16-30`: TeamStatsMinuteCardStats? = null,
    val `31-45`: TeamStatsMinuteCardStats? = null,
    val `46-60`: TeamStatsMinuteCardStats? = null,
    val `61-75`: TeamStatsMinuteCardStats? = null,
    val `76-90`: TeamStatsMinuteCardStats? = null,
    val `91-105`: TeamStatsMinuteCardStats? = null,
    val `106-120`: TeamStatsMinuteCardStats? = null
)

@Serializable
data class TeamStatsMinuteCardStats(
    val total: Int? = null,
    val percentage: String? = null
)
