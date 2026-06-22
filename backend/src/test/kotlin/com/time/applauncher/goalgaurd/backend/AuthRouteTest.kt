package com.time.applauncher.goalgaurd.backend

import com.time.applauncher.goalgaurd.backend.config.AppConfig
import com.time.applauncher.goalgaurd.backend.db.DatabaseFactory
import com.time.applauncher.goalgaurd.shared.api.GoalGuardJson
import com.time.applauncher.goalgaurd.shared.model.AuthResponse
import com.time.applauncher.goalgaurd.shared.model.GoogleSignInRequest
import com.time.applauncher.goalgaurd.shared.model.SyncPayload
import com.time.applauncher.goalgaurd.shared.model.SyncRequest
import com.time.applauncher.goalgaurd.shared.model.SyncResponse
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import com.time.applauncher.goalgaurd.shared.model.GoalSyncDto
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.assertEquals

@OptIn(ExperimentalEncodingApi::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthRouteTest {

    // No GOOGLE_WEB_CLIENT_ID → insecure dev verification (decodes the unsigned token payload).
    private val config = AppConfig.fromEnv(mapOf("DB_URL" to "", "JWT_SECRET" to "test-secret"))
        .copy(db = com.time.applauncher.goalgaurd.backend.config.DbConfig(
            "jdbc:h2:mem:routetest;DB_CLOSE_DELAY=-1;MODE=PostgreSQL", "org.h2.Driver", "sa", "",
        ))

    @BeforeAll
    fun setup() = DatabaseFactory.init(config.db)

    private fun fakeGoogleToken(sub: String, email: String): String {
        val payload = Base64.UrlSafe.encode("""{"sub":"$sub","email":"$email","name":"Test"}""".toByteArray())
            .trimEnd('=')
        return "e30.$payload.sig"
    }

    @Test
    fun signIn_then_authenticatedSync_roundTrips() = testApplication {
        application { module(config) }
        val client = createClient { install(ContentNegotiation) { json(GoalGuardJson) } }

        val auth: AuthResponse = client.post("/auth/google") {
            contentType(ContentType.Application.Json)
            setBody(GoogleSignInRequest(fakeGoogleToken("42", "t@example.com")))
        }.body()
        assertEquals("g_42", auth.user.id)

        val me = client.get("/me") { bearerAuth(auth.accessToken) }
        assertEquals(HttpStatusCode.OK, me.status)

        // Unauthenticated sync is rejected.
        assertEquals(HttpStatusCode.Unauthorized, client.post("/sync") {
            contentType(ContentType.Application.Json); setBody(SyncRequest())
        }.status)

        // Authenticated sync pushes and pulls back a goal.
        val goal = GoalSyncDto(
            "g1", "House", "🏠", 100.0, 25.0, "%", LocalDate(2030, 1, 1), "HIGH",
            LocalDate(2026, 1, 1), Instant.parse("2026-06-18T00:00:00Z"),
        )
        val sync: SyncResponse = client.post("/sync") {
            bearerAuth(auth.accessToken)
            contentType(ContentType.Application.Json)
            setBody(SyncRequest(changes = SyncPayload(goals = listOf(goal))))
        }.body()
        assertEquals(25.0, sync.changes.goals.single { it.id == "g1" }.currentValue)
    }
}
