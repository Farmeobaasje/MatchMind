package com.Lyno.matchmindai.domain.model

/**
 * Domain model for team statistics.
 * Used for calculating and storing team performance metrics.
 */
data class TeamStatistics(
    val teamId: Int,
    val played: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val points: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val goalDiff: Int,
    val form: List<MatchResult> = emptyList(),
    val homeStats: VenueStats? = null,
    val awayStats: VenueStats? = null,
    val recentMatches: List<RecentMatch> = emptyList()
) {
    val winPercentage: Double = if (played > 0) (wins.toDouble() / played) * 100 else 0.0
    val drawPercentage: Double = if (played > 0) (draws.toDouble() / played) * 100 else 0.0
    val lossPercentage: Double = if (played > 0) (losses.toDouble() / played) * 100 else 0.0
    val goalsPerGame: Double = if (played > 0) goalsFor.toDouble() / played else 0.0
    val goalsAgainstPerGame: Double = if (played > 0) goalsAgainst.toDouble() / played else 0.0
    
    /**
     * Get form string (e.g., "WWDLW")
     */
    fun getFormString(): String {
        return form.takeLast(5).joinToString("") { 
            when (it) {
                MatchResult.WIN -> "W"
                MatchResult.DRAW -> "D"
                MatchResult.LOSS -> "L"
            }
        }
    }
    
    /**
     * Get form points (last 5 matches)
     */
    fun getFormPoints(): Int {
        return form.takeLast(5).fold(0) { acc, matchResult ->
            acc + when (matchResult) {
                MatchResult.WIN -> 3
                MatchResult.DRAW -> 1
                MatchResult.LOSS -> 0
            }
        }
    }
    
    companion object {
        fun empty(teamId: Int): TeamStatistics {
            return TeamStatistics(
                teamId = teamId,
                played = 0,
                wins = 0,
                draws = 0,
                losses = 0,
                points = 0,
                goalsFor = 0,
                goalsAgainst = 0,
                goalDiff = 0
            )
        }
    }
}

/**
 * Statistics for a specific venue (home/away).
 */
data class VenueStats(
    val played: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val goalsFor: Int,
    val goalsAgainst: Int
) {
    val points: Int get() = (wins * 3) + draws
    val goalDiff: Int get() = goalsFor - goalsAgainst
    val winPercentage: Double = if (played > 0) (wins.toDouble() / played) * 100 else 0.0
}

/**
 * Recent match information for form analysis.
 */
data class RecentMatch(
    val fixtureId: Int,
    val opponentId: Int,
    val opponentName: String,
    val isHome: Boolean,
    val result: MatchResult,
    val score: String, // e.g., "2-1"
    val date: String
)
