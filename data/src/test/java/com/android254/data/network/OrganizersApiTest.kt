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

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import com.android254.data.network.apis.OrganizersApi
import com.android254.data.network.models.responses.OrganizersPagedResponse
import com.android254.data.network.util.HttpClientFactory
import com.android254.data.preferences.DefaultTokenProvider
import com.android254.domain.models.DataResult
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OrganizersApiTest {

    private lateinit var testDataStore: DataStore<Preferences>

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        testDataStore =
            PreferenceDataStoreFactory.create(produceFile = { context.preferencesDataStoreFile("test") })
    }

    @Test
    fun `organizers api should return object on valid json`() {
        val validContent = """
            {
                "data": [{
                    "name": "droidconKE",
                    "designation": "Droidcon is a global conference focused on the engineering of Android applications. Droidcon provides a forum for developers to network with other developers, share techniques, announce apps and products, and to learn and teach.",
                    "twitter_handle": "droidconke",
                    "photo": "http://localhost:8000/upload/logo/app2svg.svg",
                    "tagline": "droidconke-40",
                    "link": "active",
                    "created_at": "2019-12-20 14:39:38",
                    "type": "type",
                    "bio": "type"
                }]
            }
        """.trimIndent()
        val mockEngine = MockEngine {
            Json {
                ignoreUnknownKeys = true
            }
            respond(
                content = validContent,
                status = HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClientFactory(DefaultTokenProvider(testDataStore)).create(mockEngine)
        val api = OrganizersApi(httpClient)
        runBlocking {
            val expected = DataResult.Success(
                data = Json.decodeFromString<OrganizersPagedResponse>(validContent)
            )
            val actual = api.fetchOrganizers("individual")
            assertEquals(expected, actual)
        }
    }

    @Test
    fun `organizers api should throw exception on invalid json`() {
        val invalidContent = """
            {
                "data": [{
                    "id": 21,
                    "name": "droidconKE",
                    "email": "hello@droidcon.co.ke",
                    "description": "Droidcon is a global conference focused on the engineering of Android applications. Droidcon provides a forum for developers to network with other developers, share techniques, announce apps and products, and to learn and teach.",
                    "facebook": "appslabmoi",
                    "twitter": "droidconke",
                    "instagram": null,
                    "logo": "http://localhost:8000/upload/logo/app2svg.svg",
                    "slug": "droidconke-40",
                    "status": "active",
                    "created_at": "2019-12-20 14:39:38",
                    "creator": {
                        "id": 11,
                        "name": "Magak Emmanuel",
                        "email": "emashmagak@gmail.com",
                        "created_at": "2020-03-17 18:54:17"
                    },
                    "upcoming_events_count": 1,
                    "total_events_count": 1
                }],
              
                    "paginator": {
                        "count": 21,
                        "per_page": "15",
                        "current_page": 1,
                        "next_page": "http://localhost:8000/api/v1/organizers?per_page=15&page=2",
                        "has_more_pages": true,
                        "next_page_url": "http://localhost:8000/api/v1/organizers?per_page=15&page=2",
                        "previous_page_url": null
                    }
                }
            }
        """.trimIndent()
        val mockEngine = MockEngine {
            respond(
                content = invalidContent,
                status = HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClientFactory(DefaultTokenProvider(testDataStore)).create(mockEngine)
        val api = OrganizersApi(httpClient)

        val actual = runBlocking { api.fetchOrganizers("individual") }

        assert(actual is DataResult.Error)
    }
}