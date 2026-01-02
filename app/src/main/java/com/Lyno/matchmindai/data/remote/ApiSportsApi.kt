package com.Lyno.matchmindai.data.remote

import android.util.Log
import com.Lyno.matchmindai.data.dto.ApiSportsResponse
import com.Lyno.matchmindai.data.dto.FixtureResponse
import com.Lyno.matchmindai.data.dto.TeamsSearchResponse
import com.Lyno.matchmindai.data.dto.football.FixtureLineupsResponse
import com.Lyno.matchmindai.data.dto.football.NewsItemResponse
import com.Lyno.matchmindai.data.dto.football.NewsResponse
import com.Lyno.matchmindai.data.dto.football.StandingsResponse
import com.Lyno.matchmindai.data.dto.football.TeamStatisticsResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Ktor-based HTTP client for API-SPORTS.
 * Implements dynamic authentication where the API key is passed as a parameter.
 */
class ApiSportsApi(private val client: HttpClient) {

    companion object {
        private const val BASE_URL = "https://v3.football.api-sports.io"
        private const val FIXTURES_ENDPOINT = "/fixtures"
        private const val TEAMS_ENDPOINT = "/teams"
        private const val API_KEY_HEADER = "x-apisports-key"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        prettyPrint = true
    }

    /**
     * Searches for teams by name.
     * @param apiKey The user's API-SPORTS API key
     * @param name The team name to search for
     * @param country Optional country filter
     * @return List of matching teams
     * @throws ApiSportsApiException if the request fails
     */
    suspend fun searchTeams(
        apiKey: String,
        name: String,
        country: String? = null
    ): List<TeamsSearchResponse> {
        return try {
            val response: HttpResponse = client.get("$BASE_URL$TEAMS_ENDPOINT") {
                parameter("search", name)
                country?.let { parameter("country", it) }
            }

            if (response.status.value in 200..299) {
                // Explicit JSON decoding to avoid generic type deserialization issues
                val responseBody = response.body<String>()
                Log.d("ApiSportsApi", "Raw response for team search '$name': $responseBody")
                
                val apiResponse = json.decodeFromString<ApiSportsResponse<List<TeamsSearchResponse>>>(responseBody)
                
                if (apiResponse.errors.isNotEmpty()) {
                    val errorMessage = apiResponse.errors
                        .mapNotNull { it.value }
                        .joinToString(", ")
                    throw ApiSportsApiException(
                        "API errors: $errorMessage",
                        response.status.value
                    )
                }
                apiResponse.response
            } else {
                throw ApiSportsApiException(
                    "API request failed with status ${response.status.value}",
                    response.status.value
                )
            }
        } catch (e: Exception) {
            Log.e("ApiSportsApi", "Error searching teams: ${e.message}", e)
            throw ApiSportsApiException(
                "Network error: ${e.message}",
                cause = e
            )
        }
    }

    /**
     * Gets fixtures for a specific team.
     * @param apiKey The user's API-SPORTS API key
     * @param teamId The team ID
     * @param season Optional season year (e.g., 2024)
     * @param status Optional fixture status filter
     * @return List of fixtures for the team
     * @throws ApiSportsApiException if the request fails
     */
    suspend fun getTeamFixtures(
        apiKey: String,
        teamId: Int,
        season: Int? = null,
        status: String? = null
    ): List<FixtureResponse> {
        return try {
            val response: HttpResponse = client.get("$BASE_URL$FIXTURES_ENDPOINT") {
                parameter("team", teamId)
                season?.let { parameter("season", it) }
                status?.let { parameter("status", it) }
                parameter("last", 10) // Get last 10 fixtures
            }

            if (response.status.value in 200..299) {
                // Explicit JSON decoding to avoid generic type deserialization issues
                val responseBody = response.body<String>()
                val apiResponse = json.decodeFromString<ApiSportsResponse<List<FixtureResponse>>>(responseBody)
                
                if (apiResponse.errors.isNotEmpty()) {
                    val errorMessage = apiResponse.errors
                        .mapNotNull { it.value }
                        .joinToString(", ")
                    throw ApiSportsApiException(
                        "API errors: $errorMessage",
                        response.status.value
                    )
                }
                apiResponse.response
            } else {
                throw ApiSportsApiException(
                    "API request failed with status ${response.status.value}",
                    response.status.value
                )
            }
        } catch (e: Exception) {
            Log.e("ApiSportsApi", "Error getting team fixtures: ${e.message}", e)
            throw ApiSportsApiException(
                "Network error: ${e.message}",
                cause = e
            )
        }
    }

