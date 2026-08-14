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

import com.android254.domain.models.DataResult
import com.android254.domain.models.Success
import com.android254.domain.repos.AuthRepo
import ke.droidcon.kotlin.datasource.remote.auth.AuthApi
import ke.droidcon.kotlin.datasource.remote.auth.model.GoogleToken
import ke.droidcon.kotlin.datasource.remote.di.IoDispatcher
import ke.droidcon.kotlin.datasource.remote.utils.TokenProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import ke.droidcon.kotlin.datasource.remote.utils.DataResult as RemoteDataResult

@Singleton
class AuthManager
    @Inject
    constructor(
        private val api: AuthApi,
        private val tokenProvider: TokenProvider,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : AuthRepo {
        override suspend fun getAndSaveApiToken(idToken: String): DataResult<Success> =
            withContext(ioDispatcher) {
                when (val result = api.googleLogin(GoogleToken(idToken))) {
                    is RemoteDataResult.Success -> {
                        tokenProvider.update(result.data.token)
                        DataResult.Success(Success)
                    }
                    is RemoteDataResult.Error ->
                        DataResult.Error(
                            message = "Login failed",
                            networkError = result.networkError,
                            exc = result.exc,
                        )
                    is RemoteDataResult.Loading, RemoteDataResult.Empty ->
                        DataResult.Error("Login failed")
                }
            }
    }