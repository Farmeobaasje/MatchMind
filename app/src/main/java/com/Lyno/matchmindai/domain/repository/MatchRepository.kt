package com.Lyno.matchmindai.domain.repository

import com.Lyno.matchmindai.domain.model.AgentResponse
import com.Lyno.matchmindai.domain.model.ChatSession
import com.Lyno.matchmindai.domain.model.Injury
import com.Lyno.matchmindai.domain.model.LeagueGroup
import com.Lyno.matchmindai.domain.model.MatchContext
import com.Lyno.matchmindai.domain.model.MatchDetail
import com.Lyno.matchmindai.domain.model.MatchFixture
import com.Lyno.matchmindai.domain.model.MatchPredictionData
import com.Lyno.matchmindai.domain.model.OddsData
import kotlinx.coroutines.flow.Flow
import com.Lyno.matchmindai.data.dto.ChatStreamUpdate

/**
 * Interface for match prediction operations.
 * Domain layer contract that defines how presentation layer can request predictions.
 */
interface MatchRepository {
    /**
     * Get matches grouped by league for a specific date.
     * @param date The date in YYYY-MM-DD format
     * @return Flow of list of LeagueGroup for reactive UI updates
     */
    fun getMatchesByDateGrouped(date: String): Flow<List<LeagueGroup>>

    /**
     * Get today's football match fixtures.
     * @return Result containing either a list of MatchFixture or an error
     */
    suspend fun getTodaysMatches(): Result<List<MatchFixture>>

    /**
     * Get upcoming football match fixtures for the next 3 days.
     * @return Result containing either a list of MatchFixture or an error
     */
    suspend fun getUpcomingMatches(): Result<List<MatchFixture>>

    /**
     * Get live football match fixtures currently in progress.
     * @return Result containing either a list of MatchFixture or an error
     */
    suspend fun getLiveMatches(): Result<List<MatchFixture>>

    /**
     * Get cached fixtures from local database.
     * @return Flow of list of MatchFixture
     */
    fun getCachedFixtures(): Flow<List<MatchFixture>>

    /**
     * Cache fixtures to local database.
     * @param fixtures The fixtures to cache
     */
    suspend fun cacheFixtures(fixtures: List<MatchFixture>)

    // Chat Session Management
    /**
     * Get all chat sessions.
     * @return Flow of list of chat sessions
     */
    fun getChatSessions(): Flow<List<ChatSession>>

    /**
     * Get messages for a specific chat session.
     * @param sessionId The chat session ID
     * @return Flow of list of chat message entities
     */
    fun getChatMessages(sessionId: String): Flow<List<com.Lyno.matchmindai.data.local.entity.ChatMessageEntity>>

    /**
     * Create a new chat session.
     * @param title The title of new session
     * @return The ID of created session
     */
    suspend fun createChatSession(title: String): String

    /**
     * Update title of a chat session.
     * @param sessionId The chat session ID
     * @param title The new title
     */
    suspend fun updateChatSessionTitle(sessionId: String, title: String)

    /**
     * Delete a chat session and all its messages.
     * @param sessionId The chat session ID
     */
    suspend fun deleteChatSession(sessionId: String)

    /**
     * Get the last chat session ID, or create a new one if none exists.
     * @return The session ID
     */
    suspend fun getOrCreateLastSession(): String

    /**
     * Process a user query using AI tool orchestration.
     * This method allows the AI to decide which tools to use based on the query.
     * @param query The user's natural language query
     * @return Result containing either an AgentResponse or an error
     */
    suspend fun processUserQuery(query: String): Result<AgentResponse>

    /**
     * Get a prediction for a specific fixture.
     * @param fixtureId The ID of the fixture
     * @return Result containing either an AgentResponse with prediction data or an error
     */
    suspend fun getMatchPrediction(fixtureId: Int): Result<AgentResponse>

    /**
     * Search the internet for football-related information.
     * @param query The search query
     * @return Result containing either an AgentResponse with search results or an error
     */
    suspend fun searchInternet(query: String): Result<AgentResponse>

    /**
     * Get league standings for a specific league.
     * @param leagueId The league ID
     * @return Result containing either an AgentResponse with standings data or an error
     */
    suspend fun getLeagueStandings(leagueId: Int): Result<AgentResponse>

