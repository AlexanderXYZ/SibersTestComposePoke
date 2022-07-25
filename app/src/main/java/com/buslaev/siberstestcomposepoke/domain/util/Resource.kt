package com.buslaev.siberstestcomposepoke.domain.util

/**
 * class Resource to handle
 */
sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T?) : Resource<T>(data)
    class Loading<T> : Resource<T>()
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
}
