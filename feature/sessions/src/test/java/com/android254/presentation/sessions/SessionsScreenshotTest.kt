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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.android254.presentation.common.components.FilterOptionChips
import com.android254.presentation.common.fakedata.fakeSessions
import com.android254.presentation.common.resultstatus.ResultStatus
import com.android254.presentation.models.EventDate
import com.android254.presentation.models.SessionDetailsPresentationModel
import com.android254.presentation.models.SessionDetailsSpeakerPresentationModel
import com.android254.presentation.models.SessionStatus
import com.android254.presentation.models.SessionsFilterCategory
import com.android254.presentation.models.SessionsFilterOption
import com.android254.presentation.sessionDetails.SessionDetailsUiState
import com.android254.presentation.sessionDetails.view.SessionDetailsScreen
import com.android254.presentation.sessions.components.CurrentSessionComponent
import com.android254.presentation.sessions.models.SessionClash
import com.android254.presentation.sessions.models.SessionsUiState
import com.android254.presentation.sessions.view.SessionsScreen
import ke.droidcon.kotlin.screenshot.ChaiScreenshotTest
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import org.junit.Test

class SessionsScreenshotTest : ChaiScreenshotTest() {
    @Test
    fun sessions() = captureScreen("screens/sessions") { Sessions() }

    @Test
    fun `my sessions with a clash`() =
        captureScreen("screens/sessions_clash") {
            Sessions(
                showMySessionsOnly = true,
                clashes = persistentListOf(SessionClash(fakeSessions[4].title, fakeSessions[1].title)),
            )
        }

    @Test
    fun `my sessions with nothing starred`() = captureScreen("screens/sessions_empty_starred") { Sessions(showMySessionsOnly = true, isEmpty = true) }

    /** The column count and the gutter cap, at every size the app has to be right on. */
    @Test
    fun `sessions across form factors`() = captureFormFactors("form_factors/sessions") { Sessions() }

    /** A detail as a pane: no app bar, no back arrow, its own status bar scrim. */
    @Test
    fun `session details as a pane across form factors`() =
        captureFormFactors("form_factors/session_details_pane") {
            SessionDetails(showTopBar = false)
        }

    @Test
    fun `session details`() = captureScreen("screens/session_details") { SessionDetails() }

    /** A break: no speaker, level or abstract, so none of their labels. */
    @Test
    fun `service session details`() =
        captureScreen("screens/session_details_service") {
            SessionDetails(
                details =
                    keynote.copy(
                        title = "Recharge & Refuel",
                        description = "",
                        format = "",
                        level = "",
                        speakers = persistentListOf(),
                    ),
            )
        }

    /** A CFP abstract with hard wraps and lists, as the 2026 schedule sends them. */
    @Test
    fun `session details with a wrapped abstract`() =
        captureScreen("screens/session_details_abstract") {
            SessionDetails(
                details =
                    keynote.copy(
                        description =
                            "Shipping a decision you can defend takes two skills most teams only\r\n" +
                                "  half-learn.\r\n\r\nRunning it:\r\n" +
                                "  - Designing a test that can actually answer your question\r\n" +
                                "  - Reading results with confidence\r\n\r\nKey learning points\r\n\r\n" +
                                "1. Design a trustworthy experiment before writing any code\r\n" +
                                "2. Log exposure honestly",
                    ),
            )
        }

    /** The rail's nudge: live, your own next session with a title long enough to wrap, and a break. */
    @Test
    fun `now and next cards`() =
        captureComponent("components/now_and_next") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CurrentSessionComponent(session = fakeSessions[0].copy(sessionStatus = SessionStatus.Ongoing))
                CurrentSessionComponent(
                    session =
                        fakeSessions[1].copy(
                            title = "From Flag to Decision: Engineering and Running A/B Tests on Android You Can Actually Trust",
                            sessionStatus = SessionStatus.Upcoming,
                            isStarred = true,
                        ),
                )
                CurrentSessionComponent(
                    session =
                        fakeSessions[1].copy(
                            title = "Recharge & Refuel",
                            venue = "",
                            isService = true,
                            speakers = persistentListOf(),
                            sessionStatus = SessionStatus.Upcoming,
                        ),
                )
            }
        }

    @Test
    fun `filter chips`() =
        captureComponent("components/filter_chips") {
            val options =
                persistentListOf("Beginner", "Intermediate", "Advanced").map {
                    SessionsFilterOption(label = it, value = it, type = SessionsFilterCategory.Level)
                }
            FilterOptionChips(
                currentSelections = persistentListOf(options[1]),
                options = options.toImmutableList(),
                onClick = {},
            )
        }
}

@Composable
private fun Sessions(
    showMySessionsOnly: Boolean = false,
    clashes: ImmutableList<SessionClash> = persistentListOf(),
    isEmpty: Boolean = false,
) {
    SessionsScreen(
        sessionsUiState =
            SessionsUiState(
                sessions = if (isEmpty) persistentMapOf() else persistentMapOf("09:00 AM" to fakeSessions),
                sessionStatus = if (isEmpty) ResultStatus.Empty("") else ResultStatus.Success,
                eventDays = persistentListOf(EventDate("5", day = 1), EventDate("6", day = 2), EventDate("7", day = 3)),
                showMySessionsOnly = showMySessionsOnly,
                clashes = clashes,
            ),
        selectedEventDate = EventDate("5", day = 1),
        isRefreshing = false,
        currentSelections = persistentListOf(),
        navigateToSessionDetails = {},
        onEvent = {},
    )
}

private val keynote =
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
            persistentListOf(
                SessionDetailsSpeakerPresentationModel(
                    name = "Todd Jason",
                    speakerImage = "",
                    twitterHandle = "",
                ),
            ),
    )

@Composable
private fun SessionDetails(
    showTopBar: Boolean = true,
    details: SessionDetailsPresentationModel = keynote,
) {
    SessionDetailsScreen(
        uiState = SessionDetailsUiState.Success(data = details),
        bookmarkSession = {},
        unBookmarkSession = {},
        onNavigationIconClick = {},
        showTopBar = showTopBar,
    )
}