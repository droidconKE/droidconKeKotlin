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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/** Null until sessions with readable times have synced, so callers can fall back to a welcome. */
fun List<Session>.conferencePhase(
    now: Instant,
    zone: TimeZone,
): ConferencePhase? {
    val timed = timed()
    if (timed.isEmpty()) return null
    val firstStart = timed.minOf { it.start }
    return when {
        now < firstStart -> ConferencePhase.BeforeEvent(now.dateIn(zone).daysUntil(firstStart.dateIn(zone)))
        now > timed.maxOf { it.end } -> ConferencePhase.AfterEvent
        else -> ConferencePhase.DuringEvent
    }
}

/** Today until its last session ends, then the next event day; never before today. */
fun List<Session>.agendaDay(
    now: Instant,
    zone: TimeZone,
): LocalDate? =
    timed()
        .filter { it.end > now }
        .minOfOrNull { it.start }
        ?.dateIn(zone)
        ?.let { maxOf(it, now.dateIn(zone)) }

fun List<Session>.starredOn(
    day: LocalDate,
    zone: TimeZone,
): List<Session> =
    filter { it.isBookmarked && it.startInstant?.dateIn(zone) == day }
        .sortedBy { it.startInstant }

/** Every overlapping starred pair, not only neighbours: a long workshop can clash with several talks. */
fun List<Session>.scheduleConflicts(minOverlap: Duration = 5.minutes): List<ScheduleConflict> {
    val starred = timed().filter { it.session.isBookmarked }.sortedBy { it.start }
    return starred.flatMapIndexed { index, a ->
        starred.drop(index + 1).mapNotNull { b ->
            val overlap = minOf(a.end, b.end) - b.start
            if (overlap >= minOverlap) ScheduleConflict(a.session, b.session) else null
        }
    }
}

private class TimedSession(
    val session: Session,
    val start: Instant,
    val end: Instant,
)

private fun List<Session>.timed(): List<TimedSession> =
    mapNotNull { session ->
        val start = session.startInstant ?: return@mapNotNull null
        val end = session.endInstant ?: return@mapNotNull null
        TimedSession(session, start, end)
    }

private fun Instant.dateIn(zone: TimeZone): LocalDate = toLocalDateTime(zone).date