    /**
     * Gets a specific fixture by ID.
     * @param apiKey The user's API-SPORTS API key
     * @param fixtureId The fixture ID
     * @return The fixture details
     * @throws ApiSportsApiException if the request fails
     */
    suspend fun getFixture(
        apiKey: String,
        fixtureId: Int
    ): FixtureResponse {
        // Validate API key before making request
        if (apiKey.isBlank()) {
            Log.w("ApiSportsApi", "API key is empty for getFixture request")
            throw ApiSportsApiException(
                "API key is empty",
                statusCode = 401
            )
        }

        return try {
            val response: HttpResponse = client.get("$BASE_URL$FIXTURES_ENDPOINT") {
                parameter("id", fixtureId)
            }

            if (response.status.value in 200..299) {
                // Explicit JSON decoding to avoid generic type deserialization issues
                val responseBody = response.body<String>()
                val apiResponse = json.decodeFromString<ApiSportsResponse<List<FixtureResponse>>>(responseBody)
                
                if (apiResponse.errors.isNotEmpty()) {
                    val errorMessage = apiResponse.errors
                        .mapNotNull { it.value }
                        .joinToString(", ")
                    throw ApiSportsApiException(
                        "API errors: $errorMessage",
                        response.status.value
                    )
                }
                apiResponse.response.firstOrNull() ?: throw ApiSportsApiException(
                    "Fixture not found",
                    response.status.value
                )
            } else {
                // For 403 errors, log as warning instead of error
                if (response.status.value == 403) {
                    Log.w("ApiSportsApi", "API access forbidden (403) for fixture $fixtureId. Check API key validity and rate limits.")
                } else {
                    Log.e("ApiSportsApi", "API request failed with status ${response.status.value} for fixture $fixtureId")
                }
                throw ApiSportsApiException(
                    "API request failed with status ${response.status.value}",
                    response.status.value
                )
            }
        } catch (e: Exception) {
            // Log as warning for authentication/network errors, not as critical errors
            if (e is ApiSportsApiException && e.statusCode == 403) {
                Log.w("ApiSportsApi", "Authentication failed for getFixture: ${e.message}")
            } else {
                Log.e("ApiSportsApi", "Error getting fixture: ${e.message}", e)
            }
            throw ApiSportsApiException(
                "Network error: ${e.message}",
                cause = e
            )
        }
    }

    /**
     * Gets head-to-head fixtures between two teams.
     * @param apiKey The user's API-SPORTS API key
     * @param homeTeamId The home team ID
     * @param awayTeamId The away team ID
     * @param last Optional number of previous meetings to return (default: 10)
     * @return List of head-to-head fixtures
     * @throws ApiSportsApiException if the request fails
     */
    suspend fun getHeadToHead(
        apiKey: String,
        homeTeamId: Int,
        awayTeamId: Int,
        last: Int = 10
    ): List<FixtureResponse> {
        return try {
            val response: HttpResponse = client.get("$BASE_URL$FIXTURES_ENDPOINT") {
                parameter("h2h", "$homeTeamId-$awayTeamId")
                parameter("last", last)
            }

            if (response.status.value in 200..299) {
                // Explicit JSON decoding to avoid generic type deserialization issues
                val responseBody = response.body<String>()
                val apiResponse = json.decodeFromString<ApiSportsResponse<List<FixtureResponse>>>(responseBody)
                
                if (apiResponse.errors.isNotEmpty()) {
                    val errorMessage = apiResponse.errors
                        .mapNotNull { it.value }
                        .joinToString(", ")
                    throw ApiSportsApiException(
                        "API errors: $errorMessage",
                        response.status.value
                    )
                }
                apiResponse.response
            } else {
                throw ApiSportsApiException(
                    "API request failed with status ${response.status.value}",
                    response.status.value
                )
            }
        } catch (e: Exception) {
            Log.e("ApiSportsApi", "Error getting head-to-head: ${e.message}", e)
            throw ApiSportsApiException(
                "Network error: ${e.message}",
                cause = e
            )
        }
    }

