package com.Lyno.matchmindai.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/**
 * Response DTO from DeepSeek API.
 * Matches JSON structure defined in docs/06_prompt_engineering.md
 */
@Serializable
data class DeepSeekResponse(
    @SerialName("id") val id: String,
    @SerialName("choices") val choices: List<DeepSeekChoice>,
    @SerialName("usage") val usage: DeepSeekUsage? = null
) {
    /**
     * Parses content string as a MatchPredictionResponse.
     * @return MatchPredictionResponse parsed from JSON content
     * @throws IllegalArgumentException if content is not valid JSON
     */
    fun toMatchPrediction(): MatchPredictionResponse {
        val content = choices.firstOrNull()?.message?.content
            ?: throw IllegalArgumentException("No content in response")
        
        return Json.decodeFromString<MatchPredictionResponse>(content)
    }
}

@Serializable
data class DeepSeekChoice(
    @SerialName("index") val index: Int,
    @SerialName("message") val message: DeepSeekMessage,
    @SerialName("finish_reason") val finishReason: String?,
    @SerialName("reasoning_content") val reasoningContent: String? = null // R1 model reasoning trace
)

@Serializable
data class DeepSeekUsage(
    @SerialName("prompt_tokens") val promptTokens: Int,
    @SerialName("completion_tokens") val completionTokens: Int,
    @SerialName("total_tokens") val totalTokens: Int
)

/**
 * Flexible response data structure that can handle ANY type of AI response.
 * Supports new AgentResponse format with flexible data field.
 */
@Serializable
data class MatchPredictionResponse(
    @SerialName("type") val type: String? = null, // "TEXT_ONLY", "LIVE_MATCH", "PREDICTION", "ANALYSIS", "STANDINGS"
    @SerialName("text") val text: String? = null, // Main text response (new field)
    @SerialName("content") val content: String? = null, // Legacy field, maps to text
    @SerialName("reasoning") val reasoning: String? = null, // Legacy field, maps to text
    @SerialName("related_data") val relatedData: JsonElement? = null, // Flexible data dump
    @SerialName("winner") val winner: String? = null,
    @SerialName("confidence_score") val confidenceScore: Int? = null,
    @SerialName("risk_level") val riskLevel: RiskLevel? = null,
    @SerialName("key_factor") val keyFactor: String? = null,
    @SerialName("home_team") val homeTeam: String? = null,
    @SerialName("away_team") val awayTeam: String? = null,
    @SerialName("recent_matches") val recentMatches: List<String> = emptyList(),
    @SerialName("sources") val sources: List<String>? = null,
    @SerialName("suggested_actions") val suggestedActions: List<String>? = null
) {
    @Serializable
    enum class RiskLevel {
        LOW, MEDIUM, HIGH
    }
    
    /**
     * Gets main content text, preferring 'text' field but falling back to 'content' and 'reasoning'.
     */
    fun resolveContent(): String {
        return text ?: content ?: reasoning ?: ""
    }
    
    /**
     * Gets response type, defaulting to TEXT_ONLY if not specified.
     */
    fun resolveType(): String {
        return type ?: if (winner != null && confidenceScore != null) {
            "PREDICTION"
        } else if (homeTeam != null || awayTeam != null) {
            "ANALYSIS"
        } else {
            "TEXT_ONLY"
        }
    }
    
    /**
     * Gets suggested actions as a list, defaulting to empty list if null.
     */
    fun resolveSuggestedActions(): List<String> {
        return suggestedActions ?: emptyList()
    }
}
