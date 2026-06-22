package com.time.applauncher.goalgaurd.feature.guard.domain

/** Why the guard decided to intervene. */
enum class TriggerReason { CONTINUOUS_SCROLL, REPEATED_REOPENING, RAPID_SWITCHING }

/** A decision to show the intervention overlay for [packageName]. */
data class GuardTrigger(
    val packageName: String,
    val reason: TriggerReason,
    val minutesScrolled: Int,
)
