package com.time.applauncher.goalgaurd.feature.coach.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.time.applauncher.goalgaurd.core.domain.CoachInput
import com.time.applauncher.goalgaurd.core.domain.CoachMessage
import com.time.applauncher.goalgaurd.core.domain.CoachTextGenerator
import com.time.applauncher.goalgaurd.core.domain.CoachTone
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDate

class FallbackCoachTextGeneratorTest {

    private val input = CoachInput(
        date = LocalDate.of(2024, 6, 15),
        habitsCompleted = 3,
        habitsTotal = 5,
        focusMinutes = 30,
        socialMinutes = 0,
        primaryGoalName = "Save",
        primaryGoalPct = 50,
        daysAheadOrBehind = 0,
        topPendingHabit = null,
        currentStreak = 3,
    )

    @Test
    fun `returns primary result when primary succeeds`() = runTest {
        val primary = FixedGenerator(CoachMessage("primary-headline", "body", CoachTone.CELEBRATORY))
        val fallback = FixedGenerator(CoachMessage("fallback-headline", "body", CoachTone.NEUTRAL))
        val result = FallbackCoachTextGenerator(primary, fallback).generate(input)
        assertThat(result.headline).isEqualTo("primary-headline")
    }

    @Test
    fun `falls back to fallback when primary throws`() = runTest {
        val primary = ThrowingGenerator()
        val fallback = FixedGenerator(CoachMessage("fallback-headline", "body", CoachTone.NEUTRAL))
        val result = FallbackCoachTextGenerator(primary, fallback).generate(input)
        assertThat(result.headline).isEqualTo("fallback-headline")
    }

    @Test
    fun `fallback tone is preserved when primary throws`() = runTest {
        val primary = ThrowingGenerator()
        val fallback = FixedGenerator(CoachMessage("headline", "body", CoachTone.GENTLE_NUDGE))
        val result = FallbackCoachTextGenerator(primary, fallback).generate(input)
        assertThat(result.tone).isEqualTo(CoachTone.GENTLE_NUDGE)
    }

    private class FixedGenerator(private val message: CoachMessage) : CoachTextGenerator {
        override suspend fun generate(input: CoachInput) = message
    }

    private class ThrowingGenerator : CoachTextGenerator {
        override suspend fun generate(input: CoachInput): CoachMessage =
            throw RuntimeException("primary unavailable")
    }
}
