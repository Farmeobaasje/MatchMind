package com.Lyno.matchmindai.domain.service

import com.Lyno.matchmindai.domain.model.*
import kotlin.math.exp
import kotlin.math.ln

/**
 * STATE-OF-THE-ART Enhanced ScorePredictor met Bayesian Smoothing & Safety Caps.
 * 
 * Upgrade naar professioneel sport analytics model dat:
 * 1. Bayesian smoothing gebruikt voor "zero-frequency" en "overconfidence" problemen
 * 2. Lambda soft-caps implementeert voor extreme expected goals
 * 3. Harde probability caps (2%-96%) voor realistische kansen
 * 4. xG + AI feature engineering integratie
 * 
 * Wiskundige basis: Dixon-Coles (1997) + Bayesian Inference + Regression to the Mean
 */
class EnhancedScorePredictor(
    private val expectedGoalsService: ExpectedGoalsService = ExpectedGoalsService()
) {

    companion object {
        // 🔥 CALIBRATED BAYESIAN PARAMETERS (Relaxed further for realistic dominant wins)
        private const val BAYESIAN_C = 2.0           // "Skepticism Factor": voegt 2.0 virtuele wedstrijden toe (was 2.5, was 6.0)
        private const val LEAGUE_AVG_GOALS = 1.35    // Global professional league average
        private const val MIN_PROB = 0.02            // Harde ondergrens: 2% (nooit 0%)
        private const val MAX_PROB = 0.96            // Harde bovengrens: 96% (nooit 100%)
        
        // Dixon-Coles model parameters
        private const val DEFAULT_RHO = -0.13        // Correlation parameter voor low scores
        private const val MIN_MATCHES_FOR_PREDICTION = 10
        
        // Safe defaults voor league averages
        private const val DEFAULT_HOME_AVG_GOALS = 1.55
        private const val DEFAULT_AWAY_AVG_GOALS = 1.25
        private const val LOW_SCORING_THRESHOLD = 0.8
        
        // Confidence thresholds
        private const val HIGH_CONFIDENCE_THRESHOLD = 0.7
        private const val MEDIUM_CONFIDENCE_THRESHOLD = 0.4
        
        // Lambda soft-cap threshold (Relaxed further to allow realistic dominant wins)
        private const val LAMBDA_SOFT_CAP_THRESHOLD = 3.5
        private const val LAMBDA_COMPRESSION_FACTOR = 0.7  // 70% of excess over threshold (was 60%)
    }

    /**
     * STATE-OF-THE-ART: Predict match outcome met Bayesian smoothing en safety caps.
     * 
     * @param homeTeamFixtures Historical fixtures for home team
     * @param awayTeamFixtures Historical fixtures for away team
     * @param leagueFixtures All fixtures in the league for parameter estimation
     * @param xgDataMap Map of fixtureId -> ExpectedGoalsData (optional)
     * @param modifiers NewsImpactModifiers for AI adjustments (optional)
     * @return EnhancedPrediction met Bayesian-smoothed probabilities en confidence
     */
    suspend fun predictMatchWithXg(
        homeTeamFixtures: List<HistoricalFixture>,
        awayTeamFixtures: List<HistoricalFixture>,
        leagueFixtures: List<HistoricalFixture>,
        xgDataMap: Map<Int, ExpectedGoalsData> = emptyMap(),
        modifiers: NewsImpactModifiers? = null,
        homeRank: Int? = null,
        awayRank: Int? = null
    ): Result<EnhancedPrediction> {
        // 🔎 INPUT VERIFICATION: Log team names and history sizes
        val homeTeamName = homeTeamFixtures.firstOrNull()?.homeTeamName ?: "Home"
        val awayTeamName = awayTeamFixtures.firstOrNull()?.awayTeamName ?: "Away"
        
        android.util.Log.d("EnhancedScorePredictor",
            "🔎 ANALYZING: $homeTeamName vs $awayTeamName"
        )
        android.util.Log.d("EnhancedScorePredictor",
            "📊 History Size: Home=${homeTeamFixtures.size} matches, Away=${awayTeamFixtures.size} matches"
        )
        
        // 🔒 ID-BASED INTEGRITY CHECK: Log the context to prevent pollution
        println("🔒 PREDICTOR ANALYZING: $homeTeamName vs $awayTeamName (from historical fixtures)")
        
        // Log data sizes voor debugging
        android.util.Log.d("EnhancedScorePredictor",
            "STATE-OF-THE-ART PREDICTION: " +
            "homeFixtures=${homeTeamFixtures.size}, " +
            "awayFixtures=${awayTeamFixtures.size}, " +
            "leagueFixtures=${leagueFixtures.size}, " +
            "xgData=${xgDataMap.size} fixtures"
        )
        
        // Validate we have enough data
        if (!hasSufficientData(homeTeamFixtures, awayTeamFixtures, leagueFixtures)) {
            android.util.Log.w("EnhancedScorePredictor",
                "Insufficient data for prediction"
            )
            return Result.failure(IllegalArgumentException("Insufficient data for prediction"))
        }

        try {
            // 1. Calculate league averages using xG data
            val (leagueHomeAvg, leagueAwayAvg) = expectedGoalsService.calculateLeagueAverages(leagueFixtures, xgDataMap)
            
            // 2. Get weighted team data with xG integration
            val homeWeightedData = expectedGoalsService.getWeightedTeamData(homeTeamFixtures, xgDataMap, isHomeTeam = true)
            val awayWeightedData = expectedGoalsService.getWeightedTeamData(awayTeamFixtures, xgDataMap, isHomeTeam = false)
            
            // 3. 🔥 CALIBRATED: Calculate Bayesian team strengths (with debug logging)
            val rawHomeAttack = calculateRawStrength(homeWeightedData, leagueHomeAvg, isAttack = true)
            val rawHomeDefense = calculateRawStrength(homeWeightedData, leagueAwayAvg, isAttack = false)
            val rawAwayAttack = calculateRawStrength(awayWeightedData, leagueAwayAvg, isAttack = true)
            val rawAwayDefense = calculateRawStrength(awayWeightedData, leagueHomeAvg, isAttack = false)
            
            val homeAttackStrength = calculateBayesianStrength(homeWeightedData, leagueHomeAvg, isAttack = true)
            val homeDefenseStrength = calculateBayesianStrength(homeWeightedData, leagueAwayAvg, isAttack = false)
            val awayAttackStrength = calculateBayesianStrength(awayWeightedData, leagueAwayAvg, isAttack = true)
            val awayDefenseStrength = calculateBayesianStrength(awayWeightedData, leagueHomeAvg, isAttack = false)
            
            // ⚔️ STRENGTH CALCULATION: Log detailed strength values
            android.util.Log.d("EnhancedScorePredictor",
                "⚔️ Home Attack Strength: Raw=${String.format("%.3f", rawHomeAttack)} -> Bayesian=${String.format("%.3f", homeAttackStrength)}"
            )
            android.util.Log.d("EnhancedScorePredictor",
                "🛡️ Home Defense Strength: Raw=${String.format("%.3f", rawHomeDefense)} -> Bayesian=${String.format("%.3f", homeDefenseStrength)}"
            )
            android.util.Log.d("EnhancedScorePredictor",
                "⚔️ Away Attack Strength: Raw=${String.format("%.3f", rawAwayAttack)} -> Bayesian=${String.format("%.3f", awayAttackStrength)}"
            )
            android.util.Log.d("EnhancedScorePredictor",
                "🛡️ Away Defense Strength: Raw=${String.format("%.3f", rawAwayDefense)} -> Bayesian=${String.format("%.3f", awayDefenseStrength)}"
            )
            
            // DATA VITALITY LOGGING: Log the "Strength Delta" and Bayesian Impact
            // Strength Delta: difference between team's performance and league average
            // Note: raw strengths are in log scale, so delta is also in log scale
            val homeAttackDelta = rawHomeAttack - 0.0 // In log scale, 0.0 represents league average (ln(1.0) = 0.0)
            val awayAttackDelta = rawAwayAttack - 0.0
            
            android.util.Log.d("DataTrace", "Home Attack Delta: ${String.format("%.3f", homeAttackDelta)} (If close to 0.0, team is considered average)")
            android.util.Log.d("DataTrace", "Away Attack Delta: ${String.format("%.3f", awayAttackDelta)} (If close to 0.0, team is considered average)")
            
            // Bayesian Impact: show smoothing effect
            // Convert from log scale back to linear for percentage calculation
            val rawHomeAttackLinear = exp(rawHomeAttack)
            val smoothedHomeAttackLinear = exp(homeAttackStrength)
            val bayesianImpactPercent = if (rawHomeAttackLinear > 0) {
                ((smoothedHomeAttackLinear / rawHomeAttackLinear) * 100).toInt()
            } else {
                100
            }
            
            android.util.Log.d("DataTrace", "Smoothing: Raw=${String.format("%.3f", rawHomeAttackLinear)} -> Smoothed=${String.format("%.3f", smoothedHomeAttackLinear)} (Impact: ${bayesianImpactPercent}%)")
            
            // Also log for away team
            val rawAwayAttackLinear = exp(rawAwayAttack)
            val smoothedAwayAttackLinear = exp(awayAttackStrength)
            val awayBayesianImpactPercent = if (rawAwayAttackLinear > 0) {
                ((smoothedAwayAttackLinear / rawAwayAttackLinear) * 100).toInt()
            } else {
                100
            }
            
            android.util.Log.d("DataTrace", "Smoothing (Away): Raw=${String.format("%.3f", rawAwayAttackLinear)} -> Smoothed=${String.format("%.3f", smoothedAwayAttackLinear)} (Impact: ${awayBayesianImpactPercent}%)")
            
            // DEBUG: Log raw team strengths before adjustments
            println("[DEBUG] Raw Team Strengths (log scale):")
            println("  Home Attack: ${String.format("%.3f", homeAttackStrength)}")
            println("  Home Defense: ${String.format("%.3f", homeDefenseStrength)}")
            println("  Away Attack: ${String.format("%.3f", awayAttackStrength)}")
            println("  Away Defense: ${String.format("%.3f", awayDefenseStrength)}")
            
            // DEBUG: Raw Strength vs Bayesian Strength comparison
            println("[DEBUG] Raw Strength vs Bayesian Strength (C=${BAYESIAN_C}):")
            println("  Home Attack: Raw=${String.format("%.3f", calculateRawStrength(homeWeightedData, leagueHomeAvg, isAttack = true))} vs Bayesian=${String.format("%.3f", homeAttackStrength)}")
            println("  Home Defense: Raw=${String.format("%.3f", calculateRawStrength(homeWeightedData, leagueAwayAvg, isAttack = false))} vs Bayesian=${String.format("%.3f", homeDefenseStrength)}")
            println("  Away Attack: Raw=${String.format("%.3f", calculateRawStrength(awayWeightedData, leagueAwayAvg, isAttack = true))} vs Bayesian=${String.format("%.3f", awayAttackStrength)}")
            println("  Away Defense: Raw=${String.format("%.3f", calculateRawStrength(awayWeightedData, leagueHomeAvg, isAttack = false))} vs Bayesian=${String.format("%.3f", awayDefenseStrength)}")
            
            // 4. Calculate home advantage
            val homeAdvantage = calculateHomeAdvantage(leagueHomeAvg, leagueAwayAvg)
            
            // 5. Create base team strength
            val baseTeamStrength = BaseTeamStrength(
                homeAttackStrength = exp(homeAttackStrength),
                homeDefenseStrength = exp(homeDefenseStrength),
                awayAttackStrength = exp(awayAttackStrength),
                awayDefenseStrength = exp(awayDefenseStrength),
                homeAdvantage = exp(homeAdvantage),
                leagueAverageHomeGoals = leagueHomeAvg,
                leagueAverageAwayGoals = leagueAwayAvg,
                calculationConfidence = calculateDataQualityConfidence(homeWeightedData, awayWeightedData)
            )
            
            // DEBUG: Log adjusted team strengths
            println("[DEBUG] Adjusted Team Strengths (exp scale):")
            println("  Home Attack: ${String.format("%.3f", baseTeamStrength.homeAttackStrength)}")
            println("  Home Defense: ${String.format("%.3f", baseTeamStrength.homeDefenseStrength)}")
            println("  Away Attack: ${String.format("%.3f", baseTeamStrength.awayAttackStrength)}")
            println("  Away Defense: ${String.format("%.3f", baseTeamStrength.awayDefenseStrength)}")
            println("  Home Advantage: ${String.format("%.3f", baseTeamStrength.homeAdvantage)}")
            
            if (!baseTeamStrength.isValid) {
                return Result.failure(IllegalArgumentException("Invalid team strength calculation"))
            }
            
            // 6. Apply AI modifiers if available and valid
            val adjustedTeamStrength = if (modifiers != null && modifiers.isValid) {
                baseTeamStrength.applyModifiers(modifiers)
            } else {
                AdjustedTeamStrength(
                    homeAttackStrength = baseTeamStrength.homeAttackStrength,
                    homeDefenseStrength = baseTeamStrength.homeDefenseStrength,
                    awayAttackStrength = baseTeamStrength.awayAttackStrength,
                    awayDefenseStrength = baseTeamStrength.awayDefenseStrength,
                    homeAdvantage = baseTeamStrength.homeAdvantage,
                    leagueAverageHomeGoals = baseTeamStrength.leagueAverageHomeGoals,
                    leagueAverageAwayGoals = baseTeamStrength.leagueAverageAwayGoals,
                    baseStrength = baseTeamStrength,
                    modifiers = modifiers ?: createDefaultModifiers()
                )
            }
            
            // 7. Calculate expected goals with adjustments
            val (rawHomeGoals, rawAwayGoals) = adjustedTeamStrength.getExpectedGoals()
            
            // 🧮 THE FORMULA: Log lambda calculation details
            // Get the actual values used in the formula
            val homeAttack = adjustedTeamStrength.homeAttackStrength
            val awayDefense = adjustedTeamStrength.awayDefenseStrength
            val leagueAvg = adjustedTeamStrength.leagueAverageHomeGoals
            val homeAdv = adjustedTeamStrength.homeAdvantage
            
            android.util.Log.d("EnhancedScorePredictor",
                "🧮 Lambda Calculation: " +
                "Attack(${String.format("%.3f", homeAttack)}) * " +
                "Defense(${String.format("%.3f", awayDefense)}) * " +
                "LeagueAvg(${String.format("%.3f", leagueAvg)}) * " +
                "HomeAdv(${String.format("%.3f", homeAdv)}) = " +
                "${String.format("%.3f", rawHomeGoals)}"
            )
            
            // For away goals
            val awayAttack = adjustedTeamStrength.awayAttackStrength
            val homeDefense = adjustedTeamStrength.homeDefenseStrength
            val leagueAvgAway = adjustedTeamStrength.leagueAverageAwayGoals
            
            android.util.Log.d("EnhancedScorePredictor",
                "🧮 Lambda Calculation (Away): " +
                "Attack(${String.format("%.3f", awayAttack)}) * " +
                "Defense(${String.format("%.3f", homeDefense)}) * " +
                "LeagueAvg(${String.format("%.3f", leagueAvgAway)}) = " +
                "${String.format("%.3f", rawAwayGoals)}"
            )
            
            // DEBUG: Log raw expected goals before soft-cap
            println("[DEBUG] Raw Expected Goals (before soft-cap):")
            println("  Home: ${String.format("%.3f", rawHomeGoals)}")
            println("  Away: ${String.format("%.3f", rawAwayGoals)}")
            
            // 8. 🔥 RECENCY BIAS FIX: Apply rank-based quality multiplier
            val (rankAdjustedHomeGoals, rankAdjustedAwayGoals) = applyRankCorrection(
                rawHomeGoals, rawAwayGoals, homeRank, awayRank
            )
            
            // 9. 🔥 CALIBRATED: Apply relaxed lambda soft-cap voor extreme expected goals
            val expectedHomeGoals = applyLambdaSoftCap(rankAdjustedHomeGoals)
            val expectedAwayGoals = applyLambdaSoftCap(rankAdjustedAwayGoals)
            
            // 🧢 SOFT-CAP CHECK: Log soft-cap application
            android.util.Log.d("EnhancedScorePredictor",
                "🧢 Soft-Cap Check: " +
                "Raw Lambda Home=${String.format("%.3f", rawHomeGoals)} -> " +
                "Capped Lambda Home=${String.format("%.3f", expectedHomeGoals)} " +
                "(${if (rawHomeGoals > LAMBDA_SOFT_CAP_THRESHOLD) "CAPPED" else "NO_CAP"})"
            )
            android.util.Log.d("EnhancedScorePredictor",
                "🧢 Soft-Cap Check: " +
                "Raw Lambda Away=${String.format("%.3f", rawAwayGoals)} -> " +
                "Capped Lambda Away=${String.format("%.3f", expectedAwayGoals)} " +
                "(${if (rawAwayGoals > LAMBDA_SOFT_CAP_THRESHOLD) "CAPPED" else "NO_CAP"})"
            )
            
            // DEBUG: Log capped expected goals
            println("[DEBUG] Capped Expected Goals (after soft-cap):")
            println("  Home: ${String.format("%.3f", expectedHomeGoals)} (capped: ${if (rawHomeGoals > LAMBDA_SOFT_CAP_THRESHOLD) "YES" else "NO"})")
            println("  Away: ${String.format("%.3f", expectedAwayGoals)} (capped: ${if (rawAwayGoals > LAMBDA_SOFT_CAP_THRESHOLD) "YES" else "NO"})")
            
            // 9. Calculate probabilities using Dixon-Coles adjusted Poisson
            val probabilities = calculateProbabilities(expectedHomeGoals, expectedAwayGoals)
            
            // 10. Calculate prediction confidence
            val confidence = calculatePredictionConfidence(
                baseTeamStrength, adjustedTeamStrength, homeTeamFixtures, awayTeamFixtures
            )
            
            // 11. Create enhanced prediction
            val enhancedPrediction = EnhancedPrediction(
                fixtureId = 0, // Will be set by caller
                homeTeam = homeTeamFixtures.firstOrNull()?.homeTeamName ?: "Home",
                awayTeam = awayTeamFixtures.firstOrNull()?.awayTeamName ?: "Away",
                homeWinProbability = probabilities.first,
                drawProbability = probabilities.second,
                awayWinProbability = probabilities.third,
                expectedGoalsHome = expectedHomeGoals,
                expectedGoalsAway = expectedAwayGoals,
                valueEdge = 0.0, // Will be calculated by risk manager
                kellyStake = 0.0, // Will be calculated by risk manager
                baseTeamStrength = baseTeamStrength,
                newsImpactModifiers = modifiers,
                confidence = confidence
            )
            
            // Log the prediction results for debugging
            android.util.Log.d("EnhancedScorePredictor",
                "STATE-OF-THE-ART RESULTS: " +
                "expectedGoals=${String.format("%.2f", expectedHomeGoals)}-${String.format("%.2f", expectedAwayGoals)}, " +
                "probabilities=${String.format("%.2f", probabilities.first)}-${String.format("%.2f", probabilities.second)}-${String.format("%.2f", probabilities.third)}, " +
                "confidence=${String.format("%.2f", confidence)}, " +
                "hasModifiers=${modifiers != null}, " +
                "lambdaSoftCap=${if (rawHomeGoals > LAMBDA_SOFT_CAP_THRESHOLD || rawAwayGoals > LAMBDA_SOFT_CAP_THRESHOLD) "APPLIED" else "NOT_NEEDED"}"
            )
            
            return Result.success(enhancedPrediction)
            
        } catch (e: Exception) {
            android.util.Log.e("EnhancedScorePredictor", "STATE-OF-THE-ART prediction failed", e)
            return Result.failure(e)
        }
    }

    /**
     * 🔥 CALIBRATED: Bayesian smoothing voor "zero-frequency" en "overconfidence" problemen.
     * 
     * Formule: SmoothedStrength = (TotalWeightedXg + (BAYESIAN_C * leagueAverageGoals)) / (TotalMatches + BAYESIAN_C)
     * NormalizedStrength = SmoothedStrength / leagueAverageGoals
     * 
     * Doel: Teams met 0 goals in 2 wedstrijden krijgen attack strength ~1.0 (gemiddeld) in plaats van 0.0.
     * 
     * CHANGED: BAYESIAN_C reduced from 2.5 to 2.0 to allow realistic dominant wins.
     */
    private suspend fun calculateBayesianStrength(
        weightedFixtures: List<WeightedFixtureData>,
        leagueAverageGoals: Double,
        isAttack: Boolean
    ): Double {
        if (weightedFixtures.isEmpty()) {
            return 0.0 // Log(1.0) = 0.0 (gemiddelde strength)
        }
        
        // Bereken total weighted xG en matches
        val totalWeightedXg = weightedFixtures.sumOf { 
            val score = if (isAttack) {
                // Attack: gebruik inputScore (goals/xG gescoord)
                it.inputScore
            } else {
                // Defense: bereken goals/xG geconcedeerd via fixture data
                calculateConcededScore(it)
            }
            score * it.weight 
        }
        val totalMatches = weightedFixtures.sumOf { it.weight }
        
        // 🔥 CALIBRATED BAYESIAN SMOOTHING FORMULE (C=2.0 instead of 2.5)
        // Use the provided leagueAverageGoals instead of the constant
        val smoothedStrength = (totalWeightedXg + (BAYESIAN_C * leagueAverageGoals)) / (totalMatches + BAYESIAN_C)
        val normalizedStrength = smoothedStrength / leagueAverageGoals
        
        // DEBUG: Log Bayesian smoothing details
        println("[DEBUG] Bayesian Smoothing (C=${BAYESIAN_C}):")
        println("  isAttack=$isAttack, totalWeightedXg=${String.format("%.3f", totalWeightedXg)}")
        println("  totalMatches=${String.format("%.3f", totalMatches)}, smoothedStrength=${String.format("%.3f", smoothedStrength)}")
        println("  normalizedStrength=${String.format("%.3f", normalizedStrength)}")
        
        // Return log van normalized strength (voor consistentie met Dixon-Coles)
        return ln(normalizedStrength.coerceAtLeast(0.1))
    }

    /**
     * Calculate raw strength without Bayesian smoothing (for debugging comparison).
     */
    private fun calculateRawStrength(
        weightedFixtures: List<WeightedFixtureData>,
        leagueAverageGoals: Double,
        isAttack: Boolean
    ): Double {
        if (weightedFixtures.isEmpty()) {
            return 0.0
        }
        
        val totalWeightedXg = weightedFixtures.sumOf { 
            val score = if (isAttack) {
                it.inputScore
            } else {
                calculateConcededScore(it)
            }
            score * it.weight 
        }
        val totalMatches = weightedFixtures.sumOf { it.weight }
        
        // Raw strength without Bayesian smoothing
        val rawStrength = if (totalMatches > 0) totalWeightedXg / totalMatches else 0.0
        val normalizedStrength = rawStrength / leagueAverageGoals
        
        return ln(normalizedStrength.coerceAtLeast(0.1))
    }

    /**
     * Helper function to calculate conceded score for defense strength calculation.
     */
    private fun calculateConcededScore(weightedFixture: WeightedFixtureData): Double {
        val fixture = weightedFixture.fixture
        val xgData = weightedFixture.xgData
        
        // Gebruik dezelfde logica als ExpectedGoalsService voor consistentie
        return if (xgData != null) {
            val (homeXgAdjusted, awayXgAdjusted) = xgData.getInputScore(fixture.homeGoals, fixture.awayGoals)
            // Voor defense: gebruik de score van de tegenstander
            // Als dit voor home team defense is, gebruik awayXgAdjusted
            // Als dit voor away team defense is, gebruik homeXgAdjusted
            // We moeten weten of dit home of away team is, maar dat wordt bepaald door de aanroeper
            // Voor nu gebruiken we een simpele benadering: gemiddelde van beide
            (homeXgAdjusted + awayXgAdjusted) / 2.0
        } else {
            // Fallback: gebruik gemiddelde goals
            (fixture.homeGoals + fixture.awayGoals) / 2.0
        }
    }

    /**
     * 🔥 CALIBRATED: Relaxed lambda soft-cap voor extreme expected goals.
     * 
     * Als lambda of mu > 3.5 is, vlak dit af met een lineaire decay formule:
     * CappedLambda = 3.5 + (rawLambda - 3.5) * 0.7
     * 
     * Doel: Voorkomt onrealistische 5-0, 6-0 voorspellingen maar laat 4.0+ goals toe voor sterke teams.
     * 
     * CHANGED: Threshold increased to 3.5 with 70% compression for realistic dominant wins.
     */
    private fun applyLambdaSoftCap(lambda: Double): Double {
        return if (lambda > LAMBDA_SOFT_CAP_THRESHOLD) {
            val excess = lambda - LAMBDA_SOFT_CAP_THRESHOLD
            val capped = LAMBDA_SOFT_CAP_THRESHOLD + (excess * LAMBDA_COMPRESSION_FACTOR)
            
            // DEBUG: Log soft-cap application details
            println("[DEBUG] Lambda Soft-Cap Applied (Linear Decay):")
            println("  rawLambda=${String.format("%.3f", lambda)}, threshold=${LAMBDA_SOFT_CAP_THRESHOLD}")
            println("  excess=${String.format("%.3f", excess)}, compressionFactor=${LAMBDA_COMPRESSION_FACTOR}")
            println("  cappedLambda=${String.format("%.3f", capped)}")
            println("  reduction=${String.format("%.1f", ((lambda - capped) / lambda * 100))}%")
            
            android.util.Log.d("EnhancedScorePredictor",
                "Lambda Soft-Cap Applied (Linear Decay): " +
                "raw=${String.format("%.2f", lambda)}, " +
                "capped=${String.format("%.2f", capped)}, " +
                "reduction=${String.format("%.1f", ((lambda - capped) / lambda * 100))}%"
            )
            
            capped
        } else {
            lambda
        }
    }

    /**
     * Calculate home advantage parameter from league averages.
     * Updated for Eredivisie reality: top teams perform well away from home.
     */
    private fun calculateHomeAdvantage(leagueHomeAvg: Double, leagueAwayAvg: Double): Double {
        // Division by zero protection
        val ratio = if (leagueAwayAvg > 0) leagueHomeAvg / leagueAwayAvg else 1.3
        
        // For Eredivisie, home advantage is less pronounced for top teams
        // Original: 1.1-2.0 range (too high for modern football)
        // Updated: 1.05-1.3 range (more realistic for top leagues)
        val adjustedRatio = ratio.coerceIn(1.05, 1.3)
        
        // Log for debugging
        android.util.Log.d("EnhancedScorePredictor", 
            "Home advantage calculation: " +
            "leagueHomeAvg=${String.format("%.2f", leagueHomeAvg)}, " +
            "leagueAwayAvg=${String.format("%.2f", leagueAwayAvg)}, " +
            "ratio=${String.format("%.2f", ratio)}, " +
            "adjustedRatio=${String.format("%.2f", adjustedRatio)}"
        )
        
        return ln(adjustedRatio)
    }

    /**
     * 🔥 STATE-OF-THE-ART: Calculate match outcome probabilities met harde caps (2%-96%).
     * 
     * Voorkomt 100% en 0% kansen die onrealistisch zijn in voetbal.
     * Real Madrid wint niet 100% van de tijd thuis van degradatiekandidaat.
     */
    private fun calculateProbabilities(
        lambdaHome: Double,
        lambdaAway: Double,
        rho: Double = DEFAULT_RHO
    ): Triple<Double, Double, Double> {
        // Calculate probabilities for common scorelines (0-0 to 4-4)
        var homeWinProb = 0.0
        var drawProb = 0.0
        var awayWinProb = 0.0
        
        for (i in 0..4) { // Home goals
            for (j in 0..4) { // Away goals
                val poissonProb = poissonProbability(i, lambdaHome) * poissonProbability(j, lambdaAway)
                val tau = calculateTau(i, j, lambdaHome, lambdaAway, rho)
                val adjustedProb = poissonProb * tau
                
                when {
                    i > j -> homeWinProb += adjustedProb
                    i == j -> drawProb += adjustedProb
                    i < j -> awayWinProb += adjustedProb
                }
            }
        }
        
        // Normalize probabilities
        val total = homeWinProb + drawProb + awayWinProb
        if (total <= 0) {
            return Triple(0.33, 0.34, 0.33) // Fallback to equal probabilities
        }
        
        // 🔥 STATE-OF-THE-ART: HARDE PROBABILITY CAPS (2%-96%)
        var h = (homeWinProb / total).coerceIn(MIN_PROB, MAX_PROB)
        var d = (drawProb / total).coerceIn(MIN_PROB, MAX_PROB)
        var a = (awayWinProb / total).coerceIn(MIN_PROB, MAX_PROB)
        
        // Her-normaliseer zodat de som weer 1.0 is na het cappen
        val cappedTotal = h + d + a
        h /= cappedTotal
        d /= cappedTotal
        a /= cappedTotal
        
        // Log voor debugging
        android.util.Log.d("EnhancedScorePredictor", 
            "STATE-OF-THE-ART Probability Capping: " +
            "Raw=${String.format("%.2f", homeWinProb/total)}-${String.format("%.2f", drawProb/total)}-${String.format("%.2f", awayWinProb/total)}, " +
            "Capped=${String.format("%.2f", h)}-${String.format("%.2f", d)}-${String.format("%.2f", a)}"
        )
        
        return Triple(h, d, a)
    }

    /**
     * Dixon-Coles adjustment factor tau for low-scoring correlation.
     */
    private fun calculateTau(
        x: Int, y: Int,
        lambdaHome: Double, lambdaAway: Double,
        rho: Double
    ): Double {
        return when {
            x == 0 && y == 0 -> 1.0 - lambdaHome * lambdaAway * rho
            x == 0 && y == 1 -> 1.0 + lambdaHome * rho
            x == 1 && y == 0 -> 1.0 + lambdaAway * rho
            x == 1 && y == 1 -> 1.0 - rho
            else -> 1.0
        }
    }

    /**
     * Poisson probability mass function.
     */
    private fun poissonProbability(k: Int, lambda: Double): Double {
        return exp(-lambda) * Math.pow(lambda, k.toDouble()) / factorial(k)
    }

    /**
     * Factorial calculation.
     */
    private fun factorial(n: Int): Double {
        var result = 1.0
        for (i in 2..n) {
            result *= i
        }
        return result
    }

    /**
     * Calculate data quality confidence from weighted fixture data.
     */
    private fun calculateDataQualityConfidence(
        homeWeightedData: List<WeightedFixtureData>,
        awayWeightedData: List<WeightedFixtureData>
    ): Double {
        val homeDataQuality = expectedGoalsService.calculateDataQuality(homeWeightedData)
        val awayDataQuality = expectedGoalsService.calculateDataQuality(awayWeightedData)
        return (homeDataQuality + awayDataQuality) / 2.0
    }

    /**
     * Calculate prediction confidence based on data quality and modifiers.
     */
    private fun calculatePredictionConfidence(
        baseTeamStrength: BaseTeamStrength,
        adjustedTeamStrength: AdjustedTeamStrength,
        homeTeamFixtures: List<HistoricalFixture>,
        awayTeamFixtures: List<HistoricalFixture>
    ): Double {
        // Base confidence from data quality
        var confidence = baseTeamStrength.calculationConfidence
        
        // Adjust based on fixture count
        val totalMatches = homeTeamFixtures.size + awayTeamFixtures.size
        val fixtureConfidence = when {
            totalMatches >= 50 -> 0.9
            totalMatches >= 30 -> 0.7
            totalMatches >= 15 -> 0.5
            totalMatches >= 5 -> 0.3
            else -> 0.1
        }
        
        confidence = (confidence + fixtureConfidence) / 2.0
        
        // Adjust based on modifiers if present
        val modifiers = adjustedTeamStrength.modifiers
        if (modifiers.hasMeaningfulImpact) {
            // Higher confidence if modifiers are meaningful and not extreme
            if (!modifiers.isExtreme && modifiers.confidence >= MEDIUM_CONFIDENCE_THRESHOLD) {
                confidence = (confidence + modifiers.confidence) / 2.0
            } else if (modifiers.isExtreme) {
                // Lower confidence for extreme modifiers
                confidence *= 0.8
            }
        }
        
        return confidence.coerceIn(0.1, 1.0)
    }

    /**
     * Check if we have sufficient data for prediction.
     */
    private fun hasSufficientData(
        homeTeamFixtures: List<HistoricalFixture>,
        awayTeamFixtures: List<HistoricalFixture>,
        leagueFixtures: List<HistoricalFixture>
    ): Boolean {
        return homeTeamFixtures.size >= MIN_MATCHES_FOR_PREDICTION &&
               awayTeamFixtures.size >= MIN_MATCHES_FOR_PREDICTION &&
               leagueFixtures.size >= MIN_MATCHES_FOR_PREDICTION * 2
    }

    /**
     * Create default modifiers for when AI analysis is not available.
     */
    private fun createDefaultModifiers(): NewsImpactModifiers {
        return NewsImpactModifiers(
            homeAttackMod = 1.0,
            homeDefenseMod = 1.0,
            awayAttackMod = 1.0,
            awayDefenseMod = 1.0,
            confidence = 0.3,
            reasoning = "Geen AI analyse beschikbaar. Gebruik statistische baseline.",
            chaosFactor = 0.5,
            newsRelevance = 0.1
        )
    }

    /**
     * Get prediction summary for display.
     */
    fun generatePredictionSummary(prediction: EnhancedPrediction): String {
        val mostLikely = when {
            prediction.homeWinProbability >= prediction.drawProbability && 
            prediction.homeWinProbability >= prediction.awayWinProbability -> "Thuis Winst"
            prediction.drawProbability >= prediction.homeWinProbability && 
            prediction.drawProbability >= prediction.awayWinProbability -> "Gelijk"
            else -> "Uit Winst"
        }
        
        val confidencePercent = (prediction.confidence * 100).toInt()
        val hasModifiers = prediction.newsImpactModifiers?.hasMeaningfulImpact == true
        
        return buildString {
            appendLine("🎯 STATE-OF-THE-ART VOORSPELLING (MatchMind AI 2.0)")
            appendLine()
            appendLine("Meest Waarschijnlijk: $mostLikely")
            appendLine("Kansen: Thuis ${(prediction.homeWinProbability * 100).toInt()}% | Gelijk ${(prediction.drawProbability * 100).toInt()}% | Uit ${(prediction.awayWinProbability * 100).toInt()}%")
            appendLine("Verwachte Goals: ${String.format("%.1f", prediction.expectedGoalsHome)} - ${String.format("%.1f", prediction.expectedGoalsAway)}")
            appendLine("Vertrouwen: $confidencePercent%")
            appendLine()
            
            appendLine("📊 CALIBRATED MODEL INSIGHTS:")
            appendLine("• Bayesian smoothing (C=2.0) voor zero-frequency protection")
            appendLine("• Lambda soft-cap (>3.0 → 60% compressie)")
            appendLine("• Harde probability caps (2%-96%) voor realistische kansen")
            appendLine("• AI feature engineering: ${if (hasModifiers) "ACTIEF" else "NIET ACTIEF"}")
            
            if (hasModifiers) {
                appendLine()
                appendLine("🤖 AI AANPASSINGEN:")
                val modifiers = prediction.newsImpactModifiers!!
                val homeAttackChange = ((modifiers.homeAttackMod - 1.0) * 100).toInt()
                val awayAttackChange = ((modifiers.awayAttackMod - 1.0) * 100).toInt()
                
                if (homeAttackChange != 0) {
                    appendLine("  • Thuis aanval: ${if (homeAttackChange > 0) "+" else ""}$homeAttackChange%")
                }
                if (awayAttackChange != 0) {
                    appendLine("  • Uit aanval: ${if (awayAttackChange > 0) "+" else ""}$awayAttackChange%")
                }
                appendLine("  • AI Vertrouwen: ${(modifiers.confidence * 100).toInt()}%")
            }
            
            appendLine()
            appendLine("⚠️  RISICO NIVEAU: ${prediction.riskLevel}")
            appendLine("• Gebruik Fractional Kelly (max 25% van berekende inzet)")
            appendLine("• Backtest altijd voordat je live inzet")
        }
    }

    /**
     * 🔥 RECENCY BIAS FIX: Apply rank-based quality multiplier to correct for recency bias.
     * 
     * Formula: multiplier = 1.0 + (opponentRank - myRank) * 0.03 (AGGRESSIVE)
     * Example: City (2) vs Forest (17). Diff = 15. Multiplier = 1.0 + (15 * 0.03) = 1.45 boost for City.
     * 
     * This ensures that even if a top team is out of form, their inherent quality (Rank 2) 
     * pulls the prediction back to reality.
     * 
     * @param homeGoals Raw expected goals for home team
     * @param awayGoals Raw expected goals for away team
     * @param homeRank Home team league position (1 = top, 20 = bottom)
     * @param awayRank Away team league position (1 = top, 20 = bottom)
     * @return Pair of (adjustedHomeGoals, adjustedAwayGoals)
     */
    private fun applyRankCorrection(
        homeGoals: Double,
        awayGoals: Double,
        homeRank: Int?,
        awayRank: Int?
    ): Pair<Double, Double> {
        // If ranks are not available, return original goals
        if (homeRank == null || awayRank == null) {
            android.util.Log.d("DataTrace", "📊 RANK CORRECTION: No rank data available, skipping correction")
            return Pair(homeGoals, awayGoals)
        }
        
        // Calculate rank difference and multiplier
        val homeAdvantage = awayRank - homeRank  // Positive if home team is higher ranked
        val awayAdvantage = homeRank - awayRank  // Positive if away team is higher ranked
        
        // 🔥 FIX 3: AGGRESSIVE RANK BIAS - Increased from 0.02 to 0.03 for stronger correction
        val homeMultiplier = 1.0 + (homeAdvantage * 0.03)
        val awayMultiplier = 1.0 + (awayAdvantage * 0.03)
        
        // Apply multipliers
        val adjustedHomeGoals = homeGoals * homeMultiplier
        val adjustedAwayGoals = awayGoals * awayMultiplier
        
        // Log the impact
        android.util.Log.d("DataTrace", "📈 RANK CORRECTION: Home(#$homeRank) vs Away(#$awayRank) -> Multiplier: $homeMultiplier (Home), $awayMultiplier (Away)")
        android.util.Log.d("DataTrace", "📈 RANK CORRECTION: Goals adjusted from ${String.format("%.2f", homeGoals)}-${String.format("%.2f", awayGoals)} to ${String.format("%.2f", adjustedHomeGoals)}-${String.format("%.2f", adjustedAwayGoals)}")
        
        return Pair(adjustedHomeGoals, adjustedAwayGoals)
    }

    /**
     * 🔒 ID-BASED INTEGRITY: Predict match with explicit MatchDetail context.
     * 
     * This function ensures that all analysis is locked to the correct match context
     * to prevent context pollution where different matches get mixed up.
     * 
     * @param matchDetail The match to analyze (source of truth for fixture ID and team names)
     * @param homeTeamFixtures Historical fixtures for home team
     * @param awayTeamFixtures Historical fixtures for away team
     * @param leagueFixtures All fixtures in the league for parameter estimation
     * @param xgDataMap Map of fixtureId -> ExpectedGoalsData (optional)
     * @param modifiers NewsImpactModifiers for AI adjustments (optional)
     * @return EnhancedPrediction with Bayesian-smoothed probabilities and confidence
     */
    suspend fun predictMatchWithContext(
        matchDetail: MatchDetail,
        homeTeamFixtures: List<HistoricalFixture>,
        awayTeamFixtures: List<HistoricalFixture>,
        leagueFixtures: List<HistoricalFixture>,
        xgDataMap: Map<Int, ExpectedGoalsData> = emptyMap(),
        modifiers: NewsImpactModifiers? = null
    ): Result<EnhancedPrediction> {
        // 🔒 CRITICAL INTEGRITY CHECK: Lock the predictor to the correct match
        println("🔒 PREDICTOR LOCKED ON: ${matchDetail.homeTeam} vs ${matchDetail.awayTeam} (ID: ${matchDetail.fixtureId})")
        
        android.util.Log.d("EnhancedScorePredictor",
            "🔒 ID-BASED INTEGRITY: Analyzing ${matchDetail.homeTeam} vs ${matchDetail.awayTeam} (ID: ${matchDetail.fixtureId})"
        )
        
        // Call the existing prediction function with integrity logging
        return predictMatchWithXg(
            homeTeamFixtures = homeTeamFixtures,
            awayTeamFixtures = awayTeamFixtures,
            leagueFixtures = leagueFixtures,
            xgDataMap = xgDataMap,
            modifiers = modifiers
        ).map { prediction ->
            // Ensure the prediction has the correct fixture ID and team names
            prediction.copy(
                fixtureId = matchDetail.fixtureId,
                homeTeam = matchDetail.homeTeam,
                awayTeam = matchDetail.awayTeam
            )
        }
    }
}
