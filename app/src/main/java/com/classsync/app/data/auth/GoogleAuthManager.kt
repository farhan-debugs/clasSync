package com.classsync.app.data.auth

import android.content.Context
import androidx.credentials.*
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class GoogleSignInResult(
    val idToken: String,
    val displayName: String,
    val email: String,
    val profilePictureUri: String?
)

sealed class SignInState {
    object Idle : SignInState()
    object Loading : SignInState()
    data class Success(val result: GoogleSignInResult) : SignInState()
    data class Error(val message: String) : SignInState()
}

@Singleton
class GoogleAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val credentialManager = CredentialManager.create(context)

    /**
     * Launches the Google Sign-In bottom sheet via Credential Manager.
     * Returns [GoogleSignInResult] on success.
     *
     * NOTE: Pass [webClientId] from your Google Cloud OAuth 2.0 Web Client ID.
     */
    suspend fun signIn(
        activityContext: Context,
        webClientId: String
    ): Result<GoogleSignInResult> {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)       // show all accounts
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(
                request = request,
                context = activityContext
            )

            val credential = response.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
                Result.success(
                    GoogleSignInResult(
                        idToken           = googleIdToken.idToken,
                        displayName       = googleIdToken.displayName ?: "User",
                        email             = googleIdToken.id,
                        profilePictureUri = googleIdToken.profilePictureUri?.toString()
                    )
                )
            } else {
                Result.failure(Exception("Unexpected credential type: ${credential.type}"))
            }
        } catch (e: GetCredentialException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (_: Exception) {}
    }
}
