package com.Lyno.matchmindai.domain.model

import kotlinx.serialization.Serializable

/**
 * Performance Analysis Dashboard domain model for Project Kaptigun.
 * Contains three distinct sections for deep tactical analysis:
 * 1. Head-to-Head duels with xG and performance labels
 * 2. Recent form comparison with efficiency icons
 * 3. Deep stats comparison with bar charts and sentiment scores
 */
@Serializable
data class KaptigunAnalysis(
    val fixtureId: Int,
    val homeTeamId: Int,
    val awayTeamId: Int,
    val homeTeamName: String,
    val awayTeamName: String,
    val leagueId: Int,
    val leagueName: String,
    val headToHead: List<HeadToHeadDuel>,
    val homeRecentForm: TeamRecentForm,
    val awayRecentForm: TeamRecentForm,
    val deepStats: DeepStatsComparison,
    val lastUpdated: String
) {
    /**
     * Check if analysis has sufficient data for display.
     */
    val hasSufficientData: Boolean
        get() = headToHead.isNotEmpty() || 
                homeRecentForm.matches.isNotEmpty() || 
                awayRecentForm.matches.isNotEmpty()

    /**
     * Get overall analysis confidence based on data completeness.
     */
    val confidence: Int
        get() {
            var score = 0
            if (headToHead.isNotEmpty()) score += 30
            if (homeRecentForm.matches.isNotEmpty()) score += 20
            if (awayRecentForm.matches.isNotEmpty()) score += 20
            if (deepStats.hasValidData) score += 30
            return score
        }

    /**
     * Get summary of key insights.
     */
    val summary: String
        get() = buildString {
            if (headToHead.isNotEmpty()) {
                val dominantTeam = getDominantTeamFromH2H()
                if (dominantTeam != null) {
                    appendLine("$dominantTeam domineert in onderlinge duels")
                }
            }
            
            val homeFormStrength = homeRecentForm.getFormStrength()
            val awayFormStrength = awayRecentForm.getFormStrength()
            if (homeFormStrength > awayFormStrength + 10) {
                appendLine("${homeTeamName} in betere vorm dan ${awayTeamName}")
            } else if (awayFormStrength > homeFormStrength + 10) {
                appendLine("${awayTeamName} in betere vorm dan ${homeTeamName}")
            }
            
            if (deepStats.hasValidData) {
                val xgDominance = deepStats.getXgDominance()
                if (xgDominance != null) {
                    appendLine("Statistisch voordeel voor $xgDominance")
                }
            }
        }
    
    private fun getDominantTeamFromH2H(): String? {
        val homeWins = headToHead.count { it.isHomeWin(homeTeamName) }
        val awayWins = headToHead.count { it.isAwayWin(awayTeamName) }
        
        return when {
            homeWins > awayWins + 1 -> homeTeamName
            awayWins > homeWins + 1 -> awayTeamName
            else -> null
        }
    }
}

/**
 * Head-to-head duel between two teams with xG and performance analysis.
 */
@Serializable
data class HeadToHeadDuel(
    val fixtureId: Int,
    val date: String,
    val homeTeam: String,
    val awayTeam: String,
    val homeScore: Int,
    val awayScore: Int,
    val homeXg: Double,
    val awayXg: Double,
    val homeShotsOnTarget: Int? = null,
    val awayShotsOnTarget: Int? = null,
    val homeTotalShots: Int? = null,
    val awayTotalShots: Int? = null,
    val homePossession: Double? = null,
    val awayPossession: Double? = null
) {
    /**
     * Performance label based on score vs xG analysis.
     * Implements Tesseract Twists logic:
     * - Win + (xG Diff > 0.3) -> DOMINANT
     * - Win + (xG Diff < -0.3) -> LUCKY
     * - Loss + (xG Diff > 0.5) -> UNLUCKY
     * - Else -> NEUTRAL
     */
    val performanceLabel: PerformanceLabel
        get() {
            val isHomeWin = homeScore > awayScore
            val isAwayWin = awayScore > homeScore
            val xgDifference = homeXg - awayXg
            
            return when {
                // Home team wins
                isHomeWin && xgDifference > 0.3 -> PerformanceLabel.DOMINANT
                isHomeWin && xgDifference < -0.3 -> PerformanceLabel.LUCKY
                // Away team wins (note: xgDifference is homeXg - awayXg, so negative means away has higher xG)
                isAwayWin && xgDifference < -0.3 -> PerformanceLabel.DOMINANT
                isAwayWin && xgDifference > 0.3 -> PerformanceLabel.LUCKY
                // Loss with higher xG (home team loses but had better xG)
                !isHomeWin && isAwayWin && xgDifference > 0.5 -> PerformanceLabel.UNLUCKY
                // Loss with higher xG (away team loses but had better xG)
                isHomeWin && !isAwayWin && xgDifference < -0.5 -> PerformanceLabel.UNLUCKY
                else -> PerformanceLabel.NEUTRAL
            }
        }

    /**
     * Get score display string.
     */
    val scoreDisplay: String
        get() = "$homeScore-$awayScore"

    /**
     * Get xG display string.
     */
    val xgDisplay: String
        get() = "${String.format("%.1f", homeXg)}-${String.format("%.1f", awayXg)}"

    /**
     * Check if home team won this duel.
     */
    fun isHomeWin(teamName: String): Boolean = homeTeam == teamName && homeScore > awayScore

    /**
     * Check if away team won this duel.
     */
    fun isAwayWin(teamName: String): Boolean = awayTeam == teamName && awayScore > homeScore

    /**
     * Get possession display string.
     */
    val possessionDisplay: String
        get() = homePossession?.let { "${it.toInt()}%-${100 - it.toInt()}%" } ?: "N/A"
}

