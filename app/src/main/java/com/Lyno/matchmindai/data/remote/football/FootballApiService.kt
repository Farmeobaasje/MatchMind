package com.Lyno.matchmindai.data.remote.football

import com.Lyno.matchmindai.data.dto.football.FixtureResponse
import com.Lyno.matchmindai.data.dto.football.FixtureItemDto
import com.Lyno.matchmindai.data.dto.football.LiveScoresResponse
import com.Lyno.matchmindai.data.dto.football.MatchDetailsResponse
import com.Lyno.matchmindai.data.remote.dto.StandingsResponse
import com.Lyno.matchmindai.data.dto.football.LeagueResponse
import com.Lyno.matchmindai.data.dto.football.LeagueDiscoveryResponse
import com.Lyno.matchmindai.data.dto.football.PredictionsResponse
import com.Lyno.matchmindai.data.dto.football.OddsResponseDto
import com.Lyno.matchmindai.data.dto.football.LiveOddsResponseDto
import com.Lyno.matchmindai.data.dto.football.RateLimitErrorResponse
import com.Lyno.matchmindai.data.dto.football.InjuriesResponseDto
import com.Lyno.matchmindai.data.dto.football.FixtureStatisticsResponse
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.HttpStatusCode
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import android.util.Log

/**
 * Service for interacting with the API-SPORTS Football API V3 (Direct Subscription).
 * This service handles all football data endpoints with enhanced error handling
 * and support for the coverage object in league responses.
 * 
 * INTELLIGENCE ENGINE UPGRADE - PHASE 1:
 * - Added typed Predictions endpoint with DTO mapping
 * - Added Leagues endpoint with coverage object parsing
 * - Enhanced error handling with specific exception types
 * - Support for status-based fallback logic (PST, SUSP, CANC)
 * 
 * The API-SPORTS key is passed via HTTP client interceptor headers.
 */
