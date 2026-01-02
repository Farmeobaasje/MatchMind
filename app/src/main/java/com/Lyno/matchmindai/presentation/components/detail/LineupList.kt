package com.Lyno.matchmindai.presentation.components.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.Lyno.matchmindai.domain.model.LineupPlayer
import com.Lyno.matchmindai.domain.model.TeamLineup
import com.Lyno.matchmindai.ui.theme.ActionOrange
import com.Lyno.matchmindai.ui.theme.PrimaryNeon

/**
 * LineupList component for displaying football team lineups.
 * Shows two columns (Home/Away) with coach, formation, starting XI, and substitutes.
 * 
 * @param homeLineup Home team lineup data
 * @param awayLineup Away team lineup data
 * @param modifier Modifier for the component
 */
@Composable
fun LineupList(
    homeLineup: TeamLineup,
    awayLineup: TeamLineup,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Coach and formation info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Home coach and formation
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!homeLineup.coach.isNullOrEmpty()) {
                    Text(
                        text = "Coach: ${homeLineup.coach}",
                        style = MaterialTheme.typography.bodySmall,
                        color = PrimaryNeon,
                        fontWeight = FontWeight.Medium
                    )
                }
                if (!homeLineup.formation.isNullOrEmpty()) {
                    Text(
                        text = "Formation: ${homeLineup.formation}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Away coach and formation
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!awayLineup.coach.isNullOrEmpty()) {
                    Text(
                        text = "Coach: ${awayLineup.coach}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ActionOrange,
                        fontWeight = FontWeight.Medium
                    )
                }
                if (!awayLineup.formation.isNullOrEmpty()) {
                    Text(
                        text = "Formation: ${awayLineup.formation}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Starting XI header
        Text(
            text = "Starting XI",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        // Starting XI players in two columns
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Home team starting XI
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                homeLineup.players.forEach { player ->
                    PlayerItem(
                        player = player,
                        isHome = true,
                        isSubstitute = false
                    )
                }
            }
            
            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            )
            
            // Away team starting XI
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                awayLineup.players.forEach { player ->
                    PlayerItem(
                        player = player,
                        isHome = false,
                        isSubstitute = false
                    )
                }
            }
        }
        
        // Substitutes header (if available)
        if (homeLineup.substitutes.isNotEmpty() || awayLineup.substitutes.isNotEmpty()) {
            Text(
                text = "Substitutes",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Home team substitutes
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    homeLineup.substitutes.forEach { player ->
                        PlayerItem(
                            player = player,
                            isHome = true,
                            isSubstitute = true
                        )
                    }
                }
                
                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                )
                
                // Away team substitutes
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    awayLineup.substitutes.forEach { player ->
                        PlayerItem(
                            player = player,
                            isHome = false,
                            isSubstitute = true
                        )
                    }
                }
            }
        }
    }
}

/**
 * Player item for lineup display.
 * Shows player name, position, and captain/substitute indicators.
 */
@Composable
private fun PlayerItem(
    player: LineupPlayer,
    isHome: Boolean,
    isSubstitute: Boolean
) {
    val backgroundColor = if (isHome) PrimaryNeon.copy(alpha = 0.1f) else ActionOrange.copy(alpha = 0.1f)
    val textColor = if (isHome) PrimaryNeon else ActionOrange
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = textColor
        ),
        shape = MaterialTheme.shapes.small,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Player info
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Player name with captain indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = player.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Captain badge
                    if (player.isCaptain) {
                        Badge(
                            containerColor = textColor,
                            contentColor = Color.White
                        ) {
                            Text("C", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                
                // Position and number
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    player.position?.let { position ->
                        Text(
                            text = position,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    player.number?.let { number ->
                        Text(
                            text = "#$number",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Substitute indicator
            if (isSubstitute) {
                Icon(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = "Substitute",
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Starting Player",
                    tint = textColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Football field visualization for lineups.
 * Shows players on a simplified football field based on grid coordinates.
 */
@Composable
fun LineupFieldVisualization(
    homeLineup: TeamLineup,
    awayLineup: TeamLineup,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1B5E20), // Dark green for football field
            contentColor = Color.White
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            // Field markings
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 2.dp,
                        color = Color.White.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.medium
                    )
            )
            
            // Center line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.3f))
            )
            
            // Center circle
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.3f),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )
            
            // Home team players (top half)
            homeLineup.players.forEach { player ->
                player.grid?.let { grid ->
                    val (row, col) = parseGridCoordinates(grid)
                    PlayerDot(
                        player = player,
                        isHome = true,
                        row = row,
                        col = col,
                        totalRows = 5,
                        totalCols = 5
                    )
                }
            }
            
            // Away team players (bottom half)
            awayLineup.players.forEach { player ->
                player.grid?.let { grid ->
                    val (row, col) = parseGridCoordinates(grid)
                    // Invert row for away team (bottom half)
                    PlayerDot(
                        player = player,
                        isHome = false,
                        row = 6 - row, // Invert for bottom half
                        col = col,
                        totalRows = 5,
                        totalCols = 5
                    )
                }
            }
        }
    }
}

/**
 * Parses grid coordinates from string format "row:col".
 */
private fun parseGridCoordinates(grid: String): Pair<Int, Int> {
    return try {
        val parts = grid.split(":")
        val row = parts.getOrNull(0)?.toIntOrNull() ?: 1
        val col = parts.getOrNull(1)?.toIntOrNull() ?: 1
        row to col
    } catch (e: Exception) {
        1 to 1
    }
}

/**
 * Player dot on football field visualization.
 */
@Composable
private fun PlayerDot(
    player: LineupPlayer,
    isHome: Boolean,
    row: Int,
    col: Int,
    totalRows: Int,
    totalCols: Int
) {
    val color = if (isHome) PrimaryNeon else ActionOrange
    val position = player.position ?: ""
    
    // Calculate position based on grid
    val rowFraction = row.toFloat() / (totalRows + 1)
    val colFraction = col.toFloat() / (totalCols + 1)
    
    Box(
        modifier = Modifier
            .offset(
                x = (colFraction * 100).dp - 20.dp,
                y = (rowFraction * 100).dp - 20.dp
            )
            .size(40.dp)
            .clip(androidx.compose.foundation.shape.CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = position.take(2),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = player.name.split(" ").last().take(3),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.8f
            )
        }
    }
}

