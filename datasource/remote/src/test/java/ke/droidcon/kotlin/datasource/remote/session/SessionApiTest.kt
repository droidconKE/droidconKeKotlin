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
package ke.droidcon.kotlin.datasource.remote.session

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.mockk.mockk
import ke.droidcon.kotlin.datasource.remote.sessions.SessionsApi
import ke.droidcon.kotlin.datasource.remote.sessions.model.EventScheduleGroupedResponse
import ke.droidcon.kotlin.datasource.remote.utils.HttpClientFactory
import ke.droidcon.kotlin.datasource.remote.utils.MockTokenProvider
import ke.droidcon.kotlin.datasource.remote.utils.RemoteFeatureToggle
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test

class SessionApiTest {
    private lateinit var remoteFeatureToggleTest: RemoteFeatureToggle

    @Before
    fun setup() {
        remoteFeatureToggleTest = RemoteFeatureToggle(mockk(relaxed = true))
    }

    @Test
    fun `sessions are fetched successfully`() {
        val expectedResponse =
            EventScheduleGroupedResponse(
                data = emptyMap()
            )

        val responseText = """
            {
               data: {}
            }
        """.trimIndent()
        val mockHttpEngine = MockEngine {
            respond(
                content = responseText,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClientFactory(MockTokenProvider(), remoteFeatureToggleTest).create(mockHttpEngine)

        runBlocking {
            // WHEN
            val response = SessionsApi(httpClient).fetchSessions()
            // THEN
            assertThat(response, `is`(expectedResponse))
        }
    }
}