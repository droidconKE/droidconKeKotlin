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
import com.android254.presentation.common.theme.DroidconKE2023Theme
import com.android254.presentation.models.EventDate
import com.android254.presentation.sessions.models.SessionsUiState
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

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    @Throws(Exception::class)
    fun setUp() {
        ShadowLog.stream = System.out
    }

    @Test
    fun hasExpectedButtons() = runTest {
        composeTestRule.setContent {
            DroidconKE2023Theme {
                SessionsScreen(
                    sessionsUiState = SessionsUiState(eventDays = listOf(EventDate("16", 1), EventDate("17", 2), EventDate("18", 3))),
                    isRefreshing = true,
                    selectedEventDate = EventDate("1", 1),
                    currentSelections = emptyList(),
                    updateSelectedDay = {},
                    navigateToSessionDetails = {},
                    toggleBookmarkFilter = {},
                    refreshSessionList = {},
                    updateSelectedFilterOptionList = {},
                    fetchSessionWithFilter = {},
                    clearSelectedFilterList = {}
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
        composeTestRule.setContent {
            DroidconKE2023Theme() {
                SessionsScreen(
                    sessionsUiState = SessionsUiState(),
                    isRefreshing = true,
                    selectedEventDate = EventDate("1", 1),
                    currentSelections = emptyList(),
                    updateSelectedDay = {},
                    navigateToSessionDetails = {},
                    toggleBookmarkFilter = {},
                    refreshSessionList = {},
                    updateSelectedFilterOptionList = {},
                    fetchSessionWithFilter = {},
                    clearSelectedFilterList = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("droidcon_topBar_with_Filter").assertExists()
        composeTestRule.onNodeWithTag("droidcon_topBar_with_Filter").assertIsDisplayed()
    }
}