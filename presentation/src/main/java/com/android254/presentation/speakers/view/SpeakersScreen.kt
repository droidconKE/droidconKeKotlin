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
package com.android254.presentation.speakers.view

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android254.presentation.common.theme.DroidconKE2023Theme
import com.android254.presentation.speakers.SpeakersScreenUiState
import com.android254.presentation.speakers.SpeakersScreenViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import ke.droidcon.kotlin.presentation.R

@Composable
fun SpeakersRoute(
    speakersScreenViewModel: SpeakersScreenViewModel = hiltViewModel(),
    navigateToHomeScreen: () -> Unit = {},
    navigateToSpeaker: (Int) -> Unit = {}
) {
    val uiState by speakersScreenViewModel.speakersScreenUiState.collectAsStateWithLifecycle()
    SpeakersScreen(
        uiState = uiState,
        navigateToHomeScreen = navigateToHomeScreen,
        navigateToSpeaker = navigateToSpeaker
    )
}

@Composable
private fun SpeakersScreen(
    uiState: SpeakersScreenUiState,
    navigateToHomeScreen: () -> Unit = {},
    navigateToSpeaker: (Int) -> Unit = {}
) {
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.speakers_label),
                        fontSize = 24.sp,
                        fontFamily = FontFamily(Font(R.font.montserrat_regular)),
                        color = colorResource(id = R.color.dark)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = navigateToHomeScreen
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back_arrow),
                            contentDescription = stringResource(R.string.back_arrow_icon_description),
                            tint = colorResource(id = R.color.dark)
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = colorResource(id = R.color.dark),
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing = uiState is SpeakersScreenUiState.Loading),
            onRefresh = { /*TODO*/ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is SpeakersScreenUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }

                is SpeakersScreenUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = uiState.message
                        )
                    }
                }

                is SpeakersScreenUiState.Success -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(160.dp),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    ) {
                        items(items = uiState.speakers) { speaker ->
                            SpeakerComponent(
                                speaker = speaker,
                                onClick = {
                                    navigateToSpeaker.invoke(speaker.id)
                                }
                            )
                        }
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
fun SpeakersScreenPreview() {
    DroidconKE2023Theme {
        SpeakersScreen(
            uiState = SpeakersScreenUiState.Success(speakers = listOf())
        )
    }
}