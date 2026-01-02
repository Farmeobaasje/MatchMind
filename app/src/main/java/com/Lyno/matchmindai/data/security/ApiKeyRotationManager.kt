package com.Lyno.matchmindai.data.security

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

val Context.apiKeyRotationDataStore by preferencesDataStore(name = "api_key_rotation")

/**
 * API Key entry with metadata for rotation.
 */
@Serializable
data class ApiKeyEntry(
    val keyId: String,
    val apiKey: String,
    val apiName: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsed: Long = System.currentTimeMillis(),
    val usageCount: Int = 0,
    val isActive: Boolean = true,
    val rotationInterval: Long = 7 * 24 * 60 * 60 * 1000L, // 7 days in milliseconds
    val maxUsageCount: Int = 1000, // Maximum number of uses before rotation
    val failureCount: Int = 0, // Number of failures with this key
    val maxFailureCount: Int = 10 // Maximum failures before deactivation
) {
    /**
     * Check if this key needs rotation.
     */
    fun needsRotation(): Boolean {
        val timeSinceCreation = System.currentTimeMillis() - createdAt
        val timeSinceLastUse = System.currentTimeMillis() - lastUsed
        
        return !isActive ||
               timeSinceCreation >= rotationInterval ||
               usageCount >= maxUsageCount ||
               failureCount >= maxFailureCount ||
               timeSinceLastUse >= rotationInterval * 2 // Inactive for too long
    }
    
    /**
     * Check if this key is valid for use.
     */
    fun isValid(): Boolean {
        return isActive && 
               failureCount < maxFailureCount &&
               usageCount < maxUsageCount &&
               System.currentTimeMillis() - createdAt < rotationInterval * 2
    }
    
    /**
     * Mark the key as used.
     */
    fun markUsed(): ApiKeyEntry {
        return copy(
            lastUsed = System.currentTimeMillis(),
            usageCount = usageCount + 1
        )
    }
    
    /**
     * Mark the key as failed.
     */
    fun markFailed(): ApiKeyEntry {
        return copy(
            failureCount = failureCount + 1,
            lastUsed = System.currentTimeMillis()
        )
    }
    
    /**
     * Deactivate the key.
     */
    fun deactivate(): ApiKeyEntry {
        return copy(isActive = false)
    }
    
    /**
     * Returns a formatted string for logging (without the actual key).
     */
    fun toLogString(): String {
        return "ApiKeyEntry(id=$keyId, api=$apiName, active=$isActive, " +
               "usage=$usageCount/$maxUsageCount, failures=$failureCount/$maxFailureCount, " +
               "age=${(System.currentTimeMillis() - createdAt) / (24 * 60 * 60 * 1000)} days)"
    }
}

/**
 * API Key Rotation Manager for managing and rotating API keys.
 * 
 * Features:
 * - Multiple key support for the same API
 * - Automatic rotation based on usage, time, and failures
 * - Graceful degradation when keys are exhausted
 * - Secure storage using DataStore
 * - In-memory caching for performance
 * - Failure tracking and automatic deactivation
 */
