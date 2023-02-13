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

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.android254.presentation.common.bottomsheet.BottomSheetScaffold
import com.android254.presentation.common.bottomsheet.rememberBottomSheetScaffoldState
import com.android254.presentation.common.components.*
import com.android254.presentation.common.theme.DroidconKE2022Theme
import com.android254.presentation.sessions.components.EventDaySelector
import com.android254.presentation.sessions.components.SessionList
import com.android254.presentation.sessions.components.SessionsFilterPanel
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SessionsScreen(
    darkTheme: Boolean = isSystemInDarkTheme(),
    navController: NavHostController,
    sessionsViewModel: SessionsViewModel = hiltViewModel()
) {
    val showMySessions = remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()

    val isSessionLayoutList = rememberSaveable {
        mutableStateOf(true)
    }

    val isFilterActive = rememberSaveable {
        mutableStateOf(true)
    }

    val isFilterDialogOpen = rememberSaveable {
        mutableStateOf(false)
    }

    BottomSheetScaffold(
        sheetContent = {
            SessionsFilterPanel(
                onDismiss = {
                    scope.launch {
                        bottomSheetScaffoldState.bottomSheetState.collapse()
                    }
                },
                onChange = {},
                viewModel = sessionsViewModel
            )
        },
        scaffoldState = bottomSheetScaffoldState,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetPeekHeight = 0.dp
    ) {
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
                            if (bottomSheetScaffoldState.bottomSheetState.isCollapsed) {
                                bottomSheetScaffoldState.bottomSheetState.expand()
                            } else {
                                bottomSheetScaffoldState.bottomSheetState.collapse()
                            }
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
                    EventDaySelector(viewModel = sessionsViewModel)
                    CustomSwitch(checked = showMySessions.value, onCheckedChange = {
                        showMySessions.value = it
                        isFilterActive.value = !it
                        if (showMySessions.value) {
                            sessionsViewModel.fetchBookmarkedSessions()
                        } else {
                            sessionsViewModel.clearSelectedFilterList()
                        }
                    })
                }
                SessionList(navController = navController, viewModel = sessionsViewModel)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun SessionsScreenPreview() {
    DroidconKE2022Theme {
        SessionsScreen(navController = rememberNavController())
    }
}

@Composable
fun CustomSwitch(
    width: Dp = 72.dp,
    height: Dp = 40.dp,
    checkedTrackColor: Color = Color(0xFF35898F),
    uncheckedTrackColor: Color = Color(0xFFe0e0e0),
    gapBetweenThumbAndTrackEdge: Dp = 8.dp,
    borderWidth: Dp = 2.dp,
    cornerSize: Int = 50,
    iconInnerPadding: Dp = 4.dp,
    thumbSize: Dp = 24.dp,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    // this is to disable the ripple effect
    val interactionSource = remember {
        MutableInteractionSource()
    }

    // for moving the thumb
    val alignment by animateAlignmentAsState(if (checked) 1f else -1f)

    // outer rectangle with border
    Box(
        modifier = Modifier
            .size(width = width, height = height)
            .border(
                width = borderWidth,
                color = if (checked) checkedTrackColor else uncheckedTrackColor,
                shape = RoundedCornerShape(percent = cornerSize)
            )
            .clickable(
                indication = null,
                interactionSource = interactionSource
            ) {
                onCheckedChange(!checked)
            },
        contentAlignment = Alignment.Center
    ) {
        // this is to add padding at the each horizontal side
        Box(
            modifier = Modifier
                .padding(
                    start = gapBetweenThumbAndTrackEdge,
                    end = gapBetweenThumbAndTrackEdge
                )
                .fillMaxSize(),
            contentAlignment = alignment
        ) {
            // thumb with icon
            Icon(
                imageVector = if (checked) Icons.Filled.Star else Icons.Filled.StarOutline,
                contentDescription = if (checked) "Enabled" else "Disabled",
                modifier = Modifier
                    .size(size = thumbSize)
                    .background(
                        color = if (checked) checkedTrackColor else uncheckedTrackColor,
                        shape = CircleShape
                    )
                    .padding(all = iconInnerPadding),
                tint = Color.White
            )
        }
    }
}

@Composable
private fun animateAlignmentAsState(
    targetBiasValue: Float
): State<BiasAlignment> {
    val bias by animateFloatAsState(targetBiasValue)
    return derivedStateOf { BiasAlignment(horizontalBias = bias, verticalBias = 0f) }
}