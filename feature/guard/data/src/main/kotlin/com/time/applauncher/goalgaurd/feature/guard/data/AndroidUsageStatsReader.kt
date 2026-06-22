package com.time.applauncher.goalgaurd.feature.guard.data

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Process
import com.time.applauncher.goalgaurd.feature.guard.domain.UsageReading
import com.time.applauncher.goalgaurd.feature.guard.domain.UsageStatsReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * [UsageStatsReader] backed by Android's `UsageStatsManager`. Every method degrades to a safe,
 * empty result when PACKAGE_USAGE_STATS is not granted — the guard simply stays off, never crashes.
 */
class AndroidUsageStatsReader(private val context: Context) : UsageStatsReader {

    private val usageStatsManager: UsageStatsManager
        get() = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    override fun hasUsageAccess(): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName,
            )
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun foregroundPackage(): String? = withContext(Dispatchers.IO) {
        if (!hasUsageAccess()) return@withContext null
        runCatching {
            val now = System.currentTimeMillis()
            val events = usageStatsManager.queryEvents(now - RECENT_WINDOW_MS, now)
            var lastForeground: String? = null
            val event = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    lastForeground = event.packageName
                }
            }
            lastForeground
        }.getOrNull()
    }

    override suspend fun readingFor(packageName: String): UsageReading = withContext(Dispatchers.IO) {
        if (!hasUsageAccess()) return@withContext emptyReading(packageName)
        runCatching {
            val now = System.currentTimeMillis()
            val events = usageStatsManager.queryEvents(now - REOPEN_WINDOW_MS, now)

            var lastForegroundPkg: String? = null
            var currentForegroundSince = 0L
            var reopenCount = 0
            var switchCount = 0

            val event = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                if (event.eventType != UsageEvents.Event.MOVE_TO_FOREGROUND) continue

                // Any foreground transition counts as an app switch within the rapid-switch window.
                if (now - event.timeStamp <= RAPID_SWITCH_WINDOW_MS) switchCount++

                // Track reopenings of the target package within the reopening window.
                if (event.packageName == packageName) reopenCount++

                // Continuous-foreground start = last time the foreground app changed to this package.
                if (event.packageName != lastForegroundPkg) {
                    currentForegroundSince = event.timeStamp
                }
                lastForegroundPkg = event.packageName
            }

            val continuousMinutes = if (lastForegroundPkg == packageName && currentForegroundSince > 0) {
                TimeUnit.MILLISECONDS.toMinutes(now - currentForegroundSince).toInt()
            } else {
                0
            }

            UsageReading(
                packageName = packageName,
                continuousForegroundMinutes = continuousMinutes,
                reopenCountInWindow = reopenCount,
                appSwitchCountInWindow = switchCount,
            )
        }.getOrDefault(emptyReading(packageName))
    }

    override suspend fun todayForegroundMinutes(packages: Set<String>): Int = withContext(Dispatchers.IO) {
        if (!hasUsageAccess() || packages.isEmpty()) return@withContext 0
        runCatching {
            val startOfDay = LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            val now = System.currentTimeMillis()
            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startOfDay,
                now,
            ) ?: return@runCatching 0

            val totalMs = stats
                .filter { it.packageName in packages }
                .sumOf { it.totalTimeInForeground }
            TimeUnit.MILLISECONDS.toMinutes(totalMs).toInt()
        }.getOrDefault(0)
    }

    private fun emptyReading(packageName: String) = UsageReading(
        packageName = packageName,
        continuousForegroundMinutes = 0,
        reopenCountInWindow = 0,
        appSwitchCountInWindow = 0,
    )

    private companion object {
        val RECENT_WINDOW_MS = TimeUnit.MINUTES.toMillis(10)
        val REOPEN_WINDOW_MS = TimeUnit.MINUTES.toMillis(60)
        val RAPID_SWITCH_WINDOW_MS = TimeUnit.MINUTES.toMillis(2)
    }
}
