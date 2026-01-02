package com.Lyno.matchmindai.data.mapper

import com.Lyno.matchmindai.data.dto.football.FixtureItemDto
import com.Lyno.matchmindai.data.dto.football.LiveMatchDto
import com.Lyno.matchmindai.data.local.entity.FixtureEntity
import com.Lyno.matchmindai.domain.model.MatchFixture
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Mapper for converting RapidAPI Football DTOs to domain models and database entities.
 */
object FootballMapper {

    /**
     * Convert a RapidAPI fixture item to a domain MatchFixture.
     */
    fun FixtureItemDto.toDomain(): MatchFixture {
        // Parse date string from API (format: "YYYY-MM-DDTHH:MM:SS+00:00")
        val apiDate = try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
            inputFormat.parse(fixture.date)
        } catch (e: Exception) {
            // Fallback to current date if parsing fails
            Date()
        }

        // Format date for display (e.g., "Zo 14 dec")
        val dateFormatter = SimpleDateFormat("EE dd MMM", Locale("nl", "NL"))
        val formattedDate = dateFormatter.format(apiDate)

        // Format time for display (e.g., "14:30")
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val formattedTime = timeFormatter.format(apiDate)

        return MatchFixture(
            homeTeam = teams.home.name,
            awayTeam = teams.away.name,
            homeTeamId = teams.home.id,
            awayTeamId = teams.away.id,
            date = formattedDate,
            time = formattedTime,
            league = league.name,
            status = fixture.status?.short,
            elapsed = fixture.status?.elapsed,
            homeScore = goals?.home,
            awayScore = goals?.away,
            fixtureId = fixture.id,
            leagueId = league.id,
            leagueCountry = league.country,
            leagueLogo = league.logo
        )
    }

    /**
     * Filter fixtures to only include top leagues.
     * We want: Eredivisie, Premier League, La Liga, Bundesliga, Serie A, Champions League, Europa League, KNVB Beker
     */
    fun filterTopLeagues(fixtures: List<FixtureItemDto>): List<FixtureItemDto> {
        val topLeagueNames = listOf(
            "Eredivisie",
            "Premier League",
            "La Liga",
            "Bundesliga",
            "Serie A",
            "Champions League",
            "Europa League",
            "KNVB Beker",
            "Europa Conference League",
            "FA Cup",
            "Copa del Rey",
            "DFB-Pokal",
            "Coppa Italia"
        )

        return fixtures.filter { fixture ->
            topLeagueNames.any { leagueName ->
                fixture.league.name.contains(leagueName, ignoreCase = true)
            }
        }
    }

    /**
     * Sort fixtures by date (earliest first).
     */
    fun sortByDate(fixtures: List<FixtureItemDto>): List<FixtureItemDto> {
        return fixtures.sortedBy { fixture ->
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
                inputFormat.parse(fixture.fixture.date)?.time ?: 0L
            } catch (e: Exception) {
                0L
            }
        }
    }

    /**
     * Convert a FixtureItemDto to a FixtureEntity for database storage.
     */
    fun FixtureItemDto.toEntity(dateKey: String): FixtureEntity {
        // Parse timestamp from date string
        val timestamp = try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
            inputFormat.parse(fixture.date)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
        
        return FixtureEntity(
            id = fixture.id,
            date = fixture.date,
            timestamp = timestamp,
            homeTeam = teams.home.name,
            awayTeam = teams.away.name,
            homeLogo = teams.home.logo,
            awayLogo = teams.away.logo,
            leagueName = league.name,
            leagueCountry = league.country,
            leagueFlag = league.flag,
            status = fixture.status?.long ?: "Not Started",
            statusShort = fixture.status?.short,
            elapsed = fixture.status?.elapsed,
            venueName = null, // Not available in current DTO
            venueCity = null, // Not available in current DTO
            referee = null, // Not available in current DTO
            createdAt = System.currentTimeMillis()
        )
    }

    /**
     * Convert a FixtureEntity to a domain MatchFixture.
     */
    fun FixtureEntity.toDomain(): MatchFixture {
        // Parse date string from entity (format: "YYYY-MM-DDTHH:MM:SS+00:00")
        val apiDate = try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
            inputFormat.parse(date)
        } catch (e: Exception) {
            // Fallback to current date if parsing fails
            Date()
        }

        // Format date for display (e.g., "Zo 14 dec")
        val dateFormatter = SimpleDateFormat("EE dd MMM", Locale("nl", "NL"))
        val formattedDate = dateFormatter.format(apiDate)

        // Format time for display (e.g., "14:30")
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val formattedTime = timeFormatter.format(apiDate)

        return MatchFixture(
            homeTeam = homeTeam,
            awayTeam = awayTeam,
            date = formattedDate,
            time = formattedTime,
            league = leagueName,
            status = statusShort,
            elapsed = elapsed,
            fixtureId = id,
            leagueId = null, // Not stored in entity
            leagueCountry = leagueCountry,
            leagueLogo = null // Not stored in entity
        )
    }

    /**
     * Regular function to convert FixtureItemDto to domain (alternative to extension function).
     */
    fun mapFixtureItemToDomain(fixture: FixtureItemDto): MatchFixture {
        return fixture.toDomain()
    }

    /**
     * Regular function to convert LiveMatchDto to domain (alternative to extension function).
     */
    fun mapLiveMatchToDomain(liveMatch: LiveMatchDto): MatchFixture {
        return liveMatch.toDomain()
    }

    /**
     * Regular function to convert FixtureEntity to domain (alternative to extension function).
     */
    fun mapFixtureEntityToDomain(entity: FixtureEntity): MatchFixture {
        return entity.toDomain()
    }

    /**
     * Convert a LiveMatchDto to a domain MatchFixture with goals.
     */
    fun LiveMatchDto.toDomain(): MatchFixture {
        // Parse date string from API (format: "YYYY-MM-DDTHH:MM:SS+00:00")
        val apiDate = try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
            inputFormat.parse(fixture.date)
        } catch (e: Exception) {
            // Fallback to current date if parsing fails
            Date()
        }

        // Format date for display (e.g., "Zo 14 dec")
        val dateFormatter = SimpleDateFormat("EE dd MMM", Locale("nl", "NL"))
        val formattedDate = dateFormatter.format(apiDate)

        // Format time for display (e.g., "14:30")
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val formattedTime = timeFormatter.format(apiDate)

        // Parse goals from JsonElement
        val homeGoals = parseGoals(goals, isHome = true)
        val awayGoals = parseGoals(goals, isHome = false)

        return MatchFixture(
            homeTeam = teams.home.name,
            awayTeam = teams.away.name,
            homeTeamId = teams.home.id,
            awayTeamId = teams.away.id,
            homeScore = homeGoals,
            awayScore = awayGoals,
            status = fixture.status?.short,
            elapsed = fixture.status?.elapsed,
            date = formattedDate,
            time = formattedTime,
            league = league.name,
            fixtureId = fixture.id,
            leagueId = league.id,
            leagueCountry = league.country,
            leagueLogo = league.logo
        )
    }

    /**
     * Convert TeamsSearchResponse to TeamSelectionResult domain model.
     */
    fun mapTeamsSearchResponseToDomain(response: com.Lyno.matchmindai.data.dto.TeamsSearchResponse): com.Lyno.matchmindai.domain.model.TeamSelectionResult {
        return com.Lyno.matchmindai.domain.model.TeamSelectionResult(
            teamId = response.team.id,
            teamName = response.team.name,
            country = response.venue?.city ?: "Unknown",
            logoUrl = response.team.logo,
            leagueId = null, // Not available in search response
            leagueName = null, // Not available in search response
            isFavorite = false
        )
    }

    /**
     * Parse goals from MatchGoalsDto to extract home/away scores.
     */
    private fun parseGoals(goals: com.Lyno.matchmindai.data.dto.football.MatchGoalsDto?, isHome: Boolean): Int {
        return when {
            goals == null -> 0
            isHome -> goals.home ?: 0
            else -> goals.away ?: 0
        }
    }

    /**
     * Convert StandingsResponseDto to list of StandingRow domain models.
     */
    fun mapStandingsResponseToStandingRows(standingsResponse: com.Lyno.matchmindai.data.dto.football.StandingsResponseDto): List<com.Lyno.matchmindai.domain.model.StandingRow> {
        if (standingsResponse.response.isEmpty()) {
            return emptyList()
        }

        val leagueResponse = standingsResponse.response.first()
        val leagueDetails = leagueResponse.league
        
        // Map standings (List<List<StandingItemDto>>) to StandingRow domain models
        return leagueDetails.standings.flatMap { group ->
            group.map { standingItem ->
                com.Lyno.matchmindai.domain.model.StandingRow(
                    rank = standingItem.rank,
                    team = standingItem.team.name,
                    teamLogo = standingItem.team.logo,
                    points = standingItem.points,
                    played = standingItem.all.played,
                    wins = standingItem.all.win,
                    draws = standingItem.all.draw,
                    losses = standingItem.all.lose,
                    goalsFor = standingItem.all.goals.`for`,
                    goalsAgainst = standingItem.all.goals.against,
                    goalDiff = standingItem.goalsDiff,
                    form = standingItem.form ?: "",
                    description = standingItem.description ?: ""
                )
            }
        }
    }

    /**
     * Convert StandingsResponse (remote DTO) to list of StandingRow domain models.
     */
    fun mapStandingsResponseToStandingRows(standingsResponse: com.Lyno.matchmindai.data.remote.dto.StandingsResponse): List<com.Lyno.matchmindai.domain.model.StandingRow> {
        if (standingsResponse.response.isEmpty()) {
            return emptyList()
        }

        val leagueNode = standingsResponse.response.first()
        val leagueDetails = leagueNode.league ?: return emptyList()
        
        // Map standings (List<List<StandingTeamDto>>) to StandingRow domain models
        return leagueDetails.standings.flatMap { group ->
            group.map { standingTeam ->
                val stats = standingTeam.all ?: com.Lyno.matchmindai.data.remote.dto.StandingStatsDto(
                    played = 0,
                    win = 0,
                    draw = 0,
                    lose = 0,
                    goals = null
                )
                val goals = stats.goals ?: com.Lyno.matchmindai.data.remote.dto.GoalsDto(
                    forGoals = 0,
                    againstGoals = 0
                )
                
                com.Lyno.matchmindai.domain.model.StandingRow(
                    rank = standingTeam.rank,
                    team = standingTeam.team.name,
                    teamLogo = standingTeam.team.logo,
                    points = standingTeam.points,
                    played = stats.played,
                    wins = stats.win,
                    draws = stats.draw,
                    losses = stats.lose,
                    goalsFor = goals.forGoals,
                    goalsAgainst = goals.againstGoals,
                    goalDiff = standingTeam.goalsDiff,
                    form = standingTeam.form ?: "",
                    description = standingTeam.description ?: ""
                )
            }
        }
    }
}
