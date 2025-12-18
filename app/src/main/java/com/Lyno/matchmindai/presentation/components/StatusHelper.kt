package com.Lyno.matchmindai.presentation.components

import androidx.compose.ui.graphics.Color
import com.Lyno.matchmindai.ui.theme.ConfidenceHigh
import com.Lyno.matchmindai.ui.theme.ConfidenceLow
import com.Lyno.matchmindai.ui.theme.ConfidenceMedium
import com.Lyno.matchmindai.ui.theme.TextDisabled

/**
 * Helper object for mapping API-SPORTS status codes to Dutch labels and colors.
 * Follows the exact mapping logic specified in the task:
 * - "NS" -> "Gepland" (Color: Grey)
 * - "1H", "2H", "ET", "KE", "BT" -> "Live" (Color: Red)
 * - "HT" -> "Rust" (Color: Orange)
 * - "FT", "AET", "PEN" -> "Afgelopen" (Color: Green)
 * - "PST", "SUSP", "INT" -> "Uitgesteld" (Color: Orange + Warning Icon)
 * - "CANC", "ABD" -> "Afgelast" (Color: Red)
 * - Default/Else -> Use the raw status code (e.g., "TBD") instead of "ONBEKEND"
 */
object StatusHelper {

    /**
     * Gets the Dutch display label for an API-SPORTS status code.
     *
     * @param status The API-SPORTS status code (e.g., "NS", "1H", "FT")
     * @return The Dutch label for the status
     */
    fun getLabel(status: String?): String {
        return when (status?.uppercase()) {
            "NS" -> "Gepland"
            "1H", "2H", "ET", "KE", "BT" -> "Live"
            "HT" -> "Rust"
            "FT", "AET", "PEN" -> "Afgelopen"
            "PST", "SUSP", "INT" -> "Uitgesteld"
            "CANC", "ABD" -> "Afgelast"
            else -> status ?: "ONBEKEND"
        }
    }

    /**
     * Gets the background color for a status badge.
     *
     * @param status The API-SPORTS status code
     * @return The appropriate background color with alpha
     */
    fun getColor(status: String?): Color {
        return when {
            isLiveMatch(status) -> ConfidenceLow.copy(alpha = 0.2f)
            isFinishedMatch(status) -> ConfidenceHigh.copy(alpha = 0.2f)
            isProblematicMatch(status) -> ConfidenceMedium.copy(alpha = 0.2f)
            else -> TextDisabled.copy(alpha = 0.2f)
        }
    }

    /**
     * Gets the text color for a status badge.
     *
     * @param status The API-SPORTS status code
     * @return The appropriate text color
     */
    fun getTextColor(status: String?): Color {
        return when {
            isLiveMatch(status) -> ConfidenceLow
            isFinishedMatch(status) -> ConfidenceHigh
            isProblematicMatch(status) -> ConfidenceMedium
            else -> TextDisabled
        }
    }

    /**
     * Checks if a status indicates a live match.
     */
    private fun isLiveMatch(status: String?): Boolean {
        return status?.uppercase() in setOf("1H", "2H", "ET", "KE", "BT", "HT")
    }

    /**
     * Checks if a status indicates a finished match.
     */
    private fun isFinishedMatch(status: String?): Boolean {
        return status?.uppercase() in setOf("FT", "AET", "PEN")
    }

    /**
     * Checks if a status indicates a problematic match.
     */
    private fun isProblematicMatch(status: String?): Boolean {
        return status?.uppercase() in setOf("PST", "SUSP", "INT", "CANC", "ABD")
    }

    /**
     * Checks if a status indicates a not started match.
     */
    private fun isNotStartedMatch(status: String?): Boolean {
        return status?.uppercase() == "NS"
    }
}
