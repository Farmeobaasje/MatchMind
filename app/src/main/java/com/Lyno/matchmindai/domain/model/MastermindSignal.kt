package com.Lyno.matchmindai.domain.model

/**
 * Mastermind Signal - The "Golden Tip" decision from the Mastermind Engine.
 * Represents a high-level conclusion about the match based on integrated analysis
 * of Oracle, Tesseract, and contextual data.
 *
 * @property title Short, impactful title (e.g., "ZEKERHEIDJE", "HOOG RISICO")
 * @property description Detailed explanation in Dutch
 * @property color Color coding for the signal (Green/Yellow/Red)
 * @property confidence Confidence level from 0-100%
 * @property recommendation Specific betting recommendation (e.g., "Home Win", "BTTS Yes")
 * @property scenarioType Type of scenario (Banker, HighRisk, Goals, etc.)
 */
data class MastermindSignal(
    val title: String,
    val description: String,
    val color: SignalColor,
    val confidence: Int,
    val recommendation: String,
    val scenarioType: ScenarioType
) {
    init {
        require(confidence in 0..100) { "Confidence must be between 0 and 100" }
        require(title.isNotBlank()) { "Title cannot be blank" }
        require(description.isNotBlank()) { "Description cannot be blank" }
        require(recommendation.isNotBlank()) { "Recommendation cannot be blank" }
    }

    /**
     * Returns true if this is a "Banker" (high confidence) signal.
     */
    val isBanker: Boolean
        get() = scenarioType == ScenarioType.BANKER

    /**
     * Returns true if this is a high-risk signal.
     */
    val isHighRisk: Boolean
        get() = scenarioType == ScenarioType.HIGH_RISK

    /**
     * Returns true if this is a goals-focused signal.
     */
    val isGoalsFocus: Boolean
        get() = scenarioType == ScenarioType.GOALS_FESTIVAL

    /**
     * Returns the color as a Compose Color.
     */
    fun toComposeColor(): androidx.compose.ui.graphics.Color {
        return when (color) {
            SignalColor.GREEN -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green
            SignalColor.YELLOW -> androidx.compose.ui.graphics.Color(0xFFFFA726) // Orange/Yellow
            SignalColor.RED -> androidx.compose.ui.graphics.Color(0xFFEF5350) // Red
        }
    }
}

/**
 * Color coding for Mastermind signals.
 */
enum class SignalColor {
    GREEN,    // High confidence, safe bet
    YELLOW,   // Medium confidence, some risk
    RED       // Low confidence, high risk
}

/**
 * Type of scenario determined by the Mastermind Engine.
 */
enum class ScenarioType {
    BANKER,           // "Zekerheidje" - High confidence, multiple indicators align
    HIGH_RISK,        // "Hoog Risico" - Oracle and Tesseract disagree
    GOALS_FESTIVAL,   // "Doelpunten Festijn" - High probability of goals
    TACTICAL_DUEL,    // "Tactisch Duel" - Close match, tactical battle
    DEFENSIVE_BATTLE, // "Defensieve Strijd" - Low scoring, defensive match
    VALUE_BET         // "Value Bet" - Good odds value despite risk
}

/**
 * Companion object with factory methods for common signal types.
 */
object MastermindSignalFactory {
    
    /**
     * Creates a "Banker" (Zekerheidje) signal.
     */
    fun createBanker(
        oracleAnalysis: OracleAnalysis,
        tesseractResult: TesseractResult?,
        confidence: Int = 85
    ): MastermindSignal {
        val homeTeamWins = oracleAnalysis.prediction.split("-")[0].toInt() > oracleAnalysis.prediction.split("-")[1].toInt()
        val awayTeamWins = oracleAnalysis.prediction.split("-")[0].toInt() < oracleAnalysis.prediction.split("-")[1].toInt()
        
        val recommendation = when {
            homeTeamWins -> "Thuis Winst"
            awayTeamWins -> "Uit Winst"
            else -> "Gelijk"
        }
        
        return MastermindSignal(
            title = "ZEKERHEIDJE",
            description = "Data en fitheid wijzen unaniem naar ${if (homeTeamWins) "thuis" else "uit"}winst. " +
                         "Oracle (${oracleAnalysis.confidence}%) en Tesseract simulaties bevestigen dit patroon.",
            color = SignalColor.GREEN,
            confidence = confidence,
            recommendation = recommendation,
            scenarioType = ScenarioType.BANKER
        )
    }
    
    /**
     * Creates a "High Risk" signal when Oracle and Tesseract disagree.
     */
    fun createHighRisk(
        oracleScore: String,
        tesseractScore: String,
        confidence: Int = 60
    ): MastermindSignal {
        return MastermindSignal(
            title = "HOOG RISICO",
            description = "Oracle voorspelt $oracleScore, maar Tesseract simulaties wijzen op $tesseractScore. " +
                         "Kleine marge, grote onzekerheid. Extra voorzichtigheid is geboden.",
            color = SignalColor.YELLOW,
            confidence = confidence,
            recommendation = "Voorzichtige inzet of vermijden",
            scenarioType = ScenarioType.HIGH_RISK
        )
    }
    
    /**
     * Creates a "Goals Festival" signal when high probability of goals.
     */
    fun createGoalsFestival(
        over25Probability: Double,
        bttsProbability: Double,
        confidence: Int = 75
    ): MastermindSignal {
        return MastermindSignal(
            title = "DOELPUNTEN FESTIJN",
            description = "Over 2.5 goals kans: ${(over25Probability * 100).toInt()}%. " +
                         "Beide teams scoren kans: ${(bttsProbability * 100).toInt()}%. " +
                         "Verwacht een offensieve wedstrijd met veel doelpunten.",
            color = SignalColor.GREEN,
            confidence = confidence,
            recommendation = "Over 2.5 Goals & BTTS Yes",
            scenarioType = ScenarioType.GOALS_FESTIVAL
        )
    }
    
    /**
     * Creates a "Tactical Duel" signal for close matches.
     */
    fun createTacticalDuel(
        oracleAnalysis: OracleAnalysis,
        confidence: Int = 65
    ): MastermindSignal {
        return MastermindSignal(
            title = "TACTISCH DUEL",
            description = "Krachtverschil slechts ${oracleAnalysis.powerDelta} punten. " +
                         "Verwacht een tactische strijd waar kleine details het verschil maken.",
            color = SignalColor.YELLOW,
            confidence = confidence,
            recommendation = "Gelijk of kleine marge",
            scenarioType = ScenarioType.TACTICAL_DUEL
        )
    }
}
