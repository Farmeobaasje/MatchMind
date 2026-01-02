    package com.Lyno.matchmindai.presentation.components.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.Lyno.matchmindai.domain.model.MatchDetail
import com.Lyno.matchmindai.presentation.components.GlassCard
import com.Lyno.matchmindai.presentation.viewmodel.PredictionViewModel
import com.Lyno.matchmindai.presentation.viewmodel.AiValidationState
import com.Lyno.matchmindai.ui.theme.PrimaryNeon
import com.Lyno.matchmindai.ui.theme.SecondaryPurple
import com.Lyno.matchmindai.ui.theme.SurfaceCard
import java.util.Calendar
import androidx.compose.ui.res.stringResource
import com.Lyno.matchmindai.R
import com.Lyno.matchmindai.presentation.components.detail.ProbabilityBar
import com.Lyno.matchmindai.presentation.components.detail.ContextGrid
import com.Lyno.matchmindai.domain.model.SimulationContext

/**
 * PredictionTab - Oracle Engine prediction tab with "Hard Reality" analysis.
 * Shows deterministic, fact-based predictions based on standings and head-to-head.
 * Features a visual Power Bar showing strength difference between teams.
 */
@Composable
fun PredictionTab(
    matchDetail: MatchDetail,
    viewModel: PredictionViewModel
) {
    // Load prediction and AI validation states
    val predictionState by viewModel.predictionState.collectAsStateWithLifecycle()
    val aiValidationState by viewModel.aiValidationState.collectAsStateWithLifecycle()
    val matchScenarioState by viewModel.matchScenarioState.collectAsStateWithLifecycle()

    // Auto-load prediction with AI validation when tab is opened (Phase 3)
    LaunchedEffect(matchDetail) {
        if (predictionState is com.Lyno.matchmindai.presentation.viewmodel.PredictionState.Idle) {
            val leagueId = matchDetail.leagueId ?: return@LaunchedEffect
            val season = Calendar.getInstance().get(Calendar.YEAR) // Use current year as default season
            val homeTeamId = matchDetail.homeTeamId ?: return@LaunchedEffect
            val awayTeamId = matchDetail.awayTeamId ?: return@LaunchedEffect
            val homeTeamName = matchDetail.homeTeam ?: return@LaunchedEffect
            val awayTeamName = matchDetail.awayTeam ?: return@LaunchedEffect

            // Use AI-validated prediction (Phase 3)
            viewModel.loadPredictionWithAI(
                leagueId = leagueId,
                season = season,
                homeTeamId = homeTeamId,
                awayTeamId = awayTeamId,
                homeTeamName = homeTeamName,
                awayTeamName = awayTeamName,
                fixtureId = matchDetail.fixtureId
            )
        }
    }

            // Trigger match scenario generation when prediction is loaded
            LaunchedEffect(predictionState) {
                if (predictionState is com.Lyno.matchmindai.presentation.viewmodel.PredictionState.Success) {
                    val state = predictionState as com.Lyno.matchmindai.presentation.viewmodel.PredictionState.Success
                    val fixtureId = matchDetail.fixtureId ?: return@LaunchedEffect

                    // Generate match scenario if not already loading or loaded
                    when (matchScenarioState) {
                        is com.Lyno.matchmindai.presentation.viewmodel.MatchScenarioState.Idle -> {
                            viewModel.generateMatchScenario(
                                fixtureId = fixtureId,
                                matchDetail = matchDetail
                            )
                        }
                        else -> {
                            // Already loading or loaded, do nothing
                        }
                    }
                }
            }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
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
                    contentDescription = "Oracle Prediction",
                    tint = PrimaryNeon,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "🔮 ORACLE PREDICTION",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = PrimaryNeon
                )
            }

            // Refresh button if prediction exists
            if (predictionState is com.Lyno.matchmindai.presentation.viewmodel.PredictionState.Success) {
                IconButton(
                    onClick = {
                        val leagueId = matchDetail.leagueId ?: return@IconButton
                        val season = Calendar.getInstance().get(Calendar.YEAR)
                        val homeTeamId = matchDetail.homeTeamId ?: return@IconButton
                        val awayTeamId = matchDetail.awayTeamId ?: return@IconButton
                        val homeTeamName = matchDetail.homeTeam ?: return@IconButton
                        val awayTeamName = matchDetail.awayTeam ?: return@IconButton

                        // Use AI-validated prediction (Phase 3)
                        viewModel.loadPredictionWithAI(
                            leagueId = leagueId,
                            season = season,
                            homeTeamId = homeTeamId,
                            awayTeamId = awayTeamId,
                            homeTeamName = homeTeamName,
                            awayTeamName = awayTeamName,
                            fixtureId = matchDetail.fixtureId
                        )
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Vernieuw voorspelling",
                        tint = PrimaryNeon
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (val state = predictionState) {
            is com.Lyno.matchmindai.presentation.viewmodel.PredictionState.Loading -> {
                LoadingPredictionState()
            }

            is com.Lyno.matchmindai.presentation.viewmodel.PredictionState.Success -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PredictionSuccessState(
                        analysis = state.data,
                        aiValidationState = aiValidationState,
                        matchScenarioState = matchScenarioState,
                        matchDetail = matchDetail,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Save Prediction Button (prominent, below OracleHeroCard)
                    SavePredictionButton(
                        matchDetail = matchDetail,
                        viewModel = viewModel,
                        analysis = state.data
                    )
                }
            }

            is com.Lyno.matchmindai.presentation.viewmodel.PredictionState.Error -> {
                PredictionErrorState(
                    errorMessage = state.message,
                    onRetry = {
                        val leagueId = matchDetail.leagueId ?: return@PredictionErrorState
                        val season = Calendar.getInstance().get(Calendar.YEAR)
                        val homeTeamId = matchDetail.homeTeamId ?: return@PredictionErrorState
                        val awayTeamId = matchDetail.awayTeamId ?: return@PredictionErrorState
                        val homeTeamName = matchDetail.homeTeam ?: return@PredictionErrorState
                        val awayTeamName = matchDetail.awayTeam ?: return@PredictionErrorState

                        // Use AI-validated prediction (Phase 3)
                        viewModel.loadPredictionWithAI(
                            leagueId = leagueId,
                            season = season,
                            homeTeamId = homeTeamId,
                            awayTeamId = awayTeamId,
                            homeTeamName = homeTeamName,
                            awayTeamName = awayTeamName,
                            fixtureId = matchDetail.fixtureId
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            is com.Lyno.matchmindai.presentation.viewmodel.PredictionState.Idle -> {
                EmptyPredictionState(
                    onLoadPrediction = {
                        val leagueId = matchDetail.leagueId ?: return@EmptyPredictionState
                        val season = Calendar.getInstance().get(Calendar.YEAR)
                        val homeTeamId = matchDetail.homeTeamId ?: return@EmptyPredictionState
                        val awayTeamId = matchDetail.awayTeamId ?: return@EmptyPredictionState
                        val homeTeamName = matchDetail.homeTeam ?: return@EmptyPredictionState
                        val awayTeamName = matchDetail.awayTeam ?: return@EmptyPredictionState

                        // Use AI-validated prediction (Phase 3)
                        viewModel.loadPredictionWithAI(
                            leagueId = leagueId,
                            season = season,
                            homeTeamId = homeTeamId,
                            awayTeamId = awayTeamId,
                            homeTeamName = homeTeamName,
                            awayTeamName = awayTeamName,
                            fixtureId = matchDetail.fixtureId
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}


/**
 * Loading state for Oracle prediction.
 */
@Composable
private fun LoadingPredictionState() {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            CircularProgressIndicator(color = PrimaryNeon)
            Text(
                text = "Oracle Engine berekent voorspelling...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Analyseert stand, punten en doelpuntenverschil",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Success state showing Oracle prediction with Power Bar visualization and AI Intelligence Layer.
 * Updated for Phase 6: Clear distinction between Oracle and Tesseract scores.
 */
@Composable
private fun PredictionSuccessState(
    analysis: com.Lyno.matchmindai.domain.model.OracleAnalysis,
    aiValidationState: AiValidationState,
    matchScenarioState: com.Lyno.matchmindai.presentation.viewmodel.MatchScenarioState,
    matchDetail: com.Lyno.matchmindai.domain.model.MatchDetail,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. OracleHeroCard - Primary Oracle prediction (e.g., 2-1)
        OracleHeroCard(
            analysis = analysis,
            tesseractResult = analysis.tesseract,
            modifier = Modifier.fillMaxWidth()
        )

        // 2. Tesseract Simulation Card - Separate card for Tesseract score (e.g., 1-1)
        analysis.tesseract?.let { tesseractResult ->
            TesseractSimulationCard(
                tesseractResult = tesseractResult,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 3. Mastermind Card - The "Golden Tip" decision from Mastermind Engine
        analysis.mastermindSignal?.let { mastermindSignal ->
            MastermindCard(
                mastermindSignal = mastermindSignal,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 4. AI Intelligence Layer Section
        when (aiValidationState) {
            is AiValidationState.AnalyzingNews -> {
                AiAnalyzingNewsState()
            }
            is AiValidationState.Complete -> {
                AiValidationCompleteState(
                    adjustment = aiValidationState.adjustment,
                    finalAnalysis = aiValidationState.finalAnalysis
                )
            }
            is AiValidationState.Error -> {
                AiValidationErrorState(
                    errorMessage = aiValidationState.message
                )
            }
            else -> {
                // No AI validation state to show
            }
        }

            // 5. SignalDashboard - Enhanced with ChiChi team analysis
            analysis.simulationContext?.let { simulationContext ->
                SignalDashboard(
                    simulationContext = simulationContext,
                    aiReasoning = analysis.reasoning,
                    homeTeamName = matchDetail.homeTeam ?: "Home Team",
                    awayTeamName = matchDetail.awayTeam ?: "Away Team",
                    modifier = Modifier.fillMaxWidth()
                )
            }

        // 6. Oracle Vision Card (keep existing)
        OracleVisionCard(
            matchScenarioState = matchScenarioState,
            homeTeamName = "", // Will be filled by caller
            awayTeamName = "", // Will be filled by caller
            predictedScore = analysis.prediction,
            onGenerateScenario = {
                // This will be triggered when needed
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Confidence badge showing prediction certainty.
 */
@Composable
private fun ConfidenceBadge(confidence: Int) {
    val confidenceColor = when {
        confidence >= 80 -> PrimaryNeon
        confidence >= 60 -> Color(0xFFFFA726) // Orange
        else -> Color(0xFFEF5350) // Red
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = confidenceColor.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, confidenceColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Zekerheid:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$confidence%",
                style = MaterialTheme.typography.labelMedium,
                color = confidenceColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Oracle Power Bar visualization showing relative team strength.
 * Uses normalized values (0-100%) for visual comparison.
 */
@Composable
private fun OraclePowerBar(
    homePower: Int,
    awayPower: Int,
    modifier: Modifier = Modifier
) {
    val totalPower = homePower + awayPower
    val homePercentage = if (totalPower > 0) homePower.toFloat() / totalPower else 0.5f
    val awayPercentage = if (totalPower > 0) awayPower.toFloat() / totalPower else 0.5f

    Box(
        modifier = modifier
            .height(48.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(SurfaceCard)
    ) {
        // Home team power (left side)
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = homePercentage)
                .fillMaxHeight()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            PrimaryNeon.copy(alpha = 0.8f),
                            PrimaryNeon.copy(alpha = 0.4f)
                        )
                    )
                )
        )

        // Away team power (right side)
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = awayPercentage)
                .fillMaxHeight()
                .align(Alignment.CenterEnd)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            SecondaryPurple.copy(alpha = 0.4f),
                            SecondaryPurple.copy(alpha = 0.8f)
                        ),
                        startX = 0f,
                        endX = Float.POSITIVE_INFINITY
                    )
                )
        )

        // Center separator line
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(2.dp)
                .align(Alignment.Center)
                .background(Color.White.copy(alpha = 0.3f))
        )
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
    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "⚠️",
                fontSize = 48.sp,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Voorspelling mislukt",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error
                )

                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryNeon,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "Probeer opnieuw",
                    fontWeight = FontWeight.Bold
                )
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
    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "🔮",
                fontSize = 48.sp,
                color = PrimaryNeon.copy(alpha = 0.5f)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Oracle Engine",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = PrimaryNeon
                )

                Text(
                    text = "Krijg een voorspelling gebaseerd op stand, punten en doelpuntenverschil",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick = onLoadPrediction,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryNeon,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "Laad voorspelling",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * AI Validation Badge showing current validation status.
 */
@Composable
private fun AiValidationBadge(aiValidationState: AiValidationState) {
    val (text, color, icon) = when (aiValidationState) {
        is AiValidationState.AnalyzingNews -> Triple(
            stringResource(R.string.ai_searching_live_intel),
            Color(0xFFFFA726), // Orange
            Icons.Default.Warning
        )
        is AiValidationState.Complete -> {
            val adjustment = aiValidationState.adjustment
            if (adjustment?.hasScoreChange == true) {
                Triple(
                    stringResource(R.string.ai_adjusted),
                    Color(0xFFFFA726), // Orange
                    Icons.Default.Warning
                )
            } else {
                Triple(
                    stringResource(R.string.ai_verified),
                    Color(0xFF4CAF50), // Green
                    Icons.Default.CheckCircle
                )
            }
        }
        is AiValidationState.Error -> Triple(
            stringResource(R.string.ai_error),
            Color(0xFFEF5350), // Red
            Icons.Default.Warning
        )
        else -> Triple(
            stringResource(R.string.ai_waiting),
            Color(0xFF9E9E9E), // Gray
            Icons.Default.Warning
        )
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, color)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "AI Validation Status",
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * AI Analyzing News State (pulsing loader).
 */
@Composable
private fun AiAnalyzingNewsState() {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Pulsing dot animation
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = PrimaryNeon,
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                )
                Text(
                    text = stringResource(R.string.intelligence_layer_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = PrimaryNeon,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = stringResource(R.string.ai_searching_live_intel),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.ai_checking_news),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = PrimaryNeon
            )
        }
    }
}

/**
 * AI Validation Complete State with adjustment details.
 */
@Composable
private fun AiValidationCompleteState(
    adjustment: com.Lyno.matchmindai.domain.model.OracleAdjustment?,
    finalAnalysis: com.Lyno.matchmindai.domain.model.OracleAnalysis
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (adjustment?.hasScoreChange == true) Icons.Default.Warning else Icons.Default.CheckCircle,
                    contentDescription = "AI Validation Result",
                    tint = if (adjustment?.hasScoreChange == true) Color(0xFFFFA726) else Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "🤖 AI INTELLIGENCE LAYER",
                    style = MaterialTheme.typography.labelMedium,
                    color = PrimaryNeon,
                    fontWeight = FontWeight.Bold
                )
            }

            if (adjustment?.hasScoreChange == true) {
                // Adjusted score
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.score_adjusted),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFFFA726),
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = stringResource(R.string.based_on_news),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = adjustment.reasoning,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            } else {
                // Verified score
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.ai_validated),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = stringResource(R.string.no_critical_news),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = adjustment?.reasoning ?: stringResource(R.string.statistical_confirmed),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }

            Divider(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                thickness = 1.dp
            )

            Text(
                text = stringResource(R.string.final_prediction, finalAnalysis.prediction, finalAnalysis.confidence),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * AI Validation Error State.
 */
@Composable
private fun AiValidationErrorState(errorMessage: String) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "AI Validation Error",
                    tint = Color(0xFFEF5350),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = stringResource(R.string.ai_validation_error),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFEF5350),
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.using_base_prediction),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Oracle Vision Card - Shows the AI-generated match scenario narrative.
 * Features a shimmering/pulsing loading effect while generating.
 */
@Composable
private fun OracleVisionCard(
    matchScenarioState: com.Lyno.matchmindai.presentation.viewmodel.MatchScenarioState,
    homeTeamName: String,
    awayTeamName: String,
    predictedScore: String,
    onGenerateScenario: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with eye icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "👁️",
                    fontSize = 20.sp
                )
                Text(
                    text = "HET ORACLE VISIOEN",
                    style = MaterialTheme.typography.labelMedium,
                    color = PrimaryNeon,
                    fontWeight = FontWeight.Bold
                )
            }

            // Content based on state
            when (matchScenarioState) {
                is com.Lyno.matchmindai.presentation.viewmodel.MatchScenarioState.Idle -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "De Oracle analyseert de tijdlijnen...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Button(
                            onClick = onGenerateScenario,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryNeon,
                                contentColor = Color.Black
                            )
                        ) {
                            Text(
                                text = "Genereer Visioen",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                is com.Lyno.matchmindai.presentation.viewmodel.MatchScenarioState.Loading -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Shimmering/pulsing text effect
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            PrimaryNeon.copy(alpha = 0.1f),
                                            PrimaryNeon.copy(alpha = 0.3f),
                                            PrimaryNeon.copy(alpha = 0.1f)
                                        ),
                                        startY = 0f,
                                        endY = Float.POSITIVE_INFINITY
                                    )
                                )
                                .clip(RoundedCornerShape(8.dp))
                        )

                        Text(
                            text = "Oracle analyseert tijdlijnen...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PrimaryNeon,
                            textAlign = TextAlign.Center
                        )

                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = PrimaryNeon
                        )
                    }
                }

                is com.Lyno.matchmindai.presentation.viewmodel.MatchScenarioState.Success -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = matchScenarioState.scenario,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Center
                        )

                        Divider(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                            thickness = 1.dp
                        )

                        Text(
                            text = "🔮 DeepSeek AI - Gebaseerd op statistiek en tactische analyse",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontStyle = FontStyle.Italic,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                is com.Lyno.matchmindai.presentation.viewmodel.MatchScenarioState.Error -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚠️ Kon visioen niet genereren",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = matchScenarioState.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Button(
                            onClick = onGenerateScenario,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryNeon,
                                contentColor = Color.Black
                            )
                        ) {
                            Text(
                                text = "Probeer opnieuw",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tesseract Monte Carlo Visualization Section.
 * Shows animated probability bar and context grid when Tesseract data is available.
 */
@Composable
private fun TesseractVisualizationSection(
    analysis: com.Lyno.matchmindai.domain.model.OracleAnalysis,
    modifier: Modifier = Modifier
) {
    val tesseractResult = analysis.tesseract

    if (tesseractResult != null) {
        GlassCard(modifier = modifier) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Probability Bar for Monte Carlo distribution
                ProbabilityBar(
                    tesseractResult = tesseractResult,
                    modifier = Modifier.fillMaxWidth()
                )

                // Context Grid for fitness and distraction metrics
                // Use the simulationContext from the analysis if available
                val simulationContext = analysis.simulationContext ?: SimulationContext.NEUTRAL

                ContextGrid(
                    simulationContext = simulationContext,
                    modifier = Modifier.fillMaxWidth()
                )

                // Simulation info footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🧊 Monte Carlo Simulatie",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "${tesseractResult.simulationCount} iteraties",
                        style = MaterialTheme.typography.labelSmall,
                        color = PrimaryNeon,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    } else {
        // Show loading/placeholder state when Tesseract data is not available
        GlassCard(modifier = modifier) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Shimmer effect placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    SurfaceCard.copy(alpha = 0.3f),
                                    SurfaceCard.copy(alpha = 0.5f),
                                    SurfaceCard.copy(alpha = 0.3f)
                                )
                            )
                        )
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.tesseract_loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = stringResource(R.string.tesseract_no_data_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }

                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = PrimaryNeon
                )
            }
        }
    }
}

    /**
     * Save Prediction Button - Prominent button for saving predictions to history.
     * Shows different states: Unsaved (enabled) and Saved (disabled with checkmark).
     */
    @Composable
    private fun SavePredictionButton(
        matchDetail: com.Lyno.matchmindai.domain.model.MatchDetail,
        viewModel: com.Lyno.matchmindai.presentation.viewmodel.PredictionViewModel,
        analysis: com.Lyno.matchmindai.domain.model.OracleAnalysis
    ) {
        var isSaved by remember { mutableStateOf(false) }
        val snackbarHostState = remember { SnackbarHostState() }

        // Get snackbar message outside of coroutine scope
        val savedMessage = stringResource(R.string.prediction_saved_with_score, analysis.prediction)
        val savedButtonText = stringResource(R.string.prediction_saved)
        val saveButtonText = stringResource(R.string.save_prediction_button)
        val hintText = stringResource(R.string.save_prediction_hint)

        // Show snackbar when saved
        androidx.compose.runtime.LaunchedEffect(isSaved) {
            if (isSaved) {
                snackbarHostState.showSnackbar(
                    message = savedMessage,
                    duration = SnackbarDuration.Short
                )
            }
        }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = {
                val fixtureId = matchDetail.fixtureId ?: return@Button
                val homeTeamId = matchDetail.homeTeamId ?: return@Button
                val awayTeamId = matchDetail.awayTeamId ?: return@Button
                val homeTeamName = matchDetail.homeTeam ?: return@Button
                val awayTeamName = matchDetail.awayTeam ?: return@Button

                // Extract probabilities from analysis
                val homeProb = analysis.tesseract?.homeWinProbability ?: 0.33
                val drawProb = analysis.tesseract?.drawProbability ?: 0.33
                val awayProb = analysis.tesseract?.awayWinProbability ?: 0.33

                viewModel.savePrediction(
                    fixtureId = fixtureId,
                    homeTeamId = homeTeamId,
                    awayTeamId = awayTeamId,
                    homeTeamName = homeTeamName,
                    awayTeamName = awayTeamName,
                    predictedScore = analysis.prediction,
                    confidence = analysis.confidence
                )
                isSaved = true
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaved,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSaved) Color(0xFF4CAF50) else PrimaryNeon,
                contentColor = if (isSaved) Color.White else Color.Black
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Default.CheckCircle else Icons.Default.Save,
                    contentDescription = if (isSaved) "Voorspelling opgeslagen" else "Voorspelling opslaan"
                )
                Text(
                    text = if (isSaved) savedButtonText else saveButtonText,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Snackbar host for feedback
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.fillMaxWidth()
        )

        // Hint text
        if (!isSaved) {
            Text(
                text = hintText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
