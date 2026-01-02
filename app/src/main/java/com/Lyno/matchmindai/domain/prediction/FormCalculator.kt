package com.Lyno.matchmindai.domain.prediction

/**
 * Form & Momentum Calculator
 * 
 * Calculates team form based on recent results and generates
 * form correction factors for predictions.
 */
class FormCalculator {

    /**
     * Calculate form correction factor (0.9-1.1).
     * Excellent form = 1.1 (boosts confidence)
     * Terrible form = 0.9 (reduces confidence)
     * 
     * @param recentResults List of recent match results
     * @return Form correction factor
     */
    fun calculateFormCorrection(recentResults: List<MatchResult>): Float {
        if (recentResults.isEmpty()) return 1.0f
        
        val last5 = recentResults.takeLast(5)
        val formScore = calculateFormScore(last5)
        
        return when {
            formScore >= 12 -> 1.1f  // Excellent form (4+ wins in last 5)
            formScore >= 9 -> 1.05f  // Good form (3 wins in last 5)
            formScore >= 6 -> 1.0f   // Average form
            formScore >= 3 -> 0.95f  // Poor form
            else -> 0.9f             // Terrible form (0-1 wins in last 5)
        }
    }

    /**
     * Calculate form score based on recent results.
     * Win = 3 points, Draw = 1 point, Loss = 0 points.
     */
    private fun calculateFormScore(results: List<MatchResult>): Int {
        var totalScore = 0
        for (result in results) {
            totalScore += when (result.outcome) {
                "Win" -> 3
                "Draw" -> 1
                "Loss" -> 0
                else -> 0
            }
        }
        return totalScore
    }

    /**
     * Calculate team-specific form.
     * 
     * @param recentResults List of recent match results
     * @param teamType "HOME" or "AWAY"
     * @return Form description and score
     */
    fun calculateTeamForm(
        recentResults: List<MatchResult>,
        teamType: String
    ): TeamForm {
        val teamResults = recentResults.filter { it.teamType == teamType }
        if (teamResults.isEmpty()) return TeamForm("Unknown", 0, 1.0f)
        
        val formScore = calculateFormScore(teamResults)
        val last5Results = teamResults.takeLast(5)
        
        val formDescription = when {
            formScore >= 12 -> "Excellent"
            formScore >= 9 -> "Good"
            formScore >= 6 -> "Average"
            formScore >= 3 -> "Poor"
            else -> "Terrible"
        }
        
        val formFactor = calculateFormCorrection(teamResults)
        
        return TeamForm(
            description = formDescription,
            score = formScore,
            factor = formFactor,
            recentResults = last5Results.map { it.outcome }
        )
    }

    /**
     * Calculate momentum (trend in form).
     * Positive momentum = improving form
     * Negative momentum = declining form
     */
    fun calculateMomentum(recentResults: List<MatchResult>): Float {
        if (recentResults.size < 3) return 0.0f
        
        // Split into two halves
        val firstHalf = recentResults.take(recentResults.size / 2)
        val secondHalf = recentResults.takeLast(recentResults.size / 2)
        
        val firstHalfScore = calculateFormScore(firstHalf).toFloat() / firstHalf.size
        val secondHalfScore = calculateFormScore(secondHalf).toFloat() / secondHalf.size
        
        return secondHalfScore - firstHalfScore
    }

    /**
     * Get form summary for reasoning.
     */
    fun getFormSummary(recentResults: List<MatchResult>): String {
        if (recentResults.isEmpty()) return "No recent form data"
        
        val last5 = recentResults.takeLast(5)
        val wins = last5.count { it.outcome == "Win" }
        val draws = last5.count { it.outcome == "Draw" }
        val losses = last5.count { it.outcome == "Loss" }
        
        val formScore = calculateFormScore(last5)
        
        return when {
            formScore >= 12 -> "Excellent form (${wins}W ${draws}D ${losses}L in last 5)"
            formScore >= 9 -> "Good form (${wins}W ${draws}D ${losses}L in last 5)"
            formScore >= 6 -> "Average form (${wins}W ${draws}D ${losses}L in last 5)"
            formScore >= 3 -> "Poor form (${wins}W ${draws}D ${losses}L in last 5)"
            else -> "Terrible form (${wins}W ${draws}D ${losses}L in last 5)"
        }
    }

    /**
     * Calculate home/away form specifically.
     */
    fun calculateHomeAwayForm(
        recentResults: List<MatchResult>,
        isHome: Boolean
    ): TeamForm {
        val location = if (isHome) "Home" else "Away"
        val locationResults = recentResults.filter { it.location == location }
        
        return calculateTeamForm(locationResults, if (isHome) "HOME" else "AWAY")
    }

    /**
     * Quick form assessment for the 3-0 bias fix.
     */
    fun isPoorForm(recentResults: List<MatchResult>): Boolean {
        if (recentResults.isEmpty()) return false
        
        val last3 = recentResults.takeLast(3)
        val formScore = calculateFormScore(last3)
        
        return formScore <= 3  // 0-1 wins in last 3 matches
    }

    /**
     * Check if team is on a winning streak.
     */
    fun isWinningStreak(recentResults: List<MatchResult>, streakLength: Int = 3): Boolean {
        if (recentResults.size < streakLength) return false
        
        val lastMatches = recentResults.takeLast(streakLength)
        return lastMatches.all { it.outcome == "Win" }
    }

    /**
     * Check if team is on a losing streak.
     */
    fun isLosingStreak(recentResults: List<MatchResult>, streakLength: Int = 3): Boolean {
        if (recentResults.size < streakLength) return false
        
        val lastMatches = recentResults.takeLast(streakLength)
        return lastMatches.all { it.outcome == "Loss" }
    }
}

/**
 * Data class for match result.
 */
data class MatchResult(
    val outcome: String, // Win, Draw, Loss
    val teamType: String, // HOME or AWAY
    val location: String, // Home, Away, Neutral
    val score: String? = null,
    val date: String? = null
)

/**
 * Data class for team form analysis.
 */
data class TeamForm(
    val description: String,
    val score: Int,
    val factor: Float,
    val recentResults: List<String> = emptyList()
)
