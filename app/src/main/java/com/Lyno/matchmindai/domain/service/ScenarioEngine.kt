package com.Lyno.matchmindai.domain.service

import com.Lyno.matchmindai.data.templates.ScenarioTemplates
import com.Lyno.matchmindai.domain.model.*
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Enhanced Scenario Engine.
 * Converteert Statistische Waarschijnlijkheden (Dixon-Coles) + AI Context (Chaos) -> Verhalende Scenario's.
 *
 * Veranderingen t.o.v. vorige versie:
 * - Verwijderd: Hardcoded 'Ajax/PSV' team strengths.
 * - Toegevoegd: Data-driven categorie bepaling o.b.v. winstkansen en xG.
 * - Toegevoegd: Dynamische score generatie (geen statische 2-1 meer).
 */
class ScenarioEngine @Inject constructor() {

    /**
     * Genereert 3 unieke scenario's, GEGARANDEERD divers.
     * Voorkomt "100% Gelijkspel" bug door diversiteit af te dwingen.
     */
    fun generateScenarios(
        match: MatchDetail,
        prediction: HybridPrediction
    ): List<MatchScenario> {
        val scenarios = mutableListOf<MatchScenario>()
        val p = prediction.enhancedPrediction 
        val chaosScore = prediction.analysis.chaos_score
        
        val hProb = p.homeWinProbability
        val aProb = p.awayWinProbability
        val dProb = p.drawProbability
        val xGh = p.expectedGoalsHome
        val xGa = p.expectedGoalsAway

        // 1. PRIMAIR SCENARIO (Meest waarschijnlijk)
        val primaryCategory = determinePrimaryCategory(hProb, aProb, dProb, xGh, xGa)
        scenarios.add(createScenario(match, primaryCategory, p, chaosScore, true, false))

        // 2. SECUNDAIR SCENARIO (MOET anders zijn dan primair)
        // Als primair een Home Win is, dwing dan een Draw of Away Win af als alternatief
        val secondaryCategory = getDiverseCategory(primaryCategory, hProb, aProb, dProb, xGh, xGa)
        scenarios.add(createScenario(match, secondaryCategory, p, chaosScore, false, false))

        // 3. DERDE SCENARIO (De 'Hedge' of Chaos)
        // Als we nog geen Draw hebben, voeg die toe. Anders Chaos.
        val hasDraw = scenarios.any { it.predictedScore.contains("1-1") || it.predictedScore.contains("0-0") || it.predictedScore.contains("2-2") }
        val tertiaryCategory = if (!hasDraw && dProb > 0.20) {
            ScenarioCategory.LOW_SCORING_DRAW
        } else if (chaosScore > 50) {
            determineChaosCategory(xGh, xGa, chaosScore)
        } else {
            // Nog een variatie op de winnaar, maar met andere score
            if (hProb > aProb) ScenarioCategory.NARROW_HOME_WIN else ScenarioCategory.AWAY_TEAM_SURPRISE
        }
        
        // Voeg toe als uniek
        val thirdScenario = createScenario(match, tertiaryCategory, p, chaosScore, false, true)
        if (scenarios.none { it.title == thirdScenario.title }) {
            scenarios.add(thirdScenario)
        }

        return scenarios.take(3)
    }

    /**
     * Bepaalt de hoofdcategorie op basis van Dixon-Coles probabilities.
     */
    private fun determinePrimaryCategory(
        hProb: Double, aProb: Double, dProb: Double, xGh: Double, xGa: Double
    ): ScenarioCategory {
        val totalxG = xGh + xGa
        
        return when {
            hProb > 0.65 -> ScenarioCategory.DOMINANT_HOME_WIN
            aProb > 0.60 -> ScenarioCategory.AWAY_TEAM_SURPRISE // Of 'Dominant Away' als die bestaat
            hProb > 0.45 && totalxG < 2.0 -> ScenarioCategory.NARROW_HOME_WIN
            hProb > 0.45 && totalxG >= 3.0 -> ScenarioCategory.GOAL_FEST
            aProb > 0.40 && totalxG > 2.5 -> ScenarioCategory.AWAY_TEAM_SURPRISE
            dProb > 0.35 && totalxG < 1.8 -> ScenarioCategory.LOW_SCORING_DRAW
            dProb > 0.30 && totalxG >= 2.5 -> ScenarioCategory.HIGH_SCORING_DRAW
            hProb > aProb -> ScenarioCategory.NARROW_HOME_WIN // Fallback home favoriet
            else -> ScenarioCategory.DEFENSIVE_BATTLE // Fallback tight game
        }
    }

