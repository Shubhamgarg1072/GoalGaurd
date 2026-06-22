package com.time.applauncher.goalgaurd.feature.onboarding.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.time.applauncher.goalgaurd.core.domain.DataError
import com.time.applauncher.goalgaurd.core.domain.EmptyResult
import com.time.applauncher.goalgaurd.core.domain.Result
import com.time.applauncher.goalgaurd.feature.onboarding.domain.OnboardingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("onboarding")

class DataStoreOnboardingRepository(private val context: Context) : OnboardingRepository {

    private object Keys {
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_AGE = intPreferencesKey("user_age")
        val USER_OCCUPATION = stringPreferencesKey("user_occupation")
    }

    override fun isOnboardingComplete(): Flow<Boolean> =
        context.dataStore.data.map { it[Keys.ONBOARDING_COMPLETE] ?: false }

    override suspend fun completeOnboarding(name: String, age: Int, occupation: String?): EmptyResult<DataError.Local> = try {
        context.dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETE] = true
            prefs[Keys.USER_NAME] = name
            prefs[Keys.USER_AGE] = age
            occupation?.let { prefs[Keys.USER_OCCUPATION] = it }
        }
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(DataError.Local.UNKNOWN)
    }
}
