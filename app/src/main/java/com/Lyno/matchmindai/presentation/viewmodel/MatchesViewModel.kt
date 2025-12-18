package com.Lyno.matchmindai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Lyno.matchmindai.domain.model.LeagueGroup
import com.Lyno.matchmindai.domain.repository.MatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ViewModel for the MatchesScreen.
 * Handles loading and grouping matches by league with expand/collapse functionality.
 */
class MatchesViewModel(
    private val matchRepository: MatchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchesUiState())
    val uiState: StateFlow<MatchesUiState> = _uiState.asStateFlow()

    init {
        loadMatchesForToday()
    }

    /**
     * Load matches for today's date.
     */
    fun loadMatchesForToday() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())
        loadMatchesByDate(today)
    }

    /**
     * Load matches for a specific date.
     * @param date The date in YYYY-MM-DD format
     */
    fun loadMatchesByDate(date: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            matchRepository.getMatchesByDateGrouped(date)
                .collect { leagueGroups ->
                    _uiState.update { state ->
                        // Merge with existing state to preserve expansion state
                        val mergedGroups = mergeWithExistingState(leagueGroups, state.leagueGroups)
                        state.copy(
                            isLoading = false,
                            leagueGroups = mergedGroups,
                            selectedDate = date
                        )
                    }
                }
        }
    }

    /**
     * Toggle expansion state of a league group.
     * @param leagueId The ID of the league to toggle
     */
    fun toggleLeagueExpansion(leagueId: Int) {
        _uiState.update { state ->
            val updatedGroups = state.leagueGroups.map { group ->
                if (group.leagueId == leagueId) {
                    group.copy(isExpanded = !group.isExpanded)
                } else {
                    group
                }
            }
            state.copy(leagueGroups = updatedGroups)
        }
    }

    /**
     * Collapse all league groups.
     */
    fun collapseAll() {
        _uiState.update { state ->
            val updatedGroups = state.leagueGroups.map { group ->
                group.copy(isExpanded = false)
            }
            state.copy(leagueGroups = updatedGroups)
        }
    }

    /**
     * Expand all league groups.
     */
    fun expandAll() {
        _uiState.update { state ->
            val updatedGroups = state.leagueGroups.map { group ->
                group.copy(isExpanded = true)
            }
            state.copy(leagueGroups = updatedGroups)
        }
    }

    /**
     * Merge new league groups with existing state to preserve expansion state.
     */
    private fun mergeWithExistingState(
        newGroups: List<LeagueGroup>,
        existingGroups: List<LeagueGroup>
    ): List<LeagueGroup> {
        return newGroups.map { newGroup ->
            val existingGroup = existingGroups.find { it.leagueId == newGroup.leagueId }
            if (existingGroup != null) {
                // Preserve expansion state from existing group
                newGroup.copy(isExpanded = existingGroup.isExpanded)
            } else {
                // New group, default to expanded
                newGroup.copy(isExpanded = true)
            }
        }
    }

    /**
     * Clear any error state.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * UI state for the MatchesScreen.
 */
data class MatchesUiState(
    val isLoading: Boolean = false,
    val leagueGroups: List<LeagueGroup> = emptyList(),
    val selectedDate: String = "",
    val error: String? = null
) {
    /**
     * Check if there are any matches to display.
     */
    val hasMatches: Boolean
        get() = leagueGroups.isNotEmpty() && leagueGroups.any { it.matches.isNotEmpty() }

    /**
     * Get total match count across all leagues.
     */
    val totalMatchCount: Int
        get() = leagueGroups.sumOf { it.matches.size }

    /**
     * Get expanded league count.
     */
    val expandedLeagueCount: Int
        get() = leagueGroups.count { it.isExpanded }
}
