package com.time.applauncher.goalgaurd.feature.guard.domain

/**
 * Pure doom-scroll detection. Given a single [UsageReading] plus the current [DoomScrollConfig] and
 * a little intervention bookkeeping, decides whether to fire a [GuardTrigger].
 *
 * No Android, no time source, no I/O — the [DoomScrollGuardService] is a thin shell that feeds this
 * real readings; every rule below is exercised directly in unit tests.
 */
class DoomScrollDetector {

    /**
     * @param reading                 latest reading for the foreground app
     * @param config                  current guard settings
     * @param minutesSinceLastTrigger minutes since the last intervention, or null if none yet today
     * @param triggersToday           interventions already shown today
     */
    fun detect(
        reading: UsageReading,
        config: DoomScrollConfig,
        minutesSinceLastTrigger: Int?,
        triggersToday: Int,
    ): GuardTrigger? {
        if (!config.enabled) return null
        if (reading.packageName !in config.monitoredPackages) return null
        if (triggersToday >= config.maxTriggersPerDay) return null
        if (minutesSinceLastTrigger != null && minutesSinceLastTrigger < config.cooldownMinutes) {
            return null
        }

        val reason = when {
            reading.continuousForegroundMinutes >= config.continuousMinutesThreshold ->
                TriggerReason.CONTINUOUS_SCROLL

            reading.reopenCountInWindow >= config.reopenCountThreshold ->
                TriggerReason.REPEATED_REOPENING

            reading.appSwitchCountInWindow >= config.rapidSwitchThreshold ->
                TriggerReason.RAPID_SWITCHING

            else -> return null
        }

        return GuardTrigger(
            packageName = reading.packageName,
            reason = reason,
            minutesScrolled = reading.continuousForegroundMinutes,
        )
    }
}
