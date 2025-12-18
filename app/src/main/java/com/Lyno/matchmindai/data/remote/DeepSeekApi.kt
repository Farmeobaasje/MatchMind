package com.Lyno.matchmindai.data.remote

import com.Lyno.matchmindai.data.ai.Tools
import com.Lyno.matchmindai.data.dto.DeepSeekRequest
import com.Lyno.matchmindai.data.dto.DeepSeekResponse
import com.Lyno.matchmindai.data.dto.DeepSeekRequestFactory
import com.Lyno.matchmindai.data.dto.FunctionDto
import com.Lyno.matchmindai.data.dto.ToolDto
import com.Lyno.matchmindai.data.dto.ChatStreamUpdate
import com.Lyno.matchmindai.data.dto.StreamConnectionException
import com.Lyno.matchmindai.data.utils.SseParser
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.add
import kotlinx.serialization.encodeToString

/**
 * Ktor-based HTTP client for DeepSeek API.
 * Implements dynamic authentication where the API key is passed as a parameter.
 */
class DeepSeekApi(private val client: HttpClient) {

    companion object {
        private const val BASE_URL = "https://api.deepseek.com"
        private const val CHAT_ENDPOINT = "/chat/completions"
        private const val AUTH_HEADER = "Authorization"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        prettyPrint = true
    }

    /**
     * Converts JsonObject tool definition to ToolDto for DeepSeek API.
     */
    private fun convertJsonToolToDto(jsonTool: JsonObject): ToolDto {
        val functionObj = jsonTool["function"] as JsonObject
        return ToolDto(
            type = jsonTool["type"]?.toString()?.trim('"') ?: "function",
            function = FunctionDto(
                name = functionObj["name"]?.toString()?.trim('"') ?: "",
                description = functionObj["description"]?.toString()?.trim('"') ?: "",
                parameters = functionObj["parameters"] as JsonObject
            )
        )
    }

    /**
     * Gets all available tools as ToolDto list for DeepSeek API.
     */
    private fun getAllTools(): List<ToolDto> {
        return Tools.getAllTools().map { convertJsonToolToDto(it) }
    }

    /**
     * Creates a request with tool support for agentic workflow.
     * Supports both deepseek-chat (V3) and deepseek-reasoner (R1) models.
     *
     * @param messages The conversation messages
     * @param includeTools Whether to include tools (not supported by R1)
     * @param temperature Temperature for response generation
     * @param responseFormat Response format specification
     * @param model The model to use: "deepseek-chat" (V3) or "deepseek-reasoner" (R1)
     */
    fun createAgenticRequest(
        messages: List<com.Lyno.matchmindai.data.dto.DeepSeekMessage>,
        includeTools: Boolean = true,
        temperature: Double = 0.5,
        responseFormat: com.Lyno.matchmindai.data.dto.ResponseFormat? = com.Lyno.matchmindai.data.dto.ResponseFormat(type = "json_object"),
        model: String = "deepseek-chat"
    ): DeepSeekRequest {
        // R1 model doesn't support tools or system messages
        val isR1Model = model == "deepseek-reasoner"

        val tools = if (includeTools && !isR1Model) {
            getAllTools()
        } else {
            null
        }

        // For R1 model, convert system messages to user messages (as per report Section 10.2)
        val processedMessages = if (isR1Model) {
            messages.map { msg ->
                if (msg.role == "system") {
                    // Convert system message to user message for R1
                    com.Lyno.matchmindai.data.dto.DeepSeekMessage(
                        role = "user",
                        content = "CONTEXT: ${msg.content}",
                        toolCalls = msg.toolCalls,
                        toolCallId = msg.toolCallId
                    )
                } else {
                    msg
                }
            }
        } else {
            messages
        }

        return DeepSeekRequest(
            model = model,
            messages = processedMessages,
            tools = tools,
            toolChoice = if (includeTools && !isR1Model) "auto" else null,
            responseFormat = responseFormat,
            temperature = temperature
        )
    }

