package com.time.applauncher.goalgaurd.shared.util

/** Mirrors `core:domain` DataError.Network so the Android side can map 1:1. */
enum class NetworkError {
    BAD_REQUEST,
    REQUEST_TIMEOUT,
    UNAUTHORIZED,
    FORBIDDEN,
    NOT_FOUND,
    CONFLICT,
    TOO_MANY_REQUESTS,
    NO_INTERNET,
    PAYLOAD_TOO_LARGE,
    SERVER_ERROR,
    SERVICE_UNAVAILABLE,
    SERIALIZATION,
    UNKNOWN,
}
