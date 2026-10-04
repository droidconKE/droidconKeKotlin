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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android254.presentation.common.components.MultiToggleButton
import com.android254.presentation.models.SessionsFilterOption
import com.droidconke.chai.components.CButton
import com.droidconke.chai.components.CPrimaryButtonText
import com.droidconke.chai.components.ChaiBodyLarge
import com.droidconke.chai.components.ChaiSubTitle
import com.droidconke.chai.components.ChaiTextButtonLight
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
                .fillMaxSize()
                .background(
                    color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.52f),
                ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(bottomEnd = 14.dp, bottomStart = 14.dp),
                    ).padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 36.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
            ) {
                Row {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_filter),
                        contentDescription = null,
                        modifier = Modifier.padding(end = 12.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    ChaiBodyLarge(
                        bodyText = stringResource(id = R.string.filter_button_label),
                        textColor = MaterialTheme.colorScheme.primary,
                    )
                }

                ChaiTextButtonLight(
                    modifier =
                        Modifier.clickable {
                            clearSelectedFilterList()
                            onDismiss()
                        },
                    bodyText = stringResource(id = R.string.cancel),
                    textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            groupedFilters.forEach { filter ->
                Column(
                    Modifier
                        .fillMaxWidth(),
                ) {
                    ChaiSubTitle(
                        titleText = stringResource(id = filter.key.resId),
                        titleColor = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    MultiToggleButton(
                        toggleStates = filter.value.toImmutableList(),
                        onClick = {
                            updateSelectedFilterOptionList(it)
                        },
                        currentSelections = currentSelections,
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            CButton(
                onClick = {
                    onDismiss()
                },
                isEnabled = true,
                shape = MaterialTheme.shapes.small,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp),
            ) {
                CPrimaryButtonText(
                    text = stringResource(R.string.filter_button_label).uppercase(),
                    textColor = MaterialTheme.colorScheme.onPrimary,
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
        Spacer(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clickable {
                        onDismiss()
                    },
        )
    }
}