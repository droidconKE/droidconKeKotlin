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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertEquals
import org.junit.Test

private const val TEXT = 4.5
private const val NON_TEXT = 3.0

class ChaiColorContrastTest {
    private data class ContrastPair(
        val name: String,
        val foreground: Color,
        val background: Color,
        val required: Double,
    )

    @Test
    fun `every light role pair meets WCAG AA`() = assertAllPass(pairs(ChaiLightColorScheme, ChaiLightColorPalette))

    @Test
    fun `every dark role pair meets WCAG AA`() = assertAllPass(pairs(ChaiDarkColorScheme, ChaiDarkColorPalette))

    /** The stage is the same black in both themes; its title is display type, which WCAG treats as large text. */
    @Test
    fun `the cube stage's text meets WCAG AA`() =
        assertAllPass(
            listOf(ContrastPair("stage text on stage", CubeStageTextColor, CubeStageColor, TEXT)) +
                CubeStageTitleColors.mapIndexed { i, color -> ContrastPair("stage title stop $i on stage", color, CubeStageColor, NON_TEXT) },
        )

    private fun pairs(
        scheme: ColorScheme,
        chai: ChaiColors,
    ): List<ContrastPair> =
        with(scheme) {
            val surfaces =
                listOf(
                    "background" to background,
                    "surface" to surface,
                    "surfaceVariant" to surfaceVariant,
                    "surfaceContainerLowest" to surfaceContainerLowest,
                    "surfaceContainerLow" to surfaceContainerLow,
                    "surfaceContainer" to surfaceContainer,
                    "surfaceContainerHigh" to surfaceContainerHigh,
                    "surfaceContainerHighest" to surfaceContainerHighest,
                )
            val onSurfaces =
                listOf(
                    Triple("onSurface", onSurface, TEXT),
                    Triple("onSurfaceVariant", onSurfaceVariant, TEXT),
                    Triple("primary", primary, TEXT),
                    Triple("secondary", secondary, TEXT),
                    Triple("tertiary", tertiary, TEXT),
                    Triple("error", error, TEXT),
                    Triple("outline", outline, NON_TEXT),
                )
            surfaces.flatMap { (bgName, bg) ->
                onSurfaces.map { (fgName, fg, required) -> ContrastPair("$fgName on $bgName", fg, bg, required) }
            } +
                listOf(
                    ContrastPair("onBackground on background", onBackground, background, TEXT),
                    ContrastPair("onPrimary on primary", onPrimary, primary, TEXT),
                    ContrastPair("onPrimaryContainer on primaryContainer", onPrimaryContainer, primaryContainer, TEXT),
                    ContrastPair("onSecondary on secondary", onSecondary, secondary, TEXT),
                    ContrastPair("onSecondaryContainer on secondaryContainer", onSecondaryContainer, secondaryContainer, TEXT),
                    ContrastPair("onTertiary on tertiary", onTertiary, tertiary, TEXT),
                    ContrastPair("onTertiaryContainer on tertiaryContainer", onTertiaryContainer, tertiaryContainer, TEXT),
                    ContrastPair("onError on error", onError, error, TEXT),
                    ContrastPair("onErrorContainer on errorContainer", onErrorContainer, errorContainer, TEXT),
                    ContrastPair("inverseOnSurface on inverseSurface", inverseOnSurface, inverseSurface, TEXT),
                    ContrastPair("inversePrimary on inverseSurface", inversePrimary, inverseSurface, TEXT),
                    ContrastPair("heroOnContainer on heroContainer", chai.heroOnContainerColor, chai.heroContainerColor, TEXT),
                    // Display type only, which WCAG treats as large text.
                    ContrastPair("heroContent on heroContainer", chai.heroContentColor, chai.heroContainerColor, NON_TEXT),
                )
        }

    private fun assertAllPass(pairs: List<ContrastPair>) {
        val failures =
            pairs.mapNotNull { pair ->
                val ratio = contrastRatio(pair.foreground, pair.background)
                if (ratio < pair.required) "${pair.name}: %.2f:1, needs %.1f:1".format(ratio, pair.required) else null
            }
        assertEquals("", failures.joinToString("\n"))
    }

    private fun contrastRatio(
        a: Color,
        b: Color,
    ): Double {
        val la = a.luminance().toDouble()
        val lb = b.luminance().toDouble()
        return (maxOf(la, lb) + 0.05) / (minOf(la, lb) + 0.05)
    }
}