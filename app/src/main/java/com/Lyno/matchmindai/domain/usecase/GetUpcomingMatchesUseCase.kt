package com.Lyno.matchmindai.domain.usecase

import com.Lyno.matchmindai.domain.model.MatchFixture
import com.Lyno.matchmindai.domain.repository.MatchRepository

/**
 * Use case for getting upcoming football match fixtures for the next 3 days.
 * Contains validation logic and coordinates with the MatchRepository.
 */
class GetUpcomingMatchesUseCase(
    private val matchRepository: MatchRepository
) {
    /**
     * Get upcoming football match fixtures for the next 3 days.
     * @return Result containing either a list of MatchFixture or an error
     */
    suspend operator fun invoke(): Result<List<MatchFixture>> {
        // Call the repository to get upcoming matches
        return matchRepository.getUpcomingMatches()
    }
}
