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
package com.android254.presentation.sessionDetails.view

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import com.android254.domain.models.Session
import com.android254.domain.models.Speaker
import com.android254.domain.repos.SessionsRepo
import com.android254.presentation.common.navigation.Screens
import com.android254.presentation.common.theme.DroidconKE2023Theme
import com.android254.presentation.sessionDetails.SessionDetailsViewModel
import com.android254.presentation.sessions.mappers.toSessionDetailsPresentationModal
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
@Config(instrumentedPackages = ["androidx.loader.content"], sdk = [33])
class SessionDetailsScreenTest {
    private val sessionId = "randomSessionId"

    private val mockSavedStateHandle: SavedStateHandle = SavedStateHandle().apply {
        set(Screens.SessionDetails.sessionIdNavigationArgument, sessionId)
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    private val repo = mockk<SessionsRepo>(relaxed = true)
    private val viewModel = SessionDetailsViewModel(sessionsRepo = repo, mockSavedStateHandle)

    @Before
    @Throws(Exception::class)
    fun setUp() {
        ShadowLog.stream = System.out
        val sessionId = "randomSessionId"
        every { repo.fetchSessionById(sessionId) } returns flowOf(mockSession)
    }

    @Test
    fun `should show top bar and floating action button`() {
        composeTestRule.setContent {
            DroidconKE2023Theme() {
                SessionDetailsRoute(
                    viewModel = viewModel,
                    sessionId = sessionId,
                    onNavigationIconClick = {}
                )
            }
        }
        composeTestRule.onNodeWithTag(TestTag.TOP_BAR).assertExists()
        composeTestRule.onNodeWithTag(TestTag.TOP_BAR).assertIsDisplayed()

        composeTestRule.onNodeWithTag(TestTag.FLOATING_ACTION_BUTTON).assertExists()
        composeTestRule.onNodeWithTag(TestTag.FLOATING_ACTION_BUTTON).assertIsDisplayed()
    }

    @Test
    fun `should show favourite icon and session banner image`() {
        composeTestRule.setContent {
            DroidconKE2023Theme() {
                Body(
                    paddingValues = PaddingValues(1.dp),
                    darkTheme = false,
                    sessionDetails = sessionPresentationModel,
                    bookmarkSession = { },
                    unBookmarkSession = { }
                )
            }
        }
        composeTestRule.onNodeWithTag(TestTag.FAVOURITE_ICON, useUnmergedTree = true).apply {
            assertExists()
            assertIsDisplayed()
        }

        composeTestRule.onNodeWithTag(TestTag.IMAGE_BANNER).assertExists()
    }

    @Test
    fun `test if speaker-name, session title & description, time, room, level and twitter handle are correctly shown`() {
        composeTestRule.setContent {
            DroidconKE2023Theme() {
                Body(
                    paddingValues = PaddingValues(10.dp),
                    darkTheme = false,
                    sessionDetails = sessionPresentationModel,
                    bookmarkSession = { },
                    unBookmarkSession = { }
                )
            }
        }
        composeTestRule.onNodeWithTag(testTag = TestTag.SPEAKER_NAME).assertTextEquals(
            sessionPresentationModel.speakers.joinToString(" & ") { it.name }
        )
        composeTestRule.onNodeWithTag(testTag = TestTag.SESSION_TITLE).assertTextEquals(
            sessionPresentationModel.title
        )
        composeTestRule.onNodeWithTag(testTag = TestTag.SESSION_DESCRIPTION).assertTextEquals(
            sessionPresentationModel.description
        )
        composeTestRule.onNodeWithTag(testTag = TestTag.TIME_SLOT).assertTextEquals(
            sessionPresentationModel.timeSlot.uppercase()
        )
        composeTestRule.onNodeWithTag(testTag = TestTag.ROOM).assertTextEquals(
            sessionPresentationModel.venue.uppercase()
        )
        composeTestRule.onNodeWithTag(testTag = TestTag.LEVEL).assertTextEquals(
            "#${sessionPresentationModel.level.uppercase()}"
        )
    }

    @Test
    fun `test if twitter handle is shown`() {
        composeTestRule.setContent {
            DroidconKE2023Theme() {
                Body(
                    paddingValues = PaddingValues(10.dp),
                    darkTheme = false,
                    sessionDetails = sessionPresentationModel,
                    bookmarkSession = { },
                    unBookmarkSession = { }
                )
            }
        }
        composeTestRule.onNodeWithTag(TestTag.TWITTER_HANDLE_TEXT, true).apply {
            assertExists()
        }
    }

    companion object {
        val mockSession = Session(
            title = "Compose Beyond Material Design",
            description = "Been in the tech industry for over 20 years. Am passionate about developer communities, motivating people and building successfulBeen in the tech industry for over 20 years.",
            id = "",
            endTime = "",
            startTime = "2022-10-15 18:30:00",
            isBookmarked = true,
            isKeynote = true,
            isServiceSession = true,
            remoteId = "",
            sessionLevel = "",
            endDateTime = "",
            sessionImage = "",
            sessionFormat = "",
            startDateTime = "2022-10-15 18:30:00",
            slug = "",
            rooms = "",
            speakers = listOf(Speaker("","","", "", "", "", "","John Doe", twitter = "johnDoe")),
            eventDay = ""
        )
        val sessionPresentationModel = mockSession.toSessionDetailsPresentationModal()
    }
}