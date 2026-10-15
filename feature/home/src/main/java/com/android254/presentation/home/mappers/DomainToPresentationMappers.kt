/*
 * Copyright 2023 DroidconKE
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

import com.android254.domain.models.ConferencePhase
import com.android254.domain.models.Home
import com.android254.domain.models.Session
import com.android254.domain.models.Speaker
import com.android254.domain.models.Sponsors
import com.android254.domain.schedule.agendaDay
import com.android254.domain.schedule.conferencePhase
import com.android254.domain.schedule.scheduleConflicts
import com.android254.domain.schedule.starredOn
import com.android254.presentation.home.viewstate.AgendaDay
import com.android254.presentation.home.viewstate.AgendaItem
import com.android254.presentation.home.viewstate.HomeAgenda
import com.android254.presentation.home.viewstate.HomeHero
import com.android254.presentation.home.viewstate.HomeState
import com.android254.presentation.models.SessionPresentationModel
import com.android254.presentation.models.SpeakerUI
import com.android254.presentation.models.SponsorPresentationModel
import com.android254.presentation.sessions.mappers.toPresentationModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

fun List<Speaker>.toSpeakersPresentation(): List<SpeakerUI> =
    map {
        SpeakerUI(
            imageUrl = it.avatar,
            name = it.name,
            tagline = it.tagline,
            bio = it.biography,
            twitterHandle = it.twitter,
        )
    }

fun Sponsors.toPresentation() =
    SponsorPresentationModel(
        name = sponsorName,
        link = link,
        logo = sponsorLogoUrl,
        sponsorType = sponsorType,
    )

fun Home.toHomeState(
    isSyncing: Boolean,
    now: Instant,
    zone: TimeZone,
    showCube: Boolean = false,
): HomeState {
    val mapped = sessions.map { it.toPresentationModel(now) }
    val presented = sessions.zip(mapped).associate { (session, model) -> session.id to model }
    return HomeState(
        banner = banner,
        speakers = speakers.toSpeakersPresentation().toImmutableList(),
        sponsors = sponsors.map { it.toPresentation() }.toImmutableList(),
        organizerLogos = organizerLogos.toImmutableList(),
        sessions = mapped.toImmutableList(),
        isSyncing = isSyncing,
        hero =
            when (val phase = sessions.conferencePhase(now, zone)) {
                is ConferencePhase.BeforeEvent -> HomeHero.Countdown(phase.daysUntil)
                ConferencePhase.AfterEvent -> HomeHero.ThankYou
                ConferencePhase.DuringEvent, null -> HomeHero.Welcome
            },
        agenda = sessions.agenda(now, zone, presented),
        showCube = showCube,
    )
}

private fun List<Session>.agenda(
    now: Instant,
    zone: TimeZone,
    presented: Map<String, SessionPresentationModel>,
): HomeAgenda? {
    val day = agendaDay(now, zone) ?: return null
    val today = now.toLocalDateTime(zone).date
    val conflicts = scheduleConflicts()
    return HomeAgenda(
        day =
            when (day) {
                today -> AgendaDay.Today
                today.plus(1, DateTimeUnit.DAY) -> AgendaDay.Tomorrow
                else -> AgendaDay.On(day)
            },
        items =
            starredOn(day, zone)
                .map { session ->
                    AgendaItem(
                        session = presented.getValue(session.id),
                        clashesWith =
                            conflicts
                                .mapNotNull { conflict ->
                                    when (session.id) {
                                        conflict.first.id -> conflict.second.title
                                        conflict.second.id -> conflict.first.title
                                        else -> null
                                    }
                                }.toImmutableList(),
                    )
                }.toImmutableList(),
    )
}