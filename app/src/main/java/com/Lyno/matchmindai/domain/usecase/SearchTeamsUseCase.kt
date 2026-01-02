package com.Lyno.matchmindai.domain.usecase

import com.Lyno.matchmindai.domain.model.TeamSelectionResult
import com.Lyno.matchmindai.domain.repository.MatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Use case for searching teams by name.
 * This use case handles the business logic for team search functionality.
 */
class SearchTeamsUseCase(
    private val matchRepository: MatchRepository
) {
    /**
     * Search for teams by name.
     * 
     * @param query The search query (team name)
     * @return Flow of team search results
     */
    suspend operator fun invoke(query: String): Flow<List<TeamSelectionResult>> = flow {
        if (query.length < 3) {
            // Don't search for queries shorter than 3 characters (API requirement)
            emit(emptyList())
            return@flow
        }
        
        try {
            // Get API key from repository
            val apiKey = matchRepository.getApiSportsApiKey()
            if (apiKey.isBlank()) {
                emit(emptyList())
                return@flow
            }
            
            // Use real API search
            val searchResults = matchRepository.searchTeams(apiKey, query)
            emit(searchResults)
        } catch (e: Exception) {
            // Log error and rethrow to be handled by ViewModel
            println("Error searching teams: ${e.message}")
            throw e // Re-throw to be handled by ViewModel
        }
    }
    
}
