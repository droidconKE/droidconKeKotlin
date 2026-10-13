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

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.PathParser
import kotlin.math.min

/** One of the designers' SVGs in `core/designsystem/art/cube`: its viewBox and its shapes per ink. */
internal class CubeMarkArt(
    val width: Float,
    val height: Float,
    val layers: List<Pair<Color, String>>,
)

/** One ink of a mark, and its bounds in face units, where a face is three tiles square. */
internal class MarkLayer(
    val color: Color,
    val path: Path,
    val bounds: Rect,
)

/**
 * [art] fitted into the middle [FIT_WIDTH] × [FIT_HEIGHT] of a face and centred, at [units] face
 * units per SVG unit. Wordmarks fill the width and symbols the height.
 */
internal class CubeMark(
    art: CubeMarkArt,
) {
    val units: Float = min(FIT_WIDTH / art.width, FIT_HEIGHT / art.height)
    val left: Float = (FACE - art.width * units) / 2
    val top: Float = (FACE - art.height * units) / 2

    val layers: List<MarkLayer> by lazy {
        art.layers.map { (color, data) ->
            val path = PathParser().parsePathString(data).toPath()
            val box = path.getBounds()
            MarkLayer(color, path, Rect(left + box.left * units, top + box.top * units, left + box.right * units, top + box.bottom * units))
        }
    }

    companion object {
        const val FACE = 3f
        const val FIT_WIDTH = 2.4f
        const val FIT_HEIGHT = 2.2f
    }
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