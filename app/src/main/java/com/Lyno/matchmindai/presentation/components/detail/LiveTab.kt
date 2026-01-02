package com.Lyno.matchmindai.presentation.components.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.Lyno.matchmindai.common.Resource
import com.Lyno.matchmindai.domain.model.*
import com.Lyno.matchmindai.presentation.components.GlassCard
import com.Lyno.matchmindai.presentation.viewmodel.MatchDetailViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Live tab component for displaying real-time match data.
 * Only shown when match is live (status.isLive == true).
 */
@Composable
fun LiveTab(
    matchDetail: MatchDetail,
    viewModel: MatchDetailViewModel
) {
    // Collect live match data from ViewModel
    val liveDataResource by viewModel.liveMatchData.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshingLiveData.collectAsStateWithLifecycle()

    // Start polling when tab is shown
    LaunchedEffect(Unit) {
        if (matchDetail.status?.isLive == true) {
            viewModel.startLivePolling(matchDetail.fixtureId)
        }
    }

    // Stop polling when tab is hidden
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopLivePolling()
        }
    }

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Live status indicator and refresh button (compact version)
        LiveStatusBar(
            matchDetail = matchDetail,
            liveDataResource = liveDataResource,
            isRefreshing = isRefreshing,
            onRefresh = {
                coroutineScope.launch {
                    viewModel.refreshLiveData(matchDetail.fixtureId)
                }
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        when (liveDataResource) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is Resource.Error -> {
                val errorMessage = (liveDataResource as Resource.Error).message
                LiveErrorState(
                    errorMessage = errorMessage,
                    onRetry = {
                        coroutineScope.launch {
                            viewModel.refreshLiveData(matchDetail.fixtureId)
                        }
                    },
                    modifier = Modifier.padding(16.dp)
                )
            }

            is Resource.Success -> {
                val liveData = (liveDataResource as Resource.Success).data
                LiveContent(
                    matchDetail = matchDetail,
                    liveData = liveData,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            else -> {
                // Initial state - show match detail as fallback
                LiveFallbackContent(
                    matchDetail = matchDetail,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Compact live status bar with refresh button.
 * Shows only the essential info without duplicating the score.
 */
@Composable
private fun LiveStatusBar(
    matchDetail: MatchDetail,
    liveDataResource: Resource<LiveMatchData>?,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live status indicator with elapsed time
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Live indicator
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color.Red)
                )
                Text(
                    text = "LIVE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )

                // Elapsed time if available
                if (liveDataResource is Resource.Success) {
                    val liveData = liveDataResource.data
                    if (liveData.elapsedTime != null) {
                        Text(
                            text = liveData.elapsedTimeDisplay,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    // Show match status as fallback
                    Text(
                        text = matchDetail.status?.displayName ?: "Live",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Refresh button
            IconButton(
                onClick = onRefresh,
                enabled = !isRefreshing,
                modifier = Modifier.size(36.dp)
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Vernieuwen",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Live content when data is successfully loaded.
 */
@Composable
private fun LiveContent(
    matchDetail: MatchDetail,
    liveData: LiveMatchData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Key Events Section
        if (liveData.keyEvents.isNotEmpty()) {
            LiveEventsSection(
                events = liveData.keyEvents,
                homeTeam = matchDetail.homeTeam,
                awayTeam = matchDetail.awayTeam
            )
        }

        // Live Statistics Section
        if (liveData.hasStatistics) {
            LiveStatisticsSection(
                statistics = liveData.statistics,
                homeTeam = matchDetail.homeTeam,
                awayTeam = matchDetail.awayTeam
            )
        }

        // Recent Events Section
        if (liveData.recentEvents.isNotEmpty()) {
            RecentEventsSection(
                events = liveData.recentEvents,
                homeTeam = matchDetail.homeTeam,
                awayTeam = matchDetail.awayTeam
            )
        }

        // Live Odds Section
        if (liveData.hasLiveOdds) {
            liveData.liveOdds?.let { liveOdds ->
                LiveOddsSection(
                    liveOdds = liveOdds,
                    homeTeam = matchDetail.homeTeam,
                    awayTeam = matchDetail.awayTeam
                )
            }
        }

        // Last updated info
        Text(
            text = "Laatst bijgewerkt: ${formatLastUpdated(liveData.lastUpdated)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Live events section showing key events (goals, red cards).
 */
@Composable
private fun LiveEventsSection(
    events: List<LiveEvent>,
    homeTeam: String,
    awayTeam: String,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Section header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SportsSoccer,
                    contentDescription = "Belangrijke gebeurtenissen",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Belangrijke Gebeurtenissen",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // Events list
            events.forEach { event ->
                LiveEventItem(
                    event = event,
                    homeTeam = homeTeam,
                    awayTeam = awayTeam,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Live statistics section with comparison bars.
 */
@Composable
private fun LiveStatisticsSection(
    statistics: List<LiveStatistic>,
    homeTeam: String,
    awayTeam: String,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = "Live statistieken",
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Live Statistieken",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // Statistics list - show all available statistics
            statistics.forEach { stat ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Stat name
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stat.type,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        // Unit if available
                        stat.unit?.let { unit ->
                            Text(
                                text = unit,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Stat comparison bar
                    StatComparisonBar(
                        homeValue = stat.homeDisplay,
                        awayValue = stat.awayDisplay,
                        homeLabel = homeTeam.take(3).uppercase(),
                        awayLabel = awayTeam.take(3).uppercase(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Values
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${stat.homeDisplay}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${stat.awayDisplay}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Recent events section with timeline.
 */
@Composable
private fun RecentEventsSection(
    events: List<LiveEvent>,
    homeTeam: String,
    awayTeam: String,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Section header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Recente gebeurtenissen",
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = "Recente Gebeurtenissen",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // Events timeline
            events.forEach { event ->
                LiveEventTimelineItem(
                    event = event,
                    homeTeam = homeTeam,
                    awayTeam = awayTeam,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Live odds section with betting information.
 */
@Composable
private fun LiveOddsSection(
    liveOdds: LiveOdds,
    homeTeam: String,
    awayTeam: String,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Section header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "💰",
                        fontSize = 20.sp
                    )
                    Text(
                        text = "Live Wedkansen",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Odds status
                Badge(
                    containerColor = when (liveOdds.status) {
                        LiveOddsStatus.ACTIVE -> MaterialTheme.colorScheme.primaryContainer
                        LiveOddsStatus.SUSPENDED -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    contentColor = when (liveOdds.status) {
                        LiveOddsStatus.ACTIVE -> MaterialTheme.colorScheme.onPrimaryContainer
                        LiveOddsStatus.SUSPENDED -> MaterialTheme.colorScheme.onError
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                ) {
                    Text(text = liveOdds.status.displayName)
                }
            }

            // Bookmaker info
            Text(
                text = "Bookmaker: ${liveOdds.bookmakerName}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Divider(color = MaterialTheme.colorScheme.outlineVariant)

            // Main odds (Home / Draw / Away)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Home
                liveOdds.homeWin?.let { odds ->
                    OddsButton(
                        label = "1",
                        value = odds,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Draw
                liveOdds.draw?.let { odds ->
                    OddsButton(
                        label = "X",
                        value = odds,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Away
                liveOdds.awayWin?.let { odds ->
                    OddsButton(
                        label = "2",
                        value = odds,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Additional markets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Over 2.5
                liveOdds.overUnder["Over 2.5"]?.let { odds ->
                    OddsButton(
                        label = "Over 2.5",
                        value = odds,
                        modifier = Modifier.weight(1f)
                    )
                }

                // BTTS
                liveOdds.bothTeamsToScore["Yes"]?.let { odds ->
                    OddsButton(
                        label = "BTTS",
                        value = odds,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun OddsButton(
    label: String,
    value: Double,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = { },
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = String.format("%.2f", value),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Component for a single live event item.
 */
@Composable
private fun LiveEventItem(
    event: LiveEvent,
    homeTeam: String,
    awayTeam: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Time
        Text(
            text = "${event.displayTime}",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(40.dp)
        )

        // Icon based on event type
        val icon = when (event.type) {
            EventType.GOAL -> "⚽"
            EventType.YELLOW_CARD -> "🟨"
            EventType.RED_CARD -> "🟥"
            EventType.SUBSTITUTION -> "🔄"
            EventType.VAR -> "📺"
            EventType.PENALTY -> "🎯"
            EventType.OWN_GOAL -> "😬"
            EventType.MISSED_PENALTY -> "❌"
            else -> {
                // Handle corner events and other types
                when (event.detail?.lowercase()) {
                    "corner" -> "↩️"
                    "foul" -> "⚠️"
                    "offside" -> "🚩"
                    "shot on target" -> "🎯"
                    "shot off target" -> "🎯"
                    else -> "📝"
                }
            }
        }

        Text(
            text = icon,
            fontSize = 16.sp
        )

        // Event description
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${event.player ?: "Onbekende speler"} (${event.team})",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            event.assist?.let { assist ->
                Text(
                    text = "Assist: $assist",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            event.detail?.let { detail ->
                Text(
                    text = detail,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Component for a timeline event item.
 */
@Composable
private fun LiveEventTimelineItem(
    event: LiveEvent,
    homeTeam: String,
    awayTeam: String,
    modifier: Modifier = Modifier
) {
    // Similar structure to LiveEventItem but styled for timeline
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Time bubble
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = event.displayTime,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Event details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.type.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "${event.player ?: "Onbekende speler"} (${event.team})",
                style = MaterialTheme.typography.bodyMedium
            )
            event.detail?.let { detail ->
                Text(
                    text = detail,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Error state display.
 */
@Composable
private fun LiveErrorState(
    errorMessage: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Fout",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = errorMessage ?: "Er is een fout opgetreden bij het laden van live data.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Text("Opnieuw proberen")
        }
    }
}

/**
 * Fallback content when no live data is available.
 */
@Composable
private fun LiveFallbackContent(
    matchDetail: MatchDetail,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Wachten op live updates...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp
        )
    }
}

/**
 * Stat comparison bar component for visualizing statistics.
 */
@Composable
private fun StatComparisonBar(
    homeValue: String,
    awayValue: String,
    homeLabel: String,
    awayLabel: String,
    modifier: Modifier = Modifier
) {
    // Parse values for percentage calculation
    val homeInt = homeValue.replace("%", "").toIntOrNull() ?: 0
    val awayInt = awayValue.replace("%", "").toIntOrNull() ?: 0
    val total = homeInt + awayInt
    val homePercentage = if (total > 0) homeInt.toFloat() / total else 0.5f
    val awayPercentage = if (total > 0) awayInt.toFloat() / total else 0.5f

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = homeLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = awayLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Bar container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            // Home bar (left side)
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = homePercentage)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )

            // Away bar (right side)
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = awayPercentage)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.tertiary)
                    .align(Alignment.CenterEnd)
            )
        }
    }
}

/**
 * Helper to format last updated timestamp.
 */
private fun formatLastUpdated(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
