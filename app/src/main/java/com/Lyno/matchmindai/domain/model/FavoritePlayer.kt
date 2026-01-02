package com.Lyno.matchmindai.domain.model

/**
 * Domain model representing a favorite player.
 * Part of FavoX Fase 4: AI-Gedreven Intelligentie.
 */
data class FavoritePlayer(
    val playerId: Int,
    val playerName: String,
    val teamId: Int? = null,
    val teamName: String? = null,
    val position: String? = null,
    val nationality: String? = null,
    val photoUrl: String? = null,
    val addedAt: Long = System.currentTimeMillis(),
    val watchPriority: Int = 0 // 0-10, higher = more important to watch
) {
    /**
     * Check if this player is a key player (based on position).
     */
    fun isKeyPlayer(): Boolean {
        val keyPositions = setOf("Forward", "Attacking Midfielder", "Striker")
        return position in keyPositions
    }

    /**
     * Get player impact score for AI analysis.
     */
    fun getImpactScore(): Int {
        var score = 0
        
        // Base score based on position
        when (position) {
            "Forward", "Striker" -> score += 80
            "Attacking Midfielder" -> score += 70
            "Midfielder" -> score += 60
            "Defender" -> score += 50
            "Goalkeeper" -> score += 40
            else -> score += 30
        }
        
        // Add watch priority bonus
        score += (watchPriority * 10)
        
        return score
    }

    /**
     * Get player description for AI prompts.
     */
    fun getAiDescription(): String {
        val desc = StringBuilder()
        desc.append(playerName)
        
        position?.let { desc.append(" ($it)") }
        teamName?.let { desc.append(" van $it") }
        nationality?.let { desc.append(" ($it)") }
        
        return desc.toString()
    }
}
