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
package com.android254.data.network.util

import com.android254.domain.models.DataResult
import io.ktor.client.call.*
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.*
import timber.log.Timber

@Deprecated("Use dataResultSafeApiCall")
suspend fun <T> safeApiCall(block: suspend () -> T): T {
    try {
        return block()
    } catch (e: Exception) {
        Timber.e(e)
        when (e) {
            is ServerResponseException, is NoTransformationFoundException -> {
                throw ServerError(e)
            }
            is ConnectTimeoutException -> {
                throw NetworkError()
            }
            else -> throw e
        }
    }
}

class ServerError(cause: Throwable) : Exception(cause)
class NetworkError : Exception()

suspend fun <T : Any> dataResultSafeApiCall(
    apiCall: suspend () -> T
): DataResult<T> = try {
    DataResult.Success(apiCall.invoke())
} catch (throwable: Throwable) {
    Timber.e(throwable)
    when (throwable) {
        is ServerResponseException, is NoTransformationFoundException -> {
            DataResult.Error("Server error", exc = throwable)
        }
        is ConnectTimeoutException -> {
            DataResult.Error("Network error", exc = throwable, networkError = true)
        }
        else -> {
            DataResult.Error("Client error", exc = throwable)
        }
    }
}