package com.Lyno.matchmindai.presentation.components.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

/**
 * EvidenceFaceOffCard - Shows Oracle vs Tesseract side-by-side comparison.
 * Visual "face-off" between AI prediction (Oracle) and statistical simulation (Tesseract).
 * 
 * Design: Two glassmorphism cards in a Row, with a central "VS" indicator.
 * Each card shows:
 * - System name (Oracle/Tesseract)
 * - Predicted score (large text)
 * - Confidence/probability
 * - Brief description
 */
@Composable
fun EvidenceFaceOffCard(
    oracleAnalysis: OracleAnalysis?,
    tesseractResult: TesseractResult?,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "Evidence Face-Off",
                    tint = PrimaryNeon,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "DE BEWIJSSTUKKEN",
                    style = MaterialTheme.typography.labelLarge,
                    color = PrimaryNeon,
                    fontWeight = FontWeight.Bold
                )
            }

            // Subtitle
            Text(
                text = "Oracle (AI) vs Tesseract (Stats) - Wie heeft gelijk?",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Face-off Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Oracle Card
                EvidenceCard(
                    systemName = "ORACLE",
                    systemIcon = Icons.Default.Psychology,
                    score = oracleAnalysis?.prediction ?: "? - ?",
                    confidence = oracleAnalysis?.confidence ?: 0,
                    description = "AI-gedreven voorspelling",
                    isOracle = true,
                    modifier = Modifier.weight(1f)
                )

                // VS Divider
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = "VS",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Face-Off",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Tesseract Card
                EvidenceCard(
                    systemName = "TESSERACT",
                    systemIcon = Icons.Default.Timeline,
                    score = tesseractResult?.mostLikelyScore ?: "? - ?",
                    confidence = ((tesseractResult?.homeWinProbability ?: 0.0) * 100).toInt(),
                    description = "10k Monte Carlo simulaties",
                    isOracle = false,
                    modifier = Modifier.weight(1f)
                )
            }

            // Comparison Summary
            when {
                oracleAnalysis != null && tesseractResult != null -> {
                    val oracleConfidence = oracleAnalysis.confidence
                    val tesseractConfidence = (tesseractResult.homeWinProbability * 100).toInt()
                    
                    val (summaryText, summaryColor) = when {
                        oracleConfidence >= 70 && tesseractConfidence >= 70 -> Pair(
                            "✅ Sterke overeenstemming! Beide systemen voorspellen hetzelfde.",
                            Color(0xFF4CAF50) // Green
                        )
                        Math.abs(oracleConfidence - tesseractConfidence) <= 15 -> Pair(
                            "⚖️ Matige overeenstemming. Kleine verschillen in zekerheid.",
                            Color(0xFFFFA726) // Orange
                        )
                        else -> Pair(
                            "⚠️ Tegenstrijdige signalen. Extra voorzichtigheid vereist.",
                            Color(0xFFEF5350) // Red
                        )
                    }
                    
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = summaryColor.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, summaryColor)
                    ) {
                        Text(
                            text = summaryText,
                            style = MaterialTheme.typography.labelMedium,
                            color = summaryColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        )
                    }
                }
                
                oracleAnalysis != null -> {
                    // Only Oracle data available
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = PrimaryNeon.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, PrimaryNeon)
                    ) {
                        Text(
                            text = "📊 Wacht op Tesseract simulaties voor volledige vergelijking",
                            style = MaterialTheme.typography.labelMedium,
                            color = PrimaryNeon,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        )
                    }
                }
                
                tesseractResult != null -> {
                    // Only Tesseract data available
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = SecondaryPurple.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, SecondaryPurple)
                    ) {
                        Text(
                            text = "🤖 Wacht op Oracle AI-analyse voor volledige vergelijking",
                            style = MaterialTheme.typography.labelMedium,
                            color = SecondaryPurple,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        )
                    }
                }
                
                else -> {
                    // No data available
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant)
                    ) {
                        Text(
                            text = "⏳ Wacht op data van beide systemen",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual evidence card for Oracle or Tesseract.
 */
@Composable
private fun EvidenceCard(
    systemName: String,
    systemIcon: androidx.compose.ui.graphics.vector.ImageVector,
    score: String,
    confidence: Int,
    description: String,
    isOracle: Boolean,
    modifier: Modifier = Modifier
) {
    val cardColor = if (isOracle) PrimaryNeon else SecondaryPurple
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(1.dp, cardColor.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // System header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = systemIcon,
                    contentDescription = systemName,
                    tint = cardColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = systemName,
                    style = MaterialTheme.typography.labelSmall,
                    color = cardColor,
                    fontWeight = FontWeight.Bold
                )
            }

            // Score (large)
            Text(
                text = score,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = cardColor,
                textAlign = TextAlign.Center
            )

            // Confidence badge
            Surface(
                shape = MaterialTheme.shapes.small,
                color = cardColor.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, cardColor)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (isOracle) "Zekerheid:" else "Kans:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$confidence%",
                        style = MaterialTheme.typography.labelMedium,
                        color = cardColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Description
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Preview function for EvidenceFaceOffCard.
 */
@Composable
fun EvidenceFaceOffCardPreview() {
    val sampleOracle = OracleAnalysis(
        prediction = "2-1",
        confidence = 75,
        reasoning = "City staat 5 plekken hoger dan Forest en heeft 15 punten meer.",
        homePowerScore = 85,
        awayPowerScore = 60,
        tesseract = null
    )
    
    val sampleTesseract = TesseractResult(
        homeWinProbability = 0.62,
        drawProbability = 0.20,
        awayWinProbability = 0.18,
        mostLikelyScore = "2-1",
        simulationCount = 10000,
        bttsProbability = 0.55,
        over2_5Probability = 0.65,
        topScoreDistribution = listOf(Pair("2-1", 1200), Pair("1-1", 800), Pair("2-0", 600))
    )
    
    EvidenceFaceOffCard(
        oracleAnalysis = sampleOracle,
        tesseractResult = sampleTesseract,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}
