package com.Lyno.matchmindai.data.utils

import android.util.Log
import com.Lyno.matchmindai.data.dto.FixtureResponse
import com.Lyno.matchmindai.data.dto.TeamsDto
import com.Lyno.matchmindai.data.dto.GoalsDto
import com.Lyno.matchmindai.domain.model.TeamStatistics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Helper class for calculating tournament statistics from fixtures.
 * Used when official team statistics are not available from API.
 */
class TournamentStatisticsHelper {

    companion object {
        private const val TAG = "TournamentStatisticsHelper"
        private const val MIN_FIXTURES_FOR_STATS = 3
        private const val MAX_FIXTURES_FOR_STATS = 10
    }

    /**
     * Calculate team statistics from recent fixtures.
     * 
     * @param teamId Team ID to calculate statistics for
     * @param fixtures List of recent fixtures (should include matches for this team)
     * @param season Current season (for filtering)
     * @return TeamStatistics object with calculated stats
     */
    fun calculateTeamStatisticsFromFixtures(
        teamId: Int,
        fixtures: List<FixtureResponse>,
        season: Int
    ): TeamStatistics {
        Log.d(TAG, "🧮 Calculating statistics for team $teamId from ${fixtures.size} fixtures")
        
        var played = 0
        var wins = 0
        var draws = 0
        var losses = 0
        var goalsFor = 0
        var goalsAgainst = 0
        
        // Filter fixtures for this team and calculate stats
        fixtures.forEach { fixture ->
            val homeTeamId = fixture.teams?.home?.id
            val awayTeamId = fixture.teams?.away?.id
            
            // Check if this fixture involves our team
            if (homeTeamId == teamId || awayTeamId == teamId) {
                val homeGoals = fixture.goals?.home ?: 0
                val awayGoals = fixture.goals?.away ?: 0
                
                val isHome = homeTeamId == teamId
                val teamGoals = if (isHome) homeGoals else awayGoals
                val opponentGoals = if (isHome) awayGoals else homeGoals
                
                played++
                goalsFor += teamGoals
                goalsAgainst += opponentGoals
                
                when {
                    teamGoals > opponentGoals -> wins++
                    teamGoals < opponentGoals -> losses++
                    else -> draws++
                }
            }
        }
        
        val points = (wins * 3) + draws
        val goalDiff = goalsFor - goalsAgainst
        
        // Calculate averages
        val avgGoalsFor = if (played > 0) goalsFor.toFloat() / played else 0f
        val avgGoalsAgainst = if (played > 0) goalsAgainst.toFloat() / played else 0f
        val winPercentage = if (played > 0) (wins.toFloat() / played) * 100 else 0f
        val drawPercentage = if (played > 0) (draws.toFloat() / played) * 100 else 0f
        val lossPercentage = if (played > 0) (losses.toFloat() / played) * 100 else 0f
        
        Log.d(TAG, "📊 Calculated stats for team $teamId: " +
                  "P=$played, W=$wins, D=$draws, L=$losses, " +
                  "GF=$goalsFor, GA=$goalsAgainst, GD=$goalDiff, PTS=$points")
        
        // Calculate form results
        val formResults = mutableListOf<com.Lyno.matchmindai.domain.model.MatchResult>()
        fixtures.forEach { fixture ->
            val homeTeamId = fixture.teams?.home?.id
            val awayTeamId = fixture.teams?.away?.id
            
            if (homeTeamId == teamId || awayTeamId == teamId) {
                val homeGoals = fixture.goals?.home ?: 0
                val awayGoals = fixture.goals?.away ?: 0
                
                val isHome = homeTeamId == teamId
                val teamGoals = if (isHome) homeGoals else awayGoals
                val opponentGoals = if (isHome) awayGoals else homeGoals
                
                val result = when {
                    teamGoals > opponentGoals -> com.Lyno.matchmindai.domain.model.MatchResult.WIN
                    teamGoals < opponentGoals -> com.Lyno.matchmindai.domain.model.MatchResult.LOSS
                    else -> com.Lyno.matchmindai.domain.model.MatchResult.DRAW
                }
                formResults.add(result)
            }
        }
        
        return TeamStatistics(
            teamId = teamId,
            played = played,
            wins = wins,
            draws = draws,
            losses = losses,
            points = points,
            goalsFor = goalsFor,
            goalsAgainst = goalsAgainst,
            goalDiff = goalDiff,
            form = formResults.takeLast(5)
        )
    }

