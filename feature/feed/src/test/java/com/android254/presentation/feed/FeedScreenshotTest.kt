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
package com.android254.presentation.feed

import com.android254.presentation.feed.view.FeedScreen
import com.android254.presentation.feed.view.FeedUIState
import com.android254.presentation.models.FeedUI
import ke.droidcon.kotlin.screenshot.ChaiScreenshotTest
import org.junit.Test

class FeedScreenshotTest : ChaiScreenshotTest() {
    @Test
    fun feed() =
        captureScreen("screens/feed") {
            FeedScreen(
                feedUIState =
                    FeedUIState.Success(
                        feeds =
                            listOf(
                                FeedUI(
                                    title = "Call for speakers is open",
                                    body =
                                        "Submissions close at the end of the month. Talks, " +
                                            "workshops and lightning sessions all welcome.",
                                    topic = "Announcement",
                                    url = "",
                                    image = "",
                                    createdAt = "2026-08-01",
                                ),
                                FeedUI(
                                    title = "Venue announced",
                                    body = "This year we are back at the Sarit Expo Centre.",
                                    topic = "Logistics",
                                    url = "",
                                    image = "",
                                    createdAt = "2026-08-02",
                                ),
                            ),
                    ),
            )
        }
}