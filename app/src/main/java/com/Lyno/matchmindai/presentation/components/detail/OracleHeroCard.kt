package com.Lyno.matchmindai.presentation.components.detail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Lyno.matchmindai.R
import com.Lyno.matchmindai.domain.model.OracleAnalysis
import com.Lyno.matchmindai.domain.model.TesseractResult
import com.Lyno.matchmindai.presentation.components.GlassCard
import com.Lyno.matchmindai.ui.theme.PrimaryNeon
import com.Lyno.matchmindai.ui.theme.SecondaryPurple
import com.Lyno.matchmindai.ui.theme.SurfaceCard

/**
 * OracleHeroCard - Unified hero card combining prediction and probabilities.
 * Features a large score display with glow effect, confidence badge, and integrated probability bar.
 *
 * @param analysis The OracleAnalysis containing prediction and confidence
 * @param tesseractResult Optional Tesseract simulation results for probability bar
 * @param modifier Modifier for the component
 */
@Composable
fun OracleHeroCard(
    analysis: OracleAnalysis,
    tesseractResult: TesseractResult?,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with Oracle icon and title - Updated for Phase 6 clarity
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Oracle",
                        tint = PrimaryNeon,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(R.string.oracle_wedtip_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = PrimaryNeon,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Subtitle showing probability basis
                Text(
                    text = stringResource(R.string.oracle_wedtip_subtitle, analysis.confidence),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Large score with glow effect
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .drawWithCache {
                        onDrawWithContent {
                            // Draw glow effect
                            for (i in 1..3) {
                                val alpha = 0.3f - (i * 0.08f)
                                drawCircle(
                                    color = PrimaryNeon.copy(alpha = alpha),
                                    radius = size.minDimension / 2 + (i * 8).dp.toPx(),
                                    center = Offset(size.width / 2, size.height / 2)
                                )
                            }
                            drawContent()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = formatScore(analysis.prediction),
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNeon,
                    textAlign = TextAlign.Center
                )
            }

            // Confidence badge
            ConfidenceBadge(confidence = analysis.confidence)

            // Probability bar if Tesseract data is available
            tesseractResult?.let { tesseract ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProbabilityBar(
                        tesseractResult = tesseract,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Simulation info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.tesseract_simulation_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = "${tesseract.simulationCount} iteraties",
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryNeon,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } ?: run {
                // Placeholder when no Tesseract data
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceCard.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.tesseract_loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Confidence badge showing prediction certainty with color coding.
 */
@Composable
private fun ConfidenceBadge(confidence: Int) {
    val (confidenceText, confidenceColor) = when {
        confidence >= 80 -> Pair("ZEER HOOG", PrimaryNeon)
        confidence >= 60 -> Pair("MATIG", Color(0xFFFFA726)) // Orange
        else -> Pair("LAAG", Color(0xFFEF5350)) // Red
    }

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = confidenceColor.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, confidenceColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.confidence_label_dutch),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$confidence%",
                style = MaterialTheme.typography.titleMedium,
                color = confidenceColor,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = confidenceText,
                style = MaterialTheme.typography.labelMedium,
                color = confidenceColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Format score string for display (e.g., "1-2" -> "1 - 2")
 */
private fun formatScore(score: String): String {
    return score.replace("-", " - ")
}

/**
 * Preview version of OracleHeroCard for testing.
 */
@Composable
fun OracleHeroCardPreview() {
    val sampleAnalysis = OracleAnalysis(
        prediction = "2-1",
        confidence = 75,
        reasoning = "City staat 5 plekken hoger dan Forest en heeft 15 punten meer.",
        homePowerScore = 85,
        awayPowerScore = 60,
        tesseract = TesseractResult(
            homeWinProbability = 0.62,
            drawProbability = 0.20,
            awayWinProbability = 0.18,
            mostLikelyScore = "2-1",
            simulationCount = 10000
        )
    )

    OracleHeroCard(
        analysis = sampleAnalysis,
        tesseractResult = sampleAnalysis.tesseract,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}
