package com.time.applauncher.goalgaurd.feature.focus.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.time.applauncher.goalgaurd.core.designsystem.components.GoalGuardCard
import com.time.applauncher.goalgaurd.core.designsystem.components.GoalGuardPrimaryButton
import com.time.applauncher.goalgaurd.core.designsystem.components.GoalProgressRing
import com.time.applauncher.goalgaurd.core.designsystem.theme.BackgroundDeep
import com.time.applauncher.goalgaurd.core.designsystem.theme.BorderSubtle
import com.time.applauncher.goalgaurd.core.designsystem.theme.GoalGuardTheme
import com.time.applauncher.goalgaurd.core.designsystem.theme.Primary
import com.time.applauncher.goalgaurd.core.designsystem.theme.PrimaryLight
import com.time.applauncher.goalgaurd.core.designsystem.theme.SuccessLight
import com.time.applauncher.goalgaurd.core.designsystem.theme.SurfaceVariant
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextMuted
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextPrimary
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextSecondary
import com.time.applauncher.goalgaurd.core.presentation.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

private enum class FocusScreen { Selector, Active, Done }

@Composable
fun FocusModeRoot(
    onNavigateBack: () -> Unit,
    viewModel: FocusViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            FocusEvent.NavigateBack -> onNavigateBack()
        }
    }
    FocusModeScreen(state = state, onAction = viewModel::onAction, onNavigateBack = onNavigateBack)
}

@Composable
fun FocusModeScreen(
    state: FocusState,
    onAction: (FocusAction) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val screen = when {
        state.isSessionDone -> FocusScreen.Done
        state.isSessionActive -> FocusScreen.Active
        else -> FocusScreen.Selector
    }

    AnimatedContent(targetState = screen, label = "focus_screen") { target ->
        when (target) {
            FocusScreen.Selector -> FocusSelectorScreen(state = state, onAction = onAction, onNavigateBack = onNavigateBack)
            FocusScreen.Active -> ActiveSessionScreen(state = state, onAction = onAction)
            FocusScreen.Done -> SessionDoneScreen(durationMinutes = state.selectedDurationMinutes, onAction = onAction)
        }
    }
}

@Composable
private fun FocusSelectorScreen(
    state: FocusState,
    onAction: (FocusAction) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val durations = listOf(15, 30, 60, 90)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("Focus Mode", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        Text(
            "Block distractions and focus on what matters",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(40.dp))
        Text("Session Length", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            durations.forEach { minutes ->
                val selected = state.selectedDurationMinutes == minutes
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (selected) Primary.copy(alpha = 0.2f) else SurfaceVariant)
                        .border(1.dp, if (selected) Primary.copy(alpha = 0.4f) else BorderSubtle, RoundedCornerShape(14.dp))
                        .clickable { onAction(FocusAction.OnDurationSelect(minutes)) }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$minutes", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = if (selected) Primary else TextPrimary)
                        Text("min", fontSize = 11.sp, color = TextMuted)
                    }
                }
            }
        }
        Spacer(Modifier.height(40.dp))
        Text(
            "Today's focus: ${state.todayFocusMinutes / 60}h ${state.todayFocusMinutes % 60}m",
            fontSize = 13.sp,
            color = TextSecondary,
        )
        Spacer(Modifier.weight(1f))
        GoalGuardPrimaryButton(
            text = "Start ${state.selectedDurationMinutes}min Session",
            onClick = { onAction(FocusAction.OnStartSession) },
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ActiveSessionScreen(state: FocusState, onAction: (FocusAction) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Focus Session", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
        Spacer(Modifier.height(48.dp))
        GoalProgressRing(
            progress = state.progressFraction,
            size = 200.dp,
            strokeWidth = 16.dp,
            trackColor = SurfaceVariant,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(state.formattedRemaining, fontSize = 42.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                Text("remaining", fontSize = 13.sp, color = TextMuted)
            }
        }
        Spacer(Modifier.height(48.dp))
        Text("Stay focused. You've got this! 💪", fontSize = 14.sp, color = TextSecondary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(40.dp))
        GoalGuardPrimaryButton(
            text = "Complete Session ✓",
            onClick = { onAction(FocusAction.OnEndSession) },
        )
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = { onAction(FocusAction.OnAbandonSession) }) {
            Text("Abandon session", color = TextMuted, fontSize = 13.sp)
        }
    }
}

@Composable
private fun SessionDoneScreen(durationMinutes: Int, onAction: (FocusAction) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("🎉", fontSize = 72.sp)
        Spacer(Modifier.height(20.dp))
        Text("Session Complete!", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        Text(
            "You crushed a ${durationMinutes}min focus session",
            fontSize = 15.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatBadge(label = "XP Earned", value = "+${durationMinutes * 2}", color = SuccessLight)
            StatBadge(label = "Minutes", value = "$durationMinutes", color = PrimaryLight)
        }
        Spacer(Modifier.height(48.dp))
        GoalGuardPrimaryButton(
            text = "Back to Dashboard",
            onClick = { onAction(FocusAction.OnDismissSessionDone) },
        )
    }
}

@Composable
private fun StatBadge(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    GoalGuardCard {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(label, fontSize = 11.sp, color = TextMuted)
        }
    }
}

@Preview
@Composable
private fun FocusSelectorPreview() {
    GoalGuardTheme {
        FocusModeScreen(state = FocusState(), onAction = {}, onNavigateBack = {})
    }
}

@Preview
@Composable
private fun FocusDonePreview() {
    GoalGuardTheme {
        FocusModeScreen(state = FocusState(isSessionDone = true, selectedDurationMinutes = 30), onAction = {}, onNavigateBack = {})
    }
}
