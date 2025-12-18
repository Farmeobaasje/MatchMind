package com.Lyno.matchmindai.data.dto.football

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for the odds response from API-SPORTS V3.
 * This endpoint returns pre-match odds for fixtures.
 * Update Frequency: Every 3 hours, Recommended Calls: 1 call every 3 hours.
 */
@Serializable
data class OddsResponseDto(
    val get: String,
    val parameters: Map<String, String>,
    val errors: List<String>,
    val results: Int,
    val paging: PagingDto,
    val response: List<OddsFixtureDto>
)

/**
 * DTO for pagination information in odds responses.
 */
@Serializable
data class PagingDto(
    val current: Int,
    val total: Int
)

/**
 * DTO for individual fixture odds data.
 */
@Serializable
data class OddsFixtureDto(
    val fixture: OddsFixtureDetailsDto,
    val league: OddsLeagueDto,
    val bookmakers: List<BookmakerDto>
)

/**
 * DTO for fixture details in odds responses.
 */
@Serializable
data class OddsFixtureDetailsDto(
    val id: Int,
    val timezone: String,
    val date: String,
    val timestamp: Long
)

/**
 * DTO for league information in odds responses.
 */
@Serializable
data class OddsLeagueDto(
    val id: Int,
    val name: String,
    val country: String,
    val logo: String,
    val flag: String? = null,
    val season: Int
)

/**
 * DTO for bookmaker information.
 */
@Serializable
data class BookmakerDto(
    val id: Int,
    val name: String,
    val bets: List<BetDto>
)

/**
 * DTO for bet information.
 */
@Serializable
data class BetDto(
    val id: Int,
    val name: String,
    val values: List<BetValueDto>
)

/**
 * DTO for individual bet values with odds.
 */
@Serializable
data class BetValueDto(
    val value: String,
    val odd: String,
    val handicap: String? = null,
    val main: Boolean? = null,
    val suspended: Boolean? = null
)

/**
 * DTO for live odds response (in-play odds).
 * This endpoint returns in-play odds for fixtures in progress.
 * Update Frequency: Every 5 seconds.
 */
@Serializable
data class LiveOddsResponseDto(
    val get: String,
    val parameters: Map<String, String>,
    val errors: List<String>,
    val results: Int,
    val paging: PagingDto,
    val response: List<LiveOddsFixtureDto>
)

/**
 * DTO for live fixture odds with status information.
 */
@Serializable
data class LiveOddsFixtureDto(
    val fixture: OddsFixtureDetailsDto,
    val league: OddsLeagueDto,
    val bookmakers: List<BookmakerDto>,
    val status: OddsStatusDto? = null
)

/**
 * DTO for odds status information (in-play only).
 */
@Serializable
data class OddsStatusDto(
    val stopped: Boolean,
    val blocked: Boolean,
    val finished: Boolean
)

/**
 * DTO for simplified odds data for AI analysis.
 * Used to extract key betting information for beginners.
 */
@Serializable
data class SimplifiedOddsDto(
    val fixtureId: Int,
    val homeTeam: String,
    val awayTeam: String,
    val leagueName: String,
    val matchTime: String,
    val bookmakerName: String,
    val homeWinOdds: Double?,
    val drawOdds: Double?,
    val awayWinOdds: Double?,
    val overUnderOdds: Map<String, Double>? = null,
    val bothTeamsToScoreOdds: Map<String, Double>? = null,
    val valueRating: Double = 0.0, // 0-100 rating for value bets
    val safetyRating: Double = 0.0, // 0-100 rating for safe bets
    val highestOdds: Double? = null // Highest odds available
)
