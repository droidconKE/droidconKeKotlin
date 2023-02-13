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

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android254.presentation.models.SessionDetailsPresentationModel
import org.junit.Before
import org.junit.Rule
import org.robolectric.shadows.ShadowLog

// @RunWith(RobolectricTestRunner::class)
// @Config(instrumentedPackages = ["androidx.loader.content"])
class SessionDetailsScreenTest {

    // TODO Fix tests

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    @Throws(Exception::class)
    fun setUp() {
        ShadowLog.stream = System.out

//        val sessionId = "randomSessionId"
//        val repo = mockk<SessionsRepo>()
//        val viewModel = SessionDetailsViewModel(repo)
//        val channel = Channel<SessionDetailsPresentationModel>()
//        val flow = channel.consumeAsFlow()
//        coEvery { repo.fetchSessionById(any()) } returns flowOf(ResourceResult.Empty(""))
//
//        composeTestRule.setContent {
//            DroidconKE2022Theme() {
//                SessionDetailsScreen(
//                    viewModel = viewModel,
//                    sessionId = sessionId,
//                    onNavigationIconClick = {}
//                )
//            }
//        }
    }

//    @Test
    fun `should show top bar and floating action button`() {
        composeTestRule.onNodeWithTag(TestTag.TOP_BAR).assertExists()
        composeTestRule.onNodeWithTag(TestTag.TOP_BAR).assertIsDisplayed()

        composeTestRule.onNodeWithTag(TestTag.FLOATING_ACTION_BUTTON).assertExists()
        composeTestRule.onNodeWithTag(TestTag.FLOATING_ACTION_BUTTON).assertIsDisplayed()
    }

//    @Test
    fun `should show favourite icon and session banner image`() {
        composeTestRule.onNodeWithTag(TestTag.FAVOURITE_ICON).apply {
            assertExists()
            assertIsDisplayed()
        }

        composeTestRule.onNodeWithTag(TestTag.IMAGE_BANNER).assertExists()
    }

//    @Test
    fun `test if speaker-name, session title & description, time, room, level and twitter handle are correctly shown`() {
        val textComposableTagToValueMap = mapOf(
            TestTag.SPEAKER_NAME to DUMMY_SESSION_DETAILS.speakerName,
            TestTag.SESSION_TITLE to DUMMY_SESSION_DETAILS.title,
            TestTag.SESSION_DESCRIPTION to DUMMY_SESSION_DETAILS.description,
            TestTag.TIME_SLOT to DUMMY_SESSION_DETAILS.timeSlot,
            TestTag.ROOM to DUMMY_SESSION_DETAILS.venue.uppercase(),
            TestTag.LEVEL to "#${DUMMY_SESSION_DETAILS.level.uppercase()}"
        )

        textComposableTagToValueMap.forEach { (testTag, value) ->
            composeTestRule.onNodeWithTag(testTag).apply {
                assertExists()
                assertIsDisplayed()
                assertTextEquals(value)
            }
        }
    }

//    @Test
    fun `test if twitter handle is shown`() {
        composeTestRule.onNodeWithTag(TestTag.TWITTER_HANDLE_TEXT, true).apply {
            assertExists()
//            assertIsDisplayed() // TODO: (Rashan) This assertion fails even when node is visible. Check semantic tree to resolve this
        }
    }

    companion object {
        val DUMMY_SESSION_DETAILS = SessionDetailsPresentationModel(
            speakerName = "Frank Tamre",
            isStarred = true,
            title = "Compose Beyond Material Design",
            description = "Been in the tech industry for over 20 years. Am passionate about developer communities, motivating people and building successfulBeen in the tech industry for over 20 years.",
            sessionImageUrl = "https://www.freepnglogos.com/uploads/twitter-logo-png/twitter-logo-vector-png-clipart-1.png",
            timeSlot = "9.30AM - 10:15AM",
            venue = "Room 1",
            level = "Beginner",
            twitterHandle = "PriestTamzi",
            id = "",
            amOrPm = "AM",
            endTime = "",
            format = "",
            speakerImage = "",
            startTime = ""
        )
    }
}