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
package com.android254.presentation.home.summary

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.android254.presentation.common.components.AnimatedShimmerEffect
import com.android254.presentation.common.components.LoadingBox
import com.android254.presentation.common.components.SessionTag
import com.android254.presentation.utils.ChaiLightAndDarkComposePreviews
import com.droidconke.chai.ChaiTheme
import com.droidconke.chai.hero.AiCube
import com.droidconke.chai.icons.ChaiIcons
import ke.droidcon.kotlin.core.ui.R
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun StarredSummaryCard(
    state: StarredSummaryState,
    modifier: Modifier = Modifier,
) {
    if (state == StarredSummaryState.Hidden) return
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.animateContentSize().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AiCube(thinking = state == StarredSummaryState.Loading, modifier = Modifier.size(32.dp))
                Text(
                    text = stringResource(R.string.home_starred_summary_title).uppercase(),
                    style = MaterialTheme.typography.labelSmallEmphasized,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            AnimatedContent(
                targetState = state,
                transitionSpec = { fadeIn(tween(durationMillis = 300, delayMillis = 90)) togetherWith fadeOut(tween(90)) },
                label = "starred_summary",
            ) { shown ->
                if (shown == StarredSummaryState.Loading) SummaryShimmer() else SummaryBody(shown)
            }
        }
    }
}

@Composable
private fun SummaryBody(
    state: StarredSummaryState,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = state.bodyText(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (state is StarredSummaryState.Ready) {
            if (state.summary.themes.isNotEmpty()) SummaryThemes(state.summary)
            HorizontalDivider(modifier = Modifier.padding(top = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)
            SummarySourceLabel(state.source)
        }
    }
}

@Composable
private fun SummaryThemes(
    summary: StarredSummary,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        summary.themes.forEachIndexed { index, theme ->
            val shown = remember { MutableTransitionState(false).apply { targetState = true } }
            val delay = THEME_DELAY_MILLIS + index * THEME_STAGGER_MILLIS
            AnimatedVisibility(
                visibleState = shown,
                enter = fadeIn(tween(THEME_MILLIS, delayMillis = delay)) + scaleIn(tween(THEME_MILLIS, delayMillis = delay), initialScale = THEME_START_SCALE),
            ) {
                SessionTag(tagText = theme.uppercase())
            }
        }
    }
}

@Composable
private fun SummaryShimmer(modifier: Modifier = Modifier) {
    val loading = stringResource(R.string.home_starred_summary_loading)
    AnimatedShimmerEffect(
        gradientColors =
            persistentListOf(
                MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.3f),
                MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.2f),
                MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.3f),
            ),
    ) { brush ->
        Column(
            modifier = modifier.fillMaxWidth().padding(vertical = 4.dp).semantics { contentDescription = loading },
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            LoadingBox(height = 16.dp, widthRatio = 1f, brush = brush)
            LoadingBox(height = 16.dp, widthRatio = 0.85f, brush = brush)
            LoadingBox(height = 16.dp, widthRatio = 0.55f, brush = brush)
        }
    }
}

@Composable
private fun SummarySourceLabel(
    source: SummarySource,
    modifier: Modifier = Modifier,
) {
    val onDevice = source == SummarySource.OnDevice
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = if (onDevice) ChaiIcons.Smartphone else ChaiIcons.Cloud,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = stringResource(if (onDevice) R.string.home_starred_summary_source_on_device else R.string.home_starred_summary_source_cloud).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StarredSummaryState.bodyText(): String =
    when (this) {
        is StarredSummaryState.Ready -> summary.overview
        StarredSummaryState.Loading -> stringResource(R.string.home_starred_summary_loading)
        StarredSummaryState.NoStarred -> stringResource(R.string.home_starred_summary_empty)
        StarredSummaryState.Unavailable -> stringResource(R.string.home_starred_summary_unavailable)
        StarredSummaryState.Failed -> stringResource(R.string.home_starred_summary_failed)
        StarredSummaryState.Hidden -> ""
    }

private const val THEME_MILLIS = 250
private const val THEME_DELAY_MILLIS = 150
private const val THEME_STAGGER_MILLIS = 90
private const val THEME_START_SCALE = 0.8f

@ChaiLightAndDarkComposePreviews
@Composable
private fun StarredSummaryCardPreview() {
    ChaiTheme {
        Column {
            StarredSummaryCard(StarredSummaryState.Loading)
            StarredSummaryCard(StarredSummaryState.Unavailable)
            StarredSummaryCard(StarredSummaryState.Ready(StarredSummary("Compose and on-device AI.", listOf("Compose", "AI")), SummarySource.Cloud))
        }
    }
}