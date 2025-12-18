package com.Lyno.matchmindai.data.utils

import com.Lyno.matchmindai.domain.model.AgentResponse
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Utility object for parsing and cleaning AI responses.
 * Handles the "dirty JSON" problem where DeepSeek returns JSON strings
 * that may be wrapped in markdown code blocks.
 */
object ResponseParser {
    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
    }
    
    /**
     * Parse a raw AI response content string into an AgentResponse.
     * 
     * @param content The raw content string from DeepSeek response
     * @return Parsed AgentResponse, or a fallback text-only response if parsing fails
     */
    fun parse(content: String): AgentResponse {
        return try {
            // Step 1: Clean the content - remove markdown code blocks
            val cleanedContent = cleanJsonContent(content)
            
            // Step 2: Try to parse as AgentResponse
            json.decodeFromString<AgentResponse>(cleanedContent)
        } catch (e: Exception) {
            // Step 3: Fallback - treat as text-only response
            AgentResponse.chat(content)
        }
    }
    
    /**
     * Clean JSON content by removing markdown code blocks and trimming.
     */
    private fun cleanJsonContent(content: String): String {
        return content
            .trim()
            .removePrefix("```json")
            .removePrefix("```JSON")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
    }
    
    /**
     * Parse a JSON string into an AgentResponse with explicit error handling.
     * 
     * @param jsonString The JSON string to parse
     * @return Result containing either the parsed AgentResponse or an error
     */
    fun parseSafe(jsonString: String): Result<AgentResponse> {
        return try {
            val cleanedContent = cleanJsonContent(jsonString)
            Result.success(json.decodeFromString<AgentResponse>(cleanedContent))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if a string appears to be JSON (starts with { and ends with }).
     */
    fun isJsonLike(content: String): Boolean {
        val trimmed = content.trim()
        return trimmed.startsWith("{") && trimmed.endsWith("}")
    }
    
    /**
     * Extract JSON from a mixed content string that might contain text before/after JSON.
     */
    fun extractJsonFromMixedContent(content: String): String? {
        val jsonStart = content.indexOf('{')
        val jsonEnd = content.lastIndexOf('}')
        
        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            return content.substring(jsonStart, jsonEnd + 1)
        }
        
        return null
    }
}
