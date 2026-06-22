package com.time.applauncher.goalgaurd.backend.auth

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.time.applauncher.goalgaurd.backend.config.AppConfig
import org.slf4j.LoggerFactory
import java.util.Base64

/** Identity extracted from a verified Google ID token. */
data class GoogleIdentity(
    val subject: String,
    val email: String,
    val name: String?,
    val pictureUrl: String?,
)

interface GoogleTokenVerifier {
    /** Returns the identity if the token is valid, or null if it can't be trusted. */
    fun verify(idToken: String): GoogleIdentity?

    companion object {
        fun create(config: AppConfig): GoogleTokenVerifier =
            if (config.insecureGoogleAuth) InsecureGoogleTokenVerifier()
            else RealGoogleTokenVerifier(config.googleWebClientId!!)
    }
}

private class RealGoogleTokenVerifier(clientId: String) : GoogleTokenVerifier {
    private val verifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance())
        .setAudience(listOf(clientId))
        .build()

    override fun verify(idToken: String): GoogleIdentity? {
        val token = verifier.verify(idToken) ?: return null
        val payload = token.payload
        return GoogleIdentity(
            subject = payload.subject,
            email = payload.email ?: return null,
            name = payload["name"] as? String,
            pictureUrl = payload["picture"] as? String,
        )
    }
}

/**
 * DEV ONLY. Decodes the ID token payload WITHOUT verifying its signature, so the backend can
 * be exercised locally without a Google client ID. Never enable in production.
 */
private class InsecureGoogleTokenVerifier : GoogleTokenVerifier {
    private val log = LoggerFactory.getLogger(InsecureGoogleTokenVerifier::class.java)
    private val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }

    init {
        log.warn("⚠️  GOOGLE_WEB_CLIENT_ID is unset — verifying Google ID tokens INSECURELY (dev only).")
    }

    override fun verify(idToken: String): GoogleIdentity? = runCatching {
        val payloadJson = String(Base64.getUrlDecoder().decode(idToken.split(".")[1]))
        val obj = json.parseToJsonElement(payloadJson) as kotlinx.serialization.json.JsonObject
        fun str(key: String) = (obj[key] as? kotlinx.serialization.json.JsonPrimitive)?.contentOrNull
        GoogleIdentity(
            subject = str("sub") ?: return null,
            email = str("email") ?: "dev-${str("sub")}@example.com",
            name = str("name"),
            pictureUrl = str("picture"),
        )
    }.getOrNull()
}

private val kotlinx.serialization.json.JsonPrimitive.contentOrNull: String?
    get() = if (isString) content else null
