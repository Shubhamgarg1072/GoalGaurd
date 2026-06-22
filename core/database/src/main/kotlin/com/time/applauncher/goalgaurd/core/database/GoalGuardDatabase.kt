package com.time.applauncher.goalgaurd.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.time.applauncher.goalgaurd.core.database.converters.DateConverters
import com.time.applauncher.goalgaurd.core.database.dao.BackupDao
import com.time.applauncher.goalgaurd.core.database.dao.FocusSessionDao
import com.time.applauncher.goalgaurd.core.database.dao.GoalDao
import com.time.applauncher.goalgaurd.core.database.dao.HabitDao
import com.time.applauncher.goalgaurd.core.database.dao.HabitLogDao
import com.time.applauncher.goalgaurd.core.database.entity.FocusSessionEntity
import com.time.applauncher.goalgaurd.core.database.entity.GoalEntity
import com.time.applauncher.goalgaurd.core.database.entity.HabitEntity
import com.time.applauncher.goalgaurd.core.database.entity.HabitLogEntity
import com.time.applauncher.goalgaurd.core.database.migrations.MIGRATION_1_2

@Database(
    entities = [
        GoalEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        FocusSessionEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
@TypeConverters(DateConverters::class)
abstract class GoalGuardDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao
    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun backupDao(): BackupDao

    companion object {
        const val DATABASE_NAME = "goalguard.db"
        val migrations = arrayOf(MIGRATION_1_2)
    }
}