    /**
     * Calculate statistics for both teams in a match.
     * 
     * @param homeTeamId Home team ID
     * @param awayTeamId Away team ID
     * @param fixtures List of recent fixtures for both teams
     * @param season Current season
     * @return Pair of home and away team statistics
     */
    fun calculateMatchStatistics(
        homeTeamId: Int,
        awayTeamId: Int,
        fixtures: List<FixtureResponse>,
        season: Int
    ): Pair<TeamStatistics, TeamStatistics> {
        // Filter fixtures for each team
        val homeFixtures = fixtures.filter { 
            it.teams?.home?.id == homeTeamId || it.teams?.away?.id == homeTeamId 
        }
        
        val awayFixtures = fixtures.filter { 
            it.teams?.home?.id == awayTeamId || it.teams?.away?.id == awayTeamId 
        }
        
        val homeStats = calculateTeamStatisticsFromFixtures(homeTeamId, homeFixtures, season)
        val awayStats = calculateTeamStatisticsFromFixtures(awayTeamId, awayFixtures, season)
        
        return Pair(homeStats, awayStats)
    }

    /**
     * Calculate head-to-head statistics between two teams.
     * 
     * @param homeTeamId Home team ID
     * @param awayTeamId Away team ID
     * @param fixtures List of historical fixtures between the teams
     * @return HeadToHeadStats object with head-to-head data
     */
    fun calculateHeadToHeadStats(
        homeTeamId: Int,
        awayTeamId: Int,
        fixtures: List<FixtureResponse>
    ): HeadToHeadStats {
        var totalMatches = 0
        var homeWins = 0
        var awayWins = 0
        var draws = 0
        var homeGoals = 0
        var awayGoals = 0
        
        fixtures.forEach { fixture ->
            val fixtureHomeId = fixture.teams?.home?.id
            val fixtureAwayId = fixture.teams?.away?.id
            
            // Check if this is a match between our two teams
            if ((fixtureHomeId == homeTeamId && fixtureAwayId == awayTeamId) ||
                (fixtureHomeId == awayTeamId && fixtureAwayId == homeTeamId)) {
                
                totalMatches++
                val homeGoalsInMatch = fixture.goals?.home ?: 0
                val awayGoalsInMatch = fixture.goals?.away ?: 0
                
                // Determine which team is which in this fixture
                val isHomeTeamHome = fixtureHomeId == homeTeamId
                val actualHomeGoals = if (isHomeTeamHome) homeGoalsInMatch else awayGoalsInMatch
                val actualAwayGoals = if (isHomeTeamHome) awayGoalsInMatch else homeGoalsInMatch
                
                homeGoals += actualHomeGoals
                awayGoals += actualAwayGoals
                
                when {
                    actualHomeGoals > actualAwayGoals -> homeWins++
                    actualHomeGoals < actualAwayGoals -> awayWins++
                    else -> draws++
                }
            }
        }
        
        return HeadToHeadStats(
            totalMatches = totalMatches,
            homeWins = homeWins,
            awayWins = awayWins,
            draws = draws,
            homeGoals = homeGoals,
            awayGoals = awayGoals,
            homeWinPercentage = if (totalMatches > 0) (homeWins.toFloat() / totalMatches) * 100 else 0f,
            awayWinPercentage = if (totalMatches > 0) (awayWins.toFloat() / totalMatches) * 100 else 0f,
            drawPercentage = if (totalMatches > 0) (draws.toFloat() / totalMatches) * 100 else 0f,
            avgHomeGoals = if (totalMatches > 0) homeGoals.toFloat() / totalMatches else 0f,
            avgAwayGoals = if (totalMatches > 0) awayGoals.toFloat() / totalMatches else 0f
        )
    }

    /**
     * Calculate form string (e.g., "WWDLW") from recent fixtures.
     */
    private fun calculateFormString(fixtures: List<FixtureResponse>, teamId: Int): String {
        val form = StringBuilder()
        
        // Take last 5 fixtures (or fewer if not available)
        val recentFixtures = fixtures.takeLast(5).reversed()
        
        recentFixtures.forEach { fixture ->
            val homeTeamId = fixture.teams?.home?.id
            val awayTeamId = fixture.teams?.away?.id
            
            if (homeTeamId == teamId || awayTeamId == teamId) {
                val homeGoals = fixture.goals?.home ?: 0
                val awayGoals = fixture.goals?.away ?: 0
                
                val isHome = homeTeamId == teamId
                val teamGoals = if (isHome) homeGoals else awayGoals
                val opponentGoals = if (isHome) awayGoals else homeGoals
                
                when {
                    teamGoals > opponentGoals -> form.append('W')
                    teamGoals < opponentGoals -> form.append('L')
                    else -> form.append('D')
                }
            }
        }
        
        return form.toString()
    }

