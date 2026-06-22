package com.time.applauncher.goalgaurd.di

import com.time.applauncher.goalgaurd.cloud.RemoteCoachTextGenerator
import com.time.applauncher.goalgaurd.core.domain.CoachTextGenerator
import com.time.applauncher.goalgaurd.feature.coach.domain.FallbackCoachTextGenerator
import com.time.applauncher.goalgaurd.feature.coach.domain.TemplateCoachTextGenerator
import com.time.applauncher.goalgaurd.feature.coach.presentation.CoachViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val coachModule = module {
    // Tier-2 server coach is primary; the on-device template is the never-failing fallback.
    single<CoachTextGenerator> {
        FallbackCoachTextGenerator(
            primary = RemoteCoachTextGenerator(api = get()),
            fallback = TemplateCoachTextGenerator(),
        )
    }
    // ScreenTimeProvider is bound by guardModule (GuardScreenTimeProvider). Until Phase 3 it was
    // NoopScreenTimeProvider here.
    viewModelOf(::CoachViewModel)
}
