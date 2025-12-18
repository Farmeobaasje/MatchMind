package com.Lyno.matchmindai.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.Lyno.matchmindai.domain.model.MatchPrediction
import com.Lyno.matchmindai.presentation.model.ChatRole

/**
 * Chat UI components for the conversational interface.
 * Follows Cyber-Minimalist design principles.
 */

/**
 * User message bubble (right-aligned, neon green background - Phase 5 Cyberpunk).
 */
@Composable
fun UserMessageBubble(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = com.Lyno.matchmindai.ui.theme.UserMessageBubble,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 4.dp
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White, // White text on neon green for high contrast
                textAlign = TextAlign.End
            )
        }
    }
}

/**
 * Assistant message bubble (left-aligned, dark grey background - Phase 5 Cyberpunk).
 * Contains prediction card content in a compact format.
 * Automatically switches to general chat bubble for non-football responses.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AssistantMessageBubble(
    prediction: MatchPrediction? = null,
    text: String? = null,
    onActionClick: (String) -> Unit = {},
    suggestedActions: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    // Check if this is a general response (not football-related)
    val isGeneralResponse = prediction?.let { 
        it.winner == "General" || it.confidenceScore == 0 
    } ?: true // No prediction means general response
    
    if (isGeneralResponse || prediction == null) {
        // Show standard chat bubble for general responses
        GeneralAssistantMessageBubble(
            text = text ?: "",
            onActionClick = onActionClick,
            suggestedActions = suggestedActions,
            modifier = modifier
        )
    } else {
        // Show football prediction card for football responses
        FootballPredictionBubble(
            prediction = prediction,
            onActionClick = onActionClick,
            modifier = modifier
        )
    }
}

/**
 * Football prediction bubble with rich content (stats, percentages, etc.).
 * Used when the response is football-related.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FootballPredictionBubble(
    prediction: MatchPrediction,
    onActionClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = com.Lyno.matchmindai.ui.theme.AiMessageBubble,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 16.dp
                    )
                )
                .padding(16.dp)
        ) {
            // Compact version of PredictionCard content
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Winner and confidence
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = prediction.winner,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${prediction.confidenceScore}%",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Reasoning
                Text(
                    text = prediction.reasoning,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Key factor
                Text(
                    text = "🎯 ${prediction.keyFactor}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                // Recent matches (if available)
                if (prediction.recentMatches.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Recente Uitslagen:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    prediction.recentMatches.forEach { match ->
                        Text(
                            text = "• $match",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                // Suggested actions (if available)
                if (prediction.suggestedActions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Suggesties:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        prediction.suggestedActions.forEach { action ->
                            OutlinedButton(
                                onClick = { onActionClick(action) },
                                modifier = Modifier.height(32.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = action,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * General assistant message bubble for non-football responses.
 * Simple chat bubble like WhatsApp/Gemini.
 */
@Composable
fun GeneralAssistantMessageBubble(
    text: String,
    onActionClick: (String) -> Unit = {},
    suggestedActions: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = com.Lyno.matchmindai.ui.theme.AiMessageBubble,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 16.dp
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Suggested actions (if available)
                if (suggestedActions.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        suggestedActions.forEach { action ->
                            OutlinedButton(
                                onClick = { onActionClick(action) },
                                modifier = Modifier.height(32.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = action,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Error message bubble (left-aligned, error color).
 */
@Composable
fun ErrorMessageBubble(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 16.dp
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

/**
 * System info bubble (left-aligned, neutral color for non-error messages like "Geen Data").
 * Used for informational messages that are not errors but system responses.
 */
@Composable
fun SystemInfoBubble(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 16.dp
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

/**
 * Typing indicator with 3 pulsing dots.
 * Shows when the assistant is "thinking".
 */
@Composable
fun TypingIndicator(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 16.dp
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Create 3 pulsing dots
                listOf(0, 1, 2).forEach { index ->
                    PulsingDot(delay = index * 200)
                }
            }
        }
    }
}

/**
 * Assistant typing bubble with animated wave effect.
 * Shows when the assistant is "thinking" with a more engaging animation.
 */
@Composable
fun AssistantTypingBubble(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 16.dp
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Animated wave effect
                WaveTypingIndicator()
            }
        }
    }
}

/**
 * Wave typing indicator with 3 bars that animate in sequence.
 */
@Composable
private fun WaveTypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "waveTyping")
    
    val bar1Height by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1200
                8f at 0 with LinearEasing
                16f at 200 with LinearEasing
                8f at 400 with LinearEasing
                8f at 1200 with LinearEasing
            },
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(0)
        ),
        label = "bar1Height"
    )
    
    val bar2Height by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1200
                8f at 200 with LinearEasing
                16f at 400 with LinearEasing
                8f at 600 with LinearEasing
                8f at 1200 with LinearEasing
            },
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(0)
        ),
        label = "bar2Height"
    )
    
    val bar3Height by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1200
                8f at 400 with LinearEasing
                16f at 600 with LinearEasing
                8f at 800 with LinearEasing
                8f at 1200 with LinearEasing
            },
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(0)
        ),
        label = "bar3Height"
    )
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Bar 1
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(bar1Height.dp)
                .background(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = RoundedCornerShape(2.dp)
                )
        )
        
        // Bar 2
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(bar2Height.dp)
                .background(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = RoundedCornerShape(2.dp)
                )
        )
        
        // Bar 3
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(bar3Height.dp)
                .background(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = RoundedCornerShape(2.dp)
                )
        )
    }
}