class FootballApiService(
    private val client: HttpClient
) {
    companion object {
        private const val BASE_URL = "https://v3.football.api-sports.io/"
        private const val FIXTURES_ENDPOINT = "fixtures"
        private const val LEAGUES_ENDPOINT = "leagues"
        private const val STANDINGS_ENDPOINT = "standings"
        private const val PREDICTIONS_ENDPOINT = "predictions"
        private const val INJURIES_ENDPOINT = "injuries"
        private const val ODDS_ENDPOINT = "odds"
        private const val ODDS_LIVE_ENDPOINT = "odds/live"
        private const val STATISTICS_ENDPOINT = "fixtures/statistics"
        private const val DEFAULT_TIMEZONE = "Europe/Amsterdam"
    }
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true  // CRITICAL: Handles array vs object mismatches
        encodeDefaults = false
    }
    
    /**
     * Check if the response is a rate limit error and throw appropriate exception.
     * 
     * When API-SPORTS rate limit is exceeded, it returns:
     * {
     *   "errors": {"rateLimit": "Too many requests..."},
     *   "parameters": []  // Note: array instead of object!
     * }
     * 
     * This causes JsonConvertException if we try to parse it as normal response.
     */
    private suspend fun checkForRateLimitError(response: HttpResponse) {
        // Check HTTP status code first
        if (response.status == HttpStatusCode.TooManyRequests) {
            throw FootballApiException(
                "API rate limit exceeded (HTTP 429)",
                FootballApiErrorType.RATE_LIMIT_ERROR
            )
        }
        
        // Check for 403 Forbidden (missing or invalid API key)
        if (response.status == HttpStatusCode.Forbidden) {
            throw com.Lyno.matchmindai.data.repository.ApiKeyMissingException(
                "API-Sports key is missing or invalid. Please set your API key in settings."
            )
        }
        
        // Try to parse as rate limit error response
        try {
            val responseText = response.bodyAsText()
            val rateLimitError = json.decodeFromString<RateLimitErrorResponse>(responseText)
            
            if (rateLimitError.isRateLimitError) {
                throw FootballApiException(
                    "API rate limit exceeded: ${rateLimitError.rateLimitMessage}",
                    FootballApiErrorType.RATE_LIMIT_ERROR
                )
            }
        } catch (e: Exception) {
            // Not a rate limit error, continue with normal parsing
        }
    }
    
    /**
     * Execute API request with rate limit error checking.
     * Uses explicit JSON decoding to avoid generic type deserialization issues.
     */
    private suspend inline fun <reified T> executeRequest(
        url: String,
        block: HttpRequestBuilder.() -> Unit
    ): T {
        val response = client.get(url) {
            contentType(ContentType.Application.Json)
            block()
        }
        
        // Check for rate limit errors before parsing
        checkForRateLimitError(response)
        
        if (response.status != HttpStatusCode.OK) {
            throw FootballApiException("API request failed with status: ${response.status}")
        }
        
        // Explicit JSON decoding to avoid "Kotlin reflection is not available" error
        val responseBody = response.body<String>()
        return json.decodeFromString<T>(responseBody)
    }

    /**
     * Fetch fixtures for a specific date.
     * Headers are automatically injected by the HTTP client interceptor.
     *
     * @param date The date in format "YYYY-MM-DD"
     * @param timezone The timezone for the fixtures (default: "Europe/Amsterdam")
     * @return List of fixture DTOs
     * @throws FootballApiException if the API request fails
     */
    suspend fun getFixturesByDate(
        date: String,
        timezone: String = DEFAULT_TIMEZONE
    ): List<FixtureItemDto> {
        val response = client.get("${BASE_URL}${FIXTURES_ENDPOINT}") {
            contentType(ContentType.Application.Json)
            parameter("date", date)
            parameter("timezone", timezone)
        }
        
        // Check for rate limit and API key errors
        checkForRateLimitError(response)
        
        if (response.status != HttpStatusCode.OK) {
            throw FootballApiException("API request failed with status: ${response.status}")
        }
        
        // Explicit JSON decoding to avoid "Kotlin reflection is not available" error
        val responseBody = response.body<String>()
        val fixtureResponse = json.decodeFromString<FixtureResponse>(responseBody)
        return fixtureResponse.response
    }

    /**
     * Fetch fixtures for a date range (from-to) to batch multiple API calls.
     * This is more efficient than making separate calls for each date.
     *
     * @param from Start date in format "YYYY-MM-DD"
     * @param to End date in format "YYYY-MM-DD"
     * @param timezone The timezone for the fixtures (default: "Europe/Amsterdam")
     * @return List of fixture DTOs for the specified date range
     * @throws FootballApiException if the API request fails
     */
    suspend fun getFixturesByDateRange(
        from: String,
        to: String,
        timezone: String = DEFAULT_TIMEZONE
    ): List<FixtureItemDto> {
        val response = client.get("${BASE_URL}${FIXTURES_ENDPOINT}") {
            contentType(ContentType.Application.Json)
            parameter("from", from)
            parameter("to", to)
            parameter("timezone", timezone)
        }
        
        // Check for rate limit and API key errors
        checkForRateLimitError(response)
        
        if (response.status != HttpStatusCode.OK) {
            throw FootballApiException("API request failed with status: ${response.status}")
        }
        
        // Explicit JSON decoding to avoid "Kotlin reflection is not available" error
        val responseBody = response.body<String>()
        val fixtureResponse = json.decodeFromString<FixtureResponse>(responseBody)
        return fixtureResponse.response
    }

    /**
     * Fetch fixtures for a specific team within a date range.
     *
     * @param teamId The unique identifier of the team
     * @param season The season year (e.g., 2025), or null to omit season filter
     * @param from Start date in format "YYYY-MM-DD"
     * @param to End date in format "YYYY-MM-DD"
     * @param timezone The timezone for the fixtures (default: "Europe/Amsterdam")
     * @return List of fixture DTOs for the specified team
     * @throws FootballApiException if the API request fails
     */
    suspend fun getFixturesByTeam(
        teamId: Int,
        season: Int? = 2025,
        from: String,
        to: String,
        timezone: String = DEFAULT_TIMEZONE
    ): List<FixtureItemDto> {
        val response = client.get("${BASE_URL}${FIXTURES_ENDPOINT}") {
            contentType(ContentType.Application.Json)
            parameter("team", teamId)
            if (season != null) {
                parameter("season", season)
            }
            parameter("from", from)
            parameter("to", to)
            parameter("timezone", timezone)
        }
        
        // Check for rate limit and API key errors
        checkForRateLimitError(response)
        
        if (response.status != HttpStatusCode.OK) {
            throw FootballApiException("API request failed with status: ${response.status}")
        }
        
        // Explicit JSON decoding to avoid "Kotlin reflection is not available" error
        val responseBody = response.body<String>()
        Log.d("FootballApiService", "Raw response for fixtures by team $teamId: $responseBody")
        
        val apiResponse = json.decodeFromString<com.Lyno.matchmindai.data.dto.ApiSportsResponse<List<FixtureItemDto>>>(responseBody)
        return apiResponse.response
    }

    /**
     * Fetch the last X fixtures for a specific team (fallback for Dixon-Coles predictions).
     * This is used when league-specific data is insufficient (e.g., cup competitions).
     *
     * @param teamId The unique identifier of the team
     * @param count Number of last fixtures to fetch (e.g., 15)
     * @param status Optional status filter (e.g., "FT" for finished matches)
     * @param timezone The timezone for the fixtures (default: "Europe/Amsterdam")
     * @return List of fixture DTOs for the specified team
     * @throws FootballApiException if the API request fails
     */
    suspend fun getLastFixturesForTeam(
        teamId: Int,
        count: Int = 15,
        status: String? = "FT",
        timezone: String = DEFAULT_TIMEZONE
    ): List<FixtureItemDto> {
        val response = client.get("${BASE_URL}${FIXTURES_ENDPOINT}") {
            contentType(ContentType.Application.Json)
            parameter("team", teamId)
            parameter("last", count)
            status?.let { parameter("status", it) }
            parameter("timezone", timezone)
        }
        
        if (response.status != HttpStatusCode.OK) {
            throw FootballApiException("API request failed with status: ${response.status}")
        }
        
        // Explicit JSON decoding to avoid "Kotlin reflection is not available" error
        val responseBody = response.body<String>()
        val fixtureResponse = json.decodeFromString<FixtureResponse>(responseBody)
        return fixtureResponse.response
    }

    /**
     * Fetch fixtures for a specific league and season.
     * Used for Dixon-Coles prediction model training data.
     *
     * @param leagueId The unique identifier of the league
     * @param season The season year (e.g., 2025)
     * @param status Optional status filter (e.g., "FT" for finished matches)
     * @param timezone The timezone for the fixtures (default: "Europe/Amsterdam")
     * @return List of fixture DTOs for the specified league and season
     * @throws FootballApiException if the API request fails
     */
    suspend fun getFixturesByLeague(
        leagueId: Int,
        season: Int,
        status: String? = null,
        timezone: String = DEFAULT_TIMEZONE
    ): List<FixtureItemDto> {
        val response = client.get("${BASE_URL}${FIXTURES_ENDPOINT}") {
            contentType(ContentType.Application.Json)
            parameter("league", leagueId)
            parameter("season", season)
            status?.let { parameter("status", it) }
            parameter("timezone", timezone)
        }
        
        if (response.status != HttpStatusCode.OK) {
            throw FootballApiException("API request failed with status: ${response.status}")
        }
        
        // Explicit JSON decoding to avoid "Kotlin reflection is not available" error
        val responseBody = response.body<String>()
        val fixtureResponse = json.decodeFromString<FixtureResponse>(responseBody)
        return fixtureResponse.response
    }

    /**
     * Fetch leagues with current=true parameter to get active leagues.
     * This endpoint includes the critical coverage object which indicates
     * what data endpoints are available per league.
     *
     * @param current Whether to fetch only current leagues (default: true)
     * @return LeagueDiscoveryResponse with coverage information
     * @throws FootballApiException if the API request fails
     */
    suspend fun getLeagues(
        current: Boolean = true
    ): LeagueDiscoveryResponse {
        val response = client.get("${BASE_URL}${LEAGUES_ENDPOINT}") {
            contentType(ContentType.Application.Json)
            parameter("current", current.toString())
        }
        
        if (response.status != HttpStatusCode.OK) {
            throw FootballApiException("API request failed with status: ${response.status}")
        }
        
        // Explicit JSON decoding to avoid "Kotlin reflection is not available" error
        val responseBody = response.body<String>()
        return json.decodeFromString<LeagueDiscoveryResponse>(responseBody)
    }

    /**
     * Fetch leagues for a specific team in a given season.
     * This is used for dynamic league detection (e.g., Eredivisie vs Eerste Divisie).
     *
     * @param teamId The unique identifier of the team
     * @param season The season year (e.g., 2025)
     * @return JSON response as JsonElement
     * @throws FootballApiException if the API request fails
     */
    suspend fun getTeamLeagues(
        teamId: Int,
        season: Int = 2025
    ): JsonElement {
        val response = client.get("${BASE_URL}${LEAGUES_ENDPOINT}") {
            contentType(ContentType.Application.Json)
            parameter("team", teamId)
            parameter("season", season)
        }
        
        if (response.status != HttpStatusCode.OK) {
            throw FootballApiException("API request failed with status: ${response.status}")
        }
        
        // Explicit JSON decoding to avoid "Kotlin reflection is not available" error
        val responseBody = response.body<String>()
        return json.decodeFromString<JsonElement>(responseBody)
    }

    /**
     * Fetch live fixtures with goals information.
     *
     * @param timezone The timezone for fixtures (default: "Europe/Amsterdam")
     * @return List of live match DTOs with goals
     * @throws FootballApiException if API request fails
     */
    suspend fun getLiveFixtures(
        timezone: String = DEFAULT_TIMEZONE
    ): List<com.Lyno.matchmindai.data.dto.football.LiveMatchDto> {
        val response = client.get("${BASE_URL}${FIXTURES_ENDPOINT}") {
            contentType(ContentType.Application.Json)
            parameter("live", "all")
            parameter("timezone", timezone)
        }
        
        // Check for rate limit and API key errors
        checkForRateLimitError(response)
        
        if (response.status != HttpStatusCode.OK) {
            throw FootballApiException("API request failed with status: ${response.status}")
        }
        
        // Explicit JSON decoding to avoid "Kotlin reflection is not available" error
        val responseBody = response.body<String>()
        val liveScoresResponse = json.decodeFromString<LiveScoresResponse>(responseBody)
        return liveScoresResponse.response
    }

    /**
     * Fetch detailed match information including events, lineups, statistics, and players.
     *
     * @param fixtureId The unique identifier of the fixture
     * @return Detailed match information
     * @throws FootballApiException if the API request fails
     */
    suspend fun getMatchDetails(
        fixtureId: Int
    ): MatchDetailsResponse {
        val response = client.get("${BASE_URL}${FIXTURES_ENDPOINT}") {
            contentType(ContentType.Application.Json)
            parameter("id", fixtureId)
        }
        
        if (response.status != HttpStatusCode.OK) {
            throw FootballApiException("API request failed with status: ${response.status}")
        }
        
        // Explicit JSON decoding to avoid "Kotlin reflection is not available" error
        val responseBody = response.body<String>()
        return json.decodeFromString<MatchDetailsResponse>(responseBody)
    }

    /**
     * Fetch standings for a specific league and season.
     * Handles 204 No Content responses gracefully by throwing a specific exception.
     *
     * @param leagueId The unique identifier of the league
     * @param season The season year (e.g., 2024)
     * @return Standings information
     * @throws FootballApiException if the API request fails, including 204 No Content
     */
    suspend fun getStandings(
        leagueId: Int,
        season: Int
    ): StandingsResponse {
        val response = client.get("${BASE_URL}${STANDINGS_ENDPOINT}") {
            contentType(ContentType.Application.Json)
            parameter("league", leagueId)
            parameter("season", season)
        }
        
        // Log HTTP status for debugging
        Log.d("FootballApiService", "Standings API call: league=$leagueId, season=$season, status=${response.status}")
        
        // Handle 204 No Content specifically
        if (response.status == HttpStatusCode.NoContent) {
            Log.w("FootballApiService", "API returned 204 No Content for league $leagueId, season $season")
            throw FootballApiException(
                "API returned 204 No Content - No standings data available for this season",
                FootballApiErrorType.DATA_ERROR
            )
        }
        
        // Handle other non-OK status codes
        if (response.status != HttpStatusCode.OK) {
            throw FootballApiException("API request failed with status: ${response.status}")
        }
        
        // Explicit JSON decoding to avoid "Kotlin reflection is not available" error
        val responseBody = response.body<String>()
        return json.decodeFromString<StandingsResponse>(responseBody)
    }

    /**
     * Fetch predictions for a specific fixture with typed response.
     * This endpoint provides winning percentages, advice, and head-to-head statistics
     * which are crucial for the AI analysis logic.
     *
     * @param fixtureId The unique identifier of the fixture
     * @return PredictionsResponse with structured prediction data
     * @throws FootballApiException if the API request fails
     */
    suspend fun getPredictions(
        fixtureId: Int
    ): PredictionsResponse {
        val response = client.get("${BASE_URL}${PREDICTIONS_ENDPOINT}") {
            contentType(ContentType.Application.Json)
            parameter("fixture", fixtureId)
        }
        
        if (response.status != HttpStatusCode.OK) {
            throw FootballApiException("API request failed with status: ${response.status}")
        }
        
        // Explicit JSON decoding to avoid "Kotlin reflection is not available" error
        val responseBody = response.body<String>()
        return json.decodeFromString<PredictionsResponse>(responseBody)
    }

    /**
     * Fetch predictions as JsonElement (legacy method for backward compatibility).
     *
     * @param fixtureId The unique identifier of the fixture
     * @return Predictions data as JsonElement
     * @throws FootballApiException if the API request fails
     */
    suspend fun getPredictionsAsJson(
        fixtureId: Int
    ): JsonElement {
        val response = client.get("${BASE_URL}${PREDICTIONS_ENDPOINT}") {
            contentType(ContentType.Application.Json)
            parameter("fixture", fixtureId)
        }
        
        if (response.status != HttpStatusCode.OK) {
            throw FootballApiException("API request failed with status: ${response.status}")
        }
        
        // Explicit JSON decoding to avoid "Kotlin reflection is not available" error
        val responseBody = response.body<String>()
        return json.decodeFromString<JsonElement>(responseBody)
    }

    /**
     * Fetch injury information for a specific fixture.
     *
     * @param fixtureId The unique identifier of the fixture
     * @return Injuries data as JsonElement
     * @throws FootballApiException if the API request fails
     */
    suspend fun getInjuries(
        fixtureId: Int
    ): JsonElement {
        val response = client.get("${BASE_URL}${INJURIES_ENDPOINT}") {
            contentType(ContentType.Application.Json)
            parameter("fixture", fixtureId)
        }
        
        if (response.status != HttpStatusCode.OK) {
            throw FootballApiException("API request failed with status: ${response.status}")
        }
        
        // Explicit JSON decoding to avoid "Kotlin reflection is not available" error
        val responseBody = response.body<String>()
        return json.decodeFromString<JsonElement>(responseBody)
    }

    /**
     * Fetch injury information for a specific fixture with typed response.
     *
     * @param fixtureId The unique identifier of the fixture
     * @return InjuriesResponseDto with structured injury data
     * @throws FootballApiException if the API request fails
     */
    suspend fun getInjuriesTyped(
        fixtureId: Int
    ): InjuriesResponseDto {
        val response = client.get("${BASE_URL}${INJURIES_ENDPOINT}") {
            contentType(ContentType.Application.Json)
            parameter("fixture", fixtureId)
        }
        
        if (response.status != HttpStatusCode.OK) {
            throw FootballApiException("API request failed with status: ${response.status}")
        }
        
        // Explicit JSON decoding to avoid "Kotlin reflection is not available" error
        val responseBody = response.body<String>()
        return json.decodeFromString<InjuriesResponseDto>(responseBody)
    }

    /**
     * Fetch pre-match odds for a specific fixture with typed response.
     * This endpoint provides betting odds from various bookmakers.
     * Update Frequency: Every 3 hours, Recommended Calls: 1 call every 3 hours.
     *
     * @param fixtureId The unique identifier of the fixture
     * @return OddsResponseDto with structured odds data
     * @throws FootballApiException if the API request fails
     */
    suspend fun getOdds(
        fixtureId: Int
    ): OddsResponseDto {
        val response = client.get("${BASE_URL}${ODDS_ENDPOINT}") {
            contentType(ContentType.Application.Json)
            parameter("fixture", fixtureId)
        }
        
        if (response.status != HttpStatusCode.OK) {
            throw FootballApiException("API request failed with status: ${response.status}")
        }
        
        // Explicit JSON decoding to avoid "Kotlin reflection is not available" error
        val responseBody = response.body<String>()
        return json.decodeFromString<OddsResponseDto>(responseBody)
    }

    /**
     * Fetch pre-match odds for a specific fixture as JsonElement (legacy method).
     *
     * @param fixtureId The unique identifier of the fixture
     * @return Odds data as JsonElement
     * @throws FootballApiException if the API request fails
     */
    suspend fun getOddsAsJson(
        fixtureId: Int
    ): JsonElement {
        val response = client.get("${BASE_URL}${ODDS_ENDPOINT}") {
            contentType(ContentType.Application.Json)
            parameter("fixture", fixtureId)
        }
        
        if (response.status != HttpStatusCode.OK) {
            throw FootballApiException("API request failed with status: ${response.status}")
        }
        
        // Explicit JSON decoding to avoid "Kotlin reflection is not available" error
        val responseBody = response.body<String>()
        return json.decodeFromString<JsonElement>(responseBody)
    }

    /**
     * Fetch in-play (live) odds for fixtures in progress.
     * Fixtures are added between 15 and 5 minutes before start and removed 5-20 minutes after.
     * Update Frequency: Every 5 seconds.
     *
     * @param fixtureId Optional fixture ID to filter results
     * @param leagueId Optional league ID to filter results
     * @param betId Optional bet ID to filter results
     * @return LiveOddsResponseDto with structured live odds data
     * @throws FootballApiException if the API request fails
     */
    suspend fun getLiveOdds(
        fixtureId: Int? = null,
        leagueId: Int? = null,
        betId: Int? = null
    ): LiveOddsResponseDto {
        val response = client.get("${BASE_URL}${ODDS_LIVE_ENDPOINT}") {
            contentType(ContentType.Application.Json)
            fixtureId?.let { parameter("fixture", it) }
            leagueId?.let { parameter("league", it) }
            betId?.let { parameter("bet", it) }
        }
        
        if (response.status != HttpStatusCode.OK) {
            throw FootballApiException("API request failed with status: ${response.status}")
        }
        
        // Explicit JSON decoding to avoid "Kotlin reflection is not available" error
        val responseBody = response.body<String>()
        return json.decodeFromString<LiveOddsResponseDto>(responseBody)
    }

    /**
     * Fetch odds for a specific date.
     * Useful for getting odds for multiple fixtures on a given day.
     *
     * @param date The date in format "YYYY-MM-DD"
     * @param leagueId Optional league ID to filter results
     * @param timezone Optional timezone (default: "Europe/Amsterdam")
     * @return OddsResponseDto with structured odds data
     * @throws FootballApiException if the API request fails
     */
    suspend fun getOddsByDate(
        date: String,
        leagueId: Int? = null,
        timezone: String = DEFAULT_TIMEZONE
    ): OddsResponseDto {
        val response = client.get("${BASE_URL}${ODDS_ENDPOINT}") {
            contentType(ContentType.Application.Json)
            parameter("date", date)
            parameter("timezone", timezone)
            leagueId?.let { parameter("league", it) }
        }
        
        if (response.status != HttpStatusCode.OK) {
            throw FootballApiException("API request failed with status: ${response.status}")
        }
        
        // Explicit JSON decoding to avoid "Kotlin reflection is not available" error
        val responseBody = response.body<String>()
        return json.decodeFromString<OddsResponseDto>(responseBody)
    }

    /**
     * Fetch odds for a specific league and season.
     * Useful for getting odds for all fixtures in a league.
     *
     * @param leagueId The league ID
     * @param season The season year (e.g., 2025)
     * @param bookmakerId Optional bookmaker ID to filter results
     * @param betId Optional bet ID to filter results
     * @return OddsResponseDto with structured odds data
     * @throws FootballApiException if the API request fails
     */
    suspend fun getOddsByLeague(
        leagueId: Int,
        season: Int,
        bookmakerId: Int? = null,
        betId: Int? = null
    ): OddsResponseDto {
        val response = client.get("${BASE_URL}${ODDS_ENDPOINT}") {
            contentType(ContentType.Application.Json)
            parameter("league", leagueId)
            parameter("season", season)
            bookmakerId?.let { parameter("bookmaker", it) }
            betId?.let { parameter("bet", it) }
        }
        
        if (response.status != HttpStatusCode.OK) {
            throw FootballApiException("API request failed with status: ${response.status}")
        }
        
        // Explicit JSON decoding to avoid "Kotlin reflection is not available" error
        val responseBody = response.body<String>()
        return json.decodeFromString<OddsResponseDto>(responseBody)
    }

    /**
     * Fetch statistics for a specific fixture.
     * This endpoint provides detailed match statistics including Expected Goals (xG),
     * which is critical for the EnhancedScorePredictor model.
     * 
     * Available statistics include:
     * - Expected Goals (xG) - Most important for our predictions
     * - Shots on Goal, Shots off Goal, Total Shots
     * - Ball Possession, Total passes, Passes accurate
     * - Fouls, Corner Kicks, Offsides
     * - Yellow Cards, Red Cards, Goalkeeper Saves
     * 
     * Update Frequency: Every minute for in-progress fixtures, otherwise 1 call per day
     * Recommended Calls: 1 call every minute for teams with fixtures in progress
     *
     * @param fixtureId The unique identifier of the fixture
     * @param teamId Optional team ID to filter statistics for a specific team
     * @param type Optional statistic type to filter (e.g., "Expected Goals")
     * @param half Whether to include halftime statistics (default: false)
     * @return FixtureStatisticsResponse with team statistics
     * @throws FootballApiException if the API request fails
     */
    suspend fun getFixtureStatistics(
        fixtureId: Int,
        teamId: Int? = null,
        type: String? = null,
        half: Boolean = false
    ): FixtureStatisticsResponse {
        val response = client.get("${BASE_URL}${STATISTICS_ENDPOINT}") {
            contentType(ContentType.Application.Json)
            parameter("fixture", fixtureId)
            teamId?.let { parameter("team", it) }
            type?.let { parameter("type", it) }
            if (half) {
                parameter("half", "true")
            }
        }
        
        // Check for rate limit and API key errors
        checkForRateLimitError(response)
        
        if (response.status != HttpStatusCode.OK) {
            throw FootballApiException("API request failed with status: ${response.status}")
        }
        
        // Explicit JSON decoding to avoid "Kotlin reflection is not available" error
        val responseBody = response.body<String>()
        return json.decodeFromString<FixtureStatisticsResponse>(responseBody)
    }
}

/**
 * Custom exception for Football API errors with enhanced error types.
 */
class FootballApiException(
    message: String,
    val errorType: FootballApiErrorType = FootballApiErrorType.UNKNOWN
) : Exception(message)

/**
 * Enumeration of Football API error types for better error handling.
 */
enum class FootballApiErrorType {
    /** Network connectivity issues */
    NETWORK_ERROR,
    
    /** API rate limit exceeded (429) */
    RATE_LIMIT_ERROR,
    
    /** Authentication/authorization issues */
    AUTH_ERROR,
    
    /** Data not found or empty response */
    DATA_ERROR,
    
    /** Server-side errors (5xx) */
    SERVER_ERROR,
    
    /** Unknown error type */
    UNKNOWN
}
