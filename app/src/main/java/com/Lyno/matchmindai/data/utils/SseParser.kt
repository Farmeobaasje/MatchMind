package com.Lyno.matchmindai.data.utils

import android.util.Log
import com.Lyno.matchmindai.data.dto.ChatStreamUpdate
import com.Lyno.matchmindai.data.dto.SseParseException
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

/**
 * Utility class for parsing Server-Sent Events (SSE) from DeepSeek R1 streaming API.
 * Handles line-by-line parsing of SSE format and JSON deserialization with memory safety.
 */
class SseParser(private val json: Json) {

    companion object {
        private const val SSE_DATA_PREFIX = "data: "
        private const val SSE_DONE_MARKER = "[DONE]"
        private const val SSE_EVENT_PREFIX = "event: "
        private const val SSE_COMMENT_PREFIX = ":"
        
        // Memory safety constants
        private const val MAX_BUFFER_SIZE = 1024 * 1024 // 1MB Hard Limit
        private const val MIN_SAFE_MEMORY = 5 * 1024 * 1024 // 5MB Min Free Memory
    }

    /**
     * Parses a ByteReadChannel containing SSE data and emits ChatStreamUpdate objects.
     * Implements bounded buffering with memory safety checks to prevent OutOfMemoryError.
     * @param channel The ByteReadChannel from the HTTP response
     * @return Flow of ChatStreamUpdate objects
     */
    suspend fun parseStream(channel: ByteReadChannel): Flow<ChatStreamUpdate> = flow {
        try {
            // Pre-flight memory check
            checkAvailableMemory()
            
            var currentData = StringBuilder()
            var currentBufferSize = 0
            var done = false
            
            while (!done) {
                // Check memory before reading each line
                checkAvailableMemory()
                
                val line = channel.readUTF8Line() ?: break
                
                // Check buffer limit before processing
                if (currentBufferSize + line.length > MAX_BUFFER_SIZE) {
                    // Buffer overflow - reset for safety
                    currentData.clear()
                    currentBufferSize = 0
                    throw BufferOverflowException("SSE Buffer limit exceeded (max: ${MAX_BUFFER_SIZE} bytes)")
                }
                
                when {
                    // Skip empty lines (delimiters between SSE messages)
                    line.isEmpty() -> {
                        if (currentData.isNotEmpty()) {
                            processDataChunk(currentData.toString())?.let { update ->
                                emit(update)
                            }
                            currentData.clear()
                            currentBufferSize = 0
                        }
                    }
                    
                    // Skip comment lines
                    line.startsWith(SSE_COMMENT_PREFIX) -> {
                        // Comments are ignored
                    }
                    
                    // Skip event type lines (we only care about data)
                    line.startsWith(SSE_EVENT_PREFIX) -> {
                        // Event types are ignored for now
                    }
                    
                    // Process data lines
                    line.startsWith(SSE_DATA_PREFIX) -> {
                        val dataContent = line.substring(SSE_DATA_PREFIX.length)
                        
                        // Check for termination marker
                        if (dataContent == SSE_DONE_MARKER) {
                            done = true
                        } else {
                            // Append to current data chunk
                            currentData.append(dataContent)
                            currentBufferSize += dataContent.length
                        }
                    }
                    
                    // Continuation lines (data spanning multiple lines)
                    else -> {
                        currentData.append(line)
                        currentBufferSize += line.length
                    }
                }
            }
            
            // Process any remaining data
            if (currentData.isNotEmpty()) {
                processDataChunk(currentData.toString())?.let { update ->
                    emit(update)
                }
            }
            
        } catch (e: LowMemoryException) {
            // Graceful degradation for low memory
            android.util.Log.w("SseParser", "Streaming aborted due to low memory: ${e.message}")
            throw SseParseException("Streaming stopped due to low memory", e)
        } catch (e: BufferOverflowException) {
            // Graceful degradation for buffer overflow
            android.util.Log.w("SseParser", "Buffer overflow during streaming: ${e.message}")
            throw SseParseException("Streaming buffer overflow", e)
        } catch (e: Exception) {
            throw SseParseException("Failed to parse SSE stream: ${e.message}", e)
        }
    }

    /**
     * Processes a single data chunk and converts it to ChatStreamUpdate.
     * @param dataChunk The raw JSON data chunk
     * @return ChatStreamUpdate if valid, null if empty or invalid
     */
    private fun processDataChunk(dataChunk: String): ChatStreamUpdate? {
        if (dataChunk.isBlank()) {
            return null
        }
        
        return try {
            json.decodeFromString<ChatStreamUpdate>(dataChunk)
        } catch (e: Exception) {
            // Log parsing errors but don't crash the stream
            android.util.Log.e("SseParser", "Failed to parse data chunk: $dataChunk", e)
            null
        }
    }

    /**
     * Simple line-by-line parser for testing or direct string input.
     * @param rawStream The raw SSE string
     * @return List of ChatStreamUpdate objects
     */
    fun parseRawStream(rawStream: String): List<ChatStreamUpdate> {
        // Check memory before processing
        checkAvailableMemory()
        
        val updates = mutableListOf<ChatStreamUpdate>()
        val lines = rawStream.lines()
        var currentData = StringBuilder()
        var currentBufferSize = 0
        
        for (line in lines) {
            // Check buffer limit
            if (currentBufferSize + line.length > MAX_BUFFER_SIZE) {
                // Buffer overflow - reset for safety
                currentData.clear()
                currentBufferSize = 0
                throw BufferOverflowException("SSE Buffer limit exceeded in raw stream parsing")
            }
            
            when {
                line.isEmpty() -> {
                    if (currentData.isNotEmpty()) {
                        processDataChunk(currentData.toString())?.let { updates.add(it) }
                        currentData.clear()
                        currentBufferSize = 0
                    }
                }
                line.startsWith(SSE_DATA_PREFIX) -> {
                    val dataContent = line.substring(SSE_DATA_PREFIX.length)
                    if (dataContent == SSE_DONE_MARKER) {
                        break
                    }
                    currentData.append(dataContent)
                    currentBufferSize += dataContent.length
                }
                else -> {
                    currentData.append(line)
                    currentBufferSize += line.length
                }
            }
        }
        
        if (currentData.isNotEmpty()) {
            processDataChunk(currentData.toString())?.let { updates.add(it) }
        }
        
        return updates
    }
    
    /**
     * Checks if there is sufficient available memory for streaming operations.
     * @throws LowMemoryException if available memory is below the safe threshold
     */
    private fun checkAvailableMemory() {
        val runtime = Runtime.getRuntime()
        val freeMemory = runtime.freeMemory()
        val totalMemory = runtime.totalMemory()
        val maxMemory = runtime.maxMemory()
        
        // Calculate available memory (free + (max - total))
        val availableMemory = freeMemory + (maxMemory - totalMemory)
        
        if (availableMemory < MIN_SAFE_MEMORY) {
            throw LowMemoryException(
                "Critical low memory detected. Available: ${availableMemory / 1024 / 1024}MB, " +
                "Required: ${MIN_SAFE_MEMORY / 1024 / 1024}MB"
            )
        }
    }
}
