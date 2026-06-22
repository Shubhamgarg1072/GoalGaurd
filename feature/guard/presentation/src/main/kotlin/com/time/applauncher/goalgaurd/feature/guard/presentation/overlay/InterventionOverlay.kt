package com.time.applauncher.goalgaurd.feature.guard.presentation.overlay

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.time.applauncher.goalgaurd.core.designsystem.theme.BackgroundDeep
import com.time.applauncher.goalgaurd.core.designsystem.theme.Primary
import com.time.applauncher.goalgaurd.core.designsystem.theme.StreakOrange
import com.time.applauncher.goalgaurd.core.designsystem.theme.Surface
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextMuted
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextPrimary
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextSecondary
import com.time.applauncher.goalgaurd.core.designsystem.theme.Warning

/** Everything the overlay needs to render, assembled by [com.time.applauncher.goalgaurd.feature.guard.presentation.DoomScrollGuardService]. */
data class OverlayContent(
    val minutesScrolled: Int,
    val appLabel: String,
    val goalName: String,
    val goalTargetText: String?,
    val pendingHabits: List<String>,
)

@Composable
fun InterventionOverlay(
    content: OverlayContent,
    onStartHabit: () -> Unit,
    onContinueScrolling: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep.copy(alpha = 0.92f))
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Surface)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Warning.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Text("🛑", fontSize = 30.sp)
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "You've spent ${content.minutesScrolled} minutes scrolling",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "on ${content.appLabel}",
                fontSize = 13.sp,
                color = TextMuted,
            )

            Spacer(Modifier.height(20.dp))

            // Goal tie
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Primary.copy(alpha = 0.1f))
                    .padding(16.dp),
            ) {
                Text("YOUR GOAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Primary, letterSpacing = 1.sp)
                Spacer(Modifier.height(6.dp))
                Text(content.goalName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                content.goalTargetText?.let {
                    Spacer(Modifier.height(2.dp))
                    Text(it, fontSize = 13.sp, color = TextSecondary, lineHeight = 19.sp)
                }
            }

            if (content.pendingHabits.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("STILL PENDING TODAY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 1.sp)
                    Spacer(Modifier.height(8.dp))
                    content.pendingHabits.take(3).forEach { habit ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(StreakOrange))
                            Text(habit, fontSize = 13.sp, color = TextSecondary)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onStartHabit,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
            ) {
                Text("Start a habit instead", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = onContinueScrolling,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text("Continue scrolling", fontSize = 14.sp, color = TextMuted)
            }
        }
    }
}
