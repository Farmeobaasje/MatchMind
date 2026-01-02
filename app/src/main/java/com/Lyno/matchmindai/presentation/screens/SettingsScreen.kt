package com.Lyno.matchmindai.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Lyno.matchmindai.MatchMindApplication
import com.Lyno.matchmindai.presentation.components.CyberTextField
import com.Lyno.matchmindai.presentation.components.PrimaryActionButton
import com.Lyno.matchmindai.presentation.viewmodel.SettingsViewModel
import com.Lyno.matchmindai.presentation.viewmodel.getViewModelFactory
import com.Lyno.matchmindai.ui.theme.MatchMindAITheme
import com.Lyno.matchmindai.ui.theme.Secondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: SettingsViewModel = viewModel(
        factory = getViewModelFactory(
            LocalContext.current.applicationContext as MatchMindApplication
        )
    )
    val uiState = viewModel.uiState.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Get success message string
    val successMessage = stringResource(id = com.Lyno.matchmindai.R.string.success_saved)

    // Show snackbar for success/error messages
    LaunchedEffect(uiState.keySaved) {
        if (uiState.keySaved) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = successMessage
                )
            }
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = error
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = com.Lyno.matchmindai.R.string.settings_screen_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Terug"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // API Key Input
            CyberTextField(
                value = uiState.apiKey,
                onValueChange = { viewModel.updateApiKey(it) },
                label = stringResource(id = com.Lyno.matchmindai.R.string.api_key_label),
                placeholder = stringResource(id = com.Lyno.matchmindai.R.string.api_key_placeholder),
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                isError = uiState.error != null
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Info Text
            Text(
                text = stringResource(id = com.Lyno.matchmindai.R.string.api_key_info),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Save Button
            PrimaryActionButton(
                text = stringResource(id = com.Lyno.matchmindai.R.string.save_button),
                onClick = { viewModel.saveApiKey() },
                isLoading = uiState.isLoading,
                enabled = uiState.apiKey.isNotBlank()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Live Data Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = com.Lyno.matchmindai.R.string.use_live_data_label),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Switch(
                    checked = uiState.isLiveDataEnabled,
                    onCheckedChange = { viewModel.toggleLiveData(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                )
            }

            // Live Data Description
            Text(
                text = stringResource(id = com.Lyno.matchmindai.R.string.use_live_data_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // DeepSeek Link with clickable text
            val uriHandler = LocalUriHandler.current
            val deepSeekUrl = stringResource(id = com.Lyno.matchmindai.R.string.deepseek_url)
            
            Text(
                text = stringResource(id = com.Lyno.matchmindai.R.string.get_api_key_link),
                style = MaterialTheme.typography.bodyMedium,
                color = Secondary,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        uriHandler.openUri(deepSeekUrl)
                    }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // DeepSeek URL display
            Text(
                text = deepSeekUrl,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    MatchMindAITheme {
        SettingsScreen(
            onNavigateBack = {}
        )
    }
}
