package com.Lyno.matchmindai.data.mapper

import com.Lyno.matchmindai.data.dto.football.InjuriesResponseDto
import com.Lyno.matchmindai.data.dto.football.PlayerInjuryDto
import com.Lyno.matchmindai.data.dto.football.SimplifiedInjuryDto
import com.Lyno.matchmindai.domain.model.Injury
import com.Lyno.matchmindai.domain.model.InjurySeverity

/**
 * Mapper for converting injury DTOs to domain models.
 * Handles transformation of API-SPORTS injury data to domain models for AI analysis.
 * 
 * UPDATED FOR FLAT API RESPONSE STRUCTURE:
 * The API returns a flat list of injuries, not nested structure.
 */
object InjuriesMapper {

    /**
     * Map InjuriesResponseDto to list of domain Injury models.
     * Now handles flat structure: response is List<PlayerInjuryDto>
     */
    fun mapToDomain(response: InjuriesResponseDto): List<Injury> {
        return response.response.map { playerInjury ->
            mapPlayerInjuryToDomain(playerInjury)
        }
    }

    /**
     * Map PlayerInjuryDto to domain Injury model.
     * Uses direct fields from the flat structure.
     */
    private fun mapPlayerInjuryToDomain(
        playerInjury: PlayerInjuryDto
    ): Injury {
        val player = playerInjury.player
        val team = playerInjury.team
        
        // Use type and reason from player object (or from direct fields if available)
        val injuryType = playerInjury.type ?: player.type ?: "Onbekend"
        val injuryReason = playerInjury.reason ?: player.reason ?: "Onbekende reden"
        
        return Injury(
            playerName = player.name,
            team = team.name,
            type = injuryType,
            reason = injuryReason,
            expectedReturn = null // API doesn't provide expected return in this endpoint
        )
    }

    /**
     * Map SimplifiedInjuryDto to domain Injury model.
     * Used for AI analysis with simplified data.
     */
    fun mapSimplifiedToDomain(simplified: SimplifiedInjuryDto): Injury {
        return Injury(
            playerName = simplified.playerName,
            team = simplified.teamName,
            type = simplified.injuryType,
            reason = "Blessure",
            expectedReturn = simplified.expectedReturn
        )
    }


    /**
     * Filter injuries by team.
     */
    fun filterByTeam(injuries: List<Injury>, teamName: String): List<Injury> {
        return injuries.filter { it.team.equals(teamName, ignoreCase = true) }
    }

    /**
     * Get injuries by severity.
     */
    fun filterBySeverity(injuries: List<Injury>, severity: InjurySeverity): List<Injury> {
        // Since we don't have severity in the simplified Injury model,
        // we'll return all injuries for now
        return injuries
    }

    /**
     * Get key injuries (high impact or key players).
     */
    fun getKeyInjuries(injuries: List<Injury>): List<Injury> {
        // Simplified logic for now
        return injuries.take(3) // Return first 3 as key injuries
    }

    /**
     * Create summary text for beginners.
     */
    fun createInjurySummary(injuries: List<Injury>): String {
        if (injuries.isEmpty()) {
            return "Geen blessures gemeld voor deze wedstrijd."
        }

        val keyInjuries = getKeyInjuries(injuries)
        val totalInjuries = injuries.size

        return buildString {
            append("Er zijn $totalInjuries blessure(s) gemeld. ")
            
            if (keyInjuries.isNotEmpty()) {
                append("Belangrijke blessures: ")
                keyInjuries.take(3).forEachIndexed { index, injury ->
                    if (index > 0) append(", ")
                    append("${injury.playerName} (${injury.team})")
                }
                if (keyInjuries.size > 3) {
                    append(" en ${keyInjuries.size - 3} andere")
                }
                append(".")
            }
        }
    }
}
