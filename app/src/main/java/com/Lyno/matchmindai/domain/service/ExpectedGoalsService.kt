package com.Lyno.matchmindai.domain.service

import com.Lyno.matchmindai.domain.model.ExpectedGoalsData
import com.Lyno.matchmindai.domain.model.HistoricalFixture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.exp
import kotlin.math.ln
import android.util.Log

/**
 * Service for processing Expected Goals (xG) data for Dixon-Coles model.
 * 
 * This service implements Phase 1 of MatchMind AI 2.0:
 * - Replaces actual goals with xG data for Dixon-Coles input
 * - Provides fallback strategies when xG data is unavailable
 * - Calculates time-decay weighted averages for team strengths
 */
class ExpectedGoalsService {

    companion object {
        // Time decay parameters (adaptive based on data availability)
        private const val DEFAULT_HALF_LIFE_DAYS = 60.0 // 2 months for better form tracking
        private const val MIN_HALF_LIFE_DAYS = 30.0    // Minimum for recent form
        private const val MAX_HALF_LIFE_DAYS = 180.0   // Maximum 6 months (was 365 - too long)
        
        // xG data quality thresholds
        private const val HIGH_QUALITY_THRESHOLD = 0.8
        private const val MEDIUM_QUALITY_THRESHOLD = 0.5
        
        // Fallback parameters
        private const val SHOTS_TO_XG_RATIO = 3.0      // Shots on target / 3 = rough xG
        private const val XG_SMOOTHING_FACTOR = 0.7    // Weight for xG vs actual goals
        
        // 🔥 BAYESIAN SMOOTHING CONSTANTS
        // "Fictieve wedstrijden" die we toevoegen om extremen te dempen
        // Hoe hoger C, hoe meer het model naar het competitiegemiddelde trekt
        private const val C_SKEPTICISM = 5.0 
        private const val GLOBAL_LEAGUE_AVG_GOALS = 1.35 // Gemiddelde goals per team per match
        
    // Data source quality penalties (explicit as requested)
    private const val XG_SOURCE_PENALTY = 1.0      // Full weight for xG data
    private const val XG_PARTIAL_SOURCE_PENALTY = 0.9 // 10% penalty for partial xG data
    private const val SHOTS_SOURCE_PENALTY = 0.8   // 20% penalty for shots data
    private const val GOALS_SOURCE_PENALTY = 0.6   // 40% penalty for goals fallback (as requested)
    private const val GOALS_FALLBACK_PENALTY = 0.4 // 60% penalty for goals without any xG
    }

