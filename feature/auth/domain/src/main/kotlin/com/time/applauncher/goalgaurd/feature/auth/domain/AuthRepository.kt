package com.time.applauncher.goalgaurd.feature.auth.domain

import com.time.applauncher.goalgaurd.core.domain.DataError
import com.time.applauncher.goalgaurd.core.domain.EmptyResult
import kotlinx.coroutines.flow.Flow

data class AuthUser(
    val id: String,
    val email: String,
    val displayName: String?,
    val pictureUrl: String?,
)

interface AuthRepository {
    /** Emits the signed-in user, or null when signed out. */
    fun authState(): Flow<AuthUser?>

    /** Exchanges a Google ID token for an app session. Degrades to local on failure. */
    suspend fun signInWithGoogle(idToken: String): EmptyResult<DataError.Network>

    suspend fun signOut()
}
