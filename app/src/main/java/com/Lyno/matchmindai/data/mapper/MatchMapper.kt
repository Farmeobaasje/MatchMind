package com.Lyno.matchmindai.data.mapper

import com.Lyno.matchmindai.data.dto.FixtureItemDto
import com.Lyno.matchmindai.data.dto.FixtureListResponse
import com.Lyno.matchmindai.data.dto.football.TeamDto
import com.Lyno.matchmindai.data.dto.football.TeamsDetailsDto
import com.Lyno.matchmindai.data.dto.football.StatusDto
import com.Lyno.matchmindai.data.dto.football.LeagueDetailsDto
import com.Lyno.matchmindai.data.dto.football.LiveMatchDto
import com.Lyno.matchmindai.data.dto.football.MatchGoalsDto
import com.Lyno.matchmindai.data.dto.DeepSeekRequest
import com.Lyno.matchmindai.data.dto.DeepSeekResponse
import com.Lyno.matchmindai.data.dto.DeepSeekMessage
import com.Lyno.matchmindai.data.dto.ResponseFormat
import com.Lyno.matchmindai.domain.model.AgentResponse
import com.Lyno.matchmindai.domain.model.MatchFixture
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

/**
 * Maps data transfer objects to domain models and vice versa.
 */
object MatchMapper {

    /**
     * Maps API short status codes to MatchStatus enum.
     */
    fun mapStatus(shortStatus: String?): com.Lyno.matchmindai.domain.model.MatchStatus {
        return when (shortStatus) {
            "1H", "HT", "2H", "ET", "BT", "P", "LIVE" -> com.Lyno.matchmindai.domain.model.MatchStatus.LIVE
            "FT", "AET", "PEN" -> com.Lyno.matchmindai.domain.model.MatchStatus.FINISHED
            "NS", "TBD" -> com.Lyno.matchmindai.domain.model.MatchStatus.SCHEDULED
            else -> com.Lyno.matchmindai.domain.model.MatchStatus.UNKNOWN
        }
    }

    /**
     * Maps a list of FixtureItemDto to a list of MatchFixture domain models.
     */
    fun mapToDomainFixtures(fixtures: List<FixtureItemDto>): List<MatchFixture> {
        return fixtures.map { mapToDomainFixture(it) }
    }

    /**
     * Maps a single FixtureItemDto to a MatchFixture domain model.
     */
    fun mapToDomainFixture(fixture: FixtureItemDto): MatchFixture {
        return MatchFixture(
            homeTeam = fixture.teams.home.name,
            awayTeam = fixture.teams.away.name,
            time = extractTimeFromDateTime(fixture.fixture.date),
            league = fixture.league.name,
            date = extractDateFromDateTime(fixture.fixture.date),
            status = fixture.fixture.status?.short ?: "NS",
            elapsed = fixture.fixture.status?.elapsed,
            homeScore = fixture.goals.home ?: 0,
            awayScore = fixture.goals.away ?: 0,
            fixtureId = fixture.fixture.id,
            homeTeamId = fixture.teams.home.id,
            awayTeamId = fixture.teams.away.id
        )
    }

    /**
     * Maps a LiveMatchDto to a MatchFixture domain model.
     */
    fun mapLiveMatchToDomainFixture(liveMatch: LiveMatchDto): MatchFixture {
        return MatchFixture(
            homeTeam = liveMatch.teams.home.name,
            awayTeam = liveMatch.teams.away.name,
            time = extractTimeFromDateTime(liveMatch.fixture.date),
            league = liveMatch.league.name,
            date = extractDateFromDateTime(liveMatch.fixture.date),
            status = liveMatch.fixture.status?.short ?: "NS",
            elapsed = liveMatch.fixture.status?.elapsed,
            homeScore = liveMatch.goals?.home ?: 0,
            awayScore = liveMatch.goals?.away ?: 0,
            fixtureId = liveMatch.fixture.id,
            homeTeamId = liveMatch.teams.home.id,
            awayTeamId = liveMatch.teams.away.id
        )
    }

    /**
     * Maps a list of LiveMatchDto to a list of MatchFixture domain models.
     */
    fun mapLiveMatchesToDomainFixtures(liveMatches: List<LiveMatchDto>): List<MatchFixture> {
        return liveMatches.map { mapLiveMatchToDomainFixture(it) }
    }

