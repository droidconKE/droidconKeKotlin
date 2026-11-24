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
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.android254.presentation.models.SessionDetailsPresentationModel
import com.android254.presentation.sessionDetails.SessionDetailsUiState
import com.android254.presentation.sessionDetails.view.components.TestTag
import com.droidconke.chai.ChaiTheme
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Half-open with the hinge horizontal. The posture is passed in rather than produced. */
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

    @Test
    fun `tabletop posture places banner above and controls below an asymmetric hinge`() {
        val hinge = Rect(left = 0f, top = 260f, right = 400f, bottom = 300f)
        setContent(isTabletop = true, hingeBoundsInWindow = hinge, showTopBar = false)

        val bannerBounds = composeTestRule.onNodeWithTag("tabletop_banner").getUnclippedBoundsInRoot()
        val controlsBounds = composeTestRule.onNodeWithTag("tabletop_controls").getUnclippedBoundsInRoot()

        assertTrue(
            "Expected banner bottom (${bannerBounds.bottom}) to sit above controls top (${controlsBounds.top})",
            bannerBounds.bottom < controlsBounds.top,
        )
    }

    private fun setContent(
        isTabletop: Boolean,
        hingeBoundsInWindow: Rect? = null,
        showTopBar: Boolean = true,
    ) {
        composeTestRule.setContent {
            ChaiTheme {
                Box(modifier = Modifier.size(width = 400.dp, height = 800.dp)) {
                    SessionDetailsScreen(
                        uiState = SessionDetailsUiState.Success(data = session),
                        bookmarkSession = {},
                        unBookmarkSession = {},
                        onNavigationIconClick = {},
                        showTopBar = showTopBar,
                        isTabletop = isTabletop,
                        hingeBoundsInWindow = hingeBoundsInWindow,
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
            speakers = persistentListOf(),
        )
}