    /**
     * Gets team news for a specific team.
     * @param apiKey The user's API-SPORTS API key
     * @param teamId The team ID
     * @return List of news items for the team
     * @throws ApiSportsApiException if the request fails
     */
    suspend fun getTeamNews(
        apiKey: String,
        teamId: Int
    ): List<NewsItemResponse> {
        return try {
            val response: HttpResponse = client.get("$BASE_URL/v3/football/news") {
                parameter("team", teamId)
            }

            if (response.status.value in 200..299) {
                // Explicit JSON decoding to avoid generic type deserialization issues
                val responseBody = response.body<String>()
                val apiResponse = json.decodeFromString<ApiSportsResponse<NewsResponse>>(responseBody)
                
                if (apiResponse.errors.isNotEmpty()) {
                    val errorMessage = apiResponse.errors
                        .mapNotNull { it.value }
                        .joinToString(", ")
                    throw ApiSportsApiException(
                        "API errors: $errorMessage",
                        response.status.value
                    )
                }
                apiResponse.response.response
            } else {
                throw ApiSportsApiException(
                    "API request failed with status ${response.status.value}",
                    response.status.value
                )
            }
        } catch (e: Exception) {
            Log.e("ApiSportsApi", "Error getting team news: ${e.message}", e)
            throw ApiSportsApiException(
                "Network error: ${e.message}",
                cause = e
            )
        }
    }

    /**
     * Gets the next match for a specific team.
     * @param apiKey The user's API-SPORTS API key
     * @param teamId The team ID
     * @return The next fixture for the team, or null if no upcoming matches
     * @throws ApiSportsApiException if the request fails
     */
    suspend fun getNextMatch(
        apiKey: String,
        teamId: Int
    ): FixtureResponse? {
        return try {
            val response: HttpResponse = client.get("$BASE_URL$FIXTURES_ENDPOINT") {
                parameter("team", teamId)
                parameter("next", 1) // Get next match
            }

            if (response.status.value in 200..299) {
                // Explicit JSON decoding to avoid generic type deserialization issues
                val responseBody = response.body<String>()
                val apiResponse = json.decodeFromString<ApiSportsResponse<List<FixtureResponse>>>(responseBody)
                
                if (apiResponse.errors.isNotEmpty()) {
                    val errorMessage = apiResponse.errors
                        .mapNotNull { it.value }
                        .joinToString(", ")
                    throw ApiSportsApiException(
                        "API errors: $errorMessage",
                        response.status.value
                    )
                }
                apiResponse.response.firstOrNull()
            } else {
                throw ApiSportsApiException(
                    "API request failed with status ${response.status.value}",
                    response.status.value
                )
            }
        } catch (e: Exception) {
            Log.e("ApiSportsApi", "Error getting next match: ${e.message}", e)
            throw ApiSportsApiException(
                "Network error: ${e.message}",
                cause = e
            )
        }
    }

