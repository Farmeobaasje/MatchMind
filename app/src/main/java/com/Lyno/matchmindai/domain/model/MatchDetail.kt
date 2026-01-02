package com.Lyno.matchmindai.domain.model

import kotlinx.serialization.Serializable

/**
 * League standing row for a team.
 */
@Serializable
data class StandingRow(
    val rank: Int,
    val team: String,
    val teamLogo: String? = null,
    val points: Int,
    val played: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val goalDiff: Int,
    val form: String? = null,
    val description: String? = null
) {
    /**
     * Get goal difference display string.
     */
    val goalDiffDisplay: String
        get() = if (goalDiff >= 0) "+$goalDiff" else goalDiff.toString()
}

/**
 * Comprehensive match details domain model.
 * Contains all information about a specific fixture including events, lineups, and statistics.
 * Enhanced with injuries, predictions, and odds data for AI analysis.
 */
@Serializable
data class MatchDetail(
    val fixtureId: Int,
    val homeTeam: String,
    val awayTeam: String,
    val homeTeamId: Int? = null,
    val awayTeamId: Int? = null,
    val homeTeamLogo: String? = null,
    val awayTeamLogo: String? = null,
    val league: String,
    val leagueId: Int? = null,
    val leagueLogo: String? = null,
    val info: MatchInfo,
    val stats: List<StatItem>,
    val lineups: MatchLineups,
    val events: List<MatchEvent>,
    val score: MatchScore? = null,
    val status: MatchStatus? = null,
    val standings: List<StandingRow>? = null,
    val injuries: List<Injury> = emptyList(),
    val prediction: MatchPredictionData? = null,
    val odds: OddsData? = null
) {
    /**
     * Check if the match has started (has events or stats).
     */
    val hasStarted: Boolean
        get() = events.isNotEmpty() || stats.isNotEmpty()

    /**
     * Check if lineups are available.
     */
    val hasLineups: Boolean
        get() = lineups.home.players.isNotEmpty() || lineups.away.players.isNotEmpty()

    /**
     * Check if injuries are available.
     */
    val hasInjuries: Boolean
        get() = injuries.isNotEmpty()

    /**
     * Check if prediction is available.
     */
    val hasPrediction: Boolean
        get() = prediction != null

    /**
     * Check if odds are available.
     */
    val hasOdds: Boolean
        get() = odds != null

    /**
     * Get current score display.
     */
    val scoreDisplay: String
        get() = score?.let { "${it.home}-${it.away}" } ?: "0-0"

    /**
     * Get key injuries for beginners.
     */
    val keyInjuries: List<Injury>
        get() = injuries.filter { it.isKeyPlayer || it.severity == InjurySeverity.MAJOR }

    /**
     * Get simplified prediction for beginners.
     */
    val simplifiedPrediction: String
        get() = prediction?.let {
            "${it.primaryPrediction} (${it.winningPercent.mostLikely})"
        } ?: "Geen voorspelling beschikbaar"

    /**
     * Get value bet recommendation.
     */
    val valueBetRecommendation: String
        get() = odds?.valueBet ?: "Geen odds beschikbaar"
}

/**
 * Match information (stadium, referee, time).
 */
@Serializable
data class MatchInfo(
    val stadium: String? = null,
    val referee: String? = null,
    val date: String? = null,
    val time: String? = null,
    val timestamp: Long? = null,
    val venue: MatchVenue? = null
)

/**
 * Venue information for match details.
 */
@Serializable
data class MatchVenue(
    val name: String,
    val city: String? = null,
    val capacity: Int? = null
)

/**
 * Match score information.
 */
@Serializable
data class MatchScore(
    val home: Int,
    val away: Int,
    val halftime: ScorePeriod? = null,
    val fulltime: ScorePeriod? = null
)

/**
 * Score for a specific period.
 */
@Serializable
data class ScorePeriod(
    val home: Int? = null,
    val away: Int? = null
)

/**
 * Statistic item for match comparison.
 */
@Serializable
data class StatItem(
    val type: String,           // e.g., "Ball Possession", "Shots on Goal"
    val homeValue: Int,         // Home team value (percentage or count)
    val awayValue: Int,         // Away team value (percentage or count)
    val unit: String = "%"      // Unit of measurement (% or count)
) {
    /**
     * Get home value as percentage for display.
     */
    val homePercentage: Int
        get() = if (unit == "%") homeValue else {
            val total = homeValue + awayValue
            if (total > 0) (homeValue * 100 / total) else 0
        }

    /**
     * Get away value as percentage for display.
     */
    val awayPercentage: Int
        get() = if (unit == "%") awayValue else {
            val total = homeValue + awayValue
            if (total > 0) (awayValue * 100 / total) else 0
        }
}

