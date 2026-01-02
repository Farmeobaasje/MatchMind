package com.Lyno.matchmindai.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.Lyno.matchmindai.presentation.AppViewModelProvider
import com.Lyno.matchmindai.R
import com.Lyno.matchmindai.data.local.entity.PredictionLogEntity
import com.Lyno.matchmindai.presentation.components.GlassCard
import com.Lyno.matchmindai.presentation.navigation.Screen
import com.Lyno.matchmindai.ui.theme.PrimaryNeon
import com.Lyno.matchmindai.ui.theme.SurfaceCard
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavHostController,
    onNavigateToSettings: () -> Unit
) {
    val viewModel: com.Lyno.matchmindai.presentation.viewmodel.PredictionHistoryViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val predictions by viewModel.allPredictions.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState()
    val reconciliationStatus by viewModel.reconciliationStatus.collectAsState()
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    // Trigger lazy reconciliation when screen is opened
    LaunchedEffect(Unit) {
        viewModel.refreshPredictions()
    }

    // Delete all confirmation dialog
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = {
                Text(text = stringResource(R.string.delete_all_predictions_title))
            },
            text = {
                Text(text = stringResource(R.string.delete_all_predictions_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAllPredictions()
                        showDeleteAllDialog = false
                    }
                ) {
                    Text(text = stringResource(R.string.confirm_delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteAllDialog = false }
                ) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.history_screen_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    // Delete all button (only shown when there are predictions)
                    if (predictions.isNotEmpty()) {
                        IconButton(
                            onClick = { showDeleteAllDialog = true },
                            enabled = !isLoading
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete_all_predictions_button),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    
                    // Refresh button
                    IconButton(
                        onClick = { viewModel.refreshPredictions() },
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.refresh_predictions)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            // Show reconciliation status if present
            reconciliationStatus?.let { status ->
                if (status.isNotBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = SurfaceCard.copy(alpha = 0.8f)
                        )
                    ) {
                        Text(
                            text = status,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            if (predictions.isEmpty()) {
                EmptyHistoryState()
            } else {
                PredictionHistoryList(
                    predictions = predictions,
                    navController = navController
                )
            }
        }
    }
}

@Composable
private fun EmptyHistoryState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = "Empty History",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            
            Text(
                text = stringResource(R.string.history_empty_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = stringResource(R.string.history_empty_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PredictionHistoryList(
    predictions: List<PredictionLogEntity>,
    navController: NavHostController
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(predictions) { prediction ->
            HistoryItem(
                prediction = prediction,
                navController = navController
            )
        }
    }
}

@Composable
private fun HistoryItem(
    prediction: PredictionLogEntity,
    navController: NavHostController
) {
    val viewModel: com.Lyno.matchmindai.presentation.viewmodel.PredictionHistoryViewModel =
        viewModel(factory = AppViewModelProvider.Factory)
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(text = stringResource(R.string.delete_prediction_title))
            },
            text = {
                Text(text = stringResource(R.string.delete_prediction_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePrediction(prediction.fixtureId)
                        showDeleteDialog = false
                    }
                ) {
                    Text(text = stringResource(R.string.confirm_delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }

    Surface(
        onClick = {
            // Navigate to History Detail screen
            navController.navigate(
                Screen.HistoryDetail.createRoute(
                    predictionId = prediction.fixtureId,
                    fixtureId = prediction.fixtureId
                )
            )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = androidx.compose.ui.graphics.Color.Transparent
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header with match info and delete button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = prediction.matchName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = MaterialTheme.typography.titleMedium.fontWeight
                        )

                        Text(
                            text = formatDate(prediction.timestamp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Delete button
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete_icon),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                // Status indicator row (moved to separate row)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status indicator
                    val status = when {
                        prediction.actualScore == null -> "PENDING"
                        prediction.outcomeCorrect == true && prediction.exactScoreCorrect == true -> "CORRECT_EXACT"
                        prediction.outcomeCorrect == true -> "CORRECT_OUTCOME"
                        else -> "INCORRECT"
                    }
                    PredictionStatusIndicator(
                        status = status,
                        isOutcomeCorrect = prediction.outcomeCorrect
                    )
                }

                // Score comparison
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.history_predicted_score),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = prediction.predictedScore ?: "? - ?",
                            style = MaterialTheme.typography.titleMedium,
                            color = PrimaryNeon,
                            fontWeight = MaterialTheme.typography.titleMedium.fontWeight
                        )
                    }

                    // Arrow or equals sign
                    Text(
                        text = "→",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.history_actual_score),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = prediction.actualScore ?: "? - ?",
                            style = MaterialTheme.typography.titleMedium,
                            color = getScoreColor(prediction.outcomeCorrect),
                            fontWeight = MaterialTheme.typography.titleMedium.fontWeight
                        )
                    }
                }

                // Confidence and outcome
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val confidence = if (prediction.homeProb != null && prediction.drawProb != null && prediction.awayProb != null) {
                        maxOf(prediction.homeProb, prediction.drawProb, prediction.awayProb) * 100
                    } else {
                        0.0
                    }
                    Text(
                        text = stringResource(
                            R.string.history_confidence_label,
                            confidence.toInt()
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val outcomeText = when {
                        prediction.actualScore == null -> stringResource(R.string.outcome_pending)
                        prediction.outcomeCorrect == true && prediction.exactScoreCorrect == true -> stringResource(
                            R.string.outcome_correct_exact
                        )

                        prediction.outcomeCorrect == true -> stringResource(R.string.outcome_correct_outcome)
                        else -> stringResource(R.string.outcome_incorrect)
                    }
                    Text(
                        text = outcomeText,
                        style = MaterialTheme.typography.labelMedium,
                        color = getOutcomeColor(prediction.outcomeCorrect),
                        fontWeight = MaterialTheme.typography.labelMedium.fontWeight
                    )
                }
            }
        }
    }
}

// =========================================================================
// HULPFUNCTIES & COMPONENTS (Plak dit ONDERAAN HistoryScreen.kt)
// =========================================================================

/**
 * Formatteert een timestamp (Long) naar een leesbare datum string.
 */
private fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
    return format.format(date)
}

/**
 * Bepaalt de kleur van de score tekst.
 */
@Composable
private fun getScoreColor(isCorrect: Boolean?): Color {
    return when (isCorrect) {
        true -> PrimaryNeon // Groen
        false -> Color.Red
        null -> MaterialTheme.colorScheme.onSurface // Wit/Grijs (Pending)
    }
}

/**
 * Bepaalt de kleur van de status badge/indicator.
 */
@Composable
private fun getOutcomeColor(isOutcomeCorrect: Boolean?): Color {
    return when (isOutcomeCorrect) {
        true -> PrimaryNeon
        false -> Color.Red
        null -> Color.Gray
    }
}

/**
 * Een UI componentje dat de status (Pending/Correct/Wrong) laat zien met een icoon.
 */
@Composable
private fun PredictionStatusIndicator(
    status: String,
    isOutcomeCorrect: Boolean?,
    modifier: Modifier = Modifier
) {
    val icon = when {
        status == "PENDING" -> Icons.Filled.Schedule
        isOutcomeCorrect == true -> Icons.Filled.CheckCircle
        isOutcomeCorrect == false -> Icons.Filled.Cancel
        else -> Icons.Filled.Help
    }

    val tint = when {
        status == "PENDING" -> Color.Gray
        isOutcomeCorrect == true -> PrimaryNeon
        isOutcomeCorrect == false -> Color.Red
        else -> Color.Gray
    }

    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = modifier.size(24.dp)
    )
}
