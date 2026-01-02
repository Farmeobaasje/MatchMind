 package com.Lyno.matchmindai.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Lyno.matchmindai.data.repository.MatchRepositoryImpl
import com.Lyno.matchmindai.data.repository.ChatRepositoryImpl
import com.Lyno.matchmindai.domain.model.AgentResponse
import com.Lyno.matchmindai.presentation.model.ChatMessage
import com.Lyno.matchmindai.presentation.model.ChatRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import kotlinx.serialization.json.*
import java.util.UUID

    /**
     * ViewModel for the chat screen that handles flexible AI Agent responses.
     * Now supports polymorphic UI rendering based on response types.
     */
    class ChatViewModel(
        private val matchRepository: MatchRepositoryImpl,
        private val chatRepository: ChatRepositoryImpl
    ) : ViewModel() {
        
    private val jsonSerializer = Json { ignoreUnknownKeys = true }

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Idle)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _currentToolAction = MutableStateFlow<String?>(null)
    val currentToolAction: StateFlow<String?> = _currentToolAction.asStateFlow()

    private var currentSessionId: String? = null
    private var isNewSession: Boolean = true // Track if this is a newly created session

    init {
        Log.d("ChatDebug", "🔵 ChatViewModel.init() - STARTING FRESH CHAT")
        viewModelScope.launch {
            // ALWAYS start with a fresh chat session
            // This prevents loading old messages and ensures clean state
            try {
                Log.d("ChatDebug", "🆕 Creating fresh chat session...")
                val createdSessionId = chatRepository.createChatSession("Nieuwe Chat")
                currentSessionId = createdSessionId
                Log.d("ChatDebug", "✅ Created fresh chat session: $createdSessionId")
            } catch (e: Exception) {
                Log.e("ChatDebug", "❌ Failed to create fresh session: ${e.message}")
                // Fallback: try to get any session
                currentSessionId = chatRepository.getOrCreateLastSession()
                Log.d("ChatDebug", "🔄 Fallback to existing session: $currentSessionId")
            }
            Log.d("ChatDebug", "📥 Loading messages for session: $currentSessionId")
            loadMessages()
        }
    }

    private fun loadMessages() {
        currentSessionId?.let { sessionId ->
            viewModelScope.launch {
                // Use getVisibleMessages to filter out hidden messages (tool outputs)
                chatRepository.getChatMessages(sessionId).collect { entities ->
                    val chatMessages = entities
                        .filter { !it.isHidden } // Filter out hidden messages
                        .mapNotNull { entity ->
                            when (entity.role) {
                                "user" -> ChatMessage(
                                    id = entity.id,
                                    role = ChatRole.USER,
                                    text = entity.content ?: "",
                                    timestamp = entity.timestamp,
                                    isHidden = entity.isHidden
                                )
                                "assistant" -> {
                                    val agentResponse = try {
                                        entity.agentResponseJson?.let { jsonStr ->
                                            jsonSerializer.decodeFromString<AgentResponse>(jsonStr)
                                        }
                                    } catch (e: Exception) {
                                        // Fallback for old messages or parsing errors
                                        entity.content?.let { content ->
                                            AgentResponse(
                                                text = content,
                                                type = AgentResponse.ResponseType.TEXT_ONLY,
                                                relatedData = null
                                            )
                                        }
                                    }
                                    
                                    agentResponse?.let { response ->
                                        ChatMessage(
                                            id = entity.id,
                                            role = ChatRole.ASSISTANT,
                                            agentResponse = response,
                                            timestamp = entity.timestamp,
                                            isHidden = entity.isHidden
                                        )
                                    }
                                }
                                else -> null
                            }
                        }
                    _messages.update { chatMessages }
                }
            }
        }
    }

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        // Add user message immediately
        val userChatMessage = ChatMessage(
            id = java.util.UUID.randomUUID().toString(),
            role = ChatRole.USER,
            text = userMessage,
            timestamp = System.currentTimeMillis()
        )
        _messages.update { it + userChatMessage }

        // Save user message to database
        currentSessionId?.let { sessionId ->
            viewModelScope.launch {
                chatRepository.saveChatMessage(
                    sessionId = sessionId,
                    role = "user",
                    content = userMessage,
                    predictionJson = null,
                    agentResponseJson = null,
                    isHidden = false,
                    type = "USER"
                )
            }
        }

        // Process with AI Agent
        _uiState.update { ChatUiState.Loading() }
        _currentToolAction.update { null } // Reset tool action
        
        viewModelScope.launch {
            try {
                val result = matchRepository.processUserQuery(userMessage)
                
                result.fold(
                    onSuccess = { agentResponse ->
                        // Check if the AI response is "lazy" (says it will look but didn't call tools)
                        val lazyPatterns = listOf(
                            "ik ga kijken",
                            "even opzoeken",
                            "zal ik zoeken",
                            "ik ga even kijken",
                            "een momentje",
                            "ik zoek het op"
                        )
                        
                        val isLazyResponse = lazyPatterns.any { pattern ->
                            agentResponse.text.contains(pattern, ignoreCase = true)
                        }
                        
                        if (isLazyResponse) {
                            Log.d("ChatDebug", "Detected lazy AI response, triggering retry")
                            // Send system message to force tool usage
                            val systemMessage = "SYSTEM: Je hebt geen tool aangeroepen. Roep NU de juiste tool aan."
                            val retryResult = matchRepository.processUserQuery(systemMessage)
                            
                            retryResult.fold(
                                onSuccess = { retryResponse ->
                                    val agentChatMessage = ChatMessage(
                                        id = java.util.UUID.randomUUID().toString(),
                                        role = ChatRole.ASSISTANT,
                                        agentResponse = retryResponse,
                                        timestamp = System.currentTimeMillis()
                                    )
                                    _messages.update { it + agentChatMessage }
                                    
                                    // Save assistant message to database
                                    currentSessionId?.let { sessionId ->
                                        val agentResponseJson = try {
                                            Json.encodeToString(AgentResponse.serializer(), retryResponse)
                                        } catch (e: Exception) {
                                            null
                                        }
                                        chatRepository.saveChatMessage(
                                            sessionId = sessionId,
                                            role = "assistant",
                                            content = retryResponse.text,
                                            predictionJson = null,
                                            agentResponseJson = agentResponseJson,
                                            isHidden = false,
                                            type = "ASSISTANT"
                                        )
                                    }
                                },
                                onFailure = { error ->
                                    // Fallback to original response if retry fails
                                    val agentChatMessage = ChatMessage(
                                        id = java.util.UUID.randomUUID().toString(),
                                        role = ChatRole.ASSISTANT,
                                        agentResponse = agentResponse,
                                        timestamp = System.currentTimeMillis()
                                    )
                                    _messages.update { it + agentChatMessage }
                                    
                                currentSessionId?.let { sessionId ->
                                val agentResponseJson = jsonSerializer.encodeToString(AgentResponse.serializer(), agentResponse)
                                        chatRepository.saveChatMessage(
                                            sessionId = sessionId,
                                            role = "assistant",
                                            content = agentResponse.text,
                                            predictionJson = null,
                                            agentResponseJson = agentResponseJson,
                                            isHidden = false,
                                            type = "ASSISTANT"
                                        )
                                    }
                                }
                            )
                        } else {
                            val agentChatMessage = ChatMessage(
                                id = java.util.UUID.randomUUID().toString(),
                                role = ChatRole.ASSISTANT,
                                agentResponse = agentResponse,
                                timestamp = System.currentTimeMillis()
                            )
                            _messages.update { it + agentChatMessage }
                            
                            // Save assistant message to database
                            currentSessionId?.let { sessionId ->
                                val agentResponseJson = Json.encodeToString(AgentResponse.serializer(), agentResponse)
                                chatRepository.saveChatMessage(
                                    sessionId = sessionId,
                                    role = "assistant",
                                    content = agentResponse.text,
                                    predictionJson = null,
                                    agentResponseJson = agentResponseJson,
                                    isHidden = false,
                                    type = "ASSISTANT"
                                )
                            }
                        }
                        
                        _uiState.update { ChatUiState.Idle }
                        _currentToolAction.update { null }
                    },
                    onFailure = { error ->
                        _uiState.update { 
                            ChatUiState.Error("Fout bij verwerken van bericht: ${error.message}")
                        }
                        
                        // Add error message as agent response
                        val errorResponse = AgentResponse(
                            text = "Sorry, er ging iets mis. Probeer het opnieuw.",
                            type = AgentResponse.ResponseType.TEXT_ONLY,
                            relatedData = null
                        )
                        val errorChatMessage = ChatMessage(
                            id = java.util.UUID.randomUUID().toString(),
                            role = ChatRole.ASSISTANT,
                            agentResponse = errorResponse,
                            timestamp = System.currentTimeMillis()
                        )
                        _messages.update { it + errorChatMessage }
                        
                        _currentToolAction.update { null }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { 
                    ChatUiState.Error("Onverwachte fout: ${e.message}")
                }
                _currentToolAction.update { null }
            } finally {
                // Ensure we're not stuck in loading state
                if (_uiState.value is ChatUiState.Loading) {
                    _uiState.update { ChatUiState.Idle }
                }
                _currentToolAction.update { null }
            }
        }
    }

    fun clearError() {
        _uiState.update { ChatUiState.Idle }
    }

    fun startNewSession() {
        viewModelScope.launch {
            val newSessionId = chatRepository.createChatSession("Nieuwe Chat")
            currentSessionId = newSessionId
            _messages.update { emptyList() }
        }
    }

    /**
     * Start automatic match analysis when navigated from MatchDetail screen.
     * This function handles the loading state properly with progressive status updates.
     */
    fun startAutomaticMatchAnalysis(homeTeam: String, awayTeam: String, league: String? = null) {
        Log.d("ChatDebug", "Starting automatic match analysis for: $homeTeam vs $awayTeam")
        
        viewModelScope.launch {
            try {
                // STAP 1: Toon DIRECT de user message
                val autoPrompt = if (league != null) {
                    "Analyseer de wedstrijd $homeTeam vs $awayTeam ($league). Geef een voorspelling, tactische analyse en check op blessures."
                } else {
                    "Analyseer de wedstrijd $homeTeam vs $awayTeam. Geef een voorspelling, tactische analyse en check op blessures."
                }
                
                Log.d("ChatDebug", "Auto-prompt: $autoPrompt")
                
                val userChatMessage = ChatMessage(
                    id = java.util.UUID.randomUUID().toString(),
                    role = ChatRole.USER,
                    text = autoPrompt,
                    timestamp = System.currentTimeMillis()
                )
                _messages.update { it + userChatMessage }
                
                // Save user message to database
                currentSessionId?.let { sessionId ->
                    chatRepository.saveChatMessage(
                        sessionId = sessionId,
                        role = "user",
                        content = autoPrompt,
                        predictionJson = null,
                        agentResponseJson = null,
                        isHidden = false,
                        type = "USER"
                    )
                }
                
                // STAP 2: Start Loading Sequence met progressive updates
                
                // Fase A: API Sports data ophalen
                _uiState.update { ChatUiState.Loading("📊 Statistieken en stand ophalen...") }
                Log.d("ChatDebug", "Phase 1: Fetching API Sports data")
                
                // Fase B: Context verzamelen (parallel of sequentieel)
                _uiState.update { ChatUiState.Loading("🔍 Context en nieuws verzamelen...") }
                Log.d("ChatDebug", "Phase 2: Gathering context and news")
                
                // Fase C: AI analyse
                _uiState.update { ChatUiState.Loading("🧠 AI analyseert tactiek en blessures...") }
                Log.d("ChatDebug", "Phase 3: AI analysis")
                
                // Process the query (dit doet alle fasen intern via ToolOrchestrator)
                val result = matchRepository.processUserQuery(autoPrompt)
                
                result.fold(
                    onSuccess = { agentResponse ->
                        Log.d("ChatDebug", "Analysis successful, received agent response")
                        
                        val agentChatMessage = ChatMessage(
                            id = java.util.UUID.randomUUID().toString(),
                            role = ChatRole.ASSISTANT,
                            agentResponse = agentResponse,
                            timestamp = System.currentTimeMillis()
                        )
                        _messages.update { it + agentChatMessage }
                        
                        // Save assistant message to database
                        currentSessionId?.let { sessionId ->
                            val agentResponseJson = Json.encodeToString(AgentResponse.serializer(), agentResponse)
                            chatRepository.saveChatMessage(
                                sessionId = sessionId,
                                role = "assistant",
                                content = agentResponse.text,
                                predictionJson = null,
                                agentResponseJson = agentResponseJson,
                                isHidden = false,
                                type = "ASSISTANT"
                            )
                        }
                        
                        _uiState.update { ChatUiState.Idle }
                        _currentToolAction.update { null }
                        Log.d("ChatDebug", "Analysis completed successfully")
                    },
                    onFailure = { error ->
                        Log.e("ChatDebug", "Analysis failed: ${error.message}", error)
                        
                        _uiState.update { 
                            ChatUiState.Error("Fout bij automatische analyse: ${error.message}")
                        }
                        
                        // Add error message as agent response
                        val errorResponse = AgentResponse(
                            text = "Sorry, de automatische analyse is mislukt. Probeer het handmatig.",
                            type = AgentResponse.ResponseType.TEXT_ONLY,
                            relatedData = null
                        )
                        val errorChatMessage = ChatMessage(
                            id = java.util.UUID.randomUUID().toString(),
                            role = ChatRole.ASSISTANT,
                            agentResponse = errorResponse,
                            timestamp = System.currentTimeMillis()
                        )
                        _messages.update { it + errorChatMessage }
                        
                        _currentToolAction.update { null }
                    }
                )
            } catch (e: Exception) {
                Log.e("ChatDebug", "Unexpected error in automatic analysis", e)
                _uiState.update { 
                    ChatUiState.Error("Onverwachte fout bij automatische analyse: ${e.message}")
                }
                _currentToolAction.update { null }
            } finally {
                // CRITICAL: Always ensure we're not stuck in loading state
                Log.d("ChatDebug", "Finally block executed, ensuring loading state is cleared")
                if (_uiState.value is ChatUiState.Loading) {
                    _uiState.update { ChatUiState.Idle }
                }
                _currentToolAction.update { null }
            }
        }
    }

    fun loadSession(sessionId: String) {
        currentSessionId = sessionId
        isNewSession = false // Loading an existing session, so it's not new
        loadMessages()
    }

    /**
     * Clear all messages from the current chat session while keeping the session.
     */
    fun clearHistory() {
        currentSessionId?.let { sessionId ->
            viewModelScope.launch {
                chatRepository.clearSession(sessionId)
                _messages.update { emptyList() }
            }
        }
    }

    /**
     * Send a message with streaming support for DeepSeek R1.
     * This method uses streaming to show reasoning content in real-time.
     *
     * @param userMessage The user's message
     * @param useStreaming Whether to use streaming (true for R1, false for regular models)
     */
    fun sendMessageWithStreaming(userMessage: String, useStreaming: Boolean = true) {
        if (userMessage.isBlank()) return

        // Add user message immediately
        val userChatMessage = ChatMessage(
            id = java.util.UUID.randomUUID().toString(),
            role = ChatRole.USER,
            text = userMessage,
            timestamp = System.currentTimeMillis()
        )
        _messages.update { it + userChatMessage }

        // Save user message to database
        currentSessionId?.let { sessionId ->
            viewModelScope.launch {
                chatRepository.saveChatMessage(
                    sessionId = sessionId,
                    role = "user",
                    content = userMessage,
                    predictionJson = null,
                    agentResponseJson = null,
                    isHidden = false,
                    type = "USER"
                )
            }
        }

        // Process with AI Agent
        _uiState.update { ChatUiState.Loading() }
        _currentToolAction.update { null } // Reset tool action
        
        viewModelScope.launch {
            try {
                if (useStreaming) {
                    // Try streaming first
                    try {
                        // For now, we'll use regular processing as a fallback
                        // In production, you would call:
                        // val streamFlow = matchRepository.performDeepAnalysisWithStreaming(
                        //     apiKey = deepSeekApiKey,
                        //     matchContext = matchContext,
                        //     userQuery = userMessage,
                        //     useStreaming = true
                        // )
                        
                        // For now, fall back to regular processing
                        Log.d("ChatDebug", "Streaming not fully implemented yet, falling back to regular processing")
                        val result = matchRepository.processUserQuery(userMessage)
                        handleResult(result)
                        
                    } catch (streamingError: Exception) {
                        Log.e("ChatDebug", "Streaming failed, falling back to regular processing", streamingError)
                        
                        // Fallback to regular processing
                        val result = matchRepository.processUserQuery(userMessage)
                        handleResult(result)
                    }
                } else {
                    val result = matchRepository.processUserQuery(userMessage)
                    handleResult(result)
                }
            } catch (e: Exception) {
                _uiState.update { 
                    ChatUiState.Error("Onverwachte fout: ${e.message}")
                }
                _currentToolAction.update { null }
            } finally {
                // Ensure we're not stuck in loading state
                if (_uiState.value is ChatUiState.Loading) {
                    _uiState.update { ChatUiState.Idle }
                }
                _currentToolAction.update { null }
            }
        }
    }
    
    /**
     * Send a Prophet Module query for advanced match analysis.
     * This uses the Phase 3 "Prophet Module" with "Anchor & Adjust" strategy.
     * 
     * @param query User query like "Predict Feyenoord vs Ajax"
     */
    fun sendProphetQuery(query: String) {
        if (query.isBlank()) return

        // Add user message immediately
        val userChatMessage = ChatMessage(
            id = java.util.UUID.randomUUID().toString(),
            role = ChatRole.USER,
            text = query,
            timestamp = System.currentTimeMillis()
        )
        _messages.update { it + userChatMessage }

        // Save user message to database
        currentSessionId?.let { sessionId ->
            viewModelScope.launch {
                chatRepository.saveChatMessage(
                    sessionId = sessionId,
                    role = "user",
                    content = query,
                    predictionJson = null,
                    agentResponseJson = null,
                    isHidden = false,
                    type = "USER"
                )
            }
        }

        // Process with Prophet Module
        _uiState.update { ChatUiState.Loading("🧠 Prophet Module analyseert...") }
        _currentToolAction.update { null } // Reset tool action
        
        viewModelScope.launch {
            try {
                // Check if this is a prediction query
                val lowerQuery = query.lowercase()
                val isPredictionQuery = lowerQuery.contains("predict") || 
                                        lowerQuery.contains("voorspel") ||
                                        lowerQuery.contains("wie wint") ||
                                        lowerQuery.contains("analyseer")
                
                if (isPredictionQuery) {
                    // Use the Prophet Module for prediction queries
                    val result = matchRepository.processProphetQuery(query)
                    handleResult(result)
                } else {
                    // Use regular processing for other queries
                    val result = matchRepository.processUserQuery(query)
                    handleResult(result)
                }
            } catch (e: Exception) {
                _uiState.update { 
                    ChatUiState.Error("Onverwachte fout bij Prophet analyse: ${e.message}")
                }
                _currentToolAction.update { null }
            } finally {
                // Ensure we're not stuck in loading state
                if (_uiState.value is ChatUiState.Loading) {
                    _uiState.update { ChatUiState.Idle }
                }
                _currentToolAction.update { null }
            }
        }
    }
    
    /**
     * Start automatic Prophet analysis for a specific match.
     * This is the Phase 3 implementation with Generative UI widgets.
     * 
     * @param matchFixture The match fixture to analyze
     */
    fun startProphetMatchAnalysis(matchFixture: com.Lyno.matchmindai.domain.model.MatchFixture) {
        Log.d("ChatDebug", "Starting Prophet analysis for: ${matchFixture.displayName}")
        
        viewModelScope.launch {
            try {
                // STAP 1: Toon DIRECT de user message
                val autoPrompt = "Analyseer de wedstrijd ${matchFixture.homeTeam} vs ${matchFixture.awayTeam} (${matchFixture.league}). Gebruik de Prophet Module voor een gedetailleerde analyse met statistieken en nieuwscontext."
                
                Log.d("ChatDebug", "Prophet auto-prompt: $autoPrompt")
                
                val userChatMessage = ChatMessage(
                    id = java.util.UUID.randomUUID().toString(),
                    role = ChatRole.USER,
                    text = autoPrompt,
                    timestamp = System.currentTimeMillis()
                )
                _messages.update { it + userChatMessage }
                
                // Save user message to database
                currentSessionId?.let { sessionId ->
                    chatRepository.saveChatMessage(
                        sessionId = sessionId,
                        role = "user",
                        content = autoPrompt,
                        predictionJson = null,
                        agentResponseJson = null,
                        isHidden = false,
                        type = "USER"
                    )
                }
                
                // STAP 2: Start Loading Sequence met progressive updates
                
                // Fase A: API Sports data ophalen
                _uiState.update { ChatUiState.Loading("📊 Statistieken en voorspellingen ophalen...") }
                Log.d("ChatDebug", "Prophet Phase 1: Fetching API Sports predictions")
                
                // Fase B: News context verzamelen
                _uiState.update { ChatUiState.Loading("🔍 Nieuws en blessurecontext verzamelen...") }
                Log.d("ChatDebug", "Prophet Phase 2: Gathering news context")
                
                // Fase C: AI analyse met "Anchor & Adjust" strategie
                _uiState.update { ChatUiState.Loading("🧠 AI analyseert met Anchor & Adjust strategie...") }
                Log.d("ChatDebug", "Prophet Phase 3: AI analysis with Anchor & Adjust")
                
                // Process with Prophet Module
                val result = matchRepository.processProphetQuery(autoPrompt)
                
                result.fold(
                    onSuccess = { agentResponse ->
                        Log.d("ChatDebug", "Prophet analysis successful, received agent response")
                        
                        val agentChatMessage = ChatMessage(
                            id = java.util.UUID.randomUUID().toString(),
                            role = ChatRole.ASSISTANT,
                            agentResponse = agentResponse,
                            timestamp = System.currentTimeMillis()
                        )
                        _messages.update { it + agentChatMessage }
                        
                        // Save assistant message to database
                        currentSessionId?.let { sessionId ->
                            val agentResponseJson = Json.encodeToString(AgentResponse.serializer(), agentResponse)
                            chatRepository.saveChatMessage(
                                sessionId = sessionId,
                                role = "assistant",
                                content = agentResponse.text,
                                predictionJson = null,
                                agentResponseJson = agentResponseJson,
                                isHidden = false,
                                type = "ASSISTANT"
                            )
                        }
                        
                        _uiState.update { ChatUiState.Idle }
                        _currentToolAction.update { null }
                        Log.d("ChatDebug", "Prophet analysis completed successfully")
                    },
                    onFailure = { error ->
                        Log.e("ChatDebug", "Prophet analysis failed: ${error.message}", error)
                        
                        _uiState.update { 
                            ChatUiState.Error("Fout bij Prophet analyse: ${error.message}")
                        }
                        
                        // Add error message as agent response
                        val errorResponse = AgentResponse(
                            text = "Sorry, de Prophet analyse is mislukt. Probeer het handmatig.",
                            type = AgentResponse.ResponseType.TEXT_ONLY,
                            relatedData = null
                        )
                        val errorChatMessage = ChatMessage(
                            id = java.util.UUID.randomUUID().toString(),
                            role = ChatRole.ASSISTANT,
                            agentResponse = errorResponse,
                            timestamp = System.currentTimeMillis()
                        )
                        _messages.update { it + errorChatMessage }
                        
                        _currentToolAction.update { null }
                    }
                )
            } catch (e: Exception) {
                Log.e("ChatDebug", "Unexpected error in Prophet analysis", e)
                _uiState.update { 
                    ChatUiState.Error("Onverwachte fout bij Prophet analyse: ${e.message}")
                }
                _currentToolAction.update { null }
            } finally {
                // CRITICAL: Always ensure we're not stuck in loading state
                Log.d("ChatDebug", "Finally block executed, ensuring loading state is cleared")
                if (_uiState.value is ChatUiState.Loading) {
                    _uiState.update { ChatUiState.Idle }
                }
                _currentToolAction.update { null }
            }
        }
    }

    /**
     * Handle the result from match repository processing.
     * This is a helper method to reduce code duplication.
     */
    private fun handleResult(result: Result<AgentResponse>) {
        viewModelScope.launch {
            result.fold(
                onSuccess = { agentResponse ->
                    // Check if the AI response is "lazy" (says it will look but didn't call tools)
                    val lazyPatterns = listOf(
                        "ik ga kijken",
                        "even opzoeken",
                        "zal ik zoeken",
                        "ik ga even kijken",
                        "een momentje",
                        "ik zoek het op"
                    )
                    
                    val isLazyResponse = lazyPatterns.any { pattern ->
                        agentResponse.text.contains(pattern, ignoreCase = true)
                    }
                    
                    if (isLazyResponse) {
                        Log.d("ChatDebug", "Detected lazy AI response, triggering retry")
                        // Send system message to force tool usage
                        val systemMessage = "SYSTEM: Je hebt geen tool aangeroepen. Roep NU de juiste tool aan."
                        val retryResult = matchRepository.processUserQuery(systemMessage)
                        
                        retryResult.fold(
                            onSuccess = { retryResponse ->
                                val agentChatMessage = ChatMessage(
                                    id = java.util.UUID.randomUUID().toString(),
                                    role = ChatRole.ASSISTANT,
                                    agentResponse = retryResponse,
                                    timestamp = System.currentTimeMillis()
                                )
                                _messages.update { it + agentChatMessage }
                                
                                // Save assistant message to database
                                currentSessionId?.let { sessionId ->
                                    val agentResponseJson = try {
                                        Json.encodeToString(AgentResponse.serializer(), retryResponse)
                                    } catch (e: Exception) {
                                        null
                                    }
                                    chatRepository.saveChatMessage(
                                        sessionId = sessionId,
                                        role = "assistant",
                                        content = retryResponse.text,
                                        predictionJson = null,
                                        agentResponseJson = agentResponseJson,
                                        isHidden = false,
                                        type = "ASSISTANT"
                                    )
                                    
                                    // Generate session title after first exchange
                                    if (isNewSession) {
                                        generateSessionTitle(sessionId, systemMessage, retryResponse.text)
                                        isNewSession = false
                                    }
                                }
                            },
                            onFailure = { error ->
                                // Fallback to original response if retry fails
                                val agentChatMessage = ChatMessage(
                                    id = java.util.UUID.randomUUID().toString(),
                                    role = ChatRole.ASSISTANT,
                                    agentResponse = agentResponse,
                                    timestamp = System.currentTimeMillis()
                                )
                                _messages.update { it + agentChatMessage }
                                
                                currentSessionId?.let { sessionId ->
                                    val agentResponseJson = try {
                                        Json.encodeToString(AgentResponse.serializer(), agentResponse)
                                    } catch (e: Exception) {
                                        null
                                    }
                                    chatRepository.saveChatMessage(
                                        sessionId = sessionId,
                                        role = "assistant",
                                        content = agentResponse.text,
                                        predictionJson = null,
                                        agentResponseJson = agentResponseJson,
                                        isHidden = false,
                                        type = "ASSISTANT"
                                    )
                                    
                                    // Generate session title after first exchange
                                    if (isNewSession) {
                                        generateSessionTitle(sessionId, systemMessage, agentResponse.text)
                                        isNewSession = false
                                    }
                                }
                            }
                        )
                    } else {
                        val agentChatMessage = ChatMessage(
                            id = java.util.UUID.randomUUID().toString(),
                            role = ChatRole.ASSISTANT,
                            agentResponse = agentResponse,
                            timestamp = System.currentTimeMillis()
                        )
                        _messages.update { it + agentChatMessage }
                        
                        // Save assistant message to database
                        currentSessionId?.let { sessionId ->
                            val agentResponseJson = try {
                                Json.encodeToString(AgentResponse.serializer(), agentResponse)
                            } catch (e: Exception) {
                                null
                            }
                            chatRepository.saveChatMessage(
                                sessionId = sessionId,
                                role = "assistant",
                                content = agentResponse.text,
                                predictionJson = null,
                                agentResponseJson = agentResponseJson,
                                isHidden = false,
                                type = "ASSISTANT"
                            )
                            
                            // Generate session title after first exchange
                            if (isNewSession) {
                                // Get the last user message to use for title generation
                                val lastUserMessage = _messages.value.lastOrNull { it.role == ChatRole.USER }?.text ?: ""
                                if (lastUserMessage.isNotBlank()) {
                                    generateSessionTitle(sessionId, lastUserMessage, agentResponse.text)
                                    isNewSession = false
                                }
                            }
                        }
                    }
                    
                    _uiState.update { ChatUiState.Idle }
                    _currentToolAction.update { null }
                },
                onFailure = { error ->
                    _uiState.update { 
                        ChatUiState.Error("Fout bij verwerken van bericht: ${error.message}")
                    }
                    
                    // Add error message as agent response
                    val errorResponse = AgentResponse(
                        text = "Sorry, er ging iets mis. Probeer het opnieuw.",
                        type = AgentResponse.ResponseType.TEXT_ONLY,
                        relatedData = null
                    )
                    val errorChatMessage = ChatMessage(
                        id = java.util.UUID.randomUUID().toString(),
                        role = ChatRole.ASSISTANT,
                        agentResponse = errorResponse,
                        timestamp = System.currentTimeMillis()
                    )
                    _messages.update { it + errorChatMessage }
                    
                    _currentToolAction.update { null }
                }
            )
        }
    }
    
    /**
     * Generate a session title based on the first exchange and update the session.
     * This is called after the first user message + AI response in a new session.
     */
    private suspend fun generateSessionTitle(sessionId: String, userMessage: String, aiResponse: String) {
        try {
            // Get the ToolOrchestrator from matchRepository to generate title
            // Note: This assumes matchRepository has access to ToolOrchestrator
            // In a real implementation, you might need to inject ToolOrchestrator directly
            Log.d("ChatDebug", "Generating session title for session: $sessionId")
            
            // For now, create a simple title based on the conversation
            val title = generateSimpleTitle(userMessage, aiResponse)
            chatRepository.updateChatSessionTitle(sessionId, title)
            Log.d("ChatDebug", "Updated session title to: $title")
        } catch (e: Exception) {
            Log.e("ChatDebug", "Failed to generate session title: ${e.message}")
            // Use a default title if generation fails
            val defaultTitle = generateSimpleTitle(userMessage, aiResponse)
            try {
                chatRepository.updateChatSessionTitle(sessionId, defaultTitle)
            } catch (e2: Exception) {
                Log.e("ChatDebug", "Failed to update session title: ${e2.message}")
            }
        }
    }
    
    /**
     * Generate a simple 3-word title based on conversation content.
     * This is a fallback when AI title generation is not available.
     */
    private fun generateSimpleTitle(userMessage: String, aiResponse: String): String {
        // Extract key words from user message
        val userWords = userMessage.lowercase().split(" ").filter { word ->
            word.length > 3 && !listOf("wat", "wie", "hoe", "waar", "wanneer", "waarom").contains(word)
        }.take(3)
        
        // If we have enough words, use them
        if (userWords.size >= 2) {
            return userWords.joinToString(" ").replaceFirstChar { it.uppercase() }
        }
        
        // Fallback titles based on content
        return when {
            userMessage.contains("ajax", ignoreCase = true) -> "Ajax Analyse Gesprek"
            userMessage.contains("psv", ignoreCase = true) -> "PSV Voetbal Discussie"
            userMessage.contains("feyenoord", ignoreCase = true) -> "Feyenoord Tactiek Chat"
            userMessage.contains("voorspel", ignoreCase = true) -> "Wedstrijd Voorspelling Chat"
            userMessage.contains("stand", ignoreCase = true) -> "Competitie Stand Gesprek"
            userMessage.contains("blessure", ignoreCase = true) -> "Blessure Nieuws Chat"
            else -> "Voetbal Analyse Gesprek"
        }
    }
}

/**
 * UI state for the chat screen.
 */
sealed class ChatUiState {
    data object Idle : ChatUiState()
    data class Loading(val loadingStatus: String? = null) : ChatUiState()
    data object MissingApiKey : ChatUiState()
    data class Error(val message: String) : ChatUiState()
}
