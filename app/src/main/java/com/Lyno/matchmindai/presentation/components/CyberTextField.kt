package com.Lyno.matchmindai.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.Lyno.matchmindai.ui.theme.MatchMindAITheme
import com.Lyno.matchmindai.ui.theme.Primary
import com.Lyno.matchmindai.ui.theme.Surface
import com.Lyno.matchmindai.ui.theme.TextDisabled
import com.Lyno.matchmindai.ui.theme.TextHigh
import com.Lyno.matchmindai.ui.theme.TextMedium

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CyberTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        label = label?.let {
            { Text(text = it, style = MaterialTheme.typography.labelMedium) }
        },
        placeholder = placeholder?.let {
            { Text(text = it, style = MaterialTheme.typography.bodyMedium, color = TextMedium) }
        },
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        enabled = enabled,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Primary,
            unfocusedBorderColor = TextDisabled,
            focusedLabelColor = Primary,
            unfocusedLabelColor = TextMedium,
            cursorColor = Primary,
            errorBorderColor = MaterialTheme.colorScheme.error,
            errorLabelColor = MaterialTheme.colorScheme.error,
            errorCursorColor = MaterialTheme.colorScheme.error
        ),
        shape = MaterialTheme.shapes.medium
    )
}

@Preview(showBackground = true)
@Composable
fun CyberTextFieldPreview() {
    MatchMindAITheme {
        CyberTextField(
            value = "",
            onValueChange = {},
            label = "Team naam",
            placeholder = "Bijv. Ajax"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CyberTextFieldWithValuePreview() {
    MatchMindAITheme {
        CyberTextField(
            value = "Feyenoord",
            onValueChange = {},
            label = "Thuisploeg",
            placeholder = "Voer team naam in"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CyberTextFieldPasswordPreview() {
    MatchMindAITheme {
        CyberTextField(
            value = "secret123",
            onValueChange = {},
            label = "API Key",
            placeholder = "Voer je DeepSeek API key in",
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
        )
    }
}
