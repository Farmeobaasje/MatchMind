package com.Lyno.matchmindai.domain.model

/**
 * Domain models for team and fixture data from API-SPORTS.
 * These are pure Kotlin classes with no Android dependencies.
 */

/**
 * Represents a football team with its basic information.
 */
data class Team(
    val id: Int,
    val name: String,
    val logoUrl: String? = null,
    val winner: Boolean? = null
) {
    /**
     * The team's initials for fallback display.
     */
    val initials: String
        get() = name.take(2).uppercase()
}

/**
 * Container for home and away teams in a fixture.
 */
data class TeamsInfo(
    val home: Team,
    val away: Team
)

/**
 * Basic fixture information.
 */
data class FixtureInfo(
    val id: Int,
    val date: String,
    val status: FixtureStatus,
    val venue: Venue? = null
)

/**
 * Venue information for a fixture.
 */
data class Venue(
    val id: Int? = null,
    val name: String,
    val city: String? = null
)

/**
 * Status of a fixture.
 */
data class FixtureStatus(
    val short: String, // "NS", "1H", "HT", "2H", "FT", "ET", "PEN", "SUSP", "PST", "CAN"
    val long: String,
    val elapsed: Int? = null
)

/**
 * Goals information for a fixture.
 */
data class GoalsInfo(
    val home: Int? = null,
    val away: Int? = null
)

/**
 * Complete fixture/match information from API-SPORTS.
 */
data class ApiMatchFixture(
    val fixture: FixtureInfo,
    val teams: TeamsInfo,
    val goals: GoalsInfo,
    val league: LeagueInfo? = null
)

/**
 * League information.
 */
data class LeagueInfo(
    val id: Int,
    val name: String,
    val country: String,
    val logoUrl: String? = null,
    val season: Int
)

/**
 * Search result for teams.
 */
data class TeamSearchResult(
    val query: String,
    val results: List<Team>
)

/**
 * Individual team search result for team selection.
 */
data class TeamSelectionResult(
    val teamId: Int,
    val teamName: String,
    val country: String,
    val logoUrl: String? = null,
    val leagueId: Int? = null,
    val leagueName: String? = null,
    val isFavorite: Boolean = false
) {
    /**
     * Gets the display name for the team.
     */
    fun getDisplayName(): String {
        return teamName
    }
    
    /**
     * Gets the full display name with country and league.
     */
    fun getFullDisplayName(): String {
        return if (leagueName != null) {
            "$teamName ($leagueName, $country)"
        } else {
            "$teamName ($country)"
        }
    }
    
    /**
     * Gets flag emoji for the country.
     */
    fun getFlagEmojiForCountry(): String {
        return when (country.uppercase()) {
            "ENGLAND", "UK", "GB" -> "🏴󠁧󠁢󠁥󠁮󠁧󠁿"
            "SPAIN", "ES" -> "🇪🇸"
            "GERMANY", "DE" -> "🇩🇪"
            "ITALY", "IT" -> "🇮🇹"
            "FRANCE", "FR" -> "🇫🇷"
            "NETHERLANDS", "NL" -> "🇳🇱"
            "PORTUGAL", "PT" -> "🇵🇹"
            "BELGIUM", "BE" -> "🇧🇪"
            "SCOTLAND" -> "🏴󠁧󠁢󠁳󠁣󠁴󠁿"
            "WALES" -> "🏴󠁧󠁢󠁷󠁬󠁳󠁿"
            else -> "🏴"
        }
    }
}

/**
 * Represents a match with team information for prediction.
 */
data class MatchWithTeams(
    val homeTeam: Team,
    val awayTeam: Team,
    val fixture: FixtureInfo? = null
) {
    /**
     * Creates a simple description of the match.
     */
    val description: String
        get() = "${homeTeam.name} vs ${awayTeam.name}"
}
