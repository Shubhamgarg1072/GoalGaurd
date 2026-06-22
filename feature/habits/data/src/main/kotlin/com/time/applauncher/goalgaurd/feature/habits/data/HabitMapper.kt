package com.time.applauncher.goalgaurd.feature.habits.data

import com.time.applauncher.goalgaurd.core.database.entity.HabitEntity
import com.time.applauncher.goalgaurd.feature.habits.domain.Habit
import com.time.applauncher.goalgaurd.feature.habits.domain.HabitDifficulty
import com.time.applauncher.goalgaurd.feature.habits.domain.HabitFrequency
import java.time.Instant

fun HabitEntity.toHabit(): Habit = Habit(
    id = id,
    goalId = goalId,
    name = name,
    emoji = emoji,
    frequency = HabitFrequency.valueOf(frequency),
    difficulty = HabitDifficulty.valueOf(difficulty),
    reminderTime = reminderTime,
    streak = streak,
    isActive = isActive,
)

fun Habit.toHabitEntity(): HabitEntity = HabitEntity(
    id = id,
    goalId = goalId,
    name = name,
    emoji = emoji,
    frequency = frequency.name,
    difficulty = difficulty.name,
    reminderTime = reminderTime,
    streak = streak,
    isActive = isActive,
    updatedAt = Instant.now(),
)
