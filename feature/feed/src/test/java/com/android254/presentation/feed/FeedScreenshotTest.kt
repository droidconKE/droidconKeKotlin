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

import androidx.compose.runtime.Composable
import com.android254.domain.models.ScheduleUpdate
import com.android254.presentation.feed.view.FeedScreen
import com.android254.presentation.feed.view.FeedUIState
import com.android254.presentation.feed.view.UpdateItem
import com.android254.presentation.models.FeedUI
import ke.droidcon.kotlin.screenshot.ChaiScreenshotTest
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test

class FeedScreenshotTest : ChaiScreenshotTest() {
    @Test
    fun feed() = captureScreen("screens/feed") { Feed() }

    /** A stream stays one column; the cap is what keeps it readable on a desktop. */
    @Test
    fun `feed across form factors`() = captureFormFactors("form_factors/feed") { Feed() }

    @Test
    fun `feed with no posts`() = captureScreen("screens/feed_empty") { FeedScreen(feedUIState = FeedUIState.Empty) }
}

@Composable
private fun Feed() {
    FeedScreen(
        feedUIState =
            FeedUIState.Success(
                items =
                    persistentListOf(
                        UpdateItem.Schedule(
                            kind = ScheduleUpdate.Kind.Moved,
                            sessionId = "compose",
                            title = "Compose Animations That Don't Jank",
                            count = 0,
                            whenLabel = "Today",
                            sessionTime = "Thu, 5 Nov · 10:15 AM",
                            venue = "Opal",
                            isStarred = true,
                            atMillis = 4,
                        ),
                        UpdateItem.Schedule(
                            kind = ScheduleUpdate.Kind.Added,
                            sessionId = "kmp",
                            title = "Kotlin Everywhere: KMP, Ktor, and ML in Production",
                            count = 0,
                            whenLabel = "Yesterday",
                            sessionTime = "Fri, 6 Nov · 11:10 AM",
                            venue = "Sapphire",
                            isStarred = false,
                            atMillis = 3,
                        ),
                        UpdateItem.Post(
                            FeedUI(
                                title = "Venue announced",
                                body = "This year we are back at the Sarit Expo Centre.",
                                topic = "Logistics",
                                url = "",
                                image = "",
                                createdAt = "2 days ago",
                            ),
                            atMillis = 2,
                        ),
                        UpdateItem.Schedule(
                            kind = ScheduleUpdate.Kind.ScheduleOut,
                            sessionId = null,
                            title = "",
                            count = 20,
                            whenLabel = "3 days ago",
                            sessionTime = null,
                            venue = "",
                            isStarred = false,
                            atMillis = 1,
                        ),
                    ),
            ),
    )
}