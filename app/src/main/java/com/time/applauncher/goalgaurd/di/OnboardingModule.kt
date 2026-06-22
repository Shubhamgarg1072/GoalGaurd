package com.time.applauncher.goalgaurd.di

import com.time.applauncher.goalgaurd.feature.onboarding.data.DataStoreOnboardingRepository
import com.time.applauncher.goalgaurd.feature.onboarding.domain.OnboardingRepository
import com.time.applauncher.goalgaurd.feature.onboarding.presentation.OnboardingViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val onboardingModule = module {
    single<OnboardingRepository> { DataStoreOnboardingRepository(androidContext()) }
    viewModelOf(::OnboardingViewModel)
}