    /**
     * Get weighted historical data for a team with xG integration.
     * 
     * @param teamFixtures Historical fixtures for the team
     * @param xgDataMap Map of fixtureId -> ExpectedGoalsData
     * @param isHomeTeam Whether this is the home team
     * @return List of weighted fixture data with xG-adjusted scores
     */
    suspend fun getWeightedTeamData(
        teamFixtures: List<HistoricalFixture>,
        xgDataMap: Map<Int, ExpectedGoalsData>,
        isHomeTeam: Boolean
    ): List<WeightedFixtureData> = withContext(Dispatchers.Default) {
        val currentTime = System.currentTimeMillis()
        
        teamFixtures.mapNotNull { fixture ->
            val xgData = xgDataMap[fixture.fixtureId]
            val (inputScore, dataSource) = getInputScoreForFixture(fixture, xgData, isHomeTeam)
            
            // Calculate time decay weight
            val daysAgo = getDaysSinceFixture(fixture.date, currentTime)
            val halfLife = calculateAdaptiveHalfLife(teamFixtures.size, xgData)
            val weight = calculateTimeDecayWeight(daysAgo, halfLife)
            
            // Calculate data quality weight with explicit source penalties
            val baseDataQuality = xgData?.dataQuality ?: 0.3
            val sourceQualityMultiplier = when (dataSource) {
                "XG" -> XG_SOURCE_PENALTY
                "XG_PARTIAL" -> XG_PARTIAL_SOURCE_PENALTY
                "SHOTS" -> SHOTS_SOURCE_PENALTY
                "GOALS" -> GOALS_SOURCE_PENALTY
                "GOALS_FALLBACK" -> GOALS_FALLBACK_PENALTY
                else -> 0.5
            }
            
            // Apply source penalty directly to the weight (as requested: 0.6x for GOALS)
            // Don't double-penalize by also multiplying dataQuality
            val dataQuality = baseDataQuality
            
            val qualityWeight = calculateQualityWeight(dataQuality)
            
            // Combined weight: time decay * quality weight * source penalty
            val combinedWeight = weight * qualityWeight * sourceQualityMultiplier
            
            // Log penalty details for debugging
            android.util.Log.d("ExpectedGoalsService",
                "Data Source Penalty Applied: " +
                "source=$dataSource, " +
                "penalty=${String.format("%.1f", sourceQualityMultiplier)}x, " +
                "finalWeight=${String.format("%.3f", combinedWeight)} " +
                "(timeWeight=${String.format("%.3f", weight)}, qualityWeight=${String.format("%.3f", qualityWeight)})"
            )
            
            // Log data source for debugging
            android.util.Log.d("ExpectedGoalsService",
                "Fixture ${fixture.fixtureId}: " +
                "dataSource=$dataSource, " +
                "inputScore=${String.format("%.2f", inputScore)}, " +
                "baseQuality=${String.format("%.2f", baseDataQuality)}, " +
                "sourceMultiplier=${String.format("%.2f", sourceQualityMultiplier)}, " +
                "finalQuality=${String.format("%.2f", dataQuality)}"
            )
            
            WeightedFixtureData(
                fixture = fixture,
                xgData = xgData,
                inputScore = inputScore,
                weight = combinedWeight,
                dataQuality = dataQuality,
                hasXgData = xgData != null,
                dataSource = dataSource
            )
        }
    }

    /**
     * Calculate input score for Dixon-Coles model using xG data when available.
     * Implements the formula: InputScore = (0.7 × xG) + (0.3 × Goals)
     * Falls back to shots on target / 3 when xG unavailable.
     * 
     * @return Pair of (inputScore, dataSource) where dataSource indicates which metric was used
     */
    private fun getInputScoreForFixture(
        fixture: HistoricalFixture,
        xgData: ExpectedGoalsData?,
        isHomeTeam: Boolean
    ): Pair<Double, String> {
        return if (xgData != null) {
            // Use xG data with smoothing and get data source directly from the method
            val (homeXgAdjusted, awayXgAdjusted, dataSource) = 
                xgData.getInputScoreWithSource(fixture.homeGoals, fixture.awayGoals)
            
            val score = if (isHomeTeam) {
                // Home team's attacking performance
                homeXgAdjusted
            } else {
                // Away team's attacking performance
                awayXgAdjusted
            }
            
            Pair(score, dataSource)
        } else {
            // Fallback: Try to use shots data if available in fixture (not currently available)
            // For now, use actual goals but with lower weight/confidence
            val score = if (isHomeTeam) {
                fixture.homeGoals.toDouble()
            } else {
                fixture.awayGoals.toDouble()
            }
            
            // Log warning for data quality
            android.util.Log.w("ExpectedGoalsService", 
                "Using actual goals fallback for fixture ${fixture.fixtureId}: " +
                "No xG data available. Score variance may be inflated."
            )
            
            Pair(score, "GOALS_FALLBACK")
        }
    }

