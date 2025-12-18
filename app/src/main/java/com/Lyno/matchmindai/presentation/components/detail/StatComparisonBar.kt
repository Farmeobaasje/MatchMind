package com.Lyno.matchmindai.presentation.components.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.Lyno.matchmindai.ui.theme.ActionOrange
import com.Lyno.matchmindai.ui.theme.PrimaryNeon

/**
 * StatComparisonBar component for displaying match statistics.
 * Shows a horizontal comparison bar with home and away values.
 * 
 * @param statName The name of the statistic (e.g., "Ball Possession")
 * @param homeValue The home team value (percentage or count)
 * @param awayValue The away team value (percentage or count)
 * @param homeLabel Short label for home team (e.g., "HOM")
 * @param awayLabel Short label for away team (e.g., "AWY")
 * @param unit The unit of measurement ("%" or "")
 * @param modifier Modifier for the component
 */
@Composable
fun StatComparisonBar(
    statName: String,
    homeValue: String,
    awayValue: String,
    homeLabel: String,
    awayLabel: String,
    unit: String = "%",
    modifier: Modifier = Modifier
) {
    // Parse values for progress calculation
    val homeFloat = parseStatValue(homeValue)
    val awayFloat = parseStatValue(awayValue)
    
    // Calculate total for percentage display
    val total = homeFloat + awayFloat
    // Ensure weights are always greater than 0 to avoid IllegalArgumentException
    val homePercentage = if (total > 0) (homeFloat / total) else 0.5f
    val awayPercentage = if (total > 0) (awayFloat / total) else 0.5f
    // Add a small epsilon to ensure weights are never exactly 0.0
    val homeWeight = maxOf(homePercentage, 0.001f)
    val awayWeight = maxOf(awayPercentage, 0.001f)
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Stat name and team labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home team label and value
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = homeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = PrimaryNeon,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$homeValue$unit",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Stat name
            Text(
                text = statName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Away team label and value
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = awayLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = ActionOrange,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$awayValue$unit",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Progress bar container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            // Two bars growing from center approach
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center
            ) {
                // Home progress (left side)
                Box(
                    modifier = Modifier
                        .weight(homeWeight)
                        .fillMaxHeight()
                        .background(PrimaryNeon)
                )
                
                // Away progress (right side)
                Box(
                    modifier = Modifier
                        .weight(awayWeight)
                        .fillMaxHeight()
                        .background(ActionOrange)
                )
            }
        }
        
        // Percentage labels below bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${(homePercentage * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = PrimaryNeon,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "${(awayPercentage * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = ActionOrange,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Alternative implementation with single progress indicator.
 * Shows home progress on left, away on right with a divider in middle.
 */
@Composable
fun StatComparisonBarAlternative(
    statName: String,
    homeValue: String,
    awayValue: String,
    homeLabel: String,
    awayLabel: String,
    unit: String = "%",
    modifier: Modifier = Modifier
) {
    val homeFloat = parseStatValue(homeValue)
    val awayFloat = parseStatValue(awayValue)
    val total = homeFloat + awayFloat
    val homePercentage = if (total > 0) (homeFloat / total) else 0.5f
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$homeLabel: $homeValue$unit",
                style = MaterialTheme.typography.bodySmall,
                color = PrimaryNeon
            )
            
            Text(
                text = statName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "$awayLabel: $awayValue$unit",
                style = MaterialTheme.typography.bodySmall,
                color = ActionOrange
            )
        }
        
        // Progress bar with center indicator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
        ) {
            // Background track
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            
            // Home progress (left side)
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = homePercentage)
                    .fillMaxHeight()
                    .background(PrimaryNeon)
            )
            
            // Center divider
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(2.dp)
                    .align(Alignment.Center)
                    .background(Color.White.copy(alpha = 0.5f))
            )
            
            // Away progress (right side)
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = awayFloat / maxOf(total, 1f))
                    .fillMaxHeight()
                    .align(Alignment.CenterEnd)
                    .background(ActionOrange)
            )
        }
    }
}

/**
 * Parses statistic value from string.
 * Handles percentages (54%) and regular numbers.
 */
private fun parseStatValue(value: String): Float {
    return try {
        if (value.endsWith("%")) {
            value.dropLast(1).toFloatOrNull() ?: 0f
        } else {
            value.toFloatOrNull() ?: 0f
        }
    } catch (e: Exception) {
        0f
    }
}

/**
 * Preview function for development.
 */
@Composable
fun StatComparisonBarPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatComparisonBar(
            statName = "Ball Possession",
            homeValue = "54",
            awayValue = "46",
            homeLabel = "HOM",
            awayLabel = "AWY",
            unit = "%"
        )
        
        StatComparisonBar(
            statName = "Shots on Goal",
            homeValue = "8",
            awayValue = "4",
            homeLabel = "HOM",
            awayLabel = "AWY",
            unit = ""
        )
        
        StatComparisonBarAlternative(
            statName = "Corner Kicks",
            homeValue = "5",
            awayValue = "3",
            homeLabel = "HOM",
            awayLabel = "AWY",
            unit = ""
        )
    }
}
