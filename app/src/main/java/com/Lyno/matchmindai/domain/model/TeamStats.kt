package com.Lyno.matchmindai.domain.model

/**
 * Team statistics for Dixon-Coles model calculations.
 * Contains expected goals (xG) data for a team in a specific match.
 */
data class TeamStats(
    /** The fixture ID this stats belong to */
    val fixtureId: Int,
    
    /** Team ID */
    val teamId: Int,
    
    /** Team name */
    val teamName: String,
    
    /** Expected goals for (xG) - what the team is expected to score */
    val xgFor: Double,
    
    /** Expected goals against (xGA) - what the team is expected to concede */
    val xgAgainst: Double,
    
    /** Whether the team was playing at home */
    val isHome: Boolean,
    
    /** Actual goals scored (for reference) */
    val actualGoalsFor: Int,
    
    /** Actual goals conceded (for reference) */
    val actualGoalsAgainst: Int,
    
    /** Match date for time weighting */
    val matchDate: String,
    
    /** League ID for context */
    val leagueId: Int,
    
    /** League name for context */
    val leagueName: String,
    
    /** Weight factor for competition importance (0.0-1.5, default = 1.0) */
    val weight: Double = 1.0
) {
    /**
     * Calculate goal difference based on expected goals.
     */
    val xgDifference: Double
        get() = xgFor - xgAgainst
    
    /**
     * Calculate goal difference based on actual goals.
     */
    val actualGoalDifference: Int
        get() = actualGoalsFor - actualGoalsAgainst
    
    /**
     * Check if this is a win based on actual goals.
     */
    val isWin: Boolean
        get() = actualGoalsFor > actualGoalsAgainst
    
    /**
     * Check if this is a draw based on actual goals.
     */
    val isDraw: Boolean
        get() = actualGoalsFor == actualGoalsAgainst
    
    /**
     * Check if this is a loss based on actual goals.
     */
    val isLoss: Boolean
        get() = actualGoalsFor < actualGoalsAgainst
}
