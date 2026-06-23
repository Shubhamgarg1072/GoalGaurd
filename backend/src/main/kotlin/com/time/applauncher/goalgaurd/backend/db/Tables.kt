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

// End-to-end-encrypted storage. Content lives entirely in the opaque `blob` (AES-GCM ciphertext);
// the server keeps only the merge metadata it needs and can never read the user's data.

object Goals : Table("goals") {
    val id = varchar("id", 64)
    val userId = varchar("user_id", 64).index()
    val blob = text("blob")                           // encrypted content
    val updatedAt = timestamp("updated_at")
    val deleted = bool("deleted").default(false)
    override val primaryKey = PrimaryKey(id)
}

object Habits : Table("habits") {
    val id = varchar("id", 64)
    val userId = varchar("user_id", 64).index()
    val blob = text("blob")                           // encrypted content
    val updatedAt = timestamp("updated_at")
    val deleted = bool("deleted").default(false)
    override val primaryKey = PrimaryKey(id)
}

object HabitLogs : Table("habit_logs") {
    val id = varchar("id", 64)
    val userId = varchar("user_id", 64).index()
    val dedupeKey = varchar("dedupe_key", 64)         // blind index = HMAC(indexKey, habitId|date)
    val blob = text("blob")                           // encrypted content
    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex("uq_habit_log", userId, dedupeKey)
    }
}

object FocusSessions : Table("focus_sessions") {
    val id = varchar("id", 64)
    val userId = varchar("user_id", 64).index()
    val blob = text("blob")                           // encrypted content
    val updatedAt = timestamp("updated_at")
    val deleted = bool("deleted").default(false)
    override val primaryKey = PrimaryKey(id)
}
