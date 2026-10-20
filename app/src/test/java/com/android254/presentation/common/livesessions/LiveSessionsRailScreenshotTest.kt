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
package com.android254.presentation.common.livesessions

import com.android254.presentation.common.fakedata.fakeSessions
import com.android254.presentation.models.SessionStatus
import ke.droidcon.kotlin.screenshot.ChaiScreenshotTest
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test

class LiveSessionsRailScreenshotTest : ChaiScreenshotTest() {
    /** One nudge: your starred next session wins over a live one you did not star. */
    @Test
    fun `rail with a live session and your next one`() {
        captureComponent("live_sessions_rail") {
            LiveSessionsRail(
                sessions =
                    persistentListOf(
                        fakeSessions[0].copy(sessionStatus = SessionStatus.Ongoing),
                        fakeSessions[1].copy(sessionStatus = SessionStatus.Upcoming, isStarred = true),
                        fakeSessions[2].copy(sessionStatus = SessionStatus.Upcoming),
                    ),
                onSessionClick = {},
            )
        }
    }
}