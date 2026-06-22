package com.time.applauncher.goalgaurd.feature.habits.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.time.applauncher.goalgaurd.feature.habits.domain.Habit
import com.time.applauncher.goalgaurd.feature.habits.domain.HabitDifficulty
import com.time.applauncher.goalgaurd.feature.habits.domain.HabitFrequency
import com.time.applauncher.goalgaurd.feature.habits.domain.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

class HabitsViewModel(private val repository: HabitRepository) : ViewModel() {

    private val _state = MutableStateFlow(HabitsState())
    val state = _state
        .onStart { observeHabits() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HabitsState())

    fun onAction(action: HabitsAction) {
        when (action) {
            is HabitsAction.OnTabSelect -> _state.update { it.copy(selectedTab = action.tab) }
            is HabitsAction.OnToggleHabit -> toggleHabit(action.habitId)
            is HabitsAction.OnAddHabitClick -> _state.update { it.copy(showAddSheet = true) }
            is HabitsAction.OnDismissAddSheet -> _state.update { it.copy(showAddSheet = false) }
            is HabitsAction.OnNewHabitNameChange -> _state.update { it.copy(newHabitName = action.value) }
            is HabitsAction.OnNewHabitFrequencyChange -> _state.update { it.copy(newHabitFrequency = action.frequency) }
            is HabitsAction.OnSaveHabit -> saveHabit()
            is HabitsAction.OnDeleteHabit -> viewModelScope.launch { repository.deleteHabit(action.habitId) }
        }
    }

    private fun observeHabits() {
        val today = LocalDate.now()
        viewModelScope.launch {
            combine(
                repository.observeActiveHabits(),
                repository.observeCompletedHabitIdsForDate(today),
            ) { habits, completedIds ->
                HabitsState(
                    habits = habits,
                    completedHabitIds = completedIds,
                    isLoading = false,
                    selectedTab = _state.value.selectedTab,
                    showAddSheet = _state.value.showAddSheet,
                    newHabitName = _state.value.newHabitName,
                    newHabitFrequency = _state.value.newHabitFrequency,
                )
            }.collect { newState -> _state.value = newState }
        }
    }

    private fun toggleHabit(habitId: String) {
        viewModelScope.launch {
            repository.toggleHabitCompletion(habitId, LocalDate.now())
        }
    }

    private fun saveHabit() {
        val name = _state.value.newHabitName.trim()
        if (name.isBlank()) return
        val habit = Habit(
            id = UUID.randomUUID().toString(),
            goalId = null,
            name = name,
            emoji = "⚡",
            frequency = _state.value.newHabitFrequency,
            difficulty = HabitDifficulty.MEDIUM,
            reminderTime = null,
            streak = 0,
            isActive = true,
        )
        viewModelScope.launch {
            repository.upsertHabit(habit)
            _state.update { it.copy(showAddSheet = false, newHabitName = "") }
        }
    }
}

data class HabitsState(
    val habits: List<Habit> = emptyList(),
    val completedHabitIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val selectedTab: HabitFrequency = HabitFrequency.DAILY,
    val showAddSheet: Boolean = false,
    val newHabitName: String = "",
    val newHabitFrequency: HabitFrequency = HabitFrequency.DAILY,
) {
    val filteredHabits: List<Habit> get() = habits.filter { it.frequency == selectedTab }
    val todayCompletedCount: Int get() = habits.count { it.id in completedHabitIds }
}

sealed interface HabitsAction {
    data class OnTabSelect(val tab: HabitFrequency) : HabitsAction
    data class OnToggleHabit(val habitId: String) : HabitsAction
    data object OnAddHabitClick : HabitsAction
    data object OnDismissAddSheet : HabitsAction
    data class OnNewHabitNameChange(val value: String) : HabitsAction
    data class OnNewHabitFrequencyChange(val frequency: HabitFrequency) : HabitsAction
    data object OnSaveHabit : HabitsAction
    data class OnDeleteHabit(val habitId: String) : HabitsAction
}
