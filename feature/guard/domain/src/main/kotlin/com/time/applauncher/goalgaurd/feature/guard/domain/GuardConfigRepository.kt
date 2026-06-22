package com.time.applauncher.goalgaurd.feature.guard.domain

import kotlinx.coroutines.flow.Flow

/** Persists doom-scroll guard settings (DataStore-backed in `feature:guard:data`). */
interface GuardConfigRepository {
    fun observeConfig(): Flow<DoomScrollConfig>
    suspend fun currentConfig(): DoomScrollConfig
    suspend fun setEnabled(enabled: Boolean)
    suspend fun setContinuousMinutesThreshold(minutes: Int)
    suspend fun setMonitoredPackages(packages: Set<String>)
}
