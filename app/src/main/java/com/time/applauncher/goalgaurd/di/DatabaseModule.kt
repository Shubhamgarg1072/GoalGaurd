package com.time.applauncher.goalgaurd.di

import androidx.room.Room
import com.time.applauncher.goalgaurd.core.crypto.VaultKeyManager
import com.time.applauncher.goalgaurd.core.database.GoalGuardDatabase
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        // The Room database is encrypted at rest with SQLCipher, keyed by the vault DEK. It is only
        // resolved after the unlock gate (see MainActivity), so the DEK is present here; if it is
        // not, requireDek() fails fast rather than opening an unencrypted database.
        val dek = get<VaultKeyManager>().requireDek()
        System.loadLibrary("sqlcipher")
        val factory = SupportOpenHelperFactory(dek)

        Room.databaseBuilder(
            androidContext(),
            GoalGuardDatabase::class.java,
            GoalGuardDatabase.DATABASE_NAME,
        )
            .openHelperFactory(factory)
            .addMigrations(*GoalGuardDatabase.migrations)
            .build()
    }
    single { get<GoalGuardDatabase>().goalDao() }
    single { get<GoalGuardDatabase>().habitDao() }
    single { get<GoalGuardDatabase>().habitLogDao() }
    single { get<GoalGuardDatabase>().focusSessionDao() }
    single { get<GoalGuardDatabase>().backupDao() }
}
