package org.dynadoc

sealed class Optional<out T> {
    inline fun getOrElse(default: () -> @UnsafeVariance T): T = when (this) {
        is Some -> value
        None -> default()
    }

    object None : Optional<Nothing>()

    data class Some<T>(val value: T) : Optional<T>()

    companion object {
        fun <T> of(value: T): Optional<T> = Some(value)
    }
}
