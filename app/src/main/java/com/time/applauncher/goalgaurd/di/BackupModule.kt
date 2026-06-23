package com.time.applauncher.goalgaurd.di

import com.time.applauncher.goalgaurd.core.backup.RoomBackupRepository
import com.time.applauncher.goalgaurd.core.domain.BackupRepository
import com.time.applauncher.goalgaurd.feature.backup.presentation.BackupViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val backupModule = module {
    single<BackupRepository> {
        RoomBackupRepository(
            db = get(),
            backupDao = get(),
            goalDao = get(),
            habitDao = get(),
            habitLogDao = get(),
            focusSessionDao = get(),
            vaultKeyManager = get(),
        )
    }
    viewModel { BackupViewModel(get(), androidContext()) }
}
