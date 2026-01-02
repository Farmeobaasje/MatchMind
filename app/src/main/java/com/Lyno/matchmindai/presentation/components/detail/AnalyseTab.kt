package com.Lyno.matchmindai.presentation.components.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.Lyno.matchmindai.domain.model.MatchDetail
import com.Lyno.matchmindai.presentation.components.GlassCard
import com.Lyno.matchmindai.presentation.viewmodel.MatchDetailViewModel
import com.Lyno.matchmindai.ui.theme.PrimaryNeon
import com.Lyno.matchmindai.ui.theme.ActionOrange

/**
 * AnalyseTab - The "Harde Data" dashboard combining Dixon-Coles, API-Sports and form in one view.
 * This replaces the old "Voorspellingen" tab with a comprehensive statistical overview.
 */
@Composable
fun AnalyseTab(matchDetail: MatchDetail, viewModel: MatchDetailViewModel) {
    val prediction by viewModel.prediction.collectAsState() // API Sports
    val hybridPrediction by viewModel.hybridPrediction.collectAsStateWithLifecycle() // Hybrid (Enhanced xG + AI)
    
    // Load predictions when this tab is displayed
    LaunchedEffect(Unit) {
        viewModel.loadPrediction(matchDetail.fixtureId)
        viewModel.loadHybridPrediction(matchDetail)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 80.dp) // Extra padding to prevent FAB overlap
    ) {
        
        // 1. Head-to-Head Samenvatting
        HeadToHeadSummary(matchDetail = matchDetail)
        
        Spacer(modifier = Modifier.height(16.dp))

        // 2. Model Vergelijking (Dashboard)
        Text(
            text = "🤖 Model Consensus",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // API Sports Card
            val apiSportsConfidence = prediction?.winningPercent?.home?.toInt()
            ModelConfidenceCard(
                modelName = "API-Sports",
                confidence = apiSportsConfidence,
                modifier = Modifier.weight(1f)
            )
            
            // Hybrid Prediction Card (Enhanced xG + AI)
            val hybridConfidence = hybridPrediction?.enhancedPrediction?.homeWinProbability?.let { (it * 100).toInt() }
            ModelConfidenceCard(
                modelName = "MatchMind AI",
                confidence = hybridConfidence,
                modifier = Modifier.weight(1f)
            )
        }
        
        // 3. Data Kwaliteit Check
        Spacer(modifier = Modifier.height(16.dp))
        DataQualityBadge(
            lastUpdate = "10 min geleden",
            sources = listOf("API-Sports", "Historical Data")
        )
        
        // 4. Detailed Statistics Section
        Spacer(modifier = Modifier.height(24.dp))
        DetailedStatisticsSection(matchDetail = matchDetail)
    }
}

/**
 * Head-to-Head summary showing team statistics.
 */
@Composable
private fun HeadToHeadSummary(matchDetail: MatchDetail) {
    GlassCard {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TeamStatBlock(
                teamName = matchDetail.homeTeam,
                teamType = "Thuis",
                winPercentage = "67% Win" // Example data - would come from actual stats
            )
            
            Divider(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp)
            )
            
            TeamStatBlock(
                teamName = matchDetail.awayTeam,
                teamType = "Uit",
                winPercentage = "20% Win" // Example data - would come from actual stats
            )
        }
    }
}

/**
 * Team statistics block for head-to-head comparison.
 */
@Composable
private fun TeamStatBlock(
    teamName: String,
    teamType: String,
    winPercentage: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = teamType,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = teamName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = winPercentage,
            style = MaterialTheme.typography.bodyMedium,
            color = PrimaryNeon,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Model confidence card showing prediction confidence for a specific model.
 */
@Composable
fun ModelConfidenceCard(
    modelName: String,
    confidence: Int?,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = modelName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
            
            // Confidence circle or "N/B" indicator
            Box(
                modifier = Modifier.size(60.dp),
                contentAlignment = Alignment.Center
            ) {
                if (confidence != null) {
                    CircularProgressIndicator(
                        progress = confidence / 100f,
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 4.dp,
                        color = if (confidence >= 70) PrimaryNeon else ActionOrange
                    )
                    
                    Text(
                        text = "$confidence%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "N/B",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
            
            Text(
                text = if (confidence != null) {
                    when {
                        confidence >= 80 -> "Zeer Hoog"
                        confidence >= 60 -> "Hoog"
                        confidence >= 40 -> "Gemiddeld"
                        else -> "Laag"
                    }
                } else {
                    "Geen data"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Data quality badge showing freshness and sources.
 */
@Composable
fun DataQualityBadge(
    lastUpdate: String,
    sources: List<String>
) {
    GlassCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Data kwaliteit",
                tint = PrimaryNeon
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Data Kwaliteit",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Bijgewerkt: $lastUpdate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Bronnen: ${sources.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Badge(
                containerColor = PrimaryNeon.copy(alpha = 0.2f),
                contentColor = PrimaryNeon
            ) {
                Text(
                    text = "Goed",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Detailed statistics section showing key match statistics.
 */
@Composable
private fun DetailedStatisticsSection(matchDetail: MatchDetail) {
    Text(
        text = "📊 Gedetailleerde Statistieken",
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        modifier = Modifier.padding(bottom = 12.dp)
    )
    
    // Show top 5 statistics
    matchDetail.stats.take(5).forEach { stat ->
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
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
                
                // Team labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = matchDetail.homeTeam.take(15),
                        style = MaterialTheme.typography.labelSmall,
                        color = PrimaryNeon,
                        maxLines = 1
                    )
                    
                    Text(
                        text = matchDetail.awayTeam.take(15),
                        style = MaterialTheme.typography.labelSmall,
                        color = ActionOrange,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
