package com.Lyno.matchmindai.presentation.components.detail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.Lyno.matchmindai.domain.model.TesseractResult
import com.Lyno.matchmindai.ui.theme.PrimaryNeon
import com.Lyno.matchmindai.ui.theme.SecondaryPurple
import com.Lyno.matchmindai.ui.theme.SurfaceCard
import kotlinx.coroutines.delay

/**
 * Animated horizontal segmented progress bar showing Monte Carlo distribution.
 * Displays home win, draw, and away win probabilities with cyber-minimalist styling.
 *
 * @param tesseractResult The Tesseract simulation result containing probabilities
 * @param modifier Modifier for the component
 * @param animationDuration Duration of the width animation in milliseconds
 */
@Composable
fun ProbabilityBar(
    tesseractResult: TesseractResult,
    modifier: Modifier = Modifier,
    animationDuration: Int = 800
) {
    // Animated widths for each segment
    val homeWidth by animateFloatAsState(
        targetValue = tesseractResult.homeWinProbability.toFloat(),
        animationSpec = tween(durationMillis = animationDuration),
        label = "homeWidth"
    )
    
    val drawWidth by animateFloatAsState(
        targetValue = tesseractResult.drawProbability.toFloat(),
        animationSpec = tween(durationMillis = animationDuration),
        label = "drawWidth"
    )
    
    val awayWidth by animateFloatAsState(
        targetValue = tesseractResult.awayWinProbability.toFloat(),
        animationSpec = tween(durationMillis = animationDuration),
        label = "awayWidth"
    )
    
    // Colors for each segment
    val homeColorStart = PrimaryNeon.copy(alpha = 0.9f)
    val homeColorEnd = Color(0xFF00B8D4).copy(alpha = 0.7f) // Cyan
    
    val drawColorStart = Color(0xFFB0BEC5).copy(alpha = 0.8f) // Grey
    val drawColorEnd = Color(0xFFFFFFFF).copy(alpha = 0.6f) // White
    
    val awayColorStart = Color(0xFFEF5350).copy(alpha = 0.8f) // Red
    val awayColorEnd = Color(0xFFEC407A).copy(alpha = 0.7f) // Magenta
    
    // Calculate percentages for labels
    val homePercentage = tesseractResult.homeWinPercentage
    val drawPercentage = tesseractResult.drawPercentage
    val awayPercentage = tesseractResult.awayWinPercentage
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Text(
            text = stringResource(R.string.win_probabilities_title),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Main progress bar container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceCard.copy(alpha = 0.3f))
                .drawWithCache {
                    // Add subtle grid lines for cyber aesthetic
                    onDrawWithContent {
                        drawContent()
                        // Draw vertical grid lines
                        val lineCount = 10
                        val lineWidth = 1f
                        val spacing = size.width / lineCount
                        
                        for (i in 1 until lineCount) {
                            val x = i * spacing
                            drawLine(
                                color = Color.White.copy(alpha = 0.1f),
                                start = Offset(x, 0f),
                                end = Offset(x, size.height),
                                strokeWidth = lineWidth
                            )
                        }
                    }
                }
        ) {
            // Home win segment (left)
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = homeWidth)
                    .fillMaxHeight()
                    .clip(
                        RoundedCornerShape(
                            topStart = 12.dp,
                            bottomStart = 12.dp,
                            topEnd = if (drawWidth == 0f && awayWidth == 0f) 12.dp else 0.dp,
                            bottomEnd = if (drawWidth == 0f && awayWidth == 0f) 12.dp else 0.dp
                        )
                    )
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(homeColorStart, homeColorEnd)
                        )
                    )
            ) {
                // Home win label inside segment
                if (homeWidth > 0.15f) { // Only show label if segment is wide enough
                    Text(
                        text = "${homePercentage}%",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 4.dp)
                    )
                }
            }
            
            // Draw segment (middle)
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = drawWidth)
                    .fillMaxHeight()
                    .offset(x = (homeWidth * 100).dp) // Position after home segment
                    .clip(
                        RoundedCornerShape(
                            topStart = if (homeWidth == 0f) 12.dp else 0.dp,
                            bottomStart = if (homeWidth == 0f) 12.dp else 0.dp,
                            topEnd = if (awayWidth == 0f) 12.dp else 0.dp,
                            bottomEnd = if (awayWidth == 0f) 12.dp else 0.dp
                        )
                    )
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(drawColorStart, drawColorEnd)
                        )
                    )
            ) {
                // Draw label inside segment
                if (drawWidth > 0.15f) { // Only show label if segment is wide enough
                    Text(
                        text = "${drawPercentage}%",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 4.dp)
                    )
                }
            }
            
            // Away win segment (right)
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = awayWidth)
                    .fillMaxHeight()
                    .offset(x = ((homeWidth + drawWidth) * 100).dp) // Position after home and draw segments
                    .clip(
                        RoundedCornerShape(
                            topStart = if (homeWidth == 0f && drawWidth == 0f) 12.dp else 0.dp,
                            bottomStart = if (homeWidth == 0f && drawWidth == 0f) 12.dp else 0.dp,
                            topEnd = 12.dp,
                            bottomEnd = 12.dp
                        )
                    )
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(awayColorStart, awayColorEnd)
                        )
                    )
            ) {
                // Away win label inside segment
                if (awayWidth > 0.15f) { // Only show label if segment is wide enough
                    Text(
                        text = "${awayPercentage}%",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 4.dp)
                    )
                }
            }
            
            // Segment dividers (only show if both adjacent segments have width)
            if (homeWidth > 0f && drawWidth > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(2.dp)
                        .offset(x = (homeWidth * 100).dp)
                        .background(Color.White.copy(alpha = 0.3f))
                )
            }
            
            if (drawWidth > 0f && awayWidth > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(2.dp)
                        .offset(x = ((homeWidth + drawWidth) * 100).dp)
                        .background(Color.White.copy(alpha = 0.3f))
                )
            }
        }
        
        // Legend labels below the bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Home win label
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(R.string.home_win_probability),
                    style = MaterialTheme.typography.labelSmall,
                    color = homeColorStart,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$homePercentage%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Draw label
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(R.string.draw_probability),
                    style = MaterialTheme.typography.labelSmall,
                    color = drawColorStart,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$drawPercentage%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Away win label
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(R.string.away_win_probability),
                    style = MaterialTheme.typography.labelSmall,
                    color = awayColorStart,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$awayPercentage%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // Most likely score indicator
        var showScorePulse by remember { mutableStateOf(false) }
        
        LaunchedEffect(Unit) {
            // Pulsing animation for the score indicator
            while (true) {
                showScorePulse = true
                delay(2000)
                showScorePulse = false
                delay(1000)
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.most_likely_score, tesseractResult.mostLikelyScore),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.Center)
            )
            
            // Pulsing dot animation
            if (showScorePulse) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .align(Alignment.CenterEnd)
                        .background(
                            color = PrimaryNeon.copy(alpha = 0.7f),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                )
            }
        }
    }
}

/**
 * Preview version of ProbabilityBar for testing.
 */
@Composable
fun ProbabilityBarPreview() {
    val sampleTesseractResult = TesseractResult(
        homeWinProbability = 0.62,
        drawProbability = 0.20,
        awayWinProbability = 0.18,
        mostLikelyScore = "2-1",
        simulationCount = 10000
    )
    
    ProbabilityBar(
        tesseractResult = sampleTesseractResult,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}
