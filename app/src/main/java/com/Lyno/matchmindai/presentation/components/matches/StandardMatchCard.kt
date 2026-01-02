package com.Lyno.matchmindai.presentation.components.matches

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Lyno.matchmindai.MatchMindApplication
import com.Lyno.matchmindai.domain.model.MatchFixture
import com.Lyno.matchmindai.presentation.components.ApiSportsImage
import com.Lyno.matchmindai.presentation.viewmodel.FavoritesViewModel
import com.Lyno.matchmindai.ui.theme.SurfaceCard
import com.Lyno.matchmindai.ui.theme.TextHigh
import com.Lyno.matchmindai.ui.theme.TextMedium

/**
 * A compact row-style card for displaying match information in lists.
 * Designed for efficient rendering in LazyColumn/LazyRow layouts.
 */
@Composable
fun StandardMatchCard(
    match: MatchFixture,
    modifier: Modifier = Modifier,
    showLeague: Boolean = true,
    showTime: Boolean = true,
    isFavorite: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val viewModel: FavoritesViewModel = viewModel(
        factory = (LocalContext.current.applicationContext as MatchMindApplication).appContainer.chatViewModelFactory
    )
    val favoriteTeamIds by viewModel.favoriteTeamIds.collectAsState(initial = emptySet())
    
    val isHomeTeamFavorite = match.homeTeamId?.let { favoriteTeamIds.contains(it) } ?: false
    val isAwayTeamFavorite = match.awayTeamId?.let { favoriteTeamIds.contains(it) } ?: false
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceCard,
            contentColor = TextHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick ?: {}
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Home team with favorite button
            TeamSection(
                teamName = match.homeTeam,
                score = match.homeScore,
                isHomeTeam = true,
                teamId = match.homeTeamId,
                isFavorite = isHomeTeamFavorite,
                onFavoriteToggle = {
                    match.homeTeamId?.let { teamId ->
                        // TODO: Implement toggleTeamFavorite method in FavoritesViewModel
                    }
                },
                modifier = Modifier.weight(1f)
            )

            // Center: Match info and status
            MatchCenterSection(
                match = match,
                showLeague = showLeague,
                showTime = showTime,
                isFavorite = isFavorite,
                modifier = Modifier.weight(1f)
            )

            // Right side: Away team with favorite button
            TeamSection(
                teamName = match.awayTeam,
                score = match.awayScore,
                isHomeTeam = false,
                teamId = match.awayTeamId,
                isFavorite = isAwayTeamFavorite,
                onFavoriteToggle = {
                    match.awayTeamId?.let { teamId ->
                        // TODO: Implement toggleTeamFavorite method in FavoritesViewModel
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Team section with logo, name, and favorite button.
 */
@Composable
private fun TeamSection(
    teamName: String,
    score: Int?,
    isHomeTeam: Boolean,
    isFavorite: Boolean = false,
    onFavoriteToggle: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    teamId: Int? = null
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (isHomeTeam) Alignment.Start else Alignment.End,
        verticalArrangement = Arrangement.Center
    ) {
        // Team logo using ApiSportsImage
        ApiSportsImage(
            teamId = teamId,
            teamName = teamName,
            size = 32.dp,
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Team name and favorite button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isHomeTeam) Arrangement.Start else Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isHomeTeam && onFavoriteToggle != null) {
                FavoriteButton(
                    isFavorite = isFavorite,
                    onToggle = onFavoriteToggle,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            
            Text(
                text = teamName,
                style = MaterialTheme.typography.bodySmall,
                color = TextHigh,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = if (isHomeTeam) TextAlign.Start else TextAlign.End,
                modifier = Modifier.weight(1f)
            )
            
            if (!isHomeTeam && onFavoriteToggle != null) {
                Spacer(modifier = Modifier.width(4.dp))
                FavoriteButton(
                    isFavorite = isFavorite,
                    onToggle = onFavoriteToggle,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Score (if available)
        if (score != null) {
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextHigh,
                textAlign = if (isHomeTeam) TextAlign.Start else TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Center section with match date, time, league, and status.
 */
@Composable
private fun MatchCenterSection(
    match: MatchFixture,
    showLeague: Boolean,
    showTime: Boolean,
    isFavorite: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Match date and time
        if (showTime) {
            val timeText = buildTimeDisplayText(match)
            
            Text(
                text = timeText,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = TextMedium,
                textAlign = TextAlign.Center
            )
        }

        // Status badge
        StatusBadge(
            status = match.status,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        // League name (optional)
        if (showLeague && match.league.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = match.league,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                
                // Favorite indicator
                if (isFavorite) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Favoriete competitie",
                        tint = Color(0xFF00FF00),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        // Score display for live/finished matches
        if (match.homeScore != null && match.awayScore != null) {
            Text(
                text = "${match.homeScore} - ${match.awayScore}",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextHigh,
                modifier = Modifier.padding(top = 2.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Builds the display text for match time based on status and available data.
 */
private fun buildTimeDisplayText(match: MatchFixture): String {
    return when {
        // Live match with elapsed time
        match.elapsed != null && match.status in setOf("1H", "2H", "HT") -> {
            "${match.elapsed}'"
        }
        // Match has date information
        match.date.isNotEmpty() -> {
            "${match.date} ${match.time}"
        }
        // Just time
        else -> {
            match.time
        }
    }
}

/**
 * Favorite button for team cards.
 */
@Composable
private fun FavoriteButton(
    isFavorite: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onToggle,
        modifier = modifier
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = if (isFavorite) "Verwijder uit favorieten" else "Voeg toe aan favorieten",
            tint = if (isFavorite) Color(0xFF00FF00) else TextMedium,
            modifier = Modifier.size(16.dp)
        )
    }
}

/**
 * Preview for a live match card.
 */
@Preview(showBackground = true, backgroundColor = 0xFF0A0E17)
@Composable
fun StandardMatchCardLivePreview() {
    MaterialTheme {
        StandardMatchCard(
            match = MatchFixture(
                homeTeam = "Ajax",
                awayTeam = "Feyenoord",
                time = "14:30",
                league = "Eredivisie",
                status = "1H",
                elapsed = 35,
                homeScore = 2,
                awayScore = 1,
                leagueId = 88
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Preview for an upcoming match card.
 */
@Preview(showBackground = true, backgroundColor = 0xFF0A0E17)
@Composable
fun StandardMatchCardUpcomingPreview() {
    MaterialTheme {
        StandardMatchCard(
            match = MatchFixture(
                homeTeam = "PSV",
                awayTeam = "AZ Alkmaar",
                time = "20:00",
                league = "Eredivisie",
                status = "NS",
                leagueId = 88
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Preview for a finished match card.
 */
@Preview(showBackground = true, backgroundColor = 0xFF0A0E17)
@Composable
fun StandardMatchCardFinishedPreview() {
    MaterialTheme {
        StandardMatchCard(
            match = MatchFixture(
                homeTeam = "FC Utrecht",
                awayTeam = "FC Twente",
                time = "16:45",
                league = "Eredivisie",
                status = "FT",
                homeScore = 3,
                awayScore = 2,
                leagueId = 88
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Preview for a postponed match card.
 */
@Preview(showBackground = true, backgroundColor = 0xFF0A0E17)
@Composable
fun StandardMatchCardPostponedPreview() {
    MaterialTheme {
        StandardMatchCard(
            match = MatchFixture(
                homeTeam = "NEC Nijmegen",
                awayTeam = "Fortuna Sittard",
                time = "18:30",
                league = "Eredivisie",
                status = "PST",
                leagueId = 88
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Preview for multiple match cards in a column.
 */
@Preview(showBackground = true, backgroundColor = 0xFF0A0E17, widthDp = 400)
@Composable
fun StandardMatchCardColumnPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StandardMatchCard(
                match = MatchFixture(
                    homeTeam = "Ajax",
                    awayTeam = "Feyenoord",
                    time = "14:30",
                    league = "Eredivisie",
                    status = "1H",
                    elapsed = 35,
                    homeScore = 2,
                    awayScore = 1,
                    leagueId = 88
                )
            )
            
            StandardMatchCard(
                match = MatchFixture(
                    homeTeam = "PSV",
                    awayTeam = "AZ Alkmaar",
                    time = "20:00",
                    league = "Eredivisie",
                    status = "NS",
                    leagueId = 88
                )
            )
            
            StandardMatchCard(
                match = MatchFixture(
                    homeTeam = "FC Utrecht",
                    awayTeam = "FC Twente",
                    time = "16:45",
                    league = "Eredivisie",
                    status = "FT",
                    homeScore = 3,
                    awayScore = 2,
                    leagueId = 88
                )
            )
        }
    }
}
