/*
 * Copyright 2026 DroidconKE
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

import io.ktor.client.network.sockets.ConnectTimeoutException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.SocketException
import java.nio.channels.UnresolvedAddressException

/** Failure classification: connectivity vs server vs client. */
class SafeApiCallTest {
    @Test
    fun `unresolved address is a network failure`() =
        runTest {
            // What Ktor throws when DNS cannot resolve: airplane mode, no signal.
            val result = dataResultSafeApiCall { throw UnresolvedAddressException() }

            assertTrue(result is DataResult.Error)
            assertTrue((result as DataResult.Error).networkError)
            assertEquals("Network error", result.message)
        }

    @Test
    fun `socket exception is a network failure`() =
        runTest {
            val result = dataResultSafeApiCall { throw SocketException("connection reset") }

            assertTrue((result as DataResult.Error).networkError)
        }

    @Test
    fun `generic io exception is a network failure`() =
        runTest {
            val result = dataResultSafeApiCall { throw IOException("broken pipe") }

            assertTrue((result as DataResult.Error).networkError)
        }

    @Test
    fun `connect timeout is a network failure`() =
        runTest {
            val result = dataResultSafeApiCall { throw ConnectTimeoutException("connect timed out") }

            assertTrue((result as DataResult.Error).networkError)
        }

    @Test
    fun `unrecognised failure is a client error and not reported as offline`() =
        runTest {
            val result = dataResultSafeApiCall { throw IllegalStateException("bad state") }

            assertTrue(result is DataResult.Error)
            assertTrue(!(result as DataResult.Error).networkError)
            assertEquals("Client error", result.message)
        }

    @Test
    fun `success passes the value through`() =
        runTest {
            val result = dataResultSafeApiCall { "ok" }

            assertEquals(DataResult.Success("ok"), result)
        }

    @Suppress("DEPRECATION")
    @Test
    fun `safeApiCall maps offline failures to NetworkError and keeps the cause`() =
        runTest {
            val cause = UnresolvedAddressException()

            val thrown =
                runCatching { safeApiCall<String> { throw cause } }.exceptionOrNull()

            assertTrue(thrown is NetworkError)
            assertEquals(cause, thrown!!.cause)
        }
}