package com.Lyno.matchmindai.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entity representing a chat message in the local database.
 * Each message belongs to a specific chat session.
 */
@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sessionId"]),
        Index(value = ["timestamp"])
    ]
)
data class ChatMessageEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val role: String, // "user" or "assistant"
    val content: String? = null,
    val predictionJson: String? = null,
    val agentResponseJson: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isHidden: Boolean = false,
    val type: String? = null // "USER", "ASSISTANT", "TOOL_OUTPUT"
)
