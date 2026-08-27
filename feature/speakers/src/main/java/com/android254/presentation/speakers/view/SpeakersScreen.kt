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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android254.presentation.models.SpeakerUI
import com.android254.presentation.speakers.SpeakersScreenUiState
import com.android254.presentation.speakers.SpeakersScreenViewModel
import com.droidconke.chai.ChaiTheme
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.ChaiBodyLargeBold
import com.droidconke.chai.components.ChaiBodyMedium
import com.droidconke.chai.components.ChaiBodyMediumBold
import com.droidconke.chai.components.ChaiPullToRefreshBox
import ke.droidcon.kotlin.core.ui.R
import kotlinx.collections.immutable.persistentListOf
import ke.droidcon.kotlin.chai.R as ChaiR

@Composable
fun SpeakersRoute(
    speakersScreenViewModel: SpeakersScreenViewModel = hiltViewModel(),
    navigateToHomeScreen: () -> Unit = {},
    navigateToSpeaker: (String) -> Unit = {},
) {
    val uiState by speakersScreenViewModel.speakersScreenUiState.collectAsStateWithLifecycle()
    val searchQuery by speakersScreenViewModel.searchQuery.collectAsStateWithLifecycle()
    SpeakersScreen(
        uiState = uiState,
        searchQuery = searchQuery,
        onSearchQueryChanged = speakersScreenViewModel::onSearchQueryChanged,
        navigateToHomeScreen = navigateToHomeScreen,
        navigateToSpeaker = navigateToSpeaker,
    )
}

@Composable
internal fun SpeakersScreen(
    uiState: SpeakersScreenUiState,
    searchQuery: String = "",
    onSearchQueryChanged: (String) -> Unit = {},
    navigateToHomeScreen: () -> Unit = {},
    navigateToSpeaker: (String) -> Unit = {},
) {
    var isSearchActive by rememberSaveable { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    if (isSearchActive) {
                        SpeakersSearchField(
                            query = searchQuery,
                            onQueryChanged = onSearchQueryChanged,
                            modifier = Modifier.focusRequester(focusRequester),
                        )
                    } else {
                        ChaiBodyLargeBold(
                            bodyText = stringResource(id = R.string.speakers_label),
                            textColor = MaterialTheme.chaiColorsPalette.textBoldColor,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (isSearchActive) {
                                isSearchActive = false
                                onSearchQueryChanged("")
                            } else {
                                navigateToHomeScreen()
                            }
                        },
                    ) {
                        Icon(
                            painter = painterResource(id = ChaiR.drawable.ic_back_arrow),
                            contentDescription =
                                stringResource(
                                    if (isSearchActive) {
                                        R.string.close_search_icon_description
                                    } else {
                                        R.string.back_arrow_icon_description
                                    },
                                ),
                            tint = MaterialTheme.chaiColorsPalette.textBoldColor,
                        )
                    }
                },
                actions = {
                    if (isSearchActive) {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChanged("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = stringResource(R.string.clear_search_icon_description),
                                    tint = MaterialTheme.chaiColorsPalette.textBoldColor,
                                )
                            }
                        }
                    } else {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = stringResource(R.string.search_speakers_icon_description),
                                tint = MaterialTheme.chaiColorsPalette.textBoldColor,
                            )
                        }
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.chaiColorsPalette.textBoldColor,
                        navigationIconContentColor = MaterialTheme.chaiColorsPalette.textBoldColor,
                    ),
            )
        },
        containerColor = MaterialTheme.chaiColorsPalette.background,
    ) { paddingValues ->
        ChaiPullToRefreshBox(
            isRefreshing = uiState is SpeakersScreenUiState.Loading,
            onRefresh = { /*TODO*/ },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(paddingValues),
        ) {
            when (uiState) {
                is SpeakersScreenUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }

                is SpeakersScreenUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        ChaiBodyMediumBold(
                            modifier = Modifier.align(Alignment.Center),
                            bodyText = uiState.message,
                            textColor = MaterialTheme.chaiColorsPalette.textNormalColor,
                        )
                    }
                }

                is SpeakersScreenUiState.Success -> {
                    if (uiState.speakers.isEmpty() && searchQuery.isNotBlank()) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            ChaiBodyMedium(
                                modifier =
                                    Modifier
                                        .align(Alignment.Center)
                                        .testTag("emptySearchResults"),
                                bodyText = stringResource(R.string.no_speakers_found_label),
                                textColor = MaterialTheme.chaiColorsPalette.textWeakColor,
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            items(items = uiState.speakers, key = { it.name }) { speaker ->
                                SpeakerComponent(
                                    speaker = speaker,
                                    onClick = {
                                        navigateToSpeaker.invoke(speaker.name)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpeakersSearchField(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = modifier.fillMaxWidth().testTag("searchField"),
        singleLine = true,
        placeholder = {
            ChaiBodyMedium(
                bodyText = stringResource(R.string.search_speakers_hint),
                textColor = MaterialTheme.chaiColorsPalette.textWeakColor,
            )
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.chaiColorsPalette.textBoldColor,
                focusedTextColor = MaterialTheme.chaiColorsPalette.textBoldColor,
                unfocusedTextColor = MaterialTheme.chaiColorsPalette.textBoldColor,
            ),
    )
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
private fun SpeakersScreenPreview() {
    ChaiTheme {
        SpeakersScreen(
            uiState =
                SpeakersScreenUiState.Success(
                    speakers =
                        persistentListOf(
                            SpeakerUI(id = 1, name = "Greg Fawson", tagline = "Lead Organizer", isSpeakingNow = true),
                            SpeakerUI(id = 2, name = "Omolara Adejuwon", tagline = "Android Engineer"),
                            SpeakerUI(id = 3, name = "Jane Doe", tagline = "Senior Android Dev", isSpeakingNow = true),
                            SpeakerUI(id = 4, name = "Alex Smith", tagline = "GDE Android", isSpeakingNow = true),
                            SpeakerUI(id = 5, name = "David Kim", tagline = "Performance Engineer"),
                        ),
                ),
        )
    }
}