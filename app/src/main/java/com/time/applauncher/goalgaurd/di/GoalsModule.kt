package com.time.applauncher.goalgaurd.di

import com.time.applauncher.goalgaurd.feature.goals.data.RoomGoalRepository
import com.time.applauncher.goalgaurd.feature.goals.domain.GoalRepository
import com.time.applauncher.goalgaurd.feature.goals.presentation.GoalDetailViewModel
import com.time.applauncher.goalgaurd.feature.goals.presentation.GoalsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val goalsModule = module {
    single<GoalRepository> { RoomGoalRepository(get()) }
    viewModelOf(::GoalsViewModel)
    viewModelOf(::GoalDetailViewModel)
}
