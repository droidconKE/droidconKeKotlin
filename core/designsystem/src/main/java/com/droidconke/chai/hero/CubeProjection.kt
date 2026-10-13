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

/** The three-quarter view the cube rests at, rolled a little: front, right and top faces showing. */
internal object RestingPose {
    const val YAW = -35f
    const val PITCH = 25f
    const val ROLL = -12f
    val View = Orientation.euler(YAW, PITCH, ROLL)
}

/** How far the camera is from the cube's centre, in cubelets: near enough to foreshorten it. */
internal const val CAMERA_DISTANCE = 9f

/** Which face of the solved cube a sticker belongs to, or [Inner] for the body between cubelets. */
internal enum class CubeFace { Right, Left, Up, Down, Front, Back, Inner }

/** A layer part-way through [move], [degrees] from where it started. */
internal data class Turn(
    val move: CubeMove,
    val degrees: Float,
)

/**
 * One visible face, in perspective. [xs] and [ys] are its corners in cube units, clockwise from the
 * top left of its logo tile, which is [tileColumn] and [tileRow] of [face]'s logo. [light] runs
 * from 0, edge-on, to 1, facing the viewer, and [tiltX] and [tiltY] are where its normal leans.
 */
internal class Quad(
    val face: CubeFace,
    val tileColumn: Int,
    val tileRow: Int,
    val xs: FloatArray,
    val ys: FloatArray,
    val depth: Float,
    val light: Float,
    val tiltX: Float,
    val tiltY: Float,
)

/** The view of the cube with [face] turned to the viewer, upright, then set at the resting angle. */
internal fun presenting(face: CubeFace): Orientation {
    val local = LocalFaces.first { it.face == face }
    return Orientation.ofRows(local.right, local.up, local.normal) + RestingPose.View
}

/** The faces a viewer sees after yawing, pitching and rolling the cube; see the other overload. */
internal fun CubeState.visibleQuads(
    yawDegrees: Float,
    pitchDegrees: Float,
    turn: Turn? = null,
    rollDegrees: Float = 0f,
): List<Quad> = visibleQuads(Orientation.euler(yawDegrees, pitchDegrees, rollDegrees), turn)

/**
 * The faces a viewer sees, in painting order, with the cube turned to [orientation]. A turning
 * layer is painted as one block, before or after the rest of the cube depending on which side of
 * it faces the viewer.
 */
internal fun CubeState.visibleQuads(
    orientation: Orientation,
    turn: Turn? = null,
): List<Quad> {
    val view = Matrix(orientation.matrix())
    val turned = turn?.let { view * rotation(it.move.axis, it.move.sign * it.degrees) }
    val still = ArrayList<Quad>(VISIBLE_GUESS)
    val moving = ArrayList<Quad>(VISIBLE_GUESS)
    for (cubelet in cubelets) {
        val isTurning = turn != null && cubelet.position[turn.move.axis] == turn.move.layer
        val into = if (isTurning) moving else still
        val m = if (isTurning) turned!! else view
        val forward = cubelet.forward
        LocalFaces.forEach { local -> cubelet.quad(local, m, forward, withInner = turn != null)?.let(into::add) }
    }
    still.sortBy { it.depth }
    if (turn == null) return still
    moving.sortBy { it.depth }
    val axisTowardsViewer =
        when (turn.move.axis) {
            Axis.X -> view[2, 0]
            Axis.Y -> view[2, 1]
            Axis.Z -> view[2, 2]
        }
    val layerFacesViewer = axisTowardsViewer * turn.move.layer > 0f
    return if (layerFacesViewer) still + moving else moving + still
}

/** A row-major 3×3 rotation. */
private class Matrix(
    val m: FloatArray,
) {
    operator fun get(
        row: Int,
        column: Int,
    ) = m[row * 3 + column]

    operator fun times(o: Matrix) = Matrix(FloatArray(9) { i -> (0 until 3).sumOf { k -> (this[i / 3, k] * o[k, i % 3]).toDouble() }.toFloat() })

    fun x(
        x: Float,
        y: Float,
        z: Float,
    ) = m[0] * x + m[1] * y + m[2] * z

    fun y(
        x: Float,
        y: Float,
        z: Float,
    ) = m[3] * x + m[4] * y + m[5] * z

    fun z(
        x: Float,
        y: Float,
        z: Float,
    ) = m[6] * x + m[7] * y + m[8] * z
}

