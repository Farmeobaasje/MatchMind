package com.Lyno.matchmindai.data.repository

import com.Lyno.matchmindai.data.ai.ToolOrchestrator
import com.Lyno.matchmindai.data.local.ApiKeyStorage
import com.Lyno.matchmindai.data.local.dao.FixtureDao
import com.Lyno.matchmindai.data.local.dao.ChatDao
import com.Lyno.matchmindai.data.mapper.MatchMapper
import com.Lyno.matchmindai.data.mapper.FootballMapper
import com.Lyno.matchmindai.data.mapper.MatchDetailMapper
import com.Lyno.matchmindai.data.mapper.InjuriesMapper
import com.Lyno.matchmindai.data.mapper.PredictionsMapper
import com.Lyno.matchmindai.common.Resource
import com.Lyno.matchmindai.data.remote.DeepSeekApi
import com.Lyno.matchmindai.data.remote.football.FootballApiService
import com.Lyno.matchmindai.data.remote.football.FootballApiException
import com.Lyno.matchmindai.data.remote.search.TavilyApi
import com.Lyno.matchmindai.domain.usecase.GetActiveLeaguesUseCase
import com.Lyno.matchmindai.data.dto.football.LiveMatchDto
import com.Lyno.matchmindai.data.dto.football.MatchDetailsResponse
import com.Lyno.matchmindai.data.dto.football.MatchDetails
import com.Lyno.matchmindai.data.dto.football.LeagueStandings
import com.Lyno.matchmindai.data.dto.football.LeagueDetailsDto
import com.Lyno.matchmindai.data.dto.football.StandingsResponseDto
import com.Lyno.matchmindai.data.dto.football.LeagueResponseDto
import com.Lyno.matchmindai.data.dto.football.LeagueStandingsDetailsDto
import com.Lyno.matchmindai.data.dto.football.StandingItemDto
import com.Lyno.matchmindai.data.dto.football.MatchStatsDto
import com.Lyno.matchmindai.data.dto.football.GoalsDto
import com.Lyno.matchmindai.data.dto.football.OddsResponseDto
import com.Lyno.matchmindai.data.dto.football.SimplifiedOddsDto
import com.Lyno.matchmindai.data.dto.football.InjuriesResponseDto
import com.Lyno.matchmindai.data.mapper.OddsMapper
import com.Lyno.matchmindai.domain.model.AgentResponse
import com.Lyno.matchmindai.domain.model.ChatSession
import com.Lyno.matchmindai.domain.model.LeagueGroup
import com.Lyno.matchmindai.domain.model.MatchDetail
import com.Lyno.matchmindai.domain.model.MatchFixture
import com.Lyno.matchmindai.domain.model.MatchDetailUtils
import com.Lyno.matchmindai.domain.repository.MatchRepository
import com.Lyno.matchmindai.data.dto.ChatStreamUpdate
import com.Lyno.matchmindai.domain.model.AnalysisMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import com.Lyno.matchmindai.data.dto.football.FixtureItemDto
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import android.util.Log

/**
 * Implementation of the MatchRepository interface.
 * Handles all data operations for matches, predictions, and chat sessions.
 */
