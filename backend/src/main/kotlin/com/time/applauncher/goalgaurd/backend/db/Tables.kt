package com.time.applauncher.goalgaurd.backend.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object Users : Table("users") {
    val id = varchar("id", 64)
    val email = varchar("email", 320).uniqueIndex()
    val displayName = varchar("display_name", 256).nullable()
    val pictureUrl = varchar("picture_url", 1024).nullable()
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}

object RefreshTokens : Table("refresh_tokens") {
    val token = varchar("token", 128)
    val userId = varchar("user_id", 64).index()
    val expiresAt = timestamp("expires_at")
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(token)
}

object Goals : Table("goals") {
    val id = varchar("id", 64)
    val userId = varchar("user_id", 64).index()
    val name = varchar("name", 512)
    val emoji = varchar("emoji", 16)
    val targetValue = double("target_value")
    val currentValue = double("current_value")
    val unit = varchar("unit", 64)
    val targetDate = varchar("target_date", 32)      // ISO LocalDate
    val priority = varchar("priority", 16)
    val createdAt = varchar("created_at", 32)         // ISO LocalDate
    val updatedAt = timestamp("updated_at")
    val deleted = bool("deleted").default(false)
    override val primaryKey = PrimaryKey(id)
}

object Habits : Table("habits") {
    val id = varchar("id", 64)
    val userId = varchar("user_id", 64).index()
    val goalId = varchar("goal_id", 64).nullable()
    val name = varchar("name", 512)
    val emoji = varchar("emoji", 16)
    val frequency = varchar("frequency", 16)
    val difficulty = varchar("difficulty", 16)
    val reminderTime = varchar("reminder_time", 16).nullable()
    val streak = integer("streak")
    val isActive = bool("is_active")
    val updatedAt = timestamp("updated_at")
    val deleted = bool("deleted").default(false)
    override val primaryKey = PrimaryKey(id)
}

object HabitLogs : Table("habit_logs") {
    val id = varchar("id", 64)
    val userId = varchar("user_id", 64).index()
    val habitId = varchar("habit_id", 64)
    val date = varchar("date", 32)                    // ISO LocalDate
    val isCompleted = bool("is_completed")
    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex("uq_habit_log", userId, habitId, date)
    }
}

object FocusSessions : Table("focus_sessions") {
    val id = varchar("id", 64)
    val userId = varchar("user_id", 64).index()
    val durationMinutes = integer("duration_minutes")
    val startedAt = varchar("started_at", 48)         // ISO LocalDateTime
    val completedAt = varchar("completed_at", 48).nullable()
    val isCompleted = bool("is_completed")
    val updatedAt = timestamp("updated_at")
    val deleted = bool("deleted").default(false)
    override val primaryKey = PrimaryKey(id)
}
