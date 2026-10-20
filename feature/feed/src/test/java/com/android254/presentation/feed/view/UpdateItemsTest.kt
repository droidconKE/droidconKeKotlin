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
package com.android254.presentation.feed.view

import com.android254.domain.models.Feed
import com.android254.domain.models.ScheduleUpdate
import com.android254.domain.models.ScheduleUpdate.Kind
import ke.droidcon.kotlin.core.testing.testSession
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UpdateItemsTest {
    private val nairobi = TimeZone.of("Africa/Nairobi")
    private val now = Instant.parse("2026-10-20T09:00:00+03:00")
    private val compose = testSession("compose", Instant.parse("2026-11-05T10:15:00+03:00"), Instant.parse("2026-11-05T10:55:00+03:00"), starred = true, title = "Compose")

    @Test
    fun `posts and schedule changes share one timeline, newest first`() {
        val items =
            updateItems(
                posts = listOf(Feed("Venue announced", "", "", "", null, "2026-10-19T08:00:00")),
                updates =
                    listOf(
                        update(Kind.Moved, "compose", at = "2026-10-20T08:00:00+03:00", id = 2),
                        update(Kind.ScheduleOut, null, at = "2026-10-01T08:00:00+03:00", count = 20, id = 1),
                    ),
                sessions = listOf(compose),
                now = now,
                zone = nairobi,
            )

        assertEquals(listOf("update", "post", "update"), items.map { it.key.substringBefore(':') })
    }

    @Test
    fun `a change to a starred session is marked as yours and shows where it stands now`() {
        val item = updateItems(emptyList(), listOf(update(Kind.Rescheduled, "compose")), listOf(compose), now, nairobi).single() as UpdateItem.Schedule

        assertTrue(item.isStarred)
        assertEquals("Compose", item.title)
        assertTrue(item.sessionTime.orEmpty().contains("10:15"))
    }

    @Test
    fun `a removed session keeps the title it had and opens nothing`() {
        val item = updateItems(emptyList(), listOf(update(Kind.Removed, "gone", title = "Old talk")), listOf(compose), now, nairobi).single() as UpdateItem.Schedule

        assertEquals("Old talk", item.title)
        assertNull(item.sessionId)
    }

    @Test
    fun `two sessions dropped in one sync are two rows, not one key twice`() {
        val items = updateItems(emptyList(), listOf(update(Kind.Removed, null, title = "A", id = 1), update(Kind.Removed, null, title = "B", id = 2)), emptyList(), now, nairobi)

        assertEquals(2, items.map { it.key }.distinct().size)
    }

    private fun update(
        kind: Kind,
        sessionId: String?,
        at: String = "2026-10-20T08:00:00+03:00",
        title: String = "",
        count: Int = 0,
        id: Long = 1,
    ) = ScheduleUpdate(id, kind, sessionId, title, count, Instant.parse(at))
}