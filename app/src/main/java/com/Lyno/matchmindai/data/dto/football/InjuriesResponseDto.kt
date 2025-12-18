package com.Lyno.matchmindai.data.dto.football

import kotlinx.serialization.Serializable

/**
 * DTO for the injuries response from API-SPORTS V3.
 * This endpoint returns injury information for a specific fixture.
 * 
 * This data is crucial for AI analysis as injuries significantly impact
 * match outcomes and should be considered in predictions.
 * 
 * ACTUAL API RESPONSE STRUCTURE:
 * {
 *   "get": "injuries",
 *   "parameters": {"fixture": "686314"},
 *   "response": [
 *     {
 *       "player": { "id": 865, "name": "D. Costa", "type": "Missing Fixture", "reason": "Broken ankle" },
 *       "team": { "id": 157, "name": "Bayern Munich" },
 *       "fixture": { "id": 686314, "date": "2021-04-07T19:00:00+00:00" },
 *       "league": { "id": 2, "season": 2020, "name": "UEFA Champions League" }
 *     }
 *   ]
 * }
 */
@Serializable
data class InjuriesResponseDto(
    val get: String,
    val parameters: Map<String, String>,
    val errors: List<String>,
    val results: Int,
    val paging: PagingDto,
    val response: List<PlayerInjuryDto>  // Direct flat list, not nested!
)

/**
 * DTO for player-specific injury information.
 * Contains details about the injury, player, team, fixture, and league.
 * 
 * ACTUAL STRUCTURE: Flat object with player, team, fixture, league fields
 */
@Serializable
data class PlayerInjuryDto(
    val player: PlayerDto,
    val team: TeamDto,
    val fixture: FixtureDetailsDto,
    val league: LeagueDetailsDto,
    val type: String? = null,  // Direct field from player object
    val reason: String? = null  // Direct field from player object
)

/**
 * DTO for player information in injury context.
 */
@Serializable
data class PlayerDto(
    val id: Int,
    val name: String,
    val photo: String? = null,
    val type: String? = null, // "Expected" or "Doubtful" or "Missing Fixture"
    val reason: String? = null
)

/**
 * DTO for simplified injury data for AI analysis.
 * Used to extract key injury information for beginners.
 */
@Serializable
data class SimplifiedInjuryDto(
    val fixtureId: Int,
    val teamName: String,
    val playerName: String,
    val injuryType: String,
    val severity: String,
    val expectedReturn: String? = null,
    val impactRating: Double = 0.0, // 0-100 rating for match impact
    val isKeyPlayer: Boolean = false,
    val position: String? = null
)
