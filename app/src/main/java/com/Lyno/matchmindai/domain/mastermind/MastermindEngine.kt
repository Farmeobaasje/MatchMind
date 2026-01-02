package com.Lyno.matchmindai.domain.mastermind

import com.Lyno.matchmindai.domain.model.MastermindSignal
import com.Lyno.matchmindai.domain.model.OracleAnalysis
import com.Lyno.matchmindai.domain.model.TesseractResult
import com.Lyno.matchmindai.domain.model.ScenarioType
import com.Lyno.matchmindai.domain.model.SignalColor
import com.Lyno.matchmindai.domain.model.LLMGradeEnhancement
import com.Lyno.matchmindai.domain.model.ContextFactor
import com.Lyno.matchmindai.domain.model.OutlierScenario
import com.Lyno.matchmindai.domain.model.RiskLevel

/**
 * Mastermind Engine - The "Brain" that makes the final decision.
 * Analyzes Oracle, Tesseract, and contextual data to produce a "Golden Tip".
 * 
 * Decision Tree Logic:
 * 1. Check if Oracle and Tesseract agree (Banker scenario)
 * 2. Check if they disagree (High Risk scenario)
 * 3. Check for high goal probability (Goals Festival)
 * 4. Check for close match (Tactical Duel)
 * 5. Default to Value Bet analysis
 * 
 * Enhanced with LLMGRADE: Incorporates context factors and outlier scenarios
 * from unstructured data analysis.
 */
class MastermindEngine {
    
    /**
     * Analyze Oracle and Tesseract data to produce a Mastermind Signal.
     * 
     * @param oracleAnalysis The Oracle analysis containing prediction and confidence
     * @param tesseractResult Optional Tesseract simulation results
     * @return MastermindSignal with the "Golden Tip" decision
     */
    fun analyze(
        oracleAnalysis: OracleAnalysis,
        tesseractResult: TesseractResult?
    ): MastermindSignal {
        // If no Tesseract data, use Oracle-only analysis
        if (tesseractResult == null) {
            return analyzeOracleOnly(oracleAnalysis)
        }
        
        // Decision Tree
        return when {
            // Scenario 1: BANKER - Oracle and Tesseract align with high confidence
            isBankerScenario(oracleAnalysis, tesseractResult) -> {
                createBankerSignal(oracleAnalysis, tesseractResult)
            }
            
            // Scenario 2: HIGH RISK - Oracle and Tesseract disagree
            isHighRiskScenario(oracleAnalysis, tesseractResult) -> {
                createHighRiskSignal(oracleAnalysis, tesseractResult)
            }
            
            // Scenario 3: GOALS FESTIVAL - High probability of goals
            isGoalsFestivalScenario(tesseractResult) -> {
                createGoalsFestivalSignal(tesseractResult)
            }
            
            // Scenario 4: TACTICAL DUEL - Close match, small power difference
            isTacticalDuelScenario(oracleAnalysis) -> {
                createTacticalDuelSignal(oracleAnalysis)
            }
            
            // Scenario 5: DEFENSIVE BATTLE - Low scoring match
            isDefensiveBattleScenario(tesseractResult) -> {
                createDefensiveBattleSignal(tesseractResult)
            }
            
            // Default: VALUE BET - Good odds value
            else -> {
                createValueBetSignal(oracleAnalysis, tesseractResult)
            }
        }
    }
    
    /**
     * Analyze with LLMGRADE enhancement - Advanced analysis incorporating
     * context factors and outlier scenarios from unstructured data.
     * 
     * @param oracleAnalysis The Oracle analysis
     * @param tesseractResult Optional Tesseract simulation results
     * @param llmGradeEnhancement Optional LLMGRADE enhancement with context factors
     * @return Enhanced MastermindSignal with LLM insights
     */
    fun analyzeWithLLMGrade(
        oracleAnalysis: OracleAnalysis,
        tesseractResult: TesseractResult?,
        llmGradeEnhancement: LLMGradeEnhancement?
    ): MastermindSignal {
        // Get base signal using traditional analysis
        val baseSignal = if (tesseractResult != null) {
            analyze(oracleAnalysis, tesseractResult)
        } else {
            analyzeOracleOnly(oracleAnalysis)
        }
        
        // Apply LLMGRADE enhancements if available
        return if (llmGradeEnhancement != null) {
            applyLLMGradeEnhancement(baseSignal, llmGradeEnhancement, oracleAnalysis, tesseractResult)
        } else {
            baseSignal
        }
    }
    
