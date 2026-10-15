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
package com.droidconke.chai.hero

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onVisibilityChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.compose.LifecycleResumeEffect

/** The conference cube, turning through its logos. It rests solved when [animate] is false or animations are off, and pauses off screen and in the background. */
@Composable
internal fun CubeHero(
    modifier: Modifier = Modifier,
    animate: Boolean = true,
) {
    val scene = rememberSaveable { mutableIntStateOf(0) }
    CubeHero(remember { CubeMotion(scene) }, modifier, animate)
}

@Composable
internal fun CubeHero(
    motion: CubeMotion,
    modifier: Modifier = Modifier,
    animate: Boolean = true,
) {
    var resumed by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    LifecycleResumeEffect(Unit) {
        resumed = true
        onPauseOrDispose { resumed = false }
    }
    LaunchedEffect(Unit) { CubeMarks.cutAll() }
    LaunchedEffect(animate, resumed, visible) {
        when {
            !animate -> motion.rest()
            resumed && visible -> motion.run()
        }
    }
    val painter = remember { CubePainter() }
    Canvas(
        modifier =
            modifier
                .graphicsLayer()
                .testTag(CUBE_HERO_TAG)
                .onVisibilityChanged(minFractionVisible = VISIBLE_FRACTION) { visible = it }
                .semantics { hideFromAccessibility() },
    ) {
        val quads = motion.state.visibleQuads(motion.orientation, motion.turn)
        val scale = size.minDimension / (2 * MOVING_REACH * POP_SCALE) * motion.pop
        quads.fastForEach { with(painter) { drawFace(it, center, scale) } }
    }
}

private const val VISIBLE_FRACTION = 0.1f

/** Screenshot tests compare loosely inside this tag. */
const val CUBE_HERO_TAG = "cube_hero"

/** Canvas width per unit of resting cube width, so every pose and the click fit. */
internal const val CANVAS_PER_CUBE = MOVING_REACH * POP_SCALE / RESTING_REACH