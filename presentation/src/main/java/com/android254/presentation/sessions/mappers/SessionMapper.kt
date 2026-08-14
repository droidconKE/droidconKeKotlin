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
package com.android254.presentation.sessions.mappers

import com.android254.domain.models.Session
import com.android254.domain.models.Speaker
import com.android254.presentation.models.SessionDetailsPresentationModel
import com.android254.presentation.models.SessionDetailsSpeakerPresentationModel
import com.android254.presentation.models.SessionPresentationModel
import com.android254.presentation.models.SessionSpeakersPresentationModel
import com.android254.presentation.models.SessionStatus
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import timber.log.Timber
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import kotlin.time.Instant

fun Session.toPresentationModel(now: Instant): SessionPresentationModel {
    val startTime = getTimePeriod(this.startDateTime)
    val endTime = getTimePeriod(this.endDateTime)

    val sessionStart = this.startDateTime.toInstant()
    val sessionEnd = this.endDateTime.toInstant()

    // An unparseable time leaves the status unknown; treat it as upcoming rather than
    // claiming a session in 1970 has already finished.
    val sessionStatus =
        when {
            sessionStart == null || sessionEnd == null -> SessionStatus.Upcoming
            now < sessionStart -> SessionStatus.Upcoming
            now > sessionEnd -> SessionStatus.Past
            else -> SessionStatus.Ongoing
        }

    return SessionPresentationModel(
        id = this.id,
        title = this.title,
        description = this.description,
        venue = this.roomList.joinToString(", "),
        sessionStatus = sessionStatus,
        startTime = startTime.time,
        endTime = "${endTime.time} ${endTime.period}",
        amOrPm = startTime.period,
        isStarred = this.isBookmarked,
        level = this.sessionLevel,
        format = this.sessionFormat,
        startDate = this.startDateTime,
        endDate = this.endDateTime,
        remoteId = this.remoteId,
        isService = this.isServiceSession,
        sessionImage = this.sessionImage ?: "",
        eventDay = eventDay,
        speakers = speakers.toSessionSpeaker(),
    )
}

fun Session.toSessionDetailsPresentationModal(): SessionDetailsPresentationModel {
    val startTime = getTimePeriod(this.startDateTime)
    val endTime = getTimePeriod(this.endDateTime)
    return SessionDetailsPresentationModel(
        id = this.id,
        title = this.title,
        description = this.description,
        venue = this.roomList.joinToString(", "),
        startTime = startTime.time,
        endTime = "${endTime.time} ${endTime.period}",
        amOrPm = startTime.period,
        isStarred = this.isBookmarked,
        level = this.sessionLevel,
        format = this.sessionFormat,
        sessionImageUrl = this.sessionImage.toString(),
        timeSlot = "${startTime.time} - ${endTime.time} ${endTime.period}",
        speakers = speakers.toSessionDetailsSpeaker(),
    )
}

fun List<Speaker>.toSessionDetailsSpeaker() =
    map { speaker ->
        SessionDetailsSpeakerPresentationModel(
            speakerImage = speaker.avatar,
            name = speaker.name,
            twitterHandle =
                speaker.twitter
                    .split("/")
                    .lastOrNull()
                    .toString(),
        )
    }

fun List<Speaker>.toSessionSpeaker(): ImmutableList<SessionSpeakersPresentationModel> =
    map { speaker ->
        SessionSpeakersPresentationModel(
            speakerImage = speaker.avatar,
            name = speaker.name,
            twitterHandle = speaker.twitter,
        )
    }.toImmutableList()

fun getTimePeriod(time: String): FormattedTime {
    val parsed = time.toLocalDateTimeOrNull() ?: return FormattedTime(time = time, period = "")
    return FormattedTime(
        time = parsed.format(CLOCK_TIME),
        period = parsed.format(MERIDIEM),
    )
}

/**
 * Parses an API session time, which arrives venue-local and without an offset.
 *
 * Returns null rather than epoch zero on failure: the previous fallback made an
 * unparseable time render as a session in 1970.
 */
private fun String.toLocalDateTimeOrNull(): LocalDateTime? =
    try {
        LocalDateTime.parse(this, API_DATE_TIME)
    } catch (e: DateTimeParseException) {
        Timber.w(e, "Unparseable session time: %s", this)
        null
    }

private fun String.toInstant(): Instant? =
    toLocalDateTimeOrNull()
        ?.toInstant(CONFERENCE_OFFSET)
        ?.toEpochMilli()
        ?.let(Instant::fromEpochMilliseconds)

data class FormattedTime(
    val time: String,
    val period: String,
)

private val API_DATE_TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
private val CLOCK_TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm", Locale.US)
private val MERIDIEM: DateTimeFormatter = DateTimeFormatter.ofPattern("a", Locale.US)

/** Session times are venue-local; the API sends them without an offset. */
private val CONFERENCE_OFFSET: ZoneOffset = ZoneOffset.ofHours(3)