package com.Lyno.matchmindai.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Lyno.matchmindai.data.local.FavoritesManager
import com.Lyno.matchmindai.domain.model.FavoriteTeamData
import com.Lyno.matchmindai.domain.model.MatchFixture
import com.Lyno.matchmindai.domain.model.NewsItemData
import com.Lyno.matchmindai.domain.model.LeagueGroup
import com.Lyno.matchmindai.domain.model.StandingRow
import com.Lyno.matchmindai.domain.repository.MatchRepository
import com.Lyno.matchmindai.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * ViewModel for the Favorites screen.
 * Handles showing matches from user's favorite team.
 */
class FavoritesViewModel(
    private val matchRepository: MatchRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FavoritesUiState>(FavoritesUiState.Loading)
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    // State for team news
    private val _teamNewsState = MutableStateFlow<TeamNewsState>(TeamNewsState.Loading)
    val teamNewsState: StateFlow<TeamNewsState> = _teamNewsState.asStateFlow()

    // State for next match
    private val _nextMatchState = MutableStateFlow<NextMatchState>(NextMatchState.Loading)
    val nextMatchState: StateFlow<NextMatchState> = _nextMatchState.asStateFlow()

    // State for league standings
    private val _leagueStandingsState = MutableStateFlow<LeagueStandingsState>(LeagueStandingsState.Loading)
    val leagueStandingsState: StateFlow<LeagueStandingsState> = _leagueStandingsState.asStateFlow()

    // Favorite matches from user's favorite teams
    val favoriteMatches = combine(
        settingsRepository.getUserPreferences(),
        matchRepository.getCachedFixtures()
    ) { preferences, fixtures ->
        val favoriteTeamIds = preferences.dashSettings.selectedTeamIds
        
        if (favoriteTeamIds.isEmpty()) {
            emptyList()
        } else {
            // Filter fixtures for all favorite teams (home or away)
            fixtures.filter { fixture ->
                fixture.homeTeamId in favoriteTeamIds || fixture.awayTeamId in favoriteTeamIds
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Favorite team IDs flow
    val favoriteTeamIds: Flow<Set<Int>> = settingsRepository.getUserPreferences()
        .map { preferences ->
            preferences.dashSettings.selectedTeamIds
        }
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    init {
        android.util.Log.d("FavoritesViewModel", "ViewModel initialized")
        loadFavoriteMatches()
        loadFavoriteTeamData()
    }

    /**
     * Load favorite team data for user's favorite teams.
     * Uses centralized FavoriteTeamData approach for efficient parallel loading.
     */
    private fun loadFavoriteMatches() {
        viewModelScope.launch {
            _uiState.update { FavoritesUiState.Loading }
            
            try {
                // Get user preferences to check favorite teams
                val preferences = settingsRepository.getUserPreferences().first()
                val favoriteTeamIds = preferences.dashSettings.selectedTeamIds
                
                if (favoriteTeamIds.isEmpty()) {
                    // No favorite teams selected
                    _uiState.update { FavoritesUiState.Success(emptyList<FavoriteTeamData>()) }
                    return@launch
                }
                
                android.util.Log.d("FavoritesViewModel", 
                    "Loading favorite teams data for ${favoriteTeamIds.size} teams: $favoriteTeamIds")
                
                // Use centralized method to get all data for all favorite teams
                val favoriteTeamsDataResult = matchRepository.getFavoriteTeamsData(favoriteTeamIds.toList())
                
                if (favoriteTeamsDataResult.isFailure) {
                    val error = favoriteTeamsDataResult.exceptionOrNull()
                    android.util.Log.e("FavoritesViewModel", 
                        "Failed to load favorite teams data: ${error?.message}")
                    _uiState.update { 
                        FavoritesUiState.Error("Kan favoriete teams niet laden: ${error?.message ?: "Onbekende fout"}")
                    }
                    return@launch
                }
                
                val favoriteTeamsData = favoriteTeamsDataResult.getOrNull() ?: emptyList()
                
                android.util.Log.d("FavoritesViewModel", 
                    "Successfully loaded ${favoriteTeamsData.size} favorite teams data")
                
                // Log details for debugging
                favoriteTeamsData.forEachIndexed { index, teamData ->
                    android.util.Log.d("FavoritesViewModel", 
                        "Team ${index + 1}: ${teamData.getDisplayName()} (ID: ${teamData.teamId}) - " +
                        "News: ${teamData.news?.size ?: 0}, " +
                        "Next match: ${if (teamData.nextMatch != null) "Yes" else "No"}, " +
                        "Standings: ${teamData.standings?.size ?: 0}")
                }
                
                _uiState.update { FavoritesUiState.Success(favoriteTeamsData) }
            } catch (e: Exception) {
                android.util.Log.e("FavoritesViewModel", "Error loading favorite teams data", e)
                _uiState.update { 
                    FavoritesUiState.Error("Fout bij laden: ${e.message ?: "Onbekende fout"}")
                }
            }
        }
    }

    /**
     * Load favorite team data (news, next match, standings) for all favorite teams.
     * Uses centralized FavoriteTeamData approach for efficient parallel loading.
     */
    private fun loadFavoriteTeamData() {
        viewModelScope.launch {
            // Get user preferences to check favorite teams
            val preferences = settingsRepository.getUserPreferences().first()
            val favoriteTeamIds = preferences.dashSettings.selectedTeamIds
            
            if (favoriteTeamIds.isEmpty()) {
                // No favorite teams selected
                _teamNewsState.update { TeamNewsState.NoTeamSelected }
                _nextMatchState.update { NextMatchState.NoTeamSelected }
                _leagueStandingsState.update { LeagueStandingsState.NoTeamSelected }
                return@launch
            }

            try {
                // Use centralized method to get all data for all favorite teams
                val favoriteTeamsDataResult = matchRepository.getFavoriteTeamsData(favoriteTeamIds.toList())
                
                if (favoriteTeamsDataResult.isFailure) {
                    // Fallback to old method if centralized approach fails
                    loadFavoriteTeamDataLegacy(favoriteTeamIds)
                    return@launch
                }
                
                val favoriteTeamsData = favoriteTeamsDataResult.getOrNull() ?: emptyList()
                
                if (favoriteTeamsData.isEmpty()) {
                    _teamNewsState.update { TeamNewsState.NoData }
                    _nextMatchState.update { NextMatchState.NoData }
                    _leagueStandingsState.update { LeagueStandingsState.NoData }
                    return@launch
                }
                
                // For now, show data for the first favorite team (maintaining backward compatibility)
                val firstTeamData = favoriteTeamsData.firstOrNull()
                
                if (firstTeamData == null) {
                    _teamNewsState.update { TeamNewsState.NoData }
                    _nextMatchState.update { NextMatchState.NoData }
                    _leagueStandingsState.update { LeagueStandingsState.NoData }
                    return@launch
                }
                
                // Update team news state
                _teamNewsState.update {
                    val news = firstTeamData.news
                    if (news.isNullOrEmpty()) {
                        TeamNewsState.NoData
                    } else {
                        TeamNewsState.Success(news)
                    }
                }

                // Update next match state
                _nextMatchState.update {
                    val nextMatch = firstTeamData.nextMatch
                    if (nextMatch == null) {
                        NextMatchState.NoData
                    } else {
                        NextMatchState.Success(nextMatch)
                    }
                }

                // Update league standings state
                _leagueStandingsState.update {
                    val standings = firstTeamData.standings
                    if (standings.isNullOrEmpty()) {
                        if (firstTeamData.hasStandings) {
                            // Has standings but they're empty (friendlies league)
                            LeagueStandingsState.NoData
                        } else {
                            // No standings available (friendlies league)
                            LeagueStandingsState.NoData
                        }
                    } else {
                        LeagueStandingsState.Success(standings)
                    }
                }
                
                android.util.Log.d("FavoritesViewModel", 
                    "Loaded favorite team data for ${favoriteTeamsData.size} teams using centralized approach")
            } catch (e: Exception) {
                android.util.Log.e("FavoritesViewModel", "Error loading favorite team data", e)
                // Fallback to legacy method
                loadFavoriteTeamDataLegacy(favoriteTeamIds)
            }
        }
    }
    
    /**
     * Legacy method for loading favorite team data (fallback).
     * Shows data for the first favorite team if multiple teams are selected.
     */
    private fun loadFavoriteTeamDataLegacy(favoriteTeamIds: Set<Int>) {
        viewModelScope.launch {
            // Use the first favorite team for news, next match, and standings
            val firstFavoriteTeamId = favoriteTeamIds.firstOrNull()
            
            if (firstFavoriteTeamId == null) {
                _teamNewsState.update { TeamNewsState.NoTeamSelected }
                _nextMatchState.update { NextMatchState.NoTeamSelected }
                _leagueStandingsState.update { LeagueStandingsState.NoTeamSelected }
                return@launch
            }

            // Load all data in parallel
            val newsDeferred = async { matchRepository.getTeamNews(firstFavoriteTeamId) }
            val nextMatchDeferred = async { matchRepository.getNextMatch(firstFavoriteTeamId) }
            val standingsDeferred = async { matchRepository.getLeagueStandingsForTeam(firstFavoriteTeamId) }

            try {
                // Wait for all results
                val newsResult = newsDeferred.await()
                val nextMatchResult = nextMatchDeferred.await()
                val standingsResult = standingsDeferred.await()

                // Update team news state
                _teamNewsState.update {
                    if (newsResult.isSuccess) {
                        val news = newsResult.getOrNull() ?: emptyList()
                        if (news.isEmpty()) {
                            TeamNewsState.NoData
                        } else {
                            TeamNewsState.Success(news)
                        }
                    } else {
                        TeamNewsState.Error("Nieuws niet beschikbaar")
                    }
                }

                // Update next match state
                _nextMatchState.update {
                    if (nextMatchResult.isSuccess) {
                        val nextMatch = nextMatchResult.getOrNull()
                        if (nextMatch == null) {
                            NextMatchState.NoData
                        } else {
                            NextMatchState.Success(nextMatch)
                        }
                    } else {
                        NextMatchState.Error("Volgende wedstrijd niet beschikbaar")
                    }
                }

                // Update league standings state
                _leagueStandingsState.update {
                    if (standingsResult.isSuccess) {
                        val standings = standingsResult.getOrNull()
                        if (standings == null) {
                            LeagueStandingsState.NoData
                        } else {
                            LeagueStandingsState.Success(standings)
                        }
                    } else {
                        LeagueStandingsState.Error("Stand niet beschikbaar")
                    }
                }
            } catch (e: Exception) {
                // Handle any exceptions
                _teamNewsState.update { TeamNewsState.Error("Fout bij laden nieuws") }
                _nextMatchState.update { NextMatchState.Error("Fout bij laden volgende wedstrijd") }
                _leagueStandingsState.update { LeagueStandingsState.Error("Fout bij laden stand") }
            }
        }
    }

    /**
     * Toggle team favorite status.
     */
    fun toggleTeamFavorite(teamId: Int, teamName: String) {
        viewModelScope.launch {
            // TODO: Implement team favorite toggling
            // This should update user preferences with the favorite team
        }
    }

    /**
     * Refresh matches list.
     */
    fun refresh() {
        loadFavoriteMatches()
        loadFavoriteTeamData()
    }
}

/**
 * UI state for the Favorites screen.
 */
sealed interface FavoritesUiState {
    object Loading : FavoritesUiState
    data class Success(val favoriteTeamsData: List<FavoriteTeamData>) : FavoritesUiState
    data class Error(val message: String) : FavoritesUiState
}

/**
 * UI state for team news.
 */
sealed interface TeamNewsState {
    object Loading : TeamNewsState
    object NoTeamSelected : TeamNewsState
    object NoData : TeamNewsState
    data class Success(val news: List<NewsItemData>) : TeamNewsState
    data class Error(val message: String) : TeamNewsState
}

/**
 * UI state for next match.
 */
sealed interface NextMatchState {
    object Loading : NextMatchState
    object NoTeamSelected : NextMatchState
    object NoData : NextMatchState
    data class Success(val match: MatchFixture) : NextMatchState
    data class Error(val message: String) : NextMatchState
}

/**
 * UI state for league standings.
 */
sealed interface LeagueStandingsState {
    object Loading : LeagueStandingsState
    object NoTeamSelected : LeagueStandingsState
    object NoData : LeagueStandingsState
    data class Success(val standings: List<StandingRow>) : LeagueStandingsState
    data class Error(val message: String) : LeagueStandingsState
}
