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
package com.android254.presentation.speakers.view

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android254.domain.models.Speaker
import com.android254.domain.repos.SpeakersRepo
import com.android254.presentation.speakers.SpeakerDetailsScreenViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class SpeakerDetailsScreenTest {
    private val speakersRepo = mockk<SpeakersRepo>()
    private val mockSavedStateHandle = mockk<SavedStateHandle>()

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `all components should be displayed properly`() {
        every { mockSavedStateHandle.get<Int>("speakerId") } returns 0

        coEvery { speakersRepo.getSpeakerByName("Harun Wangereka") }.returns(flowOf(Speaker(name = "John Doe", tagline = "some tag line")))

        composeTestRule.setContent {
            SpeakerDetailsRoute(
                name = "Harun Wangereka",
                SpeakerDetailsScreenViewModel(
                    speakersRepo = speakersRepo
                )
            )
        }

        with(composeTestRule) {
            onNodeWithTag("speaker_image").assertIsDisplayed()
            onNodeWithTag("speaker_name").assertIsDisplayed()
            onNodeWithTag("speaker_tagline").assertIsDisplayed()
            onNodeWithTag("twitter_button").performScrollTo().assertIsDisplayed()
        }
    }
}