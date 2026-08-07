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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import com.android254.presentation.models.SessionPresentationModel
import com.android254.presentation.models.SessionStatus
import com.android254.presentation.sessions.models.SessionsIntentHandler
import com.android254.presentation.sessions.models.SessionsUiState
import com.android254.presentation.sessions.view.SessionScreenState
import com.droidconke.chai.ChaiTheme
import com.droidconke.chai.atoms.ChaiBlue
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.ChaiBodyLargeBold
import com.droidconke.chai.components.ChaiBodyMediumBold
import com.droidconke.chai.components.ChaiPullToRefreshBox
import com.droidconke.chai.components.ChaiSubTitle
import ke.droidcon.kotlin.presentation.R
import kotlinx.collections.immutable.ImmutableList
import ke.droidcon.kotlin.chai.R as ChaiR

@Composable
fun SessionsStateComponent(
    sessionsUiState: SessionsUiState,
    navigateToSessionDetails: (sessionId: String) -> Unit,
    isRefreshing: Boolean,
    sessionScreenState: SessionScreenState,
    isSessionLayoutList: Boolean,
    onEvent: (SessionsIntentHandler) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Hoisted above AnimatedContent so an in-flight pull survives a sessionStatus change.
    val pullToRefreshState = rememberPullToRefreshState()

    AnimatedContent(sessionsUiState.sessionStatus, label = "session_status") { status ->
        when (status) {
            is ResultStatus.Empty -> {
                Column(
                    modifier =
                        modifier
                            .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        modifier = Modifier.size(70.dp),
                        painter = painterResource(id = ChaiR.drawable.sessions_icon),
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
                SessionLoadingComponent(
                    sessionScreenState = sessionScreenState,
                    isSessionLayoutList = isSessionLayoutList,
                )
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
    sessions: ImmutableList<SessionPresentationModel>,
    sessionScreenState: SessionScreenState,
    isSessionLayoutList: Boolean,
    navigateToSessionDetails: (sessionId: String) -> Unit,
    onEvent: (SessionsIntentHandler) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(sessions) {
        val index = sessions.indexOfFirst { it.sessionStatus == SessionStatus.Ongoing }
        if (index != -1) {
            listState.animateScrollToItem(index + 1) // +1 for the header item
        }
    }

    val groupedSessions = remember(sessions) {
        sessions.groupBy { "${it.startTime} ${it.amOrPm}" }
    }

    ChaiPullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { onEvent(SessionsIntentHandler.RefreshSessions) },
        modifier = modifier,
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
                groupedSessions.forEach { (time, sessions) ->
                    item(key = "header_$time") {
                        TimeHeader(time = time)
                    }
                    items(
                        items = sessions,
                        key = { it.id },
                    ) { session ->
                        SessionsCard(
                            session = session,
                            navigateToSessionDetails = navigateToSessionDetails,
                            onBookmark = {
                                onEvent(SessionsIntentHandler.BookmarkSession(it))
                            },
                        )
                        Spacer(Modifier.height(16.dp))
                    }
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

@Composable
fun TimeHeader(time: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
    ) {
        ChaiBodyLargeBold(
            bodyText = time,
            textColor = MaterialTheme.chaiColorsPalette.textBoldColor,
        )
        Spacer(modifier = Modifier.width(12.dp))
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = MaterialTheme.chaiColorsPalette.cardsBorderColor,
        )
    }
}

@PreviewLightDark
@Composable
private fun SessionListPreview() {
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
