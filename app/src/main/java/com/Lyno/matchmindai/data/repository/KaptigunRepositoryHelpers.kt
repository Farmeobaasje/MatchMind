package com.Lyno.matchmindai.data.repository

import com.Lyno.matchmindai.data.remote.football.RateLimitedFootballApiService
import com.Lyno.matchmindai.domain.model.TeamMatchStats
import com.Lyno.matchmindai.domain.service.NewsImpactAnalyzer
import android.util.Log

/**
 * Helper functions for KaptigunRepository implementation.
 * These functions handle the complex data extraction and calculation logic.
 */
object KaptigunRepositoryHelpers {

    /**
     * Extract team match statistics from a fixture.
     */
    fun extractTeamMatchStats(
        fixture: com.Lyno.matchmindai.data.dto.football.FixtureItemDto,
        teamId: Int
    ): TeamMatchStats? {
        return try {
            // This is a simplified implementation
            // In a real implementation, you would extract actual statistics from the fixture
            TeamMatchStats(
                fixtureId = fixture.fixture.id,
                teamId = teamId,
                xg = 0.0, // Would be extracted from fixture statistics
                possession = 50.0, // Would be extracted from fixture statistics
                shotsOnTarget = 0, // Would be extracted from fixture statistics
                totalShots = 0, // Would be extracted from fixture statistics
                passes = 0, // Would be extracted from fixture statistics
                defensiveActions = 0 // Would be extracted from fixture statistics
            )
        } catch (e: Exception) {
            Log.w("KaptigunHelpers", "Error extracting team match stats", e)
            null
        }
    }

    /**
     * Calculate expected goals based on shots data.
     * Formula: (ShotsOnTarget * 0.3) + (ShotsOffTarget * 0.07)
     */
    fun calculateExpectedGoals(
        shotsOnTarget: Int,
        shotsOffTarget: Int
    ): Double {
        return (shotsOnTarget * 0.3) + (shotsOffTarget * 0.07)
    }

    /**
     * Extract expected goals from team statistics.
     */
    fun extractExpectedGoals(teamStats: com.Lyno.matchmindai.data.dto.football.FixtureItemDto): Double? {
        // Simplified implementation - in real app, extract from statistics
        return null
    }

    /**
     * Get sentiment score for a team using NewsImpactAnalyzer.
     */
    suspend fun getSentimentScore(
        newsImpactAnalyzer: NewsImpactAnalyzer,
        teamId: Int,
        teamName: String,
        leagueName: String,
        apiKey: String?
    ): Double {
        return try {
            // If no API key, return neutral sentiment
            if (apiKey.isNullOrEmpty()) {
                Log.w("KaptigunHelpers", "No API key for sentiment analysis, returning neutral")
                return 0.0
            }
            
            // Create a mock MatchDetail for the team
            val mockMatchDetail = com.Lyno.matchmindai.domain.model.MatchDetail(
                fixtureId = 0,
                homeTeam = teamName,
                awayTeam = "Opponent",
                homeTeamId = teamId,
                awayTeamId = 0,
                homeTeamLogo = null,
                awayTeamLogo = null,
                league = leagueName,
                leagueId = 0,
                leagueLogo = null,
                info = com.Lyno.matchmindai.domain.model.MatchInfo(),
                stats = emptyList(),
                lineups = com.Lyno.matchmindai.domain.model.MatchLineups(
                    home = com.Lyno.matchmindai.domain.model.TeamLineup(
                        teamName = teamName,
                        players = emptyList()
                    ),
                    away = com.Lyno.matchmindai.domain.model.TeamLineup(
                        teamName = "Opponent",
                        players = emptyList()
                    )
                ),
                events = emptyList(),
                score = null,
                status = com.Lyno.matchmindai.domain.model.MatchStatus.SCHEDULED,
                standings = null,
                injuries = emptyList(),
                prediction = null,
                odds = null
            )
            
            // Use generateMatchScenario to get simulation context
            val simulationContextResult = newsImpactAnalyzer.generateMatchScenario(
                fixtureId = 0,
                matchDetail = mockMatchDetail,
                apiKey = apiKey
            )
            
            if (simulationContextResult.isSuccess) {
                val context = simulationContextResult.getOrThrow()
                // Calculate sentiment from distraction and fitness
                // Since we passed teamName as homeTeam, use homeDistraction and homeFitness
                val distraction = context.homeDistraction.toDouble()
                val fitness = context.homeFitness.toDouble()
                
                // Convert to sentiment score (-1.0 to 1.0)
                // Higher fitness = positive sentiment, higher distraction = negative sentiment
                val fitnessScore = (fitness - 50.0) / 50.0 // -1.0 to 1.0
                val distractionScore = (50.0 - distraction) / 50.0 // -1.0 to 1.0
                
                (fitnessScore + distractionScore) / 2.0
            } else {
                0.0
            }
        } catch (e: Exception) {
            Log.w("KaptigunHelpers", "Error getting sentiment score for team $teamId", e)
            0.0 // Neutral sentiment if analysis fails
        }
    }