/**
 * Performance label for head-to-head duels.
 */
@Serializable
enum class PerformanceLabel {
    DOMINANT,    // Win with higher xG (deserved win)
    LUCKY,       // Win with lower xG (lucky win)
    UNLUCKY,     // Loss or draw with higher xG (unlucky)
    NEUTRAL;     // Balanced performance

    /**
     * Get display color for UI.
     */
    val color: String
        get() = when (this) {
            DOMINANT -> "#4CAF50" // Green
            LUCKY -> "#FF9800"    // Orange
            UNLUCKY -> "#F44336"  // Red
            NEUTRAL -> "#9E9E9E"  // Gray
        }

    /**
     * Get display text in Dutch.
     */
    val displayText: String
        get() = when (this) {
            DOMINANT -> "Dominant"
            LUCKY -> "Gelukkig"
            UNLUCKY -> "Ongelukkig"
            NEUTRAL -> "Neutraal"
        }
}

/**
 * Recent form analysis for a team.
 */
@Serializable
data class TeamRecentForm(
    val teamId: Int,
    val teamName: String,
    val matches: List<FormMatch>,
    val results: List<MatchResult>
) {
    /**
     * Get form streak (last 5 results).
     */
    val streak: List<MatchResult>
        get() = results.takeLast(5)

    /**
     * Get form strength score (0-100).
     */
    fun getFormStrength(): Int {
        if (matches.isEmpty()) return 50
        
        val recentMatches = matches.takeLast(5)
        var score = 50
        
        recentMatches.forEach { match ->
            when (match.result) {
                MatchResult.WIN -> score += 10
                MatchResult.DRAW -> score += 5
                MatchResult.LOSS -> score -= 10
            }
            
            // Bonus for efficiency
            when (match.efficiencyIcon) {
                EfficiencyIcon.CLINICAL -> score += 5
                EfficiencyIcon.INEFFICIENT -> score -= 5
                else -> {}
            }
        }
        
        return score.coerceIn(0, 100)
    }

    /**
     * Get average xG from recent matches.
     */
    val averageXg: Double
        get() = if (matches.isNotEmpty()) {
            matches.takeLast(5).map { it.xg }.average()
        } else {
            0.0
        }

    /**
     * Get average goals from recent matches.
     */
    val averageGoals: Double
        get() = if (matches.isNotEmpty()) {
            matches.takeLast(5).map { it.goals }.average()
        } else {
            0.0
        }
}

/**
 * Individual match in recent form analysis.
 */
@Serializable
data class FormMatch(
    val fixtureId: Int,
    val opponent: String,
    val isHome: Boolean,
    val goals: Int,
    val opponentGoals: Int,
    val xg: Double,
    val opponentXg: Double,
    val shotsOnTarget: Int? = null,
    val totalShots: Int? = null
) {
    /**
     * Match result.
     */
    val result: MatchResult
        get() = when {
            goals > opponentGoals -> MatchResult.WIN
            goals < opponentGoals -> MatchResult.LOSS
            else -> MatchResult.DRAW
        }

    /**
     * Efficiency icon based on goals vs xG.
     */
    val efficiencyIcon: EfficiencyIcon
        get() {
            val goalDifference = goals - xg
            return when {
                goalDifference > 0.5 -> EfficiencyIcon.CLINICAL
                goalDifference < -0.5 -> EfficiencyIcon.INEFFICIENT
                else -> EfficiencyIcon.BALANCED
            }
        }

    /**
     * Get score display string.
     */
    val scoreDisplay: String
        get() = if (isHome) "$goals-$opponentGoals" else "$opponentGoals-$goals"

    /**
     * Get xG difference display.
     */
    val xgDiffDisplay: String
        get() = String.format("%+.1f", xg - opponentXg)
}

