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
package com.android254.data.repos

import com.android254.domain.models.ScheduleUpdate.Kind
import org.junit.Assert.assertEquals
import org.junit.Test

class ScheduleChangesTest {
    private val talk = sessionEntity.copy(remote_id = "talk", title = "Compose", rooms = "Opal", startTimestamp = 1_000, endTimeStamp = 2_000)
    private val lunch = sessionEntity.copy(remote_id = "lunch", title = "Lunch", isServiceSession = true, startTimestamp = 3_000, endTimeStamp = 4_000)

    @Test
    fun `a first sync is the schedule coming out, not a list of additions`() {
        assertEquals(listOf(ScheduleChange(Kind.ScheduleOut, count = 2)), scheduleChanges(emptyList(), listOf(talk, lunch)))
    }

    @Test
    fun `a schedule with nothing in common with the last one is a new event year`() {
        val nextYear = listOf(talk.copy(remote_id = "2026-a"), talk.copy(remote_id = "2026-b"), lunch.copy(remote_id = "2026-c"))

        assertEquals(listOf(ScheduleChange(Kind.ScheduleOut, count = 3)), scheduleChanges(listOf(talk, lunch), nextYear))
    }

    @Test
    fun `new and dropped talks are news, breaks coming and going are not`() {
        val added = talk.copy(remote_id = "new", title = "Kotlin")
        val extraBreak = lunch.copy(remote_id = "coffee", title = "Coffee")

        assertEquals(
            listOf(ScheduleChange(Kind.Added, "new", "Kotlin")),
            scheduleChanges(listOf(talk, lunch), listOf(talk, lunch, added, extraBreak)),
        )
        assertEquals(
            listOf(ScheduleChange(Kind.Removed, title = "Compose")),
            scheduleChanges(listOf(talk, lunch, extraBreak), listOf(lunch)),
        )
    }

    @Test
    fun `a talk or a break that moves in time or room is news`() {
        val changes = scheduleChanges(listOf(talk, lunch), listOf(talk.copy(rooms = "Sapphire"), lunch.copy(startTimestamp = 3_500)))

        assertEquals(listOf(ScheduleChange(Kind.Moved, "talk", "Compose"), ScheduleChange(Kind.Rescheduled, "lunch", "Lunch")), changes)
    }

    @Test
    fun `nothing changed is nothing to say, and neither is an empty response`() {
        assertEquals(emptyList<ScheduleChange>(), scheduleChanges(listOf(talk, lunch), listOf(talk, lunch)))
        assertEquals(emptyList<ScheduleChange>(), scheduleChanges(listOf(talk, lunch), emptyList()))
    }

    @Test
    fun `a sync that changes a lot becomes one moment rather than a flood`() {
        val many = (1..13).map { talk.copy(remote_id = "new-$it") }

        assertEquals(listOf(ScheduleChange(Kind.ManyChanges, count = 13)), scheduleChanges(listOf(talk), listOf(talk) + many))
    }
}