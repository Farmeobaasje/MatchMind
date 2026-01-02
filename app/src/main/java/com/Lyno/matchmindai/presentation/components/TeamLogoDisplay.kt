package com.Lyno.matchmindai.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Displays a team with its logo and name.
 * Used in match predictions and team selection.
 */
@Composable
fun TeamLogoDisplay(
    teamName: String,
    teamId: Int? = null,
    isWinner: Boolean = false,
    modifier: Modifier = Modifier,
    logoSize: Int = 40
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // Team logo
        ApiSportsImage(
            teamId = teamId,
            teamName = teamName,
            size = logoSize.dp,
            modifier = Modifier.size(logoSize.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Team name
        Column {
            Text(
                text = teamName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal,
                color = if (isWinner) MaterialTheme.colorScheme.primary 
                       else MaterialTheme.colorScheme.onSurface
            )
            
            if (isWinner) {
                Text(
                    text = "Winnaar",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

/**
 * Displays two teams facing each other with their logos.
 * Used for match headers and prediction results.
 */
@Composable
fun MatchHeader(
    homeTeamName: String,
    awayTeamName: String,
    homeTeamId: Int? = null,
    awayTeamId: Int? = null,
    homeIsWinner: Boolean = false,
    awayIsWinner: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Home team
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ApiSportsImage(
                teamId = homeTeamId,
                teamName = homeTeamName,
                size = 64.dp,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.padding(4.dp))
            Text(
                text = homeTeamName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (homeIsWinner) FontWeight.Bold else FontWeight.Normal,
                color = if (homeIsWinner) MaterialTheme.colorScheme.primary 
                       else MaterialTheme.colorScheme.onSurface
            )
        }
        
        // VS separator
        Text(
            text = "VS",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        
        // Away team
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ApiSportsImage(
                teamId = awayTeamId,
                teamName = awayTeamName,
                size = 64.dp,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.padding(4.dp))
            Text(
                text = awayTeamName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (awayIsWinner) FontWeight.Bold else FontWeight.Normal,
                color = if (awayIsWinner) MaterialTheme.colorScheme.primary 
                       else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
