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

import com.android254.domain.models.Session
import com.android254.domain.models.Speaker
import com.android254.presentation.sessions.mappers.toCalendarEventOrNull
import com.android254.presentation.sessions.mappers.toSessionDetailsPresentationModal
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Instant

class CalendarExportTest {
    private val sampleEvent =
        CalendarEvent(
            id = "sess-42",
            title = "Compose Adaptive Layouts",
            description = "Deep dive into multi-pane Compose screens.",
            venue = "Room A, Main Hall",
            speakers = persistentListOf("Jane Doe", "John Smith"),
            startEpochMillis = Instant.parse("2026-11-05T07:00:00Z").toEpochMilliseconds(),
            endEpochMillis = Instant.parse("2026-11-05T08:00:00Z").toEpochMilliseconds(),
        )

    @Test
    fun `buildEventDescription joins summary, speakers line, and session deep link`() {
        val description = buildEventDescription(sampleEvent, speakersLine = "Speakers: Jane Doe, John Smith")

        assertEquals(
            "Deep dive into multi-pane Compose screens.\n\nSpeakers: Jane Doe, John Smith\n\ndroidconke://session/sess-42",
            description,
        )
    }

    @Test
    fun `buildEventDescription omits blank summary, blank speakers, and blank id`() {
        val sparse = sampleEvent.copy(id = "   ", description = "  ", speakers = persistentListOf())

        assertEquals("", buildEventDescription(sparse, speakersLine = "  "))
    }

    @Test
    fun `buildEventDescription truncates long text with ellipsis without splitting surrogate pairs`() {
        // Place a 2-code-unit emoji directly across index 499..500.
        val prefix = "a".repeat(499)
        val longDescription = "$prefix🎉 trailing text"
        val description = buildEventDescription(sampleEvent.copy(description = longDescription))
        val summaryPart = description.substringBefore("\n\n")

        assertTrue(summaryPart.endsWith("…"))
        assertEquals(500, summaryPart.length)
        assertFalse(summaryPart.any { Character.isHighSurrogate(it) || Character.isLowSurrogate(it) })
    }

    @Test
    fun `session mapper parses Nairobi local timestamps and multi-room venues into CalendarEvent`() {
        val session =
            baseSession(
                startDateTime = "2025-11-05 08:00:00",
                endDateTime = "2025-11-05 09:00:00",
                rooms = "Room 1, Room 2",
            )

        val event = session.toSessionDetailsPresentationModal().toCalendarEventOrNull()

        assertNotNull(event)
        assertEquals("sess-1", event!!.id)
        assertEquals("Room 1, Room 2", event.venue)
        assertEquals(Instant.parse("2025-11-05T05:00:00Z").toEpochMilliseconds(), event.startEpochMillis)
        assertEquals(Instant.parse("2025-11-05T06:00:00Z").toEpochMilliseconds(), event.endEpochMillis)
        assertEquals(listOf("Jane Doe"), event.speakers)
    }

    @Test
    fun `session mapper parses ISO-8601 timestamps with explicit offsets`() {
        val session =
            baseSession(
                startDateTime = "2025-11-05T08:00:00+03:00",
                endDateTime = "2025-11-05T06:00:00Z",
            )

        val event = session.toSessionDetailsPresentationModal().toCalendarEventOrNull()

        assertNotNull(event)
        assertEquals(Instant.parse("2025-11-05T05:00:00Z").toEpochMilliseconds(), event!!.startEpochMillis)
        assertEquals(Instant.parse("2025-11-05T06:00:00Z").toEpochMilliseconds(), event.endEpochMillis)
    }

    @Test
    fun `session mapper prefers domain Instant fields over legacy date strings`() {
        val preferredStart = Instant.parse("2026-11-06T07:00:00Z")
        val preferredEnd = Instant.parse("2026-11-06T08:30:00Z")
        val session =
            baseSession(
                startDateTime = "not-a-date",
                endDateTime = "also-not-a-date",
                startInstant = preferredStart,
                endInstant = preferredEnd,
            )

        val event = session.toSessionDetailsPresentationModal().toCalendarEventOrNull()

        assertNotNull(event)
        assertEquals(preferredStart.toEpochMilliseconds(), event!!.startEpochMillis)
        assertEquals(preferredEnd.toEpochMilliseconds(), event.endEpochMillis)
    }

    @Test
    fun `toCalendarEventOrNull returns null for unparseable times, non-positive duration, or blank id`() {
        assertNull(
            baseSession(startDateTime = "invalid", endDateTime = "2025-11-05 09:00:00")
                .toSessionDetailsPresentationModal()
                .toCalendarEventOrNull(),
        )
        assertNull(
            baseSession(startDateTime = "2025-11-05 09:00:00", endDateTime = "2025-11-05 09:00:00")
                .toSessionDetailsPresentationModal()
                .toCalendarEventOrNull(),
        )
        assertNull(
            baseSession(startDateTime = "2025-11-05 10:00:00", endDateTime = "2025-11-05 09:00:00")
                .toSessionDetailsPresentationModal()
                .toCalendarEventOrNull(),
        )
        assertNull(
            baseSession(id = "   ")
                .toSessionDetailsPresentationModal()
                .toCalendarEventOrNull(),
        )
    }

    private fun baseSession(
        id: String = "sess-1",
        startDateTime: String = "2025-11-05 08:00:00",
        endDateTime: String = "2025-11-05 09:00:00",
        rooms: String = "Main Hall",
        startInstant: Instant? = null,
        endInstant: Instant? = null,
    ) = Session(
        id = id,
        endDateTime = endDateTime,
        endTime = "09:00",
        isBookmarked = false,
        isKeynote = false,
        isServiceSession = false,
        sessionImage = null,
        startDateTime = startDateTime,
        startTime = "08:00",
        rooms = rooms,
        speakers = listOf(Speaker(name = "Jane Doe")),
        remoteId = id,
        description = "Talk abstract",
        sessionFormat = "Session",
        sessionLevel = "Intermediate",
        slug = "talk-slug",
        title = "Talk title",
        eventDay = "5",
        startInstant = startInstant,
        endInstant = endInstant,
    )
}