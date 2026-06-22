package com.time.applauncher.goalgaurd.shared.api

/** Supplies/persists the app JWTs. Implemented on Android over DataStore. */
interface TokenProvider {
    suspend fun accessToken(): String?
    suspend fun refreshToken(): String?
    suspend fun updateTokens(accessToken: String, refreshToken: String)
    suspend fun clear()
}

/** No-op provider for unauthenticated contexts (e.g. the coach proxy when signed out). */
object NoopTokenProvider : TokenProvider {
    override suspend fun accessToken(): String? = null
    override suspend fun refreshToken(): String? = null
    override suspend fun updateTokens(accessToken: String, refreshToken: String) {}
    override suspend fun clear() {}
}
