package com.time.applauncher.goalgaurd.core.presentation

import com.time.applauncher.goalgaurd.core.domain.DataError

fun DataError.toUiText(): UiText = when (this) {
    DataError.Network.NO_INTERNET -> UiText.DynamicString("No internet connection")
    DataError.Network.REQUEST_TIMEOUT -> UiText.DynamicString("Request timed out")
    DataError.Network.UNAUTHORIZED -> UiText.DynamicString("Session expired. Please sign in again.")
    DataError.Network.SERVER_ERROR -> UiText.DynamicString("Server error. Please try again later.")
    DataError.Network.TOO_MANY_REQUESTS -> UiText.DynamicString("Too many requests. Please slow down.")
    DataError.Local.DISK_FULL -> UiText.DynamicString("Your device storage is full")
    DataError.Local.NOT_FOUND -> UiText.DynamicString("Item not found")
    else -> UiText.DynamicString("Something went wrong")
}