    /**
     * Helper om diversiteit af te dwingen.
     * Enhanced: Uses xG data to make better decisions about alternative scenarios.
     */
    private fun getDiverseCategory(
        primary: ScenarioCategory, hProb: Double, aProb: Double, dProb: Double, xGh: Double, xGa: Double
    ): ScenarioCategory {
        val totalXg = xGh + xGa
        
        return when (primary) {
            // Als primair 'Dominant Home' is, kijk naar 'Narrow Home' of 'Draw'
            ScenarioCategory.DOMINANT_HOME_WIN -> {
                if (dProb > 0.2 && totalXg < 2.0) ScenarioCategory.LOW_SCORING_DRAW 
                else ScenarioCategory.NARROW_HOME_WIN
            }
            
            // Als primair 'Draw' is, MOETEN we een winnaar kiezen voor scenario 2
            ScenarioCategory.HIGH_SCORING_DRAW, 
            ScenarioCategory.LOW_SCORING_DRAW -> {
                if (hProb > aProb) ScenarioCategory.NARROW_HOME_WIN 
                else ScenarioCategory.AWAY_TEAM_SURPRISE
            }
            
            // Als primair 'Away' is, kies Draw of Home
            ScenarioCategory.AWAY_TEAM_SURPRISE -> {
                if (dProb > 0.25 && totalXg < 2.0) ScenarioCategory.LOW_SCORING_DRAW 
                else ScenarioCategory.NARROW_HOME_WIN
            }
            
            // Voor andere categorieën, kies een logisch alternatief
            ScenarioCategory.NARROW_HOME_WIN -> {
                if (dProb > 0.2 && totalXg < 2.0) ScenarioCategory.LOW_SCORING_DRAW 
                else ScenarioCategory.AWAY_TEAM_SURPRISE
            }
            ScenarioCategory.GOAL_FEST -> ScenarioCategory.HIGH_SCORING_DRAW
            ScenarioCategory.DEFENSIVE_BATTLE -> ScenarioCategory.LOW_SCORING_DRAW
            else -> ScenarioCategory.CONTROVERSIAL_OUTCOME
        }
    }
    
    private fun determineChaosCategory(xGh: Double, xGa: Double, chaos: Int): ScenarioCategory {
        // Use chaos score to determine how chaotic the scenario should be
        return when {
            chaos > 70 -> ScenarioCategory.GOAL_FEST  // High chaos = goal fest
            chaos > 50 && xGh + xGa > 3.0 -> ScenarioCategory.GOAL_FEST  // Medium chaos with high xG
            else -> ScenarioCategory.CONTROVERSIAL_OUTCOME  // Default chaos scenario
        }
    }

