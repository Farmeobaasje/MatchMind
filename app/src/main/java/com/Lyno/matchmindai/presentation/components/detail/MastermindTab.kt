package com.Lyno.matchmindai.presentation.components.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.Lyno.matchmindai.domain.model.MatchDetail
import com.Lyno.matchmindai.presentation.components.GlassCard
import com.Lyno.matchmindai.presentation.components.MastermindAnalysisButton
import com.Lyno.matchmindai.presentation.components.MastermindInsightCard
import com.Lyno.matchmindai.presentation.viewmodel.MatchDetailViewModel
import com.Lyno.matchmindai.ui.theme.PrimaryNeon

/**
 * MastermindTab - The "Ervaring" tab combining AI analysis with visual storytelling.
 * This is where the AI's psychological and tactical analysis comes to life.
 */
@Composable
fun MastermindTab(matchDetail: MatchDetail, viewModel: MatchDetailViewModel) {
    val hybridPrediction by viewModel.hybridPrediction.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingHybrid.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 80.dp) // Extra padding to prevent FAB overlap
    ) {
        
        if (hybridPrediction != null) {
            val analysis = hybridPrediction!!.analysis
            
            // De Visuele 'Hero' kaart
            MastermindInsightCard(
                hybridPrediction = hybridPrediction,
                isLoading = false,
                errorMessage = null
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Scenario Tijdlijn (Visueel)
            if (analysis.primary_scenario_desc.isNotEmpty()) {
                Text("📜 Het Script", fontWeight = FontWeight.Bold)
                GlassCard {
                    Text(
                        text = analysis.primary_scenario_desc,
                        modifier = Modifier.padding(16.dp),
                        lineHeight = androidx.compose.ui.unit.TextUnit(24f, androidx.compose.ui.unit.TextUnitType.Sp)
                    )
                }
            }

        } else if (isLoading) {
            // Loading State met leuke tekst
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFF00FF9D))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("De Mastermind denkt na...", color = Color.Gray)
                }
            }
        } else {
            // Empty State
            MastermindAnalysisButton(
                onClick = { viewModel.loadHybridPrediction(matchDetail) },
                isLoading = false
            )
        }
    }
}

/**
 * Alternative MastermindInsightCard that accepts individual parameters.
 * This is a simplified version for the MastermindTab.
 */
@Composable
fun MastermindInsightCard(
    chaosScore: Int,
    atmosphereScore: Int,
    scenarioTitle: String,
    scenarioDescription: String,
    tacticalKey: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Mastermind",
                        tint = PrimaryNeon
                    )
                    Text(
                        text = "Mastermind Intelligentie",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNeon
                    )
                }
                
                // Confidence badge
                Badge(
                    containerColor = PrimaryNeon.copy(alpha = 0.2f),
                    contentColor = PrimaryNeon
                ) {
                    Text(
                        text = "AI",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Chaos Meter
                MetricCard(
                    title = "Chaos Meter",
                    value = chaosScore,
                    maxValue = 100,
                    color = when {
                        chaosScore >= 80 -> Color.Red
                        chaosScore >= 60 -> Color(0xFFFFA726)
                        chaosScore >= 40 -> Color.Yellow
                        else -> Color.Green
                    }
                )
                
                // Atmosphere Score
                MetricCard(
                    title = "Atmosfeer",
                    value = atmosphereScore,
                    maxValue = 100,
                    color = when {
                        atmosphereScore >= 80 -> PrimaryNeon
                        atmosphereScore >= 60 -> Color(0xFF4CAF50)
                        atmosphereScore >= 40 -> Color(0xFFFFEB3B)
                        else -> Color.Gray
                    }
                )
            }
            
            // Scenario Title
            Text(
                text = scenarioTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Scenario Description
            Text(
                text = scenarioDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = androidx.compose.ui.unit.TextUnit(24f, androidx.compose.ui.unit.TextUnitType.Sp)
            )
            
            // Tactical Key
            if (tacticalKey.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🔑 Tactische Sleutel",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = tacticalKey,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Metric card for displaying a single metric with progress bar.
 */
@Composable
private fun MetricCard(
    title: String,
    value: Int,
    maxValue: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "$value",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            
            LinearProgressIndicator(
                progress = value.toFloat() / maxValue.toFloat(),
                modifier = Modifier.fillMaxWidth(),
                color = color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Text(
                text = "/$maxValue",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
