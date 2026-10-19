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
import ke.droidcon.kotlin.core.common.time.VenueZone
import ke.droidcon.kotlin.core.common.time.parseApiDateTime
import ke.droidcon.kotlin.datasource.local.model.SessionEntity
import ke.droidcon.kotlin.datasource.remote.sessions.model.SessionDTO
import ke.droidcon.kotlin.datasource.remote.speakers.model.SpeakerDTO
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Instant

/** Some titles carry their blurb after a line break; the first line is the title. */
fun SessionEntity.toDomainModel(): Session {
    val titleLines = title.trim().lines()
    val blurb = titleLines.drop(1).joinToString("\n").trim()
    return Session(
        id = this.remote_id,
        description = description.ifBlank { blurb },
        title = titleLines.first().trim(),
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
        eventDay = this.startTimestamp.toEventDay(),
        startInstant = Instant.fromEpochMilliseconds(startTimestamp),
        endInstant = Instant.fromEpochMilliseconds(endTimeStamp),
    )
}

fun SessionDTO.toEntity(): SessionEntity =
    SessionEntity(
        description = description.orEmpty(),
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
        sessionImageUrl = sessionImage.toString(),
        endTimeStamp = fromString(endDateTime),
    )

fun fromString(dateTime: String): Long = parseApiDateTime(dateTime).toInstant().toEpochMilli()

/** Day-of-month in the venue's timezone, zero-padded to match the API's format. */
private fun Long.toEventDay(): String =
    java.time.Instant
        .ofEpochMilli(this)
        .atZone(VenueZone)
        .dayOfMonth
        .toString()
        .padStart(2, '0')