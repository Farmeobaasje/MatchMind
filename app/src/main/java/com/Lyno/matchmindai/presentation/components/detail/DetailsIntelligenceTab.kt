package com.Lyno.matchmindai.presentation.components.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.Lyno.matchmindai.domain.model.MatchDetail
import com.Lyno.matchmindai.domain.model.Injury
import com.Lyno.matchmindai.domain.model.StandingRow
import com.Lyno.matchmindai.presentation.components.GlassCard
import com.Lyno.matchmindai.presentation.viewmodel.MatchDetailViewModel
import com.Lyno.matchmindai.ui.theme.PrimaryNeon

/**
 * DetailsIntelligenceTab - Unified tab combining Overview, Stats, Lineups, Standings, and Injuries.
 * This replaces 5 separate tabs with one powerful intelligence dashboard.
 */
@Composable
fun DetailsIntelligenceTab(matchDetail: MatchDetail, viewModel: MatchDetailViewModel) {
    // Load all necessary data
    val injuries by viewModel.injuries.collectAsState()
    val isLoadingInjuries by viewModel.isLoadingInjuries.collectAsState()
    
    // Load data when this tab is displayed
    LaunchedEffect(Unit) {
        viewModel.loadInjuries(matchDetail.fixtureId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        
        // Match Context Header
        Text(
            text = "⚡ Match Details Intelligence",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = PrimaryNeon,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Match Info Card
        MatchInfoCard(matchDetail = matchDetail)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Team Health & Lineups
        Text(
            text = "🏥 Team Health & Opstellingen",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        TeamHealthOverview(
            matchDetail = matchDetail,
            injuries = injuries,
            isLoading = isLoadingInjuries
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LineupStatusCard(matchDetail = matchDetail)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Competition Context
        Text(
            text = "🏆 Competition Context",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        StandingsOverview(
            matchDetail = matchDetail,
            standings = matchDetail.standings ?: emptyList()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Key Match Statistics
        Text(
            text = "📊 Key Match Statistics",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Simple statistics display
        if (matchDetail.stats.isNotEmpty()) {
            matchDetail.stats.take(3).forEach { stat ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stat.type,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${stat.homeValue}${stat.unit}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryNeon
                            )
                            Text(
                                text = "${stat.awayValue}${stat.unit}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryNeon
                            )
                        }
                    }
                }
            }
        } else {
            GlassCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Geen statistieken beschikbaar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Wedstrijd nog niet begonnen",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Match Timeline & Events
        Text(
            text = "⏱️ Match Timeline & Events",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        if (matchDetail.events.isNotEmpty()) {
            GlassCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    matchDetail.events.take(5).forEach { event ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = event.displayTime,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = event.type.displayName,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = event.player ?: "Onbekend",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            GlassCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Geen events beschikbaar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Wedstrijd nog niet begonnen",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Match Info Card - Shows basic match information.
 */
@Composable
private fun MatchInfoCard(matchDetail: MatchDetail) {
    GlassCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Teams and Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = matchDetail.homeTeam,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Text(
                        text = "Thuis",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Score
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    matchDetail.score?.let { score ->
                        Text(
                            text = "${score.home}-${score.away}",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNeon
                        )
                    } ?: run {
                        Text(
                            text = "VS",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Text(
                        text = matchDetail.status?.displayName ?: "Onbekend",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = matchDetail.awayTeam,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Text(
                        text = "Uit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Divider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant
            )
            
            // Match Details
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                matchDetail.info.let { info ->
                    if (!info.stadium.isNullOrEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Stadium",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = info.stadium ?: "",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    
                    if (!info.time.isNullOrEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Time",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = info.time ?: "",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                
                if (!matchDetail.league.isNullOrEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "League",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = matchDetail.league,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

/**
 * Team Health Overview - Shows injuries and lineup status.
 */
@Composable
private fun TeamHealthOverview(
    matchDetail: MatchDetail,
    injuries: List<Injury>,
    isLoading: Boolean
) {
    GlassCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Team Health Status",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
            } else {
                // Home Team Health
                val homeInjuries = injuries.filter { it.team == matchDetail.homeTeam }
                TeamHealthRow(
                    teamName = matchDetail.homeTeam,
                    injuryCount = homeInjuries.size,
                    keyPlayersOut = homeInjuries.take(3).map { it.playerName }
                )
                
                Divider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant
                )
                
                // Away Team Health
                val awayInjuries = injuries.filter { it.team == matchDetail.awayTeam }
                TeamHealthRow(
                    teamName = matchDetail.awayTeam,
                    injuryCount = awayInjuries.size,
                    keyPlayersOut = awayInjuries.take(3).map { it.playerName }
                )
                
                if (injuries.isEmpty()) {
                    Text(
                        text = "✅ Geen blessures gemeld",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Team Health Row - Shows health status for a single team.
 */
@Composable
private fun TeamHealthRow(
    teamName: String,
    injuryCount: Int,
    keyPlayersOut: List<String>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = teamName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            if (keyPlayersOut.isNotEmpty()) {
                Text(
                    text = "Out: ${keyPlayersOut.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
        
        // Injury Indicator
        Badge(
            containerColor = when {
                injuryCount == 0 -> Color.Green.copy(alpha = 0.2f)
                injuryCount <= 2 -> Color.Yellow.copy(alpha = 0.2f)
                else -> Color.Red.copy(alpha = 0.2f)
            },
            contentColor = when {
                injuryCount == 0 -> Color.Green
                injuryCount <= 2 -> Color(0xFFFFA726)
                else -> Color.Red
            }
        ) {
            Text(
                text = if (injuryCount == 0) "Fit" else "$injuryCount blessures",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Lineup Status Card - Shows lineup availability.
 */
@Composable
private fun LineupStatusCard(matchDetail: MatchDetail) {
    GlassCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Opstelling Status",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            if (matchDetail.hasLineups) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "✅ Opstellingen beschikbaar",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Formatie: ${matchDetail.lineups.home.formation ?: "Onbekend"} - ${matchDetail.lineups.away.formation ?: "Onbekend"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Badge(
                        containerColor = PrimaryNeon.copy(alpha = 0.2f),
                        contentColor = PrimaryNeon
                    ) {
                        Text(
                            text = "Live",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "⏳ Opstellingen nog niet bekend",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Wordt 1 uur voor aanvang gepubliceerd",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    
                    Badge(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        Text(
                            text = "Wachten",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Standings Overview - Shows league position of both teams.
 */
@Composable
private fun StandingsOverview(
    matchDetail: MatchDetail,
    standings: List<StandingRow>
) {
    GlassCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Competitiestand",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            if (standings.isEmpty()) {
                Text(
                    text = "Geen standen beschikbaar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Find home team standing
                val homeStanding = standings.find { it.team == matchDetail.homeTeam }
                val awayStanding = standings.find { it.team == matchDetail.awayTeam }
                
                if (homeStanding != null && awayStanding != null) {
                    // Show both teams' standings
                    StandingRowItem(
                        teamName = matchDetail.homeTeam,
                        standing = homeStanding,
                        isHomeTeam = true
                    )
                    
                    Divider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )
                    
                    StandingRowItem(
                        teamName = matchDetail.awayTeam,
                        standing = awayStanding,
                        isHomeTeam = false
                    )
                } else {
                    // Show top 3 teams in standings
                    Text(
                        text = "Top 3 van de competitie",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    standings.take(3).forEachIndexed { index, standing ->
                        StandingRowItem(
                            teamName = standing.team,
                            standing = standing,
                            isHomeTeam = false
                        )
                        if (index < 2) {
                            Divider(
                                modifier = Modifier.fillMaxWidth(),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Standing Row Item - Shows a single team's standing.
 */
@Composable
private fun StandingRowItem(
    teamName: String,
    standing: StandingRow,
    isHomeTeam: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Rank badge
                Badge(
                    containerColor = if (isHomeTeam) PrimaryNeon.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isHomeTeam) PrimaryNeon else MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Text(
                        text = "#${standing.rank}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    text = teamName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isHomeTeam) FontWeight.Bold else FontWeight.Medium
                )
            }
            
            // Form indicator
            standing.form?.let { form ->
                if (form.isNotBlank()) {
                    Text(
                        text = "Form: $form",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "${standing.points} pts",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryNeon
            )
            
            Text(
                text = "${standing.wins}W ${standing.draws}D ${standing.losses}L",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
