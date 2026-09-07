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
 * The accent colour distinguishing sessions by room.
 *
 * A composable so it tracks the theme; room names come from the API and change yearly, so
 * an unrecognised room falls back to the neutral accent rather than failing.
 */
@Composable
@ReadOnlyComposable
fun venueAccentColor(venue: String): Color {
    val palette = MaterialTheme.chaiColorsPalette
    // A session can span rooms; the API joins them with a comma.
    val primaryRoom =
        venue
            .split(',')
            .firstOrNull()
            ?.trim()
            .orEmpty()

    return when {
        primaryRoom.equals("Opal", ignoreCase = true) -> palette.eventDaySelectorActiveSurfaceColor
        primaryRoom.equals("Sapphire", ignoreCase = true) -> palette.eventDaySelectorInactiveSurfaceColor
        else -> palette.textTitlePrimaryColor
    }
}