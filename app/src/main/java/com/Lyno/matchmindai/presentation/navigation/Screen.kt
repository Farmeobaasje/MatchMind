package com.Lyno.matchmindai.presentation.navigation

/**
 * Screen sealed class with all navigation routes.
 */
sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Matches : Screen("matches")
    object Favorites : Screen("favorites")
    object Chat : Screen("chat?homeTeam={homeTeam}&awayTeam={awayTeam}&league={league}") {
        fun createRoute(homeTeam: String? = null, awayTeam: String? = null, league: String? = null): String {
            return "chat?homeTeam=${homeTeam ?: ""}&awayTeam=${awayTeam ?: ""}&league=${league ?: ""}"
        }
    }
    object Settings : Screen("settings")
    object LiveMatches : Screen("live_matches")
    object MatchDetail : Screen("match_detail/{fixtureId}") {
        fun createRoute(fixtureId: Int): String {
            return "match_detail/$fixtureId"
        }
    }
}
