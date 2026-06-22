package com.time.applauncher.goalgaurd.feature.auth.data

import com.time.applauncher.goalgaurd.core.domain.DataError
import com.time.applauncher.goalgaurd.core.domain.EmptyResult
import com.time.applauncher.goalgaurd.feature.auth.domain.AuthRepository
import com.time.applauncher.goalgaurd.feature.auth.domain.AuthUser
import com.time.applauncher.goalgaurd.shared.api.GoalGuardApi
import com.time.applauncher.goalgaurd.shared.util.NetworkError
import com.time.applauncher.goalgaurd.core.domain.Result as AppResult
import com.time.applauncher.goalgaurd.shared.util.Result as SharedResult

class GoogleAuthRepository(
    private val api: GoalGuardApi,
    private val tokenStore: DataStoreTokenStore,
) : AuthRepository {

    override fun authState() = tokenStore.authState()

    override suspend fun signInWithGoogle(idToken: String): EmptyResult<DataError.Network> =
        when (val res = api.signInWithGoogle(idToken)) {
            is SharedResult.Success -> {
                val data = res.data
                tokenStore.saveSession(
                    user = AuthUser(data.user.id, data.user.email, data.user.displayName, data.user.pictureUrl),
                    accessToken = data.accessToken,
                    refreshToken = data.refreshToken,
                )
                AppResult.Success(Unit)
            }
            is SharedResult.Error -> AppResult.Error(res.error.toDataError())
        }

    override suspend fun signOut() = tokenStore.clear()
}

/** Maps the shared [NetworkError] onto the app's [DataError.Network] (identical names). */
fun NetworkError.toDataError(): DataError.Network =
    runCatching { DataError.Network.valueOf(name) }.getOrDefault(DataError.Network.UNKNOWN)
