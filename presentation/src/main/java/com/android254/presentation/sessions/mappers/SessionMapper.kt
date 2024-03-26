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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Session.toPresentationModel(): SessionPresentationModel {
    val startTime = getTimePeriod(this.startDateTime)
    val endTime = getTimePeriod(this.endDateTime)
    return SessionPresentationModel(
        id = this.id,
        title = this.title,
        description = this.description,
        venue = this.rooms,
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
        speakers = speakers.toSessionSpeaker()
    )
}

fun Session.toSessionDetailsPresentationModal(): SessionDetailsPresentationModel {
    val startTime = getTimePeriod(this.startDateTime)
    return SessionDetailsPresentationModel(
        id = this.id,
        title = this.title,
        description = this.description,
        venue = this.rooms,
        startTime = startTime.time,
        endTime = this.endTime,
        amOrPm = startTime.period,
        isStarred = this.isBookmarked,
        level = this.sessionLevel,
        format = this.sessionFormat,
        sessionImageUrl = this.sessionImage.toString(),
        timeSlot = "${startTime.time} - ${this.endTime}",
        speakers = speakers.toSessionDetailsSpeaker()
    )
}

fun List<Speaker>.toSessionDetailsSpeaker() = map { speaker ->
    SessionDetailsSpeakerPresentationModel(
        speakerImage = speaker.avatar,
        name = speaker.name,
        twitterHandle = speaker.twitter.split("/").lastOrNull().toString()
    )
}

fun List<Speaker>.toSessionSpeaker() = map { speaker ->
    SessionSpeakersPresentationModel(
        speakerImage = speaker.avatar,
        name = speaker.name,
        twitterHandle = speaker.twitter
    )
}

fun getTimePeriod(time: String): FormattedTime {
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val date: Date = format.parse(time) ?: Date()
    return FormattedTime(
        time = SimpleDateFormat("hh:mm", Locale.getDefault()).format(date),
        period = SimpleDateFormat("a", Locale.getDefault()).format(date)
    )
}

data class FormattedTime(
    val time: String,
    val period: String
)