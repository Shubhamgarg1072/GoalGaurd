package com.time.applauncher.goalgaurd.di

import com.time.applauncher.goalgaurd.feature.coach.presentation.ScreenTimeProvider
import com.time.applauncher.goalgaurd.feature.guard.data.AndroidUsageStatsReader
import com.time.applauncher.goalgaurd.feature.guard.data.DataStoreGuardConfigRepository
import com.time.applauncher.goalgaurd.feature.guard.data.GuardScreenTimeProvider
import com.time.applauncher.goalgaurd.feature.guard.domain.DoomScrollDetector
import com.time.applauncher.goalgaurd.feature.guard.domain.GuardConfigRepository
import com.time.applauncher.goalgaurd.feature.guard.domain.UsageStatsReader
import com.time.applauncher.goalgaurd.feature.guard.presentation.GuardViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val guardModule = module {
    single<UsageStatsReader> { AndroidUsageStatsReader(androidContext()) }
    single<GuardConfigRepository> { DataStoreGuardConfigRepository(androidContext()) }
    single { DoomScrollDetector() }

    // Phase 3 supplies the real ScreenTimeProvider that Phase 1 stubbed with NoopScreenTimeProvider.
    single<ScreenTimeProvider> { GuardScreenTimeProvider(get(), get()) }

    viewModelOf(::GuardViewModel)
}
