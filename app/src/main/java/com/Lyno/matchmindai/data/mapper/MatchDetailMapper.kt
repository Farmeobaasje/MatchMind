package com.Lyno.matchmindai.data.mapper

import com.Lyno.matchmindai.data.dto.football.*
import com.Lyno.matchmindai.domain.model.MatchDetail
import com.Lyno.matchmindai.domain.model.MatchInfo
import com.Lyno.matchmindai.domain.model.MatchLineups
import com.Lyno.matchmindai.domain.model.MatchScore
import com.Lyno.matchmindai.domain.model.MatchStatus
import com.Lyno.matchmindai.domain.model.MatchVenue
import com.Lyno.matchmindai.domain.model.ScorePeriod
import com.Lyno.matchmindai.domain.model.StatItem
import com.Lyno.matchmindai.domain.model.TeamLineup
import com.Lyno.matchmindai.domain.model.StandingRow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * Mapper for converting MatchDetails DTO to MatchDetail domain model.
 * Handles complex API data with null safety and proper team matching.
 */
object MatchDetailMapper {

    /**
     * Maps MatchDetails DTO to MatchDetail domain model.
     * Critical: Uses team.id matching instead of index assumptions.
     */
    fun mapMatchDetailsToDomain(matchDetails: MatchDetails): MatchDetail {
        return mapMatchDetailsToDomain(matchDetails, null)
    }

    /**
     * Maps MatchDetails DTO to MatchDetail domain model with optional standings.
     * Critical: Uses team.id matching instead of index assumptions.
     */
    fun mapMatchDetailsToDomain(
        matchDetails: MatchDetails,
        standingsResponse: StandingsResponseDto?
    ): MatchDetail {
        val fixture = matchDetails.fixture
        val league = matchDetails.league
        val teams = matchDetails.teams
        val goals = matchDetails.goals
        val score = matchDetails.score
        
        // Get home and away team IDs for matching
        val homeTeamId = teams?.home?.id
        val awayTeamId = teams?.away?.id
        
        // Get team logos
        val homeTeamLogo = teams?.home?.logo
        val awayTeamLogo = teams?.away?.logo
        
        // Map statistics with team ID matching
        val stats = mapStatistics(matchDetails.statistics, homeTeamId, awayTeamId)
        
        // Map lineups with team ID matching
        val lineups = mapLineups(matchDetails.lineups, homeTeamId, awayTeamId)
        
        // Map events
        val events = mapEvents(matchDetails.events)
        
        // Map match info
        val matchInfo = mapMatchInfo(fixture, league, teams)
        
        // Map score
        val matchScore = mapScore(goals, score)
        
        // Map status
        val matchStatus = mapStatus(fixture?.status)
        
        // Map standings if available
        val standings = mapStandings(standingsResponse)
        
        return MatchDetail(
            fixtureId = fixture?.id ?: 0,
            homeTeam = teams?.home?.name ?: "Home Team",
            awayTeam = teams?.away?.name ?: "Away Team",
            homeTeamId = homeTeamId,
            awayTeamId = awayTeamId,
            homeTeamLogo = homeTeamLogo,
            awayTeamLogo = awayTeamLogo,
            league = league?.name ?: "Unknown League",
            leagueId = league?.id,
            leagueLogo = league?.flag,
            info = matchInfo,
            stats = stats,
            lineups = lineups,
            events = events,
            score = matchScore,
            status = matchStatus,
            standings = standings
        )
    }

    /**
     * Maps statistics with team ID matching.
     * Critical: Finds home/away stats by comparing team.id, not by index.
     */
    private fun mapStatistics(
        statistics: List<com.Lyno.matchmindai.data.dto.football.TeamStatistics>?,
        homeTeamId: Int?,
        awayTeamId: Int?
    ): List<StatItem> {
        if (statistics.isNullOrEmpty() || homeTeamId == null || awayTeamId == null) {
            return emptyList()
        }

        // Find home and away statistics by team ID
        val homeStats = statistics.find { it.team?.id == homeTeamId }
        val awayStats = statistics.find { it.team?.id == awayTeamId }

        if (homeStats == null || awayStats == null) {
            return emptyList()
        }

        // Create map of stat types to values for easier comparison
        val homeStatMap = homeStats.statistics?.associateBy { it.type ?: "" } ?: emptyMap()
        val awayStatMap = awayStats.statistics?.associateBy { it.type ?: "" } ?: emptyMap()

        // Get all unique stat types
        val allStatTypes = (homeStatMap.keys + awayStatMap.keys).distinct()

        return allStatTypes.mapNotNull { statType ->
            val homeStat = homeStatMap[statType]
            val awayStat = awayStatMap[statType]

            if (homeStat != null && awayStat != null) {
                val (homeValue, homeDisplay) = parseStatValue(homeStat.value)
                val (awayValue, awayDisplay) = parseStatValue(awayStat.value)
                
                StatItem(
                    type = statType,
                    homeValue = homeValue,
                    awayValue = awayValue,
                    unit = if (homeDisplay.toString().endsWith("%") || awayDisplay.toString().endsWith("%")) "%" else ""
                )
            } else {
                null
            }
        }
    }

