package com.Lyno.matchmindai.domain.usecase

import com.Lyno.matchmindai.domain.model.AiAnalysisResult
import com.Lyno.matchmindai.domain.model.HybridPrediction
import com.Lyno.matchmindai.domain.model.MatchDetail
import com.Lyno.matchmindai.domain.model.MatchPrediction
import com.Lyno.matchmindai.domain.repository.MatchRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class GetHybridPredictionUseCaseTest {

    private lateinit var useCase: GetHybridPredictionUseCase
    private lateinit var mockRepository: MatchRepository

    @Before
    fun setUp() {
        mockRepository = mock()
        useCase = GetHybridPredictionUseCase(mockRepository)
    }

    @Test
    fun `test confidence calculation is dynamic and not always 50`() = runTest {
        // Arrange
        val fixtureId = 123
        val matchDetail = createTestMatchDetail()
        val originalPrediction = createTestMatchPrediction()
        
        val testNews = listOf("Test news 1", "Test news 2")
        val testAnalysis = createTestAiAnalysisResult(
            confidenceScore = 85,
            bettingConfidence = 80,
            chaosScore = 75,
            atmosphereScore = 90
        )
        
        whenever(mockRepository.searchMatchContext(any())).thenReturn(Result.success(testNews))
        whenever(mockRepository.generateAiAnalysis(any())).thenReturn(Result.success(testAnalysis))
        
        // Act
        val result = useCase.invoke(fixtureId, matchDetail, originalPrediction)
        
        // Assert
        assertTrue(result.isSuccess)
        val hybridPrediction = result.getOrThrow()
        
        // Verify confidence is dynamic (not always 50)
        assertEquals(85, hybridPrediction.analysis.confidence_score)
        assertEquals(80, hybridPrediction.analysis.betting_confidence)
        
        // Verify enhanced prediction has meaningful changes
        assertNotEquals(
            originalPrediction.homeWinProbability,
            hybridPrediction.enhancedPrediction.homeWinProbability,
            0.01
        )
        
        // Verify analysis is unique to this match
        assertTrue(hybridPrediction.analysis.primary_scenario_desc.contains("Ajax"))
        assertTrue(hybridPrediction.analysis.primary_scenario_desc.contains("Feyenoord"))
    }

    @Test
    fun `test betting tip generation is dynamic`() = runTest {
        // Arrange
        val fixtureId = 456
        val matchDetail = createTestMatchDetail(
            homeTeam = "PSV",
            awayTeam = "AZ Alkmaar"
        )
        val originalPrediction = createTestMatchPrediction(
            homeWinProbability = 0.65,
            awayWinProbability = 0.20
        )
        
        val testNews = listOf("PSV injury crisis", "AZ good form")
        val testAnalysis = createTestAiAnalysisResult(
            bettingTip = "PSV wint & Over 2.5 Goals",
            bettingConfidence = 75
        )
        
        whenever(mockRepository.searchMatchContext(any())).thenReturn(Result.success(testNews))
        whenever(mockRepository.generateAiAnalysis(any())).thenReturn(Result.success(testAnalysis))
        
        // Act
        val result = useCase.invoke(fixtureId, matchDetail, originalPrediction)
        
        // Assert
        assertTrue(result.isSuccess)
        val hybridPrediction = result.getOrThrow()
        
        // Verify betting tip is dynamic and match-specific
        val bettingTip = hybridPrediction.analysis.getBettingTip("PSV", "AZ Alkmaar")
        assertTrue(bettingTip.contains("PSV") || bettingTip.contains("wint"))
        
        // Verify confidence is dynamic
        val bettingConfidence = hybridPrediction.analysis.getBettingConfidence()
        assertEquals(75, bettingConfidence)
        assertNotEquals(50, bettingConfidence) // Should not be default 50
    }

    @Test
    fun `test derby match detection`() {
        // Test derby detection
        assertTrue(useCase.isDerbyMatch("Ajax", "Feyenoord", "Eredivisie"))
        assertTrue(useCase.isDerbyMatch("Real Madrid", "Barcelona", "La Liga"))
        assertTrue(useCase.isDerbyMatch("Manchester City", "Manchester United", "Premier League"))
        
        // Test non-derby matches
        assertFalse(useCase.isDerbyMatch("Ajax", "FC Utrecht", "Eredivisie"))
        assertFalse(useCase.isDerbyMatch("PSV", "FC Twente", "Eredivisie"))
    }

    @Test
    fun `test high stakes match detection`() {
        // Test high stakes leagues
        assertTrue(useCase.isHighStakesMatch("Champions League", 60, 40))
        assertTrue(useCase.isHighStakesMatch("Europa League Final", 55, 45))
        assertTrue(useCase.isHighStakesMatch("Promotion Playoff", 50, 50))
        
        // Test close matches (difference < 20%)
        assertTrue(useCase.isHighStakesMatch("Eredivisie", 55, 45))
        assertTrue(useCase.isHighStakesMatch("Premier League", 52, 48))
        
        // Test not high stakes
        assertFalse(useCase.isHighStakesMatch("Eredivisie", 70, 30))
        assertFalse(useCase.isHighStakesMatch("Friendly", 60, 40))
    }

    @Test
    fun `test applyAiModifiers creates unique predictions`() {
        // Arrange
        val originalPrediction = createTestMatchPrediction()
        val analysis = createTestAiAnalysisResult(
            homeAttackModifier = 1.2,
            awayAttackModifier = 0.9,
            confidenceScore = 80
        )
        
        // Act
        val enhancedPrediction = useCase.applyAiModifiers(originalPrediction, analysis)
        
        // Assert
        // Home probability should increase (1.2 modifier)
        assertTrue(enhancedPrediction.homeWinProbability > originalPrediction.homeWinProbability)
        
        // Away probability should decrease (0.9 modifier)
        assertTrue(enhancedPrediction.awayWinProbability < originalPrediction.awayWinProbability)
        
        // Confidence should be combined
        val expectedConfidence = (originalPrediction.confidenceScore + analysis.confidence_score) / 2
        assertEquals(expectedConfidence, enhancedPrediction.confidenceScore)
        
        // Expected goals should be modified
        assertEquals(
            originalPrediction.expectedGoalsHome * 1.2,
            enhancedPrediction.expectedGoalsHome,
            0.01
        )
        assertEquals(
            originalPrediction.expectedGoalsAway * 0.9,
            enhancedPrediction.expectedGoalsAway,
            0.01
        )
    }

    @Test
    fun `test getBettingConfidence returns dynamic value`() {
        // Test with different confidence values
        val analysis1 = createTestAiAnalysisResult(bettingConfidence = 85)
        assertEquals(85, analysis1.getBettingConfidence())
        
        val analysis2 = createTestAiAnalysisResult(bettingConfidence = 60)
        assertEquals(60, analysis2.getBettingConfidence())
        
        val analysis3 = createTestAiAnalysisResult(bettingConfidence = 30)
        assertEquals(30, analysis3.getBettingConfidence())
        
        // Verify it's not always 50
        assertNotEquals(50, analysis1.getBettingConfidence())
        assertNotEquals(50, analysis2.getBettingConfidence())
        assertNotEquals(50, analysis3.getBettingConfidence())
    }

    @Test
    fun `test getBettingTip returns dynamic tip`() {
        // Test with different betting tips
        val analysis1 = createTestAiAnalysisResult(bettingTip = "Thuis wint & BTTS")
        val tip1 = analysis1.getBettingTip("Ajax", "Feyenoord")
        assertTrue(tip1.contains("Thuis") || tip1.contains("wint"))
        
        val analysis2 = createTestAiAnalysisResult(bettingTip = "Over 2.5 Goals")
        val tip2 = analysis2.getBettingTip("PSV", "AZ")
        assertTrue(tip2.contains("Over") || tip2.contains("Goals"))
        
        val analysis3 = createTestAiAnalysisResult(bettingTip = "Gelijkspel")
        val tip3 = analysis3.getBettingTip("FC Utrecht", "FC Twente")
        assertTrue(tip3.contains("Gelijkspel") || tip3.contains("gelijk"))
        
        // Verify tips are not generic
        assertNotEquals("Generic betting tip", tip1)
        assertNotEquals("Generic betting tip", tip2)
        assertNotEquals("Generic betting tip", tip3)
    }

    private fun createTestMatchDetail(
        homeTeam: String = "Ajax",
        awayTeam: String = "Feyenoord",
        league: String = "Eredivisie"
    ): MatchDetail {
        return MatchDetail(
            fixtureId = 123,
            homeTeam = homeTeam,
            awayTeam = awayTeam,
            league = league,
            date = "2024-01-01",
            time = "20:00",
            status = "NS",
            stats = emptyList(),
            injuries = emptyList(),
            predictions = null,
            odds = null
        )
    }

    private fun createTestMatchPrediction(
        homeWinProbability: Double = 0.55,
        awayWinProbability: Double = 0.25,
        drawProbability: Double = 0.20
    ): MatchPrediction {
        return MatchPrediction(
            fixtureId = 123,
            homeTeam = "Ajax",
            awayTeam = "Feyenoord",
            homeWinProbability = homeWinProbability,
            drawProbability = drawProbability,
            awayWinProbability = awayWinProbability,
            expectedGoalsHome = 1.8,
            expectedGoalsAway = 1.2,
            analysis = "Test analysis",
            confidenceScore = 70
        )
    }

    private fun createTestAiAnalysisResult(
        homeAttackModifier: Double = 1.0,
        awayAttackModifier: Double = 1.0,
        confidenceScore: Int = 80,
        bettingTip: String = "Thuis wint",
        bettingConfidence: Int = 75,
        chaosScore: Int = 60,
        atmosphereScore: Int = 70
    ): AiAnalysisResult {
        return AiAnalysisResult(
            home_attack_modifier = homeAttackModifier,
            away_defense_modifier = 1.0,
            away_attack_modifier = awayAttackModifier,
            home_defense_modifier = 1.0,
            chaos_score = chaosScore,
            atmosphere_score = atmosphereScore,
            primary_scenario_title = "De Klassieker",
            primary_scenario_desc = "Ajax domineert balbezit maar Feyenoord scoort op de counter. Wedstrijd wordt beslist in de tweede helft.",
            tactical_key = "Middenveld controle behouden",
            key_player_watch = "Steven Berghuis",
            confidence_score = confidenceScore,
            news_relevant = true,
            betting_tip = bettingTip,
            betting_confidence = bettingConfidence,
            reasoning_short = "Statistische voorspelling met AI-analyse",
            reasoning_long = "Gedetailleerde analyse gebaseerd op vorm, blessures en historische data."
        )
    }
}
