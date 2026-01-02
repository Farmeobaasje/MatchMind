package com.Lyno.matchmindai.domain.service

import android.util.Log
import com.Lyno.matchmindai.domain.model.AiAnalysisResult
import com.Lyno.matchmindai.domain.model.EventType
import com.Lyno.matchmindai.domain.model.LiveEvent
import com.Lyno.matchmindai.domain.model.LiveEventAnalysis
import com.Lyno.matchmindai.domain.model.LiveMatchData
import com.Lyno.matchmindai.domain.model.MatchDetail
import com.Lyno.matchmindai.domain.model.MatchFixture
import com.Lyno.matchmindai.domain.model.MatchPrediction
import com.Lyno.matchmindai.domain.model.NewsImpactModifiers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

/**
 * Service that analyzes live match events and provides real-time AI insights.
 * Uses NewsImpactAnalyzer to contextualize live events with news data.
 */
class LiveEventAnalyzer(
    private val newsImpactAnalyzer: NewsImpactAnalyzer,
    private val apiKeyStorage: com.Lyno.matchmindai.data.local.ApiKeyStorage
) {
    
    companion object {
        // System prompt for live event analysis
        const val LIVE_EVENT_ANALYSIS_PROMPT = """
JIJ BENT: Een Senior Football Analyst gespecialiseerd in real-time match analysis.
DOEL: Analyseer live events en geef real-time insights over momentum, tactiek en voorspellingen.

INPUT DATA:
1. MATCH: [Thuisploeg] vs [Uitploeg] ([Competitie])
2. LIVE SCORE: [Score] ([Minute])
3. RECENTE EVENTS: [Goals, kaarten, blessures, wissels]
4. MATCH STATISTIEKEN: [Ballbezit, schoten, corners, etc.]
5. BASIS VOORSPELLING: [Pre-match voorspelling]

ANALYSE HIERARCHIE:
1. MOMENTUM SHIFT DETECTION (Hoogste prioriteit):
   - Goal net gescoord? → Welk team heeft momentum?
   - Rode kaart? → Numeriek voordeel/disadvantage
   - Blessure sleutelspeler? → Tactische impact
   - Wissel die formatie verandert? → Tactische shift

2. TACTICAL IMPACT ANALYSIS:
   - Scoreline verandering → Offensief/defensief gedrag
   - Kaarten → Agressie niveau, speelruimte
   - Wissels → Nieuwe spelers, formatie wijziging
   - Tijd → Urgentie (laat in de wedstrijd)

3. PREDICTION ADJUSTMENT:
   - Update win/draw/loss probabilities
   - Aanpassen expected goals
   - Re-calculeren score voorspelling
   - Confidence adjustment (meer data = hoger confidence)

OUTPUT REGELS:
- Geef 1-2 key insights (max 20 woorden elk)
- Update voorspelling percentages
- Geef "momentum indicator" (0-100)
- Confidence: 0.0-1.0 (hoe zeker van de analyse)

JSON FORMAT:
{
  "key_insights": ["Insight 1", "Insight 2"],
  "momentum_home": 0-100,
  "momentum_away": 0-100,
  "prediction_adjustment": "+5% home win" of "-10% away win",
  "confidence": 0.85
}
"""
        
        // Event type weights for momentum calculation
        private const val GOAL_WEIGHT = 30
        private const val RED_CARD_WEIGHT = 25
        private const val YELLOW_CARD_WEIGHT = 5
        private const val INJURY_WEIGHT = 20
        private const val SUBSTITUTION_WEIGHT = 10
        private const val TACTICAL_CHANGE_WEIGHT = 15
    }
    
    /**
     * Analyze live events and provide real-time insights.
     * 
     * @param match The live match
     * @param liveData Current live match data
     * @param matchDetail Match details for context
     * @param basePrediction Pre-match prediction
     * @return LiveEventAnalysis with real-time insights
     */
    suspend fun analyzeLiveEvents(
        match: MatchFixture,
        liveData: LiveMatchData,
        matchDetail: MatchDetail? = null,
        basePrediction: MatchPrediction? = null
    ): Result<LiveEventAnalysis> = withContext(Dispatchers.IO) {
        try {
            // 1. Calculate momentum based on recent events
            val momentum = calculateMomentum(liveData)
            
            // 2. Detect key events and their impact
            val keyEvents = detectKeyEvents(liveData)
            
            // 3. Generate AI insights if match detail is available
            val aiInsights = if (matchDetail != null && match.fixtureId != null) {
                generateAiInsights(match, matchDetail, liveData, basePrediction)
            } else {
                generateBasicInsights(match, liveData)
            }
            
            // 4. Adjust prediction based on live events
            val adjustedPrediction = adjustPrediction(basePrediction, liveData, momentum, match)
            
            // 5. Calculate confidence
            val confidence = calculateConfidence(liveData, keyEvents.size)
            
            val analysis = LiveEventAnalysis(
                fixtureId = match.fixtureId ?: 0,
                homeTeam = match.homeTeam,
                awayTeam = match.awayTeam,
                timestamp = System.currentTimeMillis(),
                confidence = confidence,
                keyInsights = aiInsights,
                momentumShift = if (momentum.homeMomentum > momentum.awayMomentum + 20) {
                    "Momentum bij ${match.homeTeam}"
                } else if (momentum.awayMomentum > momentum.homeMomentum + 20) {
                    "Momentum bij ${match.awayTeam}"
                } else {
                    "Evenwichtig momentum"
                },
                predictedOutcome = adjustedPrediction,
                riskLevel = if (confidence > 0.8) "Laag" else if (confidence > 0.6) "Gemiddeld" else "Hoog",
                bettingOpportunities = if (aiInsights.isNotEmpty()) listOf("Live betting kansen beschikbaar") else emptyList(),
                nextExpectedEvent = "Volgende grote kans",
                analysisSummary = "Live analyse van ${match.homeTeam} vs ${match.awayTeam}"
            )
            
            Result.success(analysis)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Calculate momentum for both teams based on live events.
     */
    private fun calculateMomentum(liveData: LiveMatchData): MomentumResult {
        var homeMomentum = 50 // Start at neutral
        var awayMomentum = 50
        
        // Recent events analysis
        liveData.events.takeLast(10).forEach { event ->
            when (event.type) {
                EventType.GOAL -> {
                    if (event.team == "home") {
                        homeMomentum += GOAL_WEIGHT
                        awayMomentum -= GOAL_WEIGHT / 2
                    } else {
                        awayMomentum += GOAL_WEIGHT
                        homeMomentum -= GOAL_WEIGHT / 2
                    }
                }
                EventType.RED_CARD -> {
                    if (event.team == "home") {
                        homeMomentum -= RED_CARD_WEIGHT
                        awayMomentum += RED_CARD_WEIGHT / 2
                    } else {
                        awayMomentum -= RED_CARD_WEIGHT
                        homeMomentum += RED_CARD_WEIGHT / 2
                    }
                }
                EventType.YELLOW_CARD -> {
                    if (event.team == "home") {
                        homeMomentum -= YELLOW_CARD_WEIGHT
                    } else {
                        awayMomentum -= YELLOW_CARD_WEIGHT
                    }
                }
                EventType.SUBSTITUTION -> {
                    // Substitution - neutral to slightly negative
                    if (event.team == "home") {
                        homeMomentum -= SUBSTITUTION_WEIGHT
                    } else {
                        awayMomentum -= SUBSTITUTION_WEIGHT
                    }
                }
                EventType.VAR -> {
                    // VAR decision - depends on outcome
                    if (event.detail?.contains("goal", ignoreCase = true) == true) {
                        if (event.team == "home") {
                            homeMomentum += GOAL_WEIGHT / 2
                            awayMomentum -= GOAL_WEIGHT / 4
                        } else {
                            awayMomentum += GOAL_WEIGHT / 2
                            homeMomentum -= GOAL_WEIGHT / 4
                        }
                    }
                }
                EventType.PENALTY, EventType.OWN_GOAL, EventType.OTHER -> {
                    // Other event types with medium impact
                    if (event.team == "home") {
                        homeMomentum -= 10
                    } else {
                        awayMomentum -= 10
                    }
                }
                else -> {
                    // Default case for other event types
                    if (event.team == "home") {
                        homeMomentum -= 5
                    } else {
                        awayMomentum -= 5
                    }
                }
            }
        }
        
        // Score-based momentum
        val homeScore = liveData.homeScore
        val awayScore = liveData.awayScore
        
        if (homeScore > awayScore) {
            homeMomentum += (homeScore - awayScore) * 10
            awayMomentum -= (homeScore - awayScore) * 5
        } else if (awayScore > homeScore) {
            awayMomentum += (awayScore - homeScore) * 10
            homeMomentum -= (awayScore - homeScore) * 5
        }
        
        // Time-based momentum (late goals more impactful)
        val minute = liveData.elapsedTime ?: 0
        val timeFactor = if (minute > 75) 1.5 else if (minute > 60) 1.2 else 1.0
        
        homeMomentum = (homeMomentum * timeFactor).toInt().coerceIn(0, 100)
        awayMomentum = (awayMomentum * timeFactor).toInt().coerceIn(0, 100)
        
        return MomentumResult(homeMomentum, awayMomentum)
    }
    
    /**
     * Detect key events from live data.
     */
    private fun detectKeyEvents(liveData: LiveMatchData): List<KeyEvent> {
        val keyEvents = mutableListOf<KeyEvent>()
        
        liveData.events.takeLast(5).forEach { event ->
            val importance = when (event.type) {
                EventType.GOAL -> EventImportance.HIGH
                EventType.RED_CARD -> EventImportance.HIGH
                EventType.YELLOW_CARD -> EventImportance.MEDIUM
                EventType.SUBSTITUTION -> EventImportance.MEDIUM
                EventType.VAR -> EventImportance.HIGH
                EventType.OTHER -> EventImportance.HIGH
                EventType.PENALTY -> EventImportance.HIGH
                EventType.OWN_GOAL -> EventImportance.HIGH
                else -> EventImportance.LOW
            }
            
            if (importance != EventImportance.LOW) {
                keyEvents.add(
                    KeyEvent(
                        type = event.type.name,
                        team = event.team,
                        minute = event.minute,
                        detail = event.detail ?: "",
                        importance = importance
                    )
                )
            }
        }
        
        return keyEvents
    }
    
    /**
     * Generate AI insights using NewsImpactAnalyzer.
     */
    private suspend fun generateAiInsights(
        match: MatchFixture,
        matchDetail: MatchDetail,
        liveData: LiveMatchData,
        basePrediction: MatchPrediction?
    ): List<String> {
        return try {
            if (match.fixtureId == null) return emptyList()
            
            // Get API key for DeepSeek
            val apiKey = apiKeyStorage.getDeepSeekApiKey()
            if (apiKey.isNullOrEmpty()) {
                Log.w("LiveEventAnalyzer", "No DeepSeek API key found, using basic insights")
                return generateBasicInsights(match, liveData)
            }
            
            // Create updated prediction based on live score
            val updatedPrediction = basePrediction ?: MatchPrediction(
                fixtureId = match.fixtureId ?: 0,
                homeTeam = match.homeTeam,
                awayTeam = match.awayTeam,
                homeWinProbability = 0.4,
                drawProbability = 0.3,
                awayWinProbability = 0.3,
                expectedGoalsHome = 1.5,
                expectedGoalsAway = 1.2
            )
            
            // Adjust prediction based on current score
            val adjustedPrediction = adjustPredictionForScore(updatedPrediction, liveData)
            
            val result = newsImpactAnalyzer.analyzeNewsImpact(
                fixtureId = match.fixtureId!!,
                matchDetail = matchDetail,
                apiKey = apiKey
            )
            
            if (result.isSuccess) {
                val simulationContext = result.getOrThrow()
                extractInsightsFromSimulationContext(simulationContext, liveData)
            } else {
                generateBasicInsights(match, liveData)
            }
            
        } catch (e: Exception) {
            generateBasicInsights(match, liveData)
        }
    }
    
    /**
     * Generate basic insights without AI analysis.
     */
    private fun generateBasicInsights(match: MatchFixture, liveData: LiveMatchData): List<String> {
        val insights = mutableListOf<String>()
        val homeScore = liveData.homeScore
        val awayScore = liveData.awayScore
        val minute = liveData.elapsedTime ?: 0
        
        // Score-based insights
        when {
            homeScore > awayScore -> {
                insights.add("${match.homeTeam} leidt en controleert de wedstrijd")
                if (minute > 75) {
                    insights.add("${match.awayTeam} heeft dringend goals nodig")
                }
            }
            awayScore > homeScore -> {
                insights.add("${match.awayTeam} verrast met uitoverwicht")
                if (minute > 75) {
                    insights.add("${match.homeTeam} onder druk om gelijk te maken")
                }
            }
            else -> {
                insights.add("Evenwichtige wedstrijd, beide teams zoeken opening")
            }
        }
        
        // Time-based insights
        when {
            minute < 25 -> insights.add("Vroege fase, teams zoeken ritme")
            minute in 25..40 -> insights.add("Wedstrijd ontwikkelt zich, kansen ontstaan")
            minute in 40..45 -> insights.add("Voor rust, belangrijke minuten")
            minute in 45..60 -> insights.add("Tweede helft begonnen, frisse start")
            minute in 60..75 -> insights.add("Cruciale fase, beslissende momenten")
            minute > 75 -> insights.add("Eindfase, alles of niets")
        }
        
        return insights.take(2)
    }
    
    /**
     * Adjust prediction based on live score and events.
     */
    private fun adjustPredictionForScore(
        basePrediction: MatchPrediction,
        liveData: LiveMatchData
    ): MatchPrediction {
        val homeScore = liveData.homeScore
        val awayScore = liveData.awayScore
        val minute = liveData.elapsedTime ?: 0
        
        // Calculate time factor (more weight later in game)
        val timeFactor = minute / 90.0
        
        // Adjust probabilities based on current score
        var homeWinProb = basePrediction.homeWinProbability
        var drawProb = basePrediction.drawProbability
        var awayWinProb = basePrediction.awayWinProbability
        
        when {
            homeScore > awayScore -> {
                // Home team leading
                val leadFactor = (homeScore - awayScore) * 0.15 * timeFactor
                homeWinProb += leadFactor
                drawProb -= leadFactor * 0.5
                awayWinProb -= leadFactor * 0.5
            }
            awayScore > homeScore -> {
                // Away team leading
                val leadFactor = (awayScore - homeScore) * 0.15 * timeFactor
                awayWinProb += leadFactor
                drawProb -= leadFactor * 0.5
                homeWinProb -= leadFactor * 0.5
            }
            // Draw - no adjustment needed
        }
        
        // Adjust expected goals based on current score
        val expectedGoalsHome = basePrediction.expectedGoalsHome + (homeScore * 0.3)
        val expectedGoalsAway = basePrediction.expectedGoalsAway + (awayScore * 0.3)
        
        return MatchPrediction(
            fixtureId = basePrediction.fixtureId,
            homeTeam = basePrediction.homeTeam,
            awayTeam = basePrediction.awayTeam,
            homeWinProbability = homeWinProb.coerceIn(0.0, 1.0),
            drawProbability = drawProb.coerceIn(0.0, 1.0),
            awayWinProbability = awayWinProb.coerceIn(0.0, 1.0),
            expectedGoalsHome = expectedGoalsHome.coerceAtLeast(0.0),
            expectedGoalsAway = expectedGoalsAway.coerceAtLeast(0.0)
        )
    }
    
    /**
     * Extract insights from SimulationContext.
     */
    private fun extractInsightsFromSimulationContext(
        simulationContext: com.Lyno.matchmindai.domain.model.SimulationContext,
        liveData: LiveMatchData
    ): List<String> {
        val insights = mutableListOf<String>()
        
        // Fatigue insights
        if (simulationContext.hasHighFatigue) {
            insights.add("Hoge vermoeidheid kan prestaties beïnvloeden")
        }
        
        // Style matchup insights
        if (simulationContext.hasStyleAdvantage) {
            insights.add("Tactisch voordeel in speelstijl")
        } else if (simulationContext.hasStyleDisadvantage) {
            insights.add("Tactisch nadeel in speelstijl")
        }
        
        // Lineup strength insights
        if (simulationContext.hasWeakLineup) {
            insights.add("Zwakke opstelling - sleutelspelers missen")
        }
        
        // Legacy distraction/fitness insights
        if (simulationContext.hasHighDistraction) {
            insights.add("Hoge afleiding - concentratieproblemen")
        }
        
        if (simulationContext.hasLowFitness) {
            insights.add("Lage fitheid - fysieke beperkingen")
        }
        
        return insights.take(2)
    }
    
    /**
     * Adjust prediction based on live events.
     */
    private fun adjustPrediction(
        basePrediction: MatchPrediction?,
        liveData: LiveMatchData,
        momentum: MomentumResult,
        match: MatchFixture
    ): String? {
        if (basePrediction == null) return null
        
        val homeMomentum = momentum.homeMomentum
        val awayMomentum = momentum.awayMomentum
        
        val momentumDiff = homeMomentum - awayMomentum
        
        return when {
            momentumDiff > 30 -> "${match.homeTeam} winstkans +${(momentumDiff / 3)}%"
            momentumDiff < -30 -> "${match.awayTeam} winstkans +${(-momentumDiff / 3)}%"
            momentumDiff > 15 -> "Lichte shift naar ${match.homeTeam}"
            momentumDiff < -15 -> "Lichte shift naar ${match.awayTeam}"
            else -> null
        }
    }
    
    /**
     * Calculate confidence in the analysis.
     */
    private fun calculateConfidence(
        liveData: LiveMatchData,
        numberOfKeyEvents: Int
    ): Double {
        var confidence = 0.5 // Base confidence
        
        // More key events = higher confidence
        confidence += numberOfKeyEvents * 0.1
        
        // Later in game = higher confidence
        val minute = liveData.elapsedTime ?: 0
        confidence += (minute / 90.0) * 0.3
        
        // Score data available = higher confidence
        confidence += 0.1
        
        // Events data available = higher confidence
        if (liveData.events.isNotEmpty()) {
            confidence += 0.1
        }
        
        return confidence.coerceIn(0.3, 1.0)
    }
    
    /**
     * Data class for momentum calculation result.
     */
    private data class MomentumResult(
        val homeMomentum: Int,
        val awayMomentum: Int
    )
    
    /**
     * Data class for key event detection.
     */
    private data class KeyEvent(
        val type: String,
        val team: String,
        val minute: Int,
        val detail: String,
        val importance: EventImportance
    )
    
    /**
     * Enum for event importance levels.
     */
    private enum class EventImportance {
        LOW, MEDIUM, HIGH
    }
}
