package com.Lyno.matchmindai.presentation.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.Lyno.matchmindai.presentation.AppViewModelProvider
import com.Lyno.matchmindai.presentation.navigation.Screen
import com.Lyno.matchmindai.presentation.components.ExpandableLeagueSection
import com.Lyno.matchmindai.presentation.viewmodel.MatchesViewModel
import kotlinx.coroutines.launch

/**
 * Screen for displaying matches grouped by league with expand/collapse functionality.
 * Similar to FlashScore's interface.
 */
@Composable
fun MatchesScreen(
    navController: NavController,
    viewModel: MatchesViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.uiState.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // Show error snackbar if there's an error
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(error)
                viewModel.clearError()
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(com.Lyno.matchmindai.R.string.matches_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                },
                actions = {
                    // Date selector button
                    IconButton(onClick = {
                        // TODO: Implement date picker
                    }) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = stringResource(com.Lyno.matchmindai.R.string.select_date)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            if (uiState.leagueGroups.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        if (uiState.expandedLeagueCount > 0) {
                            viewModel.collapseAll()
                        } else {
                            viewModel.expandAll()
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (uiState.expandedLeagueCount > 0) {
                            Icons.Default.ExpandLess
                        } else {
                            Icons.Default.ExpandMore
                        },
                        contentDescription = if (uiState.expandedLeagueCount > 0) {
                            stringResource(com.Lyno.matchmindai.R.string.collapse_all)
                        } else {
                            stringResource(com.Lyno.matchmindai.R.string.expand_all)
                        }
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.hasMatches -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.leagueGroups,
                            key = { it.leagueId }
                        ) { leagueGroup ->
                            ExpandableLeagueSection(
                                leagueGroup = leagueGroup,
                                onMatchClick = { match ->
                                    Log.d("MatchesScreen", "Match clicked: ${match.homeTeam} vs ${match.awayTeam}")
                                    Log.d("MatchesScreen", "FixtureId value: ${match.fixtureId}")
                                    Log.d("MatchesScreen", "FixtureId is null: ${match.fixtureId == null}")
                                    
                                    match.fixtureId?.let { fixtureId ->
                                        Log.d("MatchesScreen", "Navigating to match detail with fixtureId: $fixtureId")
                                        // Navigate to match detail screen
                                        navController.navigate(Screen.MatchDetail.createRoute(fixtureId))
                                    } ?: run {
                                        Log.e("MatchesScreen", "Cannot navigate: fixtureId is null!")
                                        Log.e("MatchesScreen", "Match data: home=${match.homeTeam}, away=${match.awayTeam}, league=${match.league}")
                                    }
                                },
                                onToggleExpanded = {
                                    viewModel.toggleLeagueExpansion(leagueGroup.leagueId)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                            )
                        }
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(com.Lyno.matchmindai.R.string.no_matches_found),
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = stringResource(com.Lyno.matchmindai.R.string.no_matches_description),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