    /**
     * Check if this is a "Banker" (Zekerheidje) scenario.
     * Conditions:
     * - Oracle confidence > 70%
     * - Oracle and Tesseract agree on winner (or both indicate draw)
     * - No high fatigue or weak lineup (Trinity metrics)
     */
    private fun isBankerScenario(
        oracleAnalysis: OracleAnalysis,
        tesseractResult: TesseractResult
    ): Boolean {
        val oracleConfidenceHigh = oracleAnalysis.confidence >= 70
        
        // Check if Oracle and Tesseract agree on outcome
        val oracleHomeWins = oracleAnalysis.prediction.split("-")[0].toInt() > oracleAnalysis.prediction.split("-")[1].toInt()
        val oracleAwayWins = oracleAnalysis.prediction.split("-")[0].toInt() < oracleAnalysis.prediction.split("-")[1].toInt()
        val oracleDraw = !oracleHomeWins && !oracleAwayWins
        
        val tesseractHomeFavorite = tesseractResult.isHomeFavorite
        val tesseractAwayFavorite = tesseractResult.isAwayFavorite
        val tesseractDrawFavorite = tesseractResult.isDrawFavorite
        
        val outcomesAgree = (oracleHomeWins && tesseractHomeFavorite) ||
                           (oracleAwayWins && tesseractAwayFavorite) ||
                           (oracleDraw && tesseractDrawFavorite)
        
        // Check Trinity metrics if available in simulation context
        val trinityMetricsGood = oracleAnalysis.simulationContext?.let { context ->
            // No high fatigue (> 80) and no weak lineup (< 70)
            context.fatigueScore <= 80 && context.lineupStrength >= 70
        } ?: true // Assume good if not available
        
        return oracleConfidenceHigh && outcomesAgree && trinityMetricsGood
    }
    
    /**
     * Check if this is a "High Risk" scenario.
     * Conditions:
     * - Oracle and Tesseract disagree on winner
     * - Or Oracle confidence < 50%
     * - Or Trinity metrics indicate high risk (weak lineup or high fatigue)
     */
    private fun isHighRiskScenario(
        oracleAnalysis: OracleAnalysis,
        tesseractResult: TesseractResult
    ): Boolean {
        val oracleHomeWins = oracleAnalysis.prediction.split("-")[0].toInt() > oracleAnalysis.prediction.split("-")[1].toInt()
        val oracleAwayWins = oracleAnalysis.prediction.split("-")[0].toInt() < oracleAnalysis.prediction.split("-")[1].toInt()
        
        val tesseractHomeFavorite = tesseractResult.isHomeFavorite
        val tesseractAwayFavorite = tesseractResult.isAwayFavorite
        
        val outcomesDisagree = (oracleHomeWins && tesseractAwayFavorite) ||
                              (oracleAwayWins && tesseractHomeFavorite)
        
        val lowConfidence = oracleAnalysis.confidence < 50
        
        // Check Trinity metrics for high risk conditions
        val trinityHighRisk = oracleAnalysis.simulationContext?.let { context ->
            // Phase 4 Rule 1: Weak lineup (< 70) AND Oracle predicts Win
            val weakLineupAndWin = context.lineupStrength < 70 && (oracleHomeWins || oracleAwayWins)
            
            // Phase 4 Rule 2: High fatigue (> 80) AND Oracle predicts Win
            val highFatigueAndWin = context.fatigueScore > 80 && (oracleHomeWins || oracleAwayWins)
            
            weakLineupAndWin || highFatigueAndWin
        } ?: false
        
        return outcomesDisagree || lowConfidence || trinityHighRisk
    }
    
    /**
     * Check if this is a "Goals Festival" scenario.
     * Conditions:
     * - Over 2.5 probability > 65%
     * - BTTS probability > 60%
     */
    private fun isGoalsFestivalScenario(tesseractResult: TesseractResult): Boolean {
        return tesseractResult.over2_5Probability > 0.65 &&
               tesseractResult.bttsProbability > 0.60
    }
    
    /**
     * Check if this is a "Tactical Duel" scenario.
     * Conditions:
     * - Power difference < 20 points
     * - Oracle confidence 50-70% (medium)
     */
    private fun isTacticalDuelScenario(oracleAnalysis: OracleAnalysis): Boolean {
        return oracleAnalysis.powerDelta in -20..20 &&
               oracleAnalysis.confidence in 50..70
    }
    
