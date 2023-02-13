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
package com.android254.presentation.home.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android254.presentation.common.components.SponsorsCard
import com.android254.presentation.common.theme.DroidconKE2022Theme
import com.android254.presentation.home.components.HomeSessionSection
import com.android254.presentation.home.components.HomeSpeakersSection
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
@Config(instrumentedPackages = ["androidx.loader.content"])
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    @Throws(Exception::class)
    fun setUp() {
        ShadowLog.stream = System.out
    }

    @Test
    fun `Test home title is displayed`() {
        composeTestRule.setContent {
            DroidconKE2022Theme {
                HomeHeaderSection()
            }
        }

        composeTestRule.onNodeWithTag("home_header").assertIsDisplayed()
    }

    @Test
    fun `Test speakers view is displayed`() {
        composeTestRule.setContent {
            HomeSpeakersSection(speakers = emptyList())
        }

        composeTestRule.onNodeWithTag("speakersLabel").assertIsDisplayed()
        composeTestRule.onNodeWithTag("speakersRow").assertExists()
        composeTestRule.onNodeWithTag("viewAllBtn").assertExists()
    }

    @Test
    fun `Not signedIn droidcon topBar is displayed`() {
        composeTestRule.setContent {
            DroidconKE2022Theme {
                HomeToolbar(isSignedIn = false)
            }
        }

        composeTestRule.onNodeWithTag("droidcon_topBar_notSignedIn").assertExists()
        composeTestRule.onNodeWithTag("droidcon_topBar_notSignedIn").assertIsDisplayed()
    }

    @Test
    fun `SignedIn droidcon topBar is displayed`() {
        composeTestRule.setContent {
            DroidconKE2022Theme {
                HomeToolbar(isSignedIn = true)
            }
        }

        composeTestRule.onNodeWithTag("droidcon_topBar_with_Feedback").assertExists()
        composeTestRule.onNodeWithTag("droidcon_topBar_with_Feedback").assertIsDisplayed()
    }

    @Test
    fun `Test sponsors card is displayed`() {
        composeTestRule.setContent {
            SponsorsCard(sponsorsLogos = listOf("Google"))
        }
        composeTestRule.onNodeWithTag("sponsors_section")
    }

    @Test
    fun `Test sessions is displayed`() {
        composeTestRule.setContent {
            HomeSessionSection(
                sessions = emptyList(),
                onViewAllSessionClicked = {},
                onSessionClick = {}
            )
        }
        composeTestRule.onNodeWithTag("sectionHeader").assertIsDisplayed()
        composeTestRule.onNodeWithTag("viewAll").assertIsDisplayed()
        composeTestRule.onNodeWithTag("sessions").assertExists()
    }
}