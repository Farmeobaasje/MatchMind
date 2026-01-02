package com.Lyno.matchmindai.presentation.components.visual

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.Lyno.matchmindai.ui.theme.*

/**
 * Cache Context Bar component that shows information about data source (cache vs API).
 * This helps users understand where the AI is getting its data from.
 */
@Composable
fun CacheContextBar(
    context: String,
    source: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine color based on source
    val backgroundColor = when {
        source.contains("cache", ignoreCase = true) -> PrimaryNeon.copy(alpha = 0.1f)
        source.contains("API", ignoreCase = true) -> SecondaryPurple.copy(alpha = 0.1f)
        else -> SurfaceCard.copy(alpha = 0.1f)
    }
    
    val borderColor = when {
        source.contains("cache", ignoreCase = true) -> PrimaryNeon
        source.contains("API", ignoreCase = true) -> SecondaryPurple
        else -> TextMedium
    }
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Info icon and context
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Data Source",
                    tint = borderColor,
                    modifier = Modifier.size(20.dp)
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = context,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextHigh
                    )
                    Text(
                        text = "Bron: $source",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMedium
                    )
                }
            }
            
            // Close button
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Sluiten",
                    tint = TextMedium,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Data Flow Visualizer component that shows how data flows through the system.
 * This helps users understand the connection between different components.
 */
@Composable
fun DataFlowVisualizer(
    fixtureId: Int,
    matchDetail: com.Lyno.matchmindai.domain.model.MatchDetail? = null,
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                // API-Sports
                FlowNode(
                    label = "API-Sports",
                    isActive = cacheStatus != com.Lyno.matchmindai.domain.service.CacheStatus.FRESH,
                    color = SecondaryPurple
                )
                
                // Arrow
                Text("→", color = TextMedium)
                
                // Cache
                FlowNode(
                    label = "Cache",
                    isActive = cacheStatus == com.Lyno.matchmindai.domain.service.CacheStatus.FRESH,
                    color = PrimaryNeon
                )
                
                // Arrow
                Text("→", color = TextMedium)
                
                // AI Agent
                FlowNode(
                    label = "AI Agent",
                    isActive = true,
                    color = SecondaryPurple
                )
            }
            
            // Status text
            val statusText = when (cacheStatus) {
                com.Lyno.matchmindai.domain.service.CacheStatus.FRESH -> "✅ Data uit cache"
                com.Lyno.matchmindai.domain.service.CacheStatus.STALE -> "🔄 Cache verouderd, live data geladen"
                com.Lyno.matchmindai.domain.service.CacheStatus.MISSING -> "🌐 Live data van API-Sports"
                null -> "🌐 Live data van API-Sports"
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
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isActive) color.copy(alpha = 0.2f) else SurfaceCard.copy(alpha = 0.1f)
                )
                .clip(RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label.take(2),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
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
 * Connection Indicator component that shows real-time connection status.
 */
@Composable
fun ConnectionIndicator(
    isConnected: Boolean,
    latency: Int? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected) {
                PrimaryNeon.copy(alpha = 0.1f)
            } else {
                Color.Red.copy(alpha = 0.1f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Status dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (isConnected) PrimaryNeon else Color.Red
                    )
            )
            
            // Status text
            Text(
                text = if (isConnected) {
                    "Verbonden${latency?.let { " ($it ms)" } ?: ""}"
                } else {
                    "Niet verbonden"
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (isConnected) TextHigh else Color.Red
            )
        }
    }
}
