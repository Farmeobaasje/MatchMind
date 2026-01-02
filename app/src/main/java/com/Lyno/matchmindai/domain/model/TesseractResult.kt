package com.Lyno.matchmindai.domain.model

/**
 * Result of the Tesseract Monte Carlo Simulation Engine.
 * Represents probabilistic outcomes from 10,000 match simulations.
 *
 * @property homeWinProbability Probability of home win (0.0-1.0)
 * @property drawProbability Probability of draw (0.0-1.0)
 * @property awayWinProbability Probability of away win (0.0-1.0)
 * @property mostLikelyScore Most frequently occurring scoreline (e.g., "2-1")
 * @property simulationCount Number of simulations performed (default: 10,000)
 * @property bttsProbability Probability of Both Teams To Score = YES (0.0-1.0)
 * @property over2_5Probability Probability of Total Goals > 2.5 (0.0-1.0)
 * @property topScoreDistribution Top 3 most likely scores with occurrence counts
 */
data class TesseractResult(
    val homeWinProbability: Double,
    val drawProbability: Double,
    val awayWinProbability: Double,
    val mostLikelyScore: String,
    val simulationCount: Int = 10000,
    val bttsProbability: Double = 0.0,
    val over2_5Probability: Double = 0.0,
    val topScoreDistribution: List<Pair<String, Int>> = emptyList()
) {
    init {
        require(homeWinProbability in 0.0..1.0) { "Home win probability must be between 0.0 and 1.0" }
        require(drawProbability in 0.0..1.0) { "Draw probability must be between 0.0 and 1.0" }
        require(awayWinProbability in 0.0..1.0) { "Away win probability must be between 0.0 and 1.0" }
        require((homeWinProbability + drawProbability + awayWinProbability).let { 
            it >= 0.95 && it <= 1.05 
        }) { "Probabilities must sum to approximately 1.0 (tolerance: ±5%)" }
        require(mostLikelyScore.matches(Regex("^\\d+-\\d+$"))) { 
            "Most likely score must be in format 'X-Y' (e.g., '2-1')" 
        }
        require(simulationCount > 0) { "Simulation count must be positive" }
    }

    /**
     * Returns the probability of home win as a percentage (0-100).
     */
    val homeWinPercentage: Int
        get() = (homeWinProbability * 100).toInt()

    /**
     * Returns the probability of draw as a percentage (0-100).
     */
    val drawPercentage: Int
        get() = (drawProbability * 100).toInt()

    /**
     * Returns the probability of away win as a percentage (0-100).
     */
    val awayWinPercentage: Int
        get() = (awayWinProbability * 100).toInt()

    /**
     * Returns true if home team is the favorite (highest probability).
     */
    val isHomeFavorite: Boolean
        get() = homeWinProbability > awayWinProbability && homeWinProbability > drawProbability

    /**
     * Returns true if away team is the favorite (highest probability).
     */
    val isAwayFavorite: Boolean
        get() = awayWinProbability > homeWinProbability && awayWinProbability > drawProbability

    /**
     * Returns true if draw is the most likely outcome.
     */
    val isDrawFavorite: Boolean
        get() = drawProbability > homeWinProbability && drawProbability > awayWinProbability

    /**
     * Returns the probability delta between home and away win.
     * Positive values favor home team, negative values favor away team.
     */
    val winProbabilityDelta: Double
        get() = homeWinProbability - awayWinProbability

    /**
     * Returns the probability of Both Teams To Score as a percentage (0-100).
     */
    val bttsPercentage: Int
        get() = (bttsProbability * 100).toInt()

    /**
     * Returns the probability of Both Teams NOT To Score (0.0-1.0).
     */
    val bttsNoProbability: Double
        get() = 1.0 - bttsProbability

    /**
     * Returns the probability of Both Teams NOT To Score as a percentage (0-100).
     */
    val bttsNoPercentage: Int
        get() = (bttsNoProbability * 100).toInt()

    /**
     * Returns the probability of Under 2.5 goals (0.0-1.0).
     */
    val under2_5Probability: Double
        get() = 1.0 - over2_5Probability

    /**
     * Returns the probability of Under 2.5 goals as a percentage (0-100).
     */
    val under2_5Percentage: Int
        get() = (under2_5Probability * 100).toInt()

    /**
     * Returns the probability of Over 2.5 goals as a percentage (0-100).
     */
    val over2_5Percentage: Int
        get() = (over2_5Probability * 100).toInt()

    /**
     * Returns true if Both Teams To Score is more likely than not.
     */
    val isBttsYesFavorite: Boolean
        get() = bttsProbability > 0.5

    /**
     * Returns true if Over 2.5 goals is more likely than Under 2.5.
     */
    val isOver2_5Favorite: Boolean
        get() = over2_5Probability > 0.5

    /**
     * Returns a formatted string representation of probabilities.
     */
    fun getFormattedProbabilities(): String {
        return "H: ${homeWinPercentage}% | D: ${drawPercentage}% | A: ${awayWinPercentage}%"
    }

    /**
     * Returns a formatted string representation of BTTS probabilities.
     */
    fun getFormattedBttsProbabilities(): String {
        return "JA: ${bttsPercentage}% | NEE: ${bttsNoPercentage}%"
    }

    /**
     * Returns a formatted string representation of Over/Under probabilities.
     */
    fun getFormattedOverUnderProbabilities(): String {
        return "OVER 2.5: ${over2_5Percentage}% | UNDER 2.5: ${under2_5Percentage}%"
    }

    /**
     * Returns the top 3 scores with their percentages.
     */
    fun getTopScoresWithPercentages(): List<Pair<String, Int>> {
        return topScoreDistribution.map { (score, count) ->
            score to ((count.toDouble() / simulationCount) * 100).toInt()
        }
    }
}