    /**
     * Gets league standings for a specific team.
     * @param apiKey The user's API-SPORTS API key
     * @param teamId The team ID
     * @return League standings with the team highlighted
     * @throws ApiSportsApiException if the request fails
     */
    suspend fun getLeagueStandings(
        apiKey: String,
        teamId: Int
    ): StandingsResponse {
        return try {
            val response: HttpResponse = client.get("$BASE_URL/v3/football/standings") {
                parameter("team", teamId)
            }

            if (response.status.value in 200..299) {
                // Explicit JSON decoding to avoid generic type deserialization issues
                val responseBody = response.body<String>()
                val apiResponse = json.decodeFromString<ApiSportsResponse<StandingsResponse>>(responseBody)
                
                if (apiResponse.errors.isNotEmpty()) {
                    val errorMessage = apiResponse.errors
                        .mapNotNull { it.value }
                        .joinToString(", ")
                    throw ApiSportsApiException(
                        "API errors: $errorMessage",
                        response.status.value
                    )
                }
                apiResponse.response
            } else {
                throw ApiSportsApiException(
                    "API request failed with status ${response.status.value}",
                    response.status.value
                )
            }
        } catch (e: Exception) {
            Log.e("ApiSportsApi", "Error getting league standings: ${e.message}", e)
            throw ApiSportsApiException(
                "Network error: ${e.message}",
                cause = e
            )
        }
    }

    /**
     * Gets last matches for a specific team (for fatigue calculation).
     * @param apiKey The user's API-SPORTS API key
     * @param teamId The team ID
     * @param count Number of last matches to retrieve (default: 5)
     * @return List of last matches for the team
     * @throws ApiSportsApiException if the request fails
     */
    suspend fun getLastMatches(
        apiKey: String,
        teamId: Int,
        count: Int = 5
    ): List<FixtureResponse> {
        // Validate API key before making request
        if (apiKey.isBlank()) {
            Log.w("ApiSportsApi", "API key is empty for getLastMatches request")
            throw ApiSportsApiException(
                "API key is empty",
                statusCode = 401
            )
        }

        return try {
            val response: HttpResponse = client.get("$BASE_URL$FIXTURES_ENDPOINT") {
                parameter("team", teamId)
                parameter("last", count)
            }

            if (response.status.value in 200..299) {
                // Explicit JSON decoding to avoid generic type deserialization issues
                val responseBody = response.body<String>()
                val apiResponse = json.decodeFromString<ApiSportsResponse<List<FixtureResponse>>>(responseBody)
                
                if (apiResponse.errors.isNotEmpty()) {
                    val errorMessage = apiResponse.errors
                        .mapNotNull { it.value }
                        .joinToString(", ")
                    throw ApiSportsApiException(
                        "API errors: $errorMessage",
                        response.status.value
                    )
                }
                apiResponse.response
            } else {
                // For 403 errors, log as warning instead of error
                if (response.status.value == 403) {
                    Log.w("ApiSportsApi", "API access forbidden (403) for team $teamId. Check API key validity and rate limits.")
                } else {
                    Log.e("ApiSportsApi", "API request failed with status ${response.status.value} for team $teamId")
                }
                throw ApiSportsApiException(
                    "API request failed with status ${response.status.value}",
                    response.status.value
                )
            }
        } catch (e: Exception) {
            // Log as warning for authentication/network errors, not as critical errors
            if (e is ApiSportsApiException && e.statusCode == 403) {
                Log.w("ApiSportsApi", "Authentication failed for getLastMatches: ${e.message}")
            } else {
                Log.e("ApiSportsApi", "Error getting last matches: ${e.message}", e)
            }
            throw ApiSportsApiException(
                "Network error: ${e.message}",
                cause = e
            )
        }
    }

    /**
     * Gets team tactics and statistics for style clash analysis.
     * @param apiKey The user's API-SPORTS API key
     * @param teamId The team ID
     * @param season The season year (e.g., 2024)
     * @return Team statistics including lineups, clean sheets, and goal timing
     * @throws ApiSportsApiException if the request fails
     */
    suspend fun getTeamTactics(
        apiKey: String,
        teamId: Int,
        season: Int
    ): TeamStatisticsResponse {
        return try {
            val response: HttpResponse = client.get("$BASE_URL/teams/statistics") {
                parameter("team", teamId)
                parameter("season", season)
                parameter("league", 39) // Default to Premier League, can be made parameter later
            }

            if (response.status.value in 200..299) {
                // Explicit JSON decoding to avoid generic type deserialization issues
                val responseBody = response.body<String>()
                val apiResponse = json.decodeFromString<ApiSportsResponse<TeamStatisticsResponse>>(responseBody)
                
                if (apiResponse.errors.isNotEmpty()) {
                    val errorMessage = apiResponse.errors
                        .mapNotNull { it.value }
                        .joinToString(", ")
                    throw ApiSportsApiException(
                        "API errors: $errorMessage",
                        response.status.value
                    )
                }
                apiResponse.response
            } else {
                throw ApiSportsApiException(
                    "API request failed with status ${response.status.value}",
                    response.status.value
                )
            }
        } catch (e: Exception) {
            Log.e("ApiSportsApi", "Error getting team tactics: ${e.message}", e)
            throw ApiSportsApiException(
                "Network error: ${e.message}",
                cause = e
            )
        }
    }

