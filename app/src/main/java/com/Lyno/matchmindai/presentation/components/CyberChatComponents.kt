package com.Lyno.matchmindai.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.Lyno.matchmindai.domain.model.AgentResponse
import com.Lyno.matchmindai.domain.model.MatchFixture
import com.Lyno.matchmindai.domain.model.MatchPrediction
import com.Lyno.matchmindai.ui.theme.*
import com.Lyno.matchmindai.presentation.components.AgentResponseRenderer

/**
 * Cyberpunk chat components for Phase 5 "High Art Tech" design.
 * Features glassmorphism, neon colors, and cyberpunk aesthetics.
 */

/**
 * Cyberpunk user message bubble (right-aligned, neon green with glow effect).
 */
@Composable
fun CyberUserMessageBubble(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = 20.dp,
                        bottomEnd = 4.dp
                    )
                )
                .drawWithCache {
                    onDrawWithContent {
                        drawContent()
                        // Neon glow effect
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    UserMessageBubble.copy(alpha = 0.8f),
                                    UserMessageBubble.copy(alpha = 0.3f),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = size.maxDimension
                            ),
                            blendMode = BlendMode.Overlay
                        )
                    }
                }
                .background(
                    color = UserMessageBubble,
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = 20.dp,
                        bottomEnd = 4.dp
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = TextHigh,
                textAlign = TextAlign.End
            )
        }
    }
}

/**
 * Cyberpunk assistant message bubble for predictions (left-aligned, dark grey with neon border).
 */
@Composable
fun CyberAssistantMessageBubble(
    prediction: MatchPrediction,
    onActionClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 20.dp
                    )
                )
                .drawWithCache {
                    onDrawWithContent {
                        drawContent()
                        // Neon border effect
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    PrimaryNeon.copy(alpha = 0.3f),
                                    SecondaryPurple.copy(alpha = 0.3f),
                                    PrimaryNeon.copy(alpha = 0.3f)
                                )
                            ),
                            blendMode = BlendMode.Overlay
                        )
                    }
                }
                .background(
                    color = AiMessageBubble,
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 20.dp
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header with match info
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⚽ Voorspelling",
                        style = MaterialTheme.typography.titleMedium,
                        color = PrimaryNeon
                    )
                    Text(
                        text = "${prediction.confidenceScore}%",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = ConfidenceHigh
                    )
                }

                // Winner
                Text(
                    text = "🏆 ${prediction.winner}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextHigh
                )

                // Reasoning
                Text(
                    text = prediction.reasoning,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMedium
                )

                // Key factor with neon accent
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryNeon.copy(alpha = 0.1f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "🎯 ${prediction.keyFactor}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrimaryNeon
                    )
                }

                // Suggested actions
                if (prediction.suggestedActions.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "💡 Suggesties:",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextMedium
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            prediction.suggestedActions.forEach { action ->
                                OutlinedButton(
                                    onClick = { onActionClick(action) },
                                    modifier = Modifier.height(36.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = PrimaryNeon
                                    ),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = PrimaryNeon
                                    ),
                                    shape = RoundedCornerShape(18.dp)
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
}

/**
 * Cyberpunk error message bubble (left-aligned, red neon effect).
 */
@Composable
fun CyberErrorMessageBubble(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 20.dp
                    )
                )
                .background(
                    color = ConfidenceLow.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 20.dp
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚠️",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ConfidenceLow
                )
            }
        }
    }
}

/**
 * Cyberpunk system info bubble (left-aligned, neutral with info icon).
 */
@Composable
fun CyberSystemInfoBubble(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 20.dp
                    )
                )
                .background(
                    color = TextDisabled.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 20.dp
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ℹ️",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMedium
                )
            }
        }
    }
}

/**
 * Cyberpunk typing indicator with monospace font and neon dots.
 */
@Composable
fun CyberTypingIndicator(
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
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 20.dp
                    )
                )
                .background(
                    color = AiMessageBubble,
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 20.dp
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "MatchMind denkt...",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    color = PrimaryNeon
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(0, 1, 2).forEach { index ->
                        CyberPulsingDot(delay = index * 200)
                    }
                }
            }
        }
    }
}

/**
 * Cyberpunk progress indicator for AI processing.
 */
@Composable
fun CyberProgressIndicator(
    statusText: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Status text with monospace font
        Text(
            text = statusText,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace
            ),
            color = TextMedium,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Neon progress bar
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .clip(RoundedCornerShape(1.dp)),
            color = PrimaryNeon,
            trackColor = TextDisabled.copy(alpha = 0.3f)
        )
    }
}

/**
 * Cyberpunk agent response renderer for AgentResponse.
 * Delegates to the main AgentResponseRenderer for proper polymorphic rendering.
 */
@Composable
fun CyberAgentResponseRenderer(
    agentResponse: AgentResponse,
    onActionClick: (String) -> Unit = {},
    onFixtureClick: (MatchFixture) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 20.dp
                    )
                )
                .background(
                    color = AiMessageBubble,
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 20.dp
                    )
                )
                .padding(16.dp)
        ) {
            // Delegate to the main AgentResponseRenderer for proper polymorphic rendering
            AgentResponseRenderer(
                agentResponse = agentResponse,
                onActionClick = onActionClick,
                onFixtureClick = onFixtureClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Individual cyberpunk pulsing dot with neon glow.
 */
@Composable
private fun CyberPulsingDot(
    delay: Int = 0
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cyberPulsingDot")
    
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
        label = "cyberAlphaAnimation"
    )
    
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(
                color = PrimaryNeon,
                shape = RoundedCornerShape(4.dp)
            )
            .alpha(alpha)
            .drawWithCache {
                onDrawWithContent {
                    drawContent()
                    // Neon glow
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PrimaryNeon.copy(alpha = alpha * 0.8f),
                                PrimaryNeon.copy(alpha = alpha * 0.3f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = size.maxDimension * 2
                        ),
                        blendMode = BlendMode.Overlay
                    )
                }
            }
    )
}

/**
 * Glassmorphism chat input bar with cyberpunk aesthetic.
 * This is the completed version of the function that was truncated.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassChatInputBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSendClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .drawWithCache {
                onDrawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                GlassmorphismBackground.copy(alpha = 0.8f),
                                GlassmorphismBackground.copy(alpha = 0.4f)
                            )
                        ),
                        blendMode = BlendMode.Overlay
                    )
                }
            }
            .graphicsLayer {
                // Glassmorphism effect
                this.alpha = 0.95f
                this.shape = RoundedCornerShape(28.dp)
                this.clip = true
            }
            .background(
                color = GlassmorphismBackground.copy(alpha = 0.3f),
                shape = RoundedCornerShape(28.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Text field
            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                placeholder = {
                    Text(
                        "Stel een voetbalvraag...",
                        color = TextMedium,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedTextColor = TextHigh,
                    unfocusedTextColor = TextHigh,
                    disabledTextColor = TextDisabled
                ),
                enabled = enabled,
                singleLine = true,
                shape = RoundedCornerShape(24.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Send button with neon glow
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PrimaryNeon.copy(alpha = 0.8f),
                                PrimaryNeon.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )
                    .clickable(
                        enabled = enabled && query.isNotBlank(),
                        onClick = onSendClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "Send message",
                    tint = if (enabled && query.isNotBlank()) TextHigh else TextDisabled,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
