package com.Lyno.matchmindai.data.repository

import com.Lyno.matchmindai.data.utils.StandingsFallbackHelper
import com.Lyno.matchmindai.data.remote.dto.StandingsResponse
import com.Lyno.matchmindai.data.remote.dto.LeagueNode
import com.Lyno.matchmindai.data.remote.dto.LeagueDetails
import com.Lyno.matchmindai.data.remote.dto.StandingTeamDto
import com.Lyno.matchmindai.data.remote.dto.TeamIdentityDto
import com.Lyno.matchmindai.data.remote.dto.StandingStatsDto
import com.Lyno.matchmindai.data.remote.dto.GoalsDto
import com.Lyno.matchmindai.data.dto.football.FixtureItemDto
import com.Lyno.matchmindai.data.dto.football.FixtureDto
import com.Lyno.matchmindai.data.dto.football.TeamsDto
import com.Lyno.matchmindai.data.dto.football.Goals
import com.Lyno.matchmindai.domain.model.DataSource
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

/**
 * Test suite for Oracle Repository fallback mechanisms.
 * Tests the tiered fallback strategy for standings data.
 */
@RunWith(MockitoJUnitRunner::class)
class OracleRepositoryFallbackTest {

    private val standingsFallbackHelper = StandingsFallbackHelper()

    /**
     * Test 1: Official API data available (happy path)
     */
    @Test
    fun testOfficialApiDataAvailable() = runBlocking {
        // Create mock standings response
        val mockStandings = createMockStandingsResponse(
            leagueId = 6,
            season = 2025,
            teams = listOf(
                createStandingTeam(1522, "Zimbabwe", 1, 45, 15),
                createStandingTeam(1531, "South Africa", 2, 42, 12)
            )
        )

        val result = standingsFallbackHelper.getStandingsWithFallback(
            getStandings = { leagueId, season -> 
                assertEquals(6, leagueId)
                assertEquals(2025, season)
                mockStandings
            },
            getRecentFixtures = { teamId, season -> 
                emptyList() // Not needed for this test
            },
            leagueId = 6,
            season = 2025,
            homeTeamId = 1522,
            awayTeamId = 1531
        )

        assertEquals(DataSource.API_OFFICIAL, result.source)
        assertEquals(2025, result.seasonUsed)
        assertEquals(1.0f, result.confidenceAdjustment)
        assertEquals(2, standingsFallbackHelper.extractStandings(result.standings).size)
    }

    /**
     * Test 2: No official data, fallback to calculated standings from fixtures
     */
    @Test
    fun testCalculatedStandingsFromFixtures() = runBlocking {
        // Mock empty official standings
        val emptyStandings = StandingsResponse(
            get = "standings",
            results = 0,
            response = emptyList()
        )

        // Create mock fixtures for calculation
        val zimbabweFixtures = listOf(
            createFixtureItem(1522, 1001, 2, 1), // Zimbabwe win
            createFixtureItem(1522, 1002, 1, 1), // Draw
            createFixtureItem(1003, 1522, 0, 2)  // Zimbabwe win
        )

        val southAfricaFixtures = listOf(
            createFixtureItem(1531, 1004, 1, 0), // South Africa win
            createFixtureItem(1005, 1531, 2, 2), // Draw
            createFixtureItem(1531, 1006, 0, 1)  // South Africa loss
        )

        val result = standingsFallbackHelper.getStandingsWithFallback(
            getStandings = { _, _ -> emptyStandings },
            getRecentFixtures = { teamId, season ->
                when (teamId) {
                    1522 -> zimbabweFixtures
                    1531 -> southAfricaFixtures
                    else -> emptyList()
                }
            },
            leagueId = 6,
            season = 2025,
            homeTeamId = 1522,
            awayTeamId = 1531
        )

        assertEquals(DataSource.CALCULATED, result.source)
        assertEquals(2025, result.seasonUsed)
        assertEquals(0.75f, result.confidenceAdjustment)
        
        val standings = standingsFallbackHelper.extractStandings(result.standings)
        assertEquals(2, standings.size)
        
        // Zimbabwe should have more points (7 vs 4)
        val zimbabweStanding = standings.find { it.team.id == 1522 }
        val southAfricaStanding = standings.find { it.team.id == 1531 }
        
        assertNotNull(zimbabweStanding)
        assertNotNull(southAfricaStanding)
        
        // Zimbabwe: 2 wins, 1 draw = 7 points
        assertEquals(7, zimbabweStanding?.points)
        // South Africa: 1 win, 1 draw, 1 loss = 4 points
        assertEquals(4, southAfricaStanding?.points)
    }

