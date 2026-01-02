package com.Lyno.matchmindai.presentation.components.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.Lyno.matchmindai.domain.model.HybridPrediction
import com.Lyno.matchmindai.domain.model.MatchDetail
import com.Lyno.matchmindai.presentation.components.GlassCard
import com.Lyno.matchmindai.presentation.viewmodel.MatchDetailViewModel
import com.Lyno.matchmindai.ui.theme.PrimaryNeon

/**
 * VerslagTab - Enhanced tab for comprehensive AI-generated match reports.
 * Shows complete narrative analysis with sections, metrics, and betting insights.
 * Auto-generates report when hybrid prediction is available.
 */
@Composable
fun VerslagTab(matchDetail: MatchDetail, viewModel: MatchDetailViewModel) {
    // Load hybrid prediction state
    val hybridPredictionState by viewModel.hybridPrediction.collectAsStateWithLifecycle()
    val isLoadingHybrid by viewModel.isLoadingHybrid.collectAsStateWithLifecycle()
    
    // Load match report state
    val matchReportState by viewModel.matchReport.collectAsStateWithLifecycle()
    val isGeneratingReport by viewModel.isGeneratingReport.collectAsStateWithLifecycle()
    val reportError by viewModel.reportError.collectAsStateWithLifecycle()
    
    // Load quick metrics for data quality
    val quickMetrics by viewModel.quickMetrics.collectAsStateWithLifecycle()
    val isGeneratingQuickMetrics by viewModel.isGeneratingQuickMetrics.collectAsStateWithLifecycle()
    
    // Generate quick metrics when hybrid prediction is available
    LaunchedEffect(hybridPredictionState) {
        if (hybridPredictionState != null && quickMetrics == null && !isGeneratingQuickMetrics) {
            viewModel.generateQuickMetrics(matchDetail)
        }
    }
    
    // Auto-generate report when hybrid prediction is loaded
    LaunchedEffect(hybridPredictionState) {
        if (hybridPredictionState != null && matchReportState == null && !isGeneratingReport) {
            viewModel.generateMatchReport(matchDetail)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📋 MatchMind Verslag",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = PrimaryNeon
            )
            
            // Refresh button if report exists
            if (matchReportState != null) {
                IconButton(
                    onClick = { viewModel.refreshMatchReport() },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Vernieuw verslag",
                        tint = PrimaryNeon
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when {
                // Show match report if available
                matchReportState != null -> {
                    MatchReportCard(
                        matchReport = matchReportState!!,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Show error state for report generation
                reportError != null -> {
                    MatchReportErrorState(
                        errorMessage = reportError!!,
                        onRetry = { viewModel.generateMatchReport(matchDetail) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Show loading state for report generation
                isGeneratingReport -> {
                    MatchReportLoadingState(modifier = Modifier.fillMaxWidth())
                }
                
                // Show loading state for hybrid prediction
                isLoadingHybrid -> {
                    LoadingVerslagState()
                }
                
                // Show empty state when no analysis is available
                else -> {
                    EmptyVerslagState(
                        onAnalyzeClick = { viewModel.loadHybridPrediction(matchDetail) }
                    )
                }
            }
        }
    }
}

/**
 * Check if the hybrid prediction has valid analysis content.
 */
private fun hasValidAnalysis(hybridPrediction: HybridPrediction): Boolean {
    val analysis = hybridPrediction.analysis
    return analysis.reasoning_short.isNotEmpty() || analysis.primary_scenario_desc.isNotEmpty()
}

/**
 * Loading state for Verslag tab.
 */
@Composable
private fun LoadingVerslagState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = PrimaryNeon)
            Text(
                text = "AI analyse wordt geladen...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Empty state for Verslag tab when no analysis is available.
 */
@Composable
private fun EmptyVerslagState(onAnalyzeClick: () -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "📄",
                fontSize = 48.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Nog geen verslag beschikbaar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Activeer de Mastermind Analyse om een gedetailleerd verslag te genereren met volledige AI inzichten.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            
            Button(
                onClick = onAnalyzeClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryNeon,
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start analyse",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start Mastermind Analyse",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
