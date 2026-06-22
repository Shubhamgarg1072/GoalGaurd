package com.time.applauncher.goalgaurd.feature.goals.domain

import java.time.LocalDate

data class Goal(
    val id: String,
    val name: String,
    val emoji: String,
    val targetValue: Double,
    val currentValue: Double,
    val unit: String,
    val targetDate: LocalDate,
    val priority: GoalPriority,
    val createdAt: LocalDate,
) {
    val progressFraction: Float
        get() = if (targetValue == 0.0) 0f else (currentValue / targetValue).toFloat().coerceIn(0f, 1f)

    val progressPercent: Int get() = (progressFraction * 100).toInt()

    val remainingValue: Double get() = (targetValue - currentValue).coerceAtLeast(0.0)

    val daysRemaining: Long
        get() = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), targetDate).coerceAtLeast(0)
}

enum class GoalPriority { HIGH, MEDIUM, LOW }
