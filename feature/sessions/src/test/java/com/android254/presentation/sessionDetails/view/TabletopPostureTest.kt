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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android254.presentation.models.SessionDetailsPresentationModel
import com.android254.presentation.sessionDetails.SessionDetailsUiState
import com.android254.presentation.sessionDetails.view.components.TestTag
import com.droidconke.chai.ChaiTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Half-open with the hinge horizontal. No device on hand folds, so the posture is passed in
 * rather than produced, which is the only way this layout gets tested at all.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TabletopPostureTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `a tabletop posture splits the session across the fold`() {
        setContent(isTabletop = true)

        composeTestRule.onNodeWithTag(TestTag.TABLETOP_BODY).assertExists()
    }

    @Test
    fun `an ordinary window scrolls the session as one column`() {
        setContent(isTabletop = false)

        composeTestRule.onNodeWithTag(TestTag.TABLETOP_BODY).assertDoesNotExist()
    }

    private fun setContent(isTabletop: Boolean) {
        composeTestRule.setContent {
            ChaiTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    SessionDetailsScreen(
                        uiState = SessionDetailsUiState.Success(data = session),
                        bookmarkSession = {},
                        unBookmarkSession = {},
                        onNavigationIconClick = {},
                        isTabletop = isTabletop,
                    )
                }
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
            speakers = emptyList(),
        )
}