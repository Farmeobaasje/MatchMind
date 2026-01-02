package com.Lyno.matchmindai.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.Lyno.matchmindai.domain.model.MatchFixture
import com.Lyno.matchmindai.ui.theme.MatchMindAITheme
import com.Lyno.matchmindai.ui.theme.Primary
import com.Lyno.matchmindai.ui.theme.TextHigh
import com.Lyno.matchmindai.ui.theme.TextMedium

@Composable
fun UpcomingMatchesSection(
    fixtures: List<MatchFixture>,
    isLoading: Boolean,
    onFixtureSelected: (MatchFixture) -> Unit,
    onLoadFixtures: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Header with button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📅",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Komende wedstrijden",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextHigh
                )
            }
            
            IconButton(
                onClick = {
                    isExpanded = !isExpanded
                    if (isExpanded && fixtures.isEmpty() && !isLoading) {
                        onLoadFixtures()
                    }
                }
            ) {
                Text(
                    text = if (isExpanded) "▲" else "▼",
                    color = Primary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        
        // Expandable content
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(
                animationSpec = tween(durationMillis = 300)
            ),
            exit = shrinkVertically(
                animationSpec = tween(durationMillis = 300)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                if (isLoading) {
                    // Loading state
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Laden...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMedium
                        )
                    }
                } else if (fixtures.isEmpty()) {
                    // Empty state
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Geen komende wedstrijden gevonden",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMedium
                        )
                    }
                } else {
                    // Matches carousel - using Row instead of LazyRow to avoid nested scrolling
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        for (fixture in fixtures) {
                            CompactMatchFixtureCard(
                                fixture = fixture,
                                onClick = { onFixtureSelected(fixture) },
                                modifier = Modifier.width(160.dp)
                            )
                        }
                    }
                    
                    // Info text
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Klik op een wedstrijd om te analyseren",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UpcomingMatchesSectionPreview() {
    MatchMindAITheme {
        UpcomingMatchesSection(
            fixtures = listOf(
                MatchFixture(
                    homeTeam = "Real Madrid",
                    awayTeam = "Barcelona",
                    date = "Zo 14 dec",
                    time = "20:00",
                    league = "La Liga"
                ),
                MatchFixture(
                    homeTeam = "Man City",
                    awayTeam = "Arsenal",
                    date = "Ma 15 dec",
                    time = "21:00",
                    league = "Premier League"
                ),
                MatchFixture(
                    homeTeam = "Ajax",
                    awayTeam = "Feyenoord",
                    date = "Di 16 dec",
                    time = "14:30",
                    league = "Eredivisie"
                ),
                MatchFixture(
                    homeTeam = "Bayern Munich",
                    awayTeam = "Borussia Dortmund",
                    date = "Wo 17 dec",
                    time = "18:30",
                    league = "Bundesliga"
                )
            ),
            isLoading = false,
            onFixtureSelected = {},
            onLoadFixtures = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UpcomingMatchesSectionEmptyPreview() {
    MatchMindAITheme {
        UpcomingMatchesSection(
            fixtures = emptyList(),
            isLoading = false,
            onFixtureSelected = {},
            onLoadFixtures = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UpcomingMatchesSectionLoadingPreview() {
    MatchMindAITheme {
        UpcomingMatchesSection(
            fixtures = emptyList(),
            isLoading = true,
            onFixtureSelected = {},
            onLoadFixtures = {}
        )
    }
}
