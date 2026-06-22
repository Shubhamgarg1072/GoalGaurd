package com.time.applauncher.goalgaurd.shared.util

/**
 * Multiplatform mirror of the app's `core:domain` Result/Error. Kept here because
 * `core:domain` is a JVM-only module and cannot be consumed from `commonMain`.
 * The Android data layer maps [NetworkError] onto the app's `DataError.Network`.
 */
sealed interface Result<out D, out E> {
    data class Success<out D>(val data: D) : Result<D, Nothing>
    data class Error<out E>(val error: E) : Result<Nothing, E>
}

inline fun <T, E, R> Result<T, E>.map(transform: (T) -> R): Result<R, E> = when (this) {
    is Result.Error -> Result.Error(error)
    is Result.Success -> Result.Success(transform(data))
}

inline fun <T, E> Result<T, E>.onSuccess(action: (T) -> Unit): Result<T, E> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <T, E> Result<T, E>.onFailure(action: (E) -> Unit): Result<T, E> {
    if (this is Result.Error) action(error)
    return this
}

fun <T, E> Result<T, E>.getOrNull(): T? = (this as? Result.Success)?.data
