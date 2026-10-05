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

import android.content.Intent
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android254.presentation.common.adaptive.readablePaneWidth
import com.android254.presentation.common.components.DroidconAppBarWithFeedbackButton
import com.android254.presentation.common.components.EmptyStatePanel
import com.android254.presentation.common.insets.DroidconWindowInsets
import com.android254.presentation.common.insets.plus
import com.android254.presentation.feed.FeedViewModel
import com.android254.presentation.models.FeedUI
import com.droidconke.chai.ChaiTheme
import ke.droidcon.kotlin.core.ui.R
import ke.droidcon.kotlin.chai.R as ChaiR

@Composable
fun FeedRoute(
    feedViewModel: FeedViewModel = hiltViewModel(),
    navigateToFeedbackScreen: () -> Unit = {},
) {
    val feedUIState by feedViewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    FeedScreen(
        feedUIState = feedUIState,
        navigateToFeedbackScreen = navigateToFeedbackScreen,
        onShare = { feed -> context.startActivity(shareIntent(feed)) },
    )
}

private fun shareIntent(feed: FeedUI): Intent {
    val text = listOf(feed.title, feed.body, feed.url).filter(String::isNotBlank).joinToString("\n\n")
    val send =
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, feed.title)
            putExtra(Intent.EXTRA_TEXT, text)
        }
    return Intent.createChooser(send, null)
}

@Composable
internal fun FeedScreen(
    feedUIState: FeedUIState,
    navigateToFeedbackScreen: () -> Unit = {},
    onShare: (FeedUI) -> Unit = {},
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
                            icon = rememberVectorPainter(Icons.Rounded.Error),
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
                    if (feedUIState.feeds.isEmpty()) {
                        FeedEmptyState(Modifier.padding(paddingValues))
                    } else {
                        LazyColumn(
                            modifier =
                                Modifier
                                    .testTag("feeds_lazy_column")
                                    .fillMaxSize()
                                    .readablePaneWidth(),
                            contentPadding = paddingValues.plus(bottom = 16.dp),
                        ) {
                            itemsIndexed(feedUIState.feeds, key = { _, feed -> feed.title }) { index, feedPresentationModel ->
                                if (index > 0) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 20.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                    )
                                }
                                FeedComponent(
                                    feed = feedPresentationModel,
                                    modifier = Modifier.fillMaxWidth(),
                                    onShare = { onShare(feedPresentationModel) },
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
                        feeds =
                            listOf(
                                FeedUI(
                                    title = "Feed item 1",
                                    body = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec euismod, nisl eget aliquam ultricies, nisl nisl aliquet nisl, eget aliquam nisl nisl eget nisl. Donec euismod, nisl eget aliquam ultricies, nisl nisl aliquet nisl, eget aliquam nisl nisl eget nisl.",
                                    topic = "Lorem ipsum",
                                    url = "",
                                    image = "",
                                    createdAt = "2021-10-10",
                                ),
                                FeedUI(
                                    title = "Feed item 2",
                                    body = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec euismod, nisl eget aliquam ultricies, nisl nisl aliquet nisl, eget aliquam nisl nisl eget nisl. Donec euismod, nisl eget aliquam ultricies, nisl nisl aliquet nisl, eget aliquam nisl nisl eget nisl.",
                                    topic = "Lorem ipsum",
                                    url = "",
                                    image = "",
                                    createdAt = "2021-10-10",
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