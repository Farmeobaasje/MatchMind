package com.Lyno.matchmindai.presentation.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.Lyno.matchmindai.R
import com.Lyno.matchmindai.domain.model.UserPreferences
import com.Lyno.matchmindai.ui.theme.MatchMindAITheme

/**
 * Widget that displays API usage as a "Daily Energy Bar".
 * 
 * Shows the remaining API calls as a percentage with color coding:
 * - Green: > 25% remaining
 * - Yellow: 10-25% remaining (warning)
 * - Red: < 10% remaining (critical)
 * 
 * Used in the Settings screen to visualize rate limits.
 */
@Composable
fun UsageWidget(
    userPreferences: UserPreferences,
    modifier: Modifier = Modifier
) {
    val percentage = userPreferences.apiUsagePercentage
    val remaining = userPreferences.apiCallsRemaining
    val limit = userPreferences.apiCallsLimit
    
    // Determine color based on usage level
    val progressColor = when {
        userPreferences.isApiUsageCritical -> Color(0xFFEF5350) // Red
        userPreferences.isApiUsageWarning -> Color(0xFFFFB74D) // Orange/Yellow
        else -> Color(0xFF66BB6A) // Green
    }
    
    // Determine text color based on usage level
    val textColor = when {
        userPreferences.isApiUsageCritical -> Color(0xFFEF5350)
        userPreferences.isApiUsageWarning -> Color(0xFFFFB74D)
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.daily_energy_bar),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "$remaining/$limit",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
        
        // Progress bar
        LinearProgressIndicator(
            progress = percentage / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = progressColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        
        // Status text
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = when {
                    userPreferences.isApiUsageCritical -> stringResource(R.string.api_usage_critical)
                    userPreferences.isApiUsageWarning -> stringResource(R.string.api_usage_warning)
                    else -> stringResource(R.string.api_usage_normal)
                },
                style = MaterialTheme.typography.bodySmall,
                color = textColor
            )
            
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
        
        // Last update time
        if (userPreferences.lastRateLimitUpdate > 0) {
            val timeAgo = formatTimeAgo(userPreferences.lastRateLimitUpdate)
            Text(
                text = stringResource(R.string.last_updated, timeAgo),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

/**
 * Format timestamp to relative time string.
 * 
 * @param timestamp The timestamp in milliseconds
 * @return Formatted time string (e.g., "2 hours ago", "Just now")
 */
private fun formatTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60000 -> "Just now" // Less than 1 minute
        diff < 3600000 -> "${diff / 60000} minutes ago" // Less than 1 hour
        diff < 86400000 -> "${diff / 3600000} hours ago" // Less than 1 day
        else -> "${diff / 86400000} days ago" // More than 1 day
    }
}

@Preview(showBackground = true)
@Composable
fun UsageWidgetPreview() {
    MatchMindAITheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Normal usage (75%)
            UsageWidget(
                userPreferences = UserPreferences(
                    apiCallsRemaining = 75,
                    apiCallsLimit = 100,
                    lastRateLimitUpdate = System.currentTimeMillis() - 3600000 // 1 hour ago
                )
            )
            
            // Warning usage (20%)
            UsageWidget(
                userPreferences = UserPreferences(
                    apiCallsRemaining = 20,
                    apiCallsLimit = 100,
                    lastRateLimitUpdate = System.currentTimeMillis() - 7200000 // 2 hours ago
                )
            )
            
            // Critical usage (5%)
            UsageWidget(
                userPreferences = UserPreferences(
                    apiCallsRemaining = 5,
                    apiCallsLimit = 100,
                    lastRateLimitUpdate = System.currentTimeMillis() - 10800000 // 3 hours ago
                )
            )
        }
    }
}
