package com.Lyno.matchmindai.domain.model

import kotlinx.serialization.Serializable

/**
 * Data model for match fixtures list to be displayed in a widget.
 * This is used when the AI retrieves fixtures data and we want to show it in a carousel.
 */
@Serializable
data class MatchListData(
    val fixtures: List<MatchFixture>,
    val dateRange: String? = null, // e.g., "Vandaag", "Komende 3 dagen"
    val source: String? = null // e.g., "API-Football", "Tavily Search"
) {
    /**
     * Whether the list is empty.
     */
    val isEmpty: Boolean
        get() = fixtures.isEmpty()

    /**
     * Number of fixtures in the list.
     */
    val count: Int
        get() = fixtures.size

    companion object {
        /**
         * Creates an empty match list data.
         */
        fun empty(): MatchListData = MatchListData(
            fixtures = emptyList(),
            dateRange = null,
            source = null
        )
    }
}