/**
 * Match result type.
 */
@Serializable
enum class MatchResult {
    WIN, DRAW, LOSS;

    /**
     * Get display color for UI.
     */
    val color: String
        get() = when (this) {
            WIN -> "#4CAF50"   // Green
            DRAW -> "#FF9800"  // Orange
            LOSS -> "#F44336"  // Red
        }

    /**
     * Get display text in Dutch.
     */
    val displayText: String
        get() = when (this) {
            WIN -> "W"
            DRAW -> "G"
            LOSS -> "V"
        }
}

/**
 * Efficiency icon for form matches.
 */
@Serializable
enum class EfficiencyIcon {
    CLINICAL,    // Goals > xG + 0.5 (efficient finishing)
    INEFFICIENT, // Goals < xG - 0.5 (wasteful finishing)
    BALANCED;    // Goals ≈ xG (balanced)

    /**
     * Get display symbol.
     */
    val symbol: String
        get() = when (this) {
            CLINICAL -> "↑"
            INEFFICIENT -> "↓"
            BALANCED -> "→"
        }

    /**
     * Get display text in Dutch.
     */
    val displayText: String
        get() = when (this) {
            CLINICAL -> "Klinisch"
            INEFFICIENT -> "Inefficiënt"
            BALANCED -> "Gebalanceerd"
        }

    /**
     * Get display color for UI.
     */
    val color: String
        get() = when (this) {
            CLINICAL -> "#4CAF50"   // Green
            INEFFICIENT -> "#F44336" // Red
            BALANCED -> "#FF9800"   // Orange
        }
}

/**
 * Sentiment mood for team analysis.
 * Maps sentiment scores to Dutch mood labels.
 */
@Serializable
enum class SentimentMood {
    EUFORIE,        // Score >= 0.7 (Euphoria)
    OPTIMISTISCH,   // Score >= 0.3 (Optimistic)
    NEUTRAAL,       // Score >= -0.3 (Neutral)
    ZORGEN,         // Score >= -0.7 (Concern)
    CHAOS;          // Score < -0.7 (Chaos)

    /**
     * Get display color for UI.
     */
    val color: String
        get() = when (this) {
            EUFORIE -> "#4CAF50"       // Green
            OPTIMISTISCH -> "#8BC34A"  // Light Green
            NEUTRAAL -> "#FF9800"      // Orange
            ZORGEN -> "#F44336"        // Red
            CHAOS -> "#9C27B0"         // Purple
        }

    /**
     * Get display text in Dutch.
     */
    val displayText: String
        get() = when (this) {
            EUFORIE -> "Euforie"
            OPTIMISTISCH -> "Optimistisch"
            NEUTRAAL -> "Neutraal"
            ZORGEN -> "Zorgen"
            CHAOS -> "Chaos"
        }

    companion object {
        /**
         * Convert sentiment score to mood.
         * @param score Sentiment score between -1.0 and +1.0
         */
        fun fromScore(score: Double): SentimentMood {
            return when {
                score >= 0.7 -> EUFORIE
                score >= 0.3 -> OPTIMISTISCH
                score >= -0.3 -> NEUTRAAL
                score >= -0.7 -> ZORGEN
                else -> CHAOS
            }
        }
    }
}

/**
 * Team match statistics for individual matches.
 * Used for calculating averages in DeepStatsComparison.
 */
@Serializable
data class TeamMatchStats(
    val fixtureId: Int,
    val teamId: Int,
    val xg: Double,
    val possession: Double,
    val shotsOnTarget: Int,
    val totalShots: Int,
    val passes: Int,
    val defensiveActions: Int
) {
    /**
     * Calculate PPDA (Passes Per Defensive Action).
     */
    val ppda: Double
        get() = if (defensiveActions > 0) {
            passes.toDouble() / defensiveActions
        } else {
            15.0 // Default value
        }

    /**
     * Calculate expected goals if not provided.
     */
    fun calculateExpectedGoals(): Double {
        val shotsOffTarget = totalShots - shotsOnTarget
        return (shotsOnTarget * 0.3) + (shotsOffTarget * 0.07)
    }
}

/**
 * Deep statistics comparison between two teams.
 */
