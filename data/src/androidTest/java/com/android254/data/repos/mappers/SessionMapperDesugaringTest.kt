/*
 * Copyright 2026 DroidconKE
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

import androidx.test.ext.junit.runners.AndroidJUnit4
import ke.droidcon.kotlin.datasource.remote.sessions.model.RoomDTO
import ke.droidcon.kotlin.datasource.remote.sessions.model.SessionDTO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Proves `java.time` is usable at every supported API level.
 *
 * It is API 26+ while `minSdk` is 24, so without core library desugaring
 * [SessionDTO.toEntity] throws `NoClassDefFoundError` on the first sync. A JVM test cannot
 * catch this — the desktop JDK always has `java.time`:
 *
 * ```
 * ./gradlew :data:api24DebugAndroidTest
 * ```
 */
@RunWith(AndroidJUnit4::class)
class SessionMapperDesugaringTest {
    @Test
    fun mapsSessionTimestampsOnEverySupportedApiLevel() {
        val entity = sessionDto().toEntity()

        assertTrue("startTimestamp was not parsed", entity.startTimestamp > 0)
        assertTrue("endTimeStamp was not parsed", entity.endTimeStamp > 0)
        assertTrue(entity.endTimeStamp > entity.startTimestamp)
    }

    @Test
    fun joinsRoomsForMultiRoomSessions() {
        val entity = sessionDto(rooms = listOf(RoomDTO("Opal"), RoomDTO("Sapphire"))).toEntity()

        assertEquals("Opal,Sapphire", entity.rooms)
    }

    private fun sessionDto(rooms: List<RoomDTO> = listOf(RoomDTO("Opal"))) =
        SessionDTO(
            id = "1",
            backgroundColor = "#FFFFFF",
            borderColor = "#000000",
            description = "A session",
            endDateTime = "2026-11-06 10:00:00",
            endTime = "10:00",
            isBookmarked = false,
            isKeynote = false,
            isServiceSession = false,
            sessionFormat = "Session",
            sessionImage = null,
            sessionLevel = "Advanced",
            slug = "a-session",
            startDateTime = "2026-11-06 09:00:00",
            startTime = "09:00",
            title = "A session",
            rooms = rooms,
            speakers = emptyList(),
        )
}