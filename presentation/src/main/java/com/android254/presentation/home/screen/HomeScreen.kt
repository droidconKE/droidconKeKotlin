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
package com.android254.presentation.home.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android254.presentation.common.components.SponsorsCard
import com.android254.presentation.home.components.HomeBannerSection
import com.android254.presentation.home.components.HomeHeaderSectionComponent
import com.android254.presentation.home.components.HomeSessionLoadingComponent
import com.android254.presentation.home.components.HomeSessionSection
import com.android254.presentation.home.components.HomeSpacer
import com.android254.presentation.home.components.HomeSpeakersLoadingComponent
import com.android254.presentation.home.components.HomeSpeakersSection
import com.android254.presentation.home.components.HomeToolbarComponent
import com.android254.presentation.home.viewmodel.HomeViewModel
import com.android254.presentation.home.viewstate.HomeState
import com.android254.presentation.utils.ChaiLightAndDarkComposePreviews
import com.droidconke.chai.ChaiTheme
import com.droidconke.chai.chaiColorsPalette

import kotlinx.collections.immutable.toImmutableList

@Composable
fun HomeRoute(
    homeViewModel: HomeViewModel = hiltViewModel(),
    navigateToSpeakers: () -> Unit = {},
    navigateToSpeaker: (String) -> Unit = {},
    navigateToFeedbackScreen: () -> Unit = {},
    navigateToSessionScreen: () -> Unit = {},
    onActionClicked: () -> Unit = {},
    onSessionClicked: (sessionId: String) -> Unit = {},
) {
    val homeState by homeViewModel.viewState.collectAsStateWithLifecycle()
    val isSyncing by homeViewModel.isSyncing.collectAsStateWithLifecycle()
    HomeScreen(
        viewState = homeState,
        isSyncing = isSyncing,
        navigateToSpeakers = navigateToSpeakers,
        navigateToSpeaker = navigateToSpeaker,
        navigateToFeedbackScreen = navigateToFeedbackScreen,
        navigateToSessionScreen = navigateToSessionScreen,
        onActionClicked = onActionClicked,
        onSessionClicked = onSessionClicked,
        onRefresh = { homeViewModel.startRefresh() },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    viewState: HomeState,
    isSyncing: Boolean,
    navigateToSpeakers: () -> Unit = {},
    navigateToSpeaker: (String) -> Unit = {},
    navigateToFeedbackScreen: () -> Unit = {},
    navigateToSessionScreen: () -> Unit = {},
    onActionClicked: () -> Unit = {},
    onSessionClicked: (sessionId: String) -> Unit = {},
    onRefresh: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            HomeToolbarComponent(
                isSignedIn = false, // isSignedIn removed from HomeState
                navigateToFeedbackScreen = navigateToFeedbackScreen,
                onActionClicked = onActionClicked,
            )
        },
        containerColor = MaterialTheme.chaiColorsPalette.background,
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isSyncing,
            onRefresh = onRefresh,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                HomeHeaderSectionComponent()
                HomeBannerSection(viewState)
                HomeSpacer()

                AnimatedContent(
                    targetState = isSyncing || viewState.sessions.isEmpty(),
                    transitionSpec = {
                        fadeIn().togetherWith(fadeOut())
                    },
                    label = "sessions_section",
                ) { isLoading ->
                    if (isLoading) {
                        HomeSessionLoadingComponent()
                    } else {
                        HomeSessionSection(
                            sessions = viewState.sessions.toImmutableList(),
                            onSessionClick = onSessionClicked,
                            onViewAllSessionClicked = navigateToSessionScreen,
                        )
                    }
                }
                HomeSpacer()

                AnimatedContent(
                    targetState = isSyncing || viewState.speakers.isEmpty(),
                    transitionSpec = {
                        fadeIn().togetherWith(fadeOut())
                    },
                    label = "speakers_section",
                ) { isLoading ->
                    if (isLoading) {
                        HomeSpeakersLoadingComponent()
                    } else {
                        HomeSpeakersSection(
                            speakers = viewState.speakers.toImmutableList(),
                            navigateToSpeakers = navigateToSpeakers,
                            navigateToSpeaker = navigateToSpeaker,
                        )
                    }
                }
                HomeSpacer()

                AnimatedVisibility(
                    visible = viewState.sponsors.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    SponsorsCard(sponsors = viewState.sponsors.toImmutableList())
                }
                HomeSpacer()
            }
        }
    }
}

@ChaiLightAndDarkComposePreviews
@Composable
private fun HomeScreenPreview() {
    ChaiTheme {
        HomeScreen(
            viewState = HomeState(),
            isSyncing = false,
        )
    }
}
