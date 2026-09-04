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

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.compositeOver
import com.droidconke.chai.atoms.ChaiBlack
import com.droidconke.chai.atoms.ChaiBlue
import com.droidconke.chai.atoms.ChaiCoal
import com.droidconke.chai.atoms.ChaiDarkGrey
import com.droidconke.chai.atoms.ChaiGrey
import com.droidconke.chai.atoms.ChaiGrey90
import com.droidconke.chai.atoms.ChaiLightGrey
import com.droidconke.chai.atoms.ChaiLightGrey90
import com.droidconke.chai.atoms.ChaiRed
import com.droidconke.chai.atoms.ChaiSmokeyGrey
import com.droidconke.chai.atoms.ChaiSubtleGrey
import com.droidconke.chai.atoms.ChaiTeal
import com.droidconke.chai.atoms.ChaiTeal90
import com.droidconke.chai.atoms.ChaiWhite

// Authored from the brand palette in `atoms/Color.kt` rather than derived from [ChaiColors],
// so `primary` is an accent in both themes rather than an accent in one and a surface in the other.
internal val ChaiLightColorScheme: ColorScheme =
    lightColorScheme(
        primary = ChaiBlue,
        onPrimary = ChaiWhite,
        primaryContainer = ChaiLightGrey90,
        onPrimaryContainer = ChaiBlue,
        secondary = ChaiRed,
        onSecondary = ChaiWhite,
        secondaryContainer = ChaiRed.copy(alpha = CONTAINER_TINT).compositeOver(ChaiWhite),
        onSecondaryContainer = ChaiCoal,
        tertiary = ChaiTeal,
        onTertiary = ChaiCoal,
        background = ChaiWhite,
        onBackground = ChaiGrey90,
        surface = ChaiWhite,
        onSurface = ChaiGrey90,
        onSurfaceVariant = ChaiSmokeyGrey,
        surfaceContainerLowest = ChaiWhite,
        surfaceContainerLow = ChaiLightGrey90,
        surfaceContainer = ChaiLightGrey,
        surfaceContainerHigh = ChaiLightGrey,
        surfaceContainerHighest = ChaiGrey.copy(alpha = SURFACE_TINT).compositeOver(ChaiWhite),
        outline = ChaiGrey,
        outlineVariant = ChaiLightGrey,
        error = ChaiRed,
        onError = ChaiWhite,
    )

internal val ChaiDarkColorScheme: ColorScheme =
    darkColorScheme(
        primary = ChaiTeal,
        onPrimary = ChaiCoal,
        primaryContainer = ChaiSubtleGrey,
        onPrimaryContainer = ChaiTeal90,
        secondary = ChaiRed,
        onSecondary = ChaiCoal,
        tertiary = ChaiTeal90,
        onTertiary = ChaiCoal,
        background = ChaiGrey90,
        onBackground = ChaiWhite,
        surface = ChaiGrey90,
        onSurface = ChaiWhite,
        onSurfaceVariant = ChaiGrey,
        surfaceContainerLowest = ChaiBlack,
        surfaceContainerLow = ChaiGrey90,
        surfaceContainer = ChaiSubtleGrey,
        surfaceContainerHigh = ChaiDarkGrey,
        surfaceContainerHighest = ChaiSmokeyGrey,
        outline = ChaiSmokeyGrey,
        outlineVariant = ChaiSubtleGrey,
        error = ChaiRed,
        onError = ChaiCoal,
    )

private const val CONTAINER_TINT = 0.12f
private const val SURFACE_TINT = 0.24f