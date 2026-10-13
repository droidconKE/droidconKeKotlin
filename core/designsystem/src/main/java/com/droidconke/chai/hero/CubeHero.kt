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
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.compose.LifecycleResumeEffect

/**
 * The conference cube, showing its logos one face at a time, each breaking up as the cube twists
 * and coming back together as it solves. It rests solved at [RestingPose] when [animate] is false or
 * animations are off, and pauses off screen and while the app is not resumed. At rest the cube
 * spans [CANVAS_PER_CUBE] less than the canvas, so it fits in every pose it moves through.
 */
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
    // Its own layer, so a frame of the cube re-records the cube and not the screen around it.
    Canvas(
        modifier =
            modifier
                .graphicsLayer()
                .onVisibilityChanged(minFractionVisible = VISIBLE_FRACTION) { visible = it }
                .semantics { hideFromAccessibility() },
    ) {
        val quads = motion.state.visibleQuads(motion.orientation, motion.turn)
        val scale = size.minDimension / (2 * MOVING_REACH * POP_SCALE) * motion.pop
        quads.fastForEach { with(painter) { drawFace(it, center, scale) } }
    }
}

private const val VISIBLE_FRACTION = 0.1f

/** The canvas, across, for each unit of the cube's resting width: room for it to move and click. */
internal const val CANVAS_PER_CUBE = MOVING_REACH * POP_SCALE / RESTING_REACH