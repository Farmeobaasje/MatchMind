package com.Lyno.matchmindai.presentation.components.matches

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Lyno.matchmindai.domain.model.MatchFixture
import com.Lyno.matchmindai.presentation.components.ApiSportsImage
import com.Lyno.matchmindai.ui.theme.ConfidenceHigh
import com.Lyno.matchmindai.ui.theme.GradientEnd
import com.Lyno.matchmindai.ui.theme.GradientStart
import com.Lyno.matchmindai.ui.theme.SurfaceCard
import com.Lyno.matchmindai.ui.theme.TextHigh
import com.Lyno.matchmindai.ui.theme.TextMedium

/**
 * A large, prominent card for displaying the #1 curated match (hero match).
 * Features large team logos, big typography for scores, and optional timeline.
 */
@Composable
fun HeroMatchCard(
    match: MatchFixture,
    modifier: Modifier = Modifier,
    recentEvents: List<String> = emptyList(),
    hasEventCoverage: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceCard,
            contentColor = TextHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        onClick = onClick ?: {}
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // League header
            if (match.league.isNotEmpty()) {
                Text(
                    text = match.league.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextMedium,
                    letterSpacing = 1.sp
                )
            }

            // Teams and score section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home team
                HeroTeamSection(
                    teamName = match.homeTeam,
                    score = match.homeScore,
                    isHomeTeam = true,
                    teamId = match.homeTeamId,
                    modifier = Modifier.weight(1f)
                )

                // Score and time center
                HeroCenterSection(match = match)

                // Away team
                HeroTeamSection(
                    teamName = match.awayTeam,
                    score = match.awayScore,
                    isHomeTeam = false,
                    teamId = match.awayTeamId,
                    modifier = Modifier.weight(1f)
                )
            }

            // Status and time row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(
                    status = match.status,
                    modifier = Modifier.padding(end = 12.dp)
                )

                Text(
                    text = buildHeroTimeDisplayText(match),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = TextMedium
                )
            }

            // Recent events timeline (if available)
            if (hasEventCoverage && recentEvents.isNotEmpty()) {
                RecentEventsTimeline(events = recentEvents)
            }
        }
    }
}

/**
 * Team section for hero card with large logo and name.
 */
@Composable
private fun HeroTeamSection(
    teamName: String,
    score: Int?,
    isHomeTeam: Boolean,
    modifier: Modifier = Modifier,
    teamId: Int? = null
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (isHomeTeam) Alignment.Start else Alignment.End,
        verticalArrangement = Arrangement.Center
    ) {
        // Large team logo using ApiSportsImage
        ApiSportsImage(
            teamId = teamId,
            teamName = teamName,
            size = 64.dp,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Team name
        Text(
            text = teamName,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = TextHigh,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = if (isHomeTeam) TextAlign.Start else TextAlign.End,
            modifier = Modifier.fillMaxWidth()
        )

        // Score (big typography)
        if (score != null) {
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                color = TextHigh,
                textAlign = if (isHomeTeam) TextAlign.Start else TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Center section for hero card with score separator and match info.
 */
@Composable
private fun HeroCenterSection(match: MatchFixture) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        // Score separator (VS or actual score)
        if (match.homeScore != null && match.awayScore != null) {
            Text(
                text = "${match.homeScore} - ${match.awayScore}",
                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                color = ConfidenceHigh
            )
        } else {
            Text(
                text = "VS",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = TextMedium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Venue or additional info (if available)
        if (match.leagueCountry?.isNotEmpty() == true) {
            Text(
                text = match.leagueCountry,
                style = MaterialTheme.typography.labelSmall,
                color = TextMedium
            )
        }
    }
}

/**
 * Recent events timeline for live matches.
 */
@Composable
private fun RecentEventsTimeline(events: List<String>) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(MaterialTheme.shapes.small),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "RECENTE EVENTS",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = TextMedium,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(events) { event ->
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = ConfidenceHigh.copy(alpha = 0.2f),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = event,
                            style = MaterialTheme.typography.labelSmall,
                            color = ConfidenceHigh,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Preview for a live hero match with events.
 */
@Preview(showBackground = true, backgroundColor = 0xFF0A0E17)
@Composable
fun HeroMatchCardLivePreview() {
    MaterialTheme {
        HeroMatchCard(
            match = MatchFixture(
                homeTeam = "Ajax Amsterdam",
                awayTeam = "Feyenoord Rotterdam",
                time = "14:30",
                league = "Eredivisie",
                status = "1H",
                elapsed = 65,
                homeScore = 3,
                awayScore = 2,
                leagueId = 88,
                leagueCountry = "Netherlands"
            ),
            recentEvents = listOf("⚽ 23'", "🟨 45'", "⚽ 52'", "⚽ 61'"),
            hasEventCoverage = true,
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Preview for an upcoming hero match.
 */
@Preview(showBackground = true, backgroundColor = 0xFF0A0E17)
@Composable
fun HeroMatchCardUpcomingPreview() {
    MaterialTheme {
        HeroMatchCard(
            match = MatchFixture(
                homeTeam = "Real Madrid",
                awayTeam = "FC Barcelona",
                time = "21:00",
                league = "La Liga",
                status = "NS",
                leagueId = 140,
                leagueCountry = "Spain"
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Preview for a finished hero match.
 */
@Preview(showBackground = true, backgroundColor = 0xFF0A0E17)
@Composable
fun HeroMatchCardFinishedPreview() {
    MaterialTheme {
        HeroMatchCard(
            match = MatchFixture(
                homeTeam = "Manchester City",
                awayTeam = "Liverpool FC",
                time = "16:30",
                league = "Premier League",
                status = "FT",
                homeScore = 2,
                awayScore = 2,
                leagueId = 39,
                leagueCountry = "England"
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Preview for a hero match without event coverage.
 */
@Preview(showBackground = true, backgroundColor = 0xFF0A0E17)
@Composable
fun HeroMatchCardNoEventsPreview() {
    MaterialTheme {
        HeroMatchCard(
            match = MatchFixture(
                homeTeam = "Bayern München",
                awayTeam = "Borussia Dortmund",
                time = "18:30",
                league = "Bundesliga",
                status = "NS",
                leagueId = 78,
                leagueCountry = "Germany"
            ),
            hasEventCoverage = false,
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Builds the display text for hero match time based on status and available data.
 */
private fun buildHeroTimeDisplayText(match: MatchFixture): String {
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
