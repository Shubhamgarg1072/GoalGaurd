package com.time.applauncher.goalgaurd.feature.guard.domain

/**
 * A single reading for one monitored package at a point in time.
 *
 * Kept as a plain value type so [DoomScrollDetector] can be exercised with fakes in unit tests —
 * no Android types leak into the detection rules.
 */
data class UsageReading(
    val packageName: String,
    /** How long this app has been continuously in the foreground, in whole minutes. */
    val continuousForegroundMinutes: Int,
    /** Times this app moved to the foreground within the recent reopening window. */
    val reopenCountInWindow: Int,
    /** Distinct app-switch events observed within the recent rapid-switch window. */
    val appSwitchCountInWindow: Int,
)

/**
 * Thin abstraction over Android's `UsageStatsManager` so detection logic stays pure and testable.
 * Implemented in `feature:guard:data`.
 */
interface UsageStatsReader {

    /** True only when the user has granted PACKAGE_USAGE_STATS access. Never throws. */
    fun hasUsageAccess(): Boolean

    /** Package currently in the foreground, or null if it can't be determined / access is denied. */
    suspend fun foregroundPackage(): String?

    /** Current reading for a specific package. Returns zeroed values when access is unavailable. */
    suspend fun readingFor(packageName: String): UsageReading

    /** Total foreground minutes today across [packages]. Returns 0 when access is unavailable. */
    suspend fun todayForegroundMinutes(packages: Set<String>): Int
}
