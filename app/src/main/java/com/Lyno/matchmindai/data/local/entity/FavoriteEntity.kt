package com.Lyno.matchmindai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a favorite team or competition.
 * Users can mark teams/competitions as favorites to filter matches.
 */
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val id: String, // teamId or leagueId prefixed with "team_" or "league_"
    val type: FavoriteType,
    val name: String,
    val logoUrl: String? = null,
    val addedAt: Long = System.currentTimeMillis()
)

/**
 * Type of favorite item.
 */
enum class FavoriteType {
    TEAM,
    LEAGUE
}
