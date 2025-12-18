package com.Lyno.matchmindai.presentation.components

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * API-Sports WebView component for displaying football widgets.
 * Supports Games, Game, Standings, H2H, Team, and Player widgets.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ApiSportsWebView(
    widgetType: FootballWidgetType,
    parameters: Map<String, String> = emptyMap(),
    modifier: Modifier = Modifier,
    onLoadingChanged: (Boolean) -> Unit = {},
    onError: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(widgetType, parameters) {
        isLoading = true
        errorMessage = null
        onLoadingChanged(true)
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (errorMessage != null) {
            // Show error state
            ApiSportsErrorState(
                errorMessage = errorMessage!!,
                onRetry = { errorMessage = null }
            )
        } else {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            allowFileAccess = true
                            allowContentAccess = true
                            setSupportZoom(false)
                            builtInZoomControls = false
                            displayZoomControls = false
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                                onLoadingChanged(false)
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                errorCode: Int,
                                description: String?,
                                failingUrl: String?
                            ) {
                                super.onReceivedError(view, errorCode, description, failingUrl)
                                errorMessage = "WebView error: $description"
                                onError(description ?: "Unknown error")
                                isLoading = false
                                onLoadingChanged(false)
                            }
                        }

                        webChromeClient = WebChromeClient()

                        // Load the API-Sports widget
                        val htmlContent = buildApiSportsWidgetHtml(widgetType, parameters)
                        loadDataWithBaseURL(
                            "https://widgets.api-sports.io",
                            htmlContent,
                            "text/html",
                            "UTF-8",
                            null
                        )
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { webView ->
                    // Update WebView when parameters change
                    val htmlContent = buildApiSportsWidgetHtml(widgetType, parameters)
                    webView.loadDataWithBaseURL(
                        "https://widgets.api-sports.io",
                        htmlContent,
                        "text/html",
                        "UTF-8",
                        null
                    )
                }
            )

            if (isLoading) {
                CircularProgressIndicator()
            }
        }
    }
}

/**
 * Build HTML content for API-Sports widget.
 */
private fun buildApiSportsWidgetHtml(
    widgetType: FootballWidgetType,
    parameters: Map<String, String>
): String {
    val widgetParams = mutableMapOf<String, String>().apply {
        // Add widget type
        put("data-type", widgetType.typeName)
        
        // Add widget-specific required parameters
        when (widgetType) {
            is FootballWidgetType.Game -> {
                put("data-game-id", widgetType.fixtureId.toString())
            }
            is FootballWidgetType.Standings -> {
                put("data-league", widgetType.leagueId.toString())
                put("data-season", widgetType.season.toString())
            }
            is FootballWidgetType.H2H -> {
                put("data-h2h", "${widgetType.teamId1}-${widgetType.teamId2}")
            }
            is FootballWidgetType.Team -> {
                put("data-team-id", widgetType.teamId.toString())
            }
            is FootballWidgetType.Player -> {
                put("data-player-id", widgetType.playerId.toString())
            }
            else -> {
                // Games widget doesn't require specific parameters
            }
        }
        
        // Add additional parameters
        parameters.forEach { (key, value) ->
            put(key, value)
        }
    }

    // Build HTML with widget parameters
    val paramsString = widgetParams.entries.joinToString(" ") { (key, value) ->
        "$key=\"$value\""
    }

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>API-Sports Widget</title>
            <style>
                body {
                    margin: 0;
                    padding: 0;
                    background: transparent;
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                }
                .widget-container {
                    width: 100%;
                    height: 100vh;
                    overflow: hidden;
                }
                .loading {
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    height: 100vh;
                    color: #666;
                    font-size: 14px;
                }
            </style>
        </head>
        <body>
            <div class="widget-container">
                <api-sports-widget $paramsString></api-sports-widget>
            </div>
            
            <!-- API-Sports Widget Script -->
            <script type="module" src="https://widgets.api-sports.io/widgets.js"></script>
            
            <script>
                // Handle widget loading
                document.addEventListener('DOMContentLoaded', function() {
                    console.log('API-Sports widget loaded');
                });
                
                // Handle widget errors
                window.addEventListener('error', function(e) {
                    console.error('Widget error:', e.message);
                });
            </script>
        </body>
        </html>
    """.trimIndent()
}

/**
 * Error state for API-Sports widget.
 */
@Composable
private fun ApiSportsErrorState(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Widget kon niet laden",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Button(onClick = onRetry) {
                    Text("Opnieuw proberen")
                }
            }
        }
    }
}
