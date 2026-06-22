package com.time.applauncher.goalgaurd.core.domain

import java.time.LocalDate

data class CoachInput(
    val date: LocalDate,
    val habitsCompleted: Int,
    val habitsTotal: Int,
    val focusMinutes: Int,
    val socialMinutes: Int,
    val primaryGoalName: String,
    val primaryGoalPct: Int,
    val daysAheadOrBehind: Int,
    val topPendingHabit: String?,
    val currentStreak: Int,
)
