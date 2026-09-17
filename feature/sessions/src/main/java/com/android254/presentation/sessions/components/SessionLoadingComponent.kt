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
package com.android254.presentation.sessions.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.android254.presentation.common.components.AnimatedShimmerEffect
import com.android254.presentation.common.components.LoadingBox
import com.android254.presentation.sessions.view.SessionScreenState
import com.droidconke.chai.ChaiTheme
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.ChaiSubTitle
import ke.droidcon.kotlin.core.ui.R
import kotlinx.collections.immutable.persistentListOf

@Composable
fun SessionLoadingComponent(
    modifier: Modifier = Modifier,
    sessionScreenState: SessionScreenState = SessionScreenState.ALL,
    isSessionLayoutList: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(bottom = 32.dp),
) {
    // Same columns and spacing as the loaded list, which is the point of a skeleton.
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = SessionColumnMinWidth),
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
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
            repeat(3) { index ->
                item(key = "loading_header_$index", span = { GridItemSpan(maxLineSpan) }) {
                    LoadingTimeHeader()
                }
                items(3) { SessionsLoadingCard() }
            }
        } else {
            items(4) { SessionsLoadingCardWithBannerImage() }
        }
    }
}

@Composable
fun LoadingTimeHeader() {
    AnimatedShimmerEffect(
        gradientColors =
            persistentListOf(
                MaterialTheme.chaiColorsPalette.loadingStateOnCardsColor.copy(alpha = 0.3f),
                MaterialTheme.chaiColorsPalette.loadingStateOnCardsColor.copy(alpha = 0.2f),
                MaterialTheme.chaiColorsPalette.loadingStateOnCardsColor.copy(alpha = 0.3f),
            ),
    ) { brush ->
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
        ) {
            LoadingBox(height = 18.dp, width = 80.dp, brush = brush)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@PreviewLightDark
@Composable
private fun SessionLoadingPreview() {
    ChaiTheme {
        Surface(
            color = MaterialTheme.chaiColorsPalette.background,
        ) {
            SessionLoadingComponent()
        }
    }
}