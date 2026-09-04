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

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import com.droidconke.chai.atoms.ChaiTypography
import com.droidconke.chai.colors.ChaiColors
import com.droidconke.chai.colors.ChaiDarkColorPalette
import com.droidconke.chai.colors.ChaiDarkColorScheme
import com.droidconke.chai.colors.ChaiLightColorPalette
import com.droidconke.chai.colors.ChaiLightColorScheme
import com.droidconke.chai.colors.LocalChaiColorsPalette
import com.droidconke.chai.utils.CShapes

@Composable
fun ChaiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) ChaiDarkColorScheme else ChaiLightColorScheme
    val chaiColors = if (darkTheme) ChaiDarkColorPalette else ChaiLightColorPalette

    CompositionLocalProvider(LocalChaiColorsPalette provides chaiColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ChaiTypography,
            shapes = CShapes,
            content = content,
        )
    }
}

val MaterialTheme.chaiColorsPalette: ChaiColors
    @Composable
    @ReadOnlyComposable
    get() = LocalChaiColorsPalette.current