    /**
     * Check if this is a "Defensive Battle" scenario.
     * Conditions:
     * - Under 2.5 probability > 70%
     * - BTTS probability < 40%
     */
    private fun isDefensiveBattleScenario(tesseractResult: TesseractResult): Boolean {
        return tesseractResult.under2_5Probability > 0.70 &&
               tesseractResult.bttsProbability < 0.40
    }
    
    /**
     * Create a Banker (Zekerheidje) signal.
     */
    private fun createBankerSignal(
        oracleAnalysis: OracleAnalysis,
        tesseractResult: TesseractResult
    ): MastermindSignal {
        val homeTeamWins = oracleAnalysis.prediction.split("-")[0].toInt() > oracleAnalysis.prediction.split("-")[1].toInt()
        val awayTeamWins = oracleAnalysis.prediction.split("-")[0].toInt() < oracleAnalysis.prediction.split("-")[1].toInt()
        
        val recommendation = when {
            homeTeamWins -> "Thuis Winst"
            awayTeamWins -> "Uit Winst"
            else -> "Gelijk"
        }
        
        val confidence = (oracleAnalysis.confidence + tesseractResult.homeWinPercentage) / 2
        
        // Add Trinity metrics context to description
        val trinityContext = oracleAnalysis.simulationContext?.let { context ->
            buildString {
                if (context.hasMeaningfulData()) {
                    appendLine()
                    appendLine("🎯 TRINITY METRICS:")
                    appendLine("• Vermoeidheid: ${context.fatigueScore}/100 (${if (context.hasHighFatigue) "⚠️ Hoog" else "OK"})")
                    appendLine("• Opstelling sterkte: ${context.lineupStrength}/100 (${if (context.hasWeakLineup) "⚠️ Zwak" else "Sterk"})")
                    appendLine("• Stijl match-up: ${String.format("%.2f", context.styleMatchup)} (${if (context.hasStyleAdvantage) "Voordeel" else if (context.hasStyleDisadvantage) "Nadeel" else "Neutraal"})")
                }
            }
        } ?: ""
        
        return MastermindSignal(
            title = "ZEKERHEIDJE",
            description = "Data en Trinity metrics wijzen unaniem naar ${if (homeTeamWins) "thuis" else "uit"}winst. " +
                         "Oracle (${oracleAnalysis.confidence}%) en Tesseract simulaties (${tesseractResult.homeWinPercentage}%) bevestigen dit patroon.$trinityContext",
            color = SignalColor.GREEN,
            confidence = confidence,
            recommendation = recommendation,
            scenarioType = ScenarioType.BANKER
        )
    }
    
    /**
     * Create a High Risk signal.
     */
    private fun createHighRiskSignal(
        oracleAnalysis: OracleAnalysis,
        tesseractResult: TesseractResult
    ): MastermindSignal {
        val oracleScore = oracleAnalysis.prediction
        val tesseractScore = tesseractResult.mostLikelyScore
        
        // Check Trinity metrics for specific high risk reasons
        val trinityRiskReason = oracleAnalysis.simulationContext?.let { context ->
            buildString {
                if (context.lineupStrength < 70 && (oracleAnalysis.prediction.split("-")[0].toInt() > oracleAnalysis.prediction.split("-")[1].toInt() || 
                    oracleAnalysis.prediction.split("-")[0].toInt() < oracleAnalysis.prediction.split("-")[1].toInt())) {
                    appendLine("⚠️  Zwakke opstelling (${context.lineupStrength}/100) ondermijnt winstkans.")
                }
                if (context.fatigueScore > 80 && (oracleAnalysis.prediction.split("-")[0].toInt() > oracleAnalysis.prediction.split("-")[1].toInt() || 
                    oracleAnalysis.prediction.split("-")[0].toInt() < oracleAnalysis.prediction.split("-")[1].toInt())) {
                    appendLine("⚠️  Hoge vermoeidheid (${context.fatigueScore}/100) verhoogt risico.")
                }
                if (context.hasStyleDisadvantage) {
                    appendLine("⚠️  Stijl nadeel (${String.format("%.2f", context.styleMatchup)}) maakt voorspelling onzekerder.")
                }
            }
        } ?: ""
        
        val baseDescription = "Oracle voorspelt $oracleScore, maar Tesseract simulaties wijzen op $tesseractScore. " +
                             "Kleine marge (${oracleAnalysis.powerDelta} punten verschil), grote onzekerheid. Extra voorzichtigheid is geboden."
        
        val fullDescription = if (trinityRiskReason.isNotBlank()) {
            "$baseDescription\n\n🎯 TRINITY RISK FACTORS:\n$trinityRiskReason"
        } else {
            baseDescription
        }
        
        return MastermindSignal(
            title = "HOOG RISICO",
            description = fullDescription,
            color = SignalColor.YELLOW,
            confidence = 60,
            recommendation = "Voorzichtige inzet of vermijden",
            scenarioType = ScenarioType.HIGH_RISK
        )
    }
    