    /**
     * Calculate adaptive time decay half-life based on overall data quality.
     * Ensures 1-year-old matches are not weighted as heavily as yesterday's match.
     * 
     * Key improvements:
     * 1. Shorter default half-life (60 days instead of 90)
     * 2. Maximum half-life capped at 180 days (6 months, not 1 year)
     * 3. More aggressive decay for old matches
     */
    private fun calculateAdaptiveHalfLife(
        fixtureCount: Int,
        xgData: ExpectedGoalsData?
    ): Double {
        var halfLife = DEFAULT_HALF_LIFE_DAYS
        
        // Adjust based on data quality - use average quality if available
        // For now, use the xgData quality if available, otherwise assume medium
        val dataQuality = xgData?.dataQuality ?: MEDIUM_QUALITY_THRESHOLD
        
        when {
            dataQuality >= HIGH_QUALITY_THRESHOLD -> {
                // High quality xG data: shorter half-life for better form tracking
                halfLife *= 0.6  // More aggressive than before (was 0.7)
            }
            dataQuality >= MEDIUM_QUALITY_THRESHOLD -> {
                // Medium quality: standard half-life
                halfLife *= 0.9  // Slightly shorter than before (was 1.0)
            }
            else -> {
                // Low quality: longer half-life to reduce noise, but not too long
                halfLife *= 1.2  // Less aggressive than before (was 1.3)
            }
        }
        
        // Adjust based on fixture count (more data = shorter half-life)
        when {
            fixtureCount > 40 -> halfLife *= 0.7  // Lots of data, track form closely
            fixtureCount > 20 -> halfLife *= 0.8  // Good amount of data
            fixtureCount < 5 -> halfLife *= 1.8   // Very limited data, need longer memory
            fixtureCount < 10 -> halfLife *= 1.4  // Limited data
        }
        
        // Ensure old matches (>180 days) get very low weight
        // With halfLife = 60 days, a 365-day-old match gets weight = exp(-ln(2)*365/60) = 0.015
        // With halfLife = 180 days, a 365-day-old match gets weight = exp(-ln(2)*365/180) = 0.25 (too high!)
        // So we cap at 180 days maximum
        
        val finalHalfLife = halfLife.coerceIn(MIN_HALF_LIFE_DAYS, MAX_HALF_LIFE_DAYS)
        
        // Log for debugging
        android.util.Log.d("ExpectedGoalsService",
            "Time Decay Half-Life: " +
            "fixtureCount=$fixtureCount, " +
            "dataQuality=${String.format("%.2f", dataQuality)}, " +
            "finalHalfLife=${String.format("%.1f", finalHalfLife)} days, " +
            "365-day-weight=${String.format("%.3f", exp(-ln(2.0) * 365.0 / finalHalfLife))}"
        )
        
        return finalHalfLife
    }

    /**
     * Calculate time decay weight using exponential decay.
     */
    private fun calculateTimeDecayWeight(daysAgo: Double, halfLife: Double): Double {
        return exp(-ln(2.0) * daysAgo / halfLife)
    }

    /**
     * Calculate quality weight based on data quality score.
     */
    private fun calculateQualityWeight(dataQuality: Double): Double {
        return when {
            dataQuality >= HIGH_QUALITY_THRESHOLD -> 1.0
            dataQuality >= MEDIUM_QUALITY_THRESHOLD -> 0.8
            else -> 0.5
        }
    }

    /**
     * Get days since fixture date.
     */
    private fun getDaysSinceFixture(dateStr: String, currentTime: Long): Double {
        return try {
            // Parse date string (assuming ISO format: YYYY-MM-DD)
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd")
            val fixtureDate = dateFormat.parse(dateStr)
            val daysAgo = if (fixtureDate != null) {
                (currentTime - fixtureDate.time) / (1000.0 * 60 * 60 * 24)
            } else {
                180.0
            }
            daysAgo.coerceAtLeast(0.0)
        } catch (e: Exception) {
            // Default to 180 days if parsing fails
            180.0
        }
    }

