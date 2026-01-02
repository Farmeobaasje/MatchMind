package com.Lyno.matchmindai.presentation.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Size
import kotlin.math.cos
import kotlin.math.sin
import com.Lyno.matchmindai.R
import com.Lyno.matchmindai.domain.model.Fact
import kotlinx.coroutines.delay

/**
 * Cyber-Minimalist loading screen with rotating football facts.
 * Features dynamic animations, neon effects, and smooth transitions.
 * OPTIE 1 DESIGN: Tactical football field background with rotating neon football.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LoadingScreen(
    currentFact: Fact,
    progress: Float,
    onSkipClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Infinite rotation animation for football
    val infiniteTransition = rememberInfiniteTransition(label = "footballRotation")
    val rotationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationProgress"
    )
    
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // 1. BACKGROUND IMAGE - Tactical football field
        Image(
            painter = painterResource(R.drawable.background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        
        // 2. CENTRAL CONTENT - Football + Facts (Football 106.5dp lower than center)
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 106.5.dp), // 106.5dp offset naar beneden
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // 2a. ROTATING NEON FOOTBALL
            Image(
                painter = painterResource(R.drawable.ic_neon_football),
                contentDescription = "Rotating neon football",
                modifier = Modifier
                    .size(80.dp)
                    .graphicsLayer {
                        rotationZ = rotationProgress
                    }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 2b. FACT DISPLAY (BEHOUDEN - met tekstschaduw voor leesbaarheid)
            FactDisplay(
                fact = currentFact,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
        }
        
        // 3. PROGRESS BAR (BEHOUDEN - onderaan)
        CyberProgressIndicator(
            progress = progress,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 48.dp)
                .height(8.dp)
        )
        
        // 4. LOADING STATUS (BEHOUDEN)
        Text(
            text = "INITIALISING AI ENGINE...",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF888888),
            letterSpacing = 2.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
        
        // 5. SKIP BUTTON (OPTIONEEL - BEHOUDEN)
        onSkipClick?.let {
            AnimatedVisibility(
                visible = progress < 0.9f,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
            ) {
                CyberButton(
                    text = "SKIP LOADING",
                    onClick = it
                )
            }
        }
    }
}

/**
 * Animated display for football facts with cyber styling.
 * UPDATED: Added text shadow for better readability on bright background.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun FactDisplay(
    fact: Fact,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x151A1A1A))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = fact,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) +
                        slideInVertically(animationSpec = tween(300)) { -40 } with
                        fadeOut(animationSpec = tween(300)) +
                        slideOutVertically(animationSpec = tween(300)) { 40 }
            },
            label = "factAnimation"
        ) { targetFact ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Fact text with improved readability
                Text(
                    text = targetFact.getDisplayText(),
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.5.sp
                    ),
                    lineHeight = 24.sp,
                    modifier = Modifier
                        .graphicsLayer {
                            // Subtle glow effect
                            alpha = 0.95f
                        }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Category indicator
                Text(
                    text = targetFact.category.name.replace("_", " "),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF00FF9D),
                    letterSpacing = 1.sp
                )
            }
        }
        
        // Border glow
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokeWidth = 2.dp.value * density
            drawRoundRect(
                color = Color(0x3300FF9D),
                style = Stroke(width = strokeWidth),
                cornerRadius = CornerRadius(16.dp.value * density)
            )
        }
    }
}

/**
 * Cyber-styled progress indicator with neon glow.
 */
@Composable
private fun CyberProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Background track
        LinearProgressIndicator(
            progress = 1f,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = Color(0xFF2A2A2A),
            trackColor = Color(0xFF1A1A1A)
        )
        
        // Animated progress with glow
        val infiniteTransition = rememberInfiniteTransition(label = "progressGlow")
        val glowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.7f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glowAlpha"
        )
        
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .graphicsLayer {
                    alpha = 0.9f + glowAlpha * 0.1f
                },
            color = Color(0xFF00FF9D),
            trackColor = Color.Transparent
        )
        
        // Pulsing dot at progress end
        if (progress > 0) {
            val dotPulse by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, easing = EaseInOutCubic),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dotPulse"
            )
            
            Canvas(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .graphicsLayer {
                        translationX = progress * size.width - 4.dp.value * density
                        scaleX = dotPulse
                        scaleY = dotPulse
                    }
            ) {
                drawCircle(
                    color = Color(0xFF00FF9D),
                    radius = 6.dp.value * density,
                    center = center
                )
                drawCircle(
                    color = Color(0x6600FF9D),
                    radius = 10.dp.value * density,
                    center = center,
                    style = Stroke(width = 2.dp.value * density)
                )
            }
        }
    }
}

/**
 * Cyber-styled text with glow effect.
 */
@Composable
private fun CyberText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 24.sp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "textGlow")
    val textGlow by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "textGlow"
    )
    
    Box(modifier = modifier) {
        // Glow layer
        Text(
            text = text,
            style = TextStyle(
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = Color(0x3300FF9D),
                letterSpacing = 2.sp
            ),
            modifier = Modifier
                .graphicsLayer {
                    alpha = textGlow * 0.3f
                    scaleX = 1.02f
                    scaleY = 1.02f
                }
        )
        
        // Main text
        Text(
            text = text,
            style = TextStyle(
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 2.sp
            ),
            modifier = Modifier.graphicsLayer {
                alpha = 0.95f
            }
        )
    }
}

/**
 * Cyber-styled button.
 */
@Composable
private fun CyberButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x2200FF9D))
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF00FF9D),
            letterSpacing = 2.sp
        )
        
        // Border
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRoundRect(
                color = Color(0x4400FF9D),
                style = Stroke(width = 1.dp.value * density),
                cornerRadius = CornerRadius(8.dp.value * density)
            )
        }
    }
}

/**
 * Floating particles for cyber effect.
 */
@Composable
private fun FloatingParticles(
    modifier: Modifier = Modifier
) {
    // Temporarily removed to fix compilation
    // Will be re-implemented after main functionality works
}

// Preview function
@Composable
fun LoadingScreenPreview() {
    val sampleFact = com.Lyno.matchmindai.domain.model.Fact(
        id = 1,
        category = com.Lyno.matchmindai.domain.model.FactCategory.TICKET_SALES,
        text = "🎫 Kaartjes gaan in de verkoop voor de klassieker...",
        emoji = "🎫"
    )
    
    var progress by remember { mutableStateOf(0.3f) }
    
    LaunchedEffect(Unit) {
        while (progress < 1f) {
            delay(100)
            progress += 0.01f
        }
    }
    
    LoadingScreen(
        currentFact = sampleFact,
        progress = progress,
        onSkipClick = { /* Preview only */ }
    )
}
