package com.Lyno.matchmindai.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Lyno.matchmindai.MatchMindApplication
import com.Lyno.matchmindai.R
import com.Lyno.matchmindai.domain.model.FavoriteTeamData
import com.Lyno.matchmindai.domain.model.MatchFixture
import com.Lyno.matchmindai.domain.model.MatchStatus
import com.Lyno.matchmindai.domain.model.NewsItemData
import com.Lyno.matchmindai.domain.model.LeagueGroup
import com.Lyno.matchmindai.presentation.components.favorites.FavoriteTeamHeader
import com.Lyno.matchmindai.presentation.components.favorites.NextMatchCard
import com.Lyno.matchmindai.presentation.components.favorites.TeamNewsFeed
import com.Lyno.matchmindai.presentation.components.favorites.LeagueStandingsCard
import com.Lyno.matchmindai.presentation.components.favorites.FavoriteTeamItem
import com.Lyno.matchmindai.presentation.viewmodel.FavoritesViewModel
import com.Lyno.matchmindai.presentation.viewmodel.FavoritesUiState
import com.Lyno.matchmindai.presentation.viewmodel.TeamNewsState
import com.Lyno.matchmindai.presentation.viewmodel.NextMatchState
import com.Lyno.matchmindai.presentation.viewmodel.LeagueStandingsState
import com.Lyno.matchmindai.ui.theme.MatchMindAITheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel = viewModel(
        factory = (androidx.compose.ui.platform.LocalContext.current.applicationContext as MatchMindApplication).appContainer.favoritesViewModelFactory
    ),
    onNavigateBack: () -> Unit,
    onNavigateToMatch: (Int) -> Unit,
    onNavigateToNews: (String, String) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val favoriteMatches by viewModel.favoriteMatches.collectAsStateWithLifecycle()
    val teamNewsState by viewModel.teamNewsState.collectAsStateWithLifecycle()
    val nextMatchState by viewModel.nextMatchState.collectAsStateWithLifecycle()
    val leagueStandingsState by viewModel.leagueStandingsState.collectAsStateWithLifecycle()

    // Debug logging
    android.util.Log.d("FavoritesScreen", "UI State: $uiState")
    android.util.Log.d("FavoritesScreen", "Favorite matches from Flow: ${favoriteMatches.size}")
    if (uiState is FavoritesUiState.Success) {
        val favoriteTeamsData = (uiState as FavoritesUiState.Success).favoriteTeamsData
        android.util.Log.d("FavoritesScreen", "Favorite teams data from UI State: ${favoriteTeamsData.size}")
        favoriteTeamsData.forEachIndexed { index, teamData ->
            android.util.Log.d("FavoritesScreen", "Team $index: ${teamData.getDisplayName()} (ID: ${teamData.teamId})")
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "⭐ FAVOX HUB",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Terug"
                    )
                }
            }
        )

        when (uiState) {
            is FavoritesUiState.Loading -> {
                LoadingState()
            }
            is FavoritesUiState.Success -> {
                val favoriteTeamsData = (uiState as FavoritesUiState.Success).favoriteTeamsData
                if (favoriteTeamsData.isEmpty()) {
                    EmptyState(
                        onNavigateToSettings = onNavigateBack
                    )
                } else {
                    FavoXHubContent(
                        favoriteTeamsData = favoriteTeamsData,
                        teamNewsState = teamNewsState,
                        nextMatchState = nextMatchState,
                        leagueStandingsState = leagueStandingsState,
                        onMatchClick = { fixtureId -> onNavigateToMatch(fixtureId) },
                        onNewsClick = onNavigateToNews,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            is FavoritesUiState.Error -> {
                ErrorState(
                    errorMessage = (uiState as FavoritesUiState.Error).message,
                    onRetry = { viewModel.refresh() }
                )
            }
        }
    }
}

@Composable
private fun FavoXHubContent(
    favoriteTeamsData: List<FavoriteTeamData>,
    teamNewsState: TeamNewsState,
    nextMatchState: NextMatchState,
    leagueStandingsState: LeagueStandingsState,
    onMatchClick: (Int) -> Unit,
    onNewsClick: (String, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    // Determine team name from first favorite team and count unique teams
    val teamCount = favoriteTeamsData.size
    val firstTeamName = favoriteTeamsData.firstOrNull()?.getDisplayName() ?: "Onbekend team"
    
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Favorite Team Header
        item {
            FavoriteTeamHeader(
                teamName = firstTeamName,
                teamCount = teamCount
            )
        }

        // Next Match Card (for first team only, for backward compatibility)
        item {
            NextMatchCard(nextMatchState = nextMatchState, onMatchClick = onMatchClick)
        }

        // Team News Feed (for first team only, for backward compatibility)
        item {
            TeamNewsFeed(
                teamNewsState = teamNewsState,
                onNewsClick = onNewsClick
            )
        }

        // League Standings Card (for first team only, for backward compatibility)
        item {
            LeagueStandingsCard(leagueStandingsState = leagueStandingsState)
        }

        // Multiple Teams Section
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favoriete teams",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AL JE FAVORIETE TEAMS",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Text(
                    text = "Alle favoriete teams met hun laatste nieuws, volgende wedstrijden en competitie standen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Favorite Team Items
        items(favoriteTeamsData) { teamData ->
            FavoriteTeamItem(
                teamData = teamData,
                onNewsItemClick = { newsItem: com.Lyno.matchmindai.domain.model.NewsItemData ->
                    onNewsClick(newsItem.headline, newsItem.url)
                },
                onNextMatchClick = { match: com.Lyno.matchmindai.domain.model.MatchFixture ->
                    match.fixtureId?.let { fixtureId ->
                        onMatchClick(fixtureId)
                    }
                },
                onStandingsClick = { standings: List<com.Lyno.matchmindai.domain.model.StandingRow> ->
                    // Handle standings click if needed
                }
            )
        }

        // Footer
        item {
            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FAVOX HUB INFO",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "De FavoX Hub toont alles over je favoriete teams: laatste nieuws, volgende wedstrijden, competitie standen en alle komende wedstrijden.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun MatchItem(
    match: MatchFixture,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Match status and time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatMatchStatus(match),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = formatMatchTime(match),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Teams and score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = match.homeTeam,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (match.league.isNotEmpty()) {
                        Text(
                            text = match.league,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // Score or VS
                if (match.status == null || match.status == "NS" || match.status == "TBD") {
                    Text(
                        text = "VS",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${match.homeScore ?: 0}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "-",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${match.awayScore ?: 0}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = match.awayTeam,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (match.leagueCountry != null) {
                        Text(
                            text = match.leagueCountry,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            // Live indicator
            if (match.status in listOf("1H", "HT", "2H", "ET", "P", "SUSP", "INT")) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color.Red, shape = androidx.compose.foundation.shape.CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LIVE ${match.elapsed ?: 0}'",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
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
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = "Geen favorieten",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            
            Text(
                text = "Nog geen favoriete teams",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Text(
                text = "Voeg teams toe via 'Meerdere Favoriete Teams' in Instellingen om hier wedstrijden te zien.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Button(
                onClick = onNavigateToSettings,
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
                color = MaterialTheme.colorScheme.primary
            )
            
                    Text(
                        text = "De FavoX Hub toont alles over je favoriete teams: laatste nieuws, volgende wedstrijden, competitie standen en alle komende wedstrijden.",
                        style = MaterialTheme.typography.bodySmall,
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
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            
            Text(
                text = "Oeps! Er ging iets mis",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Button(
                onClick = onRetry,
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

private fun formatMatchStatus(match: MatchFixture): String {
    return when (match.status) {
        "NS", "TBD" -> "Nog niet begonnen"
        "1H" -> "Eerste helft"
        "HT" -> "Rust"
        "2H" -> "Tweede helft"
        "ET" -> "Extra tijd"
        "P" -> "Penalty's"
        "FT" -> "Einde"
        "AET" -> "Einde (extra tijd)"
        "PEN" -> "Penalty's"
        "SUSP" -> "Geschorst"
        "INT" -> "Onderbroken"
        "PST" -> "Uitgesteld"
        "CANC" -> "Geannuleerd"
        "ABD" -> "Afgebroken"
        "AWD" -> "Toegekend"
        "WO" -> "Walkover"
        else -> match.status ?: "Onbekend"
    }
}

private fun formatMatchTime(match: MatchFixture): String {
    return try {
        if (match.date.isNotEmpty()) {
            // Format like "13-12 00:00" or "14:30"
            if (match.date.contains(" ")) {
                match.date.split(" ")[1] // Get time part
            } else {
                match.date
            }
        } else {
            match.time
        }
    } catch (e: Exception) {
        match.time
    }
}

@Preview(showBackground = true)
@Composable
fun FavoritesScreenPreview() {
    MatchMindAITheme {
        // Note: In preview, we can't provide a real ViewModel
        // This is just for layout preview
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Favorites Screen Preview",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
