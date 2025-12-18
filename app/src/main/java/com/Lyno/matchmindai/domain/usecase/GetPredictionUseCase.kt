package com.Lyno.matchmindai.domain.usecase

import com.Lyno.matchmindai.domain.model.MatchPrediction
import com.Lyno.matchmindai.domain.repository.MatchRepository

/**
 * Use case for getting match predictions.
 * Contains validation logic and coordinates with the MatchRepository.
 */
class GetPredictionUseCase(
    private val matchRepository: MatchRepository
) {
    /**
     * Get a prediction for a match between two teams.
     * @param homeTeam The home team name
     * @param awayTeam The away team name
     * @return Result containing either a MatchPrediction or an error
     */
    suspend operator fun invoke(homeTeam: String, awayTeam: String): Result<MatchPrediction> {
        // Validation: Check if team names are not empty
        if (homeTeam.isBlank() || awayTeam.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Team names cannot be empty")
            )
        }

        // Call the repository to get match analysis (closest to prediction)
        val result = matchRepository.getMatchAnalysis(homeTeam, awayTeam)
        
        return result.map { agentResponse ->
            // Convert AgentResponse to MatchPrediction
            // Extract winner from analysis text (simplified)
            val winner = extractWinnerFromAnalysis(agentResponse.text, homeTeam, awayTeam)
            
            MatchPrediction(
                fixtureId = 0, // Unknown fixture ID
                homeTeam = homeTeam,
                awayTeam = awayTeam,
                homeWinProbability = 0.33, // Default equal probability
                drawProbability = 0.34,
                awayWinProbability = 0.33,
                expectedGoalsHome = 1.5, // Default expected goals
                expectedGoalsAway = 1.5,
                analysis = agentResponse.text,
                winner = winner,
                confidenceScore = 50, // Default confidence (50%)
                riskLevel = MatchPrediction.RiskLevel.MEDIUM,
                reasoning = agentResponse.text,
                keyFactor = "Recent form and head-to-head",
                recentMatches = emptyList(),
                sources = emptyList(),
                suggestedActions = emptyList()
            )
        }
    }
    
    /**
     * Simple helper to extract winner from analysis text.
     */
    private fun extractWinnerFromAnalysis(analysis: String, homeTeam: String, awayTeam: String): String {
        // Very simplified logic - in production would use NLP
        val lowerAnalysis = analysis.lowercase()
        val lowerHome = homeTeam.lowercase()
        val lowerAway = awayTeam.lowercase()
        
        return when {
            lowerAnalysis.contains("wint $lowerHome") || lowerAnalysis.contains("$lowerHome wint") -> homeTeam
            lowerAnalysis.contains("wint $lowerAway") || lowerAnalysis.contains("$lowerAway wint") -> awayTeam
            lowerAnalysis.contains("gelijkspel") || lowerAnalysis.contains("draw") -> "Draw"
            else -> "Unknown"
        }
    }

    /**
     * Get a prediction based on a natural language query.
     * Accepts free text like "Voorspel Ajax Feyenoord" or "Is Real Madrid in vorm?".
     * @param query The natural language query
     * @param sessionId Optional session ID for conversation context
     * @return Result containing either a MatchPrediction or an error
     */
    suspend fun getPredictionFromQuery(query: String, sessionId: String? = null): Result<MatchPrediction> {
        // Validation: Check if query is not empty
        if (query.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Query cannot be empty")
            )
        }

        // Call the repository to process the user query
        val result = matchRepository.processUserQuery(query)
        
        return result.map { agentResponse ->
            // Extract teams from query (simplified)
            val teams = extractTeamsFromQuery(query)
            
            // Extract winner from analysis text
            val winner = extractWinnerFromAnalysis(agentResponse.text, teams.first, teams.second)
            
            MatchPrediction(
                fixtureId = 0, // Unknown fixture ID
                homeTeam = teams.first,
                awayTeam = teams.second,
                homeWinProbability = 0.33, // Default equal probability
                drawProbability = 0.34,
                awayWinProbability = 0.33,
                expectedGoalsHome = 1.5, // Default expected goals
                expectedGoalsAway = 1.5,
                analysis = agentResponse.text,
                winner = winner,
                confidenceScore = 50, // Default confidence (50%)
                riskLevel = MatchPrediction.RiskLevel.MEDIUM,
                reasoning = agentResponse.text,
                keyFactor = "AI analysis based on query",
                recentMatches = emptyList(),
                sources = emptyList(),
                suggestedActions = emptyList()
            )
        }
    }
    
    /**
     * Simple helper to extract team names from query.
     * This is a simplified version - in production would use NLP.
     */
    private fun extractTeamsFromQuery(query: String): Pair<String, String> {
        val words = query.split(" ")
        // Look for common patterns like "Ajax vs Feyenoord"
        val vsIndex = words.indexOfFirst { it.equals("vs", ignoreCase = true) || it.equals("-") }
        
        return if (vsIndex > 0 && vsIndex < words.size - 1) {
            val homeTeam = words.take(vsIndex).joinToString(" ")
            val awayTeam = words.drop(vsIndex + 1).joinToString(" ")
            Pair(homeTeam, awayTeam)
        } else {
            // Default to generic teams
            Pair("Home Team", "Away Team")
        }
    }
}
