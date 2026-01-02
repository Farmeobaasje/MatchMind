package com.Lyno.matchmindai.domain.service

import android.util.Log
import com.Lyno.matchmindai.domain.model.AiAnalysisResult
import com.Lyno.matchmindai.domain.model.HeroMatchExplanation
import com.Lyno.matchmindai.domain.model.MatchDetail
import com.Lyno.matchmindai.domain.model.MatchFixture
import com.Lyno.matchmindai.domain.model.MatchPrediction
import com.Lyno.matchmindai.domain.model.NewsImpactModifiers
import com.Lyno.matchmindai.domain.service.CuratedFeed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

/**
 * Service that explains why a match was selected as the hero match in the dashboard.
 * Uses AI analysis to provide explainable insights into match curation.
 */
class HeroMatchExplainer(
    private val newsImpactAnalyzer: NewsImpactAnalyzer,
    private val mastermindEngine: com.Lyno.matchmindai.domain.mastermind.MastermindEngine,
    private val apiKeyStorage: com.Lyno.matchmindai.data.local.ApiKeyStorage
) {
    
    companion object {
        // System prompt for hero match explanation
        const val HERO_MATCH_EXPLANATION_PROMPT = """
JIJ BENT: Een Senior Football Analyst gespecialiseerd in match curation.
DOEL: Leg uit waarom deze wedstrijd is geselecteerd als "Hero Match" in het dashboard.

INPUT DATA:
1. MATCH: [Thuisploeg] vs [Uitploeg] ([Competitie])
2. CURATED FEED CONTEXT: [Aantal live/upcoming matches, league distribution]
3. AI ANALYSIS: [Modifiers, chaos score, atmosphere score, mastermind insights]
4. MATCH CURATION SCORE: [Score van 0-10000]

ANALYSE HIERARCHIE:
1. MATCH QUALITY FACTORS (Hoogste impact):
   - Team kwaliteit (top teams vs middenmoters)
   - Competitie belang (top league vs lower division)
   - Historische rivaliteit (derby, klassieker)
   - Wedstrijd belang (kampioenschap, degradatie, Europees ticket)

2. CONTEXTUAL FACTORS:
   - Timing (prime time, weekend vs doordeweeks)
   - Beschikbaarheid (geen andere top matches op dat moment)
   - Verhaal (narratief, terugkeer speler, manager tegen oude club)

3. AI-ENHANCED FACTORS:
   - Chaos score (hoge chaos = entertainment waarde)
   - Atmosphere score (thuisvoordeel, sfeer)
   - Mastermind scenario (interessant "script")

OUTPUT REGELS:
- Geef 2-3 primaire redenen (meest impactvol)
- Geef 2-3 secundaire factoren (ondersteunend)
- Geef betting implicaties indien relevant
- Gebruik Mastermind insights indien beschikbaar
- Confidence: 0.0-1.0 (hoe zeker ben je van de uitleg)

JSON FORMAT:
{
  "primary_reasons": ["Reden 1", "Reden 2", "Reden 3"],
  "secondary_factors": ["Factor 1", "Factor 2"],
  "betting_implications": "Optionele betting tip",
  "confidence": 0.85
}
"""
    }
    
    /**
     * Explain why a match was selected as the hero match.
     * 
     * @param match The hero match to explain
     * @param curatedFeed The full curated feed for context
     * @param matchDetail Optional match details for deeper analysis
     * @param curationScore The curation score from MatchCuratorService
     * @return HeroMatchExplanation with AI-powered insights
     */
    suspend fun explainHeroMatchSelection(
        match: MatchFixture,
        curatedFeed: CuratedFeed,
        matchDetail: MatchDetail? = null,
        curationScore: Int = 0
    ): Result<HeroMatchExplanation> = withContext(Dispatchers.IO) {
        try {
            // 1. Get AI analysis for the match
            val aiAnalysis = getAiAnalysisForMatch(match, matchDetail)
            
            // 2. Get Mastermind insights if available
            val mastermindInsights = getMastermindInsights(match, aiAnalysis)
            
            // 3. Extract primary reasons based on match characteristics
            val primaryReasons = extractPrimaryReasons(match, curatedFeed, aiAnalysis, curationScore)
            
            // 4. Extract secondary factors
            val secondaryFactors = extractSecondaryFactors(match, curatedFeed, aiAnalysis)
            
            // 5. Generate betting implications
            val bettingImplications = generateBettingImplications(match, aiAnalysis)
            
            // 6. Calculate AI confidence
            val aiConfidence = calculateConfidence(aiAnalysis, primaryReasons.size)
            
            // 7. Get chaos and atmosphere levels
            val chaosLevel = aiAnalysis.getChaosLevel()
            val atmosphereLevel = aiAnalysis.getAtmosphereLevel()
            
            val explanation = HeroMatchExplanation(
                match = match,
                primaryReasons = primaryReasons,
                secondaryFactors = secondaryFactors,
                aiConfidence = aiConfidence,
                bettingImplications = bettingImplications,
                mastermindInsights = mastermindInsights,
                chaosLevel = chaosLevel,
                atmosphereLevel = atmosphereLevel
            )
            
            Result.success(explanation)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get AI analysis for the match using NewsImpactAnalyzer.
     */
    private suspend fun getAiAnalysisForMatch(
        match: MatchFixture,
        matchDetail: MatchDetail?
    ): AiAnalysisResult {
        return if (matchDetail != null && match.fixtureId != null) {
            // Get API key for DeepSeek
            val apiKey = apiKeyStorage.getDeepSeekApiKey()
            if (apiKey.isNullOrEmpty()) {
                Log.w("HeroMatchExplainer", "No DeepSeek API key found, using default analysis")
                return createDefaultAiAnalysis(match)
            }
            
            // Create a base prediction for analysis
            val basePrediction = MatchPrediction(
                fixtureId = match.fixtureId!!,
                homeTeam = match.homeTeam,
                awayTeam = match.awayTeam,
                homeWinProbability = 0.4,
                drawProbability = 0.3,
                awayWinProbability = 0.3,
                expectedGoalsHome = 1.5,
                expectedGoalsAway = 1.2
            )
            
            val result = newsImpactAnalyzer.analyzeNewsImpact(
                fixtureId = match.fixtureId!!,
                matchDetail = matchDetail,
                apiKey = apiKey
            )
            
            if (result.isSuccess) {
                // Convert SimulationContext to AiAnalysisResult
                val simulationContext = result.getOrThrow()
                createAiAnalysisFromSimulationContext(simulationContext, match)
            } else {
                createDefaultAiAnalysis(match)
            }
        } else {
            createDefaultAiAnalysis(match)
        }
    }
    
    /**
     * Create AI analysis from SimulationContext.
     */
    private fun createAiAnalysisFromSimulationContext(
        simulationContext: com.Lyno.matchmindai.domain.model.SimulationContext,
        match: MatchFixture
    ): AiAnalysisResult {
        return AiAnalysisResult(
            home_attack_modifier = 1.0, // Default for Phase 1
            away_defense_modifier = 1.0,
            away_attack_modifier = 1.0,
            home_defense_modifier = 1.0,
            chaos_score = 50, // Default for Phase 1
            atmosphere_score = calculateAtmosphereScore(match),
            confidence_score = 50, // Default for Phase 1
            reasoning_short = simulationContext.reasoning.take(200),
            news_relevant = true,
            fatigue_score = simulationContext.fatigueScore,
            style_matchup = simulationContext.styleMatchup,
            lineup_strength = simulationContext.lineupStrength
        )
    }
    
    /**
     * Calculate atmosphere score based on match characteristics.
     */
    private fun calculateAtmosphereScore(match: MatchFixture): Int {
        // Simple heuristic: higher for home teams with strong history
        return when {
            match.leagueId in setOf(39, 88, 140, 78, 135, 61) -> 70 // Top leagues
            match.status == "LIVE" -> 80 // Live matches have atmosphere
            else -> 50 // Neutral
        }
    }
    
    /**
     * Create default AI analysis when real analysis is not available.
     */
    private fun createDefaultAiAnalysis(match: MatchFixture): AiAnalysisResult {
        return AiAnalysisResult(
            home_attack_modifier = 1.0,
            away_defense_modifier = 1.0,
            away_attack_modifier = 1.0,
            home_defense_modifier = 1.0,
            chaos_score = 50,
            atmosphere_score = calculateAtmosphereScore(match),
            confidence_score = 50,
            reasoning_short = "Standaard analyse voor ${match.homeTeam} vs ${match.awayTeam}",
            news_relevant = true
        )
    }
    
    /**
     * Get Mastermind insights for the match.
     */
    private suspend fun getMastermindInsights(
        match: MatchFixture,
        aiAnalysis: AiAnalysisResult
    ): List<String> {
        return try {
            // For now, return empty list as MastermindEngine doesn't have analyzeMatch function
            // TODO: Implement proper Mastermind insights when available
            emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Extract primary reasons for hero match selection.
     */
    private fun extractPrimaryReasons(
        match: MatchFixture,
        curatedFeed: CuratedFeed,
        aiAnalysis: AiAnalysisResult,
        curationScore: Int
    ): List<String> {
        val reasons = mutableListOf<String>()
        
        // 1. Check if it's a top league match
        if (match.leagueId in setOf(39, 88, 140, 78, 135, 61)) {
            val leagueName = when (match.leagueId) {
                39 -> "Premier League"
                88 -> "Eredivisie"
                140 -> "La Liga"
                78 -> "Bundesliga"
                135 -> "Serie A"
                61 -> "Ligue 1"
                else -> "top competitie"
            }
            reasons.add("$leagueName topwedstrijd")
        }
        
        // 2. Check if it's a derby or rivalry
        if (isDerbyMatch(match)) {
            reasons.add("Historische rivaliteit/derby")
        }
        
        // 3. Check AI chaos score (high chaos = entertaining)
        if (aiAnalysis.chaos_score > 70) {
            reasons.add("Hoge entertainment waarde (chaos score: ${aiAnalysis.chaos_score})")
        }
        
        // 4. Check if it's the only top match at this time
        if (isOnlyTopMatchAtTime(match, curatedFeed)) {
            reasons.add("Enige topwedstrijd op dit tijdstip")
        }
        
        // 5. Check curation score
        if (curationScore > 5000) {
            reasons.add("Uitzonderlijk hoge curation score ($curationScore)")
        }
        
        // 6. Check if match is live
        if (match.status in listOf("1H", "2H", "HT", "LIVE")) {
            reasons.add("Live wedstrijd met actie")
        }
        
        return reasons.take(3) // Return top 3 reasons
    }
    
    /**
     * Extract secondary factors for hero match selection.
     */
    private fun extractSecondaryFactors(
        match: MatchFixture,
        curatedFeed: CuratedFeed,
        aiAnalysis: AiAnalysisResult
    ): List<String> {
        val factors = mutableListOf<String>()
        
        // 1. Team form or ranking
        factors.add("${match.homeTeam} vs ${match.awayTeam} - evenwichtige confrontatie")
        
        // 2. Time slot
        if (match.time?.contains(":") == true) {
            val hour = match.time.substringBefore(":").toIntOrNull()
            if (hour in 18..22) {
                factors.add("Prime time slot (${match.time})")
            }
        }
        
        // 3. AI confidence
        if (aiAnalysis.confidence_score > 70) {
            factors.add("Hoge AI betrouwbaarheid (${aiAnalysis.confidence_score}%)")
        }
        
        // 4. Atmosphere score
        if (aiAnalysis.atmosphere_score > 70) {
            factors.add("Sterke thuissfeer verwacht")
        }
        
        return factors.take(3)
    }
    
    /**
     * Generate betting implications based on match analysis.
     */
    private fun generateBettingImplications(
        match: MatchFixture,
        aiAnalysis: AiAnalysisResult
    ): String? {
        if (!aiAnalysis.hasMeaningfulData()) {
            return null
        }
        
        val homeTeam = match.homeTeam
        val awayTeam = match.awayTeam
        
        return when {
            aiAnalysis.home_attack_modifier > 1.2 && aiAnalysis.atmosphere_score > 80 ->
                "$homeTeam wint & Over 2.5 goals (sterk thuisvoordeel)"
            
            aiAnalysis.chaos_score > 70 ->
                "Over 2.5 goals & Beide teams scoren (hoge chaos verwacht)"
            
            aiAnalysis.home_attack_modifier > aiAnalysis.away_attack_modifier ->
                "$homeTeam wint of Gelijk (thuisvoordeel)"
            
            else ->
                "Beide teams scoren (evenwichtige wedstrijd)"
        }
    }
    
    /**
     * Calculate confidence in the explanation.
     */
    private fun calculateConfidence(
        aiAnalysis: AiAnalysisResult,
        numberOfReasons: Int
    ): Double {
        var confidence = aiAnalysis.confidence
        
        // Adjust based on number of reasons (more reasons = higher confidence)
        if (numberOfReasons >= 2) {
            confidence = (confidence * 1.2).coerceAtMost(1.0)
        }
        
        // Adjust based on chaos score (high chaos = lower confidence)
        if (aiAnalysis.chaos_score > 70) {
            confidence = (confidence * 0.8).coerceAtLeast(0.3)
        }
        
        return confidence.coerceIn(0.3, 1.0)
    }
    
    /**
     * Check if match is a derby based on team names.
     */
    private fun isDerbyMatch(match: MatchFixture): Boolean {
        val home = match.homeTeam.lowercase()
        val away = match.awayTeam.lowercase()
        
        // Common derby patterns
        val derbyPatterns = listOf(
            "ajax" to "feyenoord",
            "psv" to "feyenoord",
            "ajax" to "psv",
            "real madrid" to "barcelona",
            "barcelona" to "real madrid",
            "man united" to "man city",
            "man city" to "man united",
            "liverpool" to "everton",
            "everton" to "liverpool",
            "ac milan" to "inter",
            "inter" to "ac milan",
            "bayern" to "dortmund",
            "dortmund" to "bayern"
        )
        
        return derbyPatterns.any { (team1, team2) ->
            home.contains(team1) && away.contains(team2) ||
            home.contains(team2) && away.contains(team1)
        }
    }
    
    /**
     * Check if this is the only top match at its time slot.
     */
    private fun isOnlyTopMatchAtTime(
        match: MatchFixture,
        curatedFeed: CuratedFeed
    ): Boolean {
        val matchTime = match.time
        if (matchTime.isNullOrEmpty()) return false
        
        // Count how many other top league matches are at the same time
        val topLeagueIds = setOf(39, 88, 140, 78, 135, 61)
        
        val matchesAtSameTime = (listOfNotNull(curatedFeed.heroMatch) + 
                                curatedFeed.liveMatches + 
                                curatedFeed.upcomingMatches)
            .filter { it.time == matchTime && it.leagueId in topLeagueIds }
            .count()
        
        return matchesAtSameTime <= 1 // This match is the only one or one of very few
    }
}
