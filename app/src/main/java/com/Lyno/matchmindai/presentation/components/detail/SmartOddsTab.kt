package com.Lyno.matchmindai.presentation.components.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.Lyno.matchmindai.domain.model.MatchDetail
import com.Lyno.matchmindai.domain.usecase.CalculateKellyUseCase
import com.Lyno.matchmindai.presentation.components.GlassCard
import com.Lyno.matchmindai.presentation.viewmodel.MatchDetailViewModel
import com.Lyno.matchmindai.ui.theme.PrimaryNeon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * SmartOddsTab - The "Connectie" tab linking AI predictions to betting odds.
 * This is where the AI's betting tip meets the actual odds for actionable insights.
 */
@Composable
fun SmartOddsTab(matchDetail: MatchDetail, viewModel: MatchDetailViewModel) {
    val odds by viewModel.odds.collectAsState()
    val hybridPrediction by viewModel.hybridPrediction.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 80.dp) // Extra padding to prevent FAB overlap
    ) {
        
        // 1. KELLY VALUE ANALYSE (New Feature)
        KellyValueCard(
            matchDetail = matchDetail,
            odds = odds,
            hybridPrediction = hybridPrediction
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 2. DE AI TIP (Smart Odds)
        if (hybridPrediction != null) {
            val analysis = hybridPrediction!!.analysis
            val tip = analysis.getBettingTip(matchDetail.homeTeam, matchDetail.awayTeam)
            val confidence = analysis.getBettingConfidence()
            
            // Enhanced AI Tip Card with better readability
            EnhancedAiTipCard(
                tip = tip,
                confidence = confidence,
                odds = odds,
                matchDetail = matchDetail
            )
        } else {
            // Call to action
            GlassCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Genereer een AI Tip",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ga naar de Mastermind tab om een AI-analyse te genereren met een betting tip",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Button(
                        onClick = { /* Navigate to Mastermind tab */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Naar Mastermind")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Standaard Odds Lijst (Bestaande logica)
        Text("Alle Markten", fontWeight = FontWeight.Bold)
        
        if (odds != null && odds!!.hasOdds) {
            StandardOddsList(odds = odds!!, matchDetail = matchDetail)
        } else {
            GlassCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Geen odds beschikbaar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Odds worden geladen of zijn niet beschikbaar voor deze wedstrijd",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Standard odds list showing all available betting markets.
 */
@Composable
private fun StandardOddsList(odds: com.Lyno.matchmindai.domain.model.OddsData, matchDetail: MatchDetail) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Match Result Odds
        GlassCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Uitslag",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OddsItem(
                        team = matchDetail.homeTeam,
                        odds = odds.homeWinOdds,
                        color = PrimaryNeon
                    )
                    
                    OddsItem(
                        team = "Gelijkspel",
                        odds = odds.drawOdds,
                        color = Color.Gray
                    )
                    
                    OddsItem(
                        team = matchDetail.awayTeam,
                        odds = odds.awayWinOdds,
                        color = Color(0xFFFFA726) // Orange
                    )
                }
            }
        }
        
        // Over/Under Odds
        if (odds.overUnderOdds != null && odds.overUnderOdds.isNotEmpty()) {
            GlassCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Totaal Goals",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    odds.overUnderOdds.entries.take(3).forEach { (market, odd) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = market,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = String.format("%.2f", odd),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryNeon
                            )
                        }
                    }
                }
            }
        }
        
        // Both Teams to Score Odds
        if (odds.bothTeamsToScoreOdds != null && odds.bothTeamsToScoreOdds.isNotEmpty()) {
            GlassCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Beide Teams Scoren",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    odds.bothTeamsToScoreOdds.entries.forEach { (market, odd) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = market,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = String.format("%.2f", odd),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryNeon
                            )
                        }
                    }
                }
            }
        }
        
        // Bookmaker Info
        GlassCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Boekmaker",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = odds.bookmakerName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Bijgewerkt",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = odds.lastUpdated,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Individual odds item for match result.
 */
