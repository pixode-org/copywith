package org.pixode.copywith

sealed class Optional<out T> {
    inline fun getOrElse(default: () -> @UnsafeVariance T): T = when (this) {
        is Some -> value
        None -> default()
    }

    fun getOrThrow(): T = when (this) {
        is Some -> value
        None -> throw IllegalStateException("No value present")
    }

    object None : Optional<Nothing>()

    data class Some<T>(val value: T) : Optional<T>()

    companion object {
        fun <T> of(value: T): Optional<T> = Some(value)
    }
}
