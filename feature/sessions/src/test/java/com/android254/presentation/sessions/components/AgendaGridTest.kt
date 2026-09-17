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
package com.android254.presentation.sessions.components

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android254.presentation.common.fakedata.DAY_YESTERDAY
import com.android254.presentation.common.fakedata.fakeSessions
import com.android254.presentation.common.resultstatus.ResultStatus
import com.android254.presentation.sessions.models.SessionsUiState
import com.android254.presentation.sessions.view.SessionScreenState
import com.droidconke.chai.ChaiTheme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AgendaGridTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    // One day, as the screen always feeds it: the view model filters by the selected day, and
    // a grid of every day at once would put two sessions in the same room at the same time.
    private val day = fakeSessions.filter { it.eventDay == DAY_YESTERDAY }

    private val sessionsByTime =
        day
            .groupBy { "${it.startTime} ${it.amOrPm}" }
            .mapValues { it.value.toImmutableList() }
            .toImmutableMap()

    @Test
    fun `a two-room session appears under both of its rooms`() {
        val twoRoom = day.first { it.roomList.size > 1 }

        composeTestRule.setContent {
            ChaiTheme {
                AgendaGrid(sessionsByTime = sessionsByTime, navigateToSessionDetails = {})
            }
        }

        assertEquals(
            "The fixture must contain a session in two rooms or this proves nothing",
            2,
            twoRoom.roomList.size,
        )
        composeTestRule.onAllNodesWithText(twoRoom.title).assertCountEquals(twoRoom.roomList.size)
    }

    @Test
    fun `every room in the day gets a column`() {
        composeTestRule.setContent {
            ChaiTheme {
                AgendaGrid(sessionsByTime = sessionsByTime, navigateToSessionDetails = {})
            }
        }

        day
            .flatMap { it.roomList }
            .distinct()
            .forEach { room ->
                composeTestRule.onNodeWithText(room.uppercase()).assertIsDisplayed()
            }
    }

    @Test
    fun `a cell opens its session`() {
        val opened = mutableListOf<String>()
        val session = day.first()

        composeTestRule.setContent {
            ChaiTheme {
                AgendaGrid(
                    sessionsByTime =
                        persistentMapOf("${session.startTime} ${session.amOrPm}" to persistentListOf(session)),
                    navigateToSessionDetails = { opened += it },
                )
            }
        }

        composeTestRule.onAllNodesWithText(session.title)[0].performClick()

        assertEquals(listOf(session.id), opened)
    }

    @Test
    fun `a day with no sessions draws no grid at all`() {
        composeTestRule.setContent {
            ChaiTheme {
                AgendaGrid(sessionsByTime = persistentMapOf(), navigateToSessionDetails = {})
            }
        }

        composeTestRule.onNodeWithTag(AGENDA_GRID_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `the grid replaces the agenda card list once there is`() {
        composeTestRule.setContent {
            ChaiTheme {
                SessionsStateComponent(
                    sessionsUiState =
                        SessionsUiState(sessions = sessionsByTime, sessionStatus = ResultStatus.Success),
                    navigateToSessionDetails = {},
                    isRefreshing = false,
                    sessionScreenState = SessionScreenState.ALL,
                    isSessionLayoutList = false,
                    onEvent = {},
                    showAgendaGrid = true,
                )
            }
        }

        composeTestRule.onNodeWithTag(AGENDA_GRID_TEST_TAG).assertExists()
    }
}