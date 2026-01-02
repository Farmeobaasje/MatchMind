package com.Lyno.matchmindai.presentation.components.detail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.NoiseAware
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Lyno.matchmindai.R
import com.Lyno.matchmindai.domain.model.SimulationContext
import com.Lyno.matchmindai.ui.theme.PrimaryNeon
import com.Lyno.matchmindai.ui.theme.SurfaceCard
import kotlin.random.Random

/**
 * Grid displaying SimulationContext metrics with animated visual indicators.
 * Shows fitness meters and distraction indices for both teams in a cyber-minimalist style.
 *
 * @param simulationContext The simulation context containing fitness and distraction data
 * @param modifier Modifier for the component
 * @param animationDuration Duration of the animation in milliseconds
 */
@Composable
fun ContextGrid(
    simulationContext: SimulationContext,
    modifier: Modifier = Modifier,
    animationDuration: Int = 800
) {
    // Animated values for fitness meters
    val homeFitnessProgress by animateFloatAsState(
        targetValue = simulationContext.homeFitness / 100f,
        animationSpec = tween(durationMillis = animationDuration),
        label = "homeFitnessProgress"
    )
    
    val awayFitnessProgress by animateFloatAsState(
        targetValue = simulationContext.awayFitness / 100f,
        animationSpec = tween(durationMillis = animationDuration),
        label = "awayFitnessProgress"
    )
    
    // Animated values for distraction meters
    val homeDistractionProgress by animateFloatAsState(
        targetValue = simulationContext.homeDistraction / 100f,
        animationSpec = tween(durationMillis = animationDuration),
        label = "homeDistractionProgress"
    )
    
    val awayDistractionProgress by animateFloatAsState(
        targetValue = simulationContext.awayDistraction / 100f,
        animationSpec = tween(durationMillis = animationDuration),
        label = "awayDistractionProgress"
    )
    
    // Determine fitness status and color
    val (homeFitnessStatus, homeFitnessColor) = getFitnessStatus(simulationContext.homeFitness)
    val (awayFitnessStatus, awayFitnessColor) = getFitnessStatus(simulationContext.awayFitness)
    
    // Determine distraction status and color
    val (homeDistractionStatus, homeDistractionColor) = getDistractionStatus(simulationContext.homeDistraction)
    val (awayDistractionStatus, awayDistractionColor) = getDistractionStatus(simulationContext.awayDistraction)
    
    // Calculate summary flags
    val hasLowFitness = simulationContext.homeFitness < 70 || simulationContext.awayFitness < 70
    val hasHighDistraction = simulationContext.homeDistraction > 60 || simulationContext.awayDistraction > 60
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = stringResource(R.string.tesseract_monte_carlo_title),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        
        // 2x2 Grid
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Row 1: Team Fitness
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Home Fitness Meter
                FitnessMeter(
                    label = stringResource(R.string.home_fitness_label),
                    value = simulationContext.homeFitness,
                    progress = homeFitnessProgress,
                    status = homeFitnessStatus,
                    color = homeFitnessColor,
                    modifier = Modifier.weight(1f)
                )
                
                // Away Fitness Meter
                FitnessMeter(
                    label = stringResource(R.string.away_fitness_label),
                    value = simulationContext.awayFitness,
                    progress = awayFitnessProgress,
                    status = awayFitnessStatus,
                    color = awayFitnessColor,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Row 2: Distraction Index
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Home Distraction Meter
                DistractionMeter(
                    label = stringResource(R.string.home_distraction_label),
                    value = simulationContext.homeDistraction,
                    progress = homeDistractionProgress,
                    status = homeDistractionStatus,
                    color = homeDistractionColor,
                    modifier = Modifier.weight(1f)
                )
                
                // Away Distraction Meter
                DistractionMeter(
                    label = stringResource(R.string.away_distraction_label),
                    value = simulationContext.awayDistraction,
                    progress = awayDistractionProgress,
                    status = awayDistractionStatus,
                    color = awayDistractionColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Summary indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Fitness summary
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceCard.copy(alpha = 0.3f))
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = "Fitness",
                        tint = if (hasLowFitness) Color(0xFFFFA726) else PrimaryNeon,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (hasLowFitness) 
                            stringResource(R.string.fitness_warning) 
                        else 
                            stringResource(R.string.fitness_good),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (hasLowFitness) Color(0xFFFFA726) else PrimaryNeon,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Distraction summary
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceCard.copy(alpha = 0.3f))
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NoiseAware,
                        contentDescription = "Distraction",
                        tint = if (hasHighDistraction) Color(0xFFEF5350) else Color(0xFF4CAF50),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (hasHighDistraction)
                            stringResource(R.string.distraction_critical)
                        else
                            stringResource(R.string.distraction_focus),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (hasHighDistraction) Color(0xFFEF5350) else Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Circular fitness meter with animated progress.
 */
@Composable
private fun FitnessMeter(
    label: String,
    value: Int,
    progress: Float,
    status: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard.copy(alpha = 0.3f))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Circular progress indicator
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background circle
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    drawCircle(
                        color = SurfaceCard.copy(alpha = 0.5f),
                        style = Stroke(width = 8f, cap = StrokeCap.Round)
                    )
                    
                    // Animated progress arc
                    drawArc(
                        color = color,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        style = Stroke(width = 8f, cap = StrokeCap.Round)
                    )
                    
                    // Add subtle glow effect
                    drawCircle(
                        color = color.copy(alpha = 0.2f),
                        radius = size.minDimension / 2 - 4f
                    )
                }
                
                // Value in center
                Text(
                    text = "$value",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            
            // Label and status
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodySmall,
                    color = color,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
            
            // Fitness icon
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = "Fitness",
                tint = color,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Horizontal distraction meter with animated bar.
 */
@Composable
private fun DistractionMeter(
    label: String,
    value: Int,
    progress: Float,
    status: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard.copy(alpha = 0.3f))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Label and value
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "$value",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            
            // Animated progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceCard.copy(alpha = 0.5f))
                    .drawWithCache {
                        // Add noise pattern for distraction effect
                        onDrawWithContent {
                            drawContent()
                            if (value > 50) {
                                // Draw random noise dots for high distraction
                                val dotCount = (value / 10).coerceAtMost(20)
                                repeat(dotCount) {
                                    val x = Random.nextFloat() * size.width
                                    val y = Random.nextFloat() * size.height
                                    val dotSize = 1f + Random.nextFloat() * 2f // 1-3 range
                                    drawCircle(
                                        color = Color.White.copy(alpha = 0.3f),
                                        center = Offset(x, y),
                                        radius = dotSize
                                    )
                                }
                            }
                        }
                    }
            ) {
                // Progress fill
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    color.copy(alpha = 0.7f),
                                    color.copy(alpha = 0.9f)
                                )
                            )
                        )
                )
                
                // Threshold markers
                if (value > 30) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = 0.3f)
                            .fillMaxHeight()
                            .background(Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(2.dp)
                                .align(Alignment.CenterEnd)
                                .background(Color.Yellow.copy(alpha = 0.5f))
                        )
                    }
                }
                
                if (value > 60) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = 0.6f)
                            .fillMaxHeight()
                            .background(Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(2.dp)
                                .align(Alignment.CenterEnd)
                                .background(Color.Red.copy(alpha = 0.5f))
                        )
                    }
                }
            }
            
            // Status and icon row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodySmall,
                    color = color,
                    fontWeight = FontWeight.Medium
                )
                
                Icon(
                    imageVector = if (value > 60) Icons.Default.Warning else Icons.Default.CheckCircle,
                    contentDescription = "Distraction Level",
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Determine fitness status and color based on fitness value.
 */
@Composable
private fun getFitnessStatus(fitness: Int): Pair<String, Color> {
    return when {
        fitness >= 90 -> Pair(stringResource(R.string.fitness_excellent), Color(0xFF4CAF50)) // Green
        fitness >= 80 -> Pair(stringResource(R.string.fitness_good), Color(0xFF8BC34A)) // Light Green
        fitness >= 70 -> Pair(stringResource(R.string.fitness_warning), Color(0xFFFFA726)) // Orange
        else -> Pair(stringResource(R.string.fitness_poor), Color(0xFFEF5350)) // Red
    }
}

/**
 * Determine distraction status and color based on distraction value.
 */
@Composable
private fun getDistractionStatus(distraction: Int): Pair<String, Color> {
    return when {
        distraction <= 30 -> Pair(stringResource(R.string.distraction_focus), Color(0xFF4CAF50)) // Green
        distraction <= 60 -> Pair(stringResource(R.string.distraction_some), Color(0xFFFFA726)) // Orange
        else -> Pair(stringResource(R.string.distraction_critical), Color(0xFFEF5350)) // Red
    }
}

/**
 * Preview version of ContextGrid for testing.
 */
@Preview
@Composable
fun ContextGridPreview() {
    val sampleSimulationContext = SimulationContext(
        homeDistraction = 25,
        awayDistraction = 65,
        homeFitness = 85,
        awayFitness = 72
    )
    
    ContextGrid(
        simulationContext = sampleSimulationContext,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}
