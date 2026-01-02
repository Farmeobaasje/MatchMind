package com.Lyno.matchmindai.data.repository

import com.Lyno.matchmindai.data.local.ApiKeyStorage
import com.Lyno.matchmindai.data.local.dao.PredictionDao
import com.Lyno.matchmindai.data.remote.dto.StandingsResponse
import com.Lyno.matchmindai.data.remote.football.FootballApiService
import com.Lyno.matchmindai.domain.model.OracleAnalysis
import com.Lyno.matchmindai.domain.model.SimulationContext
import com.Lyno.matchmindai.domain.model.TesseractResult
import com.Lyno.matchmindai.domain.service.DeepChiService
import com.Lyno.matchmindai.domain.service.NewsImpactAnalyzer
import com.Lyno.matchmindai.domain.tesseract.TesseractEngine
import com.Lyno.matchmindai.domain.usecase.LLMGradeAnalysisUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class OracleRepositoryImplTest {

    private lateinit var oracleRepository: OracleRepositoryImpl
    private lateinit var mockFootballApiService: FootballApiService
    private lateinit var mockTesseractEngine: TesseractEngine
    private lateinit var mockNewsImpactAnalyzer: NewsImpactAnalyzer
    private lateinit var mockDeepChiService: DeepChiService
    private lateinit var mockPredictionDao: PredictionDao
    private lateinit var mockLLMGradeAnalysisUseCase: LLMGradeAnalysisUseCase
    private lateinit var mockApiKeyStorage: ApiKeyStorage

    @Before
    fun setUp() {
        mockFootballApiService = mock()
        mockTesseractEngine = mock()
        mockNewsImpactAnalyzer = mock()
        mockDeepChiService = mock()
        mockPredictionDao = mock()
        mockLLMGradeAnalysisUseCase = mock()
        mockApiKeyStorage = mock()

        oracleRepository = OracleRepositoryImpl(
            footballApiService = mockFootballApiService,
            tesseractEngine = mockTesseractEngine,
            newsImpactAnalyzer = mockNewsImpactAnalyzer,
            deepChiService = mockDeepChiService,
            predictionDao = mockPredictionDao,
            llmGradeAnalysisUseCase = mockLLMGradeAnalysisUseCase,
            apiKeyStorage = mockApiKeyStorage
        )
    }

    @Test
    fun `getOracleAnalysis should return analysis with DeepChi context when API keys are available`() = runTest {
        // Arrange
        val leagueId = 39 // Premier League
        val season = 2024
        val homeTeamId = 40
        val awayTeamId = 41
        val fixtureId = 12345

        // Mock standings response
        val standingsResponse = createMockStandingsResponse()
        whenever(mockFootballApiService.getStandings(eq(leagueId), eq(season)))
            .thenReturn(standingsResponse)

        // Mock API keys
        whenever(mockApiKeyStorage.getApiSportsApiKey()).thenReturn("test-api-sports-key")
        whenever(mockApiKeyStorage.getDeepSeekApiKey()).thenReturn("test-deepseek-key")

        // Mock DeepChiService
        val mockSimulationContext = SimulationContext(
            homeDistraction = 30,
            awayDistraction = 70,
            homeFitness = 85,
            awayFitness = 90,
            fatigueScore = 65,
            lineupStrength = 80,
            styleMatchup = 75,
            reasoning = "Test DeepChi analysis"
        )
        whenever(mockDeepChiService.analyzeMatch(
            fixtureId = eq(fixtureId),
            homeTeamId = eq(homeTeamId),
            awayTeamId = eq(awayTeamId),
            season = eq(season),
            apiSportsApiKey = eq("test-api-sports-key"),
            deepSeekApiKey = eq("test-deepseek-key")
        )).thenReturn(Result.success(mockSimulationContext))

        // Mock TesseractEngine
        val mockTesseractResult = TesseractResult(
            homeWinProbability = 0.45,
            drawProbability = 0.30,
            awayWinProbability = 0.25,
            mostLikelyScore = "2-1"
        )
        whenever(mockTesseractEngine.simulateMatch(
            homePower = any(),
            awayPower = any(),
            context = any()
        )).thenReturn(mockTesseractResult)

        // Mock LLMGradeAnalysisUseCase
        whenever(mockLLMGradeAnalysisUseCase.invoke(
            fixtureId = any(),
            oracleAnalysis = any(),
            tesseractResult = any(),
            forceRefresh = any()
        )).thenReturn(null)

        // Act
        val result = oracleRepository.getOracleAnalysis(
            leagueId = leagueId,
            season = season,
            homeTeamId = homeTeamId,
            awayTeamId = awayTeamId,
            fixtureId = fixtureId
        )

        // Assert
        assertNotNull(result)
        assertEquals("2-1", result.prediction) // Should use Tesseract's most likely score
        assertNotNull(result.tesseract)
        assertEquals(0.45, result.tesseract?.homeWinProbability ?: 0.0, 0.01)
        assertNotNull(result.simulationContext)
        assertEquals(30, result.simulationContext?.homeDistraction)
        assertEquals(70, result.simulationContext?.awayDistraction)
        assertEquals(85, result.simulationContext?.homeFitness)
        assertEquals(90, result.simulationContext?.awayFitness)
    }

    @Test
    fun `getOracleAnalysis should use neutral context when API keys are missing`() = runTest {
        // Arrange
        val leagueId = 39
        val season = 2024
        val homeTeamId = 40
        val awayTeamId = 41
        val fixtureId = 12345

        // Mock standings response
        val standingsResponse = createMockStandingsResponse()
        whenever(mockFootballApiService.getStandings(eq(leagueId), eq(season)))
            .thenReturn(standingsResponse)

        // Mock missing API keys
        whenever(mockApiKeyStorage.getApiSportsApiKey()).thenReturn("")
        whenever(mockApiKeyStorage.getDeepSeekApiKey()).thenReturn("")

        // Mock TesseractEngine
        val mockTesseractResult = TesseractResult(
            homeWinProbability = 0.33,
            drawProbability = 0.34,
            awayWinProbability = 0.33,
            mostLikelyScore = "1-1"
        )
        whenever(mockTesseractEngine.simulateMatch(
            homePower = any(),
            awayPower = any(),
            context = any()
        )).thenReturn(mockTesseractResult)

        // Act
        val result = oracleRepository.getOracleAnalysis(
            leagueId = leagueId,
            season = season,
            homeTeamId = homeTeamId,
            awayTeamId = awayTeamId,
            fixtureId = fixtureId
        )

        // Assert
        assertNotNull(result)
        assertNotNull(result.simulationContext)
        // Should use neutral context when API keys are missing
        assertEquals(50, result.simulationContext?.homeDistraction)
        assertEquals(50, result.simulationContext?.awayDistraction)
        assertEquals(100, result.simulationContext?.homeFitness)
        assertEquals(100, result.simulationContext?.awayFitness)
    }

    @Test
    fun `getOracleAnalysis should handle DeepChiService failure gracefully`() = runTest {
        // Arrange
        val leagueId = 39
        val season = 2024
        val homeTeamId = 40
        val awayTeamId = 41
        val fixtureId = 12345

        // Mock standings response
        val standingsResponse = createMockStandingsResponse()
        whenever(mockFootballApiService.getStandings(eq(leagueId), eq(season)))
            .thenReturn(standingsResponse)

        // Mock API keys
        whenever(mockApiKeyStorage.getApiSportsApiKey()).thenReturn("test-key")
        whenever(mockApiKeyStorage.getDeepSeekApiKey()).thenReturn("test-key")

        // Mock DeepChiService failure
        whenever(mockDeepChiService.analyzeMatch(
            fixtureId = eq(fixtureId),
            homeTeamId = eq(homeTeamId),
            awayTeamId = eq(awayTeamId),
            season = eq(season),
            apiSportsApiKey = eq("test-key"),
            deepSeekApiKey = eq("test-key")
        )).thenReturn(Result.failure(Exception("DeepChi service error")))

        // Mock DeepChiService fallback
        val fallbackContext = SimulationContext.NEUTRAL.copy(
            reasoning = "Fallback analysis"
        )
        whenever(mockDeepChiService.analyzeMatchFallback(
            fixtureId = eq(fixtureId),
            homeTeamId = eq(homeTeamId),
            awayTeamId = eq(awayTeamId),
            season = eq(season),
            apiKey = eq("test-key")
        )).thenReturn(fallbackContext)

        // Mock TesseractEngine
        val mockTesseractResult = TesseractResult(
            homeWinProbability = 0.40,
            drawProbability = 0.35,
            awayWinProbability = 0.25,
            mostLikelyScore = "2-0"
        )
        whenever(mockTesseractEngine.simulateMatch(
            homePower = any(),
            awayPower = any(),
            context = any()
        )).thenReturn(mockTesseractResult)

        // Act
        val result = oracleRepository.getOracleAnalysis(
            leagueId = leagueId,
            season = season,
            homeTeamId = homeTeamId,
            awayTeamId = awayTeamId,
            fixtureId = fixtureId
        )

        // Assert
        assertNotNull(result)
        assertNotNull(result.simulationContext)
        // Should use fallback context when DeepChi fails
        assertEquals("Fallback analysis", result.simulationContext?.reasoning)
    }

    @Test
    fun `getOracleAnalysis should handle missing fixtureId with generated fixtureId`() = runTest {
        // Arrange
        val leagueId = 39
        val season = 2024
        val homeTeamId = 40
        val awayTeamId = 41
        val fixtureId = null // No real fixtureId

        // Mock standings response
        val standingsResponse = createMockStandingsResponse()
        whenever(mockFootballApiService.getStandings(eq(leagueId), eq(season)))
            .thenReturn(standingsResponse)

        // Mock API keys
        whenever(mockApiKeyStorage.getApiSportsApiKey()).thenReturn("test-key")
        whenever(mockApiKeyStorage.getDeepSeekApiKey()).thenReturn("test-key")

        // Mock DeepChiService fallback (should be called when fixtureId is null)
        val fallbackContext = SimulationContext.NEUTRAL.copy(
            reasoning = "Generated fixtureId analysis"
        )
        whenever(mockDeepChiService.analyzeMatchFallback(
            fixtureId = eq(40041), // Generated: homeTeamId * 1000 + awayTeamId
            homeTeamId = eq(homeTeamId),
            awayTeamId = eq(awayTeamId),
            season = eq(season),
            apiKey = eq("test-key")
        )).thenReturn(fallbackContext)

        // Mock TesseractEngine
        val mockTesseractResult = TesseractResult(
            homeWinProbability = 0.35,
            drawProbability = 0.40,
            awayWinProbability = 0.25,
            mostLikelyScore = "1-1"
        )
        whenever(mockTesseractEngine.simulateMatch(
            homePower = any(),
            awayPower = any(),
            context = any()
        )).thenReturn(mockTesseractResult)

        // Act
        val result = oracleRepository.getOracleAnalysis(
            leagueId = leagueId,
            season = season,
            homeTeamId = homeTeamId,
            awayTeamId = awayTeamId,
            fixtureId = fixtureId
        )

        // Assert
        assertNotNull(result)
        assertNotNull(result.simulationContext)
        // Should use fallback with generated fixtureId
        assertEquals("Generated fixtureId analysis", result.simulationContext?.reasoning)
    }

    @Test
    fun `getEnhancedOracleAnalysis should include LLMGRADE enhancement`() = runTest {
        // Arrange
        val leagueId = 39
        val season = 2024
        val homeTeamId = 40
        val awayTeamId = 41
        val fixtureId = 12345

        // Mock base analysis
        val baseAnalysis = OracleAnalysis(
            prediction = "2-1",
            confidence = 0.75,
            homePowerScore = 65.0,
            awayPowerScore = 35.0,
            homeAdvantage = 0.1,
            awayDisadvantage = -0.1,
            tesseract = TesseractResult(
                homeWinProbability = 0.45,
                drawProbability = 0.30,
                awayWinProbability = 0.25,
                mostLikelyScore = "2-1"
            ),
            simulationContext = SimulationContext.NEUTRAL
        )

        // Mock getOracleAnalysis to return base analysis
        val standingsResponse = createMockStandingsResponse()
        whenever(mockFootballApiService.getStandings(eq(leagueId), eq(season)))
            .thenReturn(standingsResponse)
        whenever(mockTesseractEngine.simulateMatch(any(), any(), any()))
            .thenReturn(baseAnalysis.tesseract!!)
        whenever(mockApiKeyStorage.getApiSportsApiKey()).thenReturn("test-key")
        whenever(mockApiKeyStorage.getDeepSeekApiKey()).thenReturn("test-key")
        whenever(mockDeepChiService.analyzeMatch(any(), any(), any(), any(), any(), any()))
            .thenReturn(Result.success(SimulationContext.NEUTRAL))

        // Mock LLMGRADE enhancement
        val mockLLMGradeEnhancement = com.Lyno.matchmindai.domain.model.LLMGradeEnhancement(
            contextFactors = listOf(
                com.Lyno.matchmindai.domain.model.ContextFactor(
                    factor = "Home Team Form",
                    impact = 0.8,
                    reasoning = "Won last 3 matches"
                )
            ),
            outlierScenarios = emptyList(),
            overallContextScore = 0.85,
            riskAssessment = com.Lyno.matchmindai.domain.model.RiskLevel.MEDIUM
        )
        whenever(mockLLMGradeAnalysisUseCase.invoke(
            fixtureId = eq(fixtureId),
            oracleAnalysis = any(),
            tesseractResult = any(),
            forceRefresh = eq(true)
        )).thenReturn(Result.success(mockLLMGradeEnhancement))

        // Act
        val result = oracleRepository.getEnhancedOracleAnalysis(
            leagueId = leagueId,
            season = season,
            homeTeamId = homeTeamId,
            awayTeamId = awayTeamId,
            fixtureId = fixtureId
        )

        // Assert
        assertNotNull(result)
        assertNotNull(result.llmGradeEnhancement)
        assertEquals(0.85, result.llmGradeEnhancement?.overallContextScore ?: 0.0, 0.01)
        assertEquals(1, result.llmGradeEnhancement?.contextFactors?.size ?: 0)
    }

    private fun createMockStandingsResponse(): StandingsResponse {
        // Create a minimal valid standings response for testing
        return StandingsResponse(
            response = listOf(
                com.Lyno.matchmindai.data.remote.dto.StandingsResponse.Response(
                    league = com.Lyno.matchmindai.data.remote.dto.LeagueNode(
                        id = 39,
                        name = "Premier League",
                        country = "England",
                        logo = "logo.png",
                        flag = "flag.png",
                        season = 2024,
                        standings = listOf(
                            listOf(
                                com.Lyno.matchmindai.data.remote.dto.StandingTeamDto(
                                    rank = 1,
                                    team = com.Lyno.matchmindai.data.remote.dto.TeamDto(
                                        id = 40,
                                        name = "Home Team",
                                        logo = "home.png"
                                    ),
                                    points = 70,
                                    goalsDiff = 25,
                                    all = com.Lyno.matchmindai.data.remote.dto.AllStats(
                                        played = 30,
                                        win = 20,
                                        draw = 5,
                                        lose = 5,
                                        goals = com.Lyno.matchmindai.data.remote.dto.GoalsStats(
                                            `for` = 60,
                                            against = 35
                                        )
                                    )
                                ),
                                com.Lyno.matchmindai.data.remote.dto.StandingTeamDto(
                                    rank = 2,
                                    team = com.Lyno.matchmindai.data.remote.dto.TeamDto(
                                        id = 41,
                                        name = "Away Team",
                                        logo = "away.png"
                                    ),
                                    points = 65,
                                    goalsDiff = 20,
                                    all = com.Lyno.matchmindai.data.remote.dto.AllStats(
                                        played = 30,
                                        win = 18,
                                        draw = 8,
                                        lose = 4,
                                        goals = com.Lyno.matchmindai.data.remote.dto.GoalsStats(
                                            `for` = 55,
                                            against = 35
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    }
}
