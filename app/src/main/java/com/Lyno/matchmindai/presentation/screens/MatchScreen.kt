package com.Lyno.matchmindai.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Lyno.matchmindai.MatchMindApplication
import com.Lyno.matchmindai.presentation.components.CyberTextField
import com.Lyno.matchmindai.presentation.components.PredictionCard
import com.Lyno.matchmindai.presentation.components.PrimaryActionButton
import com.Lyno.matchmindai.presentation.viewmodel.MatchViewModel
import com.Lyno.matchmindai.presentation.viewmodel.getViewModelFactory
import com.Lyno.matchmindai.ui.theme.MatchMindAITheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchScreen(
    onNavigateToSettings: () -> Unit
) {
    val viewModel: MatchViewModel = viewModel(
        factory = getViewModelFactory(
            LocalContext.current.applicationContext as MatchMindApplication
        )
    )
    val uiState = viewModel.uiState.collectAsState().value
    val homeTeam = viewModel.homeTeam.collectAsState().value
    val awayTeam = viewModel.awayTeam.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Get error message string
    val defaultErrorMessage = stringResource(id = com.Lyno.matchmindai.R.string.error_prediction_failed)

    // Handle MissingApiKey state - navigate to settings
    LaunchedEffect(uiState) {
        if (uiState is com.Lyno.matchmindai.presentation.viewmodel.MatchUiState.MissingApiKey) {
            onNavigateToSettings()
        }
    }

    // Show error snackbar
    LaunchedEffect(uiState) {
        if (uiState is com.Lyno.matchmindai.presentation.viewmodel.MatchUiState.Error) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = uiState.message ?: defaultErrorMessage
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = com.Lyno.matchmindai.R.string.match_screen_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Instellingen"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Welcome message
            Text(
                text = stringResource(id = com.Lyno.matchmindai.R.string.welcome_message),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Home Team Input
            CyberTextField(
                value = homeTeam,
                onValueChange = { viewModel.updateHomeTeam(it) },
                label = stringResource(id = com.Lyno.matchmindai.R.string.home_team_label),
                placeholder = stringResource(id = com.Lyno.matchmindai.R.string.home_team_placeholder)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Away Team Input
            CyberTextField(
                value = awayTeam,
                onValueChange = { viewModel.updateAwayTeam(it) },
                label = stringResource(id = com.Lyno.matchmindai.R.string.away_team_label),
                placeholder = stringResource(id = com.Lyno.matchmindai.R.string.away_team_placeholder)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Predict Button
            PrimaryActionButton(
                text = stringResource(id = com.Lyno.matchmindai.R.string.predict_button),
                onClick = { viewModel.predictMatch() },
                isLoading = uiState is com.Lyno.matchmindai.presentation.viewmodel.MatchUiState.Loading,
                enabled = homeTeam.isNotBlank() && awayTeam.isNotBlank()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Show prediction result
            if (uiState is com.Lyno.matchmindai.presentation.viewmodel.MatchUiState.Success) {
                PredictionCard(
                    winner = uiState.prediction.winner,
                    confidenceScore = uiState.prediction.confidenceScore,
                    riskLevel = uiState.prediction.riskLevel,
                    reasoning = uiState.prediction.reasoning,
                    recentMatches = uiState.prediction.recentMatches,
                    modifier = Modifier.fillMaxWidth(),
                    animate = false
                )
            } else if (uiState is com.Lyno.matchmindai.presentation.viewmodel.MatchUiState.Idle) {
                Text(
                    text = stringResource(id = com.Lyno.matchmindai.R.string.no_prediction_yet),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                )
            } else if (uiState is com.Lyno.matchmindai.presentation.viewmodel.MatchUiState.Loading) {
                // Loading state is handled by the button's isLoading parameter
            } else if (uiState is com.Lyno.matchmindai.presentation.viewmodel.MatchUiState.Error) {
                // Error state is handled by snackbar
            } else if (uiState is com.Lyno.matchmindai.presentation.viewmodel.MatchUiState.MissingApiKey) {
                // Handled by LaunchedEffect to navigate to settings
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MatchScreenPreview() {
    MatchMindAITheme {
        MatchScreen(
            onNavigateToSettings = {}
        )
    }
}


@Preview(showBackground = true)
@Composable
fun MatchScreenWithPredictionPreview() {
    MatchMindAITheme {
        // Simulate a success state
        MatchScreen(
            onNavigateToSettings = {}
        )
    }
}
