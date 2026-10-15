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

import androidx.compose.ui.util.fastForEach
import kotlin.math.cos
import kotlin.math.sin

internal object RestingPose {
    const val YAW = -35f
    const val PITCH = 25f
    const val ROLL = -12f
    val View = Orientation.euler(YAW, PITCH, ROLL)
}

internal const val CAMERA_DISTANCE = 9f

/** On-screen reach from the cube's centre, in cubelets. */
internal const val RESTING_REACH = 2.64f
internal const val MOVING_REACH = 2.72f

internal enum class CubeFace { Right, Left, Up, Down, Front, Back, Inner }

internal data class Turn(
    val move: CubeMove,
    val degrees: Float,
)

/** A visible face in perspective: corners clockwise from its sticker's top left, [light] from 0 edge-on to 1 facing. */
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

internal fun presenting(face: CubeFace): Orientation {
    val local = LocalFaces.first { it.face == face }
    return Orientation.ofRows(local.right, local.up, local.normal) + RestingPose.View
}

internal fun CubeState.visibleQuads(
    yawDegrees: Float,
    pitchDegrees: Float,
    turn: Turn? = null,
    rollDegrees: Float = 0f,
): List<Quad> = visibleQuads(Orientation.euler(yawDegrees, pitchDegrees, rollDegrees), turn)

/** Visible faces in painting order; a turning layer is painted as one block. */
internal fun CubeState.visibleQuads(
    orientation: Orientation,
    turn: Turn? = null,
): List<Quad> {
    val view = Matrix(orientation.matrix())
    val still = ArrayList<Quad>(VISIBLE_GUESS)
    if (turn == null) {
        cubelets.fastForEach { it.addQuads(view, null, still) }
        still.sortWith(ByDepth)
        return still
    }
    val move = turn.move
    val turned = view * rotation(move.axis, move.sign * turn.degrees)
    val moving = ArrayList<Quad>(VISIBLE_GUESS)
    cubelets.fastForEach { cubelet ->
        val isTurning = cubelet.position[move.axis] == move.layer
        cubelet.addQuads(if (isTurning) turned else view, move, if (isTurning) moving else still)
    }
    still.sortWith(ByDepth)
    moving.sortWith(ByDepth)
    val axisTowardsViewer =
        when (move.axis) {
            Axis.X -> view[2, 0]
            Axis.Y -> view[2, 1]
            Axis.Z -> view[2, 2]
        }
    // The camera is on the moving side only once it is past the cut, half a cubelet out.
    val cameraOnMovingSide = axisTowardsViewer * move.layer * CAMERA_DISTANCE > HALF
    return if (cameraOnMovingSide) still.apply { addAll(moving) } else moving.apply { addAll(still) }
}

private class Matrix(
    val m: FloatArray,
) {
    operator fun get(
        row: Int,
        column: Int,
    ) = m[row * 3 + column]

    operator fun times(o: Matrix): Matrix {
        val r = FloatArray(9)
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                r[i * 3 + j] = this[i, 0] * o[0, j] + this[i, 1] * o[1, j] + this[i, 2] * o[2, j]
            }
        }
        return Matrix(r)
    }

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

private val ByDepth = Comparator<Quad> { a, b -> a.depth.compareTo(b.depth) }

private const val HALF = 0.5f

private const val VISIBLE_GUESS = 48

private val CornerRight = intArrayOf(-1, 1, 1, -1)
private val CornerUp = intArrayOf(1, 1, -1, -1)

private fun Cubelet.addQuads(
    m: Matrix,
    cut: CubeMove?,
    into: MutableList<Quad>,
) {
    LocalFaces.fastForEach { local -> quad(local, m, cut)?.let(into::add) }
}

/** Inner faces are drawn only along a turning layer's cut. */
private fun Cubelet.quad(
    local: LocalFace,
    m: Matrix,
    cut: CubeMove?,
): Quad? {
    val isSticker = local.normal.dot(home) == 1
    val normal = world(local.normal)
    if (!isSticker && (cut == null || 2 * position[cut.axis] + normal[cut.axis] != cut.layer)) return null
    val nx = normal.x.toFloat()
    val ny = normal.y.toFloat()
    val nz = normal.z.toFloat()
    val cx = position.x + nx * HALF
    val cy = position.y + ny * HALF
    val cz = position.z + nz * HALF
    val light = m.z(nx, ny, nz)
    val tiltX = m.x(nx, ny, nz)
    val tiltY = m.y(nx, ny, nz)
    val depth = m.z(cx, cy, cz)
    val towardsCamera = -tiltX * m.x(cx, cy, cz) - tiltY * m.y(cx, cy, cz) + light * (CAMERA_DISTANCE - depth)
    if (towardsCamera <= 0f) return null
    val right = world(local.right)
    val up = world(local.up)
    val rx = right.x * HALF
    val ry = right.y * HALF
    val rz = right.z * HALF
    val ux = up.x * HALF
    val uy = up.y * HALF
    val uz = up.z * HALF
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

private fun Cubelet.world(v: Vec3) =
    Vec3(
        right.x * v.x + up.x * v.y + forward.x * v.z,
        right.y * v.x + up.y * v.y + forward.y * v.z,
        right.z * v.x + up.z * v.y + forward.z * v.z,
    )