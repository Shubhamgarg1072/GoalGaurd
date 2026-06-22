package com.time.applauncher.goalgaurd.di

import com.time.applauncher.goalgaurd.feature.focus.data.RoomFocusRepository
import com.time.applauncher.goalgaurd.feature.focus.domain.FocusRepository
import com.time.applauncher.goalgaurd.feature.focus.presentation.FocusViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val focusModule = module {
    single<FocusRepository> { RoomFocusRepository(get()) }
    viewModelOf(::FocusViewModel)
}
