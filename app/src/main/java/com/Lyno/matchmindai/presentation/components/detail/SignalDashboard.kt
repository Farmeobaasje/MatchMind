package com.Lyno.matchmindai.presentation.components.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.Lyno.matchmindai.R
import com.Lyno.matchmindai.domain.model.SimulationContext
import com.Lyno.matchmindai.presentation.components.GlassCard
import com.Lyno.matchmindai.ui.theme.PrimaryNeon
import com.Lyno.matchmindai.ui.theme.SurfaceCard

/**
 * SignalDashboard - Enhanced visual grid with expandable ChiChi team analysis.
 * Combines ContextGrid with AI summary, status badges, and detailed team analysis.
 *
 * @param simulationContext The simulation context containing fitness and distraction data
 * @param aiReasoning Optional AI reasoning text from SimulationContext
 * @param homeTeamName Name of the home team for detailed analysis
 * @param awayTeamName Name of the away team for detailed analysis
 * @param onRefresh Callback for refreshing ChiChi simulation context
 * @param isRefreshing Whether ChiChi refresh is in progress
 * @param modifier Modifier for the component
 */
@Composable
fun SignalDashboard(
    simulationContext: SimulationContext,
    aiReasoning: String? = null,
    homeTeamName: String = "Home Team",
    awayTeamName: String = "Away Team",
    onRefresh: () -> Unit = {},
    isRefreshing: Boolean = false,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showDetails by remember { mutableStateOf(false) }
    
    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header with expandable toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.signal_dashboard_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = PrimaryNeon,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Status indicator based on fitness and distraction
                    val statusBadge = getStatusBadge(simulationContext)
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = statusBadge.color.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, statusBadge.color)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = statusBadge.icon,
                                contentDescription = "Status",
                                tint = statusBadge.color,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = statusBadge.text,
                                style = MaterialTheme.typography.labelSmall,
                                color = statusBadge.color,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                // Expand/collapse button
                IconButton(
                    onClick = { showDetails = !showDetails },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (showDetails) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = if (showDetails) "Toon minder" else "Toon details",
                        tint = PrimaryNeon
                    )
                }
            }
            
            // Key Insights Row (NEW!)
            KeyInsightsRow(simulationContext = simulationContext)
            
            // Row 1: Fitness & Distraction Gauges
            ContextGrid(
                simulationContext = simulationContext,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Row 2: Detailed Team Analysis (Expandable)
            if (showDetails) {
                AnimatedVisibility(
                    visible = showDetails,
                    enter = expandVertically(animationSpec = tween(300)),
                    exit = shrinkVertically(animationSpec = tween(300))
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Team Analysis Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = "Team Analysis",
                                tint = PrimaryNeon,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "CHICHI TEAM ANALYSE",
                                style = MaterialTheme.typography.labelMedium,
                                color = PrimaryNeon,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // Home Team Analysis
                        TeamAnalysisRow(
                            teamName = homeTeamName,
                            distractionScore = simulationContext.homeDistraction,
                            fitnessScore = simulationContext.homeFitness,
                            isHomeTeam = true
                        )
                        
                        // Away Team Analysis
                        TeamAnalysisRow(
                            teamName = awayTeamName,
                            distractionScore = simulationContext.awayDistraction,
                            fitnessScore = simulationContext.awayFitness,
                            isHomeTeam = false
                        )
                        
                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                android.util.Log.d("DEBUG_CHICHI", "Knop ingedrukt in UI - SignalDashboard")
                                onRefresh()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = PrimaryNeon
                            ),
                            enabled = !isRefreshing
                        ) {
                            if (isRefreshing) {
                                CircularProgressIndicator(
                                    color = PrimaryNeon,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Text(
                                    text = "Vernieuw ChiChi",
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                            
                            OutlinedButton(
                                onClick = { /* TODO: Navigate to Analysis tab */ },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = PrimaryNeon
                                )
                            ) {
                                Text(
                                    text = "Bekijk Analyse",
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
            
            // Row 3: AI Analyse Summary
            if (!aiReasoning.isNullOrBlank()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.ai_analyze_summary),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // AI validation indicator
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = Color(0xFF4CAF50).copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, Color(0xFF4CAF50))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "AI Validated",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "AI VALIDATED",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF4CAF50),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    // Truncated reasoning with expandable section
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceCard.copy(alpha = 0.3f))
                            .padding(16.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = if (expanded || aiReasoning.length <= 150) {
                                    aiReasoning
                                } else {
                                    aiReasoning.take(150) + "..."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                            )
                            
                            if (aiReasoning.length > 150) {
                                TextButton(
                                    onClick = { expanded = !expanded },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text(
                                        text = if (expanded) "Toon minder" else stringResource(R.string.read_more),
                                        color = PrimaryNeon,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Summary indicators row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Fitness summary
                SummaryIndicator(
                    title = stringResource(R.string.fitness_label),
                    value = (simulationContext.homeFitness + simulationContext.awayFitness) / 2,
                    threshold = 70,
                    goodColor = Color(0xFF4CAF50),
                    warningColor = Color(0xFFFFA726),
                    icon = Icons.Default.FitnessCenter,
                    modifier = Modifier.weight(1f)
                )
                
                // Distraction summary
                SummaryIndicator(
                    title = stringResource(R.string.distraction_label),
                    value = (simulationContext.homeDistraction + simulationContext.awayDistraction) / 2,
                    threshold = 40,
                    goodColor = Color(0xFF4CAF50),
                    warningColor = Color(0xFFEF5350),
                    icon = Icons.Default.Warning,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Status badge data class.
 */
private data class StatusBadge(
    val text: String,
    val color: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

/**
 * Determine status badge based on fitness and distraction values.
 */
@Composable
private fun getStatusBadge(simulationContext: SimulationContext): StatusBadge {
    val hasLowDistraction = simulationContext.homeDistraction < 30 && simulationContext.awayDistraction < 30
    val hasHighFitness = simulationContext.homeFitness > 90 || simulationContext.awayFitness > 90
    
    return when {
        hasLowDistraction -> StatusBadge(
            text = stringResource(R.string.status_calm_preparation),
            color = Color(0xFF4CAF50), // Green
            icon = Icons.Default.CheckCircle
        )
        hasHighFitness -> StatusBadge(
            text = stringResource(R.string.status_topfit_selection),
            color = PrimaryNeon, // Neon Green
            icon = Icons.Default.FitnessCenter
        )
        else -> StatusBadge(
            text = stringResource(R.string.status_attention_points),
            color = Color(0xFFFFA726), // Orange
            icon = Icons.Default.Warning
        )
    }
}

/**
 * Key Insights Row - Shows quick insights based on simulation context.
 */
@Composable
private fun KeyInsightsRow(
    simulationContext: SimulationContext,
    modifier: Modifier = Modifier
) {
    val insights = mutableListOf<String>()
    
    // Analyze fitness insights
    when {
        simulationContext.homeFitness >= 90 && simulationContext.awayFitness >= 90 -> 
            insights.add("⚡ Beide teams in topvorm")
        simulationContext.homeFitness >= 90 -> 
            insights.add("🏠 Thuisteam in topvorm")
        simulationContext.awayFitness >= 90 -> 
            insights.add("✈️ Uitteam in topvorm")
        simulationContext.homeFitness < 70 || simulationContext.awayFitness < 70 -> 
            insights.add("⚠️ Fitheidsproblemen gedetecteerd")
    }
    
    // Analyze distraction insights
    when {
        simulationContext.homeDistraction <= 30 && simulationContext.awayDistraction <= 30 -> 
            insights.add("🧘 Beide teams volledig gefocust")
        simulationContext.homeDistraction <= 30 -> 
            insights.add("🏠 Thuisteam volledig gefocust")
        simulationContext.awayDistraction <= 30 -> 
            insights.add("✈️ Uitteam volledig gefocust")
        simulationContext.homeDistraction > 60 || simulationContext.awayDistraction > 60 -> 
            insights.add("🚨 Hoge afleiding gedetecteerd")
    }
    
    // Analyze comparative insights
    if (simulationContext.homeDistraction > simulationContext.awayDistraction + 20) {
        insights.add("🏠 Thuisteam meer afgeleid")
    } else if (simulationContext.awayDistraction > simulationContext.homeDistraction + 20) {
        insights.add("✈️ Uitteam meer afgeleid")
    }
    
    if (simulationContext.homeFitness < simulationContext.awayFitness - 20) {
        insights.add("🏠 Thuisteam minder fit")
    } else if (simulationContext.awayFitness < simulationContext.homeFitness - 20) {
        insights.add("✈️ Uitteam minder fit")
    }
    
    if (insights.isNotEmpty()) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = SurfaceCard.copy(alpha = 0.3f),
            border = BorderStroke(1.dp, PrimaryNeon.copy(alpha = 0.3f)),
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Key Insights",
                        tint = PrimaryNeon,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "KEY INSIGHTS",
                        style = MaterialTheme.typography.labelSmall,
                        color = PrimaryNeon,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    insights.take(3).forEach { insight ->
                        Text(
                            text = "• $insight",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                        )
                    }
                }
            }
        }
    }
}

/**
 * Team Analysis Row - Reusable row for team analysis (from TeamAnalysisCard).
 */
@Composable
private fun TeamAnalysisRow(
    teamName: String,
    distractionScore: Int,
    fitnessScore: Int,
    isHomeTeam: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Team header
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isHomeTeam) "🏠" else "✈️",
                modifier = Modifier.width(24.dp)
            )
            Text(
                text = teamName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Distraction Score
        ScoreProgressBar(
            label = "AFLEIDING",
            score = distractionScore,
            color = getDistractionColor(distractionScore),
            description = getDistractionDescription(distractionScore)
        )
        
        // Fitness Score
        ScoreProgressBar(
            label = "FITHEID",
            score = fitnessScore,
            color = getFitnessColor(fitnessScore),
            description = getFitnessDescription(fitnessScore)
        )
    }
}

/**
 * Progress bar for displaying a score with color coding.
 */
@Composable
private fun ScoreProgressBar(
    label: String,
    score: Int,
    color: Color,
    description: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            modifier = Modifier.width(80.dp)
        )
        
        // Progress bar background
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .fillMaxWidth()
        ) {
            // Background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small
                    ),
                contentAlignment = Alignment.CenterStart
            ) {
                // Progress fill
                Box(
                    modifier = Modifier
                        .fillMaxWidth(score / 100f)
                        .height(8.dp)
                        .background(
                            color = color,
                            shape = MaterialTheme.shapes.small
                        ),
                    contentAlignment = Alignment.CenterStart
                ) {
                    // Empty content - just for background
                }
            }
        }
        
        Text(
            text = "$score/100",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.width(50.dp)
        )
    }
    
    // Description
    Text(
        text = description,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.padding(start = 88.dp)
    )
}

