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
package com.android254.data.network.apis

import com.android254.data.network.models.responses.EventScheduleGroupedResponse
import com.android254.data.network.util.HttpClientFactory
import com.android254.data.network.util.MockTokenProvider
import com.android254.data.network.util.RemoteFeatureToggle
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.hamcrest.CoreMatchers.`is`
import org.junit.Before

class SessionApiTest {
    private lateinit var remoteFeatureToggleTest: RemoteFeatureToggle

    @Before
    fun setup() {
        val remoteConfig: FirebaseRemoteConfig = mockk(relaxed = true)
        remoteFeatureToggleTest = RemoteFeatureToggle(mockk(relaxed = true), remoteConfig)
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