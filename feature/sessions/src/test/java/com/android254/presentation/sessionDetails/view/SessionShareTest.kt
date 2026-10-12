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

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android254.presentation.models.SessionDetailsPresentationModel
import com.android254.presentation.models.SessionDetailsSpeakerPresentationModel
import com.android254.presentation.sessionDetails.SessionDetailsUiState
import com.android254.presentation.sessionDetails.view.components.TestTag
import com.droidconke.chai.ChaiTheme
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SessionShareTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `the share button is labelled and shares the open session`() {
        var shared: SessionDetailsPresentationModel? = null
        show(SessionDetailsUiState.Success(data = session)) { shared = it }

        composeTestRule.onNodeWithContentDescription("Share session").assertHasClickAction().performClick()

        assertEquals(session, shared)
    }

    @Test
    fun `there is nothing to share while the session loads`() {
        show(SessionDetailsUiState.Loading) {}

        composeTestRule.onNodeWithTag(TestTag.FLOATING_ACTION_BUTTON).assertDoesNotExist()
    }

    @Test
    fun `the shared text names the session, its slot, room and speakers`() {
        assertEquals(
            "Compose at scale\n10:00 - 11:00 AM · Sapphire\nAda Lovelace, Grace Hopper\nhttps://droidcon.co.ke",
            session.shareText(),
        )
    }

    @Test
    fun `blank parts are left out of the shared text`() {
        val bare = session.copy(venue = "", speakers = persistentListOf())

        assertEquals("Compose at scale\n10:00 - 11:00 AM\nhttps://droidcon.co.ke", bare.shareText())
    }

    private fun show(
        uiState: SessionDetailsUiState,
        onShare: (SessionDetailsPresentationModel) -> Unit,
    ) {
        composeTestRule.setContent {
            ChaiTheme {
                SessionDetailsScreen(
                    uiState = uiState,
                    bookmarkSession = {},
                    unBookmarkSession = {},
                    onNavigationIconClick = {},
                    onShare = onShare,
                )
            }
        }
    }

    private val session =
        SessionDetailsPresentationModel(
            id = "1",
            title = "Compose at scale",
            description = "",
            venue = "Sapphire",
            startTime = "10:00",
            endTime = "11:00",
            amOrPm = "AM",
            isStarred = false,
            format = "Talk",
            level = "Beginner",
            sessionImageUrl = "",
            timeSlot = "10:00 - 11:00 AM",
            speakers =
                persistentListOf(
                    SessionDetailsSpeakerPresentationModel(name = "Ada Lovelace", speakerImage = "", twitterHandle = ""),
                    SessionDetailsSpeakerPresentationModel(name = "Grace Hopper", speakerImage = "", twitterHandle = ""),
                ),
        )
}