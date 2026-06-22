package com.time.applauncher.goalgaurd.shared.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

// Wire DTOs for delta sync. kotlinx-datetime types serialize as ISO-8601 strings.
// A `deleted` tombstone lets last-write-wins propagate deletions.

@Serializable
data class GoalSyncDto(
    val id: String,
    val name: String,
    val emoji: String,
    val targetValue: Double,
    val currentValue: Double,
    val unit: String,
    val targetDate: LocalDate,
    val priority: String,
    val createdAt: LocalDate,
    val updatedAt: Instant,
    val deleted: Boolean = false,
)

@Serializable
data class HabitSyncDto(
    val id: String,
    val goalId: String? = null,
    val name: String,
    val emoji: String,
    val frequency: String,
    val difficulty: String,
    val reminderTime: String? = null,
    val streak: Int,
    val isActive: Boolean,
    val updatedAt: Instant,
    val deleted: Boolean = false,
)

@Serializable
data class HabitLogSyncDto(
    val id: String,
    val habitId: String,
    val date: LocalDate,
    val isCompleted: Boolean,
)

@Serializable
data class FocusSessionSyncDto(
    val id: String,
    val durationMinutes: Int,
    val startedAt: LocalDateTime,
    val completedAt: LocalDateTime? = null,
    val isCompleted: Boolean,
    val updatedAt: Instant,
    val deleted: Boolean = false,
)

@Serializable
data class SyncPayload(
    val goals: List<GoalSyncDto> = emptyList(),
    val habits: List<HabitSyncDto> = emptyList(),
    val habitLogs: List<HabitLogSyncDto> = emptyList(),
    val focusSessions: List<FocusSessionSyncDto> = emptyList(),
)

/** [since] = the client's last successful sync time; null = first sync (full push). */
@Serializable
data class SyncRequest(
    val since: Instant? = null,
    val changes: SyncPayload = SyncPayload(),
)

/** [changes] = rows the server knows that are newer than [SyncRequest.since]. */
@Serializable
data class SyncResponse(
    val serverTime: Instant,
    val changes: SyncPayload = SyncPayload(),
)
