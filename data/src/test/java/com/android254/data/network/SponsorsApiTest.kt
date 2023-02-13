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

import com.android254.data.network.apis.SponsorsApi
import com.android254.data.network.models.responses.SponsorDTO
import com.android254.data.network.models.responses.SponsorsPagedResponse
import com.android254.data.network.util.HttpClientFactory
import com.android254.domain.models.DataResult
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SponsorsApiTest {

    @Test
    fun `test successful fetching sponsors`() {
        // Arrange
        val expectedResult = SponsorsPagedResponse(
            data = listOf(
                SponsorDTO(
                    name = "AABC",
                    tagline = "abc",
                    link = "abc",
                    createdAt = "abc",
                    logo = "abc"
                )
            ),
        )
        val mockHttpEngine = MockEngine {
            respond(
                content = Json.encodeToString(expectedResult),
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClientFactory(MockTokenProvider())
            .create(mockHttpEngine)

        runBlocking {
            // Act
            val response = SponsorsApi(httpClient).fetchSponsors()

            // Assert
            assertThat(response is DataResult.Success, `is`(true))
            val result = (response as DataResult.Success).data
            val noOfItems = result.data.size
            assertThat(noOfItems, `is`(1))
            val itemName = result.data[0].name
            assertThat(itemName, `is`("AABC"))
        }
    }

    @Test
    fun `test fetching sponsors fails with an exception`() {
        // Arrange
        val mockEngine = MockEngine {
            respondError(HttpStatusCode.InternalServerError)
        }
        val httpClient = HttpClientFactory(MockTokenProvider()).create(mockEngine)
        runBlocking {
            // Act
            val response = SponsorsApi(httpClient).fetchSponsors()
            // Assert
            assertThat(response is DataResult.Error, `is`(true))
            val message = (response as DataResult.Error).message
            assertThat(message, `is`("Server error"))
        }
    }
}