    /**
     * Gets fixture lineups for lineup strength analysis.
     * @param apiKey The user's API-SPORTS API key
     * @param fixtureId The fixture ID
     * @return Lineup information for both teams
     * @throws ApiSportsApiException if the request fails
     */
    suspend fun getFixtureLineups(
        apiKey: String,
        fixtureId: Int
    ): List<FixtureLineupsResponse>? {
        return try {
            val response: HttpResponse = client.get("$BASE_URL$FIXTURES_ENDPOINT/lineups") {
                parameter("fixture", fixtureId)
            }

            if (response.status.value in 200..299) {
                // Explicit JSON decoding to avoid generic type deserialization issues
                val responseBody = response.body<String>()
                Log.d("ApiSportsApi", "Raw lineups response: $responseBody")
                
                // API returns empty array [] for future matches, so we need to handle both cases
                if (responseBody.contains("\"response\":[]")) {
                    Log.d("ApiSportsApi", "Empty lineups array for fixture $fixtureId (future match)")
                    return null
                }
                
                // API returns array of FixtureLineupsResponse objects (one for each team)
                val apiResponse = json.decodeFromString<ApiSportsResponse<List<FixtureLineupsResponse>>>(responseBody)
                
                if (apiResponse.errors.isNotEmpty()) {
                    val errorMessage = apiResponse.errors
                        .mapNotNull { it.value }
                        .joinToString(", ")
                    throw ApiSportsApiException(
                        "API errors: $errorMessage",
                        response.status.value
                    )
                }
                apiResponse.response
            } else {
                throw ApiSportsApiException(
                    "API request failed with status ${response.status.value}",
                    response.status.value
                )
            }
        } catch (e: Exception) {
            Log.e("ApiSportsApi", "Error getting fixture lineups: ${e.message}", e)
            throw ApiSportsApiException(
                "Network error: ${e.message}",
                cause = e
            )
        }
    }

    /**
     * Gets player statistics for a team and season.
     * Endpoint: /players/statistics
     * @param apiKey The user's API-SPORTS API key
     * @param teamId The team ID
     * @param season The season year (e.g., 2024)
     * @param leagueId The league ID (default: 39 for Premier League)
     * @return List of player statistics
     * @throws ApiSportsApiException if the request fails
     */
    suspend fun getPlayerStatistics(
        apiKey: String,
        teamId: Int,
        season: Int,
        leagueId: Int = 39
    ): List<com.Lyno.matchmindai.data.dto.football.PlayerStatisticsDto> {
        return try {
            val response: HttpResponse = client.get("$BASE_URL/players/statistics") {
                parameter("team", teamId)
                parameter("season", season)
                parameter("league", leagueId)
            }

            if (response.status.value in 200..299) {
                val responseBody = response.body<String>()
                Log.d("ApiSportsApi", "Raw player statistics response: $responseBody")
                
                val apiResponse = json.decodeFromString<ApiSportsResponse<List<com.Lyno.matchmindai.data.dto.football.PlayerStatisticsDto>>>(responseBody)
                
                if (apiResponse.errors.isNotEmpty()) {
                    val errorMessage = apiResponse.errors
                        .mapNotNull { it.value }
                        .joinToString(", ")
                    throw ApiSportsApiException(
                        "API errors: $errorMessage",
                        response.status.value
                    )
                }
                apiResponse.response
            } else {
                throw ApiSportsApiException(
                    "API request failed with status ${response.status.value}",
                    response.status.value
                )
            }
        } catch (e: Exception) {
            Log.e("ApiSportsApi", "Error getting player statistics: ${e.message}", e)
            throw ApiSportsApiException(
                "Network error: ${e.message}",
                cause = e
            )
        }
    }

