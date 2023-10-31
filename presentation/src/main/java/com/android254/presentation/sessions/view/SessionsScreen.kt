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
import com.android254.presentation.common.theme.DroidconKE2023Theme
import com.android254.presentation.models.EventDate
import com.android254.presentation.models.SessionsFilterOption
import com.android254.presentation.sessions.components.CustomSwitch
import com.android254.presentation.sessions.components.EventDaySelector
import com.android254.presentation.sessions.components.SessionsFilterPanel
import com.android254.presentation.sessions.components.SessionsStateComponent
import com.android254.presentation.sessions.models.SessionsUiState
import com.android254.presentation.utils.ChaiLightAndDarkComposePreview
import kotlinx.coroutines.launch

@Composable
fun SessionsRoute(
    sessionsViewModel: SessionsViewModel = hiltViewModel(),
    navigateToSessionDetails: (sessionId: String) -> Unit = {}
) {
    val isRefreshing by sessionsViewModel.isRefreshing.collectAsStateWithLifecycle()
    val sessionsUiState by sessionsViewModel.sessionsUiState.collectAsStateWithLifecycle()
    val currentSelections by sessionsViewModel.selectedFilterOptions.collectAsStateWithLifecycle()

    SessionsScreen(
        sessionsUiState = sessionsUiState,
        isRefreshing = isRefreshing,
        navigateToSessionDetails = navigateToSessionDetails,
        selectedEventDate = sessionsUiState.selectedEventDay,
        currentSelections = currentSelections,
        updateSelectedDay = { sessionsViewModel.updateSelectedDay(it) },
        toggleBookmarkFilter = { sessionsViewModel.toggleBookmarkFilter() },
        refreshSessionList = { sessionsViewModel.refreshSessionList() },
        updateSelectedFilterOptionList = { sessionsViewModel.updateSelectedFilterOptionList(it) },
        fetchSessionWithFilter = { sessionsViewModel.fetchSessionWithFilter() },
        clearSelectedFilterList = { sessionsViewModel.clearSelectedFilterList() }
    )
}

@Composable
fun SessionsScreen(
    sessionsUiState: SessionsUiState,
    selectedEventDate: EventDate,
    isRefreshing: Boolean,
    currentSelections: List<SessionsFilterOption>,
    updateSelectedDay: (EventDate) -> Unit,
    navigateToSessionDetails: (sessionId: String) -> Unit,
    toggleBookmarkFilter: () -> Unit,
    refreshSessionList: () -> Unit,
    updateSelectedFilterOptionList: (SessionsFilterOption) -> Unit,
    fetchSessionWithFilter: () -> Unit,
    clearSelectedFilterList: () -> Unit
) {
    val showMySessions = remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val isSessionLayoutList = rememberSaveable {
        mutableStateOf(true)
    }

    val isFilterActive = rememberSaveable {
        mutableStateOf(true)
    }

    val isFilterDialogOpen = rememberSaveable {
        mutableStateOf(false)
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
                }
            )
        }
    ) { paddingValues ->

        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 0.dp, end = 0.dp, top = 5.dp, bottom = 12.dp)
            ) {
                EventDaySelector(
                    selectedDate = selectedEventDate,
                    updateSelectedDay = updateSelectedDay,
                    eventDates = sessionsUiState.eventDays
                )
                CustomSwitch(checked = showMySessions.value, onCheckedChange = {
                    showMySessions.value = it
                    isFilterActive.value = !it
                    if (showMySessions.value) {
                        toggleBookmarkFilter()
                    } else {
                        clearSelectedFilterList()
                    }
                })
            }
            SessionsStateComponent(
                sessionsUiState = sessionsUiState,
                navigateToSessionDetails = navigateToSessionDetails,
                refreshSessionsList = refreshSessionList,
                retry = { },
                isRefreshing = isRefreshing
            )
            if (bottomSheetState.isVisible) {
                ModalBottomSheet(
                    sheetState = bottomSheetState,
                    onDismissRequest = {
                        scope.launch {
                            bottomSheetState.hide()
                        }
                    }
                ) {
                    SessionsFilterPanel(
                        onDismiss = {
                            scope.launch {
                                bottomSheetState.hide()
                            }
                        },
                        currentSelections = currentSelections,
                        updateSelectedFilterOptionList = updateSelectedFilterOptionList,
                        fetchSessionWithFilter = fetchSessionWithFilter,
                        clearSelectedFilterList = clearSelectedFilterList
                    )
                }
            }
        }
    }
}

@ChaiLightAndDarkComposePreview
@Composable
fun SessionsScreenPreview() {
    DroidconKE2023Theme {
        SessionsScreen(
            sessionsUiState = SessionsUiState(),
            selectedEventDate = EventDate("1", day = 1),
            isRefreshing = false,
            currentSelections = listOf(),
            updateSelectedDay = {},
            navigateToSessionDetails = {},
            toggleBookmarkFilter = {},
            refreshSessionList = {},
            updateSelectedFilterOptionList = {},
            fetchSessionWithFilter = {},
            clearSelectedFilterList = {}
        )
    }
}