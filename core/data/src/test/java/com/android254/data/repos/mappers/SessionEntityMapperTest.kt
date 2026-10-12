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

import ke.droidcon.kotlin.datasource.local.model.SessionEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Instant

class SessionEntityMapperTest {
    @Test
    fun `start and end instants come from the stored timestamps`() {
        val start = Instant.parse("2026-11-06T09:00:00+03:00")
        val end = Instant.parse("2026-11-06T10:00:00+03:00")
        val entity = entity(startTimestamp = start.toEpochMilliseconds(), endTimeStamp = end.toEpochMilliseconds())

        val session = entity.toDomainModel()

        assertEquals(start, session.startInstant)
        assertEquals(end, session.endInstant)
    }

    private fun entity(
        startTimestamp: Long,
        endTimeStamp: Long,
    ) = SessionEntity(
        remote_id = "1",
        description = "",
        sessionFormat = "",
        sessionLevel = "",
        slug = "",
        title = "",
        endDateTime = "",
        endTime = "",
        isBookmarked = false,
        isKeynote = false,
        isServiceSession = false,
        sessionImage = null,
        startDateTime = "",
        startTime = "",
        rooms = "",
        speakers = "[]",
        startTimestamp = startTimestamp,
        endTimeStamp = endTimeStamp,
        sessionImageUrl = "",
    )
}