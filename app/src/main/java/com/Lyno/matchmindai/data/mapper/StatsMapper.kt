package com.Lyno.matchmindai.data.mapper

import com.Lyno.matchmindai.domain.model.HistoricalFixture
import com.Lyno.matchmindai.domain.model.TeamStats

/**
 * Mapper for converting historical fixtures to TeamStats for Dixon-Coles model.
 * This is used by EnhancedScorePredictor to process historical data.
 */
object StatsMapper {

    /**
     * Convert historical fixtures to TeamStats for a specific team.
     * 
     * @param history List of historical fixtures involving the team
     * @param teamId The team ID to filter for
     * @return List of TeamStats for the specified team
     */
    fun mapToTeamStats(history: List<HistoricalFixture>, teamId: Int): List<TeamStats> {
        return mapToTeamStatsWithWeights(history, teamId) { _ -> 1.0 } // Default weight = 1.0
    }

    /**
     * Convert historical fixtures to TeamStats for a specific team with competition weighting.
     * This solves the "Marokko 4-0" problem by applying weights based on competition importance.
     * 
     * @param history List of historical fixtures involving the team
     * @param teamId The team ID to filter for
     * @param weightCalculator Function to calculate weight for each fixture
     * @return List of TeamStats for the specified team with applied weights
     */
    fun mapToTeamStatsWithWeights(
        history: List<HistoricalFixture>, 
        teamId: Int,
        weightCalculator: (HistoricalFixture) -> Double
    ): List<TeamStats> {
        return history.mapNotNull { match ->
            // Determine if the team is home or away
            val isHome = match.homeTeamId == teamId
            val isAway = match.awayTeamId == teamId
            
            if (!isHome && !isAway) {
                return@mapNotNull null // Skip matches not involving this team
            }
            
            // Calculate weight for this match
            val weight = weightCalculator(match)
            
            // Extract goals for and against
            val goalsFor = if (isHome) match.homeGoals else match.awayGoals
            val goalsAgainst = if (isHome) match.awayGoals else match.homeGoals
            val teamName = if (isHome) match.homeTeamName else match.awayTeamName
            
            // 🔄 DATA INTEGRITY CHECK: Log mapping details
            android.util.Log.d("StatsMapper",
                "🔄 Mapping Match: " +
                "TeamID=$teamId, " +
                "isHome=$isHome, " +
                "GoalsFor=$goalsFor, " +
                "GoalsAgainst=$goalsAgainst, " +
                "FixtureID=${match.fixtureId}, " +
                "Weight=${String.format("%.2f", weight)}"
            )
            
            // Apply weight to xG values
            // Goals in friendlies (weight = 0.35) count only 35% toward team strength
            val weightedXgFor = goalsFor.toDouble() * weight
            val weightedXgAgainst = goalsAgainst.toDouble() * weight
            
            TeamStats(
                fixtureId = match.fixtureId,
                teamId = teamId,
                teamName = teamName,
                xgFor = weightedXgFor,
                xgAgainst = weightedXgAgainst,
                isHome = isHome,
                actualGoalsFor = goalsFor,
                actualGoalsAgainst = goalsAgainst,
                matchDate = match.date,
                leagueId = match.leagueId,
                leagueName = match.leagueName,
                weight = weight
            )
        }
    }


    /**
     * Calculate average xG for a team from historical fixtures.
     */
    fun calculateAverageXgForTeam(history: List<HistoricalFixture>, teamId: Int): Pair<Double, Double> {
        val teamStats = mapToTeamStats(history, teamId)
        if (teamStats.isEmpty()) {
            return Pair(0.0, 0.0)
        }
        
        val avgXgFor = teamStats.map { it.xgFor }.average()
        val avgXgAgainst = teamStats.map { it.xgAgainst }.average()
        
        return Pair(avgXgFor, avgXgAgainst)
    }

    /**
     * Calculate form rating for a team based on recent fixtures.
     * Recent matches are weighted more heavily.
     */
    fun calculateFormRating(history: List<HistoricalFixture>, teamId: Int): Double {
        val teamStats = mapToTeamStats(history, teamId)
        if (teamStats.isEmpty()) {
            return 0.5 // Neutral rating
        }
        
        // Weight recent matches more heavily (simple linear decay)
        var totalWeight = 0.0
        var weightedPerformance = 0.0
        
        teamStats.reversed().forEachIndexed { index, stats ->
            val weight = 1.0 / (index + 1) // More recent = higher weight
            val performance = if (stats.xgFor > stats.xgAgainst) 1.0 else 0.5
            
            weightedPerformance += performance * weight
            totalWeight += weight
        }
        
        return if (totalWeight > 0) weightedPerformance / totalWeight else 0.5
    }
}
