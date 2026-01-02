package com.Lyno.matchmindai.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest

/**
 * A smart image component for loading team logos from API-SPORTS.
 * Automatically caches images locally for optimal performance.
 * 
 * URL format: https://media.api-sports.io/football/teams/{team_id}.png
 * 
 * Features:
 * - Automatic caching (memory + disk)
 * - Crossfade animation
 * - Fallback to team initials when logo is unavailable
 * - Configurable size and shape
 */
@Composable
fun ApiSportsImage(
    teamId: Int?,
    teamName: String,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    showFallback: Boolean = true
) {
    // Build the API-SPORTS logo URL
    val logoUrl = teamId?.let { 
        "https://media.api-sports.io/football/teams/$it.png" 
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (logoUrl != null) {
            // Load actual team logo with Coil caching
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(logoUrl)
                    .crossfade(true) // Smooth fade-in animation
                    .diskCachePolicy(CachePolicy.ENABLED) // Cache to device storage
                    .memoryCachePolicy(CachePolicy.ENABLED) // Cache in memory
                    .build(),
                contentDescription = contentDescription ?: "Logo van $teamName",
                contentScale = ContentScale.Fit, // Preserve aspect ratio
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
            )
        } else if (showFallback) {
            // Fallback to gradient with team initials
            TeamLogoFallback(
                teamName = teamName,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Fallback component that shows a gradient background with team initials.
 * Used when team logo is unavailable or teamId is null.
 */
@Composable
private fun TeamLogoFallback(
    teamName: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text = teamName.take(2).uppercase(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

/**
 * Convenience function for loading team logos with just team name.
 * Useful when team ID is not available but you still want consistent styling.
 */
@Composable
fun TeamLogoPlaceholder(
    teamName: String,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp
) {
    TeamLogoFallback(
        teamName = teamName,
        modifier = modifier.size(size)
    )
}
