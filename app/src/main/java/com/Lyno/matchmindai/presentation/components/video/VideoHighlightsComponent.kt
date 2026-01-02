package com.Lyno.matchmindai.presentation.components.video

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.Lyno.matchmindai.ui.theme.PrimaryNeon
import com.Lyno.matchmindai.ui.theme.SurfaceCard
import com.Lyno.matchmindai.ui.theme.TextHigh
import com.Lyno.matchmindai.ui.theme.TextMedium

/**
 * Video highlights component for DashX dashboard.
 * Shows video thumbnails and allows playing highlights in fullscreen.
 */
@Composable
fun VideoHighlightsComponent(
    fixtureId: Int,
    highlights: List<VideoHighlight> = emptyList(),
    modifier: Modifier = Modifier
) {
    var selectedHighlight by remember { mutableStateOf<VideoHighlight?>(null) }
    var isFullscreen by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "VIDEO HIGHLIGHTS",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = TextHigh
            )

            if (highlights.isNotEmpty()) {
                Text(
                    text = "${highlights.size} videos",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMedium
                )
            }
        }

        // Video thumbnails
        if (highlights.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(highlights) { highlight ->
                    VideoThumbnail(
                        highlight = highlight,
                        onClick = {
                            selectedHighlight = highlight
                            isFullscreen = true
                        },
                        modifier = Modifier.width(160.dp)
                    )
                }
            }
        } else {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceCard.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "No videos",
                        tint = TextMedium,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "Geen video's beschikbaar",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMedium
                    )
                }
            }
        }
    }

    // Fullscreen video player
    if (isFullscreen && selectedHighlight != null) {
        FullscreenVideoPlayer(
            highlight = selectedHighlight!!,
            onClose = {
                isFullscreen = false
                selectedHighlight = null
            }
        )
    }
}

/**
 * Video thumbnail for highlights list.
 */
@Composable
private fun VideoThumbnail(
    highlight: VideoHighlight,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = SurfaceCard
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Thumbnail image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
            ) {
                AsyncImage(
                    model = highlight.thumbnailUrl,
                    contentDescription = highlight.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop
                )

                // Play button overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(PrimaryNeon.copy(alpha = 0.9f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Duration badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = highlight.duration,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }

            // Video info
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = highlight.title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextHigh,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = highlight.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = highlight.time,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMedium
                    )

                    Text(
                        text = highlight.type.displayName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = highlight.type.color
                    )
                }
            }
        }
    }
}

/**
 * Fullscreen video player dialog.
 */
@Composable
private fun FullscreenVideoPlayer(
    highlight: VideoHighlight,
    onClose: () -> Unit
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        var isPlaying by remember { mutableStateOf(false) }
        var isMuted by remember { mutableStateOf(false) }
        var showControls by remember { mutableStateOf(true) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Video placeholder (in real app, use ExoPlayer or VideoPlayer)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { showControls = !showControls }
            ) {
                AsyncImage(
                    model = highlight.thumbnailUrl,
                    contentDescription = "Video",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Play button overlay
                if (!isPlaying) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(PrimaryNeon.copy(alpha = 0.9f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }
            }

            // Controls (animated)
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 2 },
                exit = fadeOut(tween(300)) + slideOutVertically(tween(300)) { it / 2 }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    // Top bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = highlight.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = highlight.time,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Bottom controls
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Play/pause
                        IconButton(
                            onClick = { isPlaying = !isPlaying },
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(PrimaryNeon.copy(alpha = 0.9f))
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Filled.Replay else Icons.Filled.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Volume control
                        IconButton(
                            onClick = { isMuted = !isMuted }
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                                contentDescription = if (isMuted) "Unmute" else "Mute",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Video info overlay
            if (showControls) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = highlight.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                        color = PrimaryNeon.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = highlight.type.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = PrimaryNeon,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Video highlight data class.
 */
data class VideoHighlight(
    val id: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val videoUrl: String,
    val duration: String, // e.g., "2:45"
    val time: String, // e.g., "45'", "HT", "FT"
    val type: HighlightType,
    val fixtureId: Int
)

/**
 * Type of video highlight.
 */
enum class HighlightType(
    val displayName: String,
    val color: Color
) {
    GOAL("Goal", Color.Green),
    SAVE("Redding", Color.Blue),
    CHANCE("Kans", Color.Yellow),
    RED_CARD("Rode kaart", Color.Red),
    YELLOW_CARD("Gele kaart", Color.Yellow),
    SUBSTITUTION("Wissel", Color.Cyan),
    HIGHLIGHT("Highlight", PrimaryNeon);

    companion object {
        /**
         * Get highlight type from event type.
         */
        fun fromEventType(eventType: String): HighlightType {
            return when (eventType.uppercase()) {
                "GOAL" -> GOAL
                "SAVE" -> SAVE
                "RED_CARD" -> RED_CARD
                "YELLOW_CARD" -> YELLOW_CARD
                "SUBSTITUTION" -> SUBSTITUTION
                else -> HIGHLIGHT
            }
        }
    }
}

/**
 * Preview data for video highlights.
 */
object VideoHighlightsPreview {
    fun getPreviewHighlights(fixtureId: Int): List<VideoHighlight> {
        return listOf(
            VideoHighlight(
                id = "1",
                title = "Prachtige vrije trap",
                description = "Messi scoort een prachtige vrije trap in de bovenhoek",
                thumbnailUrl = "https://example.com/thumbnail1.jpg",
                videoUrl = "https://example.com/video1.mp4",
                duration = "0:45",
                time = "23'",
                type = HighlightType.GOAL,
                fixtureId = fixtureId
            ),
            VideoHighlight(
                id = "2",
                title = "Spectaculaire redding",
                description = "Keeper maakt een ongelooflijke redding op de lijn",
                thumbnailUrl = "https://example.com/thumbnail2.jpg",
                videoUrl = "https://example.com/video2.mp4",
                duration = "0:32",
                time = "67'",
                type = HighlightType.SAVE,
                fixtureId = fixtureId
            ),
            VideoHighlight(
                id = "3",
                title = "Rode kaart voor tackle",
                description = "Speler krijgt rood voor een gevaarlijke tackle",
                thumbnailUrl = "https://example.com/thumbnail3.jpg",
                videoUrl = "https://example.com/video3.mp4",
                duration = "0:28",
                time = "78'",
                type = HighlightType.RED_CARD,
                fixtureId = fixtureId
            )
        )
    }
}
