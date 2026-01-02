package com.Lyno.matchmindai.domain.service

import com.Lyno.matchmindai.domain.model.MatchFixture
import com.Lyno.matchmindai.domain.model.StandingRow

/**
 * Service responsible for curating and prioritizing football matches
 * for the dashboard feed. This is a pure Kotlin domain service with
 * no Android dependencies.
 */
class MatchCuratorService {

    /**
     * League definitions for match prioritization using explicit IDs.
     * This prevents confusion between leagues with similar names (e.g., Jamaican vs English Premier League).
     */
    companion object {
        // Top tournament IDs with explicit scoring
        private const val LEAGUE_PREMIER_LEAGUE_EN = 39      // English Premier League (+2000)
        private const val LEAGUE_LA_LIGA = 140               // La Liga (+1500)
        private const val LEAGUE_COPA_DEL_REY = 143          // Copa del Rey (+1500)
        private const val LEAGUE_CHAMPIONS_LEAGUE = 2        // Champions League (+2000)
        private const val LEAGUE_EUROPA_LEAGUE = 3           // Europa League (+1500)
        private const val LEAGUE_EREDIVISIE = 88             // Eredivisie (+1500)
        private const val LEAGUE_KKD = 89                    // KKD (Netherlands 2nd tier) (+500)
        
        // Explicit league scoring map (league ID -> score)
        private val LEAGUE_SCORES = mapOf(
            LEAGUE_PREMIER_LEAGUE_EN to 2000,    // English Premier League
            LEAGUE_LA_LIGA to 1500,              // La Liga
            LEAGUE_COPA_DEL_REY to 1500,         // Copa del Rey
            LEAGUE_CHAMPIONS_LEAGUE to 2000,     // Champions League
            LEAGUE_EUROPA_LEAGUE to 1500,        // Europa League
            LEAGUE_EREDIVISIE to 1500,           // Eredivisie
            LEAGUE_KKD to 500                    // KKD
        )
        
        // Live match statuses (matches currently in play)
        private val LIVE_STATUSES = setOf("1H", "2H", "HT", "ET", "PEN")
        
        // Finished match statuses
        private val FINISHED_STATUSES = setOf("FT", "AET", "PEN")
        
        // Problematic match statuses (postponed, suspended, etc.)
        private val PROBLEMATIC_STATUSES = setOf("PST", "SUSP", "INT", "CANC")
    }

    /**
     * Curates a list of matches into a prioritized feed.
     *
     * @param fixtures List of match fixtures to curate
     * @param standings List of standings for ranking analysis (optional)
     * @param favoriteTeamId Optional favorite team ID for personalization
     * @return CuratedFeed with hero match, live matches, and upcoming matches
     */
    fun curateMatches(
        fixtures: List<MatchFixture>,
        standings: List<StandingRow> = emptyList(),
        favoriteTeamId: String? = null
    ): CuratedFeed {
        if (fixtures.isEmpty()) {
            return CuratedFeed()
        }

        // Create a map of team names to their standings for quick lookup
        val teamStandings = standings.associateBy { it.team }

        // Score ALL matches first
        val scoredMatches = fixtures.map { fixture ->
            ScoredMatch(fixture, calculateExcitementScore(fixture, teamStandings, favoriteTeamId))
        }

        // Sort ALL matches by excitement score (descending)
        val sortedMatches = scoredMatches.sortedByDescending { it.score }

        // Hero match is ALWAYS the first match from the sorted list (highest score)
        val heroMatch = sortedMatches.firstOrNull()?.fixture

        // Separate remaining matches into categories AFTER sorting (excluding hero match)
        val liveMatches = mutableListOf<MatchFixture>()
        val upcomingMatches = mutableListOf<MatchFixture>()

        // Process sorted matches (excluding hero match) to maintain priority order
        sortedMatches.forEach { scoredMatch ->
            val fixture = scoredMatch.fixture
            // Skip the hero match (it will be displayed separately)
            if (fixture == heroMatch) {
                return@forEach
            }
            
            val status = fixture.status
            if (status != null && LIVE_STATUSES.contains(status)) {
                liveMatches.add(fixture)
            } else {
                upcomingMatches.add(fixture)
            }
        }

        return CuratedFeed(
            heroMatch = heroMatch,
            liveMatches = liveMatches,
            upcomingMatches = upcomingMatches
        )
    }

    /**
     * Calculates an excitement score for a match.
     * Higher scores indicate more exciting/important matches.
     * Scoring algorithm:
     * - Start score: 0
     * - Add league score based on explicit league ID mapping
     * - +100 points if match is LIVE (1H, 2H, HT, ET, PEN)
     * - +2000 points if match involves user's favorite team (FavoX Phase 3)
     */
    private fun calculateExcitementScore(
        fixture: MatchFixture,
        teamStandings: Map<String, StandingRow>,
        favoriteTeamId: String? = null
    ): Int {
        var score = 0

        // League scoring based on explicit IDs
        if (fixture.leagueId != null) {
            score += LEAGUE_SCORES[fixture.leagueId] ?: 0
        }

        // Live match bonus
        if (fixture.status != null && LIVE_STATUSES.contains(fixture.status)) {
            score += 100
        }

        // FavoX Phase 3: Favorite team bonus (+2000 points)
        if (favoriteTeamId != null) {
            // Check if either home or away team matches the favorite team
            val favoriteTeamIdInt = favoriteTeamId.toIntOrNull()
            if (favoriteTeamIdInt != null) {
                if (fixture.homeTeamId == favoriteTeamIdInt || fixture.awayTeamId == favoriteTeamIdInt) {
                    score += 2000  // Exponential bonus for favorite team matches
                }
            }
        }

        // Optional: Additional scoring factors could be added here:
        // - Top 4 clashes (both teams in top 4 of standings)
        // - Derby matches (local rivalries)
        // - Title-deciding matches
        // - Relegation battles
        // - Star players involved

        return score
    }

    /**
     * Internal data class for tracking matches with their excitement scores.
     */
    private data class ScoredMatch(
        val fixture: MatchFixture,
        val score: Int
    )

    /**
     * Checks if a match status indicates the match is live.
     */
    fun isLiveMatch(status: String?): Boolean {
        return status != null && LIVE_STATUSES.contains(status)
    }

    /**
     * Checks if a match status indicates the match is finished.
     */
    fun isFinishedMatch(status: String?): Boolean {
        return status != null && FINISHED_STATUSES.contains(status)
    }

    /**
     * Checks if a match status indicates a problematic state.
     */
    fun isProblematicMatch(status: String?): Boolean {
        return status != null && PROBLEMATIC_STATUSES.contains(status)
    }

    /**
     * Gets the display color for a match status based on the design guidelines.
     * Returns a color resource ID or color value.
     */
    fun getStatusColor(status: String?): String {
        return when {
            isLiveMatch(status) -> "Red"      // Live matches
            isFinishedMatch(status) -> "Green" // Finished matches
            isProblematicMatch(status) -> "Orange" // Problematic matches
            else -> "Grey"                     // Not started or unknown
        }
    }
}