    /**
     * Calculate team attack and defense parameters using xG-weighted data.
     * Enhanced with team quality factors for top teams.
     * 
     * 🔥 IMPLEMENTATIE BAYESIAN SMOOTHING:
     * Voorkomt "Small Sample Overconfidence" (Fulham 4-0 probleem)
     * Formule: (GescoordGoals + (C * Gemiddelde)) / (AantalMatches + C)
     */
    fun calculateTeamParameters(
        weightedFixtures: List<WeightedFixtureData>,
        leagueAverageGoals: Double,
        isHomeTeam: Boolean
    ): Pair<Double, Double> {
        if (weightedFixtures.isEmpty()) {
            return Pair(0.0, 0.0)
        }

        // Calculate weighted average input scores
        val totalWeight = weightedFixtures.sumOf { it.weight }
        if (totalWeight <= 0.0) {
            return Pair(0.0, 0.0)
        }

        val avgGoalsScored = weightedFixtures.sumOf { it.inputScore * it.weight } / totalWeight
        val avgGoalsConceded = calculateAverageGoalsConceded(weightedFixtures, isHomeTeam)

        // 🔥 BAYESIAN SMOOTHING: Voorkomt overconfidence bij kleine samples
        // Voegt C_SKEPTICISM "gemiddelde" wedstrijden toe aan de dataset
        val smoothedGoalsScored = (avgGoalsScored * totalWeight + C_SKEPTICISM * GLOBAL_LEAGUE_AVG_GOALS) / (totalWeight + C_SKEPTICISM)
        val smoothedGoalsConceded = (avgGoalsConceded * totalWeight + C_SKEPTICISM * GLOBAL_LEAGUE_AVG_GOALS) / (totalWeight + C_SKEPTICISM)

        // Calculate ratios with protection against division by zero
        var attackRatio = (smoothedGoalsScored / leagueAverageGoals).coerceAtLeast(0.1)
        var defenseRatio = (smoothedGoalsConceded / leagueAverageGoals).coerceAtLeast(0.1)

        // Apply team quality factors for top teams
        val teamQualityFactor = calculateTeamQualityFactor(weightedFixtures, leagueAverageGoals)
        if (teamQualityFactor > 1.0) {
            // Top team: boost attack, improve defense
            attackRatio *= teamQualityFactor
            defenseRatio /= teamQualityFactor
        }

        // Convert to log scale for Dixon-Coles model
        val attackStrength = ln(attackRatio)
        val defenseStrength = ln(defenseRatio)

        // Log voor debugging (zie direct of Fulham getemd is)
        android.util.Log.d("ExpectedGoalsService", 
            "Team Parameters (Home=$isHomeTeam): " +
            "Matches=${weightedFixtures.size}, " +
            "RawAvg=${String.format("%.2f", avgGoalsScored)}->${String.format("%.2f", smoothedGoalsScored)}, " +
            "AttackRatio=${String.format("%.2f", attackRatio)}, " +
            "DefenseRatio=${String.format("%.2f", defenseRatio)}"
        )

        return Pair(attackStrength, defenseStrength)
    }

    /**
     * Calculate average goals conceded for a team.
     */
    private fun calculateAverageGoalsConceded(
        weightedFixtures: List<WeightedFixtureData>,
        isHomeTeam: Boolean
    ): Double {
        val totalWeight = weightedFixtures.sumOf { it.weight }
        
        val weightedConceded = weightedFixtures.sumOf { fixtureData ->
            val (conceded, _) = if (isHomeTeam) {
                // Home team: conceded = away team's input score
                getInputScoreForFixture(fixtureData.fixture, fixtureData.xgData, false)
            } else {
                // Away team: conceded = home team's input score
                getInputScoreForFixture(fixtureData.fixture, fixtureData.xgData, true)
            }
            conceded * fixtureData.weight
        }
        
        return weightedConceded / totalWeight
    }