    /**
     * Gets top scorers for a league and season.
     * Endpoint: /players/topscorers
     * @param apiKey The user's API-SPORTS API key
     * @param leagueId The league ID (default: 39 for Premier League)
     * @param season The season year (e.g., 2024)
     * @return List of top scorers
     * @throws ApiSportsApiException if the request fails
     */
    suspend fun getTopScorers(
        apiKey: String,
        leagueId: Int = 39,
        season: Int
    ): List<com.Lyno.matchmindai.data.dto.football.TopScorerDto> {
        return try {
            val response: HttpResponse = client.get("$BASE_URL/players/topscorers") {
                parameter("league", leagueId)
                parameter("season", season)
            }

            if (response.status.value in 200..299) {
                val responseBody = response.body<String>()
                Log.d("ApiSportsApi", "Raw top scorers response: $responseBody")
                
                val apiResponse = json.decodeFromString<ApiSportsResponse<List<com.Lyno.matchmindai.data.dto.football.TopScorerDto>>>(responseBody)
                
                if (apiResponse.errors.isNotEmpty()) {
                    val errorMessage = apiResponse.errors
                        .mapNotNull { it.value }
                        .joinToString(", ")
                    throw ApiSportsApiException(
                        "API errors: $errorMessage",
                        response.status.value
                    )
                }
                apiResponse.response
            } else {
                throw ApiSportsApiException(
                    "API request failed with status ${response.status.value}",
                    response.status.value
                )
            }
        } catch (e: Exception) {
            Log.e("ApiSportsApi", "Error getting top scorers: ${e.message}", e)
            throw ApiSportsApiException(
                "Network error: ${e.message}",
                cause = e
            )
        }
    }

    /**
     * Gets player injuries for a team.
     * Endpoint: /injuries
     * @param apiKey The user's API-SPORTS API key
     * @param teamId The team ID
     * @param season The season year (e.g., 2024)
     * @return List of player injuries
     * @throws ApiSportsApiException if the request fails
     */
    suspend fun getPlayerInjuries(
        apiKey: String,
        teamId: Int,
        season: Int
    ): List<com.Lyno.matchmindai.data.dto.football.PlayerInjuryDto> {
        return try {
            val response: HttpResponse = client.get("$BASE_URL/injuries") {
                parameter("team", teamId)
                parameter("season", season)
            }

            if (response.status.value in 200..299) {
                val responseBody = response.body<String>()
                Log.d("ApiSportsApi", "Raw injuries response: $responseBody")
                
                val apiResponse = json.decodeFromString<ApiSportsResponse<List<com.Lyno.matchmindai.data.dto.football.PlayerInjuryDto>>>(responseBody)
                
                if (apiResponse.errors.isNotEmpty()) {
                    val errorMessage = apiResponse.errors
                        .mapNotNull { it.value }
                        .joinToString(", ")
                    throw ApiSportsApiException(
                        "API errors: $errorMessage",
                        response.status.value
                    )
                }
                apiResponse.response
            } else {
                throw ApiSportsApiException(
                    "API request failed with status ${response.status.value}",
                    response.status.value
                )
            }
        } catch (e: Exception) {
            Log.e("ApiSportsApi", "Error getting player injuries: ${e.message}", e)
            throw ApiSportsApiException(
                "Network error: ${e.message}",
                cause = e
            )
        }
    }
}

/**
 * Custom exception for API-SPORTS API errors.
 */
