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
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android254.presentation.common.adaptive.DroidconTooltip
import com.android254.presentation.common.adaptive.clickablePointer
import com.android254.presentation.common.adaptive.readablePaneWidth
import com.android254.presentation.common.insets.DroidconWindowInsets
import com.android254.presentation.common.insets.plus
import com.android254.presentation.models.SpeakerUI
import com.android254.presentation.speakers.SpeakersScreenUiState
import com.android254.presentation.speakers.SpeakersScreenViewModel
import com.android254.presentation.utils.FormFactorPreviews
import com.droidconke.chai.ChaiTheme
import com.droidconke.chai.components.ChaiBodyLargeBold
import com.droidconke.chai.components.ChaiBodyMedium
import com.droidconke.chai.components.ChaiBodyMediumBold
import com.droidconke.chai.components.ChaiPullToRefreshBox
import com.droidconke.chai.icons.ChaiIcons
import ke.droidcon.kotlin.core.ui.R
import kotlinx.collections.immutable.persistentListOf
import ke.droidcon.kotlin.chai.R as ChaiR

private val SpeakerColumnMinWidth = 320.dp

@Composable
fun SpeakersRoute(
    speakersScreenViewModel: SpeakersScreenViewModel = hiltViewModel(),
    navigateToHomeScreen: () -> Unit = {},
    navigateToSpeaker: (String) -> Unit = {},
    selectedSpeakerName: String? = null,
) {
    val uiState by speakersScreenViewModel.speakersScreenUiState.collectAsStateWithLifecycle()
    val isSyncing by speakersScreenViewModel.isSyncing.collectAsStateWithLifecycle()
    val searchQuery by speakersScreenViewModel.searchQuery.collectAsStateWithLifecycle()
    SpeakersScreen(
        uiState = uiState,
        isRefreshing = isSyncing,
        searchQuery = searchQuery,
        selectedSpeakerName = selectedSpeakerName,
        onSearchQueryChanged = speakersScreenViewModel::onSearchQueryChanged,
        onRefresh = speakersScreenViewModel::refresh,
        navigateToHomeScreen = navigateToHomeScreen,
        navigateToSpeaker = navigateToSpeaker,
    )
}

