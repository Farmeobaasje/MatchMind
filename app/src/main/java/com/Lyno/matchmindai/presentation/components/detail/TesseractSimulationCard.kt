package com.Lyno.matchmindai.presentation.components.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataArray
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Lyno.matchmindai.R
import com.Lyno.matchmindai.domain.model.TesseractResult
import com.Lyno.matchmindai.ui.theme.PrimaryNeon
import com.Lyno.matchmindai.ui.theme.SecondaryPurple
import com.Lyno.matchmindai.ui.theme.SurfaceCard

/**
 * TesseractSimulationCard - Separate card for Tesseract simulation results.
 * Shows the most frequent score from 10k Monte Carlo simulations.
 * 
 * Design: Glassmorphism with subtle border to distinguish from Oracle card.
 * 
 * @param tesseractResult The Tesseract simulation results
 * @param modifier Modifier for the component
 */
@Composable
fun TesseractSimulationCard(
    tesseractResult: TesseractResult,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(1.dp, SecondaryPurple.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            SurfaceCard.copy(alpha = 0.2f),
                            SurfaceCard.copy(alpha = 0.1f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with Tesseract icon and title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DataArray,
                        contentDescription = "Tesseract Simulation",
                        tint = SecondaryPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(R.string.tesseract_simulation_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = SecondaryPurple,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Simulation info
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.tesseract_simulation_description, tesseractResult.simulationCount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    
                    Text(
                        text = stringResource(R.string.tesseract_simulation_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }

                // Most frequent score display
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.tesseract_most_frequent_score),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceCard.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = formatScore(tesseractResult.mostLikelyScore),
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryPurple,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Probability breakdown
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.tesseract_probability_breakdown),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProbabilityItem(
                            label = stringResource(R.string.home_win_dutch),
                            probability = tesseractResult.homeWinProbability,
                            color = PrimaryNeon
                        )
                        
                        ProbabilityItem(
                            label = stringResource(R.string.draw_dutch),
                            probability = tesseractResult.drawProbability,
                            color = Color(0xFFFFA726) // Orange
                        )
                        
                        ProbabilityItem(
                            label = stringResource(R.string.away_win_dutch),
                            probability = tesseractResult.awayWinProbability,
                            color = SecondaryPurple
                        )
                    }
                }

                // Goal market probabilities
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.tesseract_goal_markets),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GoalMarketItem(
                            label = stringResource(R.string.over_2_5),
                            probability = tesseractResult.over2_5Probability,
                            color = PrimaryNeon
                        )
                        
                        GoalMarketItem(
                            label = stringResource(R.string.under_2_5),
                            probability = tesseractResult.under2_5Probability,
                            color = SecondaryPurple
                        )
                        
                        GoalMarketItem(
                            label = stringResource(R.string.btts_yes),
                            probability = tesseractResult.bttsProbability,
                            color = Color(0xFFFFA726) // Orange
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProbabilityItem(
    label: String,
    probability: Double,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Surface(
            shape = MaterialTheme.shapes.small,
            color = color.copy(alpha = 0.2f),
            border = BorderStroke(1.dp, color)
        ) {
            Text(
                text = "${(probability * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun GoalMarketItem(
    label: String,
    probability: Double,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Surface(
            shape = MaterialTheme.shapes.small,
            color = color.copy(alpha = 0.2f),
            border = BorderStroke(1.dp, color)
        ) {
            Text(
                text = "${(probability * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

private fun formatScore(score: String): String {
    return score.replace("-", " - ")
}

/**
 * Preview function for TesseractSimulationCard.
 */
@Composable
fun TesseractSimulationCardPreview() {
    val sampleTesseract = TesseractResult(
        homeWinProbability = 0.62,
        drawProbability = 0.20,
        awayWinProbability = 0.18,
        mostLikelyScore = "1-1",
        simulationCount = 10000,
        over2_5Probability = 0.45,
        bttsProbability = 0.52
    )
    
    TesseractSimulationCard(
        tesseractResult = sampleTesseract,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}
