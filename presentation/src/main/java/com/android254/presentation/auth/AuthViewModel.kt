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

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.android254.domain.models.DataResult
import com.android254.domain.repos.AuthRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val googleSignInHandler: GoogleSignInHandler,
    private val authRepo: AuthRepo
) : ViewModel() {

    fun getSignInIntent() = googleSignInHandler.getSignInIntent()

    suspend fun submitGoogleToken(intent: Intent?): Boolean {
        val idToken = googleSignInHandler.getIdToken(intent)
        Timber.i("Id token is $idToken")
        // Submit ID Token to API
        // to get access token
        if (idToken == null) {
            return false
        }
        Timber.i("Fetching API token")
        return when (authRepo.getAndSaveApiToken(idToken)) {
            is DataResult.Success -> {
                true
            }
            else -> {
                false
            }
        }
    }
}