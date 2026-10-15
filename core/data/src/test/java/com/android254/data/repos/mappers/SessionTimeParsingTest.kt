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
package com.android254.data.repos.mappers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.format.DateTimeParseException
import kotlin.time.Instant

class SessionTimeParsingTest {
    private val eight = Instant.parse("2025-11-05T08:00:00+03:00").toEpochMilliseconds()

    @Test
    fun `a venue-local time with no offset is Nairobi's`() {
        assertEquals(eight, fromString("2025-11-05 08:00:00"))
    }

    @Test
    fun `an ISO time with an offset is read as sent`() {
        assertEquals(eight, fromString("2025-11-05T08:00:00+03:00"))
        assertEquals(eight, fromString("2025-11-05T05:00:00Z"))
    }

    @Test
    fun `anything else fails loudly rather than landing on the wrong day`() {
        assertThrows(DateTimeParseException::class.java) { fromString("05/11/2025 08:00") }
    }
}