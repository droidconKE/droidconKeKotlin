/*
 * Copyright 2022 DroidconKE
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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android254.presentation.common.components.DroidconAppBarWithFilter
import com.android254.presentation.common.fakedata.DAY_TODAY
import com.android254.presentation.common.fakedata.DAY_TOMORROW
import com.android254.presentation.common.fakedata.DAY_YESTERDAY
import com.android254.presentation.common.fakedata.fakeSessions
import com.android254.presentation.common.resultstatus.ResultStatus
import com.android254.presentation.models.EventDate
import com.android254.presentation.models.SessionPresentationModel
import com.android254.presentation.models.SessionsFilterOption
import com.android254.presentation.sessions.components.CustomSwitch
import com.android254.presentation.sessions.components.EventDaySelector
import com.android254.presentation.sessions.components.SessionsFilterPanel
import com.android254.presentation.sessions.components.SessionsStateComponent
import com.android254.presentation.sessions.models.SessionsIntentHandler
import com.android254.presentation.sessions.models.SessionsUiState
import com.android254.presentation.utils.ChaiLightAndDarkComposePreviews
import com.droidconke.chai.ChaiTheme
import com.droidconke.chai.atoms.ChaiGrey90
import com.droidconke.chai.chaiColorsPalette
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap

@Composable
fun SessionsRoute(
    sessionsViewModel: SessionsViewModel = hiltViewModel(),
    navigateToSessionDetails: (sessionId: String) -> Unit = {},
) {
    val isRefreshing by sessionsViewModel.isRefreshing.collectAsStateWithLifecycle()
    val sessionsUiState by sessionsViewModel.sessionsUiState.collectAsStateWithLifecycle()
    val currentSelections by sessionsViewModel.selectedFilterOptions.collectAsStateWithLifecycle()
    val selectedEventDate by sessionsViewModel.selectedEventDay.collectAsStateWithLifecycle()

    val onEvent = sessionsViewModel::handleEvent

    SessionsScreen(
        sessionsUiState = sessionsUiState,
        isRefreshing = isRefreshing,
        navigateToSessionDetails = navigateToSessionDetails,
        selectedEventDate = selectedEventDate,
        currentSelections = currentSelections,
        onEvent = onEvent,
    )
}

@Composable
fun SessionsScreen(
    sessionsUiState: SessionsUiState,
    selectedEventDate: EventDate,
    isRefreshing: Boolean,
    currentSelections: ImmutableList<SessionsFilterOption>,
    navigateToSessionDetails: (sessionId: String) -> Unit,
    onEvent: (SessionsIntentHandler) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Derived from the ViewModel rather than mirrored here, so rotation cannot leave the
    // switch and the applied filter disagreeing.
    val showMySessions = sessionsUiState.showMySessionsOnly

    val bottomSheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        )

    val isSessionLayoutList =
        rememberSaveable {
            mutableStateOf(true)
        }

    var showFilterSheet by rememberSaveable { mutableStateOf(false) }

    BackHandler(showFilterSheet) {
        showFilterSheet = false
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            DroidconAppBarWithFilter(
                isListActive = isSessionLayoutList.value,
                onListIconClick = {
                    isSessionLayoutList.value = true
                },
                onAgendaIconClick = {
                    isSessionLayoutList.value = false
                },
                isFilterActive = sessionsUiState.isFilterActive,
                onFilterButtonClick = { showFilterSheet = true },
            )
        },
        containerColor = MaterialTheme.chaiColorsPalette.background,
    ) { paddingValues ->

        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 0.dp, end = 0.dp, top = 5.dp, bottom = 12.dp),
            ) {
                EventDaySelector(
                    selectedDate = selectedEventDate,
                    updateSelectedDay = {
                        onEvent(SessionsIntentHandler.UpdateSelectedDay(it))
                    },
                    eventDates = sessionsUiState.eventDays,
                    isLoading = sessionsUiState.sessionStatus is ResultStatus.Loading,
                )
                CustomSwitch(
                    checked = showMySessions,
                    onCheckedChange = { checked ->
                        // Off clears every filter; on adds only the bookmark facet.
                        if (checked) {
                            onEvent(SessionsIntentHandler.ToggleBookmarkFilter)
                        } else {
                            onEvent(SessionsIntentHandler.ClearSelectedFilterList)
                        }
                    },
                )
            }
            SessionsStateComponent(
                sessionsUiState = sessionsUiState,
                navigateToSessionDetails = navigateToSessionDetails,
                isRefreshing = isRefreshing,
                sessionScreenState =
                    if (showMySessions) SessionScreenState.MYSESSIONS else SessionScreenState.ALL,
                isSessionLayoutList = isSessionLayoutList.value,
                onEvent = onEvent,
            )
            // Gated on our own flag, not `bottomSheetState.isVisible`, which would skip
            // the enter animation.
            if (showFilterSheet) {
                ModalBottomSheet(
                    sheetState = bottomSheetState,
                    onDismissRequest = { showFilterSheet = false },
                    shape = RoundedCornerShape(0.dp),
                    containerColor = ChaiGrey90.copy(alpha = 0.52f),
                    dragHandle = {},
                ) {
                    SessionsFilterPanel(
                        onDismiss = { showFilterSheet = false },
                        selectableFilters = sessionsUiState.availableFilters,
                        currentSelections = currentSelections,
                        updateSelectedFilterOptionList = {
                            onEvent(SessionsIntentHandler.UpdateSelectedFilterOptionList(it))
                        },
                        clearSelectedFilterList = {
                            onEvent(SessionsIntentHandler.ClearSelectedFilterList)
                        },
                    )
                }
            }
        }
    }
}

@ChaiLightAndDarkComposePreviews
@Composable
private fun SessionsScreenPreview(
    @PreviewParameter(SessionsUiStateProvider::class) sessionsUiState: SessionsUiState,
) {
    ChaiTheme {
        SessionsScreen(
            sessionsUiState = sessionsUiState,
            selectedEventDate = EventDate(DAY_TODAY, day = 2),
            isRefreshing = false,
            currentSelections = persistentListOf(),
            navigateToSessionDetails = {},
            onEvent = {},
        )
    }
}

class SessionsUiStateProvider : PreviewParameterProvider<SessionsUiState> {
    override val values =
        sequenceOf(
            SessionsUiState(
                sessionStatus = ResultStatus.Loading,
            ),
            SessionsUiState(
                sessionStatus = ResultStatus.Empty("No sessions found"),
            ),
            SessionsUiState(
                sessionStatus = ResultStatus.Error("Something went wrong"),
            ),
            SessionsUiState(
                sessions =
                    fakeSessions
                        .groupBy { session: SessionPresentationModel -> "${session.startTime} ${session.amOrPm}" }
                        .mapValues { it.value.toImmutableList() }
                        .toImmutableMap(),
                sessionStatus = ResultStatus.Success,
                eventDays =
                    persistentListOf(
                        EventDate(DAY_YESTERDAY, day = 1),
                        EventDate(DAY_TODAY, day = 2),
                        EventDate(DAY_TOMORROW, day = 3),
                    ),
            ),
        )
}

enum class SessionScreenState {
    ALL,
    MYSESSIONS,
}