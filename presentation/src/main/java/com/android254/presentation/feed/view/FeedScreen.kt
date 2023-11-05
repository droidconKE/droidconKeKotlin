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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android254.presentation.common.components.DroidconAppBarWithFeedbackButton
import com.android254.presentation.feed.FeedViewModel
import com.android254.presentation.models.FeedUI
import com.droidconke.chai.ChaiDCKE22Theme
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.ChaiBodyMediumBold
import ke.droidcon.kotlin.presentation.R
import kotlinx.coroutines.launch

@Composable
fun FeedRoute(
    feedViewModel: FeedViewModel = hiltViewModel(),
    navigateToFeedbackScreen: () -> Unit = {}
) {
    val feedUIState by feedViewModel.uiState.collectAsStateWithLifecycle()

    FeedScreen(
        feedUIState = feedUIState,
        navigateToFeedbackScreen = navigateToFeedbackScreen
    )
}

@Composable
private fun FeedScreen(
    feedUIState: FeedUIState,
    navigateToFeedbackScreen: () -> Unit = {}
) {
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()

    if (bottomSheetState.isVisible) {
        ModalBottomSheet(
            containerColor = MaterialTheme.chaiColorsPalette.bottomSheetBackgroundColor,
            sheetState = bottomSheetState,
            dragHandle = {},
            onDismissRequest = { scope.launch { bottomSheetState.hide() } }
        ) {
            FeedShareSection(
                onCancelClicked = { scope.launch { bottomSheetState.hide() } }
            )
        }
    }
    Scaffold(
        topBar = {
            DroidconAppBarWithFeedbackButton(
                onButtonClick = {
                    navigateToFeedbackScreen()
                },
                userProfile = "https://media-exp1.licdn.com/dms/image/C4D03AQGn58utIO-x3w/profile-displayphoto-shrink_200_200/0/1637478114039?e=2147483647&v=beta&t=3kIon0YJQNHZojD3Dt5HVODJqHsKdf2YKP1SfWeROnI"
            )
        },
        containerColor = MaterialTheme.chaiColorsPalette.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues = paddingValues)
                .fillMaxSize()
        ) {
            when (feedUIState) {
                is FeedUIState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            modifier = Modifier
                                .width(50.dp)
                                .height(50.dp),
                            imageVector = Icons.Rounded.Error,
                            contentDescription = "Error",
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.error)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        ChaiBodyMediumBold(
                            bodyText = feedUIState.message,
                            textColor = MaterialTheme.chaiColorsPalette.textNormalColor
                        )
                    }
                }

                FeedUIState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        repeat(3) {
                            FeedLoadingComponent()
                        }
                    }
                }

                is FeedUIState.Success -> {
                    LazyColumn(
                        modifier = Modifier.testTag("feeds_lazy_column")
                    ) {
                        items(feedUIState.feeds) { feedPresentationModel ->
                            FeedComponent(
                                modifier = Modifier.fillMaxWidth(),
                                feedPresentationModel
                            ) {
                                scope.launch {
                                    bottomSheetState.show()
                                }
                            }
                        }
                    }
                }

                FeedUIState.Empty -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            modifier = Modifier.size(70.dp),
                            painter = painterResource(id = R.drawable.feed_icon),
                            contentDescription = stringResource(id = R.string.feed_icon_description),
                            tint = MaterialTheme.chaiColorsPalette.secondaryButtonColor
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        ChaiBodyMediumBold(
                            bodyText = "No items",
                            textColor = MaterialTheme.chaiColorsPalette.textNormalColor
                        )
                    }
                }
            }
        }
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
fun FeedScreenPreview() {
    ChaiDCKE22Theme {
        Surface(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            FeedScreen(
                feedUIState = FeedUIState.Success(
                    feeds = listOf(
                        FeedUI(
                            title = "Feed item 1",
                            body = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec euismod, nisl eget aliquam ultricies, nisl nisl aliquet nisl, eget aliquam nisl nisl eget nisl. Donec euismod, nisl eget aliquam ultricies, nisl nisl aliquet nisl, eget aliquam nisl nisl eget nisl.",
                            topic = "Lorem ipsum",
                            url = "",
                            image = "",
                            createdAt = "2021-10-10"
                        ),
                        FeedUI(
                            title = "Feed item 2",
                            body = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec euismod, nisl eget aliquam ultricies, nisl nisl aliquet nisl, eget aliquam nisl nisl eget nisl. Donec euismod, nisl eget aliquam ultricies, nisl nisl aliquet nisl, eget aliquam nisl nisl eget nisl.",
                            topic = "Lorem ipsum",
                            url = "",
                            image = "",
                            createdAt = "2021-10-10"
                        )
                    )
                )
            )
        }
    }
}