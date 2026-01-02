package com.Lyno.matchmindai.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Lyno.matchmindai.MatchMindApplication
import com.Lyno.matchmindai.domain.model.MatchFixture
import com.Lyno.matchmindai.domain.service.CuratedFeed
import com.Lyno.matchmindai.domain.service.LiveMatchUpdateService
import com.Lyno.matchmindai.presentation.components.DateNavigationBar
import com.Lyno.matchmindai.presentation.components.ExpandableLeagueSection
import com.Lyno.matchmindai.presentation.components.GlassCard
import com.Lyno.matchmindai.presentation.components.matches.HeroMatchCard
import com.Lyno.matchmindai.presentation.components.matches.StandardMatchCard
import com.Lyno.matchmindai.presentation.components.matches.StatusBadge
import com.Lyno.matchmindai.presentation.components.matches.ExpandableMatchCard
import com.Lyno.matchmindai.presentation.components.matches.SwipeableMatchCard
import com.Lyno.matchmindai.presentation.components.video.VideoHighlightsComponent
import com.Lyno.matchmindai.presentation.components.video.VideoHighlightsPreview
import com.Lyno.matchmindai.presentation.components.leagues.CountrySection
import com.Lyno.matchmindai.presentation.components.leagues.StatItem
import com.Lyno.matchmindai.presentation.viewmodel.DashboardViewModel
import com.Lyno.matchmindai.presentation.viewmodel.DashboardUiState
import com.Lyno.matchmindai.ui.theme.*

/**
 * Curated Dashboard screen - The smart, prioritized feed for MatchMind AI.
 * Features hero match, live ticker, and categorized upcoming matches.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(
        factory = (LocalContext.current.applicationContext as MatchMindApplication).appContainer.chatViewModelFactory
    ),
    onMatchClick: (MatchFixture) -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Collect state from ViewModel - use derivedStateOf to reduce recomposition
    val uiState by viewModel.uiState.collectAsState(initial = DashboardUiState.Idle)
    
    // Use remember to cache expensive computations
    val dashboardContent = remember(uiState) {
        when (uiState) {
            is DashboardUiState.Success -> {
                val successState = uiState as DashboardUiState.Success
                DashboardContent.SuccessContent(
                    curatedFeed = successState.curatedFeed,
                    isLoading = successState.isLoading
                )
            }
            is DashboardUiState.Error -> {
                DashboardContent.ErrorContent(
                    errorMessage = (uiState as DashboardUiState.Error).message
                )
            }
            DashboardUiState.MissingApiKey -> DashboardContent.MissingApiKeyContent
            else -> DashboardContent.LoadingContent
        }
    }

    // Load initial data when screen is first shown
    LaunchedEffect(Unit) {
        viewModel.loadCuratedFeed()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GradientStart, GradientEnd)
                )
            )
    ) {
        when (val content = dashboardContent) {
            is DashboardContent.SuccessContent -> {
                OptimizedDashboardContent(
                    viewModel = viewModel,
                    curatedFeed = content.curatedFeed,
                    onMatchClick = onMatchClick,
                    onNavigateToChat = onNavigateToChat,
                    onNavigateToSettings = onNavigateToSettings
                )
            }
            is DashboardContent.ErrorContent -> {
                ErrorState(
                    errorMessage = content.errorMessage,
                    onRetry = { viewModel.loadCuratedFeed() }
                )
            }
            DashboardContent.MissingApiKeyContent -> {
                MissingApiKeyState(
                    onNavigateToSettings = onNavigateToSettings
                )
            }
            DashboardContent.LoadingContent -> {
                LoadingState()
            }
        }
    }
}

/**
 * Sealed class for dashboard content to avoid recomposition of the entire screen.
 */
private sealed class DashboardContent {
    data class SuccessContent(
        val curatedFeed: CuratedFeed,
        val isLoading: Boolean = false
    ) : DashboardContent()
    
    data class ErrorContent(
        val errorMessage: String
    ) : DashboardContent()
    
