package com.Lyno.matchmindai.presentation.components.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Lyno.matchmindai.domain.model.MatchReport
import com.Lyno.matchmindai.domain.model.ReportSection
import com.Lyno.matchmindai.presentation.components.ExpandableCard
import com.Lyno.matchmindai.presentation.components.GlassCard
import com.Lyno.matchmindai.ui.theme.PrimaryNeon

/**
 * Composable for displaying a comprehensive match report.
 * Shows the AI-generated narrative with "Clean & Scannable" layout.
 * 
 * Features:
 * - TL;DR Header (Executive Summary)
 * - Collapsible sections for heavy text
 * - Improved typography and spacing
 * - Visual breakers between story and betting advice
 */
@Composable
fun MatchReportCard(
    matchReport: MatchReport,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp) // Increased spacing for better scannability
    ) {
        // 1. TL;DR HEADER (Executive Summary - Always Visible)
        ExecutiveSummaryCard(matchReport)
        
        // Visual Breaker: Story vs Betting Advice
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
        
        // 2. COLLAPSIBLE SECTIONS (Default: Collapsed)
        val sections = matchReport.getFormattedSections()
        sections.forEachIndexed { index, section ->
            ExpandableReportSection(section = section)
            if (index < sections.size - 1) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        
        // 3. KEY INSIGHTS (Collapsible)
        ExpandableKeyInsightsCard(matchReport)
        
        // 4. SCENARIO ANALYSIS (Collapsible - if any)
        if (matchReport.scenarios.isNotEmpty()) {
            ExpandableScenarioAnalysisCard(matchReport)
        }
        
        // 5. SCORE PREDICTIONS (Collapsible - if any)
        if (matchReport.scorePredictions != null) {
            ExpandableScorePredictionsCard(matchReport.scorePredictions)
        }
        
        // 6. BREAKING NEWS (Collapsible - if any)
        if (matchReport.breakingNewsUsed.isNotEmpty()) {
            ExpandableBreakingNewsCard(matchReport.breakingNewsUsed)
        }
        
        // Bottom spacing
        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * TL;DR Header - Executive Summary (Always Visible).
 * Shows only the most critical information at a glance.
 */
@Composable
private fun ExecutiveSummaryCard(matchReport: MatchReport) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Title with confidence badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = matchReport.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNeon,
                    modifier = Modifier.weight(1f)
                )
                
                // Confidence badge
                Badge(
                    containerColor = when {
                        matchReport.confidence >= 80 -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                        matchReport.confidence >= 60 -> Color(0xFFFFA726).copy(alpha = 0.2f)
                        else -> Color(0xFFF44336).copy(alpha = 0.2f)
                    },
                    contentColor = when {
                        matchReport.confidence >= 80 -> Color(0xFF4CAF50)
                        matchReport.confidence >= 60 -> Color(0xFFFFA726)
                        else -> Color(0xFFF44336)
                    }
                ) {
                    Text(
                        text = "${matchReport.confidence}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Match info (compact)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = matchReport.homeTeam,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Text(
                        text = "Thuis",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "VS",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = matchReport.league,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = matchReport.awayTeam,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Text(
                        text = "Uit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Mastermind Verdict (1-2 sentences)
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🎯 MASTERMIND VERDICT",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNeon.copy(alpha = 0.8f)
                )
                
                Text(
                    text = matchReport.introduction,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = androidx.compose.ui.unit.TextUnit(24f, androidx.compose.ui.unit.TextUnitType.Sp),
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Quick stats (if available)
            if (matchReport.scorePredictions != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuickStatItem(
                        title = "Meest Waarschijnlijk",
                        value = matchReport.scorePredictions.mostLikelyScore,
                        color = PrimaryNeon
                    )
                    QuickStatItem(
                        title = "Thuis Winst",
                        value = "${matchReport.scorePredictions.homeWinProbability}%",
                        color = Color(0xFF4CAF50)
                    )
                    QuickStatItem(
                        title = "Verwachte Goals",
                        value = "${matchReport.scorePredictions.expectedHomeGoals.toInt()}-${matchReport.scorePredictions.expectedAwayGoals.toInt()}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Quick stat item for executive summary.
 */
@Composable
private fun QuickStatItem(
    title: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Expandable report section using ExpandableCard.
 */
@Composable
private fun ExpandableReportSection(section: ReportSection) {
    ExpandableCard(
        title = section.title,
        icon = section.icon,
        modifier = Modifier.fillMaxWidth(),
        initiallyExpanded = false, // Default: Collapsed
        headerColor = PrimaryNeon
    ) {
        Text(
            text = section.content,
            style = MaterialTheme.typography.bodyLarge, // Increased font size
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = androidx.compose.ui.unit.TextUnit(26f, androidx.compose.ui.unit.TextUnitType.Sp), // Better line height
            fontWeight = FontWeight.Normal // Avoid Light fonts for readability
        )
    }
}

/**
 * Key insights card with chaos and atmosphere scores.
 */
@Composable
private fun KeyInsightsCard(matchReport: MatchReport) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "📊 KEY INSIGHTS",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = PrimaryNeon
            )
            
            // Chaos and Atmosphere scores
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Chaos Score
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "⚡ Chaos",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${matchReport.chaosScore}/100",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            matchReport.chaosScore >= 80 -> Color.Red
                            matchReport.chaosScore >= 60 -> Color(0xFFFFA726)
                            matchReport.chaosScore >= 40 -> Color.Yellow
                            else -> Color.Green
                        }
                    )
                    Text(
                        text = matchReport.chaosLevel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Atmosphere Score
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "🏟️ Atmosfeer",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${matchReport.atmosphereScore}/100",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            matchReport.atmosphereScore >= 80 -> PrimaryNeon
                            matchReport.atmosphereScore >= 60 -> Color(0xFF4CAF50)
                            matchReport.atmosphereScore >= 40 -> Color(0xFFFFEB3B)
                            else -> Color.Gray
                        }
                    )
                    Text(
                        text = matchReport.atmosphereLevel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Betting tip (if available)
            if (matchReport.bettingTip.isNotEmpty()) {
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "🎯 Betting Tip",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryNeon
                    )
                    Text(
                        text = matchReport.bettingTip,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Vertrouwen: ${matchReport.bettingConfidence}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Scenario analysis card.
 */
@Composable
private fun ScenarioAnalysisCard(matchReport: MatchReport) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🎭",
                    fontSize = 20.sp
                )
                Text(
                    text = "SCENARIO ANALYSE",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNeon
                )
            }
            
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant
            )
            
            // Show scenarios using the new ScenarioCard component
            matchReport.scenarios.forEach { scenario ->
                ScenarioCard(scenario = scenario)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * Individual scenario item.
 */
@Composable
private fun ScenarioItem(scenario: com.Lyno.matchmindai.domain.model.MatchScenario) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Scenario header with probability
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = scenario.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            
            // Probability badge
            Badge(
                containerColor = when {
                    scenario.probability >= 40 -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                    scenario.probability >= 20 -> Color(0xFFFFA726).copy(alpha = 0.2f)
                    else -> Color(0xFFF44336).copy(alpha = 0.2f)
                },
                contentColor = when {
                    scenario.probability >= 40 -> Color(0xFF4CAF50)
                    scenario.probability >= 20 -> Color(0xFFFFA726)
                    else -> Color(0xFFF44336)
                }
            ) {
                Text(
                    text = "${scenario.probability}% kans",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Predicted score
        Text(
            text = "Score: ${scenario.predictedScore}",
            style = MaterialTheme.typography.bodySmall,
            color = PrimaryNeon,
            fontWeight = FontWeight.Medium
        )
        
        // Short description
        Text(
            text = scenario.description.take(100) + if (scenario.description.length > 100) "..." else "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            maxLines = 2
        )
        
        // Key factors (if any)
        if (scenario.keyFactors.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Belangrijke factoren:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
                scenario.keyFactors.take(2).forEach { factor ->
                    Text(
                        text = "• $factor",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Score predictions card.
 */
@Composable
private fun ScorePredictionsCard(scorePredictions: com.Lyno.matchmindai.domain.model.ScorePredictionMatrix) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🎯",
                    fontSize = 20.sp
                )
                Text(
                    text = "SCORE VOORSPELLINGEN",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNeon
                )
            }
            
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant
            )
            
            // Most likely score
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Meest waarschijnlijk",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = scorePredictions.mostLikelyScore,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNeon
                )
                Text(
                    text = "${scorePredictions.mostLikelyProbability}% kans",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
            
            // Outcome probabilities
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutcomeProbabilityItem(
                    title = "Thuis wint",
                    probability = scorePredictions.homeWinProbability,
                    color = Color(0xFF4CAF50)
                )
                OutcomeProbabilityItem(
                    title = "Gelijk",
                    probability = scorePredictions.drawProbability,
                    color = Color(0xFFFFA726)
                )
                OutcomeProbabilityItem(
                    title = "Uit wint",
                    probability = scorePredictions.awayWinProbability,
                    color = Color(0xFFF44336)
                )
            }
            
            // Expected goals
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Verwachte doelpunten",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Thuis",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "%.1f".format(scorePredictions.expectedHomeGoals),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Totaal",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = scorePredictions.goalExpectationRange,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNeon
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Uit",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "%.1f".format(scorePredictions.expectedAwayGoals),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Outcome probability item.
 */
@Composable
private fun OutcomeProbabilityItem(
    title: String,
    probability: Int,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
        Text(
            text = "$probability%",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/**
 * Expandable key insights card.
 */
@Composable
private fun ExpandableKeyInsightsCard(matchReport: MatchReport) {
    ExpandableCard(
        title = "KEY INSIGHTS",
        icon = "📊",
        modifier = Modifier.fillMaxWidth(),
        initiallyExpanded = false,
        headerColor = PrimaryNeon
    ) {
        // Chaos and Atmosphere scores
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Chaos Score
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "⚡ Chaos",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${matchReport.chaosScore}/100",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        matchReport.chaosScore >= 80 -> Color.Red
                        matchReport.chaosScore >= 60 -> Color(0xFFFFA726)
                        matchReport.chaosScore >= 40 -> Color.Yellow
                        else -> Color.Green
                    }
                )
                Text(
                    text = matchReport.chaosLevel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Atmosphere Score
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "🏟️ Atmosfeer",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${matchReport.atmosphereScore}/100",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        matchReport.atmosphereScore >= 80 -> PrimaryNeon
                        matchReport.atmosphereScore >= 60 -> Color(0xFF4CAF50)
                        matchReport.atmosphereScore >= 40 -> Color(0xFFFFEB3B)
                        else -> Color.Gray
                    }
                )
                Text(
                    text = matchReport.atmosphereLevel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Betting tip (if available)
        if (matchReport.bettingTip.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🎯 Betting Tip",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryNeon
                )
                Text(
                    text = matchReport.bettingTip,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = androidx.compose.ui.unit.TextUnit(24f, androidx.compose.ui.unit.TextUnitType.Sp),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Vertrouwen: ${matchReport.bettingConfidence}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Expandable scenario analysis card.
 */
@Composable
private fun ExpandableScenarioAnalysisCard(matchReport: MatchReport) {
    ExpandableCard(
        title = "SCENARIO ANALYSE",
        icon = "🎭",
        modifier = Modifier.fillMaxWidth(),
        initiallyExpanded = false,
        headerColor = PrimaryNeon
    ) {
        // Show scenarios using the new ScenarioCard component
        matchReport.scenarios.forEach { scenario ->
            ScenarioCard(scenario = scenario)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/**
 * Expandable score predictions card.
 */
@Composable
private fun ExpandableScorePredictionsCard(scorePredictions: com.Lyno.matchmindai.domain.model.ScorePredictionMatrix) {
    ExpandableCard(
        title = "SCORE VOORSPELLINGEN",
        icon = "🎯",
        modifier = Modifier.fillMaxWidth(),
        initiallyExpanded = false,
        headerColor = PrimaryNeon
    ) {
        // Most likely score
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Meest waarschijnlijk",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = scorePredictions.mostLikelyScore,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryNeon
            )
            Text(
                text = "${scorePredictions.mostLikelyProbability}% kans",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Outcome probabilities
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutcomeProbabilityItem(
                title = "Thuis wint",
                probability = scorePredictions.homeWinProbability,
                color = Color(0xFF4CAF50)
            )
            OutcomeProbabilityItem(
                title = "Gelijk",
                probability = scorePredictions.drawProbability,
                color = Color(0xFFFFA726)
            )
            OutcomeProbabilityItem(
                title = "Uit wint",
                probability = scorePredictions.awayWinProbability,
                color = Color(0xFFF44336)
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Expected goals
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Verwachte doelpunten",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Thuis",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "%.1f".format(scorePredictions.expectedHomeGoals),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Totaal",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = scorePredictions.goalExpectationRange,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNeon
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Uit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "%.1f".format(scorePredictions.expectedAwayGoals),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Expandable breaking news card.
 */
@Composable
private fun ExpandableBreakingNewsCard(breakingNews: List<String>) {
    ExpandableCard(
        title = "GEBRUIKTE NIEUWS",
        icon = "📰",
        modifier = Modifier.fillMaxWidth(),
        initiallyExpanded = false,
        headerColor = PrimaryNeon
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            breakingNews.take(3).forEach { news ->
                Text(
                    text = "• $news",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = androidx.compose.ui.unit.TextUnit(24f, androidx.compose.ui.unit.TextUnitType.Sp)
                )
            }
            
            if (breakingNews.size > 3) {
                Text(
                    text = "+ ${breakingNews.size - 3} meer nieuwsitems...",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

/**
 * Loading state for match report.
 */
@Composable
fun MatchReportLoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = PrimaryNeon)
            Text(
                text = "Verslag wordt gegenereerd...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "De AI analyseert alle data en maakt een uitgebreid verslag",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Error state for match report.
 */
@Composable
fun MatchReportErrorState(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "❌",
                fontSize = 48.sp,
                color = MaterialTheme.colorScheme.error
            )
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Fout bij genereren verslag",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
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
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Opnieuw proberen",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Opnieuw proberen",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