@Composable
private fun OddsItem(
    team: String,
    odds: Double?,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = team,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 2
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        if (odds != null) {
            Text(
                text = String.format("%.2f", odds),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        } else {
            Text(
                text = "-",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
        }
    }
}

/**
 * Find the relevant odds for an AI betting tip.
 * Analyzes the tip text to determine which odds to show.
 * Returns a Triple of (oddsValue, oddType, oddMarket)
 */
private fun findRelevantOddForTip(
    tip: String,
    odds: com.Lyno.matchmindai.domain.model.OddsData?,
    matchDetail: MatchDetail
): Triple<Double?, String?, String?> {
    if (odds == null) return Triple(null, null, null)
    
    val lowerTip = tip.lowercase()
    val homeTeam = matchDetail.homeTeam.lowercase()
    val awayTeam = matchDetail.awayTeam.lowercase()
    
    // Check for home team win recommendations
    if (containsHomeWinKeywords(lowerTip, homeTeam)) {
        return Triple(odds.homeWinOdds, "Thuiswinst", matchDetail.homeTeam)
    }
    
    // Check for away team win recommendations
    if (containsAwayWinKeywords(lowerTip, awayTeam)) {
        return Triple(odds.awayWinOdds, "Uitwinst", matchDetail.awayTeam)
    }
    
    // Check for draw recommendations
    if (containsDrawKeywords(lowerTip)) {
        return Triple(odds.drawOdds, "Gelijkspel", "X")
    }
    
    // Check for over/under recommendations
    if (containsOverKeywords(lowerTip)) {
        val overMarket = odds.overUnderOdds?.entries?.firstOrNull { it.key.contains("over", ignoreCase = true) }
        return Triple(overMarket?.value, "Over Goals", overMarket?.key)
    }
    
    if (containsUnderKeywords(lowerTip)) {
        val underMarket = odds.overUnderOdds?.entries?.firstOrNull { it.key.contains("under", ignoreCase = true) }
        return Triple(underMarket?.value, "Under Goals", underMarket?.key)
    }
    
    // Check for both teams to score recommendations
    if (containsBothTeamsToScoreKeywords(lowerTip)) {
        val bttsMarket = odds.bothTeamsToScoreOdds?.entries?.firstOrNull { it.key.contains("yes", ignoreCase = true) }
        return Triple(bttsMarket?.value, "Beide Teams Scoren", bttsMarket?.key)
    }
    
    // Fallback: return highest odds if no specific match found
    val allOdds = listOfNotNull(
        odds.homeWinOdds?.let { Triple(it, "Thuiswinst", matchDetail.homeTeam) },
        odds.drawOdds?.let { Triple(it, "Gelijkspel", "X") },
        odds.awayWinOdds?.let { Triple(it, "Uitwinst", matchDetail.awayTeam) },
        odds.overUnderOdds?.values?.maxOrNull()?.let { maxOdds ->
            val market = odds.overUnderOdds?.entries?.firstOrNull { it.value == maxOdds }?.key
            Triple(maxOdds, "Over/Under", market)
        },
        odds.bothTeamsToScoreOdds?.values?.maxOrNull()?.let { maxOdds ->
            val market = odds.bothTeamsToScoreOdds?.entries?.firstOrNull { it.value == maxOdds }?.key
            Triple(maxOdds, "Beide Teams Scoren", market)
        }
    )
    
    return allOdds.maxByOrNull { it.first } ?: Triple(null, null, null)
}

/**
 * Helper functions to analyze betting tip keywords
 */
private fun containsHomeWinKeywords(tip: String, homeTeam: String): Boolean {
    val keywords = listOf(
        homeTeam,
        "thuis",
        "home",
        "1",
        "wint thuis",
        "home win",
        "home team",
        "thuisploeg",
        "thuiswinst",
        "wint van",
        "verslaat",
        "overwint",
        "wint de wedstrijd",
        "wint het",
        "wint deze",
        "wint tegen"
    )
    return keywords.any { tip.contains(it, ignoreCase = true) }
}

private fun containsAwayWinKeywords(tip: String, awayTeam: String): Boolean {
    val lowerTip = tip.lowercase()
    val lowerAwayTeam = awayTeam.lowercase()
    
    // Check if tip contains away team name
    if (lowerTip.contains(lowerAwayTeam)) {
        // If tip contains away team, check for positive/negative context
        val positiveContext = listOf(
            "double chance", "double", "dc", "x2", "2x", "niet verliest", 
            "verliest niet", "pakt punten", "punten pakken", "minstens een punt",
            "niet verslaan", "overleeft", "houdt stand", "blijft overeind",
            "wint", "win", "overwint", "verslaat", "slaat", "klopt"
        ).any { lowerTip.contains(it) }
        
        val negativeContext = listOf(
            "verliest", "verlies", "slaat niet", "klopt niet", "wint niet"
        ).any { lowerTip.contains(it) }
        
        // Return true if positive context or no negative context
        return positiveContext || !negativeContext
    }
    
    // Check other away win keywords
    val keywords = listOf(
        "uit",
        "away",
        "2",
        "wint uit",
        "away win",
        "away team",
        "uitploeg",
        "uitwinst",
        "wint uit",
        "wint op bezoek",
        "wint bij",
        "wint uitwedstrijd",
        "wint uit duel",
        "double chance",
        "double",
        "dc",
        "x2",
        "2x",
        "niet verliest",
        "pakt punten",
        "punten pakken",
        "minstens een punt",
        "verliest niet",
        "niet verslaan",
        "overleeft",
        "houdt stand",
        "blijft overeind"
    )
    return keywords.any { lowerTip.contains(it) }
}

private fun containsDrawKeywords(tip: String): Boolean {
    val keywords = listOf(
        "gelijkspel",
        "draw",
        "x",
        "gelijk",
        "remise",
        "tie",
        "eindigt gelijk",
        "eindigt in gelijkspel",
        "blijft gelijk",
        "gelijk spel",
        "gelijkstand",
        "gelijk eindigt",
        "gelijk speelt"
    )
    return keywords.any { tip.contains(it, ignoreCase = true) }
}

private fun containsOverKeywords(tip: String): Boolean {
    val keywords = listOf(
        "over",
        "meer dan",
        ">",
        "hoog scorend",
        "veel goals",
        "goals over",
        "hoog scorende",
        "veel doelpunten",
        "meer goals",
        "over goals",
        "over totaal",
        "over aantal",
        "over 2.5",
        "over 3.5",
        "over 1.5"
    )
    return keywords.any { tip.contains(it, ignoreCase = true) }
}

private fun containsUnderKeywords(tip: String): Boolean {
    val keywords = listOf(
        "under",
        "minder dan",
        "<",
        "laag scorend",
        "weinig goals",
        "goals under",
        "laag scorende",
        "weinig doelpunten",
        "minder goals",
        "under goals",
        "under totaal",
        "under aantal",
        "under 2.5",
        "under 3.5",
        "under 1.5"
    )
    return keywords.any { tip.contains(it, ignoreCase = true) }
}

private fun containsBothTeamsToScoreKeywords(tip: String): Boolean {
    val keywords = listOf(
        "beide teams scoren",
        "both teams to score",
        "btts",
        "beide scoren",
        "beide ploegen scoren",
        "beide teams maken een goal",
        "beide teams maken doelpunten",
        "beide teams scoren een goal",
        "beide teams scoren minstens één keer",
        "beide teams raken het net",
        "beide teams vinden het net"
    )
    return keywords.any { tip.contains(it, ignoreCase = true) }
}

/**
 * Confidence indicator showing AI certainty level.
 */
@Composable
private fun ConfidenceIndicator(confidence: Int) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AI Zekerheid",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$confidence%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = when {
                    confidence >= 80 -> Color(0xFF00FF9D) // High confidence
                    confidence >= 60 -> Color(0xFFFFA726) // Medium confidence
                    else -> Color(0xFFF44336) // Low confidence
                }
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = confidence / 100f,
            modifier = Modifier.fillMaxWidth(),
            color = when {
                confidence >= 80 -> Color(0xFF00FF9D)
                confidence >= 60 -> Color(0xFFFFA726)
                else -> Color(0xFFF44336)
            },
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

