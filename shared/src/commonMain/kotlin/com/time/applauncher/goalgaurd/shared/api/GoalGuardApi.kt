package com.time.applauncher.goalgaurd.shared.api

import com.time.applauncher.goalgaurd.shared.model.AuthResponse
import com.time.applauncher.goalgaurd.shared.model.CoachInputDto
import com.time.applauncher.goalgaurd.shared.model.CoachMessageDto
import com.time.applauncher.goalgaurd.shared.model.InsightsSummaryDto
import com.time.applauncher.goalgaurd.shared.model.SyncRequest
import com.time.applauncher.goalgaurd.shared.model.SyncResponse
import com.time.applauncher.goalgaurd.shared.model.UserDto
import com.time.applauncher.goalgaurd.shared.util.NetworkError
import com.time.applauncher.goalgaurd.shared.util.Result
import kotlinx.datetime.LocalDate

/** Single contract for the GoalGuard backend, shared by the Android app and (for types) the server. */
interface GoalGuardApi {
    // Auth
    suspend fun signInWithGoogle(idToken: String): Result<AuthResponse, NetworkError>
    suspend fun refresh(refreshToken: String): Result<AuthResponse, NetworkError>
    suspend fun me(): Result<UserDto, NetworkError>

    // Sync (requires auth)
    suspend fun sync(request: SyncRequest): Result<SyncResponse, NetworkError>

    // Coach proxy (optionally authenticated)
    suspend fun generateCoachMessage(input: CoachInputDto): Result<CoachMessageDto, NetworkError>

    // Insights (requires auth)
    suspend fun insightsSummary(from: LocalDate, to: LocalDate): Result<InsightsSummaryDto, NetworkError>
}