    /**
     * Create a Goals Festival signal.
     */
    private fun createGoalsFestivalSignal(tesseractResult: TesseractResult): MastermindSignal {
        return MastermindSignal(
            title = "DOELPUNTEN FESTIJN",
            description = "Over 2.5 goals kans: ${tesseractResult.over2_5Percentage}%. " +
                         "Beide teams scoren kans: ${tesseractResult.bttsPercentage}%. " +
                         "Verwacht een offensieve wedstrijd met veel doelpunten.",
            color = SignalColor.GREEN,
            confidence = 75,
            recommendation = "Over 2.5 Goals & BTTS Yes",
            scenarioType = ScenarioType.GOALS_FESTIVAL
        )
    }
    
    /**
     * Create a Tactical Duel signal.
     */
    private fun createTacticalDuelSignal(oracleAnalysis: OracleAnalysis): MastermindSignal {
        return MastermindSignal(
            title = "TACTISCH DUEL",
            description = "Krachtverschil slechts ${oracleAnalysis.powerDelta} punten. " +
                         "Verwacht een tactische strijd waar kleine details (set pieces, individuele klasse) het verschil maken.",
            color = SignalColor.YELLOW,
            confidence = 65,
            recommendation = "Gelijk of kleine marge (±1 goal)",
            scenarioType = ScenarioType.TACTICAL_DUEL
        )
    }
    
    /**
     * Create a Defensive Battle signal.
     */
    private fun createDefensiveBattleSignal(tesseractResult: TesseractResult): MastermindSignal {
        return MastermindSignal(
            title = "DEFENSIEVE STRIJD",
            description = "Under 2.5 kans: ${tesseractResult.under2_5Percentage}%. " +
                         "Beide teams scoren kans slechts ${tesseractResult.bttsPercentage}%. " +
                         "Verwacht een defensieve, mogelijk saaie wedstrijd.",
            color = SignalColor.YELLOW,
            confidence = 70,
            recommendation = "Under 2.5 Goals",
            scenarioType = ScenarioType.DEFENSIVE_BATTLE
        )
    }
    
    /**
     * Create a Value Bet signal (default scenario).
     */
    private fun createValueBetSignal(
        oracleAnalysis: OracleAnalysis,
        tesseractResult: TesseractResult
    ): MastermindSignal {
        // Determine which outcome has the best value (highest probability vs market expectation)
        val bestValueOutcome = when {
            tesseractResult.homeWinProbability > 0.5 -> "Thuis Winst"
            tesseractResult.awayWinProbability > 0.5 -> "Uit Winst"
            else -> "Gelijk"
        }
        
        return MastermindSignal(
            title = "VALUE BET",
            description = "Statistische kans (${(tesseractResult.homeWinProbability * 100).toInt()}% H, " +
                         "${(tesseractResult.drawProbability * 100).toInt()}% D, " +
                         "${(tesseractResult.awayWinProbability * 100).toInt()}% A) biedt mogelijk value.",
            color = SignalColor.GREEN,
            confidence = oracleAnalysis.confidence,
            recommendation = bestValueOutcome,
            scenarioType = ScenarioType.VALUE_BET
        )
    }
    
