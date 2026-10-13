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

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.vector.PathParser
import kotlin.math.min

/** One of the designers' SVGs in `core/designsystem/art/cube`: its viewBox and its shapes per ink. */
internal class CubeMarkArt(
    val width: Float,
    val height: Float,
    val layers: List<Pair<Color, String>>,
)

/**
 * One ink of a mark in face units, where a face is three tiles square: its [bounds], and the piece
 * on each sticker, already cut to the sticker's rounded shape, by `tileRow * 3 + tileColumn`.
 */
internal class MarkLayer(
    val color: Color,
    val bounds: Rect,
    val pieces: List<Path?>,
)

/**
 * [art] fitted into the middle [FIT_WIDTH] × [FIT_HEIGHT] of a face and centred. Wordmarks fill the
 * width and symbols the height. The pieces are cut once, so drawing a sticker never clips.
 */
internal class CubeMark(
    private val art: CubeMarkArt,
) {
    val layers: List<MarkLayer> by lazy(LazyThreadSafetyMode.NONE) {
        val units = min(FIT_WIDTH / art.width, FIT_HEIGHT / art.height)
        val fit =
            Matrix().apply {
                translate((FACE - art.width * units) / 2, (FACE - art.height * units) / 2)
                scale(units, units)
            }
        art.layers.map { (color, data) ->
            val path = PathParser().parsePathString(data).toPath().apply { transform(fit) }
            val pieces =
                List(TILES) { tile ->
                    Path().takeIf { it.op(path, sticker(tile % 3, tile / 3), PathOperation.Intersect) && !it.isEmpty }
                }
            MarkLayer(color, path.getBounds(), pieces)
        }
    }

    companion object {
        const val FACE = 3f
        const val FIT_WIDTH = 2.4f
        const val FIT_HEIGHT = 2.2f
        private const val TILES = 9
    }
}

/** The sticker at [column] and [row] of a face, in face units, as the cube draws it. */
private fun sticker(
    column: Int,
    row: Int,
): Path =
    Path().apply {
        val lo = BEVEL
        val hi = 1f - BEVEL
        moveTo(lo + CORNER, lo)
        lineTo(hi - CORNER, lo)
        quadraticTo(hi, lo, hi, lo + CORNER)
        lineTo(hi, hi - CORNER)
        quadraticTo(hi, hi, hi - CORNER, hi)
        lineTo(lo + CORNER, hi)
        quadraticTo(lo, hi, lo, hi - CORNER)
        lineTo(lo, lo + CORNER)
        quadraticTo(lo, lo, lo + CORNER, lo)
        close()
        translate(Offset(column.toFloat(), row.toFloat()))
    }

/** What each face of the cube carries: the droidcon wordmark in front and the stacks around it. */
internal object CubeMarks {
    private val marks: Map<CubeFace, CubeMark> =
        mapOf(
            CubeFace.Front to CubeMark(CubeArt.DROIDCON),
            CubeFace.Right to CubeMark(CubeArt.FLUTTERCON),
            CubeFace.Up to CubeMark(CubeArt.REACT),
            CubeFace.Left to CubeMark(CubeArt.SWIFT),
            CubeFace.Down to CubeMark(CubeArt.AI),
            CubeFace.Back to CubeMark(CubeArt.KOTLIN),
        )

    fun of(face: CubeFace): CubeMark? = marks[face]
}