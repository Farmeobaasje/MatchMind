package com.Lyno.matchmindai.domain.model

import kotlinx.serialization.Serializable

/**
 * Injury domain model representing a player injury.
 * This model is used for AI analysis and match predictions.
 */
@Serializable
data class Injury(
    val playerName: String,
    val team: String,
    val type: String,
    val reason: String,
    val severity: InjurySeverity = InjurySeverity.MODERATE,
    val isKeyPlayer: Boolean = false,
    val expectedReturn: String? = null
) {
    /**
     * Get display text for UI.
     */
    val displayText: String
        get() = "$playerName ($team) - $type"
}

/**
 * Injury severity levels.
 */
@Serializable
enum class InjurySeverity {
    MINOR,      // Minor injury, player might play
    MODERATE,   // Moderate injury, player doubtful
    MAJOR;      // Major injury, player out

    /**
     * Get display name in Dutch.
     */
    val displayName: String
        get() = when (this) {
            MINOR -> "Klein"
            MODERATE -> "Matig"
            MAJOR -> "Groot"
        }

    /**
     * Get color for UI.
     */
    val colorName: String
        get() = when (this) {
            MINOR -> "Green"
            MODERATE -> "Yellow"
            MAJOR -> "Red"
        }
}
