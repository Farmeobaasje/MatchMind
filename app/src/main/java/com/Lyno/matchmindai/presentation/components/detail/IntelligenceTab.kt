package com.Lyno.matchmindai.presentation.components.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.Lyno.matchmindai.domain.model.MatchDetail
import com.Lyno.matchmindai.presentation.components.GlassCard
import com.Lyno.matchmindai.presentation.components.MastermindAnalysisButton
import com.Lyno.matchmindai.presentation.components.MastermindInsightCard
import com.Lyno.matchmindai.presentation.viewmodel.MatchDetailViewModel
import com.Lyno.matchmindai.ui.theme.PrimaryNeon
import com.Lyno.matchmindai.ui.theme.ActionOrange

/**
 * IntelligenceTab - The "MatchMind Intel" dashboard combining hard data with AI context.
 * This replaces the separate Analyse and Mastermind tabs with one powerful intelligence dashboard.
 * 
 * Layout Structure:
 * 1. Hero Section: Chaos Meter + Model Consensus (Quick Scan)
 * 2. Story Section: AI Narrative + Tactical Insights (Context Understanding)
 * 3. Evidence Section: Hard Statistics + Data Quality (Verification)
 */
@Composable
fun IntelligenceTab(matchDetail: MatchDetail, viewModel: MatchDetailViewModel) {
    // Load all necessary data
    val prediction by viewModel.prediction.collectAsState() // API Sports
    val hybridPrediction by viewModel.hybridPrediction.collectAsStateWithLifecycle() // AI Analysis
    val quickMetrics by viewModel.quickMetrics.collectAsStateWithLifecycle() // Quick metrics for Intel tab
    val isLoading by viewModel.isLoadingHybrid.collectAsStateWithLifecycle()
    val isGeneratingQuickMetrics by viewModel.isGeneratingQuickMetrics.collectAsStateWithLifecycle()
    
    // Load API prediction when this tab is displayed
    // AI analysis is MANUAL - user must trigger it via MastermindAnalysisButton
    LaunchedEffect(Unit) {
        viewModel.loadPrediction(matchDetail.fixtureId)
        // DO NOT load hybrid prediction automatically - user must trigger it manually
        // This prevents automatic analysis when user opens the Intel tab
    }
    
    // Generate quick metrics when hybrid prediction is available
    LaunchedEffect(hybridPrediction) {
        if (hybridPrediction != null && quickMetrics == null && !isGeneratingQuickMetrics) {
            viewModel.generateQuickMetrics(matchDetail)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 80.dp) // Extra padding to prevent FAB overlap
    ) {
        
        // ========== HERO SECTION ==========
        // Quick Scan: "Is this a safe match or chaos-pot?"
        Text(
            text = "⚡ MatchMind Intel",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = PrimaryNeon,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Hero Row: Chaos/Atmosphere + Model Consensus
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left: Chaos & Atmosphere (if AI analysis available)
            if (hybridPrediction != null && hybridPrediction!!.analysis.isMastermindAnalysis()) {
                val analysis = hybridPrediction!!.analysis
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ChaosMeterCard(chaosScore = analysis.chaos_score)
                    AtmosfeerCard(atmosphereScore = analysis.atmosphere_score)
                }
            } else {
                // Placeholder when no AI analysis
                GlassCard(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "🧠 AI Analyse",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Activeer Mastermind voor diepgaande inzichten",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
            
            // Right: Model Consensus - Use quick metrics if available
            ModelConsensusCard(
                apiConfidence = prediction?.winningPercent?.home?.toInt(),
                hybridConfidence = hybridPrediction?.enhancedPrediction?.homeWinProbability?.let { (it * 100).toInt() },
                consensusLevel = quickMetrics?.consensusLevel,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // ========== STORY SECTION ==========
        // Context Understanding: "Why do the models say this?"
        if (hybridPrediction != null && hybridPrediction!!.analysis.isMastermindAnalysis()) {
            val analysis = hybridPrediction!!.analysis
            
            // Show MastermindInsightCard with the analysis results
            MastermindInsightCard(
                hybridPrediction = hybridPrediction,
                isLoading = isLoading,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        } else if (isLoading) {
            // Loading state for AI analysis
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = PrimaryNeon)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("AI analyse wordt geladen...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            // Call to action for AI analysis
            MastermindAnalysisButton(
                onClick = { viewModel.loadHybridPrediction(matchDetail) },
                isLoading = isLoading,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // ========== EVIDENCE SECTION ==========
        // Verification: "Can I trust this analysis?"
        Text(
            text = "🔍 De Bewijslast",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
        )
        
        // Hard Statistics
        HardeStatistiekenGrid(matchDetail = matchDetail)
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Data Quality - Use quick metrics if available
        if (quickMetrics != null) {
            DataQualityIndicator(
                dataQuality = quickMetrics!!.dataQuality,
                lastUpdate = "Net gegenereerd",
                sources = listOf("API-Sports", "Historical Data", "AI Analysis"),
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            DataQualityIndicator(
                dataQuality = "Onbekend",
                lastUpdate = "10 min geleden",
                sources = listOf("API-Sports", "Historical Data"),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Chaos Meter Card - Shows unpredictability level.
 */
@Composable
private fun ChaosMeterCard(chaosScore: Int) {
    GlassCard {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "⚡ Chaos Meter",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = when {
                    chaosScore >= 80 -> Color.Red
                    chaosScore >= 60 -> Color(0xFFFFA726)
                    chaosScore >= 40 -> Color.Yellow
                    else -> Color.Green
                }
            )
            
            Text(
                text = "$chaosScore",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            LinearProgressIndicator(
                progress = chaosScore / 100f,
                modifier = Modifier.fillMaxWidth(),
                color = when {
                    chaosScore >= 80 -> Color.Red
                    chaosScore >= 60 -> Color(0xFFFFA726)
                    chaosScore >= 40 -> Color.Yellow
                    else -> Color.Green
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Text(
                text = when {
                    chaosScore >= 80 -> "Totale Oorlog"
                    chaosScore >= 60 -> "Hoog Risico"
                    chaosScore >= 40 -> "Gemiddeld"
                    else -> "Voorspelbaar"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Atmosphere Card - Shows home crowd impact.
 */
@Composable
private fun AtmosfeerCard(atmosphereScore: Int) {
    GlassCard {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "🏟️ Atmosfeer",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = when {
                    atmosphereScore >= 80 -> PrimaryNeon
                    atmosphereScore >= 60 -> Color(0xFF4CAF50)
                    atmosphereScore >= 40 -> Color(0xFFFFEB3B)
                    else -> Color.Gray
                }
            )
            
            Text(
                text = "$atmosphereScore",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            LinearProgressIndicator(
                progress = atmosphereScore / 100f,
                modifier = Modifier.fillMaxWidth(),
                color = when {
                    atmosphereScore >= 80 -> PrimaryNeon
                    atmosphereScore >= 60 -> Color(0xFF4CAF50)
                    atmosphereScore >= 40 -> Color(0xFFFFEB3B)
                    else -> Color.Gray
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Text(
                text = when {
                    atmosphereScore >= 80 -> "Heksenketel"
                    atmosphereScore >= 60 -> "Levendig"
                    atmosphereScore >= 40 -> "Normaal"
                    else -> "Doods"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Model Consensus Card - Shows agreement between different prediction models.
 */
@Composable
private fun ModelConsensusCard(
    apiConfidence: Int?,
    hybridConfidence: Int?,
    consensusLevel: String? = null,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "🤖 Model Consensus",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            
            // API-Sports
            ConfidenceRow(
                modelName = "API-Sports",
                confidence = apiConfidence,
                color = PrimaryNeon
            )
            
            // Hybrid (Enhanced xG + AI)
            ConfidenceRow(
                modelName = "MatchMind AI",
                confidence = hybridConfidence,
                color = ActionOrange
            )
            
            // Consensus indicator - use provided consensus level or calculate
            val finalConsensusLevel = consensusLevel ?: calculateConsensus(apiConfidence, hybridConfidence)
            Text(
                text = "Consensus: $finalConsensusLevel",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * Confidence row for a single model.
 */
@Composable
private fun ConfidenceRow(
    modelName: String,
    confidence: Int?,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = modelName,
            style = MaterialTheme.typography.bodySmall
        )
        
        if (confidence != null) {
            Text(
                text = "$confidence%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        } else {
            Text(
                text = "N/B",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

/**
 * Scenario Card - Shows the AI's predicted match narrative.
 */
@Composable
private fun ScenarioCard(
    title: String,
    description: String
) {
    GlassCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "🎬 $title",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryNeon
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = androidx.compose.ui.unit.TextUnit(24f, androidx.compose.ui.unit.TextUnitType.Sp)
            )
        }
    }
}

/**
 * Tactical Insight Card - Shows key tactical elements.
 */
@Composable
private fun TactischeInzichtCard(
    tacticalKey: String,
    keyPlayer: String
) {
    GlassCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                fontWeight = FontWeight.SemiBold
            )
            
            if (keyPlayer.isNotEmpty()) {
                Divider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant
                )
                
                Text(
                    text = "👁️ Speler om te volgen: $keyPlayer",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Calculate consensus level between models.
 */
private fun calculateConsensus(apiConfidence: Int?, hybridConfidence: Int?): String {
    if (apiConfidence == null || hybridConfidence == null) {
        return "Onvoldoende data"
    }
    
    val diff = kotlin.math.abs(apiConfidence - hybridConfidence)
    return when {
        diff <= 10 -> "Sterk"
        diff <= 20 -> "Matig"
        diff <= 30 -> "Zwak"
        else -> "Tegenstrijdig"
    }
}


/**
 * Hard Statistics Grid - Shows key match statistics.
 */
@Composable
private fun HardeStatistiekenGrid(matchDetail: MatchDetail) {
    // Show top 3 statistics for quick overview
    val topStats = matchDetail.stats.take(3)
    
    if (topStats.isNotEmpty()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            topStats.forEach { stat ->
                GlassCard {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stat.type,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Text(
                                text = "${stat.homeValue}${stat.unit} - ${stat.awayValue}${stat.unit}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // Progress bar for visual comparison
                        val homeValue = try { stat.homeValue.toFloat() } catch (e: NumberFormatException) { 0f }
                        val awayValue = try { stat.awayValue.toFloat() } catch (e: NumberFormatException) { 0f }
                        val total = homeValue + awayValue
                        val homePercentage = if (total > 0) homeValue / total else 0.5f
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            // Home team progress
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = homePercentage)
                                    .fillMaxHeight()
                                    .background(PrimaryNeon)
                            )
                            
                            // Away team progress
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = 1 - homePercentage)
                                    .fillMaxHeight()
                                    .align(Alignment.CenterEnd)
                                    .background(ActionOrange)
                            )
                        }
                    }
                }
            }
        }
    } else {
        GlassCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Geen statistieken beschikbaar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Wedstrijd nog niet begonnen",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
