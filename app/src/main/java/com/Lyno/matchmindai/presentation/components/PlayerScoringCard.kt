package com.Lyno.matchmindai.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.Lyno.matchmindai.R
import com.Lyno.matchmindai.domain.model.PlayerScoringProbability
import com.Lyno.matchmindai.ui.theme.MatchMindAITheme

/**
 * Composable card displaying a player's scoring probability.
 */
@Composable
fun PlayerScoringCard(
    player: PlayerScoringProbability,
    modifier: Modifier = Modifier,
    showDetails: Boolean = true
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header row with player name and probability
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = player.playerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = player.position,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                // Probability indicator
                ProbabilityIndicator(player = player)
            }

            // Probability bar
            ProbabilityBar(probability = player.adjustedProbability)

            if (showDetails) {
                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(
                        icon = R.drawable.ic_neon_football,
                        value = player.goalsPer90.format(2),
                        label = "Goals/90"
                    )
                    StatItem(
                        icon = R.drawable.ic_neon_football,
                        value = player.shotsOnTargetPer90.format(2),
                        label = "Shots/90"
                    )
                    StatItem(
                        icon = R.drawable.ic_neon_football,
                        value = player.minutesPerGame.toInt().toString(),
                        label = "Minutes"
                    )
                }

                // Form indicator
                FormIndicator(recentForm = player.recentForm, scoringStreak = player.scoringStreak)

                // Context indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (player.homeAdvantage) {
                        ContextChip(
                            text = "Home",
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            textColor = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (player.isPlaying) {
                        ContextChip(
                            text = "Starting",
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                            textColor = MaterialTheme.colorScheme.secondary
                        )
                    }
                    if (player.isInForm) {
                        ContextChip(
                            text = "In Form",
                            color = Color(0xFF4CAF50).copy(alpha = 0.2f),
                            textColor = Color(0xFF4CAF50)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Circular probability indicator.
 */
@Composable
private fun ProbabilityIndicator(player: PlayerScoringProbability) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(60.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(
                color = getProbabilityColor(player.adjustedProbability).copy(alpha = 0.2f)
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${player.adjustedProbability.toInt()}%",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = getProbabilityColor(player.adjustedProbability)
            )
            Text(
                text = player.probabilityDescription,
                style = MaterialTheme.typography.labelSmall,
                color = getProbabilityColor(player.adjustedProbability).copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * Horizontal probability bar.
 */
@Composable
private fun ProbabilityBar(probability: Double) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(probability.toFloat() / 100f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(getProbabilityColor(probability))
        )
    }
}

/**
 * Stat item with icon, value, and label.
 */
@Composable
private fun StatItem(
    icon: Int,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = label,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

/**
 * Form indicator showing recent goal scoring.
 */
@Composable
private fun FormIndicator(recentForm: List<Int>, scoringStreak: Int) {
    Column {
        Text(
            text = "Recent Form",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Last 5 matches
            for (i in 0 until 5) {
                val goals = recentForm.getOrNull(i) ?: 0
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when (goals) {
                                0 -> MaterialTheme.colorScheme.surfaceVariant
                                1 -> Color(0xFF4CAF50).copy(alpha = 0.3f)
                                2 -> Color(0xFF4CAF50).copy(alpha = 0.6f)
                                else -> Color(0xFF4CAF50).copy(alpha = 0.9f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (goals > 0) goals.toString() else "-",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (goals > 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            // Streak indicator
            if (scoringStreak > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                ContextChip(
                    text = "${scoringStreak} game streak",
                    color = Color(0xFF4CAF50).copy(alpha = 0.2f),
                    textColor = Color(0xFF4CAF50)
                )
            }
        }
    }
}

/**
 * Context chip for player status.
 */
@Composable
private fun ContextChip(
    text: String,
    color: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Gets color based on probability.
 */
private fun getProbabilityColor(probability: Double): Color {
    return when {
        probability >= 70 -> Color(0xFF4CAF50) // Green
        probability >= 50 -> Color(0xFF2196F3) // Blue
        probability >= 30 -> Color(0xFFFF9800) // Orange
        probability >= 15 -> Color(0xFFF44336) // Red
        else -> Color(0xFF9E9E9E) // Gray
    }
}

/**
 * Extension function to format doubles.
 */
private fun Double.format(digits: Int): String {
    return "%.${digits}f".format(this)
}

@Preview(showBackground = true)
@Composable
fun PlayerScoringCardPreview() {
    MatchMindAITheme {
        PlayerScoringCard(
            player = PlayerScoringProbability(
                playerId = 1,
                playerName = "Erling Haaland",
                teamId = 50,
                position = "ST",
                baseProbability = 85.0,
                adjustedProbability = 78.5,
                goalsPer90 = 1.2,
                shotsOnTargetPer90 = 2.5,
                minutesPerGame = 85.0,
                recentForm = listOf(1, 2, 0, 1, 1),
                isPlaying = true,
                homeAdvantage = true,
                scoringStreak = 2
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}
