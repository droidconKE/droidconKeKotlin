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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
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
import com.android254.presentation.common.adaptive.readablePaneWidth
import com.android254.presentation.common.divider.CustomDivider
import com.android254.presentation.common.insets.DroidconWindowInsets
import com.android254.presentation.common.insets.StatusBarProtection
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
import com.droidconke.chai.ChaiTheme
import com.droidconke.chai.atoms.ChaiRed
import com.droidconke.chai.atoms.ChaiWhite
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.ChaiBodyMediumBold

@Composable
fun SessionDetailsRoute(
    viewModel: SessionDetailsViewModel,
    onNavigationIconClick: () -> Unit,
    showTopBar: Boolean = true,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SessionDetailsScreen(
        uiState = uiState,
        bookmarkSession = viewModel::bookmarkSession,
        unBookmarkSession = viewModel::unBookmarkSession,
        onNavigationIconClick = onNavigationIconClick,
        showTopBar = showTopBar,
    )
}

@Composable
internal fun SessionDetailsScreen(
    uiState: SessionDetailsUiState,
    bookmarkSession: (String) -> Unit,
    unBookmarkSession: (String) -> Unit,
    onNavigationIconClick: () -> Unit,
    showTopBar: Boolean = true,
) {
    // The scrim is drawn over the content, so the stacking is stated here rather than left to
    // whatever container the caller happens to use.
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            // As a detail pane the bar is pure duplication: the session's own title is the first
            // thing in the body, and the list beside it already says where you are — which is also
            // why there is no back arrow to draw.
            topBar = { if (showTopBar) TopBar(onNavigationIconClick) },
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
                        imageVector = Icons.AutoMirrored.Filled.Reply,
                        contentDescription = null,
                        tint = ChaiWhite,
                    )
                }
            },
            containerColor = MaterialTheme.chaiColorsPalette.background,
            contentWindowInsets = DroidconWindowInsets.screenContent,
        ) { paddingValues ->
            when (uiState) {
                is SessionDetailsUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                    ) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }

                is SessionDetailsUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
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
                        drawsUnderStatusBar = !showTopBar,
                    )
                }
            }
        }
        if (!showTopBar) {
            StatusBarProtection(modifier = Modifier.align(Alignment.TopCenter))
        }
    }
}

@Composable
fun Body(
    paddingValues: PaddingValues,
    sessionDetails: SessionDetailsPresentationModel,
    bookmarkSession: (String) -> Unit,
    unBookmarkSession: (String) -> Unit,
    modifier: Modifier = Modifier,
    drawsUnderStatusBar: Boolean = false,
) {
    Column(
        modifier =
            modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .readablePaneWidth()
                .fillMaxWidth()
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Inside the scroll, so the first line of the session title starts below the clock and
        // then travels under it, which is the whole point of dropping the bar.
        if (drawsUnderStatusBar) {
            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
        }
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
private fun SessionDetailsScreenPreview() {
    ChaiTheme {
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
            bookmarkSession = {},
            unBookmarkSession = {},
        )
    }
}