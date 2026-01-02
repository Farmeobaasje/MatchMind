package com.Lyno.matchmindai.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Optimized DTOs for LLM consumption following DTO Flattening principles.
 * These DTOs minimize JSON depth and remove unnecessary fields to reduce token usage.
 * Reference: Agentic RAG Report Section 2.4
 */

/**
 * Simplified fixture representation for LLM processing.
 * Removes URLs, logos, and redundant IDs to minimize token usage.
 */
@Serializable
data class SimplifiedFixture(
    @SerialName("fixture_id") val fixtureId: Int,
    @SerialName("home_team") val homeTeam: String,
    @SerialName("away_team") val awayTeam: String,
    @SerialName("date") val date: String,
    @SerialName("status") val status: String,
    @SerialName("venue") val venue: String? = null,
    @SerialName("referee") val referee: String? = null,
    @SerialName("round") val round: String? = null,
    @SerialName("league") val league: String? = null,
    @SerialName("home_goals") val homeGoals: Int? = null,
    @SerialName("away_goals") val awayGoals: Int? = null
)

/**
 * Simplified standings for LLM processing.
 * Focuses on key metrics for prediction context.
 */
@Serializable
data class SimplifiedStanding(
    @SerialName("rank") val rank: Int,
    @SerialName("team") val teamName: String,
    @SerialName("points") val points: Int,
    @SerialName("played") val gamesPlayed: Int,
    @SerialName("won") val wins: Int,
    @SerialName("drawn") val draws: Int,
    @SerialName("lost") val losses: Int,
    @SerialName("goals_for") val goalsFor: Int,
    @SerialName("goals_against") val goalsAgainst: Int,
    @SerialName("goal_difference") val goalDifference: Int,
    @SerialName("form") val form: String,
    @SerialName("home_record") val homeRecord: String? = null, // "W-D-L"
    @SerialName("away_record") val awayRecord: String? = null  // "W-D-L"
)

/**
 * Simplified player statistics for LLM processing.
 * Only essential stats for match analysis.
 */
@Serializable
data class SimplifiedPlayerStats(
    @SerialName("name") val name: String,
    @SerialName("position") val position: String,
    @SerialName("rating") val rating: Double? = null,
    @SerialName("goals") val goals: Int = 0,
    @SerialName("assists") val assists: Int = 0,
    @SerialName("yellow_cards") val yellowCards: Int = 0,
    @SerialName("red_cards") val redCards: Int = 0,
    @SerialName("minutes_played") val minutesPlayed: Int? = null
)

/**
 * Simplified injury report for LLM processing.
 */
@Serializable
data class SimplifiedInjury(
    @SerialName("player") val playerName: String,
    @SerialName("team") val team: String,
    @SerialName("type") val injuryType: String,
    @SerialName("reason") val reason: String,
    @SerialName("expected_return") val expectedReturn: String? = null
)

/**
 * Simplified prediction data for LLM processing.
 */
@Serializable
data class SimplifiedPrediction(
    @SerialName("winner") val predictedWinner: String? = null, // "home", "away", or "draw"
    @SerialName("win_probability") val winProbability: Map<String, Double>? = null, // {"home": 45.0, "draw": 30.0, "away": 25.0}
    @SerialName("goals_home") val predictedGoalsHome: Double? = null,
    @SerialName("goals_away") val predictedGoalsAway: Double? = null,
    @SerialName("advice") val advice: String? = null,
    @SerialName("comparison") val teamComparison: TeamComparison? = null
)

/**
 * Team comparison for predictions.
 */
@Serializable
data class TeamComparison(
    @SerialName("form_home") val formHome: Int,
    @SerialName("form_away") val formAway: Int,
    @SerialName("att_home") val attackHome: Int,
    @SerialName("att_away") val attackAway: Int,
    @SerialName("def_home") val defenseHome: Int,
    @SerialName("def_away") val defenseAway: Int,
    @SerialName("h2h_home") val h2hHome: Int,
    @SerialName("h2h_away") val h2hAway: Int
)

/**
 * Simplified head-to-head data.
 */