    /**
     * Analyze Oracle data only (when Tesseract is not available).
     */
    private fun analyzeOracleOnly(oracleAnalysis: OracleAnalysis): MastermindSignal {
        return when {
            oracleAnalysis.confidence >= 75 -> {
                val homeTeamWins = oracleAnalysis.prediction.split("-")[0].toInt() > oracleAnalysis.prediction.split("-")[1].toInt()
                val awayTeamWins = oracleAnalysis.prediction.split("-")[0].toInt() < oracleAnalysis.prediction.split("-")[1].toInt()
                
                MastermindSignal(
                    title = "ORACLE ZEKERHEID",
                    description = "Oracle voorspelt met ${oracleAnalysis.confidence}% zekerheid een ${if (homeTeamWins) "thuis" else "uit"}winst.",
                    color = SignalColor.GREEN,
                    confidence = oracleAnalysis.confidence,
                    recommendation = if (homeTeamWins) "Thuis Winst" else if (awayTeamWins) "Uit Winst" else "Gelijk",
                    scenarioType = ScenarioType.BANKER
                )
            }
            oracleAnalysis.confidence >= 50 -> {
                MastermindSignal(
                    title = "MATIGE ZEKERHEID",
                    description = "Oracle voorspelt met ${oracleAnalysis.confidence}% zekerheid. " +
                                 "Krachtverschil: ${oracleAnalysis.powerDelta} punten.",
                    color = SignalColor.YELLOW,
                    confidence = oracleAnalysis.confidence,
                    recommendation = "Voorzichtige inzet",
                    scenarioType = ScenarioType.TACTICAL_DUEL
                )
            }
            else -> {
                MastermindSignal(
                    title = "LAAG RISICO",
                    description = "Oracle heeft slechts ${oracleAnalysis.confidence}% zekerheid. " +
                                 "Wees extra voorzichtig met deze voorspelling.",
                    color = SignalColor.RED,
                    confidence = oracleAnalysis.confidence,
                    recommendation = "Kleine inzet of vermijden",
                    scenarioType = ScenarioType.HIGH_RISK
                )
            }
        }
    }
    
    /**
     * Apply LLMGRADE enhancement to a base MastermindSignal.
     */
    private fun applyLLMGradeEnhancement(
        baseSignal: MastermindSignal,
        llmGradeEnhancement: LLMGradeEnhancement,
        oracleAnalysis: OracleAnalysis,
        tesseractResult: TesseractResult?
    ): MastermindSignal {
        // Calculate adjusted confidence
        val adjustedConfidence = llmGradeEnhancement.calculateAdjustedConfidence(baseSignal.confidence)
        
        // Check for high-probability outlier scenarios that might override the base signal
        val highProbabilityOutlier = llmGradeEnhancement.highestProbabilityOutlier
        val shouldOverrideWithOutlier = highProbabilityOutlier != null && 
                                       highProbabilityOutlier.isHighProbability &&
                                       highProbabilityOutlier.riskLevel == RiskLevel.HIGH
        
        // Build enhanced description
        val enhancedDescription = buildEnhancedDescription(
            baseSignal = baseSignal,
            llmGradeEnhancement = llmGradeEnhancement,
            oracleAnalysis = oracleAnalysis,
            tesseractResult = tesseractResult,
            shouldOverrideWithOutlier = shouldOverrideWithOutlier
        )
        
        // Determine if we need to change the signal type based on LLM insights
        val enhancedScenarioType = determineEnhancedScenarioType(
            baseScenarioType = baseSignal.scenarioType,
            llmGradeEnhancement = llmGradeEnhancement
        )
        
        // Determine enhanced color based on risk level
        val enhancedColor = determineEnhancedColor(
            baseColor = baseSignal.color,
            llmGradeEnhancement = llmGradeEnhancement
        )
        
        // Build enhanced recommendation
        val enhancedRecommendation = buildEnhancedRecommendation(
            baseRecommendation = baseSignal.recommendation,
            llmGradeEnhancement = llmGradeEnhancement,
            shouldOverrideWithOutlier = shouldOverrideWithOutlier
        )
        
        return MastermindSignal(
            title = if (shouldOverrideWithOutlier) "⚠️ ${baseSignal.title}" else baseSignal.title,
            description = enhancedDescription,
            color = enhancedColor,
            confidence = adjustedConfidence,
            recommendation = enhancedRecommendation,
            scenarioType = enhancedScenarioType
        )
    }
    
