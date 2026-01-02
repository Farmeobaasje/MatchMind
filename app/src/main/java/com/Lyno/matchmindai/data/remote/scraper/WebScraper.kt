package com.Lyno.matchmindai.data.remote.scraper

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * Web scraper service for retrieving live match context from DuckDuckGo search results.
 * This provides a free RAG (Retrieval Augmented Generation) workaround by scraping
 * real-time information about matches before making predictions.
 *
 * Note: This is a simple implementation that may break if DuckDuckGo changes their HTML structure.
 * Error handling is implemented to gracefully degrade when scraping fails.
 */
class WebScraper {

    /**
     * Scrapes match context information from DuckDuckGo search results.
     *
     * @param homeTeam The home team name
     * @param awayTeam The away team name
     * @return A string containing concatenated search result snippets, or empty string if scraping fails
     */
    suspend fun scrapeMatchContext(homeTeam: String, awayTeam: String): String {
        return withContext(Dispatchers.IO) {
            try {
            // Build search query for match prediction context with focus on recent results
            val query = "$homeTeam vs $awayTeam last matches results scores"
            val url = "https://html.duckduckgo.com/html/?q=${query.replace(" ", "+")}"
                
                Log.d("WebScraper", "Scraping match context from: $url")
                
                // Connect with a user agent to avoid being blocked
                val doc: Document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(10000) // 10 second timeout
                    .get()
                
                // Select search result snippets (DuckDuckGo uses .result__snippet class)
                val snippets = doc.select(".result__snippet")
                
                if (snippets.isEmpty()) {
                    Log.w("WebScraper", "No search result snippets found for query: $query")
                    return@withContext "Geen live data gevonden."
                }
                
                // Take top 5 results and concatenate their text
                val topResults = snippets.take(5)
                val scrapedText = StringBuilder()
                
                topResults.forEachIndexed { index, element ->
                    val text = element.text().trim()
                    if (text.isNotEmpty()) {
                        scrapedText.append("• $text\n")
                    }
                }
                
                val result = scrapedText.toString().trim()
                Log.d("WebScraper", "Successfully scraped ${topResults.size} snippets")
                
                if (result.isEmpty()) {
                    "Geen live data gevonden."
                } else {
                    result
                }
                
            } catch (e: Exception) {
                // Log the error but don't crash - return empty string to allow fallback
                Log.e("WebScraper", "Failed to scrape match context for $homeTeam vs $awayTeam", e)
                "Geen live data gevonden."
            }
        }
    }
}
