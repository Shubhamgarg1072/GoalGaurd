package com.time.applauncher.goalgaurd.cloud

import com.time.applauncher.goalgaurd.core.domain.CoachInput
import com.time.applauncher.goalgaurd.core.domain.CoachMessage
import com.time.applauncher.goalgaurd.core.domain.CoachTextGenerator
import com.time.applauncher.goalgaurd.core.domain.CoachTone
import com.time.applauncher.goalgaurd.shared.api.GoalGuardApi
import com.time.applauncher.goalgaurd.shared.model.CoachInputDto
import kotlinx.datetime.toKotlinLocalDate
import com.time.applauncher.goalgaurd.shared.util.Result as SharedResult

/**
 * Tier-2 coach generator backed by the server's `/coach/generate` endpoint. Throws on any
 * failure so the surrounding [com.time.applauncher.goalgaurd.feature.coach.domain.FallbackCoachTextGenerator]
 * falls back to the on-device template — keeping the coach on a never-failing local path.
 */
class RemoteCoachTextGenerator(
    private val api: GoalGuardApi,
) : CoachTextGenerator {

    override suspend fun generate(input: CoachInput): CoachMessage {
        val dto = CoachInputDto(
            date = input.date.toKotlinLocalDate(),
            habitsCompleted = input.habitsCompleted,
            habitsTotal = input.habitsTotal,
            focusMinutes = input.focusMinutes,
            socialMinutes = input.socialMinutes,
            primaryGoalName = input.primaryGoalName,
            primaryGoalPct = input.primaryGoalPct,
            daysAheadOrBehind = input.daysAheadOrBehind,
            topPendingHabit = input.topPendingHabit,
            currentStreak = input.currentStreak,
        )
        return when (val result = api.generateCoachMessage(dto)) {
            is SharedResult.Success -> CoachMessage(
                headline = result.data.headline,
                body = result.data.body,
                tone = runCatching { CoachTone.valueOf(result.data.tone.name) }.getOrDefault(CoachTone.ENCOURAGING),
            )
            is SharedResult.Error -> error("Remote coach failed: ${result.error}")
        }
    }
}
