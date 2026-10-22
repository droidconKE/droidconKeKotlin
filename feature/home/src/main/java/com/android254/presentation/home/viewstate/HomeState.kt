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
package com.android254.presentation.home.viewstate

import androidx.compose.runtime.Immutable
import com.android254.domain.models.HomeBanner
import com.android254.domain.models.Ticket
import com.android254.presentation.models.SessionPresentationModel
import com.android254.presentation.models.SpeakerUI
import com.android254.presentation.models.SponsorPresentationModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate

data class HomeState(
    val banner: HomeBanner = HomeBanner.None,
    val speakers: ImmutableList<SpeakerUI> = persistentListOf(),
    val sponsors: ImmutableList<SponsorPresentationModel> = persistentListOf(),
    val organizerLogos: ImmutableList<String> = persistentListOf(),
    val sessions: ImmutableList<SessionPresentationModel> = persistentListOf(),
    val isSyncing: Boolean = true,
    val hero: HomeHero = HomeHero.Welcome,
    val agenda: HomeAgenda? = null,
    val showCube: Boolean = false,
    val ticket: HomeTicket = HomeTicket.Hidden,
    val isEventDay: Boolean = false,
)

/** What the hero says when no session is live. */
sealed interface HomeHero {
    data object Welcome : HomeHero

    data class Countdown(
        val daysUntil: Int,
        val firstDay: LocalDate? = null,
        val lastDay: LocalDate? = null,
    ) : HomeHero

    data object ThankYou : HomeHero
}

sealed interface HomeTicket {
    data object Hidden : HomeTicket

    data object Add : HomeTicket

    data class Saved(
        val attendeeName: String,
        val type: String,
        val number: String,
        val status: Ticket.Status,
    ) : HomeTicket
}

data class HomeAgenda(
    val day: AgendaDay,
    val items: ImmutableList<AgendaItem>,
)

sealed interface AgendaDay {
    data object Today : AgendaDay

    data object Tomorrow : AgendaDay

    @Immutable
    data class On(
        val date: LocalDate,
    ) : AgendaDay
}

data class AgendaItem(
    val session: SessionPresentationModel,
    val clashesWith: ImmutableList<String>,
)