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

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.abs

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CubeMarksTest {
    private val outer = CubeFace.entries - CubeFace.Inner

    @Test
    fun `every outer face carries a mark, and the inside none`() {
        outer.forEach { assertNotNull(it.name, CubeMarks.of(it)) }
        assertNull(CubeMarks.of(CubeFace.Inner))
    }

    @Test
    fun `each mark fits its box, centred on the face, and fills it along one side`() {
        val across = (CubeMark.FACE - CubeMark.FIT_WIDTH) / 2 - TOLERANCE
        val down = (CubeMark.FACE - CubeMark.FIT_HEIGHT) / 2 - TOLERANCE
        outer.forEach { face ->
            val layers = CubeMarks.of(face)!!.layers
            val left = layers.minOf { it.bounds.left }
            val right = layers.maxOf { it.bounds.right }
            val top = layers.minOf { it.bounds.top }
            val bottom = layers.maxOf { it.bounds.bottom }

            assertTrue("$face across", left >= across && right <= CubeMark.FACE - across)
            assertTrue("$face down", top >= down && bottom <= CubeMark.FACE - down)
            assertEquals("$face centred across", left, CubeMark.FACE - right, CENTRING)
            assertEquals("$face centred down", top, CubeMark.FACE - bottom, CENTRING)
            assertTrue("$face filled", right - left > CubeMark.FIT_WIDTH * FILLED || bottom - top > CubeMark.FIT_HEIGHT * FILLED)
        }
    }

    @Test
    fun `each face carries the designers' inks`() {
        val inks =
            mapOf(
                CubeFace.Front to listOf(WHITE, GREEN),
                CubeFace.Right to listOf(WHITE, BLUE),
                CubeFace.Up to listOf(YELLOW),
                CubeFace.Left to listOf(PINK),
                CubeFace.Down to listOf(PURPLE),
                CubeFace.Back to listOf(GREEN),
            )
        inks.forEach { (face, colours) -> assertEquals("$face", colours.map { Color(it) }, CubeMarks.of(face)!!.layers.map { it.color }) }
    }

    @Test
    fun `each piece stays on its own sticker, and every ink lands on one`() {
        outer.forEach { face ->
            CubeMarks.of(face)!!.layers.forEach { layer ->
                assertTrue("$face ${layer.color}", layer.pieces.any { it != null })
                layer.pieces.forEachIndexed { tile, piece ->
                    val box = piece?.getBounds() ?: return@forEachIndexed
                    val column = tile % 3
                    val row = tile / 3
                    assertTrue("$face $tile $box", box.left >= column + BEVEL - TOLERANCE && box.right <= column + 1 - BEVEL + TOLERANCE)
                    assertTrue("$face $tile $box", box.top >= row + BEVEL - TOLERANCE && box.bottom <= row + 1 - BEVEL + TOLERANCE)
                    assertTrue("$face $tile $box", abs(box.width) > 0 && abs(box.height) > 0)
                }
            }
        }
    }

    private companion object {
        const val TOLERANCE = 0.01f
        const val CENTRING = 0.05f
        const val FILLED = 0.95f
        const val WHITE = 0xFFFFFFFF
        const val GREEN = 0xFF00F757
        const val BLUE = 0xFF008BFE
        const val YELLOW = 0xFFFFD600
        const val PINK = 0xFFFF2D6B
        const val PURPLE = 0xFF8A38F5
    }
}