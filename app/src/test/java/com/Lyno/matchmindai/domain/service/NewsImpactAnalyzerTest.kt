package com.Lyno.matchmindai.domain.service

import com.Lyno.matchmindai.domain.model.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import java.text.SimpleDateFormat
import java.util.*

class NewsImpactAnalyzerTest {

    private lateinit var newsImpactAnalyzer: NewsImpactAnalyzer
    private lateinit var mockMatchRepository: com.Lyno.matchmindai.domain.repository.MatchRepository

    @Before
    fun setUp() {
        mockMatchRepository = mock(com.Lyno.matchmindai.domain.repository.MatchRepository::class.java)
        newsImpactAnalyzer = NewsImpactAnalyzer(mockMatchRepository)
    }

    @Test
    fun testFeatureEngineeringSystemPrompt() {
        // Test that the system prompt is properly defined
        val systemPrompt = NewsImpactAnalyzer.FEATURE_ENGINEERING_SYSTEM_PROMPT
        
        assertNotNull("System prompt should not be null", systemPrompt)
        assertTrue("System prompt should contain role definition", 
            systemPrompt.contains("JIJ BENT: Een Senior Data Scientist"))
        assertTrue("System prompt should contain output rules", 
            systemPrompt.contains("OUTPUT REGELS (JSON)"))
        assertTrue("System prompt should contain modifier bounds", 
            systemPrompt.contains("Modifiers moeten tussen 0.5 (catastrofaal) en 1.5 (perfect) liggen"))
    }

    @Test
    fun testBuildFeatureEngineeringQuery() {
        // Test that search query is built correctly
        val matchDetail = createMockMatchDetail()
        val query = newsImpactAnalyzer.javaClass.getDeclaredMethod(
            "buildFeatureEngineeringQuery", 
            MatchDetail::class.java
        ).apply { isAccessible = true }.invoke(newsImpactAnalyzer, matchDetail) as String
        
        assertNotNull("Query should not be null", query)
        assertTrue("Query should contain home team name", query.contains("Utrecht"))
        assertTrue("Query should contain away team name", query.contains("PSV"))
        assertTrue("Query should contain league name", query.contains("Eredivisie"))
        assertTrue("Query should contain injury/suspension terms", 
            query.contains("injury") || query.contains("suspension"))
        assertTrue("Query should contain tactical terms", 
            query.contains("tactics") || query.contains("coach"))
    }

    @Test
    fun testBuildFeatureEngineeringPrompt() {
        // Test that prompt is built correctly
        val matchDetail = createMockMatchDetail()
        val basePrediction = createMockBasePrediction()
        val newsHeadlines = listOf(
            "Utrecht key player injured ahead of PSV match",
            "PSV coach confirms tactical changes for away game"
        )
        
        val prompt = newsImpactAnalyzer.javaClass.getDeclaredMethod(
            "buildFeatureEngineeringPrompt",
            MatchDetail::class.java,
            MatchPrediction::class.java,
            List::class.java
        ).apply { isAccessible = true }.invoke(
            newsImpactAnalyzer, matchDetail, basePrediction, newsHeadlines
        ) as String
        
        assertNotNull("Prompt should not be null", prompt)
        assertTrue("Prompt should contain system prompt", 
            prompt.contains(NewsImpactAnalyzer.FEATURE_ENGINEERING_SYSTEM_PROMPT))
        assertTrue("Prompt should contain match context", 
            prompt.contains("Utrecht vs PSV (Eredivisie)"))
        assertTrue("Prompt should contain statistical baseline", 
            prompt.contains("Thuis Winst: 45%"))
        assertTrue("Prompt should contain news context", 
            prompt.contains("Utrecht key player injured"))
        assertTrue("Prompt should contain JSON output format", 
            prompt.contains("\"home_attack_mod\": 1.0"))
        assertTrue("Prompt should contain Dutch language requirement", 
            prompt.contains("ALLE OUTPUT MOET IN HET NEDERLANDS ZIJN"))
    }

