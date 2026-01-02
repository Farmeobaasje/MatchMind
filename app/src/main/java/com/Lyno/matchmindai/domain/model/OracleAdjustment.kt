package com.Lyno.matchmindai.domain.model

/**
 * Oracle Adjustment result from AI Intelligence Layer validation.
 * This model represents the AI's validation or adjustment of the base Oracle prediction
 * based on recent news analysis.
 *
 * @property status Whether the prediction was VERIFIED or ADJUSTED
 * @property adjustedScoreHome The adjusted home score (null if VERIFIED)
 * @property adjustedScoreAway The adjusted away score (null if VERIFIED)
 * @property reasoning Human-readable explanation of the adjustment (e.g., "Haaland is out, City attack reduced.")
 */
data class OracleAdjustment(
    val status: AdjustmentStatus,
    val adjustedScoreHome: Int?,
    val adjustedScoreAway: Int?,
    val reasoning: String
) {
    init {
        // Validate that adjusted scores are provided when status is ADJUSTED
        if (status == AdjustmentStatus.ADJUSTED) {
            require(adjustedScoreHome != null) { "Adjusted home score must be provided when status is ADJUSTED" }
            require(adjustedScoreAway != null) { "Adjusted away score must be provided when status is ADJUSTED" }
            require(adjustedScoreHome >= 0) { "Adjusted home score must be non-negative" }
            require(adjustedScoreAway >= 0) { "Adjusted away score must be non-negative" }
        } else {
            // For VERIFIED status, adjusted scores should be null
            require(adjustedScoreHome == null) { "Adjusted home score must be null when status is VERIFIED" }
            require(adjustedScoreAway == null) { "Adjusted away score must be null when status is VERIFIED" }
        }
    }

    /**
     * Returns the adjusted prediction string if available, otherwise null.
     */
    val adjustedPrediction: String?
        get() = if (status == AdjustmentStatus.ADJUSTED && adjustedScoreHome != null && adjustedScoreAway != null) {
            "$adjustedScoreHome-$adjustedScoreAway"
        } else {
            null
        }

    /**
     * Returns true if this adjustment indicates a score change.
     */
    val hasScoreChange: Boolean
        get() = status == AdjustmentStatus.ADJUSTED

    /**
     * Returns a user-friendly description of the adjustment.
     */
    fun getDescription(): String {
        return when (status) {
            AdjustmentStatus.VERIFIED -> "✅ AI Validated: No critical news found."
            AdjustmentStatus.ADJUSTED -> "⚠️ AI Adjustment: Score changed due to $reasoning"
        }
    }

    /**
     * Returns a short status badge for UI display.
     */
    fun getStatusBadge(): String {
        return when (status) {
            AdjustmentStatus.VERIFIED -> "✅ Verified"
            AdjustmentStatus.ADJUSTED -> "⚠️ Adjusted"
        }
    }
}

/**
 * Status of Oracle prediction adjustment.
 */
enum class AdjustmentStatus {
    /**
     * The base prediction is verified by AI as accurate.
     * No score changes needed.
     */
    VERIFIED,

    /**
     * The base prediction is adjusted by AI based on news analysis.
     * Score has been modified.
     */
    ADJUSTED
}
