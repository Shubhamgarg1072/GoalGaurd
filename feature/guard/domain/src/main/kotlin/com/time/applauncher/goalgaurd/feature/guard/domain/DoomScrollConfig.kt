package com.time.applauncher.goalgaurd.feature.guard.domain

/**
 * User-tunable doom-scroll guard settings. Persisted in DataStore by `feature:guard:data`.
 *
 * Defaults mirror the PRD: 15-minute continuous-scroll threshold, with repeated-reopening and
 * rapid-switching as secondary signals. All thresholds are configurable.
 */
data class DoomScrollConfig(
    val enabled: Boolean = false,
    val monitoredPackages: Set<String> = DEFAULT_MONITORED_PACKAGES,
    /** Continuous foreground minutes on a monitored app before intervening. */
    val continuousMinutesThreshold: Int = 15,
    /** Reopenings within the reopening window before intervening. */
    val reopenCountThreshold: Int = 5,
    /** Distinct app switches within the rapid-switch window before intervening. */
    val rapidSwitchThreshold: Int = 8,
    /** Minimum minutes between two interventions (snooze window). */
    val cooldownMinutes: Int = 15,
    /** Hard cap on interventions per calendar day, so the overlay never becomes nagging. */
    val maxTriggersPerDay: Int = 8,
) {
    companion object {
        /** Common attention-economy apps. Users can edit this set. */
        val DEFAULT_MONITORED_PACKAGES: Set<String> = setOf(
            "com.instagram.android",
            "com.zhiliaoapp.musically",   // TikTok
            "com.facebook.katana",
            "com.google.android.youtube",
            "com.twitter.android",
            "com.reddit.frontpage",
            "com.snapchat.android",
            "com.pinterest",
        )
    }
}
