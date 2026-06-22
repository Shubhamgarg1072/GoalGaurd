package com.time.applauncher.goalgaurd.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
    val goalId: String?,
    val name: String,
    val emoji: String,
    val frequency: String,
    val difficulty: String,
    val reminderTime: String?,
    val streak: Int,
    val isActive: Boolean,
    val updatedAt: Instant,
)
