package com.time.applauncher.goalgaurd.core.domain

enum class CoachTone { CELEBRATORY, ENCOURAGING, NEUTRAL, GENTLE_NUDGE }

data class CoachMessage(val headline: String, val body: String, val tone: CoachTone)

interface CoachTextGenerator {
    suspend fun generate(input: CoachInput): CoachMessage
}
