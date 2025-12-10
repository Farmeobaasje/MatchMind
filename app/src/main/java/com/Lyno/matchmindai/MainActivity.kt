package com.Lyno.matchmindai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.Lyno.matchmindai.di.AppContainer
import com.Lyno.matchmindai.presentation.MatchMindApp
import com.Lyno.matchmindai.ui.theme.MatchMindAITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Get AppContainer from Application
        val appContainer = (application as? com.Lyno.matchmindai.MatchMindApplication)?.appContainer
            ?: AppContainer(applicationContext)
        
        setContent {
            MatchMindAITheme {
                MatchMindApp()
            }
        }
    }
}
