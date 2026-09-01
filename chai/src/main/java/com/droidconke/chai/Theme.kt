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
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.droidconke.chai.colors.ChaiColors
import com.droidconke.chai.colors.ChaiDarkColorPalette
import com.droidconke.chai.colors.ChaiLightColorPalette
import com.droidconke.chai.colors.LocalChaiColorsPalette

@Composable
fun ChaiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    val customColorsPalette = if (darkTheme) ChaiDarkColorPalette else ChaiLightColorPalette

    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.findActivity()?.window ?: return@SideEffect
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
 * The closest [Activity], or null when the composition is not hosted by one — Robolectric,
 * screenshot tests, a `ComposeView` outside an Activity. Never throws: a theme must not be
 * able to crash those.
 */
private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }