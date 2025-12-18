package com.Lyno.matchmindai.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.Lyno.matchmindai.domain.model.LeagueGroup
import com.Lyno.matchmindai.domain.model.MatchFixture

/**
 * A collapsible section for displaying matches grouped by league.
 * Similar to FlashScore's league grouping with expand/collapse functionality.
 *
 * @param leagueGroup The league group containing matches
 * @param onMatchClick Callback when a match is clicked
 * @param onToggleExpanded Callback when the header is clicked to toggle expansion
 * @param modifier Modifier for the component
 */
@Composable
fun ExpandableLeagueSection(
    leagueGroup: LeagueGroup,
    onMatchClick: (MatchFixture) -> Unit,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
    ) {
        // Header with league info and chevron
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpanded() }
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // League logo
                    if (leagueGroup.logoUrl.isNotEmpty()) {
                        val painter = rememberAsyncImagePainter(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(leagueGroup.logoUrl)
                                .crossfade(true)
                                .build()
                        )
                        
                        when (painter.state) {
                            is AsyncImagePainter.State.Loading -> {
                                // Show a placeholder with league initials while loading
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(MaterialTheme.shapes.small)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = leagueGroup.leagueName.take(2).uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            is AsyncImagePainter.State.Error -> {
                                // Log error for debugging
                                android.util.Log.d(
                                    "ExpandableLeagueSection",
                                    "Failed to load league logo: ${(painter.state as AsyncImagePainter.State.Error).result.throwable.message}"
                                )
                                // Show fallback with league initials when image fails to load
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(MaterialTheme.shapes.small)
                                        .background(MaterialTheme.colorScheme.errorContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = leagueGroup.leagueName.take(2).uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                            else -> {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(leagueGroup.logoUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = stringResource(
                                        com.Lyno.matchmindai.R.string.league_logo_content_description,
                                        leagueGroup.leagueName
                                    ),
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(MaterialTheme.shapes.small)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    // League name and country
                    Column {
                        Text(
                            text = leagueGroup.leagueName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = leagueGroup.country,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Chevron icon for expand/collapse
                Icon(
                    imageVector = if (leagueGroup.isExpanded) {
                        Icons.Default.ExpandLess
                    } else {
                        Icons.Default.ExpandMore
                    },
                    contentDescription = if (leagueGroup.isExpanded) {
                        stringResource(com.Lyno.matchmindai.R.string.collapse_section)
                    } else {
                        stringResource(com.Lyno.matchmindai.R.string.expand_section)
                    },
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Animated visibility for matches
        AnimatedVisibility(
            visible = leagueGroup.isExpanded,
            enter = expandVertically(
                animationSpec = tween(durationMillis = 300),
                expandFrom = Alignment.Top
            ),
            exit = shrinkVertically(
                animationSpec = tween(durationMillis = 300),
                shrinkTowards = Alignment.Top
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                leagueGroup.matches.forEach { match ->
                    MatchFixtureCard(
                        fixture = match,
                        onClick = { onMatchClick(match) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * Preview function for ExpandableLeagueSection
 */
@Composable
fun ExpandableLeagueSectionPreview() {
    // Preview data
    val previewLeagueGroup = LeagueGroup(
        leagueId = 88,
        leagueName = "Eredivisie",
        country = "Netherlands",
        logoUrl = "https://media.api-sports.io/football/leagues/88.png",
        matches = listOf(
            MatchFixture(
                homeTeam = "Ajax",
                awayTeam = "Feyenoord",
                time = "14:30",
                league = "Eredivisie",
                date = "Zo 14 dec",
                status = "NS",
                fixtureId = 12345,
                leagueId = 88,
                leagueCountry = "Netherlands",
                leagueLogo = "https://media.api-sports.io/football/leagues/88.png"
            ),
            MatchFixture(
                homeTeam = "PSV",
                awayTeam = "AZ",
                time = "16:45",
                league = "Eredivisie",
                date = "Zo 14 dec",
                status = "NS",
                fixtureId = 12346,
                leagueId = 88,
                leagueCountry = "Netherlands",
                leagueLogo = "https://media.api-sports.io/football/leagues/88.png"
            )
        ),
        isExpanded = true
    )

    ExpandableLeagueSection(
        leagueGroup = previewLeagueGroup,
        onMatchClick = {},
        onToggleExpanded = {}
    )
}
