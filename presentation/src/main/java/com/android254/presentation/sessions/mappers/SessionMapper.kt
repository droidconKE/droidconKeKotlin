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
import com.android254.presentation.models.SessionDetailsPresentationModel
import com.android254.presentation.models.SessionPresentationModel
import com.android254.presentation.models.Speaker
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

fun Session.toPresentationModel(): SessionPresentationModel {
    val startTime = getTimePeriod(this.startDateTime)
    val gson = Gson()
    val typeToken = object : TypeToken<List<Speaker>>() {}.type
    val speakers = gson.fromJson<List<Speaker>>(this.speakers, typeToken)
    val hasNoSpeakers = speakers.isEmpty()

    return SessionPresentationModel(
        id = this.id,
        title = this.title,
        description = this.description,
        venue = this.rooms,
        speakerImage = if (hasNoSpeakers) "" else speakers.first().avatar.toString(),
        speakerName = if (hasNoSpeakers) "" else speakers.first().name,
        startTime = startTime.time,
        endTime = this.endTime,
        amOrPm = startTime.period,
        isStarred = this.isBookmarked,
        level = this.sessionLevel,
        format = this.sessionFormat,
        startDate = this.startDateTime,
        endDate = this.endDateTime,
        remoteId = this.remote_id,
        isService = this.isServiceSession,
        sessionImage = this.sessionImage ?: ""
    )
}

fun Session.toSessionDetailsPresentationModal(): SessionDetailsPresentationModel {
    val startTime = getTimePeriod(this.startDateTime)
    val gson = Gson()
    val typeToken = object : TypeToken<List<Speaker>>() {}.type
    val speakers = gson.fromJson<List<Speaker>>(this.speakers, typeToken)
    val hasNoSpeakers = speakers.isEmpty()
    return SessionDetailsPresentationModel(
        id = this.id,
        title = this.title,
        description = this.description,
        venue = this.rooms,
        speakerImage = if (hasNoSpeakers) "" else speakers.first().avatar.toString(),
        speakerName = if (hasNoSpeakers) "" else speakers.first().name,
        startTime = startTime.time,
        endTime = this.endTime,
        amOrPm = startTime.period,
        isStarred = this.isBookmarked,
        level = this.sessionLevel,
        format = this.sessionFormat,
        sessionImageUrl = this.sessionImage.toString(),
        timeSlot = "${startTime.time} - ${this.endTime}",
        twitterHandle = if (hasNoSpeakers) "" else getTwitterHandle(speakers)
    )
}

fun getTwitterHandle(speakers: List<Speaker>): String {
    if (speakers.first().twitter == null) {
        return ""
    }
    return speakers.first().twitter.toString().split('/')
        .toTypedArray().last()
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