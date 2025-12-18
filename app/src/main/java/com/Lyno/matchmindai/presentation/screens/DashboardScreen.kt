package com.Lyno.matchmindai.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.Lyno.matchmindai.presentation.components.matches.HeroMatchCard
import com.Lyno.matchmindai.presentation.components.matches.StandardMatchCard
import com.Lyno.matchmindai.presentation.components.matches.StatusBadge
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
    // Collect state from ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val curatedFeed by viewModel.curatedFeed.collectAsState()

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
        when (uiState) {
            DashboardUiState.Idle -> {
                // Show loading state while idle (initial state)
                LoadingState()
            }
            DashboardUiState.Loading -> {
                LoadingState()
            }
            is DashboardUiState.Success -> {
                CuratedDashboardContent(
                    curatedFeed = curatedFeed,
                    onMatchClick = onMatchClick,
                    onNavigateToChat = onNavigateToChat,
                    onNavigateToSettings = onNavigateToSettings
                )
            }
            is DashboardUiState.Error -> {
                ErrorState(
                    errorMessage = (uiState as DashboardUiState.Error).message,
                    onRetry = { viewModel.loadCuratedFeed() }
                )
            }
            DashboardUiState.MissingApiKey -> {
                MissingApiKeyState(
                    onNavigateToSettings = onNavigateToSettings
                )
            }
            else -> {
                // Fallback for any unexpected state
                LoadingState()
            }
        }
    }
}

/**
 * Main content for the curated dashboard.
 */
@Composable
private fun CuratedDashboardContent(
    curatedFeed: CuratedFeed,
    onMatchClick: (MatchFixture) -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header section
        item {
            DashboardHeader()
        }

        // Hero match section (if available)
        if (curatedFeed.heroMatch != null) {
            item {
                HeroMatchSection(
                    match = curatedFeed.heroMatch,
                    onClick = { onMatchClick(curatedFeed.heroMatch) }
                )
            }
        }

        // Live matches ticker (if available)
        if (curatedFeed.liveMatches.isNotEmpty()) {
            item {
                LiveMatchesTicker(
                    matches = curatedFeed.liveMatches,
                    onMatchClick = onMatchClick
                )
            }
        }

        // Upcoming matches section
        if (curatedFeed.upcomingMatches.isNotEmpty()) {
            item {
                UpcomingMatchesSection(
                    matches = curatedFeed.upcomingMatches,
                    onMatchClick = onMatchClick
                )
            }
        }

        // Empty state if no matches
        if (curatedFeed.isEmpty) {
            item {
                EmptyMatchesState(
                    onNavigateToChat = onNavigateToChat
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
            items(matches) { match ->
                StandardMatchCard(
                    match = match,
                    showLeague = true,
                    showTime = true,
                    onClick = { onMatchClick(match) },
                    modifier = Modifier.width(280.dp)
                )
            }
        }
    }
}

/**
 * Upcoming matches section with categorized lists.
 */
@Composable
private fun UpcomingMatchesSection(
    matches: List<MatchFixture>,
    onMatchClick: (MatchFixture) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "📅 KOMENDE WEDSTRIJDEN",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = TextHigh
        )

        // Group matches by league for better organization
        val matchesByLeague = matches.groupBy { it.league }

        matchesByLeague.forEach { (league, leagueMatches) ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // League header
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = league,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = TextHigh
                    )

                    Text(
                        text = "${leagueMatches.size} wedstrijden",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMedium
                    )
                }

                // Matches in this league
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    leagueMatches.forEach { match ->
                        StandardMatchCard(
                            match = match,
                            showLeague = false, // Already showing league header
                            showTime = true,
                            onClick = { onMatchClick(match) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
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
                text = "Smart feed laden...",
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
        DashboardScreen(
            onMatchClick = {},
            onNavigateToChat = {},
            onNavigateToSettings = {}
        )
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
