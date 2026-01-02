package com.Lyno.matchmindai.domain.model

import kotlinx.serialization.Serializable

/**
 * Comprehensive match report generated from Mastermind AI analysis.
 * Combines statistical data with narrative storytelling for human-readable insights.
 */
@Serializable
data class MatchReport(
    // Match context
    val fixtureId: Int,
    val homeTeam: String,
    val awayTeam: String,
    val league: String,
    val timestamp: Long = System.currentTimeMillis(),
    
    // Narrative sections
    val title: String,
    val introduction: String,
    val situationAnalysis: String,
    val keyFactors: List<String>,
    val tacticalInsight: String,
    val playerFocus: String,
    val conclusion: String,
    
    // Context metrics
    val chaosLevel: String,
    val chaosScore: Int,
    val atmosphereLevel: String,
    val atmosphereScore: Int,
    
    // Confidence and betting
    val confidence: Int,
    val bettingTip: String,
    val bettingConfidence: Int,
    
    // Data sources
    val breakingNewsUsed: List<String> = emptyList(),
    val scenarioTitle: String = "",
    val scenarioDescription: String = "",
    
    // Enhanced scenario system (NEW)
    val scenarios: List<MatchScenario> = emptyList(),
    val primaryScenario: MatchScenario? = null,
    val scorePredictions: ScorePredictionMatrix? = null,
    
    // Interactive elements
    val scenarioEvolution: ScenarioEvolution? = null,
    val userScenarioPreferences: Map<String, Double> = emptyMap()
) {
    /**
     * Gets a short summary for preview display.
     */
    fun getSummary(): String {
        return "$title: $introduction"
    }
    
    /**
     * Gets the full report as formatted text.
     */
    fun getFullReport(): String {
        return buildString {
            appendLine("📝 $title")
            appendLine()
            appendLine("🏆 $homeTeam vs $awayTeam - $league")
            appendLine()
            appendLine("🔥 SITUATIE ANALYSE:")
            appendLine(situationAnalysis)
            appendLine()
            appendLine("⚡ BELANGRIJKE FACTOREN:")
            keyFactors.forEach { factor ->
                appendLine("• $factor")
            }
            appendLine()
            if (tacticalInsight.isNotEmpty()) {
                appendLine("🎯 TACTISCH INZICHT:")
                appendLine(tacticalInsight)
                appendLine()
            }
            if (playerFocus.isNotEmpty()) {
                appendLine("👤 SPELER FOCUS:")
                appendLine(playerFocus)
                appendLine()
            }
            appendLine("🎯 VERWACHTING:")
            appendLine(conclusion)
            appendLine()
            appendLine("📊 KEY INSIGHTS:")
            appendLine("• Chaos niveau: $chaosLevel ($chaosScore/100)")
            appendLine("• Atmosfeer: $atmosphereLevel ($atmosphereScore/100)")
            appendLine("• AI Vertrouwen: $confidence%")
            if (bettingTip.isNotEmpty()) {
                appendLine("• Betting tip: $bettingTip (vertrouwen: $bettingConfidence%)")
            }
            if (breakingNewsUsed.isNotEmpty()) {
                appendLine()
                appendLine("📰 GEBRUIKTE NIEUWS:")
                breakingNewsUsed.take(3).forEach { news ->
                    appendLine("• $news")
                }
            }
            
            // Add scenario information if available
            if (scenarios.isNotEmpty()) {
                appendLine()
                appendLine("🎭 SCENARIO ANALYSE:")
                scenarios.take(3).forEach { scenario ->
                    appendLine("• ${scenario.title}: ${scenario.predictedScore} (${scenario.probability}% kans)")
                }
            }
            
            if (scorePredictions != null) {
                appendLine()
                appendLine("🎯 SCORE VOORSPELLINGEN:")
                appendLine("• Meest waarschijnlijk: ${scorePredictions.mostLikelyScore} (${scorePredictions.mostLikelyProbability}%)")
                appendLine("• Thuis wint: ${scorePredictions.homeWinProbability}%")
                appendLine("• Gelijk: ${scorePredictions.drawProbability}%")
                appendLine("• Uit wint: ${scorePredictions.awayWinProbability}%")
            }
        }
    }
    
    /**
     * Checks if this report has meaningful content.
     */
    fun hasMeaningfulContent(): Boolean {
        return introduction.isNotEmpty() && 
               situationAnalysis.isNotEmpty() && 
               conclusion.isNotEmpty()
    }
    
    /**
     * Gets the report in a simplified format for UI display.
     */
    fun getFormattedSections(): List<ReportSection> {
        val sections = mutableListOf<ReportSection>()
        
        sections.add(ReportSection(
            title = "SITUATIE ANALYSE",
            content = situationAnalysis,
            icon = "🔥"
        ))
        
        if (keyFactors.isNotEmpty()) {
            sections.add(ReportSection(
                title = "BELANGRIJKE FACTOREN",
                content = keyFactors.joinToString("\n• ", "• "),
                icon = "⚡"
            ))
        }
        
        if (tacticalInsight.isNotEmpty()) {
            sections.add(ReportSection(
                title = "TACTISCH INZICHT",
                content = tacticalInsight,
                icon = "🎯"
            ))
        }
        
        if (playerFocus.isNotEmpty()) {
            sections.add(ReportSection(
                title = "SPELER FOCUS",
                content = playerFocus,
                icon = "👤"
            ))
        }
        
        sections.add(ReportSection(
            title = "VERWACHTING",
            content = conclusion,
            icon = "🎯"
        ))
        
        // Add scenario section if available
        if (scenarios.isNotEmpty()) {
            sections.add(ReportSection(
                title = "SCENARIO'S",
                content = scenarios.joinToString("\n• ", "• ") { 
                    "${it.title}: ${it.predictedScore} (${it.probability}%)" 
                },
                icon = "🎭"
            ))
        }
        
        return sections
    }
}

