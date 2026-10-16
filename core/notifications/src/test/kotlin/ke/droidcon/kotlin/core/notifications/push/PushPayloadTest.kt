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
package ke.droidcon.kotlin.core.notifications.push

import ke.droidcon.kotlin.core.notifications.push.PushPayload.ScheduleChange.ChangeType
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PushPayloadTest {
    @Test
    fun `schedule change decodes from its type discriminator`() {
        val json =
            """
            {"type": "schedule_change", "sessionId": "123", "changeType": "RoomChanged", "message": "Moved to Room B"}
            """.trimIndent()

        assertEquals(
            PushPayload.ScheduleChange("123", ChangeType.RoomChanged, "Moved to Room B"),
            PushPayload.decodeOrNull(json),
        )
    }

    @Test
    fun `announcement decodes with a null deep link`() {
        val json =
            """
            {"type": "announcement", "title": "Lunch", "body": "Lunch is served in the main hall", "deepLink": null}
            """.trimIndent()

        assertEquals(
            PushPayload.Announcement("Lunch", "Lunch is served in the main hall"),
            PushPayload.decodeOrNull(json),
        )
    }

    @Test
    fun `sync request decodes with no fields`() {
        assertEquals(PushPayload.SyncRequest, PushPayload.decodeOrNull("""{"type": "sync_request"}"""))
    }

    @Test
    fun `feed post and milestone decode`() {
        assertEquals(
            PushPayload.NewFeedPost("post456", "New post"),
            PushPayload.decodeOrNull("""{"type": "feed_post", "postId": "post456", "preview": "New post"}"""),
        )
        assertEquals(
            PushPayload.Milestone("Day 1 done", "See you tomorrow"),
            PushPayload.decodeOrNull("""{"type": "milestone", "title": "Day 1 done", "body": "See you tomorrow"}"""),
        )
    }

    @Test
    fun `every payload survives a round trip`() {
        val payloads =
            listOf(
                PushPayload.ScheduleChange("abc", ChangeType.TimeChanged, "Moved to 2 PM"),
                PushPayload.Announcement("Lunch", "Served", "droidconke://feed"),
                PushPayload.SyncRequest,
                PushPayload.NewFeedPost("p1", "Hello"),
                PushPayload.Milestone("Done", "Thanks", "trophy"),
            )

        payloads.forEach { assertEquals(it, PushPayload.decodeOrNull(PushPayload.encode(it))) }
    }

    @Test
    fun `fields the app does not know yet are ignored`() {
        val json = """{"type": "sync_request", "sentAt": "2026-11-08T09:00:00Z"}"""

        assertEquals(PushPayload.SyncRequest, PushPayload.decodeOrNull(json))
    }

    @Test
    fun `an unknown type decodes to null rather than throwing`() {
        assertNull(PushPayload.decodeOrNull("""{"type": "poll_opened", "pollId": "1"}"""))
    }

    @Test
    fun `malformed json decodes to null rather than throwing`() {
        assertNull(PushPayload.decodeOrNull("not json"))
        assertNull(PushPayload.decodeOrNull("""{"type": "schedule_change", "sessionId": "1"}"""))
    }
}