/**
 * Empty state for when lineups are not available.
 */
@Composable
fun LineupEmptyState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "No lineups",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Opstellingen nog niet bekend",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Text(
            text = "De opstellingen worden meestal 1 uur voor de wedstrijd vrijgegeven.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

/**
 * Preview function for development.
 */
@Composable
fun LineupListPreview() {
    val homeLineup = TeamLineup(
        teamName = "Ajax",
        formation = "4-3-3",
        coach = "John van 't Schip",
        players = listOf(
            LineupPlayer("Ramaj", 40, "G", "1:3", false),
            LineupPlayer("Rensch", 2, "D", "2:2", false),
            LineupPlayer("Hato", 4, "D", "2:3", true), // Captain
            LineupPlayer("Sutalo", 37, "D", "2:4", false),
            LineupPlayer("Martha", 25, "D", "2:5", false),
            LineupPlayer("Taylor", 8, "M", "3:2", false),
            LineupPlayer("Hlynsson", 38, "M", "3:3", false),
            LineupPlayer("Mannsverk", 16, "M", "3:4", false),
            LineupPlayer("Bergwijn", 7, "F", "4:1", false),
            LineupPlayer("Brobbey", 9, "F", "4:3", false),
            LineupPlayer("Forbs", 11, "F", "4:5", false)
        ),
        substitutes = listOf(
            LineupPlayer("Gorter", 1, "G", null, false, true),
            LineupPlayer("Gaaei", 3, "D", null, false, true),
            LineupPlayer("Akpom", 10, "F", null, false, true)
        )
    )
    
    val awayLineup = TeamLineup(
        teamName = "PSV",
        formation = "4-2-3-1",
        coach = "Peter Bosz",
        players = listOf(
            LineupPlayer("Benítez", 1, "G", "1:3", false),
            LineupPlayer("Teze", 3, "D", "2:2", false),
            LineupPlayer("Boscagli", 18, "D", "2:3", false),
            LineupPlayer("Schouten", 23, "D", "2:4", true), // Captain
            LineupPlayer("Dest", 8, "D", "2:5", false),
            LineupPlayer("Veerman", 23, "M", "3:2", false),
            LineupPlayer("Tillman", 10, "M", "3:4", false),
            LineupPlayer("Bakayoko", 11, "F", "4:1", false),
            LineupPlayer("Saibari", 34, "F", "4:3", false),
            LineupPlayer("Lozano", 17, "F", "4:5", false),
            LineupPlayer("de Jong", 9, "F", "5:3", false)
        ),
        substitutes = listOf(
            LineupPlayer("Waterman", 16, "G", null, false, true),
            LineupPlayer("Ramalho", 5, "D", null, false, true),
            LineupPlayer("Pepi", 14, "F", null, false, true)
        )
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        LineupList(
            homeLineup = homeLineup,
            awayLineup = awayLineup
        )
        
        LineupFieldVisualization(
            homeLineup = homeLineup,
            awayLineup = awayLineup
        )
    }
}
