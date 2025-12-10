package com.Lyno.matchmindai.domain.model

/**
 * Data class representing user preferences.
 * This is a domain model that should be independent of any data layer implementation.
 */
data class UserPreferences(
    val apiKey: String? = null,
    val useLiveData: Boolean = true
)
