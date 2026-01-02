package com.Lyno.matchmindai.domain.model

/**
 * Domain model representing a favorite league.
 * Part of FavoX Fase 4: AI-Gedreven Intelligentie.
 */
data class FavoriteLeague(
    val leagueId: Int,
    val leagueName: String,
    val country: String,
    val logoUrl: String? = null,
    val addedAt: Long = System.currentTimeMillis(),
    val priority: Int = 0 // 0-10, higher = more important to user
) {
    /**
     * Check if this league is a top-tier league.
     */
    fun isTopTier(): Boolean {
        // Premier League, La Liga, Bundesliga, Serie A, Ligue 1, Eredivisie
        return leagueId in setOf(39, 140, 78, 135, 61, 88)
    }

    /**
     * Get league importance score for AI analysis.
     */
    fun getImportanceScore(): Int {
        var score = 0
        
        // Base score based on tier
        when {
            isTopTier() -> score += 100
            leagueId in 1..100 -> score += 50  // Professional leagues
            else -> score += 20                // Lower divisions
        }
        
        // Add priority bonus
        score += (priority * 10)
        
        return score
    }
}
