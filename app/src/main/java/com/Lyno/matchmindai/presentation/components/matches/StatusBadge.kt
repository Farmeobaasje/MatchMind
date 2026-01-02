package com.Lyno.matchmindai.presentation.components.matches

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.Lyno.matchmindai.presentation.components.StatusHelper
import com.Lyno.matchmindai.ui.theme.ConfidenceHigh
import com.Lyno.matchmindai.ui.theme.ConfidenceLow
import com.Lyno.matchmindai.ui.theme.ConfidenceMedium
import com.Lyno.matchmindai.ui.theme.TextDisabled
import com.Lyno.matchmindai.ui.theme.TextHigh

/**
 * A reusable chip/badge component for displaying match status.
 * Uses StatusHelper for consistent status mapping across the app.
 */
@Composable
fun StatusBadge(
    status: String?,
    modifier: Modifier = Modifier,
    showIcon: Boolean = true
) {
    val backgroundColor = StatusHelper.getColor(status)
    val textColor = StatusHelper.getTextColor(status)
    val displayText = StatusHelper.getLabel(status)

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = backgroundColor,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showIcon) {
                // Status indicator dot
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = textColor,
                    modifier = Modifier.size(6.dp)
                ) {}
                Spacer(modifier = Modifier.width(6.dp))
            }
            
            Text(
                text = displayText,
                color = textColor,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                maxLines = 1
            )
        }
    }
}

/**
 * Preview for a live match status.
 */
@Preview(showBackground = true, backgroundColor = 0xFF0A0E17)
@Composable
fun StatusBadgeLivePreview() {
    MaterialTheme {
        StatusBadge(status = "1H")
    }
}

/**
 * Preview for a finished match status.
 */
@Preview(showBackground = true, backgroundColor = 0xFF0A0E17)
@Composable
fun StatusBadgeFinishedPreview() {
    MaterialTheme {
        StatusBadge(status = "FT")
    }
}

/**
 * Preview for a postponed match status.
 */
@Preview(showBackground = true, backgroundColor = 0xFF0A0E17)
@Composable
fun StatusBadgePostponedPreview() {
    MaterialTheme {
        StatusBadge(status = "PST")
    }
}

/**
 * Preview for a not started match status.
 */
@Preview(showBackground = true, backgroundColor = 0xFF0A0E17)
@Composable
fun StatusBadgeNotStartedPreview() {
    MaterialTheme {
        StatusBadge(status = "NS")
    }
}

/**
 * Preview for multiple status badges in a row.
 */
@Preview(showBackground = true, backgroundColor = 0xFF0A0E17, widthDp = 300)
@Composable
fun StatusBadgeRowPreview() {
    MaterialTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            StatusBadge(status = "1H")
            StatusBadge(status = "FT")
            StatusBadge(status = "PST")
            StatusBadge(status = "NS")
        }
    }
}
