package com.Lyno.matchmindai.presentation.components

/**
 * Sealed class representing different types of API-Sports football widgets.
 * Each widget type has specific required parameters.
 */
sealed class FootballWidgetType(val typeName: String) {
    /**
     * Games widget - displays list of football matches.
     * Optional parameters: data-date, data-league, data-country, data-refresh, etc.
     */
    object Games : FootballWidgetType("games")

    /**
     * Game widget - displays detailed information about a specific match.
     * @param fixtureId Required fixture ID for the match
     */
    data class Game(val fixtureId: Int) : FootballWidgetType("game")

    /**
     * Standings widget - displays league standings.
     * @param leagueId Required league ID
     * @param season Required season year
     */
    data class Standings(val leagueId: Int, val season: Int) : FootballWidgetType("standings")

    /**
     * Head-to-Head widget - displays historical matches between two teams.
     * @param teamId1 First team ID
     * @param teamId2 Second team ID
     */
    data class H2H(val teamId1: Int, val teamId2: Int) : FootballWidgetType("h2h")

    /**
     * Team widget - displays detailed team information.
     * @param teamId Required team ID
     */
    data class Team(val teamId: Int) : FootballWidgetType("team")

    /**
     * Player widget - displays player information and statistics.
     * @param playerId Required player ID
     */
    data class Player(val playerId: Int) : FootballWidgetType("player")

    /**
     * Helper function to get default parameters for each widget type.
     */
    fun getDefaultParameters(): Map<String, String> = when (this) {
        is Games -> mapOf(
            "data-refresh" to "30",
            "data-show-toolbar" to "false",
            "data-games-style" to "1"
        )
        is Game -> mapOf(
            "data-refresh" to "30",
            "data-game-tab" to "statistics",
            "data-show-toolbar" to "false"
        )
        is Standings -> mapOf(
            "data-show-toolbar" to "false"
        )
        is H2H -> mapOf(
            "data-show-toolbar" to "false"
        )
        is Team -> mapOf(
            "data-team-tab" to "statistics",
            "data-show-toolbar" to "false"
        )
        is Player -> mapOf(
            "data-player-statistics" to "true",
            "data-show-toolbar" to "false"
        )
    }

    /**
     * Get display name for UI.
     */
    fun getDisplayName(): String = when (this) {
        is Games -> "Wedstrijden"
        is Game -> "Wedstrijd Details"
        is Standings -> "Ranglijst"
        is H2H -> "Head-to-Head"
        is Team -> "Team Info"
        is Player -> "Speler Info"
    }

    /**
     * Get icon resource for UI (placeholder - would need actual icons).
     */
    fun getIconName(): String = when (this) {
        is Games -> "list"
        is Game -> "sports_soccer"
        is Standings -> "leaderboard"
        is H2H -> "compare"
        is Team -> "groups"
        is Player -> "person"
    }
}

/**
 * Extension function to create widget parameters for Copa del Rey matches.
 */
fun FootballWidgetType.withCopaDelReyConfig(): Map<String, String> {
    val baseParams = this.getDefaultParameters().toMutableMap()
    
    when (this) {
        is FootballWidgetType.Games -> {
            baseParams["data-league"] = "143" // Copa del Rey league ID
            baseParams["data-tab"] = "live"
        }
        is FootballWidgetType.Game -> {
            // Add Copa del Rey specific game parameters
            baseParams["data-game-tab"] = "statistics"
            baseParams["data-events"] = "true"
        }
        else -> {
            // No special config for other widget types
        }
    }
    
    return baseParams
}

/**
 * Extension function to create widget parameters for Eredivisie matches.
 */
fun FootballWidgetType.withEredivisieConfig(): Map<String, String> {
    val baseParams = this.getDefaultParameters().toMutableMap()
    
    when (this) {
        is FootballWidgetType.Games -> {
            baseParams["data-league"] = "88" // Eredivisie league ID
        }
        is FootballWidgetType.Standings -> {
            baseParams["data-league"] = "88"
            baseParams["data-season"] = "2025"
        }
        else -> {
            // No special config for other widget types
        }
    }
    
    return baseParams
}

/**
 * Extension function to create widget parameters for Premier League matches.
 */
fun FootballWidgetType.withPremierLeagueConfig(): Map<String, String> {
    val baseParams = this.getDefaultParameters().toMutableMap()
    
    when (this) {
        is FootballWidgetType.Games -> {
            baseParams["data-league"] = "39" // Premier League ID
        }
        is FootballWidgetType.Standings -> {
            baseParams["data-league"] = "39"
            baseParams["data-season"] = "2025"
        }
        else -> {
            // No special config for other widget types
        }
    }
    
    return baseParams
}
