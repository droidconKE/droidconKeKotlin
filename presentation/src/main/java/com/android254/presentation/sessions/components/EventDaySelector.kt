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

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android254.presentation.models.EventDate

/*
val droidconEventDays = listOf(
    EventDate(LocalDate(2023, 11, 16), 1),
    EventDate(LocalDate(2023, 11, 17), 2),
    EventDate(LocalDate(2023, 11, 18), 3)
)
*/

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
    eventDates: List<EventDate>
) {
    LazyRow {
        items(eventDates) { eventDay ->
            EventDaySelectorButton(
                title = ordinal(eventDay.value.toInt()),
                subtitle = "Day ${eventDay.day}",
                onClick = { updateSelectedDay(eventDay) },
                selected = selectedDate == eventDay
            )
            Spacer(Modifier.width(16.dp))
        }
    }
}