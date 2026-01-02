package com.Lyno.matchmindai.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import com.Lyno.matchmindai.R
import com.Lyno.matchmindai.ui.theme.MatchMindAITheme

/**
 * Floating chat input bar with glassmorphism effect for Phase 5 Cyberpunk design.
 * Features a glass-like background with neon send button.
 * Designed with "High Art Tech" styling for a premium cyberpunk interface.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInputBarSingle(
    query: String,
    onQueryChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = com.Lyno.matchmindai.ui.theme.GlassmorphismBackground,
        shape = MaterialTheme.shapes.extraLarge,
        shadowElevation = 16.dp,
        tonalElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Glassmorphism text field container
            Surface(
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            ) {
                CyberTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    placeholder = stringResource(id = R.string.chat_input_placeholder),
                    singleLine = false,
                    maxLines = 3,
                    enabled = enabled,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = { onSendClick() }
                    )
                )
            }

            // Neon send button with glow effect
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                com.Lyno.matchmindai.ui.theme.UserMessageBubble,
                                com.Lyno.matchmindai.ui.theme.UserMessageBubble.copy(alpha = 0.8f)
                            )
                        )
                    )
            ) {
                IconButton(
                    onClick = onSendClick,
                    modifier = Modifier.fillMaxSize(),
                    enabled = enabled && query.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Stuur bericht",
                        modifier = Modifier.size(24.dp),
                        tint = androidx.compose.ui.graphics.Color.White
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatInputBarSinglePreview() {
    MatchMindAITheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            ChatInputBarSingle(
                query = "Voorspel Ajax Feyenoord",
                onQueryChange = {},
                onSendClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatInputBarSingleEmptyPreview() {
    MatchMindAITheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            ChatInputBarSingle(
                query = "",
                onQueryChange = {},
                onSendClick = {}
            )
        }
    }
}
