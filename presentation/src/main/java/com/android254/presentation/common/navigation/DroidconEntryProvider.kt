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

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.android254.presentation.about.view.AboutRoute
import com.android254.presentation.feed.view.FeedRoute
import com.android254.presentation.feedback.view.FeedBackRoute
import com.android254.presentation.home.screen.HomeRoute
import com.android254.presentation.sessionDetails.SessionDetailsViewModel
import com.android254.presentation.sessionDetails.view.SessionDetailsRoute
import com.android254.presentation.sessions.view.SessionsRoute
import com.android254.presentation.speakers.view.SpeakerDetailsRoute
import com.android254.presentation.speakers.view.SpeakersRoute


@Composable
fun droidconEntryProvider(
    updateBottomBarState: (Boolean) -> Unit,
    navController: NavigationController,
    onActionClicked: () -> Unit,
): (NavKey) -> NavEntry<NavKey> {
    val entryProvider = entryProvider<NavKey> {
        entry<Screens.Home> {
            updateBottomBarState(true)
            HomeRoute(
                navigateToSpeakers = { navController.navigate(Screens.Speakers) },
                navigateToSpeaker = { speakerName ->
                    navController.navigate(
                        Screens.SpeakerDetails(speakerName)
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
        entry<Screens.Sessions> {
            updateBottomBarState(true)
            SessionsRoute(navigateToSessionDetails = { sessionId ->
                navController.navigate(Screens.SessionDetails(sessionId))
            })
        }
        entry<Screens.SessionDetails> { key ->
            updateBottomBarState(false)
            val viewModel = sessionModel(key)
            SessionDetailsRoute(
                sessionId = key.sessionId, onNavigationIconClick = {
                    navController.goBack()
                }, viewModel = viewModel
            )
        }
        entry<Screens.Feed> {
            updateBottomBarState(true)
            FeedRoute(
                navigateToFeedbackScreen = { navController.navigate(Screens.FeedBack) },
            )
        }
        entry<Screens.About> {
            updateBottomBarState(true)
            AboutRoute(
                navigateToFeedbackScreen = { navController.navigate(Screens.FeedBack) },
            )
        }
        entry<Screens.Speakers> {
            updateBottomBarState(true)
            SpeakersRoute(
                navigateToHomeScreen = { navController.navigateUp() },
                navigateToSpeaker = { speakerName ->
                    navController.navigate(
                        Screens.SpeakerDetails(speakerName)
                    )
                },
            )
        }
        entry<Screens.FeedBack> {
            updateBottomBarState(false)
            FeedBackRoute(
                navigateBack = { navController.navigateUp() },
            )
        }
        entry<Screens.SpeakerDetails> { key ->
            val speakerName = key.speakerName
            updateBottomBarState(false)
            SpeakerDetailsRoute(
                name = speakerName,
                navigateBack = { navController.navigateUp() },
            )
        }
    }
    return entryProvider
}

@Composable
private fun sessionModel(key: Screens.SessionDetails): SessionDetailsViewModel {
    val viewModel = hiltViewModel<SessionDetailsViewModel, SessionDetailsViewModel.Factory>(
        creationCallback = { factory ->
            factory.create(key)
        })
    return viewModel
}