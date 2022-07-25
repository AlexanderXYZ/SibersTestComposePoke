package com.buslaev.siberstestcomposepoke.common

/**
 * interface to handle events in viewModel
 */
interface EventHandler<T> {
    fun obtainEvent(event: T)
}