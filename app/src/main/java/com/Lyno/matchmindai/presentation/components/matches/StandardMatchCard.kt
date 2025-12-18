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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.Lyno.matchmindai.domain.model.MatchFixture
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
    onClick: (() -> Unit)? = null
) {
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
            // Left side: Home team
            TeamSection(
                teamName = match.homeTeam,
                score = match.homeScore,
                isHomeTeam = true,
                modifier = Modifier.weight(1f)
            )

            // Center: Match info and status
            MatchCenterSection(
                match = match,
                showLeague = showLeague,
                showTime = showTime,
                modifier = Modifier.weight(1f)
            )

            // Right side: Away team
            TeamSection(
                teamName = match.awayTeam,
                score = match.awayScore,
                isHomeTeam = false,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Team section with logo and name.
 */
@Composable
private fun TeamSection(
    teamName: String,
    score: Int?,
    isHomeTeam: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (isHomeTeam) Alignment.Start else Alignment.End,
        verticalArrangement = Arrangement.Center
    ) {
        // Team logo placeholder (would be replaced with actual logo URL)
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(32.dp)
        ) {
            // In a real implementation, this would be AsyncImage with team logo URL
            Text(
                text = teamName.take(2).uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = TextMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Team name
        Text(
            text = teamName,
            style = MaterialTheme.typography.bodySmall,
            color = TextHigh,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = if (isHomeTeam) TextAlign.Start else TextAlign.End,
            modifier = Modifier.fillMaxWidth()
        )

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
            Text(
                text = match.league,
                style = MaterialTheme.typography.labelSmall,
                color = TextMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
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
