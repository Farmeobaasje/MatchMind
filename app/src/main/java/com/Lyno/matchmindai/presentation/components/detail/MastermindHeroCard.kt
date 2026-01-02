package com.Lyno.matchmindai.presentation.components.detail

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Lyno.matchmindai.R
import com.Lyno.matchmindai.domain.model.MastermindSignal
import com.Lyno.matchmindai.domain.model.LLMGradeEnhancement
import com.Lyno.matchmindai.ui.theme.PrimaryNeon
import com.Lyno.matchmindai.ui.theme.SurfaceCard
import com.Lyno.matchmindai.ui.theme.WarningYellow
import com.Lyno.matchmindai.ui.theme.DangerRed

/**
 * MastermindHeroCard - The "Golden Tip" hero card showing the Mastermind's final verdict.
 * Features a large glassmorphism card with pulsing border for high-confidence signals.
 * This is the most prominent element in the unified prediction view.
 *
 * Visual Hierarchy:
 * 1. "MATCHMIND VERDICT" title with brain icon
 * 2. Large, impactful signal title (e.g., "WINST THUIS")
 * 3. Confidence level with color-coded badge
 * 4. Short reasoning summary
 * 5. Recommendation for betting action
 * 6. Save prediction button
 */
@Composable
fun MastermindHeroCard(
    mastermindSignal: MastermindSignal,
    llmGradeEnhancement: LLMGradeEnhancement? = null,
    onSavePrediction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()

    // Pulsing border animation for "Banker" signals
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(
            width = if (mastermindSignal.isBanker) 2.dp else 1.dp,
            color = mastermindSignal.toComposeColor().copy(
                alpha = if (mastermindSignal.isBanker) borderAlpha else 0.5f
            )
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            SurfaceCard.copy(alpha = 0.2f),
                            SurfaceCard.copy(alpha = 0.1f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header with brain icon and "MATCHMIND VERDICT" title
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Mastermind Brain",
                            tint = PrimaryNeon,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = stringResource(R.string.mastermind_verdict_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = PrimaryNeon,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Subtitle indicating this is the "Golden Tip"
                    Text(
                        text = stringResource(R.string.golden_tip_subtitle),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                // Large verdict title
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mastermindSignal.title,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = mastermindSignal.toComposeColor(),
                        textAlign = TextAlign.Center
                    )
                }

                // Confidence badge - larger and more prominent
                HeroConfidenceBadge(
                    confidence = mastermindSignal.confidence,
                    color = mastermindSignal.toComposeColor(),
                    modifier = Modifier.fillMaxWidth()
                )

                // Reasoning summary with LLMGRADE insights
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = mastermindSignal.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Show LLMGRADE insights if available
                    llmGradeEnhancement?.let { enhancement ->
                        LLMGradeInsightsSection(
                            enhancement = enhancement,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Recommendation and Save button row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Recommendation badge
                    HeroRecommendationBadge(
                        recommendation = mastermindSignal.recommendation,
                        color = mastermindSignal.toComposeColor(),
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Save Prediction Button
                    Button(
                        onClick = onSavePrediction,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryNeon,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Save",
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(R.string.save_prediction_button),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Scenario type indicator
                HeroScenarioTypeIndicator(
                    scenarioType = mastermindSignal.scenarioType,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * LLMGRADE Insights Section - Shows context factors and outlier scenarios.
 */
@Composable
private fun LLMGradeInsightsSection(
    enhancement: LLMGradeEnhancement,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Context Factors Summary
        if (enhancement.contextFactors.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎯",
                    fontSize = 16.sp
                )
                Text(
                    text = "LLMGRADE Insights",
                    style = MaterialTheme.typography.labelMedium,
                    color = PrimaryNeon,
                    fontWeight = FontWeight.Bold
                )
            }

            // Show most impactful factor
            enhancement.mostImpactfulFactor?.let { factor ->
                Text(
                    text = "• ${factor.type.dutchDescription()}: ${factor.score}/10",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Show high-impact factor count
            val highImpactCount = enhancement.contextFactors.count { it.isHighImpact }
            if (highImpactCount > 0) {
                Text(
                    text = "• $highImpactCount hoge-impact factoren",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Outlier Scenarios Warning
        if (enhancement.hasHighProbabilityOutliers) {
            val outlier = enhancement.highestProbabilityOutlier
            if (outlier != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚠️",
                        fontSize = 16.sp,
                        color = DangerRed
                    )
                    Text(
                        text = "Hoog-risico uitschieter",
                        style = MaterialTheme.typography.labelMedium,
                        color = DangerRed,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "${outlier.description.take(60)}... (${outlier.probability.toInt()}% kans)",
                    style = MaterialTheme.typography.labelSmall,
                    color = WarningYellow,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Confidence adjustment note
        val adjustment = enhancement.confidenceAdjustment
        if (adjustment != 0) {
            val sign = if (adjustment > 0) "+" else ""
            Text(
                text = "• Vertrouwensaanpassing: $sign$adjustment%",
                style = MaterialTheme.typography.labelSmall,
                color = if (adjustment > 0) PrimaryNeon else DangerRed,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Preview function for MastermindHeroCard.
 */
@Composable
fun MastermindHeroCardPreview() {
    val sampleSignal = com.Lyno.matchmindai.domain.model.MastermindSignal(
        title = "WINST THUIS",
        description = "Data en fitheid wijzen unaniem naar thuiswinst. Oracle (75%) en Tesseract simulaties (68%) bevestigen dit patroon.",
        color = com.Lyno.matchmindai.domain.model.SignalColor.GREEN,
        confidence = 85,
        recommendation = "Thuis Winst",
        scenarioType = com.Lyno.matchmindai.domain.model.ScenarioType.BANKER
    )

    val sampleEnhancement = com.Lyno.matchmindai.domain.model.LLMGradeEnhancement(
        contextFactors = listOf(
            com.Lyno.matchmindai.domain.model.ContextFactor(
                type = com.Lyno.matchmindai.domain.model.ContextFactorType.INJURIES,
                score = 8,
                description = "Thuisploeg mist 3 basisspelers",
                weight = 0.8
            )
        ),
        outlierScenarios = listOf(
            com.Lyno.matchmindai.domain.model.OutlierScenario(
                description = "Uitploeg scoort vroeg en houdt voorsprong",
                probability = 0.25,
                supportingFactors = listOf("Sterke uitwedstrijd statistieken"),
                historicalPrecedents = listOf("Vorige seizoen: 2-0 uitwinst"),
                impactScore = 7
            )
        ),
        enhancedReasoning = "LLM analyse voltooid",
        confidenceAdjustment = 5
    )

    MastermindHeroCard(
        mastermindSignal = sampleSignal,
        llmGradeEnhancement = sampleEnhancement,
        onSavePrediction = { /* Preview only */ },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}