    /**
     * Calculate number of clean sheets.
     */
    private fun calculateCleanSheets(fixtures: List<FixtureResponse>, teamId: Int): Int {
        return fixtures.count { fixture ->
            val homeTeamId = fixture.teams?.home?.id
            val awayTeamId = fixture.teams?.away?.id
            
            if (homeTeamId == teamId || awayTeamId == teamId) {
                val homeGoals = fixture.goals?.home ?: 0
                val awayGoals = fixture.goals?.away ?: 0
                
                val isHome = homeTeamId == teamId
                val opponentGoals = if (isHome) awayGoals else homeGoals
                
                opponentGoals == 0
            } else {
                false
            }
        }
    }

    /**
     * Calculate number of matches where team failed to score.
     */
    private fun calculateFailedToScore(fixtures: List<FixtureResponse>, teamId: Int): Int {
        return fixtures.count { fixture ->
            val homeTeamId = fixture.teams?.home?.id
            val awayTeamId = fixture.teams?.away?.id
            
            if (homeTeamId == teamId || awayTeamId == teamId) {
                val homeGoals = fixture.goals?.home ?: 0
                val awayGoals = fixture.goals?.away ?: 0
                
                val isHome = homeTeamId == teamId
                val teamGoals = if (isHome) homeGoals else awayGoals
                
                teamGoals == 0
            } else {
                false
            }
        }
    }

    /**
     * Check if we have enough fixtures for reliable statistics.
     */
    fun hasEnoughFixturesForStats(fixtures: List<FixtureResponse>, teamId: Int): Boolean {
        val teamFixtures = fixtures.count { 
            it.teams?.home?.id == teamId || it.teams?.away?.id == teamId 
        }
        return teamFixtures >= MIN_FIXTURES_FOR_STATS
    }

    /**
     * Get confidence level for calculated statistics.
     * Based on number of fixtures available.
     */
    fun getStatisticsConfidence(fixtures: List<FixtureResponse>, teamId: Int): Float {
        val teamFixtures = fixtures.count { 
            it.teams?.home?.id == teamId || it.teams?.away?.id == teamId 
        }
        
        return when {
            teamFixtures >= MAX_FIXTURES_FOR_STATS -> 0.9f  // High confidence
            teamFixtures >= MIN_FIXTURES_FOR_STATS -> 0.7f  // Medium confidence
            teamFixtures > 0 -> 0.5f  // Low confidence
            else -> 0.3f  // Very low confidence
        }
    }

    /**
     * Head-to-head statistics container.
     */
    data class HeadToHeadStats(
        val totalMatches: Int,
        val homeWins: Int,
        val awayWins: Int,
        val draws: Int,
        val homeGoals: Int,
        val awayGoals: Int,
        val homeWinPercentage: Float,
        val awayWinPercentage: Float,
        val drawPercentage: Float,
        val avgHomeGoals: Float,
        val avgAwayGoals: Float
    ) {
        /**
         * Get dominance indicator based on head-to-head record.
         */
        fun getDominance(): String {
            return when {
                homeWinPercentage > 60 -> "Home team dominates"
                awayWinPercentage > 60 -> "Away team dominates"
                homeWinPercentage > awayWinPercentage -> "Home team has slight edge"
                awayWinPercentage > homeWinPercentage -> "Away team has slight edge"
                else -> "Evenly matched"
            }
        }
        
        /**
         * Get goal dominance indicator.
         */
        fun getGoalDominance(): String {
            val goalDifference = homeGoals - awayGoals
            return when {
                goalDifference > 10 -> "Home team scores significantly more"
                goalDifference > 5 -> "Home team scores more"
                goalDifference < -10 -> "Away team scores significantly more"
                goalDifference < -5 -> "Away team scores more"
                else -> "Goals are evenly distributed"
            }
        }
    }
}
