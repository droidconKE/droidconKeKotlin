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

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.onVisibilityChanged
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.droidconke.chai.colors.CubeBodyColor
import com.droidconke.chai.colors.cubeFaceColor
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

/**
 * The conference cube solving itself: [SignatureSolve], a slow turn, then the reverse 30 seconds
 * later. It holds still at [RestingPose], solved, when [animate] is false, with animations off,
 * when off screen and when the app is not resumed.
 */
@Composable
fun CubeHero(
    modifier: Modifier = Modifier,
    animate: Boolean = true,
) {
    val motion = remember { CubeMotion() }
    var resumed by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    LifecycleResumeEffect(Unit) {
        resumed = true
        onPauseOrDispose { resumed = false }
    }
    val turnSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    LaunchedEffect(animate && resumed && visible) {
        if (animate && resumed && visible) motion.run(turnSpec)
    }
    val path = remember { Path() }
    Canvas(
        modifier =
            modifier
                .onVisibilityChanged(minFractionVisible = VISIBLE_FRACTION) { visible = it }
                .semantics { hideFromAccessibility() },
    ) {
        val quads = motion.state.visibleQuads(RestingPose.YAW + motion.yaw, RestingPose.PITCH, motion.turn)
        quads.forEach { drawFace(it, path) }
    }
}

private class CubeMotion {
    var state by mutableStateOf(CubeState.solved())
    var turn by mutableStateOf<Turn?>(null)
    var yaw by mutableFloatStateOf(0f)
    private var forward by mutableStateOf(true)
    private var step by mutableIntStateOf(0)
    private val angle = Animatable(0f)

    suspend fun run(spec: FiniteAnimationSpec<Float>) {
        if ((currentCoroutineContext()[MotionDurationScale]?.scaleFactor ?: 1f) == 0f) return
        if (step == 0 && forward && state.isSolved) state = state.apply(SignatureSolve.inverse())
        while (true) {
            val moves = if (forward) SignatureSolve else SignatureSolve.inverse()
            while (step < moves.size) {
                val move = moves[step]
                angle.snapTo(0f)
                try {
                    angle.animateTo(QUARTER, spec) { turn = Turn(move, value) }
                } finally {
                    turn = null
                }
                state = state.apply(move)
                step++
            }
            withTimeoutOrNull(IDLE_MILLIS) {
                while (true) {
                    delay(FRAME_MILLIS)
                    yaw = (yaw + IDLE_DEGREES_PER_FRAME) % FULL_TURN
                }
            }
            forward = !forward
            step = 0
        }
    }
}

private fun DrawScope.drawFace(
    quad: Quad,
    path: Path,
) {
    val scale = size.minDimension / CUBE_EXTENT
    val cx = size.width / 2f
    val cy = size.height / 2f

    fun trace(inset: Float) {
        val mx = quad.xs.average().toFloat()
        val my = quad.ys.average().toFloat()
        path.rewind()
        for (i in 0 until 4) {
            val x = cx + (mx + (quad.xs[i] - mx) * inset) * scale
            val y = cy + (my + (quad.ys[i] - my) * inset) * scale
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
    }
    trace(1f)
    drawPath(path, CubeBodyColor)
    if (quad.face != CubeFace.Inner) {
        trace(STICKER_INSET)
        drawPath(path, cubeFaceColor(quad.face))
    }
}

private const val QUARTER = 90f
private const val FULL_TURN = 360f
private const val IDLE_MILLIS = 30_000L
private const val FRAME_MILLIS = 16L
private const val IDLE_DEGREES_PER_FRAME = 0.15f
private const val STICKER_INSET = 0.86f
private const val VISIBLE_FRACTION = 0.1f

/** The cube's diagonal, plus a margin, so no pose crops a corner. */
private const val CUBE_EXTENT = 5.6f