    package com.Lyno.matchmindai.data.repository

import com.Lyno.matchmindai.domain.model.ContextFactor
import com.Lyno.matchmindai.domain.model.ContextFactorType
import com.Lyno.matchmindai.domain.model.OracleAnalysis
import com.Lyno.matchmindai.domain.model.TesseractResult
import com.Lyno.matchmindai.domain.prediction.ContextAdjustedOracle
import com.Lyno.matchmindai.domain.prediction.AdjustedPrediction
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class OracleRepositoryContextAdjustedTest {

    @Test
    fun testContextAdjustedOracle_NoContextFactors() = runTest {
        // Arrange
        val oracle = ContextAdjustedOracle()
        val baseOracle = OracleAnalysis(
            prediction = "3-0",
            confidence = 85,
            reasoning = "Strong home team",
            homePowerScore = 85,
            awayPowerScore = 45
        )
        
        // Act
        val result = oracle.calculateAdjustedPrediction(
            baseOracle = baseOracle,
            contextFactors = emptyList(),
            tesseractResult = null
        )
        
        // Assert
        assertEquals("3-0", result.score)
        assertEquals(85, result.confidence)
        assertTrue(result.reasoning.contains("Strong home team"))
    }

    @Test
    fun testContextAdjustedOracle_WithInjuries() = runTest {
        // Arrange
        val oracle = ContextAdjustedOracle()
        val baseOracle = OracleAnalysis(
            prediction = "3-0",
            confidence = 85,
            reasoning = "Strong home team",
            homePowerScore = 85,
            awayPowerScore = 45
        )
        
        val injuryFactor = ContextFactor(
            type = ContextFactorType.INJURIES,
            score = 8, // High impact
            description = "Home team missing key players",
            weight = 1.5
        )
        
        // Act
        val result = oracle.calculateAdjustedPrediction(
            baseOracle = baseOracle,
            contextFactors = listOf(injuryFactor),
            tesseractResult = null
        )
        
        // Assert
        // With injuries, score should be adjusted down from 3-0
        assertNotEquals("3-0", result.score)
        assertTrue(result.confidence < 85) // Confidence should be reduced
        assertTrue(result.reasoning.contains("injury factor"))
    }

    @Test
    fun testContextAdjustedOracle_WithTesseractAlignment() = runTest {
        // Arrange
        val oracle = ContextAdjustedOracle()
        val baseOracle = OracleAnalysis(
            prediction = "3-0",
            confidence = 85,
            reasoning = "Strong home team",
            homePowerScore = 85,
            awayPowerScore = 45
        )
        
        val tesseractResult = TesseractResult(
            homeWinProbability = 0.4,
            drawProbability = 0.3,
            awayWinProbability = 0.3,
            mostLikelyScore = "2-1"
        )
        
        // Act
        val result = oracle.calculateAdjustedPrediction(
            baseOracle = baseOracle,
            contextFactors = emptyList(),
            tesseractResult = tesseractResult
        )
        
        // Assert
        // Tesseract suggests 2-1, so confidence should be adjusted
        assertTrue(result.confidence < 85) // Lower confidence due to misalignment
        assertTrue(result.reasoning.contains("Tesseract simulation"))
    }

    @Test
    fun testContextAdjustedOracle_QuickFixFor3_0Bias() = runTest {
        // Arrange
        val oracle = ContextAdjustedOracle()
        
        // Test case 1: High power diff with many injuries -> should adjust from 3-0 to 2-0
        val result1 = oracle.adjustOracleScoreQuickFix(
            baseScore = "3-0",
            powerDiff = 55,
            totalInjuries = 9,
            homeForm = "average"
        )
        assertEquals("2-0", result1)
        
        // Test case 2: High power diff with poor home form -> should adjust from 3-0 to 2-1
        val result2 = oracle.adjustOracleScoreQuickFix(
            baseScore = "3-0",
            powerDiff = 55,
            totalInjuries = 2,
            homeForm = "poor"
        )
        assertEquals("2-1", result2)
        
        // Test case 3: Moderate power diff with injuries -> should adjust from 3-0 to 1-0
        val result3 = oracle.adjustOracleScoreQuickFix(
            baseScore = "3-0",
            powerDiff = 35,
            totalInjuries = 5,
            homeForm = "average"
        )
        assertEquals("1-0", result3)
        
        // Test case 4: No adjustment needed
        val result4 = oracle.adjustOracleScoreQuickFix(
            baseScore = "2-1",
            powerDiff = 20,
            totalInjuries = 1,
            homeForm = "good"
        )
        assertEquals("2-1", result4)
    }

    @Test
    fun testContextAdjustedOracle_FormCorrection() = runTest {
        // Arrange
        val oracle = ContextAdjustedOracle()
        val baseOracle = OracleAnalysis(
            prediction = "2-0",
            confidence = 75,
            reasoning = "Home team favored",
            homePowerScore = 70,
            awayPowerScore = 50
        )
        
        val excellentFormFactor = ContextFactor(
            type = ContextFactorType.TEAM_MORALE,
            score = 9, // Excellent form
            description = "Home team in excellent form",
            weight = 1.2
        )
        
        val poorFormFactor = ContextFactor(
            type = ContextFactorType.TEAM_MORALE,
            score = 3, // Poor form
            description = "Away team in poor form",
            weight = 1.2
        )
        
        // Act
        val resultWithExcellentForm = oracle.calculateAdjustedPrediction(
            baseOracle = baseOracle,
            contextFactors = listOf(excellentFormFactor),
            tesseractResult = null
        )
        
        val resultWithPoorForm = oracle.calculateAdjustedPrediction(
            baseOracle = baseOracle,
            contextFactors = listOf(poorFormFactor),
            tesseractResult = null
        )
        
        // Assert
        // Excellent form should boost confidence
        assertTrue(resultWithExcellentForm.confidence >= 75)
        
        // Poor form should reduce confidence
        assertTrue(resultWithPoorForm.confidence <= 75)
    }

    @Test
    fun testContextAdjustedOracle_MultipleContextFactors() = runTest {
        // Arrange
        val oracle = ContextAdjustedOracle()
        val baseOracle = OracleAnalysis(
            prediction = "3-0",
            confidence = 80,
            reasoning = "Strong prediction",
            homePowerScore = 80,
            awayPowerScore = 40
        )
        
        val contextFactors = listOf(
            ContextFactor(
                type = ContextFactorType.INJURIES,
                score = 7,
                description = "Home team injuries",
                weight = 1.5
            ),
            ContextFactor(
                type = ContextFactorType.WEATHER,
                score = 5,
                description = "Poor weather conditions",
                weight = 0.8
            ),
            ContextFactor(
                type = ContextFactorType.TACTICAL_CHANGES,
                score = 6,
                description = "Tactical changes",
                weight = 1.3
            )
        )
        
        // Act
        val result = oracle.calculateAdjustedPrediction(
            baseOracle = baseOracle,
            contextFactors = contextFactors,
            tesseractResult = null
        )
        
        // Assert
        // Multiple context factors should significantly adjust the prediction
        assertNotEquals("3-0", result.score)
        assertTrue(result.confidence < 80)
        assertTrue(result.reasoning.contains("3 injury factor(s)"))
    }

    @Test
    fun testContextAdjustedOracle_ScoreParsing() = runTest {
        // Arrange
        val oracle = ContextAdjustedOracle()
        
        // Test valid scores
        val validScores = listOf("3-0", "2-1", "0-0", "4-2", "1-3")
        
        validScores.forEach { score ->
            val baseOracle = OracleAnalysis(
                prediction = score,
                confidence = 70,
                reasoning = "Test",
                homePowerScore = 60,
                awayPowerScore = 60
            )
            
            // Act
            val result = oracle.calculateAdjustedPrediction(
                baseOracle = baseOracle,
                contextFactors = emptyList(),
                tesseractResult = null
            )
            
            // Assert
            assertTrue(result.score.matches(Regex("^\\d+-\\d+$")))
            assertTrue(result.score.split("-")[0].toInt() <= 5)
            assertTrue(result.score.split("-")[1].toInt() <= 5)
        }
    }
}
