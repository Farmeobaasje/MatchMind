package com.Lyno.matchmindai.presentation.screens

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.filled.Info
import com.Lyno.matchmindai.MatchMindApplication
import com.Lyno.matchmindai.presentation.components.StarterPrompts
import com.Lyno.matchmindai.presentation.components.CyberUserMessageBubble
import com.Lyno.matchmindai.presentation.components.CyberAssistantMessageBubble
import com.Lyno.matchmindai.presentation.components.CyberErrorMessageBubble
import com.Lyno.matchmindai.presentation.components.CyberSystemInfoBubble
import com.Lyno.matchmindai.presentation.components.CyberTypingIndicator
import com.Lyno.matchmindai.presentation.components.CyberProgressIndicator
import com.Lyno.matchmindai.presentation.components.AgentResponseRenderer
import com.Lyno.matchmindai.presentation.components.GlassChatInputBar
import com.Lyno.matchmindai.presentation.components.visual.CacheContextBar
import com.Lyno.matchmindai.presentation.model.ChatMessage
import com.Lyno.matchmindai.presentation.model.ChatRole
import com.Lyno.matchmindai.presentation.viewmodel.ChatViewModel
import com.Lyno.matchmindai.presentation.viewmodel.ChatUiState
import com.Lyno.matchmindai.ui.theme.*
import com.Lyno.matchmindai.domain.model.AgentResponse
import com.Lyno.matchmindai.domain.model.ChatSession
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ChatScreen(
    homeTeam: String? = null,
    awayTeam: String? = null,
    league: String? = null,
    onNavigateToSettings: () -> Unit
) {
    val viewModel: ChatViewModel = viewModel(
        factory = (LocalContext.current.applicationContext as MatchMindApplication).appContainer.chatViewModelFactory
    )
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(lifecycleOwner)
    val messages by viewModel.messages.collectAsStateWithLifecycle(lifecycleOwner)
    val lazyListState = rememberLazyListState()

    // Cache context state
    var showCacheContext by remember { mutableStateOf(false) }
    var cacheContext by remember { mutableStateOf("") }
    var cacheSource by remember { mutableStateOf("") }

    var userInput by remember { mutableStateOf("") }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // Check if there's REAL conversation (user messages sent)
    // Exclude initial welcome message from assistant
    val hasConversationStarted = messages.any { it.role == ChatRole.USER }

    // Auto-scroll to bottom when new messages are added
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            lazyListState.animateScrollToItem(messages.size - 1)
        }
    }

    // Handle MissingApiKey state - navigate to settings
    LaunchedEffect(uiState) {
        if (uiState is ChatUiState.MissingApiKey) {
            onNavigateToSettings()
        }
    }

    // Update cache context when messages change
    LaunchedEffect(messages) {
        val lastAssistantMessage = messages.lastOrNull { it.role == ChatRole.ASSISTANT }
        if (lastAssistantMessage != null) {
            // Check if this message used cached data
            val messageText = lastAssistantMessage.text ?: ""
            val agentResponse = lastAssistantMessage.agentResponse

            if (messageText.contains("cache", ignoreCase = true) ||
                messageText.contains("gecached", ignoreCase = true)) {
                showCacheContext = true
                cacheContext = "AI gebruikt gecachte data voor snellere antwoorden"
                cacheSource = "MatchCacheManager"
            } else if (messageText.contains("API", ignoreCase = true) ||
                      messageText.contains("live", ignoreCase = true)) {
                showCacheContext = true
                cacheContext = "AI gebruikt live API data voor actuele informatie"
                cacheSource = "API-Sports"
            }
        }
    }

    // Handle automatic match analysis when navigated from MatchDetail screen
    // Use a startup flag to prevent multiple triggers
    var hasHandledInitialParams by remember { mutableStateOf(false) }

    LaunchedEffect(homeTeam, awayTeam) {
        Log.d("ChatDebug", "🎬 ChatScreen.LaunchedEffect triggered")
        Log.d("ChatDebug", "   📋 Parameters: home=$homeTeam, away=$awayTeam, league=$league")
        Log.d("ChatDebug", "   🏷️ hasHandledInitialParams=$hasHandledInitialParams")

        // Only trigger analysis if we have BOTH parameters (not null AND not blank) and haven't handled them yet
        if (!homeTeam.isNullOrBlank() && !awayTeam.isNullOrBlank() && !hasHandledInitialParams) {
            Log.d("ChatDebug", "   🚀 Starting automatic analysis for match: $homeTeam vs $awayTeam")
            viewModel.startAutomaticMatchAnalysis(homeTeam, awayTeam, league)
            hasHandledInitialParams = true
            Log.d("ChatDebug", "   ✅ Analysis started, hasHandledInitialParams=true")
        } else if (homeTeam.isNullOrBlank() && awayTeam.isNullOrBlank()) {
            Log.d("ChatDebug", "   ⏸️ No match parameters found, waiting for user input")
            // Reset any loading state when opening chat without parameters
            if (uiState is ChatUiState.Loading) {
                Log.d("ChatDebug", "   🔄 Resetting loading state for empty chat")
                // The ViewModel should handle this, but we log it
            }
            hasHandledInitialParams = false // Reset for next time
            Log.d("ChatDebug", "   🔄 hasHandledInitialParams reset to false")
        } else {
            // One parameter is null/blank, the other isn't - this shouldn't happen
            Log.d("ChatDebug", "   ⚠️ Partial parameters: home=$homeTeam, away=$awayTeam")
            hasHandledInitialParams = false
            Log.d("ChatDebug", "   🔄 hasHandledInitialParams reset to false")
        }
    }

    // Main content without drawer for now
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GradientStart,
                        GradientEnd
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            GradientStart,
                            GradientEnd
                        )
                    )
                )
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                topBar = {
                    if (hasConversationStarted) {
                        TopAppBar(
                            title = {
                                Text(
                                    "MatchMind AI",
                                    color = TextHigh,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent
                            ),
                            navigationIcon = {
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            drawerState.open()
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Menu,
                                        contentDescription = "Open chat history",
                                        tint = PrimaryNeon
                                    )
                                }
                            },
                            actions = {
                                // Cache context toggle button
                                IconButton(
                                    onClick = { showCacheContext = !showCacheContext },
                                    enabled = hasConversationStarted
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Info,
                                        contentDescription = "Cache Context",
                                        tint = if (showCacheContext) {
                                            PrimaryNeon
                                        } else {
                                            TextMedium
                                        }
                                    )
                                }

                                // Prophet Module button
                                IconButton(
                                    onClick = {
                                        // Trigger Prophet analysis on current conversation
                                        val lastUserMessage = messages.lastOrNull { it.role == ChatRole.USER }
                                        lastUserMessage?.text?.let { lastQuery ->
                                            viewModel.sendProphetQuery(lastQuery)
                                        } ?: run {
                                            // If no user message, use a default prediction query
                                            viewModel.sendProphetQuery("Geef een voorspelling voor de laatste wedstrijd die we bespraken")
                                        }
                                    },
                                    enabled = uiState !is ChatUiState.Loading && messages.isNotEmpty()
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = "Prophet Module",
                                        tint = if (uiState !is ChatUiState.Loading && messages.isNotEmpty()) {
                                            PrimaryNeon
                                        } else {
                                            TextDisabled
                                        }
                                    )
                                }
                            }
                        )
                    }
                },
                bottomBar = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Transparent)
                    ) {
                        // Cache context bar
                        if (showCacheContext && hasConversationStarted) {
                            CacheContextBar(
                                context = cacheContext,
                                source = cacheSource,
                                onClose = { showCacheContext = false },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        // Analysis Progress Indicator when AI is processing
                        // FIX: Only show when uiState is strictly Loading
                        if (uiState is ChatUiState.Loading) {
                            val loadingState = uiState as ChatUiState.Loading
                            val statusText = loadingState.loadingStatus ?: "AI analyseert..."

                            CyberProgressIndicator(
                                statusText = statusText,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        // Glassmorphism input bar at the bottom
                        GlassChatInputBar(
                            query = userInput,
                            onQueryChange = { userInput = it },
                            onSendClick = {
                                if (userInput.isNotBlank()) {
                                    viewModel.sendMessage(userInput)
                                    userInput = ""
                                }
                            },
                            enabled = uiState !is ChatUiState.Loading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                        )
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    AnimatedContent(
                        targetState = hasConversationStarted,
                        transitionSpec = {
                            fadeIn() with fadeOut()
                        },
                        label = "chat_transition"
                    ) { targetHasConversation ->
                        if (!targetHasConversation) {
                            // Show welcome content (starter prompts) when no conversation has started
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                // Show welcome message when no match parameters
                                if (homeTeam == null || awayTeam == null) {
                                    StarterPrompts(
                                        fixtures = emptyList(), // We don't have todaysMatches in ChatViewModel
                                        onPromptSelected = { promptText, isGenericPrompt ->
                                            if (isGenericPrompt) {
                                                // For generic "Wedstrijd Voorspellen" chip: only fill input field
                                                userInput = promptText
                                            } else {
                                                // For specific match prompts: update query and trigger prediction
                                                userInput = promptText
                                                viewModel.sendMessage(promptText)
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    // Show match info with typing indicator during automatic analysis
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "Analyseer $homeTeam vs $awayTeam",
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = TextHigh
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        CyberTypingIndicator(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "AI analyseert de wedstrijd...",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                            ),
                                            color = TextMedium
                                        )
        }
    }
}
                        } else {
                            // Chat history section (takes remaining space)
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                state = lazyListState,
                                reverseLayout = false, // Newest messages at the bottom
                                verticalArrangement = Arrangement.Bottom,
                                contentPadding = PaddingValues(bottom = 16.dp, top = 16.dp)
                            ) {
                                // Show typing indicator when loading (at the bottom)
                                // FIX: Only show when uiState is strictly Loading
                                if (uiState is ChatUiState.Loading) {
                                    item {
                                        val loadingState = uiState as ChatUiState.Loading
                                        val statusText = loadingState.loadingStatus ?: "AI analyseert..."

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                        ) {
                                            CyberProgressIndicator(
                                                statusText = statusText,
                                                modifier = Modifier.align(Alignment.CenterStart)
                                            )
                                        }
                                    }
                                }

                                items(messages) { message ->
                                    when (message.role) {
                                        ChatRole.USER -> {
                                            CyberUserMessageBubble(
                                                text = message.text ?: "",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                            )
                                        }
                                        ChatRole.ASSISTANT -> {
                                            if (message.prediction != null) {
                                                CyberAssistantMessageBubble(
                                                    prediction = message.prediction,
                                                    onActionClick = { actionText ->
                                                        // When user clicks a suggestion button:
                                                        // 1. Update the query with the suggestion text
                                                        // 2. Trigger prediction automatically
                                                        userInput = actionText
                                                        viewModel.sendMessage(actionText)
                                                    },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 16.dp, vertical = 4.dp)
                                                )
                                            } else if (message.text != null) {
                                                // Check if this is a "Geen Data" response (not an error)
                                                val isGeenDataResponse = message.text.contains("geen data", ignoreCase = true)
                                                if (isGeenDataResponse) {
                                                    CyberSystemInfoBubble(
                                                        text = message.text,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 16.dp, vertical = 4.dp)
                                                    )
                                                } else {
                                                    CyberErrorMessageBubble(
                                                        text = message.text,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 16.dp, vertical = 4.dp)
                                                    )
                                                }
                                            } else if (message.agentResponse != null) {
                                                // Handle new AgentResponse format with polymorphic rendering
                                                val agentResponse = message.agentResponse
                                                AgentResponseRenderer(
                                                    agentResponse = agentResponse,
                                                    onActionClick = { actionText ->
                                                        userInput = actionText
                                                        viewModel.sendMessage(actionText)
                                                    },
                                                    onFixtureClick = { fixture ->
                                                        // Navigate to chat with fixture details
                                                        val query = "Analyseer de wedstrijd tussen ${fixture.homeTeam} en ${fixture.awayTeam}"
                                                        userInput = query
                                                        viewModel.sendMessage(query)
                                                    },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 16.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
