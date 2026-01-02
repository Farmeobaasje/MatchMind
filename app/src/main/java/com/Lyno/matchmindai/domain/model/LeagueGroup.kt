package com.Lyno.matchmindai.domain.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing a group of matches organized by league.
 * Used for displaying matches grouped by competition in a FlashScore-like interface.
 */
@Serializable
data class LeagueGroup(
    val leagueId: Int,
    val leagueName: String,
    val country: String,
    val logoUrl: String,
    val matches: List<MatchFixture>,
    var isExpanded: Boolean = false
) {
    /**
     * Display name for the league group.
     */
    val displayName: String
        get() = "$leagueName ($country)"

    /**
     * Number of matches in this league group.
     */
    val matchCount: Int
        get() = matches.size

    /**
     * Toggle the expanded state of this league group.
     */
    fun toggleExpanded() {
        isExpanded = !isExpanded
    }

    companion object {
        /**
         * Creates an empty league group for error cases.
         */
        fun empty(): LeagueGroup = LeagueGroup(
            leagueId = 0,
            leagueName = "",
            country = "",
            logoUrl = "",
            matches = emptyList()
        )

        /**
         * Priority order for leagues to determine sorting.
         * Higher priority leagues appear first.
         */
        val LEAGUE_PRIORITY = mapOf(
            88 to 1,   // Eredivisie
            39 to 2,   // Premier League
            140 to 3,  // La Liga
            78 to 4,   // Bundesliga
            135 to 5,  // Serie A
            61 to 6    // Ligue 1
        )

        /**
         * Get priority for a league ID.
         * @param leagueId The league ID
         * @return Priority value (lower = higher priority), or Int.MAX_VALUE if not in priority list
         */
        fun getLeaguePriority(leagueId: Int): Int {
            return LEAGUE_PRIORITY[leagueId] ?: Int.MAX_VALUE
        }
    }
}
