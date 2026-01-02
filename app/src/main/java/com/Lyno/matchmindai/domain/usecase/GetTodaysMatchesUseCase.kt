package com.Lyno.matchmindai.domain.usecase

import com.Lyno.matchmindai.domain.model.MatchFixture
import com.Lyno.matchmindai.domain.repository.MatchRepository

/**
 * Use case for getting today's football match fixtures.
 * Contains validation logic and coordinates with the MatchRepository.
 */
class GetTodaysMatchesUseCase(
    private val matchRepository: MatchRepository
) {
    /**
     * Get today's football match fixtures.
     * @return Result containing either a list of MatchFixture or an error
     */
    suspend operator fun invoke(): Result<List<MatchFixture>> {
        // Call the repository to get today's matches
        return matchRepository.getTodaysMatches()
    }
}
