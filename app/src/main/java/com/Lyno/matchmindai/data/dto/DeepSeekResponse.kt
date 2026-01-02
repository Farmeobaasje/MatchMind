package com.Lyno.matchmindai.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Response DTO from DeepSeek API.
 * Matches the JSON structure defined in docs/06_prompt_engineering.md
 */
@Serializable
data class DeepSeekResponse(
    @SerialName("id") val id: String,
    @SerialName("choices") val choices: List<DeepSeekChoice>,
    @SerialName("usage") val usage: DeepSeekUsage? = null
) {
    /**
     * Parses the content string as a MatchPredictionResponse.
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
    @SerialName("finish_reason") val finishReason: String?
)

@Serializable
data class DeepSeekUsage(
    @SerialName("prompt_tokens") val promptTokens: Int,
    @SerialName("completion_tokens") val completionTokens: Int,
    @SerialName("total_tokens") val totalTokens: Int
)

/**
 * The actual prediction data structure that matches our prompt engineering.
 */
@Serializable
data class MatchPredictionResponse(
    @SerialName("winner") val winner: String,
    @SerialName("confidence_score") val confidenceScore: Int,
    @SerialName("risk_level") val riskLevel: RiskLevel,
    @SerialName("reasoning") val reasoning: String,
    @SerialName("key_factor") val keyFactor: String,
    @SerialName("recent_matches") val recentMatches: List<String> = emptyList()
) {
    @Serializable
    enum class RiskLevel {
        LOW, MEDIUM, HIGH
    }
}