/**
 * Individual section of a match report for UI display.
 */
data class ReportSection(
    val title: String,
    val content: String,
    val icon: String
)

/**
 * Report generation context for template selection.
 */
data class ReportContext(
    val chaosScore: Int,
    val atmosphereScore: Int,
    val hasBreakingNews: Boolean,
    val hasTacticalKey: Boolean,
    val hasKeyPlayer: Boolean,
    val confidence: Int
) {
    /**
     * Determines the report style based on context.
     */
    fun getReportStyle(): ReportStyle {
        return when {
            chaosScore >= 80 -> ReportStyle.HIGH_CHAOS
            chaosScore >= 60 -> ReportStyle.MEDIUM_CHAOS
            atmosphereScore >= 80 -> ReportStyle.FORTRESS
            atmosphereScore <= 20 -> ReportStyle.DEAD_STADIUM
            confidence >= 80 -> ReportStyle.HIGH_CONFIDENCE
            confidence <= 40 -> ReportStyle.LOW_CONFIDENCE
            else -> ReportStyle.STANDARD
        }
    }
}

/**
 * Different styles of match reports based on context.
 */
enum class ReportStyle {
    HIGH_CHAOS,      // Totale oorlog, veel kaarten, hoge variatie
    MEDIUM_CHAOS,    // Geforceerde wedstrijd, risico's
    FORTRESS,        // Sterk thuisvoordeel, heksenketel
    DEAD_STADIUM,    // Doods stadion, weinig publiek
    HIGH_CONFIDENCE, // Duidelijke verwachting, hoge zekerheid
    LOW_CONFIDENCE,  // Onzekere verwachting, veel variabelen
    STANDARD         // Standaard analyse
}

/**
 * Individual match scenario with probability and impact analysis.
 */