/**
 * Get color for distraction score.
 */
@Composable
private fun getDistractionColor(score: Int): Color {
    return when {
        score <= 20 -> MaterialTheme.colorScheme.primary // Excellent (low distraction)
        score <= 40 -> MaterialTheme.colorScheme.secondary // Good
        score <= 60 -> MaterialTheme.colorScheme.tertiary // Average
        score <= 80 -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f) // High
        else -> MaterialTheme.colorScheme.error // Catastrophic
    }
}

/**
 * Get description for distraction score.
 */
private fun getDistractionDescription(score: Int): String {
    return when {
        score <= 20 -> "Uitstekend - Volledig gefocust"
        score <= 40 -> "Goed - Minimale afleiding"
        score <= 60 -> "Gemiddeld - Normale media aandacht"
        score <= 80 -> "Hoog - Manager issues/media storm"
        else -> "Catastrofaal - Crisis situatie"
    }
}

/**
 * Get color for fitness score.
 */
@Composable
private fun getFitnessColor(score: Int): Color {
    return when {
        score >= 80 -> MaterialTheme.colorScheme.primary // Excellent
        score >= 60 -> MaterialTheme.colorScheme.secondary // Good
        score >= 40 -> MaterialTheme.colorScheme.tertiary // Average
        score >= 20 -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f) // Low
        else -> MaterialTheme.colorScheme.error // Catastrophic
    }
}

