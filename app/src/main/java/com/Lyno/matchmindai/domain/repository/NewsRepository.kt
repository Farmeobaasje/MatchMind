package com.Lyno.matchmindai.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for fetching football news from RSS feeds.
 * Part of Project Chimera - replacing TavilyApi with free RSS feeds.
 */
interface NewsRepository {
    /**
     * Fetches relevant news for two teams from multiple RSS feeds.
     * Aggregates news from the "Chimera List" of football RSS feeds.
     *
     * @param teamA First team name
     * @param teamB Second team name
     * @return Formatted string digest of relevant news items
     */
    suspend fun fetchTeamNews(teamA: String, teamB: String): String

    /**
     * Fetches all recent news items from all feeds.
     * Useful for displaying a general news feed in the app.
     *
     * @return List of news items from all feeds
     */
    suspend fun fetchAllNews(): List<com.Lyno.matchmindai.domain.model.NewsItemData>

    /**
     * Fetches news for a list of teams.
     * Used by FavoX Hub to get news for favorite teams.
     *
     * @param teamNames List of team names to fetch news for
     * @return List of news items relevant to the specified teams
     */
    suspend fun getNewsForTeams(teamNames: List<String>): List<com.Lyno.matchmindai.domain.model.NewsItemData>

    /**
     * Fetches news for a list of favorite team IDs.
     * Used by FavoX Hub to get news for favorite teams based on team IDs.
     *
     * @param teamIds List of team IDs to fetch news for
     * @param teamIdToNameMap Map of team IDs to team names
     * @return List of news items relevant to the specified teams
     */
    suspend fun getNewsForFavoriteTeams(
        teamIds: List<Int>,
        teamIdToNameMap: Map<Int, String>
    ): List<com.Lyno.matchmindai.domain.model.NewsItemData>
}
