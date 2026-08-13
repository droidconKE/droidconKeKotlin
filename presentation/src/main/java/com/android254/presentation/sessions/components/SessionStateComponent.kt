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
package com.android254.presentation.sessions.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.android254.presentation.common.components.SessionsCard
import com.android254.presentation.common.fakedata.fakeSessions
import com.android254.presentation.common.resultstatus.ResultStatus
import com.android254.presentation.common.resultstatus.emptyMessage
import com.android254.presentation.common.resultstatus.errorMessage
import com.android254.presentation.common.stepper.verticalSteps
import com.android254.presentation.models.SessionPresentationModel
import com.android254.presentation.models.SessionStatus
import com.android254.presentation.sessions.models.SessionsIntentHandler
import com.android254.presentation.sessions.models.SessionsUiState
import com.android254.presentation.sessions.view.SessionScreenState
import com.droidconke.chai.ChaiTheme
import com.droidconke.chai.atoms.ChaiBlue
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.colors.venueAccentColor
import com.droidconke.chai.components.ChaiBodyMediumBold
import com.droidconke.chai.components.ChaiPullToRefreshBox
import com.droidconke.chai.components.ChaiSubTitle
import ke.droidcon.kotlin.presentation.R

@Composable
fun SessionsStateComponent(
    sessionsUiState: SessionsUiState,
    navigateToSessionDetails: (sessionId: String) -> Unit,
    isRefreshing: Boolean,
    sessionScreenState: SessionScreenState,
    isSessionLayoutList: Boolean,
    onEvent: (SessionsIntentHandler) -> Unit,
) {
    // Hoisted above AnimatedContent so an in-flight pull survives a sessionStatus change.
    val pullToRefreshState = rememberPullToRefreshState()

    AnimatedContent(sessionsUiState.sessionStatus) { status ->
        when (status) {
            is ResultStatus.Empty -> {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        modifier = Modifier.size(70.dp),
                        painter = painterResource(id = R.drawable.sessions_icon),
                        contentDescription = stringResource(id = R.string.sessions_icon_description),
                        tint = ChaiBlue,
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    ChaiBodyMediumBold(
                        bodyText = sessionsUiState.sessionStatus.emptyMessage,
                        textColor = MaterialTheme.chaiColorsPalette.textNormalColor,
                    )
                }
            }

            is ResultStatus.Error -> {
                SessionsErrorComponent(
                    errorMessage = sessionsUiState.sessionStatus.errorMessage,
                    retry = {
                        onEvent(SessionsIntentHandler.Retry)
                    },
                )
            }

            ResultStatus.Loading -> {
                SessionLoadingComponent()
            }

            ResultStatus.Success -> {
                SessionListComponent(
                    isRefreshing = isRefreshing,
                    pullToRefreshState = pullToRefreshState,
                    sessions = sessionsUiState.sessions,
                    navigateToSessionDetails = navigateToSessionDetails,
                    sessionScreenState = sessionScreenState,
                    isSessionLayoutList = isSessionLayoutList,
                    onEvent = onEvent,
                )
            }
        }
    }
}

@Composable
fun SessionListComponent(
    isRefreshing: Boolean,
    pullToRefreshState: PullToRefreshState,
    sessions: List<SessionPresentationModel>,
    sessionScreenState: SessionScreenState,
    isSessionLayoutList: Boolean,
    navigateToSessionDetails: (sessionId: String) -> Unit,
    onEvent: (SessionsIntentHandler) -> Unit,
) {
    val listState = rememberLazyListState()

    // Built here because LazyListScope is not a composable context.
    val verticalStepItems =
        sessions.map { session -> session.verticalStep(venueAccentColor(session.venue)) }

    LaunchedEffect(sessions) {
        val index = sessions.indexOfFirst { it.sessionStatus == SessionStatus.Ongoing }
        if (index != -1) {
            listState.animateScrollToItem(index + 1) // +1 for the header item
        }
    }

    ChaiPullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { onEvent(SessionsIntentHandler.RefreshSessions) },
        state = pullToRefreshState,
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            item {
                Spacer(modifier = Modifier.height(20.dp))

                ChaiSubTitle(
                    titleText =
                        when (sessionScreenState) {
                            SessionScreenState.ALL -> stringResource(R.string.all_sessions)
                            SessionScreenState.MYSESSIONS -> stringResource(R.string.my_sessions)
                        },
                    titleColor = MaterialTheme.chaiColorsPalette.textTitlePrimaryColor,
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
            if (isSessionLayoutList) {
                verticalSteps(
                    spacing = 16.dp,
                    items = verticalStepItems,
                ) { session ->
                    SessionsCard(
                        session = session,
                        navigateToSessionDetails = navigateToSessionDetails,
                        onBookmark = {
                            onEvent(SessionsIntentHandler.BookmarkSession(it))
                        },
                    )
                }
            } else {
                itemsIndexed(
                    items = sessions,
                    key = { _, session -> session.id },
                ) { _, session ->
                    SessionsCardWithBannerImage(
                        session = session,
                        navigateToSessionDetails = navigateToSessionDetails,
                    )

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
fun SessionListPreview() {
    ChaiTheme {
        Surface(
            color = MaterialTheme.chaiColorsPalette.background,
        ) {
            SessionListComponent(
                isRefreshing = false,
                pullToRefreshState = rememberPullToRefreshState(),
                sessions = fakeSessions,
                navigateToSessionDetails = {},
                sessionScreenState = SessionScreenState.ALL,
                isSessionLayoutList = true,
                onEvent = {},
            )
        }
    }
}