/**
 * Card showing context about which odds are relevant and why.
 * Now includes explicit team recommendation and odds validation.
 */
@Composable
private fun OddsContextCard(
    oddType: String,
    oddMarket: String?,
    oddsValue: Double,
    confidence: Int,
    matchDetail: MatchDetail
) {
    GlassCard {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.TrendingUp,
                    contentDescription = "Kelly Value",
                    tint = Color(0xFF00FF9D),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI ADVIESEERT:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00FF9D)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            // Explicit team recommendation
            val recommendationText = when (oddType) {
                "Thuiswinst" -> "✅ De AI adviseert ${matchDetail.homeTeam} te kiezen"
                "Uitwinst" -> "✅ De AI adviseert ${matchDetail.awayTeam} te kiezen"
                "Gelijkspel" -> "✅ De AI adviseert Gelijkspel te kiezen"
                "Over Goals" -> "✅ De AI adviseert Over Goals te kiezen"
                "Under Goals" -> "✅ De AI adviseert Under Goals te kiezen"
                "Beide Teams Scoren" -> "✅ De AI adviseert BTTS (Beide Teams Scoren) te kiezen"
                else -> "✅ De AI adviseert deze weddenschap"
            }
            
            Text(
                text = recommendationText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Odds validation
            Text(
                text = "Odds Validatie:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Markt:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = oddType,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            oddMarket?.let { market ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Specifiek:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = market,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Odds:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = String.format("%.2f", oddsValue),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00FF9D)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "AI Zekerheid:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$confidence%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        confidence >= 80 -> Color(0xFF00FF9D)
                        confidence >= 60 -> Color(0xFFFFA726)
                        else -> Color(0xFFF44336)
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Safety warning for high odds
            if (oddsValue >= 3.0) {
                Text(
                    text = "⚠️ Hoge odds: Hoger risico, maar potentieel hogere winst",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFFA726)
                )
            }
            
            Text(
                text = "Deze tip is gegenereerd op basis van statistische analyse, teamform en recente prestaties.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Value rating card showing if the odds offer good value.
 */
@Composable
private fun ValueRatingCard(oddsValue: Double, confidence: Int) {
    // Calculate value rating based on odds and confidence
    val valueRating = when {
        oddsValue >= 3.0 && confidence >= 70 -> "⭐️⭐️⭐️⭐️⭐️ Uitstekende waarde"
        oddsValue >= 2.0 && confidence >= 60 -> "⭐️⭐️⭐️⭐️ Goede waarde"
        oddsValue >= 1.5 && confidence >= 50 -> "⭐️⭐️⭐️ Redelijke waarde"
        else -> "⭐️⭐️ Standaard waarde"
    }
    
    val valueColor = when {
        oddsValue >= 3.0 && confidence >= 70 -> Color(0xFF00FF9D)
        oddsValue >= 2.0 && confidence >= 60 -> Color(0xFFFFA726)
        else -> Color.Gray
    }
    
    GlassCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Waarde Rating",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = valueRating,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = valueColor
                )
            }
            Text(
                text = String.format("%.2f", oddsValue),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00FF9D)
            )
        }
    }
}

