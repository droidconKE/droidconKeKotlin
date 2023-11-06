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
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val customColorsPalette = if (darkTheme) ChaiDarkColorPalette else ChaiLightColorPalette

    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context.findActivity()
            activity.window.statusBarColor = customColorsPalette.background.toArgb()
            WindowCompat.getInsetsController(activity.window, view).isAppearanceLightStatusBars =
                !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalChaiColorsPalette provides customColorsPalette
    ) {
        MaterialTheme(
            content = content
        )
    }
}

val MaterialTheme.chaiColorsPalette: ChaiColors
    @Composable
    @ReadOnlyComposable
    get() = LocalChaiColorsPalette.current

/**
 * Iterate through the context wrapper to find the closest activity associated with this context
 * This method is preferred to LocalContext.current as Activity
 * see [Theme.md](https://gist.github.com/GibsonRuitiari/7cb947228661993ee36d5c05b9e8f23f)
 * Throws [IllegalStateException] if no activity was found
 * @return an activity instance
 */
private fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("Activity absent")
}