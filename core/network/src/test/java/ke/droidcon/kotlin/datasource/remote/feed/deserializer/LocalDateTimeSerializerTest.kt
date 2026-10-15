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
package ke.droidcon.kotlin.datasource.remote.feed.deserializer

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class LocalDateTimeSerializerTest {
    private val json = Json
    private val serializer = LocalDateTimeSerializer()
    private val eight = LocalDateTime.of(2025, 11, 5, 8, 0)

    @Test
    fun `a venue-local time with no offset reads as is`() {
        assertEquals(eight, json.decodeFromString(serializer, "\"2025-11-05 08:00:00\""))
    }

    @Test
    fun `an ISO time with an offset reads as the venue's local time`() {
        assertEquals(eight, json.decodeFromString(serializer, "\"2025-11-05T08:00:00+03:00\""))
        assertEquals(eight, json.decodeFromString(serializer, "\"2025-11-05T05:00:00Z\""))
    }

    @Test
    fun `it writes the venue-local form`() {
        assertEquals("\"2025-11-05 08:00:00\"", json.encodeToString(serializer, eight))
    }
}