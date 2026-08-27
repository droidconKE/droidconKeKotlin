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
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android254.domain.models.Speaker
import com.android254.domain.repos.SpeakersRepo
import com.android254.domain.work.SyncDataWorkManager
import com.android254.presentation.speakers.SpeakersScreenViewModel
import com.droidconke.chai.ChaiTheme
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(instrumentedPackages = ["androidx.loader.content"], sdk = [33])
class SpeakersScreenTest {
    private val speakersRepo = mockk<SpeakersRepo>()
    private val mockSyncDataWorkManager = mockk<SyncDataWorkManager>()
    private val testDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `should show heading and show speaker details card`() {
        every { mockSyncDataWorkManager.isSyncing } returns flowOf(true)
        coEvery { mockSyncDataWorkManager.startSync() } just runs
        coEvery { speakersRepo.fetchSpeakers() } returns
            flowOf(
                listOf(
                    Speaker(
                        name = "John Doe",
                        tagline = "kenya partner lead",
                    ),
                ),
            )
        val viewModel = SpeakersScreenViewModel(speakersRepo, mockSyncDataWorkManager, testDispatcher)
        composeTestRule.setContent {
            ChaiTheme {
                SpeakersRoute(speakersScreenViewModel = viewModel)
            }
        }

        with(composeTestRule) {
            onNodeWithText("Speakers").assertIsDisplayed()
            onNodeWithContentDescription("Back arrow icon").assertIsDisplayed()
            onNodeWithContentDescription("Speaker headshot").assertIsDisplayed()
            onNodeWithText("John Doe").assertIsDisplayed()
            onNodeWithText("kenya partner lead", substring = true, ignoreCase = true).assertIsDisplayed()
            onNodeWithContentDescription("Search speakers").assertIsDisplayed()
            onNodeWithTag("speakingNowBadge").assertDoesNotExist()
        }
    }

    private fun speakers() =
        listOf(
            Speaker(
                name = "John Doe",
                tagline = "kenya partner lead",
                biography = "Android engineer who loves Compose",
            ),
            Speaker(
                name = "Jane Smith",
                tagline = "GDE Android",
                biography = "Works on Kotlin Multiplatform",
            ),
        )

    private fun viewModelWith(speakers: List<Speaker>): SpeakersScreenViewModel {
        every { mockSyncDataWorkManager.isSyncing } returns flowOf(true)
        coEvery { mockSyncDataWorkManager.startSync() } just runs
        coEvery { speakersRepo.fetchSpeakers() } returns flowOf(speakers)
        return SpeakersScreenViewModel(speakersRepo, mockSyncDataWorkManager, testDispatcher)
    }

    @Test
    fun `should filter speakers by name`() {
        val viewModel = viewModelWith(speakers())
        composeTestRule.setContent {
            ChaiTheme {
                SpeakersRoute(speakersScreenViewModel = viewModel)
            }
        }

        with(composeTestRule) {
            onNodeWithContentDescription("Search speakers").performClick()
            onNodeWithTag("searchField").performTextInput("jane")

            onNodeWithText("Jane Smith").assertIsDisplayed()
            onNodeWithText("John Doe").assertDoesNotExist()
        }
    }

    @Test
    fun `should filter speakers by biography`() {
        val viewModel = viewModelWith(speakers())
        composeTestRule.setContent {
            ChaiTheme {
                SpeakersRoute(speakersScreenViewModel = viewModel)
            }
        }

        with(composeTestRule) {
            onNodeWithContentDescription("Search speakers").performClick()
            onNodeWithTag("searchField").performTextInput("multiplatform")

            onNodeWithText("Jane Smith").assertIsDisplayed()
            onNodeWithText("John Doe").assertDoesNotExist()
        }
    }

    @Test
    fun `should filter speakers by tagline`() {
        val viewModel = viewModelWith(speakers())
        composeTestRule.setContent {
            ChaiTheme {
                SpeakersRoute(speakersScreenViewModel = viewModel)
            }
        }

        with(composeTestRule) {
            onNodeWithContentDescription("Search speakers").performClick()
            onNodeWithTag("searchField").performTextInput("partner lead")

            onNodeWithText("John Doe").assertIsDisplayed()
            onNodeWithText("Jane Smith").assertDoesNotExist()
        }
    }

    @Test
    fun `should show empty state when nothing matches`() {
        val viewModel = viewModelWith(speakers())
        composeTestRule.setContent {
            ChaiTheme {
                SpeakersRoute(speakersScreenViewModel = viewModel)
            }
        }

        with(composeTestRule) {
            onNodeWithContentDescription("Search speakers").performClick()
            onNodeWithTag("searchField").performTextInput("nobody here")

            onNodeWithTag("emptySearchResults").assertIsDisplayed()
            onNodeWithText("John Doe").assertDoesNotExist()
            onNodeWithText("Jane Smith").assertDoesNotExist()
        }
    }

    @Test
    fun `should restore full list when search is cleared`() {
        val viewModel = viewModelWith(speakers())
        composeTestRule.setContent {
            ChaiTheme {
                SpeakersRoute(speakersScreenViewModel = viewModel)
            }
        }

        with(composeTestRule) {
            onNodeWithContentDescription("Search speakers").performClick()
            onNodeWithTag("searchField").performTextInput("jane")
            onNodeWithText("John Doe").assertDoesNotExist()

            onNodeWithContentDescription("Clear search").performClick()

            onNodeWithText("John Doe").assertIsDisplayed()
            onNodeWithText("Jane Smith").assertIsDisplayed()
        }
    }
}