package com.Lyno.matchmindai.domain.model

/**
 * Domain model representing a chat session.
 * This is the domain layer representation of a chat session.
 */
data class ChatSession(
    val id: String,
    val title: String,
    val lastUpdated: Long
)
