package com.Lyno.matchmindai.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request DTO for DeepSeek API calls.
 * Follows OpenAI-compatible API format.
 */
@Serializable
data class DeepSeekRequest(
    @SerialName("model") val model: String = "deepseek-chat",
    @SerialName("messages") val messages: List<DeepSeekMessage>,
    @SerialName("temperature") val temperature: Double = 0.5,
    @SerialName("response_format") val responseFormat: ResponseFormat? = null,
    @SerialName("stream") val stream: Boolean = false
)

@Serializable
data class DeepSeekMessage(
    @SerialName("role") val role: String,
    @SerialName("content") val content: String
)

@Serializable
data class ResponseFormat(
    @SerialName("type") val type: String
)

/**
 * Companion object with factory methods for creating DeepSeek requests.
 */
object DeepSeekRequestFactory {
    /**
     * Creates a prediction request for a match between two teams.
     * @param homeTeam The home team name
     * @param awayTeam The away team name
     * @return DeepSeekRequest configured for match prediction
     */
    fun createMatchPredictionRequest(homeTeam: String, awayTeam: String): DeepSeekRequest {
        val prompt = """
            Analyseer de wedstrijd tussen $homeTeam en $awayTeam.
            
            INSTRUCTIES:
            1. Analyseer teamvorm, historie, en thuisvoordeel.
            2. Fallback: Gebruik interne kennis als stats ontbreken.
            3. Taal: NEDERLANDS.
            4. Antwoord altijd in valide JSON.
            
            OUTPUT (JSON):
            {
              "winner": "Team Naam",
              "confidence_score": 0-100,
              "risk_level": "LOW/MEDIUM/HIGH",
              "reasoning": "Korte analyse (max 3 zinnen).",
              "key_factor": "Eén kernzin (max 5 woorden)."
            }
        """.trimIndent()

        return DeepSeekRequest(
            messages = listOf(
                DeepSeekMessage(
                    role = "user",
                    content = prompt
                )
            ),
            responseFormat = ResponseFormat(type = "json_object")
        )
    }
}