    /**
     * Test 3: Fallback to previous season data
     */
    @Test
    fun testPreviousSeasonFallback() = runBlocking {
        // Mock empty current season standings
        val emptyCurrentSeason = StandingsResponse(
            get = "standings",
            results = 0,
            response = emptyList()
        )

        // Mock previous season standings
        val previousSeasonStandings = createMockStandingsResponse(
            leagueId = 6,
            season = 2024,
            teams = listOf(
                createStandingTeam(1522, "Zimbabwe", 3, 38, 8),
                createStandingTeam(1531, "South Africa", 5, 35, 5)
            )
        )

        val result = standingsFallbackHelper.getStandingsWithFallback(
            getStandings = { leagueId, season ->
                when (season) {
                    2025 -> emptyCurrentSeason
                    2024 -> previousSeasonStandings
                    else -> emptyCurrentSeason
                }
            },
            getRecentFixtures = { _, _ -> emptyList() },
            leagueId = 6,
            season = 2025,
            homeTeamId = 1522,
            awayTeamId = 1531
        )

        assertEquals(DataSource.PREVIOUS_SEASON, result.source)
        assertEquals(2024, result.seasonUsed)
        assertEquals(0.7f, result.confidenceAdjustment)
        
        val standings = standingsFallbackHelper.extractStandings(result.standings)
        assertEquals(2, standings.size)
    }

    /**
     * Test 4: Default data fallback (worst case)
     */
    @Test
    fun testDefaultDataFallback() = runBlocking {
        // Mock all failures
        val result = standingsFallbackHelper.getStandingsWithFallback(
            getStandings = { _, _ -> 
                throw Exception("API failure")
            },
            getRecentFixtures = { _, _ -> 
                throw Exception("Fixture API failure")
            },
            leagueId = 6,
            season = 2025,
            homeTeamId = 1522,
            awayTeamId = 1531
        )

        assertEquals(DataSource.DEFAULT, result.source)
        assertEquals(2025, result.seasonUsed)
        assertEquals(0.6f, result.confidenceAdjustment)
        
        val standings = standingsFallbackHelper.extractStandings(result.standings)
        assertEquals(2, standings.size)
        
        // Both teams should have default values
        standings.forEach { team ->
            assertEquals(10, team.rank)
            assertEquals(30, team.points)
            assertEquals(0, team.goalsDiff)
            assertEquals(20, team.all?.played)
        }
    }

    /**
     * Test 5: Confidence adjustment based on data source
     */
    @Test
    fun testConfidenceAdjustment() {
        assertEquals(1.0f, standingsFallbackHelper.getConfidenceAdjustment(DataSource.API_OFFICIAL))
        assertEquals(0.75f, standingsFallbackHelper.getConfidenceAdjustment(DataSource.CALCULATED))
        assertEquals(0.7f, standingsFallbackHelper.getConfidenceAdjustment(DataSource.PREVIOUS_SEASON))
        assertEquals(0.6f, standingsFallbackHelper.getConfidenceAdjustment(DataSource.DEFAULT))
    }

    /**
     * Test 6: Team statistics calculation from fixtures
     */
    @Test
    fun testTeamStatsCalculation() {
        val fixtures = listOf(
            createFixtureItem(1522, 1001, 2, 1), // Win
            createFixtureItem(1522, 1002, 1, 1), // Draw
            createFixtureItem(1003, 1522, 0, 2), // Win (away)
            createFixtureItem(1004, 1522, 3, 1), // Loss (away)
            createFixtureItem(1522, 1005, 0, 1)  // Loss
        )

        val stats = standingsFallbackHelper.calculateTeamStats(1522, fixtures)
        
        assertEquals(5, stats.played)
        assertEquals(2, stats.wins)
        assertEquals(1, stats.draws)
        assertEquals(2, stats.losses)
        assertEquals(7, stats.points) // 2*3 + 1 = 7
        assertEquals(6, stats.goalsFor) // 2+1+2+1+0 = 6
        assertEquals(6, stats.goalsAgainst) // 1+1+0+3+1 = 6
        assertEquals(0, stats.goalDiff)
    }

