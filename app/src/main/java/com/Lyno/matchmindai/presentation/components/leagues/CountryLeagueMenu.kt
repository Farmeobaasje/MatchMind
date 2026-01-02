package com.Lyno.matchmindai.presentation.components.leagues

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Lyno.matchmindai.domain.model.CountryGroup
import com.Lyno.matchmindai.domain.model.LeagueGroup
import com.Lyno.matchmindai.domain.model.MatchFixture
import com.Lyno.matchmindai.presentation.components.GlassCard
import com.Lyno.matchmindai.ui.theme.GradientEnd
import com.Lyno.matchmindai.ui.theme.GradientStart
import com.Lyno.matchmindai.ui.theme.PrimaryNeon
import com.Lyno.matchmindai.ui.theme.TextHigh
import com.Lyno.matchmindai.ui.theme.TextMedium

/**
 * Main component for the country-based league selection menu.
 * Shows a hierarchical view of leagues grouped by country with filtering options.
 *
 * @param countryGroups List of country groups to display
 * @param selectedCountries Set of selected country codes
 * @param selectedLeagues Set of selected league IDs
 * @param onCountryToggle Callback when a country is toggled (expanded/collapsed)
 * @param onCountrySelect Callback when a country is selected/deselected
 * @param onLeagueToggle Callback when a league is toggled (selected/deselected)
 * @param onMatchClick Callback when a match is clicked
 * @param onSearchClick Callback when search is clicked
 * @param onFilterClick Callback when filter is clicked
 * @param modifier Modifier for the component
 */
@Composable
fun CountryLeagueMenu(
    countryGroups: List<CountryGroup>,
    selectedCountries: Set<String> = emptySet(),
    selectedLeagues: Set<Int> = emptySet(),
    onCountryToggle: (String) -> Unit = {},
    onCountrySelect: (String) -> Unit = {},
    onLeagueToggle: (Int) -> Unit = {},
    onMatchClick: (MatchFixture) -> Unit = {},
    onSearchClick: () -> Unit = {},
    onFilterClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showMatches by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GradientStart, GradientEnd)
                )
            )
            .padding(16.dp)
    ) {
        // Header with title and actions
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(
                    text = "🌍 COMPETITIES",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = TextHigh
                )
                Text(
                    text = "Selecteer per land",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMedium
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Search button
                GlassCard(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onSearchClick() }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Zoek competities",
                            tint = PrimaryNeon,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                // Filter button
                GlassCard(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onFilterClick() }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FilterList,
                            contentDescription = "Filter landen",
                            tint = PrimaryNeon,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Stats bar
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            StatItem(
                label = "Landen",
                value = countryGroups.size.toString(),
                color = PrimaryNeon
            )
            
            StatItem(
                label = "Competities",
                value = countryGroups.sumOf { it.leagueCount }.toString(),
                color = TextHigh
            )
            
            StatItem(
                label = "Wedstrijden",
                value = countryGroups.sumOf { it.totalMatchCount }.toString(),
                color = TextMedium
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Toggle for showing matches
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = if (showMatches) "Toon wedstrijden" else "Toon alleen competities",
                style = MaterialTheme.typography.labelMedium,
                color = TextMedium,
                modifier = Modifier.weight(1f)
            )
            
            // Toggle switch
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (showMatches) PrimaryNeon else TextMedium.copy(alpha = 0.3f))
                    .clickable { showMatches = !showMatches }
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background)
                        .align(if (showMatches) Alignment.CenterEnd else Alignment.CenterStart)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Country groups list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(
                items = countryGroups,
                key = { it.countryCode }
            ) { countryGroup ->
                if (showMatches) {
                    // Show full country section with matches
                    CountrySectionWithMatches(
                        countryGroup = countryGroup.copy(
                            isExpanded = selectedCountries.contains(countryGroup.countryCode),
                            isSelected = selectedCountries.contains(countryGroup.countryCode)
                        ),
                        onMatchClick = onMatchClick,
                        onToggleCountryExpanded = { onCountryToggle(countryGroup.countryCode) },
                        onToggleLeagueExpanded = onLeagueToggle,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // Show compact country section for selection only
                    CountrySection(
                        countryGroup = countryGroup.copy(
                            isExpanded = selectedCountries.contains(countryGroup.countryCode),
                            isSelected = selectedCountries.contains(countryGroup.countryCode)
                        ),
                        onLeagueClick = { league -> onLeagueToggle(league.leagueId) },
                        onMatchClick = onMatchClick,
                        onToggleCountryExpanded = { onCountryToggle(countryGroup.countryCode) },
                        onToggleCountrySelected = { onCountrySelect(countryGroup.countryCode) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        // Selection summary
        val selectedCount = selectedLeagues.size
        if (selectedCount > 0) {
            Spacer(modifier = Modifier.height(16.dp))
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "$selectedCount geselecteerd",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = TextHigh
                        )
                        Text(
                            text = "Competities in ${selectedCountries.size} landen",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMedium
                        )
                    }
                    
                    Text(
                        text = "Toepassen",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNeon
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { /* Apply selection */ }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Stat item for the stats bar.
 */
@Composable
fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMedium
        )
    }
}

/**
 * Preview function for CountryLeagueMenu
 */
@Composable
fun CountryLeagueMenuPreview() {
    // Preview data
    val previewCountryGroups = listOf(
        CountryGroup(
            countryCode = "NL",
            countryName = "Netherlands",
            flagEmoji = "🇳🇱",
            leagues = listOf(
                LeagueGroup(
                    leagueId = 88,
                    leagueName = "Eredivisie",
                    country = "Netherlands",
                    logoUrl = "https://media.api-sports.io/football/leagues/88.png",
                    matches = emptyList(),
                    isExpanded = true
                ),
                LeagueGroup(
                    leagueId = 89,
                    leagueName = "Eerste Divisie",
                    country = "Netherlands",
                    logoUrl = "https://media.api-sports.io/football/leagues/89.png",
                    matches = emptyList(),
                    isExpanded = false
                )
            ),
            isExpanded = true,
            isSelected = true
        ),
        CountryGroup(
            countryCode = "GB",
            countryName = "England",
            flagEmoji = "🇬🇧",
            leagues = listOf(
                LeagueGroup(
                    leagueId = 39,
                    leagueName = "Premier League",
                    country = "England",
                    logoUrl = "https://media.api-sports.io/football/leagues/39.png",
                    matches = emptyList(),
                    isExpanded = true
                )
            ),
            isExpanded = false,
            isSelected = false
        )
    )
    
    CountryLeagueMenu(
        countryGroups = previewCountryGroups,
        selectedCountries = setOf("NL"),
        selectedLeagues = setOf(88, 39),
        onCountryToggle = {},
        onCountrySelect = {},
        onLeagueToggle = {},
        onMatchClick = {},
        onSearchClick = {},
        onFilterClick = {},
        modifier = Modifier.fillMaxWidth()
    )
}
