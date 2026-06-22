package com.time.applauncher.goalgaurd.feature.habits.presentation

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
import com.time.applauncher.goalgaurd.core.designsystem.theme.SuccessLight
import com.time.applauncher.goalgaurd.core.designsystem.theme.Surface
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextMuted
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextPrimary
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextSecondary
import com.time.applauncher.goalgaurd.core.designsystem.theme.WarningLight
import com.time.applauncher.goalgaurd.feature.habits.domain.Habit
import com.time.applauncher.goalgaurd.feature.habits.domain.HabitDifficulty
import com.time.applauncher.goalgaurd.feature.habits.domain.HabitFrequency
import org.koin.androidx.compose.koinViewModel

@Composable
fun HabitsRoot(viewModel: HabitsViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HabitsScreen(state = state, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(state: HabitsState, onAction: (HabitsAction) -> Unit) {
    val tabs = listOf(HabitFrequency.DAILY, HabitFrequency.WEEKLY, HabitFrequency.MONTHLY)

    Scaffold(
        containerColor = BackgroundDeep,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAction(HabitsAction.OnAddHabitClick) },
                containerColor = Primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add habit")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {
                Text("Habits", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${state.todayCompletedCount} of ${state.habits.size} completed today",
                    fontSize = 13.sp,
                    color = TextSecondary,
                )
            }
            // Tab row
            TabRow(
                selectedTabIndex = tabs.indexOf(state.selectedTab),
                containerColor = Surface,
                contentColor = Primary,
                indicator = { tabPositions ->
                    val selected = tabs.indexOf(state.selectedTab)
                    if (selected < tabPositions.size) {
                        Box(
                            Modifier
                                .tabIndicatorOffset(tabPositions[selected])
                                .height(3.dp)
                                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                .background(Primary)
                        )
                    }
                },
            ) {
                tabs.forEach { tab ->
                    Tab(
                        selected = state.selectedTab == tab,
                        onClick = { onAction(HabitsAction.OnTabSelect(tab)) },
                        text = {
                            Text(
                                tab.name.lowercase().replaceFirstChar { it.uppercase() },
                                fontSize = 13.sp,
                                fontWeight = if (state.selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                color = if (state.selectedTab == tab) Primary else TextMuted,
                            )
                        },
                    )
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item { Spacer(Modifier.height(12.dp)) }
                items(state.filteredHabits, key = { it.id }) { habit ->
                    HabitCard(
                        habit = habit,
                        isCompleted = habit.id in state.completedHabitIds,
                        onToggle = { onAction(HabitsAction.OnToggleHabit(habit.id)) },
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    if (state.showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { onAction(HabitsAction.OnDismissAddSheet) },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Surface,
        ) {
            AddHabitSheet(state = state, onAction = onAction)
        }
    }
}

@Composable
private fun HabitCard(habit: Habit, isCompleted: Boolean, onToggle: () -> Unit) {
    val checkBg by animateColorAsState(
        targetValue = if (isCompleted) Primary else Surface,
        label = "check_bg",
    )
    GoalGuardCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(habit.emoji, fontSize = 24.sp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(habit.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(habit.frequency.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 11.sp, color = TextMuted)
                    if (habit.streak > 0) {
                        Text("🔥 ${habit.streak}", fontSize = 11.sp, color = WarningLight)
                    }
                }
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(checkBg)
                    .clickable(onClick = onToggle),
                contentAlignment = Alignment.Center,
            ) {
                if (isCompleted) {
                    Icon(Icons.Default.Check, contentDescription = "Completed", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun AddHabitSheet(state: HabitsState, onAction: (HabitsAction) -> Unit) {
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
        Text("New Habit", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
            value = state.newHabitName,
            onValueChange = { onAction(HabitsAction.OnNewHabitNameChange(it)) },
            label = { Text("Habit name", color = TextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            colors = fieldColors,
            shape = RoundedCornerShape(14.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text("Frequency", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HabitFrequency.entries.forEach { freq ->
                val selected = state.newHabitFrequency == freq
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) Primary.copy(alpha = 0.2f) else Surface)
                        .clickable { onAction(HabitsAction.OnNewHabitFrequencyChange(freq)) }
                        .padding(10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        freq.name.lowercase().replaceFirstChar { it.uppercase() },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selected) Primary else TextMuted,
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        GoalGuardPrimaryButton(
            text = "Add Habit →",
            onClick = { onAction(HabitsAction.OnSaveHabit) },
            enabled = state.newHabitName.isNotBlank(),
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Preview
@Composable
private fun HabitsScreenPreview() {
    GoalGuardTheme {
        HabitsScreen(
            state = HabitsState(
                habits = listOf(
                    Habit("1", null, "Save ₹500 daily", "💰", HabitFrequency.DAILY, HabitDifficulty.MEDIUM, null, 17, true),
                    Habit("2", null, "Morning workout", "🏋️", HabitFrequency.DAILY, HabitDifficulty.HARD, null, 22, true),
                ),
                completedHabitIds = setOf("1"),
                isLoading = false,
            ),
            onAction = {},
        )
    }
}