class ApiSportsApiException(
    message: String,
    val statusCode: Int? = null,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Extension functions for graceful degradation and safe API calls.
 */
suspend fun <T> ApiSportsApi.safeApiCall(
    apiCall: suspend () -> T,
    fallbackValue: T? = null,
    apiKey: String? = null
): Result<T> {
    return try {
        val result = apiCall()
        Result.success(result)
    } catch (e: ApiSportsApiException) {
        when (e.statusCode) {
            403 -> {
                Log.e("ApiSportsApi", "API authentication failed (403). Check API key validity.", e)
                // Try with single API key if provided
                if (apiKey != null) {
                    try {
                        Log.d("ApiSportsApi", "Retrying with single API key...")
                        // Note: The client already adds the header via defaultRequest
                        // We just need to ensure the API key is valid
                        val result = apiCall()
                        Result.success(result)
                    } catch (e2: Exception) {
                        Log.e("ApiSportsApi", "API call failed even with single key", e2)
                        if (fallbackValue != null) {
                            Result.success(fallbackValue)
                        } else {
                            Result.failure(e2)
                        }
                    }
                } else {
                    if (fallbackValue != null) {
                        Result.success(fallbackValue)
                    } else {
                        Result.failure(e)
                    }
                }
            }
            429 -> {
                Log.w("ApiSportsApi", "API rate limited (429). Consider implementing rate limiting.", e)
                if (fallbackValue != null) {
                    Result.success(fallbackValue)
                } else {
                    Result.failure(e)
                }
            }
            else -> {
                Log.e("ApiSportsApi", "API call failed with status ${e.statusCode}", e)
                if (fallbackValue != null) {
                    Result.success(fallbackValue)
                } else {
                    Result.failure(e)
                }
            }
        }
    } catch (e: Exception) {
        Log.e("ApiSportsApi", "Unexpected API error", e)
        if (fallbackValue != null) {
            Result.success(fallbackValue)
        } else {
            Result.failure(e)
        }
    }
}

/**
 * Safe wrapper for getLastMatches with graceful degradation.
 */
suspend fun ApiSportsApi.safeGetLastMatches(
    apiKey: String,
    teamId: Int,
    count: Int = 5
): Result<List<FixtureResponse>> {
    return safeApiCall(
        apiCall = { getLastMatches(apiKey, teamId, count) },
        fallbackValue = emptyList(),
        apiKey = apiKey
    )
}

/**
 * Safe wrapper for getTeamTactics with graceful degradation.
 */
suspend fun ApiSportsApi.safeGetTeamTactics(
    apiKey: String,
    teamId: Int,
    season: Int
): Result<TeamStatisticsResponse?> {
    return safeApiCall(
        apiCall = { getTeamTactics(apiKey, teamId, season) },
        fallbackValue = null,
        apiKey = apiKey
    )
}

    /**
     * Safe wrapper for getFixtureLineups with graceful degradation.
     */
    suspend fun ApiSportsApi.safeGetFixtureLineups(
        apiKey: String,
        fixtureId: Int
    ): Result<List<FixtureLineupsResponse>?> {
        return safeApiCall(
            apiCall = { getFixtureLineups(apiKey, fixtureId) },
            fallbackValue = null,
            apiKey = apiKey
        )
    }

/**
 * Safe wrapper for getFixture with graceful degradation.
 */
suspend fun ApiSportsApi.safeGetFixture(
    apiKey: String,
    fixtureId: Int
): Result<FixtureResponse?> {
    return safeApiCall(
        apiCall = { getFixture(apiKey, fixtureId) },
        fallbackValue = null,
        apiKey = apiKey
    )
}

/**
 * Safe wrapper for getHeadToHead with graceful degradation.
 */
suspend fun ApiSportsApi.safeGetHeadToHead(
    apiKey: String,
    homeTeamId: Int,
    awayTeamId: Int,
    last: Int = 10
): Result<List<FixtureResponse>> {
    return safeApiCall(
        apiCall = { getHeadToHead(apiKey, homeTeamId, awayTeamId, last) },
        fallbackValue = emptyList(),
        apiKey = apiKey
    )
}
