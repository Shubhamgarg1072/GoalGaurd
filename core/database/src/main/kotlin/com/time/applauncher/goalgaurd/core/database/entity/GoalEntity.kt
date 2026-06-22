package com.time.applauncher.goalgaurd.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String,
    val name: String,
    val emoji: String,
    val targetValue: Double,
    val currentValue: Double,
    val unit: String,
    val targetDate: LocalDate,
    val priority: String,
    val createdAt: LocalDate,
    val updatedAt: Instant,
)
