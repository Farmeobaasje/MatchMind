package com.Lyno.matchmindai.domain.repository

import com.Lyno.matchmindai.domain.model.KaptigunAnalysis
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Kaptigun Performance Analysis Dashboard.
 * Provides access to deep tactical analysis data including head-to-head,
 * recent form, and deep statistics comparisons.
 */
interface KaptigunRepository {
    /**
     * Fetch comprehensive performance analysis for a match.
     * Aggregates data from three sources:
     * 1. Head-to-head duels (last 5 matches)
     * 2. Recent form for both teams (last 5 matches each)
     * 3. Deep statistics comparison with sentiment analysis
     *
     * @param fixtureId The ID of the fixture
     * @param homeTeamId The ID of the home team
     * @param awayTeamId The ID of the away team
     * @return Result containing either KaptigunAnalysis or an error
     */
    suspend fun fetchAnalysis(
        fixtureId: Int,
        homeTeamId: Int,
        awayTeamId: Int
    ): Result<KaptigunAnalysis>

    /**
     * Get Kaptigun analysis as a Flow for reactive UI updates.
     * Automatically updates when new data is available.
     *
     * @param fixtureId The ID of the fixture
     * @return Flow of Resource<KaptigunAnalysis> with loading/error states
     */
    fun getAnalysisFlow(fixtureId: Int): Flow<com.Lyno.matchmindai.common.Resource<KaptigunAnalysis>>

    /**
     * Fetch head-to-head analysis between two teams.
     * Returns last 5 matches with xG and performance labels.
     *
     * @param homeTeamId The ID of the home team
     * @param awayTeamId The ID of the away team
     * @return Result containing list of head-to-head duels or an error
     */
    suspend fun fetchHeadToHead(
        homeTeamId: Int,
        awayTeamId: Int
    ): Result<List<com.Lyno.matchmindai.domain.model.HeadToHeadDuel>>

    /**
     * Fetch recent form for a team.
     * Returns last 5 matches with efficiency analysis.
     *
     * @param teamId The ID of the team
     * @param isHome Whether to filter for home matches only
     * @return Result containing team recent form or an error
     */
    suspend fun fetchTeamRecentForm(
        teamId: Int,
        isHome: Boolean = false
    ): Result<com.Lyno.matchmindai.domain.model.TeamRecentForm>

    /**
     * Fetch deep statistics comparison between two teams.
     * Calculates averages for xG, possession, shots on target, PPDA, and sentiment.
     *
     * @param homeTeamId The ID of the home team
     * @param awayTeamId The ID of the away team
     * @return Result containing deep stats comparison or an error
     */
    suspend fun fetchDeepStatsComparison(
        homeTeamId: Int,
        awayTeamId: Int
    ): Result<com.Lyno.matchmindai.domain.model.DeepStatsComparison>

    /**
     * Calculate xG for a match based on shots data.
     * Uses formula: (ShotsOnTarget * 0.3) + (ShotsOffTarget * 0.07)
     * Falls back to API xG if available.
     *
     * @param shotsOnTarget Number of shots on target
     * @param shotsOffTarget Number of shots off target
     * @param apiXg Optional xG from API (if available)
     * @return Calculated xG value
     */
    fun calculateExpectedGoals(
        shotsOnTarget: Int,
        shotsOffTarget: Int,
        apiXg: Double? = null
    ): Double

    /**
     * Determine performance label based on score and xG.
     * Implements Tesseract Twists logic:
     * - Win with higher xG → DOMINANT (Green)
     * - Win with lower xG → LUCKY (Orange)
     * - Loss with higher xG → UNLUCKY (Red)
     * - Balanced → NEUTRAL (Gray)
     *
     * @param isWin Whether the team won
     * @param teamXg Team's expected goals
     * @param opponentXg Opponent's expected goals
     * @return Performance label
     */
    fun determinePerformanceLabel(
        isWin: Boolean,
        teamXg: Double,
        opponentXg: Double
    ): com.Lyno.matchmindai.domain.model.PerformanceLabel

    /**
     * Determine efficiency icon based on goals vs xG.
     * - Goals > xG + 0.5 → CLINICAL (↑ Green)
     * - Goals < xG - 0.5 → INEFFICIENT (↓ Red)
     * - Balanced → BALANCED (→ Orange)
     *
     * @param goals Actual goals scored
     * @param xg Expected goals
     * @return Efficiency icon
     */
    fun determineEfficiencyIcon(
        goals: Int,
        xg: Double
    ): com.Lyno.matchmindai.domain.model.EfficiencyIcon

    /**
     * Get sentiment score for a team using NewsImpactAnalyzer.
     * Normalizes distraction/fitness metrics to -1.0 (Chaos) to +1.0 (Euphoria).
     *
     * @param teamId The ID of the team
     * @param teamName The name of the team
     * @param leagueName The league name for context
     * @return Sentiment score between -1.0 and +1.0
     */
    suspend fun getSentimentScore(
        teamId: Int,
        teamName: String,
        leagueName: String
    ): Double

    /**
     * Calculate PPDA (Passes Per Defensive Action) for a team.
     * Lower PPDA indicates better pressing intensity.
     *
     * @param passesCompleted Number of passes completed
     * @param defensiveActions Number of defensive actions (tackles, interceptions)
     * @return PPDA value (higher = less pressing)
     */
    fun calculatePPDA(
        passesCompleted: Int,
        defensiveActions: Int
    ): Double

    /**
     * Cache Kaptigun analysis for offline access.
     *
     * @param analysis The Kaptigun analysis to cache
     */
    suspend fun cacheAnalysis(analysis: KaptigunAnalysis)

    /**
     * Get cached Kaptigun analysis.
     *
     * @param fixtureId The ID of the fixture
     * @return Cached analysis or null if not found
     */
    suspend fun getCachedAnalysis(fixtureId: Int): KaptigunAnalysis?

    /**
     * Clear Kaptigun analysis cache.
     */
    suspend fun clearAnalysisCache()
}
