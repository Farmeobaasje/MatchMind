    package com.Lyno.matchmindai.di

import android.content.Context
import androidx.room.Room
import com.Lyno.matchmindai.BuildConfig
import com.Lyno.matchmindai.data.local.ApiKeyStorage
import com.Lyno.matchmindai.data.local.dao.ChatDao
import com.Lyno.matchmindai.data.local.dao.FixtureDao
import com.Lyno.matchmindai.data.local.dao.LeagueDao
import com.Lyno.matchmindai.data.local.db.AppDatabase
import com.Lyno.matchmindai.data.remote.DeepSeekApi
import com.Lyno.matchmindai.data.remote.football.SimpleApiKeyInterceptor
import com.Lyno.matchmindai.data.remote.football.FootballApiService
import com.Lyno.matchmindai.data.remote.search.TavilyApi
import com.Lyno.matchmindai.data.remote.search.SearchService
import com.Lyno.matchmindai.data.repository.ChatRepositoryImpl
import com.Lyno.matchmindai.data.repository.MatchRepositoryImpl
import com.Lyno.matchmindai.data.repository.SettingsRepositoryImpl
import com.Lyno.matchmindai.data.repository.LeagueRepositoryImpl
import com.Lyno.matchmindai.domain.usecase.GetActiveLeaguesUseCase
import com.Lyno.matchmindai.domain.service.MatchCacheManager
import com.Lyno.matchmindai.domain.service.InMemoryMatchCacheManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.request.headers
import kotlinx.serialization.json.Json

/**
 * Manual Dependency Injection container for the MatchMind AI application.
 * Provides singleton instances of all dependencies following Clean Architecture principles.
 */
class AppContainer(private val context: Context) {

    // JSON serializer for Ktor
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true // This ensures default values like "model" are included in JSON
        prettyPrint = true
    }

    // HTTP Client for standard network requests
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
                requestTimeoutMillis = 30000 // Reduced from 60s to 30s for memory safety
                connectTimeoutMillis = 30000 // Reduced from 60s to 30s
                socketTimeoutMillis = 30000 // Reduced from 60s to 30s
            }
            
            // TODO: Install rate limit tracking for DeepSeek and Tavily
            // installRateLimit(settingsRepository)
        }
    }

    // HTTP Client specifically for DeepSeek R1 streaming (SSE)
    // Optimized for memory safety with connection pooling limits
    private val streamingHttpClient by lazy {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(json)
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        // Log to Android Logcat in debug builds
                        android.util.Log.d("Ktor-Streaming", message)
                    }
                }
                level = LogLevel.ALL
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 0 // No timeout for streaming requests
                connectTimeoutMillis = 30000 // 30 seconds for initial connection
                socketTimeoutMillis = 0 // No socket timeout for streaming
            }
            
            // TODO: Install rate limit tracking for DeepSeek streaming
            // installRateLimit(settingsRepository)
        }
    }

    // HTTP Client specifically for API-Sports with dynamic API key injection
    private val apiSportsClient by lazy {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(json)
            }
            // Simple API key injection using headers directly
            defaultRequest {
                // Get API key from storage
                val apiKey = SimpleApiKeyInterceptor(apiKeyStorage).getApiKey()
                val finalApiKey = if (apiKey.isNotBlank()) apiKey else BuildConfig.API_SPORTS_KEY
                
                // Add headers
                headers {
                    append("x-apisports-key", finalApiKey)
                    append("Accept", "application/json")
                    append("Accept-Charset", "UTF-8")
                    append("Content-Type", "application/json")
                }
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        android.util.Log.d("Ktor-ApiSports", message)
                    }
                }
                level = LogLevel.ALL
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 30000 // Reduced from 60s to 30s for memory safety
                connectTimeoutMillis = 30000 // Reduced from 60s to 30s
                socketTimeoutMillis = 30000 // Reduced from 60s to 30s
            }
            
            // TODO: Install rate limit tracking for API-Sports
            // installRateLimit(settingsRepository)
        }
    }

    // Room Database
    private val appDatabase: AppDatabase by lazy {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "matchmind-db"
        )
            .fallbackToDestructiveMigration() // Allow destructive migration for development
            .build()
    }

    // Chat Data Access Object
    val chatDao: ChatDao by lazy {
        appDatabase.chatDao()
    }

    // Fixture Data Access Object
    val fixtureDao: FixtureDao by lazy {
        appDatabase.fixtureDao()
    }

    // League Data Access Object for Dynamic League Discovery
    val leagueDao: LeagueDao by lazy {
        appDatabase.leagueDao()
    }

    // Local storage for API key
    val apiKeyStorage: ApiKeyStorage by lazy {
        ApiKeyStorage(context)
    }

    // DeepSeek API client (uses streaming client for R1 support)
    val deepSeekApi: DeepSeekApi by lazy {
        DeepSeekApi(httpClient)
    }

    // DeepSeek API client specifically for streaming (uses infinite timeout client)
    val deepSeekStreamingApi: DeepSeekApi by lazy {
        DeepSeekApi(streamingHttpClient)
    }

    // Tavily API client for AI-powered search
    val tavilyApi: TavilyApi by lazy {
        TavilyApi(httpClient)
    }

    // Search service for RAG functionality (replaces WebScraper)
    val searchService: SearchService by lazy {
        SearchService(tavilyApi)
    }

    // Football API Service for API-Sports integration (uses dedicated client with auto headers)
    val footballApiService: FootballApiService by lazy {
        FootballApiService(apiSportsClient)
    }

    // Settings Repository implementation
    val settingsRepository: SettingsRepositoryImpl by lazy {
        SettingsRepositoryImpl(apiKeyStorage)
    }

    // Chat Repository implementation
    val chatRepository: ChatRepositoryImpl by lazy {
        ChatRepositoryImpl(chatDao)
    }

    // League Repository implementation for Dynamic League Discovery
    val leagueRepository: LeagueRepositoryImpl by lazy {
        LeagueRepositoryImpl(leagueDao, footballApiService)
    }

    // GetActiveLeaguesUseCase for Dynamic League Discovery
    val getActiveLeaguesUseCase: GetActiveLeaguesUseCase by lazy {
        GetActiveLeaguesUseCase(leagueRepository)
    }

    // Match Cache Manager for AI agent integration
    val matchCacheManager: MatchCacheManager by lazy {
        InMemoryMatchCacheManager()
    }

    // Match Repository implementation
    val matchRepository: MatchRepositoryImpl by lazy {
        MatchRepositoryImpl(
            footballApiService = footballApiService,
            searchService = searchService,
            deepSeekApi = deepSeekApi,
            fixtureDao = fixtureDao,
            chatDao = chatDao,
            apiKeyStorage = apiKeyStorage,
            settingsRepository = settingsRepository,
            getActiveLeaguesUseCase = getActiveLeaguesUseCase,
            matchCacheManager = matchCacheManager
        )
    }

    // ViewModel Factory for Compose
    val chatViewModelFactory: androidx.lifecycle.ViewModelProvider.Factory
        get() = com.Lyno.matchmindai.presentation.AppViewModelProvider.Factory
}
