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
package com.android254.presentation.sessionDetails.view

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android254.presentation.common.divider.CustomDivider
import com.android254.presentation.models.SessionDetailsPresentationModel
import com.android254.presentation.models.SessionDetailsSpeakerPresentationModel
import com.android254.presentation.sessionDetails.SessionDetailsUiState
import com.android254.presentation.sessionDetails.SessionDetailsViewModel
import com.android254.presentation.sessionDetails.view.components.SessionBannerImage
import com.android254.presentation.sessionDetails.view.components.SessionLevel
import com.android254.presentation.sessionDetails.view.components.SessionSpeakerNameAndFavouriteIcon
import com.android254.presentation.sessionDetails.view.components.SessionTimeAndRoom
import com.android254.presentation.sessionDetails.view.components.SessionTitleAndDescription
import com.android254.presentation.sessionDetails.view.components.SpeakerTwitterHandle
import com.android254.presentation.sessionDetails.view.components.TestTag
import com.android254.presentation.sessionDetails.view.components.TopBar
import com.droidconke.chai.ChaiDCKE22Theme
import com.droidconke.chai.atoms.ChaiRed
import com.droidconke.chai.atoms.ChaiWhite
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.ChaiBodyMediumBold

@Composable
fun SessionDetailsRoute(
    viewModel: SessionDetailsViewModel,
    sessionId: String,
    onNavigationIconClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SessionDetailsScreen(
        uiState = uiState,
        sessionId = sessionId,
        bookmarkSession = viewModel::bookmarkSession,
        unBookmarkSession = viewModel::unBookmarkSession,
        onNavigationIconClick = onNavigationIconClick,
    )
}

@Composable
private fun SessionDetailsScreen(
    uiState: SessionDetailsUiState,
    sessionId: String,
    bookmarkSession: (String) -> Unit,
    unBookmarkSession: (String) -> Unit,
    onNavigationIconClick: () -> Unit,
) {
    Scaffold(
        topBar = { TopBar(onNavigationIconClick) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {},
                modifier =
                    Modifier
                        .size(44.dp)
                        .testTag(TestTag.FLOATING_ACTION_BUTTON),
                containerColor = ChaiRed,
                shape = CircleShape,
            ) {
                Icon(
                    modifier = Modifier.scale(scaleX = -1f, scaleY = 1f),
                    imageVector = Icons.Filled.Reply,
                    contentDescription = null,
                    tint = ChaiWhite,
                )
            }
        },
        containerColor = MaterialTheme.chaiColorsPalette.background,
    ) { paddingValues ->
        when (uiState) {
            is SessionDetailsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }

            is SessionDetailsUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    ChaiBodyMediumBold(
                        modifier = Modifier.align(Alignment.Center),
                        bodyText = uiState.message,
                        textColor = MaterialTheme.chaiColorsPalette.textNormalColor,
                    )
                }
            }

            is SessionDetailsUiState.Success -> {
                Body(
                    paddingValues = paddingValues,
                    sessionDetails = uiState.data,
                    bookmarkSession = bookmarkSession,
                    unBookmarkSession = unBookmarkSession,
                )
            }
        }
    }
}

@Composable
fun Body(
    paddingValues: PaddingValues,
    sessionDetails: SessionDetailsPresentationModel,
    bookmarkSession: (String) -> Unit,
    unBookmarkSession: (String) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .padding(paddingValues)
                .fillMaxWidth()
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CustomDivider()
        Column(modifier = Modifier.padding(start = 18.dp, end = 18.dp)) {
            Spacer(modifier = Modifier.height(24.dp))

            SessionSpeakerNameAndFavouriteIcon(
                sessionDetails = sessionDetails,
                bookmarkSession = bookmarkSession,
                unBookmarkSession = unBookmarkSession,
            )

            Spacer(modifier = Modifier.height(25.dp))

            SessionTitleAndDescription(sessionDetails)

            Spacer(modifier = Modifier.height(15.dp))

            SessionBannerImage(sessionDetails)
        }

        Spacer(modifier = Modifier.height(25.dp))

        CustomDivider()

        Spacer(modifier = Modifier.height(19.dp))

        Column(modifier = Modifier.padding(start = 18.dp, end = 18.dp)) {
            SessionTimeAndRoom(sessionDetails)

            Spacer(modifier = Modifier.height(15.dp))

            SessionLevel(sessionDetails.level)

            Spacer(modifier = Modifier.height(18.dp))
        }

        CustomDivider()

        Spacer(modifier = Modifier.height(18.dp))

        Column(modifier = Modifier.padding(start = 18.dp, end = 18.dp)) {
            sessionDetails.speakers.forEach { speaker ->
                if (speaker.twitterHandle.isNotEmpty()) {
                    SpeakerTwitterHandle(speaker)
                }
            }
        }

        Spacer(modifier = Modifier.height(140.dp))
    }
}

@Preview(
    name = "Light",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun SessionDetailsScreenPreview() {
    ChaiDCKE22Theme {
        SessionDetailsScreen(
            onNavigationIconClick = {},
            uiState =
                SessionDetailsUiState.Success(
                    data =
                        SessionDetailsPresentationModel(
                            id = "1",
                            title = "Welcome at DroidconKE",
                            description = "Welcome to DroidconKE 2022. We are excited to have you here. We hope you will have a great time.",
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
                                listOf(
                                    SessionDetailsSpeakerPresentationModel(
                                        name = "Todd Jason",
                                        speakerImage = "",
                                        twitterHandle = "",
                                    ),
                                ),
                        ),
                ),
            sessionId = "1",
            bookmarkSession = {},
            unBookmarkSession = {},
        )
    }
}