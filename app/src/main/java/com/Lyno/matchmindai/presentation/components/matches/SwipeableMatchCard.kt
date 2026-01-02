package com.Lyno.matchmindai.presentation.components.matches

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.Lyno.matchmindai.R
import com.Lyno.matchmindai.domain.model.MatchFixture
import com.Lyno.matchmindai.ui.theme.PrimaryNeon
import com.Lyno.matchmindai.ui.theme.SurfaceCard
import com.Lyno.matchmindai.ui.theme.TextHigh
import kotlin.math.roundToInt

/**
 * Draggable match card with swipe actions (simplified implementation).
 */
@Composable
fun SwipeableMatchCard(
    match: MatchFixture,
    onFavorite: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    val maxOffset = with(LocalDensity.current) { 120.dp.toPx() }
    var isSwiped by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    offsetX += delta
                },
                onDragStopped = {
                    when {
                        offsetX < -maxOffset -> {
                            onFavorite()
                            offsetX = 0f
                        }
                        offsetX > maxOffset -> {
                            // Show action menu or trigger delete
                            offsetX = 0f
                        }
                        else -> offsetX = 0f
                    }
                    isSwiped = false
                }
            )
    ) {
        // Background actions
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left action background (favorite)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .background(
                        Color.Yellow.copy(
                            alpha = calculateAlpha(-offsetX, maxOffset)
                        )
                    ),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (offsetX < -50f) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_favorite),
                        contentDescription = "Favoriet",
                        tint = Color.White,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(end = 16.dp)
                    )
                }
            }

            // Right action background (share/delete)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .background(
                        Color.Blue.copy(
                            alpha = calculateAlpha(offsetX, maxOffset)
                        )
                    ),
                contentAlignment = Alignment.CenterStart
            ) {
                if (offsetX > 50f) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(start = 16.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_share),
                            contentDescription = "Delen",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete),
                            contentDescription = "Verwijder",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }

        // Main card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.roundToInt(), 0) },
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = SurfaceCard.copy(alpha = 0.8f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                content()

                // Swipe indicator
                SwipeIndicator(
                    offsetX = offsetX,
                    maxOffset = maxOffset,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }

        // Drag hint
        if (offsetX == 0f && !isSwiped) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "←",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Yellow
                    )
                    Text(
                        text = "Swipe voor acties",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextHigh.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "→",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Blue
                    )
                }
            }
        }
    }
}

/**
 * Swipe indicator showing current swipe state.
 */
@Composable
private fun SwipeIndicator(
    offsetX: Float,
    maxOffset: Float,
    modifier: Modifier = Modifier
) {
    val indicatorText = when {
        offsetX < -maxOffset / 2 -> "← Favoriet"
        offsetX > maxOffset / 2 -> "Acties →"
        else -> "Swipe voor acties"
    }

    val indicatorColor = when {
        offsetX < -maxOffset / 2 -> Color.Yellow.copy(alpha = 0.2f)
        offsetX > maxOffset / 2 -> Color.Blue.copy(alpha = 0.2f)
        else -> PrimaryNeon.copy(alpha = 0.1f)
    }

    Box(
        modifier = modifier
            .padding(end = 16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(indicatorColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = indicatorText,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = when {
                offsetX < -maxOffset / 2 -> Color.Yellow
                offsetX > maxOffset / 2 -> Color.Blue
                else -> TextHigh.copy(alpha = 0.6f)
            }
        )
    }
}

/**
 * Swipe action button (simplified).
 */
@Composable
private fun SwipeAction(
    icon: Int,
    label: String,
    color: Color,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedSize by animateDpAsState(
        targetValue = if (isVisible) 48.dp else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "swipeActionSize"
    )

    Box(
        modifier = modifier
            .size(animatedSize)
            .clip(CircleShape)
            .background(color.copy(alpha = if (isVisible) 0.9f else 0f)),
        contentAlignment = Alignment.Center
    ) {
        if (isVisible) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Calculate alpha for background based on swipe offset.
 */
private fun calculateAlpha(offset: Float, maxOffset: Float): Float {
    return (kotlin.math.abs(offset) / maxOffset).coerceIn(0f, 0.5f)
}
