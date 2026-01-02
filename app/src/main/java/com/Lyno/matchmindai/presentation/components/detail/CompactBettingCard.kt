package com.Lyno.matchmindai.presentation.components.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import com.Lyno.matchmindai.R
import com.Lyno.matchmindai.domain.model.TesseractResult
import com.Lyno.matchmindai.presentation.components.GlassCard
import com.Lyno.matchmindai.ui.theme.PrimaryNeon
import com.Lyno.matchmindai.ui.theme.SecondaryPurple

/**
 * CompactBettingCard - Shows top 3 betting insights in a compact form.
 * Includes: BTTS (Both Teams To Score), Over/Under 2.5 Goals, and #1 Correct Score.
 * 
 * Design: Three probability bars with clear visual indicators for favorite outcomes.
 */
@Composable
fun CompactBettingCard(
    tesseractResult: TesseractResult?,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AttachMoney,
                    contentDescription = "Betting Insights",
                    tint = PrimaryNeon,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "SMART BETTING INSIGHTS",
                    style = MaterialTheme.typography.labelLarge,
                    color = PrimaryNeon,
                    fontWeight = FontWeight.Bold
                )
            }

            // Subtitle
            Text(
                text = "Top 3 inzichten gebaseerd op 10k Monte Carlo simulaties",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            if (tesseractResult != null) {
                // Three betting insights
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 1. BTTS Insight
                    BettingInsightRow(
                        icon = Icons.Default.SportsSoccer,
                        title = stringResource(R.string.btts_title),
                        yesProbability = (tesseractResult.bttsProbability * 100).toInt(),
                        noProbability = (tesseractResult.bttsNoProbability * 100).toInt(),
                        isYesFavorite = tesseractResult.isBttsYesFavorite,
                        yesLabel = stringResource(R.string.btts_yes),
                        noLabel = stringResource(R.string.btts_no),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 2. Over/Under Insight
                    BettingInsightRow(
                        icon = Icons.Default.TrendingUp,
                        title = stringResource(R.string.total_goals_title),
                        yesProbability = (tesseractResult.over2_5Probability * 100).toInt(),
                        noProbability = (tesseractResult.under2_5Probability * 100).toInt(),
                        isYesFavorite = tesseractResult.isOver2_5Favorite,
                        yesLabel = stringResource(R.string.over_2_5),
                        noLabel = stringResource(R.string.under_2_5),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 3. Correct Score Insight
                    CorrectScoreInsight(
                        mostLikelyScore = tesseractResult.mostLikelyScore,
                        probability = tesseractResult.getTopScoresWithPercentages().firstOrNull()?.second ?: 0,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Simulation info footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
            } else {
                // No Tesseract data
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsSoccer,
                        contentDescription = "No Data",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = stringResource(R.string.no_tesseract_data_title),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.no_tesseract_data_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Row showing a betting insight with two probabilities (Yes/No or Over/Under).
 */
@Composable
private fun BettingInsightRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    yesProbability: Int,
    noProbability: Int,
    isYesFavorite: Boolean,
    yesLabel: String,
    noLabel: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Title row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = PrimaryNeon,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }

        // Probability bars
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Yes/Over bar
            ProbabilityBar(
                label = yesLabel,
                probability = yesProbability,
                isFavorite = isYesFavorite,
                isPositive = true,
                modifier = Modifier.weight(1f)
            )

            // No/Under bar
            ProbabilityBar(
                label = noLabel,
                probability = noProbability,
                isFavorite = !isYesFavorite,
                isPositive = false,
                modifier = Modifier.weight(1f)
            )
        }

        // Summary text
        val summaryText = if (isYesFavorite) {
            when (icon) {
                Icons.Default.SportsSoccer -> stringResource(R.string.btts_yes_likely, yesProbability)
                Icons.Default.TrendingUp -> stringResource(R.string.over_likely, yesProbability)
                else -> "$yesLabel is waarschijnlijker ($yesProbability%)"
            }
        } else {
            when (icon) {
                Icons.Default.SportsSoccer -> stringResource(R.string.btts_no_likely, noProbability)
                Icons.Default.TrendingUp -> stringResource(R.string.under_likely, noProbability)
                else -> "$noLabel is waarschijnlijker ($noProbability%)"
            }
        }

        Text(
            text = summaryText,
            style = MaterialTheme.typography.labelSmall,
            color = if (isYesFavorite) PrimaryNeon else SecondaryPurple,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Probability bar visualization for betting insights.
 */
@Composable
private fun ProbabilityBar(
    label: String,
    probability: Int,
    isFavorite: Boolean,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    val barColor = if (isPositive) PrimaryNeon else SecondaryPurple
    val favoriteColor = if (isFavorite) barColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Label and percentage
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isFavorite) barColor else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isFavorite) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = "$probability%",
                style = MaterialTheme.typography.labelSmall,
                color = if (isFavorite) barColor else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isFavorite) FontWeight.Bold else FontWeight.Normal
            )
        }

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = probability / 100f)
                    .fillMaxHeight()
                    .clip(MaterialTheme.shapes.small)
                    .background(favoriteColor)
            )
        }
    }
}

/**
 * Correct score insight showing the most likely score.
 */
@Composable
private fun CorrectScoreInsight(
    mostLikelyScore: String,
    probability: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "🎯",
                fontSize = 16.sp
            )
            Text(
                text = stringResource(R.string.correct_score_title),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }

        // Score and probability
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = PrimaryNeon.copy(alpha = 0.1f),
            border = BorderStroke(1.dp, PrimaryNeon)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Meest Waarschijnlijk",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = mostLikelyScore,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNeon
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Kans",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$probability%",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNeon
                    )
                }
            }
        }

        // Hint
        Text(
            text = stringResource(R.string.most_likely_score_label, mostLikelyScore),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Preview function for CompactBettingCard.
 */
@Composable
fun CompactBettingCardPreview() {
    val sampleTesseract = TesseractResult(
        homeWinProbability = 0.62,
        drawProbability = 0.20,
        awayWinProbability = 0.18,
        mostLikelyScore = "2-1",
        simulationCount = 10000,
        bttsProbability = 0.55,
        over2_5Probability = 0.65,
        topScoreDistribution = listOf(Pair("2-1", 1200), Pair("1-1", 800), Pair("2-0", 600))
    )
    
    CompactBettingCard(
        tesseractResult = sampleTesseract,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}
