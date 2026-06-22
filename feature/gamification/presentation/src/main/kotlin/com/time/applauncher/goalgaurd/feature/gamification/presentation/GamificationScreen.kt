package com.time.applauncher.goalgaurd.feature.gamification.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.time.applauncher.goalgaurd.core.designsystem.components.GoalGuardCard
import com.time.applauncher.goalgaurd.core.designsystem.theme.BackgroundDeep
import com.time.applauncher.goalgaurd.core.designsystem.theme.BorderSubtle
import com.time.applauncher.goalgaurd.core.designsystem.theme.GoalGuardTheme
import com.time.applauncher.goalgaurd.core.designsystem.theme.Primary
import com.time.applauncher.goalgaurd.core.designsystem.theme.PrimaryLight
import com.time.applauncher.goalgaurd.core.designsystem.theme.SuccessLight
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextMuted
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextPrimary
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextSecondary
import com.time.applauncher.goalgaurd.core.designsystem.theme.Warning
import com.time.applauncher.goalgaurd.feature.gamification.domain.Badge
import com.time.applauncher.goalgaurd.feature.gamification.domain.UserLevel

private val sampleBadges = listOf(
    Badge("1", "🔥", "Streak Master", "17-day habit streak", true),
    Badge("2", "🎯", "Goal Setter", "Created first goal", true),
    Badge("3", "⚡", "Focus Hero", "10+ focus sessions", true),
    Badge("4", "📱", "Scroll Buster", "Reduced 5+ hours", false),
    Badge("5", "🏆", "Achiever", "Reach Level 4", false),
    Badge("6", "💎", "Legend", "Complete all goals", false),
)

@Composable
fun GamificationScreen() {
    val level = UserLevel.fromXp(2840).copy(badges = sampleBadges)
    val levels = listOf("Beginner", "Builder", "Achiever", "Master", "Legend")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        Text("Progress", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        Spacer(Modifier.height(20.dp))

        // XP card
        GoalGuardCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(Primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("${level.level}", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Primary)
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(level.levelName, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                    Text("${level.xp} XP", fontSize = 13.sp, color = TextSecondary)
                }
                Text("+150 XP today", fontSize = 12.sp, color = SuccessLight, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { level.progressToNextLevel },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = Primary,
                trackColor = BackgroundDeep,
            )
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${level.xp} XP", fontSize = 11.sp, color = TextMuted)
                if (level.xpForNextLevel != Int.MAX_VALUE) {
                    Text("${level.xpForNextLevel} XP next level", fontSize = 11.sp, color = TextMuted)
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        // Level path
        GoalGuardCard(modifier = Modifier.fillMaxWidth()) {
            Text("Level Path", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                levels.forEachIndexed { i, name ->
                    val isReached = i < level.level
                    val isCurrent = i == level.level - 1
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isReached) Primary else BackgroundDeep)
                                .border(2.dp, if (isCurrent) Primary else if (isReached) Primary else BorderSubtle, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("${i + 1}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isReached) Color.White else TextMuted)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(name, fontSize = 9.sp, color = if (isReached) PrimaryLight else TextMuted, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal)
                    }
                    if (i < levels.size - 1) {
                        Box(
                            modifier = Modifier.width(20.dp).height(2.dp).clip(RoundedCornerShape(1.dp))
                                .background(if (i < level.level - 1) Primary else BorderSubtle)
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        // Badges
        GoalGuardCard(modifier = Modifier.fillMaxWidth()) {
            Text("Badges", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                level.badges.chunked(3).take(2).flatten().forEach { badge ->
                    BadgeItem(badge = badge, modifier = Modifier.weight(1f))
                }
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun BadgeItem(badge: Badge, modifier: Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (badge.isUnlocked) Primary.copy(alpha = 0.2f) else BackgroundDeep)
                .border(1.dp, if (badge.isUnlocked) Primary.copy(alpha = 0.4f) else BorderSubtle, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(badge.emoji, fontSize = if (badge.isUnlocked) 24.sp else 20.sp)
        }
        Spacer(Modifier.height(4.dp))
        Text(badge.name, fontSize = 10.sp, color = if (badge.isUnlocked) TextPrimary else TextMuted, fontWeight = FontWeight.SemiBold)
    }
}

@Preview
@Composable
private fun GamificationScreenPreview() {
    GoalGuardTheme {
        GamificationScreen()
    }
}
