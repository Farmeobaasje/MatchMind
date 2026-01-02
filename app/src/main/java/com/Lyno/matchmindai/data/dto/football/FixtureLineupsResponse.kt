package com.Lyno.matchmindai.data.dto.football

import kotlinx.serialization.Serializable

/**
 * DTO for fixture lineups response from API-SPORTS /fixtures/lineups endpoint.
 * Used for lineup strength analysis in Voorspellingen-X.
 */
@Serializable
data class FixtureLineupsResponse(
    val team: LineupTeam,
    val coach: LineupCoach? = null,
    val formation: String,
    val startXI: List<PlayerPosition>,
    val substitutes: List<PlayerPosition>,
    val missing: List<MissingPlayer>? = emptyList()
)

@Serializable
data class LineupTeam(
    val id: Int,
    val name: String,
    val logo: String?,
    val colors: TeamColors? = null
)

@Serializable
data class TeamColors(
    val player: ColorInfo? = null,
    val goalkeeper: ColorInfo? = null
)

@Serializable
data class ColorInfo(
    val primary: String? = null,
    val number: String? = null,
    val border: String? = null
)

@Serializable
data class LineupCoach(
    val id: Int? = null,
    val name: String? = null,
    val photo: String? = null
)

@Serializable
data class PlayerPosition(
    val player: PlayerInfo,
    val position: String? = null
)

@Serializable
data class PlayerInfo(
    val id: Int? = null,
    val name: String,
    val number: Int? = null,
    val pos: String? = null,
    val grid: String? = null,
    val photo: String? = null
)

@Serializable
data class MissingPlayer(
    val player: MissingPlayerInfo,
    val reason: String? = null
)

@Serializable
data class MissingPlayerInfo(
    val id: Int? = null,
    val name: String,
    val photo: String? = null
)

/**
 * Wrapper for the full lineups response (both teams).
 * Handles both empty arrays (for future matches) and populated arrays (for played matches).
 */
@Serializable
data class FixtureLineupsWrapper(
    val response: List<FixtureLineupsResponse?>
)
