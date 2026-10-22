/*
 * Copyright 2026 DroidconKE
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
package com.android254.presentation.common.livesessions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android254.presentation.activity.MainViewModel
import com.android254.presentation.common.fakedata.fakeSessions
import com.android254.presentation.common.insets.DroidconWindowInsets
import com.android254.presentation.common.insets.plus
import com.android254.presentation.models.SessionPresentationModel
import com.android254.presentation.sessions.components.CurrentSessionComponent
import com.android254.presentation.sessions.models.SessionUIState
import com.droidconke.chai.ChaiTheme
import com.droidconke.chai.components.ChaiBodyMedium
import com.droidconke.chai.components.ChaiSubTitle
import ke.droidcon.kotlin.core.ui.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/** Wide enough to still read as a card. */
val HappeningNowPaneWidth: Dp = 320.dp

internal const val LIVE_SESSIONS_RAIL_TEST_TAG = "live_sessions_rail"
internal const val HAPPENING_NOW_PANE_TEST_TAG = "happening_now_pane"

/** Merged once: both lists key on session id, and a duplicate key crashes. */
@Composable
fun rememberLiveSessions(state: SessionUIState): ImmutableList<SessionPresentationModel> =
    remember(state) {
        (state.current + state.upNext).distinctBy { it.id }.toImmutableList()
    }

/** Reads the view model itself: entry content is built once per back stack change. */
@Composable
fun HappeningNowRoute(
    onSessionClick: (String) -> Unit,
    viewModel: MainViewModel = hiltViewModel(),
) {
    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
    HappeningNowPane(
        sessions = rememberLiveSessions(sessionState),
        onSessionClick = onSessionClick,
    )
}

internal fun ImmutableList<SessionPresentationModel>.featured(): SessionPresentationModel? = firstOrNull { it.isStarred } ?: firstOrNull()

/** The bottom-most element, so it pays the bottom inset. */
@Composable
fun LiveSessionsRail(
    sessions: ImmutableList<SessionPresentationModel>,
    onSessionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val session = sessions.featured() ?: return
    CurrentSessionComponent(
        session = session,
        onClicked = onSessionClick,
        modifier =
            modifier
                .testTag(LIVE_SESSIONS_RAIL_TEST_TAG)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 8.dp),
    )
}

/** The same sessions stacked, for the pane. */
@Composable
fun HappeningNowPane(
    sessions: ImmutableList<SessionPresentationModel>,
    onSessionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.testTag(HAPPENING_NOW_PANE_TEST_TAG).fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding =
                DroidconWindowInsets.screenContent
                    .asPaddingValues()
                    .plus(horizontal = 20.dp, top = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "happening-now-title") {
                ChaiSubTitle(
                    titleText = stringResource(R.string.happening_now),
                    titleColor = MaterialTheme.colorScheme.primary,
                )
            }
            if (sessions.isEmpty()) {
                item(key = "happening-now-empty") {
                    ChaiBodyMedium(
                        bodyText = stringResource(R.string.happening_now_empty),
                        textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(sessions, key = { it.id }) { session ->
                CurrentSessionComponent(
                    modifier = Modifier.fillMaxWidth(),
                    session = session,
                    onClicked = onSessionClick,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun LiveSessionsRailPreview() {
    ChaiTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            LiveSessionsRail(
                sessions = fakeSessions.take(2).toImmutableList(),
                onSessionClick = {},
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun HappeningNowPaneEmptyPreview() {
    ChaiTheme {
        Column(modifier = Modifier.fillMaxWidth()) {
            HappeningNowPane(sessions = persistentListOf(), onSessionClick = {})
        }
    }
}