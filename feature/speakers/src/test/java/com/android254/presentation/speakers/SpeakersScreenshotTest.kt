/*
 * Copyright 2023 DroidconKE
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
package com.android254.presentation.speakers

import com.android254.presentation.common.fakedata.fakeSessions
import com.android254.presentation.models.SpeakerUI
import com.android254.presentation.speakers.view.SpeakerDetailsScreen
import com.android254.presentation.speakers.view.SpeakersScreen
import ke.droidcon.kotlin.screenshot.ChaiScreenshotTest
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.junit.Test

class SpeakersScreenshotTest : ChaiScreenshotTest() {
    private val speakers =
        persistentListOf(
            SpeakerUI(
                id = 1,
                name = "Harun Wangereka",
                tagline = "Kenya Partner Lead at droidcon Berlin | Android | Kotlin | C++",
                isSpeakingNow = true,
            ),
            SpeakerUI(
                id = 2,
                name = "Omolara Adejuwon",
                tagline = "Android Engineer",
            ),
            SpeakerUI(
                id = 3,
                name = "Frank Tamre",
                tagline = "Mobile Engineer at a very long company name that will not fit",
            ),
        )

    private val speaker =
        SpeakerUI(
            id = 1,
            name = "Harun Wangereka",
            tagline = "Kenya Partner Lead at droidcon Berlin",
            bio = "Hi there!\nThis is Harun, working as an Android Engineer.",
            twitterHandle = "https://twitter.com/wangerekaharun",
        )

    @Test
    fun speakers() =
        captureScreen("screens/speakers") {
            SpeakersScreen(uiState = SpeakersScreenUiState.Success(speakers = speakers))
        }

    @Test
    fun `speakers searching`() =
        captureScreen("screens/speakers_searching") {
            SpeakersScreen(
                uiState = SpeakersScreenUiState.Success(speakers = persistentListOf(speakers[1])),
                searchQuery = "omolara",
            )
        }

    @Test
    fun `speakers no search results`() =
        captureScreen("screens/speakers_no_results") {
            SpeakersScreen(
                uiState = SpeakersScreenUiState.Success(speakers = persistentListOf()),
                searchQuery = "nobody here",
            )
        }

    @Test
    fun `speaker details`() =
        captureScreen("screens/speaker_details") {
            SpeakerDetailsScreen(
                uiState =
                    SpeakerDetailsScreenUiState.Success(
                        speaker = speaker,
                        sessions = fakeSessions.take(2).toImmutableList(),
                    ),
            )
        }

    @Test
    fun `speaker details twitter row`() =
        captureScreen("screens/speaker_details_twitter") {
            SpeakerDetailsScreen(
                uiState =
                    SpeakerDetailsScreenUiState.Success(
                        speaker = speaker.copy(imageUrl = null, tagline = null, bio = null),
                    ),
            )
        }

    @Test
    fun `speaker details without sessions`() =
        captureScreen("screens/speaker_details_no_sessions") {
            SpeakerDetailsScreen(
                uiState = SpeakerDetailsScreenUiState.Success(speaker = speaker),
            )
        }
}