    /**
     * Parses statistic value from API (can be Int, String, or null).
     * Returns integer value for domain model and display string.
     */
    private fun parseStatValue(value: JsonElement?): Pair<Int, String> {
        // Extract raw value from JsonElement
        val rawValue = value?.jsonPrimitive?.contentOrNull ?: "0"
        
        return when {
            rawValue.toIntOrNull() != null -> {
                val intValue = rawValue.toInt()
                intValue to rawValue
            }
            rawValue.endsWith("%") -> {
                val percentage = rawValue.dropLast(1).toIntOrNull() ?: 0
                percentage to rawValue
            }
            else -> 0 to rawValue
        }
    }

    /**
     * Maps lineups with team ID matching.
     * Critical: Finds home/away lineups by comparing team.id.
     */
    private fun mapLineups(
        lineups: List<Lineup>?,
        homeTeamId: Int?,
        awayTeamId: Int?
    ): MatchLineups {
        if (lineups.isNullOrEmpty() || homeTeamId == null || awayTeamId == null) {
            return MatchLineups(
                home = TeamLineup(teamName = "Home Team", players = emptyList()),
                away = TeamLineup(teamName = "Away Team", players = emptyList())
            )
        }

        // Find home and away lineups by team ID
        val homeLineup = lineups.find { it.team?.id == homeTeamId }
        val awayLineup = lineups.find { it.team?.id == awayTeamId }

        return MatchLineups(
            home = mapTeamLineup(homeLineup, "Home Team"),
            away = mapTeamLineup(awayLineup, "Away Team")
        )
    }

    /**
     * Maps a single lineup to TeamLineup domain model.
     */
    private fun mapTeamLineup(lineup: Lineup?, defaultTeamName: String): TeamLineup {
        val players = lineup?.startXI?.mapNotNull { lineupPlayer ->
            lineupPlayer.player?.name?.let { playerName ->
                com.Lyno.matchmindai.domain.model.LineupPlayer(
                    name = playerName,
                    number = null, // API doesn't provide number in lineup
                    position = lineupPlayer.position,
                    grid = lineupPlayer.grid,
                    isCaptain = false, // API doesn't provide captain info
                    isSubstitute = false
                )
            }
        } ?: emptyList()

        val substitutes = lineup?.substitutes?.mapNotNull { subPlayer ->
            subPlayer.player?.name?.let { playerName ->
                com.Lyno.matchmindai.domain.model.LineupPlayer(
                    name = playerName,
                    number = null,
                    position = subPlayer.position,
                    grid = subPlayer.grid,
                    isCaptain = false,
                    isSubstitute = true
                )
            }
        } ?: emptyList()

        return TeamLineup(
            teamName = lineup?.team?.name ?: defaultTeamName,
            formation = lineup?.formation,
            coach = lineup?.coach?.name,
            players = players,
            substitutes = substitutes
        )
    }

    /**
     * Maps events to domain model.
     */
    private fun mapEvents(events: List<com.Lyno.matchmindai.data.dto.football.MatchEvent>?): List<com.Lyno.matchmindai.domain.model.MatchEvent> {
        return events?.mapNotNull { event ->
            val eventType = mapEventType(event.type, event.detail)
            val minute = event.time?.elapsed ?: 0
            val extraMinute = event.time?.extra
            
            com.Lyno.matchmindai.domain.model.MatchEvent(
                type = eventType,
                minute = minute,
                extraMinute = extraMinute,
                team = event.team?.name ?: "Unknown",
                player = event.player?.name,
                assist = event.assist?.name,
                detail = event.detail,
                comments = event.comments
            )
        } ?: emptyList()
    }

    /**
     * Maps API event type to domain EventType.
     */
    private fun mapEventType(type: String?, detail: String?): com.Lyno.matchmindai.domain.model.EventType {
        return when {
            type == "Goal" -> com.Lyno.matchmindai.domain.model.EventType.GOAL
            type == "Card" && detail?.contains("Yellow") == true -> com.Lyno.matchmindai.domain.model.EventType.YELLOW_CARD
            type == "Card" && detail?.contains("Red") == true -> com.Lyno.matchmindai.domain.model.EventType.RED_CARD
            type == "subst" -> com.Lyno.matchmindai.domain.model.EventType.SUBSTITUTION
            type == "Var" -> com.Lyno.matchmindai.domain.model.EventType.VAR
            detail?.contains("Penalty") == true -> com.Lyno.matchmindai.domain.model.EventType.PENALTY
            detail?.contains("Own Goal") == true -> com.Lyno.matchmindai.domain.model.EventType.OWN_GOAL
            detail?.contains("Missed Penalty") == true -> com.Lyno.matchmindai.domain.model.EventType.MISSED_PENALTY
            else -> com.Lyno.matchmindai.domain.model.EventType.OTHER
        }
    }

