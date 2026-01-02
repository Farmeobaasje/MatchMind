package com.Lyno.matchmindai.presentation.components.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.Lyno.matchmindai.domain.model.EventType
import com.Lyno.matchmindai.domain.model.MatchEvent
import com.Lyno.matchmindai.ui.theme.ActionOrange
import com.Lyno.matchmindai.ui.theme.PrimaryNeon

/**
 * EventTimeline component for displaying match events in chronological order.
 * Shows a vertical timeline with icons for goals, cards, substitutions, etc.
 * 
 * @param events List of match events
 * @param modifier Modifier for the component
 */
@Composable
fun EventTimeline(
    events: List<MatchEvent>,
    modifier: Modifier = Modifier
) {
    if (events.isEmpty()) {
        EventTimelineEmptyState(modifier = modifier)
        return
    }

    // Sort events by minute (chronological order)
    val sortedEvents = events.sortedBy { it.minute }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        sortedEvents.forEach { event ->
            TimelineEventItem(event = event)
        }
    }
}

/**
 * Individual timeline event item.
 */
@Composable
private fun TimelineEventItem(
    event: MatchEvent,
    modifier: Modifier = Modifier
) {
    val isHomeEvent = event.team.contains("Home", ignoreCase = true) || 
                     event.team.contains(event.player ?: "", ignoreCase = true)
    val teamColor = if (isHomeEvent) PrimaryNeon else ActionOrange

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = teamColor.copy(alpha = 0.1f),
            contentColor = teamColor
        ),
        shape = MaterialTheme.shapes.small,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Time indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(teamColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = event.displayTime,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = teamColor
                    )
                }
            }

            // Event icon
            Box(
                modifier = Modifier.size(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = event.type.emoji,
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            // Event details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Event type and player
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = event.type.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = teamColor
                    )
                    
                    event.player?.let { player ->
                        Text(
                            text = "• $player",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Team and additional details
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = event.team,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    event.detail?.let { detail ->
                        if (detail.isNotBlank()) {
                            Text(
                                text = detail,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Assist and comments
                event.assist?.let { assist ->
                    Text(
                        text = "Assist: $assist",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                event.comments?.let { comments ->
                    if (comments.isNotBlank()) {
                        Text(
                            text = comments,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
        }
    }
}

/**
 * Compact timeline event item for dense layouts.
 */
@Composable
fun CompactTimelineEventItem(
    event: MatchEvent,
    modifier: Modifier = Modifier
) {
    val isHomeEvent = event.team.contains("Home", ignoreCase = true)
    val teamColor = if (isHomeEvent) PrimaryNeon else ActionOrange

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Time
        Text(
            text = event.displayTime,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = teamColor,
            modifier = Modifier.width(40.dp)
        )

        // Icon
        Text(
            text = event.type.emoji,
            style = MaterialTheme.typography.bodyLarge
        )

        // Event summary
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "${event.type.displayName} - ${event.player ?: event.team}",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            
            event.detail?.let { detail ->
                if (detail.isNotBlank()) {
                    Text(
                        text = detail,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Event type filter for timeline.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EventTypeFilter(
    selectedTypes: Set<EventType>,
    onTypeSelected: (EventType) -> Unit,
    modifier: Modifier = Modifier
) {
    val allEventTypes = EventType.values().toList()
    
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        allEventTypes.forEach { eventType ->
            FilterChip(
                selected = selectedTypes.contains(eventType),
                onClick = { onTypeSelected(eventType) },
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = eventType.emoji)
                        Text(text = eventType.displayName)
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = when (eventType) {
                        EventType.GOAL -> Color(0xFF4CAF50)
                        EventType.YELLOW_CARD -> Color(0xFFFFEB3B)
                        EventType.RED_CARD -> Color(0xFFF44336)
                        EventType.SUBSTITUTION -> Color(0xFF2196F3)
                        else -> MaterialTheme.colorScheme.primary
                    },
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

/**
 * Timeline with filtering capabilities.
 */
@Composable
fun FilterableEventTimeline(
    events: List<MatchEvent>,
    modifier: Modifier = Modifier
) {
    var selectedTypes by remember { mutableStateOf(emptySet<EventType>()) }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Filter chips
        EventTypeFilter(
            selectedTypes = selectedTypes,
            onTypeSelected = { eventType ->
                selectedTypes = if (selectedTypes.contains(eventType)) {
                    selectedTypes - eventType
                } else {
                    selectedTypes + eventType
                }
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        // Filter events
        val filteredEvents = if (selectedTypes.isEmpty()) {
            events
        } else {
            events.filter { selectedTypes.contains(it.type) }
        }
        
        // Timeline
        EventTimeline(
            events = filteredEvents,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Empty state for when no events are available.
 */
@Composable
fun EventTimelineEmptyState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
            Icon(
                imageVector = Icons.Default.Sports,
                contentDescription = "No events",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Nog geen gebeurtenissen",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Text(
            text = "Er zijn nog geen goals, kaarten of wissels geregistreerd.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

/**
 * Match summary with key events.
 */
@Composable
fun MatchEventSummary(
    events: List<MatchEvent>,
    modifier: Modifier = Modifier
) {
    val goals = events.filter { it.type == EventType.GOAL }
    val cards = events.filter { it.type == EventType.YELLOW_CARD || it.type == EventType.RED_CARD }
    val substitutions = events.filter { it.type == EventType.SUBSTITUTION }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Goals summary
        if (goals.isNotEmpty()) {
            EventSummarySection(
                title = "Goals (${goals.size})",
                events = goals.take(3),
                icon = "⚽"
            )
        }
        
        // Cards summary
        if (cards.isNotEmpty()) {
            EventSummarySection(
                title = "Cards (${cards.size})",
                events = cards.take(3),
                icon = "🟨"
            )
        }
        
        // Substitutions summary
        if (substitutions.isNotEmpty()) {
            EventSummarySection(
                title = "Substitutions (${substitutions.size})",
                events = substitutions.take(3),
                icon = "🔄"
            )
        }
    }
}

/**
 * Event summary section for match overview.
 */
@Composable
private fun EventSummarySection(
    title: String,
    events: List<MatchEvent>,
    icon: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Section header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = icon, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Event list
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                events.forEach { event ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${event.displayTime} ${event.player ?: event.team}",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        
                        event.detail?.let { detail ->
                            if (detail.isNotBlank()) {
                                Text(
                                    text = detail,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Preview function for development.
 */
@Composable
fun EventTimelinePreview() {
    val sampleEvents = listOf(
        MatchEvent(
            type = EventType.GOAL,
            minute = 23,
            team = "Ajax",
            player = "Steven Bergwijn",
            assist = "Brian Brobbey",
            detail = "Normal Goal"
        ),
        MatchEvent(
            type = EventType.YELLOW_CARD,
            minute = 34,
            team = "PSV",
            player = "Joey Veerman",
            detail = "Foul"
        ),
        MatchEvent(
            type = EventType.SUBSTITUTION,
            minute = 65,
            team = "Ajax",
            player = "Kenneth Taylor",
            detail = "In: Devyne Rensch"
        ),
        MatchEvent(
            type = EventType.GOAL,
            minute = 78,
            team = "PSV",
            player = "Luuk de Jong",
            assist = "Johan Bakayoko",
            detail = "Header"
        ),
        MatchEvent(
            type = EventType.RED_CARD,
            minute = 89,
            team = "Ajax",
            player = "Jorrel Hato",
            detail = "Second Yellow"
        )
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        EventTimeline(
            events = sampleEvents,
            modifier = Modifier.fillMaxWidth()
        )
        
        Divider()
        
        MatchEventSummary(
            events = sampleEvents,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
