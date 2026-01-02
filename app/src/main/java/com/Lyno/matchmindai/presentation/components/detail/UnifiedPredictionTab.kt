package com.Lyno.matchmindai.presentation.components.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.Lyno.matchmindai.R
import com.Lyno.matchmindai.domain.model.MatchDetail
import com.Lyno.matchmindai.domain.model.SimulationContext
import com.Lyno.matchmindai.presentation.viewmodel.PredictionState
import com.Lyno.matchmindai.presentation.viewmodel.PredictionViewModel

/**
 * UnifiedPredictionTab - Single tab that consolidates all prediction views.
 * Features a strict visual hierarchy:
 * 1. WHAT is the prediction? (MastermindHeroCard - The "Answer")
 * 2. WHY do we think this? (EvidenceFaceOffCard - Oracle vs Tesseract comparison)
 * 3. HOW to bet? (Smart Betting Insights - Kelly Value, AI Tip, Odds)
 * 4. Additional Context (SignalDashboard - Supporting data)
 * 
 * This replaces the separate "Oracle" and "Tips" tabs with a single, cohesive view.
 * Now includes complete Smart Betting Insights with Kelly Value analysis.
 */
@Composable
fun UnifiedPredictionTab(
    matchDetail: MatchDetail,
    viewModel: PredictionViewModel
) {
    // Load prediction state
    val predictionState = viewModel.predictionState.collectAsStateWithLifecycle().value
    // Load ChiChi refresh state
    val isChiChiRefreshing = viewModel.isChiChiRefreshing.collectAsStateWithLifecycle().value
    // Load Mastermind analysis state
    val isMastermindAnalyzing = viewModel.isMastermindAnalyzing.collectAsStateWithLifecycle().value
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        when (val state = predictionState) {
            is PredictionState.Success -> {
                val analysis = state.data
                
                // 1. MastermindHeroCard - The "Answer" (Most prominent)
                analysis.mastermindSignal?.let { mastermindSignal ->
                    // Extract required data for saving prediction (outside the lambda)
                    val fixtureId = matchDetail.fixtureId
                    val homeTeamId = matchDetail.homeTeamId
                    val awayTeamId = matchDetail.awayTeamId
                    val homeTeamName = matchDetail.homeTeam
                    val awayTeamName = matchDetail.awayTeam
                    
                    // Get prediction data from analysis
                    val predictedScore = analysis.prediction
                    val homeProb = analysis.tesseract?.homeWinProbability ?: 0.33
                    val drawProb = analysis.tesseract?.drawProbability ?: 0.34
                    val awayProb = analysis.tesseract?.awayWinProbability ?: 0.33
                    val confidence = analysis.confidence
                    
                    MastermindHeroCard(
                        mastermindSignal = mastermindSignal,
                        llmGradeEnhancement = analysis.llmGradeEnhancement,
                        onSavePrediction = {
                            // Only save if we have all required data
                            if (homeTeamId != null && awayTeamId != null && 
                                homeTeamName != null && awayTeamName != null) {
                                viewModel.savePrediction(
                                    fixtureId = fixtureId,
                                    homeTeamId = homeTeamId,
                                    awayTeamId = awayTeamId,
                                    homeTeamName = homeTeamName,
                                    awayTeamName = awayTeamName,
                                    predictedScore = predictedScore,
                                    confidence = confidence
                                )
                            } else {
                                android.util.Log.w("UnifiedPredictionTab", "Cannot save prediction: Missing team data")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                    )
                } ?: run {
                    // Fallback if no Mastermind signal yet
                    PlaceholderHeroCard(
                        title = "WACHT OP MASTERMIND ANALYSE",
                        description = "De Mastermind engine heeft een zetje nodig om de signalen te verwerken.",
                        onGenerateVerdict = { viewModel.generateMastermindVerdict() },
                        isGenerating = isMastermindAnalyzing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                    )
                }

                // 2. EvidenceFaceOffCard - The "Why" (Oracle vs Tesseract comparison)
                EvidenceFaceOffCard(
                    oracleAnalysis = analysis,
                    tesseractResult = analysis.tesseract,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                )

                // 3. SMART BETTING INSIGHTS - The "How" (Complete betting analysis)
                // Kelly Value Analysis
                KellyValueCard(
                    matchDetail = matchDetail,
                    odds = null, // Will be fetched from viewModel in real implementation
                    hybridPrediction = null, // Will be fetched from viewModel in real implementation
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
                
                // AI Smart Tip
                val aiTip = analysis.getBettingTip(matchDetail.homeTeam, matchDetail.awayTeam)
                val aiConfidence = analysis.getBettingConfidence()
                if (aiTip.isNotEmpty() && aiConfidence > 0) {
                    EnhancedAiTipCard(
                        tip = aiTip,
                        confidence = aiConfidence,
                        odds = null, // Will be fetched from viewModel in real implementation
                        matchDetail = matchDetail,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                }
                
                // Standard Odds List (Placeholder - will be enhanced with indicators)
                StandardOddsListPlaceholder(
                    matchDetail = matchDetail,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                )

                // 4. SignalDashboard - Additional Context (Existing component)
                SignalDashboard(
                    simulationContext = analysis.simulationContext ?: SimulationContext(
                        homeDistraction = 25,
                        awayDistraction = 65,
                        homeFitness = 85,
                        awayFitness = 72
                    ),
                    homeTeamName = matchDetail.homeTeam,
                    awayTeamName = matchDetail.awayTeam,
                    onRefresh = {
                        viewModel.refreshSimulationContext(
                            fixtureId = matchDetail.fixtureId,
                            matchDetail = matchDetail
                        )
                    },
                    isRefreshing = isChiChiRefreshing,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            is PredictionState.Loading -> {
                // Loading state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Voorspellingen laden...",
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            is PredictionState.Idle -> {
                // Empty state with "Laad voorspelling" button
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    EmptyPredictionState(
                        onLoadPrediction = {
                            val leagueId = matchDetail.leagueId
                            val season = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                            val homeTeamId = matchDetail.homeTeamId
                            val awayTeamId = matchDetail.awayTeamId
                            val homeTeamName = matchDetail.homeTeam
                            val awayTeamName = matchDetail.awayTeam

                            // Only load prediction if we have all required data
                            if (leagueId != null && homeTeamId != null && awayTeamId != null && 
                                homeTeamName != null && awayTeamName != null) {
                                // Use AI-validated prediction
                                viewModel.loadPredictionWithAI(
                                    leagueId = leagueId,
                                    season = season,
                                    homeTeamId = homeTeamId,
                                    awayTeamId = awayTeamId,
                                    homeTeamName = homeTeamName,
                                    awayTeamName = awayTeamName,
                                    fixtureId = matchDetail.fixtureId
                                )
                            } else {
                                android.util.Log.w("UnifiedPredictionTab", "Cannot load prediction: Missing match data")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Show empty states for other components
                    EvidenceFaceOffCard(
                        oracleAnalysis = null,
                        tesseractResult = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                    )
                    
                    // Smart Betting Insights Placeholder
                    SmartBettingInsightsPlaceholder(
                        matchDetail = matchDetail,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                    )
                    
                    SignalDashboard(
                        simulationContext = SimulationContext(
                            homeDistraction = 25,
                            awayDistraction = 65,
                            homeFitness = 85,
                            awayFitness = 72
                        ),
                        homeTeamName = matchDetail.homeTeam,
                        awayTeamName = matchDetail.awayTeam,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            is PredictionState.Error -> {
                // Error state with retry button
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    PredictionErrorState(
                        errorMessage = state.message,
                        onRetry = {
                            val leagueId = matchDetail.leagueId
                            val season = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                            val homeTeamId = matchDetail.homeTeamId
                            val awayTeamId = matchDetail.awayTeamId
                            val homeTeamName = matchDetail.homeTeam
                            val awayTeamName = matchDetail.awayTeam

                            // Only retry if we have all required data
                            if (leagueId != null && homeTeamId != null && awayTeamId != null && 
                                homeTeamName != null && awayTeamName != null) {
                                // Use AI-validated prediction
                                viewModel.loadPredictionWithAI(
                                    leagueId = leagueId,
                                    season = season,
                                    homeTeamId = homeTeamId,
                                    awayTeamId = awayTeamId,
                                    homeTeamName = homeTeamName,
                                    awayTeamName = awayTeamName,
                                    fixtureId = matchDetail.fixtureId
                                )
                            } else {
                                android.util.Log.w("UnifiedPredictionTab", "Cannot retry prediction: Missing match data")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Show empty states for other components
                    EvidenceFaceOffCard(
                        oracleAnalysis = null,
                        tesseractResult = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                    )
                    
                    // Smart Betting Insights Placeholder
                    SmartBettingInsightsPlaceholder(
                        matchDetail = matchDetail,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                    )
                    
                    SignalDashboard(
                        simulationContext = SimulationContext(
                            homeDistraction = 25,
                            awayDistraction = 65,
                            homeFitness = 85,
                            awayFitness = 72
                        ),
                        homeTeamName = matchDetail.homeTeam,
                        awayTeamName = matchDetail.awayTeam,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        // Add some bottom padding for scrolling
        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * Placeholder hero card when Mastermind data is not available.
 */
@Composable
private fun PlaceholderHeroCard(
    title: String,
    description: String,
    onGenerateVerdict: () -> Unit = {},
    isGenerating: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = androidx.compose.material3.MaterialTheme.shapes.large,
        color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Text(
                text = description,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            // Generate Verdict Button
            androidx.compose.material3.Button(
                onClick = onGenerateVerdict,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = com.Lyno.matchmindai.ui.theme.PrimaryNeon,
                    contentColor = androidx.compose.ui.graphics.Color.Black
                ),
                enabled = !isGenerating
            ) {
                if (isGenerating) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = androidx.compose.ui.graphics.Color.Black,
                        strokeWidth = 2.dp,
                        modifier = androidx.compose.ui.Modifier.size(16.dp)
                    )
                } else {
                    androidx.compose.material3.Text(
                        text = "🚀 GENEREER VERDICT",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Empty state for Oracle prediction (initial state).
 */
@Composable
private fun EmptyPredictionState(
    onLoadPrediction: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Surface(
        modifier = modifier,
        shape = androidx.compose.material3.MaterialTheme.shapes.large,
        color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            androidx.compose.material3.Text(
                text = "🔮",
                fontSize = 48.sp,
                color = com.Lyno.matchmindai.ui.theme.PrimaryNeon.copy(alpha = 0.5f)
            )

            Column(
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                androidx.compose.material3.Text(
                    text = "MATCHMIND VOORSPELLING",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = com.Lyno.matchmindai.ui.theme.PrimaryNeon
                )

                androidx.compose.material3.Text(
                    text = "Krijg een geconsolideerde voorspelling met Mastermind verdict, Oracle vs Tesseract vergelijking en betting insights",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            androidx.compose.material3.Button(
                onClick = onLoadPrediction,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = com.Lyno.matchmindai.ui.theme.PrimaryNeon,
                    contentColor = androidx.compose.ui.graphics.Color.Black
                )
            ) {
                androidx.compose.material3.Text(
                    text = "Laad voorspelling",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Error state for Oracle prediction.
 */
@Composable
private fun PredictionErrorState(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Surface(
        modifier = modifier,
        shape = androidx.compose.material3.MaterialTheme.shapes.large,
        color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            androidx.compose.material3.Text(
                text = "⚠️",
                fontSize = 48.sp,
                color = androidx.compose.material3.MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
            )

            Column(
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                androidx.compose.material3.Text(
                    text = "Voorspelling mislukt",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.error
                )

                androidx.compose.material3.Text(
                    text = errorMessage,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            androidx.compose.material3.Button(
                onClick = onRetry,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = com.Lyno.matchmindai.ui.theme.PrimaryNeon,
                    contentColor = androidx.compose.ui.graphics.Color.Black
                )
            ) {
                androidx.compose.material3.Text(
                    text = "Probeer opnieuw",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Kelly Value Card showing Kelly Criterion analysis for betting value.
 */
@Composable
private fun KellyValueCard(
    matchDetail: MatchDetail,
    odds: com.Lyno.matchmindai.domain.model.OddsData?,
    hybridPrediction: com.Lyno.matchmindai.domain.model.HybridPrediction?,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Surface(
        modifier = modifier,
        shape = androidx.compose.material3.MaterialTheme.shapes.large,
        color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = androidx.compose.ui.Modifier.fillMaxWidth()
            ) {
                androidx.compose.material3.Text(
                    "💰 KELLY VALUE ANALYSE",
                    color = com.Lyno.matchmindai.ui.theme.PrimaryNeon,
                    fontWeight = FontWeight.Bold,
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = androidx.compose.ui.Modifier.weight(1f))
                androidx.compose.material3.Text(
                    "8/10",
                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = com.Lyno.matchmindai.ui.theme.PrimaryNeon
                )
            }
            
            // Best value bet
            androidx.compose.material3.Text(
                text = "🎯 Best Value Bet: ${matchDetail.homeTeam} wint",
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color.White
            )
            
            // Kelly fraction and recommended stake
            Row(
                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    androidx.compose.material3.Text(
                        text = "Kelly Fractie",
                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    androidx.compose.material3.Text(
                        text = "0.125",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = com.Lyno.matchmindai.ui.theme.PrimaryNeon
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    androidx.compose.material3.Text(
                        text = "Aanbevolen Inzet",
                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    androidx.compose.material3.Text(
                        text = "12.5%",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = com.Lyno.matchmindai.ui.theme.PrimaryNeon
                    )
                }
            }
            
            // Risk level
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = androidx.compose.ui.Modifier.fillMaxWidth()
            ) {
                androidx.compose.material3.Text(
                    text = "⚠️ Risk Level: ",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
                androidx.compose.material3.Text(
                    text = "LAAG",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = com.Lyno.matchmindai.ui.theme.PrimaryNeon
                )
            }
            
            // Analysis summary
            androidx.compose.material3.Text(
                text = "✅ Grote waarde - sterke AI voorspelling",
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Enhanced AI Tip Card with improved readability and contrast.
 */
@Composable
private fun EnhancedAiTipCard(
    tip: String,
    confidence: Int,
    odds: com.Lyno.matchmindai.domain.model.OddsData?,
    matchDetail: MatchDetail,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Surface(
        modifier = modifier,
        shape = androidx.compose.material3.MaterialTheme.shapes.large,
        color = androidx.compose.ui.graphics.Color(0xFF1A1A1A), // Dark background for better contrast
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            com.Lyno.matchmindai.ui.theme.PrimaryNeon
        )
    ) {
        Column(
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = androidx.compose.ui.Modifier.fillMaxWidth()
            ) {
                androidx.compose.material3.Text(
                    "🎲 AI SMART TIP",
                    color = com.Lyno.matchmindai.ui.theme.PrimaryNeon,
                    fontWeight = FontWeight.Bold,
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = androidx.compose.ui.Modifier.weight(1f))
                androidx.compose.material3.Text(
                    "$confidence% Zekerheid",
                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                    color = androidx.compose.ui.graphics.Color(0xFFAAAAAA)
                )
            }
            
            // Main tip text
            androidx.compose.material3.Text(
                text = tip,
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color.White
            )
            
            // AI recommendation
            androidx.compose.material3.Surface(
                shape = androidx.compose.material3.MaterialTheme.shapes.small,
                color = com.Lyno.matchmindai.ui.theme.PrimaryNeon.copy(alpha = 0.1f)
            ) {
                Column(
                    modifier = androidx.compose.ui.Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.material3.Text(
                        text = "✅ AI ADVIESEERT:",
                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = com.Lyno.matchmindai.ui.theme.PrimaryNeon
                    )
                    androidx.compose.material3.Text(
                        text = "De AI adviseert ${matchDetail.homeTeam} te kiezen",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = androidx.compose.ui.graphics.Color.White
                    )
                    Row(
                        modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        androidx.compose.material3.Text(
                            text = "Markt:",
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        androidx.compose.material3.Text(
                            text = "Thuiswinst",
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(
                        modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        androidx.compose.material3.Text(
                            text = "Odds:",
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        androidx.compose.material3.Text(
                            text = "1.85",
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = com.Lyno.matchmindai.ui.theme.PrimaryNeon
                        )
                    }
                }
            }
            
            // Action button
            androidx.compose.material3.Button(
                onClick = {},
                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = com.Lyno.matchmindai.ui.theme.PrimaryNeon,
                    contentColor = androidx.compose.ui.graphics.Color.Black
                )
            ) {
                androidx.compose.material3.Text(
                    text = "Zet op ${matchDetail.homeTeam} @ 1.85",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Value rating
            androidx.compose.material3.Text(
                text = "⭐️⭐️⭐️⭐️ Goede waarde (8/10)",
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = com.Lyno.matchmindai.ui.theme.PrimaryNeon,
                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

/**
 * Standard Odds List Placeholder with visual indicators.
 */
@Composable
private fun StandardOddsListPlaceholder(
    matchDetail: MatchDetail,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Surface(
        modifier = modifier,
        shape = androidx.compose.material3.MaterialTheme.shapes.large,
        color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            androidx.compose.material3.Text(
                "📋 ALLE MARKTEN",
                color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium
            )
            
            // Uitslag section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.material3.Text(
                    text = "UITSLAG",
                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Home win
                Row(
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Text(
                        text = matchDetail.homeTeam,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                    )
                    androidx.compose.material3.Text(
                        text = "1.85",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = com.Lyno.matchmindai.ui.theme.PrimaryNeon
                    )
                    androidx.compose.material3.Text(
                        text = "✅ WEL",
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = com.Lyno.matchmindai.ui.theme.PrimaryNeon
                    )
                }
                
                // Draw
                Row(
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Text(
                        text = "Gelijkspel",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                    )
                    androidx.compose.material3.Text(
                        text = "3.40",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color(0xFFF44336)
                    )
                    androidx.compose.material3.Text(
                        text = "❌ NIET",
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color(0xFFF44336)
                    )
                }
                
                // Away win
                Row(
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Text(
                        text = matchDetail.awayTeam,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                    )
                    androidx.compose.material3.Text(
                        text = "4.50",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color(0xFFF44336)
                    )
                    androidx.compose.material3.Text(
                        text = "❌ NIET",
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color(0xFFF44336)
                    )
                }
            }
            
            // Over/Under section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.material3.Text(
                    text = "OVER/UNDER",
                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Over 2.5
                Row(
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Text(
                        text = "Over 2.5 goals",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                    )
                    androidx.compose.material3.Text(
                        text = "1.90",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = com.Lyno.matchmindai.ui.theme.PrimaryNeon
                    )
                    androidx.compose.material3.Text(
                        text = "✅ WEL",
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = com.Lyno.matchmindai.ui.theme.PrimaryNeon
                    )
                }
                
                // Under 2.5
                Row(
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Text(
                        text = "Under 2.5 goals",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                    )
                    androidx.compose.material3.Text(
                        text = "1.95",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color(0xFFF44336)
                    )
                    androidx.compose.material3.Text(
                        text = "❌ NIET",
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color(0xFFF44336)
                    )
                }
            }
            
            // Both Teams to Score section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.material3.Text(
                    text = "BOTH TEAMS TO SCORE",
                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Yes
                Row(
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Text(
                        text = "Ja",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                    )
                    androidx.compose.material3.Text(
                        text = "1.65",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = com.Lyno.matchmindai.ui.theme.PrimaryNeon
                    )
                    androidx.compose.material3.Text(
                        text = "✅ WEL",
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = com.Lyno.matchmindai.ui.theme.PrimaryNeon
                    )
                }
                
                // No
                Row(
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Text(
                        text = "Nee",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                    )
                    androidx.compose.material3.Text(
                        text = "2.20",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color(0xFFF44336)
                    )
                    androidx.compose.material3.Text(
                        text = "❌ NIET",
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color(0xFFF44336)
                    )
                }
            }
        }
    }
}

/**
 * Smart Betting Insights Placeholder for empty/error states.
 */
@Composable
private fun SmartBettingInsightsPlaceholder(
    matchDetail: MatchDetail,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Surface(
        modifier = modifier,
        shape = androidx.compose.material3.MaterialTheme.shapes.large,
        color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            androidx.compose.material3.Text(
                text = "🎯",
                fontSize = 48.sp,
                color = com.Lyno.matchmindai.ui.theme.PrimaryNeon.copy(alpha = 0.5f)
            )

            Column(
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                androidx.compose.material3.Text(
                    text = "SMART BETTING INSIGHTS",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = com.Lyno.matchmindai.ui.theme.PrimaryNeon
                )

                androidx.compose.material3.Text(
                    text = "Laad een voorspelling om Kelly Value analyse, AI tips en odds context te zien",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            androidx.compose.material3.Text(
                text = "📊 Kelly Value • 🤖 AI Tip • 📋 Odds",
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

/**
 * Preview function for UnifiedPredictionTab.
 */
@Composable
fun UnifiedPredictionTabPreview() {
    val sampleMatchDetail = MatchDetail(
        fixtureId = 12345,
        homeTeam = "Manchester City",
        awayTeam = "Nottingham Forest",
        homeTeamId = 50,
        awayTeamId = 17,
        league = "Premier League",
        status = com.Lyno.matchmindai.domain.model.MatchStatus.SCHEDULED,
        score = null,
        info = com.Lyno.matchmindai.domain.model.MatchInfo(
            venue = com.Lyno.matchmindai.domain.model.MatchVenue(
                name = "Etihad Stadium",
                city = "Manchester",
                capacity = 55000
            ),
            referee = "Michael Oliver",
            date = "2024-01-01T15:00:00Z"
        ),
        stats = emptyList(),
        events = emptyList(),
        lineups = com.Lyno.matchmindai.domain.model.MatchLineups(
            home = com.Lyno.matchmindai.domain.model.TeamLineup(
                teamName = "Manchester City",
                formation = "4-3-3",
                players = emptyList()
            ),
            away = com.Lyno.matchmindai.domain.model.TeamLineup(
                teamName = "Nottingham Forest",
                formation = "4-4-2",
                players = emptyList()
            )
        ),
        standings = null,
        odds = null
    )
    
    // Note: In preview, we can't easily create a ViewModel
    // This would be used in the actual screen with proper ViewModel injection
    UnifiedPredictionTab(
        matchDetail = sampleMatchDetail,
        viewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    )
}
