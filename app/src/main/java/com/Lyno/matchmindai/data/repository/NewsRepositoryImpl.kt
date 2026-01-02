package com.Lyno.matchmindai.data.repository

import com.Lyno.matchmindai.data.remote.rss.RssItem
import com.Lyno.matchmindai.data.remote.rss.RssParser
import com.Lyno.matchmindai.domain.model.NewsItemData
import com.Lyno.matchmindai.domain.repository.NewsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Implementation of NewsRepository that fetches football news from RSS feeds.
 * Part of Project Chimera - replacing TavilyApi with free RSS feeds.
 *
 * Uses the "Chimera List" of football RSS feeds from major sources worldwide.
 */
class NewsRepositoryImpl @Inject constructor(
    private val rssParser: RssParser
) : NewsRepository {

    /**
     * The "Chimera List" of football RSS feeds.
     * Each entry is a Pair of (sourceName, feedUrl)
     */
    private val rssFeeds = listOf(
        "BBC" to "http://feeds.bbci.co.uk/sport/football/rss.xml",
        "ESPN" to "https://www.espn.com/espn/rss/soccer/news",
        "Marca" to "https://e00-marca.uecdn.es/rss/futbol.xml",
        "VoetbalZone" to "https://www.voetbalzone.nl/rss/rss.xml",
        "Kicker" to "https://www.kicker.de/_feeds/rss/fussball/bundesliga/index.rss",
        "Gazzetta" to "https://www.gazzetta.it/rss/calcio.xml",
        "L'Equipe" to "https://www.lequipe.fr/rss/actu_rss_Football.xml"
    )

    /**
     * Team-specific RSS feeds for major European clubs.
     * Maps team names to their specific RSS feed URLs.
     * Part of Project ChiChi: Team-specific news aggregation.
     */
    private val teamSpecificRssFeeds = mapOf(
        // Premier League
        "Manchester United" to listOf(
            "https://www.skysports.com/rss/12040/team/manchester-united",
            "https://www.bbc.co.uk/sport/football/teams/manchester-united/rss"
        ),
        "Manchester City" to listOf(
            "https://www.skysports.com/rss/12040/team/manchester-city",
            "https://www.bbc.co.uk/sport/football/teams/manchester-city/rss"
        ),
        "Liverpool" to listOf(
            "https://www.skysports.com/rss/12040/team/liverpool",
            "https://www.bbc.co.uk/sport/football/teams/liverpool/rss"
        ),
        "Chelsea" to listOf(
            "https://www.skysports.com/rss/12040/team/chelsea",
            "https://www.bbc.co.uk/sport/football/teams/chelsea/rss"
        ),
        "Arsenal" to listOf(
            "https://www.skysports.com/rss/12040/team/arsenal",
            "https://www.bbc.co.uk/sport/football/teams/arsenal/rss"
        ),
        "Tottenham" to listOf(
            "https://www.skysports.com/rss/12040/team/tottenham-hotspur",
            "https://www.bbc.co.uk/sport/football/teams/tottenham-hotspur/rss"
        ),
        
        // La Liga
        "Real Madrid" to listOf(
            "https://e00-marca.uecdn.es/rss/futbol/real-madrid.xml",
            "https://as.com/rss/tags/real_madrid.xml"
        ),
        "Barcelona" to listOf(
            "https://e00-marca.uecdn.es/rss/futbol/barcelona.xml",
            "https://as.com/rss/tags/fc_barcelona.xml"
        ),
        "Atletico Madrid" to listOf(
            "https://e00-marca.uecdn.es/rss/futbol/atletico-madrid.xml",
            "https://as.com/rss/tags/atletico_madrid.xml"
        ),
        
        // Bundesliga
        "Bayern Munich" to listOf(
            "https://www.kicker.de/_feeds/rss/fussball/bundesliga/vereine/bayern-muenchen/index.rss",
            "https://www.bundesliga.com/en/feed/news/rss/team/bayern-munich"
        ),
        "Borussia Dortmund" to listOf(
            "https://www.kicker.de/_feeds/rss/fussball/bundesliga/vereine/borussia-dortmund/index.rss",
            "https://www.bundesliga.com/en/feed/news/rss/team/borussia-dortmund"
        ),
        
        // Serie A
        "Juventus" to listOf(
            "https://www.gazzetta.it/rss/calcio/squadre/juventus.xml",
            "https://www.corrieredellosport.it/rss/calcio/serie-a/juventus"
        ),
        "AC Milan" to listOf(
            "https://www.gazzetta.it/rss/calcio/squadre/milan.xml",
            "https://www.corrieredellosport.it/rss/calcio/serie-a/milan"
        ),
        "Inter Milan" to listOf(
            "https://www.gazzetta.it/rss/calcio/squadre/inter.xml",
            "https://www.corrieredellosport.it/rss/calcio/serie-a/inter"
        ),
        
        // Eredivisie
        "Ajax" to listOf(
            "https://www.ajax.nl/rss/nieuws/",
            "https://www.vi.nl/rss/teams/ajax"
        ),
        "PSV" to listOf(
            "https://www.psv.nl/rss/nieuws/",
            "https://www.vi.nl/rss/teams/psv"
        ),
        "Feyenoord" to listOf(
            "https://www.feyenoord.nl/rss/nieuws/",
            "https://www.vi.nl/rss/teams/feyenoord"
        )
    )

    /**
     * Fetches relevant news for two teams from multiple RSS feeds.
     * Aggregates news from all feeds in parallel, filters for team relevance,
     * and returns a formatted digest string.
     * 
     * UPGRADE: Now tries Google News RSS first for match-specific news,
     * falls back to generic feeds if Google News returns no results.
     * 
     * CHICHI UPGRADE: Uses team-specific RSS feeds for major clubs
     * combined with generic feeds for comprehensive coverage.
     */
    override suspend fun fetchTeamNews(teamA: String, teamB: String): String =
        withContext(Dispatchers.IO) {
            try {
                // STEP 1: Try Google News RSS first (most relevant)
                val googleNewsItems = try {
                    fetchGoogleNewsForMatch(teamA, teamB)
                } catch (e: Exception) {
                    // Google News might rate limit or fail - continue with fallback
                    emptyList()
                }

                // STEP 2: If Google News returned items, use them
                if (googleNewsItems.isNotEmpty()) {
                    val relevantGoogleItems = googleNewsItems.filter { item ->
                        RssItem.isRelevantForTeams(item, teamA, teamB)
                    }
                    val sortedGoogleItems = relevantGoogleItems.sortedByDescending { it.pubDate }
                    val limitedGoogleItems = sortedGoogleItems.take(10)
                    
                    if (limitedGoogleItems.isNotEmpty()) {
                        return@withContext formatNewsDigest(limitedGoogleItems, "GoogleNews")
                    }
                }

                // STEP 3: Fetch from team-specific feeds (ChiChi feature)
                val teamSpecificItems = fetchTeamSpecificFeeds(teamA, teamB)

                // STEP 4: Fetch from generic RSS feeds as fallback
                val genericItems = fetchAllFeedsInParallel()

                // Combine all items
                val allItems = teamSpecificItems + genericItems

                // Filter for relevant items
                val relevantItems = allItems.filter { item ->
                    RssItem.isRelevantForTeams(item, teamA, teamB)
                }

                // Sort by date (most recent first) and limit to 10 items
                val sortedItems = relevantItems.sortedByDescending { it.pubDate }
                val limitedItems = sortedItems.take(10)

                // Format the digest
                val sourceType = if (teamSpecificItems.isNotEmpty()) "TeamSpecific+GenericRSS" else "GenericRSS"
                formatNewsDigest(limitedItems, sourceType)
            } catch (e: Exception) {
                // Log error in production, return empty string to avoid breaking the flow
                ""
            }
        }

    /**
     * Fetches all recent news items from all feeds.
     * Returns domain model NewsItemData for display in the app.
     */
    override suspend fun fetchAllNews(): List<NewsItemData> =
        withContext(Dispatchers.IO) {
            try {
                val allItems = fetchAllFeedsInParallel()
                
                // Sort by date (most recent first)
                val sortedItems = allItems.sortedByDescending { it.pubDate }
                
                // Limit to 20 items for performance
                val limitedItems = sortedItems.take(20)
                
                // Map to domain model
                limitedItems.map { rssItem ->
                    NewsItemData(
                        headline = rssItem.title,
                        source = rssItem.source,
                        url = "", // RSS items don't have URLs in our current model
                        snippet = if (rssItem.description.length > 100) {
                            rssItem.description.take(97) + "..."
                        } else {
                            rssItem.description
                        },
                        publishedDate = formatRelativeDate(rssItem.pubDate)
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }
        }

    /**
     * Builds Google News RSS URL for a specific match.
     */
    private fun buildGoogleNewsRssUrl(homeTeam: String, awayTeam: String): String {
        val query = "$homeTeam vs $awayTeam injury team news"
        val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
        return "https://news.google.com/rss/search?q=$encodedQuery&hl=en-US&gl=US&ceid=US:en"
    }

    /**
     * Fetches Google News RSS feed for a specific match.
     */
    private suspend fun fetchGoogleNewsForMatch(homeTeam: String, awayTeam: String): List<RssItem> {
        val googleNewsUrl = buildGoogleNewsRssUrl(homeTeam, awayTeam)
        return fetchSingleFeed(googleNewsUrl, "GoogleNews")
    }

    /**
     * Fetches all RSS feeds in parallel using coroutines.
     * If a feed fails, it's silently ignored and other feeds continue.
     */
    private suspend fun fetchAllFeedsInParallel(): List<RssItem> = coroutineScope {
        val deferredResults = rssFeeds.map { (sourceName, feedUrl) ->
            async {
                try {
                    fetchSingleFeed(feedUrl, sourceName)
                } catch (e: Exception) {
                    // Log error in production, return empty list for this feed
                    emptyList()
                }
            }
        }

        // Wait for all feeds to complete and flatten the results
        deferredResults.flatMap { it.await() }
    }

    /**
     * Fetches and parses a single RSS feed.
     */
    private suspend fun fetchSingleFeed(feedUrl: String, sourceName: String): List<RssItem> {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(feedUrl)
            connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                connectTimeout = 10000 // 10 seconds
                readTimeout = 10000 // 10 seconds
                setRequestProperty("User-Agent", "MatchMindAI/1.0")
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.use { inputStream ->
                    rssParser.parse(inputStream, sourceName)
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * Formats a list of RSS items into a digest string.
     * Format: "[Source: GoogleNews] Title: ... | Date: ... | Summary: ..."
     */
    private fun formatNewsDigest(items: List<RssItem>, source: String): String {
        if (items.isEmpty()) {
            return "Geen recent nieuws gevonden voor deze teams."
        }

        return items.joinToString("\n\n") { item ->
            "[Source: $source] Title: ${item.title} | Date: ${item.pubDate} | Summary: ${item.description}"
        }
    }

    /**
     * Formats a date string to a relative time (e.g., "2 uur geleden").
     * Falls back to original date if parsing fails.
     */
    private fun formatRelativeDate(dateString: String): String {
        return try {
            val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH)
            val date = dateFormat.parse(dateString) ?: return dateString
            
            val now = Date()
            val diffInMillis = now.time - date.time
            
            when {
                diffInMillis < 60000 -> "Zojuist"
                diffInMillis < 3600000 -> "${diffInMillis / 60000} minuten geleden"
                diffInMillis < 86400000 -> "${diffInMillis / 3600000} uur geleden"
                diffInMillis < 604800000 -> "${diffInMillis / 86400000} dagen geleden"
                else -> dateString
            }
        } catch (e: Exception) {
            dateString
        }
    }

    /**
     * Fetches RSS feeds specific to the given teams.
     * Part of Project ChiChi: Team-specific news aggregation.
     * 
     * @param teamA First team name
     * @param teamB Second team name
     * @return List of RSS items from team-specific feeds
     */
    private suspend fun fetchTeamSpecificFeeds(teamA: String, teamB: String): List<RssItem> = coroutineScope {
        // Get team-specific feed URLs for both teams
        val teamAFeeds = teamSpecificRssFeeds[teamA] ?: emptyList()
        val teamBFeeds = teamSpecificRssFeeds[teamB] ?: emptyList()
        
        // Combine and deduplicate feed URLs
        val allTeamFeeds = (teamAFeeds + teamBFeeds).distinct()
        
        if (allTeamFeeds.isEmpty()) {
            return@coroutineScope emptyList()
        }
        
        // Fetch all team-specific feeds in parallel
        val deferredResults = allTeamFeeds.map { feedUrl ->
            async {
                try {
                    // Use team name as source name for better tracking
                    val sourceName = when {
                        teamAFeeds.contains(feedUrl) -> "Team-$teamA"
                        teamBFeeds.contains(feedUrl) -> "Team-$teamB"
                        else -> "TeamSpecific"
                    }
                    fetchSingleFeed(feedUrl, sourceName)
                } catch (e: Exception) {
                    // Log error in production, return empty list for this feed
                    emptyList()
                }
            }
        }
        
        // Wait for all feeds to complete and flatten the results
        deferredResults.flatMap { it.await() }
    }

    /**
     * Gets RSS feed URLs for a specific team.
     * Part of Project ChiChi: Team-specific news aggregation.
     * 
     * @param teamName The name of the team
     * @return List of RSS feed URLs for the team, or empty list if not found
     */
    fun getRssUrlsForTeam(teamName: String): List<String> {
        return teamSpecificRssFeeds[teamName] ?: emptyList()
    }

    /**
     * Fetches news for a list of teams.
     * Used by FavoX Hub to get news for favorite teams.
     * 
     * @param teamNames List of team names to fetch news for
     * @return List of news items relevant to the specified teams
     */
    override suspend fun getNewsForTeams(teamNames: List<String>): List<com.Lyno.matchmindai.domain.model.NewsItemData> =
        withContext(Dispatchers.IO) {
            try {
                if (teamNames.isEmpty()) {
                    return@withContext emptyList()
                }

                // STEP 1: Fetch team-specific feeds for all teams
                val teamSpecificItems = fetchTeamSpecificFeedsForMultipleTeams(teamNames)

                // STEP 2: Fetch from generic RSS feeds as fallback
                val genericItems = fetchAllFeedsInParallel()

                // Combine all items
                val allItems = teamSpecificItems + genericItems

                // Filter for relevant items (any of the teams)
                val relevantItems = allItems.filter { item ->
                    teamNames.any { teamName ->
                        RssItem.isRelevantForTeam(item, teamName)
                    }
                }

                // Sort by date (most recent first) and limit to 15 items
                val sortedItems = relevantItems.sortedByDescending { it.pubDate }
                val limitedItems = sortedItems.take(15)

                // Map to domain model
                limitedItems.map { rssItem ->
                    com.Lyno.matchmindai.domain.model.NewsItemData(
                        headline = rssItem.title,
                        source = rssItem.source,
                        url = "", // RSS items don't have URLs in our current model
                        snippet = if (rssItem.description.length > 100) {
                            rssItem.description.take(97) + "..."
                        } else {
                            rssItem.description
                        },
                        publishedDate = formatRelativeDate(rssItem.pubDate)
                    )
                }
            } catch (e: Exception) {
                // Log error in production, return empty list to avoid breaking the flow
                emptyList()
            }
        }

    /**
     * Fetches news for a list of favorite team IDs.
     * Used by FavoX Hub to get news for favorite teams based on team IDs.
     * 
     * @param teamIds List of team IDs to fetch news for
     * @param teamIdToNameMap Map of team IDs to team names
     * @return List of news items relevant to the specified teams
     */
    override suspend fun getNewsForFavoriteTeams(
        teamIds: List<Int>,
        teamIdToNameMap: Map<Int, String>
    ): List<com.Lyno.matchmindai.domain.model.NewsItemData> =
        withContext(Dispatchers.IO) {
            try {
                if (teamIds.isEmpty()) {
                    return@withContext emptyList()
                }

                // Convert team IDs to team names
                val teamNames = teamIds.mapNotNull { teamId ->
                    teamIdToNameMap[teamId]
                }

                if (teamNames.isEmpty()) {
                    return@withContext emptyList()
                }

                // Use existing method to fetch news for team names
                getNewsForTeams(teamNames)
            } catch (e: Exception) {
                // Log error in production, return empty list to avoid breaking the flow
                emptyList()
            }
        }

    /**
     * Fetches RSS feeds specific to multiple teams.
     * 
     * @param teamNames List of team names
     * @return List of RSS items from team-specific feeds
     */
    private suspend fun fetchTeamSpecificFeedsForMultipleTeams(teamNames: List<String>): List<RssItem> = coroutineScope {
        // Get team-specific feed URLs for all teams
        val allTeamFeeds = teamNames.flatMap { teamName ->
            teamSpecificRssFeeds[teamName] ?: emptyList()
        }.distinct()

        if (allTeamFeeds.isEmpty()) {
            return@coroutineScope emptyList()
        }

        // Fetch all team-specific feeds in parallel
        val deferredResults = allTeamFeeds.map { feedUrl ->
            async {
                try {
                    // Determine which team this feed belongs to
                    val sourceTeam = teamNames.firstOrNull { teamName ->
                        teamSpecificRssFeeds[teamName]?.contains(feedUrl) == true
                    } ?: "TeamSpecific"
                    
                    val sourceName = "Team-$sourceTeam"
                    fetchSingleFeed(feedUrl, sourceName)
                } catch (e: Exception) {
                    // Log error in production, return empty list for this feed
                    emptyList()
                }
            }
        }

        // Wait for all feeds to complete and flatten the results
        deferredResults.flatMap { it.await() }
    }
}
