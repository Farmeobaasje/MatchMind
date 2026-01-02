package com.Lyno.matchmindai.domain.service

import com.Lyno.matchmindai.data.remote.DeepSeekApi
import com.Lyno.matchmindai.domain.model.MatchDetail
import com.Lyno.matchmindai.domain.model.NewsImpactModifiers
import com.Lyno.matchmindai.domain.model.SimulationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Enhanced NewsImpactAnalyzer with DeepSeek AI integration.
 * Translates qualitative news into quantitative model modifiers using the optimized system prompt.
 * 
 * Based on the "Feature Engineering" approach from 10_dixon_coles_kelly_mastermind_integration.md.
 */
class NewsImpactAnalyzer(
    private val deepSeekApi: DeepSeekApi,
    private val apiKeyStorage: com.Lyno.matchmindai.data.local.ApiKeyStorage
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    companion object {
        /**
         * Optimized system prompt for feature engineering.
         * From section 3.4 of 10_dixon_coles_kelly_mastermind_integration.md
         */
        const val FEATURE_ENGINEERING_SYSTEM_PROMPT = """
            JIJ BENT: Een Senior Data Scientist gespecialiseerd in voetbalmodellen.
            DOEL: Vertaal kwalitatief nieuws naar kwantitatieve model-modifiers.
            
            INPUT DATA:
            1. Teamnamen & Competitie
            2. Basis Voorspelling (Win/Gelijk/Verlies %)
            3. Nieuws Snippets (Blessures, Ruzies, Transfers)
            
            OUTPUT REGELS (JSON):
            - Modifiers moeten tussen 0.5 (catastrofaal) en 1.5 (perfect) liggen.
            - 1.0 is neutraal (geen nieuws = 1.0).
            - 'confidence': Hoe zeker ben je van het nieuws? (0.0 - 1.0).
            - 'chaos_factor': 0.0 (Saai) tot 1.0 (Totale chaos/Derby).
            
            RANGE GUIDE:
            - 0.85 - 0.95: Belangrijke speler twijfelachtig/lichte blessure.
            - 0.70 - 0.80: Sterspeler definitief afwezig.
            - < 0.70: Meerdere sleutelspelers weg / Team in crisis.
            - > 1.10: Team in topvorm / Manager 'bounce' effect.
            
            BELANGRIJK:
            - Wees CONSERVATIEF. Bij twijfel, houd modifiers dicht bij 1.0.
            - Hallucineer GEEN blessures die niet in de snippets staan.
        """

        /**
         * Confidence threshold for applying modifiers (from section 9.1).
         */
        private const val MIN_CONFIDENCE_THRESHOLD = 0.4

        /**
         * Modifier bounds (from section 9.2).
         */
        private const val MODIFIER_LOWER_BOUND = 0.5
        private const val MODIFIER_UPPER_BOUND = 1.5
    }

    /**
     * AI response format for news impact analysis.
     */
    @Serializable
    data class NewsImpactResponse(
        val homeAttackMod: Double,
        val homeDefenseMod: Double,
        val awayAttackMod: Double,
        val awayDefenseMod: Double,
        val confidence: Double,
        val reasoning: String,
        val chaosFactor: Double,
        val newsRelevance: Double
    ) {
        init {
            require(homeAttackMod in MODIFIER_LOWER_BOUND..MODIFIER_UPPER_BOUND) { "homeAttackMod must be between $MODIFIER_LOWER_BOUND and $MODIFIER_UPPER_BOUND" }
            require(homeDefenseMod in MODIFIER_LOWER_BOUND..MODIFIER_UPPER_BOUND) { "homeDefenseMod must be between $MODIFIER_LOWER_BOUND and $MODIFIER_UPPER_BOUND" }
            require(awayAttackMod in MODIFIER_LOWER_BOUND..MODIFIER_UPPER_BOUND) { "awayAttackMod must be between $MODIFIER_LOWER_BOUND and $MODIFIER_UPPER_BOUND" }
            require(awayDefenseMod in MODIFIER_LOWER_BOUND..MODIFIER_UPPER_BOUND) { "awayDefenseMod must be between $MODIFIER_LOWER_BOUND and $MODIFIER_UPPER_BOUND" }
            require(confidence in 0.0..1.0) { "confidence must be between 0.0 and 1.0" }
            require(chaosFactor in 0.0..1.0) { "chaosFactor must be between 0.0 and 1.0" }
            require(newsRelevance in 0.0..1.0) { "newsRelevance must be between 0.0 and 1.0" }
        }

        /**
         * Convert to NewsImpactModifiers domain model.
         */
        fun toNewsImpactModifiers(): NewsImpactModifiers {
            return NewsImpactModifiers(
                homeAttackMod = homeAttackMod,
                homeDefenseMod = homeDefenseMod,
                awayAttackMod = awayAttackMod,
                awayDefenseMod = awayDefenseMod,
                confidence = confidence,
                reasoning = reasoning,
                chaosFactor = chaosFactor,
                newsRelevance = newsRelevance
            )
        }
    }

    /**
     * Generate a match scenario with AI-powered news impact analysis.
     * 
     * @param fixtureId The fixture ID
     * @param matchDetail The match details
     * @param apiKey User's DeepSeek API key
     * @param newsSnippets Optional news snippets for analysis
     * @param basePrediction Optional base prediction percentages
     * @return Result with SimulationContext containing AI analysis
     */
    suspend fun generateMatchScenario(
        fixtureId: Int,
        matchDetail: MatchDetail,
        apiKey: String,
        newsSnippets: List<String> = emptyList(),
        basePrediction: Triple<Double, Double, Double>? = null // (homeWin%, draw%, awayWin%)
    ): Result<SimulationContext> = withContext(Dispatchers.IO) {
        try {
            // Build the analysis prompt
            val prompt = buildAnalysisPrompt(matchDetail, newsSnippets, basePrediction)
            
            // Send to DeepSeek API with JSON response format
            val aiResponse = deepSeekApi.sendPrompt(
                prompt = prompt,
                responseFormat = "json_object",
                apiKey = apiKey
            )
            
            if (aiResponse.isFailure) {
                return@withContext Result.failure(
                    aiResponse.exceptionOrNull() ?: Exception("DeepSeek API failed")
                )
            }

            val responseText = aiResponse.getOrNull()?.choices?.firstOrNull()?.message?.content
                ?: return@withContext Result.failure(Exception("Empty response from DeepSeek"))

            // Parse AI response
            val parsedResponse =    json.decodeFromString<NewsImpactResponse>(responseText)
            
            // Only apply modifiers if confidence meets threshold
            val modifiers = if (parsedResponse.confidence >= MIN_CONFIDENCE_THRESHOLD) {
                parsedResponse.toNewsImpactModifiers()
            } else {
                // Return neutral modifiers with low confidence
                NewsImpactModifiers(
                    homeAttackMod = 1.0,
                    homeDefenseMod = 1.0,
                    awayAttackMod = 1.0,
                    awayDefenseMod = 1.0,
                    confidence = parsedResponse.confidence,
                    reasoning = "AI confidence too low (${(parsedResponse.confidence * 100).toInt()}% < ${(MIN_CONFIDENCE_THRESHOLD * 100).toInt()}%). Using neutral modifiers.",
                    chaosFactor = parsedResponse.chaosFactor,
                    newsRelevance = parsedResponse.newsRelevance
                )
            }

            // Convert to SimulationContext
            val simulationContext = SimulationContext(
                fatigueScore = calculateFatigueFromChaos(modifiers.chaosFactor),
                styleMatchup = calculateStyleMatchup(modifiers),
                lineupStrength = calculateLineupStrength(modifiers),
                reasoning = modifiers.reasoning,
                // Legacy fields
                homeFitness = 100 - calculateFatigueFromChaos(modifiers.chaosFactor),
                awayFitness = 100 - calculateFatigueFromChaos(modifiers.chaosFactor),
                homeDistraction = 100 - calculateLineupStrength(modifiers),
                awayDistraction = 100 - calculateLineupStrength(modifiers)
            )

            Result.success(simulationContext)

        } catch (e: Exception) {
            Result.failure(Exception("News impact analysis failed: ${e.message}", e))
        }
    }

    /**
     * Analyze news impact for a match (legacy method).
     * 
     * @param fixtureId The fixture ID
     * @param matchDetail The match details
     * @param apiKey User's DeepSeek API key
     * @return Result with SimulationContext containing AI analysis
     */
    suspend fun analyzeNewsImpact(
        fixtureId: Int,
        matchDetail: MatchDetail,
        apiKey: String
    ): Result<SimulationContext> = withContext(Dispatchers.IO) {
        generateMatchScenario(fixtureId, matchDetail, apiKey)
    }

    /**
     * Validate Oracle prediction with AI analysis.
     * 
     * @param fixtureId The fixture ID
     * @param matchDetail The match details
     * @param oracleAnalysis The Oracle analysis to validate
     * @param apiKey User's DeepSeek API key
     * @return Result with SimulationContext containing validation analysis
     */
    suspend fun validatePrediction(
        fixtureId: Int,
        matchDetail: MatchDetail,
        oracleAnalysis: com.Lyno.matchmindai.domain.model.OracleAnalysis,
        apiKey: String
    ): Result<SimulationContext> = withContext(Dispatchers.IO) {
        // Use Oracle analysis as news snippets
        val newsSnippets = listOf(
            "Oracle voorspelling: ${oracleAnalysis.prediction}",
            "Oracle vertrouwen: ${oracleAnalysis.confidence}%",
            "Oracle reden: ${oracleAnalysis.reasoning}"
        )
        
        generateMatchScenario(fixtureId, matchDetail, apiKey, newsSnippets)
    }

    /**
     * Generate match scenario as Flow (for reactive UI).
     * 
     * @param fixtureId The fixture ID
     * @param matchDetail The match details
     * @param apiKey User's DeepSeek API key
     * @param newsSnippets Optional news snippets for analysis
     * @return Flow emitting SimulationContext with AI analysis
     */
    fun generateMatchScenarioFlow(
        fixtureId: Int,
        matchDetail: MatchDetail,
        apiKey: String,
        newsSnippets: List<String> = emptyList()
    ): Flow<Result<SimulationContext>> = flow {
        emit(generateMatchScenario(fixtureId, matchDetail, apiKey, newsSnippets))
    }

    /**
     * Build the analysis prompt for DeepSeek.
     */
    private fun buildAnalysisPrompt(
        matchDetail: MatchDetail,
        newsSnippets: List<String>,
        basePrediction: Triple<Double, Double, Double>?
    ): String {
        val homeTeam = matchDetail.homeTeam
        val awayTeam = matchDetail.awayTeam
        val league = matchDetail.league ?: "Onbekende competitie"
        
        val basePredictionText = if (basePrediction != null) {
            "Thuis Winst: ${(basePrediction.first * 100).toInt()}%, " +
            "Gelijk: ${(basePrediction.second * 100).toInt()}%, " +
            "Uit Winst: ${(basePrediction.third * 100).toInt()}%"
        } else {
            "Geen basisvoorspelling beschikbaar"
        }
        
        val newsText = if (newsSnippets.isNotEmpty()) {
            newsSnippets.joinToString("\n") { "• $it" }
        } else {
            "Geen nieuws snippets beschikbaar"
        }
        
        return """
            $FEATURE_ENGINEERING_SYSTEM_PROMPT
            
            SPECIFIEKE MATCH DATA:
            
            1. TEAMS & COMPETITIE:
               - Thuis: $homeTeam
               - Uit: $awayTeam
               - Competitie: $league
            
            2. BASIS VOORSPELLING:
               - $basePredictionText
            
            3. NIEUWS SNIPPETS:
               $newsText
            
            OUTPUT (JSON FORMAT):
            {
              "homeAttackMod": 1.0,
              "homeDefenseMod": 1.0,
              "awayAttackMod": 1.0,
              "awayDefenseMod": 1.0,
              "confidence": 0.5,
              "reasoning": "Korte, zakelijke reden voor de modifiers",
              "chaosFactor": 0.5,
              "newsRelevance": 0.5
            }
            
            REGELS:
            - Gebruik alleen de gegeven nieuws snippets
            - Wees conservatief met modifiers
            - Geef concrete cijfers, geen vage taal
            - Output MOET geldige JSON zijn
        """.trimIndent()
    }

    /**
     * Calculate fatigue score from chaos factor.
     */
    private fun calculateFatigueFromChaos(chaosFactor: Double): Int {
        // Higher chaos = higher fatigue (0-100)
        return (chaosFactor * 100).toInt().coerceIn(0, 100)
    }

    /**
     * Calculate style matchup from modifiers.
     */
    private fun calculateStyleMatchup(modifiers: NewsImpactModifiers): Double {
        // Style matchup advantage for home team (0.5-1.5)
        // Based on relative attack/defense modifiers
        val homeAdvantage = (modifiers.homeAttackMod / modifiers.awayDefenseMod).coerceIn(0.5, 1.5)
        return homeAdvantage
    }

    /**
     * Calculate lineup strength from modifiers.
     */
    private fun calculateLineupStrength(modifiers: NewsImpactModifiers): Int {
        // Average of all modifiers, scaled to 0-100
        val averageModifier = (modifiers.homeAttackMod + modifiers.homeDefenseMod + 
                              modifiers.awayAttackMod + modifiers.awayDefenseMod) / 4.0
        // Convert to 0-100 scale where 1.0 = 50%
        val strength = ((averageModifier - 0.5) * 100).toInt().coerceIn(0, 100)
        return strength
    }

    /**
     * Fallback analysis when AI service is unavailable.
     * Returns neutral SimulationContext.
     */
    suspend fun generateFallbackScenario(
        fixtureId: Int,
        matchDetail: MatchDetail
    ): SimulationContext {
        return SimulationContext.NEUTRAL.copy(
            reasoning = "AI analysis unavailable. Using neutral context."
        )
    }
}
