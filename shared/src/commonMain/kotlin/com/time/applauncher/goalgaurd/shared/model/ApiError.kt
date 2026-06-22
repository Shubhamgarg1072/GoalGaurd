package com.time.applauncher.goalgaurd.shared.model

import kotlinx.serialization.Serializable

/** Uniform error envelope returned by the backend on non-2xx responses. */
@Serializable
data class ApiError(
    val code: String,
    val message: String,
)
