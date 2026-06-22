package com.time.applauncher.goalgaurd.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.time.applauncher.goalgaurd.feature.goals.domain.Goal
import com.time.applauncher.goalgaurd.feature.goals.domain.GoalRepository
import com.time.applauncher.goalgaurd.feature.habits.domain.Habit
import com.time.applauncher.goalgaurd.feature.habits.domain.HabitRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class DashboardViewModel(
    private val goalRepository: GoalRepository,
    private val habitRepository: HabitRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state = _state
        .onStart { observeData() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardState())

    private val _events = Channel<DashboardEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: DashboardAction) {
        when (action) {
            is DashboardAction.OnGoalClick -> viewModelScope.launch {
                _events.send(DashboardEvent.NavigateToGoalDetail(action.goalId))
            }
            is DashboardAction.OnToggleHabit -> viewModelScope.launch {
                habitRepository.toggleHabitCompletion(action.habitId, LocalDate.now())
            }
            is DashboardAction.OnFocusClick -> viewModelScope.launch {
                _events.send(DashboardEvent.NavigateToFocus)
            }
            is DashboardAction.OnGoalsClick -> viewModelScope.launch {
                _events.send(DashboardEvent.NavigateToGoals)
            }
            is DashboardAction.OnHabitsClick -> viewModelScope.launch {
                _events.send(DashboardEvent.NavigateToHabits)
            }
            is DashboardAction.OnInsightsClick -> viewModelScope.launch {
                _events.send(DashboardEvent.NavigateToInsights)
            }
            is DashboardAction.OnSettingsClick -> viewModelScope.launch {
                _events.send(DashboardEvent.NavigateToSettings)
            }
        }
    }

    private fun observeData() {
        val today = LocalDate.now()
        viewModelScope.launch {
            combine(
                goalRepository.observeGoals(),
                habitRepository.observeActiveHabits(),
                habitRepository.observeCompletedHabitIdsForDate(today),
            ) { goals, habits, completedIds ->
                val completedCount = habits.count { it.id in completedIds }
                val goalScore = calculateGoalScore(goals, completedCount, habits.size)
                _state.value.copy(
                    goals = goals,
                    habits = habits,
                    completedHabitIds = completedIds,
                    goalScore = goalScore,
                    isLoading = false,
                )
            }.collect { _state.value = it }
        }
    }

    private fun calculateGoalScore(goals: List<Goal>, completedToday: Int, totalHabits: Int): Int {
        if (goals.isEmpty() && totalHabits == 0) return 0
        val avgGoalProgress = if (goals.isEmpty()) 0f else goals.map { it.progressFraction }.average().toFloat()
        val habitScore = if (totalHabits == 0) 0f else completedToday.toFloat() / totalHabits
        return ((avgGoalProgress * 0.5f + habitScore * 0.5f) * 100).toInt().coerceIn(0, 100)
    }
}

data class DashboardState(
    val goals: List<Goal> = emptyList(),
    val habits: List<Habit> = emptyList(),
    val completedHabitIds: Set<String> = emptySet(),
    val goalScore: Int = 0,
    val focusMinutesToday: Int = 0,
    val socialMinutesToday: Int = 72,
    val streak: Int = 17,
    val isLoading: Boolean = true,
) {
    val primaryGoal: Goal? get() = goals.firstOrNull()
    val todayCompletedCount: Int get() = habits.count { it.id in completedHabitIds }
}

sealed interface DashboardAction {
    data class OnGoalClick(val goalId: String) : DashboardAction
    data class OnToggleHabit(val habitId: String) : DashboardAction
    data object OnFocusClick : DashboardAction
    data object OnGoalsClick : DashboardAction
    data object OnHabitsClick : DashboardAction
    data object OnInsightsClick : DashboardAction
    data object OnSettingsClick : DashboardAction
}

sealed interface DashboardEvent {
    data class NavigateToGoalDetail(val goalId: String) : DashboardEvent
    data object NavigateToFocus : DashboardEvent
    data object NavigateToGoals : DashboardEvent
    data object NavigateToHabits : DashboardEvent
    data object NavigateToInsights : DashboardEvent
    data object NavigateToSettings : DashboardEvent
}
