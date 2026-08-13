/*
 * Copyright 2022 DroidconKE
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
package com.droidconke.chai

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.droidconke.chai.colors.ChaiColors
import com.droidconke.chai.colors.ChaiDarkColorPalette
import com.droidconke.chai.colors.ChaiLightColorPalette
import com.droidconke.chai.colors.LocalChaiColorsPalette

@Composable
fun ChaiDCKE22Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    val customColorsPalette = if (darkTheme) ChaiDarkColorPalette else ChaiLightColorPalette

    if (!view.isInEditMode) {
        SideEffect {
            // findActivity() returns null outside an Activity-hosted composition —
            // Robolectric, screenshot tests, Glance hosts. It used to throw, which made
            // the theme unusable in any of those.
            val window = view.context.findActivity()?.window ?: return@SideEffect
            @Suppress("DEPRECATION")
            window.statusBarColor = customColorsPalette.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalChaiColorsPalette provides customColorsPalette,
    ) {
        MaterialTheme(
            content = content,
        )
    }
}

val MaterialTheme.chaiColorsPalette: ChaiColors
    @Composable
    @ReadOnlyComposable
    get() = LocalChaiColorsPalette.current

/**
 * Walks the context wrapper chain to find the closest [Activity], or null when the
 * composition is not hosted by one.
 *
 * Returns null rather than throwing. A theme composable should never be able to crash a
 * screenshot test, a Robolectric test, or a `ComposeView` hosted outside an Activity,
 * and the previous `IllegalStateException` did exactly that — the `isInEditMode` guard
 * only covers Studio previews.
 */
private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }