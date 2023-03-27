package com.android254.presentation.sessions.models

import com.android254.presentation.models.SessionPresentationModel

sealed interface SessionsUiState{
    object Idle: SessionsUiState
    object Loading: SessionsUiState
    data class Data(val  data: List<SessionPresentationModel>): SessionsUiState
    @JvmInline
    value class Error(val message: String): SessionsUiState
    @JvmInline
    value  class Empty(val message: String): SessionsUiState
}