    /**
     * Calculate league averages using xG data when available.
     * Enhanced to track data source distribution for debugging.
     */
    fun calculateLeagueAverages(
        leagueFixtures: List<HistoricalFixture>,
        xgDataMap: Map<Int, ExpectedGoalsData>
    ): Pair<Double, Double> {
        if (leagueFixtures.isEmpty()) {
            return Pair(1.55, 1.25) // Global professional averages
        }

        var totalHomeInput = 0.0
        var totalAwayInput = 0.0
        var count = 0
        val dataSourceCounts = mutableMapOf<String, Int>()

        leagueFixtures.forEach { fixture ->
            val xgData = xgDataMap[fixture.fixtureId]
            if (xgData != null) {
                val (homeInput, awayInput, dataSource) = 
                    xgData.getInputScoreWithSource(fixture.homeGoals, fixture.awayGoals)
                
                totalHomeInput += homeInput
                totalAwayInput += awayInput
                dataSourceCounts[dataSource] = dataSourceCounts.getOrDefault(dataSource, 0) + 1
            } else {
                // No xG data available
                totalHomeInput += fixture.homeGoals.toDouble()
                totalAwayInput += fixture.awayGoals.toDouble()
                dataSourceCounts["GOALS_FALLBACK"] = dataSourceCounts.getOrDefault("GOALS_FALLBACK", 0) + 1
            }
            count++
        }

        if (count == 0) {
            return Pair(1.55, 1.25)
        }

        val homeAvg = totalHomeInput / count
        val awayAvg = totalAwayInput / count

        // Apply low-scoring bias prevention
        val safeHomeAvg = if (homeAvg < 0.8) (homeAvg + 1.55) / 2 else homeAvg
        val safeAwayAvg = if (awayAvg < 0.8) (awayAvg + 1.25) / 2 else awayAvg

        // Log data source distribution for debugging
        val sourceDistribution = dataSourceCounts.entries.joinToString(", ") { 
            "${it.key}: ${it.value} (${String.format("%.1f", it.value.toDouble() / count * 100)}%)" 
        }
        android.util.Log.d("ExpectedGoalsService",
            "League Averages Data Sources: " +
            "totalFixtures=$count, " +
            "homeAvg=${String.format("%.2f", safeHomeAvg)}, " +
            "awayAvg=${String.format("%.2f", safeAwayAvg)}, " +
            "sources=[$sourceDistribution]"
        )

        return Pair(safeHomeAvg, safeAwayAvg)
    }

    /**
     * Calculate team quality factor based on performance relative to league average.
     * Returns >1.0 for top teams, <1.0 for weak teams, 1.0 for average teams.
     */
    private fun calculateTeamQualityFactor(
        weightedFixtures: List<WeightedFixtureData>,
        leagueAverageGoals: Double
    ): Double {
        if (weightedFixtures.isEmpty() || leagueAverageGoals <= 0) {
            return 1.0
        }

        val totalWeight = weightedFixtures.sumOf { it.weight }
        if (totalWeight <= 0) {
            return 1.0
        }

        // Calculate weighted average goals scored and conceded
        val avgGoalsScored = weightedFixtures.sumOf { it.inputScore * it.weight } / totalWeight
        val avgGoalsConceded = calculateAverageGoalsConceded(weightedFixtures, true) // Use home team perspective

        // Calculate performance ratios
        val attackRatio = avgGoalsScored / leagueAverageGoals
        val defenseRatio = avgGoalsConceded / leagueAverageGoals

        // Calculate net performance (attack - defense)
        val netPerformance = attackRatio - defenseRatio

        // Apply quality factor based on net performance
        return when {
            netPerformance > 0.5 -> 1.2  // Top team (e.g., PSV, Ajax, Feyenoord)
            netPerformance > 0.3 -> 1.1  // Strong team
            netPerformance > 0.1 -> 1.05 // Above average
            netPerformance < -0.3 -> 0.9 // Weak team
            netPerformance < -0.5 -> 0.8 // Very weak team
            else -> 1.0 // Average team
        }
    }

