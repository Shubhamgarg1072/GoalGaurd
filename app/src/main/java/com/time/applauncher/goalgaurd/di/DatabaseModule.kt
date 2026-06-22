package com.time.applauncher.goalgaurd.di

import androidx.room.Room
import com.time.applauncher.goalgaurd.core.database.GoalGuardDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            GoalGuardDatabase::class.java,
            GoalGuardDatabase.DATABASE_NAME,
        ).addMigrations(*GoalGuardDatabase.migrations).build()
    }
    single { get<GoalGuardDatabase>().goalDao() }
    single { get<GoalGuardDatabase>().habitDao() }
    single { get<GoalGuardDatabase>().habitLogDao() }
    single { get<GoalGuardDatabase>().focusSessionDao() }
    single { get<GoalGuardDatabase>().backupDao() }
}
