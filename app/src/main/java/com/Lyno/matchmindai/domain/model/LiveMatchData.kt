package com.Lyno.matchmindai.domain.model

import kotlinx.serialization.Serializable

/**
 * Live match data for real-time updates during a match.
 * Contains live events, statistics, and odds that update during the match.
 */
@Serializable
data class LiveMatchData(
    val fixtureId: Int,
    val elapsedTime: Int? = null,          // Minutes elapsed (e.g., 45)
    val extraTime: Int? = null,            // Extra minutes (e.g., 2)
    val status: MatchStatus = MatchStatus.SCHEDULED,
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val homeHalftimeScore: Int? = null,
    val awayHalftimeScore: Int? = null,
    val events: List<LiveEvent> = emptyList(),
    val statistics: List<LiveStatistic> = emptyList(),
    val liveOdds: LiveOdds? = null,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    /**
     * Get current score display.
     */
    val scoreDisplay: String
        get() = "$homeScore-$awayScore"

    /**
     * Get elapsed time display (e.g., "45+2'").
     */
    val elapsedTimeDisplay: String
        get() = if (extraTime != null && extraTime > 0) {
            "${elapsedTime ?: 0}+${extraTime}'"
        } else {
            "${elapsedTime ?: 0}'"
        }

    /**
     * Check if there are any live events.
     */
    val hasEvents: Boolean
        get() = events.isNotEmpty()

    /**
     * Check if there are any live statistics.
     */
    val hasStatistics: Boolean
        get() = statistics.isNotEmpty()

    /**
     * Check if live odds are available.
     */
    val hasLiveOdds: Boolean
        get() = liveOdds != null

    /**
     * Get recent events (last 10 events).
     */
    val recentEvents: List<LiveEvent>
        get() = events.takeLast(10)

    /**
     * Get key events (goals, red cards).
     */
    val keyEvents: List<LiveEvent>
        get() = events.filter { it.isKeyEvent }

    /**
     * Get possession statistic if available.
     */
    val possession: LiveStatistic?
        get() = statistics.find { it.type == "Ball Possession" }

    /**
     * Get shots on target statistic if available.
     */
    val shotsOnTarget: LiveStatistic?
        get() = statistics.find { it.type == "Shots on Goal" }
}

/**
 * Live event during a match (goal, card, substitution, etc.)
 */
@Serializable
data class LiveEvent(
    val id: Int,
    val type: EventType,
    val minute: Int,
    val extraMinute: Int? = null,
    val team: String,
    val teamId: Int? = null,
    val player: String? = null,
    val playerId: Int? = null,
    val assist: String? = null,
    val assistId: Int? = null,
    val detail: String? = null,
    val comments: String? = null
) {
    /**
     * Get display time (e.g., "45+2'").
     */
    val displayTime: String
        get() = if (extraMinute != null && extraMinute > 0) {
            "${minute}+${extraMinute}'"
        } else {
            "${minute}'"
        }

    /**
     * Check if this is a key event (goal, red card).
     */
    val isKeyEvent: Boolean
        get() = type == EventType.GOAL || type == EventType.RED_CARD

    /**
     * Get event description for display.
     */
    val description: String
        get() = buildString {
            append(type.displayName)
            player?.let { append(": $it") }
            detail?.let { append(" ($it)") }
        }
}

/**
 * Live statistic for a match.
 */
@Serializable
data class LiveStatistic(
    val type: String,           // e.g., "Ball Possession", "Shots on Goal"
    val homeValue: Int,         // Home team value
    val awayValue: Int,         // Away team value
    val unit: String = "%",     // Unit of measurement
    val lastUpdated: Long = System.currentTimeMillis()
) {
    /**
     * Get total value for percentage calculation.
     */
    private val total: Int
        get() = homeValue + awayValue

    /**
     * Get home percentage for display.
     */
    val homePercentage: Int
        get() = if (unit == "%") homeValue else {
            if (total > 0) (homeValue * 100 / total) else 0
        }

    /**
     * Get away percentage for display.
     */
    val awayPercentage: Int
        get() = if (unit == "%") awayValue else {
            if (total > 0) (awayValue * 100 / total) else 0
        }

    /**
     * Get display value for home team.
     */
    val homeDisplay: String
        get() = if (unit == "%") "$homeValue%" else homeValue.toString()

    /**
     * Get display value for away team.
     */
    val awayDisplay: String
        get() = if (unit == "%") "$awayValue%" else awayValue.toString()
}

