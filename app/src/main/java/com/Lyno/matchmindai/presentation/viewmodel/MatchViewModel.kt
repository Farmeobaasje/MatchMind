package com.Lyno.matchmindai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Lyno.matchmindai.data.repository.ApiKeyMissingException
import com.Lyno.matchmindai.domain.model.ChatSession
import com.Lyno.matchmindai.domain.model.MatchFixture
import com.Lyno.matchmindai.domain.model.MatchPrediction
import com.Lyno.matchmindai.domain.repository.ChatRepository
import com.Lyno.matchmindai.domain.repository.MatchRepository
import com.Lyno.matchmindai.domain.usecase.GetPredictionUseCase
import com.Lyno.matchmindai.domain.usecase.GetTodaysMatchesUseCase
import com.Lyno.matchmindai.domain.usecase.GetUpcomingMatchesUseCase
import com.Lyno.matchmindai.presentation.model.ChatMessage
import com.Lyno.matchmindai.presentation.model.ChatRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * UI state for the Match prediction screen.
 * Sealed interface for different states in the prediction flow.
 */
sealed interface MatchUiState {
    object Idle : MatchUiState
    object Loading : MatchUiState
    data class Success(val prediction: MatchPrediction) : MatchUiState
    data class Error(val message: String) : MatchUiState
    object MissingApiKey : MatchUiState
}

/**
 * ViewModel for the Match prediction screen.
 * Handles match prediction requests and state management.
 */
