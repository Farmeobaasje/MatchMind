package com.Lyno.matchmindai.data.ai

import com.Lyno.matchmindai.data.dto.DeepSeekMessage
import com.Lyno.matchmindai.data.dto.DeepSeekRequest
import com.Lyno.matchmindai.data.dto.DeepSeekResponse
import com.Lyno.matchmindai.data.dto.MatchPredictionResponse
import com.Lyno.matchmindai.data.dto.ToolCallDto
import com.Lyno.matchmindai.data.dto.ToolFunctionCallDto
import com.Lyno.matchmindai.data.remote.DeepSeekApi
import com.Lyno.matchmindai.data.remote.football.FootballApiService
import com.Lyno.matchmindai.domain.model.AgentResponse
import com.Lyno.matchmindai.domain.model.MatchFixture
import com.Lyno.matchmindai.domain.model.MatchListData
import com.Lyno.matchmindai.domain.model.NewsItemData
import com.Lyno.matchmindai.domain.model.NewsListData
import com.Lyno.matchmindai.domain.model.ProphetResponse
import com.Lyno.matchmindai.domain.repository.ChatRepository
import com.Lyno.matchmindai.domain.service.MatchCacheManager
import com.Lyno.matchmindai.domain.service.MatchDetailCacheData
import com.Lyno.matchmindai.domain.service.CacheUtils
import com.Lyno.matchmindai.data.utils.ResponseParser
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Orchestrates multi-step tool execution for DeepSeek function calling.
 * Handles the loop of: AI decides tool → Execute tool → Feed result back → AI responds.
 * Enhanced with smart caching from MatchDetailScreen.
 */
