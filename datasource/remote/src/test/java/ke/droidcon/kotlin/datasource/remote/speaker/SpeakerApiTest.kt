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
package ke.droidcon.kotlin.datasource.remote.speaker

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.mockk.mockk
import ke.droidcon.kotlin.datasource.remote.speakers.SpeakersApi
import ke.droidcon.kotlin.datasource.remote.speakers.model.SpeakerDTO
import ke.droidcon.kotlin.datasource.remote.speakers.model.SpeakersPagedResponse
import ke.droidcon.kotlin.datasource.remote.utils.DataResult
import ke.droidcon.kotlin.datasource.remote.utils.HttpClientFactory
import ke.droidcon.kotlin.datasource.remote.utils.MockTokenProvider
import ke.droidcon.kotlin.datasource.remote.utils.RemoteFeatureToggle
import ke.droidcon.kotlin.datasource.remote.utils.SamplePaginationMetaData
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test

class SpeakerApiTest {
    private lateinit var remoteFeatureToggleTest: RemoteFeatureToggle

    @Before
    fun setup() {
        remoteFeatureToggleTest = RemoteFeatureToggle(mockk(relaxed = true))
    }

    @Test
    fun `test DataResult error is returned when http client returns server error response`() {
        val mockHttpEngine = MockEngine {
            respondError(HttpStatusCode.InternalServerError)
        }
        val httpClient = HttpClientFactory(MockTokenProvider(), remoteFeatureToggleTest)
            .create(mockHttpEngine)

        runBlocking {
            val result = SpeakersApi(httpClient).fetchSpeakers()
            assert(result is DataResult.Error)
        }
    }

    @Test
    fun `test DataResult error is returned when http client returns error response beside server error`() {
        val mockHttpEngine = MockEngine {
            respondError(HttpStatusCode.NotFound)
        }
        val httpClient = HttpClientFactory(MockTokenProvider(), remoteFeatureToggleTest)
            .create(mockHttpEngine)

        runBlocking {
            val result = SpeakersApi(httpClient).fetchSpeakers()
            assert(result is DataResult.Error)
        }
    }

    @Test
    fun `test successful speakers fetch`() {
        // GIVEN
        val expectedResponse = SpeakersPagedResponse(
            data = listOf(
                SpeakerDTO(
                    name = "John Doe",
                    bio = "Very cool guy",
                    avatar = "https://example.com",
                    tagline = "Wassup",
                    twitter = null
                )
            ),
            meta = SamplePaginationMetaData
        )
        val mockHttpEngine = MockEngine {
            // To ensure correct http method and url are used
            respond(
                content = Json.encodeToString(expectedResponse),
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClientFactory(MockTokenProvider(), remoteFeatureToggleTest)
            .create(mockHttpEngine)

        runBlocking {
            // WHEN
            val response = SpeakersApi(httpClient).fetchSpeakers()

            // THEN
            assertThat(response, `is`(DataResult.Success(expectedResponse)))
        }
    }
}