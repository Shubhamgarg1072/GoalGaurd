package com.time.applauncher.goalgaurd.feature.dashboard.presentation

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
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
import com.time.applauncher.goalgaurd.core.designsystem.components.GoalProgressRing
import com.time.applauncher.goalgaurd.core.designsystem.theme.BackgroundDeep
import com.time.applauncher.goalgaurd.core.designsystem.theme.GoalGuardTheme
import com.time.applauncher.goalgaurd.core.designsystem.theme.Primary
import com.time.applauncher.goalgaurd.core.designsystem.theme.PrimaryLight
import com.time.applauncher.goalgaurd.core.designsystem.theme.PrimarySubtle
import com.time.applauncher.goalgaurd.core.designsystem.theme.SuccessLight
import com.time.applauncher.goalgaurd.core.designsystem.theme.SurfaceVariant
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextMuted
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextPrimary
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextSecondary
import com.time.applauncher.goalgaurd.core.designsystem.theme.Warning
import com.time.applauncher.goalgaurd.core.designsystem.theme.WarningLight
import com.time.applauncher.goalgaurd.core.presentation.ObserveAsEvents
import com.time.applauncher.goalgaurd.feature.goals.domain.Goal
import com.time.applauncher.goalgaurd.feature.goals.domain.GoalPriority
import com.time.applauncher.goalgaurd.feature.habits.domain.Habit
import com.time.applauncher.goalgaurd.feature.habits.domain.HabitDifficulty
import com.time.applauncher.goalgaurd.feature.habits.domain.HabitFrequency
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DashboardRoot(
    onNavigateToGoalDetail: (String) -> Unit,
    onNavigateToFocus: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToHabits: () -> Unit,
    onNavigateToInsights: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is DashboardEvent.NavigateToGoalDetail -> onNavigateToGoalDetail(event.goalId)
            DashboardEvent.NavigateToFocus -> onNavigateToFocus()
            DashboardEvent.NavigateToGoals -> onNavigateToGoals()
            DashboardEvent.NavigateToHabits -> onNavigateToHabits()
            DashboardEvent.NavigateToInsights -> onNavigateToInsights()
            DashboardEvent.NavigateToSettings -> onNavigateToSettings()
        }
    }
    DashboardScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun DashboardScreen(state: DashboardState, onAction: (DashboardAction) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(52.dp))
        DashboardHeader(state = state, onAction = onAction)
        Spacer(Modifier.height(20.dp))
        GoalScoreCard(state = state, onFocusClick = { onAction(DashboardAction.OnFocusClick) })
        Spacer(Modifier.height(14.dp))
        TodayHabitsCard(
            state = state,
            onToggle = { onAction(DashboardAction.OnToggleHabit(it)) },
            onSeeAll = { onAction(DashboardAction.OnHabitsClick) },
        )
        Spacer(Modifier.height(14.dp))
        TimeStatsRow(
            state = state,
            onInsightsClick = { onAction(DashboardAction.OnInsightsClick) },
        )
        Spacer(Modifier.height(14.dp))
        state.primaryGoal?.let { goal ->
            GoalsSectionHeader(onSeeAll = { onAction(DashboardAction.OnGoalsClick) })
            Spacer(Modifier.height(8.dp))
            PrimaryGoalCard(goal = goal, onClick = { onAction(DashboardAction.OnGoalClick(goal.id)) })
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun DashboardHeader(state: DashboardState, onAction: (DashboardAction) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
                fontSize = 13.sp,
                color = TextSecondary,
            )
            Text("Good morning 👋", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Warning.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("🔥", fontSize = 14.sp)
                    Text("${state.streak}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WarningLight)
                }
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Primary)
                    .clickable { onAction(DashboardAction.OnSettingsClick) },
                contentAlignment = Alignment.Center,
            ) {
                Text("A", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
private fun GoalScoreCard(state: DashboardState, onFocusClick: () -> Unit) {
    GoalGuardCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                GoalProgressRing(
                    progress = state.goalScore / 100f,
                    size = 90.dp,
                    strokeWidth = 10.dp,
                    trackColor = BackgroundDeep,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${state.goalScore}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                        )
                    }
                }
                Column {
                    Text("Goal Score", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 0.8.sp)
                    Text("${state.goalScore} / 100", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SuccessLight.copy(alpha = 0.12f))
                            .padding(horizontal = 9.dp, vertical = 4.dp),
                    ) {
                        Text("Builder · Lvl 3", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SuccessLight)
                    }
                }
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(13.dp))
                    .background(Primary.copy(alpha = 0.15f))
                    .clickable(onClick = onFocusClick)
                    .padding(10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎯", fontSize = 20.sp)
                    Text("Focus", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun TodayHabitsCard(state: DashboardState, onToggle: (String) -> Unit, onSeeAll: () -> Unit) {
    GoalGuardCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onSeeAll),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Today's Habits", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${state.todayCompletedCount}/${state.habits.size}", fontSize = 12.sp, color = TextSecondary)
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Default.ChevronRight, contentDescription = "See all habits", tint = TextMuted, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(Modifier.height(14.dp))
        if (state.habits.isEmpty()) {
            Text("No habits yet — tap to add some!", fontSize = 13.sp, color = TextMuted)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.habits.take(5).forEach { habit ->
                    HabitRow(
                        habit = habit,
                        isCompleted = habit.id in state.completedHabitIds,
                        onToggle = { onToggle(habit.id) },
                    )
                }
                if (state.habits.size > 5) {
                    Text(
                        "+ ${state.habits.size - 5} more",
                        fontSize = 12.sp,
                        color = PrimaryLight,
                        modifier = Modifier
                            .clickable(onClick = onSeeAll)
                            .padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun HabitRow(habit: Habit, isCompleted: Boolean, onToggle: () -> Unit) {
    val checkBg by animateColorAsState(
        targetValue = if (isCompleted) Primary else SurfaceVariant,
        label = "check",
    )
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(checkBg)
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center,
        ) {
            if (isCompleted) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
        Spacer(Modifier.width(11.dp))
        Text(
            "${habit.emoji} ${habit.name}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (isCompleted) TextMuted else TextPrimary,
            modifier = Modifier.weight(1f),
        )
        if (habit.streak > 0) {
            Text("🔥${habit.streak}", fontSize = 11.sp, color = TextMuted)
        }
    }
}

@Composable
private fun TimeStatsRow(state: DashboardState, onInsightsClick: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        TimeStatCard(
            label = "Focus",
            dotColor = Primary,
            value = "${state.focusMinutesToday / 60}h ${state.focusMinutesToday % 60}m",
            trend = "↑ 45m vs yesterday",
            trendColor = SuccessLight,
            onClick = onInsightsClick,
            modifier = Modifier.weight(1f),
        )
        TimeStatCard(
            label = "Social",
            dotColor = Warning,
            value = "${state.socialMinutesToday / 60}h ${state.socialMinutesToday % 60}m",
            trend = "↓ 18m saved",
            trendColor = SuccessLight,
            onClick = onInsightsClick,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TimeStatCard(
    label: String,
    dotColor: Color,
    value: String,
    trend: String,
    trendColor: Color,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    GoalGuardCard(modifier = modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(dotColor))
            Text(label, fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(6.dp))
        Text(value, fontSize = 21.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text(trend, fontSize = 11.sp, color = trendColor)
    }
}

@Composable
private fun GoalsSectionHeader(onSeeAll: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSeeAll),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Primary Goal", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("See all", fontSize = 12.sp, color = PrimaryLight)
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = PrimaryLight, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun PrimaryGoalCard(goal: Goal, onClick: () -> Unit) {
    GoalGuardCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(goal.emoji, fontSize = 22.sp)
                Column {
                    Text(goal.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(
                        "Dec ${goal.targetDate.year} · ${goal.daysRemaining} days left",
                        fontSize = 11.sp,
                        color = TextSecondary,
                    )
                }
            }
            Text("${goal.progressPercent}%", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = PrimarySubtle)
        }
        Spacer(Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { goal.progressFraction },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = Primary,
            trackColor = BackgroundDeep,
        )
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${goal.unit}${goal.currentValue.toLong()} saved", fontSize = 11.sp, color = TextSecondary)
            Text("${goal.unit}${goal.remainingValue.toLong()} to go", fontSize = 11.sp, color = TextSecondary)
        }
    }
}

@Preview
@Composable
private fun DashboardScreenPreview() {
    GoalGuardTheme {
        DashboardScreen(
            state = DashboardState(
                goals = listOf(Goal("1", "Buy a House", "🏠", 1500000.0, 540000.0, "₹", LocalDate.of(2028, 12, 1), GoalPriority.HIGH, LocalDate.now())),
                habits = listOf(
                    Habit("1", null, "Save ₹500 daily", "💰", HabitFrequency.DAILY, HabitDifficulty.MEDIUM, null, 17, true),
                    Habit("2", null, "Morning workout", "🏋️", HabitFrequency.DAILY, HabitDifficulty.HARD, null, 22, true),
                ),
                completedHabitIds = setOf("1"),
                goalScore = 82,
                focusMinutesToday = 135,
                streak = 17,
                isLoading = false,
            ),
            onAction = {},
        )
    }
}
