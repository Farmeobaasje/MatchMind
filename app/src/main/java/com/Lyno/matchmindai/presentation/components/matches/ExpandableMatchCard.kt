package com.Lyno.matchmindai.presentation.components.matches

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.Lyno.matchmindai.domain.model.LiveEvent
import com.Lyno.matchmindai.domain.model.LiveStatistic
import com.Lyno.matchmindai.domain.model.MatchFixture
import com.Lyno.matchmindai.ui.theme.ConfidenceHigh
import com.Lyno.matchmindai.ui.theme.PrimaryNeon
import com.Lyno.matchmindai.ui.theme.SurfaceCard
import com.Lyno.matchmindai.ui.theme.TextHigh
import com.Lyno.matchmindai.ui.theme.TextMedium

/**
 * Expandable match card for DashX dashboard.
 * Shows basic match info when collapsed, expands to show detailed info.
 */
@Composable
fun ExpandableMatchCard(
    match: MatchFixture,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    liveEvents: List<LiveEvent> = emptyList(),
    liveStatistics: List<LiveStatistic> = emptyList(),
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        onClick = {
            onToggleExpanded()
            onClick()
        },
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = SurfaceCard.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Collapsed content (always visible)
            CollapsedMatchContent(
                match = match,
                isExpanded = isExpanded,
                hasLiveData = liveEvents.isNotEmpty() || liveStatistics.isNotEmpty()
            )

            // Expanded content (animated)
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = tween(durationMillis = 300)
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 300)
                ),
                exit = shrinkVertically(
                    animationSpec = tween(durationMillis = 300)
                ) + fadeOut(
                    animationSpec = tween(durationMillis = 300)
                )
            ) {
                ExpandedMatchContent(
                    match = match,
                    liveEvents = liveEvents,
                    liveStatistics = liveStatistics,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Collapsed match content (basic info).
 */
@Composable
private fun CollapsedMatchContent(
    match: MatchFixture,
    isExpanded: Boolean,
    hasLiveData: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Teams and score
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Home team
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = match.homeTeam,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextHigh,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                if (match.homeScore != null) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(PrimaryNeon.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = match.homeScore.toString(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = PrimaryNeon
                        )
                    }
                }
            }

            // Away team
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = match.awayTeam,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextHigh,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                if (match.awayScore != null) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(PrimaryNeon.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = match.awayScore.toString(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = PrimaryNeon
                        )
                    }
                }
            }
        }

        // Status and expand icon
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Status badge
            StatusBadgeInternal(
                status = match.status,
                showIcon = true,
                modifier = Modifier
            )

            // Expand icon
            Icon(
                imageVector = if (isExpanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                contentDescription = if (isExpanded) "Inklappen" else "Uitklappen",
                tint = TextMedium,
                modifier = Modifier.size(24.dp)
            )
        }
    }

    // League and time info
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = match.league,
            style = MaterialTheme.typography.labelSmall,
            color = TextMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = match.time,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = if (match.status in listOf("1H", "2H", "HT", "LIVE")) ConfidenceHigh else TextMedium
        )
    }

    // Live data indicator
    if (hasLiveData) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color.Red)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Live data beschikbaar",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Red
            )
        }
    }
}

/**
 * Expanded match content (detailed info).
 */
@Composable
private fun ExpandedMatchContent(
    match: MatchFixture,
    liveEvents: List<LiveEvent>,
    liveStatistics: List<LiveStatistic>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Divider
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp),
            color = TextMedium.copy(alpha = 0.2f)
        ) {}

        // Live events section
        if (liveEvents.isNotEmpty()) {
            LiveEventsSection(
                events = liveEvents,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Statistics section
        if (liveStatistics.isNotEmpty()) {
            LiveStatisticsSection(
                statistics = liveStatistics,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Match details section
        MatchDetailsSection(
            match = match,
            modifier = Modifier.fillMaxWidth()
        )

        // Quick actions
        QuickActionsSection(
            match = match,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Live events section for expanded card.
 */
@Composable
private fun LiveEventsSection(
    events: List<LiveEvent>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.SportsSoccer,
                    contentDescription = "Events",
                    tint = PrimaryNeon,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "LIVE EVENTS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextHigh
                )
            }
            
            Text(
                text = "${events.size} events",
                style = MaterialTheme.typography.labelSmall,
                color = TextMedium
            )
        }

        // Show last 5 events
        val recentEvents = events.takeLast(5)
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            recentEvents.forEach { event ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.displayTime,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextMedium,
                        modifier = Modifier.width(40.dp)
                    )
                    
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextHigh,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Text(
                        text = event.team,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(60.dp)
                    )
                }
            }
        }
    }
}

