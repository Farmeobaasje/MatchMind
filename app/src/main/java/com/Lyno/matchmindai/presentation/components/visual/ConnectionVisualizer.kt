package com.Lyno.matchmindai.presentation.components.visual

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.Lyno.matchmindai.ui.theme.*

/**
 * Connection status for the visualizer.
 */
enum class ConnectionStatus {
    CONNECTED,      // Fully connected and synced
    SYNCING,        // Syncing data
    DISCONNECTED,   // No connection
    CACHED          // Using cached data
}

/**
 * Simple connection indicator showing live connection status.
 */
@Composable
fun ConnectionIndicator(
    status: ConnectionStatus,
    lastSyncTime: Long? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (status) {
                ConnectionStatus.CONNECTED -> PrimaryNeon.copy(alpha = 0.1f)
                ConnectionStatus.SYNCING -> SecondaryPurple.copy(alpha = 0.1f)
                ConnectionStatus.DISCONNECTED -> Color.Red.copy(alpha = 0.1f)
                ConnectionStatus.CACHED -> SurfaceCard.copy(alpha = 0.2f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Status dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .then(
                        Modifier.background(
                            when (status) {
                                ConnectionStatus.CONNECTED -> PrimaryNeon
                                ConnectionStatus.SYNCING -> SecondaryPurple
                                ConnectionStatus.DISCONNECTED -> Color.Red
                                ConnectionStatus.CACHED -> TextMedium
                            }
                        )
                    )
            )

            // Status text
            Column {
                Text(
                    text = when (status) {
                        ConnectionStatus.CONNECTED -> "Live verbonden"
                        ConnectionStatus.SYNCING -> "Data synchroniseren..."
                        ConnectionStatus.DISCONNECTED -> "Offline"
                        ConnectionStatus.CACHED -> "Gecachte data"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = when (status) {
                        ConnectionStatus.CONNECTED -> PrimaryNeon
                        ConnectionStatus.SYNCING -> SecondaryPurple
                        ConnectionStatus.DISCONNECTED -> Color.Red
                        ConnectionStatus.CACHED -> TextMedium
                    }
                )

                lastSyncTime?.let {
                    Text(
                        text = formatTimeAgo(it),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMedium.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Utility function to format time ago.
 */
private fun formatTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60000 -> "Zojuist" // Less than 1 minute
        diff < 3600000 -> "${diff / 60000} min geleden" // Less than 1 hour
        diff < 86400000 -> "${diff / 3600000} uur geleden" // Less than 1 day
        else -> "${diff / 86400000} dagen geleden"
    }
}

/**
 * Simple data flow visualizer showing the flow of data through the system.
 */
@Composable
fun DataFlowVisualizer(
    isActive: Boolean = true,
    cacheStatus: com.Lyno.matchmindai.domain.service.CacheStatus? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceCard.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Data Flow",
                    tint = PrimaryNeon,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Data Flow",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextHigh
                )
            }

            // Flow visualization
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // API Source
                FlowNode(
                    label = "API",
                    isActive = isActive && cacheStatus != com.Lyno.matchmindai.domain.service.CacheStatus.FRESH,
                    color = SecondaryPurple
                )

                // Arrow
                Text("→", color = TextMedium)

                // Cache
                FlowNode(
                    label = "Cache",
                    isActive = isActive && cacheStatus == com.Lyno.matchmindai.domain.service.CacheStatus.FRESH,
                    color = PrimaryNeon
                )

                // Arrow
                Text("→", color = TextMedium)

                // AI Agent
                FlowNode(
                    label = "AI",
                    isActive = isActive,
                    color = SecondaryPurple
                )
            }

            // Status text
            val statusText = when (cacheStatus) {
                com.Lyno.matchmindai.domain.service.CacheStatus.FRESH -> "✅ Data uit cache"
                com.Lyno.matchmindai.domain.service.CacheStatus.STALE -> "🔄 Cache verouderd, live data geladen"
                com.Lyno.matchmindai.domain.service.CacheStatus.MISSING -> "🌐 Live data van API"
                null -> "🌐 Live data van API"
            }
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                color = TextMedium
            )
        }
    }
}

/**
 * Flow node for data flow visualization.
 */
@Composable
private fun FlowNode(
    label: String,
    isActive: Boolean,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Node visual
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .then(
                    Modifier.background(
                        if (isActive) color.copy(alpha = 0.2f)
                        else SurfaceCard.copy(alpha = 0.1f)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = if (isActive) color else TextDisabled
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) TextHigh else TextDisabled
        )
    }
}

/**
 * Preview function for ConnectionVisualizer components.
 */
@Composable
@androidx.compose.ui.tooling.preview.Preview
fun ConnectionVisualizerPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Connection indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ConnectionIndicator(status = ConnectionStatus.CONNECTED)
                ConnectionIndicator(status = ConnectionStatus.SYNCING)
                ConnectionIndicator(status = ConnectionStatus.CACHED)
                ConnectionIndicator(status = ConnectionStatus.DISCONNECTED)
            }

            // Data flow visualizer
            DataFlowVisualizer(
                isActive = true,
                cacheStatus = com.Lyno.matchmindai.domain.service.CacheStatus.FRESH,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
