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
package com.android254.presentation.screenshots

import com.android254.presentation.about.view.AboutScreen
import com.android254.presentation.about.view.AboutScreenUiState
import com.android254.presentation.common.fakedata.fakeSessions
import com.android254.presentation.common.resultstatus.ResultStatus
import com.android254.presentation.feed.view.FeedScreen
import com.android254.presentation.feed.view.FeedUIState
import com.android254.presentation.feedback.view.FeedBackScreen
import com.android254.presentation.home.screen.HomeScreen
import com.android254.presentation.home.viewstate.HomeState
import com.android254.presentation.models.EventDate
import com.android254.presentation.models.FeedUI
import com.android254.presentation.models.OrganizingTeamMember
import com.android254.presentation.models.SessionDetailsPresentationModel
import com.android254.presentation.models.SessionDetailsSpeakerPresentationModel
import com.android254.presentation.models.speakersDummyData
import com.android254.presentation.sessionDetails.SessionDetailsUiState
import com.android254.presentation.sessionDetails.view.SessionDetailsScreen
import com.android254.presentation.sessions.models.SessionsUiState
import com.android254.presentation.sessions.view.SessionsScreen
import ke.droidcon.kotlin.screenshot.ChaiScreenshotTest
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import org.junit.Test

class ScreenScreenshotTest : ChaiScreenshotTest() {
    @Test
    fun home() =
        captureScreen("screens/home") {
            HomeScreen(
                viewState =
                    HomeState(
                        speakers = speakersDummyData.toImmutableList(),
                        sessions = fakeSessions,
                        isSyncing = false,
                    ),
                isSyncing = false,
            )
        }

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

    @Test
    fun feed() =
        captureScreen("screens/feed") {
            FeedScreen(
                feedUIState =
                    FeedUIState.Success(
                        feeds =
                            listOf(
                                FeedUI(
                                    title = "Call for speakers is open",
                                    body =
                                        "Submissions close at the end of the month. Talks, " +
                                            "workshops and lightning sessions all welcome.",
                                    topic = "Announcement",
                                    url = "",
                                    image = "",
                                    createdAt = "2026-08-01",
                                ),
                                FeedUI(
                                    title = "Venue announced",
                                    body = "This year we are back at the Sarit Expo Centre.",
                                    topic = "Logistics",
                                    url = "",
                                    image = "",
                                    createdAt = "2026-08-02",
                                ),
                            ),
                    ),
            )
        }

    @Test
    fun about() =
        captureScreen("screens/about") {
            AboutScreen(
                uiState =
                    AboutScreenUiState.Success(
                        teamMembers =
                            persistentListOf(
                                OrganizingTeamMember(
                                    name = "Member One",
                                    desc = "Organiser",
                                    image = "",
                                ),
                                OrganizingTeamMember(
                                    name = "Member Two",
                                    desc = "Organiser",
                                    image = "",
                                ),
                            ),
                        stakeHoldersLogos = emptyList(),
                    ),
            )
        }

    @Test
    fun feedback() =
        captureScreen("screens/feedback") {
            FeedBackScreen(darkTheme = false)
        }
}