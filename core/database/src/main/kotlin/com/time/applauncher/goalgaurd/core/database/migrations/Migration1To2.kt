package com.time.applauncher.goalgaurd.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add updatedAt to goals; backfill from createdAt (LocalDate → Instant at midnight UTC)
        db.execSQL("ALTER TABLE goals ADD COLUMN updatedAt TEXT NOT NULL DEFAULT ''")
        db.execSQL("UPDATE goals SET updatedAt = createdAt || 'T00:00:00Z'")

        // Add updatedAt to habits; no createdAt column, use current time
        db.execSQL("ALTER TABLE habits ADD COLUMN updatedAt TEXT NOT NULL DEFAULT ''")
        db.execSQL("UPDATE habits SET updatedAt = strftime('%Y-%m-%dT%H:%M:%SZ', 'now')")

        // Add updatedAt to focus_sessions; backfill from startedAt (LocalDateTime → Instant, treat as UTC)
        db.execSQL("ALTER TABLE focus_sessions ADD COLUMN updatedAt TEXT NOT NULL DEFAULT ''")
        db.execSQL("UPDATE focus_sessions SET updatedAt = startedAt || 'Z'")

        // Add unique index on habit_logs(habitId, date) for union-merge during restore
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS index_habit_logs_habitId_date ON habit_logs(habitId, date)"
        )
    }
}
