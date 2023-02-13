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

import com.android254.data.network.models.responses.Feed
import com.android254.data.network.apis.FeedApi
import com.android254.data.network.util.HttpClientFactory
import com.android254.domain.models.DataResult
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class FeedApiTest {
    @Test
    fun `sends correct http request`() = runTest {
        val mockEngine = MockEngine { respondOk() }
        val httpClient = HttpClientFactory(MockTokenProvider()).create(mockEngine)

        FeedApi(httpClient).fetchFeed(page = 2, size = 50)

        assertThat(mockEngine.requestHistory.size, `is`(1))
        mockEngine.requestHistory.first().run {
            val expectedUrl = "${Constants.EVENT_BASE_URL}/feeds?page=2&per_page=50"
            assertThat(url.toString(), `is`(expectedUrl))
            assertThat(method, `is`(HttpMethod.Get))
        }
    }

    @Test
    fun `when successful returns a list of feed items`() = runTest {
        val httpClient = HttpClientFactory(MockTokenProvider()).create(
            MockEngine {
                respond(
                    content = """{
                        "data": [
                            {
                              "title": "Test",
                              "body": "Good one",
                              "topic": "droidconweb",
                              "url": "https://droidcon.co.ke",
                              "image": "http://localhost:8000/upload/event/feeds/dangyntvmaet8jgjpg.jpg",
                              "created_at": "2020-03-19 18:45:49"
                            },
                            {
                              "title": "niko fine",
                              "body": "this is a test",
                              "topic": "droidconweb",
                              "url": "https://droidcon.co.ke",
                              "image": null,
                              "created_at": "2020-03-19 18:43:38"
                            }
                        ],
                        "meta": {
                            "paginator": {
                              "count": 2,
                              "per_page": "10",
                              "current_page": 1,
                              "next_page": null,
                              "has_more_pages": false,
                              "next_page_url": null,
                              "previous_page_url": null
                            }
                        }
                    }
                    """.trimIndent(),
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
        )

        val feed = FeedApi(httpClient).fetchFeed()

        assertThat(
            feed,
            `is`(
                DataResult.Success(
                    listOf(
                        Feed(
                            title = "Test",
                            body = "Good one",
                            topic = "droidconweb",
                            url = "https://droidcon.co.ke",
                            image = "http://localhost:8000/upload/event/feeds/dangyntvmaet8jgjpg.jpg",
                            createdAt = LocalDateTime.of(
                                LocalDate.parse("2020-03-19"),
                                LocalTime.parse("18:45:49")
                            )
                        ),
                        Feed(
                            title = "niko fine",
                            body = "this is a test",
                            topic = "droidconweb",
                            url = "https://droidcon.co.ke",
                            image = null,
                            createdAt = LocalDateTime.of(
                                LocalDate.parse("2020-03-19"),
                                LocalTime.parse("18:43:38")
                            )
                        )
                    )
                )
            )
        )
    }
}