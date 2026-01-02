package com.Lyno.matchmindai.domain.model

/**
 * Enum representing the status of a football match.
 * Maps API short codes to meaningful statuses.
 * Based on API-Sports documentation: https://www.api-sports.io/documentation/football/v3
 */
enum class MatchStatus {
    // Live statuses
    FIRST_HALF,     // 1H - First Half, Kick Off
    HALFTIME,       // HT - Halftime
    SECOND_HALF,    // 2H - Second Half, 2nd Half Started
    EXTRA_TIME,     // ET - Extra Time
    BREAK_TIME,     // BT - Break Time
    PENALTY,        // P - Penalty In Progress
    SUSPENDED,      // SUSP - Match Suspended
    INTERRUPTED,    // INT - Match Interrupted
    LIVE,           // LIVE - In Progress (rare cases)
    
    // Finished statuses
    FINISHED,       // FT - Match Finished (regular time)
    FINISHED_AET,   // AET - Match Finished after extra time
    FINISHED_PEN,   // PEN - Match Finished after penalty shootout
    
    // Scheduled statuses
    SCHEDULED,      // NS - Not Started
    TBD,            // TBD - Time To Be Defined
    POSTPONED,      // PST - Match Postponed
    
    // Other statuses
    CANCELLED,      // CANC - Match Cancelled
    ABANDONED,      // ABD - Match Abandoned
    AWARDED,        // AWD - Technical Loss
    WALKOVER,       // WO - WalkOver
    UNKNOWN;        // Unknown or unsupported status

    /**
     * Get display name in Dutch.
     */
    val displayName: String
        get() = when (this) {
            FIRST_HALF -> "Eerste Helft"
            HALFTIME -> "Rust"
            SECOND_HALF -> "Tweede Helft"
            EXTRA_TIME -> "Extra Tijd"
            BREAK_TIME -> "Pauze"
            PENALTY -> "Penalty's"
            SUSPENDED -> "Geschorst"
            INTERRUPTED -> "Onderbroken"
            LIVE -> "Live"
            FINISHED -> "Beëindigd"
            FINISHED_AET -> "Beëindigd (ET)"
            FINISHED_PEN -> "Beëindigd (Pen)"
            SCHEDULED -> "Gepland"
            TBD -> "Nog te bepalen"
            POSTPONED -> "Uitgesteld"
            CANCELLED -> "Geannuleerd"
            ABANDONED -> "Afgebroken"
            AWARDED -> "Toegekend"
            WALKOVER -> "Walkover"
            UNKNOWN -> "Onbekend"
        }

    /**
     * Check if match is currently live/in progress.
     */
    val isLive: Boolean
        get() = when (this) {
            FIRST_HALF, HALFTIME, SECOND_HALF, EXTRA_TIME, 
            BREAK_TIME, PENALTY, SUSPENDED, INTERRUPTED, LIVE -> true
            else -> false
        }

    /**
     * Check if match is finished.
     */
    val isFinished: Boolean
        get() = when (this) {
            FINISHED, FINISHED_AET, FINISHED_PEN -> true
            else -> false
        }

    /**
     * Check if match is scheduled for future.
     */
    val isScheduled: Boolean
        get() = when (this) {
            SCHEDULED, TBD, POSTPONED -> true
            else -> false
        }

    /**
     * Get emoji representation for UI.
     */
    val emoji: String
        get() = when (this) {
            FIRST_HALF -> "⏱️"
            HALFTIME -> "🔄"
            SECOND_HALF -> "⏱️"
            EXTRA_TIME -> "➕"
            BREAK_TIME -> "⏸️"
            PENALTY -> "🎯"
            SUSPENDED -> "⏸️"
            INTERRUPTED -> "⚠️"
            LIVE -> "🔴"
            FINISHED -> "✅"
            FINISHED_AET -> "✅➕"
            FINISHED_PEN -> "✅🎯"
            SCHEDULED -> "📅"
            TBD -> "❓"
            POSTPONED -> "📅❌"
            CANCELLED -> "❌"
            ABANDONED -> "🚫"
            AWARDED -> "🏆"
            WALKOVER -> "🚶"
            UNKNOWN -> "❓"
        }

    companion object {
        /**
         * Convert API short code to MatchStatus enum.
         */
        fun fromApiCode(code: String?): MatchStatus {
            return when (code?.uppercase()) {
                "1H" -> FIRST_HALF
                "HT" -> HALFTIME
                "2H" -> SECOND_HALF
                "ET" -> EXTRA_TIME
                "BT" -> BREAK_TIME
                "P" -> PENALTY
                "SUSP" -> SUSPENDED
                "INT" -> INTERRUPTED
                "LIVE" -> LIVE
                "FT" -> FINISHED
                "AET" -> FINISHED_AET
                "PEN" -> FINISHED_PEN
                "NS" -> SCHEDULED
                "TBD" -> TBD
                "PST" -> POSTPONED
                "CANC" -> CANCELLED
                "ABD" -> ABANDONED
                "AWD" -> AWARDED
                "WO" -> WALKOVER
                else -> UNKNOWN
            }
        }

        /**
         * Get all live statuses.
         */
        fun liveStatuses(): List<MatchStatus> = listOf(
            FIRST_HALF, HALFTIME, SECOND_HALF, EXTRA_TIME,
            BREAK_TIME, PENALTY, SUSPENDED, INTERRUPTED, LIVE
        )
    }
}