@Serializable
data class SimplifiedH2H(
    @SerialName("last_matches") val lastMatches: List<H2HMatch>,
    @SerialName("home_wins") val homeWins: Int,
    @SerialName("draws") val draws: Int,
    @SerialName("away_wins") val awayWins: Int,
    @SerialName("total_goals") val averageGoals: Double? = null
)

/**
 * Individual H2H match result.
 */
@Serializable
data class H2HMatch(
    @SerialName("date") val date: String,
    @SerialName("home_team") val homeTeam: String,
    @SerialName("away_team") val awayTeam: String,
    @SerialName("home_goals") val homeGoals: Int,
    @SerialName("away_goals") val awayGoals: Int,
    @SerialName("competition") val competition: String? = null
)

/**
 * News snippet from web sources.
 */
@Serializable
data class NewsSnippet(
    @SerialName("title") val title: String,
    @SerialName("snippet") val snippet: String,
    @SerialName("source") val source: String,
    @SerialName("published_date") val publishedDate: String? = null,
    @SerialName("relevance_score") val relevanceScore: Double? = null
)

/**
 * Weather information for match day.
 */
@Serializable
data class WeatherInfo(
    @SerialName("temperature") val temperature: Int? = null,
    @SerialName("condition") val condition: String? = null,
    @SerialName("wind_speed") val windSpeed: Int? = null,
    @SerialName("humidity") val humidity: Int? = null,
    @SerialName("precipitation") val precipitation: Int? = null
)

/**
 * Complete match context combining all flattened data sources.
 * This is the main DTO sent to the LLM for analysis.
 */
@Serializable
data class MatchContextDto(
    @SerialName("analysis_date") val analysisDate: String, // Current date for context
    @SerialName("fixture") val fixture: SimplifiedFixture,
    @SerialName("standings") val standings: List<SimplifiedStanding>? = null,
    @SerialName("home_form") val homeForm: TeamForm? = null,
    @SerialName("away_form") val awayForm: TeamForm? = null,
    @SerialName("injuries") val injuries: List<SimplifiedInjury>? = null,
    @SerialName("prediction") val prediction: SimplifiedPrediction? = null,
    @SerialName("h2h") val headToHead: SimplifiedH2H? = null,
    @SerialName("key_players") val keyPlayers: KeyPlayers? = null,
    @SerialName("news") val news: List<NewsSnippet>? = null,
    @SerialName("weather") val weather: WeatherInfo? = null,
    @SerialName("betting_context") val bettingContext: BettingContext? = null
)

/**
 * Team form summary.
 */
@Serializable
data class TeamForm(
    @SerialName("team") val team: String,
    @SerialName("last_5_form") val last5Form: String, // e.g., "WWLDW"
    @SerialName("last_5_goals_scored") val last5GoalsScored: Int,
    @SerialName("last_5_goals_conceded") val last5GoalsConceded: Int,
    @SerialName("clean_sheets") val cleanSheets: Int = 0,
    @SerialName("failed_to_score") val failedToScore: Int = 0
)

/**
 * Key players for both teams.
 */
@Serializable
data class KeyPlayers(
    @SerialName("home_top_scorer") val homeTopScorer: SimplifiedPlayerStats? = null,
    @SerialName("away_top_scorer") val awayTopScorer: SimplifiedPlayerStats? = null,
    @SerialName("home_top_assists") val homeTopAssists: SimplifiedPlayerStats? = null,
    @SerialName("away_top_assists") val awayTopAssists: SimplifiedPlayerStats? = null,
    @SerialName("home_injured") val homeInjuredCount: Int = 0,
    @SerialName("away_injured") val awayInjuredCount: Int = 0
)

/**
 * Betting odds and market context.
 */
@Serializable
data class BettingContext(
    @SerialName("odds_1x2") val odds1X2: Map<String, Double>? = null, // {"home": 2.10, "draw": 3.40, "away": 3.50}
    @SerialName("over_under_2_5") val overUnder25: Map<String, Double>? = null, // {"over": 1.85, "under": 1.95}
    @SerialName("both_teams_score") val bothTeamsScore: Map<String, Double>? = null, // {"yes": 1.70, "no": 2.10}
    @SerialName("asian_handicap") val asianHandicap: String? = null // e.g., "Home -0.5"
)