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
package com.android254.domain.schedule

import com.android254.domain.models.ConferencePhase
import com.android254.domain.models.ScheduleConflict
import com.android254.domain.models.Session
import ke.droidcon.kotlin.core.testing.testSession
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.time.Instant

class ScheduleTest {
    @Test
    fun `there is no phase before any timed session has synced`() {
        assertNull(emptyList<Session>().conferencePhase(at("2026-11-06T09:00"), nairobi))
        assertNull(listOf(session("1", null, null)).conferencePhase(at("2026-11-06T09:00"), nairobi))
    }

    @Test
    fun `days until the event are counted in Nairobi, not UTC`() {
        // 22:30 UTC on the 4th is already the 5th in Nairobi, one day before the event.
        val phase = schedule.conferencePhase(Instant.parse("2026-11-04T22:30:00Z"), nairobi)

        assertEquals(ConferencePhase.BeforeEvent(daysUntil = 1), phase)
    }

    @Test
    fun `the morning of day one is before the event, zero days out`() {
        assertEquals(ConferencePhase.BeforeEvent(daysUntil = 0), schedule.conferencePhase(at("2026-11-06T07:00"), nairobi))
    }

    @Test
    fun `from the first session to the last one ending is during the event, overnight included`() {
        assertEquals(ConferencePhase.DuringEvent, schedule.conferencePhase(at("2026-11-06T09:00"), nairobi))
        assertEquals(ConferencePhase.DuringEvent, schedule.conferencePhase(at("2026-11-06T23:00"), nairobi))
        assertEquals(ConferencePhase.DuringEvent, schedule.conferencePhase(at("2026-11-07T11:00"), nairobi))
    }

    @Test
    fun `after the last session ends the event is over`() {
        assertEquals(ConferencePhase.AfterEvent, schedule.conferencePhase(at("2026-11-07T11:01"), nairobi))
    }

    @Test
    fun `the agenda day is today until its last session ends, then the next event day`() {
        assertEquals(LocalDate(2026, 11, 6), schedule.agendaDay(at("2026-10-01T09:00"), nairobi))
        assertEquals(LocalDate(2026, 11, 6), schedule.agendaDay(at("2026-11-06T11:30"), nairobi))
        assertEquals(LocalDate(2026, 11, 7), schedule.agendaDay(at("2026-11-06T12:01"), nairobi))
        assertNull(schedule.agendaDay(at("2026-11-07T12:00"), nairobi))
    }

    @Test
    fun `a session running past midnight does not pull the agenda back to yesterday`() {
        val lateNight = schedule + session("hackathon", "2026-11-06T22:00", "2026-11-07T02:00")

        assertEquals(LocalDate(2026, 11, 7), lateNight.agendaDay(at("2026-11-07T00:30"), nairobi))
    }

    @Test
    fun `the event runs from its first session's day to its last`() {
        assertEquals(LocalDate(2026, 11, 6)..LocalDate(2026, 11, 7), schedule.eventDates(nairobi))
        assertNull(emptyList<Session>().eventDates(nairobi))
    }

    @Test
    fun `only sessions later today are up next`() {
        assertEquals(emptyList<Session>(), schedule.upNextToday(at("2026-10-19T09:00"), nairobi))
        assertEquals(listOf("day 1 last"), schedule.upNextToday(at("2026-11-06T09:30"), nairobi).map { it.id })
        assertEquals(emptyList<Session>(), schedule.upNextToday(at("2026-11-06T23:00"), nairobi))
        assertEquals(listOf("day 2"), schedule.upNextToday(at("2026-11-07T00:30"), nairobi).map { it.id })
    }

    @Test
    fun `starred sessions on a day come in start order`() {
        val day =
            listOf(
                session("late", "2026-11-06T11:00", "2026-11-06T12:00", starred = true),
                session("early", "2026-11-06T09:00", "2026-11-06T10:00", starred = true),
                session("unstarred", "2026-11-06T10:00", "2026-11-06T11:00"),
                session("other day", "2026-11-07T09:00", "2026-11-07T10:00", starred = true),
            )

        assertEquals(listOf("early", "late"), day.starredOn(LocalDate(2026, 11, 6), nairobi).map { it.id })
    }

    @Test
    fun `overlapping starred sessions conflict`() {
        val a = session("a", "2026-11-06T09:00", "2026-11-06T10:00", starred = true)
        val b = session("b", "2026-11-06T09:30", "2026-11-06T10:30", starred = true)

        assertEquals(listOf(ScheduleConflict(a, b)), listOf(b, a).scheduleConflicts())
    }

    @Test
    fun `back to back and barely overlapping sessions do not conflict`() {
        val a = session("a", "2026-11-06T09:00", "2026-11-06T10:00", starred = true)
        val backToBack = session("b", "2026-11-06T10:00", "2026-11-06T11:00", starred = true)
        val rounding = session("c", "2026-11-06T10:56", "2026-11-06T12:00", starred = true)

        assertEquals(emptyList<ScheduleConflict>(), listOf(a, backToBack, rounding).scheduleConflicts())
    }

    @Test
    fun `a long session conflicts with every talk inside it, not only the next one`() {
        val workshop = session("workshop", "2026-11-06T09:00", "2026-11-06T12:00", starred = true)
        val first = session("first", "2026-11-06T09:30", "2026-11-06T10:00", starred = true)
        val second = session("second", "2026-11-06T10:30", "2026-11-06T11:00", starred = true)

        assertEquals(
            listOf(ScheduleConflict(workshop, first), ScheduleConflict(workshop, second)),
            listOf(workshop, first, second).scheduleConflicts(),
        )
    }

    @Test
    fun `only starred sessions conflict`() {
        val a = session("a", "2026-11-06T09:00", "2026-11-06T10:00", starred = true)
        val b = session("b", "2026-11-06T09:30", "2026-11-06T10:30")

        assertEquals(emptyList<ScheduleConflict>(), listOf(a, b).scheduleConflicts())
    }

    private val nairobi = TimeZone.of("Africa/Nairobi")

    private val schedule =
        listOf(
            session("day 1 morning", "2026-11-06T09:00", "2026-11-06T10:00"),
            session("day 1 last", "2026-11-06T11:00", "2026-11-06T12:00"),
            session("day 2", "2026-11-07T10:00", "2026-11-07T11:00"),
        )

    private fun at(nairobiTime: String) = Instant.parse("$nairobiTime:00+03:00")

    private fun session(
        id: String,
        start: String?,
        end: String?,
        starred: Boolean = false,
    ) = testSession(id, start?.let(::at), end?.let(::at), starred)
}