@Serializable
data class DeepStatsComparison(
    val avgXg: Pair<Double, Double>,           // Home vs Away average xG
    val avgPossession: Pair<Double, Double>,   // Home vs Away average possession %
    val avgShotsOnTarget: Pair<Double, Double>, // Home vs Away average shots on target
    val ppda: Pair<Double, Double>,            // Home vs Away PPDA (Passes Per Defensive Action)
    val sentimentScore: Pair<Double, Double>   // Home vs Away sentiment (-1.0 to +1.0)
) {
    /**
     * Check if data is valid for display.
     */
    val hasValidData: Boolean
        get() = avgXg.first > 0.0 || avgXg.second > 0.0

    /**
     * Get xG dominance indicator.
     */
    fun getXgDominance(): String? {
        val xgDifference = avgXg.first - avgXg.second
        return when {
            xgDifference > 0.3 -> "thuisploeg"
            xgDifference < -0.3 -> "uitploeg"
            else -> null
        }
    }

    /**
     * Get possession dominance indicator.
     */
    fun getPossessionDominance(): String? {
        val possessionDifference = avgPossession.first - avgPossession.second
        return when {
            possessionDifference > 5.0 -> "thuisploeg"
            possessionDifference < -5.0 -> "uitploeg"
            else -> null
        }
    }

    /**
     * Get pressing intensity indicator (lower PPDA = better pressing).
     */
    fun getPressingIntensity(): String? {
        val ppdaDifference = ppda.first - ppda.second
        return when {
            ppdaDifference < -2.0 -> "thuisploeg"
            ppdaDifference > 2.0 -> "uitploeg"
            else -> null
        }
    }

    /**
     * Get sentiment mood indicator.
     */
    fun getSentimentMood(): Pair<SentimentMood, SentimentMood> {
        val homeMood = SentimentMood.fromScore(sentimentScore.first)
        val awayMood = SentimentMood.fromScore(sentimentScore.second)
        return Pair(homeMood, awayMood)
    }

    /**
     * Get all metrics as list for easy iteration in UI.
     */
    val metrics: List<Pair<String, Pair<Double, Double>>>
        get() = listOf(
            "xG" to avgXg,
            "Balbezit" to avgPossession,
            "Schoten op doel" to avgShotsOnTarget,
            "PPDA" to ppda,
            "Sentiment" to sentimentScore
        )
}

/**
 * Companion object with utility functions.
 */
object KaptigunAnalysisUtils {
    /**
     * Creates an empty KaptigunAnalysis for loading/error states.
     */
    fun empty(
        fixtureId: Int,
        homeTeamId: Int,
        awayTeamId: Int,
        homeTeamName: String,
        awayTeamName: String,
        leagueId: Int,
        leagueName: String
    ): KaptigunAnalysis {
        return KaptigunAnalysis(
            fixtureId = fixtureId,
            homeTeamId = homeTeamId,
            awayTeamId = awayTeamId,
            homeTeamName = homeTeamName,
            awayTeamName = awayTeamName,
            leagueId = leagueId,
            leagueName = leagueName,
            headToHead = emptyList(),
            homeRecentForm = TeamRecentForm(
                teamId = homeTeamId,
                teamName = homeTeamName,
                matches = emptyList(),
                results = emptyList()
            ),
            awayRecentForm = TeamRecentForm(
                teamId = awayTeamId,
                teamName = awayTeamName,
                matches = emptyList(),
                results = emptyList()
            ),
            deepStats = DeepStatsComparison(
                avgXg = Pair(0.0, 0.0),
                avgPossession = Pair(50.0, 50.0),
                avgShotsOnTarget = Pair(0.0, 0.0),
                ppda = Pair(15.0, 15.0),
                sentimentScore = Pair(0.0, 0.0)
            ),
            lastUpdated = ""
        )
    }

    /**
     * Creates a KaptigunAnalysis with "no data available" message.
     */
    fun noDataAvailable(
        fixtureId: Int,
        homeTeamId: Int,
        awayTeamId: Int,
        homeTeamName: String,
        awayTeamName: String,
        leagueId: Int,
        leagueName: String
    ): KaptigunAnalysis {
        return KaptigunAnalysis(
            fixtureId = fixtureId,
            homeTeamId = homeTeamId,
            awayTeamId = awayTeamId,
            homeTeamName = homeTeamName,
            awayTeamName = awayTeamName,
            leagueId = leagueId,
            leagueName = leagueName,
            headToHead = emptyList(),
            homeRecentForm = TeamRecentForm(
                teamId = homeTeamId,
                teamName = homeTeamName,
                matches = emptyList(),
                results = emptyList()
            ),
            awayRecentForm = TeamRecentForm(
                teamId = awayTeamId,
                teamName = awayTeamName,
                matches = emptyList(),
                results = emptyList()
            ),
            deepStats = DeepStatsComparison(
                avgXg = Pair(0.0, 0.0),
                avgPossession = Pair(50.0, 50.0),
                avgShotsOnTarget = Pair(0.0, 0.0),
                ppda = Pair(15.0, 15.0),
                sentimentScore = Pair(0.0, 0.0)
            ),
            lastUpdated = "Geen data beschikbaar"
        )
    }
}
