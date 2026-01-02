package com.Lyno.matchmindai.data.utils

/**
 * Extension functions for JSON string cleaning and manipulation.
 * Prevents crashes on markdown code blocks in AI responses.
 */
fun String.cleanJson(): String {
    return this.trim()
        .replace("```json", "")
        .replace("```", "")
        .trim()
}

/**
 * Safely parses a JSON string that might contain markdown code blocks.
 * @return Clean JSON string ready for parsing
 */
fun String.safeJsonParse(): String {
    return this.cleanJson()
}

/**
 * Extracts JSON content from a string that might be wrapped in markdown.
 * @return The JSON content without markdown wrappers
 */
fun String.extractJsonContent(): String {
    val cleaned = this.cleanJson()
    // Remove any remaining markdown-like formatting
    return cleaned.replaceFirst("^\\{", "{")
        .replaceFirst("}$", "}")
        .trim()
}
