package com.Lyno.matchmindai.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Lyno.matchmindai.MatchMindApplication
import com.Lyno.matchmindai.presentation.components.MatchFixtureCard
import com.Lyno.matchmindai.presentation.viewmodel.MatchUiState
import com.Lyno.matchmindai.presentation.viewmodel.MatchViewModel
import com.Lyno.matchmindai.presentation.viewmodel.getViewModelFactory
import com.Lyno.matchmindai.ui.theme.MatchMindAITheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveMatchesScreen(
    onNavigateToChat: (String, String, String?) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val viewModel: MatchViewModel = viewModel(
        factory = getViewModelFactory(
            LocalContext.current.applicationContext as MatchMindApplication
        )
    )
    val uiState = viewModel.uiState.collectAsState().value
    val todaysMatches = viewModel.todaysMatches.collectAsState().value
    val isLoadingTodaysMatches = viewModel.isLoadingTodaysMatches.collectAsState().value
    val upcomingMatches = viewModel.upcomingMatches.collectAsState().value
    val isLoadingUpcomingMatches = viewModel.isLoadingUpcomingMatches.collectAsState().value
    val scope = rememberCoroutineScope()

    // Load matches when screen is first shown
    LaunchedEffect(Unit) {
        viewModel.loadTodaysMatches()
        viewModel.loadUpcomingMatches()
    }

    // Handle MissingApiKey state - navigate to settings
    LaunchedEffect(uiState) {
        if (uiState is MatchUiState.MissingApiKey) {
            onNavigateToSettings()
        }
    }

    // Combine both match lists
    val allMatches = remember(todaysMatches, upcomingMatches) {
        todaysMatches + upcomingMatches
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // Header with refresh button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = com.Lyno.matchmindai.R.string.live_screen_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(
                onClick = {
                    scope.launch {
                        viewModel.loadTodaysMatches()
                        viewModel.loadUpcomingMatches()
                    }
                },
                enabled = !isLoadingTodaysMatches && !isLoadingUpcomingMatches
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Vernieuwen"
                )
            }
        }

        // Hint text
        Text(
            text = stringResource(id = com.Lyno.matchmindai.R.string.live_matches_select_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )

        // Loading state
        if (isLoadingTodaysMatches || isLoadingUpcomingMatches) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = stringResource(id = com.Lyno.matchmindai.R.string.live_matches_loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        // Empty state
        else if (allMatches.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(id = com.Lyno.matchmindai.R.string.live_matches_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.loadTodaysMatches()
                                viewModel.loadUpcomingMatches()
                            }
                        }
                    ) {
                        Text(text = "Opnieuw proberen")
                    }
                }
            }
        }
        // Matches list
        else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                        items(allMatches) { fixture ->
                            MatchFixtureCard(
                                fixture = fixture,
                                onClick = {
                                    // Update ViewModel with selected match
                                    viewModel.onMatchSelected(fixture, triggerPrediction = false)
                                    // Navigate to chat screen with league parameter
                                    onNavigateToChat(fixture.homeTeam, fixture.awayTeam, fixture.league)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LiveMatchesScreenPreview() {
    MatchMindAITheme {
        LiveMatchesScreen(
            onNavigateToChat = { _, _, _ -> },
            onNavigateToSettings = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LiveMatchesScreenLoadingPreview() {
    MatchMindAITheme {
        LiveMatchesScreen(
            onNavigateToChat = { _, _, _ -> },
            onNavigateToSettings = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LiveMatchesScreenEmptyPreview() {
    MatchMindAITheme {
        LiveMatchesScreen(
            onNavigateToChat = { _, _, _ -> },
            onNavigateToSettings = {}
        )
    }
}