/**
 * Live statistics section for expanded card.
 */
@Composable
private fun LiveStatisticsSection(
    statistics: List<LiveStatistic>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Event,
                contentDescription = "Statistics",
                tint = PrimaryNeon,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "STATISTIEKEN",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = TextHigh
            )
        }

        // Show key statistics
        val keyStats = statistics.filter { stat ->
            stat.type in listOf("Ball Possession", "Shots on Goal", "Total Shots", "Corner Kicks", "Fouls")
        }.take(3)

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            keyStats.forEach { stat ->
                StatisticRow(
                    stat = stat,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Single statistic row.
 */
@Composable
private fun StatisticRow(
    stat: LiveStatistic,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stat.type,
                style = MaterialTheme.typography.labelSmall,
                color = TextMedium,
                modifier = Modifier.weight(1f)
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stat.homeDisplay,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextHigh,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(40.dp)
                )
                
                // Progress bar
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(TextMedium.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(stat.homePercentage / 100f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(PrimaryNeon)
                    )
                }
                
                Text(
                    text = stat.awayDisplay,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextHigh,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.width(40.dp)
                )
            }
        }
    }
}

/**
 * Match details section.
 */
@Composable
private fun MatchDetailsSection(
    match: MatchFixture,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Timer,
                contentDescription = "Details",
                tint = PrimaryNeon,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "WEDSTRIJD DETAILS",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = TextHigh
            )
        }

        // Match details grid
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                DetailItem(
                    label = "Competitie",
                    value = match.league,
                    modifier = Modifier.fillMaxWidth()
                )
                DetailItem(
                    label = "Land",
                    value = match.leagueCountry ?: "Onbekend",
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                DetailItem(
                    label = "Status",
                    value = match.status ?: "Gepland",
                    modifier = Modifier.fillMaxWidth()
                )
                DetailItem(
                    label = "Tijd",
                    value = match.time,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Detail item for match details.
 */
@Composable
private fun DetailItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = TextHigh,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Quick actions section for expanded card.
 */
@Composable
private fun QuickActionsSection(
    match: MatchFixture,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // View details button
        Box(
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(PrimaryNeon.copy(alpha = 0.1f))
                .clickable { /* Navigate to match detail */ },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Details bekijken",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = PrimaryNeon
            )
        }

        // Add to favorites button
        Box(
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(TextMedium.copy(alpha = 0.1f))
                .clickable { /* Add to favorites */ },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Favoriet",
                style = MaterialTheme.typography.labelMedium,
                color = TextHigh
            )
        }
    }
}

/**
 * Status badge for match status.
 */
@Composable
private fun StatusBadgeInternal(
    status: String?,
    showIcon: Boolean = true,
    modifier: Modifier = Modifier
) {
    val (text, color) = when (status) {
        "1H", "2H", "LIVE", "IN_PLAY" -> Pair("LIVE", Color.Red)
        "HT" -> Pair("RUST", Color.Yellow)
        "FT" -> Pair("EINDE", Color.Green)
        "POSTPONED", "CANCELED" -> Pair("AFGELAST", Color.Gray)
        "SUSPENDED" -> Pair("GESCHORST", Color(0xFFFFA500)) // Orange color
        else -> Pair("GEPLAND", TextMedium)
    }
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (showIcon && status in listOf("1H", "2H", "LIVE", "IN_PLAY")) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color.Red)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = color
            )
        }
    }
}
