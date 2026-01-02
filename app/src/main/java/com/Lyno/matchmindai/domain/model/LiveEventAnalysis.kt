package com.Lyno.matchmindai.domain.model

import kotlinx.serialization.Serializable

/**
 * AI analysis of live events during a match.
 * Provides real-time insights and predictions based on match events.
 */
@Serializable
data class LiveEventAnalysis(
    val fixtureId: Int,
    val homeTeam: String,
    val awayTeam: String,
    val timestamp: Long = System.currentTimeMillis(),
    val confidence: Double = 0.0, // 0.0-1.0 confidence in the analysis
    val keyInsights: List<String> = emptyList(),
    val momentumShift: String? = null,
    val predictedOutcome: String? = null,
    val riskLevel: String = "Gemiddeld",
    val bettingOpportunities: List<String> = emptyList(),
    val nextExpectedEvent: String? = null,
    val analysisSummary: String = ""
) {
    /**
     * Checks if this analysis is fresh (less than 5 minutes old).
     */
    fun isFresh(): Boolean {
        val currentTime = System.currentTimeMillis()
        return (currentTime - timestamp) < 300000 // 5 minutes
    }

    /**
     * Gets a summary of the analysis for display.
     */
    fun getSummary(): String {
        return if (analysisSummary.isNotEmpty()) {
            analysisSummary
        } else if (keyInsights.isNotEmpty()) {
            "Live analyse: ${keyInsights.first()}"
        } else {
            "Live analyse beschikbaar"
        }
    }

    /**
     * Gets the confidence as a percentage.
     */
    fun getConfidencePercentage(): Int {
        return (confidence * 100).toInt()
    }

    /**
     * Checks if this analysis has meaningful insights.
     */
    fun hasMeaningfulInsights(): Boolean {
        return keyInsights.isNotEmpty() || momentumShift != null || predictedOutcome != null
    }

    /**
     * Gets the full analysis text.
     */
    fun getFullAnalysis(): String {
        val builder = StringBuilder()

        builder.append("## ⚡ Live Event Analyse\n\n")

        if (keyInsights.isNotEmpty()) {
            builder.append("**Belangrijkste inzichten:**\n")
            keyInsights.take(3).forEach { insight ->
                builder.append("• $insight\n")
            }
            builder.append("\n")
        }

        momentumShift?.let {
            builder.append("**Momentum shift:** $it\n\n")
        }

        predictedOutcome?.let {
            builder.append("**Voorspeld resultaat:** $it\n\n")
        }

        builder.append("**Risico niveau:** $riskLevel\n")
        builder.append("**Betrouwbaarheid:** ${getConfidencePercentage()}%\n")

        if (bettingOpportunities.isNotEmpty()) {
            builder.append("\n**Betting kansen:**\n")
            bettingOpportunities.take(2).forEach { opportunity ->
                builder.append("• $opportunity\n")
            }
        }

        nextExpectedEvent?.let {
            builder.append("\n**Volgende verwachte gebeurtenis:** $it")
        }

        return builder.toString()
    }
}

/**
 * Companion object with utility functions.
 */
object LiveEventAnalysisUtils {
    /**
     * Creates empty live event analysis for loading/error states.
     */
    fun empty(fixtureId: Int, homeTeam: String, awayTeam: String): LiveEventAnalysis {
        return LiveEventAnalysis(
            fixtureId = fixtureId,
            homeTeam = homeTeam,
            awayTeam = awayTeam,
            timestamp = System.currentTimeMillis(),
            confidence = 0.0,
            analysisSummary = "Geen live analyse beschikbaar"
        )
    }

    /**
     * Creates live event analysis from match events.
     */
    fun fromEvents(
        fixtureId: Int,
        homeTeam: String,
        awayTeam: String,
        events: List<LiveEvent>,
        currentScore: String
    ): LiveEventAnalysis {
        val keyInsights = mutableListOf<String>()
        var momentumShift: String? = null
        var predictedOutcome: String? = null

        // Analyze events for insights
        val goals = events.count { it.type == EventType.GOAL }
        val cards = events.count { it.type == EventType.YELLOW_CARD || it.type == EventType.RED_CARD }

        if (goals > 0) {
            keyInsights.add("$goals doelpunten gescoord in de wedstrijd")
        }

        if (cards > 0) {
            keyInsights.add("$cards kaarten getoond")
        }

        // Determine momentum based on recent events
        val recentEvents = events.takeLast(5)
        if (recentEvents.any { it.type == EventType.GOAL }) {
            momentumShift = "Momentum shift na recent doel"
        }

        // Simple prediction based on events
        if (events.any { it.type == EventType.RED_CARD }) {
            predictedOutcome = "Team met rode kaart heeft verhoogd risico"
        }

        return LiveEventAnalysis(
            fixtureId = fixtureId,
            homeTeam = homeTeam,
            awayTeam = awayTeam,
            timestamp = System.currentTimeMillis(),
            confidence = 0.7, // Default confidence
            keyInsights = keyInsights,
            momentumShift = momentumShift,
            predictedOutcome = predictedOutcome,
            riskLevel = if (cards > 2) "Hoog" else "Gemiddeld",
            bettingOpportunities = if (goals > 2) listOf("Over 2.5 goals") else emptyList(),
            nextExpectedEvent = if (events.isEmpty()) "Eerste grote kans" else "Volgende doel of kaart",
            analysisSummary = "Live analyse van $homeTeam vs $awayTeam (${events.size} events)"
        )
    }
}
