package com.time.applauncher.goalgaurd.feature.habits.domain

data class Habit(
    val id: String,
    val goalId: String?,
    val name: String,
    val emoji: String,
    val frequency: HabitFrequency,
    val difficulty: HabitDifficulty,
    val reminderTime: String?,
    val streak: Int,
    val isActive: Boolean,
)

enum class HabitFrequency { DAILY, WEEKLY, MONTHLY }
enum class HabitDifficulty { EASY, MEDIUM, HARD }
