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
package com.android254.data.repos

import com.android254.data.di.IoDispatcher
import com.android254.data.network.apis.AuthApi
import com.android254.data.network.models.payloads.GoogleToken
import com.android254.data.network.util.NetworkError
import com.android254.data.network.util.ServerError
import com.android254.data.network.util.TokenProvider
import com.android254.domain.models.DataResult
import com.android254.domain.models.Success
import com.android254.domain.repos.AuthRepo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(
    private val api: AuthApi,
    private val tokenProvider: TokenProvider,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AuthRepo {
    override suspend fun getAndSaveApiToken(idToken: String): DataResult<Success> {
        return withContext(ioDispatcher) {
            try {
                val result = api.googleLogin(GoogleToken(idToken))
                tokenProvider.update(result.token)
                DataResult.Success(Success)
            } catch (e: Exception) {
                when (e) {
                    is ServerError, is NetworkError -> {
                        DataResult.Error("Login failed", networkError = true, exc = e)
                    }
                    else -> {
                        DataResult.Error("Login failed", exc = e)
                    }
                }
            }
        }
    }
}