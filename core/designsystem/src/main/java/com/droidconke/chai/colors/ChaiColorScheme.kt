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
import com.droidconke.chai.atoms.ChaiBlack
import com.droidconke.chai.atoms.ChaiBlue100
import com.droidconke.chai.atoms.ChaiBlue200
import com.droidconke.chai.atoms.ChaiBlue300
import com.droidconke.chai.atoms.ChaiBlue700
import com.droidconke.chai.atoms.ChaiBlue800
import com.droidconke.chai.atoms.ChaiBlue900
import com.droidconke.chai.atoms.ChaiErrorDark
import com.droidconke.chai.atoms.ChaiErrorLight
import com.droidconke.chai.atoms.ChaiGreen300
import com.droidconke.chai.atoms.ChaiGreen500
import com.droidconke.chai.atoms.ChaiGreen800
import com.droidconke.chai.atoms.ChaiGrey100
import com.droidconke.chai.atoms.ChaiGrey150
import com.droidconke.chai.atoms.ChaiGrey200
import com.droidconke.chai.atoms.ChaiGrey400
import com.droidconke.chai.atoms.ChaiGrey500
import com.droidconke.chai.atoms.ChaiGrey600
import com.droidconke.chai.atoms.ChaiGrey700
import com.droidconke.chai.atoms.ChaiGrey800
import com.droidconke.chai.atoms.ChaiGrey900
import com.droidconke.chai.atoms.ChaiInk
import com.droidconke.chai.atoms.ChaiOnErrorDark
import com.droidconke.chai.atoms.ChaiWhite

// Neon green is only ever a container with ink on it: as text on white it is 1.36:1.
internal val ChaiLightColorScheme: ColorScheme =
    lightColorScheme(
        primary = ChaiBlue700,
        onPrimary = ChaiWhite,
        primaryContainer = ChaiBlue100,
        onPrimaryContainer = ChaiBlue900,
        inversePrimary = ChaiBlue300,
        secondary = ChaiGreen800,
        onSecondary = ChaiWhite,
        secondaryContainer = ChaiGreen500,
        onSecondaryContainer = ChaiInk,
        tertiary = ChaiBlue900,
        onTertiary = ChaiWhite,
        tertiaryContainer = ChaiBlue200,
        onTertiaryContainer = ChaiBlue900,
        background = ChaiWhite,
        onBackground = ChaiInk,
        surface = ChaiWhite,
        onSurface = ChaiInk,
        surfaceVariant = ChaiGrey100,
        onSurfaceVariant = ChaiGrey600,
        surfaceContainerLowest = ChaiWhite,
        surfaceContainerLow = ChaiWhite,
        surfaceContainer = ChaiGrey100,
        surfaceContainerHigh = ChaiGrey150,
        surfaceContainerHighest = ChaiGrey200,
        inverseSurface = ChaiInk,
        inverseOnSurface = ChaiWhite,
        outline = ChaiGrey500,
        outlineVariant = ChaiGrey200,
        error = ChaiErrorLight,
        onError = ChaiWhite,
    )

// Blue text on black is 3.7:1, so dark mode leads with green, as droidcon.co.ke does.
internal val ChaiDarkColorScheme: ColorScheme =
    darkColorScheme(
        primary = ChaiGreen500,
        onPrimary = ChaiInk,
        primaryContainer = ChaiBlue800,
        onPrimaryContainer = ChaiWhite,
        inversePrimary = ChaiBlue700,
        secondary = ChaiGreen300,
        onSecondary = ChaiInk,
        secondaryContainer = ChaiGreen500,
        onSecondaryContainer = ChaiInk,
        tertiary = ChaiBlue300,
        onTertiary = ChaiBlue900,
        tertiaryContainer = ChaiBlue900,
        onTertiaryContainer = ChaiBlue100,
        background = ChaiBlack,
        onBackground = ChaiWhite,
        surface = ChaiBlack,
        onSurface = ChaiWhite,
        surfaceVariant = ChaiGrey900,
        onSurfaceVariant = ChaiGrey400,
        surfaceContainerLowest = ChaiBlack,
        surfaceContainerLow = ChaiGrey900,
        surfaceContainer = ChaiGrey900,
        surfaceContainerHigh = ChaiInk,
        surfaceContainerHighest = ChaiGrey800,
        inverseSurface = ChaiGrey100,
        inverseOnSurface = ChaiInk,
        outline = ChaiGrey700,
        outlineVariant = ChaiGrey800,
        error = ChaiErrorDark,
        onError = ChaiOnErrorDark,
    )