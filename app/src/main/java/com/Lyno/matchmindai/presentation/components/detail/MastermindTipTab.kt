package com.Lyno.matchmindai.presentation.components.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.Lyno.matchmindai.domain.model.MatchDetail
import com.Lyno.matchmindai.presentation.components.GlassCard
import com.Lyno.matchmindai.presentation.viewmodel.MatchDetailViewModel
import kotlinx.coroutines.launch
import com.Lyno.matchmindai.ui.theme.PrimaryNeon
import com.Lyno.matchmindai.ui.theme.ActionOrange

/**
 * MastermindTipTab - State-of-the-art betting tip with clear action.
 * 
 * Combines all analysis into one clear betting recommendation:
 * 1. Clear betting tip with odds
 * 2. Value score and confidence
 * 3. Kelly stake recommendation
 * 4. One-click action to bookmaker
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MastermindTipTab(matchDetail: MatchDetail, viewModel: MatchDetailViewModel) {
    val coroutineScope = rememberCoroutineScope()
    
    // Observe states
    val prediction by viewModel.prediction.collectAsStateWithLifecycle()
    val odds by viewModel.odds.collectAsStateWithLifecycle()
    val isLoadingOdds by viewModel.isLoadingOdds.collectAsStateWithLifecycle()
    val isLoadingPrediction by viewModel.isLoadingPrediction.collectAsStateWithLifecycle()
    val mastermindTip by viewModel.mastermindTip.collectAsStateWithLifecycle()
    val isLoadingMastermind by viewModel.isLoadingMastermind.collectAsStateWithLifecycle()
    val mastermindError by viewModel.mastermindError.collectAsStateWithLifecycle()
    
    // Load data when tab is displayed
    LaunchedEffect(Unit) {
        viewModel.loadPrediction(matchDetail.fixtureId)
        viewModel.loadOdds(matchDetail.fixtureId)
        // Load Mastermind tip automatically when tab is displayed
        viewModel.loadMastermindTip()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // ========== HERO TIP SECTION ==========
        Text(
            text = "🎯 MASTERMIND TIP",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = PrimaryNeon,
            modifier = Modifier.padding(bottom = 20.dp)
        )
        
        // Main Tip Card
        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Match Info
                Text(
                    text = "${matchDetail.homeTeam} vs ${matchDetail.awayTeam}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Text(
                    text = matchDetail.league ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Divider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant
                )
                
                // Betting Tip
                if (mastermindTip != null && !isLoadingMastermind) {
                    // Use Mastermind tip if available
                    val bestBet = BestBetData(
                        description = mastermindTip!!.bestBet.description,
                        odds = mastermindTip!!.odds ?: 0.0,
                        confidence = mastermindTip!!.confidence,
                        valueScore = mastermindTip!!.valueScore,
                        kellyStake = mastermindTip!!.kellyStakePercentage
                    )
                } else if (prediction != null && odds != null && !isLoadingOdds && !isLoadingPrediction) {
                    // Fallback to simple prediction if Mastermind tip not available
                    val bestBet = determineBestBet(prediction!!, odds!!)
                    
                    // Tip Display
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "🔥 BESTE WAARDE",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = ActionOrange
                        )
                        
                        Text(
                            text = bestBet.description,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        
                        Text(
                            text = "Odds: ${String.format("%.2f", bestBet.odds)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Value Indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ValueIndicator(
                            label = "Confidence",
                            value = "${bestBet.confidence}%",
                            color = getConfidenceColor(bestBet.confidence),
                            icon = Icons.Default.Verified
                        )
                        
                        ValueIndicator(
                            label = "Value Score",
                            value = "${bestBet.valueScore}/10",
                            color = getValueScoreColor(bestBet.valueScore),
                            icon = Icons.Default.TrendingUp
                        )
                        
                        ValueIndicator(
                            label = "Kelly Stake",
                            value = "${bestBet.kellyStake}%",
                            color = getKellyStakeColor(bestBet.kellyStake),
                            icon = Icons.Default.AttachMoney
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Action Button
                    Button(
                        onClick = {
                            // TODO: Implement bookmaker deep link
                            coroutineScope.launch {
                                // For now, just log
                                println("Opening bookmaker for bet: ${bestBet.description} @ ${bestBet.odds}")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsSoccer,
                            contentDescription = "Bet",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ZET NU @ ${String.format("%.2f", bestBet.odds)}",
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Risk Warning
                    Text(
                        text = "⚠️  Fractional Kelly (25%): Inzet ${bestBet.kellyStake}% van bankroll",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    
                } else if (isLoadingMastermind) {
                    // Loading Mastermind tip
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = PrimaryNeon)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Mastermind analyse wordt geladen...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else if (mastermindError != null) {
                    // Error loading Mastermind tip
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Fout bij laden Mastermind tip",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = mastermindError ?: "Onbekende fout",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadMastermindTip() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryNeon,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Opnieuw proberen")
                            }
                        }
                    }
                } else {
                    // Loading prediction and odds
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = PrimaryNeon)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Voorspellingen worden geladen...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // ========== ANALYSIS SECTION ==========
        Text(
            text = "📊 TECHNISCHE ANALYSE",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = PrimaryNeon,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        if (mastermindTip != null) {
            // Show Mastermind analysis
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "🧠 MASTERMIND ANALYSE",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNeon
                    )
                    
                    Text(
                        text = mastermindTip!!.analysis,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        } else if (prediction != null && odds != null) {
            // Model Comparison
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "🤖 Model Consensus",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    ModelComparisonRow(
                        modelName = "Dixon-Coles + xG",
                        probability = prediction!!.winningPercent.home?.times(100)?.toInt() ?: 0,
                        color = PrimaryNeon
                    )
                    
                    ModelComparisonRow(
                        modelName = "Bookmaker Implied",
                        probability = odds!!.homeWinProbability?.times(100)?.toInt() ?: 0,
                        color = ActionOrange
                    )
                    
                    val edge = calculateEdge(
                        ourProbability = prediction!!.winningPercent.home ?: 0.0,
                        bookmakerProbability = odds!!.homeWinProbability ?: 0.0
                    )
                    
                    Text(
                        text = "Edge: ${String.format("%.1f", edge * 100)}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (edge > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Risk Assessment
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "⚠️  RISICO BEOORDELING",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    
                    RiskFactor(
                        factor = "Data Kwaliteit",
                        level = "Hoog",
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    RiskFactor(
                        factor = "Model Confidence",
                        level = if ((prediction!!.winningPercent.home ?: 0.0) > 0.6) "Hoog" else "Gemiddeld",
                        color = if ((prediction!!.winningPercent.home ?: 0.0) > 0.6) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                    )
                    
                    RiskFactor(
                        factor = "Market Efficiency",
                        level = "Gemiddeld",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    
                    Text(
                        text = "✅ Gebruik ALTIJD Fractional Kelly (max 25%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Expected Goals
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "⚽ EXPECTED GOALS (xG)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = matchDetail.homeTeam,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = String.format("%.1f", prediction!!.expectedGoals?.home ?: 0.0),
                                style = MaterialTheme.typography.headlineSmall,
                                color = PrimaryNeon
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "xG",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "-",
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = matchDetail.awayTeam,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = String.format("%.1f", prediction!!.expectedGoals?.away ?: 0.0),
                                style = MaterialTheme.typography.headlineSmall,
                                color = ActionOrange
                            )
                        }
                    }
                    
                    LinearProgressIndicator(
                        progress = calculateXgProgress(
                            homeXg = prediction!!.expectedGoals?.home ?: 0.0,
                            awayXg = prediction!!.expectedGoals?.away ?: 0.0
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        color = PrimaryNeon,
                        trackColor = ActionOrange
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * Determine the best bet based on prediction and odds.
 */
