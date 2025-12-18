package com.Lyno.matchmindai.domain.repository

import com.Lyno.matchmindai.domain.model.ChatSession
import kotlinx.coroutines.flow.Flow
import com.Lyno.matchmindai.data.dto.ChatStreamUpdate

/**
 * Interface for chat operations.
 * Separates chat functionality from match prediction functionality.
 */
interface ChatRepository {
    /**
     * Get all chat sessions.
     * @return Flow of list of chat sessions
     */
    fun getChatSessions(): Flow<List<ChatSession>>

    /**
     * Get messages for a specific chat session.
     * @param sessionId The chat session ID
     * @return Flow of list of chat message entities
     */
    fun getChatMessages(sessionId: String): Flow<List<com.Lyno.matchmindai.data.local.entity.ChatMessageEntity>>

    /**
     * Create a new chat session.
     * @param title The title of the new session
     * @return The ID of the created session
     */
    suspend fun createChatSession(title: String): String

    /**
     * Update the title of a chat session.
     * @param sessionId The chat session ID
     * @param title The new title
     */
    suspend fun updateChatSessionTitle(sessionId: String, title: String)

    /**
     * Delete a chat session and all its messages.
     * @param sessionId The chat session ID
     */
    suspend fun deleteChatSession(sessionId: String)

    /**
     * Get the last chat session ID, or create a new one if none exists.
     * @return The session ID
     */
    suspend fun getOrCreateLastSession(): String

    /**
     * Save a chat message to the database.
     * @param sessionId The chat session ID
     * @param role The role of message sender (user or assistant)
     * @param content The message content
     * @param predictionJson Optional JSON string of prediction data for assistant messages (legacy)
     * @param agentResponseJson Optional JSON string of agent response data for new flow
     * @param isHidden Whether this message should be hidden from the UI (e.g., tool outputs)
     * @param type Optional type classification (e.g., "USER", "ASSISTANT", "TOOL_OUTPUT")
     */
    suspend fun saveChatMessage(
        sessionId: String,
        role: String,
        content: String? = null,
        predictionJson: String? = null,
        agentResponseJson: String? = null,
        isHidden: Boolean = false,
        type: String? = null
    )

    /**
     * Get context messages for AI processing (sliding window).
     * Returns system prompt + last 10 visible messages.
     * @param sessionId The chat session ID
     * @return List of chat message entities for AI context
     */
    suspend fun getContextMessages(sessionId: String): List<com.Lyno.matchmindai.data.local.entity.ChatMessageEntity>

    /**
     * Save tool output as a hidden message for context retention.
     * @param sessionId The chat session ID
     * @param toolName The name of the tool that generated the output
     * @param result The tool result as JSON string
     */
    suspend fun saveToolOutput(sessionId: String, toolName: String, result: String)

    /**
     * Clear all messages from a chat session while keeping the session itself.
     * @param sessionId The chat session ID to clear
     */
    suspend fun clearSession(sessionId: String)

    /**
     * Stream chat completions from DeepSeek R1 (reasoner) model.
     * This method supports real-time streaming of reasoning content and final answers.
     *
     * @param apiKey The user's DeepSeek API key
     * @param request The chat completion request
     * @return Flow of ChatStreamUpdate objects containing reasoning and content deltas
     */
    suspend fun streamChat(
        apiKey: String,
        request: com.Lyno.matchmindai.data.dto.DeepSeekRequest
    ): Flow<ChatStreamUpdate>

    /**
     * Save streaming chat message with reasoning and final content.
     * This method handles the special case of streaming responses where we have
     * both reasoning content (thinking process) and final content.
     *
     * @param sessionId The chat session ID
     * @param reasoningContent The AI's reasoning/thinking process
     * @param finalContent The final answer/content
     * @param agentResponseJson Optional JSON string of agent response data
     */
    suspend fun saveStreamingChatMessage(
        sessionId: String,
        reasoningContent: String?,
        finalContent: String?,
        agentResponseJson: String? = null
    )
}