    /**
     * Build enhanced description incorporating LLM insights.
     */
    private fun buildEnhancedDescription(
        baseSignal: MastermindSignal,
        llmGradeEnhancement: LLMGradeEnhancement,
        oracleAnalysis: OracleAnalysis,
        tesseractResult: TesseractResult?,
        shouldOverrideWithOutlier: Boolean
    ): String {
        return buildString {
            // Start with base description
            appendLine(baseSignal.description)
            appendLine()
            
            // Add LLMGRADE context summary
            appendLine("🎯 LLMGRADE INSIGHTS:")
            
            // Add context factors summary
            if (llmGradeEnhancement.contextFactors.isNotEmpty()) {
                val mostImpactful = llmGradeEnhancement.mostImpactfulFactor
                if (mostImpactful != null) {
                    appendLine("• Belangrijkste factor: ${mostImpactful.type.dutchDescription()} (${mostImpactful.score}/10)")
                    appendLine("  ${mostImpactful.description}")
                }
                
                val highImpactCount = llmGradeEnhancement.contextFactors.count { it.isHighImpact }
                if (highImpactCount > 0) {
                    appendLine("• $highImpactCount hoge-impact factoren geïdentificeerd")
                }
            }
            
            // Add outlier scenarios warning if applicable
            if (shouldOverrideWithOutlier) {
                val outlier = llmGradeEnhancement.highestProbabilityOutlier!!
                appendLine()
                appendLine("⚠️  HOOG-RISICO UITSCHIETER:")
                appendLine("${outlier.description} (${outlier.probability.toInt()}% kans)")
                
                if (outlier.supportingFactors.isNotEmpty()) {
                    appendLine("Ondersteunende factoren:")
                    outlier.supportingFactors.take(2).forEach { factor ->
                        appendLine("• $factor")
                    }
                }
            } else if (llmGradeEnhancement.outlierScenarios.isNotEmpty()) {
                val outlierCount = llmGradeEnhancement.outlierScenarios.size
                appendLine("• $outlierCount uitschieterscenario's geïdentificeerd")
            }
            
            // Add confidence adjustment note
            val adjustment = llmGradeEnhancement.confidenceAdjustment
            if (adjustment != 0) {
                val sign = if (adjustment > 0) "+" else ""
                appendLine("• Vertrouwensaanpassing: $sign$adjustment% (op basis van context)")
            }
        }
    }
    
    /**
     * Determine enhanced scenario type based on LLM insights.
     */
    private fun determineEnhancedScenarioType(
        baseScenarioType: ScenarioType,
        llmGradeEnhancement: LLMGradeEnhancement
    ): ScenarioType {
        // If there are high-probability outliers, consider it high risk
        if (llmGradeEnhancement.hasHighProbabilityOutliers) {
            return ScenarioType.HIGH_RISK
        }
        
        // If context factors are mostly negative, adjust to higher risk
        val negativeFactorCount = llmGradeEnhancement.contextFactors.count { it.isNegative }
        val totalFactorCount = llmGradeEnhancement.contextFactors.size
        
        if (totalFactorCount > 0 && negativeFactorCount.toDouble() / totalFactorCount > 0.5) {
            return when (baseScenarioType) {
                ScenarioType.BANKER -> ScenarioType.TACTICAL_DUEL
                ScenarioType.TACTICAL_DUEL -> ScenarioType.HIGH_RISK
                else -> baseScenarioType
            }
        }
        
        return baseScenarioType
    }
    
    /**
     * Determine enhanced color based on LLM risk assessment.
     */
    private fun determineEnhancedColor(
        baseColor: SignalColor,
        llmGradeEnhancement: LLMGradeEnhancement
    ): SignalColor {
        val overallRisk = llmGradeEnhancement.overallRiskLevel()
        
        return when (overallRisk) {
            RiskLevel.VERY_HIGH -> SignalColor.RED
            RiskLevel.HIGH -> SignalColor.RED
            RiskLevel.MEDIUM -> SignalColor.YELLOW
            RiskLevel.LOW -> when (baseColor) {
                SignalColor.RED -> SignalColor.YELLOW // Upgrade from red to yellow if LLM says low risk
                else -> baseColor
            }
        }
    }
    
    /**
     * Build enhanced recommendation incorporating LLM insights.
     */
    private fun buildEnhancedRecommendation(
        baseRecommendation: String,
        llmGradeEnhancement: LLMGradeEnhancement,
        shouldOverrideWithOutlier: Boolean
    ): String {
        if (shouldOverrideWithOutlier) {
            val outlier = llmGradeEnhancement.highestProbabilityOutlier!!
            return "⚠️  Overweeg: ${outlier.description.take(50)}..."
        }
        
        // Add LLM context to base recommendation
        val contextSummary = buildString {
            if (llmGradeEnhancement.hasHighImpactFactors) {
                append(" (Hoge-impact context factoren)")
            }
            if (llmGradeEnhancement.hasHighProbabilityOutliers) {
                append(" (Let op uitschieters)")
            }
        }
        
        return if (contextSummary.isNotEmpty()) {
            "$baseRecommendation$contextSummary"
        } else {
            baseRecommendation
        }
    }
    
    /**
     * Helper method to fix the syntax error in the previous version.
     */
    private fun fixSyntaxError(): String {
        return "Fixed syntax error in buildEnhancedRecommendation"
    }
}
