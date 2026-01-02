package com.Lyno.matchmindai.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Flexible response model that can handle different types of AI responses.
 * Replaces the rigid MatchPrediction model for more versatile conversations.
 */
@Serializable
data class AgentResponse(
    val text: String, // The "human" response
    val type: ResponseType, // TEXT_ONLY, LIVE_MATCH, PREDICTION, ANALYSIS, STANDINGS
    val relatedData: JsonElement? = null, // Flexible data dump (can be a match, list or fact)
    val suggestedActions: List<String> = emptyList()
) {
    @Serializable
    enum class ResponseType { 
        TEXT_ONLY,      // Just chatting 
        LIVE_MATCH,     // Shows scoreboard + events
        PREDICTION,     // Shows the prediction card
        ANALYSIS,        // Shows analysis with data
        STANDINGS,       // Shows rankings
        FIXTURES_WIDGET, // Shows match fixtures in a carousel
        NEWS_WIDGET,     // Shows news/search results in cards
        ODDS_WIDGET,     // Shows betting odds with safety and value ratings
        UNKNOWN          // Fallback for unknown types from AI
    }
    
        /**
         * Whether this is a football-related response.
         */
        val isFootballRelated: Boolean
            get() = type == ResponseType.LIVE_MATCH || type == ResponseType.PREDICTION || 
                    type == ResponseType.ANALYSIS || type == ResponseType.STANDINGS ||
                    type == ResponseType.FIXTURES_WIDGET || type == ResponseType.NEWS_WIDGET ||
                    type == ResponseType.ODDS_WIDGET
    
    /**
     * Whether this is a casual chat response.
     */
    val isCasualChat: Boolean
        get() = type == ResponseType.TEXT_ONLY
    
    companion object {
        /**
         * Creates a casual chat response.
         */
        fun chat(text: String, suggestedActions: List<String> = emptyList()): AgentResponse {
            return AgentResponse(
                text = text,
                type = ResponseType.TEXT_ONLY,
                suggestedActions = suggestedActions
            )
        }
        
        /**
         * Creates a live match response.
         */
        fun liveMatch(text: String, relatedData: JsonElement?, suggestedActions: List<String> = emptyList()): AgentResponse {
            return AgentResponse(
                text = text,
                type = ResponseType.LIVE_MATCH,
                relatedData = relatedData,
                suggestedActions = suggestedActions
            )
        }
        
        /**
         * Creates a prediction response.
         */
        fun prediction(text: String, relatedData: JsonElement?, suggestedActions: List<String> = emptyList()): AgentResponse {
            return AgentResponse(
                text = text,
                type = ResponseType.PREDICTION,
                relatedData = relatedData,
                suggestedActions = suggestedActions
            )
        }
        
        /**
         * Creates an analysis response.
         */
        fun analysis(text: String, relatedData: JsonElement? = null, suggestedActions: List<String> = emptyList()): AgentResponse {
            return AgentResponse(
                text = text,
                type = ResponseType.ANALYSIS,
                relatedData = relatedData,
                suggestedActions = suggestedActions
            )
        }
        
        /**
         * Creates a standings response.
         */
        fun standings(text: String, relatedData: JsonElement?, suggestedActions: List<String> = emptyList()): AgentResponse {
            return AgentResponse(
                text = text,
                type = ResponseType.STANDINGS,
                relatedData = relatedData,
                suggestedActions = suggestedActions
            )
        }
        
        /**
         * Creates a fixtures widget response.
         */
        fun fixturesWidget(text: String, relatedData: JsonElement?, suggestedActions: List<String> = emptyList()): AgentResponse {
            return AgentResponse(
                text = text,
                type = ResponseType.FIXTURES_WIDGET,
                relatedData = relatedData,
                suggestedActions = suggestedActions
            )
        }
        
        /**
         * Creates a news widget response.
         */
        fun newsWidget(text: String, relatedData: JsonElement?, suggestedActions: List<String> = emptyList()): AgentResponse {
            return AgentResponse(
                text = text,
                type = ResponseType.NEWS_WIDGET,
                relatedData = relatedData,
                suggestedActions = suggestedActions
            )
        }
        
        /**
         * Creates an odds widget response.
         */
        fun oddsWidget(text: String, relatedData: JsonElement?, suggestedActions: List<String> = emptyList()): AgentResponse {
            return AgentResponse(
                text = text,
                type = ResponseType.ODDS_WIDGET,
                relatedData = relatedData,
                suggestedActions = suggestedActions
            )
        }
        
        /**
         * Creates an error response.
         */
        fun error(text: String): AgentResponse {
            return AgentResponse(
                text = text,
                type = ResponseType.TEXT_ONLY
            )
        }
    }
}
