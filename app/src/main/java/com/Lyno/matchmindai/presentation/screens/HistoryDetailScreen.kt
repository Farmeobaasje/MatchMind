package com.Lyno.matchmindai.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Lyno.matchmindai.R
import com.Lyno.matchmindai.presentation.AppViewModelProvider
import com.Lyno.matchmindai.presentation.components.historixyi.HistoryDetailTab
import com.Lyno.matchmindai.presentation.viewmodel.HistoryDetailViewModel
import com.Lyno.matchmindai.presentation.viewmodel.HistoryDetailUiState
import kotlinx.coroutines.launch

/**
 * History Detail Screen for Project Historxyi - Phase 3.
 * 
 * Shows comprehensive retrospective analysis comparing prediction vs reality.
 * Features:
 * 1. Prediction vs Actual Score comparison
 * 2. Retrospective insights (MASTERCLASS, REALITY_CHECK, etc.)
 * 3. Deep stats comparison (xG, possession, shots, etc.)
 * 4. Historical context and learning points
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailScreen(
    viewModel: HistoryDetailViewModel = viewModel(factory = AppViewModelProvider.Factory),
    predictionId: Int,
    fixtureId: Int,
    onNavigateBack: () -> Unit
) {
    // Initialize ViewModel with parameters
    LaunchedEffect(predictionId, fixtureId) {
        viewModel.initialize(predictionId, fixtureId)
    }

    // Collect UI state
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.history_detail_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back)
                        )
                    }
                },
                actions = {
                    // Refresh button
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                viewModel.refresh()
                            }
                        },
                        enabled = uiState !is HistoryDetailUiState.Loading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.refresh)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState) {
                is HistoryDetailUiState.Loading -> {
                    LoadingState()
                }
                
                is HistoryDetailUiState.Success -> {
                    val analysis = (uiState as HistoryDetailUiState.Success).analysis
                    
                    // Extract team names for the HistoryDetailTab
                    val homeTeamName = analysis.actualMatch.homeTeam
                    val awayTeamName = analysis.actualMatch.awayTeam
                    
                    HistoryDetailTab(
                        retrospective = analysis,
                        historicalStats = analysis.kaptigunStats?.deepStats
                            ?: analysis.createDefaultDeepStatsComparison(),
                        singleMatchStats = analysis.kaptigunStats?.deepStats,
                        homeTeamName = homeTeamName,
                        awayTeamName = awayTeamName,
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    )
                }
                
                is HistoryDetailUiState.Error -> {
                    val errorMessage = (uiState as HistoryDetailUiState.Error).message
                    ErrorState(
                        errorMessage = errorMessage,
                        onRetry = { viewModel.refresh() }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = stringResource(R.string.loading_retrospective_analysis),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorState(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = stringResource(R.string.error_loading_analysis),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Button(onClick = onRetry) {
                Text(text = stringResource(R.string.retry))
            }
        }
    }
}

// Extension function to create default DeepStatsComparison if none exists
private fun com.Lyno.matchmindai.domain.model.RetrospectiveAnalysis.createDefaultDeepStatsComparison(): 
    com.Lyno.matchmindai.domain.model.DeepStatsComparison {
    return com.Lyno.matchmindai.domain.model.DeepStatsComparison(
        avgXg = Pair(0.0, 0.0),
        avgPossession = Pair(50.0, 50.0),
        avgShotsOnTarget = Pair(0.0, 0.0),
        ppda = Pair(15.0, 15.0),
        sentimentScore = Pair(0.0, 0.0)
    )
}
