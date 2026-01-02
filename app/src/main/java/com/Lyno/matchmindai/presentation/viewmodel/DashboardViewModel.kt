package com.Lyno.matchmindai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Lyno.matchmindai.data.local.FavoritesManager
import com.Lyno.matchmindai.data.mapper.MatchMapper
import com.Lyno.matchmindai.data.repository.ApiKeyMissingException
import com.Lyno.matchmindai.domain.model.LeagueGroup
import com.Lyno.matchmindai.domain.model.MatchFixture
import com.Lyno.matchmindai.domain.model.MatchStatus
import com.Lyno.matchmindai.domain.repository.MatchRepository
import com.Lyno.matchmindai.domain.service.CuratedFeed
import com.Lyno.matchmindai.domain.service.MatchCuratorService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * UI state for the Dashboard screen.
 */
sealed interface DashboardUiState {
    object Idle : DashboardUiState
    object Loading : DashboardUiState
    data class Success(
        val curatedFeed: CuratedFeed,
        val isLoading: Boolean = false
    ) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
    object MissingApiKey : DashboardUiState
}

/**
 * ViewModel for the Dashboard screen.
 * Handles curated feed data with MatchCuratorService integration and date navigation.
 */
class DashboardViewModel(
    private val matchRepository: MatchRepository,
    private val favoritesManager: FavoritesManager,
    private val matchCuratorService: MatchCuratorService = MatchCuratorService()
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Idle)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // Curated feed state
    private val _curatedFeed = MutableStateFlow<CuratedFeed>(CuratedFeed())
    val curatedFeed: StateFlow<CuratedFeed> = _curatedFeed.asStateFlow()

    // Standings cache for curator service (empty for now - would need proper standings model)
    private val _standings = MutableStateFlow<List<Any>>(emptyList())
    val standings: StateFlow<List<Any>> = _standings.asStateFlow()

    // Date navigation state (stored as Calendar instance)
    private val _selectedDate = MutableStateFlow(Calendar.getInstance())
    val selectedDate: StateFlow<Calendar> = _selectedDate.asStateFlow()

    // Track expanded/collapsed state for each league (TOP COMPETITIES)
    private val _expandedLeagueStates = MutableStateFlow<Set<Int>>(emptySet())
    val expandedLeagueStates: StateFlow<Set<Int>> = _expandedLeagueStates.asStateFlow()
    
    // Track expanded/collapsed state for upcoming leagues (KOMENDE WEDSTRIJDEN)
    private val _upcomingExpandedLeagueStates = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val upcomingExpandedLeagueStates: StateFlow<Map<Int, Boolean>> = _upcomingExpandedLeagueStates.asStateFlow()
    
    // Combined state for UI with favorites sorting - returns flat list of all matches
    val sortedMatches = combine(
        _curatedFeed,
        favoritesManager.favoriteLeagues
    ) { curatedFeed, favoriteIds ->
        // Combine all matches into a single list for sorting
        val allMatches = mutableListOf<MatchFixture>()
        curatedFeed.heroMatch?.let { allMatches.add(it) }
        allMatches.addAll(curatedFeed.liveMatches)
        allMatches.addAll(curatedFeed.upcomingMatches)
        
        // Sort with favorites first
        sortMatches(allMatches, favoriteIds)
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // League groups for collapsible sections - grouped by league ID with priority sorting
    val leagueGroups = combine<CuratedFeed, Set<String>, Set<Int>, List<LeagueGroup>>(
        _curatedFeed,
        favoritesManager.favoriteLeagues,
        _expandedLeagueStates
    ) { curatedFeed, favoriteIds, expandedStates ->
        // Combine all matches into a single list
        val allMatches = mutableListOf<MatchFixture>()
        curatedFeed.heroMatch?.let { allMatches.add(it) }
        allMatches.addAll(curatedFeed.liveMatches)
        allMatches.addAll(curatedFeed.upcomingMatches)
        
        // Filter for "Top Competities" using hardcoded league IDs
        val topLeagueIds = setOf(39, 88, 140, 78, 135, 61) // PL, Eredivisie, La Liga, Bundesliga, Serie A, Ligue 1
        val filteredMatches = allMatches.filter { match ->
            match.leagueId != null && topLeagueIds.contains(match.leagueId)
        }
        
        // Group by league ID (not by league name to avoid "Premier" contamination)
        val groupedByLeagueId = filteredMatches.groupBy { match ->
            match.leagueId ?: 0
        }
        
        // Convert to LeagueGroup objects
        val leagueGroups = groupedByLeagueId.map { (leagueId, matches) ->
            val firstMatch = matches.firstOrNull()
            LeagueGroup(
                leagueId = leagueId,
                leagueName = firstMatch?.league ?: "",
                country = firstMatch?.leagueCountry ?: "",
                logoUrl = firstMatch?.leagueLogo ?: "",
                matches = matches,
                isExpanded = expandedStates.contains(leagueId)
            )
        }
        
        // Sort by priority (Eredivisie first, then PL, etc.)
        leagueGroups.sortedBy { LeagueGroup.getLeaguePriority(it.leagueId) }
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // Country groups for hierarchical league selection - grouped by country
    val countryGroups = combine<CuratedFeed, Set<String>, Set<Int>, Map<Int, Boolean>, List<com.Lyno.matchmindai.domain.model.CountryGroup>>(
        _curatedFeed,
        favoritesManager.favoriteLeagues,
        _expandedLeagueStates,
        _upcomingExpandedLeagueStates
    ) { curatedFeed, favoriteIds, expandedStates, upcomingExpandedStates ->
        // Combine all matches into a single list
        val allMatches = mutableListOf<MatchFixture>()
        curatedFeed.heroMatch?.let { allMatches.add(it) }
        allMatches.addAll(curatedFeed.liveMatches)
        allMatches.addAll(curatedFeed.upcomingMatches)
        
        // Group by league ID
        val groupedByLeagueId = allMatches.groupBy { match ->
            match.leagueId ?: 0
        }
        
        // Convert to LeagueGroup objects
        val leagueGroups = groupedByLeagueId.map { (leagueId, matches) ->
            val firstMatch = matches.firstOrNull()
            // Determine if league is expanded (check both top and upcoming states)
            val isExpanded = expandedStates.contains(leagueId) || 
                            (upcomingExpandedStates[leagueId] ?: false)
            
            LeagueGroup(
                leagueId = leagueId,
                leagueName = firstMatch?.league ?: "",
                country = firstMatch?.leagueCountry ?: "",
                logoUrl = firstMatch?.leagueLogo ?: "",
                matches = matches,
                isExpanded = isExpanded
            )
        }
        
        // Group leagues by country
        com.Lyno.matchmindai.domain.model.CountryGroup.groupLeaguesByCountry(leagueGroups)
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // Selected countries state
    private val _selectedCountries = MutableStateFlow<Set<String>>(emptySet())
    val selectedCountries: StateFlow<Set<String>> = _selectedCountries.asStateFlow()
    
    // Selected leagues state (for country menu)
    private val _selectedLeagues = MutableStateFlow<Set<Int>>(emptySet())
    val selectedLeagues: StateFlow<Set<Int>> = _selectedLeagues.asStateFlow()
    
    // Public access to favorites manager
    val favoritesManagerPublic = favoritesManager

    // Cache for different dates to avoid unnecessary API calls
    private val _feedCache = mutableMapOf<String, CuratedFeed>()
    private val _fixturesCache = mutableMapOf<String, List<MatchFixture>>()

    // Date formatters
    private val apiDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormatter = SimpleDateFormat("EEE d MMM", Locale("nl", "NL"))

    init {
        // Start auto-refresh loop for curated feed
        startCuratedFeedAutoRefresh()
        
        // Load initial data for today
        viewModelScope.launch {
            _selectedDate.collect { date ->
                loadCuratedFeedForDate(date)
            }
        }
    }

    /**
     * Start the auto-refresh loop for curated feed.
     * Checks every 90 seconds for updates.
     */
    private fun startCuratedFeedAutoRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(90_000) // 90 seconds
                
                // Only refresh if we're in a success state (not loading or error)
                when (val currentState = _uiState.value) {
                    is DashboardUiState.Success -> {
                        // Refresh curated feed for current date
                        loadCuratedFeedForDate(_selectedDate.value)
                    }
                    is DashboardUiState.Idle -> {
                        // Initial load
                        loadCuratedFeedForDate(_selectedDate.value)
                    }
                    else -> {
                        // Don't refresh if we're in loading or error state
                    }
                }
            }
        }
    }

    /**
     * Load curated feed for a specific date from repository and process with MatchCuratorService.
     */
    private fun loadCuratedFeedForDate(date: Calendar) {
        viewModelScope.launch {
            _uiState.update { DashboardUiState.Loading }
            
            try {
                val dateKey = formatDateForApi(date)
                
                // Check cache first
                if (_feedCache.containsKey(dateKey)) {
                    val cachedFeed = _feedCache[dateKey]!!
                    _curatedFeed.update { cachedFeed }
                    _uiState.update {
                        DashboardUiState.Success(
                            curatedFeed = cachedFeed,
                            isLoading = false
                        )
                    }
                    return@launch
                }
                
                // Fetch fixtures for specific date
                val fixturesResult = getFixturesForDate(date)
                
                fixturesResult.fold(
                    onSuccess = { fixtures ->
                        // Cache fixtures
                        _fixturesCache[dateKey] = fixtures
                        
                        // For now, use empty standings since getStandings() returns AgentResponse
                        // In a real implementation, we would need to parse standings from AgentResponse
                        val curated = matchCuratorService.curateMatches(
                            fixtures = fixtures,
                            standings = emptyList()
                        )
                        
                        // Cache curated feed
                        _feedCache[dateKey] = curated
                        
                        _curatedFeed.update { curated }
                        
                        // Update UI state
                        _uiState.update {
                            DashboardUiState.Success(
                                curatedFeed = curated,
                                isLoading = false
                            )
                        }
                    },
                    onFailure = { error ->
                        // Handle specific errors
                        when (error) {
                            is ApiKeyMissingException -> {
                                _uiState.update { DashboardUiState.MissingApiKey }
                            }
                            else -> {
                                _uiState.update { 
                                    DashboardUiState.Error(
                                        error.message ?: "Kon wedstrijden niet ophalen"
                                    ) 
                                }
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { 
                    DashboardUiState.Error(
                        e.message ?: "Onverwachte fout bij laden feed"
                    ) 
                }
            }
        }
    }

    /**
     * Get fixtures for a specific date.
     */
    private suspend fun getFixturesForDate(date: Calendar): Result<List<MatchFixture>> {
        val dateKey = formatDateForApi(date)
        return matchRepository.getFixturesByDate(dateKey)
    }

    /**
     * Change the selected date by adding/subtracting days.
     * @param daysToAdd Number of days to add (negative for previous days)
     */
    fun changeDate(daysToAdd: Int) {
        _selectedDate.update { calendar ->
            val newCalendar = calendar.clone() as Calendar
            newCalendar.add(Calendar.DAY_OF_YEAR, daysToAdd)
            newCalendar
        }
    }

    /**
     * Navigate to today's date.
     */
    fun navigateToToday() {
        _selectedDate.update { Calendar.getInstance() }
    }

    /**
     * Format date for API calls (YYYY-MM-DD).
     */
    private fun formatDateForApi(date: Calendar): String {
        return apiDateFormatter.format(date.time)
    }

    /**
     * Format date for display in UI.
     * Returns "Vandaag", "Gisteren", "Morgen", or formatted date like "Wo 18 Dec".
     */
    fun formatDateForDisplay(date: Calendar): String {
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        
        return when {
            isSameDay(date, today) -> "Vandaag"
            isSameDay(date, yesterday) -> "Gisteren"
            isSameDay(date, tomorrow) -> "Morgen"
            else -> displayDateFormatter.format(date.time)
        }
    }

    /**
     * Check if two Calendar instances represent the same day.
     */
    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Load curated feed (backward compatibility - uses current selected date).
     */
    fun loadCuratedFeed() {
        loadCuratedFeedForDate(_selectedDate.value)
    }

    /**
     * Refresh live matches specifically (for manual refresh).
     */
    fun refreshLiveMatches() {
        viewModelScope.launch {
            val result = matchRepository.getLiveMatches()
            
            result.fold(
                onSuccess = { fixtures ->
                    // Filter for truly live matches using MatchMapper.mapStatus
                    val liveFixtures = fixtures.filter { fixture ->
                        val matchStatus = MatchMapper.mapStatus(fixture.status)
                        matchStatus == MatchStatus.LIVE
                    }
                    
                    // Update curated feed with new live matches
                    val currentFeed = _curatedFeed.value
                    val updatedFeed = currentFeed.copy(
                        liveMatches = liveFixtures
                    )
                    
                    _curatedFeed.update { updatedFeed }
                    
                    // Update UI state if we're in success state
                    if (_uiState.value is DashboardUiState.Success) {
                        _uiState.update {
                            DashboardUiState.Success(
                                curatedFeed = updatedFeed,
                                isLoading = false
                            )
                        }
                    }
                },
                onFailure = { error ->
                    // Don't update UI state for live match refresh failures
                    // Just log the error
                    println("Live match refresh failed: ${error.message}")
                }
            )
        }
    }

    /**
     * Handle match selection from the dashboard.
     */
    fun onMatchSelected(fixture: MatchFixture) {
        // TODO: Navigate to match details or trigger analysis
        // For now, just log the selection
        println("Match selected: ${fixture.homeTeam} vs ${fixture.awayTeam}")
    }

    /**
     * Reset the UI state to Idle.
     */
    fun resetState() {
        _uiState.update { DashboardUiState.Idle }
    }

    /**
     * Clear any error messages.
     */
    fun clearError() {
        if (_uiState.value is DashboardUiState.Error) {
            _uiState.update { DashboardUiState.Idle }
        }
    }

    /**
     * Clear cache for a specific date.
     */
    fun clearCacheForDate(date: Calendar) {
        val dateKey = formatDateForApi(date)
        _feedCache.remove(dateKey)
        _fixturesCache.remove(dateKey)
    }

    /**
     * Clear all caches.
     */
    fun clearAllCaches() {
        _feedCache.clear()
        _fixturesCache.clear()
    }

    /**
     * Check if a match has event coverage for timeline display.
     */
    fun hasEventCoverage(fixture: MatchFixture): Boolean {
        // TODO: Implement based on fixture.coverage object
        // For now, return false
        return false
    }

    /**
     * Get recent events for a match (for timeline display).
     */
    fun getRecentEvents(fixture: MatchFixture): List<String> {
        // TODO: Implement based on fixture.events or API
        // For now, return empty list
        return emptyList()
    }

    /**
     * Sort matches with favorites first.
     * @param matches List of matches to sort
     * @param favoriteIds Set of favorite league IDs as strings
     * @return Sorted list with favorite league matches first
     */
    private fun sortMatches(
        matches: List<MatchFixture>,
        favoriteIds: Set<String>
    ): List<MatchFixture> {
        return matches.sortedWith(
            compareByDescending<MatchFixture> { match ->
                // 1. First sort by: Is it a favorite league? (True comes before False)
                favoriteIds.contains(match.leagueId.toString())
            }
            .thenBy {
                // 2. Then group by league ID (so Premier League stays together)
                it.leagueId
            }
            .thenBy {
                // 3. Finally by date/time
                it.date
            }
        )
    }

    /**
     * Toggle the expanded state for a specific league (TOP COMPETITIES).
     * @param leagueId The ID of the league to toggle
     */
    fun toggleLeagueExpansion(leagueId: Int) {
        _expandedLeagueStates.update { currentStates ->
            if (currentStates.contains(leagueId)) {
                currentStates - leagueId
            } else {
                currentStates + leagueId
            }
        }
    }
    
    /**
     * Toggle the expanded state for a specific upcoming league (KOMENDE WEDSTRIJDEN).
     * @param leagueId The ID of the league to toggle
     */
    fun toggleUpcomingLeagueExpansion(leagueId: Int) {
        _upcomingExpandedLeagueStates.update { currentStates ->
            val currentValue = currentStates[leagueId] ?: false
            currentStates + (leagueId to !currentValue)
        }
    }
    
    /**
     * Toggle country expansion (for country menu).
     * @param countryCode The country code to toggle
     */
    fun toggleCountryExpansion(countryCode: String) {
        // For now, we'll just update selected countries
        // In a real implementation, we would track expanded states separately
        _selectedCountries.update { currentCountries ->
            if (currentCountries.contains(countryCode)) {
                currentCountries - countryCode
            } else {
                currentCountries + countryCode
            }
        }
    }
    
    /**
     * Toggle country selection (select/deselect all leagues in country).
     * @param countryCode The country code to toggle
     */
    fun toggleCountrySelection(countryCode: String) {
        // Get all league IDs for this country
        val countryGroup = countryGroups.value.find { it.countryCode == countryCode }
        countryGroup?.leagues?.forEach { league ->
            toggleLeagueSelection(league.leagueId)
        }
    }
    
    /**
     * Toggle league selection (for country menu).
     * @param leagueId The league ID to toggle
     */
    fun toggleLeagueSelection(leagueId: Int) {
        _selectedLeagues.update { currentLeagues ->
            if (currentLeagues.contains(leagueId)) {
                currentLeagues - leagueId
            } else {
                currentLeagues + leagueId
            }
        }
        
        // Also update the expanded state for consistency
        if (_selectedLeagues.value.contains(leagueId)) {
            // If league is selected, expand it in the appropriate section
            if (leagueId in setOf(39, 88, 140, 78, 135, 61)) {
                // Top league
                toggleLeagueExpansion(leagueId)
            } else {
                // Upcoming league
                toggleUpcomingLeagueExpansion(leagueId)
            }
        }
    }
    
    /**
     * Clear all selections (countries and leagues).
     */
    fun clearAllSelections() {
        _selectedCountries.update { emptySet() }
        _selectedLeagues.update { emptySet() }
    }
    
    /**
     * Apply selected leagues to filter matches.
     * This would be called when user clicks "Toepassen" in the country menu.
     */
    fun applyLeagueSelections() {
        // In a real implementation, this would filter the matches
        // For now, we just log the selections
        println("Applying ${_selectedLeagues.value.size} league selections")
        println("Selected countries: ${_selectedCountries.value}")
        println("Selected leagues: ${_selectedLeagues.value}")
        
        // TODO: Implement match filtering based on selected leagues
        // This would update the curated feed or apply filters
    }
}
