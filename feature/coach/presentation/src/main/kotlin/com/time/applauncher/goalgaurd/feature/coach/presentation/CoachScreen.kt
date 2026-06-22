package com.time.applauncher.goalgaurd.feature.coach.presentation

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.time.applauncher.goalgaurd.core.designsystem.components.GoalGuardCard
import com.time.applauncher.goalgaurd.core.designsystem.theme.BackgroundDeep
import com.time.applauncher.goalgaurd.core.designsystem.theme.Primary
import com.time.applauncher.goalgaurd.core.designsystem.theme.PrimaryContainer
import com.time.applauncher.goalgaurd.core.designsystem.theme.PrimaryLight
import com.time.applauncher.goalgaurd.core.designsystem.theme.StreakOrange
import com.time.applauncher.goalgaurd.core.designsystem.theme.SurfaceVariant
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextMuted
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextPrimary
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextSecondary
import com.time.applauncher.goalgaurd.core.designsystem.theme.Warning
import com.time.applauncher.goalgaurd.core.domain.CoachInput
import com.time.applauncher.goalgaurd.core.domain.CoachMessage
import com.time.applauncher.goalgaurd.core.domain.CoachTone
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun CoachRoot(
    onNavigateBack: () -> Unit,
    viewModel: CoachViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CoachScreen(state = state, onAction = viewModel::onAction, onNavigateBack = onNavigateBack)
}

@Composable
fun CoachScreen(
    state: CoachState,
    onAction: (CoachAction) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep),
    ) {
        CoachTopBar(onNavigateBack = onNavigateBack)

        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            }

            state.input != null && state.message != null -> {
                CoachContent(input = state.input, message = state.message)
            }

            else -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Couldn't load your summary", color = TextSecondary, fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = { onAction(CoachAction.OnRetry) }) {
                            Text("Retry", color = Primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CoachTopBar(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = TextPrimary,
            )
        }
        Spacer(Modifier.width(4.dp))
        Column {
            Text(
                "Evening Summary",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Text(
                LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d")),
                fontSize = 12.sp,
                color = TextSecondary,
            )
        }
    }
}

@Composable
private fun CoachContent(input: CoachInput, message: CoachMessage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        StatCardsRow(input = input)
        Spacer(Modifier.height(14.dp))
        CoachMessageCard(message = message)
        Spacer(Modifier.height(14.dp))
        PrimaryGoalCard(input = input)
        Spacer(Modifier.height(14.dp))
        StreakStrip(streak = input.currentStreak)
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun StatCardsRow(input: CoachInput) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatCard(
            label = "Habits",
            value = "${input.habitsCompleted}/${input.habitsTotal}",
            dotColor = Primary,
            modifier = Modifier.weight(1f),
        )
        StatCard(
            label = "Focus",
            value = input.focusMinutes.toTimeString(),
            dotColor = PrimaryLight,
            modifier = Modifier.weight(1f),
        )
        StatCard(
            label = "Social",
            value = input.socialMinutes.toTimeString(),
            dotColor = Warning,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, dotColor: Color, modifier: Modifier) {
    GoalGuardCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(dotColor))
            Text(label, fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(6.dp))
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}

@Composable
private fun CoachMessageCard(message: CoachMessage) {
    GoalGuardCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                message.headline,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            ToneBadge(tone = message.tone)
        }
        Spacer(Modifier.height(10.dp))
        Text(
            message.body,
            fontSize = 13.sp,
            color = TextSecondary,
            lineHeight = 20.sp,
        )
    }
}

@Composable
private fun ToneBadge(tone: CoachTone) {
    val (label, color) = when (tone) {
        CoachTone.CELEBRATORY -> "🎉 Great" to Primary
        CoachTone.ENCOURAGING -> "💪 Good" to PrimaryLight
        CoachTone.NEUTRAL -> "📊 Steady" to TextSecondary
        CoachTone.GENTLE_NUDGE -> "⚠ Focus" to StreakOrange
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
private fun PrimaryGoalCard(input: CoachInput) {
    GoalGuardCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Primary Goal", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
                Text(input.primaryGoalName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Text(
                "${input.primaryGoalPct}%",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Primary,
            )
        }
        Spacer(Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { input.primaryGoalPct / 100f },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = Primary,
            trackColor = BackgroundDeep,
        )
        Spacer(Modifier.height(8.dp))
        val scheduleLabel = when {
            input.daysAheadOrBehind > 0 -> "${input.daysAheadOrBehind} days ahead of schedule"
            input.daysAheadOrBehind < 0 -> "${-input.daysAheadOrBehind} days behind schedule"
            else -> "Right on schedule"
        }
        val scheduleColor = if (input.daysAheadOrBehind >= 0) PrimaryLight else StreakOrange
        Text(scheduleLabel, fontSize = 11.sp, color = scheduleColor)
    }
}

@Composable
private fun StreakStrip(streak: Int) {
    GoalGuardCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("7-Day Streak", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("🔥", fontSize = 14.sp)
                Text("$streak days", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StreakOrange)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val days = listOf("M", "T", "W", "T", "F", "S", "S")
            // Last `streak` bars (up to 7) are active; rest are inactive
            val activeDays = streak.coerceIn(0, 7)
            days.forEachIndexed { index, label ->
                val isActive = index >= (7 - activeDays)
                StreakBar(label = label, isActive = isActive, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StreakBar(label: String, isActive: Boolean, modifier: Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isActive) PrimaryContainer else SurfaceVariant),
        )
        Text(label, fontSize = 10.sp, color = if (isActive) Primary else TextMuted)
    }
}

private fun Int.toTimeString(): String = if (this >= 60) "${this / 60}h ${this % 60}m" else "${this}m"
