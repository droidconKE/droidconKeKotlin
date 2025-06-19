package com.droidconke

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import com.droidconke.chai.colors.ChaiColors
import com.droidconke.chai.colors.ChaiDarkColorPalette
import com.droidconke.chai.colors.ChaiLightColorPalette
import com.droidconke.chai.colors.LocalChaiColorsPalette

@Composable
actual fun ChaiDCKE22Theme(darkTheme: Boolean, content: @Composable (() -> Unit)) {
    val customColorsPalette = if (darkTheme) ChaiDarkColorPalette else ChaiLightColorPalette

    CompositionLocalProvider(
        LocalChaiColorsPalette provides customColorsPalette
    ) {
        MaterialTheme(
            content = content
        )
    }
}



