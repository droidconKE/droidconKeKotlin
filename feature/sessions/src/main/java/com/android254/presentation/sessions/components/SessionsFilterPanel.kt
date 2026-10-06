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
package com.android254.presentation.sessions.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.android254.presentation.common.components.FilterOptionChips
import com.android254.presentation.models.SessionsFilterOption
import ke.droidcon.kotlin.core.ui.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun SessionsFilterPanel(
    onDismiss: () -> Unit,
    selectableFilters: ImmutableList<SessionsFilterOption>,
    currentSelections: ImmutableList<SessionsFilterOption>,
    updateSelectedFilterOptionList: (SessionsFilterOption) -> Unit,
    clearSelectedFilterList: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val groupedFilters =
        remember(selectableFilters) {
            selectableFilters.groupBy { it.type }
        }
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(id = R.string.filter_sheet_title),
                style = MaterialTheme.typography.headlineSmallEmphasized,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() },
            )
            TextButton(onClick = clearSelectedFilterList, enabled = currentSelections.isNotEmpty()) {
                Text(text = stringResource(id = R.string.filter_clear))
            }
        }

        groupedFilters.forEach { filter ->
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(id = filter.key.resId),
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                FilterOptionChips(
                    options = filter.value.toImmutableList(),
                    onClick = updateSelectedFilterOptionList,
                    currentSelections = currentSelections,
                )
            }
        }

        Button(
            onClick = onDismiss,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
        ) {
            Text(text = stringResource(R.string.filter_show_sessions), style = MaterialTheme.typography.labelLarge)
        }
    }
}