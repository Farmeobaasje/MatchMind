package com.Lyno.matchmindai.presentation.model

import com.Lyno.matchmindai.domain.model.AiAnalysisResult
import com.Lyno.matchmindai.domain.model.HeroMatchExplanation
import com.Lyno.matchmindai.domain.model.LiveEventAnalysis

/**
 * UI state for Dashboard AI insights.
 */
data class DashboardAiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val heroMatchExplanation: HeroMatchExplanation? = null,
    val liveEventAnalysis: LiveEventAnalysis? = null,
    val aiAnalysis: AiAnalysisResult? = null,
    val analyzedMatches: Set<Int> = emptySet()
) {
    /**
     * Checks if there are any AI insights available.
     */
    fun hasInsights(): Boolean {
        return heroMatchExplanation != null || 
               liveEventAnalysis != null || 
               aiAnalysis != null
    }
    
    /**
     * Gets the most recent insight timestamp.
     */
    fun getLatestTimestamp(): Long {
        val timestamps = listOfNotNull(
            heroMatchExplanation?.let { System.currentTimeMillis() },
            liveEventAnalysis?.timestamp,
            aiAnalysis?.let { System.currentTimeMillis() }
        )
        
        return timestamps.maxOrNull() ?: 0L
    }
    
    /**
     * Checks if insights are fresh (less than 5 minutes old).
     */
    fun areInsightsFresh(): Boolean {
        val currentTime = System.currentTimeMillis()
        val latestTimestamp = getLatestTimestamp()
        
        return (currentTime - latestTimestamp) < 300000 // 5 minutes
    }
    
    /**
     * Gets a summary of available insights.
     */
    fun getInsightsSummary(): String {
        return when {
            heroMatchExplanation != null -> "Hero match uitleg beschikbaar"
            liveEventAnalysis != null -> "Live analyse beschikbaar"
            aiAnalysis != null -> "AI voorspelling beschikbaar"
            else -> "Geen AI insights"
        }
    }
    
    /**
     * Gets the primary insight type.
     */
    fun getPrimaryInsightType(): InsightType {
        return when {
            heroMatchExplanation != null -> InsightType.HERO_MATCH
            liveEventAnalysis != null -> InsightType.LIVE_EVENT
            aiAnalysis != null -> InsightType.AI_PREDICTION
            else -> InsightType.NONE
        }
    }
}

/**
 * Types of AI insights available.
 */
enum class InsightType {
    NONE,
    HERO_MATCH,
    LIVE_EVENT,
    AI_PREDICTION
}
