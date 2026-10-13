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

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.MotionDurationScale
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.cos

/**
 * Shows each logo in turn, as the conference cube does: it holds a face, twists itself up as it
 * turns to the next one, then solves so that logo comes together facing the viewer. [scene] is the
 * face being shown; it is hoisted so the cube carries on from it after a configuration change.
 * When [run] is cancelled the cube is left whole, showing that face, and [run] starts again from it.
 */
@Stable
internal class CubeMotion(
    private val sceneState: MutableIntState = mutableIntStateOf(0),
) {
    var state by mutableStateOf(CubeState.solved())
    var turn by mutableStateOf<Turn?>(null)
    var orientation by mutableStateOf(Poses[sceneState.intValue])
    val pop: Float get() = popScale.value
    val scene: Int get() = sceneState.intValue
    private val angle = Animatable(0f)
    private val progress = Animatable(0f)
    private val popScale = Animatable(1f)

    suspend fun run() {
        try {
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
                    launch { travel(Travels[scene]) }
                    playAll(scramble, ScrambleSpec)
                }
                sceneState.intValue = next
                playAll(scramble.inverse(), SolveSpec)
                click()
            }
        } finally {
            withContext(NonCancellable) { settle() }
        }
    }

    suspend fun rest() {
        sceneState.intValue = 0
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

    private suspend fun travel(travel: Travel) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(TRAVEL_MILLIS, easing = EmphasizedEasing)) { orientation = travel.at(value) }
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

/** The order the faces are shown in: droidcon, FlutterCon, React, Kotlin, Swift, AI. */
internal val Showcase = listOf(CubeFace.Front, CubeFace.Right, CubeFace.Up, CubeFace.Back, CubeFace.Left, CubeFace.Down)

internal val Poses = Showcase.map(::presenting)

/** The whole cube's turn from one scene's pose to the next, [at] a fraction of the way. */
internal class Travel(
    val from: Orientation,
    val turn: Orientation,
) {
    fun at(fraction: Float): Orientation = from + turn.scaled(fraction)
}

/**
 * The travel into each scene's successor. Most are a quarter or a third of a turn, which have one
 * short way round. React to Kotlin is a half turn, which has two; it goes on in the sense the cube
 * was already turning, so the flip reads as one tumble rather than a change of mind.
 */
internal val Travels: List<Travel> =
    buildList {
        var previous = Poses.last().turnTo(Poses.first())
        Poses.indices.forEach { i ->
            val from = Poses[i]
            var turn = from.turnTo(Poses[(i + 1) % Poses.size])
            if (turn.isHalfTurn && turn.axisDot(previous) < 0f) turn = turn.inverse()
            add(Travel(from, turn))
            previous = turn
        }
    }

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

private const val QUARTER = 90f
private const val HOLD_MILLIS = 2_600
private const val TRAVEL_MILLIS = 1_300
private const val SCRAMBLE_TURN_MILLIS = 380
private const val SOLVE_TURN_MILLIS = 520
private const val TURN_GAP_MILLIS = 80L
internal const val LEAN_YAW = 10f
internal const val LEAN_PITCH = -5f
internal const val POP_SCALE = 1.04f
private const val POP_MILLIS = 90
private const val POP_DAMPING = 0.4f
private const val ANIMATIONS_OFF_POLL_MILLIS = 1_000L