class MatchViewModel(
    private val getPredictionUseCase: GetPredictionUseCase,
    private val getTodaysMatchesUseCase: GetTodaysMatchesUseCase,
    private val getUpcomingMatchesUseCase: GetUpcomingMatchesUseCase,
    private val matchRepository: MatchRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MatchUiState>(MatchUiState.Idle)
    val uiState: StateFlow<MatchUiState> = _uiState.asStateFlow()

    // Chat history for the conversation
    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatHistory: StateFlow<List<ChatMessage>> = _chatHistory.asStateFlow()

    // Current session ID
    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId: StateFlow<String?> = _currentSessionId.asStateFlow()

    // Chat sessions list
    private val _chatSessions = MutableStateFlow<List<ChatSession>>(emptyList())
    val chatSessions: StateFlow<List<ChatSession>> = _chatSessions.asStateFlow()

    init {
        // Load or create initial session
        viewModelScope.launch {
            loadOrCreateSession()
            loadChatSessions()
        }
    }

    // Input state for single query (Pure Chat experience)
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    // Today's matches state
    private val _todaysMatches = MutableStateFlow<List<MatchFixture>>(emptyList())
    val todaysMatches: StateFlow<List<MatchFixture>> = _todaysMatches.asStateFlow()

    private val _isLoadingTodaysMatches = MutableStateFlow(false)
    val isLoadingTodaysMatches: StateFlow<Boolean> = _isLoadingTodaysMatches.asStateFlow()

    // Upcoming matches state (for the next 3 days)
    private val _upcomingMatches = MutableStateFlow<List<MatchFixture>>(emptyList())
    val upcomingMatches: StateFlow<List<MatchFixture>> = _upcomingMatches.asStateFlow()

    private val _isLoadingUpcomingMatches = MutableStateFlow(false)
    val isLoadingUpcomingMatches: StateFlow<Boolean> = _isLoadingUpcomingMatches.asStateFlow()

    /**
     * Load or create a chat session.
     */
    private suspend fun loadOrCreateSession() {
        val sessionId = chatRepository.getOrCreateLastSession()
        _currentSessionId.update { sessionId }
        
        // Load chat history for this session
        loadChatHistory(sessionId)
    }

    /**
     * Load chat history for a session.
     */
    private suspend fun loadChatHistory(sessionId: String) {
        chatRepository.getChatMessages(sessionId).collect { messages ->
            val chatMessages = messages.map { entity ->
                ChatMessage(
                    id = entity.id,
                    role = if (entity.role == "user") ChatRole.USER else ChatRole.ASSISTANT,
                    text = entity.content,
                    timestamp = entity.timestamp
                )
            }
            
            // If no messages, show welcome message
            if (chatMessages.isEmpty()) {
                _chatHistory.update {
                    listOf(
                        ChatMessage(
                            id = UUID.randomUUID().toString(),
                            role = ChatRole.ASSISTANT,
                            text = "Ik ben klaar voor de start. Welke wedstrijd moet ik analyseren?"
                        )
                    )
                }
            } else {
                _chatHistory.update { chatMessages }
            }
        }
    }

    /**
     * Load all chat sessions.
     */
    private suspend fun loadChatSessions() {
        chatRepository.getChatSessions().collect { sessions ->
            _chatSessions.update { sessions }
        }
    }

    /**
     * Create a new chat session.
     */
    fun createNewChat() {
        viewModelScope.launch {
            val sessionId = chatRepository.createChatSession("Nieuwe Chat")
            _currentSessionId.update { sessionId }
            _chatHistory.update { emptyList() }
            
            // Add welcome message
            _chatHistory.update {
                listOf(
                    ChatMessage(
                        id = UUID.randomUUID().toString(),
                        role = ChatRole.ASSISTANT,
                        text = "Nieuwe chat gestart! Welke wedstrijd moet ik analyseren?"
                    )
                )
            }
            
            // Reload sessions
            loadChatSessions()
        }
    }

    /**
     * Load a specific chat session.
     */
    fun loadSession(sessionId: String) {
        viewModelScope.launch {
            _currentSessionId.update { sessionId }
            loadChatHistory(sessionId)
        }
    }

    /**
     * Delete a chat session.
     */
    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            chatRepository.deleteChatSession(sessionId)
            loadChatSessions()
            
            // If current session is deleted, create new one
            if (_currentSessionId.value == sessionId) {
                loadOrCreateSession()
            }
        }
    }

    /**
     * Update query input.
     */
    fun updateQuery(text: String) {
        _query.update { text }
    }

    /**
     * Get a prediction based on a natural language query.
     * Accepts free text like "Voorspel Ajax Feyenoord" or "Is Real Madrid in vorm?".
     * Now adds messages to chat history for conversational UI.
     */
    fun predictMatch() {
        val queryText = _query.value.trim()
        
        if (queryText.isBlank()) {
            _uiState.update { 
                MatchUiState.Error("Voer een vraag of wedstrijd in") 
            }
            return
        }
        
        // Add user message to chat history
        val userMessage = ChatMessage(
            role = ChatRole.USER,
            text = queryText
        )
        _chatHistory.update { currentHistory ->
            currentHistory + userMessage
        }
        
        // Clear input field after sending
        _query.update { "" }
        
        viewModelScope.launch {
            _uiState.update { MatchUiState.Loading }
            
            // Use the new single-query version of GetPredictionUseCase with session context
            val result = getPredictionUseCase.getPredictionFromQuery(queryText, _currentSessionId.value)
            
            result.fold(
                onSuccess = { prediction ->
                    // Add assistant message with prediction to chat history
                    val assistantMessage = ChatMessage(
                        role = ChatRole.ASSISTANT,
                        prediction = prediction
                    )
                    _chatHistory.update { currentHistory ->
                        currentHistory + assistantMessage
                    }
                    _uiState.update { MatchUiState.Success(prediction) }
                },
                onFailure = { error ->
                    // Add assistant message with error to chat history
                    val errorMessage = when (error) {
                        is ApiKeyMissingException -> {
                            _uiState.update { MatchUiState.MissingApiKey }
                            "API key ontbreekt. Ga naar instellingen om je API key in te stellen."
                        }
                        is IllegalArgumentException -> {
                            _uiState.update { 
                                MatchUiState.Error(error.message ?: "Invalid input") 
                            }
                            error.message ?: "Ongeldige invoer"
                        }
                        else -> {
                            _uiState.update { 
                                MatchUiState.Error(
                                    error.message ?: "Kon geen voorspelling maken"
                                ) 
                            }
                            error.message ?: "Kon geen voorspelling maken"
                        }
                    }
                    
                    val assistantMessage = ChatMessage(
                        role = ChatRole.ASSISTANT,
                        text = "Fout: $errorMessage"
                    )
                    _chatHistory.update { currentHistory ->
                        currentHistory + assistantMessage
                    }
                }
            )
        }
    }

    /**
     * Load today's football match fixtures.
     */
    fun loadTodaysMatches() {
        viewModelScope.launch {
            _isLoadingTodaysMatches.update { true }
            
            val result = getTodaysMatchesUseCase()
            
            result.fold(
                onSuccess = { fixtures ->
                    _todaysMatches.update { fixtures }
                    _isLoadingTodaysMatches.update { false }
                },
                onFailure = { error ->
                    _isLoadingTodaysMatches.update { false }
                    // Handle specific errors
                    when (error) {
                        is ApiKeyMissingException -> {
                            _uiState.update { MatchUiState.MissingApiKey }
                        }
                        else -> {
                            _uiState.update { 
                                MatchUiState.Error(
                                    error.message ?: "Kon programma niet ophalen"
                                ) 
                            }
                        }
                    }
                }
            )
        }
    }

    /**
     * Load upcoming football match fixtures for the next 3 days.
     */
    fun loadUpcomingMatches() {
        viewModelScope.launch {
            _isLoadingUpcomingMatches.update { true }
            
            val result = getUpcomingMatchesUseCase()
            
            result.fold(
                onSuccess = { fixtures ->
                    _upcomingMatches.update { fixtures }
                    _isLoadingUpcomingMatches.update { false }
                },
                onFailure = { error ->
                    _isLoadingUpcomingMatches.update { false }
                    // Handle specific errors
                    when (error) {
                        is ApiKeyMissingException -> {
                            _uiState.update { MatchUiState.MissingApiKey }
                        }
                        else -> {
                            _uiState.update { 
                                MatchUiState.Error(
                                    error.message ?: "Kon komende wedstrijden niet ophalen"
                                ) 
                            }
                        }
                    }
                }
            )
        }
    }

    /**
     * Handle match selection from the today's matches list.
     * Auto-fills the query field and optionally triggers prediction.
     * @param fixture The selected match fixture
     * @param triggerPrediction Whether to automatically trigger prediction after selection
     */
    fun onMatchSelected(fixture: MatchFixture, triggerPrediction: Boolean = true) {
        // Update query field with the match
        val queryText = "${fixture.homeTeam} vs ${fixture.awayTeam}"
        _query.update { queryText }
        
        // Add user message to chat history
        val userMessage = ChatMessage(
            role = ChatRole.USER,
            text = queryText
        )
        _chatHistory.update { currentHistory ->
            currentHistory + userMessage
        }
        
        // Optionally trigger prediction
        if (triggerPrediction) {
            predictMatch()
        }
    }

    /**
     * Clear today's matches list.
     */
    fun clearTodaysMatches() {
        _todaysMatches.update { emptyList() }
    }

    /**
     * Reset the UI state to Idle.
     */
    fun resetState() {
        _uiState.update { MatchUiState.Idle }
    }

    /**
     * Clear any error messages.
     */
    fun clearError() {
        if (_uiState.value is MatchUiState.Error) {
            _uiState.update { MatchUiState.Idle }
        }
    }

    /**
     * Clear the chat history.
     */
    fun clearChatHistory() {
        _chatHistory.update { emptyList() }
    }
}
