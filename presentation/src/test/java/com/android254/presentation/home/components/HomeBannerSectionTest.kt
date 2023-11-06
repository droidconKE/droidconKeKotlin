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
package com.android254.presentation.home.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android254.presentation.home.viewstate.HomeViewState
import com.droidconke.chai.ChaiDCKE22Theme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
@Config(instrumentedPackages = ["androidx.loader.content"], sdk = [33])
class HomeBannerSectionTest {
    private val homeViewState = HomeViewState()

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    @Throws(Exception::class)
    fun setUp() {
        ShadowLog.stream = System.out
    }

    @Test
    fun `Test home poster is displayed`() {
        composeTestRule.setContent {
            ChaiDCKE22Theme {
                HomeBannerSection(homeViewState)
            }
        }
        composeTestRule.onNodeWithTag("home_event_poster").assertIsDisplayed()
    }

    @Test
    fun `Test home poster is hidden`() {
        composeTestRule.setContent {
            ChaiDCKE22Theme {
                HomeBannerSection(homeViewState.copy(isPosterVisible = false))
            }
        }
        composeTestRule.onNodeWithTag("home_event_poster").assertDoesNotExist()
    }

    @Test
    fun `Test home call for speakers is displayed`() {
        composeTestRule.setContent {
            ChaiDCKE22Theme {
                HomeBannerSection(homeViewState.copy(isCallForSpeakersVisible = true))
            }
        }
        composeTestRule.onNodeWithTag("home_call_for_speakers_link").assertIsDisplayed()
    }

    @Test
    fun `Test home call for speakers is hidden`() {
        composeTestRule.setContent {
            ChaiDCKE22Theme {
                HomeBannerSection(homeViewState.copy(isCallForSpeakersVisible = false))
            }
        }
        composeTestRule.onNodeWithTag("home_call_for_speakers_link").assertDoesNotExist()
    }
}