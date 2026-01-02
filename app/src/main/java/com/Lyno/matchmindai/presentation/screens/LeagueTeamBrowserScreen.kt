package com.Lyno.matchmindai.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.Lyno.matchmindai.MatchMindApplication
import com.Lyno.matchmindai.domain.model.CountryGroup
import com.Lyno.matchmindai.domain.model.LeagueGroup
import com.Lyno.matchmindai.presentation.components.ExpandableLeagueSection
import com.Lyno.matchmindai.presentation.components.leagues.CountrySection
import com.Lyno.matchmindai.presentation.components.leagues.StatItem
import com.Lyno.matchmindai.presentation.viewmodel.LeagueTeamBrowserViewModel
import com.Lyno.matchmindai.ui.theme.*
import androidx.compose.foundation.BorderStroke
import kotlinx.coroutines.launch

/**
 * Screen for browsing leagues and teams to add as favorites.
 * Similar to Dashboard's CountryLeagueMenuSection but focused on team selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeagueTeamBrowserScreen(
    onNavigateBack: () -> Unit,
    viewModel: LeagueTeamBrowserViewModel = viewModel(
        factory = (LocalContext.current.applicationContext as MatchMindApplication).appContainer.chatViewModelFactory
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Teams toevoegen aan favorieten",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Terug")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(GradientStart, GradientEnd)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Text(
                    text = "🌍 Competities per land",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextHigh,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "Selecteer een competitie om teams te zien en toe te voegen aan je favorieten",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Stats summary
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    StatItem(
                        label = "Landen",
                        value = uiState.countryGroups.size.toString(),
                        color = PrimaryNeon
                    )
                    
                    StatItem(
                        label = "Competities",
                        value = uiState.countryGroups.sumOf { it.leagueCount }.toString(),
                        color = TextHigh
                    )
                    
                    StatItem(
                        label = "Favorieten",
                        value = uiState.favoriteTeamIds.size.toString(),
                        color = ConfidenceHigh
                    )
                }

                // Country groups list
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.countryGroups) { countryGroup ->
                        CountryTeamSection(
                            countryGroup = countryGroup.copy(
                                isExpanded = uiState.expandedCountries.contains(countryGroup.countryCode),
                                isSelected = uiState.selectedCountries.contains(countryGroup.countryCode)
                            ),
                            favoriteTeamIds = uiState.favoriteTeamIds,
                            onTeamFavoriteToggle = { teamId, teamName ->
                                scope.launch {
                                    viewModel.toggleTeamFavorite(teamId, teamName)
                                    snackbarHostState.showSnackbar(
                                        message = "$teamName toegevoegd aan favorieten",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            },
                            onToggleCountryExpanded = { viewModel.toggleCountryExpansion(countryGroup.countryCode) },
                            onToggleCountrySelected = { viewModel.toggleCountrySelection(countryGroup.countryCode) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Selected teams summary
                if (uiState.favoriteTeamIds.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = SurfaceCard.copy(alpha = 0.8f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Text(
                                        text = "${uiState.favoriteTeamIds.size} favoriete teams",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = TextHigh
                                    )
                                    Text(
                                        text = "Klaar om op te slaan",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMedium
                                    )
                                }
                                
                                Button(
                                    onClick = {
                                        scope.launch {
                                            viewModel.saveFavorites()
                                            snackbarHostState.showSnackbar(
                                                message = "Favorieten opgeslagen!",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PrimaryNeon,
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Text(
                                        text = "Opslaan",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                            
                            // Favorite teams preview
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                            ) {
                                items(uiState.favoriteTeamIds.take(5)) { teamId ->
                                    val team = uiState.allTeams.firstOrNull { it.teamId == teamId }
                                    team?.let {
                                        FavoriteTeamChip(
                                            team = it,
                                            onRemove = {
                                                scope.launch {
                                                    viewModel.toggleTeamFavorite(teamId, it.teamName)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Country section with expandable leagues and teams.
 */
@Composable
private fun CountryTeamSection(
    countryGroup: CountryGroup,
    favoriteTeamIds: Set<Int>,
    onTeamFavoriteToggle: (Int, String) -> Unit,
    onToggleCountryExpanded: () -> Unit,
    onToggleCountrySelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = SurfaceCard.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Country header
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleCountryExpanded() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                        // Country flag/logo placeholder
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(PrimaryNeon.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = countryGroup.countryCode.take(2),
                                style = MaterialTheme.typography.labelMedium,
                                color = TextHigh,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    
                    Column {
                        Text(
                            text = countryGroup.countryName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextHigh
                        )
                        Text(
                            text = "${countryGroup.leagueCount} competities",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMedium
                        )
                    }
                }
                
                // Expand/collapse indicator
                Text(
                    text = if (countryGroup.isExpanded) "▲" else "▼",
                    style = MaterialTheme.typography.bodyLarge,
                    color = PrimaryNeon
                )
            }

            // Expanded content with leagues and teams
            if (countryGroup.isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    countryGroup.leagues.forEach { league ->
                        LeagueTeamSection(
                            league = league,
                            favoriteTeamIds = favoriteTeamIds,
                            onTeamFavoriteToggle = onTeamFavoriteToggle,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

/**
 * League section with teams grid.
 */
@Composable
private fun LeagueTeamSection(
    league: LeagueGroup,
    favoriteTeamIds: Set<Int>,
    onTeamFavoriteToggle: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // League header
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (league.logoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(league.logoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "League logo",
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                
                Text(
                    text = league.leagueName,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextHigh
                )
            }
            
            Text(
                text = "${league.matches.size} teams",
                style = MaterialTheme.typography.labelSmall,
                color = TextMedium
            )
        }

            // Teams grid
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Extract unique teams from matches
                val teams = league.matches.flatMap { match ->
                    listOf(
                        TeamInfo(match.homeTeamId ?: 0, match.homeTeam, ""),
                        TeamInfo(match.awayTeamId ?: 0, match.awayTeam, "")
                    )
                }.distinctBy { it.teamId }
                
                items(teams) { team ->
                    TeamCard(
                        team = team,
                        isFavorite = favoriteTeamIds.contains(team.teamId),
                        onFavoriteToggle = { onTeamFavoriteToggle(team.teamId, team.teamName) },
                        modifier = Modifier.width(120.dp)
                    )
                }
            }
    }
}

/**
 * Team card with favorite toggle.
 */
@Composable
private fun TeamCard(
    team: TeamInfo,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isFavorite) {
                PrimaryNeon.copy(alpha = 0.2f)
            } else {
                SurfaceCard.copy(alpha = 0.8f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isFavorite) 4.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Team logo
            if (team.logoUrl.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(team.logoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Team logo",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            
            // Team name
            Text(
                text = team.teamName,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = TextHigh,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Favorite button
            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (isFavorite) "Verwijder uit favorieten" else "Voeg toe aan favorieten",
                    tint = if (isFavorite) ConfidenceHigh else TextMedium,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Favorite team chip for preview.
 */
@Composable
private fun FavoriteTeamChip(
    team: TeamInfo,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = PrimaryNeon.copy(alpha = 0.2f),
        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryNeon)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (team.logoUrl.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(team.logoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Team logo",
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            
            Text(
                text = team.teamName,
                style = MaterialTheme.typography.labelSmall,
                color = TextHigh,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Verwijder",
                    tint = ConfidenceHigh,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

/**
 * Data class for team information.
 */
data class TeamInfo(
    val teamId: Int,
    val teamName: String,
    val logoUrl: String
)
