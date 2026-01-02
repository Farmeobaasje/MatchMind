package com.Lyno.matchmindai.presentation.components.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.Lyno.matchmindai.domain.model.SimulationContext

/**
 * Team Analysis Card for displaying distraction and fitness scores.
 * Part of Project ChiChi: Visualizing team psychological and physical factors.
 * 
 * Displays distraction index (0-100) and fitness level (0-100) for both teams
 * with color-coded progress bars and descriptive labels.
 */
@Composable
fun TeamAnalysisCard(
    simulationContext: SimulationContext,
    homeTeamName: String,
    awayTeamName: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Text(
                text = "TEAM ANALYSE (CHICHI)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Psychologische & fysieke factoren gebaseerd op nieuwsanalyse",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
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
            
            // Summary
            if (simulationContext.hasMeaningfulData()) {
                Spacer(modifier = Modifier.height(8.dp))
                AnalysisSummary(simulationContext = simulationContext)
            }
        }
    }
}

/**
 * Row displaying analysis for a single team.
 */
@Composable
private fun TeamAnalysisRow(
    teamName: String,
    distractionScore: Int,
    fitnessScore: Int,
    isHomeTeam: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
 * Summary of the analysis with key insights.
 */
@Composable
private fun AnalysisSummary(simulationContext: SimulationContext) {
    val insights = mutableListOf<String>()
    
    if (simulationContext.hasHighDistraction) {
        insights.add("⚠️ Hoge afleiding gedetecteerd (onvoorspelbare wedstrijd)")
    }
    
    if (simulationContext.hasLowFitness) {
        insights.add("⚠️ Lage fitheid gedetecteerd (prestatie impact)")
    }
    
    if (simulationContext.homeDistraction > simulationContext.awayDistraction + 20) {
        insights.add("🏠 Thuisteam meer afgeleid dan uitteam")
    } else if (simulationContext.awayDistraction > simulationContext.homeDistraction + 20) {
        insights.add("✈️ Uitteam meer afgeleid dan thuisteam")
    }
    
    if (simulationContext.homeFitness < simulationContext.awayFitness - 20) {
        insights.add("🏠 Thuisteam minder fit dan uitteam")
    } else if (simulationContext.awayFitness < simulationContext.homeFitness - 20) {
        insights.add("✈️ Uitteam minder fit dan thuisteam")
    }
    
    if (insights.isNotEmpty()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "KEY INSIGHTS:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            insights.forEach { insight ->
                Text(
                    text = "• $insight",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
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