/** Anticlockwise about [axis], seen from its positive end. */
private fun rotation(
    axis: Axis,
    degrees: Float,
): Matrix {
    val r = Math.toRadians(degrees.toDouble())
    val c = cos(r).toFloat()
    val s = sin(r).toFloat()
    return Matrix(
        when (axis) {
            Axis.X -> floatArrayOf(1f, 0f, 0f, 0f, c, -s, 0f, s, c)
            Axis.Y -> floatArrayOf(c, 0f, s, 0f, 1f, 0f, -s, 0f, c)
            Axis.Z -> floatArrayOf(c, -s, 0f, s, c, 0f, 0f, 0f, 1f)
        },
    )
}

/** A face in the cubelet's own frame, with [right] and [up] the way its logo reads from outside. */
private class LocalFace(
    val normal: Vec3,
    val right: Vec3,
    val up: Vec3,
    val face: CubeFace,
)

private val LocalFaces =
    listOf(
        LocalFace(Vec3(1, 0, 0), Vec3(0, 0, -1), Vec3.UnitY, CubeFace.Right),
        LocalFace(Vec3(-1, 0, 0), Vec3.UnitZ, Vec3.UnitY, CubeFace.Left),
        LocalFace(Vec3(0, 1, 0), Vec3.UnitX, Vec3(0, 0, -1), CubeFace.Up),
        LocalFace(Vec3(0, -1, 0), Vec3.UnitX, Vec3.UnitZ, CubeFace.Down),
        LocalFace(Vec3(0, 0, 1), Vec3.UnitX, Vec3.UnitY, CubeFace.Front),
        LocalFace(Vec3(0, 0, -1), Vec3(-1, 0, 0), Vec3.UnitY, CubeFace.Back),
    )

private const val HALF = 0.5f
private const val VISIBLE_GUESS = 96

/** Unit corners of a face, clockwise from top left, as (right, up) multiples of [HALF]. */
private val CornerRight = intArrayOf(-1, 1, 1, -1)
private val CornerUp = intArrayOf(1, 1, -1, -1)

/**
 * [withInner] keeps the faces between cubelets, which only show while a layer turns: at rest the
 * cube is solid, so every one of them is hidden.
 */
private fun Cubelet.quad(
    local: LocalFace,
    m: Matrix,
    f: Vec3,
    withInner: Boolean,
): Quad? {
    val isSticker = local.normal.dot(home) == 1
    if (!isSticker && !withInner) return null
    val nx = worldX(local.normal, f)
    val ny = worldY(local.normal, f)
    val nz = worldZ(local.normal, f)
    val cx = position.x + nx * HALF
    val cy = position.y + ny * HALF
    val cz = position.z + nz * HALF
    val light = m.z(nx, ny, nz)
    val tiltX = m.x(nx, ny, nz)
    val tiltY = m.y(nx, ny, nz)
    val depth = m.z(cx, cy, cz)
    val towardsCamera = -tiltX * m.x(cx, cy, cz) - tiltY * m.y(cx, cy, cz) + light * (CAMERA_DISTANCE - depth)
    if (towardsCamera <= 0f) return null
    val rx = worldX(local.right, f) * HALF
    val ry = worldY(local.right, f) * HALF
    val rz = worldZ(local.right, f) * HALF
    val ux = worldX(local.up, f) * HALF
    val uy = worldY(local.up, f) * HALF
    val uz = worldZ(local.up, f) * HALF
    val xs = FloatArray(4)
    val ys = FloatArray(4)
    for (i in 0 until 4) {
        val px = cx + rx * CornerRight[i] + ux * CornerUp[i]
        val py = cy + ry * CornerRight[i] + uy * CornerUp[i]
        val pz = cz + rz * CornerRight[i] + uz * CornerUp[i]
        val perspective = CAMERA_DISTANCE / (CAMERA_DISTANCE - m.z(px, py, pz))
        xs[i] = m.x(px, py, pz) * perspective
        ys[i] = -m.y(px, py, pz) * perspective
    }
    return Quad(
        face = if (isSticker) local.face else CubeFace.Inner,
        tileColumn = local.right.dot(home) + 1,
        tileRow = 1 - local.up.dot(home),
        xs = xs,
        ys = ys,
        depth = depth,
        light = light.coerceAtLeast(0f),
        tiltX = tiltX,
        tiltY = tiltY,
    )
}

private fun Vec3.dot(o: Vec3) = x * o.x + y * o.y + z * o.z

// From the cubelet's frame to the world's: right, up and forward ([f]) are the columns.
private fun Cubelet.worldX(
    v: Vec3,
    f: Vec3,
) = (right.x * v.x + up.x * v.y + f.x * v.z).toFloat()

private fun Cubelet.worldY(
    v: Vec3,
    f: Vec3,
) = (right.y * v.x + up.y * v.y + f.y * v.z).toFloat()

private fun Cubelet.worldZ(
    v: Vec3,
    f: Vec3,
) = (right.z * v.x + up.z * v.y + f.z * v.z).toFloat()