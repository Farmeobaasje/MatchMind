package com.Lyno.matchmindai.domain.model

/**
 * Enum representing the status of a football match.
 * Maps API short codes to meaningful statuses.
 */
enum class MatchStatus {
    LIVE,       // Match is currently in progress
    FINISHED,   // Match has ended
    SCHEDULED,  // Match is scheduled for future
    UNKNOWN;    // Unknown or unsupported status

    /**
     * Get display name in Dutch.
     */
    val displayName: String
        get() = when (this) {
            LIVE -> "Live"
            FINISHED -> "Beëindigd"
            SCHEDULED -> "Gepland"
            UNKNOWN -> "Onbekend"
        }
}
