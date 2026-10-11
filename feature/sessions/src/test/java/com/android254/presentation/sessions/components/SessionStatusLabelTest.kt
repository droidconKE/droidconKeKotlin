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
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import com.android254.presentation.common.components.SessionsCard
import com.android254.presentation.common.fakedata.fakeSessions
import com.android254.presentation.models.SessionStatus
import com.droidconke.chai.ChaiTheme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SessionStatusLabelTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val session = fakeSessions.first()

    private fun showCard(status: SessionStatus) {
        composeTestRule.setContent {
            ChaiTheme {
                SessionsCard(
                    session = session.copy(sessionStatus = status),
                    navigateToSessionDetails = {},
                    onBookmark = {},
                )
            }
        }
    }

    @Test
    fun `a past session card says it has ended`() {
        showCard(SessionStatus.Past)

        composeTestRule.onAllNodesWithText("ENDED").assertCountEquals(1)
        composeTestRule.onAllNodesWithText("LIVE").assertCountEquals(0)
    }

    @Test
    fun `an ongoing session card says it is live`() {
        showCard(SessionStatus.Ongoing)

        composeTestRule.onAllNodesWithText("LIVE").assertCountEquals(1)
        composeTestRule.onAllNodesWithText("ENDED").assertCountEquals(0)
    }

    @Test
    fun `an upcoming session card carries no status label`() {
        showCard(SessionStatus.Upcoming)

        composeTestRule.onAllNodesWithText("LIVE").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("ENDED").assertCountEquals(0)
    }

    @Test
    fun `an ongoing agenda cell says it is live, not just by its border`() {
        val live = session.copy(sessionStatus = SessionStatus.Ongoing)
        composeTestRule.setContent {
            ChaiTheme {
                AgendaGrid(
                    sessionsByTime = persistentMapOf("${live.startTime} ${live.amOrPm}" to persistentListOf(live)),
                    navigateToSessionDetails = {},
                )
            }
        }

        composeTestRule.onAllNodesWithText("LIVE").assertCountEquals(live.roomList.size)
    }
}