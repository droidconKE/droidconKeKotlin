/*
 * Copyright 2023 DroidconKE
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
package com.android254.presentation.feed.view

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android254.domain.models.ScheduleUpdate
import com.android254.presentation.common.adaptive.readablePaneWidth
import com.android254.presentation.common.components.DroidconAppBarWithFeedbackButton
import com.android254.presentation.common.components.EmptyStatePanel
import com.android254.presentation.common.insets.DroidconWindowInsets
import com.android254.presentation.common.insets.plus
import com.android254.presentation.common.share.shareTextIntent
import com.android254.presentation.feed.FeedViewModel
import com.android254.presentation.models.FeedUI
import com.droidconke.chai.ChaiTheme
import com.droidconke.chai.icons.ChaiIcons
import ke.droidcon.kotlin.core.ui.R
import kotlinx.collections.immutable.persistentListOf
import ke.droidcon.kotlin.chai.R as ChaiR

@Composable
fun FeedRoute(
    feedViewModel: FeedViewModel = hiltViewModel(),
    navigateToFeedbackScreen: () -> Unit = {},
    onSessionClick: (String) -> Unit = {},
    navigateToSessions: () -> Unit = {},
) {
    val feedUIState by feedViewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    FeedScreen(
        feedUIState = feedUIState,
        navigateToFeedbackScreen = navigateToFeedbackScreen,
        onShare = { feed -> context.startActivity(shareIntent(feed)) },
        onSessionClick = onSessionClick,
        navigateToSessions = navigateToSessions,
    )
}

private fun shareIntent(feed: FeedUI) =
    shareTextIntent(
        subject = feed.title,
        text = listOf(feed.title, feed.body, feed.url).filter(String::isNotBlank).joinToString("\n\n"),
    )

@Composable
internal fun FeedScreen(
    feedUIState: FeedUIState,
    navigateToFeedbackScreen: () -> Unit = {},
    onShare: (FeedUI) -> Unit = {},
    onSessionClick: (String) -> Unit = {},
    navigateToSessions: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            DroidconAppBarWithFeedbackButton(
                onButtonClick = {
                    navigateToFeedbackScreen()
                },
            )
        },
        contentWindowInsets = DroidconWindowInsets.screenContent,
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .consumeWindowInsets(paddingValues)
                    .fillMaxSize(),
        ) {
            when (feedUIState) {
                is FeedUIState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(paddingValues).padding(20.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        EmptyStatePanel(
                            icon = rememberVectorPainter(ChaiIcons.ErrorRounded),
                            message = feedUIState.message,
                            badgeColor = MaterialTheme.colorScheme.errorContainer,
                            iconColor = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }

                FeedUIState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                    ) {
                        repeat(3) {
                            FeedLoadingComponent()
                        }
                    }
                }

                is FeedUIState.Success -> {
                    LazyColumn(
                        modifier =
                            Modifier
                                .testTag("feeds_lazy_column")
                                .fillMaxSize()
                                .readablePaneWidth(),
                        contentPadding = paddingValues.plus(bottom = 16.dp),
                    ) {
                        item(key = "updates-title") {
                            Text(
                                text = stringResource(R.string.updates_title),
                                style = MaterialTheme.typography.headlineSmallEmphasized,
                                color = MaterialTheme.colorScheme.primary,
                                modifier =
                                    Modifier
                                        .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 4.dp)
                                        .semantics { heading() },
                            )
                        }
                        itemsIndexed(feedUIState.items, key = { _, item -> item.key }) { index, item ->
                            if (index > 0) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 20.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                )
                            }
                            when (item) {
                                is UpdateItem.Post ->
                                    FeedComponent(
                                        feed = item.feed,
                                        modifier = Modifier.fillMaxWidth(),
                                        onShare = { onShare(item.feed) },
                                    )
                                is UpdateItem.Schedule ->
                                    ScheduleUpdateRow(
                                        item = item,
                                        onSessionClick = onSessionClick,
                                        onBrowseSessions = navigateToSessions,
                                    )
                            }
                        }
                    }
                }

                FeedUIState.Empty -> FeedEmptyState(Modifier.padding(paddingValues))
            }
        }
    }
}

@Preview(
    name = "Light",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun FeedScreenPreview() {
    ChaiTheme {
        Surface(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            FeedScreen(
                feedUIState =
                    FeedUIState.Success(
                        items =
                            persistentListOf(
                                UpdateItem.Schedule(
                                    kind = ScheduleUpdate.Kind.Moved,
                                    sessionId = "1",
                                    title = "Compose Animations That Don't Jank",
                                    count = 0,
                                    whenLabel = "Today",
                                    sessionTime = "Thu, 5 Nov · 10:15 AM",
                                    venue = "Opal",
                                    isStarred = true,
                                    atMillis = 2,
                                ),
                                UpdateItem.Post(
                                    FeedUI(
                                        title = "Doors open at 8:00",
                                        body = "Registration is at the main entrance. Bring your ticket.",
                                        topic = "Venue",
                                        url = "",
                                        image = null,
                                        createdAt = "Yesterday",
                                    ),
                                    atMillis = 1,
                                ),
                            ),
                    ),
            )
        }
    }
}

@Composable
private fun FeedEmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
        EmptyStatePanel(
            icon = painterResource(id = ChaiR.drawable.feed_icon),
            message = stringResource(id = R.string.feed_empty),
        )
    }
}