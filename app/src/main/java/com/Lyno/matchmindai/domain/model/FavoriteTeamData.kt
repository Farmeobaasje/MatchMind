package com.Lyno.matchmindai.domain.model

/**
 * Centralized data model for favorite team information in the FavoX hub.
 * Combines all relevant data for a favorite team into a single, nullable-safe structure.
 *
 * @property teamId The unique ID of the team (required)
 * @property teamName The name of the team (nullable with default)
 * @property teamLogoUrl The URL of the team's logo (nullable)
 * @property news List of recent news items for the team (nullable with default)
 * @property nextMatch The next scheduled match for the team (nullable)
 * @property standings Current league standings for the team's league (nullable)
 * @property hasStandings Boolean indicating if standings are available (false for friendlies)
 * @property leagueId The ID of the team's current league (nullable)
 * @property leagueName The name of the team's current league (nullable)
 */
data class FavoriteTeamData(
    val teamId: Int,
    val teamName: String? = null,
    val teamLogoUrl: String? = null,
    val news: List<NewsItemData>? = null,
    val nextMatch: MatchFixture? = null,
    val standings: List<StandingRow>? = null,
    val hasStandings: Boolean = false,
    val leagueId: Int? = null,
    val leagueName: String? = null
) {
    /**
     * Creates a default instance with safe null values.
     */
    companion object {
        fun default(teamId: Int, teamName: String? = null, teamLogoUrl: String? = null): FavoriteTeamData {
            return FavoriteTeamData(
                teamId = teamId,
                teamName = teamName,
                teamLogoUrl = teamLogoUrl,
                news = emptyList(),
                nextMatch = null,
                standings = null,
                hasStandings = false,
                leagueId = null,
                leagueName = null
            )
        }
    }

    /**
     * Returns a user-friendly display name for the team.
     * Falls back to "Team [ID]" if name is null.
     */
    fun getDisplayName(): String {
        return teamName ?: "Team $teamId"
    }

    /**
     * Checks if the team has any news available.
     */
    fun hasNews(): Boolean {
        return !news.isNullOrEmpty()
    }

    /**
     * Gets the first 3 news items for display.
     */
    fun getLimitedNews(): List<NewsItemData> {
        return news?.take(3) ?: emptyList()
    }

    /**
     * Checks if the team has a next match scheduled.
     */
    fun hasNextMatch(): Boolean {
        return nextMatch != null
    }

    /**
     * Checks if the team has standings data available.
     * Returns true only if standings are non-null AND hasStandings is true.
     */
    fun hasValidStandings(): Boolean {
        return hasStandings && !standings.isNullOrEmpty()
    }

    /**
     * Creates a copy with updated team name.
     */
    fun withTeamName(name: String): FavoriteTeamData {
        return copy(teamName = name)
    }

    /**
     * Creates a copy with updated team logo URL.
     */
    fun withTeamLogoUrl(logoUrl: String?): FavoriteTeamData {
        return copy(teamLogoUrl = logoUrl)
    }

    /**
     * Creates a copy with updated news.
     */
    fun withNews(newsList: List<NewsItemData>?): FavoriteTeamData {
        return copy(news = newsList)
    }

    /**
     * Creates a copy with updated next match.
     */
    fun withNextMatch(match: MatchFixture?): FavoriteTeamData {
        return copy(nextMatch = match)
    }

    /**
     * Creates a copy with updated standings.
     */
    fun withStandings(
        standingsList: List<StandingRow>?,
        leagueId: Int? = null,
        leagueName: String? = null
    ): FavoriteTeamData {
        return copy(
            standings = standingsList,
            hasStandings = !standingsList.isNullOrEmpty(),
            leagueId = leagueId ?: this.leagueId,
            leagueName = leagueName ?: this.leagueName
        )
    }

    /**
     * Creates a copy marking this as a friendlies league (no standings).
     */
    fun asFriendliesLeague(): FavoriteTeamData {
        return copy(
            standings = null,
            hasStandings = false,
            leagueName = leagueName ?: "Vriendschappelijke competitie"
        )
    }
}

/**
 * Extension function to convert a list of FavoriteTeamData to a map by team ID.
 */
fun List<FavoriteTeamData>.toMapByTeamId(): Map<Int, FavoriteTeamData> {
    return associateBy { it.teamId }
}

/**
 * Extension function to get team IDs from a list of FavoriteTeamData.
 */
fun List<FavoriteTeamData>.getTeamIds(): List<Int> {
    return map { it.teamId }
}

/**
 * Extension function to filter FavoriteTeamData that have valid standings.
 */
fun List<FavoriteTeamData>.filterWithStandings(): List<FavoriteTeamData> {
    return filter { it.hasValidStandings() }
}

/**
 * Extension function to filter FavoriteTeamData that have next matches.
 */
fun List<FavoriteTeamData>.filterWithNextMatches(): List<FavoriteTeamData> {
    return filter { it.hasNextMatch() }
}

/**
 * Extension function to filter FavoriteTeamData that have news.
 */
fun List<FavoriteTeamData>.filterWithNews(): List<FavoriteTeamData> {
    return filter { it.hasNews() }
}