    /**
     * Get match analysis including head-to-head and recent form.
     * @param homeTeam The home team name
     * @param awayTeam The away team name
     * @return Result containing either an AgentResponse with analysis data or an error
     */
    suspend fun getMatchAnalysis(homeTeam: String, awayTeam: String): Result<AgentResponse>

    /**
     * Get detailed match information including events, lineups, and statistics.
     * @param fixtureId The ID of the fixture
     * @return Flow of Resource<MatchDetail> for reactive UI updates with loading/error states
     */
    fun getMatchDetails(fixtureId: Int): Flow<com.Lyno.matchmindai.common.Resource<MatchDetail>>

    /**
     * Get fixtures for AI tool calls with optional search query filtering.
     * This function handles the "Kotlin Douane" filtering since API-Sports doesn't support search by name.
     * @param date The date in YYYY-MM-DD format
     * @param searchQuery Optional search query for team name filtering
     * @param leagueId Optional league ID for filtering
     * @param live Whether to fetch live matches
     * @return JSON string with simplified fixture data for AI consumption
     */
    suspend fun getFixturesForTool(date: String, searchQuery: String?, leagueId: Int?, live: Boolean): String

    /**
     * Get comprehensive match analysis context by aggregating data from multiple sources in parallel.
     * This serves as the 'Single Source of Truth' for LLM prompts.
     * @param fixtureId The unique identifier of the fixture
     * @return MatchContext containing all aggregated data
     */
    suspend fun getMatchAnalysisContext(fixtureId: Int): MatchContext

    /**
     * Stream chat completions from DeepSeek R1 (reasoner) model.
     * This method supports real-time streaming of reasoning content and final answers.
     *
     * @param apiKey The user's DeepSeek API key
     * @param request The chat completion request
     * @return Flow of ChatStreamUpdate objects containing reasoning and content deltas
     */
    suspend fun streamChat(
        apiKey: String,
        request: com.Lyno.matchmindai.data.dto.DeepSeekRequest
    ): Flow<ChatStreamUpdate>

    /**
     * Perform deep analysis using DeepSeek R1 with streaming support.
     * This method automatically routes R1 requests to streaming and regular requests to standard API.
     *
     * @param apiKey The user's DeepSeek API key
     * @param matchContext The comprehensive match context for analysis
     * @param userQuery The specific user question about the match
     * @param useStreaming Whether to use streaming (true for R1, false for regular models)
     * @return Flow of ChatStreamUpdate objects for streaming, or null for regular response
     */
    suspend fun performDeepAnalysisWithStreaming(
        apiKey: String,
        matchContext: com.Lyno.matchmindai.data.dto.MatchContextDto,
        userQuery: String,
        useStreaming: Boolean = true
    ): Flow<ChatStreamUpdate>?

    /**
     * Process a Prophet Module query for advanced match analysis.
     * This uses the Phase 3 "Prophet Module" with "Anchor & Adjust" strategy.
     * 
     * @param query User query like "Predict Feyenoord vs Ajax"
     * @return Result containing either an AgentResponse with Prophet analysis or an error
     */
    suspend fun processProphetQuery(query: String): Result<AgentResponse>

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
    suspend fun getBestOddsForTool(
        date: String? = null,
        leagueId: Int? = null,
        teamName: String? = null,
        limit: Int = 10
    ): String

    /**
     * Get injuries for a specific fixture.
     * Maps API response to domain Injury models.
     * 
     * @param fixtureId The ID of the fixture
     * @return Result containing either a list of Injury models or an error
     */
    suspend fun getInjuries(fixtureId: Int): Result<List<Injury>>

    /**
     * Get predictions for a specific fixture.
     * Maps API response to domain MatchPredictionData model.
     * 
     * @param fixtureId The ID of the fixture
     * @return Result containing either a MatchPredictionData model or an error
     */
    suspend fun getPredictions(fixtureId: Int): Result<MatchPredictionData?>

    /**
     * Get odds for a specific fixture.
     * Maps API response to domain OddsData model.
     * 
     * @param fixtureId The ID of the fixture
     * @return Result containing either an OddsData model or an error
     */
    suspend fun getOdds(fixtureId: Int): Result<OddsData?>

    /**
     * Clear all in-memory caches for predictions, injuries, odds, and match details.
     * This forces the next API calls to fetch fresh data instead of using cached data.
     * 
     * @return Result indicating success or failure
     */
    suspend fun clearCache(): Result<Unit>
}
