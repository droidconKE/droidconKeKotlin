package com.android254.presentation.sessions.models

import com.android254.presentation.models.SessionPresentationModel

data class SessionUIState(
    val current: List<SessionPresentationModel> = emptyList(),
    val upNext: List<SessionPresentationModel> = emptyList(),
)