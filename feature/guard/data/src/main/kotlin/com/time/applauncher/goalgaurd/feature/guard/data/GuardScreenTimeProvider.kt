package com.time.applauncher.goalgaurd.feature.guard.data

import com.time.applauncher.goalgaurd.feature.coach.presentation.ScreenTimeProvider
import com.time.applauncher.goalgaurd.feature.guard.domain.GuardConfigRepository
import com.time.applauncher.goalgaurd.feature.guard.domain.UsageStatsReader

/**
 * The real [ScreenTimeProvider] that Phase 1 stubbed with `NoopScreenTimeProvider`. Sums today's
 * foreground minutes across the user's monitored social apps via [UsageStatsReader].
 *
 * Degrades gracefully: if usage access is denied the reader returns 0, so the coach summary just
 * shows 0 social minutes rather than failing.
 */
class GuardScreenTimeProvider(
    private val usageStatsReader: UsageStatsReader,
    private val configRepository: GuardConfigRepository,
) : ScreenTimeProvider {

    override suspend fun getTodaySocialMinutes(): Int {
        val packages = configRepository.currentConfig().monitoredPackages
        return usageStatsReader.todayForegroundMinutes(packages)
    }
}