    /**
     * Maps API response to domain fixture list.
     */
    fun mapFromResponse(response: FixtureListResponse): List<MatchFixture> {
        return response.response.map { fixtureItem -> mapToDomainFixture(fixtureItem) }
    }

    /**
     * Maps a DeepSeek response to an AgentResponse domain model.
     */
    fun mapToAgentResponse(response: DeepSeekResponse): AgentResponse {
        val content = response.choices.firstOrNull()?.message?.content
        
        return if (content != null) {
            // For now, just return the content as a chat response
            // TODO: Implement proper JSON parsing when Json serialization is fixed
            AgentResponse.chat(content)
        } else {
            AgentResponse.error("No response received from AI")
        }
    }

    /**
     * Maps domain model to DeepSeek request.
     */
    fun mapToDeepSeekRequest(
        query: String,
        systemPrompt: String,
        apiKey: String
    ): DeepSeekRequest {
        return DeepSeekRequest(
            model = "deepseek-chat",
            messages = listOf(
                DeepSeekMessage(role = "system", content = systemPrompt),
                DeepSeekMessage(role = "user", content = query)
            ),
            responseFormat = ResponseFormat(type = "json_object"),
            temperature = 0.7
        )
    }

    /**
     * Creates a simple text-only AgentResponse for fallback cases.
     */
    fun mapToTextResponse(text: String): AgentResponse {
        return AgentResponse.chat(text)
    }

    /**
     * Creates a live match response with fixture data.
     */
    fun mapToLiveMatchResponse(
        text: String,
        fixtures: List<MatchFixture>
    ): AgentResponse {
        // For now, return without JSON data
        return AgentResponse.liveMatch(text, null)
    }

    /**
     * Creates a prediction response with match data.
     */
    fun mapToPredictionResponse(
        text: String,
        matchData: JsonElement?
    ): AgentResponse {
        return AgentResponse.prediction(text, matchData)
    }

    /**
     * Creates an analysis response with related data.
     */
    fun mapToAnalysisResponse(
        text: String,
        analysisData: JsonElement? = null
    ): AgentResponse {
        return AgentResponse.analysis(text, analysisData)
    }

    /**
     * Creates a standings response with rankings data.
     */
    fun mapToStandingsResponse(
        text: String,
        standingsData: JsonElement? = null
    ): AgentResponse {
        return AgentResponse.standings(text, standingsData)
    }

    /**
     * Helper method to extract time from ISO datetime string.
     */
    private fun extractTimeFromDateTime(dateTime: String): String {
        return try {
            // Simple extraction - assumes format like "2024-01-15T19:00:00+00:00"
            val timePart = dateTime.substringAfter("T").substringBefore("+")
            timePart.substringBefore(":") + ":" + timePart.substringAfter(":").substringBefore(":")
        } catch (e: Exception) {
            "00:00"
        }
    }

    /**
     * Helper method to extract date from ISO datetime string.
     */
    private fun extractDateFromDateTime(dateTime: String): String {
        return try {
            // Extract date part and format as "Zo 14 dec"
            val datePart = dateTime.substringBefore("T")
            val parts = datePart.split("-")
            if (parts.size >= 3) {
                val year = parts[0].toInt()
                val month = parts[1].toInt()
                val day = parts[2].toInt()
                
                // Get day of week (simplified - this would need proper date handling in production)
                // Using Calendar instead of java.time for backward compatibility
                val calendar = java.util.Calendar.getInstance()
                calendar.set(year, month - 1, day)
                val dayOfWeek = when (calendar.get(java.util.Calendar.DAY_OF_WEEK)) {
                    java.util.Calendar.SUNDAY -> "Zo"
                    java.util.Calendar.MONDAY -> "Ma"
                    java.util.Calendar.TUESDAY -> "Di"
                    java.util.Calendar.WEDNESDAY -> "Wo"
                    java.util.Calendar.THURSDAY -> "Do"
                    java.util.Calendar.FRIDAY -> "Vr"
                    java.util.Calendar.SATURDAY -> "Za"
                    else -> ""
                }
                
                val monthName = when (month) {
                    1 -> "jan"
                    2 -> "feb"
                    3 -> "mrt"
                    4 -> "apr"
                    5 -> "mei"
                    6 -> "jun"
                    7 -> "jul"
                    8 -> "aug"
                    9 -> "sep"
                    10 -> "okt"
                    11 -> "nov"
                    12 -> "dec"
                    else -> ""
                }
                
                "$dayOfWeek $day $monthName"
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }
}
