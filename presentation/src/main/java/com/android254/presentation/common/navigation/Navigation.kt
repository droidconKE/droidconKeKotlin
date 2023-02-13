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

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.android254.presentation.about.view.AboutScreen
import com.android254.presentation.feed.view.FeedScreen
import com.android254.presentation.feedback.view.FeedBackScreen
import com.android254.presentation.home.screen.HomeScreen
import com.android254.presentation.sessionDetails.view.SessionDetailsScreen
import com.android254.presentation.sessions.view.SessionsScreen
import com.android254.presentation.speakers.view.SpeakerDetailsScreen
import com.android254.presentation.speakers.view.SpeakersScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Navigation(
    navController: NavHostController,
    updateBottomBarState: (Boolean) -> Unit,
    onActionClicked: () -> Unit = {}
) {
    NavHost(navController, startDestination = Screens.Home.route) {
        composable(Screens.Home.route) {
            updateBottomBarState(true)
            HomeScreen(
                navigateToSpeakers = { navController.navigate(Screens.Speakers.route) },
                navigateToSpeaker = { twitterHandle ->
                    navController.navigate(
                        Screens.SpeakerDetails.route.replace("{twitterHandle}", twitterHandle)
                    )
                },
                navigateToFeedbackScreen = { navController.navigate(Screens.FeedBack.route) },
                navigateToSessionScreen = { navController.navigate(Screens.Sessions.route) },
                onActionClicked = onActionClicked,
                onSessionClicked = {}
            )
        }
        composable(Screens.Sessions.route) {
            updateBottomBarState(true)
            SessionsScreen(navController = navController)
        }
        composable(
            Screens.SessionDetails.route,
            arguments = listOf(navArgument(Screens.SessionDetails.sessionIdNavigationArgument) { type = NavType.StringType })
        ) { backStackEntry ->
            updateBottomBarState(false)
            SessionDetailsScreen(
                sessionId = requireNotNull(backStackEntry.arguments?.getString(Screens.SessionDetails.sessionIdNavigationArgument)),
                onNavigationIconClick = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screens.Feed.route) {
            updateBottomBarState(true)
            FeedScreen(
                navigateToFeedbackScreen = { navController.navigate(Screens.FeedBack.route) }
            )
        }
        composable(Screens.About.route) {
            updateBottomBarState(true)
            AboutScreen(
                navigateToFeedbackScreen = { navController.navigate(Screens.FeedBack.route) }
            )
        }
        composable(Screens.Speakers.route) {
            updateBottomBarState(true)
            SpeakersScreen(
                navigateToHomeScreen = { navController.navigateUp() },
                navigateToSpeaker = { speakerId ->
                    navController.navigate(Screens.SpeakerDetails.route.replace("{speakerId}", "$speakerId"))
                }
            )
        }
        composable(Screens.FeedBack.route) {
            updateBottomBarState(false)
            FeedBackScreen(
                navigateBack = { navController.navigateUp() }
            )
        }

        composable(
            Screens.SpeakerDetails.route,
            arguments = listOf(navArgument("speakerId") { type = NavType.IntType })
        ) {
            val speakerId = it.arguments?.getInt("speakerId")
                ?: throw IllegalStateException("Speaker data missing.")
            updateBottomBarState(false)
            SpeakerDetailsScreen(
                id = speakerId,
                navigateBack = { navController.navigateUp() }
            )
        }
    }
}