@Composable
internal fun SpeakersScreen(
    uiState: SpeakersScreenUiState,
    isRefreshing: Boolean = false,
    searchQuery: String = "",
    selectedSpeakerName: String? = null,
    onSearchQueryChanged: (String) -> Unit = {},
    onRefresh: () -> Unit = {},
    navigateToHomeScreen: () -> Unit = {},
    navigateToSpeaker: (String) -> Unit = {},
) {
    var isSearchActive by rememberSaveable { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) focusRequester.requestFocus()
    }

    fun closeSearch() {
        isSearchActive = false
        onSearchQueryChanged("")
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    BackHandler(enabled = isSearchActive) { closeSearch() }

    Scaffold(
        modifier =
            Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .onPreviewKeyEvent {
                    handleSpeakersKeyEvent(
                        event = it,
                        isSearchActive = isSearchActive,
                        onOpenSearch = { isSearchActive = true },
                        onCloseSearch = ::closeSearch,
                        onRefresh = onRefresh,
                    )
                },
        topBar = {
            SpeakersTopBar(
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                focusRequester = focusRequester,
                scrollBehavior = scrollBehavior,
                onSearchQueryChanged = onSearchQueryChanged,
                onOpenSearch = { isSearchActive = true },
                onCloseSearch = ::closeSearch,
                onNavigateBack = navigateToHomeScreen,
            )
        },
        contentWindowInsets = DroidconWindowInsets.screenContent,
    ) { paddingValues ->
        ChaiPullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            indicatorTopPadding = paddingValues.calculateTopPadding(),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .consumeWindowInsets(paddingValues),
        ) {
            when (uiState) {
                is SpeakersScreenUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                    ) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }

                is SpeakersScreenUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                    ) {
                        ChaiBodyMediumBold(
                            modifier = Modifier.align(Alignment.Center),
                            bodyText = uiState.message,
                            textColor = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                is SpeakersScreenUiState.Success -> {
                    if (uiState.speakers.isEmpty() && searchQuery.isNotBlank()) {
                        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                            ChaiBodyMedium(
                                modifier =
                                    Modifier
                                        .align(Alignment.Center)
                                        .testTag("emptySearchResults"),
                                bodyText = stringResource(R.string.no_speakers_found_label),
                                textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = SpeakerColumnMinWidth),
                            modifier = Modifier.fillMaxSize().readablePaneWidth().testTag("speakersGrid"),
                            contentPadding = paddingValues.plus(horizontal = 8.dp, top = 8.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            items(items = uiState.speakers, key = { it.name }, contentType = { "speaker" }) { speaker ->
                                SpeakerComponent(
                                    speaker = speaker,
                                    isSelected = speaker.name == selectedSpeakerName,
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
private fun SpeakersTopBar(
    isSearchActive: Boolean,
    searchQuery: String,
    focusRequester: FocusRequester,
    scrollBehavior: TopAppBarScrollBehavior,
    onSearchQueryChanged: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onCloseSearch: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    CenterAlignedTopAppBar(
        scrollBehavior = scrollBehavior,
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
                    textColor = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        navigationIcon = {
            val navLabel =
                stringResource(
                    if (isSearchActive) {
                        R.string.close_search_icon_description
                    } else {
                        R.string.back_arrow_icon_description
                    },
                )
            DroidconTooltip(text = navLabel) {
                IconButton(
                    onClick = { if (isSearchActive) onCloseSearch() else onNavigateBack() },
                    modifier = Modifier.clickablePointer(),
                ) {
                    Icon(
                        painter = painterResource(id = ChaiR.drawable.ic_back_arrow),
                        contentDescription = navLabel,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
        actions = {
            if (isSearchActive) {
                if (searchQuery.isNotEmpty()) {
                    val clearLabel = stringResource(R.string.clear_search_icon_description)
                    DroidconTooltip(text = clearLabel) {
                        IconButton(
                            onClick = { onSearchQueryChanged("") },
                            modifier = Modifier.clickablePointer(),
                        ) {
                            Icon(
                                imageVector = ChaiIcons.Clear,
                                contentDescription = clearLabel,
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            } else {
                DroidconTooltip(text = stringResource(R.string.search_speakers_tooltip)) {
                    IconButton(
                        onClick = onOpenSearch,
                        modifier = Modifier.clickablePointer(),
                    ) {
                        Icon(
                            imageVector = ChaiIcons.Search,
                            contentDescription = stringResource(R.string.search_speakers_icon_description),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            ),
    )
}

internal fun handleSpeakersKeyEvent(
    event: KeyEvent,
    isSearchActive: Boolean,
    onOpenSearch: () -> Unit,
    onCloseSearch: () -> Unit,
    onRefresh: () -> Unit,
): Boolean {
    if (event.type != KeyEventType.KeyDown || event.nativeKeyEvent.repeatCount > 0) return false
    return when {
        event.isCtrlPressed && event.key == Key.F -> {
            onOpenSearch()
            true
        }
        event.key == Key.Escape && isSearchActive -> {
            onCloseSearch()
            true
        }
        event.key == Key.F5 || (event.isCtrlPressed && event.key == Key.R) -> {
            onRefresh()
            true
        }
        else -> false
    }
}

@Composable
private fun SpeakersSearchField(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    TextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = modifier.fillMaxWidth().testTag("searchField"),
        singleLine = true,
        placeholder = {
            ChaiBodyMedium(
                bodyText = stringResource(R.string.search_speakers_hint),
                textColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.onSurface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            ),
    )
}

@FormFactorPreviews
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