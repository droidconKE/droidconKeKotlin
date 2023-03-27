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

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.android254.droidconKE2023.presentation.R
import com.android254.presentation.common.components.SessionsCard
import com.android254.presentation.common.components.SessionsLoadingSkeleton
import com.android254.presentation.common.navigation.Screens
import com.android254.presentation.sessions.view.SessionsViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SessionList(
    viewModel: SessionsViewModel,
    navController: NavHostController
) {
    val sessions = viewModel.sessions.collectAsStateWithLifecycle()
    val loading = viewModel.loading.collectAsStateWithLifecycle(initialValue = false)
    val empty = viewModel.loading.collectAsStateWithLifecycle(initialValue = false)
    val swipeRefreshState = rememberSwipeRefreshState(false)

    if (loading.value) {
        SessionsLoadingSkeleton()
    } else {
        SwipeRefresh(state = swipeRefreshState, onRefresh = {
            viewModel.refreshSessionList()
        }) {
            LazyColumn(modifier = Modifier.fillMaxHeight()) {
                sessions.value?.let {
                    itemsIndexed(
                        items = it,
                        key = { _, session -> session.remoteId }
                    ) { index, session ->
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                            SessionsCard(session = session, onclick = {
                                navController.navigate(
                                    Screens.SessionDetails.route.replace(
                                        oldValue = "{sessionId}",
                                        newValue = session.id
                                    )
                                ) {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            })
                            if (index != sessions.value?.size?.minus(1)) {
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
                                        contentDescription = "spacer icon"
                                    )
                                }
                            }
                            if (index == sessions.value?.size?.minus(1)) {
                                Spacer(modifier = Modifier.height(32.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}