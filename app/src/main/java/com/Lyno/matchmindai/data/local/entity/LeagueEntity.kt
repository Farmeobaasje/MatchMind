package com.Lyno.matchmindai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.Date

/**
 * Entity representing a football league in the local database.
 * This is used for Dynamic League Discovery to cache active leagues
 * and their coverage information from API-SPORTS.
 */
@Entity(tableName = "leagues")
data class LeagueEntity(
    @PrimaryKey
    @ColumnInfo(name = "league_id")
    val leagueId: Int,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "country")
    val country: String,
    
    @ColumnInfo(name = "logo_url")
    val logoUrl: String?,
    
    @ColumnInfo(name = "type")
    val type: String, // "League" or "Cup"
    
    @ColumnInfo(name = "season")
    val season: Int,
    
    @ColumnInfo(name = "is_current")
    val isCurrent: Boolean,
    
    // Coverage flags from API-SPORTS
    @ColumnInfo(name = "has_standings")
    val hasStandings: Boolean,
    
    @ColumnInfo(name = "has_players")
    val hasPlayers: Boolean,
    
    @ColumnInfo(name = "has_top_scorers")
    val hasTopScorers: Boolean,
    
    @ColumnInfo(name = "has_predictions")
    val hasPredictions: Boolean,
    
    @ColumnInfo(name = "has_odds")
    val hasOdds: Boolean,
    
    @ColumnInfo(name = "has_fixtures")
    val hasFixtures: Boolean,
    
    @ColumnInfo(name = "has_events")
    val hasEvents: Boolean,
    
    @ColumnInfo(name = "has_lineups")
    val hasLineups: Boolean,
    
    @ColumnInfo(name = "has_statistics")
    val hasStatistics: Boolean,
    
    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "priority_score")
    val priorityScore: Int = 0 // Higher score = higher priority in filtering
) {
    /**
     * Calculate a priority score based on coverage and league type.
     * Leagues with more data coverage get higher priority.
     */
    fun calculatePriorityScore(): Int {
        var score = 0
        
        // Base score for league type
        score += when (type) {
            "League" -> 50
            "Cup" -> 30
            else -> 10
        }
        
        // Bonus for current season
        if (isCurrent) score += 20
        
        // Coverage bonuses
        if (hasStandings) score += 10
        if (hasEvents) score += 15
        if (hasLineups) score += 15
        if (hasStatistics) score += 10
        if (hasPredictions) score += 5
        if (hasOdds) score += 5
        
        // Country bonus (prioritize Netherlands)
        if (country.equals("Netherlands", ignoreCase = true)) score += 25
        
        // Popular league bonus
        val popularLeagues = listOf(
            "Eredivisie", "Premier League", "La Liga", "Bundesliga", "Serie A", "Ligue 1",
            "Champions League", "Europa League", "Conference League", "KNVB Beker"
        )
        if (popularLeagues.any { name.contains(it, ignoreCase = true) }) score += 30
        
        return score
    }
    
    companion object {
        /**
         * Create a LeagueEntity from API-SPORTS league data.
         */
        fun fromApiResponse(
            leagueId: Int,
            name: String,
            country: String,
            logoUrl: String?,
            type: String,
            season: Int,
            isCurrent: Boolean,
            coverage: Map<String, Boolean> = emptyMap()
        ): LeagueEntity {
            val entity = LeagueEntity(
                leagueId = leagueId,
                name = name,
                country = country,
                logoUrl = logoUrl,
                type = type,
                season = season,
                isCurrent = isCurrent,
                hasStandings = coverage["standings"] ?: false,
                hasPlayers = coverage["players"] ?: false,
                hasTopScorers = coverage["top_scorers"] ?: false,
                hasPredictions = coverage["predictions"] ?: false,
                hasOdds = coverage["odds"] ?: false,
                hasFixtures = coverage["fixtures"] ?: true, // Default true
                hasEvents = coverage["events"] ?: false,
                hasLineups = coverage["lineups"] ?: false,
                hasStatistics = coverage["statistics"] ?: false
            )
            
            // Calculate and set priority score
            return entity.copy(priorityScore = entity.calculatePriorityScore())
        }
    }
}
