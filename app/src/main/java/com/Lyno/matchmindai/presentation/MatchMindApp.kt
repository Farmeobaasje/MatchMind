package com.Lyno.matchmindai.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.Lyno.matchmindai.presentation.screens.MatchScreen
import com.Lyno.matchmindai.presentation.screens.SettingsScreen

sealed class Screen(val route: String) {
    object Match : Screen("match")
    object Settings : Screen("settings")
}

@Composable
fun MatchMindApp(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Match.route,
        modifier = modifier
    ) {
        composable(Screen.Match.route) {
            MatchScreen(
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
