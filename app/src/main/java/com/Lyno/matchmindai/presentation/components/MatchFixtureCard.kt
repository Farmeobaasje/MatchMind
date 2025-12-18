    package com.Lyno.matchmindai.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.Lyno.matchmindai.domain.model.MatchFixture
import com.Lyno.matchmindai.ui.theme.MatchMindAITheme
import com.Lyno.matchmindai.ui.theme.Primary
import com.Lyno.matchmindai.ui.theme.Surface
import com.Lyno.matchmindai.ui.theme.TextHigh
import com.Lyno.matchmindai.ui.theme.TextMedium

@Composable
fun MatchFixtureCard(
    fixture: MatchFixture,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Surface,
            contentColor = TextHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Time (small top)
            Text(
                text = fixture.time,
                style = MaterialTheme.typography.labelSmall,
                color = Primary,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Teams (bold, main content)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = fixture.homeTeam,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                
                Text(
                    text = "vs",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMedium,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                
                Text(
                    text = fixture.awayTeam,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // League (small bottom)
            Text(
                text = fixture.league,
                style = MaterialTheme.typography.labelSmall,
                color = TextMedium
            )
        }
    }
}

@Composable
fun CompactMatchFixtureCard(
    fixture: MatchFixture,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Surface,
            contentColor = TextHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Time (small top)
            Text(
                text = fixture.time,
                style = MaterialTheme.typography.labelSmall,
                color = Primary,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Teams (stacked vertically for compact view)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = fixture.homeTeam,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                
                Text(
                    text = "vs",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMedium,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
                
                Text(
                    text = fixture.awayTeam,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // League (small bottom)
            Text(
                text = fixture.league,
                style = MaterialTheme.typography.labelSmall,
                color = TextMedium,
                maxLines = 1
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MatchFixtureCardPreview() {
    MatchMindAITheme {
        MatchFixtureCard(
            fixture = MatchFixture(
                homeTeam = "Real Madrid",
                awayTeam = "Barcelona",
                date = "Zo 14 dec",
                time = "20:00",
                league = "La Liga"
            ),
            onClick = {}
        )
    }
}

/**
 * Live match card component that displays live matches with scores and goal information.
 * Shows goals with scorer, minute, and team information in a compact format.
 * Uses Column instead of LazyColumn to avoid nested scrolling in chat.
 */
@Composable
fun LiveMatchCard(
    matches: List<MatchFixture>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        matches.forEach { match ->
            LiveMatchItem(match = match)
        }
    }
}

@Composable
private fun LiveMatchItem(
    match: MatchFixture,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Surface,
            contentColor = TextHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Live indicator and minute
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = Color.Red,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "LIVE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    text = match.time,
                    style = MaterialTheme.typography.labelSmall,
                    color = Primary,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Teams and scores
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = match.homeTeam,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
                
                // Score
                Text(
                    text = "${match.homeScore} - ${match.awayScore}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = match.awayTeam,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // League
            Text(
                text = match.league,
                style = MaterialTheme.typography.labelSmall,
                color = TextMedium
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun CompactMatchFixtureCardPreview() {
    MatchMindAITheme {
        CompactMatchFixtureCard(
            fixture = MatchFixture(
                homeTeam = "Man City",
                awayTeam = "Arsenal",
                date = "Ma 15 dec",
                time = "21:00",
                league = "Premier League"
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LiveMatchCardPreview() {
    MatchMindAITheme {
        LiveMatchCard(
            matches = listOf(
                MatchFixture(
                    homeTeam = "Ajax",
                    awayTeam = "Feyenoord",
                    homeScore = 2,
                    awayScore = 1,
                    time = "75'",
                    league = "Eredivisie",
                    status = "LIVE",
                    elapsed = 75
                )
            )
        )
    }
}
