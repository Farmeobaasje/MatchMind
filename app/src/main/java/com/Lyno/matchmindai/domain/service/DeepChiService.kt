package com.Lyno.matchmindai.domain.service

import com.Lyno.matchmindai.data.dto.FixtureDto
import com.Lyno.matchmindai.data.dto.FixtureResponse
import com.Lyno.matchmindai.data.dto.GoalsDto
import com.Lyno.matchmindai.data.dto.StatusDto
import com.Lyno.matchmindai.data.dto.TeamDto
import com.Lyno.matchmindai.data.dto.TeamsDto
import com.Lyno.matchmindai.data.dto.football.FixtureLineupsResponse
import com.Lyno.matchmindai.data.dto.football.TeamStatisticsResponse
import com.Lyno.matchmindai.data.remote.ApiSportsApi
import com.Lyno.matchmindai.data.remote.DeepSeekApi
import com.Lyno.matchmindai.domain.ai.DeepChiPrompts
import com.Lyno.matchmindai.domain.model.SimulationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * DeepChi Service: AI-powered football match analysis using DeepSeek LLM.
 * Analyzes fatigue, style matchup, and lineup strength to generate simulation context.
 */
class DeepChiService(
    private val apiSportsApi: ApiSportsApi,
    private val deepSeekApi: DeepSeekApi
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Analyzes a football match and generates simulation context.
     * @param fixtureId The fixture ID to analyze
     * @param homeTeamId Home team ID
     * @param awayTeamId Away team ID
     * @param season Season year (e.g., 2024)
     * @param apiSportsApiKey User's API-SPORTS API key
     * @param deepSeekApiKey User's DeepSeek API key
     * @return SimulationContext with AI analysis results
     */
    suspend fun analyzeMatch(
        fixtureId: Int,
        homeTeamId: Int,
        awayTeamId: Int,
        season: Int,
        apiSportsApiKey: String,
        deepSeekApiKey: String
    ): Result<SimulationContext> = withContext(Dispatchers.IO) {
        try {
            // 1. Collect data from API-SPORTS
            val (homeData, awayData, fixtureData) = collectMatchData(
                fixtureId, homeTeamId, awayTeamId, season, apiSportsApiKey
            )

            // 2. Build AI prompt
            val promptInput = DeepChiPrompts.MatchAnalysisInput(
                homeTeam = DeepChiPrompts.TeamAnalysisInput(
                    teamName = homeData.teamName,
                    lastMatches = homeData.lastMatches,
                    teamStats = homeData.teamStats,
                    lineup = homeData.lineup
                ),
                awayTeam = DeepChiPrompts.TeamAnalysisInput(
                    teamName = awayData.teamName,
                    lastMatches = awayData.lastMatches,
                    teamStats = awayData.teamStats,
                    lineup = awayData.lineup
                ),
                fixtureDate = fixtureData.date,
                venueCity = fixtureData.venueCity
            )

            val prompt = DeepChiPrompts.buildAnalysisPrompt(promptInput)

            // 3. Send to DeepSeek API
            val aiResponse = deepSeekApi.sendPrompt(
                prompt = prompt,
                responseFormat = "json_object",
                apiKey = deepSeekApiKey
            )
            
            if (aiResponse.isFailure) {
                return@withContext Result.failure(
                    aiResponse.exceptionOrNull() ?: Exception("DeepSeek API failed")
                )
            }

            val responseText = aiResponse.getOrNull()?.choices?.firstOrNull()?.message?.content
                ?: return@withContext Result.failure(Exception("Empty response from DeepSeek"))

            // 4. Parse AI response
            val parsedResponse = json.decodeFromString<DeepChiPrompts.DeepChiResponse>(responseText)

            // 5. Convert to SimulationContext
            val simulationContext = SimulationContext(
                fatigueScore = calculateOverallFatigue(parsedResponse.fatigue_home, parsedResponse.fatigue_away),
                styleMatchup = parsedResponse.style_matchup,
                lineupStrength = calculateOverallLineupStrength(
                    parsedResponse.lineup_strength_home,
                    parsedResponse.lineup_strength_away
                ),
                reasoning = parsedResponse.reasoning,
                // Legacy fields (calculated from new metrics)
                homeFitness = 100 - parsedResponse.fatigue_home,
                awayFitness = 100 - parsedResponse.fatigue_away,
                homeDistraction = 100 - parsedResponse.lineup_strength_home,
                awayDistraction = 100 - parsedResponse.lineup_strength_away
            )

            Result.success(simulationContext)

        } catch (e: Exception) {
            Result.failure(Exception("DeepChi analysis failed: ${e.message}", e))
        }
    }

    /**
     * Collects all necessary match data from API-SPORTS.
     */
    private suspend fun collectMatchData(
        fixtureId: Int,
        homeTeamId: Int,
        awayTeamId: Int,
        season: Int,
        apiKey: String
    ): Triple<TeamData, TeamData, FixtureData> {
        // Check if API key is valid
        if (apiKey.isBlank()) {
            throw IllegalArgumentException("API key is empty")
        }

        // Get fixture details for date
        val fixture = try {
            apiSportsApi.getFixture(apiKey, fixtureId)
        } catch (e: Exception) {
            // Return empty fixture data with dummy values
            com.Lyno.matchmindai.data.dto.FixtureResponse(
                fixture = com.Lyno.matchmindai.data.dto.FixtureDto(
                    id = fixtureId,
                    date = "",
                    status = com.Lyno.matchmindai.data.dto.StatusDto(short = "NS", long = "Not Started", elapsed = null)
                ),
                teams = com.Lyno.matchmindai.data.dto.TeamsDto(
                    home = com.Lyno.matchmindai.data.dto.TeamDto(id = homeTeamId, name = "Home Team"),
                    away = com.Lyno.matchmindai.data.dto.TeamDto(id = awayTeamId, name = "Away Team")
                ),
                goals = com.Lyno.matchmindai.data.dto.GoalsDto(home = null, away = null),
                league = null
            )
        }

        val fixtureDate = fixture.fixture?.date ?: ""
        val venueCity = "Unknown"

        // Get home team data with error handling
        val homeLastMatches = try {
            apiSportsApi.getLastMatches(apiKey, homeTeamId, 5)
        } catch (e: Exception) {
            emptyList()
        }
        
        val homeTeamStats = try {
            apiSportsApi.getTeamTactics(apiKey, homeTeamId, season)
        } catch (e: Exception) {
            null
        }
        
        val homeLineup = try {
            apiSportsApi.getFixtureLineups(apiKey, fixtureId)
        } catch (e: Exception) {
            null
        }

        // Get away team data with error handling
        val awayLastMatches = try {
            apiSportsApi.getLastMatches(apiKey, awayTeamId, 5)
        } catch (e: Exception) {
            emptyList()
        }
        
        val awayTeamStats = try {
            apiSportsApi.getTeamTactics(apiKey, awayTeamId, season)
        } catch (e: Exception) {
            null
        }
        
        val awayLineup = try {
            apiSportsApi.getFixtureLineups(apiKey, fixtureId)
        } catch (e: Exception) {
            null
        }

        val homeTeamName = fixture.teams?.home?.name ?: "Home Team"
        val awayTeamName = fixture.teams?.away?.name ?: "Away Team"

        return Triple(
            TeamData(homeTeamName, homeLastMatches, homeTeamStats, homeLineup),
            TeamData(awayTeamName, awayLastMatches, awayTeamStats, awayLineup),
            FixtureData(fixtureDate, venueCity)
        )
    }

    /**
     * Calculates overall fatigue score (average of home and away fatigue).
     */
    private fun calculateOverallFatigue(homeFatigue: Int, awayFatigue: Int): Int {
        return ((homeFatigue + awayFatigue) / 2).coerceIn(0, 100)
    }

    /**
     * Calculates overall lineup strength (average of home and away lineup strength).
     */
    private fun calculateOverallLineupStrength(homeLineup: Int, awayLineup: Int): Int {
        return ((homeLineup + awayLineup) / 2).coerceIn(0, 100)
    }

    /**
     * Data class for team analysis data.
     */
    private data class TeamData(
        val teamName: String,
        val lastMatches: List<FixtureResponse>,
        val teamStats: TeamStatisticsResponse?,
        val lineup: List<FixtureLineupsResponse>?
    )

    /**
     * Data class for fixture data.
     */
    private data class FixtureData(
        val date: String,
        val venueCity: String
    )

    /**
     * Fallback analysis when AI service is unavailable.
     * Uses basic heuristics based on available data.
     */
    suspend fun analyzeMatchFallback(
        fixtureId: Int,
        homeTeamId: Int,
        awayTeamId: Int,
        season: Int,
        apiKey: String
    ): SimulationContext = withContext(Dispatchers.IO) {
        try {
            // Basic data collection with error handling
            val homeLastMatches = try {
                apiSportsApi.getLastMatches(apiKey, homeTeamId, 3)
            } catch (e: Exception) {
                emptyList()
            }
            
            val awayLastMatches = try {
                apiSportsApi.getLastMatches(apiKey, awayTeamId, 3)
            } catch (e: Exception) {
                emptyList()
            }

            // Simple heuristics
            val fatigueScore = calculateFatigueHeuristic(homeLastMatches, awayLastMatches)
            val lineupStrength = 85 // Default assumption
            val styleMatchup = 1.0 // Neutral

            SimulationContext(
                fatigueScore = fatigueScore,
                styleMatchup = styleMatchup,
                lineupStrength = lineupStrength,
                reasoning = "Fallback analysis using basic heuristics",
                homeFitness = 100 - fatigueScore / 2,
                awayFitness = 100 - fatigueScore / 2,
                homeDistraction = 100 - lineupStrength,
                awayDistraction = 100 - lineupStrength
            )
        } catch (e: Exception) {
            // Ultimate fallback
            SimulationContext(
                fatigueScore = 50,
                styleMatchup = 1.0,
                lineupStrength = 85,
                reasoning = "Emergency fallback - no data available",
                homeFitness = 75,
                awayFitness = 75,
                homeDistraction = 15,
                awayDistraction = 15
            )
        }
    }

    /**
     * Calculates fatigue heuristic based on recent matches.
     */
    private fun calculateFatigueHeuristic(
        homeMatches: List<FixtureResponse>,
        awayMatches: List<FixtureResponse>
    ): Int {
        // Simple heuristic: more matches = more fatigue
        val homeFatigue = minOf(100, homeMatches.size * 20)
        val awayFatigue = minOf(100, awayMatches.size * 20)
        return ((homeFatigue + awayFatigue) / 2).coerceIn(0, 100)
    }
}
