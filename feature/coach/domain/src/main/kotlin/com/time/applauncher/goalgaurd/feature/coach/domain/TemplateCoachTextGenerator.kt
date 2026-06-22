package com.time.applauncher.goalgaurd.feature.coach.domain

import com.time.applauncher.goalgaurd.core.domain.CoachInput
import com.time.applauncher.goalgaurd.core.domain.CoachMessage
import com.time.applauncher.goalgaurd.core.domain.CoachTextGenerator
import com.time.applauncher.goalgaurd.core.domain.CoachTone

class TemplateCoachTextGenerator : CoachTextGenerator {

    override suspend fun generate(input: CoachInput): CoachMessage {
        val tone = selectTone(input)
        val variant = input.date.dayOfYear % 3
        return CoachMessage(
            headline = headline(tone, variant),
            body = body(tone, input, variant),
            tone = tone,
        )
    }

    internal fun selectTone(input: CoachInput): CoachTone {
        val ratio = if (input.habitsTotal == 0) 1.0 else input.habitsCompleted.toDouble() / input.habitsTotal
        val ahead = input.daysAheadOrBehind >= 0
        return when {
            ratio >= 0.8 && ahead -> CoachTone.CELEBRATORY
            ratio < 0.5 -> CoachTone.GENTLE_NUDGE
            ratio < 0.8 -> CoachTone.ENCOURAGING
            else -> CoachTone.NEUTRAL // ratio >= 0.8 but behind schedule
        }
    }

    private fun headline(tone: CoachTone, variant: Int): String = when (tone) {
        CoachTone.CELEBRATORY -> when (variant) {
            0 -> "Crushing it today!"
            1 -> "You're on fire!"
            else -> "Outstanding progress!"
        }
        CoachTone.ENCOURAGING -> when (variant) {
            0 -> "Keep the momentum going!"
            1 -> "You're building something great!"
            else -> "Stay the course!"
        }
        CoachTone.NEUTRAL -> when (variant) {
            0 -> "Steady progress today"
            1 -> "Another day, another step"
            else -> "Consistent and on track"
        }
        CoachTone.GENTLE_NUDGE -> when (variant) {
            0 -> "Let's refocus — your goal needs you"
            1 -> "Small steps still move mountains"
            else -> "Today's a great day to restart"
        }
    }

    private fun body(tone: CoachTone, input: CoachInput, variant: Int): String {
        val schedulePhrase = when {
            input.daysAheadOrBehind > 0 -> "${input.daysAheadOrBehind} days ahead of schedule"
            input.daysAheadOrBehind < 0 -> "${-input.daysAheadOrBehind} days behind schedule"
            else -> "right on schedule"
        }
        val habitPhrase = "${input.habitsCompleted} of ${input.habitsTotal} habits"
        val focusPhrase = if (input.focusMinutes >= 60) {
            "${input.focusMinutes / 60}h ${input.focusMinutes % 60}m of focus"
        } else {
            "${input.focusMinutes}m of focus"
        }
        val streakPhrase = if (input.currentStreak > 0) " ${input.currentStreak}-day streak!" else ""
        val pendingPhrase = input.topPendingHabit?.let { " Next up: $it." } ?: ""

        return when (tone) {
            CoachTone.CELEBRATORY -> when (variant) {
                0 -> "You've completed $habitPhrase and you're $schedulePhrase on ${input.primaryGoalName}. You've logged $focusPhrase today.$streakPhrase$pendingPhrase"
                1 -> "Amazing! $habitPhrase done, $focusPhrase logged, and ${input.primaryGoalName} is ${input.primaryGoalPct}% complete — $schedulePhrase.$streakPhrase$pendingPhrase"
                else -> "${input.primaryGoalName} is ${input.primaryGoalPct}% done and you're $schedulePhrase. $habitPhrase completed with $focusPhrase in the zone.$streakPhrase$pendingPhrase"
            }
            CoachTone.ENCOURAGING -> when (variant) {
                0 -> "You've done $habitPhrase so far today and you're $schedulePhrase on ${input.primaryGoalName} (${input.primaryGoalPct}%). Keep pushing — $focusPhrase logged.$streakPhrase$pendingPhrase"
                1 -> "${input.primaryGoalName} is ${input.primaryGoalPct}% complete and you're $schedulePhrase. With $habitPhrase done and $focusPhrase in, you're building real momentum.$streakPhrase$pendingPhrase"
                else -> "Good work on $habitPhrase completed. ${input.primaryGoalName} stands at ${input.primaryGoalPct}%, $schedulePhrase. Your $focusPhrase today keeps the engine running.$streakPhrase$pendingPhrase"
            }
            CoachTone.NEUTRAL -> when (variant) {
                0 -> "Today: $habitPhrase, $focusPhrase. ${input.primaryGoalName} is ${input.primaryGoalPct}% complete, $schedulePhrase.$streakPhrase$pendingPhrase"
                1 -> "${input.primaryGoalName} at ${input.primaryGoalPct}%, $schedulePhrase. You've completed $habitPhrase and logged $focusPhrase today.$streakPhrase$pendingPhrase"
                else -> "You're $schedulePhrase on ${input.primaryGoalName} (${input.primaryGoalPct}%). $habitPhrase complete today with $focusPhrase of focused work.$streakPhrase$pendingPhrase"
            }
            CoachTone.GENTLE_NUDGE -> when (variant) {
                0 -> "${input.primaryGoalName} is ${input.primaryGoalPct}% complete and you're $schedulePhrase. Only $habitPhrase done so far — every habit counts toward closing that gap.$pendingPhrase"
                1 -> "You're $schedulePhrase on ${input.primaryGoalName} (${input.primaryGoalPct}%). $habitPhrase completed today. Even ${input.focusMinutes}m of focus moves you forward.$pendingPhrase"
                else -> "Your goal of '${input.primaryGoalName}' needs attention — you're $schedulePhrase. $habitPhrase done today. A little focus now can change tomorrow's picture.$pendingPhrase"
            }
        }.trim()
    }
}
