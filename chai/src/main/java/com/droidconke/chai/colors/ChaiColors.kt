package com.droidconke.chai.colors

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import javax.annotation.concurrent.Immutable

@Immutable
data class ChaiColors(
    val textColorPrimary: Color = Color.Unspecified
)

val LocalChaiColorsPalette = staticCompositionLocalOf { ChaiColors() }

val ChaiLightColorPalette = ChaiColors(
    textColorPrimary = Color(color = 0xFF20201E)
)

val ChaiDarkColorPalette = ChaiColors(
    textColorPrimary = Color(color = 0xFFF5F5F5)
)