/**
 * Match lineups for both teams.
 */
@Serializable
data class MatchLineups(
    val home: TeamLineup,
    val away: TeamLineup
)

/**
 * Team lineup with formation and players.
 */
@Serializable
data class TeamLineup(
    val teamName: String,
    val formation: String? = null,
    val coach: String? = null,
    val players: List<LineupPlayer>,
    val substitutes: List<LineupPlayer> = emptyList()
)

/**
 * Player in lineup with position.
 */
@Serializable
data class LineupPlayer(
    val name: String,
    val number: Int? = null,
    val position: String? = null,  // e.g., "G", "D", "M", "F"
    val grid: String? = null,      // e.g., "1:1", "2:3" for field position
    val isCaptain: Boolean = false,
    val isSubstitute: Boolean = false
)

/**
 * Match event (goal, card, substitution, etc.)
 */
@Serializable
data class MatchEvent(
    val type: EventType,
    val minute: Int,
    val extraMinute: Int? = null,
    val team: String,
    val player: String? = null,
    val assist: String? = null,
    val detail: String? = null,   // e.g., "Normal Goal", "Yellow Card", "Penalty"
    val comments: String? = null
) {
    /**
     * Get display time (e.g., "45+2'").
     */
    val displayTime: String
        get() = if (extraMinute != null) "${minute}+${extraMinute}'" else "${minute}'"
}

/**
 * Type of match event.
 */
@Serializable
enum class EventType {
    GOAL,
    YELLOW_CARD,
    RED_CARD,
    SUBSTITUTION,
    VAR,
    PENALTY,
    OWN_GOAL,
    MISSED_PENALTY,
    OTHER;

    /**
     * Get emoji representation for UI.
     */
    val emoji: String
        get() = when (this) {
            GOAL -> "⚽"
            YELLOW_CARD -> "🟨"
            RED_CARD -> "🟥"
            SUBSTITUTION -> "🔄"
            VAR -> "📺"
            PENALTY -> "🎯"
            OWN_GOAL -> "😬"
            MISSED_PENALTY -> "❌"
            OTHER -> "📝"
        }

    /**
     * Get display name in Dutch.
     */
    val displayName: String
        get() = when (this) {
            GOAL -> "Goal"
            YELLOW_CARD -> "Gele Kaart"
            RED_CARD -> "Rode Kaart"
            SUBSTITUTION -> "Wissel"
            VAR -> "VAR"
            PENALTY -> "Penalty"
            OWN_GOAL -> "Eigen Doelpunt"
            MISSED_PENALTY -> "Gemiste Penalty"
            OTHER -> "Ander"
        }
}

/**
 * Companion object with utility functions.
 */
object MatchDetailUtils {
    /**
     * Creates an empty match detail for loading/error states.
     */
    fun empty(fixtureId: Int, homeTeam: String, awayTeam: String, league: String): MatchDetail {
        return MatchDetail(
            fixtureId = fixtureId,
            homeTeam = homeTeam,
            awayTeam = awayTeam,
            league = league,
            leagueId = null,
            info = MatchInfo(),
            stats = emptyList(),
            lineups = MatchLineups(
                home = TeamLineup(teamName = homeTeam, players = emptyList()),
                away = TeamLineup(teamName = awayTeam, players = emptyList())
            ),
            events = emptyList()
        )
    }

    /**
     * Creates a match detail with "no data available" message.
     */
    fun noDataAvailable(fixtureId: Int, homeTeam: String, awayTeam: String, league: String): MatchDetail {
        return MatchDetail(
            fixtureId = fixtureId,
            homeTeam = homeTeam,
            awayTeam = awayTeam,
            league = league,
            leagueId = null,
            info = MatchInfo(stadium = "Nog geen data beschikbaar"),
            stats = emptyList(),
            lineups = MatchLineups(
                home = TeamLineup(teamName = homeTeam, players = emptyList()),
                away = TeamLineup(teamName = awayTeam, players = emptyList())
            ),
            events = emptyList(),
            injuries = emptyList(),
            prediction = null,
            odds = null
        )
    }
}
