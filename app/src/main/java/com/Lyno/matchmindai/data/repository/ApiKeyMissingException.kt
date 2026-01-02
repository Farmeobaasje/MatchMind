package com.Lyno.matchmindai.data.repository

/**
 * Exception thrown when an API key is missing or invalid.
 * This is used to trigger UI flows for API key configuration.
 */
class ApiKeyMissingException(message: String = "API key is missing") : Exception(message)
