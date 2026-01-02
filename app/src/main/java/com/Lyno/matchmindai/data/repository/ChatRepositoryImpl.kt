package com.Lyno.matchmindai.data.repository

import android.util.Log
import com.Lyno.matchmindai.data.local.dao.ChatDao
import com.Lyno.matchmindai.data.local.entity.ChatMessageEntity
import com.Lyno.matchmindai.data.local.entity.ChatSession
import com.Lyno.matchmindai.domain.model.ChatSession as DomainChatSession
import com.Lyno.matchmindai.domain.repository.ChatRepository
import com.Lyno.matchmindai.data.dto.ChatStreamUpdate
import com.Lyno.matchmindai.data.dto.DeepSeekRequest
import com.Lyno.matchmindai.data.remote.DeepSeekApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Implementation of ChatRepository that handles chat session and message operations.
 */
class ChatRepositoryImpl(
    private val chatDao: ChatDao
) : ChatRepository {

    override fun getChatSessions(): Flow<List<DomainChatSession>> {
        return chatDao.getAllSessions().map { sessions ->
            sessions.map { entity ->
                DomainChatSession(
                    id = entity.id,
                    title = entity.title,
                    lastUpdated = entity.lastUpdated
                )
            }
        }
    }

    override fun getChatMessages(sessionId: String): Flow<List<ChatMessageEntity>> {
        return chatDao.getMessagesBySessionId(sessionId)
    }

    override suspend fun createChatSession(title: String): String {
        val session = com.Lyno.matchmindai.data.local.entity.ChatSession(
            id = UUID.randomUUID().toString(),
            title = title,
            lastUpdated = System.currentTimeMillis()
        )
        chatDao.insertSession(session)
        return session.id
    }

    override suspend fun updateChatSessionTitle(sessionId: String, title: String) {
        chatDao.updateSessionTitle(sessionId, title)
    }

    override suspend fun deleteChatSession(sessionId: String) {
        chatDao.deleteSessionWithMessages(sessionId)
    }

    override suspend fun getOrCreateLastSession(): String {
        val sessions = chatDao.getAllSessions().first()
        return if (sessions.isNotEmpty()) {
            sessions.first().id
        } else {
            createChatSession("Nieuwe Chat")
        }
    }

    override suspend fun saveChatMessage(
        sessionId: String,
        role: String,
        content: String?,
        predictionJson: String?,
        agentResponseJson: String?,
        isHidden: Boolean,
        type: String?
    ) {
        val message = com.Lyno.matchmindai.data.local.entity.ChatMessageEntity(
            id = java.util.UUID.randomUUID().toString(),
            sessionId = sessionId,
            role = role,
            content = content,
            predictionJson = predictionJson,
            agentResponseJson = agentResponseJson,
            timestamp = System.currentTimeMillis(),
            isHidden = isHidden,
            type = type
        )
        chatDao.insertMessage(message)
        
        // Update session timestamp (only for visible messages)
        if (!isHidden) {
            chatDao.updateSessionTitle(sessionId, "", System.currentTimeMillis())
        }
    }

    override suspend fun getContextMessages(sessionId: String): List<com.Lyno.matchmindai.data.local.entity.ChatMessageEntity> {
        // Get last 10 visible messages for sliding window
        return chatDao.getLastVisibleMessages(sessionId, 10).reversed() // Reverse to chronological order
    }

    override suspend fun saveToolOutput(sessionId: String, toolName: String, result: String) {
        withContext(Dispatchers.IO) {
            try {
                ensureSessionExists(sessionId) // CRUCIAL STEP
                
                val message = ChatMessageEntity(
                    id = UUID.randomUUID().toString(),
                    sessionId = sessionId,
                    role = "tool",
                    content = result,
                    predictionJson = null,
                    agentResponseJson = null,
                    timestamp = System.currentTimeMillis(),
                    isHidden = true,
                    type = "TOOL_OUTPUT"
                )
                chatDao.insertMessage(message)
            } catch (e: Exception) {
                Log.e("ChatRepo", "Failed to save tool output (ignoring to keep chat alive): ${e.message}")
                // CRITICAL: Gooi geen exception! Laat de flow doorgaan zodat de AI de data alsnog in-memory krijgt.
            }
        }
    }

    /**
     * Ensure that a chat session exists before trying to save messages to it.
     * If the session doesn't exist, create it with a default title.
     * Enhanced with robust validation and error handling.
     */
    private suspend fun ensureSessionExists(sessionId: String) {
        try {
            // Validate sessionId format (should be a valid UUID)
            if (sessionId.isBlank()) {
                Log.w("ChatRepo", "Blank sessionId provided, cannot ensure session exists")
                return
            }
            
            // Try to parse as UUID to validate format
            try {
                UUID.fromString(sessionId)
            } catch (e: IllegalArgumentException) {
                Log.w("ChatRepo", "Invalid sessionId format (not a valid UUID): $sessionId")
                // Don't create session with invalid ID - it will cause foreign key constraint
                return
            }
            
            // Check if session exists
            if (chatDao.getSessionById(sessionId) == null) {
                Log.d("ChatRepo", "Creating new session with ID: $sessionId")
                chatDao.insertSession(ChatSession(
                    id = sessionId, 
                    title = "Nieuw Gesprek", 
                    lastUpdated = System.currentTimeMillis()
                ))
                Log.d("ChatRepo", "Session created successfully")
            }
        } catch (e: Exception) {
            Log.e("ChatRepo", "Failed to ensure session exists for ID: $sessionId", e)
            // Don't throw - let the caller handle gracefully
        }
    }

    override suspend fun clearSession(sessionId: String) {
        chatDao.deleteMessagesBySessionId(sessionId)
    }

    // Note: The streaming implementation requires a DeepSeekApi instance.
    // This should be injected via constructor or provided through AppContainer.
    // For now, we'll throw an exception indicating this needs to be implemented
    // in a separate repository that has access to the API.
    override suspend fun streamChat(
        apiKey: String,
        request: DeepSeekRequest
    ): Flow<ChatStreamUpdate> {
        throw UnsupportedOperationException(
            "Streaming chat requires DeepSeekApi instance. " +
            "Use MatchRepository for streaming functionality."
        )
    }

    override suspend fun saveStreamingChatMessage(
        sessionId: String,
        reasoningContent: String?,
        finalContent: String?,
        agentResponseJson: String?
    ) {
        // First, save reasoning content as a hidden message if it exists
        reasoningContent?.let { reasoning ->
            saveChatMessage(
                sessionId = sessionId,
                role = "assistant",
                content = reasoning,
                predictionJson = null,
                agentResponseJson = null,
                isHidden = true,
                type = "REASONING"
            )
        }

        // Then, save final content as a visible message if it exists
        finalContent?.let { final ->
            saveChatMessage(
                sessionId = sessionId,
                role = "assistant",
                content = final,
                predictionJson = null,
                agentResponseJson = agentResponseJson,
                isHidden = false,
                type = "ASSISTANT"
            )
        }

        // If we have both reasoning and final content, we could also save a combined message
        // For now, we'll just log that streaming message was saved
        Log.d("ChatRepo", "Saved streaming message: reasoning=${reasoningContent?.length ?: 0} chars, final=${finalContent?.length ?: 0} chars")
    }
}
