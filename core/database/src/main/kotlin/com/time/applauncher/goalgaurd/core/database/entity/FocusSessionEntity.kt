package com.time.applauncher.goalgaurd.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDateTime

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey val id: String,
    val durationMinutes: Int,
    val startedAt: LocalDateTime,
    val completedAt: LocalDateTime?,
    val isCompleted: Boolean,
    val updatedAt: Instant,
)
