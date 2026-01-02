package com.Lyno.matchmindai.presentation.components.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.Lyno.matchmindai.R
import com.Lyno.matchmindai.domain.model.MatchDetail
import com.Lyno.matchmindai.presentation.components.GlassCard
import com.Lyno.matchmindai.presentation.viewmodel.PredictionState
import com.Lyno.matchmindai.presentation.viewmodel.PredictionViewModel
import com.Lyno.matchmindai.ui.theme.PrimaryNeon
import com.Lyno.matchmindai.ui.theme.SecondaryPurple
import com.Lyno.matchmindai.ui.theme.SurfaceCard

/**
 * BettingTipsTab - Smart betting tips based on Tesseract Monte Carlo simulations.
 * Shows BTTS (Both Teams To Score), Over/Under 2.5 goals, and Correct Score probabilities.
 */
@Composable
fun BettingTipsTab(
    matchDetail: MatchDetail,
    viewModel: PredictionViewModel
) {
    // Load prediction state to get Tesseract data
    val predictionState by viewModel.predictionState.collectAsStateWithLifecycle()

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
                    imageVector = Icons.Default.AttachMoney,
                    contentDescription = "Betting Tips",
                    tint = PrimaryNeon,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = stringResource(R.string.betting_tips_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = PrimaryNeon
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (val state = predictionState) {
            is PredictionState.Success -> {
                val tesseractResult = state.data.tesseract
                if (tesseractResult != null) {
                    // Show betting tips when Tesseract data is available
                    BettingTipsContent(
                        tesseractResult = tesseractResult,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // No Tesseract data available
                    NoTesseractDataState(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            is PredictionState.Loading -> {
                LoadingBettingTipsState(
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            else -> {
                // Prediction not loaded yet
                EmptyBettingTipsState(
                    onLoadPrediction = {
                        // Trigger prediction load if needed
                        // This would be handled by the parent screen
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Content showing all betting tips when Tesseract data is available.
 */
@Composable
private fun BettingTipsContent(
    tesseractResult: com.Lyno.matchmindai.domain.model.TesseractResult,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. BTTS (Both Teams To Score) Card
        BttsCard(
            tesseractResult = tesseractResult,
            modifier = Modifier.fillMaxWidth()
        )

        // 2. Total Goals (Over/Under 2.5) Card
        OverUnderCard(
            tesseractResult = tesseractResult,
            modifier = Modifier.fillMaxWidth()
        )

        // 3. Correct Score Card
        CorrectScoreCard(
            tesseractResult = tesseractResult,
            modifier = Modifier.fillMaxWidth()
        )

        // 4. Simulation Info Footer
        SimulationInfoFooter(
            tesseractResult = tesseractResult,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * BTTS (Both Teams To Score) Card with probability visualization.
 */
@Composable
private fun BttsCard(
    tesseractResult: com.Lyno.matchmindai.domain.model.TesseractResult,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SportsSoccer,
                    contentDescription = "BTTS",
                    tint = PrimaryNeon,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = stringResource(R.string.btts_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = PrimaryNeon,
                    fontWeight = FontWeight.Bold
                )
            }

            // Probability bars
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Yes bar
                BttsProbabilityBar(
                    label = stringResource(R.string.btts_yes),
                    probability = tesseractResult.bttsProbability,
                    isFavorite = tesseractResult.isBttsYesFavorite,
                    isYes = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // No bar
                BttsProbabilityBar(
                    label = stringResource(R.string.btts_no),
                    probability = tesseractResult.bttsNoProbability,
                    isFavorite = !tesseractResult.isBttsYesFavorite,
                    isYes = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Summary
            Text(
                text = if (tesseractResult.isBttsYesFavorite) {
                    stringResource(R.string.btts_yes_likely, tesseractResult.bttsPercentage)
                } else {
                    stringResource(R.string.btts_no_likely, tesseractResult.bttsPercentage)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Over/Under 2.5 Goals Card with probability visualization.
 */
@Composable
private fun OverUnderCard(
    tesseractResult: com.Lyno.matchmindai.domain.model.TesseractResult,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = "Over/Under",
                    tint = PrimaryNeon,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = stringResource(R.string.total_goals_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = PrimaryNeon,
                    fontWeight = FontWeight.Bold
                )
            }

            // Probability bars
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Over 2.5 bar
                OverUnderProbabilityBar(
                    label = stringResource(R.string.over_2_5),
                    probability = tesseractResult.over2_5Probability,
                    isFavorite = tesseractResult.isOver2_5Favorite,
                    isOver = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Under 2.5 bar
                OverUnderProbabilityBar(
                    label = stringResource(R.string.under_2_5),
                    probability = tesseractResult.under2_5Probability,
                    isFavorite = !tesseractResult.isOver2_5Favorite,
                    isOver = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Summary
            Text(
                text = if (tesseractResult.isOver2_5Favorite) {
                    stringResource(R.string.over_likely, tesseractResult.over2_5Percentage)
                } else {
                    stringResource(R.string.under_likely, tesseractResult.under2_5Percentage)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Correct Score Card showing top 3 most likely scores.
 */
@Composable
private fun CorrectScoreCard(
    tesseractResult: com.Lyno.matchmindai.domain.model.TesseractResult,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🎯",
                    fontSize = 20.sp
                )
                Text(
                    text = stringResource(R.string.correct_score_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = PrimaryNeon,
                    fontWeight = FontWeight.Bold
                )
            }

            // Top 3 scores
            val topScores = tesseractResult.getTopScoresWithPercentages()
            
            if (topScores.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    topScores.forEachIndexed { index, (score, percentage) ->
                        CorrectScoreRow(
                            rank = index + 1,
                            score = score,
                            percentage = percentage,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Most likely score highlight
                Text(
                    text = stringResource(R.string.most_likely_score_label, tesseractResult.mostLikelyScore),
                    style = MaterialTheme.typography.bodyMedium,
                    color = PrimaryNeon,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // No score data
                Text(
                    text = stringResource(R.string.no_score_data),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * BTTS Probability Bar visualization.
 */
@Composable
private fun BttsProbabilityBar(
    label: String,
    probability: Double,
    isFavorite: Boolean,
    isYes: Boolean,
    modifier: Modifier = Modifier
) {
    val percentage = (probability * 100).toInt()
    val barColor = if (isYes) PrimaryNeon else SecondaryPurple
    val favoriteColor = if (isFavorite) barColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isFavorite) barColor else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isFavorite) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.labelMedium,
                color = if (isFavorite) barColor else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isFavorite) FontWeight.Bold else FontWeight.Normal
            )
        }

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = probability.toFloat())
                    .fillMaxHeight()
                    .clip(MaterialTheme.shapes.small)
                    .background(favoriteColor)
            )
        }
    }
}

/**
 * Over/Under Probability Bar visualization.
 */
@Composable
private fun OverUnderProbabilityBar(
    label: String,
    probability: Double,
    isFavorite: Boolean,
    isOver: Boolean,
    modifier: Modifier = Modifier
) {
    val percentage = (probability * 100).toInt()
    val barColor = if (isOver) PrimaryNeon else SecondaryPurple
    val favoriteColor = if (isFavorite) barColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isFavorite) barColor else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isFavorite) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.labelMedium,
                color = if (isFavorite) barColor else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isFavorite) FontWeight.Bold else FontWeight.Normal
            )
        }

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = probability.toFloat())
                    .fillMaxHeight()
                    .clip(MaterialTheme.shapes.small)
                    .background(favoriteColor)
            )
        }
    }
}

/**
 * Correct Score Row showing rank, score, and percentage.
 */
@Composable
private fun CorrectScoreRow(
    rank: Int,
    score: String,
    percentage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Rank badge
            Surface(
                shape = MaterialTheme.shapes.small,
                color = when (rank) {
                    1 -> PrimaryNeon.copy(alpha = 0.2f)
                    2 -> SecondaryPurple.copy(alpha = 0.2f)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                },
                border = BorderStroke(
                    1.dp,
                    when (rank) {
                        1 -> PrimaryNeon
                        2 -> SecondaryPurple
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    }
                )
            ) {
                Text(
                    text = "#$rank",
                    style = MaterialTheme.typography.labelSmall,
                    color = when (rank) {
                        1 -> PrimaryNeon
                        2 -> SecondaryPurple
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Score
            Text(
                text = score,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }

        // Percentage
        Text(
            text = "$percentage%",
            style = MaterialTheme.typography.titleMedium,
            color = PrimaryNeon,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Simulation Info Footer showing Monte Carlo simulation details.
 */
@Composable
private fun SimulationInfoFooter(
    tesseractResult: com.Lyno.matchmindai.domain.model.TesseractResult,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.monte_carlo_simulation_label),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "${tesseractResult.simulationCount} iteraties",
            style = MaterialTheme.typography.labelSmall,
            color = PrimaryNeon,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Loading state showing a spinner.
 */
@Composable
private fun LoadingBettingTipsState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(300.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = PrimaryNeon)
    }
}

/**
 * State showing when no Tesseract data is found.
 */
@Composable
private fun NoTesseractDataState(
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.SportsSoccer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Geen simulatie data beschikbaar",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Initial empty state.
 */
@Composable
private fun EmptyBettingTipsState(
    onLoadPrediction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(300.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Selecteer een wedstrijd om tips te laden...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