    /**
     * Sends a prediction request to DeepSeek API.
     * @param apiKey The user's DeepSeek API key (dynamic authentication)
     * @param request The prediction request
     * @return DeepSeekResponse from the API
     * @throws DeepSeekApiException if the request fails
     */
    suspend fun getPrediction(
        apiKey: String,
        request: DeepSeekRequest
    ): DeepSeekResponse {
        return try {
            val response: HttpResponse = client.post("$BASE_URL$CHAT_ENDPOINT") {
                header(AUTH_HEADER, "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (response.status.value in 200..299) {
                response.body()
            } else {
                throw DeepSeekApiException(
                    "API request failed with status ${response.status.value}",
                    response.status.value
                )
            }
        } catch (e: Exception) {
            throw DeepSeekApiException(
                "Network error: ${e.message}",
                cause = e
            )
        }
    }

    /**
     * Simplified method to get a match prediction.
     * @param apiKey The user's DeepSeek API key
     * @param homeTeam The home team name
     * @param awayTeam The away team name
     * @return DeepSeekResponse containing the prediction
     */
    suspend fun predictMatch(
        apiKey: String,
        homeTeam: String,
        awayTeam: String
    ): DeepSeekResponse {
        val request = DeepSeekRequestFactory.createMatchPredictionRequest(homeTeam, awayTeam)
        return getPrediction(apiKey, request)
    }

    /**
     * Performs deep analysis using DeepSeek R1 (reasoner) model.
     * This model provides reasoning traces for complex analytical tasks.
     *
     * @param apiKey The user's DeepSeek API key
     * @param matchContext The comprehensive match context for analysis
     * @param userQuery The specific user question about the match
     * @return DeepSeekResponse with reasoning content and final answer
     */
    suspend fun performDeepAnalysis(
        apiKey: String,
        matchContext: com.Lyno.matchmindai.data.dto.MatchContextDto,
        userQuery: String
    ): DeepSeekResponse {
        // Serialize match context to JSON string for the prompt
        val contextJson = json.encodeToString(
            com.Lyno.matchmindai.data.dto.MatchContextDto.serializer(),
            matchContext
        )

        // Build the single comprehensive prompt (as per report Section 5)
        val prompt = buildString {
            appendLine("DATUM VANDAAG: ${matchContext.analysisDate}")
            appendLine()
            appendLine("DATA CONTEXT (API-SPORTS & TAVILY):")
            appendLine(contextJson)
            appendLine()
            appendLine("TAAK:")
            appendLine(userQuery)
            appendLine()
            appendLine("INSTRUCTIES:")
            appendLine("1. Analyseer de data grondig en methodisch.")
            appendLine("2. Bij conflicten tussen API data en nieuws, geloof het recentere nieuws.")
            appendLine("3. Geef een gestructureerd antwoord in het NEDERLANDS.")
            appendLine("4. Baseer je analyse op feiten uit de context.")
        }

        // Create request with R1 model (no system messages, no tools)
        val messages = listOf(
            com.Lyno.matchmindai.data.dto.DeepSeekMessage(
                role = "user",
                content = prompt
            )
        )

        val request = createAgenticRequest(
            messages = messages,
            includeTools = false, // R1 doesn't support tools
            temperature = 0.7, // Slightly higher for reasoning
            responseFormat = null, // R1 handles its own formatting
            model = "deepseek-reasoner"
        )

        return getPrediction(apiKey, request)
    }

    /**
     * Streams chat completions from DeepSeek R1 (reasoner) model using Server-Sent Events (SSE).
     * This method supports real-time streaming of reasoning content and final answers with memory safety.
     *
     * @param apiKey The user's DeepSeek API key
     * @param request The chat completion request (must have stream: true and model: "deepseek-reasoner")
     * @return Flow of ChatStreamUpdate objects containing reasoning and content deltas
     * @throws StreamConnectionException if the streaming connection fails
     */
    suspend fun streamChat(
        apiKey: String,
        request: DeepSeekRequest
    ): Flow<ChatStreamUpdate> = flow {
        try {
            // Pre-flight memory check before starting streaming
            val freeMem = Runtime.getRuntime().freeMemory()
            val minRequiredMemory = 10 * 1024 * 1024 // 10MB minimum free memory
            
            if (freeMem < minRequiredMemory) {
                // Graceful degradation: emit error instead of crashing
                android.util.Log.w("DeepSeekApi", 
                    "Insufficient memory for streaming. Free: ${freeMem / 1024 / 1024}MB, Required: ${minRequiredMemory / 1024 / 1024}MB")
                
                // Create error update for graceful degradation
                val errorUpdate = ChatStreamUpdate(
                    id = "memory_error",
                    choices = listOf(
                        com.Lyno.matchmindai.data.dto.StreamChoice(
                            index = 0,
                            delta = com.Lyno.matchmindai.data.dto.StreamDelta(
                                content = "⚠️ Apparaat geheugen is te vol om streaming te starten. Probeer andere apps te sluiten.",
                                role = "assistant"
                            ),
                            finishReason = "stop"
                        )
                    )
                )
                emit(errorUpdate)
                return@flow
            }

            val response: HttpResponse = client.post("$BASE_URL$CHAT_ENDPOINT") {
                header(AUTH_HEADER, "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(request.copy(stream = true)) // Ensure streaming is enabled
            }

            if (response.status.value in 200..299) {
                val sseParser = SseParser(json)
                sseParser.parseStream(response.bodyAsChannel()).collect { update ->
                    emit(update)
                }
            } else {
                throw StreamConnectionException(
                    "Streaming request failed with status ${response.status.value}"
                )
            }
        } catch (e: com.Lyno.matchmindai.data.utils.LowMemoryException) {
            // Graceful degradation for low memory during streaming
            android.util.Log.w("DeepSeekApi", "Streaming stopped due to low memory: ${e.message}")
            
            val errorUpdate = ChatStreamUpdate(
                id = "low_memory_error",
                choices = listOf(
                    com.Lyno.matchmindai.data.dto.StreamChoice(
                        index = 0,
                        delta = com.Lyno.matchmindai.data.dto.StreamDelta(
                            content = "⚠️ Streaming gestopt wegens geheugenlimiet. De AI-response was te groot voor dit apparaat.",
                            role = "assistant"
                        ),
                        finishReason = "stop"
                    )
                )
            )
            emit(errorUpdate)
        } catch (e: com.Lyno.matchmindai.data.utils.BufferOverflowException) {
            // Graceful degradation for buffer overflow
            android.util.Log.w("DeepSeekApi", "Buffer overflow during streaming: ${e.message}")
            
            val errorUpdate = ChatStreamUpdate(
                id = "buffer_overflow_error",
                choices = listOf(
                    com.Lyno.matchmindai.data.dto.StreamChoice(
                        index = 0,
                        delta = com.Lyno.matchmindai.data.dto.StreamDelta(
                            content = "⚠️ Response te groot voor verwerking. Probeer een kortere vraag.",
                            role = "assistant"
                        ),
                        finishReason = "stop"
                    )
                )
            )
            emit(errorUpdate)
        } catch (e: OutOfMemoryError) {
            // Last resort: catch actual OutOfMemoryError and attempt graceful recovery
            android.util.Log.e("DeepSeekApi", "OutOfMemoryError caught during streaming: ${e.message}")
            System.gc() // Suggest garbage collection to OS
            
            val errorUpdate = ChatStreamUpdate(
                id = "out_of_memory_error",
                choices = listOf(
                    com.Lyno.matchmindai.data.dto.StreamChoice(
                        index = 0,
                        delta = com.Lyno.matchmindai.data.dto.StreamDelta(
                            content = "⚠️ Kritiek geheugentekort. Stream gestopt om crash te voorkomen.",
                            role = "assistant"
                        ),
                        finishReason = "stop"
                    )
                )
            )
            emit(errorUpdate)
        } catch (e: Exception) {
            throw StreamConnectionException(
                "Streaming connection error: ${e.message}",
                e
            )
        }
    }

    /**
     * Creates a streaming request for DeepSeek R1 with streaming enabled.
     * This is a convenience method that ensures the request is properly configured for streaming.
     *
     * @param messages The conversation messages
     * @param temperature Temperature for response generation
     * @param model The model to use (default: "deepseek-reasoner")
     * @return DeepSeekRequest configured for streaming
     */
    fun createStreamingRequest(
        messages: List<com.Lyno.matchmindai.data.dto.DeepSeekMessage>,
        temperature: Double = 0.7,
        model: String = "deepseek-reasoner"
    ): DeepSeekRequest {
        // Ensure model is R1 for streaming
        val finalModel = if (model != "deepseek-reasoner") "deepseek-reasoner" else model
        
        return createAgenticRequest(
            messages = messages,
            includeTools = false, // R1 doesn't support tools
            temperature = temperature,
            responseFormat = null, // R1 handles its own formatting
            model = finalModel
        ).copy(stream = true) // Enable streaming
    }
}

/**
 * Custom exception for DeepSeek API errors.
 */
class DeepSeekApiException(
    message: String,
    val statusCode: Int? = null,
    cause: Throwable? = null
) : Exception(message, cause)
