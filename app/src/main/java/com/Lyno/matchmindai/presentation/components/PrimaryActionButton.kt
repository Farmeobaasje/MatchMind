package com.Lyno.matchmindai.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.Lyno.matchmindai.ui.theme.MatchMindAITheme
import com.Lyno.matchmindai.ui.theme.Primary

@Composable
fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    val buttonText = if (isLoading) "ANALYSEREN..." else text
    val alpha by animateFloatAsState(
        targetValue = if (isLoading) 0.7f else 1f,
        animationSpec = tween(durationMillis = 500),
        label = "loadingAlpha"
    )
    
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = Primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = Primary.copy(alpha = 0.5f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Text(
            text = buttonText,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.alpha(alpha)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PrimaryActionButtonPreview() {
    MatchMindAITheme {
        PrimaryActionButton(
            text = "VOORSPELLEN",
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PrimaryActionButtonLoadingPreview() {
    MatchMindAITheme {
        PrimaryActionButton(
            text = "VOORSPELLEN",
            onClick = {},
            isLoading = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PrimaryActionButtonDisabledPreview() {
    MatchMindAITheme {
        PrimaryActionButton(
            text = "VOORSPELLEN",
            onClick = {},
            enabled = false
        )
    }
}
