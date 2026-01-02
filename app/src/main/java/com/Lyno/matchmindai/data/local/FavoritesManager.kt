package com.Lyno.matchmindai.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "user_settings")

private val FAV_LEAGUES_KEY = stringSetPreferencesKey("favorite_leagues")
private val FAV_TEAMS_KEY = stringSetPreferencesKey("favorite_teams")
private val FAV_PLAYERS_KEY = stringSetPreferencesKey("favorite_players")

/**
 * Manages user's favorite leagues, teams, and players using DataStore.
 * Provides reactive Flow for UI updates and suspend functions for modifications.
 * 
 * FavoX Fase 4 Update: Uitgebreid met ondersteuning voor teams en spelers.
 */
class FavoritesManager(private val context: Context) {

    // Leagues
    val favoriteLeagues: Flow<Set<String>> = context.dataStore.data
        .map { preferences: Preferences ->
            preferences[FAV_LEAGUES_KEY] ?: emptySet()
        }

    // Teams
    val favoriteTeams: Flow<Set<String>> = context.dataStore.data
        .map { preferences: Preferences ->
            preferences[FAV_TEAMS_KEY] ?: emptySet()
        }

    // Players
    val favoritePlayers: Flow<Set<String>> = context.dataStore.data
        .map { preferences: Preferences ->
            preferences[FAV_PLAYERS_KEY] ?: emptySet()
        }

    /**
     * Toggle a league as favorite.
     * @param leagueId The ID of the league to toggle
     */
    suspend fun toggleFavoriteLeague(leagueId: Int) {
        context.dataStore.edit { preferences ->
            val current = preferences[FAV_LEAGUES_KEY] ?: emptySet<String>()
            val idStr = leagueId.toString()
            
            val newSet = if (current.contains(idStr)) {
                current - idStr
            } else {
                current + idStr
            }
            
            preferences[FAV_LEAGUES_KEY] = newSet
        }
    }

    /**
     * Toggle a team as favorite.
     * @param teamId The ID of the team to toggle
     */
    suspend fun toggleFavoriteTeam(teamId: Int) {
        context.dataStore.edit { preferences ->
            val current = preferences[FAV_TEAMS_KEY] ?: emptySet<String>()
            val idStr = teamId.toString()
            
            val newSet = if (current.contains(idStr)) {
                current - idStr
            } else {
                current + idStr
            }
            
            preferences[FAV_TEAMS_KEY] = newSet
        }
    }

    /**
     * Toggle a player as favorite.
     * @param playerId The ID of the player to toggle
     */
    suspend fun toggleFavoritePlayer(playerId: Int) {
        context.dataStore.edit { preferences ->
            val current = preferences[FAV_PLAYERS_KEY] ?: emptySet<String>()
            val idStr = playerId.toString()
            
            val newSet = if (current.contains(idStr)) {
                current - idStr
            } else {
                current + idStr
            }
            
            preferences[FAV_PLAYERS_KEY] = newSet
        }
    }

    /**
     * Check if a league is favorited.
     * @param leagueId The ID of the league to check
     * @return Boolean indicating if the league is favorited
     */
    suspend fun isLeagueFavorite(leagueId: Int): Boolean {
        val preferences: Preferences = context.dataStore.data.first()
        val current = preferences[FAV_LEAGUES_KEY] ?: emptySet()
        return current.contains(leagueId.toString())
    }

    /**
     * Check if a team is favorited.
     * @param teamId The ID of the team to check
     * @return Boolean indicating if the team is favorited
     */
    suspend fun isTeamFavorite(teamId: Int): Boolean {
        val preferences: Preferences = context.dataStore.data.first()
        val current = preferences[FAV_TEAMS_KEY] ?: emptySet()
        return current.contains(teamId.toString())
    }

    /**
     * Check if a player is favorited.
     * @param playerId The ID of the player to check
     * @return Boolean indicating if the player is favorited
     */
    suspend fun isPlayerFavorite(playerId: Int): Boolean {
        val preferences: Preferences = context.dataStore.data.first()
        val current = preferences[FAV_PLAYERS_KEY] ?: emptySet()
        return current.contains(playerId.toString())
    }

    /**
     * Get all favorite league IDs as a list of integers.
     */
    suspend fun getFavoriteLeagueIds(): List<Int> {
        val preferences: Preferences = context.dataStore.data.first()
        val current = preferences[FAV_LEAGUES_KEY] ?: emptySet()
        return current.mapNotNull { it.toIntOrNull() }.filter { it > 0 }
    }

    /**
     * Get all favorite team IDs as a list of integers.
     */
    suspend fun getFavoriteTeamIds(): List<Int> {
        val preferences: Preferences = context.dataStore.data.first()
        val current = preferences[FAV_TEAMS_KEY] ?: emptySet()
        return current.mapNotNull { it.toIntOrNull() }.filter { it > 0 }
    }

    /**
     * Get all favorite player IDs as a list of integers.
     */
    suspend fun getFavoritePlayerIds(): List<Int> {
        val preferences: Preferences = context.dataStore.data.first()
        val current = preferences[FAV_PLAYERS_KEY] ?: emptySet()
        return current.mapNotNull { it.toIntOrNull() }.filter { it > 0 }
    }

    /**
     * Clear all favorite leagues.
     */
    suspend fun clearAllLeagues() {
        context.dataStore.edit { preferences ->
            preferences.remove(FAV_LEAGUES_KEY)
        }
    }

    /**
     * Clear all favorite teams.
     */
    suspend fun clearAllTeams() {
        context.dataStore.edit { preferences ->
            preferences.remove(FAV_TEAMS_KEY)
        }
    }

    /**
     * Clear all favorite players.
     */
    suspend fun clearAllPlayers() {
        context.dataStore.edit { preferences ->
            preferences.remove(FAV_PLAYERS_KEY)
        }
    }

    /**
     * Clear all favorites (leagues, teams, players).
     */
    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.remove(FAV_LEAGUES_KEY)
            preferences.remove(FAV_TEAMS_KEY)
            preferences.remove(FAV_PLAYERS_KEY)
        }
    }

    /**
     * Get all favorites as a map of types to IDs.
     */
    suspend fun getAllFavorites(): Map<String, List<Int>> {
        return mapOf(
            "leagues" to getFavoriteLeagueIds(),
            "teams" to getFavoriteTeamIds(),
            "players" to getFavoritePlayerIds()
        )
    }
}
