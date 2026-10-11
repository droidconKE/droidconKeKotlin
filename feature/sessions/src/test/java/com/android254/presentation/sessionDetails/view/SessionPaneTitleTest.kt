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
package com.android254.presentation.sessionDetails.view

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.android254.presentation.models.SessionDetailsPresentationModel
import com.android254.presentation.sessionDetails.SessionDetailsUiState
import com.droidconke.chai.ChaiTheme
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SessionPaneTitleTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `an open session names its pane so TalkBack announces it`() {
        show(SessionDetailsUiState.Success(data = session))

        composeTestRule.onNode(paneTitle("Welcome at DroidconKE")).assertExists()
    }

    @Test
    fun `a loading session still names its pane`() {
        show(SessionDetailsUiState.Loading)

        composeTestRule.onNode(paneTitle("Session details")).assertExists()
    }

    private fun paneTitle(title: String) = SemanticsMatcher.expectValue(SemanticsProperties.PaneTitle, title)

    private fun show(uiState: SessionDetailsUiState) {
        composeTestRule.setContent {
            ChaiTheme {
                SessionDetailsScreen(
                    uiState = uiState,
                    bookmarkSession = {},
                    unBookmarkSession = {},
                    onNavigationIconClick = {},
                )
            }
        }
    }

    private val session =
        SessionDetailsPresentationModel(
            id = "1",
            title = "Welcome at DroidconKE",
            description = "Welcome to DroidconKE.",
            venue = "Main Hall",
            startTime = "10:00",
            endTime = "11:00",
            amOrPm = "AM",
            isStarred = false,
            format = "Keynote",
            level = "Beginner",
            sessionImageUrl = "",
            timeSlot = "10:00 - 11:00 AM",
            speakers = persistentListOf(),
        )
}