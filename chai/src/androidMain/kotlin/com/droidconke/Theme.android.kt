package com.droidconke

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.droidconke.chai.colors.ChaiDarkColorPalette
import com.droidconke.chai.colors.ChaiLightColorPalette
import com.droidconke.chai.colors.LocalChaiColorsPalette

@Composable
actual fun ChaiDcKeTheme(
    darkTheme: Boolean,
    content: @Composable (() -> Unit)
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