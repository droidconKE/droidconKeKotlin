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

import com.android254.data.network.models.payloads.Feedback
import com.android254.data.network.apis.FeedbackApi
import com.android254.data.network.models.payloads.FeedbackRating
import com.android254.data.network.util.HttpClientFactory
import com.android254.domain.models.DataResult

import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.http.content.*

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json

import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class FeedbackApiTest {
    @Test
    fun `sends correct http request`() = runTest {
        val mockEngine = MockEngine { respondOk() }
        val httpClient = HttpClientFactory(MockTokenProvider()).create(mockEngine)

        val sessionId = "1"
        FeedbackApi(httpClient).postFeedback(
            Feedback(rating = FeedbackRating.GOOD, message = "Was nice"),
            sessionId
        )

        mockEngine.requestHistory.first().run {
            val expectedUrl = "${Constants.EVENT_BASE_URL}/feedback/sessions/$sessionId"
            assertThat(url.toString(), `is`(expectedUrl))
            assertThat(method, `is`(HttpMethod.Post))
            assertThat(body.toJsonString(), `is`("""{"rating":3,"message":"Was nice"}"""))
        }
    }

    @Test
    fun `when request succeeds, returns empty Success result`() = runTest {
        val mockEngine = MockEngine { respondOk() }
        val httpClient = HttpClientFactory(MockTokenProvider()).create(mockEngine)

        val sessionId = "1"
        val result = FeedbackApi(httpClient).postFeedback(
            Feedback(rating = FeedbackRating.GOOD, message = "Was nice"),
            sessionId
        )

        assertThat(result, `is`(DataResult.Success(Unit)))
    }

    @Test
    fun `when request fails, returns Error result`() = runTest {
        val mockEngine = MockEngine { respondError(HttpStatusCode.InternalServerError) }
        val httpClient = HttpClientFactory(MockTokenProvider()).create(mockEngine)

        val sessionId = "1"
        val result = FeedbackApi(httpClient).postFeedback(
            Feedback(rating = FeedbackRating.BAD, message = "Food haikuwa na nyama"),
            sessionId
        )

        assertThat(result, `is`(instanceOf(DataResult.Error::class.java)))
    }

    private suspend fun OutgoingContent.toJsonString() =
        // Parse to JsonElement to remove pretty-print formatting
        Json.parseToJsonElement(toByteArray().decodeToString()).toString()
}