/**
 * Get description for fitness score.
 */
private fun getFitnessDescription(score: Int): String {
    return when {
        score >= 80 -> "Uitstekend - Volledige selectie beschikbaar"
        score >= 60 -> "Goed - Weinig blessures"
        score >= 40 -> "Gemiddeld - Normale blessures"
        score >= 20 -> "Laag - Meerdere blessures"
        else -> "Catastrofaal - Veel sleutelspelers weg"
    }
}

/**
 * Summary indicator for fitness or distraction.
 */
@Composable
private fun SummaryIndicator(
    title: String,
    value: Int,
    threshold: Int,
    goodColor: Color,
    warningColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    val isGood = value <= threshold
    val color = if (isGood) goodColor else warningColor
    
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = color.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, color),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                text = "$value%",
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = if (isGood) "GOED" else "LET OP",
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Preview version of SignalDashboard for testing.
 */
@Composable
fun SignalDashboardPreview() {
    val sampleSimulationContext = SimulationContext(
        homeDistraction = 25,
        awayDistraction = 65,
        homeFitness = 85,
        awayFitness = 72
    )
    
    val sampleAiReasoning = "De AI analyse toont aan dat Manchester City een significante voorsprong heeft op basis van recente prestaties. Het team heeft 5 overwinningen op rij behaald en toont uitstekende fitheid. Nottingham Forest daarentegen kampt met blessures in de verdediging en een hoge distractie-index door aanhoudende transfergeruchten. Deze factoren versterken de statistische voorspelling van een thuisoverwinning."
    
    SignalDashboard(
        simulationContext = sampleSimulationContext,
        aiReasoning = sampleAiReasoning,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}
