package com.Lyno.matchmindai.domain.service

import com.Lyno.matchmindai.domain.model.MatchDetail
import com.Lyno.matchmindai.domain.model.HybridPrediction
import com.Lyno.matchmindai.domain.model.AiAnalysisResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.mock

/**
 * Unit tests for ScenarioEngine.
 */
class ScenarioEngineTest {
    
    @Test
    fun `test generateScenarios returns scenarios`() = runBlocking {
        // Given
        val mockRepository = mock(com.Lyno.matchmindai.domain.repository.MatchRepository::class.java)
        val engine = ScenarioEngine(mockRepository)
        
        val matchDetail = MatchDetail(
            fixtureId = 12345,
            homeTeam = "Ajax",
            awayTeam = "Feyenoord",
            league = "Eredivisie",
            date = "2024-01-01",
            time = "20:00",
            venue = "Johan Cruijff ArenA",
            referee = "Danny Makkelie",
            status = "NS",
            homeLogo = "",
            awayLogo = "",
            homeGoals = null,
            awayGoals = null,
            elapsed = null
        )
        
        val hybridPrediction = HybridPrediction(
            fixtureId = 12345,
            analysis = AiAnalysisResult(
                primary_scenario_title = "Klassieke Derby",
                primary_scenario_desc = "Een intense derby met veel emotie",
                reasoning_short = "Historische rivaliteit",
                confidence_score = 75,
                chaos_score = 65,
                atmosphere_score = 85,
                tactical_key = "Counter-attack",
                key_player_watch = "Steven Berghuis",
                betting_tip = "Over 2.5 goals",
                betting_confidence = 70
            ),
            breakingNewsUsed = listOf("Ajax zonder sleutelspeler", "Feyenoord in topvorm")
        )
        
        // When
        val result = engine.generateScenarios(matchDetail, hybridPrediction)
        
        // Then
        assertTrue(result.isSuccess)
        val scenarios = result.getOrThrow()
        assertTrue(scenarios.isNotEmpty())
        assertTrue(scenarios.size in 3..5) // Should generate 3-5 scenarios
        
        // Verify scenario properties
        scenarios.forEach { scenario ->
            assertTrue(scenario.id.isNotEmpty())
            assertTrue(scenario.title.isNotEmpty())
            assertTrue(scenario.description.isNotEmpty())
            assertTrue(scenario.predictedScore.isNotEmpty())
            assertTrue(scenario.probability in 5..95)
            assertTrue(scenario.confidence in 30..90)
            assertTrue(scenario.chaosImpact in 0..100)
            assertTrue(scenario.atmosphereImpact in 0..100)
            assertTrue(scenario.bettingValue in 0.0..1.0)
            assertTrue(scenario.keyFactors.isNotEmpty())
            assertTrue(scenario.triggerEvents.isNotEmpty())
            assertTrue(scenario.timeline.isNotEmpty())
            assertTrue(scenario.dataSources.isNotEmpty())
        }
    }
    
    @Test
    fun `test calculateScoreMatrix returns valid matrix`() = runBlocking {
        // Given
        val mockRepository = mock(com.Lyno.matchmindai.domain.repository.MatchRepository::class.java)
        val engine = ScenarioEngine(mockRepository)
        
        val matchDetail = MatchDetail(
            fixtureId = 12345,
            homeTeam = "PSV",
            awayTeam = "AZ",
            league = "Eredivisie",
            date = "2024-01-01",
            time = "18:00",
            venue = "Philips Stadion",
            referee = "Serdar Gözübüyük",
            status = "NS",
            homeLogo = "",
            awayLogo = "",
            homeGoals = null,
            awayGoals = null,
            elapsed = null
        )
        
        // Generate scenarios first
        val scenariosResult = engine.generateScenarios(matchDetail, null)
        assertTrue(scenariosResult.isSuccess)
        val scenarios = scenariosResult.getOrThrow()
        
        // When
        val matrixResult = engine.calculateScoreMatrix(matchDetail, scenarios)
        
        // Then
        assertTrue(matrixResult.isSuccess)
        val matrix = matrixResult.getOrThrow()
        
        assertTrue(matrix.mostLikelyScore.isNotEmpty())
        assertTrue(matrix.mostLikelyProbability in 5..95)
        assertTrue(matrix.homeWinProbability in 0..100)
        assertTrue(matrix.drawProbability in 0..100)
        assertTrue(matrix.awayWinProbability in 0..100)
        assertTrue(matrix.expectedHomeGoals >= 0)
        assertTrue(matrix.expectedAwayGoals >= 0)
        assertTrue(matrix.goalExpectationRange.isNotEmpty())
        
        // Verify probabilities sum to approximately 100
        val totalProbability = matrix.homeWinProbability + matrix.drawProbability + matrix.awayWinProbability
        assertTrue(totalProbability in 80..120) // Allow some rounding error
        
        // Verify score lists
        assertTrue(matrix.homeWinScores.isNotEmpty() || matrix.drawScores.isNotEmpty() || matrix.awayWinScores.isNotEmpty())
    }
    
