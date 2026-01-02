package com.Lyno.matchmindai.domain.service

import com.Lyno.matchmindai.domain.model.MatchFixture

/**
 * Data class representing a curated feed of football matches.
 * This is the output of the MatchCuratorService, organizing matches
 * into prioritized categories for the dashboard UI.
 */
data class CuratedFeed(
    /**
     * The hero match - the most exciting/important match of the day.
     * This will be displayed prominently at the top of the dashboard.
     */
    val heroMatch: MatchFixture? = null,

    /**
     * Matches that are currently live (in play).
     * These will be shown in a horizontal ticker for quick access.
     */
    val liveMatches: List<MatchFixture> = emptyList(),

    /**
     * Upcoming matches that haven't started yet.
     * These are organized by league/priority for the main feed.
     */
    val upcomingMatches: List<MatchFixture> = emptyList()
) {
    /**
     * Total number of matches in the curated feed.
     */
    val totalMatches: Int
        get() = (if (heroMatch != null) 1 else 0) + liveMatches.size + upcomingMatches.size

    /**
     * Check if the curated feed has any content.
     */
    val isEmpty: Boolean
        get() = heroMatch == null && liveMatches.isEmpty() && upcomingMatches.isEmpty()
}
