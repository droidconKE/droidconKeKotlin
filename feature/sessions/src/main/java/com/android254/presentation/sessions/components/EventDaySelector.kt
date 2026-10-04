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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android254.presentation.common.components.AnimatedShimmerEffect
import com.android254.presentation.common.components.ConnectedToggleGroup
import com.android254.presentation.common.components.LoadingBox
import com.android254.presentation.models.EventDate
import ke.droidcon.kotlin.core.ui.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

fun ordinal(i: Int): String {
    val suffixes = arrayOf("th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th")
    return when (i % 100) {
        11, 12, 13 -> i.toString() + "th"
        else -> i.toString() + suffixes[i % 10]
    }
}

@Composable
fun EventDaySelector(
    selectedDate: EventDate,
    updateSelectedDay: (EventDate) -> Unit,
    eventDates: ImmutableList<EventDate>,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    if (isLoading) {
        AnimatedShimmerEffect(
            gradientColors =
                persistentListOf(
                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.3f),
                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.2f),
                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.3f),
                ),
        ) { brush ->
            LazyRow(modifier = modifier) {
                items(3) {
                    LoadingBox(height = 48.dp, width = 64.dp, brush = brush, cornerRadius = 24.dp)
                    Spacer(Modifier.width(ButtonGroupDefaults.ConnectedSpaceBetween))
                }
            }
        }
    } else {
        ConnectedToggleGroup(
            items = eventDates,
            selected = selectedDate,
            onSelect = updateSelectedDay,
            modifier = modifier,
        ) { eventDay ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = ordinal(eventDay.value.toInt()),
                    style = MaterialTheme.typography.titleSmallEmphasized,
                )
                Text(
                    text = stringResource(R.string.event_day_label, eventDay.day),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}