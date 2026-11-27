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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android254.presentation.common.components.SessionTag
import com.android254.presentation.utils.ChaiLightAndDarkComposePreviews
import com.droidconke.chai.ChaiTheme
import ke.droidcon.kotlin.core.ui.R

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
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.home_starred_summary_title).uppercase(),
                style = MaterialTheme.typography.labelSmallEmphasized,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = state.bodyText(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (state is StarredSummaryState.Ready) {
                if (state.summary.themes.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        state.summary.themes.forEach { theme -> SessionTag(tagText = theme.uppercase()) }
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(top = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)
                Text(
                    text =
                        stringResource(
                            if (state.source == SummarySource.OnDevice) {
                                R.string.home_starred_summary_source_on_device
                            } else {
                                R.string.home_starred_summary_source_cloud
                            },
                        ).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
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

@ChaiLightAndDarkComposePreviews
@Composable
private fun StarredSummaryCardPreview() {
    ChaiTheme {
        Column {
            StarredSummaryCard(StarredSummaryState.Unavailable)
            StarredSummaryCard(StarredSummaryState.Ready(StarredSummary("Compose and on-device AI.", listOf("Compose", "AI")), SummarySource.Cloud))
        }
    }
}