package com.Lyno.matchmindai.data.dto.football

import kotlinx.serialization.Serializable

/**
 * DTO for winner prediction in the predictions response.
 */
@Serializable
data class WinnerDto(
    val id: Int? = null,
    val name: String? = null,
    val comment: String? = null
)

/**
 * DTO for goals prediction in the predictions response.
 */
@Serializable
data class PredictionGoalsDto(
    val home: String? = null,
    val away: String? = null
)

/**
 * DTO for comparison data in the predictions response.
 * Actual API structure:
 * {
 *   "form": {"home": "60%", "away": "40%"},
 *   "att": {"home": "43%", "away": "57%"},
 *   "def": {"home": "62%", "away": "38%"},
 *   "poisson_distribution": {"home": "75%", "away": "25%"},
 *   "h2h": {"home": "29%", "away": "71%"},
 *   "goals": {"home": "40%", "away": "60%"},
 *   "total": {"home": "51.5%", "away": "48.5%"}
 * }
 */
@Serializable
data class ComparisonDto(
    val form: ComparisonItemDto? = null,
    val att: ComparisonItemDto? = null,
    val def: ComparisonItemDto? = null,
    @kotlinx.serialization.SerialName("poisson_distribution")
    val poissonDistribution: ComparisonItemDto? = null,
    val h2h: ComparisonItemDto? = null,
    val goals: ComparisonItemDto? = null,
    val total: ComparisonItemDto? = null
)

/**
 * DTO for individual comparison item with home and away percentages.
 */
@Serializable
data class ComparisonItemDto(
    val home: String? = null,
    val away: String? = null
)

/**
 * DTO for head-to-head match in the predictions response.
 * This represents a single historical match between the teams.
 */
@Serializable
data class H2HMatchDto(
    val fixture: H2HFixtureDto? = null,
    val league: H2HLeagueDto? = null,
    val teams: H2HTeamsDto? = null,
    val goals: H2HGoalsDto? = null,
    val score: H2HScoreDto? = null
)

/**
 * DTO for fixture details in H2H match.
 */
@Serializable
data class H2HFixtureDto(
    val id: Int? = null,
    val referee: String? = null,
    val timezone: String? = null,
    val date: String? = null,
    val timestamp: Long? = null,
    val periods: H2HPeriodsDto? = null,
    val venue: H2HVenueDto? = null,
    val status: H2HStatusDto? = null
)

/**
 * DTO for league details in H2H match.
 */
@Serializable
data class H2HLeagueDto(
    val id: Int? = null,
    val name: String? = null,
    val country: String? = null,
    val logo: String? = null,
    val flag: String? = null,
    val season: Int? = null,
    val round: String? = null
)

/**
 * DTO for teams in H2H match.
 */
@Serializable
data class H2HTeamsDto(
    val home: H2HTeamDto? = null,
    val away: H2HTeamDto? = null
)

/**
 * DTO for team in H2H match.
 */
@Serializable
data class H2HTeamDto(
    val id: Int? = null,
    val name: String? = null,
    val logo: String? = null,
    val winner: Boolean? = null
)

/**
 * DTO for goals in H2H match.
 */
@Serializable
data class H2HGoalsDto(
    val home: Int? = null,
    val away: Int? = null
)

/**
 * DTO for score in H2H match.
 */
@Serializable
data class H2HScoreDto(
    val halftime: H2HScoreDetailDto? = null,
    val fulltime: H2HScoreDetailDto? = null,
    val extratime: H2HScoreDetailDto? = null,
    val penalty: H2HScoreDetailDto? = null
)

/**
 * DTO for score details in H2H match.
 */
@Serializable
data class H2HScoreDetailDto(
    val home: Int? = null,
    val away: Int? = null
)

/**
 * DTO for periods in H2H match.
 */
@Serializable
data class H2HPeriodsDto(
    val first: Long? = null,
    val second: Long? = null
)

/**
 * DTO for venue in H2H match.
 */
@Serializable
data class H2HVenueDto(
    val id: Int? = null,
    val name: String? = null,
    val city: String? = null
)

/**
 * DTO for status in H2H match.
 */
@Serializable
data class H2HStatusDto(
    val long: String? = null,
    val short: String? = null,
    val elapsed: Int? = null,
    val extra: String? = null
)