    @Test
    fun testConvertToNewsImpactModifiers() {
        // Test conversion from AiAnalysisResult to NewsImpactModifiers
        val aiAnalysis = createMockAiAnalysisResult()
        val hasNews = true
        
        val modifiers = newsImpactAnalyzer.javaClass.getDeclaredMethod(
            "convertToNewsImpactModifiers",
            com.Lyno.matchmindai.domain.model.AiAnalysisResult::class.java,
            Boolean::class.java
        ).apply { isAccessible = true }.invoke(
            newsImpactAnalyzer, aiAnalysis, hasNews
        ) as NewsImpactModifiers
        
        assertNotNull("Modifiers should not be null", modifiers)
        
        // Test that modifiers are within bounds
        assertTrue("Home attack modifier should be within bounds", 
            modifiers.homeAttackMod in 0.5..1.5)
        assertTrue("Home defense modifier should be within bounds", 
            modifiers.homeDefenseMod in 0.5..1.5)
        assertTrue("Away attack modifier should be within bounds", 
            modifiers.awayAttackMod in 0.5..1.5)
        assertTrue("Away defense modifier should be within bounds", 
            modifiers.awayDefenseMod in 0.5..1.5)
        
        // Test that confidence is within bounds
        assertTrue("Confidence should be between 0.0 and 1.0", 
            modifiers.confidence in 0.0..1.0)
        
        // Test that chaos factor is within bounds
        assertTrue("Chaos factor should be between 0.0 and 1.0", 
            modifiers.chaosFactor in 0.0..1.0)
        
        // Test that news relevance is within bounds
        assertTrue("News relevance should be between 0.0 and 1.0", 
            modifiers.newsRelevance in 0.0..1.0)
        
        // Test that reasoning is not empty
        assertTrue("Reasoning should not be empty", 
            modifiers.reasoning.isNotEmpty())
    }

    @Test
    fun testAiAnalysisResultBackwardCompatibility() {
        // Test that AiAnalysisResult supports both old and new JSON formats
        
        // Test with old format (home_attack_modifier, chaos_score, confidence_score)
        val oldFormatJson = """
            {
                "home_attack_modifier": 1.2,
                "home_defense_modifier": 0.9,
                "away_attack_modifier": 1.1,
                "away_defense_modifier": 0.95,
                "chaos_score": 75,
                "confidence_score": 80,
                "reasoning_short": "Test analysis",
                "news_relevant": true
            }
        """.trimIndent()
        
        // Test with new format (home_attack_mod, chaos_factor, confidence)
        val newFormatJson = """
            {
                "home_attack_mod": 1.2,
                "home_defense_mod": 0.9,
                "away_attack_mod": 1.1,
                "away_defense_mod": 0.95,
                "chaos_factor": 0.75,
                "confidence": 0.8,
                "reasoning": "Test analysis in Dutch",
                "news_relevance": 0.9
            }
        """.trimIndent()
        
        // Both should parse successfully
        val jsonParser = kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
        
        val oldFormatResult = jsonParser.decodeFromString<com.Lyno.matchmindai.domain.model.AiAnalysisResult>(oldFormatJson)
        val newFormatResult = jsonParser.decodeFromString<com.Lyno.matchmindai.domain.model.AiAnalysisResult>(newFormatJson)
        
        // Test that both formats produce valid results
        assertTrue("Old format should produce valid result", oldFormatResult.isValid())
        assertTrue("New format should produce valid result", newFormatResult.isValid())
        
        // Test that computed properties work correctly
        assertEquals("Home attack mod should be 1.2 for old format", 1.2, oldFormatResult.homeAttackMod, 0.01)
        assertEquals("Home attack mod should be 1.2 for new format", 1.2, newFormatResult.homeAttackMod, 0.01)
        assertEquals("Chaos factor should be 0.75 for old format", 0.75, oldFormatResult.chaosFactor, 0.01)
        assertEquals("Chaos factor should be 0.75 for new format", 0.75, newFormatResult.chaosFactor, 0.01)
        assertEquals("Confidence should be 0.8 for old format", 0.8, oldFormatResult.confidence, 0.01)
        assertEquals("Confidence should be 0.8 for new format", 0.8, newFormatResult.confidence, 0.01)
    }

