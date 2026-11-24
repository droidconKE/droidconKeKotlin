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
package com.android254.presentation.common.calendar

import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Instant

class CalendarExportTest {
    private val sampleEvent =
        CalendarEvent(
            id = "sess-42",
            title = "Compose, Adaptive; Layouts\\Deep Dive",
            description = "Line one\nLine two, with; punctuation",
            venue = "Room A, Main Hall",
            speakers = persistentListOf("Jane Doe", "John Smith"),
            startEpochMillis = Instant.parse("2026-11-05T07:00:00Z").toEpochMilliseconds(),
            endEpochMillis = Instant.parse("2026-11-05T08:00:00Z").toEpochMilliseconds(),
        )

    @Test
    fun `buildIcs produces valid RFC 5545 calendar with CRLF, UTC times, escaped fields, and 10m alarm`() {
        val ics =
            buildIcs(
                events = listOf(sampleEvent),
                generatedAt = Instant.parse("2026-11-01T12:00:00Z"),
            )

        assertTrue(ics.contains("BEGIN:VCALENDAR\r\n"))
        assertTrue(ics.contains("X-WR-TIMEZONE:Africa/Nairobi\r\n"))
        assertTrue(ics.contains("UID:sess-42@droidcon.co.ke\r\n"))
        assertTrue(ics.contains("DTSTAMP:20261101T120000Z\r\n"))
        assertTrue(ics.contains("DTSTART:20261105T070000Z\r\n"))
        assertTrue(ics.contains("DTEND:20261105T080000Z\r\n"))
        assertTrue(ics.contains("SUMMARY:Compose\\, Adaptive\\; Layouts\\\\Deep Dive\r\n"))
        assertTrue(ics.contains("LOCATION:Room A\\, Main Hall\r\n"))
        assertTrue(ics.contains("DESCRIPTION:Line one\\nLine two\\, with\\; punctuation\\n\\nSpeakers: Jane Doe\\, John Smith\\n\\ndroidconke://session/sess-42\r\n"))
        assertTrue(ics.contains("BEGIN:VALARM\r\nTRIGGER:-PT10M\r\nACTION:DISPLAY\r\n"))
        assertTrue(ics.endsWith("END:VCALENDAR\r\n"))
    }

    @Test
    fun `buildIcs skips events with non-positive duration`() {
        val invalid = sampleEvent.copy(id = "bad", endEpochMillis = sampleEvent.startEpochMillis)
        val ics =
            buildIcs(
                events = listOf(invalid, sampleEvent),
                generatedAt = Instant.parse("2026-11-01T12:00:00Z"),
            )

        assertFalse(ics.contains("UID:bad@droidcon.co.ke"))
        assertTrue(ics.contains("UID:sess-42@droidcon.co.ke"))
    }
}