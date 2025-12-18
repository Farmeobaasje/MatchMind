package com.Lyno.matchmindai

import android.app.Application
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import com.Lyno.matchmindai.di.AppContainer
import com.Lyno.matchmindai.presentation.widgets.WidgetSyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Main application class for MatchMind AI.
 * Initializes the dependency injection container and provides application-wide services.
 */
class MatchMindApplication : Application(), ImageLoaderFactory {

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
        
        // Perform data migration for existing users
        performDataMigration()
        
        // Initialize widget services
        initializeWidgetServices()
    }
    
    /**
     * Creates and configures the ImageLoader for Coil with SVG support.
     * This enables loading SVG images from URLs (e.g., team logos from API-Sports).
     * Important: SVG rendering requires hardware acceleration to be disabled.
     */
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(SvgDecoder.Factory())
            }
            .crossfade(true)
            .allowHardware(false) // Required for SVG rendering
            .build()
    }
    
    /**
     * Perform data migration for existing users.
     * This ensures backward compatibility when the data structure changes.
     */
    private fun performDataMigration() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Migrate old API key structure if needed
                appContainer.apiKeyStorage.migrateOldApiKey()
                Log.d("MatchMindApplication", "Data migration completed successfully")
            } catch (e: Exception) {
                Log.e("MatchMindApplication", "Data migration failed", e)
            }
        }
    }
    
    /**
     * Initialize widget-related services.
     * This includes WorkManager sync and FCM setup.
     */
    private fun initializeWidgetServices() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Start periodic widget synchronization
                WidgetSyncWorker.schedulePeriodicSync(this@MatchMindApplication)
                Log.d("MatchMindApplication", "Widget sync service initialized")
            } catch (e: Exception) {
                Log.e("MatchMindApplication", "Failed to initialize widget services", e)
            }
        }
    }
}
