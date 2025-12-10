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

        // Call the repository to get the prediction
        return matchRepository.getPrediction(homeTeam, awayTeam)
    }
}
