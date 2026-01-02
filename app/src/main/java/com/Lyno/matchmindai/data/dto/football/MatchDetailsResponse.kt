package com.Lyno.matchmindai.data.dto.football

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Response for match details endpoint.
 * Contains comprehensive information about a specific fixture including events, lineups, statistics, and players.
 */
@Serializable
data class MatchDetailsResponse(
    val get: String? = null,
    val parameters: MatchDetailsParameters? = null,
    val errors: List<String>? = null,
    val results: Int? = null,
    val paging: Paging? = null,
    val response: List<MatchDetails>? = null
)

/**
 * Parameters for match details request.
 */
@Serializable
data class MatchDetailsParameters(
    val id: String? = null
)

/**
 * Comprehensive match details including events, lineups, statistics, and players.
 */
@Serializable
data class MatchDetails(
    val fixture: FixtureDetails? = null,
    val league: LeagueDetailsDto? = null,
    val teams: TeamsDetailsDto? = null,
    val goals: Goals? = null,
    val score: ScoreDetails? = null,
    val events: List<MatchEvent>? = null,
    val lineups: List<Lineup>? = null,
    val statistics: List<TeamStatistics>? = null,
    val players: List<PlayerStats>? = null
)

/**
 * Detailed fixture information.
 */
@Serializable
data class FixtureDetails(
    val id: Int? = null,
    val referee: String? = null,
    val timezone: String? = null,
    val date: String? = null,
    val timestamp: Long? = null,
    val periods: Periods? = null,
    val venue: Venue? = null,
    val status: StatusDto? = null
)

/**
 * Match periods (first half, second half, etc.)
 */
@Serializable
data class Periods(
    val first: Long? = null,
    val second: Long? = null
)

/**
 * Venue information.
 */
@Serializable
data class Venue(
    val id: Int? = null,
    val name: String? = null,
    val city: String? = null
)

/**
 * Goals scored by each team.
 */
@Serializable
data class Goals(
    val home: Int? = null,
    val away: Int? = null
)

/**
 * Detailed score information.
 */
@Serializable
data class ScoreDetails(
    val halftime: Goals? = null,
    val fulltime: Goals? = null,
    val extratime: Goals? = null,
    val penalty: Goals? = null
)

/**
 * Match event (goal, card, substitution, etc.)
 */
@Serializable
data class MatchEvent(
    val time: Time? = null,
    val team: TeamDto? = null,
    val player: Player? = null,
    val assist: Player? = null,
    val type: String? = null, // "Goal", "Card", "subst", "Var"
    val detail: String? = null, // "Normal Goal", "Yellow Card", etc.
    val comments: String? = null
)

/**
 * Event time information.
 */
@Serializable
data class Time(
    val elapsed: Int? = null,
    val extra: Int? = null
)

/**
 * Player information for events.
 */
@Serializable
data class Player(
    val id: Int? = null,
    val name: String? = null
)

/**
 * Team lineup with formation and starting XI.
 */
@Serializable
data class Lineup(
    val team: TeamDto? = null,
    val formation: String? = null,
    val startXI: List<LineupPlayer>? = null,
    val substitutes: List<LineupPlayer>? = null,
    val coach: Coach? = null
)

/**
 * Player in lineup with position and grid coordinates.
 */
@Serializable
data class LineupPlayer(
    val player: Player? = null,
    val position: String? = null, // "G", "D", "M", "F"
    val grid: String? = null // "1:1", "2:3" format for field position
)

/**
 * Coach information.
 */
@Serializable
data class Coach(
    val id: Int? = null,
    val name: String? = null,
    val photo: String? = null
)

/**
 * Team statistics for a match.
 */
@Serializable
data class TeamStatistics(
    val team: TeamDto? = null,
    val statistics: List<Statistic>? = null
)

/**
 * Individual statistic.
 */
@Serializable
data class Statistic(
    val type: String? = null, // "Shots on Goal", "Shots off Goal", "Total Shots", etc.
    val value: kotlinx.serialization.json.JsonElement? = null // Can be Int, String, or null
)

/**
 * Player statistics for a match.
 */
@Serializable
data class PlayerStats(
    val team: TeamDto? = null,
    val players: List<PlayerDetail>? = null
)

/**
 * Detailed player information with statistics.
 */
@Serializable
data class PlayerDetail(
    val player: Player? = null,
    val statistics: List<PlayerStatistic>? = null
)

/**
 * Player-specific statistic.
 */
@Serializable
data class PlayerStatistic(
    val games: GameStats? = null,
    val offsides: Int? = null,
    val shots: ShotStats? = null,
    val goals: GoalStats? = null,
    val passes: PassStats? = null,
    val tackles: TackleStats? = null,
    val duels: DuelStats? = null,
    val dribbles: DribbleStats? = null,
    val fouls: FoulStats? = null,
    val cards: CardStats? = null,
    val penalty: PenaltyStats? = null
)

/**
 * Game statistics for a player.
 */
@Serializable
data class GameStats(
    val minutes: Int? = null,
    val number: Int? = null,
    val position: String? = null,
    val rating: String? = null,
    val captain: Boolean? = null,
    val substitute: Boolean? = null
)

/**
 * Shot statistics.
 */
@Serializable
data class ShotStats(
    val total: Int? = null,
    val on: Int? = null
)

/**
 * Goal statistics.
 */
@Serializable
data class GoalStats(
    val total: Int? = null,
    val conceded: Int? = null,
    val assists: Int? = null,
    val saves: Int? = null
)

/**
 * Pass statistics.
 */
@Serializable
data class PassStats(
    val total: Int? = null,
    val key: Int? = null,
    val accuracy: String? = null
)

/**
 * Tackle statistics.
 */
@Serializable
data class TackleStats(
    val total: Int? = null,
    val blocks: Int? = null,
    val interceptions: Int? = null
)

/**
 * Duel statistics.
 */
@Serializable
data class DuelStats(
    val total: Int? = null,
    val won: Int? = null
)

/**
 * Dribble statistics.
 */
@Serializable
data class DribbleStats(
    val attempts: Int? = null,
    val success: Int? = null,
    val past: Int? = null
)

/**
 * Foul statistics.
 */
@Serializable
data class FoulStats(
    val drawn: Int? = null,
    val committed: Int? = null
)

/**
 * Card statistics.
 */
@Serializable
data class CardStats(
    val yellow: Int? = null,
    val red: Int? = null
)

/**
 * Penalty statistics.
 */
@Serializable
data class PenaltyStats(
    val won: Int? = null,
    val committed: Int? = null,
    val scored: Int? = null,
    val missed: Int? = null,
    val saved: Int? = null
)

/**
 * Paging information for API responses.
 */
@Serializable
data class Paging(
    val current: Int? = null,
    val total: Int? = null
)
