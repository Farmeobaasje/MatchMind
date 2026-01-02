package com.Lyno.matchmindai.data.remote.rss

import kotlinx.serialization.Serializable

/**
 * Data Transfer Object (DTO) for parsed RSS feed items.
 * This represents a single news item from an RSS feed.
 */
@Serializable
data class RssItem(
    val title: String,
    val description: String,
    val pubDate: String,
    val source: String
) {
    /**
     * Creates a formatted string representation for display.
     * Format: "Source: BBC | Date: ... | Title: ... | Summary: ..."
     */
    fun toFormattedString(): String {
        val cleanDescription = if (description.length > 150) {
            description.take(147) + "..."
        } else {
            description
        }
        return "Source: $source | Date: $pubDate | Title: $title | Summary: $cleanDescription"
    }

    companion object {
        /**
         * Checks if this item is relevant for the given teams.
         * Performs case-insensitive search in title and description.
         */
        fun isRelevantForTeams(item: RssItem, teamA: String, teamB: String): Boolean {
            val searchText = "${item.title} ${item.description}".lowercase()
            return teamA.lowercase() in searchText || teamB.lowercase() in searchText
        }

        /**
         * Checks if this item is relevant for a single team.
         * Performs case-insensitive search in title and description.
         */
        fun isRelevantForTeam(item: RssItem, teamName: String): Boolean {
            val searchText = "${item.title} ${item.description}".lowercase()
            return teamName.lowercase() in searchText
        }
    }
}
