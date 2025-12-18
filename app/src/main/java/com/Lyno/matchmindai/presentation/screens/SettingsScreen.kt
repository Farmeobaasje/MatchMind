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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.TextButton
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
import com.Lyno.matchmindai.presentation.widgets.UsageWidget
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
        // Cache Confirmation Dialog
        if (uiState.showCacheConfirmation) {
            AlertDialog(
                onDismissRequest = { viewModel.hideCacheConfirmation() },
                title = {
                    Text(
                        text = "Cache Wissen",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                text = {
                    Text(
                        text = "Weet je zeker dat je alle cache wilt wissen? Dit zal alle in-memory cache voor predictions, blessures, odds en wedstrijddetails verwijderen. De volgende API calls zullen verse data ophalen.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.clearAllCache() }
                    ) {
                        Text("Ja, wis cache")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { viewModel.hideCacheConfirmation() }
                    ) {
                        Text("Annuleren")
                    }
                }
            )
        }
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Phase 4: Control Room Header
            item {
                Text(
                    text = "🎮 Control Room",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp)
                )
                Text(
                    text = "Beheer je persoonlijke voetbalervaring",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }

            // Phase 4: Usage Widget (Daily Energy Bar)
            item {
                val userPreferences = com.Lyno.matchmindai.domain.model.UserPreferences(
                    apiCallsRemaining = uiState.apiCallsRemaining,
                    apiCallsLimit = uiState.apiCallsLimit,
                    lastRateLimitUpdate = uiState.lastRateLimitUpdate
                )
                UsageWidget(
                    userPreferences = userPreferences,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Phase 4: Favorite Team Section
            item {
                Text(
                    text = stringResource(id = com.Lyno.matchmindai.R.string.favorite_team_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 8.dp)
                )
                
                if (uiState.favoriteTeamName != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = uiState.favoriteTeamName ?: "",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "ID: ${uiState.favoriteTeamId ?: ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            TextButton(
                                onClick = { viewModel.clearFavoriteTeam() }
                            ) {
                                Text(text = stringResource(id = com.Lyno.matchmindai.R.string.favorite_team_clear))
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(id = com.Lyno.matchmindai.R.string.favorite_team_none),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "TODO: Team selectie implementeren",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
                Text(
                    text = stringResource(id = com.Lyno.matchmindai.R.string.favorite_team_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 16.dp)
                )
            }

            // Phase 4: Data Saver Mode
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(id = com.Lyno.matchmindai.R.string.data_saver_title),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(id = com.Lyno.matchmindai.R.string.data_saver_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Switch(
                        checked = uiState.liveDataSaver,
                        onCheckedChange = { viewModel.toggleDataSaver(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            // Cache Refresh Button
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            enabled = !uiState.isLoading,
                            onClick = { viewModel.showCacheConfirmation() }
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "🔄 Cache Wissen",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Wis alle in-memory cache voor verse data",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Cache wissen",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                Text(
                    text = "Wis de cache om verse data op te halen bij de volgende API calls",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 16.dp)
                )
            }

            // Live Data Switch (Existing)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(id = com.Lyno.matchmindai.R.string.use_live_data_label),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(id = com.Lyno.matchmindai.R.string.use_live_data_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
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
            }

            // API Keys Section Header
            item {
                Text(
                    text = "🔑 API Keys",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp)
                )
                Text(
                    text = "Configureer je API keys voor volledige functionaliteit",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }

            // DeepSeek API Key Input
            item {
                CyberTextField(
                    value = uiState.deepSeekApiKey,
                    onValueChange = { viewModel.updateDeepSeekApiKey(it) },
                    label = stringResource(id = com.Lyno.matchmindai.R.string.deepseek_api_key_label),
                    placeholder = stringResource(id = com.Lyno.matchmindai.R.string.deepseek_api_key_placeholder),
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    isError = uiState.error != null
                )
            }

            // Tavily API Key Input
            item {
                CyberTextField(
                    value = uiState.tavilyApiKey,
                    onValueChange = { viewModel.updateTavilyApiKey(it) },
                    label = stringResource(id = com.Lyno.matchmindai.R.string.tavily_api_key_label),
                    placeholder = stringResource(id = com.Lyno.matchmindai.R.string.tavily_api_key_placeholder),
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    isError = uiState.error != null
                )
            }

            // API-Sports (Direct Subscription) Key Input
            item {
                CyberTextField(
                    value = uiState.apiSportsKey,
                    onValueChange = { viewModel.updateApiSportsKey(it) },
                    label = "API-Sports Key",
                    placeholder = "Voer je API-Sports key in",
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    isError = uiState.error != null
                )
            }

            // Info Text
            item {
                Text(
                    text = stringResource(id = com.Lyno.matchmindai.R.string.api_key_info),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Save Button
            item {
                PrimaryActionButton(
                    text = stringResource(id = com.Lyno.matchmindai.R.string.save_button),
                    onClick = { viewModel.saveApiKeys() },
                    isLoading = uiState.isLoading,
                    enabled = uiState.deepSeekApiKey.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )
            }

            // API Key Links Section
            item {
                val uriHandler = LocalUriHandler.current
                val deepSeekUrl = stringResource(id = com.Lyno.matchmindai.R.string.deepseek_url)
                val tavilyUrl = stringResource(id = com.Lyno.matchmindai.R.string.tavily_url)
                val apiSportsUrl = "https://dashboard.api-football.com"
                
                Text(
                    text = "API Key Links:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                
                // DeepSeek Link
                Text(
                    text = "• DeepSeek API Key",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Secondary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            uriHandler.openUri(deepSeekUrl)
                        }
                )
                
                // Tavily Link
                Text(
                    text = "• Tavily API Key",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Secondary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            uriHandler.openUri(tavilyUrl)
                        }
                )
                
                // API-Sports Link
                Text(
                    text = "• API-Sports Dashboard",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Secondary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            uriHandler.openUri(apiSportsUrl)
                        }
                )
            }
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
