package com.Lyno.matchmindai.data.utils

import android.content.Context
import java.util.Properties

/**
 * Helper class to read configuration values from local.properties file.
 * Used to access API keys and other configuration without hardcoding them.
 */
object BuildConfigHelper {

    private var properties: Properties? = null

    /**
     * Initializes the properties from local.properties file.
     * Should be called once during app startup.
     */
    fun initialize(context: Context) {
        try {
            properties = Properties().apply {
                val inputStream = context.assets.open("local.properties")
                load(inputStream)
                inputStream.close()
            }
        } catch (e: Exception) {
            // In production, we might want to use BuildConfig fields instead
            android.util.Log.e("BuildConfigHelper", "Failed to load local.properties", e)
        }
    }

    /**
     * Gets the Tavily API key from configuration.
     * Reads from local.properties file.
     */
    fun getTavilyApiKey(): String {
        return try {
            properties?.getProperty("TAVILY_API_KEY") ?: "your_tavily_api_key_here"
        } catch (e: Exception) {
            android.util.Log.e("BuildConfigHelper", "Failed to get Tavily API key", e)
            "your_tavily_api_key_here"
        }
    }

    /**
     * Gets the DeepSeek API key from configuration (if needed).
     * Currently DeepSeek API key is user-managed via ApiKeyStorage.
     */
    fun getDeepSeekApiKey(): String? {
        return properties?.getProperty("DEEPSEEK_API_KEY")
    }
}
