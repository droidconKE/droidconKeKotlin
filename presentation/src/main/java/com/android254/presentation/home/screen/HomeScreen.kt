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

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android254.presentation.common.components.DroidconAppBar
import com.android254.presentation.common.components.DroidconAppBarWithFeedbackButton
import com.android254.presentation.common.components.SponsorsCard
import com.android254.presentation.common.theme.DroidconKE2023Theme
import com.android254.presentation.home.components.HomeBannerSection
import com.android254.presentation.home.components.HomeSessionLoadingComponent
import com.android254.presentation.home.components.HomeSessionSection
import com.android254.presentation.home.components.HomeSpacer
import com.android254.presentation.home.components.HomeSpeakersLoadingComponent
import com.android254.presentation.home.components.HomeSpeakersSection
import com.android254.presentation.home.viewmodel.HomeViewModel
import com.android254.presentation.home.viewstate.HomeViewState
import com.android254.presentation.models.SessionPresentationModel
import com.droidconke.chai.atoms.MontserratSemiBold
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import ke.droidcon.kotlin.presentation.R

@Composable
fun HomeRoute(
    homeViewModel: HomeViewModel = hiltViewModel(),
    navigateToSpeakers: () -> Unit = {},
    navigateToSpeaker: (String) -> Unit = {},
    navigateToFeedbackScreen: () -> Unit = {},
    navigateToSessionScreen: () -> Unit = {},
    onActionClicked: () -> Unit = {},
    onSessionClicked: (SessionPresentationModel) -> Unit = {}
) {
    val homeViewState by homeViewModel.viewState.collectAsStateWithLifecycle()
    val isSyncing by homeViewModel.isSyncing.collectAsStateWithLifecycle()
    HomeScreen(
        viewState = homeViewState,
        isSyncing = isSyncing,
        navigateToSpeakers = navigateToSpeakers,
        navigateToSpeaker = navigateToSpeaker,
        navigateToFeedbackScreen = navigateToFeedbackScreen,
        navigateToSessionScreen = navigateToSessionScreen,
        onActionClicked = onActionClicked,
        onSessionClicked = onSessionClicked,
        onRefresh = { homeViewModel.startRefresh() }
    )
}

@Composable
private fun HomeScreen(
    viewState: HomeViewState,
    isSyncing: Boolean,
    navigateToSpeakers: () -> Unit = {},
    navigateToSpeaker: (String) -> Unit = {},
    navigateToFeedbackScreen: () -> Unit = {},
    navigateToSessionScreen: () -> Unit = {},
    onActionClicked: () -> Unit = {},
    onSessionClicked: (SessionPresentationModel) -> Unit = {},
    onRefresh: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            HomeToolbar(
                isSignedIn = viewState.isSignedIn,
                navigateToFeedbackScreen = navigateToFeedbackScreen,
                onActionClicked = onActionClicked
            )
        }
    ) { paddingValues ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing = isSyncing),
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HomeHeaderSection()
                HomeBannerSection(viewState)
                HomeSpacer()
                when {
                    isSyncing -> {
                        HomeSessionLoadingComponent()
                    }

                    else -> {
                        if (viewState.isSessionsSectionVisible) {
                            HomeSessionSection(
                                sessions = viewState.sessions,
                                onSessionClick = onSessionClicked,
                                onViewAllSessionClicked = navigateToSessionScreen
                            )
                            HomeSpacer()
                        }
                    }
                }
                when {
                    isSyncing -> {
                        HomeSpeakersLoadingComponent()
                    }

                    else -> {
                        if (viewState.isSpeakersSectionVisible) {
                            HomeSpeakersSection(
                                speakers = viewState.speakers,
                                navigateToSpeakers = navigateToSpeakers,
                                navigateToSpeaker = navigateToSpeaker
                            )
                            HomeSpacer()
                        }
                    }
                }
                SponsorsCard(sponsorsLogos = viewState.sponsors)
                HomeSpacer()
            }
        }
    }
}

@Composable
fun HomeHeaderSection() {
    Text(
        text = stringResource(id = R.string.home_header_welcome_label),
        modifier = Modifier.testTag("home_header"),
        fontFamily = MontserratSemiBold,
        fontSize = 16.sp
    )
}

@Composable
fun HomeToolbar(
    isSignedIn: Boolean,
    navigateToFeedbackScreen: () -> Unit = {},
    onActionClicked: () -> Unit = {}
) {
    if (isSignedIn) {
        DroidconAppBarWithFeedbackButton(
            onButtonClick = {
                navigateToFeedbackScreen()
            },
            userProfile = "https://media-exp1.licdn.com/dms/image/C4D03AQGn58utIO-x3w/profile-displayphoto-shrink_200_200/0/1637478114039?e=2147483647&v=beta&t=3kIon0YJQNHZojD3Dt5HVODJqHsKdf2YKP1SfWeROnI"
        )
    } else {
        DroidconAppBar(
            onActionClicked = onActionClicked
        )
    }
}

@Preview(
    name = "Light",
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun HomeScreenPreview() {
    DroidconKE2023Theme {
        HomeScreen(
            viewState = HomeViewState(
                isPosterVisible = true,
                isCallForSpeakersVisible = true,
                linkToCallForSpeakers = "https://droidconke.com",
                isSignedIn = false,
                speakers = listOf(),
                isSpeakersSectionVisible = true,
                isSessionsSectionVisible = true,
                sponsors = listOf(),
                organizedBy = listOf(),
                sessions = listOf()
            ),
            isSyncing = false
        )
    }
}