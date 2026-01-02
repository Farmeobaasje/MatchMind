package com.Lyno.matchmindai.presentation.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Lyno.matchmindai.domain.model.MatchFixture
import com.Lyno.matchmindai.ui.theme.PrimaryNeon
import com.Lyno.matchmindai.ui.theme.TextHigh
import com.Lyno.matchmindai.ui.theme.TextMedium

/**
 * Cyber-styled live match card with glassmorphism effect.
 * Features team logos, neon score, and live indicator.
 */
@Composable
fun LiveMatchCard(
    match: MatchFixture,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .width(280.dp)
            .height(160.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // LIVE label in top-left
            LiveLabel(
                modifier = Modifier
                    .align(Alignment.TopStart)
            )

            // Main content: teams and score
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home team
                TeamSection(
                    teamName = match.homeTeam,
                    score = match.homeScore ?: 0,
                    isHome = true,
                    teamId = match.homeTeamId,
                    modifier = Modifier.weight(1f)
                )

                // Score and minute
                ScoreSection(
                    homeScore = match.homeScore ?: 0,
                    awayScore = match.awayScore ?: 0,
                    minute = match.elapsed,
                    modifier = Modifier.weight(1f)
                )

                // Away team
                TeamSection(
                    teamName = match.awayTeam,
                    score = match.awayScore ?: 0,
                    isHome = false,
                    teamId = match.awayTeamId,
                    modifier = Modifier.weight(1f)
                )
            }

            // League name at bottom
            Text(
                text = match.league,
                style = MaterialTheme.typography.labelSmall,
                color = TextMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
            )
        }
    }
}

/**
 * Red "LIVE" label with white text.
 */
@Composable
private fun LiveLabel(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(start = 8.dp, top = 8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color.Red)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "LIVE",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}

/**
 * Team section with logo and name.
 */
@Composable
private fun TeamSection(
    teamName: String,
    score: Int,
    isHome: Boolean,
    modifier: Modifier = Modifier,
    teamId: Int? = null
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Team logo using ApiSportsImage
        ApiSportsImage(
            teamId = teamId,
            teamName = teamName,
            size = 48.dp,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Team name
        Text(
            text = teamName,
            style = MaterialTheme.typography.bodyMedium,
            color = TextHigh,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Score section with neon green score and minute indicator.
 */
@Composable
private fun ScoreSection(
    homeScore: Int,
    awayScore: Int,
    minute: Int?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Score
        Text(
            text = "$homeScore - $awayScore",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            ),
            color = PrimaryNeon
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Minute with live indicator
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pulsing red dot
            PulsingLiveDot()

            Text(
                text = "${minute ?: 0}'",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMedium
            )
        }
    }
}

/**
 * Pulsing red dot animation for live matches.
 * Reused from DashboardScreen.
 */
@Composable
private fun PulsingLiveDot() {
    // Note: This is a simplified version. For full animation,
    // we should extract the PulsingLiveDot from DashboardScreen
    // into a shared component. For now, using a static dot.
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(Color.Red)
    )
}
