package com.Lyno.matchmindai.common

/**
 * A generic sealed class that represents a resource with loading, success, and error states.
 * Used for handling asynchronous operations in a type-safe manner.
 */
sealed class Resource<out T> {
    /**
     * Represents a loading state.
     */
    data class Loading<out T>(val data: T? = null) : Resource<T>()

    /**
     * Represents a success state with data.
     */
    data class Success<out T>(val data: T) : Resource<T>()

    /**
     * Represents an error state with an optional message.
     */
    data class Error<out T>(val message: String, val data: T? = null) : Resource<T>()

    /**
     * Check if the resource is in loading state.
     */
    val isLoading: Boolean
        get() = this is Loading

    /**
     * Check if the resource is in success state.
     */
    val isSuccess: Boolean
        get() = this is Success

    /**
     * Check if the resource is in error state.
     */
    val isError: Boolean
        get() = this is Error

    /**
     * Get the data if the resource is in success state, null otherwise.
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    /**
     * Get the data or throw an exception if the resource is not in success state.
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw IllegalStateException(message ?: "Unknown error")
        is Loading -> throw IllegalStateException("Data is still loading")
    }

    /**
     * Transform the data if the resource is in success state.
     */
    fun <R> map(transform: (T) -> R): Resource<R> = when (this) {
        is Loading -> Loading(data?.let(transform))
        is Success -> Success(transform(data))
        is Error -> Error(message, data?.let(transform))
    }

    /**
     * Execute a block if the resource is in success state.
     */
    fun onSuccess(block: (T) -> Unit): Resource<T> {
        if (this is Success) {
            block(data)
        }
        return this
    }

    /**
     * Execute a block if the resource is in error state.
     */
    fun onError(block: (String) -> Unit): Resource<T> {
        if (this is Error) {
            block(message)
        }
        return this
    }

    /**
     * Execute a block if the resource is in loading state.
     */
    fun onLoading(block: () -> Unit): Resource<T> {
        if (this is Loading) {
            block()
        }
        return this
    }
}
