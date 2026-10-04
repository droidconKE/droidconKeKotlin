/*
 * Copyright 2023 DroidconKE
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

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.droidconke.chai.atoms.ChaiCoal
import com.droidconke.chai.atoms.ChaiDarkGrey
import com.droidconke.chai.atoms.ChaiGrey
import com.droidconke.chai.atoms.ChaiGrey90
import com.droidconke.chai.atoms.ChaiTeal90
import com.droidconke.chai.atoms.ChaiWhite

/** Colours no Material role holds in both themes. Read `MaterialTheme.colorScheme` first. */
@Immutable
data class ChaiColors(
    /** Skeleton fill, between card and border in both themes. */
    val loadingShimmerColor: Color,
    /** Pastel teal in both themes; `tertiary` is the saturated teal in light. */
    val tealAccentColor: Color,
    /** White on the red accent in both themes; `onSecondary` is dark in dark. */
    val selectedDayContentColor: Color,
    /** Dark in both themes, under the white level label. */
    val badgeContainerColor: Color,
    /** White in both themes, which no role is. */
    val switchThumbColor: Color,
    /** Dark grey on the white thumb in both themes. */
    val switchOffIconColor: Color,
)

val LocalChaiColorsPalette =
    staticCompositionLocalOf<ChaiColors> {
        error("No ChaiColors provided. Wrap the content in ChaiTheme { }.")
    }

val ChaiLightColorPalette =
    ChaiColors(
        loadingShimmerColor = ChaiGrey,
        tealAccentColor = ChaiTeal90,
        selectedDayContentColor = ChaiWhite,
        badgeContainerColor = ChaiCoal,
        switchThumbColor = ChaiWhite,
        switchOffIconColor = ChaiGrey90,
    )

val ChaiDarkColorPalette =
    ChaiColors(
        loadingShimmerColor = ChaiDarkGrey,
        tealAccentColor = ChaiTeal90,
        selectedDayContentColor = ChaiWhite,
        badgeContainerColor = ChaiDarkGrey,
        switchThumbColor = ChaiWhite,
        switchOffIconColor = ChaiGrey90,
    )