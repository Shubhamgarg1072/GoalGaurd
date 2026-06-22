package com.time.applauncher.goalgaurd.shared.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class CoachInputDto(
    val date: LocalDate,
    val habitsCompleted: Int,
    val habitsTotal: Int,
    val focusMinutes: Int,
    val socialMinutes: Int,
    val primaryGoalName: String,
    val primaryGoalPct: Int,
    val daysAheadOrBehind: Int,
    val topPendingHabit: String? = null,
    val currentStreak: Int,
)

@Serializable
enum class CoachToneDto { CELEBRATORY, ENCOURAGING, NEUTRAL, GENTLE_NUDGE }

@Serializable
data class CoachMessageDto(
    val headline: String,
    val body: String,
    val tone: CoachToneDto,
)