    /**
     * Bouwt het daadwerkelijke scenario object.
     * BELANGRIJK: Hier genereren we de score dynamisch via `generateSmartScore`.
     * Enhanced: Uses chaosScore to adjust scenario properties.
     */
    private fun createScenario(
        match: MatchDetail,
        category: ScenarioCategory,
        prediction: MatchPrediction,
        chaosScore: Int,
        isPrimary: Boolean,
        isChaos: Boolean
    ): MatchScenario {
        // Genereer een realistische score voor deze categorie
        // 🔥 SANITY CHECK: Primary scenario stays close to raw prediction
        val score = generateSmartScore(
            category, 
            prediction.expectedGoalsHome, 
            prediction.expectedGoalsAway,
            isPrimary = isPrimary
        )
        
        // Bereken waarschijnlijkheid (Primary krijgt de base probability, de rest een fractie)
        val baseProb = if (isPrimary) {
             maxOf(prediction.homeWinProbability, prediction.awayWinProbability, prediction.drawProbability) * 100
        } else {
             (1.0 - maxOf(prediction.homeWinProbability, prediction.awayWinProbability, prediction.drawProbability)) * 50
        }
        
        // Voeg wat random variatie toe zodat het niet altijd "52%" is
        val probability = (baseProb + Random.nextInt(-5, 5)).toInt().coerceIn(5, 95)

        // Haal tekstuele templates op
        val title = ScenarioTemplates.getScenarioTitle(category, match.homeTeam, match.awayTeam)
        val description = ScenarioTemplates.getScenarioDescription(category, match.homeTeam, match.awayTeam, score)
        val keyFactors = ScenarioTemplates.getKeyFactors(category)

        // Use chaosScore to adjust chaosImpact and confidence
        val adjustedChaosImpact = if (isChaos) {
            // Scale chaos impact based on chaosScore (20-80 range)
            20 + (chaosScore * 0.6).toInt().coerceIn(20, 80)
        } else {
            // Even non-chaos scenarios have some chaos impact based on score
            (chaosScore * 0.2).toInt().coerceIn(5, 30)
        }

        return MatchScenario(
            id = "${match.fixtureId}_${category.name}",
            title = title,
            description = description,
            predictedScore = score,
            probability = probability,
            confidence = (prediction.confidenceScore * (if (isPrimary) 1.0 else 0.8)).toInt(),
            chaosImpact = adjustedChaosImpact,
            atmosphereImpact = 50, // Kan later uit matchDetail komen
            bettingValue = 0.0, // Wordt later ingevuld door Kelly engine
            keyFactors = keyFactors,
            triggerEvents = emptyList(),
            timeline = emptyList(),
            dataSources = listOf("Dixon-Coles", "DeepSeek Analysis"),
            lastUpdated = System.currentTimeMillis()
        )
    }
    
    /**
     * Calculate tournament factor for score adjustment.
     * Tournaments (Cups, Tournaments) are often more defensive/cautious.
     * 
     * @param leagueName The league name to check for tournament context
     * @return Factor (0.85 for tournaments, 1.0 for regular leagues)
     */
    private fun calculateTournamentFactor(leagueName: String): Double {
        return when {
            // Toernooien zijn vaak defensiever/voorzichtiger
            leagueName.contains("Cup", ignoreCase = true) || 
            leagueName.contains("Tournament", ignoreCase = true) -> 0.85 
            // Competities zijn 'normaal'
            else -> 1.0 
        }
    }

