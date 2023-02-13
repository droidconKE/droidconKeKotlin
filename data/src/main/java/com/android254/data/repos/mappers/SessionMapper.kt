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

import android.os.Build
import androidx.annotation.RequiresApi
import com.android254.data.db.model.SessionEntity
import com.android254.data.network.models.responses.SessionDTO
import com.android254.domain.models.Session
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

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
    speakers = this.speakers,
    remote_id = this.remote_id
)

@RequiresApi(Build.VERSION_CODES.O)
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

@RequiresApi(Build.VERSION_CODES.O)
fun fromString(offsetDateTime: String): Long {
    val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    return LocalDateTime.parse(offsetDateTime, pattern).toInstant(ZoneOffset.ofHours(3))
        .toEpochMilli()
}