package com.Lyno.matchmindai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Room entity representing a football fixture stored in the local database.
 * This serves as the cache for RapidAPI football fixtures.
 */
@Entity(tableName = "fixtures")
data class FixtureEntity(
    @PrimaryKey
    val id: Int,
    val date: String,
    val timestamp: Long,
    val homeTeam: String,
    val awayTeam: String,
    val homeLogo: String?,
    val awayLogo: String?,
    val leagueName: String,
    val leagueCountry: String,
    val leagueFlag: String?,
    val status: String,
    val statusShort: String?,
    val elapsed: Int?,
    val venueName: String?,
    val venueCity: String?,
    val referee: String?,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        /**
         * Creates a unique cache key for a specific date.
         * This helps with cache invalidation and retrieval.
         */
        fun createCacheKey(date: String): String = "fixtures_$date"
    }
}