    /**
     * Maps match information.
     */
    private fun mapMatchInfo(
        fixture: FixtureDetails?,
        league: LeagueDetailsDto?,
        teams: TeamsDetailsDto?
    ): MatchInfo {
        val venue = fixture?.venue?.let { venue ->
            com.Lyno.matchmindai.domain.model.MatchVenue(
                name = venue.name ?: "Unknown",
                city = venue.city
            )
        }

        return MatchInfo(
            stadium = fixture?.venue?.name,
            referee = fixture?.referee,
            date = fixture?.date,
            time = fixture?.date?.let { extractTimeFromDateTime(it) },
            timestamp = fixture?.timestamp,
            venue = venue
        )
    }

    /**
     * Maps score information.
     */
    private fun mapScore(goals: Goals?, score: ScoreDetails?): MatchScore? {
        return if (goals?.home != null && goals.away != null) {
            MatchScore(
                home = goals.home,
                away = goals.away,
                halftime = score?.halftime?.let { ScorePeriod(it.home, it.away) },
                fulltime = score?.fulltime?.let { ScorePeriod(it.home, it.away) }
            )
        } else {
            null
        }
    }

    /**
     * Maps status information.
     */
    private fun mapStatus(statusDto: StatusDto?): MatchStatus? {
        return statusDto?.short?.let { shortStatus ->
            when (shortStatus) {
                "1H", "HT", "2H", "ET", "BT", "P", "LIVE" -> MatchStatus.LIVE
                "FT", "AET", "PEN" -> MatchStatus.FINISHED
                "NS", "TBD" -> MatchStatus.SCHEDULED
                else -> MatchStatus.UNKNOWN
            }
        }
    }

    /**
     * Extracts time from ISO datetime string with timezone conversion.
     * Converts UTC time from API to local timezone.
     */
    private fun extractTimeFromDateTime(dateTime: String): String {
        return try {
            // Format: "2024-01-15T19:00:00+00:00"
            val timePart = dateTime.substringAfter("T").substringBefore("+")
            val hours = timePart.substringBefore(":").toInt()
            val minutes = timePart.substringAfter(":").substringBefore(":")
            String.format("%02d:%02d", hours, minutes.toInt())
        } catch (e: Exception) {
            "00:00"
        }
    }

    /**
     * Maps standings response to domain model.
     * Handles the nested List<List<StandingItemDto>> structure by flattening it.
     */
    private fun mapStandings(standingsResponse: StandingsResponseDto?): List<StandingRow>? {
        if (standingsResponse == null || standingsResponse.response.isEmpty()) {
            return null
        }

        val leagueResponse = standingsResponse.response.firstOrNull() ?: return null
        val leagueStandings = leagueResponse.league.standings
        
        // Flatten the nested list structure: List<List<StandingItemDto>> -> List<StandingItemDto>
        val flattenedStandings = leagueStandings.flatten()
        
        return flattenedStandings.mapNotNull { standingItem ->
            val team = standingItem.team
            val allStats = standingItem.all
            val homeStats = standingItem.home
            val awayStats = standingItem.away
            
            StandingRow(
                rank = standingItem.rank,
                team = team?.name ?: "Unknown Team",
                teamLogo = team?.logo,
                points = standingItem.points,
                played = allStats?.played ?: 0,
                wins = allStats?.win ?: 0,
                draws = allStats?.draw ?: 0,
                losses = allStats?.lose ?: 0,
                goalsFor = allStats?.goals?.`for` ?: 0,
                goalsAgainst = allStats?.goals?.against ?: 0,
                goalDiff = standingItem.goalsDiff,
                form = standingItem.form,
                description = standingItem.description
            )
        }
    }

    /**
     * Creates an empty match detail for error states.
     */
    fun createEmptyMatchDetail(
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
            leagueId = null,
            info = MatchInfo(stadium = "Nog geen data beschikbaar"),
            stats = emptyList(),
            lineups = MatchLineups(
                home = TeamLineup(teamName = homeTeam, players = emptyList()),
                away = TeamLineup(teamName = awayTeam, players = emptyList())
            ),
            events = emptyList()
        )
    }
}