    /**
     * 🔥 CORE LOGIC: Genereert een score die past bij de categorie ÉN de xG data.
     * REFACTORED: Multiplicative scaling met tightened variance.
     * 
     * Key changes:
     * 1. Multiplicative scaling: `adjustedXGh * (1.0 + randomFactor)` instead of `adjustedXGh + random`
     * 2. Tightened variance: Random range reduced from (0.5, 1.5) to (0.1, 0.4)
     * 3. Sanity check: Primary scenario stays close to raw prediction
     * 
     * @param category Scenario category
     * @param xGh Expected goals home
     * @param xGa Expected goals away
     * @param leagueName Optional league name for tournament factor adjustment
     * @param isPrimary Whether this is the primary (most likely) scenario
     * @return Generated score string
     */
    private fun generateSmartScore(
        category: ScenarioCategory, 
        xGh: Double, 
        xGa: Double,
        leagueName: String = "",
        isPrimary: Boolean = false
    ): String {
        val r = Random.Default
        
        // Apply tournament factor if league name is provided
        val tournamentFactor = if (leagueName.isNotBlank()) calculateTournamentFactor(leagueName) else 1.0
        val adjustedXGh = xGh * tournamentFactor
        val adjustedXGa = xGa * tournamentFactor
        
        // 🔥 SANITY CHECK: Primary scenario should stay close to raw prediction
        // For primary scenario, use minimal variance (0.0-0.1) instead of full variance
        val baseVariance = if (isPrimary) 0.1 else 0.4
        val minVariance = if (isPrimary) 0.0 else 0.1
        
        // Baseer score op xG, maar duw het in de richting van de categorie
        return when (category) {
            ScenarioCategory.DOMINANT_HOME_WIN -> {
                // 🔥 MULTIPLICATIVE SCALING: Home team gets boost, away team gets penalty
                val homeBoost = 1.0 + r.nextDouble(minVariance, baseVariance) // 0.0-0.1 for primary, 0.1-0.4 for alternate
                val awayPenalty = 0.5 + r.nextDouble(-0.1, 0.1) // Fixed penalty for away team
                val h = kotlin.math.max(2, (adjustedXGh * homeBoost).roundToInt())
                val a = kotlin.math.min(1, (adjustedXGa * awayPenalty).roundToInt())
                "$h-$a"
            }
            ScenarioCategory.NARROW_HOME_WIN -> {
                // 🔥 MULTIPLICATIVE SCALING: Small boost for home, small reduction for away
                val homeBoost = 1.0 + r.nextDouble(0.0, 0.2) // Small boost for narrow win
                val awayPenalty = 0.8 + r.nextDouble(-0.1, 0.1) // Small reduction
                val h = kotlin.math.max(1, (adjustedXGh * homeBoost).roundToInt())
                val a = kotlin.math.max(0, (adjustedXGa * awayPenalty).roundToInt())
                "$h-$a"
            }
            ScenarioCategory.LOW_SCORING_DRAW -> {
                // Anchored to xG: if both teams have low xG, 0-0 is more likely
                val totalXg = adjustedXGh + adjustedXGa
                if (totalXg < 1.5) "0-0" else "1-1"
            }
            ScenarioCategory.HIGH_SCORING_DRAW -> {
                // 🔥 MULTIPLICATIVE SCALING: Both teams get similar boost
                val drawBoost = 1.0 + r.nextDouble(minVariance, baseVariance)
                val baseScore = ((adjustedXGh + adjustedXGa) / 2.0 * drawBoost).roundToInt()
                val score = kotlin.math.max(2, baseScore)
                "$score-$score"
            }
            ScenarioCategory.GOAL_FEST -> {
                // 🔥 MULTIPLICATIVE SCALING: Both teams get boost
                val chaosFactor = 1.0 + r.nextDouble(minVariance, baseVariance)
                val h = kotlin.math.max(2, (adjustedXGh * chaosFactor).roundToInt())
                val a = kotlin.math.max(1, (adjustedXGa * chaosFactor).roundToInt())
                "$h-$a"
            }
            ScenarioCategory.DEFENSIVE_BATTLE -> {
                // Anchored to xG: if both teams have low xG
                if (adjustedXGh < 1.0 && adjustedXGa < 1.0) "0-0" else "1-0"
            }
            ScenarioCategory.AWAY_TEAM_SURPRISE -> {
                // 🔥 MULTIPLICATIVE SCALING: Away team gets boost, home team gets penalty
                val awayBoost = 1.0 + r.nextDouble(minVariance, baseVariance)
                val homePenalty = 0.7 + r.nextDouble(-0.1, 0.1)
                val a = kotlin.math.max(1, (adjustedXGa * awayBoost).roundToInt())
                val h = kotlin.math.max(0, (adjustedXGh * homePenalty).roundToInt())
                "$h-$a"
            }
            else -> {
                // 🔥 SANITY CHECK: Primary scenario should be closest to raw xG
                // For primary, use raw xG; for alternate, add small variance
                if (isPrimary) {
                    "${adjustedXGh.roundToInt()}-${adjustedXGa.roundToInt()}"
                } else {
                    val variance = r.nextDouble(-0.2, 0.2)
                    val h = kotlin.math.max(0, (adjustedXGh * (1.0 + variance)).roundToInt())
                    val a = kotlin.math.max(0, (adjustedXGa * (1.0 - variance)).roundToInt())
                    "$h-$a"
                }
            }
        }
    }
    
    private fun maxOf(a: Double, b: Double, c: Double): Double = 
        kotlin.math.max(a, kotlin.math.max(b, c))
}
