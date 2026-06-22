package com.time.applauncher.goalgaurd.feature.goals.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.time.applauncher.goalgaurd.core.domain.onFailure
import com.time.applauncher.goalgaurd.core.domain.onSuccess
import com.time.applauncher.goalgaurd.core.presentation.UiText
import com.time.applauncher.goalgaurd.feature.goals.domain.Goal
import com.time.applauncher.goalgaurd.feature.goals.domain.GoalPriority
import com.time.applauncher.goalgaurd.feature.goals.domain.GoalRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

class GoalsViewModel(private val repository: GoalRepository) : ViewModel() {

    private val _state = MutableStateFlow(GoalsState())
    val state = _state
        .onStart { observeGoals() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GoalsState())

    private val _events = Channel<GoalsEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: GoalsAction) {
        when (action) {
            is GoalsAction.OnGoalClick -> viewModelScope.launch {
                _events.send(GoalsEvent.NavigateToDetail(action.goalId))
            }
            is GoalsAction.OnAddGoalClick -> _state.update { it.copy(showAddGoalSheet = true) }
            is GoalsAction.OnDismissAddGoal -> _state.update {
                it.copy(showAddGoalSheet = false, newGoalName = "", newGoalTarget = "", newGoalCurrent = "", newGoalUnit = "₹", newGoalEmoji = "🎯")
            }
            is GoalsAction.OnNewGoalNameChange -> _state.update { it.copy(newGoalName = action.value) }
            is GoalsAction.OnNewGoalTargetChange -> _state.update { it.copy(newGoalTarget = action.value) }
            is GoalsAction.OnNewGoalCurrentChange -> _state.update { it.copy(newGoalCurrent = action.value) }
            is GoalsAction.OnNewGoalUnitChange -> _state.update { it.copy(newGoalUnit = action.value) }
            is GoalsAction.OnNewGoalEmojiChange -> _state.update { it.copy(newGoalEmoji = action.value) }
            is GoalsAction.OnNewGoalPriorityChange -> _state.update { it.copy(newGoalPriority = action.priority) }
            is GoalsAction.OnSaveGoal -> saveGoal()
            is GoalsAction.OnDeleteGoal -> deleteGoal(action.goalId)
        }
    }

    private fun observeGoals() {
        viewModelScope.launch {
            repository.observeGoals().collect { goals ->
                _state.update { it.copy(goals = goals, isLoading = false) }
            }
        }
    }

    private fun saveGoal() {
        val s = _state.value
        val target = s.newGoalTarget.toDoubleOrNull() ?: return
        val current = s.newGoalCurrent.toDoubleOrNull() ?: 0.0
        val goal = Goal(
            id = UUID.randomUUID().toString(),
            name = s.newGoalName.trim(),
            emoji = s.newGoalEmoji,
            targetValue = target,
            currentValue = current,
            unit = s.newGoalUnit,
            targetDate = LocalDate.now().plusYears(2),
            priority = s.newGoalPriority,
            createdAt = LocalDate.now(),
        )
        viewModelScope.launch {
            repository.upsertGoal(goal)
                .onSuccess { _state.update { it.copy(showAddGoalSheet = false) } }
                .onFailure { _state.update { it.copy(error = UiText.DynamicString("Failed to save goal")) } }
        }
    }

    private fun deleteGoal(id: String) {
        viewModelScope.launch {
            repository.deleteGoal(id)
                .onFailure { _state.update { it.copy(error = UiText.DynamicString("Failed to delete goal")) } }
        }
    }
}

data class GoalsState(
    val goals: List<Goal> = emptyList(),
    val isLoading: Boolean = true,
    val error: UiText? = null,
    val showAddGoalSheet: Boolean = false,
    val newGoalName: String = "",
    val newGoalEmoji: String = "🎯",
    val newGoalTarget: String = "",
    val newGoalCurrent: String = "",
    val newGoalUnit: String = "₹",
    val newGoalPriority: GoalPriority = GoalPriority.HIGH,
)

sealed interface GoalsAction {
    data class OnGoalClick(val goalId: String) : GoalsAction
    data object OnAddGoalClick : GoalsAction
    data object OnDismissAddGoal : GoalsAction
    data class OnNewGoalNameChange(val value: String) : GoalsAction
    data class OnNewGoalTargetChange(val value: String) : GoalsAction
    data class OnNewGoalCurrentChange(val value: String) : GoalsAction
    data class OnNewGoalUnitChange(val value: String) : GoalsAction
    data class OnNewGoalEmojiChange(val value: String) : GoalsAction
    data class OnNewGoalPriorityChange(val priority: GoalPriority) : GoalsAction
    data object OnSaveGoal : GoalsAction
    data class OnDeleteGoal(val goalId: String) : GoalsAction
}

sealed interface GoalsEvent {
    data class NavigateToDetail(val goalId: String) : GoalsEvent
}
