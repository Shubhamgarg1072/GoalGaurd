package com.time.applauncher.goalgaurd.feature.coach.domain

import com.time.applauncher.goalgaurd.core.domain.CoachInput
import com.time.applauncher.goalgaurd.core.domain.CoachMessage
import com.time.applauncher.goalgaurd.core.domain.CoachTextGenerator

// TODO: Wire a real on-device model (MediaPipe LLM Inference / Gemini Nano) or a stateless
//       Cloudflare Worker proxy as `primary` when ready — this is the seam for Tier-2 generators.
class FallbackCoachTextGenerator(
    private val primary: CoachTextGenerator,
    private val fallback: CoachTextGenerator,
) : CoachTextGenerator {

    override suspend fun generate(input: CoachInput): CoachMessage {
        return try {
            primary.generate(input)
        } catch (_: Exception) {
            fallback.generate(input)
        }
    }
}
