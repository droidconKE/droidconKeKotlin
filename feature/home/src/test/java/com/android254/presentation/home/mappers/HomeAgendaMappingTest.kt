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
package com.android254.presentation.home.mappers

import com.android254.domain.models.Home
import com.android254.presentation.home.viewstate.AgendaDay
import com.android254.presentation.home.viewstate.HomeHero
import ke.droidcon.kotlin.core.testing.testSession
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.time.Instant

class HomeAgendaMappingTest {
    private val nairobi = TimeZone.of("Africa/Nairobi")
    private val keynote = session("keynote", "2026-11-06T09:00", "2026-11-06T10:00", starred = true)
    private val compose = session("compose", "2026-11-06T09:30", "2026-11-06T10:30", starred = true)
    private val kotlin = session("kotlin", "2026-11-06T11:00", "2026-11-06T12:00", starred = true)
    private val dayTwo = session("day two", "2026-11-07T09:00", "2026-11-07T10:00")
    private val home = Home(sessions = listOf(dayTwo, kotlin, compose, keynote))

    @Test
    fun `before the event the hero counts down and the agenda shows day one`() {
        val state = home.toHomeState(isSyncing = false, now = at("2026-10-25T12:00"), zone = nairobi)

        assertEquals(HomeHero.Countdown(daysUntil = 12), state.hero)
        assertEquals(listOf("keynote", "compose", "kotlin"), state.agenda?.items?.map { it.session.id })
        assertEquals(AgendaDay.On(LocalDate(2026, 11, 6)), state.agenda?.day)
    }

    @Test
    fun `during the event the agenda is today's, with clashes named both ways`() {
        val state = home.toHomeState(isSyncing = false, now = at("2026-11-06T09:15"), zone = nairobi)

        assertEquals(HomeHero.Welcome, state.hero)
        assertEquals(AgendaDay.Today, state.agenda?.day)
        val clashes = state.agenda!!.items.associate { it.session.id to it.clashesWith.toList() }
        assertEquals(mapOf("keynote" to listOf("compose"), "compose" to listOf("keynote"), "kotlin" to emptyList()), clashes)
    }

    @Test
    fun `once today's sessions end the agenda moves to tomorrow`() {
        val state = home.toHomeState(isSyncing = false, now = at("2026-11-06T18:00"), zone = nairobi)

        assertEquals(AgendaDay.Tomorrow, state.agenda?.day)
        assertEquals(emptyList<String>(), state.agenda?.items?.map { it.session.id })
    }

    @Test
    fun `after the event the hero thanks people and there is no agenda`() {
        val state = home.toHomeState(isSyncing = false, now = at("2026-11-08T09:00"), zone = nairobi)

        assertEquals(HomeHero.ThankYou, state.hero)
        assertNull(state.agenda)
    }

    @Test
    fun `before anything has synced the hero welcomes and there is no agenda`() {
        val state = Home().toHomeState(isSyncing = true, now = at("2026-11-06T09:00"), zone = nairobi)

        assertEquals(HomeHero.Welcome, state.hero)
        assertNull(state.agenda)
    }

    private fun at(nairobiTime: String) = Instant.parse("$nairobiTime:00+03:00")

    private fun session(
        id: String,
        start: String,
        end: String,
        starred: Boolean = false,
    ) = testSession(id, at(start), at(end), starred)
}