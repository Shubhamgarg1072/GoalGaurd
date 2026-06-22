package com.time.applauncher.goalgaurd.feature.auth.data

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.time.applauncher.goalgaurd.core.domain.DataError
import com.time.applauncher.goalgaurd.core.domain.Result

/**
 * Obtains a Google ID token via Credential Manager (no Firebase). The token is sent to the
 * backend, which verifies it with Google. [webClientId] is the OAuth *web* client ID.
 */
class GoogleCredentialProvider(private val webClientId: String) {

    suspend fun getIdToken(activityContext: Context): Result<String, DataError.Network> {
        val option = GetGoogleIdOption.Builder()
            .setServerClientId(webClientId)
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
        return try {
            val response = CredentialManager.create(activityContext).getCredential(activityContext, request)
            val credential = GoogleIdTokenCredential.createFrom(response.credential.data)
            Result.Success(credential.idToken)
        } catch (e: GetCredentialException) {
            Result.Error(DataError.Network.UNAUTHORIZED)
        } catch (e: Exception) {
            Result.Error(DataError.Network.UNKNOWN)
        }
    }
}
