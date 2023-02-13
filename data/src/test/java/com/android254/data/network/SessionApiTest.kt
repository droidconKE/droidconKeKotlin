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
package com.android254.data.network

import com.android254.data.network.apis.SessionsApi
import com.android254.data.network.models.responses.EventScheduleGroupedResponse
import com.android254.data.network.util.HttpClientFactory
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.hamcrest.CoreMatchers.`is`

class SessionApiTest {
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

        val httpClient = HttpClientFactory(MockTokenProvider()).create(mockHttpEngine)

        runBlocking {
            // WHEN
            val response = SessionsApi(httpClient).fetchSessions()
            // THEN
            assertThat(response, `is`(expectedResponse))
        }
    }
}