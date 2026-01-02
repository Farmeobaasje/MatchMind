package com.Lyno.matchmindai.data.utils

/**
 * Exception thrown when memory is critically low during streaming operations.
 * This prevents OutOfMemoryError crashes by aborting operations early.
 */
class LowMemoryException(message: String) : Exception(message)

/**
 * Exception thrown when a buffer exceeds its maximum allowed size.
 * Prevents unbounded memory growth during data processing.
 */
class BufferOverflowException(message: String) : Exception(message)

/**
 * Exception thrown when streaming cannot proceed due to memory constraints.
 * Used for graceful degradation instead of hard crashes.
 */
class MemoryConstraintException(message: String) : Exception(message)
