package com.Lyno.matchmindai.data.local.dao

import androidx.room.*
import com.Lyno.matchmindai.data.local.entity.FavoriteEntity
import com.Lyno.matchmindai.data.local.entity.FavoriteType
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for favorite teams and competitions.
 */
@Dao
interface FavoritesDao {
    
    /**
     * Insert or update a favorite item.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)
    
    /**
     * Delete a favorite item by ID.
     */
    @Delete
    suspend fun deleteFavorite(favorite: FavoriteEntity)
    
    /**
     * Delete a favorite item by ID.
     */
    @Query("DELETE FROM favorites WHERE id = :id")
    suspend fun deleteFavoriteById(id: String)
    
    /**
     * Get all favorite items.
     */
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>
    
    /**
     * Get favorite items by type.
     */
    @Query("SELECT * FROM favorites WHERE type = :type ORDER BY addedAt DESC")
    fun getFavoritesByType(type: FavoriteType): Flow<List<FavoriteEntity>>
    
    /**
     * Get favorite teams.
     */
    fun getFavoriteTeams(): Flow<List<FavoriteEntity>> = getFavoritesByType(FavoriteType.TEAM)
    
    /**
     * Get favorite leagues.
     */
    fun getFavoriteLeagues(): Flow<List<FavoriteEntity>> = getFavoritesByType(FavoriteType.LEAGUE)
    
    /**
     * Check if an item is favorited.
     */
    @Query("SELECT COUNT(*) FROM favorites WHERE id = :id")
    suspend fun isFavorite(id: String): Int
    
    /**
     * Toggle favorite status.
     * Returns true if the item is now favorited, false if it was removed.
     */
    suspend fun toggleFavorite(favorite: FavoriteEntity): Boolean {
        val isCurrentlyFavorite = isFavorite(favorite.id) > 0
        if (isCurrentlyFavorite) {
            deleteFavorite(favorite)
            return false
        } else {
            insertFavorite(favorite)
            return true
        }
    }
    
    /**
     * Get favorite team IDs.
     */
    @Query("SELECT id FROM favorites WHERE type = 'TEAM'")
    suspend fun getFavoriteTeamIds(): List<String>
    
    /**
     * Get favorite league IDs.
     */
    @Query("SELECT id FROM favorites WHERE type = 'LEAGUE'")
    suspend fun getFavoriteLeagueIds(): List<String>
    
    /**
     * Clear all favorites.
     */
    @Query("DELETE FROM favorites")
    suspend fun clearAll()
}
