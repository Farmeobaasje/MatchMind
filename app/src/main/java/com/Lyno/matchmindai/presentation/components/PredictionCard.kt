package com.Lyno.matchmindai.presentation.components

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.Lyno.matchmindai.ui.theme.ConfidenceHigh
import com.Lyno.matchmindai.ui.theme.ConfidenceLow
import com.Lyno.matchmindai.ui.theme.ConfidenceMedium
import com.Lyno.matchmindai.ui.theme.MatchMindAITheme
import com.Lyno.matchmindai.ui.theme.Surface
import com.Lyno.matchmindai.ui.theme.TextHigh
import com.Lyno.matchmindai.ui.theme.TextMedium

@Composable
fun PredictionCard(
    winner: String,
    confidenceScore: Int,
    riskLevel: com.Lyno.matchmindai.domain.model.MatchPrediction.RiskLevel,
    reasoning: String,
    modifier: Modifier = Modifier,
    animate: Boolean = true
) {
    val confidenceColor = when {
        confidenceScore >= 80 -> ConfidenceHigh
        confidenceScore >= 60 -> ConfidenceMedium
        else -> ConfidenceLow
    }
    
    val confidenceText = when {
        confidenceScore >= 80 -> "ZEER HOOG"
        confidenceScore >= 60 -> "MATIG"
        else -> "LAAG"
    }
    
    val riskLevelText = when (riskLevel) {
        com.Lyno.matchmindai.domain.model.MatchPrediction.RiskLevel.LOW -> "LAAG"
        com.Lyno.matchmindai.domain.model.MatchPrediction.RiskLevel.MEDIUM -> "MATIG"
        com.Lyno.matchmindai.domain.model.MatchPrediction.RiskLevel.HIGH -> "HOOG"
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface,
            contentColor = TextHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header: Winner and Confidence Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "WINNAAR",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMedium
                    )
                    Text(
                        text = winner,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Confidence Badge
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CONFIDENCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = confidenceColor,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                    Text(
                        text = "$confidenceScore%",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                    }
                    Text(
                        text = confidenceText,
                        style = MaterialTheme.typography.labelSmall,
                        color = confidenceColor,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Risk Level
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RISICO NIVEAU:",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = riskLevelText,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Reasoning
            Column {
                Text(
                    text = "ANALYSE",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = reasoning,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PredictionCardHighConfidencePreview() {
    MatchMindAITheme {
        PredictionCard(
            winner = "Ajax",
            confidenceScore = 85,
            riskLevel = com.Lyno.matchmindai.domain.model.MatchPrediction.RiskLevel.LOW,
            reasoning = "Ajax toont consistente prestaties thuis met een sterk middenveld. De tegenstander heeft moeite met uitwedstrijden en mist enkele sleutelspelers door blessures."
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PredictionCardMediumConfidencePreview() {
    MatchMindAITheme {
        PredictionCard(
            winner = "Feyenoord",
            confidenceScore = 72,
            riskLevel = com.Lyno.matchmindai.domain.model.MatchPrediction.RiskLevel.MEDIUM,
            reasoning = "Feyenoord heeft thuisvoordeel maar de tegenstander komt met een versterkte defensie. De uitkomst hangt af van vroege doelpunten."
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PredictionCardLowConfidencePreview() {
    MatchMindAITheme {
        PredictionCard(
            winner = "PSV",
            confidenceScore = 45,
            riskLevel = com.Lyno.matchmindai.domain.model.MatchPrediction.RiskLevel.HIGH,
            reasoning = "Beide teams zijn in goede vorm. PSV heeft een licht voordeel door recente overwinningen, maar statistieken wijzen op een gelijkspel."
        )
    }
}
