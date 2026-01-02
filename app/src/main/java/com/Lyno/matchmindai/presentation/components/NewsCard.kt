package com.Lyno.matchmindai.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.Lyno.matchmindai.domain.model.NewsItemData
import com.Lyno.matchmindai.ui.theme.MatchMindAITheme
import com.Lyno.matchmindai.ui.theme.Primary
import com.Lyno.matchmindai.ui.theme.Surface
import com.Lyno.matchmindai.ui.theme.TextHigh
import com.Lyno.matchmindai.ui.theme.TextMedium

/**
 * Card component for displaying a single news item.
 * Shows headline, source, snippet, and opens URL when clicked.
 */
@Composable
fun NewsCard(
    newsItem: NewsItemData,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                try {
                    uriHandler.openUri(newsItem.url)
                } catch (e: Exception) {
                    // URL opening failed, do nothing
                }
            },
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
            // Source and date row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = newsItem.source,
                    style = MaterialTheme.typography.labelSmall,
                    color = Primary,
                    fontWeight = FontWeight.Medium
                )

                newsItem.publishedDate?.let { date ->
                    Text(
                        text = date,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Headline
            Text(
                text = newsItem.headline,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextHigh
            )

            // Snippet (if available)
            newsItem.snippet?.let { snippet ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = snippet,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMedium,
                    maxLines = 3
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // URL domain
            Text(
                text = newsItem.domain,
                style = MaterialTheme.typography.labelSmall,
                color = TextMedium
            )
        }
    }
}

/**
 * Widget that combines AI summary text with news items list.
 * Used when AI retrieves news/search results and wants to show them in a widget.
 */
@Composable
fun NewsWidget(
    summaryText: String,
    newsItems: List<NewsItemData>,
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

        if (newsItems.isEmpty()) {
            // Empty state
            Text(
                text = "Geen nieuws gevonden",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                newsItems.forEach { newsItem ->
                    NewsCard(newsItem = newsItem)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun NewsCardPreview() {
    MatchMindAITheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            NewsCard(
                newsItem = NewsItemData(
                    headline = "Ajax verliest weer, trainer op de tocht",
                    source = "VI.nl",
                    url = "https://www.vi.nl/artikel/12345",
                    snippet = "Ajax heeft opnieuw een nederlaag geleden in de Eredivisie. De positie van trainer John van 't Schip staat onder druk na de 3-1 nederlaag tegen Feyenoord.",
                    publishedDate = "2 uur geleden"
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NewsWidgetPreview() {
    MatchMindAITheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            NewsWidget(
                summaryText = "Hier is het laatste nieuws over Ajax en Feyenoord:",
                newsItems = listOf(
                    NewsItemData(
                        headline = "Ajax verliest weer, trainer op de tocht",
                        source = "VI.nl",
                        url = "https://www.vi.nl/artikel/12345",
                        snippet = "Ajax heeft opnieuw een nederlaag geleden in de Eredivisie.",
                        publishedDate = "2 uur geleden"
                    ),
                    NewsItemData(
                        headline = "Feyenoord wint klassieker met 3-1",
                        source = "ESPN",
                        url = "https://www.espn.nl/artikel/67890",
                        snippet = "Feyenoord heeft de klassieker tegen Ajax gewonnen met 3-1.",
                        publishedDate = "Vandaag"
                    )
                )
            )
        }
    }
}
