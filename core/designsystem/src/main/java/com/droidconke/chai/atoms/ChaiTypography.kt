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
package com.droidconke.chai.atoms

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import ke.droidcon.kotlin.chai.R

private val Montserrat =
    FontFamily(
        Font(R.font.montserrat_thin, FontWeight.Thin),
        Font(R.font.montserrat_extra_light, FontWeight.ExtraLight),
        Font(R.font.montserrat_light, FontWeight.Light),
        Font(R.font.montserrat_regular, FontWeight.Normal),
        Font(R.font.montserrat_medium, FontWeight.Medium),
        Font(R.font.montserrat_semi_bold, FontWeight.SemiBold),
        Font(R.font.montserrat_bold, FontWeight.Bold),
        Font(R.font.montserrat_extra_bold, FontWeight.ExtraBold),
    )

// Sizes are chai's own, so the display and headline ramp keeps Material's.
private val ChaiBaseTypography: Typography =
    with(Typography()) {
        copy(
            displayLarge = displayLarge.chai(FontWeight.Bold),
            displayMedium = displayMedium.chai(FontWeight.Bold),
            displaySmall = displaySmall.chai(FontWeight.Bold),
            headlineLarge = headlineLarge.chai(FontWeight.Bold),
            headlineMedium = headlineMedium.chai(FontWeight.SemiBold),
            headlineSmall = headlineSmall.chai(FontWeight.SemiBold),
            titleLarge = titleLarge.chai(FontWeight.Bold, size = 20, lineHeight = 28),
            titleMedium = titleMedium.chai(FontWeight.Bold, size = 18, lineHeight = 24),
            titleSmall = titleSmall.chai(FontWeight.Medium, size = 14, lineHeight = 20),
            bodyLarge = bodyLarge.chai(FontWeight.Normal, size = 18, lineHeight = 26),
            bodyMedium = bodyMedium.chai(FontWeight.Normal, size = 16, lineHeight = 24),
            bodySmall = bodySmall.chai(FontWeight.Normal, size = 14, lineHeight = 20),
            labelLarge = labelLarge.chai(FontWeight.SemiBold, size = 18, lineHeight = 24),
            labelMedium = labelMedium.chai(FontWeight.Normal, size = 12, lineHeight = 16),
            labelSmall = labelSmall.chai(FontWeight.Normal, size = 11, lineHeight = 16),
        )
    }

// One weight heavier; display and headline roles also tighten.
val ChaiTypography: Typography =
    with(ChaiBaseTypography) {
        copy(
            displayLargeEmphasized = displayLarge.emphasized().copy(letterSpacing = (-0.01).em),
            displayMediumEmphasized = displayMedium.emphasized().copy(letterSpacing = (-0.01).em),
            displaySmallEmphasized = displaySmall.emphasized().copy(letterSpacing = (-0.01).em),
            headlineLargeEmphasized = headlineLarge.emphasized().copy(letterSpacing = (-0.01).em),
            headlineMediumEmphasized = headlineMedium.emphasized().copy(letterSpacing = (-0.01).em),
            headlineSmallEmphasized = headlineSmall.emphasized().copy(letterSpacing = (-0.01).em),
            titleLargeEmphasized = titleLarge.emphasized(),
            titleMediumEmphasized = titleMedium.emphasized(),
            titleSmallEmphasized = titleSmall.emphasized(),
            bodyLargeEmphasized = bodyLarge.emphasized(),
            bodyMediumEmphasized = bodyMedium.emphasized(),
            bodySmallEmphasized = bodySmall.emphasized(),
            labelLargeEmphasized = labelLarge.emphasized(),
            labelMediumEmphasized = labelMedium.emphasized(),
            labelSmallEmphasized = labelSmall.emphasized(),
        )
    }

// Chai-sized roles drop letter spacing, which wraps session card labels at large font scales.
private fun TextStyle.chai(
    weight: FontWeight,
    size: Int? = null,
    lineHeight: Int? = null,
) = copy(
    fontFamily = Montserrat,
    fontWeight = weight,
    fontSize = size?.sp ?: fontSize,
    lineHeight = lineHeight?.sp ?: this.lineHeight,
    letterSpacing = if (size != null) 0.sp else letterSpacing,
)

private fun TextStyle.emphasized() =
    copy(
        fontWeight =
            when (fontWeight) {
                FontWeight.Normal -> FontWeight.Medium
                FontWeight.Medium -> FontWeight.SemiBold
                FontWeight.SemiBold -> FontWeight.Bold
                else -> FontWeight.ExtraBold
            },
    )