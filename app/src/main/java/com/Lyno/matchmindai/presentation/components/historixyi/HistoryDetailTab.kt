package com.Lyno.matchmindai.presentation.components.historixyi

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.Lyno.matchmindai.domain.model.DeepStatsComparison
import com.Lyno.matchmindai.domain.model.RetrospectiveAnalysis
import com.Lyno.matchmindai.presentation.components.kaptigun.DeepStatsChart

/**
 * Tab content for History Detail screen that combines all UI components.
 * Shows prediction vs reality comparison, retrospective insights, and match statistics.
 */
@Composable
fun HistoryDetailTab(
    retrospective: RetrospectiveAnalysis,
    historicalStats: DeepStatsComparison,
    singleMatchStats: DeepStatsComparison?,
    homeTeamName: String,
    awayTeamName: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Reality Check Card - Prediction vs Actual
        RealityCheckCard(
            retrospective = retrospective,
            modifier = Modifier.fillMaxWidth()
        )

        // 2. Retrospective Insight Banner
        RetrospectiveInsight(
            retrospective = retrospective,
            modifier = Modifier.fillMaxWidth()
        )

        // 3. Deep Stats Chart (with single match data if available)
        DeepStatsChart(
            deepStats = historicalStats,
            singleMatchStats = singleMatchStats,
            homeTeamName = homeTeamName,
            awayTeamName = awayTeamName,
            modifier = Modifier.fillMaxWidth()
        )

        // 4. Additional context or notes
        if (singleMatchStats != null) {
            HistoricalContextNote()
        }
    }
}

@Composable
private fun HistoricalContextNote() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "📊 Historische Context",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "De statistieken hierboven tonen de werkelijke matchdata. " +
                       "Vergelijk dit met de historische gemiddelden om te zien of het " +
                       "een typische of uitzonderlijke wedstrijd was.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "💡 Tip: Een 'MASTERCLASS' insight betekent dat de voorspelling " +
                       "perfect overeenkwam met zowel de uitslag als de statistische verhoudingen.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
