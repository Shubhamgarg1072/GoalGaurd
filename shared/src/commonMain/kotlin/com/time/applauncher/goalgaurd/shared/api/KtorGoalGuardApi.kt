package com.time.applauncher.goalgaurd.shared.api

import com.time.applauncher.goalgaurd.shared.model.AuthResponse
import com.time.applauncher.goalgaurd.shared.model.CoachInputDto
import com.time.applauncher.goalgaurd.shared.model.CoachMessageDto
import com.time.applauncher.goalgaurd.shared.model.GoogleSignInRequest
import com.time.applauncher.goalgaurd.shared.model.InsightsSummaryDto
import com.time.applauncher.goalgaurd.shared.model.RefreshRequest
import com.time.applauncher.goalgaurd.shared.model.SyncRequest
import com.time.applauncher.goalgaurd.shared.model.SyncResponse
import com.time.applauncher.goalgaurd.shared.model.UserDto
import com.time.applauncher.goalgaurd.shared.util.NetworkError
import com.time.applauncher.goalgaurd.shared.util.Result
import com.time.applauncher.goalgaurd.shared.util.safeCall
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import kotlinx.datetime.LocalDate

/** Builds a [GoalGuardApi] with the default platform HTTP engine, hiding Ktor from callers. */
fun createGoalGuardApi(baseUrl: String, tokens: TokenProvider = NoopTokenProvider): GoalGuardApi =
    KtorGoalGuardApi(GoalGuardHttpClientFactory.create(), baseUrl, tokens)

class KtorGoalGuardApi(
    private val client: HttpClient,
    private val baseUrl: String,
    private val tokens: TokenProvider = NoopTokenProvider,
) : GoalGuardApi {

    private fun url(path: String) = "${baseUrl.trimEnd('/')}/$path"

    override suspend fun signInWithGoogle(idToken: String): Result<AuthResponse, NetworkError> =
        safeCall {
            client.post(url("auth/google")) { setBody(GoogleSignInRequest(idToken)) }
        }

    override suspend fun refresh(refreshToken: String): Result<AuthResponse, NetworkError> =
        safeCall {
            client.post(url("auth/refresh")) { setBody(RefreshRequest(refreshToken)) }
        }

    override suspend fun me(): Result<UserDto, NetworkError> =
        authed { client.get(url("me")) { it() } }

    override suspend fun sync(request: SyncRequest): Result<SyncResponse, NetworkError> =
        authed { client.post(url("sync")) { it(); setBody(request) } }

    override suspend fun generateCoachMessage(input: CoachInputDto): Result<CoachMessageDto, NetworkError> =
        authed { client.post(url("coach/generate")) { it(); setBody(input) } }

    override suspend fun insightsSummary(from: LocalDate, to: LocalDate): Result<InsightsSummaryDto, NetworkError> =
        authed {
            client.get(url("insights/summary")) {
                it()
                parameter("from", from.toString())
                parameter("to", to.toString())
            }
        }

    /**
     * Runs an authenticated call, injecting the bearer token via the supplied builder hook.
     * On 401 it refreshes the tokens once and retries; persistent 401 clears the session.
     */
    private suspend inline fun <reified T> authed(
        crossinline execute: suspend (auth: HttpRequestBuilder.() -> Unit) -> HttpResponse,
    ): Result<T, NetworkError> {
        val access = tokens.accessToken()
        val first = safeCall<T> { execute { bearer(access) } }
        if (first !is Result.Error || first.error != NetworkError.UNAUTHORIZED) return first

        val refreshToken = tokens.refreshToken() ?: return first
        return when (val refreshed = refresh(refreshToken)) {
            is Result.Success -> {
                tokens.updateTokens(refreshed.data.accessToken, refreshed.data.refreshToken)
                safeCall { execute { bearer(refreshed.data.accessToken) } }
            }
            is Result.Error -> {
                tokens.clear()
                first
            }
        }
    }

    private fun HttpRequestBuilder.bearer(token: String?) {
        if (token != null) header(HttpHeaders.Authorization, "Bearer $token")
    }
}
