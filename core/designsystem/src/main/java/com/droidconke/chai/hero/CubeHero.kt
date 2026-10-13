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
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.onVisibilityChanged
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.droidconke.chai.colors.CubeEdgeColor
import com.droidconke.chai.colors.CubeGapColor
import com.droidconke.chai.colors.CubeMarkInk
import com.droidconke.chai.colors.CubeSheenColor
import com.droidconke.chai.colors.CubeTileLitColor
import com.droidconke.chai.colors.CubeTileShadowColor
import com.droidconke.chai.colors.CubeWordInk
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * The conference cube, showing its logos one face at a time, each breaking up as the cube twists
 * and coming back together as it solves. It rests solved at [RestingPose] when [animate] is false or
 * animations are off, and pauses off screen and while the app is not resumed. At rest the cube
 * spans [CANVAS_PER_CUBE] less than the canvas, so it fits in every pose it moves through.
 */
@Composable
fun CubeHero(
    modifier: Modifier = Modifier,
    animate: Boolean = true,
) {
    CubeHero(remember { CubeMotion() }, modifier, animate)
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
                .onVisibilityChanged(minFractionVisible = VISIBLE_FRACTION) { visible = it }
                .semantics { hideFromAccessibility() },
    ) {
        val quads = motion.state.visibleQuads(motion.orientation, motion.turn)
        val scale = size.minDimension / (2 * MOVING_REACH * POP_SCALE) * motion.pop
        quads.forEach { with(painter) { drawFace(it, center, scale) } }
    }
}

/**
 * Shows each logo in turn, as the conference cube does: it holds a face, twists itself up as it
 * turns to the next one, then solves so that logo comes together facing the viewer. [run] starts
 * again from the face it was showing, whole, when it is cancelled.
 */
internal class CubeMotion {
    var state by mutableStateOf(CubeState.solved())
    var turn by mutableStateOf<Turn?>(null)
    var orientation by mutableStateOf(Poses[0])
    val pop: Float get() = popScale.value
    private var scene = 0
    private val angle = Animatable(0f)
    private val progress = Animatable(0f)
    private val popScale = Animatable(1f)

    suspend fun run() {
        settle()
        while (true) {
            if (animationsOff()) {
                rest()
                delay(ANIMATIONS_OFF_POLL_MILLIS)
                continue
            }
            hold()
            val next = (scene + 1) % Showcase.size
            val scramble = scrambleTowards(Showcase[next])
            coroutineScope {
                launch { travel(Poses[scene], Poses[next]) }
                playAll(scramble, ScrambleSpec)
            }
            scene = next
            playAll(scramble.inverse(), SolveSpec)
            click()
        }
    }

    suspend fun rest() {
        scene = 0
        settle()
    }

    private suspend fun settle() {
        state = CubeState.solved()
        turn = null
        orientation = Poses[scene]
        popScale.snapTo(1f)
    }

    /** Leans the cube a little and back, so it never sits dead still. */
    private suspend fun hold() {
        val pose = Poses[scene]
        progress.snapTo(0f)
        progress.animateTo(1f, tween(HOLD_MILLIS, easing = LinearEasing)) {
            val lean = (1 - cos(2 * PI.toFloat() * value)) / 2
            orientation = pose + Orientation.euler(LEAN_YAW * lean, LEAN_PITCH * lean)
        }
    }

    private suspend fun travel(
        from: Orientation,
        to: Orientation,
    ) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(TRAVEL_MILLIS, easing = EmphasizedEasing)) { orientation = from.towards(to, value) }
    }

    private suspend fun playAll(
        moves: List<CubeMove>,
        spec: FiniteAnimationSpec<Float>,
    ) {
        moves.forEachIndexed { i, move ->
            if (i > 0) delay(TURN_GAP_MILLIS)
            angle.snapTo(0f)
            try {
                angle.animateTo(QUARTER, spec) { turn = Turn(move, value) }
            } finally {
                turn = null
            }
            state = state.apply(move)
        }
    }

    /** The last layer clicks home: the cube swells a touch and springs back. */
    private suspend fun click() {
        popScale.animateTo(POP_SCALE, tween(POP_MILLIS))
        popScale.animateTo(1f, spring(dampingRatio = POP_DAMPING, stiffness = Spring.StiffnessMediumLow))
    }

    private suspend fun animationsOff() = currentCoroutineContext()[MotionDurationScale]?.scaleFactor == 0f
}

/** The order the faces are shown in; each is next to the one before, so the cube turns a quarter. */
internal val Showcase = listOf(CubeFace.Front, CubeFace.Right, CubeFace.Up, CubeFace.Back, CubeFace.Left, CubeFace.Down)

private val Poses = Showcase.map(::presenting)

/** Three turns of layers across [face], so its logo arrives in pieces; played backwards, they solve it. */
internal fun scrambleTowards(face: CubeFace): List<CubeMove> {
    val (across, along) =
        when (face) {
            CubeFace.Right, CubeFace.Left -> Axis.Y to Axis.Z
            CubeFace.Up, CubeFace.Down -> Axis.Z to Axis.X
            else -> Axis.X to Axis.Y
        }
    return listOf(CubeMove(across, 1, clockwise = true), CubeMove(along, -1, clockwise = true), CubeMove(across, -1, clockwise = false))
}

