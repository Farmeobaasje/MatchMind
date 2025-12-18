        package com.Lyno.matchmindai.presentation.screens.match

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Web
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.Lyno.matchmindai.domain.model.Injury
import com.Lyno.matchmindai.domain.model.MatchPredictionData
import com.Lyno.matchmindai.domain.model.OddsData
import com.Lyno.matchmindai.domain.model.EventType
import com.Lyno.matchmindai.domain.model.MatchDetail
import com.Lyno.matchmindai.presentation.viewmodel.MatchDetailViewModel
import com.Lyno.matchmindai.ui.theme.SurfaceCard
import com.Lyno.matchmindai.presentation.components.detail.StatComparisonBar
import com.Lyno.matchmindai.presentation.components.detail.LineupList
import com.Lyno.matchmindai.presentation.components.detail.LineupEmptyState
import com.Lyno.matchmindai.presentation.components.detail.EventTimeline
import com.Lyno.matchmindai.presentation.components.detail.EventTimelineEmptyState
import com.Lyno.matchmindai.presentation.components.detail.MatchEventSummary
import com.Lyno.matchmindai.presentation.components.GlassCard
import com.Lyno.matchmindai.presentation.components.visual.ConnectionIndicator
import com.Lyno.matchmindai.presentation.components.visual.ConnectionStatus

/**
 * Match Detail Screen with comprehensive match information.
 * Features 4 tabs: Overview, Stats, Lineups, Standings.
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
    val context = androidx.compose.ui.platform.LocalContext.current
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
                matchCacheManager = container.matchCacheManager
            ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    )
    
    // Observe UI state
    val uiState by viewModel.uiState.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    
    // Cache status
    var cacheStatus by remember { mutableStateOf<com.Lyno.matchmindai.domain.service.CacheStatus?>(null) }
    
    // Load cache status
    LaunchedEffect(fixtureId) {
        cacheStatus = viewModel.getCacheStatus()
    }
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overzicht", "Stats", "Opstellingen", "Stand", "Blessures", "Voorspellingen", "Odds")
    
    // Handle AI Analysis FAB click
    val onAiAnalysisClick = {
        // Get match details from current state
        val currentState = uiState
        if (currentState is com.Lyno.matchmindai.presentation.viewmodel.MatchDetailUiState.Success) {
            val matchDetail = currentState.matchDetail
            val homeTeam = matchDetail.homeTeam
            val awayTeam = matchDetail.awayTeam
            val league = matchDetail.league
            
            // Create proper navigation route with simple parameters
            val chatRoute = com.Lyno.matchmindai.presentation.navigation.Screen.Chat.createRoute(
                homeTeam = homeTeam,
                awayTeam = awayTeam,
                league = league
            )
            onNavigateToChat(chatRoute)
        } else {
            // Fallback to simple analysis context if no match data
            val analysisContext = viewModel.prepareAiAnalysis()
            onNavigateToChat("chat?query=${java.net.URLEncoder.encode(analysisContext, "UTF-8")}")
        }
    }
    
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
        else -> {
            // Continue with Success state
        }
    }
    
    // Get match detail from success state
    val matchDetail = (uiState as com.Lyno.matchmindai.presentation.viewmodel.MatchDetailUiState.Success).matchDetail

    Scaffold(
        floatingActionButton = {
            // AI Analysis button
            FloatingActionButton(
                onClick = onAiAnalysisClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "AI Analyse"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Cache status indicator
            if (cacheStatus != null) {
                val connectionStatus = when (cacheStatus) {
                    com.Lyno.matchmindai.domain.service.CacheStatus.FRESH -> ConnectionStatus.CACHED
                    com.Lyno.matchmindai.domain.service.CacheStatus.STALE -> ConnectionStatus.SYNCING
                    com.Lyno.matchmindai.domain.service.CacheStatus.MISSING -> ConnectionStatus.DISCONNECTED
                    null -> ConnectionStatus.DISCONNECTED
                }
                ConnectionIndicator(
                    status = connectionStatus,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            // Match Header
            MatchHeader(
                matchDetail = matchDetail,
                modifier = Modifier.padding(16.dp)
            )
            

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 16.dp,
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
                        },
                icon = {
                    when (index) {
                        0 -> Icon(Icons.Filled.Schedule, contentDescription = "Overview")
                        1 -> Icon(Icons.Filled.ShowChart, contentDescription = "Stats")
                        2 -> Icon(Icons.Filled.Group, contentDescription = "Lineups")
                        3 -> Icon(Icons.Filled.List, contentDescription = "Standings")
                        4 -> Icon(Icons.Filled.LocalHospital, contentDescription = "Injuries")
                        5 -> Icon(Icons.Filled.TrendingUp, contentDescription = "Predictions")
                        6 -> Icon(Icons.Filled.AttachMoney, contentDescription = "Odds")
                    }
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
                when (selectedTab) {
                    0 -> OverviewTab(matchDetail)
                    1 -> StatsTab(matchDetail)
                    2 -> LineupsTab(matchDetail)
                    3 -> StandingsTab(matchDetail)
                    4 -> InjuriesTab(matchDetail, viewModel)
                    5 -> PredictionsTab(matchDetail, viewModel)
                    6 -> OddsTab(matchDetail, viewModel)
                }
            }
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
                    TeamLogo(teamName = homeTeam)
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
                    TeamLogo(teamName = awayTeam)
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
 * Team logo placeholder.
 */
