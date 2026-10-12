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

import kotlin.math.cos
import kotlin.math.sin

/** The three-quarter view the cube rests at: front, right and top faces showing. */
object RestingPose {
    const val YAW = -35f
    const val PITCH = 25f
}

/** Which face of the solved cube a sticker belongs to, or [Inner] for the body between cubelets. */
enum class CubeFace { Right, Left, Up, Down, Front, Back, Inner }

/** A layer part-way through [move], [degrees] from where it started. */
data class Turn(
    val move: CubeMove,
    val degrees: Float,
)

/** One visible face, projected orthographically: [xs] and [ys] are its four corners in cube units. */
class Quad(
    val face: CubeFace,
    val xs: FloatArray,
    val ys: FloatArray,
    val depth: Float,
)

private class Point(
    val x: Float,
    val y: Float,
    val z: Float,
) {
    operator fun plus(o: Point) = Point(x + o.x, y + o.y, z + o.z)

    operator fun times(s: Float) = Point(x * s, y * s, z * s)

    fun rotated(
        axis: Axis,
        radians: Float,
    ): Point {
        val c = cos(radians)
        val s = sin(radians)
        return when (axis) {
            Axis.X -> Point(x, y * c - z * s, y * s + z * c)
            Axis.Y -> Point(x * c + z * s, y, -x * s + z * c)
            Axis.Z -> Point(x * c - y * s, x * s + y * c, z)
        }
    }
}

private fun Vec3.toPoint() = Point(x.toFloat(), y.toFloat(), z.toFloat())

/**
 * The faces a viewer sees, back to front, after yawing the cube about its vertical axis and then
 * pitching it towards the viewer. Faces pointing away are culled; the rest are painted in order.
 */
fun CubeState.visibleQuads(
    yawDegrees: Float,
    pitchDegrees: Float,
    turn: Turn? = null,
): List<Quad> {
    val yaw = Math.toRadians(yawDegrees.toDouble()).toFloat()
    val pitch = Math.toRadians(pitchDegrees.toDouble()).toFloat()

    fun view(
        p: Point,
        turning: Boolean,
    ): Point {
        val turned = if (turning && turn != null) p.rotated(turn.move.axis, turn.move.sign * Math.toRadians(turn.degrees.toDouble()).toFloat()) else p
        return turned.rotated(Axis.Y, yaw).rotated(Axis.X, pitch)
    }
    return cubelets
        .flatMap { cubelet ->
            val turning = turn != null && cubelet.position[turn.move.axis] == turn.move.layer
            LocalFaces.mapNotNull { local -> cubelet.quad(local, turning, ::view) }
        }.sortedBy { it.depth }
}

private class LocalFace(
    val normal: Vec3,
    val tangentA: Vec3,
    val tangentB: Vec3,
    val face: CubeFace,
)

private val LocalFaces =
    listOf(
        LocalFace(Vec3(1, 0, 0), Vec3.UnitY, Vec3.UnitZ, CubeFace.Right),
        LocalFace(Vec3(-1, 0, 0), Vec3.UnitY, Vec3.UnitZ, CubeFace.Left),
        LocalFace(Vec3(0, 1, 0), Vec3.UnitX, Vec3.UnitZ, CubeFace.Up),
        LocalFace(Vec3(0, -1, 0), Vec3.UnitX, Vec3.UnitZ, CubeFace.Down),
        LocalFace(Vec3(0, 0, 1), Vec3.UnitX, Vec3.UnitY, CubeFace.Front),
        LocalFace(Vec3(0, 0, -1), Vec3.UnitX, Vec3.UnitY, CubeFace.Back),
    )

private const val HALF = 0.5f

private fun Cubelet.toWorld(local: Vec3): Point = (right.toPoint() * local.x.toFloat()) + (up.toPoint() * local.y.toFloat()) + (forward.toPoint() * local.z.toFloat())

private fun Cubelet.quad(
    local: LocalFace,
    turning: Boolean,
    view: (Point, Boolean) -> Point,
): Quad? {
    val normal = view(toWorld(local.normal), turning)
    if (normal.z <= 0f) return null
    val centre = position.toPoint() + (toWorld(local.normal) * HALF)
    val a = toWorld(local.tangentA) * HALF
    val b = toWorld(local.tangentB) * HALF
    val corners = listOf(centre + a + b, centre + a + (b * -1f), centre + (a * -1f) + (b * -1f), centre + (a * -1f) + b).map { view(it, turning) }
    val isSticker = local.normal.x * home.x + local.normal.y * home.y + local.normal.z * home.z == 1
    return Quad(
        face = if (isSticker) local.face else CubeFace.Inner,
        xs = FloatArray(4) { corners[it].x },
        ys = FloatArray(4) { -corners[it].y },
        depth = view(centre, turning).z,
    )
}