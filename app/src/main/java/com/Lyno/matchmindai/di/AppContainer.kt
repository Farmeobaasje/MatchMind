package com.Lyno.matchmindai.di

import android.content.Context
import com.Lyno.matchmindai.data.local.ApiKeyStorage
import com.Lyno.matchmindai.data.remote.DeepSeekApi
import com.Lyno.matchmindai.data.remote.scraper.WebScraper
import com.Lyno.matchmindai.data.repository.MatchRepositoryImpl
import com.Lyno.matchmindai.data.repository.SettingsRepositoryImpl
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Manual Dependency Injection container for the MatchMind AI application.
 * Provides singleton instances of all dependencies following Clean Architecture principles.
 */
class AppContainer(private val context: Context) {

    // JSON serializer for Ktor
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true // This ensures default values like "model" are included in JSON
        prettyPrint = true
    }

    // HTTP Client for network requests
    private val httpClient by lazy {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(json)
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        // Log to Android Logcat in debug builds
                        android.util.Log.d("Ktor", message)
                    }
                }
                level = LogLevel.ALL
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 15000 // 15 seconds
                connectTimeoutMillis = 15000 // 15 seconds
                socketTimeoutMillis = 30000 // 30 seconds
            }
        }
    }

    // Local storage for API key
    val apiKeyStorage: ApiKeyStorage by lazy {
        ApiKeyStorage(context)
    }

    // DeepSeek API client
    val deepSeekApi: DeepSeekApi by lazy {
        DeepSeekApi(httpClient)
    }

    // Web scraper for RAG functionality
    val webScraper: WebScraper by lazy {
        WebScraper()
    }

    // Settings Repository implementation
    val settingsRepository: SettingsRepositoryImpl by lazy {
        SettingsRepositoryImpl(apiKeyStorage)
    }

    // Match Repository implementation
    val matchRepository: MatchRepositoryImpl by lazy {
        MatchRepositoryImpl(apiKeyStorage, deepSeekApi, webScraper, settingsRepository)
    }
}
