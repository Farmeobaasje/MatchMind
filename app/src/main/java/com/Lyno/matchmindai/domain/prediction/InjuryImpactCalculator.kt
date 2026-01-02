package com.Lyno.matchmindai.domain.prediction

/**
 * Injury Impact Calculator
 * 
 * Calculates the impact of player injuries on match predictions.
 * Considers player roles, severity, and team depth.
 */
class InjuryImpactCalculator {

    /**
     * Calculate injury correction factor (0.0-1.0).
     * Higher injuries = lower correction factor (reduces confidence and adjusts score).
     * 
     * @param injuries List of player injuries
     * @return Correction factor where 1.0 = no impact, 0.4 = maximum impact
     */
    fun calculateInjuryCorrection(injuries: List<PlayerInjury>): Float {
        if (injuries.isEmpty()) return 1.0f
        
        var keyPlayerImpact = 0.0f
        for (injury in injuries) {
            keyPlayerImpact += calculatePlayerImpact(injury)
        }
        
        val totalImpact = keyPlayerImpact.coerceAtMost(0.6f)
        return 1.0f - totalImpact
    }

    /**
     * Calculate impact of a single player injury.
     */
    private fun calculatePlayerImpact(injury: PlayerInjury): Float {
        // Base impact based on player role
        val roleImpact = when (injury.role) {
            "Goalkeeper" -> 0.3f
            "Defender" -> 0.2f
            "Midfielder" -> 0.15f
            "Forward" -> 0.25f
            else -> 0.1f
        }
        
        // Adjust based on injury severity
        val severityMultiplier = when (injury.severity) {
            "Long-term" -> 1.5f
            "Medium-term" -> 1.2f
            "Short-term" -> 1.0f
            "Doubtful" -> 0.7f
            else -> 0.5f
        }
        
        // Adjust based on player importance
        val importanceMultiplier = when (injury.importance) {
            "Key Player" -> 1.5f
            "Regular Starter" -> 1.2f
            "Rotation Player" -> 1.0f
            "Backup" -> 0.7f
            else -> 0.5f
        }
        
        return roleImpact * severityMultiplier * importanceMultiplier
    }

    /**
     * Calculate team-specific injury impact.
     * 
     * @param injuries List of player injuries
     * @param teamType "HOME" or "AWAY"
     * @return Impact score (0.0-1.0) where 1.0 = maximum impact
     */
    fun calculateTeamInjuryImpact(
        injuries: List<PlayerInjury>,
        teamType: String
    ): Float {
        val teamInjuries = injuries.filter { it.teamAffected == teamType }
        if (teamInjuries.isEmpty()) return 0.0f
        
        var totalImpact = 0.0f
        for (injury in teamInjuries) {
            totalImpact += calculatePlayerImpact(injury)
        }
        return totalImpact.coerceAtMost(1.0f)
    }

    /**
     * Get injury summary for reasoning.
     */
    fun getInjurySummary(injuries: List<PlayerInjury>): String {
        if (injuries.isEmpty()) return "No significant injuries"
        
        val keyInjuries = injuries.filter { it.importance == "Key Player" }
        val totalInjuries = injuries.size
        
        return when {
            keyInjuries.size >= 3 -> "Critical injury situation with ${keyInjuries.size} key players out"
            keyInjuries.size >= 2 -> "Major injury concern with ${keyInjuries.size} key players missing"
            keyInjuries.size == 1 -> "One key player injured"
            totalInjuries >= 5 -> "Multiple injuries (${totalInjuries} players affected)"
            totalInjuries >= 3 -> "Several injuries affecting squad depth"
            else -> "Minor injury concerns"
        }
    }

    /**
     * Calculate positional impact (e.g., all defenders injured).
     */
    fun calculatePositionalImpact(
        injuries: List<PlayerInjury>,
        position: String
    ): Float {
        val positionInjuries = injuries.filter { it.role == position }
        if (positionInjuries.isEmpty()) return 0.0f
        
        var totalImpact = 0.0f
        for (injury in positionInjuries) {
            totalImpact += calculatePlayerImpact(injury)
        }
        
        // Positional crisis multiplier (e.g., all center backs injured)
        val crisisMultiplier = when {
            positionInjuries.size >= 3 -> 1.5f
            positionInjuries.size >= 2 -> 1.3f
            else -> 1.0f
        }
        
        return (totalImpact * crisisMultiplier).coerceAtMost(1.0f)
    }

    /**
     * Quick assessment for the 3-0 bias fix.
     */
    fun hasCriticalInjuries(injuries: List<PlayerInjury>): Boolean {
        val criticalCount = injuries.count { 
            it.importance == "Key Player" && 
            (it.severity == "Long-term" || it.severity == "Medium-term")
        }
        return criticalCount >= 2
    }

    /**
     * Get most significant injury.
     */
    fun getMostSignificantInjury(injuries: List<PlayerInjury>): PlayerInjury? {
        return injuries.maxByOrNull { 
            calculatePlayerImpact(it) 
        }
    }
}

/**
 * Data class for player injury information.
 */
data class PlayerInjury(
    val playerName: String,
    val role: String, // Goalkeeper, Defender, Midfielder, Forward
    val severity: String, // Long-term, Medium-term, Short-term, Doubtful
    val importance: String, // Key Player, Regular Starter, Rotation Player, Backup
    val teamAffected: String, // HOME or AWAY
    val expectedReturn: String? = null
)