private val EmphasizedEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
private val ScrambleSpec = tween<Float>(SCRAMBLE_TURN_MILLIS, easing = CubicBezierEasing(0.6f, 0f, 0.3f, 1f))
private val SolveSpec = tween<Float>(SOLVE_TURN_MILLIS, easing = EmphasizedEasing)

/** Draws one face at a time, reusing its paths, matrix and edge stroke between faces and frames. */
private class CubePainter {
    private val path = Path()
    private val tile = Path()
    private val matrix = Matrix()
    private var edge = Stroke(width = 0f)

    fun DrawScope.drawFace(
        quad: Quad,
        origin: Offset,
        scale: Float,
    ) {
        path.rewind()
        path.addCell(quad, origin, scale, 0f, 0f, 1f, 1f)
        drawPath(path, CubeGapColor)
        tile.rewind()
        tile.addRoundedTile(quad, origin, scale)
        drawPath(tile, lerp(CubeTileShadowColor, CubeTileLitColor, quad.light))
        val shine = quad.shine()
        val topLeft = Offset(quad.screenX(origin, scale, 0f, 0f), quad.screenY(origin, scale, 0f, 0f))
        val bottomRight = Offset(quad.screenX(origin, scale, 1f, 1f), quad.screenY(origin, scale, 1f, 1f))
        drawPath(tile, Brush.linearGradient(listOf(CubeSheenColor.copy(alpha = SHEEN_ALPHA * shine), Color.Transparent), topLeft, bottomRight))
        when (quad.face) {
            CubeFace.Inner -> Unit
            CubeFace.Front -> drawWordmark(quad, origin, scale)
            else -> drawPixels(quad, origin, scale)
        }
        if (edge.width != EDGE_WIDTH * scale) edge = Stroke(width = EDGE_WIDTH * scale)
        drawPath(tile, CubeEdgeColor.copy(alpha = EDGE_ALPHA + EDGE_SHINE_ALPHA * shine), style = edge)
    }

    private fun DrawScope.drawPixels(
        quad: Quad,
        origin: Offset,
        scale: Float,
    ) {
        val runs = CubeLogos.runs(quad.face, quad.tileColumn, quad.tileRow)
        val step = (1f - 2 * BEVEL) / CubeLogos.TILE
        path.rewind()
        runs.forEachIndexed { i, run ->
            path.addCell(quad, origin, scale, BEVEL + run.start * step, BEVEL + run.row * step, BEVEL + run.end * step, BEVEL + (run.row + 1) * step)
            if (i == runs.lastIndex || runs[i + 1].color != run.color) {
                drawPath(path, run.color)
                path.rewind()
            }
        }
    }

    /** "droid" across the top two rows of the face and the "con" mark below, cut into ninths. */
    private fun DrawScope.drawWordmark(
        quad: Quad,
        origin: Offset,
        scale: Float,
    ) {
        clipPath(tile) {
            if (quad.tileRow < 2) {
                drawLogoPath(quad, origin, scale, CubeWordmark.word, FACE_WORD_LEFT, FACE_WORD_TOP, FACE_WORD_WIDTH / CubeWordmark.WORD_WIDTH, CubeWordInk)
            }
            if (quad.tileRow > 0) {
                val markScale = FACE_MARK_WIDTH / CubeWordmark.MARK_WIDTH
                drawLogoPath(quad, origin, scale, CubeWordmark.mark, FACE_MARK_LEFT - CubeWordmark.MARK_LEFT * markScale, FACE_MARK_TOP, markScale, CubeMarkInk)
            }
        }
    }

    /** Draws [logo], placed at ([left], [top]) in face units and [units] face units per point. */
    private fun DrawScope.drawLogoPath(
        quad: Quad,
        origin: Offset,
        scale: Float,
        logo: Path,
        left: Float,
        top: Float,
        units: Float,
        color: Color,
    ) {
        val ox = quad.screenX(origin, scale, 0f, 0f)
        val oy = quad.screenY(origin, scale, 0f, 0f)
        val ax = quad.screenX(origin, scale, 1f, 0f) - ox
        val ay = quad.screenY(origin, scale, 1f, 0f) - oy
        val bx = quad.screenX(origin, scale, 0f, 1f) - ox
        val by = quad.screenY(origin, scale, 0f, 1f) - oy
        val u = left - quad.tileColumn
        val v = top - quad.tileRow
        matrix.reset()
        matrix.values[Matrix.ScaleX] = units * ax
        matrix.values[Matrix.SkewY] = units * ay
        matrix.values[Matrix.SkewX] = units * bx
        matrix.values[Matrix.ScaleY] = units * by
        matrix.values[Matrix.TranslateX] = ox + u * ax + v * bx
        matrix.values[Matrix.TranslateY] = oy + u * ay + v * by
        withTransform({ transform(matrix) }) { drawPath(logo, color) }
    }
}

