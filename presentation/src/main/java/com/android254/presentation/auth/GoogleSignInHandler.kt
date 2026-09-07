/*
 * Copyright 2022 DroidconKE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android254.presentation.auth

import android.content.Context
import android.util.Base64
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.qualifiers.ApplicationContext
import ke.droidcon.kotlin.core.ui.R
import timber.log.Timber
import java.security.SecureRandom
import javax.inject.Inject

class GoogleSignInHandler
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val credentialManager = CredentialManager.create(context)

        /** [activityContext] must be an Activity: Credential Manager renders the picker over it. */
        suspend fun signIn(activityContext: Context): Result<String> =
            runCatching {
                val nonce = generateNonce()

                suspend fun attempt(filterByAuthorizedAccounts: Boolean): GetCredentialResponse {
                    val googleIdOption =
                        GetGoogleIdOption
                            .Builder()
                            .setServerClientId(context.getString(R.string.default_web_client_id))
                            .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
                            .setAutoSelectEnabled(filterByAuthorizedAccounts)
                            .setNonce(nonce)
                            .build()

                    return credentialManager.getCredential(
                        context = activityContext,
                        request =
                            GetCredentialRequest
                                .Builder()
                                .addCredentialOption(googleIdOption)
                                .build(),
                    )
                }

                val response =
                    try {
                        attempt(filterByAuthorizedAccounts = true)
                    } catch (e: NoCredentialException) {
                        Timber.d(e, "No previously authorized account; showing the full picker")
                        attempt(filterByAuthorizedAccounts = false)
                    }

                val credential = response.credential
                require(
                    credential is CustomCredential &&
                        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL,
                ) { "Unexpected credential type: ${credential.type}" }

                GoogleIdTokenCredential.createFrom(credential.data).idToken
            }.onFailure { e ->
                when (e) {
                    is GetCredentialCancellationException -> Timber.d("User cancelled sign-in")
                    else -> Timber.e(e, "Google sign-in failed")
                }
            }

        suspend fun signOut() {
            runCatching {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            }.onFailure { Timber.e(it, "Clearing the credential state failed") }
        }

        // Client-generated: the backend does not verify a nonce yet, so this only prevents
        // replay across this app's own sessions.
        private fun generateNonce(): String =
            ByteArray(NONCE_BYTES)
                .also { SecureRandom().nextBytes(it) }
                .let { Base64.encodeToString(it, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING) }

        private companion object {
            const val NONCE_BYTES = 32
        }
    }