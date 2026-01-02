package com.Lyno.matchmindai.presentation.screens.match

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Lyno.matchmindai.domain.model.MatchDetail
import com.Lyno.matchmindai.presentation.viewmodel.MatchDetailViewModel
import com.Lyno.matchmindai.presentation.components.ApiSportsImage
import com.Lyno.matchmindai.presentation.components.detail.DetailsIntelligenceTab
import com.Lyno.matchmindai.presentation.components.detail.IntelligenceTab
import com.Lyno.matchmindai.presentation.components.detail.SmartOddsTab
import com.Lyno.matchmindai.presentation.components.detail.LiveTab
import com.Lyno.matchmindai.presentation.components.detail.VerslagTab
import com.Lyno.matchmindai.presentation.components.detail.MastermindTipTab
import com.Lyno.matchmindai.presentation.components.detail.PredictionTab
import com.Lyno.matchmindai.presentation.components.detail.BettingTipsTab
import com.Lyno.matchmindai.presentation.components.detail.AnalysisTab
import com.Lyno.matchmindai.presentation.components.detail.UnifiedPredictionTab
import com.Lyno.matchmindai.presentation.viewmodel.PredictionViewModel

/**
 * Match Detail Screen with comprehensive match information.
 * Features tabs: Live, Details, Intel, Odds.
 * Uses MatchDetailViewModel to fetch real match data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailScreen(
    fixtureId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    Log.d("MatchDetailScreen", "Screen loaded with fixtureId: $fixtureId")
    
    // Create ViewModel with custom factory
    val context = LocalContext.current
    val application = context.applicationContext as com.Lyno.matchmindai.MatchMindApplication
    val container = application.appContainer
    
    val viewModel: MatchDetailViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MatchDetailViewModel::class.java)) {
                    Log.d("MatchDetailScreen", "Creating ViewModel with fixtureId: $fixtureId")
                    return MatchDetailViewModel(
                        fixtureId = fixtureId,
                        matchRepository = container.matchRepository,
                        matchCacheManager = container.matchCacheManager,
                        getHybridPredictionUseCase = container.getHybridPredictionUseCase,
                        matchReportGenerator = container.matchReportGenerator,
                        mastermindAnalysisUseCase = container.mastermindAnalysisUseCase
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    )
    
    // Observe UI state
    val uiState by viewModel.uiState.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    
    // Show loading or error states
    when (uiState) {
        is com.Lyno.matchmindai.presentation.viewmodel.MatchDetailUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return
        }
        is com.Lyno.matchmindai.presentation.viewmodel.MatchDetailUiState.Error -> {
            val errorMessage = (uiState as com.Lyno.matchmindai.presentation.viewmodel.MatchDetailUiState.Error).message
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Fout bij ophalen wedstrijdgegevens",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = { viewModel.refresh() }) {
                        Text("Opnieuw proberen")
                    }
                }
            }
            return
        }
        is com.Lyno.matchmindai.presentation.viewmodel.MatchDetailUiState.NoDataAvailable -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Nog geen data beschikbaar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Deze wedstrijd is nog niet begonnen of er zijn nog geen statistieken beschikbaar.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = { viewModel.refresh() }) {
                        Text("Vernieuwen")
                    }
                }
            }
            return
        }
        is com.Lyno.matchmindai.presentation.viewmodel.MatchDetailUiState.Success -> {
            val matchDetail = (uiState as com.Lyno.matchmindai.presentation.viewmodel.MatchDetailUiState.Success).matchDetail
            
            // Create PredictionViewModel using the AppViewModelProvider factory
            val predictionViewModel: PredictionViewModel = viewModel(
                factory = com.Lyno.matchmindai.presentation.AppViewModelProvider.Factory
            )
            
            // Determine tabs based on match status
            val isMatchLive = matchDetail.status?.isLive == true
            val tabs = remember(isMatchLive) {
                if (isMatchLive) {
                    listOf("Live", "Details", "Voorspelling", "Analyse")
                } else {
                    listOf("Details", "Voorspelling", "Analyse")
                }
            }
            
            // Adjust selected tab if needed when tabs change
            LaunchedEffect(isMatchLive) {
                if (isMatchLive && selectedTab >= tabs.size) {
                    selectedTab = 0 // Reset to first tab if current selection is invalid
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Match Header
                MatchHeader(
                    matchDetail = matchDetail,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    edgePadding = 0.dp,
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
                
                // Tab Content with fixed height constraint
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    when (tabs[selectedTab]) {
                        "Live" -> LiveTab(matchDetail, viewModel)
                        "Details" -> DetailsIntelligenceTab(matchDetail, viewModel)
                        "Voorspelling" -> UnifiedPredictionTab(matchDetail, predictionViewModel)
                        "Analyse" -> AnalysisTab(fixtureId, matchDetail.homeTeam, matchDetail.awayTeam)
                        else -> DetailsIntelligenceTab(matchDetail, viewModel)
                    }
                }
            }
        }
        else -> {
            // Should not happen, but handle gracefully
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Onbekende status")
            }
            return
        }
    }
}

/**
 * Match header with teams, score, and match info.
 */
@Composable
private fun MatchHeader(
    matchDetail: MatchDetail,
    modifier: Modifier = Modifier
) {
    val homeTeam = matchDetail.homeTeam
    val awayTeam = matchDetail.awayTeam
    val score = matchDetail.score?.let { "${it.home}-${it.away}" }
    val league = matchDetail.league
    val stadium = matchDetail.info?.stadium
    val matchTime = matchDetail.info?.time
    val status = matchDetail.status?.displayName ?: "Onbekend"
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // League info
            if (!league.isNullOrEmpty()) {
                Text(
                    text = league.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Teams and Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Team
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TeamLogo(
                        teamName = homeTeam,
                        teamId = matchDetail.homeTeamId
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = homeTeam,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }

                // Score and Status
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (score != null) {
                        Text(
                            text = score,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            text = "VS",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Show status and time
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = status,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        if (matchTime != null) {
                            Text(
                                text = matchTime,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Away Team
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TeamLogo(
                        teamName = awayTeam,
                        teamId = matchDetail.awayTeamId
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = awayTeam,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
            }

            // Stadium info
            if (!stadium.isNullOrEmpty()) {
                Text(
                    text = "📍 $stadium",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}

/**
 * Team logo using ApiSportsImage.
 */
@Composable
private fun TeamLogo(
    teamName: String,
    teamId: Int? = null
) {
    ApiSportsImage(
        teamId = teamId,
        teamName = teamName,
        size = 60.dp,
        modifier = Modifier.size(60.dp)
    )
}