    @Test
    fun `test scenario templates generate valid data`() {
        // Test that scenario templates work correctly
        val homeTeam = "Ajax"
        val awayTeam = "Feyenoord"
        
        com.Lyno.matchmindai.domain.model.ScenarioCategory.values().forEach { category ->
            val title = com.Lyno.matchmindai.data.templates.ScenarioTemplates.getScenarioTitle(category, homeTeam, awayTeam)
            assertTrue(title.isNotEmpty())
            
            val predictedScore = com.Lyno.matchmindai.data.templates.ScenarioTemplates.getPredictedScore(
                category, homeTeam, awayTeam, 0.7, 0.5
            )
            assertTrue(predictedScore.isNotEmpty())
            assertTrue(predictedScore.contains("-"))
            
            val description = com.Lyno.matchmindai.data.templates.ScenarioTemplates.getScenarioDescription(
                category, homeTeam, awayTeam, predictedScore
            )
            assertTrue(description.isNotEmpty())
            
            val keyFactors = com.Lyno.matchmindai.data.templates.ScenarioTemplates.getKeyFactors(category)
            assertTrue(keyFactors.isNotEmpty())
            
            val triggerEvents = com.Lyno.matchmindai.data.templates.ScenarioTemplates.getTriggerEvents(category)
            assertTrue(triggerEvents.isNotEmpty())
            
            val timelineEvents = com.Lyno.matchmindai.data.templates.ScenarioTemplates.getTimelineEvents(category, homeTeam, awayTeam)
            assertTrue(timelineEvents.isNotEmpty())
            
            val dataSources = com.Lyno.matchmindai.data.templates.ScenarioTemplates.getDataSources(category)
            assertTrue(dataSources.isNotEmpty())
            
            val baseProbability = com.Lyno.matchmindai.data.templates.ScenarioTemplates.getBaseProbability(
                category, 0.7, 0.5, true
            )
            assertTrue(baseProbability in 5..40)
            
            val confidence = com.Lyno.matchmindai.data.templates.ScenarioTemplates.getConfidenceScore(
                category, 0.8, 0.7
            )
            assertTrue(confidence in 30..90)
            
            val (chaosImpact, atmosphereImpact) = com.Lyno.matchmindai.data.templates.ScenarioTemplates.getImpactScores(category)
            assertTrue(chaosImpact in 0..100)
            assertTrue(atmosphereImpact in 0..100)
            
            val bettingValue = com.Lyno.matchmindai.data.templates.ScenarioTemplates.getBettingValue(category, 30)
            assertTrue(bettingValue in 0.0..1.0)
        }
    }
    
    @Test
    fun `test match report generator integrates scenarios`() {
        // Test that match report generator can create reports with scenarios
        val generator = MatchReportGenerator()
        
        val matchDetail = MatchDetail(
            fixtureId = 12345,
            homeTeam = "Ajax",
            awayTeam = "Feyenoord",
            league = "Eredivisie",
            date = "2024-01-01",
            time = "20:00",
            venue = "Johan Cruijff ArenA",
            referee = "Danny Makkelie",
            status = "NS",
            homeLogo = "",
            awayLogo = "",
            homeGoals = null,
            awayGoals = null,
            elapsed = null
        )
        
        val hybridPrediction = HybridPrediction(
            fixtureId = 12345,
            analysis = AiAnalysisResult(
                primary_scenario_title = "Klassieke Derby",
                primary_scenario_desc = "Een intense derby met veel emotie",
                reasoning_short = "Historische rivaliteit",
                confidence_score = 75,
                chaos_score = 65,
                atmosphere_score = 85,
                tactical_key = "Counter-attack",
                key_player_watch = "Steven Berghuis",
                betting_tip = "Over 2.5 goals",
                betting_confidence = 70
            ),
            breakingNewsUsed = listOf("Ajax zonder sleutelspeler", "Feyenoord in topvorm")
        )
        
        // Generate report (scenarios will be empty without scenario engine)
        val report = generator.generateReport(hybridPrediction, matchDetail)
        
        // Verify report structure
        assertEquals(12345, report.fixtureId)
        assertEquals("Ajax", report.homeTeam)
        assertEquals("Feyenoord", report.awayTeam)
        assertEquals("Eredivisie", report.league)
        assertTrue(report.title.isNotEmpty())
        assertTrue(report.introduction.isNotEmpty())
        assertTrue(report.situationAnalysis.isNotEmpty())
        assertTrue(report.keyFactors.isNotEmpty())
        assertTrue(report.conclusion.isNotEmpty())
        assertEquals(75, report.confidence)
        assertEquals("Over 2.5 goals", report.bettingTip)
        assertEquals(70, report.bettingConfidence)
        assertEquals(listOf("Ajax zonder sleutelspeler", "Feyenoord in topvorm"), report.breakingNewsUsed)
        
        // Scenarios should be empty without scenario engine
        assertTrue(report.scenarios.isEmpty())
        assertNull(report.scorePredictions)
    }
}