/**
 * Enhanced AI Tip Card with improved readability and contrast.
 * Uses dark background with neon accents for better visibility.
 */
@Composable
private fun EnhancedAiTipCard(
    tip: String,
    confidence: Int,
    odds: com.Lyno.matchmindai.domain.model.OddsData?,
    matchDetail: MatchDetail
) {
    // Find relevant odds for the tip
    val (relevantOdd, oddType, oddMarket) = findRelevantOddForTip(tip, odds, matchDetail)
    
    // Dark background with neon border for better contrast
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF1A1A1A), // Dark background for better contrast
                shape = MaterialTheme.shapes.medium
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with AI Tip label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = "AI Tip",
                    tint = Color(0xFF00FF9D),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "AI SMART TIP",
                    color = Color(0xFF00FF9D),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "$confidence% Zekerheid",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFAAAAAA) // Lighter gray for better contrast
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Main tip text with improved contrast
            Text(
                text = tip,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White // White text for maximum contrast
            )
            
            // Confidence indicator
            Spacer(modifier = Modifier.height(16.dp))
            ConfidenceIndicator(confidence = confidence)
            
            // Odds context explanation (if relevant odds found)
            if (relevantOdd != null && oddType != null) {
                Spacer(modifier = Modifier.height(16.dp))
                OddsContextCard(
                    oddType = oddType,
                    oddMarket = oddMarket,
                    oddsValue = relevantOdd,
                    confidence = confidence,
                    matchDetail = matchDetail
                )
            }
            
            // Action button with clear context - EXPLICIT team/market indication
            val buttonText = if (relevantOdd != null && oddType != null) {
                when (oddType) {
                    "Thuiswinst" -> "Zet op ${matchDetail.homeTeam} @ ${String.format("%.2f", relevantOdd)}"
                    "Uitwinst" -> "Zet op ${matchDetail.awayTeam} @ ${String.format("%.2f", relevantOdd)}"
                    "Gelijkspel" -> "Zet op Gelijkspel @ ${String.format("%.2f", relevantOdd)}"
                    "Over Goals" -> "Zet op Over @ ${String.format("%.2f", relevantOdd)}"
                    "Under Goals" -> "Zet op Under @ ${String.format("%.2f", relevantOdd)}"
                    "Beide Teams Scoren" -> "Zet op BTTS @ ${String.format("%.2f", relevantOdd)}"
                    else -> "Zet in @ ${String.format("%.2f", relevantOdd)}"
                }
            } else {
                "Geen odds beschikbaar"
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {}, 
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00FF9D),
                    contentColor = Color.Black
                ),
                enabled = relevantOdd != null
            ) {
                Text(
                    buttonText, 
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Value rating (if odds available)
            if (relevantOdd != null) {
                Spacer(modifier = Modifier.height(12.dp))
                ValueRatingCard(oddsValue = relevantOdd, confidence = confidence)
            }
        }
    }
}

