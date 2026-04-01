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
import androidx.compose.foundation.background
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android254.presentation.common.components.DroidconAppBarWithFilter
import com.android254.presentation.models.EventDate
import com.android254.presentation.models.SessionsFilterOption
import com.android254.presentation.sessions.components.CustomSwitch
import com.android254.presentation.sessions.components.EventDaySelector
import com.android254.presentation.sessions.components.SessionsFilterPanel
import com.android254.presentation.sessions.components.SessionsStateComponent
import com.android254.presentation.sessions.models.SessionsIntentHandler
import com.android254.presentation.sessions.models.SessionsUiState
import com.android254.presentation.utils.ChaiLightAndDarkComposePreview
import com.droidconke.chai.ChaiDCKE22Theme
import com.droidconke.chai.atoms.ChaiGrey90
import com.droidconke.chai.chaiColorsPalette
import kotlinx.coroutines.launch

@Composable
fun SessionsRoute(
    sessionsViewModel: SessionsViewModel = hiltViewModel(),
    navigateToSessionDetails: (sessionId: String) -> Unit = {},
) {
    val isRefreshing by sessionsViewModel.isRefreshing.collectAsStateWithLifecycle()
    val sessionsUiState by sessionsViewModel.sessionsUiState.collectAsStateWithLifecycle()
    val currentSelections by sessionsViewModel.selectedFilterOptions.collectAsStateWithLifecycle()

    val onEvent = sessionsViewModel::handleEvent

    SessionsScreen(
        sessionsUiState = sessionsUiState,
        isRefreshing = isRefreshing,
        navigateToSessionDetails = navigateToSessionDetails,
        selectedEventDate = sessionsUiState.selectedEventDay,
        currentSelections = currentSelections,
        onEvent = onEvent
    )
}

@Composable
fun SessionsScreen(
    sessionsUiState: SessionsUiState,
    selectedEventDate: EventDate,
    isRefreshing: Boolean,
    currentSelections: List<SessionsFilterOption>,
    navigateToSessionDetails: (sessionId: String) -> Unit,
    onEvent: (SessionsIntentHandler) -> Unit,
) {
    val showMySessions =
        remember {
            mutableStateOf(false)
        }

    val scope = rememberCoroutineScope()
    val bottomSheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        )

    val isSessionLayoutList =
        rememberSaveable {
            mutableStateOf(true)
        }

    val isFilterActive =
        rememberSaveable {
            mutableStateOf(true)
        }

    val isFilterDialogOpen =
        rememberSaveable {
            mutableStateOf(false)
        }

    val sessionScreenSessionsState =
        rememberSaveable {
            mutableStateOf(SessionScreenState.ALL)
        }

    BackHandler(bottomSheetState.isVisible) {
        scope.launch { bottomSheetState.hide() }
    }

    Scaffold(
        topBar = {
            DroidconAppBarWithFilter(
                isListActive = isSessionLayoutList.value,
                onListIconClick = {
                    isSessionLayoutList.value = true
                },
                onAgendaIconClick = {
                    isSessionLayoutList.value = false
                },
                isFilterActive = isFilterActive.value,
                onFilterButtonClick = {
                    isFilterDialogOpen.value = true
                    scope.launch {
                        bottomSheetState.show()
                    }
                },
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
                )
                CustomSwitch(checked = showMySessions.value, onCheckedChange = {
                    showMySessions.value = it
                    isFilterActive.value = !it
                    if (showMySessions.value) {
                        sessionScreenSessionsState.value = SessionScreenState.MYSESSIONS
                        onEvent(SessionsIntentHandler.ToggleBookmarkFilter)
                    } else {
                        sessionScreenSessionsState.value = SessionScreenState.ALL
                        onEvent(SessionsIntentHandler.ClearSelectedFilterList)
                    }
                })
            }
            SessionsStateComponent(
                sessionsUiState = sessionsUiState,
                navigateToSessionDetails = navigateToSessionDetails,
                refreshSessionsList = {
                    onEvent(SessionsIntentHandler.RefreshSessions)
                },
                retry = { },
                isRefreshing = isRefreshing,
                sessionScreenState = sessionScreenSessionsState.value,
                isSessionLayoutList = isSessionLayoutList.value,
            )
            if (bottomSheetState.isVisible) {
                ModalBottomSheet(
                    sheetState = bottomSheetState,
                    onDismissRequest = {
                        scope.launch {
                            bottomSheetState.hide()
                        }
                    },
                    shape = RoundedCornerShape(0.dp),
                    containerColor = ChaiGrey90.copy(alpha = 0.52f),
                    dragHandle = {},
                ) {
                    SessionsFilterPanel(
                        onDismiss = {
                            scope.launch {
                                bottomSheetState.hide()
                            }
                        },
                        currentSelections = currentSelections,
                        updateSelectedFilterOptionList = {
                            onEvent(SessionsIntentHandler.UpdateSelectedFilterOptionList(it))
                        },
                        fetchSessionWithFilter = {
                            onEvent(SessionsIntentHandler.FetchSessionWithFilter)
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

@ChaiLightAndDarkComposePreview
@Composable
fun SessionsScreenPreview() {
    ChaiDCKE22Theme {
        SessionsScreen(
            sessionsUiState = SessionsUiState(),
            selectedEventDate = EventDate("1", day = 1),
            isRefreshing = false,
            currentSelections = listOf(),
            navigateToSessionDetails = {},
            onEvent = {},
        )
    }
}

enum class SessionScreenState {
    ALL,
    MYSESSIONS,
}