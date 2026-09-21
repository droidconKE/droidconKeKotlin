/*
 * Copyright 2025 DroidconKE
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
package com.android254.presentation.common.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material3.adaptive.navigation3.LocalListDetailSceneScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.android254.presentation.about.view.AboutRoute
import com.android254.presentation.common.components.DetailPanePlaceholder
import com.android254.presentation.common.livesessions.HappeningNowRoute
import com.android254.presentation.feed.view.FeedRoute
import com.android254.presentation.feedback.view.FeedBackRoute
import com.android254.presentation.home.screen.HomeRoute
import com.android254.presentation.sessionDetails.SessionDetailsViewModel
import com.android254.presentation.sessionDetails.view.SessionDetailsRoute
import com.android254.presentation.sessions.view.SessionsRoute
import com.android254.presentation.speakers.view.SpeakerDetailsRoute
import com.android254.presentation.speakers.view.SpeakersRoute
import ke.droidcon.kotlin.core.ui.R
import ke.droidcon.kotlin.chai.R as ChaiR

/**
 * Maps every [Screens] key to its screen, and tells `NavDisplay` which pane each one is.
 *
 * Nothing here branches on window size: a scene strategy reads the metadata and decides, which
 * is why the same entry is a full screen on a phone and a pane on a tablet.
 */
@Composable
fun droidconEntryProvider(
    navController: NavigationController,
    onActionClicked: () -> Unit,
): (NavKey) -> NavEntry<NavKey> {
    val entryProvider =
        entryProvider<NavKey> {
            entry<Screens.Home>(metadata = mainPaneMetadata()) {
                HomeRoute(
                    navigateToSpeakers = { navController.navigate(Screens.Speakers) },
                    navigateToSpeaker = { speakerName ->
                        navController.navigate(
                            Screens.SpeakerDetails(speakerName),
                        )
                    },
                    navigateToFeedbackScreen = { navController.navigate(Screens.FeedBack) },
                    navigateToSessionScreen = { navController.navigate(Screens.Sessions) },
                    onActionClicked = onActionClicked,
                    onSessionClicked = { sessionId ->
                        navController.navigate(Screens.SessionDetails(sessionId))
                    },
                )
            }
            entry<Screens.Sessions>(
                metadata =
                    listPaneMetadata(DroidconPaneScene.Sessions) {
                        DetailPanePlaceholder(
                            message = stringResource(R.string.select_a_session),
                            icon = painterResource(id = ChaiR.drawable.sessions_icon),
                        )
                    },
            ) {
                SessionsRoute(navigateToSessionDetails = { sessionId ->
                    navController.navigate(Screens.SessionDetails(sessionId))
                })
            }
            entry<Screens.SessionDetails>(
                metadata = detailPaneMetadata(DroidconPaneScene.Sessions),
            ) { key ->
                val viewModel = sessionModel(key)
                SessionDetailsRoute(
                    onNavigationIconClick = {
                        navController.goBack()
                    },
                    viewModel = viewModel,
                    // The scaffold's own answer, so the screen cannot disagree with the layout.
                    showTopBar = LocalListDetailSceneScope.current == null,
                )
            }
            entry<Screens.Feed>(metadata = mainPaneMetadata()) {
                FeedRoute(
                    navigateToFeedbackScreen = { navController.navigate(Screens.FeedBack) },
                )
            }
            entry<Screens.About>(metadata = mainPaneMetadata()) {
                AboutRoute(
                    navigateToFeedbackScreen = { navController.navigate(Screens.FeedBack) },
                )
            }
            entry<Screens.Speakers>(
                metadata =
                    listPaneMetadata(DroidconPaneScene.Speakers) {
                        DetailPanePlaceholder(
                            message = stringResource(R.string.select_a_speaker),
                            icon = rememberVectorPainter(Icons.Outlined.RecordVoiceOver),
                        )
                    },
            ) {
                SpeakersRoute(
                    navigateToHomeScreen = { navController.navigateUp() },
                    navigateToSpeaker = { speakerName ->
                        navController.navigate(
                            Screens.SpeakerDetails(speakerName),
                        )
                    },
                )
            }
            entry<Screens.FeedBack> {
                FeedBackRoute(
                    navigateBack = { navController.navigateUp() },
                )
            }
            entry<Screens.SpeakerDetails>(
                metadata = detailPaneMetadata(DroidconPaneScene.Speakers),
            ) { key ->
                val speakerName = key.speakerName
                SpeakerDetailsRoute(
                    name = speakerName,
                    navigateBack = { navController.navigateUp() },
                    navigateToSessionDetails = { sessionId ->
                        navController.navigate(Screens.SessionDetails(sessionId))
                    },
                    showTopBar = LocalListDetailSceneScope.current == null,
                )
            }
            entry<Screens.HappeningNow>(metadata = supportingPaneMetadata()) {
                HappeningNowRoute(
                    onSessionClick = { sessionId ->
                        navController.navigate(Screens.SessionDetails(sessionId))
                    },
                )
            }
        }
    return entryProvider
}

@Composable
private fun sessionModel(
    key: Screens.SessionDetails,
    viewModel: SessionDetailsViewModel =
        hiltViewModel<SessionDetailsViewModel, SessionDetailsViewModel.Factory>(
            key = key.sessionId,
            creationCallback = { factory ->
                factory.create(key)
            },
        ),
): SessionDetailsViewModel = viewModel