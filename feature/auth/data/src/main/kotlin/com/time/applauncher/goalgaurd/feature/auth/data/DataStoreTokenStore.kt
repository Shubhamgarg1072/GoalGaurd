package com.time.applauncher.goalgaurd.feature.auth.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.time.applauncher.goalgaurd.feature.auth.domain.AuthUser
import com.time.applauncher.goalgaurd.shared.api.TokenProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore("auth")

/** Persists app JWTs + the signed-in user. Doubles as the shared [TokenProvider]. */
class DataStoreTokenStore(private val context: Context) : TokenProvider {

    private object Keys {
        val ACCESS = stringPreferencesKey("access_token")
        val REFRESH = stringPreferencesKey("refresh_token")
        val USER_ID = stringPreferencesKey("user_id")
        val EMAIL = stringPreferencesKey("email")
        val NAME = stringPreferencesKey("display_name")
        val PICTURE = stringPreferencesKey("picture_url")
    }

    override suspend fun accessToken(): String? =
        context.authDataStore.data.first()[Keys.ACCESS]

    override suspend fun refreshToken(): String? =
        context.authDataStore.data.first()[Keys.REFRESH]

    override suspend fun updateTokens(accessToken: String, refreshToken: String) {
        context.authDataStore.edit {
            it[Keys.ACCESS] = accessToken
            it[Keys.REFRESH] = refreshToken
        }
    }

    suspend fun saveSession(user: AuthUser, accessToken: String, refreshToken: String) {
        context.authDataStore.edit {
            it[Keys.ACCESS] = accessToken
            it[Keys.REFRESH] = refreshToken
            it[Keys.USER_ID] = user.id
            it[Keys.EMAIL] = user.email
            user.displayName?.let { v -> it[Keys.NAME] = v } ?: it.remove(Keys.NAME)
            user.pictureUrl?.let { v -> it[Keys.PICTURE] = v } ?: it.remove(Keys.PICTURE)
        }
    }

    override suspend fun clear() {
        context.authDataStore.edit { it.clear() }
    }

    fun authState(): Flow<AuthUser?> = context.authDataStore.data.map { prefs ->
        val id = prefs[Keys.USER_ID]
        val email = prefs[Keys.EMAIL]
        if (id == null || email == null) null
        else AuthUser(id, email, prefs[Keys.NAME], prefs[Keys.PICTURE])
    }
}