private fun determineBestBet(prediction: com.Lyno.matchmindai.domain.model.MatchPredictionData, odds: com.Lyno.matchmindai.domain.model.OddsData): BestBetData {
    // Simple logic: choose market with highest probability and reasonable odds
    val homeProbability = prediction.winningPercent.home ?: 0.0
    val drawProbability = prediction.winningPercent.draw ?: 0.0
    val awayProbability = prediction.winningPercent.away ?: 0.0
    
    val homeOdds = odds.homeWinOdds ?: 0.0
    val drawOdds = odds.drawOdds ?: 0.0
    val awayOdds = odds.awayWinOdds ?: 0.0
    
    // Calculate value scores (simple Kelly approximation)
    val homeValue = calculateSimpleKelly(homeProbability, homeOdds)
    val drawValue = calculateSimpleKelly(drawProbability, drawOdds)
    val awayValue = calculateSimpleKelly(awayProbability, awayOdds)
    
    return when {
        homeValue >= drawValue && homeValue >= awayValue -> BestBetData(
            description = "${prediction.homeTeam} wint",
            odds = homeOdds,
            confidence = (homeProbability * 100).toInt(),
            valueScore = (homeValue * 10).toInt().coerceIn(0, 10),
            kellyStake = (homeValue * 100).toInt().coerceIn(1, 5)
        )
        drawValue >= homeValue && drawValue >= awayValue -> BestBetData(
            description = "Gelijkspel",
            odds = drawOdds,
            confidence = (drawProbability * 100).toInt(),
            valueScore = (drawValue * 10).toInt().coerceIn(0, 10),
            kellyStake = (drawValue * 100).toInt().coerceIn(1, 5)
        )
        else -> BestBetData(
            description = "${prediction.awayTeam} wint",
            odds = awayOdds,
            confidence = (awayProbability * 100).toInt(),
            valueScore = (awayValue * 10).toInt().coerceIn(0, 10),
            kellyStake = (awayValue * 100).toInt().coerceIn(1, 5)
        )
    }
}

