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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android254.presentation.common.components.SessionsCard
import com.android254.presentation.models.SessionPresentationModel
import com.android254.presentation.sessions.models.SessionsUiState
import com.droidconke.chai.atoms.ChaiBlue
import com.droidconke.chai.atoms.ChaiDarkGrey
import com.droidconke.chai.atoms.MontserratRegular
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshState
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import ke.droidcon.kotlin.presentation.R

@Composable
fun SessionsStateComponent(
    sessionsUiState: SessionsUiState,
    navigateToSessionDetails: (sessionId: String) -> Unit,
    refreshSessionsList: () -> Unit,
    retry: () -> Unit,
    isRefreshing: Boolean
) {
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)

    if (sessionsUiState.isLoading){
        SessionLoadingComponent()
    }

    if (sessionsUiState.isEmpty){
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier.size(70.dp),
                painter = painterResource(id = R.drawable.sessions_icon),
                contentDescription = stringResource(id = R.string.sessions_icon_description),
                tint = ChaiBlue
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = sessionsUiState.isEmptyMessage,
                style = TextStyle(
                    color = ChaiDarkGrey,
                    fontSize = 18.sp,
                    fontFamily = MontserratRegular,
                    textAlign = TextAlign.Center
                )
            )
        }
    }

    if (sessionsUiState.isError){
        SessionsErrorComponent(errorMessage = sessionsUiState.errorMessage, retry = retry)
    }

    if (!sessionsUiState.isEmpty){
        SessionListComponent(
            swipeRefreshState = swipeRefreshState,
            sessions = sessionsUiState.sessions,
            navigateToSessionDetails = navigateToSessionDetails,
            refreshSessionsList = refreshSessionsList
        )
    }
}

@Composable
fun SessionListComponent(
    swipeRefreshState: SwipeRefreshState,
    sessions: List<SessionPresentationModel>,
    navigateToSessionDetails: (sessionId: String) -> Unit,
    refreshSessionsList: () -> Unit
) {
    SwipeRefresh(state = swipeRefreshState, onRefresh = refreshSessionsList) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            itemsIndexed(
                items = sessions,
                key = { _, session -> session.remoteId }
            ) { index, session ->
                SessionsCard(
                    session = session,
                    navigateToSessionDetails = navigateToSessionDetails
                )
                if (index != sessions.lastIndex) {
                    Box(
                        Modifier.padding(
                            start = 40.dp,
                            end = 0.dp,
                            top = 10.dp,
                            bottom = 10.dp
                        )
                    ) {
                        Image(
                            painter = painterResource(id = if (index % 2 == 0) R.drawable.ic_green_session_card_spacer else R.drawable.ic_orange_session_card_spacer),
                            contentDescription = stringResource(R.string.spacer_icon_descript)
                        )
                    }
                }
            }
        }
    }
}