package com.Lyno.matchmindai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Lyno.matchmindai.domain.model.TeamSelectionResult
import com.Lyno.matchmindai.domain.repository.SettingsRepository
import com.Lyno.matchmindai.domain.usecase.SearchTeamsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for TeamSelectionScreen that manages team search and selection.
 */
class TeamSelectionViewModel(
    private val searchTeamsUseCase: SearchTeamsUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeamSelectionUiState())
    val uiState: StateFlow<TeamSelectionUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        // Observe search query changes with proper debouncing and distinct checking
        viewModelScope.launch {
            _uiState
                .debounce(400) // Debounce to avoid too many API calls
                .distinctUntilChanged { old, new -> 
                    old.searchQuery == new.searchQuery && 
                    old.isLoading == new.isLoading
                }
                .filter { state -> 
                    state.searchQuery.length >= 3 && !state.isLoading
                }
                .collect { state ->
                    val query = state.searchQuery
                    // Clear previous search results and start loading
                    _uiState.update { it.copy(isLoading = true, searchResults = emptyList()) }
                    
                    viewModelScope.launch {
                        try {
                            val results = searchTeamsUseCase.invoke(query).first()
                            _uiState.update { it.copy(
                                searchResults = results,
                                isLoading = false,
                                error = if (results.isEmpty() && query.isNotEmpty()) {
                                    "Geen teams gevonden voor '$query'"
                                } else null
                            ) }
                        } catch (e: Exception) {
                            _uiState.update { it.copy(
                                isLoading = false,
                                error = "Fout bij zoeken: ${e.message}"
                            ) }
                        }
                    }
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectTeam(team: TeamSelectionResult) {
        _uiState.update { it.copy(selectedTeam = team) }
    }

    fun saveSelectedTeam() {
        val selectedTeam = _uiState.value.selectedTeam
        if (selectedTeam != null) {
            viewModelScope.launch {
                _uiState.update { it.copy(isSaving = true, error = null) }
                try {
                    // Update user preferences with selected team in dashSettings.selectedTeamIds
                    val currentPrefs = settingsRepository.getUserPreferences().first()
                    
                    // Add team ID to selectedTeamIds (Set<Int>)
                    val currentTeamIds = currentPrefs.dashSettings.selectedTeamIds
                    val updatedTeamIds = currentTeamIds + selectedTeam.teamId
                    
                    // Update dashSettings with new team IDs
                    val updatedDashSettings = currentPrefs.dashSettings.copy(
                        selectedTeamIds = updatedTeamIds
                    )
                    
                    // Also keep legacy favoriteTeamId for backward compatibility
                    val updatedPrefs = currentPrefs.copy(
                        favoriteTeamId = selectedTeam.teamId.toString(),
                        favoriteTeamName = selectedTeam.teamName,
                        dashSettings = updatedDashSettings
                    )
                    
                    settingsRepository.updateUserPreferences(updatedPrefs)
                    
                    _uiState.update { it.copy(
                        isSaving = false,
                        saveSuccess = true,
                        error = null
                    ) }
                    
                    // Clear success message after 2 seconds
                    viewModelScope.launch {
                        delay(2000)
                        _uiState.update { it.copy(saveSuccess = false) }
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(
                        isSaving = false,
                        error = "Fout bij opslaan: ${e.message}"
                    ) }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
}

/**
 * UI State for TeamSelectionScreen.
 */
data class TeamSelectionUiState(
    val searchQuery: String = "",
    val searchResults: List<TeamSelectionResult> = emptyList(),
    val selectedTeam: TeamSelectionResult? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)