    object MissingApiKeyContent : DashboardContent()
    object LoadingContent : DashboardContent()
}

/**
 * Main content for the curated dashboard with date navigation.
 */
@Composable
private fun CuratedDashboardContent(
    viewModel: DashboardViewModel,
    sortedMatches: List<MatchFixture>,
    favoriteLeagues: Set<String>,
    onMatchClick: (MatchFixture) -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    // Collect selected date from ViewModel
    val selectedDate by viewModel.selectedDate.collectAsState()
    
    // Collect league groups for collapsible sections
    val leagueGroups by viewModel.leagueGroups.collectAsState(initial = emptyList())
    
    // Separate matches into categories
    val heroMatch = sortedMatches.firstOrNull()
    val liveMatches = sortedMatches.filter { it.status in listOf("1H", "2H", "HT", "LIVE") }
    
    // Filter upcoming matches for non-top leagues (to show in UpcomingMatchesSection)
    val topLeagueIds = setOf(39, 88, 140, 78, 135, 61) // PL, Eredivisie, La Liga, Bundesliga, Serie A, Ligue 1
    val upcomingMatches = sortedMatches.filter { match ->
        match.status in listOf("NS", "TBD") && 
        (match.leagueId == null || !topLeagueIds.contains(match.leagueId))
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Date navigation bar
        item {
            DateNavigationBar(
                selectedDate = selectedDate,
                onDateChange = { daysToAdd -> viewModel.changeDate(daysToAdd) },
                onNavigateToToday = { viewModel.navigateToToday() },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Header section
        item {
            DashboardHeader()
        }

        // Hero match section (if available)
        if (heroMatch != null) {
            item {
                HeroMatchSection(
                    match = heroMatch,
                    onClick = { onMatchClick(heroMatch) }
                )
            }
        }

        // Live matches ticker (if available)
        if (liveMatches.isNotEmpty()) {
            item {
                LiveMatchesTicker(
                    matches = liveMatches,
                    favoriteLeagues = favoriteLeagues,
                    onMatchClick = onMatchClick
                )
            }
        }

        // Country-based league selection menu
        item {
            CountryLeagueMenuSection(
                viewModel = viewModel,
                onMatchClick = onMatchClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // League groups section (collapsible) - Top Competities
        if (leagueGroups.isNotEmpty()) {
            item {
                Text(
                    text = "📅 TOP COMPETITIES",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextHigh,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            items(items = leagueGroups, key = { it.leagueId }) { leagueGroup ->
                ExpandableLeagueSection(
                    leagueGroup = leagueGroup,
                    onMatchClick = onMatchClick,
                    onToggleExpanded = { viewModel.toggleLeagueExpansion(leagueGroup.leagueId) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Upcoming matches section for non-top leagues
        if (upcomingMatches.isNotEmpty()) {
            renderUpcomingMatchesSection(
                viewModel = viewModel,
                matches = upcomingMatches,
                favoriteLeagues = favoriteLeagues,
                onMatchClick = onMatchClick
            )
        }

        // Empty state if no matches
        if (sortedMatches.isEmpty() && leagueGroups.isEmpty() && upcomingMatches.isEmpty()) {
            item {
                EmptyMatchesState(
                    onNavigateToChat = onNavigateToChat
                )
            }
        }

        // DashX Features Section
        item {
            DashXFeaturesSection(
                matches = sortedMatches,
                onMatchClick = onMatchClick,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // AI Tools section
        item {
            AiToolsSection(
                onNavigateToChat = onNavigateToChat,
                onNavigateToSettings = onNavigateToSettings
            )
        }
    }
}

/**
 * Dashboard header with greeting and robot mascot.
 */
@Composable
private fun DashboardHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "MatchMind AI ⚽",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = TextHigh
            )
            
            Text(
                text = "Smart feed voor echte fans",
                style = MaterialTheme.typography.bodyLarge,
                color = TextMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Robot mascot icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(PrimaryNeon.copy(alpha = 0.2f))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = "AI Robot Mascot",
                tint = PrimaryNeon,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

/**
 * Hero match section - displays the #1 curated match.
 */
@Composable
private fun HeroMatchSection(
    match: MatchFixture,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "🔥 MUST-WATCH",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = ConfidenceHigh
            )

            StatusBadge(
                status = match.status,
                showIcon = true
            )
        }

        HeroMatchCard(
            match = match,
            hasEventCoverage = false, // Would come from match.coverage in real implementation
            onClick = onClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Live matches horizontal ticker.
 */
@Composable
private fun LiveMatchesTicker(
    matches: List<MatchFixture>,
    favoriteLeagues: Set<String>,
    onMatchClick: (MatchFixture) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "⚡ LIVE NU",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.Red
            )

            Text(
                text = "${matches.size} wedstrijden",
                style = MaterialTheme.typography.labelMedium,
                color = TextMedium
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items = matches, key = { match ->
                // Always return a String key to avoid nullability issues
                match.fixtureId?.toString() ?: "${match.homeTeam}_${match.awayTeam}_${match.time}_${match.league}"
            }) { match ->
                        StandardMatchCard(
                            match = match,
                            showLeague = true,
                            showTime = true,
                            isFavorite = match.leagueId?.toString()?.let { id: String -> id in favoriteLeagues } ?: false,
                            onClick = { onMatchClick(match) },
                            modifier = Modifier.width(280.dp)
                        )
            }
        }
    }
}

/**
 * League groups section with collapsible league sections.
 * Replaces the old UpcomingMatchesSection with proper league ID grouping.
 */
@Composable
private fun LeagueGroupsSection(
    leagueGroups: List<com.Lyno.matchmindai.domain.model.LeagueGroup>,
    onMatchClick: (MatchFixture) -> Unit,
    onToggleLeagueExpansion: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "📅 TOP COMPETITIES",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = TextHigh
        )

        // Display each league group as a collapsible section
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            leagueGroups.forEach { leagueGroup ->
                ExpandableLeagueSection(
                    leagueGroup = leagueGroup,
                    onMatchClick = onMatchClick,
                    onToggleExpanded = { onToggleLeagueExpansion(leagueGroup.leagueId) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Composable function for rendering upcoming matches section.
 * This handles state management for expandable league groups using ViewModel.
 */
@Composable
private fun UpcomingMatchesSection(
    viewModel: DashboardViewModel,
    matches: List<MatchFixture>,
    favoriteLeagues: Set<String>,
    onMatchClick: (MatchFixture) -> Unit
) {
    // Collect expansion states from ViewModel
    val upcomingExpandedStates by viewModel.upcomingExpandedLeagueStates.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Text(
            text = "📅 KOMENDE WEDSTRIJDEN",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = TextHigh,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Group matches by league ID for better organization and collapsible sections
        val matchesByLeagueId = matches.groupBy { it.leagueId ?: 0 }
        
        // Create LeagueGroup objects for each league
        val leagueGroups = matchesByLeagueId.map { (leagueId, leagueMatches) ->
            val firstMatch = leagueMatches.firstOrNull()
            com.Lyno.matchmindai.domain.model.LeagueGroup(
                leagueId = leagueId,
                leagueName = firstMatch?.league ?: "Onbekende competitie",
                country = firstMatch?.leagueCountry ?: "",
                logoUrl = firstMatch?.leagueLogo ?: "",
                matches = leagueMatches,
                isExpanded = upcomingExpandedStates[leagueId] ?: false
            )
        }
        
        // Sort league groups by league name
        val sortedLeagueGroups = leagueGroups.sortedBy { it.leagueName }
        
        // Render each league group as a collapsible section
        sortedLeagueGroups.forEach { leagueGroup ->
            ExpandableLeagueSection(
                leagueGroup = leagueGroup,
                onMatchClick = onMatchClick,
                onToggleExpanded = {
                    // Use ViewModel to toggle expansion state
                    viewModel.toggleUpcomingLeagueExpansion(leagueGroup.leagueId)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * LazyListScope extension function for rendering upcoming matches section.
 * This improves scroll performance by properly utilizing LazyColumn's lazy loading.
 * Now uses UpcomingMatchesSection composable for proper state management.
 */
private fun LazyListScope.renderUpcomingMatchesSection(
    viewModel: DashboardViewModel,
    matches: List<MatchFixture>,
    favoriteLeagues: Set<String>,
    onMatchClick: (MatchFixture) -> Unit
) {
    item {
        UpcomingMatchesSection(
            viewModel = viewModel,
            matches = matches,
            favoriteLeagues = favoriteLeagues,
            onMatchClick = onMatchClick
        )
    }
}

/**
 * Empty state when no matches are available.
 */
@Composable
private fun EmptyMatchesState(
    onNavigateToChat: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        shape = MaterialTheme.shapes.large,
        color = SurfaceCard.copy(alpha = 0.6f),
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = "AI Robot",
                tint = PrimaryNeon,
                modifier = Modifier.size(48.dp)
            )

            Text(
                text = "Geen wedstrijden gevonden",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = TextHigh,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Vraag de AI analist om voorspellingen voor specifieke wedstrijden",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMedium,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onNavigateToChat,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryNeon,
                    contentColor = Color.Black
                ),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "Naar Chat Analist",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

/**
 * AI Tools section with glassmorphic cards.
 */
@Composable
private fun AiToolsSection(
    onNavigateToChat: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "🤖 AI TOOLS",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = TextHigh,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Chat Analyst card
            AiToolCard(
                title = "Chat Analist",
                icon = Icons.Filled.Info,
                description = "Vraag voorspellingen en analyses",
                onClick = onNavigateToChat,
                modifier = Modifier.weight(1f)
            )

            // Settings card
            AiToolCard(
                title = "Instellingen",
                icon = Icons.Filled.Settings,
                description = "API keys en voorkeuren",
                onClick = onNavigateToSettings,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * AI Tool card with glassmorphic effect.
 */
@Composable
private fun AiToolCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = SurfaceCard.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(PrimaryNeon.copy(alpha = 0.2f))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = PrimaryNeon,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextHigh
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

/**
 * Loading state for dashboard.
 * UPDATED: Simplified since smart feed is pre-loaded during loading screen.
 */
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
            CircularProgressIndicator(
                color = PrimaryNeon,
                strokeWidth = 2.dp
            )

            Text(
                text = "Finalising dashboard...",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMedium
            )
        }
    }
}

/**
 * Error state for dashboard.
 */
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
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = "Error",
                tint = ConfidenceLow,
                modifier = Modifier.size(48.dp)
            )

            Text(
                text = "Oeps! Er ging iets mis",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = TextHigh,
                textAlign = TextAlign.Center
            )

            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMedium,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryNeon,
                    contentColor = Color.Black
                ),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "Opnieuw proberen",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

/**
 * Missing API key state for dashboard.
 */
@Composable
private fun MissingApiKeyState(
    onNavigateToSettings: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Settings",
                tint = PrimaryNeon,
                modifier = Modifier.size(48.dp)
            )

            Text(
                text = "API Key vereist",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = TextHigh,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Voeg je SportData API key toe in de instellingen om wedstrijden te kunnen zien",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMedium,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onNavigateToSettings,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryNeon,
                    contentColor = Color.Black
                ),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "Naar Instellingen",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

/**
 * Preview for DashboardScreen with mock data.
 */
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun DashboardScreenPreview() {
    MatchMindAITheme {
        // Note: In preview, we can't provide a real ViewModel
        // This is just for layout preview
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(GradientStart, GradientEnd)
                    )
                )
        ) {
            LoadingState()
        }
    }
}

/**
 * Preview for LoadingState.
 */
@Preview(showBackground = true)
@Composable
fun LoadingStatePreview() {
    MatchMindAITheme {
        LoadingState()
    }
}

/**
 * Preview for ErrorState.
 */
@Preview(showBackground = true)
@Composable
fun ErrorStatePreview() {
    MatchMindAITheme {
        ErrorState(
            errorMessage = "Kon geen wedstrijden laden. Controleer je internetverbinding.",
            onRetry = {}
        )
    }
}

/**
 * DashX Features Section with expandable cards, video highlights, and swipe actions.
 */
@Composable
private fun DashXFeaturesSection(
    matches: List<MatchFixture>,
    onMatchClick: (MatchFixture) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section header
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "🚀 DASHX FEATURES",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = PrimaryNeon
            )

            Text(
                text = "Live updates & interactie",
                style = MaterialTheme.typography.labelSmall,
                color = TextMedium
            )
        }

        // Expandable match cards for live matches
        val liveMatches = matches.filter { it.status in listOf("1H", "2H", "HT", "LIVE") }
        if (liveMatches.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Uitklapbare Live Wedstrijden",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextHigh
                )

                liveMatches.take(3).forEach { match ->
                    val (isExpanded, setIsExpanded) = remember { mutableStateOf(false) }
                    
                    ExpandableMatchCard(
                        match = match,
                        isExpanded = isExpanded,
                        onToggleExpanded = { setIsExpanded(!isExpanded) },
                        onClick = { onMatchClick(match) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Video highlights for featured match
        val featuredMatch = matches.firstOrNull { it.fixtureId != null }
        if (featuredMatch?.fixtureId != null) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Video Highlights",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextHigh
                )

                VideoHighlightsComponent(
                    fixtureId = featuredMatch.fixtureId!!,
                    highlights = VideoHighlightsPreview.getPreviewHighlights(featuredMatch.fixtureId!!),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Swipeable match cards demo
        val upcomingMatches = matches.filter { it.status in listOf("NS", "TBD") }
        if (upcomingMatches.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Swipe Actions Demo",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextHigh
                )

                upcomingMatches.take(2).forEach { match ->
                    SwipeableMatchCard(
                        match = match,
                        onFavorite = {
                            // Handle favorite action
                        },
                        onShare = {
                            // Handle share action
                        },
                        onDelete = {
                            // Handle delete action
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StandardMatchCard(
                            match = match,
                            showLeague = true,
                            showTime = true,
                            isFavorite = false,
                            onClick = { onMatchClick(match) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Live update service status
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Live Update Service",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextHigh
                )

                // Status indicator
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color.Green)
                )
            }

            Text(
                text = "Live updates actief voor ${liveMatches.size} wedstrijden",
                style = MaterialTheme.typography.labelSmall,
                color = TextMedium
            )
        }
    }
}

/**
 * Optimized dashboard content that reduces recomposition by using derived state.
 */
@Composable
private fun OptimizedDashboardContent(
    viewModel: DashboardViewModel,
    curatedFeed: CuratedFeed,
    onMatchClick: (MatchFixture) -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    // Collect only necessary states with minimal recomposition
    val selectedDate by viewModel.selectedDate.collectAsState()
    val favoriteLeagues by viewModel.favoritesManagerPublic.favoriteLeagues.collectAsState(initial = emptySet())
    val leagueGroups by viewModel.leagueGroups.collectAsState(initial = emptyList())
    
    // Use remember to cache expensive computations
    val heroMatch = remember(curatedFeed) { curatedFeed.heroMatch }
    val liveMatches = remember(curatedFeed) { 
        curatedFeed.liveMatches.filter { it.status in listOf("1H", "2H", "HT", "LIVE") }
    }
    
    // Filter upcoming matches for non-top leagues (to show in UpcomingMatchesSection)
    val topLeagueIds = remember { setOf(39, 88, 140, 78, 135, 61) } // PL, Eredivisie, La Liga, Bundesliga, Serie A, Ligue 1
    val upcomingMatches = remember(curatedFeed, topLeagueIds) {
        curatedFeed.upcomingMatches.filter { match ->
            match.status in listOf("NS", "TBD") && 
            (match.leagueId == null || !topLeagueIds.contains(match.leagueId))
        }
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Date navigation bar
        item {
            DateNavigationBar(
                selectedDate = selectedDate,
                onDateChange = { daysToAdd -> viewModel.changeDate(daysToAdd) },
                onNavigateToToday = { viewModel.navigateToToday() },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Header section
        item {
            DashboardHeader()
        }

        // Hero match section (if available)
        if (heroMatch != null) {
            item {
                HeroMatchSection(
                    match = heroMatch,
                    onClick = { onMatchClick(heroMatch) }
                )
            }
        }

        // Live matches ticker (if available)
        if (liveMatches.isNotEmpty()) {
            item {
                LiveMatchesTicker(
                    matches = liveMatches,
                    favoriteLeagues = favoriteLeagues,
                    onMatchClick = onMatchClick
                )
            }
        }

        // League groups section (collapsible) - Top Competities
        if (leagueGroups.isNotEmpty()) {
            item {
                Text(
                    text = "📅 TOP COMPETITIES",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextHigh,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            items(items = leagueGroups, key = { it.leagueId }) { leagueGroup ->
                ExpandableLeagueSection(
                    leagueGroup = leagueGroup,
                    onMatchClick = onMatchClick,
                    onToggleExpanded = { viewModel.toggleLeagueExpansion(leagueGroup.leagueId) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Upcoming matches section for non-top leagues
        if (upcomingMatches.isNotEmpty()) {
            renderUpcomingMatchesSection(
                viewModel = viewModel,
                matches = upcomingMatches,
                favoriteLeagues = favoriteLeagues,
                onMatchClick = onMatchClick
            )
        }

        // Empty state if no matches
        if (curatedFeed.heroMatch == null && curatedFeed.liveMatches.isEmpty() && 
            curatedFeed.upcomingMatches.isEmpty() && leagueGroups.isEmpty()) {
            item {
                EmptyMatchesState(
                    onNavigateToChat = onNavigateToChat
                )
            }
        }

        // DashX Features Section - only show if there are matches
        if (curatedFeed.liveMatches.isNotEmpty() || curatedFeed.upcomingMatches.isNotEmpty()) {
            item {
                OptimizedDashXFeaturesSection(
                    curatedFeed = curatedFeed,
                    onMatchClick = onMatchClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // AI Tools section
        item {
            AiToolsSection(
                onNavigateToChat = onNavigateToChat,
                onNavigateToSettings = onNavigateToSettings
            )
        }
    }
}

/**
 * Optimized DashX Features Section that reduces recomposition.
 */
@Composable
private fun OptimizedDashXFeaturesSection(
    curatedFeed: CuratedFeed,
    onMatchClick: (MatchFixture) -> Unit,
    modifier: Modifier = Modifier
) {
    // Use remember to cache computations
    val liveMatches = remember(curatedFeed) { 
        curatedFeed.liveMatches.filter { it.status in listOf("1H", "2H", "HT", "LIVE") }
    }
    val upcomingMatches = remember(curatedFeed) { 
        curatedFeed.upcomingMatches.filter { it.status in listOf("NS", "TBD") }
    }
    val featuredMatch = remember(curatedFeed) { 
        curatedFeed.liveMatches.firstOrNull { it.fixtureId != null } ?: 
        curatedFeed.upcomingMatches.firstOrNull { it.fixtureId != null }
    }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section header
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "🚀 DASHX FEATURES",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = PrimaryNeon
            )

            Text(
                text = "Live updates & interactie",
                style = MaterialTheme.typography.labelSmall,
                color = TextMedium
            )
        }

        // Expandable match cards for live matches
        if (liveMatches.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Uitklapbare Live Wedstrijden",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextHigh
                )

                liveMatches.take(3).forEach { match ->
                    val (isExpanded, setIsExpanded) = remember { mutableStateOf(false) }
                    
                    ExpandableMatchCard(
                        match = match,
                        isExpanded = isExpanded,
                        onToggleExpanded = { setIsExpanded(!isExpanded) },
                        onClick = { onMatchClick(match) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Video highlights for featured match
        if (featuredMatch?.fixtureId != null) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Video Highlights",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextHigh
                )

                VideoHighlightsComponent(
                    fixtureId = featuredMatch.fixtureId!!,
                    highlights = VideoHighlightsPreview.getPreviewHighlights(featuredMatch.fixtureId!!),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Swipeable match cards demo
        if (upcomingMatches.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Swipe Actions Demo",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextHigh
                )

                upcomingMatches.take(2).forEach { match ->
                    SwipeableMatchCard(
                        match = match,
                        onFavorite = {
                            // Handle favorite action
                        },
                        onShare = {
                            // Handle share action
                        },
                        onDelete = {
                            // Handle delete action
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StandardMatchCard(
                            match = match,
                            showLeague = true,
                            showTime = true,
                            isFavorite = false,
                            onClick = { onMatchClick(match) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Live update service status
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Live Update Service",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextHigh
                )

                // Status indicator
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color.Green)
                )
            }

            Text(
                text = "Live updates actief voor ${liveMatches.size} wedstrijden",
                style = MaterialTheme.typography.labelSmall,
                color = TextMedium
            )
        }
    }
}

/**
 * Country-based league selection menu section.
 */
@Composable
private fun CountryLeagueMenuSection(
    viewModel: DashboardViewModel,
    onMatchClick: (MatchFixture) -> Unit,
    modifier: Modifier = Modifier
) {
    // Collect country groups and selection states
    val countryGroups by viewModel.countryGroups.collectAsState(initial = emptyList())
    val selectedCountries by viewModel.selectedCountries.collectAsState(initial = emptySet())
    val selectedLeagues by viewModel.selectedLeagues.collectAsState(initial = emptySet())
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section header
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "🌍 COMPETITIES PER LAND",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = PrimaryNeon
            )
            
            Text(
                text = "Selecteer per land",
                style = MaterialTheme.typography.labelSmall,
                color = TextMedium
            )
        }
        
        // Stats summary
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            StatItem(
                label = "Landen",
                value = countryGroups.size.toString(),
                color = PrimaryNeon
            )
            
            StatItem(
                label = "Competities",
                value = countryGroups.sumOf { it.leagueCount }.toString(),
                color = TextHigh
            )
            
            StatItem(
                label = "Geselecteerd",
                value = selectedLeagues.size.toString(),
                color = ConfidenceHigh
            )
        }
        
        // Country groups list (show first 3 countries as preview)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            countryGroups.take(3).forEach { countryGroup ->
                CountrySection(
                    countryGroup = countryGroup.copy(
                        isExpanded = selectedCountries.contains(countryGroup.countryCode),
                        isSelected = selectedCountries.contains(countryGroup.countryCode)
                    ),
                    onLeagueClick = { league -> viewModel.toggleLeagueSelection(league.leagueId) },
                    onMatchClick = onMatchClick,
                    onToggleCountryExpanded = { viewModel.toggleCountryExpansion(countryGroup.countryCode) },
                    onToggleCountrySelected = { viewModel.toggleCountrySelection(countryGroup.countryCode) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Show "View all" button if there are more countries
            if (countryGroups.size > 3) {
                Text(
                    text = "En nog ${countryGroups.size - 3} landen...",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            // TODO: Navigate to full country selection screen
                        }
                )
            }
        }
        
        // Selection summary
        if (selectedLeagues.isNotEmpty()) {
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "${selectedLeagues.size} geselecteerd",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = TextHigh
                        )
                        Text(
                            text = "Competities in ${selectedCountries.size} landen",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMedium
                        )
                    }
                    
                    Text(
                        text = "Toepassen",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNeon
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.applyLeagueSelections() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

/**
 * Preview for MissingApiKeyState.
 */
@Preview(showBackground = true)
@Composable
fun MissingApiKeyStatePreview() {
    MatchMindAITheme {
        MissingApiKeyState(
            onNavigateToSettings = {}
        )
    }
}