    @Test
    fun testShouldApplyModifiers() {
        // Test modifier application logic
        
        // Test case 1: Valid modifiers with good confidence
        val validModifiers = NewsImpactModifiers(
            homeAttackMod = 1.1,
            homeDefenseMod = 0.95,
            awayAttackMod = 1.05,
            awayDefenseMod = 0.98,
            confidence = 0.6, // Above medium threshold
            reasoning = "Valid analysis",
            chaosFactor = 0.4,
            newsRelevance = 0.7
        )
        
        assertTrue("Valid modifiers should be applicable", 
            newsImpactAnalyzer.shouldApplyModifiers(validModifiers))
        
        // Test case 2: Low confidence modifiers
        val lowConfidenceModifiers = NewsImpactModifiers(
            homeAttackMod = 1.1,
            homeDefenseMod = 0.95,
            awayAttackMod = 1.05,
            awayDefenseMod = 0.98,
            confidence = 0.3, // Below medium threshold
            reasoning = "Low confidence analysis",
            chaosFactor = 0.4,
            newsRelevance = 0.7
        )
        
        assertFalse("Low confidence modifiers should not be applicable", 
            newsImpactAnalyzer.shouldApplyModifiers(lowConfidenceModifiers))
        
        // Test case 3: Extreme modifiers
        val extremeModifiers = NewsImpactModifiers(
            homeAttackMod = 1.4, // Extreme
            homeDefenseMod = 0.6, // Extreme
            awayAttackMod = 1.05,
            awayDefenseMod = 0.98,
            confidence = 0.7,
            reasoning = "Extreme analysis",
            chaosFactor = 0.4,
            newsRelevance = 0.7
        )
        
        assertFalse("Extreme modifiers should not be applicable", 
            newsImpactAnalyzer.shouldApplyModifiers(extremeModifiers))
        
        // Test case 4: No meaningful impact
        val noImpactModifiers = NewsImpactModifiers(
            homeAttackMod = 1.01,
            homeDefenseMod = 0.99,
            awayAttackMod = 1.02,
            awayDefenseMod = 0.98,
            confidence = 0.7,
            reasoning = "No meaningful impact",
            chaosFactor = 0.4,
            newsRelevance = 0.7
        )
        
        assertFalse("Modifiers with no meaningful impact should not be applicable", 
            newsImpactAnalyzer.shouldApplyModifiers(noImpactModifiers))
    }

    @Test
    fun testGenerateImpactSummary() {
        // Test impact summary generation
        
        // Test with meaningful impact
        val meaningfulModifiers = NewsImpactModifiers(
            homeAttackMod = 1.15, // +15%
            homeDefenseMod = 0.9,  // -10%
            awayAttackMod = 1.05,  // +5%
            awayDefenseMod = 0.95, // -5%
            confidence = 0.7,
            reasoning = "Meaningful impact analysis",
            chaosFactor = 0.6,
            newsRelevance = 0.8
        )
        
        val summary = newsImpactAnalyzer.generateImpactSummary(meaningfulModifiers)
        
        assertNotNull("Summary should not be null", summary)
        assertTrue("Summary should contain impact analysis header", 
            summary.contains("NIEUWS IMPACT ANALYSE"))
        assertTrue("Summary should contain home team changes", 
            summary.contains("Thuisploeg:"))
        assertTrue("Summary should contain away team changes", 
            summary.contains("Uitploeg:"))
        assertTrue("Summary should contain confidence percentage", 
            summary.contains("Vertrouwen:"))
        assertTrue("Summary should contain chaos level", 
            summary.contains("Chaos Niveau:"))
        
        // Test with no meaningful impact
        val noImpactModifiers = NewsImpactModifiers(
            homeAttackMod = 1.01,
            homeDefenseMod = 0.99,
            awayAttackMod = 1.02,
            awayDefenseMod = 0.98,
            confidence = 0.7,
            reasoning = "No meaningful impact",
            chaosFactor = 0.4,
            newsRelevance = 0.7
        )
        
        val noImpactSummary = newsImpactAnalyzer.generateImpactSummary(noImpactModifiers)
        assertTrue("No impact summary should indicate no significant impact", 
            noImpactSummary.contains("Geen significante impact van nieuws op teamsterktes"))
    }

