package com.Lyno.matchmindai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.Lyno.matchmindai.data.local.entity.ChatMessageEntity
import com.Lyno.matchmindai.data.local.entity.ChatSession
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for chat-related database operations.
 */
@Dao
interface ChatDao {

    // ChatSession operations
    @Query("SELECT * FROM chat_sessions ORDER BY lastUpdated DESC")
    fun getAllSessions(): Flow<List<ChatSession>>

    @Query("SELECT * FROM chat_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: String): ChatSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSession)

    @Update
    suspend fun updateSession(session: ChatSession)

    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Query("UPDATE chat_sessions SET title = :title, lastUpdated = :timestamp WHERE id = :sessionId")
    suspend fun updateSessionTitle(sessionId: String, title: String, timestamp: Long = System.currentTimeMillis())

    // ChatMessageEntity operations
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesBySessionId(sessionId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId AND isHidden = 0 ORDER BY timestamp ASC")
    fun getVisibleMessages(sessionId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId AND isHidden = 0 ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLastVisibleMessages(sessionId: String, limit: Int): List<ChatMessageEntity>

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLastMessages(sessionId: String, limit: Int): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>)

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesBySessionId(sessionId: String)

    @Query("SELECT COUNT(*) FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun getMessageCount(sessionId: String): Int

    // Combined operations
    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    suspend fun deleteSessionWithMessages(sessionId: String)
}
