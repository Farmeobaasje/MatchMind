package com.Lyno.matchmindai.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a single update from the DeepSeek R1 streaming API.
 * Contains both reasoning content (thinking process) and final content.
 */
@Serializable
data class ChatStreamUpdate(
    @SerialName("id") val id: String? = null,
    @SerialName("choices") val choices: List<StreamChoice> = emptyList(),
    @SerialName("usage") val usage: DeepSeekUsage? = null
) {
    /**
     * Extracts the reasoning content from the first choice.
     * @return Reasoning content if available, null otherwise
     */
    fun getReasoningContent(): String? {
        return choices.firstOrNull()?.delta?.reasoningContent
    }

    /**
     * Extracts the final content from the first choice.
     * @return Final content if available, null otherwise
     */
    fun getContent(): String? {
        return choices.firstOrNull()?.delta?.content
    }

    /**
     * Checks if this is a final update (stream complete).
     * @return True if this update indicates stream completion
     */
    fun isDone(): Boolean {
        return choices.firstOrNull()?.finishReason == "stop"
    }
}

/**
 * Represents a choice in the streaming response.
 */
@Serializable
data class StreamChoice(
    @SerialName("index") val index: Int,
    @SerialName("delta") val delta: StreamDelta,
    @SerialName("finish_reason") val finishReason: String? = null
)

/**
 * Represents the delta content in a streaming response.
 * Contains both reasoning content (thinking process) and final content.
 */
@Serializable
data class StreamDelta(
    @SerialName("reasoning_content") val reasoningContent: String? = null,
    @SerialName("content") val content: String? = null,
    @SerialName("role") val role: String? = null
)

/**
 * SSE (Server-Sent Events) data wrapper for parsing raw stream lines.
 */
@Serializable
data class SseData(
    @SerialName("data") val data: String
)

/**
 * Exception thrown when SSE parsing fails.
 */
class SseParseException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Exception thrown when streaming connection fails.
 */
class StreamConnectionException(message: String, cause: Throwable? = null) : Exception(message, cause)
