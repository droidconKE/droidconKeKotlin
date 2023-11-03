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
import com.droidconke.chai.atoms.ChaiBlack
import com.droidconke.chai.atoms.ChaiBlue
import com.droidconke.chai.atoms.ChaiGrey
import com.droidconke.chai.atoms.ChaiGrey90
import com.droidconke.chai.atoms.ChaiLightGrey
import com.droidconke.chai.atoms.ChaiRed
import com.droidconke.chai.atoms.ChaiSmokeyGrey
import com.droidconke.chai.atoms.ChaiTeal90
import com.droidconke.chai.atoms.ChaiWhite

@Immutable
data class ChaiColors(
    val textColorPrimary: Color = Color.Unspecified,
    val background: Color = Color.Unspecified,
    val primaryContainer: Color = Color.Unspecified,
    val subtitleTextColor: Color = Color.Unspecified,
    val activeBottomNavIconColor: Color = Color.Unspecified,
    val inactiveBottomNavIconColor: Color = Color.Unspecified,
    val titleTextColorPrimary: Color = Color.Unspecified,
    val linkTextColorPrimary: Color = Color.Unspecified,
    val eventDaySelectorActiveSurfaceColor: Color = Color.Unspecified,
    val eventDaySelectorInactiveSurfaceColor: Color = Color.Unspecified,
    val eventDaySelectorInactiveTextColor: Color = Color.Unspecified,
    val eventDaySelectorActiveTextColor: Color = Color.Unspecified
)

val LocalChaiColorsPalette = staticCompositionLocalOf { ChaiColors() }

val ChaiLightColorPalette = ChaiColors(
    textColorPrimary = ChaiGrey90,
    background = ChaiWhite,
    primaryContainer = ChaiLightGrey,
    subtitleTextColor = ChaiSmokeyGrey,
    activeBottomNavIconColor = ChaiBlue,
    inactiveBottomNavIconColor = ChaiGrey90,
    titleTextColorPrimary = ChaiBlue,
    linkTextColorPrimary = ChaiBlue,
    eventDaySelectorActiveSurfaceColor = ChaiRed,
    eventDaySelectorInactiveSurfaceColor = ChaiTeal90,
    eventDaySelectorActiveTextColor = ChaiWhite,
    eventDaySelectorInactiveTextColor = ChaiGrey90
)

val ChaiDarkColorPalette = ChaiColors(
    textColorPrimary = ChaiLightGrey,
    background = ChaiGrey90,
    primaryContainer = ChaiBlack,
    subtitleTextColor = ChaiGrey,
    activeBottomNavIconColor = ChaiTeal90,
    inactiveBottomNavIconColor = ChaiWhite,
    titleTextColorPrimary = ChaiWhite,
    linkTextColorPrimary = ChaiLightGrey,
    eventDaySelectorActiveSurfaceColor = ChaiRed,
    eventDaySelectorInactiveSurfaceColor = ChaiTeal90,
    eventDaySelectorActiveTextColor = ChaiWhite,
    eventDaySelectorInactiveTextColor = ChaiWhite
)