/**
 * Individual pulsing dot with alpha animation.
 */
@Composable
private fun PulsingDot(
    delay: Int = 0
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsingDot")
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1200
                0.3f at 0 with LinearEasing
                1f at 400 with LinearEasing
                0.3f at 800 with LinearEasing
                0.3f at 1200 with LinearEasing
            },
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(delay)
        ),
        label = "alphaAnimation"
    )
    
    Box(
        modifier = Modifier
            .size(8.dp)
            .background(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                shape = RoundedCornerShape(4.dp)
            )
            .alpha(alpha)
    )
}

/**
 * Thinking indicator that shows above the input bar when AI is processing.
 * Displays contextual messages based on what the AI is doing.
 */
@Composable
fun ThinkingIndicator(
    modifier: Modifier = Modifier
) {
    // Rotate through different thinking messages
    val thinkingMessages = listOf(
        "🔎 MatchMind zoekt naar wedstrijden...",
        "⚽ Data analyseren...",
        "🧠 AI denkt na...",
        "📊 Statistieken verwerken..."
    )
    
    var currentMessageIndex by remember { mutableStateOf<Int>(0) }
    
    // Animate message changes every 2 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            currentMessageIndex = (currentMessageIndex + 1) % thinkingMessages.size
        }
    }
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Animated dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(0, 1, 2).forEach { index ->
                PulsingDot(delay = index * 200)
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Thinking message with crossfade animation
        androidx.compose.animation.Crossfade(
            targetState = currentMessageIndex,
            label = "thinkingMessage"
        ) { index ->
            Text(
                text = thinkingMessages[index],
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * Analysis progress indicator that shows real-time status updates during match analysis.
 * Displays a sleek linear progress indicator with contextual status text (Phase 5 Cyberpunk).
 */
@Composable
fun AnalysisProgressIndicator(
    statusText: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Sleek linear progress indicator with neon glow
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = com.Lyno.matchmindai.ui.theme.UserMessageBubble,
            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
        
        // Status text with subtle animation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pulsing dot animation
            val infiniteTransition = rememberInfiniteTransition(label = "pulsingStatus")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 1500
                        0.5f at 0 with LinearEasing
                        1f at 500 with LinearEasing
                        0.5f at 1000 with LinearEasing
                        0.5f at 1500 with LinearEasing
                    },
                    repeatMode = RepeatMode.Restart
                ),
                label = "statusAlpha"
            )
            
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = com.Lyno.matchmindai.ui.theme.UserMessageBubble,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .alpha(alpha)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
            )
        }
    }
}

/**
 * Streaming reasoning bubble that shows the AI's thinking process in real-time.
 * This is used for DeepSeek R1's reasoning_content stream.
 */
@Composable
fun StreamingReasoningBubble(
    reasoningText: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 12.dp
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header with thinking indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(6.dp)
                            )
                    )
                    Text(
                        text = "MatchMind denkt...",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Reasoning content (monospaced font for code-like thinking)
                Text(
                    text = reasoningText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    lineHeight = androidx.compose.ui.unit.TextUnit(16f, androidx.compose.ui.unit.TextUnitType.Sp)
                )
            }
        }
    }
}

/**
 * Streaming assistant bubble that shows both reasoning and final content.
 * This component handles the streaming updates from DeepSeek R1.
 */
@Composable
fun StreamingAssistantBubble(
    reasoningText: String?,
    finalText: String,
    isStreamingComplete: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Show reasoning bubble if we have reasoning content
        reasoningText?.let { reasoning ->
            StreamingReasoningBubble(
                reasoningText = reasoning,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Show final content bubble
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = 4.dp,
                            bottomEnd = 16.dp
                        )
                    )
                    .padding(16.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Final content with optional streaming indicator
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = finalText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Show streaming indicator if still streaming
                        if (!isStreamingComplete) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                    }
                    
                    // Show "Streaming..." text if still streaming
                    if (!isStreamingComplete) {
                        Text(
                            text = "Streaming...",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Streaming typing indicator specifically for DeepSeek R1 streaming.
 * Shows a more advanced animation to indicate real-time thinking.
 */
@Composable
fun StreamingTypingIndicator(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 16.dp
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(6.dp)
                            )
                    )
                    Text(
                        text = "MatchMind is aan het nadenken...",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Advanced wave animation
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 20.dp)
                ) {
                    WaveTypingIndicator()
                }
            }
        }
    }
}