/**
 * Kelly Value Card showing Kelly Criterion analysis for betting value.
 * Calculates optimal bet sizing based on our predictions vs bookmaker odds.
 */
@Composable
private fun KellyValueCard(
    matchDetail: MatchDetail,
    odds: com.Lyno.matchmindai.domain.model.OddsData?,
    hybridPrediction: com.Lyno.matchmindai.domain.model.HybridPrediction?
) {
    // Calculate Kelly result
    val kellyResult = remember(odds, hybridPrediction) {
        calculateKellyResult(matchDetail, odds, hybridPrediction)
    }
    
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Filled.TrendingUp,
                    contentDescription = "Kelly Value",
                    tint = Color(0xFF00FF9D),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "KELLY VALUE ANALYSE",
                    color = Color(0xFF00FF9D),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.weight(1f))
                
                // Value score indicator
                Box(
                    modifier = Modifier
                        .background(
                            color = when (kellyResult.overallValueScore) {
                                in 8..10 -> Color(0xFF00FF9D).copy(alpha = 0.2f)
                                in 6..7 -> Color(0xFFFFA726).copy(alpha = 0.2f)
                                in 4..5 -> Color(0xFFFFA726).copy(alpha = 0.1f)
                                else -> Color(0xFFF44336).copy(alpha = 0.1f)
                            },
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${kellyResult.overallValueScore}/10",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = when (kellyResult.overallValueScore) {
                            in 8..10 -> Color(0xFF00FF9D)
                            in 6..7 -> Color(0xFFFFA726)
                            in 4..5 -> Color(0xFFFFA726).copy(alpha = 0.8f)
                            else -> Color(0xFFF44336)
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Best value bet
            Text(
                text = kellyResult.bestValueBet.description,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Kelly fraction and recommended stake
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Kelly Fractie",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = kellyResult.bestKellyFraction?.let { 
                            String.format("%.3f", it) 
                        } ?: "Geen waarde",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            kellyResult.bestKellyFraction == null -> Color.Gray
                            kellyResult.bestKellyFraction!! >= 0.10 -> Color(0xFF00FF9D)
                            kellyResult.bestKellyFraction!! >= 0.05 -> Color(0xFFFFA726)
                            else -> Color(0xFFF44336)
                        }
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Aanbevolen inzet",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = kellyResult.recommendedStakeFormatted,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            kellyResult.recommendedStakePercentage <= 0.0 -> Color.Gray
                            kellyResult.recommendedStakePercentage >= 0.05 -> Color(0xFF00FF9D)
                            kellyResult.recommendedStakePercentage >= 0.02 -> Color(0xFFFFA726)
                            else -> Color(0xFFF44336)
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Risk assessment
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = "Risk Level",
                    tint = when (kellyResult.riskLevel) {
                        com.Lyno.matchmindai.domain.model.RiskLevel.LOW -> Color(0xFF00FF9D)
                        com.Lyno.matchmindai.domain.model.RiskLevel.MEDIUM -> Color(0xFFFFA726)
                        com.Lyno.matchmindai.domain.model.RiskLevel.HIGH -> Color(0xFFFFA726)
                        com.Lyno.matchmindai.domain.model.RiskLevel.VERY_HIGH -> Color(0xFFF44336)
                    },
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = kellyResult.riskDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = when (kellyResult.riskLevel) {
                        com.Lyno.matchmindai.domain.model.RiskLevel.LOW -> Color(0xFF00FF9D)
                        com.Lyno.matchmindai.domain.model.RiskLevel.MEDIUM -> Color(0xFFFFA726)
                        com.Lyno.matchmindai.domain.model.RiskLevel.HIGH -> Color(0xFFFFA726)
                        com.Lyno.matchmindai.domain.model.RiskLevel.VERY_HIGH -> Color(0xFFF44336)
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Analysis summary
            if (kellyResult.analysis.isNotEmpty()) {
                Text(
                    text = kellyResult.analysis,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3
                )
            }
            
            // Confidence indicator
            if (kellyResult.confidence > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Analyse Zekerheid",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${kellyResult.confidence}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            kellyResult.confidence >= 80 -> Color(0xFF00FF9D)
                            kellyResult.confidence >= 60 -> Color(0xFFFFA726)
                            else -> Color(0xFFF44336)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Helper function to map AI tip to BettingMarket.
 * Improved to handle "dubbele kans (X2)" and other complex betting tips.
 */
private fun mapAiTipToMarket(
    tip: String,
    homeTeam: String,
    awayTeam: String
): com.Lyno.matchmindai.domain.model.BettingMarket? {
    val lowerTip = tip.lowercase()
    val lowerHomeTeam = homeTeam.lowercase()
    val lowerAwayTeam = awayTeam.lowercase()
    
    // Check for "dubbele kans" (double chance) patterns first
    val hasDoubleChance = lowerTip.contains("dubbele kans") || 
                          lowerTip.contains("double chance") || 
                          lowerTip.contains("dc") ||
                          lowerTip.contains("x2") ||
                          lowerTip.contains("2x")
    
    if (hasDoubleChance) {
        // For double chance, determine which team is mentioned
        return when {
            lowerTip.contains(lowerHomeTeam) -> {
                // Home team double chance (1X) - map to HOME_WIN as primary
                com.Lyno.matchmindai.domain.model.BettingMarket.HOME_WIN
            }
            lowerTip.contains(lowerAwayTeam) -> {
                // Away team double chance (X2) - map to AWAY_WIN as primary
                com.Lyno.matchmindai.domain.model.BettingMarket.AWAY_WIN
            }
            else -> {
                // Generic double chance - map to DRAW as safest option
                com.Lyno.matchmindai.domain.model.BettingMarket.DRAW
            }
        }
    }
    
    // Regular market mapping
    return when {
        containsHomeWinKeywords(lowerTip, lowerHomeTeam) -> 
            com.Lyno.matchmindai.domain.model.BettingMarket.HOME_WIN
        containsAwayWinKeywords(lowerTip, lowerAwayTeam) -> 
            com.Lyno.matchmindai.domain.model.BettingMarket.AWAY_WIN
        containsDrawKeywords(lowerTip) -> 
            com.Lyno.matchmindai.domain.model.BettingMarket.DRAW
        else -> null
    }
}

/**
 * Calculate Kelly result for a specific AI tip.
 */
private fun calculateKellyForAiTip(
    matchDetail: MatchDetail,
    odds: com.Lyno.matchmindai.domain.model.OddsData,
    aiTip: String,
    aiConfidence: Int
): com.Lyno.matchmindai.domain.model.KellyResult {
    // Map AI tip to betting market
    val targetMarket = mapAiTipToMarket(aiTip, matchDetail.homeTeam, matchDetail.awayTeam)
    
    // Get odds for the target market
    val (targetOdds, oddType, oddMarket) = findRelevantOddForTip(aiTip, odds, matchDetail)
    
    if (targetMarket == null || targetOdds == null) {
        // Fallback to general Kelly calculation
        return calculateGeneralKellyResult(matchDetail, odds, null)
    }
    
    // Convert AI confidence (0-100) to probability (0.0-1.0)
    val probability = aiConfidence / 100.0
    
    // Calculate Kelly for the specific market
    val kellyFraction = com.Lyno.matchmindai.domain.model.calculateKellyFraction(probability, targetOdds)
    val valueScore = com.Lyno.matchmindai.domain.model.kellyToValueScore(kellyFraction)
    val riskLevel = com.Lyno.matchmindai.domain.model.kellyToRiskLevel(kellyFraction)
    val recommendedStake = com.Lyno.matchmindai.domain.model.getRecommendedStake(kellyFraction, riskLevel)
    
    // Create value bet description with market details
    val marketDescription = oddMarket?.let { " ($it)" } ?: ""
    val valueBetDescription = when (targetMarket) {
        com.Lyno.matchmindai.domain.model.BettingMarket.HOME_WIN -> 
            "${matchDetail.homeTeam} wint${marketDescription} - ${if (valueScore >= 6) "Goede" else "Matige"} waarde"
        com.Lyno.matchmindai.domain.model.BettingMarket.DRAW -> 
            "Gelijkspel${marketDescription} - ${if (valueScore >= 6) "Goede" else "Matige"} waarde"
        com.Lyno.matchmindai.domain.model.BettingMarket.AWAY_WIN -> 
            "${matchDetail.awayTeam} wint${marketDescription} - ${if (valueScore >= 6) "Goede" else "Matige"} waarde"
    }
    
    val valueBet = com.Lyno.matchmindai.domain.model.ValueBet(
        market = targetMarket,
        description = valueBetDescription,
        valueScore = valueScore
    )
    
    // Set Kelly values for all markets (only target market has value)
    val homeWinKelly = if (targetMarket == com.Lyno.matchmindai.domain.model.BettingMarket.HOME_WIN) kellyFraction else null
    val drawKelly = if (targetMarket == com.Lyno.matchmindai.domain.model.BettingMarket.DRAW) kellyFraction else null
    val awayWinKelly = if (targetMarket == com.Lyno.matchmindai.domain.model.BettingMarket.AWAY_WIN) kellyFraction else null
    
    val homeWinValueScore = if (targetMarket == com.Lyno.matchmindai.domain.model.BettingMarket.HOME_WIN) valueScore else 0
    val drawValueScore = if (targetMarket == com.Lyno.matchmindai.domain.model.BettingMarket.DRAW) valueScore else 0
    val awayWinValueScore = if (targetMarket == com.Lyno.matchmindai.domain.model.BettingMarket.AWAY_WIN) valueScore else 0
    
    // Create detailed analysis text
    val analysisText = buildString {
        append("Kelly analyse specifiek voor AI tip: '$aiTip' (${aiConfidence}% zekerheid)")
        append(" vs ${oddType ?: "odds"} ${String.format("%.2f", targetOdds)}")
        oddMarket?.let { append(" ($it)") }
        append(".")
    }
    
    return com.Lyno.matchmindai.domain.model.KellyResult(
        fixtureId = matchDetail.fixtureId,
        homeTeam = matchDetail.homeTeam,
        awayTeam = matchDetail.awayTeam,
        homeWinKelly = homeWinKelly,
        drawKelly = drawKelly,
        awayWinKelly = awayWinKelly,
        homeWinValueScore = homeWinValueScore,
        drawValueScore = drawValueScore,
        awayWinValueScore = awayWinValueScore,
        bestValueBet = valueBet,
        riskLevel = riskLevel,
        recommendedStakePercentage = recommendedStake,
        analysis = analysisText,
        confidence = aiConfidence
    )
}

/**
 * Calculate general Kelly result (fallback when no AI tip available).
 */
private fun calculateGeneralKellyResult(
    matchDetail: MatchDetail,
    odds: com.Lyno.matchmindai.domain.model.OddsData?,
    hybridPrediction: com.Lyno.matchmindai.domain.model.HybridPrediction?
): com.Lyno.matchmindai.domain.model.KellyResult {
    // Determine which prediction to use (prefer hybrid)
    val prediction = when {
        hybridPrediction != null -> hybridPrediction.enhancedPrediction
        else -> return com.Lyno.matchmindai.domain.model.KellyResult.empty(
            matchDetail.fixtureId,
            matchDetail.homeTeam,
            matchDetail.awayTeam
        )
    }
    
    // Check if we have odds
    if (odds == null || !odds.hasOdds) {
        return com.Lyno.matchmindai.domain.model.KellyResult.empty(
            matchDetail.fixtureId,
            matchDetail.homeTeam,
            matchDetail.awayTeam
        )
    }
    
    // Calculate Kelly result (in a real app, this would be async)
    // For now, we'll simulate the calculation
    return try {
        // In a real implementation, this would be a suspend call
        // For now, we'll create a simple result
        val homeWinKelly = com.Lyno.matchmindai.domain.model.calculateKellyFraction(
            prediction.homeWinProbability,
            odds.homeWinOdds ?: 0.0
        )
        val drawKelly = com.Lyno.matchmindai.domain.model.calculateKellyFraction(
            prediction.drawProbability,
            odds.drawOdds ?: 0.0
        )
        val awayWinKelly = com.Lyno.matchmindai.domain.model.calculateKellyFraction(
            prediction.awayWinProbability,
            odds.awayWinOdds ?: 0.0
        )
        
        val homeWinValueScore = com.Lyno.matchmindai.domain.model.kellyToValueScore(homeWinKelly)
        val drawValueScore = com.Lyno.matchmindai.domain.model.kellyToValueScore(drawKelly)
        val awayWinValueScore = com.Lyno.matchmindai.domain.model.kellyToValueScore(awayWinKelly)
        
        // Determine best value bet
        val bestMarket = when {
            homeWinValueScore >= drawValueScore && homeWinValueScore >= awayWinValueScore -> 
                com.Lyno.matchmindai.domain.model.BettingMarket.HOME_WIN
            drawValueScore >= homeWinValueScore && drawValueScore >= awayWinValueScore -> 
                com.Lyno.matchmindai.domain.model.BettingMarket.DRAW
            else -> com.Lyno.matchmindai.domain.model.BettingMarket.AWAY_WIN
        }
        
        val bestValueBet = com.Lyno.matchmindai.domain.model.ValueBet(
            market = bestMarket,
            description = when (bestMarket) {
                com.Lyno.matchmindai.domain.model.BettingMarket.HOME_WIN -> 
                    "${matchDetail.homeTeam} wint - ${if (homeWinValueScore >= 6) "Goede" else "Matige"} waarde"
                com.Lyno.matchmindai.domain.model.BettingMarket.DRAW -> 
                    "Gelijkspel - ${if (drawValueScore >= 6) "Goede" else "Matige"} waarde"
                com.Lyno.matchmindai.domain.model.BettingMarket.AWAY_WIN -> 
                    "${matchDetail.awayTeam} wint - ${if (awayWinValueScore >= 6) "Goede" else "Matige"} waarde"
            },
            valueScore = maxOf(homeWinValueScore, drawValueScore, awayWinValueScore)
        )
        
        val bestKelly = listOfNotNull(homeWinKelly, drawKelly, awayWinKelly).maxOrNull()
        val riskLevel = com.Lyno.matchmindai.domain.model.kellyToRiskLevel(bestKelly)
        val recommendedStake = com.Lyno.matchmindai.domain.model.getRecommendedStake(bestKelly, riskLevel)
        
        com.Lyno.matchmindai.domain.model.KellyResult(
            fixtureId = matchDetail.fixtureId,
            homeTeam = matchDetail.homeTeam,
            awayTeam = matchDetail.awayTeam,
            homeWinKelly = homeWinKelly,
            drawKelly = drawKelly,
            awayWinKelly = awayWinKelly,
            homeWinValueScore = homeWinValueScore,
            drawValueScore = drawValueScore,
            awayWinValueScore = awayWinValueScore,
            bestValueBet = bestValueBet,
            riskLevel = riskLevel,
            recommendedStakePercentage = recommendedStake,
            analysis = "Kelly analyse gebaseerd op ${if (hybridPrediction != null) "AI-verrijkte" else "statistische"} voorspelling vs bookmaker odds.",
            confidence = prediction.confidenceScore
        )
    } catch (e: Exception) {
        com.Lyno.matchmindai.domain.model.KellyResult.empty(
            matchDetail.fixtureId,
            matchDetail.homeTeam,
            matchDetail.awayTeam
        )
    }
}

/**
 * Calculate Kelly result based on available predictions and odds.
 * Uses AI tip-specific calculation when available, falls back to general calculation.
 */
private fun calculateKellyResult(
    matchDetail: MatchDetail,
    odds: com.Lyno.matchmindai.domain.model.OddsData?,
    hybridPrediction: com.Lyno.matchmindai.domain.model.HybridPrediction?
): com.Lyno.matchmindai.domain.model.KellyResult {
    // Check if we have odds
    if (odds == null || !odds.hasOdds) {
        return com.Lyno.matchmindai.domain.model.KellyResult.empty(
            matchDetail.fixtureId,
            matchDetail.homeTeam,
            matchDetail.awayTeam
        )
    }
    
    // If we have AI prediction with tip, use tip-specific calculation
    if (hybridPrediction != null) {
        val analysis = hybridPrediction.analysis
        val aiTip = analysis.getBettingTip(matchDetail.homeTeam, matchDetail.awayTeam)
        val aiConfidence = analysis.getBettingConfidence()
        
        // Only use AI-specific calculation if we have valid tip and confidence
        if (aiTip.isNotEmpty() && aiConfidence > 0) {
            return calculateKellyForAiTip(matchDetail, odds, aiTip, aiConfidence)
        }
    }
    
    // Fallback to general calculation
    return calculateGeneralKellyResult(matchDetail, odds, hybridPrediction)
}
