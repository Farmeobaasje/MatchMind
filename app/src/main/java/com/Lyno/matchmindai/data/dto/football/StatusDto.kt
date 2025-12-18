package com.Lyno.matchmindai.data.dto.football

import kotlinx.serialization.Serializable

/**
 * DTO for match status from RapidAPI Football API.
 */
@Serializable
data class StatusDto(
    val short: String,
    val long: String,
    val elapsed: Int? = null
)
