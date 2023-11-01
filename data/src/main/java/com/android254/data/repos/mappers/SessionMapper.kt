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
package com.android254.data.repos.mappers

import com.android254.domain.models.Session
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import ke.droidcon.kotlin.datasource.local.model.SessionEntity
import ke.droidcon.kotlin.datasource.remote.sessions.model.SessionDTO
import ke.droidcon.kotlin.datasource.remote.speakers.model.SpeakerDTO
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun SessionEntity.toDomainModel() = Session(
    id = this.id.toString(),
    description = this.description,
    title = this.title,
    sessionFormat = this.sessionFormat,
    sessionLevel = this.sessionLevel,
    slug = this.slug,
    endDateTime = this.endDateTime,
    isBookmarked = this.isBookmarked,
    endTime = this.endTime,
    isKeynote = this.isKeynote,
    isServiceSession = this.isServiceSession,
    sessionImage = this.sessionImage,
    startDateTime = this.startDateTime,
    startTime = this.startTime,
    rooms = this.rooms,
    speakers = Json.decodeFromString<List<SpeakerDTO>>(speakers).map { it.toDomain() },
    remoteId = this.remote_id,
    eventDay = this.startTimestamp.toEventDay()
)

fun SessionDTO.toEntity(): SessionEntity {
    return SessionEntity(
        id = 0,
        description = description,
        title = title,
        sessionFormat = sessionFormat,
        sessionLevel = sessionLevel,
        slug = slug,
        endDateTime = this.endDateTime,
        isBookmarked = this.isBookmarked,
        endTime = this.endTime,
        isKeynote = this.isKeynote,
        isServiceSession = this.isServiceSession,
        sessionImage = this.sessionImage,
        startDateTime = this.startDateTime,
        startTime = startTime,
        rooms = this.rooms.joinToString(separator = ",") { it.title },
        speakers = Json.encodeToString(this.speakers),
        startTimestamp = fromString(startDateTime),
        remote_id = this.id,
        sessionImageUrl = sessionImage.toString()
    )
}

fun fromString(offsetDateTime: String): Long {
    val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    return LocalDateTime.parse(offsetDateTime, pattern).toInstant(ZoneOffset.ofHours(3))
        .toEpochMilli()
}

private fun Long.toEventDay(): String {
    val date = Date(this)
    val sdf = SimpleDateFormat("dd", Locale.getDefault())
    return sdf.format(date)
}