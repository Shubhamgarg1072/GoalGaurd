package com.time.applauncher.goalgaurd.feature.guard.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.time.applauncher.goalgaurd.feature.guard.domain.DoomScrollConfig
import com.time.applauncher.goalgaurd.feature.guard.domain.GuardConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.guardDataStore: DataStore<Preferences> by preferencesDataStore("guard")

class DataStoreGuardConfigRepository(private val context: Context) : GuardConfigRepository {

    private object Keys {
        val ENABLED = booleanPreferencesKey("guard_enabled")
        val CONTINUOUS_MINUTES = intPreferencesKey("continuous_minutes_threshold")
        val MONITORED_PACKAGES = stringSetPreferencesKey("monitored_packages")
    }

    override fun observeConfig(): Flow<DoomScrollConfig> =
        context.guardDataStore.data.map { it.toConfig() }

    override suspend fun currentConfig(): DoomScrollConfig =
        context.guardDataStore.data.first().toConfig()

    override suspend fun setEnabled(enabled: Boolean) {
        context.guardDataStore.edit { it[Keys.ENABLED] = enabled }
    }

    override suspend fun setContinuousMinutesThreshold(minutes: Int) {
        context.guardDataStore.edit { it[Keys.CONTINUOUS_MINUTES] = minutes }
    }

    override suspend fun setMonitoredPackages(packages: Set<String>) {
        context.guardDataStore.edit { it[Keys.MONITORED_PACKAGES] = packages }
    }

    private fun Preferences.toConfig(): DoomScrollConfig {
        val defaults = DoomScrollConfig()
        return defaults.copy(
            enabled = this[Keys.ENABLED] ?: defaults.enabled,
            continuousMinutesThreshold = this[Keys.CONTINUOUS_MINUTES] ?: defaults.continuousMinutesThreshold,
            monitoredPackages = this[Keys.MONITORED_PACKAGES] ?: defaults.monitoredPackages,
        )
    }
}
