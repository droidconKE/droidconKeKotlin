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
package com.android254.presentation.common.insets

import android.app.Activity
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Overrides the status bar icon appearance for as long as this stays in the composition.
 *
 * `enableEdgeToEdge()` derives the icons from the theme, which is the right answer for every
 * screen whose app bar is a theme surface. A screen that draws artwork up there — the feedback
 * hero is the only one — is a surface the theme knows nothing about, and gets dark icons on a
 * saturated background. Only such a screen should reach for this: the previous appearance is
 * restored on the way out, so a screen that opts in cannot leave the next one wrong.
 */
@Composable
fun StatusBarIconAppearance(darkIcons: Boolean) {
    val view = LocalView.current
    if (LocalInspectionMode.current) return

    DisposableEffect(view, darkIcons) {
        val window = view.context.findActivity()?.window
        if (window == null) {
            onDispose { }
        } else {
            val controller = WindowCompat.getInsetsController(window, view)
            val restore = controller.isAppearanceLightStatusBars
            controller.isAppearanceLightStatusBars = darkIcons
            onDispose { controller.isAppearanceLightStatusBars = restore }
        }
    }
}

private tailrec fun android.content.Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }