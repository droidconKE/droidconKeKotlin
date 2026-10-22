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
package com.android254.presentation.home

import androidx.compose.runtime.Composable
import com.android254.domain.models.Ticket
import com.android254.presentation.common.fakedata.fakeSessions
import com.android254.presentation.home.screen.HomeScreen
import com.android254.presentation.home.viewstate.AgendaDay
import com.android254.presentation.home.viewstate.AgendaItem
import com.android254.presentation.home.viewstate.HomeAgenda
import com.android254.presentation.home.viewstate.HomeHero
import com.android254.presentation.home.viewstate.HomeState
import com.android254.presentation.home.viewstate.HomeTicket
import com.android254.presentation.models.speakersDummyData
import ke.droidcon.kotlin.screenshot.ChaiScreenshotTest
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate
import org.junit.Test

class HomeScreenshotTest : ChaiScreenshotTest() {
    @Test
    fun home() = captureScreen("screens/home") { Home() }

    /** Home is a single column everywhere; what changes is where that column stops. */
    @Test
    fun `home across form factors`() = captureFormFactors("form_factors/home") { Home() }

    @Test
    fun `home agenda across form factors`() = captureFormFactors("form_factors/home_agenda") { Home(agenda = clashingAgenda) }

    @Test
    fun `home before the event, nothing starred`() =
        captureScreen("screens/home_before") {
            Home(
                hero = HomeHero.Countdown(daysUntil = 12, firstDay = LocalDate(2026, 11, 6), lastDay = LocalDate(2026, 11, 7)),
                agenda = HomeAgenda(AgendaDay.On(LocalDate(2026, 11, 6)), persistentListOf()),
                ticket = HomeTicket.Add,
            )
        }

    @Test
    fun `home on the day, with a ticket saved`() =
        captureScreen("screens/home_ticket") {
            Home(
                hero = HomeHero.Welcome,
                agenda = clashingAgenda,
                ticket = HomeTicket.Saved("Magak Emmanuel", "Regular", "TIC-70235", Ticket.Status.Active),
                isEventDay = true,
            )
        }

    @Test
    fun `home before the event, with a ticket saved`() =
        captureScreen("screens/home_before_ticket") {
            Home(
                hero = HomeHero.Countdown(daysUntil = 12, firstDay = LocalDate(2026, 11, 6), lastDay = LocalDate(2026, 11, 7)),
                agenda = HomeAgenda(AgendaDay.On(LocalDate(2026, 11, 6)), persistentListOf()),
                ticket = HomeTicket.Saved("Magak Emmanuel", "Regular", "TIC-70235", Ticket.Status.Active),
            )
        }

    @Test
    fun `home during the event, with a clash`() =
        captureScreen("screens/home_agenda") {
            Home(
                hero = HomeHero.Welcome,
                agenda = clashingAgenda,
            )
        }

    @Test
    fun `home after the event`() = captureScreen("screens/home_after") { Home(hero = HomeHero.ThankYou) }
}

@Composable
private fun Home(
    hero: HomeHero = HomeHero.Welcome,
    agenda: HomeAgenda? = null,
    ticket: HomeTicket = HomeTicket.Hidden,
    isEventDay: Boolean = false,
) {
    HomeScreen(
        viewState =
            HomeState(
                speakers = speakersDummyData.toImmutableList(),
                sessions = fakeSessions,
                isSyncing = false,
                hero = hero,
                agenda = agenda,
                showCube = true,
                ticket = ticket,
                isEventDay = isEventDay,
            ),
        isSyncing = false,
        animateCube = false,
    )
}

private val clashingAgenda =
    HomeAgenda(
        AgendaDay.Today,
        persistentListOf(
            AgendaItem(fakeSessions[4], persistentListOf(fakeSessions[1].title)),
            AgendaItem(fakeSessions[1].copy(id = "today-1"), persistentListOf(fakeSessions[4].title)),
            AgendaItem(fakeSessions[6], persistentListOf()),
        ),
    )