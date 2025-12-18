package com.Lyno.matchmindai.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.Lyno.matchmindai.domain.model.AgentResponse
import com.Lyno.matchmindai.domain.model.MatchFixture
import com.Lyno.matchmindai.domain.model.MatchListData
import com.Lyno.matchmindai.domain.model.NewsItemData
import com.Lyno.matchmindai.domain.model.NewsListData
import com.Lyno.matchmindai.domain.model.StandingRow
import com.Lyno.matchmindai.ui.theme.MatchMindAITheme
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer as KListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/**
 * Composable that renders different types of AgentResponse with appropriate UI components.
 * This provides polymorphic rendering based on the response type.
 * 
 * FIX: Suggested actions are now rendered OUTSIDE the content bubble to ensure they're always clickable,
 * even when the AI returns unknown types like "ODDS_WIDGET".
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AgentResponseRenderer(
    agentResponse: AgentResponse,
    onActionClick: (String) -> Unit,
    onFixtureClick: (MatchFixture) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // STAP 1: De Inhoud (De tekst of de widget)
        // Gebruik een niet-exhaustive when statement om onbekende types te kunnen afhandelen
        // De AI kan types zoals "ODDS_WIDGET" teruggeven die niet in de enum staan
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            when (agentResponse.type) {
                AgentResponse.ResponseType.FIXTURES_WIDGET -> {
                    // Parse fixtures data from relatedData
                    val fixtures = parseFixturesFromJson(agentResponse.relatedData)
                    FixturesWidget(
                        summaryText = agentResponse.text,
                        fixtures = fixtures,
                        onFixtureClick = onFixtureClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                AgentResponse.ResponseType.NEWS_WIDGET -> {
                    // Parse news data from relatedData
                    val newsItems = parseNewsFromJson(agentResponse.relatedData)
                    NewsWidget(
                        summaryText = agentResponse.text,
                        newsItems = newsItems,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                AgentResponse.ResponseType.STANDINGS -> {
                    // Parse standings data from relatedData
                    val standings = parseStandingsFromJson(agentResponse.relatedData)
                    StandingsWidget(
                        summaryText = agentResponse.text,
                        standings = standings,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                AgentResponse.ResponseType.TEXT_ONLY,
                AgentResponse.ResponseType.LIVE_MATCH,
                AgentResponse.ResponseType.PREDICTION,
                AgentResponse.ResponseType.ANALYSIS,
                AgentResponse.ResponseType.ODDS_WIDGET,
                AgentResponse.ResponseType.UNKNOWN -> {
                    // Use GeneralAssistantMessageBubble for text-based responses
                    // But WITHOUT suggested actions (they'll be rendered separately below)
                    GeneralAssistantMessageBubble(
                        text = agentResponse.text,
                        onActionClick = onActionClick,
                        suggestedActions = emptyList(), // Empty - we'll render them separately
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        // STAP 2: De Knoppen (Suggested Actions)
        // PLAATS DIT BUITEN HET 'WHEN' BLOK!
        // Hierdoor worden de knoppen ALTIJD getoond, zelfs als de widget erboven 'stuk' is.
        if (agentResponse.suggestedActions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // FlowRow zorgt dat ze netjes op de volgende regel komen als het niet past
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                agentResponse.suggestedActions.forEach { action ->
                    SuggestionChip(
                        onClick = { onActionClick(action) },
                        label = { 
                            Text(
                                text = action, 
                                style = MaterialTheme.typography.bodySmall 
                            ) 
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
    }
}

/**
 * Parse fixtures data from JsonElement.
 * Supports both MatchListData format and direct list of MatchFixture.
 */
private fun parseFixturesFromJson(jsonElement: JsonElement?): List<MatchFixture> {
    if (jsonElement == null) return emptyList()
    
    return try {
        val json = Json { ignoreUnknownKeys = true }
        
        // Try to parse as MatchListData first
        val matchListData = json.decodeFromJsonElement(MatchListData.serializer(), jsonElement)
        matchListData.fixtures
    } catch (e: Exception) {
        // Fallback: try to parse as direct list of MatchFixture
        try {
            val json = Json { ignoreUnknownKeys = true }
            json.decodeFromJsonElement(ListSerializer(MatchFixture.serializer()), jsonElement)
        } catch (e2: Exception) {
            emptyList()
        }
    }
}

/**
 * Parse news items data from JsonElement.
 * Supports both NewsListData format and direct list of NewsItemData.
 */
private fun parseNewsFromJson(jsonElement: JsonElement?): List<NewsItemData> {
    if (jsonElement == null) return emptyList()
    
    return try {
        val json = Json { ignoreUnknownKeys = true }
        
        // Try to parse as NewsListData first
        val newsListData = json.decodeFromJsonElement(NewsListData.serializer(), jsonElement)
        newsListData.items
    } catch (e: Exception) {
        // Fallback: try to parse as direct list of NewsItemData
        try {
            val json = Json { ignoreUnknownKeys = true }
            json.decodeFromJsonElement(ListSerializer(NewsItemData.serializer()), jsonElement)
        } catch (e2: Exception) {
            emptyList()
        }
    }
}

/**
 * Parse standings data from JsonElement.
 * Supports both list of StandingRow format.
 */
private fun parseStandingsFromJson(jsonElement: JsonElement?): List<StandingRow> {
    if (jsonElement == null) return emptyList()
    
    return try {
        val json = Json { ignoreUnknownKeys = true }
        json.decodeFromJsonElement(ListSerializer(StandingRow.serializer()), jsonElement)
    } catch (e: Exception) {
        emptyList()
    }
}

// Helper for list serialization
private class ListSerializer<T>(private val elementSerializer: KSerializer<T>) : 
    KSerializer<List<T>> by KListSerializer(elementSerializer)

@Preview(showBackground = true)
@Composable
fun AgentResponseRendererFixturesPreview() {
    MatchMindAITheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            AgentResponseRenderer(
                agentResponse = AgentResponse.fixturesWidget(
                    text = "Hier zijn de wedstrijden van vanavond in de top competities:",
                    relatedData = null // In preview we can't easily create JsonElement
                ),
                onActionClick = {},
                onFixtureClick = {},
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AgentResponseRendererNewsPreview() {
    MatchMindAITheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            AgentResponseRenderer(
                agentResponse = AgentResponse.newsWidget(
                    text = "Hier is het laatste nieuws over Ajax en Feyenoord:",
                    relatedData = null // In preview we can't easily create JsonElement
                ),
                onActionClick = {},
                onFixtureClick = {},
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AgentResponseRendererTextPreview() {
    MatchMindAITheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            AgentResponseRenderer(
                agentResponse = AgentResponse.chat(
                    text = "Dit is een gewoon chatbericht met suggesties.",
                    suggestedActions = listOf("Voorspel winnaar", "Toon opstellingen")
                ),
                onActionClick = {},
                onFixtureClick = {},
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
