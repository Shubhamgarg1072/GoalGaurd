package com.time.applauncher.goalgaurd.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "habit_logs",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index(value = ["habitId", "date"], unique = true)],
)
data class HabitLogEntity(
    @PrimaryKey val id: String,
    val habitId: String,
    val date: LocalDate,
    val isCompleted: Boolean,
)
