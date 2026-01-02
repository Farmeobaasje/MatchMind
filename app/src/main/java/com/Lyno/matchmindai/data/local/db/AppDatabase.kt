package com.Lyno.matchmindai.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.Lyno.matchmindai.data.local.dao.ChatDao
import com.Lyno.matchmindai.data.local.dao.FavoritesDao
import com.Lyno.matchmindai.data.local.dao.FixtureDao
import com.Lyno.matchmindai.data.local.dao.LeagueDao
import com.Lyno.matchmindai.data.local.dao.PredictionDao
import com.Lyno.matchmindai.data.local.entity.ChatMessageEntity
import com.Lyno.matchmindai.data.local.entity.ChatSession
import com.Lyno.matchmindai.data.local.entity.FavoriteEntity
import com.Lyno.matchmindai.data.local.entity.FixtureEntity
import com.Lyno.matchmindai.data.local.entity.LeagueEntity
import com.Lyno.matchmindai.data.local.entity.PredictionLogEntity

/**
 * Room database for the MatchMind AI application.
 * Contains tables for chat sessions, messages, and cached football fixtures.
 */
@Database(
    entities = [
        ChatSession::class,
        ChatMessageEntity::class,
        FixtureEntity::class,
        FavoriteEntity::class,
        LeagueEntity::class,
        PredictionLogEntity::class
    ],
    version = 8
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun fixtureDao(): FixtureDao
    abstract fun favoritesDao(): FavoritesDao
    abstract fun leagueDao(): LeagueDao
    abstract fun predictionDao(): PredictionDao

    companion object {
        /**
         * Migration from version 3 to 4:
         * - Add isHidden column to chat_messages table (default: 0)
         * - Add type column to chat_messages table (nullable)
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add isHidden column with default value 0 (false)
                database.execSQL("ALTER TABLE chat_messages ADD COLUMN isHidden INTEGER NOT NULL DEFAULT 0")
                // Add type column (nullable)
                database.execSQL("ALTER TABLE chat_messages ADD COLUMN type TEXT")
            }
        }

        /**
         * Migration from version 4 to 5:
         * - Create favorites table
         */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE favorites (
                        id TEXT NOT NULL PRIMARY KEY,
                        type TEXT NOT NULL,
                        name TEXT NOT NULL,
                        logoUrl TEXT,
                        addedAt INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        /**
         * Migration from version 5 to 6:
         * - Create leagues table for Dynamic League Discovery
         */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE leagues (
                        league_id INTEGER NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        country TEXT NOT NULL,
                        logo_url TEXT,
                        type TEXT NOT NULL,
                        season INTEGER NOT NULL,
                        is_current INTEGER NOT NULL,
                        has_standings INTEGER NOT NULL,
                        has_players INTEGER NOT NULL,
                        has_top_scorers INTEGER NOT NULL,
                        has_predictions INTEGER NOT NULL,
                        has_odds INTEGER NOT NULL,
                        has_fixtures INTEGER NOT NULL,
                        has_events INTEGER NOT NULL,
                        has_lineups INTEGER NOT NULL,
                        has_statistics INTEGER NOT NULL,
                        last_updated INTEGER NOT NULL,
                        priority_score INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        /**
         * Migration from version 6 to 7:
         * - Create prediction_logs table for Tesseract Black Box Recorder (Phase 4)
         */
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE prediction_logs (
                        fixtureId INTEGER NOT NULL PRIMARY KEY,
                        matchName TEXT NOT NULL,
                        predictedScore TEXT NOT NULL,
                        homeProb REAL NOT NULL,
                        drawProb REAL NOT NULL,
                        awayProb REAL NOT NULL,
                        homeFitness INTEGER NOT NULL,
                        homeDistraction INTEGER NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        /**
         * Migration from version 7 to 8:
         * - Add homeTeamId and awayTeamId columns for analytics
         * - Add actualScore column for match result
         * - Add outcomeCorrect column for winner prediction accuracy
         * - Add exactScoreCorrect column for exact score accuracy
         */
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns for team IDs and result tracking
                database.execSQL("ALTER TABLE prediction_logs ADD COLUMN homeTeamId INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE prediction_logs ADD COLUMN awayTeamId INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE prediction_logs ADD COLUMN actualScore TEXT")
                database.execSQL("ALTER TABLE prediction_logs ADD COLUMN outcomeCorrect INTEGER")
                database.execSQL("ALTER TABLE prediction_logs ADD COLUMN exactScoreCorrect INTEGER")
            }
        }
    }
}
