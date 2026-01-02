package com.Lyno.matchmindai.domain.model

import kotlinx.serialization.Serializable

/**
 * MatchContext serves as the 'Single Source of Truth' for LLM prompts.
 * It aggregates all relevant data for match analysis from multiple sources.
 * 
 * All API data fields are nullable because endpoints can fail or return empty data.
 * 
 * @property matchId The unique identifier of the fixture
 * @property teams Formatted string "Home vs Away"
 * @property date The match date in ISO format
 * @property predictions Summary of prediction/advice from API-Sports
 * @property injuries List of important injured players with reasons
 * @property odds Formatted odds string (e.g., "Home: 1.5, Draw: 4.0, Away: 6.0")
 * @property standings Current league standings summary
 * @property newsContext News summary from Tavily search
 */
@Serializable
data class MatchContext(
    val matchId: Int,
    val teams: String, // "Home vs Away"
    val date: String,
    // API-Sports Data (Nullable, want API kan falen/leeg zijn)
    val predictions: String?, // Samenvatting van prediction/advice
    val injuries: List<String>?, // Lijst van belangrijke geblesseerde spelers
    val odds: String?, // Bijv. "Home: 1.5, Draw: 4.0, Away: 6.0"
    val standings: String?, // De Huidige stand (die we al gefixt hebben)
    // Tavily Data
    val newsContext: String? // Samenvatting van het nieuws
)
