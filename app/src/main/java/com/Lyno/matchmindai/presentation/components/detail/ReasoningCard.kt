package com.Lyno.matchmindai.presentation.components.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Lyno.matchmindai.R
import com.Lyno.matchmindai.presentation.components.GlassCard
import com.Lyno.matchmindai.ui.theme.PrimaryNeon
import com.Lyno.matchmindai.ui.theme.SurfaceCard

/**
 * ReasoningCard - Displays pure statistical reasoning from Oracle analysis.
 * Clean, focused card for the "Hard Reality Engine" explanation.
 *
 * @param reasoning The statistical reasoning text
 * @param modifier Modifier for the component
 */
@Composable
fun ReasoningCard(
    reasoning: String,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with insights icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Insights,
                    contentDescription = "Statistical Reasoning",
                    tint = PrimaryNeon,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = stringResource(R.string.statistical_reasoning),
                    style = MaterialTheme.typography.labelLarge,
                    color = PrimaryNeon,
                    fontWeight = FontWeight.Bold
                )
            }

            // Reasoning text in a subtle background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceCard.copy(alpha = 0.3f))
                    .padding(16.dp)
            ) {
                Text(
                    text = reasoning,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2f
                )
            }

            // Hard Reality Engine footer
            Surface(
                shape = MaterialTheme.shapes.small,
                color = PrimaryNeon.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, PrimaryNeon.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🔍",
                        fontSize = 14.sp
                    )
                    Text(
                        text = stringResource(R.string.hard_reality_engine),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    }
}

/**
 * Compact version of ReasoningCard for use in tighter spaces.
 */
@Composable
fun CompactReasoningCard(
    reasoning: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = SurfaceCard.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, PrimaryNeon.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.statistical_reasoning),
                style = MaterialTheme.typography.labelMedium,
                color = PrimaryNeon,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = reasoning,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.2f,
                maxLines = 4
            )
        }
    }
}

/**
 * Preview version of ReasoningCard for testing.
 */
@Composable
fun ReasoningCardPreview() {
    val sampleReasoning = "Manchester City staat momenteel op de 2e plaats in de Premier League met 65 punten uit 28 wedstrijden. Nottingham Forest staat 17e met 25 punten uit 28 wedstrijden. City heeft een doelsaldo van +45, terwijl Forest een doelsaldo heeft van -18. Op basis van deze statistieken heeft City een significant voordeel in zowel aanvalskracht als verdediging. Het verschil van 40 punten en 63 doelpunten in het doelsaldo ondersteunt de voorspelling van een duidelijke thuisoverwinning."

    ReasoningCard(
        reasoning = sampleReasoning,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}
