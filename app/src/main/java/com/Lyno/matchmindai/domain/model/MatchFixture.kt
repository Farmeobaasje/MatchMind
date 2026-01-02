package com.Lyno.matchmindai.domain.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing a football match fixture.
 * This is a pure Kotlin class with no Android dependencies.
 */
@Serializable
data class MatchFixture(
    val homeTeam: String,
    val awayTeam: String,
    val time: String,      // e.g., "13-12 00:00" or "14:30"
    val league: String,    // e.g., "Eredivisie"
    val date: String = "",  // Optional: e.g., "Zo 14 dec" (backward compatibility)
    val status: String? = null,  // e.g., "LIVE", "1H", "HT", "2H", "FT"
    val elapsed: Int? = null,   // Minutes elapsed for live matches
    val homeScore: Int? = null,  // Home team score
    val awayScore: Int? = null,  // Away team score
    val fixtureId: Int? = null,   // API fixture ID for detailed requests
    val leagueId: Int? = null,    // League ID for grouping
    val leagueCountry: String? = null, // League country
    val leagueLogo: String? = null,     // League logo URL
    val homeTeamId: Int? = null,        // Home team ID for logo loading
    val awayTeamId: Int? = null         // Away team ID for logo loading
) {
    /**
     * Display name for the match in format "Home vs Away".
     */
    val displayName: String
        get() = "$homeTeam vs $awayTeam"

    /**
     * Compact display format for cards.
     */
    val compactDisplay: String
        get() = if (date.isNotEmpty()) {
            "$date $time | $homeTeam - $awayTeam | $league"
        } else {
            "$time | $homeTeam - $awayTeam | $league"
        }

    companion object {
        /**
         * Creates an invalid fixture for error cases.
         */
        fun invalid(): MatchFixture = MatchFixture(
            homeTeam = "",
            awayTeam = "",
            time = "",
            league = ""
        )
    }
}
