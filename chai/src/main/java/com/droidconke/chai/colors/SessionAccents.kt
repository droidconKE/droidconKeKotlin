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
package com.droidconke.chai.colors

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.droidconke.chai.chaiColorsPalette

/**
 * The accent colour used to distinguish sessions by the room they run in.
 *
 * This lives in chai, as a composable, for two reasons:
 *
 *  - It used to be a `val color` on `SessionPresentationModel`, a plain data class that
 *    imported the raw palette directly. Because a data class cannot read `MaterialTheme`,
 *    those colours were structurally unable to respond to dark mode or to any future
 *    theme change — they were fixed at construction.
 *  - The mapping keyed off the whole venue string with an exact `when`, so a session
 *    listed in two rooms ("Sapphire, Opal") matched neither and silently fell through to
 *    the default. [venueAccentColor] matches on the first room instead.
 *
 * Room names come from the conference API and change every year, so an unrecognised room
 * gets the neutral primary accent rather than being a bug.
 */
@Composable
@ReadOnlyComposable
fun venueAccentColor(venue: String): Color {
    val palette = MaterialTheme.chaiColorsPalette
    // A session can span rooms; the API joins them with a comma.
    val primaryRoom = venue.split(',').firstOrNull()?.trim().orEmpty()

    return when {
        primaryRoom.equals("Opal", ignoreCase = true) -> palette.eventDaySelectorActiveSurfaceColor
        primaryRoom.equals("Sapphire", ignoreCase = true) -> palette.eventDaySelectorInactiveSurfaceColor
        else -> palette.textTitlePrimaryColor
    }
}