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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.onVisibilityChanged
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.droidconke.chai.colors.CubeBodyLitColor
import com.droidconke.chai.colors.CubeBodyShadowColor
import com.droidconke.chai.colors.CubeTileColor
import com.droidconke.chai.colors.cubeLogoInk
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * The conference cube, tumbling, with a pixel logo on each face that breaks up as it scrambles
 * itself and comes back as it solves. It rests solved at [RestingPose] when [animate] is false or
 * animations are off, and pauses off screen and while the app is not resumed.
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
        val quads = motion.state.visibleQuads(RestingPose.YAW + motion.yaw, RestingPose.PITCH + motion.pitch, motion.turn)
        val scale = size.minDimension / CUBE_EXTENT
        quads.forEach { drawFace(it, path, center, scale) }
    }
}

/** Scrambles, solves, then holds the logos still; [run] picks up where it was cancelled. */
internal class CubeMotion {
    var state by mutableStateOf(CubeState.solved())
    var turn by mutableStateOf<Turn?>(null)
    var yaw by mutableFloatStateOf(0f)
    var pitch by mutableFloatStateOf(0f)
    private var next = 0
    private var seconds = 0f
    private val angle = Animatable(0f)

    suspend fun run(spec: FiniteAnimationSpec<Float>) =
        coroutineScope {
            launch { tumble() }
            while (true) {
                if (animationsOff()) {
                    rest()
                    delay(ANIMATIONS_OFF_POLL_MILLIS)
                    continue
                }
                if (next == 0) delay(HOLD_MILLIS)
                val move = Cycle[next]
                angle.snapTo(0f)
                try {
                    angle.animateTo(QUARTER, spec) { turn = Turn(move, value) }
                } finally {
                    turn = null
                }
                state = state.apply(move)
                next = (next + 1) % Cycle.size
            }
        }

    private suspend fun tumble() {
        var last = withFrameNanos { it }
        while (true) {
            if (animationsOff()) {
                delay(ANIMATIONS_OFF_POLL_MILLIS)
                last = withFrameNanos { it }
                continue
            }
            withFrameNanos { now ->
                seconds = (seconds + (now - last) / NANOS_PER_SECOND) % TUMBLE_SECONDS
                last = now
                yaw = FULL_TURN * seconds / TUMBLE_SECONDS
                pitch = PITCH_SWING * sin(2 * PI.toFloat() * PITCH_SWINGS * seconds / TUMBLE_SECONDS)
            }
        }
    }

    private fun rest() {
        state = CubeState.solved()
        turn = null
        yaw = 0f
        pitch = 0f
        next = 0
        seconds = 0f
    }

    private suspend fun animationsOff() = currentCoroutineContext()[MotionDurationScale]?.scaleFactor == 0f
}

private val Cycle = SignatureSolve.inverse() + SignatureSolve

private fun DrawScope.drawFace(
    quad: Quad,
    path: Path,
    origin: Offset,
    scale: Float,
) {
    path.rewind()
    path.addCell(quad, origin, scale, 0f, 0f, 1f, 1f)
    drawPath(path, lerp(CubeBodyShadowColor, CubeBodyLitColor, quad.light))
    path.rewind()
    path.addCell(quad, origin, scale, BEVEL, BEVEL, 1f - BEVEL, 1f - BEVEL)
    drawPath(path, CubeTileColor)
    if (quad.face == CubeFace.Inner) return
    for (ink in 1..2) {
        path.rewind()
        if (path.addLogoTile(quad, origin, scale, ink)) drawPath(path, cubeLogoInk(quad.face, ink))
    }
}

/** Adds the [ink] pixels of [quad]'s logo tile, a row run at a time; false if there are none. */
private fun Path.addLogoTile(
    quad: Quad,
    origin: Offset,
    scale: Float,
    ink: Int,
): Boolean {
    val size = CubeLogos.TILE
    val step = (1f - 2 * BEVEL) / size

    fun inkAt(
        column: Int,
        row: Int,
    ) = CubeLogos.ink(quad.face, quad.tileColumn * size + column, quad.tileRow * size + row)
    var any = false
    for (row in 0 until size) {
        var column = 0
        while (column < size) {
            val start = column
            while (column < size && inkAt(column, row) == ink) column++
            if (column > start) {
                addCell(quad, origin, scale, BEVEL + start * step, BEVEL + row * step, BEVEL + column * step, BEVEL + (row + 1) * step)
                any = true
            } else {
                column++
            }
        }
    }
    return any
}

/** Adds the part of [quad] between (u0, v0) and (u1, v1), measured from its top left corner. */
@Suppress("LongParameterList")
private fun Path.addCell(
    quad: Quad,
    origin: Offset,
    scale: Float,
    u0: Float,
    v0: Float,
    u1: Float,
    v1: Float,
) {
    val xs = quad.xs
    val ys = quad.ys

    fun x(
        u: Float,
        v: Float,
    ) = origin.x + (xs[0] + u * (xs[1] - xs[0]) + v * (xs[3] - xs[0])) * scale

    fun y(
        u: Float,
        v: Float,
    ) = origin.y + (ys[0] + u * (ys[1] - ys[0]) + v * (ys[3] - ys[0])) * scale
    moveTo(x(u0, v0), y(u0, v0))
    lineTo(x(u1, v0), y(u1, v0))
    lineTo(x(u1, v1), y(u1, v1))
    lineTo(x(u0, v1), y(u0, v1))
    close()
}

private const val QUARTER = 90f
private const val FULL_TURN = 360f
private const val HOLD_MILLIS = 6_000L
private const val ANIMATIONS_OFF_POLL_MILLIS = 1_000L
private const val NANOS_PER_SECOND = 1_000_000_000f
private const val TUMBLE_SECONDS = 30f
private const val PITCH_SWINGS = 2
private const val PITCH_SWING = 35f
private const val BEVEL = 0.07f
private const val VISIBLE_FRACTION = 0.1f

/** The cube's diagonal, plus a margin, so no pose crops a corner. */
private const val CUBE_EXTENT = 5.6f