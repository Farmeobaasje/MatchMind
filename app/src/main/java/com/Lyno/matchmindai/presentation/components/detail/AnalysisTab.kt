package com.Lyno.matchmindai.presentation.components.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Info
import com.Lyno.matchmindai.domain.model.SimulationContext
import com.Lyno.matchmindai.presentation.components.kaptigun.*
import com.Lyno.matchmindai.presentation.viewmodel.KaptigunViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

/**
 * Analysis tab for MatchDetailScreen.
 * Displays Kaptigun performance analysis with three sections:
 * 1. Head-to-head history
 * 2. Recent form comparison
 * 3. Deep statistics with sentiment analysis
 * 
 * CHICHI UPGRADE: Now includes Team Analysis Card with distraction/fitness scores
 */
@Composable
fun AnalysisTab(
    fixtureId: Int,
    homeTeamName: String,
    awayTeamName: String,
    modifier: Modifier = Modifier
) {
    val viewModel: KaptigunViewModel = viewModel(factory = com.Lyno.matchmindai.presentation.AppViewModelProvider.Factory)
    val uiState by viewModel.uiState.collectAsState()

    // State for simulation context (ChiChi feature)
    var simulationContext by remember { mutableStateOf<SimulationContext?>(null) }
    var isLoadingSimulation by remember { mutableStateOf(false) }
    var simulationError by remember { mutableStateOf<String?>(null) }

    // Get application container for dependencies (outside LaunchedEffect)
    val context = LocalContext.current
    val application = remember(context) { context.applicationContext as com.Lyno.matchmindai.MatchMindApplication }
    val container = remember(application) { application.appContainer }

    // Load analysis when tab is shown
    LaunchedEffect(fixtureId, container) {
        if (uiState.analysis == null || uiState.analysis?.fixtureId != fixtureId) {
            viewModel.loadAnalysis(fixtureId)
        }
        
        // Load simulation context for ChiChi analysis
        isLoadingSimulation = true
        
        // Create a simple MatchDetail object for the simulation using the utility function
        val matchDetail = com.Lyno.matchmindai.domain.model.MatchDetailUtils.empty(
            fixtureId = fixtureId,
            homeTeam = homeTeamName,
            awayTeam = awayTeamName,
            league = "Unknown"
        )
        
        // Generate simulation context using NewsImpactAnalyzer
        val apiKey = container.apiKeyStorage.getDeepSeekApiKey()
        val result = if (apiKey != null) {
            container.newsImpactAnalyzer.generateMatchScenario(fixtureId, matchDetail, apiKey)
        } else {
            // Fallback to neutral context if API key is missing
            Result.success(SimulationContext.NEUTRAL)
        }
        
        simulationContext = if (result.isSuccess) {
            result.getOrNull()
        } else {
            null
        }
        simulationError = if (apiKey == null) {
            "DeepSeek API key niet gevonden. Ga naar instellingen om een API key in te voeren."
        } else {
            null
        }
        isLoadingSimulation = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Prestatie Analyse",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Refresh button
            IconButton(
                onClick = { viewModel.refreshAnalysis(fixtureId) },
                enabled = !uiState.isRefreshing
            ) {
                if (uiState.isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Vernieuwen"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Last updated info
        if (uiState.lastUpdated > 0) {
            Text(
                text = "Bijgewerkt: ${uiState.lastUpdatedFormatted}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
            
            if (uiState.isDataStale) {
                Text(
                    text = "⚠️ Data is verouderd. Vernieuw voor actuele informatie.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading -> {
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
                            text = "Analyse laden...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            uiState.error != null -> {
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
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Fout",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = uiState.error ?: "Onbekende fout",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Button(
                            onClick = { viewModel.loadAnalysis(fixtureId) }
                        ) {
                            Text(text = "Opnieuw proberen")
                        }
                    }
                }
            }

            uiState.analysis != null -> {
                val analysis = uiState.analysis!!

                // Head-to-head section
                HeadToHeadCard(
                    headToHeadDuels = analysis.headToHead,
                    homeTeamName = analysis.homeTeamName,
                    awayTeamName = analysis.awayTeamName,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Recent form section
                FormComparisonCard(
                    homeRecentForm = analysis.homeRecentForm,
                    awayRecentForm = analysis.awayRecentForm,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Deep stats section
                DeepStatsChart(
                    deepStats = analysis.deepStats,
                    homeTeamName = analysis.homeTeamName,
                    awayTeamName = analysis.awayTeamName,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Analysis summary
                AnalysisSummary(analysis = analysis)

                Spacer(modifier = Modifier.height(16.dp))

                // ChiChi Team Analysis Card (if simulation context is available)
                if (simulationContext != null) {
                    TeamAnalysisCard(
                        simulationContext = simulationContext!!,
                        homeTeamName = homeTeamName,
                        awayTeamName = awayTeamName,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (isLoadingSimulation) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "Team analyse laden...",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                } else if (simulationError != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Team analyse mislukt",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = simulationError ?: "Onbekende fout",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            else -> {
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
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "Analyse",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Geen analyse beschikbaar",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Button(
                            onClick = { viewModel.loadAnalysis(fixtureId) }
                        ) {
                            Text(text = "Analyse laden")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun AnalysisSummary(
    analysis: com.Lyno.matchmindai.domain.model.KaptigunAnalysis
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Analyse Samenvatting",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Key insights
            val insights = mutableListOf<String>()

            // Head-to-head insight
            if (analysis.headToHead.isNotEmpty()) {
                val homeWins = analysis.headToHead.count { it.homeScore > it.awayScore }
                val awayWins = analysis.headToHead.count { it.awayScore > it.homeScore }
                val draws = analysis.headToHead.count { it.homeScore == it.awayScore }
                
                insights.add("Onderlinge duels: $homeWins-$draws-$awayWins (Thuis-Uit)")
            }

            // Form insight
            val homeForm = analysis.homeRecentForm.results.takeLast(5)
            val awayForm = analysis.awayRecentForm.results.takeLast(5)
            
            val homeRecentWins = homeForm.count { it == com.Lyno.matchmindai.domain.model.MatchResult.WIN }
            val awayRecentWins = awayForm.count { it == com.Lyno.matchmindai.domain.model.MatchResult.WIN }
            
            insights.add("Recente vorm: ${homeRecentWins}W/${awayRecentWins}W (Thuis/Uit)")

            // xG insight
            val homeXgAdvantage = analysis.deepStats.avgXg.first > analysis.deepStats.avgXg.second
            insights.add("xG voordeel: ${if (homeXgAdvantage) analysis.homeTeamName else analysis.awayTeamName}")

            // Sentiment insight
            val homeSentiment = analysis.deepStats.sentimentScore.first
            val awaySentiment = analysis.deepStats.sentimentScore.second
            
            val sentimentText = when {
                homeSentiment > 0.5 && awaySentiment > 0.5 -> "Beide teams positief"
                homeSentiment < -0.5 && awaySentiment < -0.5 -> "Beide teams negatief"
                homeSentiment > awaySentiment + 0.3 -> "${analysis.homeTeamName} positiever"
                awaySentiment > homeSentiment + 0.3 -> "${analysis.awayTeamName} positiever"
                else -> "Gelijk sentiment"
            }
            insights.add("Team sentiment: $sentimentText")

            // Display insights
            insights.forEach { insight ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = insight,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Last updated
            Text(
                text = "Analyse gegenereerd: ${analysis.lastUpdated}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
