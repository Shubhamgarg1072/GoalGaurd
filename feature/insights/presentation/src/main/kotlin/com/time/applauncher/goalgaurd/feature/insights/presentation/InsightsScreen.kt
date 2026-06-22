package com.time.applauncher.goalgaurd.feature.insights.presentation

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.time.applauncher.goalgaurd.core.designsystem.components.GoalGuardCard
import com.time.applauncher.goalgaurd.core.designsystem.theme.BackgroundDeep
import com.time.applauncher.goalgaurd.core.designsystem.theme.GoalGuardTheme
import com.time.applauncher.goalgaurd.core.designsystem.theme.Primary
import com.time.applauncher.goalgaurd.core.designsystem.theme.PrimaryLight
import com.time.applauncher.goalgaurd.core.designsystem.theme.SuccessLight
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextMuted
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextPrimary
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextSecondary
import com.time.applauncher.goalgaurd.core.designsystem.theme.Warning

@Composable
fun InsightsScreen() {
    val weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val habitRates = listOf(0.6f, 0.8f, 0.5f, 0.9f, 0.7f, 0.4f, 0.85f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        Text("Insights", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        Text("Your progress at a glance", fontSize = 14.sp, color = TextSecondary)
        Spacer(Modifier.height(20.dp))

        // Highlight cards
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HighlightCard("🕐", "6h", "Recovered this week from social media", SuccessLight, Modifier.weight(1f))
            HighlightCard("📈", "312h", "Projected savings this year", Primary, Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))

        // Weekly habit bar chart
        GoalGuardCard(modifier = Modifier.fillMaxWidth()) {
            Text("Habit Completion Rate", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("This week", fontSize = 11.sp, color = TextSecondary)
            Spacer(Modifier.height(16.dp))
            HabitBarChart(days = weekDays, rates = habitRates)
        }
        Spacer(Modifier.height(16.dp))

        // Insights list
        GoalGuardCard(modifier = Modifier.fillMaxWidth()) {
            Text("Key Insights", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(12.dp))
            InsightRow("📅", "Most productive day", "Thursday (90% habits)")
            InsightRow("😴", "Worst scrolling day", "Sunday (3h 12m)")
            InsightRow("✅", "Habit success rate", "72% this week")
            InsightRow("🏠", "Goal projection", "On track — Dec 2028")
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun HighlightCard(emoji: String, value: String, label: String, valueColor: Color, modifier: Modifier) {
    GoalGuardCard(modifier = modifier) {
        Text(emoji, fontSize = 24.sp)
        Spacer(Modifier.height(8.dp))
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = valueColor)
        Text(label, fontSize = 11.sp, color = TextSecondary, lineHeight = 14.sp)
    }
}

@Composable
private fun HabitBarChart(days: List<String>, rates: List<Float>) {
    val barColor = Primary
    val trackColor = BackgroundDeep
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        days.forEachIndexed { i, day ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.width(32.dp).height(80.dp), contentAlignment = Alignment.BottomCenter) {
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(80.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(trackColor),
                    )
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height((80 * rates[i]).dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (rates[i] >= 0.8f) SuccessLight else if (rates[i] >= 0.5f) Primary else Warning.copy(alpha = 0.7f)),
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(day, fontSize = 10.sp, color = TextMuted)
            }
        }
    }
}

@Composable
private fun InsightRow(emoji: String, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(emoji, fontSize = 18.sp)
        Spacer(Modifier.width(10.dp))
        Text(label, fontSize = 13.sp, color = TextSecondary, modifier = Modifier.weight(1f))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}

@Preview
@Composable
private fun InsightsScreenPreview() {
    GoalGuardTheme {
        InsightsScreen()
    }
}
