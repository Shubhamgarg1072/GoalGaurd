package com.time.applauncher.goalgaurd.feature.coach.domain

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.doesNotContain
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotEmpty
import com.time.applauncher.goalgaurd.core.domain.CoachInput
import com.time.applauncher.goalgaurd.core.domain.CoachTone
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDate

class TemplateCoachTextGeneratorTest {

    private val generator = TemplateCoachTextGenerator()

    private val base = CoachInput(
        date = LocalDate.of(2024, 6, 15),
        habitsCompleted = 3,
        habitsTotal = 5,
        focusMinutes = 45,
        socialMinutes = 30,
        primaryGoalName = "Buy a House",
        primaryGoalPct = 36,
        daysAheadOrBehind = 2,
        topPendingHabit = "Morning workout",
        currentStreak = 7,
    )

    // ── Tone selection ──────────────────────────────────────────────────────

    @Test
    fun `CELEBRATORY when ratio at 0_8 boundary and ahead`() {
        assertThat(generator.selectTone(base.copy(habitsCompleted = 4, habitsTotal = 5, daysAheadOrBehind = 0)))
            .isEqualTo(CoachTone.CELEBRATORY)
    }

    @Test
    fun `CELEBRATORY when all habits done and ahead`() {
        assertThat(generator.selectTone(base.copy(habitsCompleted = 5, habitsTotal = 5, daysAheadOrBehind = 3)))
            .isEqualTo(CoachTone.CELEBRATORY)
    }

    @Test
    fun `CELEBRATORY when no habits configured`() {
        // ratio defaults to 1.0 when habitsTotal == 0
        assertThat(generator.selectTone(base.copy(habitsCompleted = 0, habitsTotal = 0, daysAheadOrBehind = 1)))
            .isEqualTo(CoachTone.CELEBRATORY)
    }

    @Test
    fun `GENTLE_NUDGE when ratio below 0_5`() {
        assertThat(generator.selectTone(base.copy(habitsCompleted = 2, habitsTotal = 5, daysAheadOrBehind = 5)))
            .isEqualTo(CoachTone.GENTLE_NUDGE)
    }

    @Test
    fun `GENTLE_NUDGE when zero habits completed out of many`() {
        assertThat(generator.selectTone(base.copy(habitsCompleted = 0, habitsTotal = 4, daysAheadOrBehind = 10)))
            .isEqualTo(CoachTone.GENTLE_NUDGE)
    }

    @Test
    fun `ENCOURAGING when ratio between 0_5 and 0_8 and ahead`() {
        assertThat(generator.selectTone(base.copy(habitsCompleted = 3, habitsTotal = 5, daysAheadOrBehind = 1)))
            .isEqualTo(CoachTone.ENCOURAGING)
    }

    @Test
    fun `ENCOURAGING when ratio between 0_5 and 0_8 and behind`() {
        assertThat(generator.selectTone(base.copy(habitsCompleted = 3, habitsTotal = 5, daysAheadOrBehind = -2)))
            .isEqualTo(CoachTone.ENCOURAGING)
    }

    @Test
    fun `NEUTRAL when ratio at least 0_8 but behind schedule`() {
        assertThat(generator.selectTone(base.copy(habitsCompleted = 4, habitsTotal = 5, daysAheadOrBehind = -3)))
            .isEqualTo(CoachTone.NEUTRAL)
    }

    // ── Body interpolation ──────────────────────────────────────────────────

    @Test
    fun `body contains goal name`() = runTest {
        val msg = generator.generate(base.copy(habitsCompleted = 3, habitsTotal = 5))
        assertThat(msg.body).contains("Buy a House")
    }

    @Test
    fun `body contains habit counts`() = runTest {
        val msg = generator.generate(base.copy(habitsCompleted = 3, habitsTotal = 5))
        assertThat(msg.body).contains("3")
        assertThat(msg.body).contains("5")
    }

    @Test
    fun `body contains pending habit when present`() = runTest {
        val msg = generator.generate(base.copy(habitsCompleted = 3, habitsTotal = 5, topPendingHabit = "Morning workout"))
        assertThat(msg.body).contains("Morning workout")
    }

    @Test
    fun `body omits pending habit phrase when null`() = runTest {
        val msg = generator.generate(base.copy(habitsCompleted = 5, habitsTotal = 5, topPendingHabit = null))
        assertThat(msg.body).doesNotContain("Next up:")
    }

    @Test
    fun `body references schedule delta for ahead case`() = runTest {
        val msg = generator.generate(base.copy(habitsCompleted = 3, habitsTotal = 5, daysAheadOrBehind = 4))
        assertThat(msg.body).contains("ahead")
    }

    @Test
    fun `body references schedule delta for behind case`() = runTest {
        val msg = generator.generate(base.copy(habitsCompleted = 3, habitsTotal = 5, daysAheadOrBehind = -3))
        assertThat(msg.body).contains("behind")
    }

    @Test
    fun `headline is non-empty for every tone`() = runTest {
        CoachTone.entries.forEach { tone ->
            val input = when (tone) {
                CoachTone.CELEBRATORY -> base.copy(habitsCompleted = 5, habitsTotal = 5, daysAheadOrBehind = 1)
                CoachTone.GENTLE_NUDGE -> base.copy(habitsCompleted = 1, habitsTotal = 5)
                CoachTone.ENCOURAGING -> base.copy(habitsCompleted = 3, habitsTotal = 5)
                CoachTone.NEUTRAL -> base.copy(habitsCompleted = 4, habitsTotal = 5, daysAheadOrBehind = -1)
            }
            assertThat(generator.generate(input).headline).isNotEmpty()
        }
    }

    @Test
    fun `different days produce different headlines for CELEBRATORY`() = runTest {
        val celebInput = base.copy(habitsCompleted = 5, habitsTotal = 5, daysAheadOrBehind = 1)
        val day0 = celebInput.copy(date = LocalDate.ofYearDay(2024, 3))
        val day1 = celebInput.copy(date = LocalDate.ofYearDay(2024, 4))
        val day2 = celebInput.copy(date = LocalDate.ofYearDay(2024, 5))
        val headlines = setOf(
            generator.generate(day0).headline,
            generator.generate(day1).headline,
            generator.generate(day2).headline,
        )
        assertThat(headlines.size == 1).isFalse()
    }
}
