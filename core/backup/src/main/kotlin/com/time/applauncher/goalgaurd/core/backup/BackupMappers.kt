package com.time.applauncher.goalgaurd.core.backup

import com.time.applauncher.goalgaurd.core.database.entity.FocusSessionEntity
import com.time.applauncher.goalgaurd.core.database.entity.GoalEntity
import com.time.applauncher.goalgaurd.core.database.entity.HabitEntity
import com.time.applauncher.goalgaurd.core.database.entity.HabitLogEntity
import com.time.applauncher.goalgaurd.core.domain.FocusSessionDto
import com.time.applauncher.goalgaurd.core.domain.GoalDto
import com.time.applauncher.goalgaurd.core.domain.HabitDto
import com.time.applauncher.goalgaurd.core.domain.HabitLogDto

// ── Entity → DTO ─────────────────────────────────────────────────────────────

fun GoalEntity.toDto() = GoalDto(
    id = id,
    name = name,
    emoji = emoji,
    targetValue = targetValue,
    currentValue = currentValue,
    unit = unit,
    targetDate = targetDate,
    priority = priority,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun HabitEntity.toDto() = HabitDto(
    id = id,
    goalId = goalId,
    name = name,
    emoji = emoji,
    frequency = frequency,
    difficulty = difficulty,
    reminderTime = reminderTime,
    streak = streak,
    isActive = isActive,
    updatedAt = updatedAt,
)

fun HabitLogEntity.toDto() = HabitLogDto(
    id = id,
    habitId = habitId,
    date = date,
    isCompleted = isCompleted,
)

fun FocusSessionEntity.toDto() = FocusSessionDto(
    id = id,
    durationMinutes = durationMinutes,
    startedAt = startedAt,
    completedAt = completedAt,
    isCompleted = isCompleted,
    updatedAt = updatedAt,
)

// ── DTO → Entity ─────────────────────────────────────────────────────────────

fun GoalDto.toEntity() = GoalEntity(
    id = id,
    name = name,
    emoji = emoji,
    targetValue = targetValue,
    currentValue = currentValue,
    unit = unit,
    targetDate = targetDate,
    priority = priority,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun HabitDto.toEntity() = HabitEntity(
    id = id,
    goalId = goalId,
    name = name,
    emoji = emoji,
    frequency = frequency,
    difficulty = difficulty,
    reminderTime = reminderTime,
    streak = streak,
    isActive = isActive,
    updatedAt = updatedAt,
)

fun HabitLogDto.toEntity() = HabitLogEntity(
    id = id,
    habitId = habitId,
    date = date,
    isCompleted = isCompleted,
)

fun FocusSessionDto.toEntity() = FocusSessionEntity(
    id = id,
    durationMinutes = durationMinutes,
    startedAt = startedAt,
    completedAt = completedAt,
    isCompleted = isCompleted,
    updatedAt = updatedAt,
)
