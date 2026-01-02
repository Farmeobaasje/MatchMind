package com.Lyno.matchmindai.presentation.components.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.Lyno.matchmindai.presentation.components.GlassCard
import com.Lyno.matchmindai.ui.theme.PrimaryNeon

/**
 * Data Quality Indicator - Shows freshness and sources.
 * Common component used across IntelligenceTab and VerslagTab.
 */
@Composable
fun DataQualityIndicator(
    dataQuality: String,
    lastUpdate: String,
    sources: List<String>,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Data kwaliteit",
                tint = PrimaryNeon
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Data Kwaliteit",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Bijgewerkt: $lastUpdate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Bronnen: ${sources.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Dynamic badge based on data quality
            val badgeColor = when (dataQuality.lowercase()) {
                "excellent" -> Color(0xFF4CAF50) // Green
                "good" -> Color(0xFF2196F3) // Blue
                "fair" -> Color(0xFFFF9800) // Orange
                "poor" -> Color(0xFFF44336) // Red
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
            
            Badge(
                containerColor = badgeColor.copy(alpha = 0.2f),
                contentColor = badgeColor
            ) {
                Text(
                    text = dataQuality,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