    @Test
    fun testModifierBoundsEnforcement() {
        // Test that modifiers are properly bounded
        
        // Create AiAnalysisResult with out-of-bounds values
        val outOfBoundsAiAnalysis = com.Lyno.matchmindai.domain.model.AiAnalysisResult(
            home_attack_modifier = 2.0, // Too high
            home_defense_modifier = 0.3, // Too low
            away_attack_modifier = 1.8, // Too high
            away_defense_modifier = 0.4, // Too low
            chaos_score = 150, // Out of bounds (0-100)
            confidence_score = 120, // Out of bounds (0-100)
            reasoning_short = "Out of bounds test"
        )
        
        val modifiers = newsImpactAnalyzer.javaClass.getDeclaredMethod(
            "convertToNewsImpactModifiers",
            com.Lyno.matchmindai.domain.model.AiAnalysisResult::class.java,
            Boolean::class.java
        ).apply { isAccessible = true }.invoke(
            newsImpactAnalyzer, outOfBoundsAiAnalysis, true
        ) as NewsImpactModifiers
        
        // Test that modifiers are coerced to bounds
        assertTrue("Home attack modifier should be coerced to max 1.5", 
            modifiers.homeAttackMod <= 1.5)
        assertTrue("Home defense modifier should be coerced to min 0.5", 
            modifiers.homeDefenseMod >= 0.5)
        assertTrue("Away attack modifier should be coerced to max 1.5", 
            modifiers.awayAttackMod <= 1.5)
        assertTrue("Away defense modifier should be coerced to min 0.5", 
            modifiers.awayDefenseMod >= 0.5)
        assertTrue("Chaos factor should be coerced to max 1.0", 
            modifiers.chaosFactor <= 1.0)
        assertTrue("Confidence should be coerced to max 1.0", 
            modifiers.confidence <= 1.0)
    }

    private fun createMockMatchDetail(): MatchDetail {
        return MatchDetail(
            fixtureId = 12345,
            homeTeam = "Utrecht",
            awayTeam = "PSV",
            league = "Eredivisie",
            date = "2025-12-22",
            status = MatchStatus.SCHEDULED,
            homeTeamId = 1,
            awayTeamId = 2,
            leagueId = 1,
            score = null,
            stats = emptyList(),
            events = emptyList(),
            venue = null,
            referee = null
        )
    }

    private fun createMockBasePrediction(): MatchPrediction {
        return MatchPrediction(
            fixtureId = 12345,
            homeTeam = "Utrecht",
            awayTeam = "PSV",
            homeWinProbability = 0.45,
            drawProbability = 0.30,
            awayWinProbability = 0.25,
            expectedGoalsHome = 1.8,
            expectedGoalsAway = 1.2,
            analysis = "Base statistical prediction",
            confidenceScore = 65
        )
    }

    private fun createMockAiAnalysisResult(): com.Lyno.matchmindai.domain.model.AiAnalysisResult {
        return com.Lyno.matchmindai.domain.model.AiAnalysisResult(
            home_attack_modifier = 1.15,
            home_defense_modifier = 0.92,
            away_attack_modifier = 1.08,
            away_defense_modifier = 0.96,
            chaos_score = 65,
            confidence_score = 75,
            reasoning_short = "Utrecht heeft een belangrijke speler geblesseerd, PSV is in goede vorm.",
            news_relevant = true
        )
    }
}
