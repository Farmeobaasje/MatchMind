package com.Lyno.matchmindai

import android.app.Application
import com.Lyno.matchmindai.di.AppContainer

/**
 * Main application class for MatchMind AI.
 * Initializes the dependency injection container and provides application-wide services.
 */
class MatchMindApplication : Application() {

    /**
     * Application-wide dependency container.
     * Accessible via (applicationContext as MatchMindApplication).appContainer
     */
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        // Initialize the dependency injection container
        appContainer = AppContainer(this)
    }
}
