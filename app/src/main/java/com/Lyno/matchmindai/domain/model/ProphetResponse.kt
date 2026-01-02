package com.Lyno.matchmindai.domain.model

import kotlinx.serialization.Serializable

/**
 * Sealed class hierarchy for the Prophet Module's Generative UI system.
 * This represents structured responses from the AI that can render both text
 * and interactive widgets in the chat interface.
 * 
 * This is the Phase 3 implementation that enables "Generative UI" where
 * the AI analyzes hard data (API-SPORTS) and soft context (Tavily),
 * then returns structured JSON that renders both text and interactive widgets.
 */
@Serializable
sealed class ProphetResponse {
    
    /**
     * Simple text response.
     */
    @Serializable
    data class Text(
        val content: String
    ) : ProphetResponse()
    
    /**
     * Loading indicator response.
     */
    @Serializable
    data class Loading(
        val message: String
    ) : ProphetResponse()
    
    /**
     * A widget showing the match card inside the chat.
     * This embeds a StandardMatchCard component within the chat stream.
     */
    @Serializable
    data class MatchWidget(
        val match: MatchFixture,
        val analysis: String,
        val confidenceScore: Int? = null // Optional confidence score (0-100)
    ) : ProphetResponse()
    
    /**
     * A widget showing specific stats/prediction details.
     * This shows Win % Bars and the "Risk Factor" with a warning icon.
     */
    @Serializable
    data class PredictionWidget(
        val homeChance: Int,
        val awayChance: Int,
        val drawChance: Int,
        val risk: String,
        val confidenceScore: Int, // Overall confidence (0-100)
        val keyInsights: List<String> = emptyList(),
        val recommendation: String? = null // e.g., "Thuiswinst", "Uitwinst", "Gelijk"
    ) : ProphetResponse()
    
    /**
     * Error response.
     */
    @Serializable
    data class Error(
        val message: String,
        val errorType: ErrorType = ErrorType.GENERIC
    ) : ProphetResponse() {
        @Serializable
        enum class ErrorType {
            GENERIC,
            NO_PREDICTION_COVERAGE,
            MATCH_POSTPONED,
            API_ERROR,
            NETWORK_ERROR
        }
    }
    
    /**
     * Analysis response with detailed breakdown.
     */
    @Serializable
    data class Analysis(
        val title: String,
        val content: String,
        val sections: List<AnalysisSection> = emptyList(),
        val confidence: ConfidenceLevel = ConfidenceLevel.MEDIUM
    ) : ProphetResponse() {
        @Serializable
        data class AnalysisSection(
            val title: String,
            val content: String,
            val icon: String? = null // Optional icon name
        )
        
        @Serializable
        enum class ConfidenceLevel {
            LOW, MEDIUM, HIGH
        }
    }
    
    /**
     * Converts this ProphetResponse to the legacy AgentResponse format.
     * This enables backward compatibility with existing UI components.
     */
    fun toAgentResponse(): AgentResponse {
        return when (this) {
            is Text -> AgentResponse.chat(content)
            is Loading -> AgentResponse.chat("⏳ $message")
            is MatchWidget -> {
                val text = "⚽ ${match.displayName}\n$analysis"
                AgentResponse.prediction(text, null)
            }
            is PredictionWidget -> {
                val text = buildString {
                    appendLine("📊 Voorspelling:")
                    appendLine("• ${homeChance}% kans op thuiswinst")
                    appendLine("• ${drawChance}% kans op gelijkspel")
                    appendLine("• ${awayChance}% kans op uitwinst")
                    appendLine()
                    appendLine("⚠️ Risico: $risk")
                    appendLine("🎯 Vertrouwen: $confidenceScore%")
                    if (recommendation != null) {
                        appendLine("💡 Aanbeveling: $recommendation")
                    }
                    if (keyInsights.isNotEmpty()) {
                        appendLine()
                        appendLine("🔍 Belangrijke inzichten:")
                        keyInsights.forEach { insight ->
                            appendLine("• $insight")
                        }
                    }
                }
                AgentResponse.prediction(text, null)
            }
            is Error -> AgentResponse.error("❌ $message")
            is Analysis -> {
                val text = buildString {
                    appendLine("📈 $title")
                    appendLine()
                    appendLine(content)
                    if (sections.isNotEmpty()) {
                        appendLine()
                        sections.forEach { section ->
                            appendLine("### ${section.title}")
                            appendLine(section.content)
                            appendLine()
                        }
                    }
                    appendLine()
                    appendLine("Vertrouwensniveau: ${confidence.name.lowercase()}")
                }
                AgentResponse.analysis(text, null)
            }
        }
    }
    
    companion object {
        /**
         * Creates a ProphetResponse from a JSON string returned by the AI.
         * This parses the structured JSON output from the PromptBuilder.
         */
        fun fromJson(json: String): Result<ProphetResponse> {
            return try {
                // TODO: Implement proper JSON parsing with kotlinx.serialization
                // For now, return a simple text response
                Result.success(Text("JSON parsing not yet implemented. Raw: $json"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
        
        /**
         * Creates a PredictionWidget from AI analysis data.
         */
        fun createPredictionWidget(
            homeChance: Int,
            awayChance: Int,
            drawChance: Int,
            risk: String,
            confidenceScore: Int,
            keyInsights: List<String> = emptyList(),
            recommendation: String? = null
        ): PredictionWidget {
            return PredictionWidget(
                homeChance = homeChance,
                awayChance = awayChance,
                drawChance = drawChance,
                risk = risk,
                confidenceScore = confidenceScore,
                keyInsights = keyInsights,
                recommendation = recommendation
            )
        }
        
        /**
         * Creates a MatchWidget from match and analysis data.
         */
        fun createMatchWidget(
            match: MatchFixture,
            analysis: String,
            confidenceScore: Int? = null
        ): MatchWidget {
            return MatchWidget(
                match = match,
                analysis = analysis,
                confidenceScore = confidenceScore
            )
        }
    }
}
