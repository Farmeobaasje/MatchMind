package com.Lyno.matchmindai.domain.repository

import com.Lyno.matchmindai.domain.model.MatchPrediction

/**
 * Interface for match prediction operations.
 * Domain layer contract that defines how the presentation layer can request predictions.
 */
interface MatchRepository {
    /**
     * Get a prediction for a match between two teams.
     * @param homeTeam The home team name
     * @param awayTeam The away team name
     * @return Result containing either a MatchPrediction or an error
     */
    suspend fun getPrediction(homeTeam: String, awayTeam: String): Result<MatchPrediction>
}
