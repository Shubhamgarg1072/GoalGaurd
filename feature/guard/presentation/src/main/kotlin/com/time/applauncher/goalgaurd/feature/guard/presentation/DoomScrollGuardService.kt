package com.time.applauncher.goalgaurd.feature.guard.presentation

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.time.applauncher.goalgaurd.feature.guard.domain.DoomScrollConfig
import com.time.applauncher.goalgaurd.feature.guard.domain.DoomScrollDetector
import com.time.applauncher.goalgaurd.feature.guard.domain.GuardConfigRepository
import com.time.applauncher.goalgaurd.feature.guard.domain.GuardTrigger
import com.time.applauncher.goalgaurd.feature.guard.domain.UsageStatsReader
import com.time.applauncher.goalgaurd.feature.guard.presentation.overlay.GuardOverlayController
import com.time.applauncher.goalgaurd.feature.guard.presentation.overlay.OverlayContent
import com.time.applauncher.goalgaurd.feature.goals.domain.GoalRepository
import com.time.applauncher.goalgaurd.feature.habits.domain.HabitRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Foreground service that polls [UsageStatsReader] and shows the intervention overlay when the pure
 * [DoomScrollDetector] fires. Deliberate battery behaviour:
 *  - polls at [SHORT_INTERVAL_MS] only while a monitored app is foreground, otherwise [LONG_INTERVAL_MS];
 *  - stops polling entirely while the screen is off (suspends until SCREEN_ON);
 *  - turns itself off if Guard is disabled or required permissions are missing — never crashes.
 */
class DoomScrollGuardService : Service() {

    private val configRepository: GuardConfigRepository by inject()
    private val usageStatsReader: UsageStatsReader by inject()
    private val detector: DoomScrollDetector by inject()
    private val goalRepository: GoalRepository by inject()
    private val habitRepository: HabitRepository by inject()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val overlay by lazy { GuardOverlayController(this) }

    private val screenOn = MutableStateFlow(true)
    private var pollingStarted = false

    private var lastTriggerMs = 0L
    private var triggersToday = 0
    private var triggersDay: LocalDate = LocalDate.now()

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_ON -> screenOn.value = true
                Intent.ACTION_SCREEN_OFF -> screenOn.value = false
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
        registerReceiver(
            screenReceiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
            },
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startAsForeground()
        if (!pollingStarted) {
            pollingStarted = true
            scope.launch { pollLoop() }
        }
        return START_STICKY
    }

    private suspend fun pollLoop() {
        while (scope.isActive) {
            val config = configRepository.currentConfig()

            // Guard turned off, or required access revoked → fail closed and stop.
            if (!config.enabled) {
                stopGuard()
                return
            }
            if (!usageStatsReader.hasUsageAccess() || !overlay.canShow()) {
                delay(LONG_INTERVAL_MS)
                continue
            }

            // Battery: no polling at all while the screen is off.
            if (!screenOn.value) {
                screenOn.first { it }
                continue
            }

            val foreground = usageStatsReader.foregroundPackage()
            if (foreground != null && foreground in config.monitoredPackages && !overlay.isShowing) {
                val reading = usageStatsReader.readingFor(foreground)
                val trigger = detector.detect(
                    reading = reading,
                    config = config,
                    minutesSinceLastTrigger = minutesSinceLastTrigger(),
                    triggersToday = triggersTodayCount(),
                )
                if (trigger != null) intervene(trigger)
                delay(SHORT_INTERVAL_MS)
            } else {
                delay(LONG_INTERVAL_MS)
            }
        }
    }

    private suspend fun intervene(trigger: GuardTrigger) {
        val content = buildOverlayContent(trigger)
        withContext(Dispatchers.Main) {
            overlay.show(
                content = content,
                onStartHabit = {
                    overlay.dismiss()
                    launchHabitFlow()
                },
                onContinueScrolling = {
                    overlay.dismiss()
                    // Snooze: re-arm cooldown so we don't immediately re-trigger.
                    lastTriggerMs = System.currentTimeMillis()
                },
            )
        }
        if (overlay.isShowing) {
            lastTriggerMs = System.currentTimeMillis()
            triggersToday = triggersTodayCount() + 1
        }
    }

    private suspend fun buildOverlayContent(trigger: GuardTrigger): OverlayContent {
        val primaryGoal = goalRepository.observeGoals().first().firstOrNull()
        val habits = habitRepository.observeActiveHabits().first()
        val completed = habitRepository.observeCompletedHabitIdsForDate(LocalDate.now()).first()
        val pending = habits.filter { it.id !in completed }.map { it.name }

        val targetText = primaryGoal?.let {
            "${it.progressPercent}% there · target ${it.targetDate.format(DATE_FORMAT)}"
        }

        return OverlayContent(
            minutesScrolled = trigger.minutesScrolled,
            appLabel = resolveAppLabel(trigger.packageName),
            goalName = primaryGoal?.name ?: "Your goals",
            goalTargetText = targetText,
            pendingHabits = pending,
        )
    }

    private fun resolveAppLabel(packageName: String): String = runCatching {
        val info = packageManager.getApplicationInfo(packageName, 0)
        packageManager.getApplicationLabel(info).toString()
    }.getOrDefault(packageName)

    private fun launchHabitFlow() {
        val launch = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(EXTRA_NAVIGATE, NAVIGATE_HABITS)
        }
        runCatching { launch?.let { startActivity(it) } }
    }

    private fun minutesSinceLastTrigger(): Int? =
        if (lastTriggerMs == 0L) null
        else ((System.currentTimeMillis() - lastTriggerMs) / 60_000L).toInt()

    private fun triggersTodayCount(): Int {
        val today = LocalDate.now()
        if (today != triggersDay) {
            triggersDay = today
            triggersToday = 0
        }
        return triggersToday
    }

    private fun startAsForeground() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIF_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIF_ID, notification)
        }
    }

    private fun stopGuard() {
        overlay.dismiss()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Doom-scroll Guard",
                NotificationManager.IMPORTANCE_LOW,
            ).apply { description = "Keeps watch for doom-scrolling while Guard mode is on." }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification =
        Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Guard mode is on")
            .setContentText("Watching for doom-scrolling on monitored apps")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setOngoing(true)
            .build()

    override fun onDestroy() {
        overlay.dismiss()
        runCatching { unregisterReceiver(screenReceiver) }
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "doom_scroll_guard"
        private const val NOTIF_ID = 4711
        private const val SHORT_INTERVAL_MS = 5_000L
        private const val LONG_INTERVAL_MS = 30_000L

        const val EXTRA_NAVIGATE = "guard_navigate"
        const val NAVIGATE_HABITS = "habits"

        private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM yyyy")

        fun start(context: Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, DoomScrollGuardService::class.java),
            )
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, DoomScrollGuardService::class.java))
        }
    }
}
