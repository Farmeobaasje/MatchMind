package com.Lyno.matchmindai.data.dto.football

import kotlinx.serialization.Serializable

/**
 * DTO for API-SPORTS news response.
 * Based on API-SPORTS v3 football/news endpoint structure.
 */
@Serializable
data class NewsItemResponse(
    val title: String,
    val description: String,
    val url: String,
    val source: String,
    val publishedAt: String,
    val image: String? = null,
    val author: String? = null
)

/**
 * Response wrapper for news endpoint.
 */
@Serializable
data class NewsResponse(
    val response: List<NewsItemResponse> = emptyList(),
    val results: Int = 0
)
