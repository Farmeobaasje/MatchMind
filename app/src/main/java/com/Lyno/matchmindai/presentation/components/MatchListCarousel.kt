package com.Lyno.matchmindai.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.Lyno.matchmindai.domain.model.MatchFixture
import com.Lyno.matchmindai.ui.theme.MatchMindAITheme
import com.Lyno.matchmindai.ui.theme.Primary
import com.Lyno.matchmindai.ui.theme.TextHigh
import com.Lyno.matchmindai.ui.theme.TextMedium

/**
 * A horizontal carousel component for displaying match fixtures.
 * Used in chat widgets when AI retrieves fixtures data.
 */
@Composable
fun MatchListCarousel(
    fixtures: List<MatchFixture>,
    onFixtureClick: (MatchFixture) -> Unit,
    title: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Optional title
        title?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = TextHigh,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (fixtures.isEmpty()) {
            // Empty state
            Text(
                text = "Geen wedstrijden gevonden",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(fixtures) { fixture ->
                    CompactMatchFixtureCard(
                        fixture = fixture,
                        onClick = { onFixtureClick(fixture) }
                    )
                }
            }
        }
    }
}

/**
 * Widget that combines AI summary text with match fixtures carousel.
 * Used when AI retrieves fixtures data and wants to show it in a widget.
 */
@Composable
fun FixturesWidget(
    summaryText: String,
    fixtures: List<MatchFixture>,
    onFixtureClick: (MatchFixture) -> Unit,
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

        // Match fixtures carousel
        MatchListCarousel(
            fixtures = fixtures,
            onFixtureClick = onFixtureClick,
            title = "Wedstrijden",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun MatchListCarouselPreview() {
    MatchMindAITheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            MatchListCarousel(
                fixtures = listOf(
                    MatchFixture(
                        homeTeam = "Ajax",
                        awayTeam = "Feyenoord",
                        time = "20:00",
                        league = "Eredivisie",
                        date = "Vandaag"
                    ),
                    MatchFixture(
                        homeTeam = "PSV",
                        awayTeam = "AZ",
                        time = "21:00",
                        league = "Eredivisie",
                        date = "Vandaag"
                    ),
                    MatchFixture(
                        homeTeam = "Real Madrid",
                        awayTeam = "Barcelona",
                        time = "22:00",
                        league = "La Liga",
                        date = "Vandaag"
                    )
                ),
                onFixtureClick = {},
                title = "Wedstrijden van vandaag"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FixturesWidgetPreview() {
    MatchMindAITheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            FixturesWidget(
                summaryText = "Hier zijn de wedstrijden van vanavond in de top competities:",
                fixtures = listOf(
                    MatchFixture(
                        homeTeam = "Ajax",
                        awayTeam = "Feyenoord",
                        time = "20:00",
                        league = "Eredivisie",
                        date = "Vandaag"
                    ),
                    MatchFixture(
                        homeTeam = "PSV",
                        awayTeam = "AZ",
                        time = "21:00",
                        league = "Eredivisie",
                        date = "Vandaag"
                    )
                ),
                onFixtureClick = {}
            )
        }
    }
}
