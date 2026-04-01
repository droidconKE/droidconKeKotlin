package com.android254.presentation.sessions.models

import com.android254.presentation.models.EventDate
import com.android254.presentation.models.SessionsFilterOption

sealed interface SessionsIntentHandler {
    class UpdateSelectedFilterOptionList(val option: SessionsFilterOption): SessionsIntentHandler
    class UpdateSelectedDay(val day: EventDate): SessionsIntentHandler
    object ToggleBookmarkFilter: SessionsIntentHandler
    object RefreshSessions: SessionsIntentHandler
    object FetchSessionWithFilter: SessionsIntentHandler
    object ClearSelectedFilterList: SessionsIntentHandler
}