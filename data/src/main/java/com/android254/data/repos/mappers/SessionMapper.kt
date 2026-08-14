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
import ke.droidcon.kotlin.datasource.local.model.SessionEntity
import ke.droidcon.kotlin.datasource.remote.sessions.model.SessionDTO
import ke.droidcon.kotlin.datasource.remote.speakers.model.SpeakerDTO
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

fun SessionEntity.toDomainModel() =
    Session(
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
        eventDay = this.startTimestamp.toEventDay(),
    )

fun SessionDTO.toEntity(): SessionEntity =
    SessionEntity(
        id = 0,
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

fun fromString(offsetDateTime: String): Long =
    LocalDateTime
        .parse(offsetDateTime, API_DATE_TIME)
        .toInstant(CONFERENCE_OFFSET)
        .toEpochMilli()

/** Day-of-month in the venue's timezone, zero-padded to match the API's format. */
private fun Long.toEventDay(): String =
    java.time.Instant
        .ofEpochMilli(this)
        .atZone(CONFERENCE_ZONE)
        .dayOfMonth
        .toString()
        .padStart(2, '0')

private val API_DATE_TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

/** Session times are venue-local; the API sends them without an offset. */
private val CONFERENCE_OFFSET: ZoneOffset = ZoneOffset.ofHours(3)
private val CONFERENCE_ZONE: ZoneId = ZoneId.of("Africa/Nairobi")