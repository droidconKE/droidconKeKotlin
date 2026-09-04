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
package com.android254.presentation.speakers

import com.android254.presentation.models.speakersDummyData
import com.android254.presentation.speakers.view.SpeakerDetailsScreen
import com.android254.presentation.speakers.view.SpeakersScreen
import ke.droidcon.kotlin.screenshot.ChaiScreenshotTest
import kotlinx.collections.immutable.toImmutableList
import org.junit.Test

class SpeakersScreenshotTest : ChaiScreenshotTest() {
    @Test
    fun speakers() =
        captureScreen("screens/speakers") {
            SpeakersScreen(
                uiState =
                    SpeakersScreenUiState.Success(
                        speakers = speakersDummyData.toImmutableList(),
                    ),
            )
        }

    @Test
    fun `speaker details`() =
        captureScreen("screens/speaker_details") {
            SpeakerDetailsScreen(
                uiState = SpeakerDetailsScreenUiState.Success(speaker = speakersDummyData.first()),
            )
        }
}