package com.time.applauncher.goalgaurd.backend.config

/** All configuration comes from environment variables, with dev-friendly defaults. */
data class AppConfig(
    val port: Int,
    val jwtSecret: String,
    val jwtIssuer: String,
    val jwtAudience: String,
    val accessTokenMinutes: Long,
    val refreshTokenDays: Long,
    val googleWebClientId: String?,
    val anthropicApiKey: String?,
    val coachModel: String,
    val db: DbConfig,
) {
    /** True when no Google client ID is set — verifies ID tokens insecurely (DEV ONLY). */
    val insecureGoogleAuth: Boolean get() = googleWebClientId.isNullOrBlank()

    companion object {
        fun fromEnv(env: Map<String, String> = System.getenv()): AppConfig {
            val dbUrl = env["DB_URL"]
            return AppConfig(
                port = env["PORT"]?.toIntOrNull() ?: 8080,
                jwtSecret = env["JWT_SECRET"] ?: "dev-insecure-secret-change-me",
                jwtIssuer = env["JWT_ISSUER"] ?: "goalguard-backend",
                jwtAudience = env["JWT_AUDIENCE"] ?: "goalguard-app",
                accessTokenMinutes = env["ACCESS_TOKEN_MINUTES"]?.toLongOrNull() ?: 60,
                refreshTokenDays = env["REFRESH_TOKEN_DAYS"]?.toLongOrNull() ?: 60,
                googleWebClientId = env["GOOGLE_WEB_CLIENT_ID"],
                anthropicApiKey = env["ANTHROPIC_API_KEY"],
                coachModel = env["COACH_MODEL"] ?: "claude-opus-4-8",
                db = if (dbUrl != null) {
                    DbConfig(
                        url = dbUrl,
                        driver = "org.postgresql.Driver",
                        user = env["DB_USER"] ?: "postgres",
                        password = env["DB_PASSWORD"] ?: "postgres",
                    )
                } else {
                    // In-memory H2 fallback so the server runs with zero infra.
                    DbConfig(
                        url = "jdbc:h2:mem:goalguard;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
                        driver = "org.h2.Driver",
                        user = "sa",
                        password = "",
                    )
                },
            )
        }
    }
}

data class DbConfig(
    val url: String,
    val driver: String,
    val user: String,
    val password: String,
)