/**
 * Live odds data for in-play betting.
 */
@Serializable
data class LiveOdds(
    val fixtureId: Int,
    val bookmakerName: String,
    val homeWin: Double? = null,
    val draw: Double? = null,
    val awayWin: Double? = null,
    val overUnder: Map<String, Double> = emptyMap(),      // e.g., "Over 2.5" -> 1.85
    val bothTeamsToScore: Map<String, Double> = emptyMap(), // e.g., "Yes" -> 1.65
    val status: LiveOddsStatus = LiveOddsStatus.ACTIVE,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    /**
     * Check if main odds are available.
     */
    val hasMainOdds: Boolean
        get() = homeWin != null || draw != null || awayWin != null

    /**
     * Get best odds for home win.
     */
    val bestHomeWin: Double?
        get() = homeWin

    /**
     * Get best odds for draw.
     */
    val bestDraw: Double?
        get() = draw

    /**
     * Get best odds for away win.
     */
    val bestAwayWin: Double?
        get() = awayWin
}

/**
 * Status of live odds.
 */
@Serializable
enum class LiveOddsStatus {
    ACTIVE,         // Odds are active and updating
    SUSPENDED,      // Odds are temporarily suspended
    CLOSED,         // Odds are closed (match finished)
    UNAVAILABLE;    // Odds are not available

    /**
     * Get display name in Dutch.
     */
    val displayName: String
        get() = when (this) {
            ACTIVE -> "Actief"
            SUSPENDED -> "Geschorst"
            CLOSED -> "Gesloten"
            UNAVAILABLE -> "Niet beschikbaar"
        }
}

/**
 * Companion object with utility functions.
 */
object LiveMatchDataUtils {
    /**
     * Creates empty live match data for loading/error states.
     */
    fun empty(fixtureId: Int): LiveMatchData {
        return LiveMatchData(
            fixtureId = fixtureId,
            status = MatchStatus.SCHEDULED
        )
    }

    /**
     * Creates live match data from match detail.
     */
    fun fromMatchDetail(matchDetail: MatchDetail): LiveMatchData {
        return LiveMatchData(
            fixtureId = matchDetail.fixtureId,
            status = matchDetail.status ?: MatchStatus.SCHEDULED,
            homeScore = matchDetail.score?.home ?: 0,
            awayScore = matchDetail.score?.away ?: 0,
            homeHalftimeScore = matchDetail.score?.halftime?.home,
            awayHalftimeScore = matchDetail.score?.halftime?.away,
            events = matchDetail.events.map { event ->
                LiveEvent(
                    id = 0, // Will be populated from API
                    type = event.type,
                    minute = event.minute,
                    extraMinute = event.extraMinute,
                    team = event.team,
                    player = event.player,
                    assist = event.assist,
                    detail = event.detail,
                    comments = event.comments
                )
            },
            statistics = matchDetail.stats.map { stat ->
                LiveStatistic(
                    type = stat.type,
                    homeValue = stat.homeValue,
                    awayValue = stat.awayValue,
                    unit = stat.unit
                )
            }
        )
    }

    /**
     * Check if live data should be refreshed based on last update.
     */
    fun shouldRefresh(lastUpdated: Long, intervalSeconds: Int = 30): Boolean {
        val currentTime = System.currentTimeMillis()
        val intervalMillis = intervalSeconds * 1000L
        return (currentTime - lastUpdated) > intervalMillis
    }
}