    /**
     * Test 7: Zimbabwe vs South Africa specific scenario
     * Simulates the exact scenario from the log data
     */
    @Test
    fun testZimbabweSouthAfricaScenario() = runBlocking {
        // Simulate the exact failure from the logs
        val result = standingsFallbackHelper.getStandingsWithFallback(
            getStandings = { leagueId, season ->
                if (season == 2025) {
                    // API returns 204 No Content or empty data
                    StandingsResponse(
                        get = "standings",
                        results = 0,
                        response = emptyList()
                    )
                } else {
                    // Previous season also fails
                    throw Exception("No data available")
                }
            },
            getRecentFixtures = { teamId, season ->
                // Simulate fixture API failure
                throw Exception("Fixture API unavailable")
            },
            leagueId = 6,
            season = 2025,
            homeTeamId = 1522,
            awayTeamId = 1531
        )

        // Should fall back to default data
        assertEquals(DataSource.DEFAULT, result.source)
        assertEquals(0.6f, result.confidenceAdjustment)
        
        val standings = standingsFallbackHelper.extractStandings(result.standings)
        assertEquals(2, standings.size)
        
        // Verify default values
        val homeTeam = standings.find { it.team.id == 0 } // Default home team ID
        val awayTeam = standings.find { it.team.id == 1 } // Default away team ID
        
        assertNotNull(homeTeam)
        assertNotNull(awayTeam)
        
        assertEquals(10, homeTeam?.rank)
        assertEquals(30, homeTeam?.points)
        assertEquals(0, homeTeam?.goalsDiff)
        
        assertEquals(10, awayTeam?.rank)
        assertEquals(30, awayTeam?.points)
        assertEquals(0, awayTeam?.goalsDiff)
    }

    // Helper methods for creating test data
    private fun createMockStandingsResponse(
        leagueId: Int,
        season: Int,
        teams: List<StandingTeamDto>
    ): StandingsResponse {
        val leagueDetails = LeagueDetails(
            id = leagueId,
            name = "Test League",
            country = "Test Country",
            season = season,
            standings = listOf(teams)
        )
        
        val leagueNode = LeagueNode(league = leagueDetails)
        
        return StandingsResponse(
            get = "standings",
            results = teams.size,
            response = listOf(leagueNode)
        )
    }

    private fun createStandingTeam(
        teamId: Int,
        teamName: String,
        rank: Int,
        points: Int,
        goalsDiff: Int
    ): StandingTeamDto {
        return StandingTeamDto(
            rank = rank,
            team = TeamIdentityDto(
                id = teamId,
                name = teamName,
                logo = null
            ),
            points = points,
            goalsDiff = goalsDiff,
            all = StandingStatsDto(
                played = 20,
                win = 10,
                draw = 5,
                lose = 5,
                goals = GoalsDto(forGoals = 30, againstGoals = 20)
            )
        )
    }

    private fun createFixtureItem(
        homeTeamId: Int,
        awayTeamId: Int,
        homeGoals: Int,
        awayGoals: Int
    ): FixtureItemDto {
        return FixtureItemDto(
            fixture = FixtureDto(
                id = 1000,
                referee = null,
                timezone = "UTC",
                date = "2025-01-01T15:00:00+00:00",
                timestamp = 1735736400,
                periods = null,
                venue = null,
                status = null
            ),
            league = null,
            teams = TeamsDto(
                home = com.Lyno.matchmindai.data.dto.TeamDto(
                    id = homeTeamId,
                    name = "Home Team",
                    logo = null,
                    winner = null
                ),
                away = com.Lyno.matchmindai.data.dto.TeamDto(
                    id = awayTeamId,
                    name = "Away Team",
                    logo = null,
                    winner = null
                )
            ),
            goals = Goals(
                home = homeGoals,
                away = awayGoals
            ),
            score = null,
            events = null,
            lineups = null,
            statistics = null,
            players = null
        )
    }
}
