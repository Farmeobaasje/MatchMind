package com.Lyno.matchmindai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.Lyno.matchmindai.di.AppContainer
import com.Lyno.matchmindai.presentation.MatchMindAppScaffold
import com.Lyno.matchmindai.ui.theme.MatchMindAITheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import android.util.Log

class MainActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize AppContainer
        val appContainer = AppContainer(applicationContext)
        
        // Start Dynamic League Discovery on app startup
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Starting Dynamic League Discovery on app startup...")
                
                // Refresh leagues if needed (once per week)
                val refreshResult = appContainer.getActiveLeaguesUseCase.refreshIfNeeded()
                
                if (refreshResult.isSuccess) {
                    val leaguesRefreshed = refreshResult.getOrNull() ?: 0
                    if (leaguesRefreshed > 0) {
                        Log.d(TAG, "Successfully refreshed $leaguesRefreshed leagues from API")
                    } else {
                        Log.d(TAG, "Leagues are up to date, no refresh needed")
                    }
                    
                    // Get current league count for logging
                    val leagueFlow = appContainer.getActiveLeaguesUseCase.execute()
                    val leagueCount = leagueFlow.first().size
                    Log.d(TAG, "Current active leagues in cache: $leagueCount")
                    
                    // Get top priority league IDs for debugging
                    val topLeagueIds = appContainer.getActiveLeaguesUseCase.getTopPriorityLeagueIds()
                    Log.d(TAG, "Top priority league IDs: $topLeagueIds")
                    
                } else {
                    Log.e(TAG, "Failed to refresh leagues: ${refreshResult.exceptionOrNull()?.message}")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error during Dynamic League Discovery startup: ${e.message}", e)
            }
        }
        
        setContent {
            MatchMindAITheme {
                MatchMindAppScaffold()
            }
        }
    }
}
