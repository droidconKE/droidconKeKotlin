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
package ke.droidcon.kotlin.core.common.time

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException

class ApiDateTimeTest {
    private val eight: ZonedDateTime = LocalDateTime.of(2025, 11, 5, 8, 0).atZone(VenueZone)

    @Test
    fun `a time with no offset is the venue's`() {
        assertEquals(eight, parseApiDateTime("2025-11-05 08:00:00"))
    }

    @Test
    fun `a time with an offset is the same instant at the venue`() {
        assertEquals(eight, parseApiDateTime("2025-11-05T08:00:00+03:00"))
        assertEquals(eight, parseApiDateTime("2025-11-05T05:00:00Z"))
    }

    @Test
    fun `anything else fails loudly`() {
        assertThrows(DateTimeParseException::class.java) { parseApiDateTime("05/11/2025 08:00") }
    }
}