/**
 * Simple Kelly calculation for value scoring.
 */
private fun calculateSimpleKelly(probability: Double, odds: Double): Double {
    if (odds <= 1.0 || probability <= 0.0) return 0.0
    val b = odds - 1.0
    val p = probability
    val q = 1.0 - p
    return ((b * p - q) / b).coerceAtLeast(0.0) * 0.25 // Fractional Kelly (25%)
}

/**
 * Calculate edge between our probability and bookmaker implied probability.
 */
private fun calculateEdge(ourProbability: Double, bookmakerProbability: Double): Double {
    return ourProbability - bookmakerProbability
}

/**
 * Calculate xG progress for visualization.
 */
private fun calculateXgProgress(homeXg: Double, awayXg: Double): Float {
    val total = homeXg + awayXg
    return if (total > 0) (homeXg / total).toFloat() else 0.5f
}

/**
 * Get color for confidence level.
 */
@Composable
private fun getConfidenceColor(confidence: Int): Color {
    return when {
        confidence >= 70 -> MaterialTheme.colorScheme.primary
        confidence >= 50 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }
}

/**
 * Get color for value score.
 */
@Composable
private fun getValueScoreColor(score: Int): Color {
    return when {
        score >= 8 -> MaterialTheme.colorScheme.primary
        score >= 6 -> PrimaryNeon
        score >= 4 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }
}

/**
 * Get color for Kelly stake.
 */
@Composable
private fun getKellyStakeColor(stake: Int): Color {
    return when {
        stake >= 4 -> MaterialTheme.colorScheme.error
        stake >= 2 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
}

/**
 * Value indicator composable.
 */
@Composable
private fun ValueIndicator(
    label: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/**
 * Model comparison row.
 */
@Composable
private fun ModelComparisonRow(
    modelName: String,
    probability: Int,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = modelName,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = "$probability%",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/**
 * Risk factor composable.
 */
@Composable
private fun RiskFactor(
    factor: String,
    level: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = factor,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Badge(
            containerColor = color.copy(alpha = 0.2f),
            contentColor = color
        ) {
            Text(
                text = level,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Data class for best bet information.
 */
private data class BestBetData(
    val description: String,
    val odds: Double,
    val confidence: Int,
    val valueScore: Int,
    val kellyStake: Int
)
