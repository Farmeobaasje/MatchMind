package com.Lyno.matchmindai.presentation.components.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.Lyno.matchmindai.R

/**
 * Confidence badge showing the Mastermind's confidence level.
 * Larger and more prominent than the standard badge.
 */
@Composable
fun HeroConfidenceBadge(
    confidence: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val confidenceText = when {
        confidence >= 80 -> "ZEER HOOG"
        confidence >= 60 -> "MATIG"
        else -> "LAAG"
    }

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = color.copy(alpha = 0.2f),
        border = BorderStroke(2.dp, color),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.confidence_label_dutch),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$confidence%",
                    style = MaterialTheme.typography.displaySmall,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = confidenceText,
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
fun HeroRecommendationBadge(
    recommendation: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = color.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, color),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "AANBEVELING",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = recommendation,
                style = MaterialTheme.typography.titleMedium,
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
fun HeroScenarioTypeIndicator(
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Scenario Type",
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = color,
                maxLines = 2
            )
        }
    }
}
