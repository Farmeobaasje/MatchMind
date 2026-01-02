package com.Lyno.matchmindai.presentation.components.kaptigun

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.Lyno.matchmindai.domain.model.HeadToHeadDuel
import com.Lyno.matchmindai.domain.model.PerformanceLabel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Card displaying head-to-head history between two teams.
 * Shows last 5 matches with scores, xG, and performance indicators.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeadToHeadCard(
    headToHeadDuels: List<HeadToHeadDuel>,
    homeTeamName: String,
    awayTeamName: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Onderlinge Duels",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "Laatste ${headToHeadDuels.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Column headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Datum",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Uitslag",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "xG",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Perf.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(0.5f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Head-to-head list
            if (headToHeadDuels.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Geen onderlinge duels gevonden",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    headToHeadDuels.forEach { duel ->
                        HeadToHeadRow(duel = duel)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PerformanceLegendItem(
                    label = PerformanceLabel.DOMINANT,
                    text = "Dominant"
                )
                PerformanceLegendItem(
                    label = PerformanceLabel.LUCKY,
                    text = "Geluk"
                )
                PerformanceLegendItem(
                    label = PerformanceLabel.UNLUCKY,
                    text = "Pech"
                )
                PerformanceLegendItem(
                    label = PerformanceLabel.NEUTRAL,
                    text = "Neutraal"
                )
            }
        }
    }
}

@Composable
private fun HeadToHeadRow(
    duel: HeadToHeadDuel
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date
        Text(
            text = formatDate(duel.date),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )

        // Score
        Text(
            text = "${duel.homeScore} - ${duel.awayScore}",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        // xG
        Text(
            text = String.format("%.1f - %.1f", duel.homeXg, duel.awayXg),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        // Performance indicator
        Box(
            modifier = Modifier
                .size(16.dp)
                .weight(0.5f),
            contentAlignment = Alignment.Center
        ) {
            PerformanceIndicator(performanceLabel = duel.performanceLabel)
        }
    }
}

@Composable
private fun PerformanceIndicator(
    performanceLabel: PerformanceLabel
) {
    val color = when (performanceLabel) {
        PerformanceLabel.DOMINANT -> Color(0xFF4CAF50) // Green
        PerformanceLabel.LUCKY -> Color(0xFFFF9800) // Orange
        PerformanceLabel.UNLUCKY -> Color(0xFFF44336) // Red
        PerformanceLabel.NEUTRAL -> Color(0xFF9E9E9E) // Gray
    }

    Box(
        modifier = Modifier
            .size(12.dp)
            .background(color, shape = CircleShape)
    )
}

@Composable
private fun PerformanceLegendItem(
    label: PerformanceLabel,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        PerformanceIndicator(performanceLabel = label)
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: return dateString)
    } catch (e: Exception) {
        dateString
    }
}
