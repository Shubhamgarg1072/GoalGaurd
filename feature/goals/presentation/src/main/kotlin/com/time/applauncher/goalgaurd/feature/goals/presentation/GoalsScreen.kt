package com.time.applauncher.goalgaurd.feature.goals.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.time.applauncher.goalgaurd.core.designsystem.components.GoalGuardCard
import com.time.applauncher.goalgaurd.core.designsystem.components.GoalGuardPrimaryButton
import com.time.applauncher.goalgaurd.core.designsystem.theme.BackgroundDeep
import com.time.applauncher.goalgaurd.core.designsystem.theme.BorderSubtle
import com.time.applauncher.goalgaurd.core.designsystem.theme.GoalGuardTheme
import com.time.applauncher.goalgaurd.core.designsystem.theme.Primary
import com.time.applauncher.goalgaurd.core.designsystem.theme.PrimarySubtle
import com.time.applauncher.goalgaurd.core.designsystem.theme.Success
import com.time.applauncher.goalgaurd.core.designsystem.theme.SuccessLight
import com.time.applauncher.goalgaurd.core.designsystem.theme.Surface
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextMuted
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextPrimary
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextSecondary
import com.time.applauncher.goalgaurd.core.presentation.ObserveAsEvents
import com.time.applauncher.goalgaurd.feature.goals.domain.Goal
import com.time.applauncher.goalgaurd.feature.goals.domain.GoalPriority
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun GoalsRoot(
    onNavigateToDetail: (String) -> Unit,
    viewModel: GoalsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is GoalsEvent.NavigateToDetail -> onNavigateToDetail(event.goalId)
        }
    }
    GoalsScreen(state = state, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    state: GoalsState,
    onAction: (GoalsAction) -> Unit,
) {
    Scaffold(
        containerColor = BackgroundDeep,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAction(GoalsAction.OnAddGoalClick) },
                containerColor = Primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add goal")
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Spacer(Modifier.height(24.dp))
                Text("My Goals", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                GoalsSummaryRow(state.goals)
                Spacer(Modifier.height(8.dp))
            }
            items(state.goals, key = { it.id }) { goal ->
                GoalCard(goal = goal, onClick = { onAction(GoalsAction.OnGoalClick(goal.id)) })
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (state.showAddGoalSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { onAction(GoalsAction.OnDismissAddGoal) },
            sheetState = sheetState,
            containerColor = Surface,
        ) {
            AddGoalSheet(state = state, onAction = onAction)
        }
    }
}

@Composable
private fun GoalsSummaryRow(goals: List<Goal>) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        SummaryChip("${goals.size}", "Active", Modifier.weight(1f))
        SummaryChip(
            "${if (goals.isEmpty()) 0 else goals.map { it.progressPercent }.average().toInt()}%",
            "Avg Progress",
            Modifier.weight(1f),
            valueColor = SuccessLight,
        )
    }
}

@Composable
private fun SummaryChip(value: String, label: String, modifier: Modifier, valueColor: Color = PrimarySubtle) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = valueColor)
            Text(label, fontSize = 11.sp, color = TextSecondary)
        }
    }
}

@Composable
fun GoalCard(goal: Goal, onClick: () -> Unit, modifier: Modifier = Modifier) {
    GoalGuardCard(modifier = modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(goal.emoji, fontSize = 28.sp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(goal.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(
                    "${goal.targetDate.format(DateTimeFormatter.ofPattern("MMM yyyy"))} · ${goal.daysRemaining} days left",
                    fontSize = 11.sp,
                    color = TextSecondary,
                )
            }
            Text(
                "${goal.progressPercent}%",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PrimarySubtle,
            )
        }
        Spacer(Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { goal.progressFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = Primary,
            trackColor = BackgroundDeep,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("${goal.unit}${formatValue(goal.currentValue)} saved", fontSize = 11.sp, color = TextSecondary)
            Text("${goal.unit}${formatValue(goal.remainingValue)} to go", fontSize = 11.sp, color = TextSecondary)
        }
    }
}

private fun formatValue(value: Double): String =
    if (value >= 100_000) "${(value / 100_000).toInt()},${((value % 100_000) / 1000).toInt().toString().padStart(2, '0')},000"
    else if (value >= 1_000) "${(value / 1000).toInt()},${(value % 1000).toInt().toString().padStart(3, '0')}"
    else value.toInt().toString()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGoalSheet(state: GoalsState, onAction: (GoalsAction) -> Unit) {
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Primary,
        unfocusedBorderColor = BorderSubtle,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        cursorColor = Primary,
        focusedContainerColor = BackgroundDeep,
        unfocusedContainerColor = BackgroundDeep,
    )
    Column(modifier = Modifier.padding(24.dp)) {
        Text("New Goal", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
            value = state.newGoalName,
            onValueChange = { onAction(GoalsAction.OnNewGoalNameChange(it)) },
            label = { Text("Goal name", color = TextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            colors = fieldColors,
            shape = RoundedCornerShape(14.dp),
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = state.newGoalTarget,
                onValueChange = { onAction(GoalsAction.OnNewGoalTargetChange(it)) },
                label = { Text("Target", color = TextSecondary) },
                modifier = Modifier.weight(1f),
                colors = fieldColors,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            OutlinedTextField(
                value = state.newGoalCurrent,
                onValueChange = { onAction(GoalsAction.OnNewGoalCurrentChange(it)) },
                label = { Text("Current", color = TextSecondary) },
                modifier = Modifier.weight(1f),
                colors = fieldColors,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        }
        Spacer(Modifier.height(12.dp))
        Text("Priority", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GoalPriority.entries.forEach { priority ->
                val selected = state.newGoalPriority == priority
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) Primary.copy(alpha = 0.2f) else Surface)
                        .clickable { onAction(GoalsAction.OnNewGoalPriorityChange(priority)) }
                        .padding(10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        priority.name.lowercase().replaceFirstChar { it.uppercase() },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selected) Primary else TextMuted,
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        GoalGuardPrimaryButton(
            text = "Create Goal →",
            onClick = { onAction(GoalsAction.OnSaveGoal) },
            enabled = state.newGoalName.isNotBlank() && state.newGoalTarget.isNotBlank(),
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Preview
@Composable
private fun GoalsScreenPreview() {
    GoalGuardTheme {
        GoalsScreen(
            state = GoalsState(
                goals = listOf(
                    Goal("1", "Buy a House", "🏠", 1500000.0, 540000.0, "₹", LocalDate.of(2028, 12, 1), GoalPriority.HIGH, LocalDate.now()),
                    Goal("2", "Learn Android Dev", "📱", 100.0, 45.0, "%", LocalDate.of(2026, 12, 1), GoalPriority.MEDIUM, LocalDate.now()),
                ),
                isLoading = false,
            ),
            onAction = {},
        )
    }
}