class ApiKeyRotationManager(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }
    
    // In-memory cache for fast access (thread-safe)
    private val memoryCache = ConcurrentHashMap<String, List<ApiKeyEntry>>()
    
    companion object {
        private const val TAG = "ApiKeyRotationManager"
        
        // DataStore keys
        private fun apiKeysKey(apiName: String) = stringPreferencesKey("api_keys_$apiName")
        
        // Default rotation intervals (in milliseconds)
        const val ROTATION_INTERVAL_DAYS = 7L
        const val ROTATION_INTERVAL_MS = ROTATION_INTERVAL_DAYS * 24 * 60 * 60 * 1000L
        
        // Default usage limits
        const val DEFAULT_MAX_USAGE = 1000
        const val DEFAULT_MAX_FAILURES = 10
    }

    /**
     * Register a new API key.
     * 
     * @param apiName The name of the API (e.g., "API-SPORTS", "DeepSeek")
     * @param apiKey The API key value
     * @param keyId Optional custom key ID (auto-generated if not provided)
     * @param rotationInterval Optional rotation interval in milliseconds
     * @param maxUsage Optional maximum usage count before rotation
     * @param maxFailures Optional maximum failures before deactivation
     * @return The created ApiKeyEntry
     */
    suspend fun registerKey(
        apiName: String,
        apiKey: String,
        keyId: String? = null,
        rotationInterval: Long = ROTATION_INTERVAL_MS,
        maxUsage: Int = DEFAULT_MAX_USAGE,
        maxFailures: Int = DEFAULT_MAX_FAILURES
    ): ApiKeyEntry {
        val actualKeyId = keyId ?: generateKeyId(apiName)
        
        val entry = ApiKeyEntry(
            keyId = actualKeyId,
            apiKey = apiKey,
            apiName = apiName,
            rotationInterval = rotationInterval,
            maxUsageCount = maxUsage,
            maxFailureCount = maxFailures
        )
        
        // Get existing keys
        val existingKeys = getKeys(apiName).toMutableList()
        
        // Add new key
        existingKeys.add(entry)
        
        // Save updated list
        saveKeys(apiName, existingKeys)
        
        Log.d(TAG, "Registered new key for $apiName: ${entry.toLogString()}")
        return entry
    }

    /**
     * Get an active API key for the specified API.
     * 
     * @param apiName The name of the API
     * @return An active API key, or null if no valid keys available
     */
    suspend fun getActiveKey(apiName: String): String? {
        Log.d(TAG, "Getting active key for API: $apiName")
        
        // Get all keys for this API
        val allKeys = getKeys(apiName)
        
        if (allKeys.isEmpty()) {
            Log.w(TAG, "No keys registered for API: $apiName")
            return null
        }
        
        // Find the first valid, active key
        val validKey = allKeys.firstOrNull { it.isValid() }
        
        if (validKey == null) {
            Log.w(TAG, "No valid keys found for API: $apiName. All keys: ${allKeys.size}")
            allKeys.forEach { Log.d(TAG, "  - ${it.toLogString()}") }
            return null
        }
        
        // Mark the key as used and save
        val updatedKey = validKey.markUsed()
        updateKey(apiName, validKey.keyId, updatedKey)
        
        Log.d(TAG, "Using key for $apiName: ${updatedKey.toLogString()}")
        return updatedKey.apiKey
    }

    /**
     * Get all keys for an API.
     * 
     * @param apiName The name of the API
     * @return List of all keys for the API
     */
    suspend fun getKeys(apiName: String): List<ApiKeyEntry> {
        // Check in-memory cache first
        memoryCache[apiName]?.let { return it }
        
        // Load from DataStore
        val storedKeys = loadFromDataStore(apiName)
        if (storedKeys != null) {
            memoryCache[apiName] = storedKeys
            return storedKeys
        }
        
        // Return empty list if not found
        return emptyList()
    }

    /**
     * Record a successful API call with a key.
     * 
     * @param apiName The name of the API
     * @param keyId The key ID that was used
     */
    suspend fun recordSuccess(apiName: String, keyId: String) {
        Log.d(TAG, "Recording success for API: $apiName, key: $keyId")
        
        val keys = getKeys(apiName).toMutableList()
        val keyIndex = keys.indexOfFirst { it.keyId == keyId }
        
        if (keyIndex >= 0) {
            val key = keys[keyIndex]
            val updatedKey = key.markUsed()
            keys[keyIndex] = updatedKey
            saveKeys(apiName, keys)
            
            Log.d(TAG, "Success recorded for key $keyId of $apiName")
        } else {
            Log.w(TAG, "Key $keyId not found for API $apiName")
        }
    }

    /**
     * Record a failed API call with a key.
     * 
     * @param apiName The name of the API
     * @param keyId The key ID that was used
     * @param deactivateIfExhausted If true, deactivate the key if it exceeds max failures
     */
    suspend fun recordFailure(apiName: String, keyId: String, deactivateIfExhausted: Boolean = true) {
        Log.w(TAG, "Recording failure for API: $apiName, key: $keyId")
        
        val keys = getKeys(apiName).toMutableList()
        val keyIndex = keys.indexOfFirst { it.keyId == keyId }
        
        if (keyIndex >= 0) {
            val key = keys[keyIndex]
            val updatedKey = key.markFailed()
            
            // Check if we should deactivate the key
            val finalKey = if (deactivateIfExhausted && updatedKey.failureCount >= updatedKey.maxFailureCount) {
                Log.w(TAG, "Key $keyId has reached max failures (${updatedKey.failureCount}/${updatedKey.maxFailureCount}). Deactivating.")
                updatedKey.deactivate()
            } else {
                updatedKey
            }
            
            keys[keyIndex] = finalKey
            saveKeys(apiName, keys)
            
            Log.w(TAG, "Failure recorded for key $keyId of $apiName. Failures: ${finalKey.failureCount}/${finalKey.maxFailureCount}")
        } else {
            Log.w(TAG, "Key $keyId not found for API $apiName")
        }
    }

    /**
     * Rotate keys for an API (deactivate old keys, activate new ones).
     * 
     * @param apiName The name of the API
     * @param newKeys Optional list of new keys to add
     * @return List of deactivated key IDs
     */
    suspend fun rotateKeys(apiName: String, newKeys: List<String> = emptyList()): List<String> {
        Log.d(TAG, "Rotating keys for API: $apiName, new keys: ${newKeys.size}")
        
        val keys = getKeys(apiName).toMutableList()
        val deactivatedKeys = mutableListOf<String>()
        
        // Deactivate keys that need rotation
        keys.forEachIndexed { index, key ->
            if (key.needsRotation() && key.isActive) {
                Log.d(TAG, "Deactivating key ${key.keyId} for rotation")
                keys[index] = key.deactivate()
                deactivatedKeys.add(key.keyId)
            }
        }
        
        // Add new keys
        newKeys.forEach { newKey ->
            val keyId = generateKeyId(apiName)
            val entry = ApiKeyEntry(
                keyId = keyId,
                apiKey = newKey,
                apiName = apiName
            )
            keys.add(entry)
            Log.d(TAG, "Added new key $keyId during rotation")
        }
        
        // Save updated keys
        saveKeys(apiName, keys)
        
        Log.d(TAG, "Key rotation completed for $apiName. Deactivated: ${deactivatedKeys.size}, Total keys: ${keys.size}")
        return deactivatedKeys
    }

    /**
     * Check if any keys need rotation.
     * 
     * @param apiName The name of the API
     * @return True if any keys need rotation
     */
    suspend fun needsRotation(apiName: String): Boolean {
        return getKeys(apiName).any { it.needsRotation() }
    }

    /**
     * Get rotation status for an API.
     * 
     * @param apiName The name of the API
     * @return Map with rotation statistics
     */
    suspend fun getRotationStatus(apiName: String): Map<String, Any> {
        val keys = getKeys(apiName)
        val activeKeys = keys.count { it.isActive && it.isValid() }
        val needsRotation = keys.count { it.needsRotation() }
        val totalKeys = keys.size
        
        return mapOf(
            "apiName" to apiName,
            "totalKeys" to totalKeys,
            "activeKeys" to activeKeys,
            "needsRotation" to needsRotation,
            "keys" to keys.map { it.toLogString() }
        )
    }

    /**
     * Get all APIs with registered keys.
     * 
     * @return List of API names
     */
    suspend fun getAllApis(): List<String> {
        return try {
            val preferences = context.apiKeyRotationDataStore.data.first()
            val apiNames = mutableListOf<String>()
            
            for ((key, _) in preferences.asMap()) {
                if (key.name.startsWith("api_keys_")) {
                    val apiName = key.name.removePrefix("api_keys_")
                    apiNames.add(apiName)
                }
            }
            
            apiNames
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all APIs", e)
            emptyList()
        }
    }

    /**
     * Remove all keys for an API.
     * 
     * @param apiName The name of the API
     */
    suspend fun clearKeys(apiName: String) {
        Log.d(TAG, "Clearing all keys for API: $apiName")
        
        try {
            context.apiKeyRotationDataStore.edit { preferences ->
                preferences.remove(apiKeysKey(apiName))
            }
            memoryCache.remove(apiName)
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing keys for API $apiName", e)
        }
    }

    /**
     * Remove all keys for all APIs.
     */
    suspend fun clearAllKeys() {
        Log.d(TAG, "Clearing all keys for all APIs")
        
        val allApis = getAllApis()
        allApis.forEach { apiName ->
            clearKeys(apiName)
        }
    }

    // Private helper methods
    
    private suspend fun saveKeys(apiName: String, keys: List<ApiKeyEntry>) {
        try {
            val jsonString = json.encodeToString(keys)
            context.apiKeyRotationDataStore.edit { preferences ->
                preferences[apiKeysKey(apiName)] = jsonString
            }
            memoryCache[apiName] = keys
        } catch (e: Exception) {
            Log.e(TAG, "Error saving keys for API $apiName", e)
        }
    }
    
    private suspend fun loadFromDataStore(apiName: String): List<ApiKeyEntry>? {
        return try {
            val preferences = context.apiKeyRotationDataStore.data.first()
            val jsonString = preferences[apiKeysKey(apiName)]
            jsonString?.let { json.decodeFromString<List<ApiKeyEntry>>(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading keys for API $apiName", e)
            null
        }
    }
    
    private suspend fun updateKey(apiName: String, keyId: String, updatedKey: ApiKeyEntry) {
        val keys = getKeys(apiName).toMutableList()
        val keyIndex = keys.indexOfFirst { it.keyId == keyId }
        
        if (keyIndex >= 0) {
            keys[keyIndex] = updatedKey
            saveKeys(apiName, keys)
        }
    }
    
    private fun generateKeyId(apiName: String): String {
        val timestamp = System.currentTimeMillis()
        val random = (Math.random() * 10000).toInt()
        return "${apiName}_${timestamp}_$random"
    }
}

/**
 * Extension function for easy API key rotation in repositories.
 */
suspend fun ApiKeyRotationManager.withRotatingKey(
    apiName: String,
    action: suspend (key: String) -> Unit
): Boolean {
    return try {
        val key = getActiveKey(apiName)
        if (key == null) {
            Log.e("ApiKeyRotation", "No active key available for API: $apiName")
            return false
        }
        
        // Extract key ID from the key (we need to find which key was used)
        val keys = getKeys(apiName)
        val keyEntry = keys.firstOrNull { it.apiKey == key }
        val keyId = keyEntry?.keyId ?: "unknown"
        
        try {
            action(key)
            recordSuccess(apiName, keyId)
            true
        } catch (e: Exception) {
            recordFailure(apiName, keyId)
            false
        }
    } catch (e: Exception) {
        Log.e("ApiKeyRotation", "Error in withRotatingKey for $apiName", e)
        false
    }
}

/**
 * Extension function for API calls that return a result.
 */
suspend fun <T> ApiKeyRotationManager.withRotatingKeyResult(
    apiName: String,
    action: suspend (key: String) -> T
): Result<T> {
    return try {
        val key = getActiveKey(apiName)
        if (key == null) {
            Log.e("ApiKeyRotation", "No active key available for API: $apiName")
            return Result.failure(IllegalStateException("No active key available for $apiName"))
        }
        
        // Extract key ID from the key
        val keys = getKeys(apiName)
        val keyEntry = keys.firstOrNull { it.apiKey == key }
        val keyId = keyEntry?.keyId ?: "unknown"
        
        try {
            val result = action(key)
            recordSuccess(apiName, keyId)
            Result.success(result)
        } catch (e: Exception) {
            recordFailure(apiName, keyId)
            Result.failure(e)
        }
    } catch (e: Exception) {
        Log.e("ApiKeyRotation", "Error in withRotatingKeyResult for $apiName", e)
        Result.failure(e)
    }
}
