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

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android254.domain.models.ResourceResult
import com.android254.domain.models.Speaker
import com.android254.domain.repos.SpeakersRepo
import com.android254.presentation.speakers.SpeakersViewModel
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(instrumentedPackages = ["androidx.loader.content"])
class SpeakersScreenTest {
    private val speakersRepo = mockk<SpeakersRepo>()

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `should show heading and show speaker details card`() {
        coEvery { speakersRepo.fetchSpeakers() } returns ResourceResult.Success(
            listOf(
                Speaker(
                    name = "Harun Wangereka",
                    tagline = "kenya partner lead"
                )
            )
        )
        composeTestRule.setContent {
            SpeakersScreen(SpeakersViewModel(speakersRepo))
        }

        with(composeTestRule) {
            onNodeWithText("Speakers").assertIsDisplayed()
            onNodeWithContentDescription("Back arrow icon").assertIsDisplayed()
            onNodeWithContentDescription("Speaker headshot").assertIsDisplayed()
            onNodeWithText("Harun Wangereka").assertIsDisplayed()
            onNodeWithText("kenya partner lead", substring = true, ignoreCase = true)
            onNodeWithText("Session").assertIsDisplayed()
        }
    }
}