    /**
     * Calculate PPDA (Passes Per Defensive Action).
     * Lower PPDA indicates better pressing.
     */
    fun calculatePpda(passes: Int, defensiveActions: Int): Double {
        return if (defensiveActions > 0) {
            passes.toDouble() / defensiveActions
        } else {
            15.0 // Default value
        }
    }

    /**
     * Calculate average statistics from a list of team match stats.
     */
    fun calculateAverageStats(statsList: List<TeamMatchStats>): TeamMatchStats? {
        if (statsList.isEmpty()) return null

        return TeamMatchStats(
            fixtureId = 0,
            teamId = statsList.first().teamId,
            xg = statsList.map { it.xg }.average(),
            possession = statsList.map { it.possession }.average(),
            shotsOnTarget = statsList.map { it.shotsOnTarget.toDouble() }.average().toInt(),
            totalShots = statsList.map { it.totalShots.toDouble() }.average().toInt(),
            passes = statsList.map { it.passes.toDouble() }.average().toInt(),
            defensiveActions = statsList.map { it.defensiveActions.toDouble() }.average().toInt()
        )
    }

    /**
     * Determine performance label based on match result and xG.
     * Implements Tesseract Twists logic:
     * - Win + (xG Diff > 0.3) -> DOMINANT
     * - Win + (xG Diff < -0.3) -> LUCKY
     * - Loss + (xG Diff > 0.5) -> UNLUCKY
     * - Else -> NEUTRAL
     */
    fun determinePerformanceLabel(
        homeScore: Int,
        awayScore: Int,
        homeXg: Double,
        awayXg: Double
    ): com.Lyno.matchmindai.domain.model.PerformanceLabel {
        val isHomeWin = homeScore > awayScore
        val isAwayWin = awayScore > homeScore
        val xgDifference = homeXg - awayXg

        return when {
            // Home team wins
            isHomeWin && xgDifference > 0.3 -> com.Lyno.matchmindai.domain.model.PerformanceLabel.DOMINANT
            isHomeWin && xgDifference < -0.3 -> com.Lyno.matchmindai.domain.model.PerformanceLabel.LUCKY
            // Away team wins (note: xgDifference is homeXg - awayXg, so negative means away has higher xG)
            isAwayWin && xgDifference < -0.3 -> com.Lyno.matchmindai.domain.model.PerformanceLabel.DOMINANT
            isAwayWin && xgDifference > 0.3 -> com.Lyno.matchmindai.domain.model.PerformanceLabel.LUCKY
            // Loss with higher xG (home team loses but had better xG)
            !isHomeWin && isAwayWin && xgDifference > 0.5 -> com.Lyno.matchmindai.domain.model.PerformanceLabel.UNLUCKY
            // Loss with higher xG (away team loses but had better xG)
            isHomeWin && !isAwayWin && xgDifference < -0.5 -> com.Lyno.matchmindai.domain.model.PerformanceLabel.UNLUCKY
            else -> com.Lyno.matchmindai.domain.model.PerformanceLabel.NEUTRAL
        }
    }

    /**
     * Determine efficiency icon based on goals vs xG.
     */
    fun determineEfficiencyIcon(goals: Int, xg: Double): com.Lyno.matchmindai.domain.model.EfficiencyIcon {
        return when {
            goals > xg + 0.5 -> com.Lyno.matchmindai.domain.model.EfficiencyIcon.CLINICAL
            xg > goals + 0.5 -> com.Lyno.matchmindai.domain.model.EfficiencyIcon.INEFFICIENT
            else -> com.Lyno.matchmindai.domain.model.EfficiencyIcon.BALANCED
        }
    }
}