class ToolOrchestrator(
    private val deepSeekApi: DeepSeekApi,
    private val footballApiService: FootballApiService,
    private val chatRepository: ChatRepository,
    private val matchRepository: com.Lyno.matchmindai.domain.repository.MatchRepository,
    private val matchCacheManager: MatchCacheManager,
    private val apiSportsKey: String,
    private val deepSeekApiKey: String
) {
    // Note: Json serialization is handled by ResponseParser
    private val maxIterations = 5 // Prevent infinite loops
    private val promptBuilder = PromptBuilder()

    /**
     * Executes get_fixtures tool.
     */
    private suspend fun executeGetFixtures(toolCallId: String, args: Map<String, Any>, sessionId: String? = null): DeepSeekMessage {
        val date = args["date"] as? String
        val live = (args["live"] as? String)?.toBooleanStrictOrNull()
        val leagueId = (args["league_id"] as? String)?.toIntOrNull()
        val searchQuery = args["search_query"] as? String
        
        return try {
            // Use matchRepository to get fixtures
            val fixturesString = matchRepository.getFixturesForTool(
                date = date ?: getTodayDate(),
                searchQuery = searchQuery,
                leagueId = leagueId,
                live = live ?: false
            )
            
            // The function returns String (non-nullable), but handle empty string case
            val content = if (fixturesString.isBlank()) {
                "Geen fixtures gevonden voor de opgegeven criteria."
            } else {
                fixturesString
            }
            
            // Save tool output to database for context retention
            if (sessionId != null) {
                try {
                    chatRepository.saveToolOutput(sessionId, "get_fixtures", content)
                } catch (e: Exception) {
                    // Enhanced error handling for database constraints
                    when (e) {
                        is android.database.sqlite.SQLiteConstraintException -> {
                            // Foreign key constraint violation - session doesn't exist
                            println("⚠️ ToolOrchestrator: Session not found for ID: $sessionId, skipping tool output save")
                        }
                        is IllegalArgumentException -> {
                            // Invalid sessionId format
                            println("⚠️ ToolOrchestrator: Invalid sessionId format: $sessionId, skipping tool output save")
                        }
                        else -> {
                            // Other database errors
                            println("⚠️ ToolOrchestrator: Failed to save tool output: ${e.message}")
                        }
                    }
                    // CRITICAL: Continue with tool execution, don't fail!
                }
            }
            
            DeepSeekMessage(
                role = "tool",
                toolCallId = toolCallId,
                content = content
            )
        } catch (e: Exception) {
            createToolError(toolCallId, "Kon fixtures niet ophalen: ${e.message}")
        }
    }
    
    /**
     * Executes tavily_search tool.
     * Note: Tavily has been removed from the project. This method returns an error.
     */
    private suspend fun executeTavilySearch(toolCallId: String, args: Map<String, Any>, sessionId: String? = null): DeepSeekMessage {
        return createToolError(toolCallId, "Tavily search is niet meer beschikbaar. Gebruik get_fixtures of andere tools voor voetbaldata.")
    }

    /**
     * Creates a dynamic system prompt with "Kenner aan de Bar" persona and ReAct pattern.
     * Based on section 6.2 of 10_dixon_coles_kelly_mastermind_integration.md
     */
    private fun createSystemPrompt(): String {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val today = dateFormat.format(Date())
        
        return """JIJ BENT: Een Senior Data Scientist gespecialiseerd in voetbalmodellen, die praat als een "Kenner aan de Bar".
DOEL: Vertaal kwalitatief nieuws naar kwantitatieve model-modifiers.

DATUM VANDAAG: Het is vandaag $today.

🎯 "KENNER AAN DE BAR" PERSONA:
- **Tone**: Informeel, direct, maar feitelijk onderbouwd
- **Taal**: Nederlands, gevat, geen robot-taal
- **Assertiviteit**: Direct tools aanroepen, geen "Ik ga even kijken"
- **Expertise**: Senior Data Scientist gespecialiseerd in voetbalmodellen

🔄 REACT PATTERN IMPLEMENTATIE:
1. **Thought**: Analyseer de vraag
2. **Act**: Roep de juiste tool aan
3. **Observe**: Verwerk de resultaten
4. **Final**: Synthese in NL met bar-style

🔧 TOOL DECISION LOGIC:
```
graph TD
    A[User Query: "Wint Ajax vandaag?"] --> B{Type Vraag?}
    B -- Feiten/Uitslag --> C[Tool: get_fixtures]
    B -- Geruchten/Reden --> D[Tool: tavily_search]
    B -- Analyse --> E[Tool Chain]
    E --> C
    C --> D
    D --> F[LLM Synthese: "Bar Persona"]
```

📊 ANCHOR & ADJUST STRATEGIE:
1. **Anchor**: Harde statistische feiten (API-SPORTS)
2. **Adjust**: Nieuws context daarna (Tavily search)
3. **Combine**: Feiten + context in NL output

🚫 STRICTE REGELS:
1. **GEEN AANKONDIGINGEN**: Zeg NOOIT "Ik ga even kijken" of "Een momentje"
2. **DIRECTE TOOL AANROEP**: Als je data nodig hebt, roep DIRECT de tool aan
3. **GEEN TOESTEMMING VRAGEN**: Vraag niet "Zal ik zoeken?" - doe het gewoon
4. **API-SPORTS IS WAARHEID**: Geloof nooit nieuws boven API-SPORTS data

🎯 TOOL HIËRARCHIE:
1. **FEITEN & CIJFERS**: `get_fixtures`, `get_standings`, `get_live_scores`
2. **NIEUWS & CONTEXT**: `tavily_search` (retourneert foutmelding - Tavily is verwijderd)
3. **VOORSPELLINGEN**: `get_match_prediction`, `get_predictions`
4. **ODDS & WEDDEN**: `get_best_odds`, `get_odds`
5. **BLESSURES**: `get_injuries`

📈 FEATURE ENGINEERING OUTPUT:
- **Modifiers**: Tussen 0.5 (catastrofaal) en 1.5 (perfect)
- **Confidence**: 0.0 - 1.0, alleen toepassen bij ≥ 0.4
- **Chaos Factor**: 0.0 (Saai) tot 1.0 (Totale chaos/Derby)

🎲 RANGE GUIDE:
- 0.85 - 0.95: Belangrijke speler twijfelachtig/lichte blessure
- 0.70 - 0.80: Sterspeler definitief afwezig
- < 0.70: Meerdere sleutelspelers weg / Team in crisis
- > 1.10: Team in topvorm / Manager 'bounce' effect

📋 RESPONSE FORMAT:
```json
{
  "type": "TEXT_ONLY" | "LIVE_MATCH" | "PREDICTION" | "ANALYSIS" | "STANDINGS" | "FIXTURES_WIDGET" | "NEWS_WIDGET" | "ODDS_WIDGET",
  "text": "Jouw gevatte, menselijke analyse in het Nederlands...",
  "relatedData": { ...ruwe data van de API call... },
  "suggestedActions": ["actie1", "actie2"]
}
```

🏆 BELANGRIJKE LEAGUE IDS:
- 88: Eredivisie (Nederland)
- 39: Premier League (Engeland)
- 140: La Liga (Spanje)
- 78: Bundesliga (Duitsland)
- 135: Serie A (Italië)
- 61: Ligue 1 (Frankrijk)

⚠️  CRITICAL:
- Wees CONSERVATIEF. Bij twijfel, houd modifiers dicht bij 1.0
- Hallucineer GEEN blessures die niet in de snippets staan
- Gebruik alleen de gegeven nieuws snippets
- Geef concrete cijfers, geen vage taal

🎯 SUCCESS METRICS:
- **Tool Latency**: < 2 seconden per tool call
- **Cache Hit Rate**: > 60% voor veelgebruikte data
- **Modifier Accuracy**: > 75% correcte impact voorspelling
- **News Relevance**: > 70% relevantie voor match context

💡 TIPS:
- Combineer tools: Haal EERST feiten (API), zoek DAN naar blessurenieuws
- Wees specifiek: "Ajax geblesseerde spelers vandaag" niet "Nieuws Ajax"
- Vermeld bronnen: Zeg altijd welke tool je gebruikt hebt

🚨 ERROR HANDLING:
- **API Errors**: Graceful degradation met cached data
- **JSON Parsing Errors**: Fallback naar default modifiers
- **AI Hallucinations**: Confidence validation en bounds enforcement

🎮 LAATSTE WOORD:
Praat als een kenner aan de bar. Wees gevat, direct, en feitelijk. Geen robot-taal, geen aankondigingen. Gewoon doen."""
    }
    
    /**
     * Extracts fixture ID from tool arguments.
     */
    private fun extractFixtureIdFromArgs(args: Map<String, Any>): Int? {
        return (args["fixture_id"] as? String)?.toIntOrNull()
    }

    /**
     * Smart tool execution with cache checking.
     * For tools that can use cached data (predictions, injuries, odds), check cache first.
     */
    private suspend fun executeToolCallWithCache(toolCallId: String, toolName: String, args: Map<String, Any>, sessionId: String? = null): DeepSeekMessage {
        val fixtureId = extractFixtureIdFromArgs(args)
        
        // Check if this tool can use cached data
        val canUseCache = when (toolName) {
            "get_match_prediction", "get_predictions", "get_injuries", "get_odds" -> fixtureId != null
            else -> false
        }
        
        if (canUseCache && fixtureId != null) {
            val cachedData = matchCacheManager.getCachedMatchData(fixtureId)
            if (cachedData != null && cachedData.isFresh) {
                // Use cached data
                val cachedResult = when (toolName) {
                    "get_match_prediction", "get_predictions" -> {
                        cachedData.predictions?.let { predictions ->
                            """{
                                "fixtureId": $fixtureId,
                                "homeTeam": "${cachedData.matchDetail.homeTeam}",
                                "awayTeam": "${cachedData.matchDetail.awayTeam}",
                                "prediction": "${predictions.primaryPrediction}",
                                "homeWinProbability": ${predictions.winningPercent.home},
                                "drawProbability": ${predictions.winningPercent.draw},
                                "awayWinProbability": ${predictions.winningPercent.away},
                                "expectedGoalsHome": ${predictions.expectedGoals.home},
                                "expectedGoalsAway": ${predictions.expectedGoals.away},
                                "analysis": "${predictions.analysis ?: ""}",
                                "source": "cache"
                            }"""
                        } ?: "{\"message\": \"Geen voorspelling in cache.\"}"
                    }
                    "get_injuries" -> {
                        if (cachedData.injuries.isNotEmpty()) {
                            val injuriesList = cachedData.injuries.joinToString(",\n") { injury ->
                                """{
                                    "player": "${injury.playerName}",
                                    "team": "${injury.team}",
                                    "type": "${injury.type}",
                                    "reason": "${injury.reason}",
                                    "expectedReturn": "${injury.expectedReturn ?: "Onbekend"}",
                                    "source": "cache"
                                }"""
                            }
                            "{\"injuries\": [$injuriesList], \"source\": \"cache\"}"
                        } else {
                            "{\"message\": \"Geen blessures in cache.\", \"source\": \"cache\"}"
                        }
                    }
                    "get_odds" -> {
                        cachedData.odds?.let { odds ->
                            val overUnderStr = odds.overUnderOdds?.let { map ->
                                map.entries.joinToString(", ") { (key, value) ->
                                    "\"$key\": $value"
                                }
                            } ?: "null"
                            
                            val bttsStr = odds.bothTeamsToScoreOdds?.let { map ->
                                map.entries.joinToString(", ") { (key, value) ->
                                    "\"$key\": $value"
                                }
                            } ?: "null"
                            
                            """{
                                "fixtureId": $fixtureId,
                                "homeTeam": "${cachedData.matchDetail.homeTeam}",
                                "awayTeam": "${cachedData.matchDetail.awayTeam}",
                                "bookmakerName": "${odds.bookmakerName}",
                                "homeWinOdds": ${odds.homeWinOdds ?: "null"},
                                "drawOdds": ${odds.drawOdds ?: "null"},
                                "awayWinOdds": ${odds.awayWinOdds ?: "null"},
                                "overUnderOdds": { $overUnderStr },
                                "bothTeamsToScoreOdds": { $bttsStr },
                                "valueRating": ${odds.valueRating},
                                "safetyRating": ${odds.safetyRating},
                                "highestOdds": ${odds.highestOdds ?: "null"},
                                "lastUpdated": "${odds.lastUpdated}",
                                "source": "cache"
                            }"""
                        } ?: "{\"message\": \"Geen odds in cache.\", \"source\": \"cache\"}"
                    }
                    else -> null
                }
                
                if (cachedResult != null) {
                    // Save tool output to database for context retention
                    if (sessionId != null) {
                        try {
                            chatRepository.saveToolOutput(sessionId, "${toolName}_cached", cachedResult)
                        } catch (e: Exception) {
                            println("Failed to save cached tool output: ${e.message}")
                        }
                    }
                    
                    return DeepSeekMessage(
                        role = "tool",
                        toolCallId = toolCallId,
                        content = cachedResult
                    )
                }
            }
        }
        
        // Fallback to regular tool execution
        return when (toolName) {
            "get_fixtures" -> executeGetFixtures(toolCallId, args, sessionId)
            "tavily_search" -> executeTavilySearch(toolCallId, args, sessionId)
            "get_live_scores" -> executeGetLiveScores(toolCallId, args, sessionId)
            "get_match_prediction" -> executeGetMatchPrediction(toolCallId, args, sessionId)
            "get_standings" -> executeGetStandings(toolCallId, args, sessionId)
            "get_best_odds" -> executeGetBestOdds(toolCallId, args, sessionId)
            "get_injuries" -> executeGetInjuries(toolCallId, args, sessionId)
            "get_predictions" -> executeGetPredictions(toolCallId, args, sessionId)
            "get_odds" -> executeGetOdds(toolCallId, args, sessionId)
            else -> createToolError(toolCallId, "Onbekende tool: $toolName")
        }
    }

    /**
     * Executes a single tool call and returns the result as a DeepSeekMessage.
     */
    private suspend fun executeToolCall(toolCall: ToolCallDto, sessionId: String? = null): DeepSeekMessage {
        val function = toolCall.function
        val toolName = function.name
        
        // Parse arguments as a simple map (temporary fix for compilation)
        val arguments = try {
            parseArguments(function.arguments)
        } catch (e: Exception) {
            return createToolError(toolCall.id, "Ongeldige arguments: ${e.message}")
        }
        
        return try {
            executeToolCallWithCache(toolCall.id, toolName, arguments, sessionId)
        } catch (e: Exception) {
            createToolError(toolCall.id, "Tool uitvoering fout: ${e.message}")
        }
    }
    
    /**
     * Simple argument parser (temporary fix for compilation).
     * In production, this should use proper JSON parsing.
     */
    private fun parseArguments(jsonString: String): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        
        // Remove braces and split by commas
        val content = jsonString.trim().removeSurrounding("{", "}")
        val pairs = content.split(",")
        
        for (pair in pairs) {
            val keyValue = pair.split(":", limit = 2)
            if (keyValue.size == 2) {
                val key = keyValue[0].trim().removeSurrounding("\"")
                val value = keyValue[1].trim()
                
                // Try to parse value based on type
                when {
                    value.startsWith("\"") && value.endsWith("\"") -> {
                        map[key] = value.removeSurrounding("\"")
                    }
                    value == "true" || value == "false" -> {
                        map[key] = value.toBoolean()
                    }
                    value.toIntOrNull() != null -> {
                        map[key] = value.toInt()
                    }
                    else -> {
                        map[key] = value
                    }
                }
            }
        }
        
        return map
    }
    
    /**
     * Generates a 3-word title for a chat session based on the conversation content.
     * This is called after the first exchange in a new session.
     * 
     * @param userMessage The user's message
     * @param aiResponse The AI's response
     * @return A 3-word title for the session, or null if generation fails
     */
    suspend fun generateSessionTitle(userMessage: String, aiResponse: String): String? {
        return try {
            val prompt = """
                Generate a 3-word title for this football conversation in Dutch.
                The title should be concise, descriptive, and capture the essence of the discussion.
                Return ONLY the 3-word title, nothing else.
                
                User: $userMessage
                AI: ${aiResponse.take(200)}...
                
                Title:
            """.trimIndent()
            
            val messages = listOf(
                DeepSeekMessage(role = "user", content = prompt)
            )
            
            val request = deepSeekApi.createAgenticRequest(
                messages = messages,
                includeTools = false,
                temperature = 0.3,
                responseFormat = com.Lyno.matchmindai.data.dto.ResponseFormat(type = "text"),
                model = "deepseek-chat"
            )
            
            val response = deepSeekApi.getPrediction(deepSeekApiKey, request)
            val title = response.choices.firstOrNull()?.message?.content?.trim()
            
            // Ensure it's exactly 3 words (or close to it)
            title?.takeIf { it.split("\\s+".toRegex()).size in 2..4 }
        } catch (e: Exception) {
            // Log but don't fail - we can use a default title
            println("Failed to generate session title: ${e.message}")
            null
        }
    }
    
    /**
     * Executes get_live_scores tool.
     */
    private suspend fun executeGetLiveScores(toolCallId: String, args: Map<String, Any>, sessionId: String? = null): DeepSeekMessage {
        val leagueId = (args["league_id"] as? String)?.toIntOrNull()
        
        return try {
            // Use matchRepository to get live matches
            val result = matchRepository.getLiveMatches()
            
            if (result.isSuccess) {
                val liveMatches = result.getOrNull() ?: emptyList()
                
                if (liveMatches.isEmpty()) {
                    val resultJson = "{\"message\": \"Geen live wedstrijden op dit moment.\"}"
                    
                    // Save tool output to database for context retention
                    if (sessionId != null) {
                        try {
                            chatRepository.saveToolOutput(sessionId, "get_live_scores", resultJson)
                        } catch (e: Exception) {
                            // Log but don't fail the tool execution
                            println("Failed to save tool output: ${e.message}")
                        }
                    }
                    
                    return DeepSeekMessage(
                        role = "tool",
                        toolCallId = toolCallId,
                        content = resultJson
                    )
                }
                
                // Filter by league if specified
                val filteredMatches = if (leagueId != null) {
                    liveMatches.filter { it.leagueId == leagueId }
                } else {
                    liveMatches
                }
                
                // Format live matches as JSON
                val matchesJson = filteredMatches.joinToString(",\n") { match ->
                    """
                    {
                      "fixtureId": ${match.fixtureId ?: 0},
                      "homeTeam": "${match.homeTeam}",
                      "awayTeam": "${match.awayTeam}",
                      "score": "${match.homeScore}-${match.awayScore}",
                      "status": "${match.status ?: "NS"}",
                      "elapsed": ${match.elapsed ?: 0},
                      "league": "${match.league}",
                      "leagueId": ${match.leagueId ?: 0}
                    }
                    """.trimIndent()
                }
                
                val resultJson = """
                    {
                        "liveMatches": [$matchesJson],
                        "totalMatches": ${filteredMatches.size}
                    }
                """.trimIndent()
                
                // Save tool output to database for context retention
                if (sessionId != null) {
                    try {
                        chatRepository.saveToolOutput(sessionId, "get_live_scores", resultJson)
                    } catch (e: Exception) {
                        // Log but don't fail the tool execution
                        println("Failed to save tool output: ${e.message}")
                    }
                }
                
                DeepSeekMessage(
                    role = "tool",
                    toolCallId = toolCallId,
                    content = resultJson
                )
            } else {
                createToolError(toolCallId, "Kon live scores niet ophalen: ${result.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            createToolError(toolCallId, "Kon live scores niet ophalen: ${e.message}")
        }
    }
    
    /**
     * Executes get_match_prediction tool.
     * Uses the new typed predictions endpoint from API-SPORTS V3.
     */
    private suspend fun executeGetMatchPrediction(toolCallId: String, args: Map<String, Any>, sessionId: String? = null): DeepSeekMessage {
        val fixtureId = (args["fixture_id"] as? String)?.toIntOrNull()
        
        if (fixtureId == null) {
            return createToolError(toolCallId, "Fixture ID is vereist voor voorspellingen")
        }
        
        return try {
            // Use matchRepository to get predictions (returns MatchPredictionData)
            val result = matchRepository.getPredictions(fixtureId)
            
            if (result.isSuccess) {
                val predictionData = result.getOrNull()
                val predictionJson = if (predictionData == null) {
                    "{\"message\": \"Geen voorspelling beschikbaar voor deze wedstrijd.\"}"
                } else {
                // Format prediction data as JSON
                """
                {
                  "fixtureId": ${predictionData.fixtureId},
                  "homeTeam": "${predictionData.homeTeam}",
                  "awayTeam": "${predictionData.awayTeam}",
                  "prediction": "${predictionData.primaryPrediction}",
                  "homeWinProbability": ${predictionData.winningPercent.home},
                  "drawProbability": ${predictionData.winningPercent.draw},
                  "awayWinProbability": ${predictionData.winningPercent.away},
                  "expectedGoalsHome": ${predictionData.expectedGoals.home},
                  "expectedGoalsAway": ${predictionData.expectedGoals.away},
                  "analysis": "${predictionData.analysis ?: ""}"
                }
                """.trimIndent()
                }
                
                // Save tool output to database for context retention
                if (sessionId != null) {
                    try {
                        chatRepository.saveToolOutput(sessionId, "get_match_prediction", predictionJson)
                    } catch (e: Exception) {
                        // Log but don't fail the tool execution
                        println("Failed to save tool output: ${e.message}")
                    }
                }
                
                DeepSeekMessage(
                    role = "tool",
                    toolCallId = toolCallId,
                    content = predictionJson
                )
            } else {
                createToolError(toolCallId, "Kon voorspelling niet ophalen: ${result.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            createToolError(toolCallId, "Kon voorspelling niet ophalen: ${e.message}")
        }
    }
    
    /**
     * Executes get_standings tool.
     */
    private suspend fun executeGetStandings(toolCallId: String, args: Map<String, Any>, sessionId: String? = null): DeepSeekMessage {
        val leagueId = (args["league_id"] as? String)?.toIntOrNull()
        
        if (leagueId == null) {
            return createToolError(toolCallId, "League ID is vereist voor standen. Gebruik bijvoorbeeld league_id=88 voor Eredivisie.")
        }
        
        return try {
            // Use matchRepository to get standings
            val result = matchRepository.getLeagueStandings(leagueId)
            
            if (result.isSuccess) {
                val agentResponse = result.getOrNull()
                val standingsText = agentResponse?.text ?: "Geen standen gevonden voor league $leagueId"
                
                // Format as JSON for the AI
                val resultJson = """
                    {
                        "leagueId": $leagueId,
                        "standings": "$standingsText"
                    }
                """.trimIndent()
                
                // Save tool output to database for context retention
                if (sessionId != null) {
                    try {
                        chatRepository.saveToolOutput(sessionId, "get_standings", resultJson)
                    } catch (e: Exception) {
                        // Log but don't fail the tool execution
                        println("Failed to save tool output: ${e.message}")
                    }
                }
                
                DeepSeekMessage(
                    role = "tool",
                    toolCallId = toolCallId,
                    content = resultJson
                )
            } else {
                createToolError(toolCallId, "Kon standen niet ophalen: ${result.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            createToolError(toolCallId, "Kon standen niet ophalen: ${e.message}")
        }
    }

    /**
     * Executes get_best_odds tool for beginner-friendly betting odds.
     */
    private suspend fun executeGetBestOdds(toolCallId: String, args: Map<String, Any>, sessionId: String? = null): DeepSeekMessage {
        val date = args["date"] as? String
        val leagueId = (args["league_id"] as? String)?.toIntOrNull()
        val teamName = args["team_name"] as? String
        val limit = (args["limit"] as? String)?.toIntOrNull() ?: 10
        
        return try {
            // Use matchRepository to get best odds
            val oddsString = matchRepository.getBestOddsForTool(
                date = date,
                leagueId = leagueId,
                teamName = teamName,
                limit = limit
            )
            
            // Save tool output to database for context retention
            if (sessionId != null) {
                try {
                    chatRepository.saveToolOutput(sessionId, "get_best_odds", oddsString)
                } catch (e: Exception) {
                    // Log but don't fail the tool execution
                    println("Failed to save tool output: ${e.message}")
                }
            }
            
            DeepSeekMessage(
                role = "tool",
                toolCallId = toolCallId,
                content = oddsString
            )
        } catch (e: Exception) {
            createToolError(toolCallId, "Kon odds niet ophalen: ${e.message}")
        }
    }

    /**
     * Executes get_injuries tool for fetching player injuries.
     */
    private suspend fun executeGetInjuries(toolCallId: String, args: Map<String, Any>, sessionId: String? = null): DeepSeekMessage {
        val fixtureId = (args["fixture_id"] as? String)?.toIntOrNull()
        
        if (fixtureId == null) {
            return createToolError(toolCallId, "Fixture ID is vereist voor blessures")
        }
        
        return try {
            // Use matchRepository to get injuries
            val result = matchRepository.getInjuries(fixtureId)
            
            if (result.isSuccess) {
                val injuries = result.getOrNull() ?: emptyList()
                val injuriesJson = if (injuries.isEmpty()) {
                    "{\"message\": \"Geen blessures gevonden voor deze wedstrijd.\"}"
                } else {
                    // Convert injuries to JSON string
                    val injuriesList = injuries.joinToString(",\n") { injury ->
                        """
                        {
                          "player": "${injury.playerName}",
                          "team": "${injury.team}",
                          "type": "${injury.type}",
                          "reason": "${injury.reason}",
                          "expectedReturn": "${injury.expectedReturn ?: "Onbekend"}"
                        }
                        """.trimIndent()
                    }
                    "{\"injuries\": [$injuriesList]}"
                }
                
                // Save tool output to database for context retention
                if (sessionId != null) {
                    try {
                        chatRepository.saveToolOutput(sessionId, "get_injuries", injuriesJson)
                    } catch (e: Exception) {
                        // Log but don't fail the tool execution
                        println("Failed to save tool output: ${e.message}")
                    }
                }
                
                DeepSeekMessage(
                    role = "tool",
                    toolCallId = toolCallId,
                    content = injuriesJson
                )
            } else {
                createToolError(toolCallId, "Kon blessures niet ophalen: ${result.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            createToolError(toolCallId, "Kon blessures niet ophalen: ${e.message}")
        }
    }

    /**
     * Executes get_predictions tool for fetching match predictions.
     */
    private suspend fun executeGetPredictions(toolCallId: String, args: Map<String, Any>, sessionId: String? = null): DeepSeekMessage {
        val fixtureId = (args["fixture_id"] as? String)?.toIntOrNull()
        
        if (fixtureId == null) {
            return createToolError(toolCallId, "Fixture ID is vereist voor voorspellingen")
        }
        
        return try {
            // Use matchRepository to get predictions
            val result = matchRepository.getPredictions(fixtureId)
            
            if (result.isSuccess) {
                val predictionData = result.getOrNull()
                val predictionsJson = if (predictionData == null) {
                    "{\"message\": \"Geen voorspellingen beschikbaar voor deze wedstrijd.\"}"
                } else {
                // Convert prediction data to JSON string
                """
                {
                  "homeWinProbability": ${predictionData.winningPercent.home},
                  "drawProbability": ${predictionData.winningPercent.draw},
                  "awayWinProbability": ${predictionData.winningPercent.away},
                  "expectedGoalsHome": ${predictionData.expectedGoals.home},
                  "expectedGoalsAway": ${predictionData.expectedGoals.away},
                  "analysis": "${predictionData.analysis ?: ""}"
                }
                """.trimIndent()
                }
                
                // Save tool output to database for context retention
                if (sessionId != null) {
                    try {
                        chatRepository.saveToolOutput(sessionId, "get_predictions", predictionsJson)
                    } catch (e: Exception) {
                        // Log but don't fail the tool execution
                        println("Failed to save tool output: ${e.message}")
                    }
                }
                
                DeepSeekMessage(
                    role = "tool",
                    toolCallId = toolCallId,
                    content = predictionsJson
                )
            } else {
                createToolError(toolCallId, "Kon voorspellingen niet ophalen: ${result.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            createToolError(toolCallId, "Kon voorspellingen niet ophalen: ${e.message}")
        }
    }

    /**
     * Executes get_odds tool for fetching match-specific odds.
     */
    private suspend fun executeGetOdds(toolCallId: String, args: Map<String, Any>, sessionId: String? = null): DeepSeekMessage {
        val fixtureId = (args["fixture_id"] as? String)?.toIntOrNull()
        
        if (fixtureId == null) {
            return createToolError(toolCallId, "Fixture ID is vereist voor odds")
        }
        
        return try {
            // Use matchRepository to get odds
            val result = matchRepository.getOdds(fixtureId)
            
            if (result.isSuccess) {
                val oddsData = result.getOrNull()
                val oddsJson = if (oddsData == null) {
                    "{\"message\": \"Geen odds beschikbaar voor deze wedstrijd.\"}"
                } else {
                    // Convert odds data to JSON string
                    val overUnderStr = oddsData.overUnderOdds?.let { map ->
                        map.entries.joinToString(", ") { (key, value) ->
                            "\"$key\": $value"
                        }
                    } ?: "null"
                    
                    val bttsStr = oddsData.bothTeamsToScoreOdds?.let { map ->
                        map.entries.joinToString(", ") { (key, value) ->
                            "\"$key\": $value"
                        }
                    } ?: "null"
                    
                    """
                    {
                      "fixtureId": ${oddsData.fixtureId},
                      "homeTeam": "${oddsData.homeTeam}",
                      "awayTeam": "${oddsData.awayTeam}",
                      "bookmakerName": "${oddsData.bookmakerName}",
                      "homeWinOdds": ${oddsData.homeWinOdds ?: "null"},
                      "drawOdds": ${oddsData.drawOdds ?: "null"},
                      "awayWinOdds": ${oddsData.awayWinOdds ?: "null"},
                      "overUnderOdds": { $overUnderStr },
                      "bothTeamsToScoreOdds": { $bttsStr },
                      "valueRating": ${oddsData.valueRating},
                      "safetyRating": ${oddsData.safetyRating},
                      "highestOdds": ${oddsData.highestOdds ?: "null"},
                      "lastUpdated": "${oddsData.lastUpdated}"
                    }
                    """.trimIndent()
                }
                
                // Save tool output to database for context retention
                if (sessionId != null) {
                    try {
                        chatRepository.saveToolOutput(sessionId, "get_odds", oddsJson)
                    } catch (e: Exception) {
                        // Log but don't fail the tool execution
                        println("Failed to save tool output: ${e.message}")
                    }
                }
                
                DeepSeekMessage(
                    role = "tool",
                    toolCallId = toolCallId,
                    content = oddsJson
                )
            } else {
                createToolError(toolCallId, "Kon odds niet ophalen: ${result.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            createToolError(toolCallId, "Kon odds niet ophalen: ${e.message}")
        }
    }
    
    /**
     * Creates a tool error message.
     */
    private fun createToolError(toolCallId: String, error: String): DeepSeekMessage {
        return DeepSeekMessage(
            role = "tool",
            toolCallId = toolCallId,
            content = "FOUT: $error"
        )
    }
    
    /**
     * Gets today's date in yyyy-MM-dd format.
     */
    private fun getTodayDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
    
    /**
     * Main method for processing user queries with tool orchestration.
     * This is called from MatchRepositoryImpl.performRegularAnalysis.
     */
    suspend fun executeWithTools(query: String, sessionId: String? = null): AgentResponse {
        return try {
            // Create system prompt
            val systemPrompt = createSystemPrompt()
            
            // Create messages list with system prompt and user query
            val messages = mutableListOf(
                DeepSeekMessage(role = "system", content = systemPrompt),
                DeepSeekMessage(role = "user", content = query)
            )
            
            // Create request with tools
            val request = deepSeekApi.createAgenticRequest(
                messages = messages,
                includeTools = true,
                temperature = 0.7,
                responseFormat = com.Lyno.matchmindai.data.dto.ResponseFormat(type = "json_object"),
                model = "deepseek-chat"
            )
            
            // Get initial response
            val response = deepSeekApi.getPrediction(deepSeekApiKey, request)
            
            // Check for tool calls
            val toolCalls = response.choices.firstOrNull()?.message?.toolCalls
            
            if (toolCalls.isNullOrEmpty()) {
                // No tool calls, parse the response directly
                val content = response.choices.firstOrNull()?.message?.content ?: "Geen antwoord ontvangen"
                return ResponseParser.parse(content)
            }
            
            // CRITICAL FIX: Save the original assistant message with tool_calls
            val assistantMessage = response.choices.firstOrNull()?.message
            if (assistantMessage != null) {
                // Create a DeepSeekMessage from the assistant response
                val assistantDeepSeekMessage = DeepSeekMessage(
                    role = "assistant",
                    content = assistantMessage.content,
                    toolCalls = assistantMessage.toolCalls
                )
                messages.add(assistantDeepSeekMessage)
            }
            
            // Execute tool calls and collect results
            val toolResults = mutableListOf<DeepSeekMessage>()
            for (toolCall in toolCalls) {
                val toolResult = executeToolCall(toolCall, sessionId)
                toolResults.add(toolResult)
            }
            
            // Add tool results to messages
            messages.addAll(toolResults)
            
            // Get final response with tool results
            val finalRequest = deepSeekApi.createAgenticRequest(
                messages = messages,
                includeTools = false,
                temperature = 0.7,
                responseFormat = com.Lyno.matchmindai.data.dto.ResponseFormat(type = "json_object"),
                model = "deepseek-chat"
            )
            
            val finalResponse = deepSeekApi.getPrediction(deepSeekApiKey, finalRequest)
            val finalContent = finalResponse.choices.firstOrNull()?.message?.content ?: "Geen antwoord ontvangen"

            // Parse the JSON response into AgentResponse
            ResponseParser.parse(finalContent)
        } catch (e: Exception) {
            AgentResponse.error("Fout bij verwerken query: ${e.message}")
        }
    }
    
    /**
     * Executes a Prophet Module query for advanced match analysis.
     * This uses the Phase 3 "Prophet Module" with "Anchor & Adjust" strategy.
     */
    suspend fun executeProphetQuery(query: String, sessionId: String? = null): AgentResponse {
        return try {
            // For now, use the same implementation as executeWithTools
            executeWithTools(query, sessionId)
        } catch (e: Exception) {
            AgentResponse.error("Fout bij Prophet analyse: ${e.message}")
        }
    }
}
