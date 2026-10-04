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

/**
 * Tier 3: the colours Material's roles cannot express. Everything else is read from
 * `MaterialTheme.colorScheme`.
 *
 * A token belongs here only when no role holds its value in both themes.
 */
@Immutable
data class ChaiColors(
    /** Skeleton fill, between the card and its border in both themes. */
    val loadingShimmerColor: Color,
    /** The pastel teal in both themes. `tertiary` is the saturated teal in light. */
    val tealAccent: Color,
    /** White on the red accent in both themes, where `onSecondary` turns dark in dark. */
    val selectedDayContentColor: Color,
    /** Dark in both themes, so the white level label reads on a light or dark card. */
    val badgeContainerColor: Color,
    val switchThumbColor: Color,
    val switchOffIconColor: Color,
)

val LocalChaiColorsPalette =
    staticCompositionLocalOf<ChaiColors> {
        error("No ChaiColors provided. Wrap the content in ChaiTheme { }.")
    }

val ChaiLightColorPalette =
    ChaiColors(
        loadingShimmerColor = ChaiGrey,
        tealAccent = ChaiTeal90,
        selectedDayContentColor = ChaiWhite,
        badgeContainerColor = ChaiCoal,
        switchThumbColor = ChaiWhite,
        switchOffIconColor = ChaiGrey90,
    )

val ChaiDarkColorPalette =
    ChaiColors(
        loadingShimmerColor = ChaiDarkGrey,
        tealAccent = ChaiTeal90,
        selectedDayContentColor = ChaiWhite,
        badgeContainerColor = ChaiDarkGrey,
        switchThumbColor = ChaiWhite,
        switchOffIconColor = ChaiGrey90,
    )