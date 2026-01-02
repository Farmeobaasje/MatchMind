package com.Lyno.matchmindai.presentation.components.detail

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Lyno.matchmindai.R
import com.Lyno.matchmindai.domain.model.MastermindSignal
import com.Lyno.matchmindai.presentation.components.GlassCard
import com.Lyno.matchmindai.ui.theme.PrimaryNeon
import com.Lyno.matchmindai.ui.theme.SecondaryPurple
import com.Lyno.matchmindai.ui.theme.SurfaceCard

/**
 * MastermindCard - The "Brain" card showing the Mastermind's "Golden Tip" decision.
 * Features a "Cyber-Brain" aesthetic with pulsing border for high-confidence signals.
 * 
 * Visual Hierarchy:
 * 1. Brain icon + "MASTERMIND CONCLUSIE" title
 * 2. Large, impactful signal title (e.g., "ZEKERHEIDJE")
 * 3. Detailed description in Dutch
 * 4. Confidence badge with color coding
 * 5. Recommendation for betting action
 */
@Composable
fun MastermindCard(
    mastermindSignal: MastermindSignal,
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
        border = if (mastermindSignal.isBanker) {
            BorderStroke(
                width = 2.dp,
                color = mastermindSignal.toComposeColor().copy(alpha = borderAlpha)
            )
        } else {
            BorderStroke(
                width = 1.dp,
                color = mastermindSignal.toComposeColor().copy(alpha = 0.5f)
            )
        }
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with brain icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "Mastermind Brain",
                        tint = PrimaryNeon,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = stringResource(R.string.mastermind_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = PrimaryNeon,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Signal title with glow effect for high confidence
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .drawWithCache {
                            onDrawWithContent {
                                if (mastermindSignal.confidence >= 70) {
                                    // Draw glow effect for high confidence signals
                                    for (i in 1..3) {
                                        val alpha = 0.2f - (i * 0.05f)
                                        drawCircle(
                                            color = mastermindSignal.toComposeColor().copy(alpha = alpha),
                                            radius = size.minDimension / 2 + (i * 6).dp.toPx(),
                                            center = Offset(size.width / 2, size.height / 2)
                                        )
                                    }
                                }
                                drawContent()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mastermindSignal.title,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = mastermindSignal.toComposeColor(),
                        textAlign = TextAlign.Center
                    )
                }

                // Description
                Text(
                    text = mastermindSignal.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                // Confidence and recommendation row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Confidence badge
                    ConfidenceBadge(
                        confidence = mastermindSignal.confidence,
                        color = mastermindSignal.toComposeColor(),
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Recommendation badge
                    RecommendationBadge(
                        recommendation = mastermindSignal.recommendation,
                        color = mastermindSignal.toComposeColor(),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Scenario type indicator
                ScenarioTypeIndicator(
                    scenarioType = mastermindSignal.scenarioType,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Confidence badge showing the Mastermind's confidence level.
 */
@Composable
fun ConfidenceBadge(
    confidence: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, color),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.confidence_label_dutch),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$confidence%",
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Recommendation badge showing the betting recommendation.
 */
@Composable
fun RecommendationBadge(
    recommendation: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, color),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "AANBEVELING",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = recommendation,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Scenario type indicator showing what type of scenario this is.
 */
@Composable
fun ScenarioTypeIndicator(
    scenarioType: com.Lyno.matchmindai.domain.model.ScenarioType,
    modifier: Modifier = Modifier
) {
    val (icon, description, color) = when (scenarioType) {
        com.Lyno.matchmindai.domain.model.ScenarioType.BANKER -> Triple(
            Icons.Default.Star,
            "Zekerheidje - Meerdere indicatoren wijzen dezelfde richting",
            Color(0xFF4CAF50) // Green
        )
        com.Lyno.matchmindai.domain.model.ScenarioType.HIGH_RISK -> Triple(
            Icons.Default.Warning,
            "Hoog Risico - Tegenstrijdige signalen, extra voorzichtigheid",
            Color(0xFFFFA726) // Orange/Yellow
        )
        com.Lyno.matchmindai.domain.model.ScenarioType.GOALS_FESTIVAL -> Triple(
            Icons.Default.Star,
            "Doelpunten Festijn - Hoge kans op veel doelpunten",
            Color(0xFF4CAF50) // Green
        )
        com.Lyno.matchmindai.domain.model.ScenarioType.TACTICAL_DUEL -> Triple(
            Icons.Default.Psychology,
            "Tactisch Duel - Kleine marge, tactiek bepaalt uitkomst",
            Color(0xFFFFA726) // Orange/Yellow
        )
        com.Lyno.matchmindai.domain.model.ScenarioType.DEFENSIVE_BATTLE -> Triple(
            Icons.Default.Warning,
            "Defensieve Strijd - Lage score verwacht",
            Color(0xFFFFA726) // Orange/Yellow
        )
        com.Lyno.matchmindai.domain.model.ScenarioType.VALUE_BET -> Triple(
            Icons.Default.Star,
            "Value Bet - Goede odds waarde ondanks risico",
            Color(0xFF4CAF50) // Green
        )
    }
    
    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.1f),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Scenario Type",
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                maxLines = 2
            )
        }
    }
}

/**
 * Preview function for MastermindCard.
 */
@Composable
fun MastermindCardPreview() {
    val sampleSignal = com.Lyno.matchmindai.domain.model.MastermindSignal(
        title = "ZEKERHEIDJE",
        description = "Data en fitheid wijzen unaniem naar thuiswinst. Oracle (75%) en Tesseract simulaties (68%) bevestigen dit patroon.",
        color = com.Lyno.matchmindai.domain.model.SignalColor.GREEN,
        confidence = 85,
        recommendation = "Thuis Winst",
        scenarioType = com.Lyno.matchmindai.domain.model.ScenarioType.BANKER
    )
    
    MastermindCard(
        mastermindSignal = sampleSignal,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}
