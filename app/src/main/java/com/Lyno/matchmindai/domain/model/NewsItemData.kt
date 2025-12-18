package com.Lyno.matchmindai.domain.model

import kotlinx.serialization.Serializable

/**
 * Data model for news/search result items to be displayed in a widget.
 * This is used when the AI retrieves news or search results and we want to show them as cards.
 */
@Serializable
data class NewsItemData(
    val headline: String,
    val source: String, // e.g., "VI.nl", "ESPN", "NOS Sport"
    val url: String,
    val snippet: String? = null, // Short preview text
    val publishedDate: String? = null // e.g., "2 uur geleden", "Vandaag"
) {
    /**
     * Extracts domain from URL for display purposes.
     */
    val domain: String
        get() = try {
            val uri = java.net.URI(url)
            val host = uri.host ?: url
            host.removePrefix("www.").substringBefore("/")
        } catch (e: Exception) {
            url
        }

    companion object {
        /**
         * Creates an empty news item data.
         */
        fun empty(): NewsItemData = NewsItemData(
            headline = "",
            source = "",
            url = ""
        )
    }
}

/**
 * Data model for a list of news items.
 */
@Serializable
data class NewsListData(
    val items: List<NewsItemData>,
    val query: String? = null, // The search query that produced these results
    val totalResults: Int? = null // Total number of results available
) {
    /**
     * Whether the list is empty.
     */
    val isEmpty: Boolean
        get() = items.isEmpty()

    /**
     * Number of items in the list.
     */
    val count: Int
        get() = items.size

    companion object {
        /**
         * Creates an empty news list data.
         */
        fun empty(): NewsListData = NewsListData(
            items = emptyList(),
            query = null,
            totalResults = null
        )
    }
}
