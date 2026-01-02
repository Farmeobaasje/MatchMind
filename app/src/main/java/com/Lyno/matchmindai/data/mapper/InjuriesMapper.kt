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
     * 
     * ENHANCED: Filters out players that don't belong to the expected teams
     * and logs data inconsistencies for debugging.
     */
    fun mapToDomain(response: InjuriesResponseDto): List<Injury> {
        val allInjuries = response.response.map { playerInjury ->
            mapPlayerInjuryToDomain(playerInjury)
        }
        
        // Log data quality for debugging
        logDataQuality(allInjuries)
        
        return allInjuries
    }
    
    /**
     * Map InjuriesResponseDto to list of domain Injury models with team filtering.
     * 
     * @param response The API response
     * @param expectedTeamNames List of expected team names (e.g., ["Manchester United", "Newcastle"])
     * @return Filtered list of injuries only for the expected teams
     */
    fun mapToDomainWithTeamFilter(
        response: InjuriesResponseDto,
        expectedTeamNames: List<String>
    ): List<Injury> {
        val allInjuries = response.response.map { playerInjury ->
            mapPlayerInjuryToDomain(playerInjury)
        }
        
        // Filter by expected teams
        val filteredInjuries = allInjuries.filter { injury ->
            expectedTeamNames.any { expectedTeam ->
                injury.team.equals(expectedTeam, ignoreCase = true)
            }
        }
        
        // Log data inconsistencies
        val filteredOutCount = allInjuries.size - filteredInjuries.size
        if (filteredOutCount > 0) {
            android.util.Log.w("InjuriesMapper", 
                "Filtered out $filteredOutCount injuries not belonging to expected teams: $expectedTeamNames")
            
            // Log specific filtered out players for debugging
            val filteredOutPlayers = allInjuries.filter { injury ->
                !expectedTeamNames.any { expectedTeam ->
                    injury.team.equals(expectedTeam, ignoreCase = true)
                }
            }.take(3) // Log first 3 for brevity
            
            filteredOutPlayers.forEach { injury ->
                android.util.Log.d("InjuriesMapper", 
                    "Filtered out: ${injury.playerName} (${injury.team}) - API data inconsistency")
            }
        }
        
        // Log data quality
        logDataQuality(filteredInjuries)
        
        return filteredInjuries
    }
    
    /**
     * Log data quality metrics for debugging.
     */
    private fun logDataQuality(injuries: List<Injury>) {
        if (injuries.isEmpty()) {
            android.util.Log.d("InjuriesMapper", "No injuries data available")
            return
        }
        
        // Group by team for analysis
        val injuriesByTeam = injuries.groupBy { it.team }
        
        android.util.Log.d("InjuriesMapper", 
            "Injuries data quality: ${injuries.size} total injuries across ${injuriesByTeam.size} teams")
        
        injuriesByTeam.forEach { (team, teamInjuries) ->
            android.util.Log.d("InjuriesMapper", 
                "  $team: ${teamInjuries.size} injuries")
        }
        
        // Check for data inconsistencies
        val unknownTypeCount = injuries.count { it.type == "Onbekend" }
        val unknownReasonCount = injuries.count { it.reason == "Onbekende reden" }
        
        if (unknownTypeCount > 0 || unknownReasonCount > 0) {
            android.util.Log.w("InjuriesMapper", 
                "Data quality issues: $unknownTypeCount unknown types, $unknownReasonCount unknown reasons")
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
