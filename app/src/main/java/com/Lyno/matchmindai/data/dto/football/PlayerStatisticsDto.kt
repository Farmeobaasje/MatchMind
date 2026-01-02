package com.Lyno.matchmindai.data.dto.football

import kotlinx.serialization.Serializable

/**
 * DTO for player statistics from API-SPORTS /players/statistics endpoint.
 * Used for player scoring probability calculations.
 */
@Serializable
data class PlayerStatisticsDto(
    val player: PlayerStatsInfoDto,
    val statistics: List<PlayerSeasonStatisticsDto>
)

@Serializable
data class PlayerStatsInfoDto(
    val id: Int,
    val name: String,
    val firstname: String? = null,
    val lastname: String? = null,
    val age: Int? = null,
    val birth: PlayerBirthDto? = null,
    val nationality: String? = null,
    val height: String? = null,
    val weight: String? = null,
    val injured: Boolean? = false,
    val photo: String? = null
)

@Serializable
data class PlayerBirthDto(
    val date: String? = null,
    val place: String? = null,
    val country: String? = null
)

@Serializable
data class PlayerSeasonStatisticsDto(
    val team: PlayerStatsTeamInfoDto,
    val league: PlayerStatsLeagueInfoDto,
    val games: PlayerGamesDto,
    val substitutes: PlayerSubstitutesDto,
    val shots: PlayerShotsDto,
    val goals: PlayerGoalsDto,
    val passes: PlayerPassesDto? = null,
    val tackles: PlayerTacklesDto? = null,
    val duels: PlayerDuelsDto? = null,
    val dribbles: PlayerDribblesDto? = null,
    val fouls: PlayerFoulsDto? = null,
    val cards: PlayerCardsDto? = null,
    val penalty: PlayerPenaltyDto? = null
)

@Serializable
data class PlayerStatsTeamInfoDto(
    val id: Int,
    val name: String,
    val logo: String? = null
)

@Serializable
data class PlayerStatsLeagueInfoDto(
    val id: Int,
    val name: String,
    val country: String? = null,
    val logo: String? = null,
    val flag: String? = null,
    val season: Int
)

@Serializable
data class PlayerGamesDto(
    val appearances: Int? = 0,
    val lineups: Int? = 0,
    val minutes: Int? = 0,
    val number: Int? = null,
    val position: String? = null,
    val rating: String? = null,
    val captain: Boolean? = false
)

@Serializable
data class PlayerSubstitutesDto(
    val `in`: Int? = 0,
    val out: Int? = 0,
    val bench: Int? = 0
)

@Serializable
data class PlayerShotsDto(
    val total: Int? = 0,
    val on: Int? = 0
)

@Serializable
data class PlayerGoalsDto(
    val total: Int? = 0,
    val conceded: Int? = 0,
    val assists: Int? = 0,
    val saves: Int? = null
)

@Serializable
data class PlayerPassesDto(
    val total: Int? = 0,
    val key: Int? = 0,
    val accuracy: Int? = 0
)

@Serializable
data class PlayerTacklesDto(
    val total: Int? = 0,
    val blocks: Int? = 0,
    val interceptions: Int? = 0
)

@Serializable
data class PlayerDuelsDto(
    val total: Int? = 0,
    val won: Int? = 0
)

@Serializable
data class PlayerDribblesDto(
    val attempts: Int? = 0,
    val success: Int? = 0,
    val past: Int? = null
)

@Serializable
data class PlayerFoulsDto(
    val drawn: Int? = 0,
    val committed: Int? = 0
)

@Serializable
data class PlayerCardsDto(
    val yellow: Int? = 0,
    val yellowred: Int? = 0,
    val red: Int? = 0
)

@Serializable
data class PlayerPenaltyDto(
    val won: Int? = 0,
    val committed: Int? = 0,
    val scored: Int? = 0,
    val missed: Int? = 0,
    val saved: Int? = 0
)

/**
 * DTO for top scorers from API-SPORTS /players/topscorers endpoint.
 */
@Serializable
data class TopScorerDto(
    val player: PlayerStatsInfoDto,
    val statistics: List<TopScorerStatisticsDto>
)

@Serializable
data class TopScorerStatisticsDto(
    val team: PlayerStatsTeamInfoDto,
    val league: PlayerStatsLeagueInfoDto,
    val games: PlayerGamesDto,
    val substitutes: PlayerSubstitutesDto,
    val shots: PlayerShotsDto,
    val goals: TopScorerGoalsDto,
    val passes: PlayerPassesDto? = null
)

@Serializable
data class TopScorerGoalsDto(
    val total: Int? = 0,
    val conceded: Int? = 0,
    val assists: Int? = 0,
    val saves: Int? = null
)

/**
 * DTO for player injuries from API-SPORTS /injuries endpoint.
 * Note: Different from PlayerInjuryDto in InjuriesResponseDto.kt
 */
@Serializable
data class PlayerStatsInjuryDto(
    val player: PlayerStatsInfoDto,
    val team: PlayerStatsTeamInfoDto,
    val fixture: InjuryFixtureDto,
    val league: PlayerStatsLeagueInfoDto
)

@Serializable
data class InjuryFixtureDto(
    val id: Int? = null,
    val timezone: String? = null,
    val date: String? = null,
    val timestamp: Long? = null
)

/**
 * Wrapper for player statistics API response.
 */
@Serializable
data class PlayerStatisticsResponse(
    val response: List<PlayerStatisticsDto>
)

/**
 * Wrapper for top scorers API response.
 */
@Serializable
data class TopScorersResponse(
    val response: List<TopScorerDto>
)

/**
 * Wrapper for injuries API response.
 */
@Serializable
data class PlayerStatsInjuriesResponse(
    val response: List<PlayerStatsInjuryDto>
)
