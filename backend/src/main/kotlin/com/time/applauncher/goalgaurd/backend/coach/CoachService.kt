package com.time.applauncher.goalgaurd.backend.coach

import com.anthropic.client.okhttp.AnthropicOkHttpClient
import com.anthropic.models.messages.MessageCreateParams
import com.time.applauncher.goalgaurd.backend.config.AppConfig
import com.time.applauncher.goalgaurd.shared.model.CoachInputDto
import com.time.applauncher.goalgaurd.shared.model.CoachMessageDto
import com.time.applauncher.goalgaurd.shared.model.CoachToneDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

/**
 * Server-side coach text. Tries the Anthropic API (key stays on the server); on a missing key
 * or ANY failure it falls back to a deterministic on-server template — mirroring the app's
 * FallbackCoachTextGenerator so the endpoint never hard-fails.
 */
class CoachService(private val config: AppConfig) {
    private val log = LoggerFactory.getLogger(CoachService::class.java)
    private val json = Json { ignoreUnknownKeys = true }

    private val client = config.anthropicApiKey?.let { key ->
        AnthropicOkHttpClient.builder().apiKey(key).build()
    }

    suspend fun generate(input: CoachInputDto): CoachMessageDto {
        val viaApi = client?.let { runCatching { generateWithClaude(input) }.getOrNull() }
        return viaApi ?: template(input)
    }

    private suspend fun generateWithClaude(input: CoachInputDto): CoachMessageDto = withContext(Dispatchers.IO) {
        val prompt = """
            You are a concise, supportive productivity coach. Given today's stats, write a short
            evening summary. Respond with ONLY a JSON object, no prose, in this exact shape:
            {"headline": "...", "body": "...", "tone": "CELEBRATORY|ENCOURAGING|NEUTRAL|GENTLE_NUDGE"}

            Stats:
            - date: ${input.date}
            - habits completed: ${input.habitsCompleted}/${input.habitsTotal}
            - focus minutes: ${input.focusMinutes}
            - social minutes: ${input.socialMinutes}
            - primary goal: ${input.primaryGoalName} (${input.primaryGoalPct}% complete)
            - schedule delta (days): ${input.daysAheadOrBehind} (positive = ahead)
            - top pending habit: ${input.topPendingHabit ?: "none"}
            - current streak: ${input.currentStreak} days

            Keep headline under 8 words and body under 40 words. Reference the goal and the
            schedule delta. Mention the pending habit only if present.
        """.trimIndent()

        val params = MessageCreateParams.builder()
            .model(config.coachModel)
            .maxTokens(512)
            .addUserMessage(prompt)
            .build()

        val response = client!!.messages().create(params)
        val text = response.content()
            .mapNotNull { it.text().orElse(null)?.text() }
            .joinToString("")
            .trim()
            .removePrefix("```json").removePrefix("```").removeSuffix("```").trim()

        val parsed = json.decodeFromString<ClaudeCoachJson>(text)
        CoachMessageDto(
            headline = parsed.headline,
            body = parsed.body,
            tone = runCatching { CoachToneDto.valueOf(parsed.tone) }.getOrDefault(CoachToneDto.ENCOURAGING),
        )
    }

    /** Pure, deterministic fallback. */
    fun template(input: CoachInputDto): CoachMessageDto {
        val ratio = if (input.habitsTotal == 0) 0.0 else input.habitsCompleted.toDouble() / input.habitsTotal
        val tone = when {
            ratio >= 0.8 && input.daysAheadOrBehind >= 0 -> CoachToneDto.CELEBRATORY
            ratio < 0.5 -> CoachToneDto.GENTLE_NUDGE
            input.daysAheadOrBehind < 0 -> CoachToneDto.ENCOURAGING
            else -> CoachToneDto.NEUTRAL
        }
        val schedule = when {
            input.daysAheadOrBehind > 0 -> "${input.daysAheadOrBehind} days ahead of schedule"
            input.daysAheadOrBehind < 0 -> "${-input.daysAheadOrBehind} days behind"
            else -> "right on schedule"
        }
        val headline = when (tone) {
            CoachToneDto.CELEBRATORY -> "Outstanding day!"
            CoachToneDto.ENCOURAGING -> "Keep the momentum"
            CoachToneDto.NEUTRAL -> "Steady progress"
            CoachToneDto.GENTLE_NUDGE -> "Tomorrow is a fresh start"
        }
        val pending = input.topPendingHabit?.let { " Try \"$it\" first tomorrow." } ?: ""
        val body = "You completed ${input.habitsCompleted}/${input.habitsTotal} habits and focused for " +
            "${input.focusMinutes} min. ${input.primaryGoalName} is ${input.primaryGoalPct}% done — you're $schedule." +
            pending
        return CoachMessageDto(headline, body, tone)
    }
}

@Serializable
private data class ClaudeCoachJson(val headline: String, val body: String, val tone: String)