    /**
     * Calculate data quality score for a set of fixtures.
     * Enhanced to account for data source quality (XG > XG_PARTIAL > SHOTS > GOALS > GOALS_FALLBACK).
     * FIXED: Improved quality calculation to prevent low quality scores (< 0.2)
     */
    fun calculateDataQuality(weightedFixtures: List<WeightedFixtureData>): Double {
        if (weightedFixtures.isEmpty()) {
            return 0.0
        }

        val xgDataCount = weightedFixtures.count { it.hasXgData }
        val totalWeight = weightedFixtures.sumOf { it.weight }
        
        // Calculate weighted average data quality with source penalty
        val avgDataQuality = weightedFixtures.sumOf { 
            val sourcePenalty = when (it.dataSource) {
                "XG" -> 1.0
                "XG_PARTIAL" -> 0.9  // Partial xG is still good quality
                "SHOTS" -> 0.8
                "GOALS" -> 0.6
                "GOALS_FALLBACK" -> 0.4
                else -> 0.5
            }
            it.dataQuality * it.weight * sourcePenalty 
        } / totalWeight

        // Combine xG availability with enhanced data quality
        // FIX: Use more balanced weighting to prevent low scores
        val xgCoverage = xgDataCount.toDouble() / weightedFixtures.size
        val combinedQuality = (xgCoverage * 0.4) + (avgDataQuality * 0.6)  // More weight on data quality

        // Apply minimum quality boost for teams with any xG data
        val qualityWithBoost = if (xgDataCount > 0) {
            // Teams with xG data get a minimum quality boost
            combinedQuality.coerceAtLeast(0.5)  // Increased from 0.3 to 0.5
        } else {
            // Even teams without xG data should get a reasonable baseline
            combinedQuality.coerceAtLeast(0.3)  // Added baseline for all teams
        }

        // Calculate raw goals average for logging
        val avgGoals = if (weightedFixtures.isNotEmpty()) {
            weightedFixtures.sumOf { it.inputScore * it.weight } / totalWeight
        } else {
            0.0
        }

        // Try to extract team ID from fixtures (if available)
        val teamId = weightedFixtures.firstOrNull()?.fixture?.let { fixture ->
            // Try to determine if this is home or away team data
            // This is a simplified approach - in practice we'd need to know which team this data is for
            fixture.homeTeamId ?: fixture.awayTeamId ?: 0
        } ?: 0

        // DATA VITALITY LOGGING: Log the quality of the data
        android.util.Log.d("DataTrace", "Stats for Team $teamId: xG_Coverage=${String.format("%.2f", xgCoverage)}, Raw_Goals=${String.format("%.2f", avgGoals)}, Data_Quality=${String.format("%.2f", qualityWithBoost)}")
        
        // Critical: If qualityScore is < 0.2, the model will ignore the data. We need to know this.
        if (qualityWithBoost < 0.2) {
            android.util.Log.w("DataTrace", "⚠️ LOW DATA QUALITY WARNING: Team $teamId has qualityScore=${String.format("%.2f", qualityWithBoost)} (< 0.2). Model may ignore this data.")
        }

        // Log data quality analysis
        android.util.Log.d("ExpectedGoalsService",
            "Data Quality Analysis: " +
            "fixtures=${weightedFixtures.size}, " +
            "xgCoverage=${String.format("%.1f", xgCoverage * 100)}%, " +
            "avgQuality=${String.format("%.2f", avgDataQuality)}, " +
            "combinedQuality=${String.format("%.2f", combinedQuality)}, " +
            "qualityWithBoost=${String.format("%.2f", qualityWithBoost)}"
        )

        return qualityWithBoost.coerceIn(0.0, 1.0)
    }
}

/**
 * Data class for weighted fixture data with xG integration.
 */
data class WeightedFixtureData(
    val fixture: HistoricalFixture,
    val xgData: ExpectedGoalsData?,
    val inputScore: Double,      // xG-adjusted score for Dixon-Coles
    val weight: Double,          // Combined time decay and quality weight
    val dataQuality: Double,     // 0.0 to 1.0
    val hasXgData: Boolean,
    val dataSource: String       // "XG", "SHOTS", "GOALS", "GOALS_FALLBACK"
)
