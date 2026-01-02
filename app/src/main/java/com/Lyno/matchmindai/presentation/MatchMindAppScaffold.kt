package com.Lyno.matchmindai.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.Lyno.matchmindai.presentation.navigation.Screen
import com.Lyno.matchmindai.presentation.screens.ChatScreen
import com.Lyno.matchmindai.presentation.screens.DashboardScreen
import com.Lyno.matchmindai.presentation.screens.FavoritesScreen
import com.Lyno.matchmindai.presentation.screens.HistoryDetailScreen
import com.Lyno.matchmindai.presentation.screens.HistoryScreen
import com.Lyno.matchmindai.presentation.screens.LiveMatchesScreen
import com.Lyno.matchmindai.presentation.screens.MatchesScreen
import com.Lyno.matchmindai.presentation.screens.NewsWebViewScreen
import com.Lyno.matchmindai.presentation.screens.SettingsScreen
import com.Lyno.matchmindai.presentation.screens.TeamSelectionScreen
import com.Lyno.matchmindai.presentation.screens.LeagueTeamBrowserScreen
import com.Lyno.matchmindai.presentation.screens.match.MatchDetailScreen
import kotlin.text.substringBefore

/**
 * Root scaffold for MatchMind AI with bottom navigation.
 * Provides a modern AI assistant experience with hub-based navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchMindAppScaffold(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    // Current screen title based on destination
    val currentScreenTitle = remember { derivedStateOf {
        when (navController.currentDestination?.route?.substringBefore("?")) {
            Screen.Dashboard.route -> "Dashboard"
            Screen.Matches.route -> "Wedstrijden"
            Screen.Favorites.route -> "Favorieten"
            Screen.History.route -> "Historie"
            Screen.Settings.route -> "Instellingen"
            else -> "MatchMind AI"
        }
    }}
    
    // Bottom navigation items
    val items = listOf(
        BottomNavItem(
            route = Screen.Dashboard.route,
            label = "Dashboard",
            icon = Icons.Filled.DateRange
        ),
        BottomNavItem(
            route = Screen.Favorites.route,
            label = "Favorieten",
            icon = Icons.Filled.Favorite
        ),
        BottomNavItem(
            route = Screen.History.route,
            label = "Historie",
            icon = Icons.Filled.List
        ),
        BottomNavItem(
            route = Screen.Settings.route,
            label = "Instellingen",
            icon = Icons.Filled.Settings
        )
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                items.forEach { item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) },
                        selected = false, // Always show as unselected on detail screens
                        onClick = {
                            // Navigate to the selected screen
                            if (item.route == Screen.Dashboard.route) {
                                // Special handling for Dashboard - always start fresh
                                navController.navigate(item.route) {
                                    // Pop up to the start destination and DON'T save state
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = false  // Don't save previous state (e.g., MatchDetail)
                                    }
                                    // Avoid multiple copies of the same destination
                                    launchSingleTop = true
                                    // DON'T restore previous state - always show clean Dashboard
                                    restoreState = false
                                }
                            } else {
                                // Normal handling for other navigation items
                                navController.navigate(item.route) {
                                    // Pop up to the start destination to avoid building up a large back stack
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination
                                    launchSingleTop = true
                                    // Restore state when re-selecting a previously selected item
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onMatchClick = { match ->
                        // Navigate to match detail when a match is clicked
                        navController.navigate(Screen.MatchDetail.createRoute(match.fixtureId ?: 0))
                    },
                    onNavigateToChat = {
                        navController.navigate(Screen.Chat.route)
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }
            composable(Screen.Matches.route) {
                MatchesScreen(navController = navController)
            }
            composable(Screen.LiveMatches.route) {
                LiveMatchesScreen(
                    onNavigateToChat = { homeTeam, awayTeam, league ->
                        navController.navigate(Screen.Chat.createRoute(homeTeam, awayTeam, league))
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }
            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToMatch = { fixtureId ->
                        navController.navigate(Screen.MatchDetail.createRoute(fixtureId))
                    },
                    onNavigateToNews = { url, title ->
                        navController.navigate(Screen.NewsWebView.createRoute(url, title))
                    }
                )
            }
            composable(Screen.History.route) {
                HistoryScreen(
                    navController = navController,
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }
            composable(
                route = Screen.HistoryDetail.route,
                arguments = listOf(
                    navArgument("predictionId") { type = NavType.IntType },
                    navArgument("fixtureId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val predictionId = backStackEntry.arguments?.getInt("predictionId") ?: 0
                val fixtureId = backStackEntry.arguments?.getInt("fixtureId") ?: 0
                
                HistoryDetailScreen(
                    predictionId = predictionId,
                    fixtureId = fixtureId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(
                route = Screen.Chat.route,
                arguments = listOf(
                    navArgument("homeTeam") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = ""
                    },
                    navArgument("awayTeam") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = ""
                    },
                    navArgument("league") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val homeTeam = backStackEntry.arguments?.getString("homeTeam")
                val awayTeam = backStackEntry.arguments?.getString("awayTeam")
                val league = backStackEntry.arguments?.getString("league")
                
                ChatScreen(
                    homeTeam = homeTeam,
                    awayTeam = awayTeam,
                    league = league,
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToTeamSelection = {
                        navController.navigate("team_selection")
                    }
                )
            }
            
            // Team Selection Screen (old search-based)
            composable("team_selection") {
                TeamSelectionScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            // League Team Browser Screen (new league-based)
            composable("league_team_browser") {
                LeagueTeamBrowserScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(
                route = Screen.MatchDetail.route,
                arguments = listOf(
                    navArgument("fixtureId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val fixtureId = backStackEntry.arguments?.getInt("fixtureId") ?: return@composable
                
                MatchDetailScreen(
                    fixtureId = fixtureId,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToChat = { analysisContext: String ->
                        // Pass the analysis context as the homeTeam parameter
                        // The ChatScreen will need to handle this analysis context
                        navController.navigate(
                            Screen.Chat.createRoute(
                                homeTeam = analysisContext,
                                awayTeam = null,
                                league = null
                            )
                        )
                    }
                )
            }
            
            // News WebView Screen
            composable(
                route = Screen.NewsWebView.route,
                arguments = listOf(
                    navArgument("url") {
                        type = NavType.StringType
                        nullable = false
                    },
                    navArgument("title") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = "Nieuws Artikel"
                    }
                )
            ) { backStackEntry ->
                val url = backStackEntry.arguments?.getString("url") ?: return@composable
                val title = backStackEntry.arguments?.getString("title") ?: "Nieuws Artikel"
                
                NewsWebViewScreen(
                    url = url,
                    title = title,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

/**
 * Data class for bottom navigation items.
 */
data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)
