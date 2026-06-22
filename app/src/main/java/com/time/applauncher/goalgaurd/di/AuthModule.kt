package com.time.applauncher.goalgaurd.di

import com.time.applauncher.goalgaurd.cloud.SyncRepository
import com.time.applauncher.goalgaurd.feature.auth.data.GoogleAuthRepository
import com.time.applauncher.goalgaurd.feature.auth.domain.AuthRepository
import com.time.applauncher.goalgaurd.feature.auth.presentation.SignInViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authModule = module {
    single<AuthRepository> { GoogleAuthRepository(api = get(), tokenStore = get()) }
    single { SyncRepository(api = get(), backupRepository = get()) }
    viewModelOf(::SignInViewModel)
}
