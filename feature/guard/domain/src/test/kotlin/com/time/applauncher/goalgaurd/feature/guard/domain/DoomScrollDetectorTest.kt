package com.time.applauncher.goalgaurd.feature.guard.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import org.junit.Test

class DoomScrollDetectorTest {

    private val detector = DoomScrollDetector()
    private val pkg = "com.instagram.android"
    private val config = DoomScrollConfig(
        enabled = true,
        monitoredPackages = setOf(pkg),
        continuousMinutesThreshold = 15,
        reopenCountThreshold = 5,
        rapidSwitchThreshold = 8,
        cooldownMinutes = 15,
        maxTriggersPerDay = 8,
    )

    private fun reading(
        continuous: Int = 0,
        reopen: Int = 0,
        switches: Int = 0,
        packageName: String = pkg,
    ) = UsageReading(packageName, continuous, reopen, switches)

    @Test
    fun `fires continuous-scroll trigger at threshold`() {
        val trigger = detector.detect(reading(continuous = 15), config, null, 0)

        assertThat(trigger).isNotNull()
        assertThat(trigger!!.reason).isEqualTo(TriggerReason.CONTINUOUS_SCROLL)
        assertThat(trigger.minutesScrolled).isEqualTo(15)
        assertThat(trigger.packageName).isEqualTo(pkg)
    }

    @Test
    fun `does not fire below continuous threshold`() {
        assertThat(detector.detect(reading(continuous = 14), config, null, 0)).isNull()
    }

    @Test
    fun `fires repeated-reopening trigger`() {
        val trigger = detector.detect(reading(reopen = 5), config, null, 0)

        assertThat(trigger).isNotNull()
        assertThat(trigger!!.reason).isEqualTo(TriggerReason.REPEATED_REOPENING)
    }

    @Test
    fun `fires rapid-switching trigger`() {
        val trigger = detector.detect(reading(switches = 8), config, null, 0)

        assertThat(trigger).isNotNull()
        assertThat(trigger!!.reason).isEqualTo(TriggerReason.RAPID_SWITCHING)
    }

    @Test
    fun `continuous-scroll wins when multiple signals fire`() {
        val trigger = detector.detect(reading(continuous = 20, reopen = 9, switches = 9), config, null, 0)

        assertThat(trigger!!.reason).isEqualTo(TriggerReason.CONTINUOUS_SCROLL)
    }

    @Test
    fun `ignores packages that are not monitored`() {
        assertThat(
            detector.detect(reading(continuous = 30, packageName = "com.android.chrome"), config, null, 0),
        ).isNull()
    }

    @Test
    fun `does nothing when guard is disabled`() {
        assertThat(detector.detect(reading(continuous = 30), config.copy(enabled = false), null, 0)).isNull()
    }

    @Test
    fun `respects cooldown window`() {
        // 10 minutes since last trigger, cooldown is 15 → suppressed
        assertThat(detector.detect(reading(continuous = 30), config, minutesSinceLastTrigger = 10, triggersToday = 1))
            .isNull()
        // 15 minutes elapsed → allowed again
        assertThat(detector.detect(reading(continuous = 30), config, minutesSinceLastTrigger = 15, triggersToday = 1))
            .isNotNull()
    }

    @Test
    fun `respects per-day cap`() {
        assertThat(detector.detect(reading(continuous = 30), config, minutesSinceLastTrigger = 60, triggersToday = 8))
            .isNull()
    }
}
