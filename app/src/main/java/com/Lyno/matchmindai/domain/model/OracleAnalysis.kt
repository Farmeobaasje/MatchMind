package com.Lyno.matchmindai.domain.model

/**
 * Data source for standings information.
 */
enum class DataSource {
    API_OFFICIAL,    // Official API data for current season
    CALCULATED,      // Calculated from recent fixtures
    PREVIOUS_SEASON, // Data from previous season
    DEFAULT          // Default fallback data
}

/**
 * Oracle Analysis result from the "Hard Reality" prediction engine.
 * This model represents a deterministic, fact-based prediction based strictly on
 * standings (rank, points) and head-to-head data, ignoring complex match history loops.
 *
 * @property prediction The predicted score (e.g., "0-3", "2-1", "1-1")
 * @property confidence Confidence level from 0-100%
 * @property reasoning Human-readable explanation of the prediction (e.g., "City (#2) is 15 ranks higher than Forest (#17)")
 * @property homePowerScore Calculated power score for the home team (0-200 scale)
 * @property awayPowerScore Calculated power score for the away team (0-200 scale)
 * @property tesseract Optional Tesseract Monte Carlo simulation results
 * @property simulationContext Optional SimulationContext containing fitness and distraction metrics
 * @property mastermindSignal Optional Mastermind Signal (Phase 9) - The "Golden Tip" decision
 * @property llmGradeEnhancement Optional LLMGRADE enhancement with context factors and outlier scenarios
 * @property standingsSource Source of the standings data used for this prediction
 * @property confidenceAdjustment Multiplier applied to confidence based on data source quality (0.0-1.0)
 */
data class OracleAnalysis(
    val prediction: String,
    val confidence: Int,
    val reasoning: String,
    val homePowerScore: Int,
    val awayPowerScore: Int,
    val tesseract: TesseractResult? = null,
    val simulationContext: SimulationContext? = null,
    val mastermindSignal: MastermindSignal? = null,
    val llmGradeEnhancement: com.Lyno.matchmindai.domain.model.LLMGradeEnhancement? = null,
    val standingsSource: DataSource = DataSource.API_OFFICIAL,
    val confidenceAdjustment: Float = 1.0f
) {
    init {
        require(confidence in 0..100) { "Confidence must be between 0 and 100" }
        require(homePowerScore in 0..200) { "Home power score must be between 0 and 200" }
        require(awayPowerScore in 0..200) { "Away power score must be between 0 and 200" }
        require(prediction.matches(Regex("^\\d+-\\d+$"))) { "Prediction must be in format 'X-Y' (e.g., '0-3')" }
    }

    /**
     * Returns the power score delta (Home - Away).
     * Positive values favor home team, negative values favor away team.
     */
    val powerDelta: Int
        get() = homePowerScore - awayPowerScore

    /**
     * Returns true if this prediction indicates a strong away win (0-3 or similar).
     */
    val isStrongAwayWin: Boolean
        get() = powerDelta < -30

    /**
     * Returns true if this prediction indicates a strong home win (3-0 or similar).
     */
    val isStrongHomeWin: Boolean
        get() = powerDelta > 30

    /**
     * Returns true if this prediction indicates a close game or draw.
     */
    val isCloseGame: Boolean
        get() = powerDelta in -15..15

    /**
     * Gets a betting tip based on the prediction and power delta.
     */
    fun getBettingTip(homeTeam: String = "Thuis", awayTeam: String = "Uit"): String {
        return when {
            powerDelta < -30 -> "$awayTeam wint & Under 2.5 Goals"
            powerDelta < -15 -> "$awayTeam wint of Gelijk"
            powerDelta > 30 -> "$homeTeam wint & Over 2.5 Goals"
            powerDelta > 15 -> "$homeTeam wint of Gelijk"
            else -> "Gelijkspel & Beide Teams Scoren"
        }
    }

    /**
     * Gets the betting confidence based on power delta and confidence.
     */
    fun getBettingConfidence(): Int {
        val baseConfidence = confidence
        
        // Adjust based on power delta magnitude
        val deltaAdjustment = when {
            powerDelta < -30 || powerDelta > 30 -> 15  // Strong prediction
            powerDelta < -15 || powerDelta > 15 -> 10  // Moderate prediction
            else -> 0  // Close game
        }
        
        // Adjust based on confidence adjustment from data source
        val sourceAdjustment = when (standingsSource) {
            DataSource.API_OFFICIAL -> 10
            DataSource.CALCULATED -> 5
            DataSource.PREVIOUS_SEASON -> 0
            DataSource.DEFAULT -> -5
        }
        
        val adjustedConfidence = baseConfidence + deltaAdjustment + sourceAdjustment
        return adjustedConfidence.coerceIn(0, 100)
    }
}
