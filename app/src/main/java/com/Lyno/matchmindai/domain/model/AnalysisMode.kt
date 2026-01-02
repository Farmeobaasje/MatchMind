package com.Lyno.matchmindai.domain.model

/**
 * Enum representing the analysis mode for AI predictions.
 * 
 * - TURBO: Fast analysis with basic statistics and quick reasoning.
 * - BALANCED: Balanced analysis with moderate depth and speed.
 * - DEEP_DIVE: Comprehensive analysis with detailed statistics, historical data,
 *   and in-depth reasoning. Takes longer but provides more accurate predictions.
 */
enum class AnalysisMode {
    TURBO,
    BALANCED,
    DEEP_DIVE
}
