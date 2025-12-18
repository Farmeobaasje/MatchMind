package com.Lyno.matchmindai.data.remote.search

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Search service that replaces the unstable Jsoup-based WebScraper with Tavily AI Search API.
 * Provides high-quality, RAG-optimized search results for match context retrieval.
 *
 * Features:
 * - Professional search API (Tavily) instead of HTML scraping
 * - Focus-based query enhancement (stats/news/general)
 * - Built-in answer generation by Tavily
 * - Graceful degradation on API failures
 */
class SearchService(
    private val tavilyApi: TavilyApi
) {

    companion object {
        private const val TAG = "SearchService"
    }

    /**
     * Searches for match context information using Tavily AI Search API.
     * Replaces the old `scrapeMatchContext` method from WebScraper.
     *
     * @param homeTeam The home team name (used for team-specific queries)
     * @param awayTeam The away team name (used for team-specific queries)
     * @param focus The type of information to focus on: "stats" for scores/standings,
     *              "news" for injuries/lineups, "general" for mixed results
     * @return A string containing concatenated search result content and Tavily's answer,
     *         or fallback message if search fails
     */
    suspend fun searchContext(
        homeTeam: String,
        awayTeam: String,
        focus: String = "general",
        apiKey: String
    ): String {
        return withContext(Dispatchers.IO) {
            try {
                // Build search query based on focus
                val baseQuery = when (focus) {
                    "stats" -> "$homeTeam vs $awayTeam uitslagen stand statistieken"
                    "news" -> "$homeTeam vs $awayTeam blessures selectie nieuws"
                    else -> "$homeTeam vs $awayTeam last matches results scores"
                }
                
                Log.d(TAG, "Searching context for: $homeTeam vs $awayTeam (focus: $focus)")
                Log.d(TAG, "Query: $baseQuery")
                
                // Call Tavily API
                val result = tavilyApi.search(baseQuery, focus, apiKey)
                
                if (result.isSuccess) {
                    val response = result.getOrThrow()
                    val formattedResult = formatTavilyResponse(response, focus)
                    
                    Log.d(TAG, "Tavily search successful: ${response.results.size} results")
                    formattedResult
                } else {
                    Log.w(TAG, "Tavily search failed: ${result.exceptionOrNull()?.message}")
                    "Geen live data gevonden."
                }
                
            } catch (e: Exception) {
                // Log the error but don't crash - return fallback message
                Log.e(TAG, "Failed to search match context for $homeTeam vs $awayTeam (focus: $focus)", e)
                "Geen live data gevonden."
            }
        }
    }

    /**
     * Alternative method that accepts a generic query string (for tool calls).
     * Used when the AI provides a custom search query.
     *
     * @param query The search query from AI tool call
     * @param focus The type of information to focus on
     * @return Formatted search results or fallback message
     */
    suspend fun searchWithQuery(
        query: String,
        focus: String = "general",
        apiKey: String
    ): String {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Searching with custom query: '$query' (focus: $focus)")
                
                // Call Tavily API with the provided query
                val result = tavilyApi.search(query, focus, apiKey)
                
                if (result.isSuccess) {
                    val response = result.getOrThrow()
                    val formattedResult = formatTavilyResponse(response, focus)
                    
                    Log.d(TAG, "Tavily search successful: ${response.results.size} results")
                    formattedResult
                } else {
                    Log.w(TAG, "Tavily search failed: ${result.exceptionOrNull()?.message}")
                    "Geen live data gevonden voor query: $query"
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to search with query: '$query', focus: $focus", e)
                "Geen live data gevonden voor query: $query"
            }
        }
    }

    /**
     * Searches and returns raw TavilyResponse for structured data access.
     * Used when you need to work with individual search results directly.
     *
     * @param query The search query
     * @param focus The type of information to focus on
     * @param apiKey The Tavily API key
     * @return Result containing TavilyResponse with structured search results
     */
    suspend fun searchWithQueryRaw(
        query: String,
        focus: String = "general",
        apiKey: String
    ): Result<TavilyResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Searching with custom query (raw): '$query' (focus: $focus)")
                tavilyApi.search(query, focus, apiKey)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to search with query (raw): '$query', focus: $focus", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Formats Tavily response into a readable string for the AI.
     * Combines Tavily's generated answer with top search results.
     */
    private fun formatTavilyResponse(response: TavilyResponse, focus: String): String {
        val stringBuilder = StringBuilder()
        
        // Add Tavily's AI-generated answer (if available and not empty)
        response.answer?.let { answer ->
            if (answer.isNotBlank()) {
                stringBuilder.append("🤖 TAVILY ANSWER:\n")
                stringBuilder.append(answer.trim())
                stringBuilder.append("\n\n")
            }
        }
        
        // Add top search results
        stringBuilder.append("🔍 SEARCH RESULTS (${response.results.size}):\n")
        
        response.results.take(3).forEachIndexed { index, result ->
            stringBuilder.append("\n${index + 1}. ${result.title}\n")
            stringBuilder.append("   URL: ${result.url}\n")
            
            // Truncate content if too long
            val contentPreview = if (result.content.length > 300) {
                result.content.take(300) + "..."
            } else {
                result.content
            }
            
            stringBuilder.append("   Content: $contentPreview\n")
        }
        
        // Add focus-specific note
        when (focus) {
            "stats" -> stringBuilder.append("\n📊 Focus: Statistics & Scores")
            "news" -> stringBuilder.append("\n📰 Focus: News & Injuries")
            else -> stringBuilder.append("\n🌐 Focus: General Information")
        }
        
        return stringBuilder.toString().trim()
    }

    /**
     * Extracts URLs from Tavily results for source attribution.
     * Used to populate the "sources" field in predictions.
     */
    fun extractUrlsFromResponse(response: TavilyResponse): List<String> {
        return response.results.map { it.url }
    }
}
