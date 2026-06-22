package com.time.applauncher.goalgaurd.cloud

import com.time.applauncher.goalgaurd.core.domain.BackupBundle
import com.time.applauncher.goalgaurd.core.domain.FocusSessionDto
import com.time.applauncher.goalgaurd.core.domain.GoalDto
import com.time.applauncher.goalgaurd.core.domain.HabitDto
import com.time.applauncher.goalgaurd.core.domain.HabitLogDto
import com.time.applauncher.goalgaurd.shared.model.FocusSessionSyncDto
import com.time.applauncher.goalgaurd.shared.model.GoalSyncDto
import com.time.applauncher.goalgaurd.shared.model.HabitLogSyncDto
import com.time.applauncher.goalgaurd.shared.model.HabitSyncDto
import com.time.applauncher.goalgaurd.shared.model.SyncPayload
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toKotlinLocalDateTime
import java.time.Instant

// Bridges the local backup DTOs (java.time) and the wire sync DTOs (kotlinx-datetime).

fun BackupBundle.toSyncPayload(): SyncPayload = SyncPayload(
    goals = goals.map { it.toSync() },
    habits = habits.map { it.toSync() },
    habitLogs = habitLogs.map { it.toSync() },
    focusSessions = focusSessions.map { it.toSync() },
)

fun SyncPayload.toBackupBundle(): BackupBundle = BackupBundle(
    createdAt = Instant.now(),
    goals = goals.map { it.toLocal() },
    habits = habits.map { it.toLocal() },
    habitLogs = habitLogs.map { it.toLocal() },
    focusSessions = focusSessions.map { it.toLocal() },
)

private fun GoalDto.toSync() = GoalSyncDto(
    id, name, emoji, targetValue, currentValue, unit,
    targetDate.toKotlinLocalDate(), priority, createdAt.toKotlinLocalDate(), updatedAt.toKotlinInstant(),
)

private fun GoalSyncDto.toLocal() = GoalDto(
    id, name, emoji, targetValue, currentValue, unit,
    targetDate.toJavaLocalDate(), priority, createdAt.toJavaLocalDate(), updatedAt.toJavaInstant(),
)

private fun HabitDto.toSync() = HabitSyncDto(
    id, goalId, name, emoji, frequency, difficulty, reminderTime, streak, isActive, updatedAt.toKotlinInstant(),
)

private fun HabitSyncDto.toLocal() = HabitDto(
    id, goalId, name, emoji, frequency, difficulty, reminderTime, streak, isActive, updatedAt.toJavaInstant(),
)

private fun HabitLogDto.toSync() = HabitLogSyncDto(id, habitId, date.toKotlinLocalDate(), isCompleted)

private fun HabitLogSyncDto.toLocal() = HabitLogDto(id, habitId, date.toJavaLocalDate(), isCompleted)

private fun FocusSessionDto.toSync() = FocusSessionSyncDto(
    id, durationMinutes, startedAt.toKotlinLocalDateTime(), completedAt?.toKotlinLocalDateTime(),
    isCompleted, updatedAt.toKotlinInstant(),
)

private fun FocusSessionSyncDto.toLocal() = FocusSessionDto(
    id, durationMinutes, startedAt.toJavaLocalDateTime(), completedAt?.toJavaLocalDateTime(),
    isCompleted, updatedAt.toJavaInstant(),
)
