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

import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** How the whole cube is turned in front of the viewer, as a unit quaternion. */
internal data class Orientation(
    val w: Float,
    val x: Float,
    val y: Float,
    val z: Float,
) {
    /** This turn followed by [then]. */
    operator fun plus(then: Orientation) =
        Orientation(
            w = then.w * w - then.x * x - then.y * y - then.z * z,
            x = then.w * x + then.x * w + then.y * z - then.z * y,
            y = then.w * y - then.x * z + then.y * w + then.z * x,
            z = then.w * z + then.x * y - then.y * x + then.z * w,
        )

    /** The row-major 3×3 rotation matrix. */
    fun matrix() =
        floatArrayOf(
            1 - 2 * (y * y + z * z),
            2 * (x * y - w * z),
            2 * (x * z + w * y),
            2 * (x * y + w * z),
            1 - 2 * (x * x + z * z),
            2 * (y * z - w * x),
            2 * (x * z - w * y),
            2 * (y * z + w * x),
            1 - 2 * (x * x + y * y),
        )

    /** The shortest way round from this orientation to [to], [fraction] of the way. */
    fun towards(
        to: Orientation,
        fraction: Float,
    ): Orientation {
        var dot = w * to.w + x * to.x + y * to.y + z * to.z
        val sign = if (dot < 0f) -1f else 1f
        dot *= sign
        if (dot > NEARLY_SAME) {
            return Orientation(
                w + (sign * to.w - w) * fraction,
                x + (sign * to.x - x) * fraction,
                y + (sign * to.y - y) * fraction,
                z + (sign * to.z - z) * fraction,
            ).normalised()
        }
        val angle = acos(dot)
        val from = sin((1 - fraction) * angle) / sin(angle)
        val towards = sign * sin(fraction * angle) / sin(angle)
        return Orientation(from * w + towards * to.w, from * x + towards * to.x, from * y + towards * to.y, from * z + towards * to.z)
    }

    private fun normalised(): Orientation {
        val length = sqrt(w * w + x * x + y * y + z * z)
        return Orientation(w / length, x / length, y / length, z / length)
    }

    companion object {
        /** Anticlockwise about [axis], seen from its positive end. */
        fun about(
            axis: Axis,
            degrees: Float,
        ): Orientation {
            val half = Math.toRadians(degrees.toDouble()).toFloat() / 2
            val s = sin(half)
            return when (axis) {
                Axis.X -> Orientation(cos(half), s, 0f, 0f)
                Axis.Y -> Orientation(cos(half), 0f, s, 0f)
                Axis.Z -> Orientation(cos(half), 0f, 0f, s)
            }
        }

        /** Yawed about the vertical, then pitched towards the viewer, then rolled about the line of sight. */
        fun euler(
            yawDegrees: Float,
            pitchDegrees: Float,
            rollDegrees: Float = 0f,
        ) = about(Axis.Y, yawDegrees) + about(Axis.X, pitchDegrees) + about(Axis.Z, rollDegrees)

        /** The rotation whose matrix has [right], [up] and [forward] as its rows. */
        fun ofRows(
            right: Vec3,
            up: Vec3,
            forward: Vec3,
        ): Orientation {
            val m00 = right.x.toFloat()
            val m11 = up.y.toFloat()
            val m22 = forward.z.toFloat()
            val trace = m00 + m11 + m22
            val q =
                when {
                    trace > 0f -> {
                        val s = sqrt(trace + 1f) * 2
                        Orientation(s / 4, (forward.y - up.z) / s, (right.z - forward.x) / s, (up.x - right.y) / s)
                    }
                    m00 >= m11 && m00 >= m22 -> {
                        val s = sqrt(1f + m00 - m11 - m22) * 2
                        Orientation((forward.y - up.z) / s, s / 4, (right.y + up.x) / s, (right.z + forward.x) / s)
                    }
                    m11 >= m22 -> {
                        val s = sqrt(1f + m11 - m00 - m22) * 2
                        Orientation((right.z - forward.x) / s, (right.y + up.x) / s, s / 4, (up.z + forward.y) / s)
                    }
                    else -> {
                        val s = sqrt(1f + m22 - m00 - m11) * 2
                        Orientation((up.x - right.y) / s, (right.z + forward.x) / s, (up.z + forward.y) / s, s / 4)
                    }
                }
            return q.normalised()
        }
    }
}

private const val NEARLY_SAME = 0.9995f