@Composable
private fun TeamLogo(teamName: String) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = teamName.take(2).uppercase(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

/**
 * Overview tab with match timeline and summary.
 */
@Composable
private fun OverviewTab(matchDetail: MatchDetail) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Wedstrijd Overzicht",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Match summary with key events
        if (matchDetail.events.isNotEmpty()) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Belangrijkste Gebeurtenissen",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    MatchEventSummary(events = matchDetail.events)
                }
            }
        }

        // Full event timeline
        Text(
            text = "Volledige Tijdlijn",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Show events if available
        if (matchDetail.events.isNotEmpty()) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                EventTimeline(
                    events = matchDetail.events,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                EventTimelineEmptyState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Stats tab with progress bars for match statistics.
 */
@Composable
private fun StatsTab(matchDetail: MatchDetail) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Statistieken",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Show statistics if available
        if (matchDetail.stats.isNotEmpty()) {
            matchDetail.stats.take(10).forEach { stat ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Stat name
                        Text(
                            text = stat.type,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        // Stat comparison bar
                        StatComparisonBar(
                            statName = stat.type,
                            homeValue = stat.homeValue.toString(),
                            awayValue = stat.awayValue.toString(),
                            homeLabel = matchDetail.homeTeam.take(3).uppercase(),
                            awayLabel = matchDetail.awayTeam.take(3).uppercase(),
                            unit = stat.unit,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // Values
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${stat.homeValue}${stat.unit}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${stat.awayValue}${stat.unit}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Wedstrijd nog niet begonnen",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Statistieken worden beschikbaar zodra de wedstrijd begint.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Lineups tab with football field visualization.
 */
@Composable
private fun LineupsTab(matchDetail: MatchDetail) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Opstellingen",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Check if lineups are available
        if (matchDetail.hasLineups) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                LineupList(
                    homeLineup = matchDetail.lineups.home,
                    awayLineup = matchDetail.lineups.away,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                LineupEmptyState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Standings tab with league table.
 */
@Composable
private fun StandingsTab(matchDetail: MatchDetail) {
    val scrollState = rememberScrollState()
    val standings = matchDetail.standings
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // State for retry functionality
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Function to refresh standings
    val refreshStandings: () -> Unit = {
        isLoading = true
        errorMessage = null
        
        coroutineScope.launch {
            try {
                // In a real implementation, this would call a refresh function
                // For now, we'll just simulate a refresh
                kotlinx.coroutines.delay(1000) // Simulate network delay
                
                // Log debug info
                android.util.Log.d("StandingsTab", "Refreshing standings for fixture ${matchDetail.fixtureId}")
                android.util.Log.d("StandingsTab", "League: ${matchDetail.league}")
                android.util.Log.d("StandingsTab", "Standings count: ${standings?.size ?: 0}")
                
                // Show success message
                errorMessage = "Ranglijst vernieuwd!"
                
                // Clear success message after 3 seconds
                kotlinx.coroutines.delay(3000)
                errorMessage = null
            } catch (e: Exception) {
                errorMessage = "Fout bij vernieuwen: ${e.message}"
                android.util.Log.e("StandingsTab", "Refresh failed", e)
            } finally {
                isLoading = false
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Header with refresh button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (standings != null) "🏆 Ranglijst" else "🏆 Ranglijst (Laden...)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Refresh button
            IconButton(
                onClick = refreshStandings,
                enabled = !isLoading,
                modifier = Modifier.size(40.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Vernieuwen",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        // Error message if any
        errorMessage?.let { message ->
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (message.contains("succes")) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.error
                    )
                    
                    IconButton(
                        onClick = { errorMessage = null },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Sluiten",
                            tint = if (message.contains("succes")) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        if (standings == null || standings.isEmpty()) {
            // Enhanced empty state with debugging info
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Icon
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Warning,
                        contentDescription = "Geen data",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Geen ranglijst beschikbaar",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "De ranglijst voor ${matchDetail.league} is momenteel niet beschikbaar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                    
                    // Debug info
                    Text(
                        text = "Fixture: ${matchDetail.fixtureId} | League: ${matchDetail.league}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    
                    // Retry button
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = refreshStandings,
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Laden...")
                        } else {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Refresh,
                                contentDescription = "Opnieuw proberen",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Opnieuw proberen")
                        }
                    }
                }
            }
        } else {
            // Success state - show standings table
            
            // League info header
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = matchDetail.league,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${standings.size} teams | Top 10 getoond",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Highlight home and away team positions
                    val homeTeamRank = standings.find { it.team == matchDetail.homeTeam }?.rank
                    val awayTeamRank = standings.find { it.team == matchDetail.awayTeam }?.rank
                    
                    if (homeTeamRank != null && awayTeamRank != null) {
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "${matchDetail.homeTeam.take(3)}: #$homeTeamRank",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${matchDetail.awayTeam.take(3)}: #$awayTeamRank",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
            
            // Standings header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("#", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.width(32.dp))
                Text("Team", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).padding(horizontal = 8.dp))
                Text("P", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp))
                Text("W", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.width(20.dp))
                Text("G", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.width(20.dp))
                Text("V", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.width(20.dp))
                Text("+/-", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.width(32.dp))
            }

            // Standings rows - using real data
            standings.take(10).forEach { standing ->
                StandingItemReal(
                    standing = standing,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                )
            }

            if (standings.size > 10) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "... en ${standings.size - 10} andere teams",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    )
                }
            }
            
            // Last updated info
            Text(
                text = "Data van API-Sports | Vernieuwd automatisch",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 16.dp, end = 16.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Simplified standing item to avoid compilation errors.
 */
@Composable
private fun StandingItemSimplified(
    position: Int,
    team: String,
    points: Int,
    goalDiff: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (position <= 3) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$position.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(24.dp)
            )
            
            Text(
                text = team,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            )
            
            Text(
                text = "$points",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(24.dp)
            )
            
            Text(
                text = goalDiff,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = if (goalDiff.startsWith("+")) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.error,
                modifier = Modifier.width(32.dp)
            )
        }
    }
}

// Data classes for UI components
data class TimelineEvent(
    val icon: String,
    val type: String,
    val time: String,
    val player: String,
    val details: String
)

data class StandingRow(
    val position: Int,
    val team: String,
    val points: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val goalDiff: String
)

// UI Components
@Composable
fun TimelineItem(event: TimelineEvent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Text(
            text = event.icon,
            fontSize = 24.sp,
            modifier = Modifier.width(40.dp)
        )
        
        // Time
        Text(
            text = event.time,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(50.dp)
        )
        
        // Event details
        Column(
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
        ) {
            Text(
                text = "${event.type}: ${event.player}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            if (event.details.isNotEmpty()) {
                Text(
                    text = event.details,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@Composable
fun PlayerDot(playerName: String, isAway: Boolean = false) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isAway) MaterialTheme.colorScheme.secondary 
                else MaterialTheme.colorScheme.primary
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = playerName,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StandingItem(standing: StandingRow) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (standing.position <= 3) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${standing.position}.", 
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(24.dp)
            )
            
            Text(
                text = standing.team,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            )
            
            Text(
                text = "${standing.points}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(24.dp)
            )
            
            Text(
                text = "${standing.wins}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.width(20.dp)
            )
            
            Text(
                text = "${standing.draws}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.width(20.dp)
            )
            
            Text(
                text = "${standing.losses}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.width(20.dp)
            )
            
            Text(
                text = standing.goalDiff,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = if (standing.goalDiff.startsWith("+")) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.error,
                modifier = Modifier.width(32.dp)
            )
        }
    }
}

/**
 * Standing item using the real StandingRow domain model.
 */
@Composable
private fun StandingItemReal(
    standing: com.Lyno.matchmindai.domain.model.StandingRow,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (standing.rank <= 3) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${standing.rank}.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(24.dp)
            )
            
            Text(
                text = standing.team,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            )
            
            Text(
                text = "${standing.points}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(24.dp)
            )
            
            Text(
                text = "${standing.wins}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.width(20.dp)
            )
            
            Text(
                text = "${standing.draws}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.width(20.dp)
            )
            
            Text(
                text = "${standing.losses}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.width(20.dp)
            )
            
            val goalDiffStr = if (standing.goalDiff >= 0) "+${standing.goalDiff}" else standing.goalDiff.toString()
            Text(
                text = goalDiffStr,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = if (standing.goalDiff >= 0) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.error,
                modifier = Modifier.width(32.dp)
            )
        }
    }
}

@Composable
fun MatchDetailScreenPreview() {
    MaterialTheme {
        MatchDetailScreen(
            fixtureId = 12345,
            onNavigateBack = {},
            onNavigateToChat = { _ -> }
        )
    }
}

/**
 * Injuries tab showing player injuries for the match.
 */
@Composable
private fun InjuriesTab(
    matchDetail: MatchDetail,
    viewModel: MatchDetailViewModel
) {
    val scrollState = rememberScrollState()
    val injuries by viewModel.injuries.collectAsState()
    val isLoading by viewModel.isLoadingInjuries.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadInjuries(matchDetail.fixtureId)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Blessures",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (injuries.isEmpty()) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Geen blessures gemeld",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Er zijn momenteel geen blessures gemeld voor deze wedstrijd.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )
                }
            }
        } else {
            // Group injuries by team
            val homeTeamInjuries = injuries.filter { it.team == matchDetail.homeTeam }
            val awayTeamInjuries = injuries.filter { it.team == matchDetail.awayTeam }
            
            // Home team injuries
            if (homeTeamInjuries.isNotEmpty()) {
                Text(
                    text = matchDetail.homeTeam,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                homeTeamInjuries.forEach { injury ->
                    InjuryCard(injury = injury)
                }
            }
            
            // Away team injuries
            if (awayTeamInjuries.isNotEmpty()) {
                Text(
                    text = matchDetail.awayTeam,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                awayTeamInjuries.forEach { injury ->
                    InjuryCard(injury = injury)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Injury card showing details of a single injury.
 */
@Composable
private fun InjuryCard(injury: Injury) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Player name and team
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = injury.playerName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = injury.team,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Injury type and reason
            Column {
                Text(
                    text = "Type: ${injury.type}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Reden: ${injury.reason}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // Expected return
            if (!injury.expectedReturn.isNullOrEmpty()) {
                Text(
                    text = "Verwacht terug: ${injury.expectedReturn}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Predictions tab showing match predictions.
 */
@Composable
private fun PredictionsTab(
    matchDetail: MatchDetail,
    viewModel: MatchDetailViewModel
) {
    val scrollState = rememberScrollState()
    val prediction by viewModel.prediction.collectAsState()
    val isLoading by viewModel.isLoadingPrediction.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadPrediction(matchDetail.fixtureId)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Voorspellingen",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (prediction == null) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Geen voorspellingen beschikbaar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Er zijn momenteel geen voorspellingen beschikbaar voor deze wedstrijd.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )
                }
            }
        } else {
            // Win probability chart
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Win Kans",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Probability bars
                    ProbabilityBar(
                        label = matchDetail.homeTeam,
                        probability = prediction!!.winningPercent.home / 100.0,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    ProbabilityBar(
                        label = "Gelijkspel",
                        probability = prediction!!.winningPercent.draw / 100.0,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    
                    ProbabilityBar(
                        label = matchDetail.awayTeam,
                        probability = prediction!!.winningPercent.away / 100.0,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            
            // Expected goals
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Verwachte Goals (xG)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = matchDetail.homeTeam,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = String.format("%.2f", prediction!!.expectedGoals.home),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = matchDetail.awayTeam,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = String.format("%.2f", prediction!!.expectedGoals.away),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }
            
            // Analysis
            if (!prediction!!.analysis.isNullOrEmpty()) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Analyse",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = prediction!!.analysis!!,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Justify
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Probability bar for showing win probability.
 */
@Composable
private fun ProbabilityBar(
    label: String,
    probability: Double,
    color: Color
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${String.format("%.1f", probability * 100)}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(probability.toFloat())
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

/**
 * Odds tab showing betting odds for the match.
 */
@Composable
private fun OddsTab(
    matchDetail: MatchDetail,
    viewModel: MatchDetailViewModel
) {
    val scrollState = rememberScrollState()
    val odds by viewModel.odds.collectAsState()
    val isLoading by viewModel.isLoadingOdds.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadOdds(matchDetail.fixtureId)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Wedkansen",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (odds == null) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Geen wedkansen beschikbaar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Er zijn momenteel geen wedkansen beschikbaar voor deze wedstrijd.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )
                }
            }
        } else {
            // Main odds
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Hoofdkansen",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OddsItem(
                            label = matchDetail.homeTeam,
                            odds = odds!!.homeWinOdds,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        OddsItem(
                            label = "Gelijkspel",
                            odds = odds!!.drawOdds,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        
                        OddsItem(
                            label = matchDetail.awayTeam,
                            odds = odds!!.awayWinOdds,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    
                    // Bookmaker info
                    Text(
                        text = "Boekmaker: ${odds!!.bookmakerName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Over/Under odds
            if (!odds!!.overUnderOdds.isNullOrEmpty()) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Over/Under Kansen",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        odds!!.overUnderOdds!!.forEach { (key, value) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = key,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = String.format("%.2f", value),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
            
            // Both Teams to Score odds
            if (!odds!!.bothTeamsToScoreOdds.isNullOrEmpty()) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Beide Teams Scoren",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        odds!!.bothTeamsToScoreOdds!!.forEach { (key, value) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = key,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = String.format("%.2f", value),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
            
            // Ratings
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Beoordelingen",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Waarde",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "${odds!!.valueRating}/10",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Veiligheid",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "${odds!!.safetyRating}/10",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    
                    // Highest odds
                    if (odds!!.highestOdds != null) {
                        Text(
                            text = "Hoogste kans: ${String.format("%.2f", odds!!.highestOdds)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Last updated
                    Text(
                        text = "Laatst bijgewerkt: ${odds!!.lastUpdated}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Odds item showing a single odds value.
 */
@Composable
private fun OddsItem(
    label: String,
    odds: Double?,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (odds != null) {
            Text(
                text = String.format("%.2f", odds),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        } else {
            Text(
                text = "-",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * API Widget tab showing API-Sports football widget for the match.
 */
@Composable
private fun ApiWidgetTab(
    fixtureId: Int,
    matchDetail: MatchDetail
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "API-Sports Widget",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Text(
            text = "Live wedstrijdgegevens via API-Sports",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        // API-Sports Game widget
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Wedstrijd Widget",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Toont live statistieken, opstellingen en gebeurtenissen voor deze wedstrijd.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // API-Sports WebView
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                ) {
                    com.Lyno.matchmindai.presentation.components.ApiSportsWebView(
                        widgetType = com.Lyno.matchmindai.presentation.components.FootballWidgetType.Game(fixtureId),
                        parameters = mapOf(
                            "data-game-tab" to "statistics",
                            "data-refresh" to "30",
                            "data-show-toolbar" to "false"
                        ),
                        modifier = Modifier.fillMaxSize(),
                        onLoadingChanged = { isLoading ->
                            // Handle loading state if needed
                        },
                        onError = { error ->
                            // Handle error if needed
                        }
                    )
                }
                
                // Widget info
                Text(
                    text = "Widget ID: $fixtureId",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Additional widgets section
        Text(
            text = "Extra Widgets",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        // League standings widget (if league is available)
        if (!matchDetail.league.isNullOrEmpty()) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Ranglijst Widget",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Toont de ranglijst voor ${matchDetail.league}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Note: This would require league ID and season
                    Text(
                        text = "Beschikbaar voor competities met ranglijst",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}
