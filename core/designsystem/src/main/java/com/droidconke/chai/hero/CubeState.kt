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

/** A whole-number vector: cubelet positions and orientations stay on the grid between turns. */
internal data class Vec3(
    val x: Int,
    val y: Int,
    val z: Int,
) {
    operator fun get(axis: Axis): Int =
        when (axis) {
            Axis.X -> x
            Axis.Y -> y
            Axis.Z -> z
        }

    infix fun cross(other: Vec3) = Vec3(y * other.z - z * other.y, z * other.x - x * other.z, x * other.y - y * other.x)

    /** A quarter turn about [axis], anticlockwise seen from its positive end when [sign] is 1. */
    fun quarterTurn(
        axis: Axis,
        sign: Int,
    ): Vec3 =
        when (axis) {
            Axis.X -> Vec3(x, -sign * z, sign * y)
            Axis.Y -> Vec3(sign * z, y, -sign * x)
            Axis.Z -> Vec3(-sign * y, sign * x, z)
        }

    companion object {
        val UnitX = Vec3(1, 0, 0)
        val UnitY = Vec3(0, 1, 0)
        val UnitZ = Vec3(0, 0, 1)
    }
}

internal enum class Axis { X, Y, Z }

/** One outer layer turned a quarter, clockwise as seen from outside that face. */
internal data class CubeMove(
    val axis: Axis,
    val layer: Int,
    val clockwise: Boolean,
) {
    init {
        require(layer == 1 || layer == -1) { "Only outer layers turn" }
    }

    /** +1 for an anticlockwise turn about the positive axis. */
    val sign: Int get() = if (clockwise) -layer else layer

    fun inverse() = copy(clockwise = !clockwise)

    companion object {
        val R = CubeMove(Axis.X, 1, clockwise = true)
        val L = CubeMove(Axis.X, -1, clockwise = true)
        val U = CubeMove(Axis.Y, 1, clockwise = true)
        val D = CubeMove(Axis.Y, -1, clockwise = true)
        val F = CubeMove(Axis.Z, 1, clockwise = true)
        val B = CubeMove(Axis.Z, -1, clockwise = true)
    }
}

internal fun List<CubeMove>.inverse(): List<CubeMove> = reversed().map(CubeMove::inverse)

/** [right] and [up] are where the cubelet's own x and y axes point now. */
internal data class Cubelet(
    val home: Vec3,
    val position: Vec3 = home,
    val right: Vec3 = Vec3.UnitX,
    val up: Vec3 = Vec3.UnitY,
) {
    val forward: Vec3 get() = right cross up

    fun turned(move: CubeMove) =
        copy(
            position = position.quarterTurn(move.axis, move.sign),
            right = right.quarterTurn(move.axis, move.sign),
            up = up.quarterTurn(move.axis, move.sign),
        )
}

internal data class CubeState(
    val cubelets: List<Cubelet>,
) {
    val isSolved: Boolean
        get() = cubelets.all { it.position == it.home && it.right == Vec3.UnitX && it.up == Vec3.UnitY }

    fun apply(move: CubeMove) = CubeState(cubelets.map { if (it.position[move.axis] == move.layer) it.turned(move) else it })

    fun apply(moves: List<CubeMove>) = moves.fold(this) { state, move -> state.apply(move) }

    companion object {
        fun solved() =
            CubeState(
                buildList {
                    for (x in -1..1) for (y in -1..1) for (z in -1..1) add(Cubelet(Vec3(x, y, z)))
                },
            )
    }
}

/** Twelve quarter turns, played from the scrambled cube to the solved one. */
internal val SignatureSolve: List<CubeMove> =
    with(CubeMove) {
        listOf(R, U, R.inverse(), U.inverse(), F, R, F.inverse(), L, D, B.inverse(), U, L.inverse())
    }