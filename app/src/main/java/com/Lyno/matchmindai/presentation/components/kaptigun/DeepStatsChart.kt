package com.Lyno.matchmindai.presentation.components.kaptigun

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.Lyno.matchmindai.domain.model.DeepStatsComparison
import kotlin.math.max

/**
 * Card displaying deep statistics comparison with horizontal bar charts.
 * Shows xG, possession, shots on target, PPDA, and sentiment analysis.
 * 
 * @param deepStats Historical averages for comparison
 * @param singleMatchStats Optional actual match statistics (for History Detail)
 * @param homeTeamName Name of the home team
 * @param awayTeamName Name of the away team
 * @param modifier Modifier for styling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeepStatsChart(
    deepStats: DeepStatsComparison,
    singleMatchStats: DeepStatsComparison? = null,
    homeTeamName: String,
    awayTeamName: String,
    modifier: Modifier = Modifier
) {
    val isSingleMatchMode = singleMatchStats != null
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Diepgaande Statistieken",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = if (isSingleMatchMode) "ACTUELE WEDSTRIJD" else "Gemiddelden laatste 5 wedstrijden",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Team names
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = homeTeamName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start
                )
                
                Text(
                    text = awayTeamName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Statistics charts - use single match stats if available
            val statsToUse = singleMatchStats ?: deepStats
            
            StatisticChart(
                label = "xG per wedstrijd",
                homeValue = statsToUse.avgXg.first,
                awayValue = statsToUse.avgXg.second,
                maxValue = max(statsToUse.avgXg.first, statsToUse.avgXg.second) * 1.2,
                unit = "",
                homeColor = MaterialTheme.colorScheme.primary,
                awayColor = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            StatisticChart(
                label = "Balbezit (%)",
                homeValue = statsToUse.avgPossession.first,
                awayValue = statsToUse.avgPossession.second,
                maxValue = 100.0,
                unit = "%",
                homeColor = MaterialTheme.colorScheme.primary,
                awayColor = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            StatisticChart(
                label = "Schoten op doel",
                homeValue = statsToUse.avgShotsOnTarget.first,
                awayValue = statsToUse.avgShotsOnTarget.second,
                maxValue = max(statsToUse.avgShotsOnTarget.first, statsToUse.avgShotsOnTarget.second) * 1.2,
                unit = "",
                homeColor = MaterialTheme.colorScheme.primary,
                awayColor = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            StatisticChart(
                label = "PPDA (lager = beter)",
                homeValue = statsToUse.ppda.first,
                awayValue = statsToUse.ppda.second,
                maxValue = max(statsToUse.ppda.first, statsToUse.ppda.second) * 1.2,
                unit = "",
                homeColor = MaterialTheme.colorScheme.primary,
                awayColor = MaterialTheme.colorScheme.secondary,
                reverseComparison = true // Lower is better
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Sentiment meter (simplified)
            SentimentMeter(
                homeSentiment = statsToUse.sentimentScore.first,
                awaySentiment = statsToUse.sentimentScore.second,
                homeTeamName = homeTeamName,
                awayTeamName = awayTeamName
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem(
                    color = MaterialTheme.colorScheme.primary,
                    text = "Thuis"
                )
                LegendItem(
                    color = MaterialTheme.colorScheme.secondary,
                    text = "Uit"
                )
                LegendItem(
                    color = Color(0xFF4CAF50),
                    text = "Positief"
                )
                LegendItem(
                    color = Color(0xFFF44336),
                    text = "Negatief"
                )
            }
        }
    }
}

@Composable
private fun StatisticChart(
    label: String,
    homeValue: Double,
    awayValue: Double,
    maxValue: Double,
    unit: String,
    homeColor: Color,
    awayColor: Color,
    reverseComparison: Boolean = false
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Label
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Chart container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                val width = size.width
                val height = size.height
                val centerX = width / 2
                
                // Center line
                drawLine(
                    color = Color.Gray.copy(alpha = 0.3f),
                    start = Offset(centerX, 0f),
                    end = Offset(centerX, height),
                    strokeWidth = 1.dp.toPx()
                )
                
                // Home bar (left side)
                val homePercentage = (homeValue / maxValue).toFloat().coerceIn(0f, 1f)
                val homeBarWidth = (width / 2) * homePercentage
                
                drawRect(
                    color = homeColor.copy(alpha = 0.7f),
                    topLeft = Offset(centerX - homeBarWidth, 20f),
                    size = Size(homeBarWidth, 20f)
                )
                
                // Away bar (right side)
                val awayPercentage = (awayValue / maxValue).toFloat().coerceIn(0f, 1f)
                val awayBarWidth = (width / 2) * awayPercentage
                
                drawRect(
                    color = awayColor.copy(alpha = 0.7f),
                    topLeft = Offset(centerX, 20f),
                    size = Size(awayBarWidth, 20f)
                )
                
                // Home value text (simplified - just show value below bar)
                // We'll show the values in a separate row below
            }
            
            // Value labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = String.format("%.1f%s", homeValue, unit),
                    style = MaterialTheme.typography.labelSmall,
                    color = homeColor,
                    modifier = Modifier.padding(start = 8.dp)
                )
                
                Text(
                    text = String.format("%.1f%s", awayValue, unit),
                    style = MaterialTheme.typography.labelSmall,
                    color = awayColor,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            
            // Comparison indicator
            if (homeValue > 0 && awayValue > 0) {
                val isHomeBetter = if (reverseComparison) homeValue < awayValue else homeValue > awayValue
                val indicatorColor = if (isHomeBetter) homeColor else awayColor
                
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(16.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = indicatorColor,
                            center = Offset(size.width / 2, size.height / 2),
                            radius = 8.dp.toPx()
                        )
                    }
                    
                    Text(
                        text = if (isHomeBetter) "↑" else "↓",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
private fun SentimentMeter(
    homeSentiment: Double,
    awaySentiment: Double,
    homeTeamName: String,
    awayTeamName: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Label
        Text(
            text = "Team Sentiment (-1.0 tot +1.0)",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Simple sentiment indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SentimentIndicator(
                teamName = homeTeamName,
                sentiment = homeSentiment,
                isHome = true
            )
            
            SentimentIndicator(
                teamName = awayTeamName,
                sentiment = awaySentiment,
                isHome = false
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Sentiment scale
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "CHAOS",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFF44336)
            )
            
            Text(
                text = "NEUTRAAL",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Text(
                text = "EUFORIE",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF4CAF50)
            )
        }
    }
}

@Composable
private fun SentimentIndicator(
    teamName: String,
    sentiment: Double,
    isHome: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Sentiment value with color
        val sentimentColor = when {
            sentiment >= 0.5 -> Color(0xFF4CAF50) // Strong positive
            sentiment >= 0.2 -> Color(0xFF8BC34A) // Positive
            sentiment <= -0.5 -> Color(0xFFF44336) // Strong negative
            sentiment <= -0.2 -> Color(0xFFFF9800) // Negative
            else -> Color.Gray // Neutral
        }
        
        Text(
            text = String.format("%.2f", sentiment),
            style = MaterialTheme.typography.titleSmall,
            color = sentimentColor,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = teamName,
            style = MaterialTheme.typography.labelSmall,
            color = if (isHome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
        )
        
        // Sentiment indicator bar
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(8.dp)
                .padding(top = 4.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Background
                drawRect(
                    color = Color.Gray.copy(alpha = 0.2f),
                    size = size
                )
                
                // Sentiment fill
                val fillWidth = size.width * ((sentiment + 1) / 2).toFloat().coerceIn(0f, 1f)
                drawRect(
                    color = sentimentColor,
                    size = Size(fillWidth, size.height)
                )
                
                // Center line
                drawLine(
                    color = Color.Gray,
                    start = Offset(size.width / 2, 0f),
                    end = Offset(size.width / 2, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = CircleShape)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
