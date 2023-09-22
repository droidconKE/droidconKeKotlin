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
package com.android254.presentation.sessions.view

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.android254.domain.models.Session
import com.android254.domain.repos.SessionsRepo
import com.android254.presentation.common.theme.DroidconKE2023Theme
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

/**
 * TODO - add more tests as we work with API data
 *
 */
@RunWith(RobolectricTestRunner::class)
@Config(instrumentedPackages = ["androidx.loader.content"], sdk = [33])
class SessionScreenTest {
    private val repo = mockk<SessionsRepo>()
    private val mockSyncDataWorkManager = FakeSyncWorkManager()

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    @Throws(Exception::class)
    fun setUp() {
        ShadowLog.stream = System.out
    }

    @Test
    fun hasExpectedButtons() = runTest {
        val navController = TestNavHostController(
            ApplicationProvider.getApplicationContext()
        )
        every { repo.fetchSessions() } returns flowOf(mockSessions)
        every { repo.fetchBookmarkedSessions() } returns flowOf(mockSessions)

        composeTestRule.setContent {
            DroidconKE2023Theme {
                SessionsRoute(
                    sessionsViewModel = SessionsViewModel(repo, mockSyncDataWorkManager)
                )
            }
        }

        composeTestRule.onNodeWithText("Day 1").assertExists()
        composeTestRule.onNodeWithText("Day 2").assertExists()
        composeTestRule.onNodeWithText("Day 3").assertExists()
        composeTestRule.onNodeWithTag("droidcon_topBar_with_Filter").assertExists()
        composeTestRule.onNodeWithTag("droidcon_topBar_with_Filter").assertIsDisplayed()
    }

    @Test
    fun `should show topBar`() = runTest {
        val navController = TestNavHostController(
            ApplicationProvider.getApplicationContext()
        )

        every { repo.fetchSessions() } returns flowOf(mockSessions)
        every { repo.fetchBookmarkedSessions() } returns flowOf(mockSessions)

        composeTestRule.setContent {
            DroidconKE2023Theme() {
                SessionsRoute(
                    sessionsViewModel = SessionsViewModel(repo, mockSyncDataWorkManager)
                )
            }
        }

        composeTestRule.onNodeWithTag("droidcon_topBar_with_Filter").assertExists()
        composeTestRule.onNodeWithTag("droidcon_topBar_with_Filter").assertIsDisplayed()
    }
}

val mockSessions = listOf(
    Session(
        id = "1",
        endDateTime = "2023-08-17T12:00:00Z",
        endTime = "12:00 PM",
        isBookmarked = false,
        isKeynote = true,
        isServiceSession = false,
        sessionImage = "https://example.com/session-1.jpg",
        startDateTime = "2023-08-17T10:00:00Z",
        startTime = "10:00 AM",
        rooms = "Room 1",
        speakers = "John Doe, Jane Doe",
        remote_id = "1234567890",
        description = "This is a keynote session about the future of technology.",
        sessionFormat = "Keynote",
        sessionLevel = "Beginner",
        slug = "keynote-session",
        title = "The Future of Technology"
    ),
    Session(
        id = "2",
        endDateTime = "2023-08-17T13:00:00Z",
        endTime = "1:00 PM",
        isBookmarked = true,
        isKeynote = false,
        isServiceSession = false,
        sessionImage = "https://example.com/session-2.jpg",
        startDateTime = "2023-08-17T11:00:00Z",
        startTime = "11:00 AM",
        rooms = "Room 2",
        speakers = "Steve Smith, Bill Jones",
        remote_id = "9876543210",
        description = "This is a session about the latest trends in artificial intelligence.",
        sessionFormat = "Workshop",
        sessionLevel = "Intermediate",
        slug = "ai-trends",
        title = "The Latest Trends in Artificial Intelligence"
    ),
    Session(
        id = "3",
        endDateTime = "2023-08-17T14:00:00Z",
        endTime = "2:00 PM",
        isBookmarked = false,
        isKeynote = false,
        isServiceSession = true,
        sessionImage = null,
        startDateTime = "2023-08-17T12:00:00Z",
        startTime = "12:00 PM",
        rooms = "Room 3",
        speakers = "No speakers",
        remote_id = "",
        description = "This is a service session about how to use the conference app.",
        sessionFormat = "Service Session",
        sessionLevel = "All Levels",
        slug = "conference-app",
        title = "How to Use the Conference App"
    )
)