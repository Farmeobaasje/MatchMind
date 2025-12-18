package com.Lyno.matchmindai.domain.model

import kotlinx.serialization.Serializable

/**
 * Domain model for match prediction data from API-SPORTS.
 * Contains comprehensive prediction information including win probabilities,
 * expected goals, and analysis.
 */
@Serializable
data class MatchPredictionData(
    val fixtureId: Int,
    val homeTeam: String,
    val awayTeam: String,
    val league: String,
    val primaryPrediction: String,
    val winningPercent: WinningPercent,
    val expectedGoals: ExpectedGoals,
    val analysis: String? = null,
    val lastUpdated: String = ""
) {
    /**
     * Get the most likely outcome.
     */
    val mostLikelyOutcome: String
        get() = primaryPrediction

    /**
     * Get the confidence level for the prediction.
     */
    val confidenceLevel: String
        get() = when {
            winningPercent.mostLikely >= 70 -> "Hoog"
            winningPercent.mostLikely >= 50 -> "Gemiddeld"
            else -> "Laag"
        }

    /**
     * Get a beginner-friendly prediction summary.
     */
    val beginnerSummary: String
        get() = "$homeTeam vs $awayTeam: $primaryPrediction (${winningPercent.mostLikely}% kans)"
}

/**
 * Winning percentages for different outcomes.
 */
@Serializable
data class WinningPercent(
    val home: Double,
    val draw: Double,
    val away: Double
) {
    /**
     * Get the most likely outcome percentage.
     */
    val mostLikely: Double
        get() = maxOf(home, draw, away)

    /**
     * Get the most likely outcome type.
     */
    val mostLikelyType: String
        get() = when (mostLikely) {
            home -> "Home"
            draw -> "Draw"
            away -> "Away"
            else -> "Unknown"
        }

    /**
     * Get total percentage (should be close to 100%).
     */
    val total: Double
        get() = home + draw + away
}

/**
 * Expected goals data.
 */
@Serializable
data class ExpectedGoals(
    val home: Double,
    val away: Double
) {
    /**
     * Get total expected goals.
     */
    val total: Double
        get() = home + away

    /**
     * Get expected goal difference.
     */
    val difference: Double
        get() = home - away

    /**
     * Check if high-scoring match is expected.
     */
    val isHighScoring: Boolean
        get() = total >= 2.5
}

/**
 * Companion object with utility functions.
 */
object MatchPredictionDataUtils {
    /**
     * Create an empty prediction for matches without prediction data.
     * Shows clear message instead of misleading 33% probabilities.
     */
    fun empty(fixtureId: Int, homeTeam: String, awayTeam: String, league: String): MatchPredictionData {
        return MatchPredictionData(
            fixtureId = fixtureId,
            homeTeam = homeTeam,
            awayTeam = awayTeam,
            league = league,
            primaryPrediction = "Geen voorspelling beschikbaar",
            winningPercent = WinningPercent(0.0, 0.0, 0.0),
            expectedGoals = ExpectedGoals(0.0, 0.0),
            analysis = "API-SPORTS heeft geen voorspellingsdata voor deze wedstrijd ($homeTeam vs $awayTeam in $league).\n\nDit kan komen omdat:\n• De competitie geen voorspellingscoverage heeft\n• De wedstrijd te recent is toegevoegd\n• Er technische problemen zijn met de API\n\nProbeer een Eredivisie, Premier League of andere topcompetitie wedstrijd voor volledige voorspellingen."
        )
    }

    /**
     * Create a prediction from basic probabilities.
     */
    fun fromProbabilities(
        fixtureId: Int,
        homeTeam: String,
        awayTeam: String,
        league: String,
        homeWinProbability: Double,
        drawProbability: Double,
        awayWinProbability: Double,
        expectedGoalsHome: Double,
        expectedGoalsAway: Double,
        analysis: String? = null
    ): MatchPredictionData {
        val primaryPrediction = when {
            homeWinProbability >= drawProbability && homeWinProbability >= awayWinProbability -> "$homeTeam wint"
            drawProbability >= homeWinProbability && drawProbability >= awayWinProbability -> "Gelijkspel"
            else -> "$awayTeam wint"
        }

        return MatchPredictionData(
            fixtureId = fixtureId,
            homeTeam = homeTeam,
            awayTeam = awayTeam,
            league = league,
            primaryPrediction = primaryPrediction,
            winningPercent = WinningPercent(homeWinProbability, drawProbability, awayWinProbability),
            expectedGoals = ExpectedGoals(expectedGoalsHome, expectedGoalsAway),
            analysis = analysis
        )
    }
}
