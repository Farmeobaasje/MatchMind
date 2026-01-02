package com.Lyno.matchmindai.presentation.components.historixyi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.Lyno.matchmindai.R
import com.Lyno.matchmindai.domain.model.RetrospectiveAnalysis
import com.Lyno.matchmindai.ui.theme.*

/**
 * Horizontal comparison card showing predicted vs actual match outcome.
 * Used in History Detail screen to visualize prediction accuracy.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealityCheckCard(
    retrospective: RetrospectiveAnalysis,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = SurfaceCard,
            contentColor = TextHigh
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "Realiteitscheck",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryNeon,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Main comparison row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
    // Left side: THE PROPHECY
    ProphecySection(
        predictedScore = retrospective.predictedScore,
        confidence = retrospective.confidencePercentage
    )

                // Center: VS indicator
                VsIndicator()

                // Right side: THE REALITY
                RealitySection(
                    actualScore = retrospective.actualScore,
                    isCorrect = retrospective.outcomeCorrect
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Labels row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.history_predicted_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMedium,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.width(48.dp))

                Text(
                    text = stringResource(R.string.history_actual_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMedium,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ProphecySection(
    predictedScore: String,
    confidence: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Score display
        Text(
            text = predictedScore,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = TextHigh
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Confidence badge
        ConfidenceBadge(confidence = confidence)
    }
}

@Composable
private fun ConfidenceBadge(confidence: Int) {
    val confidenceColor = when {
        confidence >= 80 -> ConfidenceHigh
        confidence >= 60 -> ConfidenceMedium
        else -> ConfidenceLow
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(confidenceColor.copy(alpha = 0.2f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "$confidence%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = confidenceColor
            )
        }
    }
}

@Composable
private fun VsIndicator() {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(GradientEnd)
    ) {
        Text(
            text = "VS",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextMedium,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun RealitySection(
    actualScore: String,
    isCorrect: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Score display
        Text(
            text = actualScore,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = TextHigh
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Status badge
        StatusBadge(isCorrect = isCorrect)
    }
}

@Composable
private fun StatusBadge(isCorrect: Boolean) {
    val (text, color, icon) = if (isCorrect) {
        Triple(
            stringResource(R.string.history_correct),
            PrimaryNeon,
            "✅"
        )
    } else {
        Triple(
            stringResource(R.string.history_incorrect),
            Error,
            "❌"
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
