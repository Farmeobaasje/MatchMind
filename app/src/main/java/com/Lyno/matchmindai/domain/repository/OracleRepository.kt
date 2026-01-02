package com.Lyno.matchmindai.domain.repository

import com.Lyno.matchmindai.domain.model.OracleAnalysis

/**
 * Repository interface for the Oracle Engine - the "Hard Reality" prediction system.
 * This engine bases predictions strictly on standings (rank, points) and head-to-head data,
 * ignoring complex match history loops to identify clear favorites.
 */
interface OracleRepository {
    /**
     * Get Oracle analysis for a specific match.
     *
     * @param leagueId The league identifier
     * @param season The season year (e.g., 2025)
     * @param homeTeamId The home team identifier
     * @param awayTeamId The away team identifier
     * @param fixtureId Optional fixture ID for LLMGRADE analysis (if null, generates fake ID)
     * @return OracleAnalysis with prediction, confidence, reasoning, and power scores
     */
    suspend fun getOracleAnalysis(
        leagueId: Int,
        season: Int,
        homeTeamId: Int,
        awayTeamId: Int,
        fixtureId: Int? = null
    ): OracleAnalysis

    /**
     * Get enhanced Oracle analysis with LLMGRADE context factors.
     *
     * @param leagueId The league identifier
     * @param season The season year (e.g., 2025)
     * @param homeTeamId The home team identifier
     * @param awayTeamId The away team identifier
     * @param fixtureId The actual fixture ID for LLM context analysis
     * @return Enhanced OracleAnalysis with LLMGRADE context factors
     */
    suspend fun getEnhancedOracleAnalysis(
        leagueId: Int,
        season: Int,
        homeTeamId: Int,
        awayTeamId: Int,
        fixtureId: Int
    ): OracleAnalysis

    /**
     * Save a prediction to the history database (user-driven).
     *
     * @param fixtureId The actual fixture ID from the match
     * @param homeTeamId Home team ID
     * @param awayTeamId Away team ID
     * @param homeTeamName Home team name
     * @param awayTeamName Away team name
     * @param analysis Oracle analysis to save
     * @return Boolean indicating success
     */
    suspend fun savePrediction(
        fixtureId: Int,
        homeTeamId: Int,
        awayTeamId: Int,
        homeTeamName: String,
        awayTeamName: String,
        analysis: OracleAnalysis
    ): Boolean

    /**
     * Check for pending predictions that need reconciliation with actual results.
     * This method should be called periodically to update predictions with actual match outcomes.
     *
     * @return Number of predictions that were successfully reconciled
     */
    suspend fun checkPendingPredictions(): Int

    /**
     * Get context-adjusted Oracle analysis with 3-0 bias correction.
     * Uses the new ContextAdjustedOracle model to apply contextual corrections.
     *
     * @param leagueId The league identifier
     * @param season The season year (e.g., 2025)
     * @param homeTeamId The home team identifier
     * @param awayTeamId The away team identifier
     * @param fixtureId Optional fixture ID for LLMGRADE analysis (if null, generates fake ID)
     * @return Context-adjusted OracleAnalysis with bias correction
     */
    suspend fun getContextAdjustedOracleAnalysis(
        leagueId: Int,
        season: Int,
        homeTeamId: Int,
        awayTeamId: Int,
        fixtureId: Int? = null
    ): OracleAnalysis
}
