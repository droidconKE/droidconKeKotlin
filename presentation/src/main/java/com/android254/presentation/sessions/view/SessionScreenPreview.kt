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
    override val values = sequenceOf(
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
    @PreviewParameter(SessionStateProvider::class) state: SessionsUiState
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
