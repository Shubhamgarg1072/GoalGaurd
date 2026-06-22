package com.time.applauncher.goalgaurd.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val displayName: String? = null,
    val pictureUrl: String? = null,
)

/** App posts the Google ID token obtained via Credential Manager. */
@Serializable
data class GoogleSignInRequest(val idToken: String)

@Serializable
data class RefreshRequest(val refreshToken: String)

@Serializable
data class AuthResponse(
    val user: UserDto,
    val accessToken: String,
    val refreshToken: String,
)
