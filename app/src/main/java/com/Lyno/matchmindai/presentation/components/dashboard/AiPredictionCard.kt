package com.Lyno.matchmindai.presentation.components.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Lyno.matchmindai.domain.model.AiAnalysisResult
import com.Lyno.matchmindai.domain.model.MatchFixture
import com.Lyno.matchmindai.ui.theme.*

/**
 * AI Prediction Card for dashboard showing AI-powered match predictions.
 * Displays win probabilities, predicted score, and key insights.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiPredictionCard(
    match: MatchFixture,
    aiAnalysis: AiAnalysisResult,
    modifier: Modifier = Modifier,
    onExpand: (() -> Unit)? = null
) {
    Card(
        onClick = { onExpand?.invoke() },
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceCard.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with AI icon and confidence
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryNeon.copy(alpha = 0.2f))
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Insights,
                            contentDescription = "AI Insights",
                            tint = PrimaryNeon,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Text(
                        text = "🧠 AI VOORSPELLING",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = PrimaryNeon
                    )
                }
                
                // Confidence badge
                ConfidenceBadge(confidence = aiAnalysis.getBettingConfidence())
            }
            
            // Match teams
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = match.homeTeam,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = TextHigh,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = "Thuis",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMedium
                    )
                }
                
                Text(
                    text = "VS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextMedium
                )
                
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = match.awayTeam,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = TextHigh,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = "Uit",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMedium
                    )
                }
            }
            
            // Win probabilities
            ProbabilityBar(
                homeWin = aiAnalysis.home_attack_modifier,
                awayWin = aiAnalysis.away_attack_modifier,
                homeTeam = match.homeTeam,
                awayTeam = match.awayTeam,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Predicted score
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Voorspelde score:",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMedium
                )
                
                Text(
                    text = getPredictedScore(aiAnalysis),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextHigh
                )
            }
            
            // Key players to watch (if available)
            if (aiAnalysis.key_player_watch.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Speler om in de gaten te houden:",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMedium
                    )
                    
                    Text(
                        text = aiAnalysis.key_player_watch,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextHigh,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Quick summary
            if (aiAnalysis.reasoning.isNotEmpty()) {
                Text(
                    text = aiAnalysis.reasoning.take(100) + if (aiAnalysis.reasoning.length > 100) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Betting tip (if available)
            if (aiAnalysis.betting_tip.isNotEmpty() || aiAnalysis.hasMeaningfulData()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = PrimaryNeon.copy(alpha = 0.1f),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "💰 AI BETTING TIP",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = PrimaryNeon
                        )
                        
                        Text(
                            text = aiAnalysis.getBettingTip(match.homeTeam, match.awayTeam),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextHigh
                        )
                    }
                }
            }
        }
    }
}

/**
 * Confidence badge showing AI confidence percentage.
 */
@Composable
private fun ConfidenceBadge(
    confidence: Int,
    modifier: Modifier = Modifier
) {
    val confidenceColor = when {
        confidence >= 80 -> ConfidenceHigh
        confidence >= 60 -> Color(0xFFF59E0B) // Amber
        else -> ConfidenceLow
    }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = confidenceColor.copy(alpha = 0.1f),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "$confidence%",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = confidenceColor
            )
        }
    }
}

/**
 * Probability bar showing win probabilities for home and away teams.
 */
@Composable
private fun ProbabilityBar(
    homeWin: Double,
    awayWin: Double,
    homeTeam: String,
    awayTeam: String,
    modifier: Modifier = Modifier
) {
    // Normalize probabilities to percentages
    val total = homeWin + awayWin
    val homePercent = if (total > 0) (homeWin / total * 100).toInt() else 50
    val awayPercent = 100 - homePercent
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Probability labels
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "$homeTeam",
                style = MaterialTheme.typography.labelSmall,
                color = TextMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            
            Text(
                text = "$awayTeam",
                style = MaterialTheme.typography.labelSmall,
                color = TextMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Probability bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(TextMedium.copy(alpha = 0.2f))
        ) {
            // Home win probability
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(homePercent / 100f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(PrimaryNeon)
            )
            
            // Away win probability
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(awayPercent / 100f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(ConfidenceLow)
            )
        }
        
        // Percentage labels
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "${homePercent}%",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = PrimaryNeon
            )
            
            Text(
                text = "${awayPercent}%",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = ConfidenceLow
            )
        }
    }
}

/**
 * Get predicted score from AI analysis.
 * Uses tactical key or generates based on modifiers.
 */
private fun getPredictedScore(aiAnalysis: AiAnalysisResult): String {
    // Try to extract score from tactical key or reasoning
    val scorePattern = Regex("""(\d+)-(\d+)""")
    val match = scorePattern.find(aiAnalysis.tactical_key) ?: scorePattern.find(aiAnalysis.reasoning)
    
    return if (match != null) {
        "${match.groupValues[1]}-${match.groupValues[2]}"
    } else {
        // Generate score based on attack modifiers
        val homeGoals = (aiAnalysis.home_attack_modifier * 1.5).toInt().coerceIn(0, 5)
        val awayGoals = (aiAnalysis.away_attack_modifier * 1.2).toInt().coerceIn(0, 5)
        "$homeGoals-$awayGoals"
    }
}

/**
 * Preview for AiPredictionCard.
 */
@Composable
fun AiPredictionCardPreview() {
    val mockMatch = com.Lyno.matchmindai.domain.model.MatchFixture(
        fixtureId = 123,
        homeTeam = "Ajax",
        awayTeam = "Feyenoord",
        league = "Eredivisie",
        leagueId = 88,
        time = "20:00",
        date = "2024-01-15",
        status = "NS"
    )
    
    val mockAnalysis = AiAnalysisResult(
        home_attack_modifier = 1.3,
        away_attack_modifier = 1.1,
        home_defense_modifier = 0.9,
        away_defense_modifier = 1.0,
        chaos_score = 75,
        atmosphere_score = 85,
        confidence_score = 82,
        reasoning_short = "Ajax heeft sterk thuisvoordeel maar Feyenoord is in goede vorm. Verwacht een offensieve wedstrijd met veel kansen.",
        primary_scenario_title = "De Klassieker Showdown",
        primary_scenario_desc = "Ajax probeert thuisvoordeel te benutten tegen een gevaarlijke Feyenoord.",
        tactical_key = "Ajax moet de hoge druk van Feyenoord counteren met snelle omschakelingen.",
        key_player_watch = "Steven Berghuis (Ajax) vs Santiago Giménez (Feyenoord)",
        betting_tip = "Ajax wint & Over 2.5 goals",
        betting_confidence = 75
    )
    
    AiPredictionCard(
        match = mockMatch,
        aiAnalysis = mockAnalysis,
        modifier = Modifier.padding(16.dp)
    )
}
