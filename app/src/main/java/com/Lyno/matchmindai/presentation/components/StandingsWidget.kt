package com.Lyno.matchmindai.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.Lyno.matchmindai.domain.model.StandingRow
import com.Lyno.matchmindai.ui.theme.MatchMindAITheme
import com.Lyno.matchmindai.ui.theme.Primary
import com.Lyno.matchmindai.ui.theme.Surface
import com.Lyno.matchmindai.ui.theme.TextHigh
import com.Lyno.matchmindai.ui.theme.TextMedium

/**
 * Widget that displays league standings in a non-scrollable format.
 * Designed to be embedded in chat messages without causing nested scrolling issues.
 */
@Composable
fun StandingsWidget(
    summaryText: String,
    standings: List<StandingRow>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // AI summary text
        Text(
            text = summaryText,
            style = MaterialTheme.typography.bodyMedium,
            color = TextHigh,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Standings table header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("#", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text("Team", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).padding(horizontal = 8.dp))
            Text("P", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text("W", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text("G", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text("V", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text("+/-", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }

        // Standings rows - using Column instead of LazyColumn to avoid nested scrolling
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            standings.take(10).forEach { standing ->
                StandingItem(
                    standing = standing,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (standings.size > 10) {
            Text(
                text = "... en ${standings.size - 10} andere teams",
                style = MaterialTheme.typography.bodySmall,
                color = TextMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            )
        }
    }
}

/**
 * Individual standing row item.
 */
@Composable
private fun StandingItem(
    standing: StandingRow,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (standing.rank <= 3) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                maxLines = 1,
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
                    Primary 
                else 
                    MaterialTheme.colorScheme.error,
                modifier = Modifier.width(32.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StandingsWidgetPreview() {
    MatchMindAITheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            StandingsWidget(
                summaryText = "Hier is de huidige ranglijst van de Eredivisie:",
                standings = listOf(
                    StandingRow(
                        rank = 1,
                        team = "PSV",
                        points = 45,
                        played = 17,
                        wins = 14,
                        draws = 3,
                        losses = 0,
                        goalsFor = 48,
                        goalsAgainst = 6,
                        goalDiff = 42
                    ),
                    StandingRow(
                        rank = 2,
                        team = "Feyenoord",
                        points = 38,
                        played = 17,
                        wins = 12,
                        draws = 2,
                        losses = 3,
                        goalsFor = 42,
                        goalsAgainst = 16,
                        goalDiff = 26
                    ),
                    StandingRow(
                        rank = 3,
                        team = "Ajax",
                        points = 32,
                        played = 17,
                        wins = 9,
                        draws = 5,
                        losses = 3,
                        goalsFor = 40,
                        goalsAgainst = 25,
                        goalDiff = 15
                    ),
                    StandingRow(
                        rank = 4,
                        team = "AZ",
                        points = 31,
                        played = 17,
                        wins = 9,
                        draws = 4,
                        losses = 4,
                        goalsFor = 35,
                        goalsAgainst = 19,
                        goalDiff = 16
                    ),
                    StandingRow(
                        rank = 5,
                        team = "FC Twente",
                        points = 30,
                        played = 17,
                        wins = 9,
                        draws = 3,
                        losses = 5,
                        goalsFor = 30,
                        goalsAgainst = 21,
                        goalDiff = 9
                    )
                )
            )
        }
    }
}