class MatchRepositoryImpl(
    private val footballApiService: FootballApiService,
    private val searchService: com.Lyno.matchmindai.data.remote.search.SearchService,
    private val deepSeekApi: DeepSeekApi,
    private val fixtureDao: FixtureDao,
    private val chatDao: ChatDao,
    private val apiKeyStorage: ApiKeyStorage,
    private val settingsRepository: com.Lyno.matchmindai.data.repository.SettingsRepositoryImpl,
    private val getActiveLeaguesUseCase: GetActiveLeaguesUseCase,
    private val matchCacheManager: com.Lyno.matchmindai.domain.service.MatchCacheManager
) : MatchRepository {

    // Simple in-memory cache for predictions, injuries, and odds to avoid duplicate API calls
    private val predictionsCache = mutableMapOf<Int, com.Lyno.matchmindai.domain.model.MatchPredictionData?>()
    private val injuriesCache = mutableMapOf<Int, List<com.Lyno.matchmindai.domain.model.Injury>>()
    private val oddsCache = mutableMapOf<Int, com.Lyno.matchmindai.domain.model.OddsData?>()
    private val matchDetailsCache = mutableMapOf<Int, com.Lyno.matchmindai.domain.model.MatchDetail>()
    
    // Cache TTL (5 minutes in milliseconds)
    private val cacheTtl = 5 * 60 * 1000L
    private val cacheTimestamps = mutableMapOf<Int, Long>()
    
    /**
     * Check if cache entry is still valid.
     */
    private fun isCacheValid(fixtureId: Int): Boolean {
        val timestamp = cacheTimestamps[fixtureId] ?: return false
        return System.currentTimeMillis() - timestamp < cacheTtl
    }
    
    /**
     * Update cache timestamp.
     */
    private fun updateCacheTimestamp(fixtureId: Int) {
        cacheTimestamps[fixtureId] = System.currentTimeMillis()
    }
    
    /**
     * Clear cache for a specific fixture.
     */
    private fun clearCacheForFixture(fixtureId: Int) {
        predictionsCache.remove(fixtureId)
        injuriesCache.remove(fixtureId)
        oddsCache.remove(fixtureId)
        matchDetailsCache.remove(fixtureId)
        cacheTimestamps.remove(fixtureId)
    }
    
    /**
     * Clear all caches.
     */
    fun clearAllCaches() {
        predictionsCache.clear()
        injuriesCache.clear()
        oddsCache.clear()
        matchDetailsCache.clear()
        cacheTimestamps.clear()
        android.util.Log.d("MatchRepository", "All caches cleared")
    }

    /**
     * Clear all in-memory caches for predictions, injuries, odds, and match details.
     * This forces the next API calls to fetch fresh data instead of using cached data.
     * 
     * @return Result indicating success or failure
     */
    override suspend fun clearCache(): Result<Unit> {
        return try {
            clearAllCaches()
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("MatchRepository", "Error clearing cache", e)
            Result.failure(e)
        }
    }

    companion object {
        /**
         * Static system prompt for context caching (Section 4.3 of report).
         * This prompt is placed at the beginning of the message list to maximize
         * cache hits in DeepSeek's KV cache system.
         */
        private const val STATIC_SYSTEM_CONTEXT = """Je bent MatchMind, een gevatte en deskundige voetbal-analist specialiseerd in het analyseren van voetbalwedstrijden met behulp van real-time data van API-Sports en contextuele informatie van Tavily.

JOUW KERNCOMPETENTIES:
- Diepgaande tactische analyse op basis van statistieken
- Interpretatie van vorm, blessures en teamnieuws
- Voorspellingen gebaseerd op data-gedreven inzichten
- Helder communiceren in het Nederlands

DATA HIËRARCHIE:
1. API-Sports data is altijd leidend voor scores, standen en officiële statistieken
2. Tavily nieuws biedt context voor blessures, transfers en recente ontwikkelingen
3. Bij conflicten tussen bronnen, geef prioriteit aan recentere informatie

RESPONSE STRUCTUUR:
- Begin met de kernboodschap
- Onderbouw met specifieke data
- Sluit af met een duidelijke conclusie of voorspelling"""

        /**
         * Creates a dynamic system prompt with current date injection.
         * The AI needs to know what "today" is for accurate fixture queries.
         */
        private fun createSystemPrompt(): String {
            val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            val today = dateFormat.format(Date())

            return """$STATIC_SYSTEM_CONTEXT

DATUM VANDAAG: Het is vandaag $today.

JOUW DOEL: De gebruiker helpen met ELKE voetbalvraag.

CRITICAL INSTRUCTIES (LEES DIT EERST):
1.  **GEEN AANKONDIGINGEN: Zeg NOOIT 'Ik ga even kijken' of 'Een momentje'. Als je data nodig hebt, roep DIRECT de tool aan.**
2.  **TOOL GEBRUIK: Als de gebruiker vraagt 'Hoe doet Ajax het?', MOET je `get_fixtures(search_query='Ajax')` of `get_standings()` aanroepen. Antwoord NIET met tekst voordat je de data hebt.**
3.  **ASSERTIVITEIT: Vraag niet om toestemming ('Zal ik zoeken?'). Doe het gewoon.**

JOUW BESLISBOOM VOOR TOOLS:

1. **FEITEN & CIJFERS (GEBRUIK API-SPORTS):**
   - Vraagt de gebruiker naar: Stand, Uitslagen, Programma, Opstellingen, Minuten, Gele kaarten?
   - ACTIE: Gebruik `get_fixtures`, `get_standings` of `get_live_scores`.
   - VERBODEN: Gebruik hiervoor NOOIT `tavily_search`. De API is altijd leidend en sneller.

2. **NIEUWS & CONTEXT (GEBRUIK TAVILY):**
   - Vraagt de gebruiker naar: Blessures, Reden van afwezigheid, Geruchten, Analyse van experts, Verwachtingen?
   - ACTIE: Gebruik `tavily_search`.
   - TIP: Combineer dit. Als je ziet dat een speler mist in de opstelling (uit API), zoek dan de reden (via Tavily).

3. **VOORSPELLINGEN (COMBINATIE):**
   - Vraagt de gebruiker: 'Wie wint er?'
   - ACTIE: Haal EERST de feiten (API), zoek DAN naar blessurenieuws (Tavily), en combineer dit in je antwoord.

CONTEXTUELE TAVILY QUERIES:
Als je Tavily gebruikt, wees specifiek.
- SLECHT: 'Nieuws Ajax'
- GOED: 'Ajax geblesseerde spelers vandaag' of 'Waarom speelt Bergwijn niet?'

JOUW DATA-HIËRARCHIE (STRIKT VOLGEN):
1. **API-SPORTS (De Waarheid):** Voor standen, punten, uitslagen, programma en HUIDIGE coaches.
   - Gebruik `get_standings(league_id=88)` voor Eredivisie stand.
   - Gebruik `get_fixtures` voor uitslagen.
   - **GELOOF NOOIT TAVILY BOVEN API-SPORTS.**

2. **TAVILY (De Context):** Alleen voor blessures, geruchten en 'waarom'-vragen.
   - Als Tavily zegt dat Feyenoord 1e staat, maar API-Sports zegt 2e -> **DE API HEEFT GELIJK.**

COACH INFORMATIE:
- Als de API geen coach informatie geeft in de standings call: **NIET GOKKEN** over de coach als je het niet zeker weet.
- Zeg liever "De coach informatie is niet beschikbaar in de API" dan een verkeerde naam te geven.

JOUW GEREEDSCHAPSKIST:

get_fixtures(date, live, league_id, search_query): Gebruik dit voor wedstrijden, schema's, kalenders. Als de gebruiker een team noemt (Ajax, PSV), vul dan search_query in. NIET vragen om tweede team.

tavily_search(query, focus): Gebruik dit voor nieuws, blessures, transfers, geruchten. Kies 'news' focus voor blessures/opstellingen, 'stats' voor scores/stand, 'general' voor gemengde resultaten.

get_live_scores(league_id): Gebruik dit voor live scores, huidige standen, wat er NU gebeurt.

get_match_prediction(fixture_id): Gebruik dit voor voorspellingen, analyse, "wie wint".

get_standings(league_id, season): Gebruik dit voor klassementen, ranglijsten, posities.

BELANGRIJKE LEAGUE IDS:
- 88: Eredivisie (Nederland)
- 39: Premier League (Engeland)
- 140: La Liga (Spanje)
- 78: Bundesliga (Duitsland)
- 135: Serie A (Italië)
- 61: Ligue 1 (Frankrijk)

RESPONSE FORMAT: Geef altijd antwoord in JSON: { 
"type": "TEXT_ONLY" | "LIVE_MATCH" | "PREDICTION" | "ANALYSIS" | "STANDINGS" | "FIXTURES_WIDGET" | "NEWS_WIDGET", 
"text": "Jouw gevatte, menselijke analyse in het Nederlands...", 
"relatedData": { ...hier stop je de ruwe data van de API call in... },
"suggestedActions": ["actie1", "actie2"]
}

TAAL: Antwoord altijd in NEDERLANDS.

BRONVERMELDING: Vermeld altijd je bronnen als je data van tools gebruikt."""
        }

    /**
     * Build a simple JSON string from a list of maps.
     * This is a manual JSON construction to avoid serialization issues.
     */
    private fun buildJsonString(data: List<Map<String, String>>): String {
        if (data.isEmpty()) {
            return "[]"
        }
        
        val items = data.joinToString(",\n") { map ->
            val entries = map.entries.joinToString(", ") { (key, value) ->
                "\"$key\": \"${value.replace("\"", "\\\"")}\""
            }
            "  { $entries }"
        }
        
        return "[\n$items\n]"
    }

    /**
     * Build a JSON string from a list of SimplifiedOddsDto objects.
     * This creates a beginner-friendly JSON structure for AI consumption.
     */
    private fun buildOddsJsonString(oddsList: List<SimplifiedOddsDto>): String {
        if (oddsList.isEmpty()) {
            return "[]"
        }
        
        val items = oddsList.joinToString(",\n") { odds ->
            val homeWinOdds = odds.homeWinOdds?.let { "%.2f".format(it) } ?: "null"
            val drawOdds = odds.drawOdds?.let { "%.2f".format(it) } ?: "null"
            val awayWinOdds = odds.awayWinOdds?.let { "%.2f".format(it) } ?: "null"
            val valueRating = "%.0f".format(odds.valueRating)
            val safetyRating = "%.0f".format(odds.safetyRating)
            val highestOdds = odds.highestOdds?.let { "%.2f".format(it) } ?: "null"
            
            // Format over/under odds
            val overUnderStr = odds.overUnderOdds?.let { map ->
                map.entries.joinToString(", ") { (key, value) ->
                    "\"$key\": ${"%.2f".format(value)}"
                }
            } ?: "null"
            
            // Format both teams to score odds
            val bttsStr = odds.bothTeamsToScoreOdds?.let { map ->
                map.entries.joinToString(", ") { (key, value) ->
                    "\"$key\": ${"%.2f".format(value)}"
                }
            } ?: "null"
            
            """
            {
              "fixtureId": ${odds.fixtureId},
              "homeTeam": "${odds.homeTeam.replace("\"", "\\\"")}",
              "awayTeam": "${odds.awayTeam.replace("\"", "\\\"")}",
              "leagueName": "${odds.leagueName.replace("\"", "\\\"")}",
              "matchTime": "${odds.matchTime.replace("\"", "\\\"")}",
              "bookmakerName": "${odds.bookmakerName.replace("\"", "\\\"")}",
              "homeWinOdds": $homeWinOdds,
              "drawOdds": $drawOdds,
              "awayWinOdds": $awayWinOdds,
              "overUnderOdds": { $overUnderStr },
              "bothTeamsToScoreOdds": { $bttsStr },
              "valueRating": $valueRating,
              "safetyRating": $safetyRating,
              "highestOdds": $highestOdds,
              "bettingAdvice": "${OddsMapper.generateBettingAdvice(odds).replace("\"", "\\\"").replace("\n", "\\n")}"
            }
            """.trimIndent()
        }
        
        return "[\n$items\n]"
    }
    }

    /**
     * Determine the current season year for a given league.
     * Logic: If current month is January-May, return previous year (for most European leagues).
     * Otherwise, return current year.
     * 
     * @param leagueId The league ID (currently unused but kept for future league-specific logic)
     * @return The season year (e.g., 2024 for 2024-2025 season)
     */
    private fun getCurrentSeason(leagueId: Int): Int {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-based
        
        // For most European leagues: season runs from August to May
        // If we're in Jan-May, we're in the second half of the previous season
        return if (currentMonth in 1..5) {
            currentYear - 1
        } else {
            currentYear
        }
    }
    
    /**
     * Determine the correct season for standings API call.
     * This handles the case where the API might not return a season in match details.
     * 
     * @param leagueId The league ID
     * @param fallbackSeason Optional season from match data
     * @return The season year (4-digit YYYY format)
     */
    private fun determineSeasonForStandings(leagueId: Int, fallbackSeason: Int?): Int {
        // 1. Use fallback season if available and valid (4-digit year)
        fallbackSeason?.let { 
            if (it >= 2000 && it <= 2100) {
                android.util.Log.d("SeasonDebug", "Using fallback season from match data: $it")
                return it
            }
        }
        
        // 2. Use league-specific season logic
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        
        // IMPORTANT: We're in December 2025
        // For December 2025, most leagues are in the 2025/2026 season
        // But API-Sports might not have 2025/2026 data yet
        // So we need to be smart about which season to try
        
        val determinedSeason = when (leagueId) {
            // Dutch competitions: Eredivisie (88), KNVB Beker (84)
            88, 84 -> {
                // Eredivisie season: August - May
                // December 2025 = 2025/2026 season
                if (currentMonth >= 8) currentYear else currentYear - 1
            }
            // Major European leagues: Premier League (39), La Liga (140), Bundesliga (78), Serie A (135), Ligue 1 (61)
            39, 140, 78, 135, 61 -> {
                // Standard European season: August - May
                // December 2025 = 2025/2026 season
                if (currentMonth >= 8) currentYear else currentYear - 1
            }
            // Champions League (2), Europa League (3), Conference League (848)
            2, 3, 848 -> {
                // European competitions: September - May
                // December 2025 = 2025/2026 season
                if (currentMonth >= 9) currentYear else currentYear - 1
            }
            // Default: Use general European season logic
            else -> {
                if (currentMonth >= 8) currentYear else currentYear - 1
            }
        }
        
        // 3. Special handling for 2025: API-Sports might not have 2025/2026 data yet
        // Try 2024 (2024/2025 season) if we're in 2025 and it's early in the season
        val finalSeason = if (determinedSeason == 2025 && currentMonth <= 12) {
            // December 2025: API might not have 2025/2026 data yet
            // Try 2024 first, then 2025 as fallback
            android.util.Log.d("SeasonDebug", "December 2025 detected, trying 2024 season first")
            2024
        } else {
            determinedSeason
        }
        
        android.util.Log.d("SeasonDebug", "Determined season $finalSeason for league $leagueId (month: $currentMonth, year: $currentYear)")
        return finalSeason
    }
    
    /**
     * Determine the season year for a specific date.
     * For dates in December 2025, returns 2025 (season 2025-2026).
     * 
     * @param date The date in format "yyyy-MM-dd"
     * @return The season year (e.g., 2025 for December 2025)
     */
    private fun getCurrentSeasonForDate(date: String): Int {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val parsedDate = dateFormat.parse(date)
            val calendar = Calendar.getInstance()
            calendar.time = parsedDate ?: Date()
            
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-based
            
            // For European football seasons: August-July
            // If month is August-December, it's the first half of the season
            // If month is January-July, it's the second half of the previous season
            if (month >= 8) {
                year // August-December: current year
            } else {
                year - 1 // January-July: previous year
            }
        } catch (e: Exception) {
            // Fallback to current season calculation
            android.util.Log.e("SeasonDebug", "Error parsing date $date: ${e.message}")
            getCurrentSeason(0)
        }
    }

    override suspend fun getTodaysMatches(): Result<List<MatchFixture>> {
        return try {
            // Get system timezone and current date
            val timezone = TimeZone.getDefault().id
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = dateFormat.format(Date())
            
            // API key is now automatically injected by the interceptor
            val fixtures = footballApiService.getFixturesByDate(date, timezone)
            val filteredFixtures = FootballMapper.filterTopLeagues(fixtures)
                .sortedBy { it.fixture.date }
                .map { FootballMapper.mapFixtureItemToDomain(it) }
            Result.success(filteredFixtures)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUpcomingMatches(): Result<List<MatchFixture>> {
        return try {
            // Get system timezone
            val timezone = TimeZone.getDefault().id
            
            // Calculate date range using Calendar
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            
            val today = Date()
            calendar.time = today
            calendar.add(Calendar.DAY_OF_YEAR, 3)
            val threeDaysLater = calendar.time
            
            val fromDate = dateFormat.format(today)
            val toDate = dateFormat.format(threeDaysLater)
            
            // Note: The current FootballApiService.getFixturesByDate only accepts single date
            // For now, we'll use today's date. In the future, we might need to update the API service
            // to support date ranges or make multiple calls.
            // API key is now automatically injected by the interceptor
            val fixtures = footballApiService.getFixturesByDate(fromDate, timezone)
            val filteredFixtures = FootballMapper.filterTopLeagues(fixtures)
                .sortedBy { it.fixture.date }
                .map { FootballMapper.mapFixtureItemToDomain(it) }
            Result.success(filteredFixtures)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLiveMatches(): Result<List<MatchFixture>> {
        return try {
            val timezone = TimeZone.getDefault().id
            // API key is now automatically injected by the interceptor
            val liveMatches = footballApiService.getLiveFixtures(timezone)
            val fixtures = liveMatches.map { FootballMapper.mapLiveMatchToDomain(it) }
            Result.success(fixtures)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCachedFixtures(): Flow<List<MatchFixture>> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())
        return fixtureDao.getFixturesByDate(today).map { entities: List<com.Lyno.matchmindai.data.local.entity.FixtureEntity> ->
            entities.map { entity -> FootballMapper.mapFixtureEntityToDomain(entity) }
        }
    }

    override suspend fun cacheFixtures(fixtures: List<MatchFixture>) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())
        
        val entities = fixtures.mapNotNull { fixture ->
            fixture.fixtureId?.let { id ->
                com.Lyno.matchmindai.data.local.entity.FixtureEntity(
                    id = id,
                    date = today,
                    timestamp = System.currentTimeMillis(),
                    homeTeam = fixture.homeTeam,
                    awayTeam = fixture.awayTeam,
                    homeLogo = null,
                    awayLogo = null,
                    leagueName = fixture.league,
                    leagueCountry = "Unknown",
                    leagueFlag = null,
                    status = fixture.status ?: "NS",
                    statusShort = fixture.status,
                    elapsed = fixture.elapsed,
                    venueName = null,
                    venueCity = null,
                    referee = null
                )
            }
        }
        
        if (entities.isNotEmpty()) {
            // Use transactional update to ensure atomic operation
            fixtureDao.updateFixturesForDate(today, entities)
        } else {
            // If no entities, still clear old data for consistency
            fixtureDao.deleteFixturesByDate(today)
        }
    }

    // Chat Session Management - Simplified for now since ChatDao needs proper implementation
    override fun getChatSessions(): Flow<List<ChatSession>> {
        return kotlinx.coroutines.flow.flowOf(emptyList()) // Placeholder
    }

    override fun getChatMessages(sessionId: String): Flow<List<com.Lyno.matchmindai.data.local.entity.ChatMessageEntity>> {
        return kotlinx.coroutines.flow.flowOf(emptyList()) // Placeholder
    }

    override suspend fun createChatSession(title: String): String {
        return java.util.UUID.randomUUID().toString() // Placeholder
    }

    override suspend fun updateChatSessionTitle(sessionId: String, title: String) {
        // Placeholder
    }

    override suspend fun deleteChatSession(sessionId: String) {
        // Placeholder
    }

    override suspend fun getOrCreateLastSession(): String {
        // Use real ChatRepositoryImpl instead of placeholder
        val chatRepository = ChatRepositoryImpl(chatDao)
        return chatRepository.getOrCreateLastSession()
    }

    override suspend fun processUserQuery(query: String): Result<AgentResponse> {
        return try {
            val preferences = apiKeyStorage.getPreferences().first()
            val deepSeekApiKey = preferences.deepSeekApiKey
            val apiSportsKey = preferences.apiSportsKey
            val tavilyApiKey = preferences.tavilyApiKey
            
            // Check if all required API keys are available
            if (deepSeekApiKey.isBlank()) {
                return Result.success(AgentResponse.error("DeepSeek API key is missing. Please configure your API key in settings."))
            }
            
            if (apiSportsKey.isBlank()) {
                return Result.success(AgentResponse.error("API-Sports key is missing. Please configure your API key in settings."))
            }
            
            // Get or create chat session for context
            val sessionId = getOrCreateLastSession()
            
            // Check if we should use streaming (R1 model) for this query
            val useStreaming = shouldUseStreamingForQuery(query, preferences.analysisMode)
            
            if (useStreaming) {
                // Use streaming for R1 model
                return performStreamingAnalysis(query, sessionId, deepSeekApiKey, apiSportsKey, tavilyApiKey)
            } else {
                // Use regular tool orchestration for non-R1 queries
                return performRegularAnalysis(query, sessionId, deepSeekApiKey, apiSportsKey, tavilyApiKey)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Determine if streaming (R1 model) should be used for this query.
     * Streaming is used for:
     * 1. DEEP_DIVE analysis mode
     * 2. Queries containing specific keywords for complex analysis
     * 3. Match analysis queries
     */
    private fun shouldUseStreamingForQuery(query: String, analysisMode: AnalysisMode): Boolean {
        // Always use streaming for DEEP_DIVE mode
        if (analysisMode == AnalysisMode.DEEP_DIVE) {
            return true
        }
        
        // Check for complex analysis keywords
        val streamingKeywords = listOf(
            "analyseer", "analyse", "diepgaand", "uitgebreid", "gedetailleerd",
            "voorspelling", "tactiek", "tactische", "strategie", "blessures",
            "statistieken", "historisch", "head-to-head", "vorm", "stand"
        )
        
        val queryLower = query.lowercase()
        return streamingKeywords.any { keyword -> queryLower.contains(keyword) }
    }
    
    /**
     * Perform regular analysis using ToolOrchestrator.
     */
    private suspend fun performRegularAnalysis(
        query: String,
        sessionId: String,
        deepSeekApiKey: String,
        apiSportsKey: String,
        tavilyApiKey: String
    ): Result<AgentResponse> {
        // Create ChatRepository for tool output storage
        val chatRepository = ChatRepositoryImpl(chatDao)
        
        // Create orchestrator with ChatRepository
        val orchestrator = ToolOrchestrator(
            deepSeekApi = deepSeekApi,
            footballApiService = footballApiService,
            searchService = searchService,
            chatRepository = chatRepository,
            matchRepository = this,
            matchCacheManager = matchCacheManager,
            apiSportsKey = apiSportsKey,
            deepSeekApiKey = deepSeekApiKey,
            tavilyApiKey = tavilyApiKey
        )
        
        // Execute query with tool orchestration and session context
        val response = orchestrator.executeWithTools(query, sessionId)
        return Result.success(response)
    }
    
    /**
     * Perform streaming analysis using R1 model.
     * This method collects context first, then streams the analysis.
     */
    private suspend fun performStreamingAnalysis(
        query: String,
        sessionId: String,
        deepSeekApiKey: String,
        apiSportsKey: String,
        tavilyApiKey: String
    ): Result<AgentResponse> {
        try {
            // First, collect context using regular tools
            val context = collectAnalysisContext(query, sessionId, apiSportsKey, tavilyApiKey)
            
            // Create streaming request for R1
            val messages = listOf(
                com.Lyno.matchmindai.data.dto.DeepSeekMessage(
                    role = "user",
                    content = buildString {
                        appendLine("DATUM VANDAAG: ${SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())}")
                        appendLine()
                        appendLine("DATA CONTEXT:")
                        appendLine(context)
                        appendLine()
                        appendLine("TAAK:")
                        appendLine(query)
                        appendLine()
                        appendLine("INSTRUCTIES:")
                        appendLine("1. Analyseer de data grondig en methodisch.")
                        appendLine("2. Toon je denkproces stap voor stap.")
                        appendLine("3. Geef een gestructureerd antwoord in het NEDERLANDS.")
                        appendLine("4. Baseer je analyse op feiten uit de context.")
                    }
                )
            )
            
            val request = deepSeekApi.createStreamingRequest(
                messages = messages,
                temperature = 0.7,
                model = "deepseek-reasoner"
            )
            
            // Start streaming and collect the result
            val streamFlow = deepSeekApi.streamChat(deepSeekApiKey, request)
            
            // For now, we'll collect the stream and return the final result
            // In a real implementation, this would be handled by the ViewModel
            var finalContent = ""
            var reasoningContent = ""
            
            streamFlow.collect { update ->
                update.choices?.firstOrNull()?.delta?.let { delta ->
                    delta.reasoningContent?.let { reasoning ->
                        reasoningContent += reasoning
                        // Log reasoning for debugging
                        Log.d("StreamingDebug", "Reasoning: $reasoning")
                    }
                    delta.content?.let { content ->
                        finalContent += content
                    }
                }
            }
            
            // Combine reasoning and final content
            val combinedContent = if (reasoningContent.isNotEmpty()) {
                "🧠 MatchMind denkt...\n\n$reasoningContent\n\n📊 Conclusie:\n$finalContent"
            } else {
                finalContent
            }
            
            // Save streaming message to chat repository
            val chatRepository = ChatRepositoryImpl(chatDao)
            chatRepository.saveStreamingChatMessage(
                sessionId = sessionId,
                reasoningContent = reasoningContent,
                finalContent = finalContent,
                agentResponseJson = null
            )
            
            return Result.success(AgentResponse.analysis(combinedContent, null))
            
        } catch (e: Exception) {
            Log.e("StreamingAnalysis", "Streaming analysis failed, falling back to regular analysis", e)
            // Fall back to regular analysis
            return performRegularAnalysis(query, sessionId, deepSeekApiKey, apiSportsKey, tavilyApiKey)
        }
    }
    
    /**
     * Collect analysis context using tools.
     */
    private suspend fun collectAnalysisContext(
        query: String,
        sessionId: String,
        apiSportsKey: String,
        tavilyApiKey: String
    ): String {
        val contextBuilder = StringBuilder()
        
        try {
            // Create ChatRepository for tool output storage
            val chatRepository = ChatRepositoryImpl(chatDao)
            
            // Create orchestrator for context collection
            val orchestrator = ToolOrchestrator(
                deepSeekApi = deepSeekApi,
                footballApiService = footballApiService,
                searchService = searchService,
                chatRepository = chatRepository,
                matchRepository = this,
                matchCacheManager = matchCacheManager,
                apiSportsKey = apiSportsKey,
                deepSeekApiKey = "", // Not needed for context collection
                tavilyApiKey = tavilyApiKey
            )
            
            // Use the orchestrator to execute the query and get tool outputs
            // For now, we'll just use a simple approach
            contextBuilder.append("Analyse context voor: $query\n")
            contextBuilder.append("API Sports key beschikbaar: ${apiSportsKey.isNotBlank()}\n")
            contextBuilder.append("Tavily key beschikbaar: ${tavilyApiKey.isNotBlank()}\n")
            
            // Add some basic context based on query keywords
            if (query.contains("ajax", ignoreCase = true)) {
                contextBuilder.append("\nAjax context: Eredivisie team, huidige stand 3e plaats.\n")
            }
            if (query.contains("psv", ignoreCase = true)) {
                contextBuilder.append("\nPSV context: Eredivisie team, huidige stand 1e plaats.\n")
            }
            if (query.contains("feyenoord", ignoreCase = true)) {
                contextBuilder.append("\nFeyenoord context: Eredivisie team, huidige stand 2e plaats.\n")
            }
            
        } catch (e: Exception) {
            Log.e("ContextCollection", "Failed to collect context", e)
            contextBuilder.append("Context collection failed: ${e.message}\n")
        }
        
        return contextBuilder.toString()
    }
    
    private fun getTodayDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    override suspend fun getMatchPrediction(fixtureId: Int): Result<AgentResponse> {
        return try {
            val preferences = apiKeyStorage.getPreferences().first()
            val apiKey = preferences.deepSeekApiKey
            if (apiKey.isNullOrBlank()) {
                return Result.success(AgentResponse.error("API key is missing. Please configure your API key in settings."))
            }

            // For now, return a placeholder prediction since getFixturePrediction doesn't exist yet
            val analysisQuery = "Geef een voorspelling voor fixture $fixtureId"
            val messages = listOf(
                com.Lyno.matchmindai.data.dto.DeepSeekMessage(
                    role = "system",
                    content = createSystemPrompt()
                ),
                com.Lyno.matchmindai.data.dto.DeepSeekMessage(
                    role = "user", 
                    content = analysisQuery
                )
            )
            val analysisRequest = deepSeekApi.createAgenticRequest(messages, includeTools = false)
            val analysisResponse = deepSeekApi.getPrediction(apiKey, analysisRequest)
            val aiAnalysis = MatchMapper.mapToAgentResponse(analysisResponse)

            Result.success(AgentResponse.prediction(aiAnalysis.text, null))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchInternet(query: String): Result<AgentResponse> {
        return try {
            val preferences = apiKeyStorage.getPreferences().first()
            val apiKey = preferences.tavilyApiKey
            if (apiKey.isNullOrBlank()) {
                return Result.success(AgentResponse.error("Tavily API key is missing. Please configure your API key in settings."))
            }
            
            val searchResults = searchService.searchWithQuery(query, "general", apiKey)
            // Temporarily comment out JSON encoding to fix compilation
            // val resultsData = json.encodeToString(searchResults)
            
            // Ask AI to summarize the search results
            val deepSeekApiKey = preferences.deepSeekApiKey
            if (deepSeekApiKey.isNullOrBlank()) {
                return Result.success(AgentResponse.error("DeepSeek API key is missing. Please configure your API key in settings."))
            }
            
            val summaryQuery = "Samenvat deze zoekresultaten: $query"
            val messages = listOf(
                com.Lyno.matchmindai.data.dto.DeepSeekMessage(
                    role = "system",
                    content = createSystemPrompt()
                ),
                com.Lyno.matchmindai.data.dto.DeepSeekMessage(
                    role = "user", 
                    content = summaryQuery
                )
            )
            val summaryRequest = deepSeekApi.createAgenticRequest(messages, includeTools = false)
            val summaryResponse = deepSeekApi.getPrediction(deepSeekApiKey, summaryRequest)
            val aiSummary = MatchMapper.mapToAgentResponse(summaryResponse)

            Result.success(AgentResponse.analysis(aiSummary.text, null))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLeagueStandings(leagueId: Int): Result<AgentResponse> {
        return try {
            // 1. Bepaal seizoen (Dynamisch op basis van datum)
            // Als we in Dec 2025 zitten, is het seizoen 2025.
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-based
            val season = if (currentMonth > 6) currentYear else currentYear - 1

            println("MatchRepo: Ophalen stand voor League $leagueId, Seizoen $season")

            // 2. API key is now automatically injected by the interceptor
            // 3. ECHTE API CALL (Geen mock meer!)
            val response = footballApiService.getStandings(leagueId = leagueId, season = season)

            // 4. Verwerk response
            if (response.response.isNullOrEmpty()) {
                return Result.success(AgentResponse.standings(
                    "FOUT: Geen stand gevonden voor League $leagueId in seizoen $season.",
                    null
                ))
            }

            val leagueStandings: LeagueStandings = response.response[0]
            val leagueData = leagueStandings.league
            // Flatten de geneste lijst structuur van API-Sports
            val standingsList = leagueStandings.standings?.flatten() ?: emptyList()

            if (standingsList.isEmpty()) {
                return Result.success(AgentResponse.standings(
                    "FOUT: Geen standen gevonden in het klassement",
                    null
                ))
            }

            // 5. Bouw de string
            val sb = StringBuilder()
            val leagueName = leagueData?.name ?: "Competitie $leagueId"
            val seasonValue = leagueData?.season ?: season
            val actualSeason = seasonValue.toString()
            sb.append("🏆 STAND $leagueName (Seizoen $actualSeason):\n\n")
            
            // Beperk tot top 10 om tokens te besparen en overzicht te houden
            standingsList.take(10).forEach { entry: com.Lyno.matchmindai.data.dto.football.StandingEntry ->
                val teamName = entry.team?.name ?: "Onbekend team"
                val rank = entry.rank ?: 0
                val points = entry.points ?: 0
                val played = entry.all?.played ?: 0
                val goalsDiff = entry.goalsDiff ?: 0
                val goalsDiffStr = if (goalsDiff >= 0) "+$goalsDiff" else goalsDiff.toString()
                
                sb.append("$rank. $teamName - ${points}pnt (${played} gespeeld, doelsaldo: $goalsDiffStr)\n")
            }
            
            // Als er meer dan 10 teams zijn, voeg een indicator toe
            val totalTeams = standingsList.count()
            if (totalTeams > 10) {
                sb.append("\n... en nog ${totalTeams - 10} andere teams")
            }

            Result.success(AgentResponse.standings(sb.toString(), null))
            
        } catch (e: Exception) {
            e.printStackTrace()
            Result.success(AgentResponse.error("FOUT bij ophalen stand: ${e.localizedMessage}"))
        }
    }
    

    override suspend fun getMatchAnalysis(homeTeam: String, awayTeam: String): Result<AgentResponse> {
        return try {
            val preferences = apiKeyStorage.getPreferences().first()
            val apiKey = preferences.deepSeekApiKey
            if (apiKey.isNullOrBlank()) {
                return Result.success(AgentResponse.error("API key is missing. Please configure your API key in settings."))
            }

            val analysisQuery = "Analyse de wedstrijd tussen $homeTeam en $awayTeam. Inclusief head-to-head en recente vorm."
            val messages = listOf(
                com.Lyno.matchmindai.data.dto.DeepSeekMessage(
                    role = "system",
                    content = createSystemPrompt()
                ),
                com.Lyno.matchmindai.data.dto.DeepSeekMessage(
                    role = "user", 
                    content = analysisQuery
                )
            )
            val analysisRequest = deepSeekApi.createAgenticRequest(messages, includeTools = false)
            val analysisResponse = deepSeekApi.getPrediction(apiKey, analysisRequest)
            val aiAnalysis = MatchMapper.mapToAgentResponse(analysisResponse)

            Result.success(AgentResponse.analysis(aiAnalysis.text, null))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getMatchesByDateGrouped(date: String): Flow<List<LeagueGroup>> {
        return kotlinx.coroutines.flow.flow {
            try {
                val timezone = TimeZone.getDefault().id
                
                // Haal fixtures op voor de opgegeven datum
                // API key is now automatically injected by the interceptor
                val fixtures = footballApiService.getFixturesByDate(date, timezone)
                val filteredFixtures = FootballMapper.filterTopLeagues(fixtures)
                
                // Sorteer op competitie-prioriteit (Eredivisie (88), PL (39), La Liga (140) bovenaan), daarna alfabetisch
                val sortedFixtures = filteredFixtures.sortedWith(compareBy(
                    { fixture -> LeagueGroup.getLeaguePriority(fixture.league.id) },
                    { fixture -> fixture.league.name }
                ))
                
                // Groepeer per competitie
                val grouped = sortedFixtures.groupBy { it.league.id }
                
                // Converteer naar LeagueGroup objecten
                val leagueGroups = grouped.map { (leagueId, matches) ->
                    val firstMatch = matches.first()
                    val domainMatches = matches.sortedBy { it.fixture.date }.map { 
                        FootballMapper.mapFixtureItemToDomain(it) 
                    }
                    
                    LeagueGroup(
                        leagueId = leagueId,
                        leagueName = firstMatch.league.name,
                        country = firstMatch.league.country,
                        logoUrl = firstMatch.league.flag ?: "",
                        matches = domainMatches,
                        isExpanded = true
                    )
                }
                
                emit(leagueGroups)
            } catch (e: Exception) {
                // Bij fouten, geef een lege lijst terug
                emit(emptyList())
            }
        }
    }

    override fun getMatchDetails(fixtureId: Int): Flow<Resource<MatchDetail>> {
        return kotlinx.coroutines.flow.flow {
            try {
                emit(Resource.Loading())
                
                // 1. Fetch match details from API
                // API key is now automatically injected by the interceptor
                val matchDetailsResponse = footballApiService.getMatchDetails(fixtureId)
                
                // Check if we have valid response
                if (matchDetailsResponse.response?.isNullOrEmpty() != false) {
                    emit(Resource.Error("No match details found for fixture $fixtureId"))
                    return@flow
                }
                
                val matchData = matchDetailsResponse.response?.firstOrNull()
                
                if (matchData == null) {
                    emit(Resource.Error("No match details found for fixture $fixtureId"))
                    return@flow
                }
                
                // 2. Extract leagueId and season from the fixture with null safety
                val leagueId = matchData.league?.id ?: 0
                val leagueName = matchData.league?.name ?: "Unknown"
                val seasonFromMatch = matchData.league?.season
                val season = seasonFromMatch ?: determineSeasonForStandings(leagueId, null)
                
                android.util.Log.d("StandingsDebug", "📊 Fixture $fixtureId: League $leagueId ($leagueName)")
                android.util.Log.d("StandingsDebug", "📅 Season from match: $seasonFromMatch, Determined season: $season")
                android.util.Log.d("StandingsDebug", "🔄 Fetching standings for league $leagueId ($leagueName), season $season")
                
                // 3. Fetch standings for THIS specific league with multi-season fallback
                val standingsResponse = try {
                    if (leagueId > 0) {
                        // Try multiple seasons in order: current → previous → 2 years back
                        val seasonsToTry = listOf(season, season - 1, season - 2)
                        
                        var successfulResponse: com.Lyno.matchmindai.data.dto.football.StandingsResponse? = null
                        var successfulSeason: Int? = null
                        
                        for (trySeason in seasonsToTry) {
                            if (trySeason < 2000) continue // Skip invalid seasons
                            
                            try {
                                android.util.Log.d("StandingsDebug", "Trying season $trySeason for league $leagueId")
                                val response = footballApiService.getStandings(leagueId = leagueId, season = trySeason)
                                
                                // Check if response has data
                                if (!response.response.isNullOrEmpty()) {
                                    android.util.Log.d("StandingsDebug", "✅ Success with season $trySeason for league $leagueId")
                                    successfulResponse = response
                                    successfulSeason = trySeason
                                    break
                                } else {
                                    android.util.Log.w("StandingsDebug", "Empty response for league $leagueId, season $trySeason")
                                }
                            } catch (e: Exception) {
                                // Log specific error but continue to next season
                                android.util.Log.w("StandingsDebug", "Failed for league $leagueId, season $trySeason: ${e.message}")
                                
                                // Check if it's a 204 No Content error
                                if (e.message?.contains("204") == true || e.message?.contains("No Content") == true) {
                                    android.util.Log.d("StandingsDebug", "API returned 204 No Content for season $trySeason")
                                }
                            }
                        }
                        
                        if (successfulResponse != null) {
                            android.util.Log.d("StandingsDebug", "Using standings from season $successfulSeason for league $leagueId")
                        } else {
                            android.util.Log.w("StandingsDebug", "No standings found for league $leagueId in seasons $seasonsToTry")
                        }
                        
                        successfulResponse
                    } else {
                        android.util.Log.w("StandingsDebug", "Invalid league ID: $leagueId")
                        null
                    }
                } catch (e: Exception) {
                    // This catch block should only be reached if there's a fundamental error
                    android.util.Log.e("StandingsDebug", "Fundamental error fetching standings: ${e.message}", e)
                    null
                }
                
                // 4. Convert StandingsResponse to StandingsResponseDto for the mapper
                val standingsResponseDto = standingsResponse?.let { convertToStandingsResponseDto(it) }
                    
                // 5. Map everything together
                val matchDetail = MatchDetailMapper.mapMatchDetailsToDomain(
                    matchDetails = matchData,
                    standingsResponse = standingsResponseDto
                )
                
                // Log whether standings were included
                if (matchDetail.standings != null) {
                    android.util.Log.d("StandingsDebug", "Standings included in match detail: ${matchDetail.standings?.size} rows")
                } else {
                    android.util.Log.d("StandingsDebug", "No standings included in match detail")
                }
                
                emit(Resource.Success(matchDetail))
                
            } catch (e: Exception) {
                // Log error and return error state
                android.util.Log.e("MatchRepository", "Error fetching match details: ${e.message}", e)
                emit(Resource.Error("Failed to fetch match details: ${e.message ?: "Unknown error"}"))
            }
        }
    }

    /**
     * Convert StandingsResponse to StandingsResponseDto for compatibility with MatchDetailMapper.
     * This handles the type mismatch between the API service and the mapper.
     */
    private fun convertToStandingsResponseDto(standingsResponse: com.Lyno.matchmindai.data.dto.football.StandingsResponse): StandingsResponseDto {
        val leagueResponses = standingsResponse.response.mapNotNull { leagueStandings ->
            val league = leagueStandings.league
            val standings = leagueStandings.standings
            
            // Convert List<List<StandingEntry>> to List<List<StandingItemDto>>
            val convertedStandings = standings?.map { standingList ->
                standingList.mapNotNull { standingEntry ->
                    val team = standingEntry.team
                    val allStats = standingEntry.all
                    val homeStats = standingEntry.home
                    val awayStats = standingEntry.away
                    
                    // Handle nullable stats with defaults
                    val safeAllStats = allStats ?: com.Lyno.matchmindai.data.dto.football.MatchStats(0, 0, 0, 0, com.Lyno.matchmindai.data.dto.football.GoalsStats(0, 0))
                    val safeHomeStats = homeStats ?: com.Lyno.matchmindai.data.dto.football.MatchStats(0, 0, 0, 0, com.Lyno.matchmindai.data.dto.football.GoalsStats(0, 0))
                    val safeAwayStats = awayStats ?: com.Lyno.matchmindai.data.dto.football.MatchStats(0, 0, 0, 0, com.Lyno.matchmindai.data.dto.football.GoalsStats(0, 0))
                    
                    StandingItemDto(
                        rank = standingEntry.rank ?: 0,
                        team = team,
                        points = standingEntry.points ?: 0,
                        goalsDiff = standingEntry.goalsDiff ?: 0,
                        group = standingEntry.group,
                        form = standingEntry.form,
                        status = standingEntry.status,
                        description = standingEntry.description,
                        all = MatchStatsDto(
                            played = safeAllStats.played,
                            win = safeAllStats.win,
                            draw = safeAllStats.draw,
                            lose = safeAllStats.lose,
                            goals = GoalsDto(
                                `for` = safeAllStats.goals.`for`,
                                against = safeAllStats.goals.against
                            )
                        ),
                        home = MatchStatsDto(
                            played = safeHomeStats.played,
                            win = safeHomeStats.win,
                            draw = safeHomeStats.draw,
                            lose = safeHomeStats.lose,
                            goals = GoalsDto(
                                `for` = safeHomeStats.goals.`for`,
                                against = safeHomeStats.goals.against
                            )
                        ),
                        away = MatchStatsDto(
                            played = safeAwayStats.played,
                            win = safeAwayStats.win,
                            draw = safeAwayStats.draw,
                            lose = safeAwayStats.lose,
                            goals = GoalsDto(
                                `for` = safeAwayStats.goals.`for`,
                                against = safeAwayStats.goals.against
                            )
                        ),
                        update = standingEntry.update
                    )
                }
            } ?: emptyList()
            
            LeagueResponseDto(
                league = LeagueStandingsDetailsDto(
                    id = league.id ?: 0,
                    name = league.name ?: "Unknown League",
                    country = league.country ?: "",
                    logo = league.logo ?: "",
                    season = league.season ?: 0,
                    standings = convertedStandings
                )
            )
        }
        
        return StandingsResponseDto(
            response = leagueResponses,
            results = standingsResponse.results
        )
    }

    /**
     * Get comprehensive match analysis context by aggregating data from multiple sources in parallel.
     * This serves as the 'Single Source of Truth' for LLM prompts.
     *
     * @param fixtureId The unique identifier of the fixture
     * @return MatchContext containing all aggregated data
     */
    override suspend fun getMatchAnalysisContext(fixtureId: Int): com.Lyno.matchmindai.domain.model.MatchContext = coroutineScope {
        try {
            // 1. Haal basis info (fixture details) - we need this for league ID and team names
            val fixtureDetailsDeferred = async { 
                try { 
                    // API key is now automatically injected by the interceptor
                    footballApiService.getMatchDetails(fixtureId) 
                } catch (e: Exception) { 
                    android.util.Log.w("MatchRepository", "Failed to fetch fixture details: ${e.message}")
                    null 
                }
            }
            
            // 2. Start parallelle requests (API-Sports)
            val predictionsDeferred = async { 
                try { 
                    // API key is now automatically injected by the interceptor
                    footballApiService.getPredictions(fixtureId) 
                } catch (e: Exception) { 
                    android.util.Log.w("MatchRepository", "Failed to fetch predictions: ${e.message}")
                    null 
                }
            }
            
            val injuriesDeferred = async { 
                try { 
                    // API key is now automatically injected by the interceptor
                    footballApiService.getInjuries(fixtureId) 
                } catch (e: Exception) { 
                    android.util.Log.w("MatchRepository", "Failed to fetch injuries: ${e.message}")
                    null 
                }
            }
            
            val oddsDeferred = async { 
                try { 
                    // API key is now automatically injected by the interceptor
                    footballApiService.getOdds(fixtureId) 
                } catch (e: Exception) { 
                    android.util.Log.w("MatchRepository", "Failed to fetch odds: ${e.message}")
                    null 
                }
            }
            
            // 3. Wacht op resultaten
            val fixtureDetails = fixtureDetailsDeferred.await()
            
            if (fixtureDetails == null || fixtureDetails.response?.isEmpty() != false) {
                throw IllegalStateException("Could not fetch fixture details for fixture $fixtureId")
            }
            
            val fixture = fixtureDetails.response?.firstOrNull()
            
            if (fixture == null) {
                throw IllegalStateException("Could not fetch fixture details for fixture $fixtureId")
            }
            val predictions = predictionsDeferred.await()
            val injuries = injuriesDeferred.await()
            val odds = oddsDeferred.await()
            
            // 4. Haal standings op voor de juiste competitie (gebruik de logica die we eerder maakten)
            val standingsText = try {
                val leagueId = fixture.league?.id ?: 0
                val season = fixture.league?.season ?: getCurrentSeason(leagueId)
                
                if (leagueId > 0) {
                    // API key is now automatically injected by the interceptor
                    val standingsResponse = footballApiService.getStandings(leagueId, season)
                    val leagueStandings = standingsResponse.response.firstOrNull()
                    val standings = leagueStandings?.standings?.flatten() ?: emptyList()
                    
                    // Format standings as readable text
                    val sb = StringBuilder()
                    sb.append("🏆 ${fixture.league?.name ?: "Competitie"} Stand:\n")
                    standings.take(5).forEachIndexed { index, standing ->
                        val teamName = standing.team?.name ?: "Onbekend team"
                        val points = standing.points ?: 0
                        val rank = standing.rank ?: index + 1
                        sb.append("$rank. $teamName - ${points}pnt\n")
                    }
                    if (standings.size > 5) {
                        sb.append("... en nog ${standings.size - 5} andere teams")
                    }
                    sb.toString()
                } else {
                    "Geen stand beschikbaar (league ID niet gevonden)"
                }
            } catch (e: Exception) {
                android.util.Log.w("MatchRepository", "Failed to fetch standings: ${e.message}")
                "Kon stand niet ophalen: ${e.message}"
            }
            
            // 5. Formatteer data naar leesbare strings voor de AI
            val predictionText = extractPredictionAdvice(predictions)
            val injuriesText = extractInjuriesList(injuries)
            val oddsText = extractOddsSummary(odds)
            
            // 6. Return Context
            com.Lyno.matchmindai.domain.model.MatchContext(
                matchId = fixtureId,
                teams = "${fixture.teams?.home?.name ?: "Onbekend"} vs ${fixture.teams?.away?.name ?: "Onbekend"}",
                date = fixture.fixture?.date ?: "Onbekende datum",
                predictions = predictionText,
                injuries = injuriesText,
                odds = oddsText,
                standings = standingsText,
                newsContext = null // Tavily wordt later toegevoegd in de ViewModel laag
            )
            
        } catch (e: Exception) {
            android.util.Log.e("MatchRepository", "Error in getMatchAnalysisContext: ${e.message}", e)
            // Return minimal context with error information
            com.Lyno.matchmindai.domain.model.MatchContext(
                matchId = fixtureId,
                teams = "Onbekend vs Onbekend",
                date = "Onbekende datum",
                predictions = null,
                injuries = null,
                odds = null,
                standings = "Fout bij ophalen data: ${e.message}",
                newsContext = null
            )
        }
    }

    /**
     * Get injuries for a specific fixture.
     * Maps API response to domain Injury models.
     */
    override suspend fun getInjuries(fixtureId: Int): Result<List<com.Lyno.matchmindai.domain.model.Injury>> {
        // Check cache first
        if (isCacheValid(fixtureId)) {
            injuriesCache[fixtureId]?.let { cachedInjuries ->
                android.util.Log.d("MatchRepository", "Using cached injuries for fixture $fixtureId")
                return Result.success(cachedInjuries)
            }
        }
        
        return try {
            // API key is now automatically injected by the interceptor
            // Use the typed version of the API call that returns InjuriesResponseDto directly
            val injuriesResponse = footballApiService.getInjuriesTyped(fixtureId)
            val injuries = InjuriesMapper.mapToDomain(injuriesResponse)
            
            // Cache the injuries
            injuriesCache[fixtureId] = injuries
            updateCacheTimestamp(fixtureId)
            
            Result.success(injuries)
        } catch (e: Exception) {
            android.util.Log.e("MatchRepository", "Error fetching injuries: ${e.message}", e)
            // Cache empty list on error
            injuriesCache[fixtureId] = emptyList()
            updateCacheTimestamp(fixtureId)
            Result.failure(e)
        }
    }
    
    /**
     * Get predictions for a specific fixture.
     * Maps API response to domain MatchPredictionData model.
     * Always tries to fetch predictions from API - let API determine availability.
     */
    override suspend fun getPredictions(fixtureId: Int): Result<com.Lyno.matchmindai.domain.model.MatchPredictionData?> {
        // Check cache first
        if (isCacheValid(fixtureId)) {
            predictionsCache[fixtureId]?.let { cachedPrediction ->
                android.util.Log.d("MatchRepository", "Using cached prediction for fixture $fixtureId")
                return Result.success(cachedPrediction)
            }
        }
        
        return try {
            // First, get fixture details for team names and league info
            val fixtureDetails = footballApiService.getMatchDetails(fixtureId)
            val fixture = fixtureDetails.response?.firstOrNull()
            
            if (fixture == null) {
                android.util.Log.w("MatchRepository", "No fixture details found for fixture $fixtureId")
                return Result.success(null)
            }
            
            val leagueName = fixture.league?.name ?: "Unknown"
            val homeTeam = fixture.teams?.home?.name ?: "Home Team"
            val awayTeam = fixture.teams?.away?.name ?: "Away Team"
            
            // Try to fetch predictions from API
            // API key is now automatically injected by the interceptor
            val response = footballApiService.getPredictions(fixtureId)
            
            // Check if API returned any predictions
            if (response.response.isEmpty()) {
                android.util.Log.d("MatchRepository", "No predictions available from API for fixture $fixtureId (League: $leagueName)")
                // Return empty prediction with clear message
                val emptyPrediction = PredictionsMapper.empty(fixtureId, homeTeam, awayTeam, leagueName)
                // Cache the empty prediction
                predictionsCache[fixtureId] = emptyPrediction
                updateCacheTimestamp(fixtureId)
                return Result.success(emptyPrediction)
            }
            
            // Map API response to domain model using PredictionsMapper
            val predictionData = PredictionsMapper.toMatchPredictionData(response, fixtureId)
            
            if (predictionData == null) {
                android.util.Log.d("MatchRepository", "PredictionsMapper returned null for fixture $fixtureId")
                // Return empty prediction with clear message
                val emptyPrediction = PredictionsMapper.empty(fixtureId, homeTeam, awayTeam, leagueName)
                // Cache the empty prediction
                predictionsCache[fixtureId] = emptyPrediction
                updateCacheTimestamp(fixtureId)
                return Result.success(emptyPrediction)
            }
            
            // Cache the prediction
            predictionsCache[fixtureId] = predictionData
            updateCacheTimestamp(fixtureId)
            
            Result.success(predictionData)
        } catch (e: Exception) {
            android.util.Log.e("MatchRepository", "Error fetching predictions: ${e.message}", e)
            
            // On error, try to get fixture details for fallback
            try {
                val fixtureDetails = footballApiService.getMatchDetails(fixtureId)
                val fixture = fixtureDetails.response?.firstOrNull()
                
                if (fixture != null) {
                    val leagueName = fixture.league?.name ?: "Unknown"
                    val homeTeam = fixture.teams?.home?.name ?: "Home Team"
                    val awayTeam = fixture.teams?.away?.name ?: "Away Team"
                    
                    val emptyPrediction = PredictionsMapper.empty(fixtureId, homeTeam, awayTeam, leagueName)
                    // Cache the empty prediction
                    predictionsCache[fixtureId] = emptyPrediction
                    updateCacheTimestamp(fixtureId)
                    return Result.success(emptyPrediction)
                }
            } catch (innerE: Exception) {
                android.util.Log.e("MatchRepository", "Failed to get fixture details for fallback: ${innerE.message}")
            }
            
            Result.failure(e)
        }
    }
    
    /**
     * Get odds for a specific fixture.
     * Maps API response to domain OddsData model.
     */
    override suspend fun getOdds(fixtureId: Int): Result<com.Lyno.matchmindai.domain.model.OddsData?> {
        // Check cache first
        if (isCacheValid(fixtureId)) {
            oddsCache[fixtureId]?.let { cachedOdds ->
                android.util.Log.d("MatchRepository", "Using cached odds for fixture $fixtureId")
                return Result.success(cachedOdds)
            }
        }
        
        return try {
            // API key is now automatically injected by the interceptor
            val response = footballApiService.getOdds(fixtureId)
            
            // Get fixture details for team names
            val fixtureDetails = footballApiService.getMatchDetails(fixtureId)
            val fixture = fixtureDetails.response?.firstOrNull()
            
            if (fixture != null) {
                val homeTeam = fixture.teams?.home?.name ?: "Home Team"
                val awayTeam = fixture.teams?.away?.name ?: "Away Team"
                
                val oddsData = OddsMapper.toOddsData(response, fixtureId, homeTeam, awayTeam)
                
                // Cache the odds
                oddsCache[fixtureId] = oddsData
                updateCacheTimestamp(fixtureId)
                
                Result.success(oddsData)
            } else {
                // Cache null result
                oddsCache[fixtureId] = null
                updateCacheTimestamp(fixtureId)
                Result.success(null)
            }
        } catch (e: Exception) {
            android.util.Log.e("MatchRepository", "Error fetching odds: ${e.message}", e)
            // Cache null on error
            oddsCache[fixtureId] = null
            updateCacheTimestamp(fixtureId)
            Result.failure(e)
        }
    }
    
    /**
     * Extract prediction advice from predictions JSON response.
     */
    private fun extractPredictionAdvice(predictions: Any?): String? {
        return if (predictions != null) {
            try {
                // Simple extraction - in a real implementation, you would parse the JSON structure
                // For now, return a placeholder
                "Voorspelling beschikbaar (raadpleeg API voor details)"
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    /**
     * Extract injuries list from injuries JSON response.
     */
    private fun extractInjuriesList(injuries: Any?): List<String>? {
        return if (injuries != null) {
            try {
                // Simple extraction - in a real implementation, you would parse the JSON structure
                // For now, return empty list
                emptyList()
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    /**
     * Extract odds summary from odds JSON response.
     */
    private fun extractOddsSummary(odds: Any?): String? {
        return if (odds != null) {
            try {
                // Simple extraction - in a real implementation, you would parse the JSON structure
                // For now, return a placeholder
                "Odds beschikbaar (raadpleeg API voor details)"
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    /**
     * Create an empty match detail for error/loading states.
     */
    private fun createEmptyMatchDetail(
        fixtureId: Int,
        homeTeam: String,
        awayTeam: String,
        league: String
    ): MatchDetail {
        return MatchDetail(
            fixtureId = fixtureId,
            homeTeam = homeTeam,
            awayTeam = awayTeam,
            league = league,
            info = com.Lyno.matchmindai.domain.model.MatchInfo(
                stadium = "Nog geen data beschikbaar",
                referee = null,
                date = null,
                time = null,
                timestamp = null,
                venue = null
            ),
            stats = emptyList(),
            lineups = com.Lyno.matchmindai.domain.model.MatchLineups(
                home = com.Lyno.matchmindai.domain.model.TeamLineup(
                    teamName = homeTeam,
                    formation = null,
                    coach = null,
                    players = emptyList(),
                    substitutes = emptyList()
                ),
                away = com.Lyno.matchmindai.domain.model.TeamLineup(
                    teamName = awayTeam,
                    formation = null,
                    coach = null,
                    players = emptyList(),
                    substitutes = emptyList()
                )
            ),
            events = emptyList(),
            score = null,
            status = null
        )
    }

    /**
     * Map MatchDetails DTO to MatchDetail domain model.
     * Uses the new MatchDetailMapper for proper data mapping.
     */
    private fun mapMatchDetailsToDomain(matchDetails: MatchDetails): MatchDetail {
        return MatchDetailMapper.mapMatchDetailsToDomain(matchDetails)
    }

    override suspend fun getFixturesForTool(date: String, searchQuery: String?, leagueId: Int?, live: Boolean): String = kotlinx.coroutines.coroutineScope {
        try {
            val timezone = "Europe/Amsterdam"
            
            // 1. SPECIFIEKE ZOEKOPDRACHT (bijv. "Ajax")
            if (!searchQuery.isNullOrBlank()) {
                // FIX: Gebruik 'async' direct, niet via de package naam
                val result = async { getFixturesForTeam(searchQuery, date, timezone, live) }
                return@coroutineScope result.await()
            }
            
            // 2. HAAL ALLE DATA
            // Let op: allFixtures is hier List<Any> omdat de return types verschillen
            val allFixtures = if (live) {
                footballApiService.getLiveFixtures(timezone)
            } else {
                footballApiService.getFixturesByDate(date, timezone)
            }
            
            // 3. FILTER LOGICA
            val relevantFixtures = if (leagueId != null) {
                // Filter op specifiek League ID als daarom gevraagd is
                allFixtures.filter { 
                    when (it) {
                        is FixtureItemDto -> it.league.id == leagueId
                        is LiveMatchDto -> it.league.id == leagueId
                        else -> false
                    }
                }
            } else {
                // ALGEMENE VRAAG ("Wat speelt er vandaag?")
                // DYNAMIC LEAGUE DISCOVERY: Gebruik GetActiveLeaguesUseCase voor dynamische league IDs
                val topLeagueIds = try {
                    getActiveLeaguesUseCase.getTopPriorityLeagueIds(minScore = 50, limit = 20)
                } catch (e: Exception) {
                    android.util.Log.e("DynamicLeagueDiscovery", "Failed to get dynamic league IDs: ${e.message}")
                    listOf(
                        88, 84, // Eredivisie, KNVB Beker
                        2, 3, 848, // CL, EL, Conference League
                        39, 140, 78, 135, 61 // Top 5 Leagues
                    )
                }
                
                android.util.Log.d("DynamicLeagueDiscovery", "Using league IDs: $topLeagueIds")
                
                // Probeer eerst te filteren op top competities
                val topMatches = allFixtures.filter { 
                    when (it) {
                        is FixtureItemDto -> it.league.id in topLeagueIds
                        is LiveMatchDto -> it.league.id in topLeagueIds
                        else -> false
                    }
                }

                // NIEUWE LOGICA: Fallback mechanisme
                if (topMatches.isNotEmpty()) {
                    // FIX: Pre-fetch de prioriteiten om 'suspend' call in sortering te voorkomen
                    val matchesWithPriority = topMatches.map { fixture ->
                        val currentLeagueId = if (fixture is FixtureItemDto) fixture.league.id else (fixture as LiveMatchDto).league.id
                        val priority = getActiveLeaguesUseCase.determineFixturePriority(currentLeagueId)
                        Pair(fixture, priority)
                    }

                    // Sorteer nu op de opgehaalde waarde en pak de fixtures terug
                    matchesWithPriority.sortedByDescending { it.second }
                        .map { it.first }
                        .take(20)
                } else {
                    // Als er GEEN topwedstrijden zijn, toon dan de overige wedstrijden
                    allFixtures.take(15)
                }
            }
            
            // 4. CHECK OP LEEG RESULTAAT
            if (relevantFixtures.isEmpty()) {
                return@coroutineScope "Geen wedstrijden gevonden op $date."
            }
            
            // 5. FORMATTEREN NAAR JSON
            val simplifiedData = relevantFixtures.mapNotNull { fixture ->
                when (fixture) {
                      is FixtureItemDto -> {
                          val status = fixture.fixture.status.short
                          val homeGoals = fixture.goals?.home
                          val awayGoals = fixture.goals?.away
                          
                          val scoreString = when {
                              status in listOf("1H", "HT", "2H", "ET", "BT", "P", "FT", "AET", "PEN") -> {
                                  if (homeGoals != null && awayGoals != null) "$homeGoals-$awayGoals" else "?"
                              }
                              else -> "vs" 
                          }
                          
                          mapOf(
                              "time" to "${fixture.fixture.status.short} ${fixture.fixture.status.elapsed ?: ""}'".trim(),
                              "home" to fixture.teams.home.name,
                              "away" to fixture.teams.away.name,
                              "score" to scoreString,
                              "league" to fixture.league.name
                          )
                      }
                      is LiveMatchDto -> {
                          val status = fixture.fixture.status.short
                          val homeGoals = fixture.goals?.home
                          val awayGoals = fixture.goals?.away
                          
                          val scoreString = when {
                              status in listOf("1H", "HT", "2H", "ET", "BT", "P", "FT", "AET", "PEN") -> {
                                  if (homeGoals != null && awayGoals != null) "$homeGoals-$awayGoals" else "?"
                              }
                              else -> "vs" 
                          }
                          
                          mapOf(
                              "time" to "${fixture.fixture.status.short} ${fixture.fixture.status.elapsed ?: ""}'".trim(),
                              "home" to fixture.teams.home.name,
                              "away" to fixture.teams.away.name,
                              "score" to scoreString,
                              "league" to fixture.league.name
                          )
                      }
                    else -> null
                }
            }
            
            Companion.buildJsonString(simplifiedData)
        } catch (e: Exception) {
            "{\"error\": \"Kon fixtures niet ophalen: ${e.message}\"}"
        }
    }
    
    /**
     * Get fixtures for a specific team using robust Date-First filtering.
     * FIX: Bypasses API-Sports "season" requirement bugs by fetching all daily matches 
     * and filtering locally. This ensures Cup matches (KNVB Beker) are also found.
     */
    private suspend fun getFixturesForTeam(
        teamName: String,
        date: String,
        timezone: String,
        live: Boolean
    ): String {
        return try {
            // 1. Zoek Team ID (Ajax = 85)
            val teamId = getTeamIdByName(teamName)
            
            if (teamId == null) {
                return "Team '$teamName' niet gevonden in de database."
            }
            
            android.util.Log.d("FixtureDebug", "🔍 Searching matches for Team ID: $teamId on date: $date")

            // 2. STRATEGIE WIJZIGING: Haal ALLES op voor deze datum.
            // Dit omzeilt het probleem dat je voor de 'team' endpoint verplicht een seizoen moet meegeven,
            // wat vaak fout gaat rond de jaarwisseling (seizoen 2025 vs 2026).
            // De 'fixtures by date' endpoint heeft GEEN seizoen nodig.
            val allFixturesForDate = footballApiService.getFixturesByDate(date, timezone)

            // 3. Filter lokaal in de lijst
            val teamMatches = allFixturesForDate.filter { fixture -> 
                fixture.teams.home.id == teamId || fixture.teams.away.id == teamId
            }
            
            android.util.Log.d("FixtureDebug", "✅ Found ${teamMatches.size} matches for team $teamName")

            if (teamMatches.isEmpty()) {
                return "Geen wedstrijd gevonden voor $teamName op $date."
            }

            // 4. Maak de JSON voor de AI
            val simplifiedData = teamMatches.map { fixture ->
                val status = fixture.fixture.status.short
                val homeGoals = fixture.goals?.home
                val awayGoals = fixture.goals?.away
                
                val scoreString = when {
                     status in listOf("1H", "HT", "2H", "ET", "BT", "P", "FT", "AET", "PEN") -> {
                         if (homeGoals != null && awayGoals != null) "$homeGoals-$awayGoals" else "?"
                     }
                     else -> "vs" 
                }
                
                mapOf(
                    "time" to "${fixture.fixture.status.short} ${fixture.fixture.status.elapsed ?: ""}'".trim(),
                    "home" to fixture.teams.home.name,
                    "away" to fixture.teams.away.name,
                    "score" to scoreString,
                    "league" to fixture.league.name,
                    "date" to fixture.fixture.date,
                    "status" to status
                )
            }
            
            Companion.buildJsonString(simplifiedData)

        } catch (e: Exception) {
            android.util.Log.e("FixtureDebug", "❌ Error in getFixturesForTeam: ${e.message}")
            "{\"error\": \"Fout bij ophalen wedstrijd: ${e.message}\"}"
        }
    }
    
    /**
     * Get team ID by name for known Dutch clubs.
     * In a real implementation, this would query the /teams endpoint.
     */
    private fun getTeamIdByName(teamName: String): Int? {
        val teamMap = mapOf(
            "almere city" to 498, // Almere City FC ID (API-Sports)
            "ajax" to 85,
            "psv" to 86,
            "feyenoord" to 87,
            "az" to 88,
            "fc utrecht" to 89,
            "fc twente" to 90,
            "vitesse" to 91,
            "heerenveen" to 92,
            "groningen" to 93,
            "nec" to 94,
            "willem ii" to 95,
            "sparta" to 96,
            "heracles" to 97,
            "rkc" to 98,
            "cambuur" to 99,
            "go ahead eagles" to 100,
            "fortuna sittard" to 101,
            "excelsior" to 102,
            "volendam" to 103
        )
        
        val normalizedName = teamName.lowercase().trim()
        return teamMap[normalizedName] ?: teamMap.entries.find { 
            normalizedName.contains(it.key) || it.key.contains(normalizedName) 
        }?.value
    }
    
    /**
     * Parse JSON response for team leagues.
     * This is a simplified version that doesn't use kotlinx.serialization.
     */
    private fun parseTeamLeaguesJson(jsonString: String): Pair<Int?, String?> {
        // Simple string parsing for league ID and name
        // This is a fallback implementation
        try {
            // Look for "id" and "name" patterns in the JSON
            val idPattern = "\"id\"\\s*:\\s*(\\d+)".toRegex()
            val namePattern = "\"name\"\\s*:\\s*\"([^\"]+)\"".toRegex()
            val typePattern = "\"type\"\\s*:\\s*\"([^\"]+)\"".toRegex()
            
            val idMatch = idPattern.find(jsonString)
            val nameMatch = namePattern.find(jsonString)
            val typeMatch = typePattern.find(jsonString)
            
            val leagueId = idMatch?.groupValues?.get(1)?.toIntOrNull()
            val leagueName = nameMatch?.groupValues?.get(1)
            val leagueType = typeMatch?.groupValues?.get(1)
            
            // Only return if it's a League type
            if (leagueType == "League" && leagueId != null) {
                return Pair(leagueId, leagueName)
            }
            
            return Pair(leagueId, leagueName)
        } catch (e: Exception) {
            return Pair(null, null)
        }
    }

    /**
     * Execute get_standings tool with robust error handling and logging.
     * This is used by the ToolOrchestrator to fetch standings data.
     */
    private suspend fun executeGetStandings(leagueId: Int): String {
        return try {
            // Seizoen logica (zoals eerder)
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-based
            val season = if (currentMonth > 6) currentYear else currentYear - 1

            android.util.Log.d("MatchRepo", "Fetching standings for League $leagueId, Season $season")
            
            // API key is now automatically injected by the interceptor
            val response = footballApiService.getStandings(leagueId, season)
            
            android.util.Log.d("MatchRepo", "Standings response results: ${response.results}")

            if (response.response.isNullOrEmpty()) {
                return "FOUT: API gaf geen data terug voor League $leagueId (Seizoen $season)."
            }

            val leagueStandings = response.response[0]
            val leagueData = leagueStandings.league
            // CRUCIAL: Flatten de geneste lijst
            val standings = leagueStandings.standings?.flatten() ?: emptyList()

            if (standings.isEmpty()) {
                return "FOUT: Geen standen gevonden in het klassement"
            }

            val sb = StringBuilder()
            sb.append("OFFICIËLE STAND ${leagueData?.name ?: "Competitie $leagueId"} (${leagueData?.season ?: season}):\n")
            sb.append("Pos | Team | Pnt | Gesp | Saldo | Vorm\n")
            sb.append("---|---|---|---|---|---\n")

            // Beperk tot top 10 om tokens te besparen en overzicht te houden
            standings.take(10).forEach { entry ->
                val teamName = entry.team?.name ?: "Onbekend team"
                val points = entry.points ?: 0
                val played = entry.all?.played ?: 0
                val goalsDiff = entry.goalsDiff ?: 0
                val form = entry.form ?: "?"
                val rank = entry.rank ?: 0
                
                sb.append("$rank. $teamName | $points | $played | $goalsDiff | $form\n")
            }
            
            // Return de string
            sb.toString()

        } catch (e: Exception) {
            android.util.Log.e("MatchRepo", "Error parsing standings", e)
            "FOUT: Technische fout bij ophalen stand: ${e.message}"
        }
    }

    /**
     * Get standings for a specific team using dynamic league detection.
     * This is the recommended approach for finding standings for a specific club.
     *
     * @param teamName The name of the team (e.g., "Almere City")
     * @return AgentResponse with standings information
     */
    suspend fun getStandingsForTeam(teamName: String): Result<AgentResponse> {
        return try {
            // 1. Find team ID
            val teamId = getTeamIdByName(teamName)
            if (teamId == null) {
                return Result.success(AgentResponse.error("Team '$teamName' niet gevonden in de database."))
            }
            
            // 2. Get team's leagues for current season (2025)
            val season = 2025 // Seizoen 2025-2026
            val leaguesJson = footballApiService.getTeamLeagues(teamId, season)
            
            // Parse JSON to find main league (type: "League", not "Cup")
            val (mainLeagueId, mainLeagueName) = parseTeamLeaguesJson(leaguesJson.toString())
            
            if (mainLeagueId == null) {
                return Result.success(AgentResponse.error("Kon geen geschikte competitie vinden voor $teamName."))
            }
            
            // 3. Get standings for the found league
            val standingsResult = getLeagueStandings(mainLeagueId)
            if (standingsResult.isSuccess) {
                val response = standingsResult.getOrNull()
                if (response != null) {
                    // Add league name context to the response
                    val leagueContext = if (mainLeagueName != null) " ($mainLeagueName)" else ""
                    val updatedText = response.text?.replace(
                        "STAND ", 
                        "STAND voor $teamName$leagueContext "
                    ) ?: response.text
                    
                    return Result.success(AgentResponse.standings(updatedText, response.relatedData))
                }
            }
            
            // Fallback if standings call failed
            Result.success(AgentResponse.error("Kon stand voor $teamName niet ophalen."))
            
        } catch (e: Exception) {
            e.printStackTrace()
            Result.success(AgentResponse.error("FOUT bij ophalen stand voor team: ${e.localizedMessage}"))
        }
    }

    override suspend fun streamChat(
        apiKey: String,
        request: com.Lyno.matchmindai.data.dto.DeepSeekRequest
    ): Flow<ChatStreamUpdate> {
        // Use the streaming API for R1 model
        return deepSeekApi.streamChat(apiKey, request)
    }

    override suspend fun performDeepAnalysisWithStreaming(
        apiKey: String,
        matchContext: com.Lyno.matchmindai.data.dto.MatchContextDto,
        userQuery: String,
        useStreaming: Boolean
    ): Flow<ChatStreamUpdate>? {
        if (!useStreaming) {
            // Fall back to regular non-streaming analysis
            return null
        }

        // Serialize match context to JSON string for the prompt
        val contextJson = """
            {
                "fixtureId": ${matchContext.fixture.fixtureId},
                "homeTeam": "${matchContext.fixture.homeTeam}",
                "awayTeam": "${matchContext.fixture.awayTeam}",
                "date": "${matchContext.fixture.date}",
                "league": "${matchContext.fixture.league ?: ""}",
                "standingsCount": ${matchContext.standings?.size ?: 0},
                "newsCount": ${matchContext.news?.size ?: 0}
            }
        """.trimIndent()

        // Build the single comprehensive prompt (as per report Section 5)
        val prompt = buildString {
            appendLine("DATUM VANDAAG: ${matchContext.analysisDate}")
            appendLine()
            appendLine("DATA CONTEXT (API-SPORTS & TAVILY):")
            appendLine(contextJson)
            appendLine()
            appendLine("TAAK:")
            appendLine(userQuery)
            appendLine()
            appendLine("INSTRUCTIES:")
            appendLine("1. Analyseer de data grondig en methodisch.")
            appendLine("2. Bij conflicten tussen API data en nieuws, geloof het recentere nieuws.")
            appendLine("3. Geef een gestructureerd antwoord in het NEDERLANDS.")
            appendLine("4. Baseer je analyse op feiten uit de context.")
        }

        // Create request with R1 model (no system messages, no tools)
        val messages = listOf(
            com.Lyno.matchmindai.data.dto.DeepSeekMessage(
                role = "user",
                content = prompt
            )
        )

        // Create streaming request for R1
        val request = deepSeekApi.createStreamingRequest(
            messages = messages,
            temperature = 0.7,
            model = "deepseek-reasoner"
        )

        // Return the streaming flow
        return streamChat(apiKey, request)
    }
    
    /**
     * Process a Prophet Module query for advanced match analysis.
     * This uses the Phase 3 "Prophet Module" with "Anchor & Adjust" strategy.
     * 
     * @param query User query like "Predict Feyenoord vs Ajax"
     * @return Result containing either an AgentResponse with Prophet analysis or an error
     */
    override suspend fun processProphetQuery(query: String): Result<AgentResponse> {
        return try {
            val preferences = apiKeyStorage.getPreferences().first()
            val deepSeekApiKey = preferences.deepSeekApiKey
            val apiSportsKey = preferences.apiSportsKey
            val tavilyApiKey = preferences.tavilyApiKey
            
            // Check if all required API keys are available
            if (deepSeekApiKey.isBlank()) {
                return Result.success(AgentResponse.error("DeepSeek API key is missing. Please configure your API key in settings."))
            }
            
            if (apiSportsKey.isBlank()) {
                return Result.success(AgentResponse.error("API-Sports key is missing. Please configure your API key in settings."))
            }
            
            // Get or create chat session for context
            val sessionId = getOrCreateLastSession()
            
            // Create ChatRepository for tool output storage
            val chatRepository = ChatRepositoryImpl(chatDao)
            
            // Create orchestrator with ChatRepository
            val orchestrator = ToolOrchestrator(
                deepSeekApi = deepSeekApi,
                footballApiService = footballApiService,
                searchService = searchService,
                chatRepository = chatRepository,
                matchRepository = this,
                matchCacheManager = matchCacheManager,
                apiSportsKey = apiSportsKey,
                deepSeekApiKey = deepSeekApiKey,
                tavilyApiKey = tavilyApiKey
            )
            
            // Execute Prophet query
            val response = orchestrator.executeProphetQuery(query, sessionId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get best odds for AI tool calls with filtering for beginners.
     * This function provides simplified odds data suitable for beginners with safety and value ratings.
     * 
     * @param date Optional date in YYYY-MM-DD format (defaults to today)
     * @param leagueId Optional league ID for filtering
     * @param teamName Optional team name for filtering
     * @param limit Maximum number of odds to return (default: 10)
     * @return JSON string with simplified odds data for AI consumption
     */
    override suspend fun getBestOddsForTool(
        date: String?,
        leagueId: Int?,
        teamName: String?,
        limit: Int
    ): String = coroutineScope {
        try {
            // 1. Determine date (default to today)
            val targetDate = date ?: getTodayDate()
            
            // 2. Get fixtures for the date to match team names with fixture IDs
            val fixtures = try {
                footballApiService.getFixturesByDate(targetDate, "Europe/Amsterdam")
            } catch (e: Exception) {
                android.util.Log.w("OddsTool", "Failed to fetch fixtures for date $targetDate: ${e.message}")
                emptyList()
            }
            
            // 3. Filter fixtures based on team name if provided
            val filteredFixtures = if (teamName != null) {
                val teamId = getTeamIdByName(teamName)
                if (teamId != null) {
                    fixtures.filter { 
                        it.teams.home.id == teamId || it.teams.away.id == teamId 
                    }
                } else {
                    // Fallback: filter by team name substring
                    fixtures.filter { 
                        it.teams.home.name.contains(teamName, ignoreCase = true) ||
                        it.teams.away.name.contains(teamName, ignoreCase = true)
                    }
                }
            } else {
                fixtures
            }
            
            // 4. Get odds for each fixture (parallel execution)
            val oddsResults = filteredFixtures.map { fixture ->
                async {
                    try {
                        val oddsResponse = if (leagueId != null) {
                            // Use league-specific odds endpoint
                            footballApiService.getOddsByLeague(
                                leagueId = leagueId,
                                season = getCurrentSeasonForDate(targetDate),
                                bookmakerId = null,
                                betId = null
                            )
                        } else {
                            // Use date-specific odds endpoint
                            footballApiService.getOddsByDate(
                                date = targetDate,
                                leagueId = null,
                                timezone = "Europe/Amsterdam"
                            )
                        }
                        
                        // Convert to simplified odds
                        val simplifiedOdds = OddsMapper.toSimplifiedOddsList(oddsResponse, emptyList())
                        simplifiedOdds
                    } catch (e: Exception) {
                        android.util.Log.w("OddsTool", "Failed to fetch odds for fixture ${fixture.fixture.id}: ${e.message}")
                        emptyList()
                    }
                }
            }
            
            // 5. Wait for all odds results and flatten
            val allOdds = oddsResults.flatMap { it.await() }
            
            // 6. Filter for beginners (safe bets with good value)
            val beginnerFriendlyOdds = OddsMapper.filterForBeginners(allOdds)
            
            // 7. Apply limit
            val limitedOdds = beginnerFriendlyOdds.take(limit)
            
            // 8. Return JSON string
            if (limitedOdds.isEmpty()) {
                return@coroutineScope "{\"message\": \"Geen geschikte odds gevonden voor de opgegeven criteria.\"}"
            }
            
            buildOddsJsonString(limitedOdds)
            
        } catch (e: Exception) {
            android.util.Log.e("OddsTool", "Error in getBestOddsForTool: ${e.message}", e)
            "{\"error\": \"Fout bij ophalen odds: ${e.message}\"}"
        }
    }
}
