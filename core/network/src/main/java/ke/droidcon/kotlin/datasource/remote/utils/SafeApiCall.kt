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
package ke.droidcon.kotlin.datasource.remote.utils

import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import timber.log.Timber
import java.io.IOException
import java.nio.channels.UnresolvedAddressException

suspend fun <T : Any> dataResultSafeApiCall(apiCall: suspend () -> T): DataResult<T> =
    try {
        DataResult.Success(apiCall.invoke())
    } catch (e: ServerResponseException) {
        serverError(e)
    } catch (e: NoTransformationFoundException) {
        serverError(e)
    } catch (e: UnresolvedAddressException) {
        networkError(e)
    } catch (e: ConnectTimeoutException) {
        networkError(e)
    } catch (e: SocketTimeoutException) {
        networkError(e)
    } catch (e: HttpRequestTimeoutException) {
        networkError(e)
    } catch (e: IOException) {
        networkError(e)
    } catch (e: Exception) {
        Timber.e(e)
        DataResult.Error("Client error", exc = e)
    }

private fun serverError(e: Throwable): DataResult<Nothing> {
    Timber.e(e)
    return DataResult.Error("Server error", exc = e)
}

private fun networkError(e: Throwable): DataResult<Nothing> {
    Timber.e(e)
    return DataResult.Error("Network error", exc = e, networkError = true)
}