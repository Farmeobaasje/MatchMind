package com.Lyno.matchmindai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Lyno.matchmindai.data.local.FavoritesManager
import com.Lyno.matchmindai.domain.model.CountryGroup
import com.Lyno.matchmindai.domain.model.LeagueGroup
import com.Lyno.matchmindai.domain.model.MatchFixture
import com.Lyno.matchmindai.domain.service.MatchCuratorService
import com.Lyno.matchmindai.presentation.screens.TeamInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for LeagueTeamBrowserScreen.
 * Manages country/league expansion states and team favorite toggles.
 */
class LeagueTeamBrowserViewModel @Inject constructor(
    private val matchCuratorService: MatchCuratorService,
    private val favoritesManager: FavoritesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeagueTeamBrowserUiState())
    val uiState: StateFlow<LeagueTeamBrowserUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
        loadFavoriteTeams()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // Get matches from repository (simplified for now)
            val matches = emptyList<MatchFixture>()
            
            // Extract country groups from matches
            val countryGroups = extractCountryGroups(matches)
            
            _uiState.update { state ->
                state.copy(
                    countryGroups = countryGroups,
                    allTeams = extractAllTeams(matches)
                )
            }
        }
    }

    private fun loadFavoriteTeams() {
        viewModelScope.launch {
            val favoriteTeamIds = favoritesManager.getFavoriteTeamIds()
            _uiState.update { state ->
                state.copy(favoriteTeamIds = favoriteTeamIds.toSet())
            }
        }
    }

    fun toggleCountryExpansion(countryCode: String) {
        _uiState.update { state ->
            val currentExpanded = state.expandedCountries.toMutableSet()
            if (currentExpanded.contains(countryCode)) {
                currentExpanded.remove(countryCode)
            } else {
                currentExpanded.add(countryCode)
            }
            state.copy(expandedCountries = currentExpanded)
        }
    }

    fun toggleCountrySelection(countryCode: String) {
        _uiState.update { state ->
            val currentSelected = state.selectedCountries.toMutableSet()
            if (currentSelected.contains(countryCode)) {
                currentSelected.remove(countryCode)
            } else {
                currentSelected.add(countryCode)
            }
            state.copy(selectedCountries = currentSelected)
        }
    }

    fun toggleTeamFavorite(teamId: Int, teamName: String) {
        viewModelScope.launch {
            favoritesManager.toggleFavoriteTeam(teamId)
            
            // Update local state
            _uiState.update { state ->
                val currentFavorites = state.favoriteTeamIds.toMutableSet()
                if (currentFavorites.contains(teamId)) {
                    currentFavorites.remove(teamId)
                } else {
                    currentFavorites.add(teamId)
                }
                state.copy(favoriteTeamIds = currentFavorites)
            }
        }
    }

    fun saveFavorites() {
        // Favorites are already saved via toggleTeamFavorite
        // This method is for UI feedback only
        viewModelScope.launch {
            // Reload favorites to ensure consistency
            loadFavoriteTeams()
        }
    }

    private fun extractCountryGroups(matches: List<MatchFixture>): List<CountryGroup> {
        // Group matches by country
        val matchesByCountry = matches.groupBy { it.leagueCountry ?: "Unknown" }
        
        return matchesByCountry.map { (country, countryMatches) ->
            // Group matches by league within country
            val matchesByLeague = countryMatches.groupBy { it.leagueId ?: 0 }
            
            val leagues = matchesByLeague.map { (leagueId, leagueMatches) ->
                val firstMatch = leagueMatches.firstOrNull()
                LeagueGroup(
                    leagueId = leagueId,
                    leagueName = firstMatch?.league ?: "Unknown League",
                    country = firstMatch?.leagueCountry ?: country,
                    logoUrl = firstMatch?.leagueLogo ?: "",
                    matches = leagueMatches,
                    isExpanded = false
                )
            }
            
            val firstMatch = countryMatches.firstOrNull()
            val countryCode = country.take(2).uppercase()
            CountryGroup(
                countryCode = countryCode,
                countryName = firstMatch?.leagueCountry ?: country,
                flagEmoji = CountryGroup.getFlagEmoji(countryCode),
                leagues = leagues,
                isExpanded = false,
                isSelected = false
            )
        }.sortedBy { it.countryName }
    }

    private fun extractAllTeams(matches: List<MatchFixture>): List<TeamInfo> {
        return matches.flatMap { match ->
            listOfNotNull(
                match.homeTeamId?.let { id ->
                    TeamInfo(id, match.homeTeam, "")
                },
                match.awayTeamId?.let { id ->
                    TeamInfo(id, match.awayTeam, "")
                }
            )
        }.distinctBy { it.teamId }
    }
}

/**
 * UI State for LeagueTeamBrowserScreen.
 */
data class LeagueTeamBrowserUiState(
    val countryGroups: List<CountryGroup> = emptyList(),
    val expandedCountries: Set<String> = emptySet(),
    val selectedCountries: Set<String> = emptySet(),
    val favoriteTeamIds: Set<Int> = emptySet(),
    val allTeams: List<TeamInfo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
