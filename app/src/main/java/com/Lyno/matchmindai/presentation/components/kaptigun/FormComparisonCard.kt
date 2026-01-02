package com.Lyno.matchmindai.presentation.components.kaptigun

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.Lyno.matchmindai.domain.model.*

/**
 * Card comparing recent form of both teams.
 * Shows win/draw/loss streaks and efficiency analysis.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormComparisonCard(
    homeRecentForm: TeamRecentForm,
    awayRecentForm: TeamRecentForm,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recente Vorm",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "Laatste 5 wedstrijden",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Two columns for home and away teams
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Home team column
                TeamFormColumn(
                    teamForm = homeRecentForm,
                    isHomeTeam = true,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Away team column
                TeamFormColumn(
                    teamForm = awayRecentForm,
                    isHomeTeam = false,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Efficiency legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EfficiencyLegendItem(
                    icon = EfficiencyIcon.CLINICAL,
                    text = "Klinisch"
                )
                EfficiencyLegendItem(
                    icon = EfficiencyIcon.BALANCED,
                    text = "Gebalanceerd"
                )
                EfficiencyLegendItem(
                    icon = EfficiencyIcon.INEFFICIENT,
                    text = "Inefficiënt"
                )
            }
        }
    }
}

@Composable
private fun TeamFormColumn(
    teamForm: TeamRecentForm,
    isHomeTeam: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Team name
        Text(
            text = teamForm.teamName,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (isHomeTeam) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
            maxLines = 2
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Win streak
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            teamForm.results.takeLast(5).forEach { result ->
                ResultIndicator(result = result)
                Spacer(modifier = Modifier.width(4.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Form matches table
        if (teamForm.matches.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Geen recente wedstrijden",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // Table headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Tegenstander",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(2f)
                )
                Text(
                    text = "Uitslag",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Eff.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(0.5f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Form matches list
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                teamForm.matches.takeLast(5).forEach { match ->
                    FormMatchRow(match = match)
                }
            }
        }
    }
}

@Composable
private fun ResultIndicator(
    result: MatchResult
) {
    val (color, text) = when (result) {
        MatchResult.WIN -> Pair(Color(0xFF4CAF50), "W") // Green
        MatchResult.DRAW -> Pair(Color(0xFFFF9800), "G") // Orange
        MatchResult.LOSS -> Pair(Color(0xFFF44336), "V") // Red
    }

    Box(
        modifier = Modifier
            .size(24.dp)
            .background(color, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
    }
}

@Composable
private fun FormMatchRow(
    match: FormMatch
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Opponent name (truncated)
        Text(
            text = match.opponent.take(12) + if (match.opponent.length > 12) "..." else "",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(2f)
        )

        // Score
        Text(
            text = "${match.goals}-${match.opponentGoals}",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        // Efficiency icon
        Box(
            modifier = Modifier
                .size(20.dp)
                .weight(0.5f),
            contentAlignment = Alignment.Center
        ) {
            EfficiencyIconIndicator(efficiencyIcon = match.efficiencyIcon)
        }
    }
}

@Composable
private fun EfficiencyIconIndicator(
    efficiencyIcon: EfficiencyIcon
) {
    val (color, text) = when (efficiencyIcon) {
        EfficiencyIcon.CLINICAL -> Pair(Color(0xFF4CAF50), "↑") // Green
        EfficiencyIcon.BALANCED -> Pair(Color(0xFFFF9800), "→") // Orange
        EfficiencyIcon.INEFFICIENT -> Pair(Color(0xFFF44336), "↓") // Red
    }

    Box(
        modifier = Modifier
            .size(18.dp)
            .background(color, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
    }
}

@Composable
private fun EfficiencyLegendItem(
    icon: EfficiencyIcon,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        EfficiencyIconIndicator(efficiencyIcon = icon)
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
