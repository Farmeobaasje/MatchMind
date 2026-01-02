package com.Lyno.matchmindai.presentation.components.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.Lyno.matchmindai.R
import com.Lyno.matchmindai.domain.model.FavoriteTeamData
import com.Lyno.matchmindai.domain.model.MatchFixture
import com.Lyno.matchmindai.domain.model.NewsItemData
import com.Lyno.matchmindai.domain.model.StandingRow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Composable for displaying a favorite team item in the Favorites screen.
 * Shows team logo, name, next match, news, and standings.
 */
@Composable
fun FavoriteTeamItem(
    teamData: FavoriteTeamData,
    modifier: Modifier = Modifier,
    onNewsItemClick: (NewsItemData) -> Unit = {},
    onNextMatchClick: (MatchFixture) -> Unit = {},
    onStandingsClick: (List<StandingRow>) -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Team header with logo and name
            TeamHeader(teamData = teamData)

            // Next match section
            if (teamData.nextMatch != null) {
                SimpleNextMatchCard(
                    match = teamData.nextMatch,
                    onMatchClick = { onNextMatchClick(teamData.nextMatch) }
                )
            }

            // News section
            if (!teamData.news.isNullOrEmpty()) {
                SimpleTeamNewsFeed(
                    newsItems = teamData.news,
                    onNewsItemClick = onNewsItemClick
                )
            }

            // Standings section
            if (teamData.hasValidStandings() && !teamData.standings.isNullOrEmpty()) {
                SimpleLeagueStandingsCard(
                    standings = teamData.standings,
                    teamId = teamData.teamId,
                    onStandingsClick = { onStandingsClick(teamData.standings!!) }
                )
            }

            // Empty state if no data
            if (teamData.nextMatch == null && teamData.news.isNullOrEmpty() && !teamData.hasValidStandings()) {
                Text(
                    text = stringResource(R.string.no_team_data_available),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Team header with logo and name.
 */
@Composable
private fun TeamHeader(
    teamData: FavoriteTeamData,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // Team logo
        if (!teamData.teamLogoUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(teamData.teamLogoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.team_logo_description, teamData.getDisplayName()),
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                error = null,
                placeholder = null
            )
            Spacer(modifier = Modifier.width(12.dp))
        } else {
            // Show team initials if no logo URL
            TeamInitialsFallback(teamName = teamData.getDisplayName())
            Spacer(modifier = Modifier.width(12.dp))
        }

        // Team name and info
        Column(
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = teamData.getDisplayName(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // League info if available
            if (teamData.leagueName?.isNotBlank() == true) {
                Text(
                    text = teamData.leagueName ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Fallback component showing team initials when logo is not available.
 */
@Composable
private fun TeamInitialsFallback(
    teamName: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.size(48.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = teamName.take(2).uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SimpleNextMatchCard(
    match: MatchFixture,
    onMatchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onMatchClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Volgende wedstrijd",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "VOLGENDE WEDSTRIJD",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
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
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (match.league.isNotEmpty()) {
                        Text(
                            text = match.league,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Text(
                    text = "VS",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = match.awayTeam,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (match.leagueCountry != null) {
                        Text(
                            text = match.leagueCountry,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatSimpleMatchDate(match),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatSimpleMatchTime(match),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SimpleTeamNewsFeed(
    newsItems: List<NewsItemData>,
    onNewsItemClick: (NewsItemData) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Newspaper,
                    contentDescription = "Nieuws",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LAATSTE NIEUWS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            newsItems.take(3).forEach { newsItem ->
                SimpleNewsItem(
                    newsItem = newsItem,
                    onClick = { onNewsItemClick(newsItem) }
                )
            }
        }
    }
}

@Composable
private fun SimpleNewsItem(
    newsItem: NewsItemData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = newsItem.headline,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            if (!newsItem.snippet.isNullOrBlank()) {
                Text(
                    text = newsItem.snippet,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (!newsItem.source.isNullOrBlank()) {
                    Text(
                        text = newsItem.source,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                if (!newsItem.publishedDate.isNullOrBlank()) {
                    Text(
                        text = newsItem.publishedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SimpleLeagueStandingsCard(
    standings: List<StandingRow>,
    teamId: Int,
    onStandingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onStandingsClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = "Stand",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "COMPETITIE STAND",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Find the team's standing
            val teamStanding = standings.firstOrNull { 
                // Try to match by team name or other identifier
                it.team.contains(teamId.toString()) || it.team.contains("Ajax") // Simplified for now
            }
            
            if (teamStanding != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Positie",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "#${teamStanding.rank}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Punten",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = teamStanding.points.toString(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Doelsaldo",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (teamStanding.goalDiff > 0) "+${teamStanding.goalDiff}" else teamStanding.goalDiff.toString(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (teamStanding.goalDiff > 0) Color.Green else if (teamStanding.goalDiff < 0) Color.Red else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Gespeeld: ${teamStanding.played}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "W/D/V: ${teamStanding.wins}/${teamStanding.draws}/${teamStanding.losses}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = "Geen stand beschikbaar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun formatSimpleMatchDate(match: MatchFixture): String {
    return try {
        if (match.date.isNotEmpty()) {
            if (match.date.contains(" ")) {
                match.date.split(" ")[0]
            } else {
                match.date
            }
        } else {
            "Binnenkort"
        }
    } catch (e: Exception) {
        "Binnenkort"
    }
}

private fun formatSimpleMatchTime(match: MatchFixture): String {
    return try {
        if (match.time.isNotEmpty()) {
            if (match.time.contains(":")) {
                match.time
            } else {
                "TBA"
            }
        } else {
            "TBA"
        }
    } catch (e: Exception) {
        "TBA"
    }
}

private fun formatSimpleNewsDate(dateString: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = parser.parse(dateString)
        val now = Date()
        
        val daysBetween = ((now.time - date.time) / (1000 * 60 * 60 * 24)).toInt()
        when {
            daysBetween == 0 -> "Vandaag"
            daysBetween == 1 -> "Gisteren"
            daysBetween < 7 -> "$daysBetween dagen geleden"
            else -> "${daysBetween / 7} weken geleden"
        }
    } catch (e: Exception) {
        "Recent"
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun FavoriteTeamItemPreview() {
    MaterialTheme {
        FavoriteTeamItem(
            teamData = FavoriteTeamData(
                teamId = 209,
                teamName = "Feyenoord",
                teamLogoUrl = "https://media.api-sports.io/football/teams/209.png",
                leagueName = "Eredivisie",
                nextMatch = MatchFixture(
                    fixtureId = 12345,
                    homeTeam = "Feyenoord",
                    awayTeam = "Ajax",
                    league = "Eredivisie",
                    leagueCountry = "Netherlands",
                    date = "2024-01-15",
                    time = "20:00"
                ),
                news = listOf(
                    NewsItemData(
                        headline = "Feyenoord wint belangrijke wedstrijd",
                        source = "VI.nl",
                        url = "https://example.com",
                        snippet = "Feyenoord heeft een belangrijke overwinning geboekt in de strijd om de titel.",
                        publishedDate = "Vandaag"
                    )
                ),
                standings = listOf(
                    StandingRow(
                        rank = 1,
                        team = "Feyenoord",
                        points = 45,
                        played = 18,
                        wins = 14,
                        draws = 3,
                        losses = 1,
                        goalsFor = 42,
                        goalsAgainst = 12,
                        goalDiff = 30
                    )
                )
            )
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun FavoriteTeamItemEmptyPreview() {
    MaterialTheme {
        FavoriteTeamItem(
            teamData = FavoriteTeamData(
                teamId = 209,
                teamName = "Feyenoord",
                teamLogoUrl = null,
                leagueName = "Eredivisie"
            )
        )
    }
}
