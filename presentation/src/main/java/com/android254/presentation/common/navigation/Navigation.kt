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
package com.android254.presentation.common.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.android254.presentation.about.view.AboutRoute
import com.android254.presentation.feed.view.FeedRoute
import com.android254.presentation.feedback.view.FeedBackRoute
import com.android254.presentation.home.screen.HomeRoute
import com.android254.presentation.sessionDetails.view.SessionDetailsRoute
import com.android254.presentation.sessions.view.SessionsRoute
import com.android254.presentation.speakers.view.SpeakerDetailsRoute
import com.android254.presentation.speakers.view.SpeakersRoute

@Composable
fun Navigation(
    navController: NavigationController,
    navigationState: NavigationState,
    updateBottomBarState: (Boolean) -> Unit,
    onActionClicked: () -> Unit = {},
) {
    val entryProvider = entryProvider<NavKey> {
        entry<Screens.Home> {
            updateBottomBarState(true)
            HomeRoute(
                navigateToSpeakers = { navController.navigate(Screens.Speakers) },
                navigateToSpeaker = { speakerName ->
                    navController.navigate(
                        Screens.SpeakerDetails(speakerName)
//                            .route.replace(
//                            "{speakerName}",
//                            speakerName,
//                        ),
                    )
                },
                navigateToFeedbackScreen = { navController.navigate(Screens.FeedBack) },
                navigateToSessionScreen = { navController.navigate(Screens.Sessions) },
                onActionClicked = onActionClicked,
                onSessionClicked = { sessionId ->

                    navController.navigate(
                        Screens.SessionDetails(sessionId)
//                            .route.replace(
//                            oldValue = "{sessionId}",
//                            newValue = sessionId,
//                        ),
                    )
//                    {
//                        launchSingleTop = true
//                        restoreState = true
//                    }
                },
            )
        }
        entry<Screens.Sessions> {
            updateBottomBarState(true)
            SessionsRoute(navigateToSessionDetails = { sessionId ->

                navController.navigate(Screens.SessionDetails(sessionId))
//                        .route.replace(
//                        oldValue = "{sessionId}",
//                        newValue = sessionId,
//                    ),
//                ) {
//                    launchSingleTop = true
//                    restoreState = true
//                }
            })
        }
        entry<Screens.SessionDetails> { key ->
            updateBottomBarState(false)
            SessionDetailsRoute(
                sessionId = key.sessionIdNavigationArgument,
                onNavigationIconClick = {
                    navController.goBack()
                },
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
//                            .route
//                            .replace(
//                            "{speakerName}",
//                            speakerName,
//                        ),
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

    NavDisplay(
        entries = navigationState.toEntries(entryProvider),
        onBack = { navController.goBack() },
    )
}