@Serializable
data class MatchScenario(
    val id: String,                    // Unique scenario identifier
    val title: String,                 // Short descriptive title
    val description: String,           // Detailed narrative description
    val predictedScore: String,        // Concrete score prediction (e.g., "2-1")
    val probability: Int,              // 0-100% likelihood
    val confidence: Int,               // 0-100% confidence in this prediction
    
    // Impact analysis
    val chaosImpact: Int,              // How this scenario affects chaos (0-100)
    val atmosphereImpact: Int,         // How this affects atmosphere (0-100)
    val bettingValue: Double,          // Betting value score (0.0-1.0)
    
    // Key factors
    val keyFactors: List<String>,      // What drives this scenario
    val triggerEvents: List<String>,   // What could trigger this scenario
    val timeline: List<ScenarioTimelineEvent>, // How scenario unfolds
    
    // Data sources
    val dataSources: List<String>,     // Which data supports this scenario
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Timeline event within a scenario.
 */
@Serializable
data class ScenarioTimelineEvent(
    val minute: Int,                   // Match minute
    val eventType: ScenarioEventType,  // Type of event
    val description: String,           // What happens
    val impact: Int                    // Impact on scenario (0-100)
)

/**
 * Types of events in scenario timelines.
 */
enum class ScenarioEventType {
    GOAL,              // Goal scored
    RED_CARD,          // Red card
    INJURY,           // Key player injury
    TACTICAL_CHANGE,   // Tactical substitution/change
    MOMENTUM_SHIFT,    // Momentum change
    CONTROVERSY,       // Controversial decision
    WEATHER_CHANGE,    // Weather impact
    CROWD_INFLUENCE    // Crowd influence
}

/**
 * Score prediction matrix with probabilities.
 */
@Serializable
data class ScorePredictionMatrix(
    val mostLikelyScore: String,       // Most probable score (e.g., "2-1")
    val mostLikelyProbability: Int,    // Probability of most likely score
    
    // Score ranges
    val homeWinScores: List<ScoreProbability>,    // All home win scores
    val drawScores: List<ScoreProbability>,       // All draw scores
    val awayWinScores: List<ScoreProbability>,    // All away win scores
    
    // Aggregated probabilities
    val homeWinProbability: Int,       // Probability of home win
    val drawProbability: Int,          // Probability of draw
    val awayWinProbability: Int,       // Probability of away win
    
    // Expected goals
    val expectedHomeGoals: Double,     // xG for home team
    val expectedAwayGoals: Double,     // xG for away team
    val goalExpectationRange: String   // Range (e.g., "2.5-3.5 goals")
)

/**
 * Individual score with probability.
 */
@Serializable
data class ScoreProbability(
    val score: String,     // e.g., "2-1"
    val probability: Int,  // 0-100%
    val confidence: Int    // 0-100% confidence
)

/**
 * Scenario evolution over time.
 */
@Serializable
data class ScenarioEvolution(
    val preMatchScenarios: List<MatchScenario>,    // Before match
    val liveScenarios: List<MatchScenario>,        // During match (updated)
    val postMatchAnalysis: ScenarioAnalysis?,      // After match
    
    // Evolution tracking
    val scenarioChanges: List<ScenarioChange>,     // How scenarios changed
    val accuracyScore: Int? = null                 // How accurate predictions were
)

/**
 * Analysis of scenario accuracy post-match.
 */
@Serializable
data class ScenarioAnalysis(
    val actualScore: String,                       // Actual match score
    val closestScenario: MatchScenario?,           // Which scenario was closest
    val accuracyPercentage: Int,                   // How accurate overall
    val lessonsLearned: List<String>               // Insights for future
)

/**
 * Scenario change tracking.
 */
@Serializable
data class ScenarioChange(
    val timestamp: Long,
    val scenarioId: String,
    val oldProbability: Int,
    val newProbability: Int,
    val trigger: String
)

/**
 * Scenario categories for classification.
 */
enum class ScenarioCategory {
    DOMINANT_HOME_WIN,      // Home team dominates
    NARROW_HOME_WIN,        // Close home win
    HIGH_SCORING_DRAW,      // Entertaining draw
    LOW_SCORING_DRAW,       // Tactical stalemate
    AWAY_TEAM_SURPRISE,     // Away team upset
    GOAL_FEST,              // Many goals expected
    DEFENSIVE_BATTLE,       // Few goals expected
    CONTROVERSIAL_OUTCOME,  // Referee/controversy impact
    LATE_DRAMA,             // Late goals/comeback
    ONE_SIDED_AFFAIR        // Complete domination
}

/**
 * Quick metrics for Intel tab display.
 * Lightweight version of MatchReport with key metrics only.
 */
@Serializable
data class QuickMetrics(
    // Match context
    val fixtureId: Int,
    val homeTeam: String,
    val awayTeam: String,
    val league: String,
    
    // Key metrics
    val chaosScore: Int,
    val chaosLevel: String,
    val atmosphereScore: Int,
    val atmosphereLevel: String,
    
    // Model consensus
    val apiConfidence: Int?,           // API-Sports confidence
    val hybridConfidence: Int?,        // Hybrid AI confidence
    val consensusLevel: String,        // Consensus between models
    
    // Quick insights
    val bettingTip: String,
    val bettingConfidence: Int,
    val confidence: Int,               // Overall AI confidence
    
    // Data quality
    val hasBreakingNews: Boolean,
    val hasTacticalKey: Boolean,
    val hasKeyPlayer: Boolean,
    val dataQuality: String,           // "Good", "Fair", "Poor"
    
    // Timestamp
    val generatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Gets the consensus level between models.
     */
    fun calculateConsensus(): String {
        if (apiConfidence == null || hybridConfidence == null) {
            return "Onvoldoende data"
        }
        
        val diff = kotlin.math.abs(apiConfidence - hybridConfidence)
        return when {
            diff <= 10 -> "Sterk"
            diff <= 20 -> "Matig"
            diff <= 30 -> "Zwak"
            else -> "Tegenstrijdig"
        }
    }
    
    /**
     * Gets data quality based on available data.
     */
    fun calculateDataQuality(): String {
        var score = 0
        
        if (apiConfidence != null) score += 25
        if (hybridConfidence != null) score += 25
        if (hasBreakingNews) score += 20
        if (hasTacticalKey) score += 15
        if (hasKeyPlayer) score += 15
        
        return when {
            score >= 80 -> "Excellent"
            score >= 60 -> "Good"
            score >= 40 -> "Fair"
            else -> "Poor"
        }
    }
    
    /**
     * Gets a quick summary for display.
     */
    fun getQuickSummary(): String {
        return buildString {
            appendLine("⚡ MatchMind Intel")
            appendLine("$homeTeam vs $awayTeam - $league")
            appendLine()
            appendLine("📊 Key Metrics:")
            appendLine("• Chaos: $chaosLevel ($chaosScore/100)")
            appendLine("• Atmosfeer: $atmosphereLevel ($atmosphereScore/100)")
            appendLine("• AI Vertrouwen: $confidence%")
            appendLine("• Model Consensus: $consensusLevel")
            
            if (bettingTip.isNotEmpty()) {
                appendLine()
                appendLine("🎯 Betting Tip:")
                appendLine("$bettingTip (vertrouwen: $bettingConfidence%)")
            }
            
            appendLine()
            appendLine("🔍 Data Kwaliteit: $dataQuality")
        }
    }
}
