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
import com.droidconke.chai.atoms.ChaiBlue700
import com.droidconke.chai.atoms.ChaiGreen500
import com.droidconke.chai.atoms.ChaiGrey100
import com.droidconke.chai.atoms.ChaiGrey900
import com.droidconke.chai.atoms.ChaiWhite

/** Colours no Material role holds in both themes. Read `MaterialTheme.colorScheme` first. */
@Immutable
data class ChaiColors(
    val heroContainerColor: Color,
    /** Neon on the blue panel, for headline and display text only (4.1:1). */
    val heroContentColor: Color,
    /** White on the blue panel, for anything smaller than display type. */
    val heroOnContainerColor: Color,
    /** Fixed light backing for content (e.g. a dark logo) that must read against either theme. */
    val contrastLightSurface: Color,
    /** Fixed dark backing for content (e.g. a light logo) that must read against either theme. */
    val contrastDarkSurface: Color,
)

val LocalChaiColorsPalette =
    staticCompositionLocalOf<ChaiColors> {
        error("No ChaiColors provided. Wrap the content in ChaiTheme { }.")
    }

private val ChaiBrandColors =
    ChaiColors(
        heroContainerColor = ChaiBlue700,
        heroContentColor = ChaiGreen500,
        heroOnContainerColor = ChaiWhite,
        contrastLightSurface = ChaiGrey100,
        contrastDarkSurface = ChaiGrey900,
    )

val ChaiLightColorPalette = ChaiBrandColors

val ChaiDarkColorPalette = ChaiBrandColors