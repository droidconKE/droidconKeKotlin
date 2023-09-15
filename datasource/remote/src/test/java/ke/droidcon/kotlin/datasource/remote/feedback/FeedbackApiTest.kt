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
package ke.droidcon.kotlin.datasource.remote.feedback

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.mockk.mockk
import ke.droidcon.kotlin.datasource.remote.feedback.model.Feedback
import ke.droidcon.kotlin.datasource.remote.feedback.model.FeedbackRating
import ke.droidcon.kotlin.datasource.remote.utils.DataResult
import ke.droidcon.kotlin.datasource.remote.utils.HttpClientFactory
import ke.droidcon.kotlin.datasource.remote.utils.MockTokenProvider
import ke.droidcon.kotlin.datasource.remote.utils.RemoteFeatureToggle
import ke.droidcon.kotlin.datasource.remote.utils.provideEventBaseUrl
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test

class FeedbackApiTest {
    private lateinit var remoteFeatureToggleTest: RemoteFeatureToggle

    @Before
    fun setup() {
        remoteFeatureToggleTest = RemoteFeatureToggle(mockk(relaxed = true))
    }

    @Test
    fun `sends correct http request`() = runTest {
        val mockEngine = MockEngine { respondOk() }
        val httpClient = HttpClientFactory(MockTokenProvider(), remoteFeatureToggleTest).create(mockEngine)

        val sessionId = "1"
        FeedbackApi(httpClient).postFeedback(
            Feedback(rating = FeedbackRating.GOOD, message = "Was nice"),
            sessionId
        )

        mockEngine.requestHistory.first().run {
            val expectedUrl = "${provideEventBaseUrl()}/feedback/sessions/$sessionId"
            assertThat(url.toString(), `is`(expectedUrl))
            assertThat(method, `is`(HttpMethod.Post))
            assertThat(body.toJsonString(), `is`("""{"rating":3,"message":"Was nice"}"""))
        }
    }

    @Test
    fun `when request succeeds, returns empty Success result`() = runTest {
        val mockEngine = MockEngine { respondOk() }
        val httpClient = HttpClientFactory(MockTokenProvider(), remoteFeatureToggleTest).create(mockEngine)

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
        val httpClient = HttpClientFactory(MockTokenProvider(), remoteFeatureToggleTest).create(mockEngine)

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