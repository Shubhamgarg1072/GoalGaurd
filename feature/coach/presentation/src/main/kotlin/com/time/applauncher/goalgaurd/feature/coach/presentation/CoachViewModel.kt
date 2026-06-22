package com.time.applauncher.goalgaurd.feature.coach.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.time.applauncher.goalgaurd.core.domain.CoachInput
import com.time.applauncher.goalgaurd.core.domain.CoachMessage
import com.time.applauncher.goalgaurd.core.domain.CoachTextGenerator
import com.time.applauncher.goalgaurd.feature.focus.domain.FocusRepository
import com.time.applauncher.goalgaurd.feature.goals.domain.Goal
import com.time.applauncher.goalgaurd.feature.goals.domain.GoalRepository
import com.time.applauncher.goalgaurd.feature.habits.domain.Habit
import com.time.applauncher.goalgaurd.feature.habits.domain.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class CoachViewModel(
    private val goalRepository: GoalRepository,
    private val habitRepository: HabitRepository,
    private val focusRepository: FocusRepository,
    private val coachTextGenerator: CoachTextGenerator,
    private val screenTimeProvider: ScreenTimeProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(CoachState())
    val state = _state
        .onStart { load() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CoachState())

    fun onAction(action: CoachAction) {
        when (action) {
            CoachAction.OnRetry -> load()
        }
    }

    private fun load() {
        val today = LocalDate.now()
        viewModelScope.launch {
            combine(
                goalRepository.observeGoals(),
                habitRepository.observeActiveHabits(),
                habitRepository.observeCompletedHabitIdsForDate(today),
                focusRepository.observeTodayFocusMinutes(),
            ) { goals, habits, completedIds, focusMinutes ->
                val social = screenTimeProvider.getTodaySocialMinutes()
                buildInput(goals, habits, completedIds, focusMinutes, social, today)
            }.collect { input ->
                val message = coachTextGenerator.generate(input)
                _state.update { it.copy(input = input, message = message, isLoading = false) }
            }
        }
    }

    private fun buildInput(
        goals: List<Goal>,
        habits: List<Habit>,
        completedIds: Set<String>,
        focusMinutes: Int,
        socialMinutes: Int,
        today: LocalDate,
    ): CoachInput {
        val primaryGoal = goals.firstOrNull()
        return CoachInput(
            date = today,
            habitsCompleted = habits.count { it.id in completedIds },
            habitsTotal = habits.size,
            focusMinutes = focusMinutes,
            socialMinutes = socialMinutes,
            primaryGoalName = primaryGoal?.name ?: "your goal",
            primaryGoalPct = primaryGoal?.progressPercent ?: 0,
            daysAheadOrBehind = primaryGoal?.let { scheduleDelta(it, today) } ?: 0,
            topPendingHabit = habits.firstOrNull { it.id !in completedIds }?.name,
            currentStreak = habits.maxOfOrNull { it.streak } ?: 0,
        )
    }

    private fun scheduleDelta(goal: Goal, today: LocalDate): Int {
        val totalDays = ChronoUnit.DAYS.between(goal.createdAt, goal.targetDate)
        if (totalDays <= 0 || goal.targetValue == 0.0) return 0
        val daysElapsed = ChronoUnit.DAYS.between(goal.createdAt, today).coerceAtLeast(0)
        val expectedValue = (daysElapsed.toDouble() / totalDays) * goal.targetValue
        val valueDelta = goal.currentValue - expectedValue
        val dailyRate = goal.targetValue / totalDays
        return (valueDelta / dailyRate).toInt()
    }
}

data class CoachState(
    val isLoading: Boolean = true,
    val input: CoachInput? = null,
    val message: CoachMessage? = null,
)

sealed interface CoachAction {
    data object OnRetry : CoachAction
}
