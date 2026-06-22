package com.time.applauncher.goalgaurd.feature.goals.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.time.applauncher.goalgaurd.core.designsystem.components.GoalGuardCard
import com.time.applauncher.goalgaurd.core.designsystem.components.GoalProgressRing
import com.time.applauncher.goalgaurd.core.designsystem.theme.BackgroundDeep
import com.time.applauncher.goalgaurd.core.designsystem.theme.GoalGuardTheme
import com.time.applauncher.goalgaurd.core.designsystem.theme.Primary
import com.time.applauncher.goalgaurd.core.designsystem.theme.PrimarySubtle
import com.time.applauncher.goalgaurd.core.designsystem.theme.SuccessLight
import com.time.applauncher.goalgaurd.core.designsystem.theme.Surface
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextMuted
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextPrimary
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextSecondary
import com.time.applauncher.goalgaurd.core.domain.onSuccess
import com.time.applauncher.goalgaurd.feature.goals.domain.Goal
import com.time.applauncher.goalgaurd.feature.goals.domain.GoalPriority
import com.time.applauncher.goalgaurd.feature.goals.domain.GoalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ViewModel
class GoalDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: GoalRepository,
) : ViewModel() {
    private val goalId: String = checkNotNull(savedStateHandle["goalId"])

    private val _state = MutableStateFlow(GoalDetailState())
    val state = _state.asStateFlow()

    init {
        loadGoal()
    }

    private fun loadGoal() {
        viewModelScope.launch {
            repository.getGoal(goalId).onSuccess { goal ->
                _state.update { it.copy(goal = goal) }
            }
        }
    }
}

data class GoalDetailState(val goal: Goal? = null)

// Screen
@Composable
fun GoalDetailRoot(
    goalId: String,
    onNavigateBack: () -> Unit,
    viewModel: GoalDetailViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    GoalDetailScreen(state = state, onNavigateBack = onNavigateBack)
}

@Composable
fun GoalDetailScreen(state: GoalDetailState, onNavigateBack: () -> Unit) {
    val goal = state.goal
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep)
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text("Goal Detail", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        if (goal != null) {
            GoalDetailContent(goal = goal)
        }
    }
}

@Composable
private fun GoalDetailContent(goal: Goal) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        // Header card
        GoalGuardCard(modifier = Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(goal.emoji, fontSize = 48.sp)
                Spacer(Modifier.height(8.dp))
                Text(goal.name, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                Text(
                    "Target: ${goal.targetDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"))}",
                    fontSize = 13.sp,
                    color = TextSecondary,
                )
                Spacer(Modifier.height(20.dp))
                GoalProgressRing(
                    progress = goal.progressFraction,
                    size = 140.dp,
                    strokeWidth = 14.dp,
                    trackColor = BackgroundDeep,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${goal.progressPercent}%",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                        )
                        Text("complete", fontSize = 11.sp, color = TextMuted)
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        // Stats grid
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Current", "₹${formatValue(goal.currentValue)}", SuccessLight, Modifier.weight(1f))
            StatCard("Target", "₹${formatValue(goal.targetValue)}", PrimarySubtle, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Remaining", "₹${formatValue(goal.remainingValue)}", TextSecondary, Modifier.weight(1f))
            StatCard("Days Left", "${goal.daysRemaining}", TextSecondary, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        // Progress bar card
        GoalGuardCard(modifier = Modifier.fillMaxWidth()) {
            Text("Progress", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { goal.progressFraction },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = Primary,
                trackColor = BackgroundDeep,
            )
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("₹${formatValue(goal.currentValue)}", fontSize = 12.sp, color = TextSecondary)
                Text("₹${formatValue(goal.targetValue)}", fontSize = 12.sp, color = TextSecondary)
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun StatCard(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    GoalGuardCard(modifier = modifier) {
        Text(label, fontSize = 11.sp, color = TextMuted)
        Spacer(Modifier.height(4.dp))
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

private fun formatValue(value: Double): String =
    if (value >= 100_000) "${(value / 100_000).toInt()},${((value % 100_000) / 1000).toInt().toString().padStart(2, '0')},000"
    else if (value >= 1_000) "${(value / 1000).toInt()},${(value % 1000).toInt().toString().padStart(3, '0')}"
    else value.toInt().toString()

@Preview
@Composable
private fun GoalDetailScreenPreview() {
    GoalGuardTheme {
        GoalDetailScreen(
            state = GoalDetailState(
                Goal("1", "Buy a House", "🏠", 1500000.0, 540000.0, "₹", LocalDate.of(2028, 12, 1), GoalPriority.HIGH, LocalDate.now())
            ),
            onNavigateBack = {},
        )
    }
}
