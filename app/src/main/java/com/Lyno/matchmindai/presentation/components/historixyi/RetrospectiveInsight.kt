package com.Lyno.matchmindai.presentation.components.historixyi

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.Lyno.matchmindai.domain.model.RetrospectiveAnalysis
import com.Lyno.matchmindai.domain.model.XgVerdict
import com.Lyno.matchmindai.ui.theme.*

/**
 * Colored alert/banner showing retrospective insight about prediction accuracy.
 * Three possible states: MASTERCLASS, BAD BEAT, LUCKY.
 */
@Composable
fun RetrospectiveInsight(
    retrospective: RetrospectiveAnalysis,
    modifier: Modifier = Modifier
) {
    val insight = determineInsight(retrospective)

    AnimatedVisibility(
        visible = insight != null,
        enter = fadeIn(animationSpec = tween(durationMillis = 500)),
        exit = fadeOut(animationSpec = tween(durationMillis = 300))
    ) {
        insight?.let { (type, message, color) ->
            InsightBanner(
                insightType = type,
                message = message,
                backgroundColor = color,
                modifier = modifier
            )
        }
    }
}

/**
 * Determines which insight to show based on retrospective analysis.
 */
@Composable
private fun determineInsight(retrospective: RetrospectiveAnalysis): InsightData? {
    return when (retrospective.xgVerdict) {
        XgVerdict.DOMINANT -> InsightData(
            type = InsightType.MASTERCLASS,
            message = stringResource(R.string.history_insight_masterclass),
            backgroundColor = PrimaryNeon.copy(alpha = 0.15f)
        )
        XgVerdict.UNLUCKY -> {
            val message = buildBadBeatMessage(retrospective)
            InsightData(
                type = InsightType.BAD_BEAT,
                message = message,
                backgroundColor = Error.copy(alpha = 0.15f)
            )
        }
        XgVerdict.LUCKY -> InsightData(
            type = InsightType.LUCKY,
            message = stringResource(R.string.history_insight_lucky),
            backgroundColor = ConfidenceMedium.copy(alpha = 0.15f)
        )
        XgVerdict.NEUTRAL -> null
    }
}

/**
 * Builds a dynamic BAD BEAT message with actual xG values.
 */
@Composable
private fun buildBadBeatMessage(retrospective: RetrospectiveAnalysis): String {
    val kaptigunStats = retrospective.kaptigunStats
    if (kaptigunStats == null || kaptigunStats.deepStats?.avgXg == null) {
        return "⚠️ Pech gehad! Statistieken niet beschikbaar."
    }
    
    val (homeXg, awayXg) = kaptigunStats.deepStats.avgXg
    val dominantTeam = if (homeXg > awayXg) "Team A" else "Team B"
    val dominantXg = maxOf(homeXg, awayXg)
    val otherXg = minOf(homeXg, awayXg)
    
    return stringResource(
        R.string.history_insight_bad_beat,
        dominantTeam,
        String.format("%.1f", dominantXg),
        String.format("%.1f", otherXg)
    )
}

@Composable
private fun InsightBanner(
    insightType: InsightType,
    message: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon
            val iconColor = when (insightType) {
                InsightType.MASTERCLASS -> PrimaryNeon.copy(alpha = 0.3f)
                InsightType.BAD_BEAT -> Error.copy(alpha = 0.3f)
                InsightType.LUCKY -> ConfidenceMedium.copy(alpha = 0.3f)
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = iconColor,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = insightType.icon,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // Message
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextHigh,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start
            )
        }
    }
}

/**
 * Data class representing an insight to display.
 */
private data class InsightData(
    val type: InsightType,
    val message: String,
    val backgroundColor: Color
)

/**
 * Enumeration of possible insight types.
 */
private enum class InsightType(val icon: String) {
    MASTERCLASS("✅"),
    BAD_BEAT("⚠️"),
    LUCKY("🍀")
}
