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

import android.graphics.Matrix
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.util.fastAll
import androidx.compose.ui.util.fastForEach
import com.droidconke.chai.colors.CubeEdgeColor
import com.droidconke.chai.colors.CubeGapColor
import com.droidconke.chai.colors.CubeSheenColor
import com.droidconke.chai.colors.CubeTileLitColor
import com.droidconke.chai.colors.CubeTileShadowColor
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

/** Draws each sticker as the unit square under its quad's projective transform, allocating nothing per sticker. */
internal class CubePainter {
    private val cell = Path().apply { addRect(Rect(0f, 0f, 1f, 1f)) }
    private val tile = stickerPath(0, 0)
    private val sheen = Brush.linearGradient(listOf(CubeSheenColor, Color.Transparent), Offset.Zero, Offset(1f, 1f))
    private val edge = Stroke(width = EDGE_WIDTH)
    private val transform = Matrix()
    private val values = FloatArray(9)

    fun DrawScope.drawFace(
        quad: Quad,
        origin: Offset,
        scale: Float,
    ) {
        if (!quad.homography(origin, scale, values)) return
        transform.setValues(values)
        val canvas = drawContext.canvas.nativeCanvas
        val checkpoint = canvas.save()
        canvas.concat(transform)
        drawPath(cell, CubeGapColor)
        drawPath(tile, lerp(CubeTileShadowColor, CubeTileLitColor, quad.light))
        val shine = quad.shine()
        drawPath(tile, sheen, alpha = SHEEN_ALPHA * shine)
        CubeMarks.of(quad.face)?.layersIfCut?.let { drawPieces(quad, it) }
        drawPath(tile, CubeEdgeColor, alpha = EDGE_ALPHA + EDGE_SHINE_ALPHA * shine, style = edge)
        canvas.restoreToCount(checkpoint)
    }

    private fun DrawScope.drawPieces(
        quad: Quad,
        layers: List<MarkLayer>,
    ) {
        val tile = quad.tileRow * 3 + quad.tileColumn
        if (layers.fastAll { it.pieces[tile] == null }) return
        val canvas = drawContext.canvas.nativeCanvas
        val checkpoint = canvas.save()
        canvas.translate(-quad.tileColumn.toFloat(), -quad.tileRow.toFloat())
        layers.fastForEach { layer -> layer.pieces[tile]?.let { drawPath(it, layer.color) } }
        canvas.restoreToCount(checkpoint)
    }
}

/** The unit square's projective map onto this quad, row-major in [out]; false for a degenerate quad. */
private fun Quad.homography(
    origin: Offset,
    scale: Float,
    out: FloatArray,
): Boolean {
    val x0 = origin.x + xs[0] * scale
    val x1 = origin.x + xs[1] * scale
    val x2 = origin.x + xs[2] * scale
    val x3 = origin.x + xs[3] * scale
    val y0 = origin.y + ys[0] * scale
    val y1 = origin.y + ys[1] * scale
    val y2 = origin.y + ys[2] * scale
    val y3 = origin.y + ys[3] * scale
    val dx1 = x1 - x2
    val dx2 = x3 - x2
    val dy1 = y1 - y2
    val dy2 = y3 - y2
    val det = dx1 * dy2 - dx2 * dy1
    if (abs(det) < THIN) return false
    val sx = x0 - x1 + x2 - x3
    val sy = y0 - y1 + y2 - y3
    val g = (sx * dy2 - dx2 * sy) / det
    val h = (dx1 * sy - sx * dy1) / det
    out[0] = x1 - x0 + g * x1
    out[1] = x3 - x0 + h * x3
    out[2] = x0
    out[3] = y1 - y0 + g * y1
    out[4] = y3 - y0 + h * y3
    out[5] = y0
    out[6] = g
    out[7] = h
    out[8] = 1f
    return true
}

private fun Quad.shine(): Float {
    val h = LightX * tiltX + LightY * tiltY + LightZ * light
    return max(0f, h).pow(SHINE_POWER)
}

private val LightLength = sqrt(LIGHT_X * LIGHT_X + LIGHT_Y * LIGHT_Y + (LIGHT_Z + 1f) * (LIGHT_Z + 1f))
private val LightX = LIGHT_X / LightLength
private val LightY = LIGHT_Y / LightLength
private val LightZ = (LIGHT_Z + 1f) / LightLength

private const val THIN = 1e-4f
private const val EDGE_WIDTH = 0.012f
private const val EDGE_ALPHA = 0.18f
private const val EDGE_SHINE_ALPHA = 0.6f
private const val SHEEN_ALPHA = 0.7f
private const val SHINE_POWER = 4f
private const val LIGHT_X = -0.45f
private const val LIGHT_Y = 0.65f
private const val LIGHT_Z = 0.6f