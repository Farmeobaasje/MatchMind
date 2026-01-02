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
import com.Lyno.matchmindai.data.dto.football.InjuriesResponseDto
import com.Lyno.matchmindai.data.dto.football.FixtureStatisticsResponse
import kotlinx.serialization.json.JsonElement

/**
 * Rate-limited wrapper for FootballApiService that ensures all API calls
 * go through the ApiSportsRateLimiter to prevent rate limit errors.
 * 
 * This service wraps all FootballApiService methods with rate limiting
 * and provides consistent error handling for rate limit responses.
 */
class RateLimitedFootballApiService(
    private val footballApiService: FootballApiService
) {
    private val rateLimiter = ApiSportsRateLimiter.getInstance()
    
    /**
     * Fetch fixtures for a specific date with rate limiting.
     */
    suspend fun getFixturesByDate(
        date: String,
        timezone: String = "Europe/Amsterdam"
    ): List<FixtureItemDto> {
        return rateLimiter.executeWithRateLimit(
            id = "fixtures_by_date_${date}_${System.currentTimeMillis()}",
            priority = ApiSportsRateLimiter.Priority.LOW
        ) {
            footballApiService.getFixturesByDate(date, timezone)
        }
    }
    
    /**
     * Fetch fixtures for a date range (from-to) with rate limiting.
     * This is more efficient than making separate calls for each date.
     */
    suspend fun getFixturesByDateRange(
        from: String,
        to: String,
        timezone: String = "Europe/Amsterdam"
    ): List<FixtureItemDto> {
        return rateLimiter.executeWithRateLimit(
            id = "fixtures_by_date_range_${from}_${to}",
            priority = ApiSportsRateLimiter.Priority.LOW
        ) {
            footballApiService.getFixturesByDateRange(from, to, timezone)
        }
    }
    
    /**
     * Fetch fixtures for a specific team within a date range with rate limiting.
     */
    suspend fun getFixturesByTeam(
        teamId: Int,
        season: Int? = 2025,
        from: String,
        to: String,
        timezone: String = "Europe/Amsterdam"
    ): List<FixtureItemDto> {
        return rateLimiter.executeWithRateLimit(
            id = "fixtures_by_team_${teamId}_${from}_${to}",
            priority = ApiSportsRateLimiter.Priority.LOW
        ) {
            footballApiService.getFixturesByTeam(teamId, season, from, to, timezone)
        }
    }
    
    /**
     * Fetch the last X fixtures for a specific team with rate limiting.
     */
    suspend fun getLastFixturesForTeam(
        teamId: Int,
        count: Int = 15,
        status: String? = "FT",
        timezone: String = "Europe/Amsterdam"
    ): List<FixtureItemDto> {
        return rateLimiter.executeWithRateLimit(
            id = "last_fixtures_team_${teamId}_${count}",
            priority = ApiSportsRateLimiter.Priority.LOW
        ) {
            footballApiService.getLastFixturesForTeam(teamId, count, status, timezone)
        }
    }
    
    /**
     * Fetch fixtures for a specific league and season with rate limiting.
     */
    suspend fun getFixturesByLeague(
        leagueId: Int,
        season: Int,
        status: String? = null,
        timezone: String = "Europe/Amsterdam"
    ): List<FixtureItemDto> {
        return rateLimiter.executeWithRateLimit(
            id = "fixtures_by_league_${leagueId}_${season}",
            priority = ApiSportsRateLimiter.Priority.LOW
        ) {
            footballApiService.getFixturesByLeague(leagueId, season, status, timezone)
        }
    }
    
    /**
     * Fetch leagues with current=true parameter with rate limiting.
     */
    suspend fun getLeagues(
        current: Boolean = true
    ): LeagueDiscoveryResponse {
        return rateLimiter.executeWithRateLimit(
            id = "leagues_current_${current}",
            priority = ApiSportsRateLimiter.Priority.LOW
        ) {
            footballApiService.getLeagues(current)
        }
    }
    
    /**
     * Fetch leagues for a specific team in a given season with rate limiting.
     */
    suspend fun getTeamLeagues(
        teamId: Int,
        season: Int = 2025
    ): JsonElement {
        return rateLimiter.executeWithRateLimit(
            id = "team_leagues_${teamId}_${season}",
            priority = ApiSportsRateLimiter.Priority.LOW
        ) {
            footballApiService.getTeamLeagues(teamId, season)
        }
    }
    
    /**
     * Fetch live fixtures with goals information with rate limiting.
     */
    suspend fun getLiveFixtures(
        timezone: String = "Europe/Amsterdam"
    ): List<com.Lyno.matchmindai.data.dto.football.LiveMatchDto> {
        return rateLimiter.executeWithRateLimit(
            id = "live_fixtures_${System.currentTimeMillis()}",
            priority = ApiSportsRateLimiter.Priority.MEDIUM
        ) {
            footballApiService.getLiveFixtures(timezone)
        }
    }
    
    /**
     * Fetch detailed match information with rate limiting.
     */
    suspend fun getMatchDetails(
        fixtureId: Int
    ): MatchDetailsResponse {
        return rateLimiter.executeWithRateLimit(
            id = "match_details_${fixtureId}",
            priority = ApiSportsRateLimiter.Priority.MEDIUM
        ) {
            footballApiService.getMatchDetails(fixtureId)
        }
    }
    
    /**
     * Fetch standings for a specific league and season with rate limiting.
     */
    suspend fun getStandings(
        leagueId: Int,
        season: Int
    ): com.Lyno.matchmindai.data.remote.dto.StandingsResponse {
        return rateLimiter.executeWithRateLimit(
            id = "standings_${leagueId}_${season}",
            priority = ApiSportsRateLimiter.Priority.MEDIUM
        ) {
            footballApiService.getStandings(leagueId, season)
        }
    }
    
    /**
     * Fetch predictions for a specific fixture with rate limiting.
     */
    suspend fun getPredictions(
        fixtureId: Int
    ): PredictionsResponse {
        return rateLimiter.executeWithRateLimit(
            id = "predictions_${fixtureId}",
            priority = ApiSportsRateLimiter.Priority.MEDIUM
        ) {
            footballApiService.getPredictions(fixtureId)
        }
    }
    
    /**
     * Fetch predictions as JsonElement with rate limiting.
     */
    suspend fun getPredictionsAsJson(
        fixtureId: Int
    ): JsonElement {
        return rateLimiter.executeWithRateLimit(
            id = "predictions_json_${fixtureId}",
            priority = ApiSportsRateLimiter.Priority.MEDIUM
        ) {
            footballApiService.getPredictionsAsJson(fixtureId)
        }
    }
    
    /**
     * Fetch injury information for a specific fixture with rate limiting.
     */
    suspend fun getInjuries(
        fixtureId: Int
    ): JsonElement {
        return rateLimiter.executeWithRateLimit(
            id = "injuries_${fixtureId}",
            priority = ApiSportsRateLimiter.Priority.LOW
        ) {
            footballApiService.getInjuries(fixtureId)
        }
    }
    
    /**
     * Fetch injury information with typed response with rate limiting.
     */
    suspend fun getInjuriesTyped(
        fixtureId: Int
    ): InjuriesResponseDto {
        return rateLimiter.executeWithRateLimit(
            id = "injuries_typed_${fixtureId}",
            priority = ApiSportsRateLimiter.Priority.LOW
        ) {
            footballApiService.getInjuriesTyped(fixtureId)
        }
    }
    
    /**
     * Fetch pre-match odds for a specific fixture with rate limiting.
     */
    suspend fun getOdds(
        fixtureId: Int
    ): OddsResponseDto {
        return rateLimiter.executeWithRateLimit(
            id = "odds_${fixtureId}",
            priority = ApiSportsRateLimiter.Priority.MEDIUM
        ) {
            footballApiService.getOdds(fixtureId)
        }
    }
    
    /**
     * Fetch pre-match odds as JsonElement with rate limiting.
     */
    suspend fun getOddsAsJson(
        fixtureId: Int
    ): JsonElement {
        return rateLimiter.executeWithRateLimit(
            id = "odds_json_${fixtureId}",
            priority = ApiSportsRateLimiter.Priority.MEDIUM
        ) {
            footballApiService.getOddsAsJson(fixtureId)
        }
    }
    
    /**
     * Fetch in-play (live) odds with rate limiting.
     */
    suspend fun getLiveOdds(
        fixtureId: Int? = null,
        leagueId: Int? = null,
        betId: Int? = null
    ): LiveOddsResponseDto {
        return rateLimiter.executeWithRateLimit(
            id = "live_odds_${fixtureId ?: "all"}",
            priority = ApiSportsRateLimiter.Priority.MEDIUM
        ) {
            footballApiService.getLiveOdds(fixtureId, leagueId, betId)
        }
    }
    
    /**
     * Fetch odds for a specific date with rate limiting.
     */
    suspend fun getOddsByDate(
        date: String,
        leagueId: Int? = null,
        timezone: String = "Europe/Amsterdam"
    ): OddsResponseDto {
        return rateLimiter.executeWithRateLimit(
            id = "odds_by_date_${date}",
            priority = ApiSportsRateLimiter.Priority.LOW
        ) {
            footballApiService.getOddsByDate(date, leagueId, timezone)
        }
    }
    
    /**
     * Fetch odds for a specific league and season with rate limiting.
     */
    suspend fun getOddsByLeague(
        leagueId: Int,
        season: Int,
        bookmakerId: Int? = null,
        betId: Int? = null
    ): OddsResponseDto {
        return rateLimiter.executeWithRateLimit(
            id = "odds_by_league_${leagueId}_${season}",
            priority = ApiSportsRateLimiter.Priority.LOW
        ) {
            footballApiService.getOddsByLeague(leagueId, season, bookmakerId, betId)
        }
    }
    
    /**
     * Fetch statistics for a specific fixture with rate limiting.
     * This is HIGH priority because xG data is critical for predictions.
     */
    suspend fun getFixtureStatistics(
        fixtureId: Int,
        teamId: Int? = null,
        type: String? = null,
        half: Boolean = false
    ): FixtureStatisticsResponse {
        return rateLimiter.executeWithRateLimit(
            id = "fixture_stats_${fixtureId}",
            priority = ApiSportsRateLimiter.Priority.HIGH
        ) {
            footballApiService.getFixtureStatistics(fixtureId, teamId, type, half)
        }
    }
    
    /**
     * Get the current rate limiter queue statistics for debugging.
     */
    fun getRateLimiterStats(): String {
        return rateLimiter.getQueueStats()
    }
    
    /**
     * Clear the rate limiter queue.
     */
    fun clearRateLimiterQueue() {
        rateLimiter.clearQueue()
    }
}
