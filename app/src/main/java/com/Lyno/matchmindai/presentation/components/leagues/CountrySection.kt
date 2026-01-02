package com.Lyno.matchmindai.presentation.components.leagues

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.Lyno.matchmindai.domain.model.CountryGroup
import com.Lyno.matchmindai.domain.model.LeagueGroup
import com.Lyno.matchmindai.domain.model.MatchFixture
import com.Lyno.matchmindai.presentation.components.ExpandableLeagueSection
import com.Lyno.matchmindai.presentation.components.GlassCard
import com.Lyno.matchmindai.ui.theme.ConfidenceHigh
import com.Lyno.matchmindai.ui.theme.PrimaryNeon
import com.Lyno.matchmindai.ui.theme.TextHigh
import com.Lyno.matchmindai.ui.theme.TextMedium

/**
 * A collapsible section for displaying leagues grouped by country.
 * Shows country flag, name, and expandable list of leagues.
 *
 * @param countryGroup The country group containing leagues
 * @param onLeagueClick Callback when a league is clicked (for selection)
 * @param onMatchClick Callback when a match is clicked
 * @param onToggleCountryExpanded Callback when country header is clicked to toggle expansion
 * @param onToggleCountrySelected Callback when country selection checkbox is clicked
 * @param modifier Modifier for the component
 */
@Composable
fun CountrySection(
    countryGroup: CountryGroup,
    onLeagueClick: (LeagueGroup) -> Unit = {},
    onMatchClick: (MatchFixture) -> Unit = {},
    onToggleCountryExpanded: () -> Unit = {},
    onToggleCountrySelected: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
    ) {
        // Country header with flag, name, and selection checkbox
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleCountryExpanded() }
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Country flag emoji
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryNeon.copy(alpha = 0.1f))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = countryGroup.flagEmoji,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))

                    // Country name and league count
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = countryGroup.countryName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextHigh
                        )
                        Text(
                            text = "${countryGroup.leagueCount} competities • ${countryGroup.totalMatchCount} wedstrijden",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMedium
                        )
                    }

                    // Selection checkbox
                    Icon(
                        imageVector = if (countryGroup.isSelected) {
                            Icons.Filled.CheckCircle
                        } else {
                            Icons.Outlined.CheckCircle
                        },
                        contentDescription = if (countryGroup.isSelected) {
                            "Deselecteer alle competities in ${countryGroup.countryName}"
                        } else {
                            "Selecteer alle competities in ${countryGroup.countryName}"
                        },
                        tint = if (countryGroup.isSelected) ConfidenceHigh else TextMedium,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(onClick = onToggleCountrySelected)
                            .padding(4.dp)
                    )
                }

                // Chevron icon for expand/collapse
                Icon(
                    imageVector = if (countryGroup.isExpanded) {
                        Icons.Filled.ArrowDropUp
                    } else {
                        Icons.Filled.ArrowDropDown
                    },
                    contentDescription = if (countryGroup.isExpanded) {
                        "Klap ${countryGroup.countryName} in"
                    } else {
                        "Klap ${countryGroup.countryName} uit"
                    },
                    tint = PrimaryNeon
                )
            }
        }

        // Animated visibility for leagues
        AnimatedVisibility(
            visible = countryGroup.isExpanded,
            enter = expandVertically(
                animationSpec = tween(durationMillis = 300),
                expandFrom = Alignment.Top
            ),
            exit = shrinkVertically(
                animationSpec = tween(durationMillis = 300),
                shrinkTowards = Alignment.Top
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                countryGroup.leagues.forEach { league ->
                    // League item with selection indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLeagueClick(league) }
                            .padding(vertical = 8.dp, horizontal = 12.dp)
                    ) {
                        // Selection indicator
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(
                                    if (league.isExpanded) PrimaryNeon else Color.Transparent,
                                    shape = CircleShape
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (league.isExpanded) PrimaryNeon else TextMedium,
                                    shape = CircleShape
                                )
                        )

                        // League name and match count
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = league.leagueName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (league.isExpanded) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (league.isExpanded) TextHigh else TextMedium
                            )
                            Text(
                                text = "${league.matchCount} wedstrijden",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMedium
                            )
                        }

                        // Expandable league section for matches (if league is expanded)
                        if (league.isExpanded) {
                            // In a real implementation, this would show the matches
                            // For now, we just show a visual indicator
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryNeon)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Alternative version that shows full ExpandableLeagueSection for each league
 * when the country is expanded and league is selected.
 */
@Composable
fun CountrySectionWithMatches(
    countryGroup: CountryGroup,
    onMatchClick: (MatchFixture) -> Unit,
    onToggleCountryExpanded: () -> Unit,
    onToggleLeagueExpanded: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
    ) {
        // Country header (same as above but without selection checkbox)
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleCountryExpanded() }
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Country flag emoji
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryNeon.copy(alpha = 0.1f))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = countryGroup.flagEmoji,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))

                    // Country name and league count
                    Column {
                        Text(
                            text = countryGroup.countryName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextHigh
                        )
                        Text(
                            text = "${countryGroup.leagueCount} competities",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMedium
                        )
                    }
                }

                // Chevron icon
                Icon(
                    imageVector = if (countryGroup.isExpanded) {
                        Icons.Filled.ArrowDropUp
                    } else {
                        Icons.Filled.ArrowDropDown
                    },
                    contentDescription = if (countryGroup.isExpanded) {
                        "Klap ${countryGroup.countryName} in"
                    } else {
                        "Klap ${countryGroup.countryName} uit"
                    },
                    tint = PrimaryNeon
                )
            }
        }

        // Animated visibility for leagues with matches
        AnimatedVisibility(
            visible = countryGroup.isExpanded,
            enter = expandVertically(
                animationSpec = tween(durationMillis = 300),
                expandFrom = Alignment.Top
            ),
            exit = shrinkVertically(
                animationSpec = tween(durationMillis = 300),
                shrinkTowards = Alignment.Top
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                countryGroup.leagues.forEach { league ->
                    ExpandableLeagueSection(
                        leagueGroup = league,
                        onMatchClick = onMatchClick,
                        onToggleExpanded = { onToggleLeagueExpanded(league.leagueId) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