/** How brightly a face catches a light above and to the left of the viewer, from 0 to 1. */
private fun Quad.shine(): Float {
    val h = LightX * tiltX + LightY * tiltY + LightZ * light
    return max(0f, h).pow(SHINE_POWER)
}

private val LightLength = sqrt(LIGHT_X * LIGHT_X + LIGHT_Y * LIGHT_Y + (LIGHT_Z + 1f) * (LIGHT_Z + 1f))
private val LightX = LIGHT_X / LightLength
private val LightY = LIGHT_Y / LightLength
private val LightZ = (LIGHT_Z + 1f) / LightLength

/** Adds the part of [quad] between (u0, v0) and (u1, v1), measured from its top left corner. */
private fun Path.addCell(
    quad: Quad,
    origin: Offset,
    scale: Float,
    u0: Float,
    v0: Float,
    u1: Float,
    v1: Float,
) {
    moveTo(quad.screenX(origin, scale, u0, v0), quad.screenY(origin, scale, u0, v0))
    lineTo(quad.screenX(origin, scale, u1, v0), quad.screenY(origin, scale, u1, v0))
    lineTo(quad.screenX(origin, scale, u1, v1), quad.screenY(origin, scale, u1, v1))
    lineTo(quad.screenX(origin, scale, u0, v1), quad.screenY(origin, scale, u0, v1))
    close()
}

/** The tile inside the gaps, with corners rounded by [CORNER] of its side. */
private fun Path.addRoundedTile(
    quad: Quad,
    origin: Offset,
    scale: Float,
) {
    val lo = BEVEL
    val hi = 1f - BEVEL

    fun x(
        u: Float,
        v: Float,
    ) = quad.screenX(origin, scale, u, v)

    fun y(
        u: Float,
        v: Float,
    ) = quad.screenY(origin, scale, u, v)
    moveTo(x(lo + CORNER, lo), y(lo + CORNER, lo))
    lineTo(x(hi - CORNER, lo), y(hi - CORNER, lo))
    quadraticTo(x(hi, lo), y(hi, lo), x(hi, lo + CORNER), y(hi, lo + CORNER))
    lineTo(x(hi, hi - CORNER), y(hi, hi - CORNER))
    quadraticTo(x(hi, hi), y(hi, hi), x(hi - CORNER, hi), y(hi - CORNER, hi))
    lineTo(x(lo + CORNER, hi), y(lo + CORNER, hi))
    quadraticTo(x(lo, hi), y(lo, hi), x(lo, hi - CORNER), y(lo, hi - CORNER))
    lineTo(x(lo, lo + CORNER), y(lo, lo + CORNER))
    quadraticTo(x(lo, lo), y(lo, lo), x(lo + CORNER, lo), y(lo + CORNER, lo))
    close()
}

private fun Quad.screenX(
    origin: Offset,
    scale: Float,
    u: Float,
    v: Float,
) = origin.x + (xs[0] + u * (xs[1] - xs[0]) + v * (xs[3] - xs[0]) + u * v * (xs[2] - xs[1] - xs[3] + xs[0])) * scale

private fun Quad.screenY(
    origin: Offset,
    scale: Float,
    u: Float,
    v: Float,
) = origin.y + (ys[0] + u * (ys[1] - ys[0]) + v * (ys[3] - ys[0]) + u * v * (ys[2] - ys[1] - ys[3] + ys[0])) * scale

private const val QUARTER = 90f
private const val HOLD_MILLIS = 2_600
private const val TRAVEL_MILLIS = 1_300
private const val SCRAMBLE_TURN_MILLIS = 380
private const val SOLVE_TURN_MILLIS = 520
private const val TURN_GAP_MILLIS = 80L
private const val LEAN_YAW = 10f
private const val LEAN_PITCH = -5f
internal const val POP_SCALE = 1.04f
private const val POP_MILLIS = 90
private const val POP_DAMPING = 0.4f
private const val ANIMATIONS_OFF_POLL_MILLIS = 1_000L
private const val VISIBLE_FRACTION = 0.1f

private const val BEVEL = 0.045f
private const val CORNER = 0.14f
private const val EDGE_WIDTH = 0.012f
private const val EDGE_ALPHA = 0.18f
private const val EDGE_SHINE_ALPHA = 0.6f
private const val SHEEN_ALPHA = 0.7f
private const val SHINE_POWER = 4f
private const val LIGHT_X = -0.45f
private const val LIGHT_Y = 0.65f
private const val LIGHT_Z = 0.6f

// The front face's wordmark, in face units: three tiles across.
private const val FACE_WORD_LEFT = 0.3f
private const val FACE_WORD_TOP = 0.55f
private const val FACE_WORD_WIDTH = 2.4f
private const val FACE_MARK_LEFT = 0.6f
private const val FACE_MARK_TOP = 1.75f
private const val FACE_MARK_WIDTH = 1.8f

/** The canvas, across, for each unit of the cube's resting width: room for it to move and click. */
internal const val CANVAS_PER_CUBE = MOVING_REACH * POP_SCALE / RESTING_REACH