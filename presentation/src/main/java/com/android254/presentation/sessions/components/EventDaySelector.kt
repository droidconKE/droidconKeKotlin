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

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android254.presentation.models.EventDate
import com.android254.presentation.sessions.view.SessionsViewModel
import kotlinx.datetime.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
val droidconEventDays = listOf(
    EventDate(LocalDate(2022, 11, 16)),
    EventDate(LocalDate(2022, 11, 17)),
    EventDate(LocalDate(2022, 11, 18)),
)

@RequiresApi(Build.VERSION_CODES.O)

fun ordinal(i: Int): String {
    val suffixes = arrayOf("th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th")
    return when (i % 100) {
        11, 12, 13 -> i.toString() + "th"
        else -> i.toString() + suffixes[i % 10]
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EventDaySelector(
    viewModel: SessionsViewModel = hiltViewModel()
) {
    val selectedDate by viewModel.selectedEventDate.observeAsState()
    Row() {
        droidconEventDays.forEachIndexed { index, eventDate ->
            EventDaySelectorButton(
                title = ordinal(eventDate.value.dayOfMonth),
                subtitle = "Day ${index + 1}",
                onClick = { viewModel.updateSelectedDay(eventDate) },
                selected = selectedDate == eventDate
            ) {
            }
            Spacer(Modifier.width(16.dp))
        }
    }
}