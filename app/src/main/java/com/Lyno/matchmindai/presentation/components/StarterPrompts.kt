package com.Lyno.matchmindai.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.Lyno.matchmindai.R
import com.Lyno.matchmindai.domain.model.MatchFixture
import com.Lyno.matchmindai.ui.theme.MatchMindAITheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Material Icons - Using Default icons that are available in Material 3
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.LocalHospital

/**
 * Data class representing a starter prompt suggestion.
 * @param icon The icon to display for the prompt
 * @param label The label text to display (should be a string resource ID)
 * @param promptText The text that will be sent to the AI when selected
 */
data class StarterPrompt(
    val icon: ImageVector,
    @androidx.annotation.StringRes val labelResId: Int? = null,
    val labelText: String? = null,
    val promptText: String,
    val isGenericPrompt: Boolean = false
)

/**
 * Get time-based greeting for Gemini styling.
 */
private fun getTimeBasedGreeting(): String {
    val hour = SimpleDateFormat("HH", Locale.getDefault()).format(Date()).toInt()
    return when {
        hour < 12 -> "Goedemorgen"
        hour < 18 -> "Goedemiddag"
        else -> "Goedenavond"
    }
}

/**
 * Generate dynamic starter prompts from match fixtures.
 * @param fixtures List of match fixtures to create prompts from
 * @param includeGenericPrompts Whether to include generic prompts when no fixtures are available
 */
fun generateStarterPrompts(
    fixtures: List<MatchFixture> = emptyList(),
    includeGenericPrompts: Boolean = true
): List<StarterPrompt> {
    val prompts = mutableListOf<StarterPrompt>()

    // Add dynamic match prompts from fixtures
    fixtures.take(4).forEach { match ->
        prompts.add(
            StarterPrompt(
                icon = Icons.Filled.Sports,
                labelText = "${match.homeTeam} - ${match.awayTeam}",
                promptText = "Predict the match ${match.homeTeam} vs ${match.awayTeam}. Analyze form, h2h and news. Reply strictly in Dutch.",
                isGenericPrompt = false
            )
        )
    }

    // Add generic prompts only if we have space or no fixtures
    if (includeGenericPrompts && prompts.size < 4) {
        val genericPrompts = listOf(
            StarterPrompt(
                icon = Icons.Filled.Sports,
                labelResId = R.string.starter_predict_match,
                promptText = "Check the football schedule for TODAY (${java.time.LocalDate.now()}). List the top 3 matches playing today and ask me which one to analyze. Reply in Dutch.",
                isGenericPrompt = false
            ),
            StarterPrompt(
                icon = Icons.Filled.Star,
                labelResId = R.string.starter_value_spotter,
                promptText = "Find matches playing TODAY with interesting odds or underdog potential. Reply in Dutch.",
                isGenericPrompt = false
            ),
            StarterPrompt(
                icon = Icons.Filled.Whatshot,
                labelResId = R.string.starter_spectacular_match,
                promptText = "Welke wedstrijd wordt vandaag het meest spectaculair of doelpuntrijk verwacht?",
                isGenericPrompt = false
            ),
            StarterPrompt(
                icon = Icons.Filled.LocalHospital,
                labelResId = R.string.starter_injury_check,
                promptText = "Zijn er belangrijke spelers geblesseerd bij de topteams vandaag?",
                isGenericPrompt = false
            )
        )

        // Add only enough generic prompts to fill up to 4 total
        val remainingSlots = 4 - prompts.size
        prompts.addAll(genericPrompts.take(remainingSlots))
    }

    return prompts
}

/**
 * Composable that displays a grid of starter prompt suggestions.
 * @param fixtures List of match fixtures to create dynamic prompts from
 * @param onPromptSelected Callback invoked when a prompt is selected
 * @param modifier Modifier for the component
 */
@Composable
fun StarterPrompts(
    fixtures: List<MatchFixture> = emptyList(),
    onPromptSelected: (String, Boolean) -> Unit, // (promptText, isGenericPrompt)
    modifier: Modifier = Modifier
) {
    val prompts = generateStarterPrompts(fixtures, includeGenericPrompts = fixtures.isEmpty())
    val greeting = getTimeBasedGreeting()

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Gemini-style greeting
        Text(
            text = greeting,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Klaar voor de aftrap?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(prompts) { prompt ->
                StarterPromptCard(
                    prompt = prompt,
                    onPromptSelected = onPromptSelected
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Of typ je eigen vraag hieronder",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

/**
 * Individual starter prompt card.
 * @param prompt The starter prompt to display
 * @param onPromptSelected Callback when the card is clicked
 * @param modifier Modifier for the card
 */
@Composable
private fun StarterPromptCard(
    prompt: StarterPrompt,
    onPromptSelected: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clickable { 
                onPromptSelected(prompt.promptText, prompt.isGenericPrompt)
            }
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        ),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Icon in top-left
            Icon(
                imageVector = prompt.icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Label text - handle both string resource and direct text
            Text(
                text = prompt.labelText ?: stringResource(id = prompt.labelResId!!),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StarterPromptsPreview() {
    MatchMindAITheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            StarterPrompts(
                fixtures = listOf(
                    MatchFixture("Ajax", "Feyenoord", "Zo 14 dec", "14:30", "Eredivisie"),
                    MatchFixture("PSV", "AZ", "Zo 14 dec", "16:45", "Eredivisie")
                ),
                onPromptSelected = { _, _ -> }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StarterPromptsEmptyPreview() {
    MatchMindAITheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            StarterPrompts(
                fixtures = emptyList(),
                onPromptSelected = { _, _ -> }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StarterPromptCardPreview() {
    MatchMindAITheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            StarterPromptCard(
                prompt = StarterPrompt(
                    icon = Icons.Filled.Sports,
                    labelText = "Ajax - Feyenoord",
                    promptText = "Predict the match Ajax vs Feyenoord. Analyze form, h2h and news. Reply strictly in Dutch.",
                    isGenericPrompt = false
                ),
                onPromptSelected = { _, _ -> }
            )
        }
    }
}
