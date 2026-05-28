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
package com.android254.presentation.sessions.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.android254.presentation.common.fake_data.fakeSessions
import com.android254.presentation.common.results_status.ResultStatus
import com.android254.presentation.models.EventDate
import com.android254.presentation.sessions.models.SessionsUiState
import com.droidconke.chai.ChaiDCKE22Theme
import com.droidconke.chai.chaiColorsPalette

class SessionStateProvider : PreviewParameterProvider<SessionsUiState> {
    override val values =
        sequenceOf(
            SessionsUiState(
                sessionStatus = ResultStatus.Loading,
            ),
            SessionsUiState(
                sessionStatus = ResultStatus.Error("Something went wrong"),
            ),
            SessionsUiState(
                sessionStatus = ResultStatus.Empty("No sessions found"),
            ),
            SessionsUiState(
                sessions = fakeSessions,
                sessionStatus = ResultStatus.Success,
            ),
        )
}

@PreviewLightDark
@Composable
fun SessionScreenPreview(
    @PreviewParameter(SessionStateProvider::class) state: SessionsUiState,
) {
    ChaiDCKE22Theme {
        Surface(
            color = MaterialTheme.chaiColorsPalette.background,
        ) {
            SessionsScreen(
                sessionsUiState = state,
                selectedEventDate = EventDate("2023-11-16", day = 1),
                isRefreshing = false,
                currentSelections = emptyList(),
                navigateToSessionDetails = {},
                onEvent = {},
            )
        }
    }
}