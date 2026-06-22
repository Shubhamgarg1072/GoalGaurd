package com.time.applauncher.goalgaurd.di

import com.time.applauncher.goalgaurd.feature.habits.data.RoomHabitRepository
import com.time.applauncher.goalgaurd.feature.habits.domain.HabitRepository
import com.time.applauncher.goalgaurd.feature.habits.presentation.HabitsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val habitsModule = module {
    single<HabitRepository> { RoomHabitRepository(get(), get()) }
    viewModelOf(::HabitsViewModel)
}
