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
package com.android254.presentation.sessions

import com.android254.presentation.common.fakedata.fakeSessions
import com.android254.presentation.common.resultstatus.ResultStatus
import com.android254.presentation.models.EventDate
import com.android254.presentation.models.SessionDetailsPresentationModel
import com.android254.presentation.models.SessionDetailsSpeakerPresentationModel
import com.android254.presentation.sessionDetails.SessionDetailsUiState
import com.android254.presentation.sessionDetails.view.SessionDetailsScreen
import com.android254.presentation.sessions.models.SessionsUiState
import com.android254.presentation.sessions.view.SessionsScreen
import ke.droidcon.kotlin.screenshot.ChaiScreenshotTest
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import org.junit.Test

class SessionsScreenshotTest : ChaiScreenshotTest() {
    @Test
    fun sessions() =
        captureScreen("screens/sessions") {
            SessionsScreen(
                sessionsUiState =
                    SessionsUiState(
                        sessions = persistentMapOf("09:00 AM" to fakeSessions),
                        sessionStatus = ResultStatus.Success,
                    ),
                selectedEventDate = EventDate("1", day = 1),
                isRefreshing = false,
                currentSelections = persistentListOf(),
                navigateToSessionDetails = {},
                onEvent = {},
            )
        }

    @Test
    fun `session details`() =
        captureScreen("screens/session_details") {
            SessionDetailsScreen(
                uiState =
                    SessionDetailsUiState.Success(
                        data =
                            SessionDetailsPresentationModel(
                                id = "1",
                                title = "Welcome at DroidconKE",
                                description =
                                    "Welcome to DroidconKE. We are excited to have you here " +
                                        "and hope you have a great time.",
                                venue = "Main Hall",
                                startTime = "10:00",
                                endTime = "11:00",
                                amOrPm = "AM",
                                isStarred = false,
                                format = "Keynote",
                                level = "Beginner",
                                sessionImageUrl = "",
                                timeSlot = "10:00 - 11:00 AM",
                                speakers =
                                    listOf(
                                        SessionDetailsSpeakerPresentationModel(
                                            name = "Todd Jason",
                                            speakerImage = "",
                                            twitterHandle = "",
                                        ),
                                    ),
                            ),
                    ),
                bookmarkSession = {},
                unBookmarkSession = {},
                onNavigationIconClick = {},
            )
        }
}