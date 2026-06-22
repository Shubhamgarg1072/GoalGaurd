package com.time.applauncher.goalgaurd.feature.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.time.applauncher.goalgaurd.feature.goals.domain.Goal
import com.time.applauncher.goalgaurd.feature.goals.domain.GoalPriority
import com.time.applauncher.goalgaurd.feature.goals.domain.GoalRepository
import com.time.applauncher.goalgaurd.feature.onboarding.domain.OnboardingRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

class OnboardingViewModel(
    private val onboardingRepository: OnboardingRepository,
    private val goalRepository: GoalRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state = _state.asStateFlow()

    private val _events = Channel<OnboardingEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: OnboardingAction) {
        when (action) {
            is OnboardingAction.OnNameChange -> _state.update { it.copy(name = action.value) }
            is OnboardingAction.OnAgeChange -> _state.update { it.copy(age = action.value) }
            is OnboardingAction.OnOccupationChange -> _state.update { it.copy(occupation = action.value) }
            is OnboardingAction.OnGoalNameChange -> _state.update { it.copy(goalName = action.value) }
            is OnboardingAction.OnGoalTargetChange -> _state.update { it.copy(goalTarget = action.value) }
            is OnboardingAction.OnGoalCurrentChange -> _state.update { it.copy(goalCurrent = action.value) }
            is OnboardingAction.OnGoalEmojiChange -> _state.update { it.copy(goalEmoji = action.value) }
            is OnboardingAction.OnGoalPriorityChange -> _state.update { it.copy(goalPriority = action.priority) }
            OnboardingAction.OnContinueToGoal -> _state.update { it.copy(step = OnboardingStep.CREATE_GOAL) }
            OnboardingAction.OnBackToProfile -> _state.update { it.copy(step = OnboardingStep.PROFILE) }
            OnboardingAction.OnBackToWelcome -> _state.update { it.copy(step = OnboardingStep.WELCOME) }
            OnboardingAction.OnGetStarted -> _state.update { it.copy(step = OnboardingStep.PROFILE) }
            OnboardingAction.OnFinish -> finishOnboarding()
        }
    }

    private fun finishOnboarding() {
        val s = _state.value
        viewModelScope.launch {
            onboardingRepository.completeOnboarding(
                name = s.name,
                age = s.age.toIntOrNull() ?: 0,
                occupation = s.occupation.ifBlank { null },
            )
            val target = s.goalTarget.toDoubleOrNull()
            if (!s.goalName.isBlank() && target != null) {
                goalRepository.upsertGoal(
                    Goal(
                        id = UUID.randomUUID().toString(),
                        name = s.goalName,
                        emoji = s.goalEmoji,
                        targetValue = target,
                        currentValue = s.goalCurrent.toDoubleOrNull() ?: 0.0,
                        unit = "₹",
                        targetDate = LocalDate.now().plusYears(2),
                        priority = s.goalPriority,
                        createdAt = LocalDate.now(),
                    )
                )
            }
            _events.send(OnboardingEvent.NavigateToDashboard)
        }
    }
}

enum class OnboardingStep { WELCOME, PROFILE, CREATE_GOAL }

data class OnboardingState(
    val step: OnboardingStep = OnboardingStep.WELCOME,
    val name: String = "",
    val age: String = "",
    val occupation: String = "",
    val goalName: String = "",
    val goalEmoji: String = "🎯",
    val goalTarget: String = "",
    val goalCurrent: String = "",
    val goalPriority: GoalPriority = GoalPriority.HIGH,
)

sealed interface OnboardingAction {
    data object OnGetStarted : OnboardingAction
    data object OnContinueToGoal : OnboardingAction
    data object OnBackToProfile : OnboardingAction
    data object OnBackToWelcome : OnboardingAction
    data object OnFinish : OnboardingAction
    data class OnNameChange(val value: String) : OnboardingAction
    data class OnAgeChange(val value: String) : OnboardingAction
    data class OnOccupationChange(val value: String) : OnboardingAction
    data class OnGoalNameChange(val value: String) : OnboardingAction
    data class OnGoalTargetChange(val value: String) : OnboardingAction
    data class OnGoalCurrentChange(val value: String) : OnboardingAction
    data class OnGoalEmojiChange(val value: String) : OnboardingAction
    data class OnGoalPriorityChange(val priority: GoalPriority) : OnboardingAction
}

sealed interface OnboardingEvent {
    data object NavigateToDashboard : OnboardingEvent
}
